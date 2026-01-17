package cs520.TermProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Warehouse implements IWarehouse {

	//max number of totes
    private static final int max_totes = 100;

    //array storing 0–99 tote objects
    private final Tote[] totes;

    //number of totes currently created (0..100)
    private int toteCount;
    
    //mapping the UPCs. list of totes that contian that UPC 
    // --this is used later for merging
    //example: "1082747929", [1,2,4,8,10]
    private final Map<String, List<Integer>> toteMap;
    
    private Random random; 
    //initializing an empty warehouse 
    public Warehouse() {
    	totes = new Tote[max_totes];
    	toteCount = max_totes; //determining that all 100 totes exist, even if some are empty
    	toteMap = new HashMap<>();
    	random = new Random();
    	
    	for (int i = 0; i < max_totes; i++) {
    		totes[i] = new Tote(i);
    	}
    }
    
//loading products from the text file Products.txt 
@Override
public void LoadProducts(String filename) {
	System.out.println("Loading products...");
    	
	try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
    			
		String line;

        while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) 
        	continue;

        String[] parts = line.split(",", 2);
        String upc = parts[0].trim();
        String desc = parts.length > 1 ? parts[1] : ""; //explain this line 

        Product p = new Product(upc, desc);

        storeProductRoundRobin(p); //and this one 
     }

     } catch (IOException e) {
    	 System.out.println("Error reading file: " + filename);
     }

      System.out.println("Loading complete. \n");
}
    
//places product in correct tote
private void storeProductRoundRobin(Product p) {
	String upc = p. getUPC();
    	
	//list of totes already storing this UPC  
    List<Integer> list = toteMap.get(upc);
    	
    //if UPC already has totes, place product in a totes that's not full
    if (list != null) {
    	for(int id : list) {
    		Tote t = totes[id];
    		if (!t.isFull()) {
    			t.addProduct(p);
    			return;
    			}
    		}
    	}
    	
    //try to store in an empty tote or partially filled tote 
    for (int i = 0; i < toteCount; i++) {
        Tote t = totes[i];
        if (t.canStore(upc)) { //canStore handles empty tote case 
        	t.addProduct(p);
        
        	//if it's a new UPC for this tote, update the map
        	toteMap.computeIfAbsent(upc, k -> new ArrayList<>());
        	if (!toteMap.get(upc).contains(i)) {
        		toteMap.get(upc).add(i);
        		System.out.println("Used additional tote (" + i + ") for UPC: " + upc);
        	}
        	return;
        }
    }
    
	//if no tote is available
    if (toteCount >= max_totes) {
    	throw new IllegalStateException("Warehouse tote limit reached, no empty tote available.");
    }
}
//fulfill orders 
@Override
public void FulfillOrders(String filename) {
    System.out.println("\nFulfilling orders...");
    	
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
    		
    	String line;
    		
    	while ((line = br.readLine()) != null) {
    			
    	if (line.trim().isEmpty()) 
    		continue;
    			
    	String[] parts = line.split(",");
    	String orderNum = parts[0].trim();
    	System.out.println("\nOrder fulfillment started: Order " + orderNum);
    			
    	List<String> upcs = new ArrayList<>();
    	for (int i = 1; i < parts.length; i++) {
    		String u = parts[i].trim();
    		if (!u.isEmpty()) upcs.add(u);
    	}
    			
    	List<String> retrieved = new ArrayList<>();
    			
    	for (String upc : upcs) {
    		retrieveProduct(upc, retrieved);
    	}
    			
    	//print final order output 
    	System.out.print("Order fulfilled--> Order " + orderNum);
    	
    			
    	for (String upc : retrieved) {
    		System.out.print(", " + upc);
    	}
    	System.out.println();	
    	}
    } catch (IOException e) {
    	System.out.println("Error reading file: " + filename);
    }
    	
    System.out.println("\nOrders complete. \n");
}
    	
//retrieve product from a random tote containing that UPC
private void retrieveProduct(String upc, List<String> retrievedList) {
	List<Integer> list = toteMap.get(upc);
    		
    if(list == null || list.isEmpty()) 
    	return;
    		
    //pick random tote index
    int index = random.nextInt(list.size());
    int toteId = list.get(index);
    Tote t = totes[toteId];
    		
    //remove product from tote
    t.removeProduct();
    		
    System.out.println("Retrieving product from tote (" + toteId + ") for UPC: " + upc);
    		
    retrievedList.add(upc);
    		
    //if tote becomes empty, remove tote from list 
    if (t.isEmpty()) {
    	list.remove(Integer.valueOf(toteId));
    	if (list.isEmpty()) 
    		toteMap.remove(upc);
    }
 }
  
@Override
public boolean MergeTotes() {
    System.out.println("\nMerging partially filled totes...");

    for (String upc : new ArrayList<>(toteMap.keySet())) {
        List<Integer> toteIds = toteMap.get(upc);
        if (toteIds == null || toteIds.size() <= 1)
            continue;

        List<Product> buffer = new ArrayList<>();
        List<Integer> partialTotes = new ArrayList<>();

        //collect products from partially filled totes only
        for (int id : toteIds) {
            Tote t = totes[id];
            if (!t.isFull() && !t.isEmpty()) {
                buffer.addAll(t.removeAllProducts());
                partialTotes.add(id);
            }
        }

        //remove partial totes from the map temporarily
        toteIds.removeAll(partialTotes);

        int idx = 0;

        //refill the same partial totes first
        for (int id : partialTotes) {
            if (idx >= buffer.size())
                break;
            Tote t = totes[id];
            int added = t.fillFromBuffer(buffer, idx);
            if (added > 0) {
                toteIds.add(id);
                idx += added;
            }
        }

        //if products remain, place them in the first available partially empty tote
        for (int id : partialTotes) {
            if (idx >= buffer.size())
                break;
            Tote t = totes[id];
            if (!t.isFull()) {
                int added = t.fillFromBuffer(buffer, idx);
                if (added > 0) {
                    idx += added;
                }
            }
        }

        //only use empty totes if absolutely necessary
        for (int i = 0; i < totes.length && idx < buffer.size(); i++) {
            Tote t = totes[i];
            if (t.isEmpty()) {
                int added = t.fillFromBuffer(buffer, idx);
                if (added > 0) {
                    toteIds.add(i);
                    idx += added;
                }
            }
        }
    }

    System.out.println("Merge complete. \n");
    return true;
}



//display details, formatted correctly
@Override
public void DisplayDetails() {

	int full = 0, partial = 0, empty = 0;

    for (int i = 0; i < toteCount; i++) {
    	Tote t = totes[i];
        if (t.isEmpty()) 
        	empty++;
        else if (t.isFull()) 
        	full++;
        else partial++;
     }

     System.out.println("Warehouse details: \n");
     System.out.println("   " + full + " full totes, " + partial + " partially filled totes, and " + empty + " empty totes \n");

     System.out.println("   UPC             # Totes");
     System.out.println("   --------------- -------");

     for (String upc : toteMap.keySet()) {
    	 System.out.printf("   %-15s %d%n", upc, toteMap.get(upc).size()); 
     }
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    }
}
