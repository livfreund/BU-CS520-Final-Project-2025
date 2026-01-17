package cs520.TermProject;

public class Test {

	public static void main(String[] args) {
		
		IWarehouse w = new Warehouse(); 
		   
		w.LoadProducts("Products.txt"); 
		w.DisplayDetails(); 
		   
		w.FulfillOrders("Orders.txt"); 
		w.DisplayDetails(); 
		   
		w.MergeTotes(); 
		w.DisplayDetails(); 
		
	}
}

