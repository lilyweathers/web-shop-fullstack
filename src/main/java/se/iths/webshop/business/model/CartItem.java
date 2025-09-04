package se.iths.webshop.business.model;

import se.iths.webshop.business.entity.Product;

public class CartItem {

    Product product;
    int count;

    public CartItem() {}

    public CartItem(Product product, int count) {
        this.product = product;
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return count + " " + product.getName() + " " + getPrice();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPrice() {
        return product.getPrice() * count;
    }
}
