package cs520.TermProject;

//represents a single product stored inside the warehouse. 
public class Product {

	//declaring variables. final because they will not be changing
	//upc = universal product code
	private final String upc;
	//what the product is 
    private final String description;

    public Product(String upc, String description) {
        this.upc = upc;
        this.description = description;
    }

    //returns the upc for this product
    public String getUPC() {
        return upc;
    }

    //returns the description for this product
    public String getDescription() {
        return description;
    }

    //returns example: "079400242907 (Shampoo)" for better readability
    @Override
    public String toString() {
        return upc + " (" + description + ")";
    }
}

