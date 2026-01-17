package cs520.TermProject;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;

//represents a single tote in the warehouse
public class Tote {

	//instantiating the max capacity of 10 products for each tote
	public static final int max_capacity = 10;

	//declaring id for the tote. 
	//totes number 0-99 becuase there cannot be more than 100 totes in the warehouse 
	private final int id;
	//the upc stored in the tote. 
	//null when the tote is empty or not assigned 
	private String upc; 
	//list of product objects 
	//using an Array because: 
	// -- fixed length, index-based placement, forced capacity checks
	private final ArrayList<Product> items;

	//creates a tote with a unique ID
	//the tote is created empty, meaning no UPC yet
	public Tote(int id) {
		this.id = id;
		this.upc = null;
		this.items = new ArrayList<>();
	}

	//returns the tote's ID
	public int getId() {
	    return id;
	}

	//returns the UPC stored in this tote. may be null if empty
	public String getUPC() {
	    return upc;
	}

//returns current number of products in this tote
public int size() {
	return items.size();
}
	
//checks if tote has products in it. true if no products
public boolean isEmpty() {
	return items.isEmpty();
}
	
//checks if totes is meximum capacity. true if yes 
public boolean isFull() {
	return items.size() == max_capacity;
}
	
//checks if this tote can store the given UPC: 
// -- if tote is empty, it can store any UPC
// -- if tote contains a UPC, it can only store a product of the same UPC
// -- if tote is full, it cannot store anything more 
public boolean canStore(String upc) {
	return !isFull() &&
	// || is the or operator 
	(this.upc == null || this.upc.equals(upc));
}

//method to add product to this tote. 
//if tote empty assign its UPC, if full throw an excpetion =)
public void addProduct(Product p) {
	if (isFull()) {
		throw new IllegalStateException("Tote " + id + " is full");
	}
	if (isEmpty()) {
	    this.upc = p.getUPC();
	}
	items.add(p);
}

//remove and return a product (LIFO) = (last in first out) from this tote. 
//clears UPC if tote becomes empty. returns null if tote is empty.    
public Product removeProduct() {
	if (isEmpty()) 
		return null;
	
	Product p = items.remove(items.size() - 1);
	
	if (items.isEmpty()) {
	//clear UPC when this tote becomes empty
	    this.upc = null;
	}
	return p;
}

	   
//remove all items and return them as a list- clears UPC 
// --this is used during merging
//the returned list will contain the product objects previously stored in this tote
public List<Product> removeAllProducts() {
	ArrayList<Product> copy = new ArrayList<>(items);
	items.clear();
	this.upc = null;
	return copy;
}

//add up to 'n' products from source list starting at index 'startIdx'. returns number added.
//used by merge logic.
	    
public int fillFromBuffer(List<Product> buffer, int startIdx) {
	int added = 0;
	if (buffer == null) 
		return 0;
	
	while (!isFull() && startIdx + added < buffer.size()) {
		Product p = buffer.get(startIdx + added);
	    //if this tote is empty, set its upc from the product
	    if (isEmpty()) 
	    	this.upc = p.getUPC();
	    items.add(p);
	    added++;
	 }
	 return added;
}

@Override
public String toString() {
	return "Tote(" + id + ", upc=" + upc + ", size=" + size() + ")";
	}
}
