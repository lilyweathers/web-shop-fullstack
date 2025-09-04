package se.iths.webshop.business.entity;

import jakarta.persistence.*;

@Entity
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    String product;
    String category;
    double price;
    int count;

    public OrderLine() {}

    public OrderLine(String product, String category, double price, int count) {
        this.product = product;
        this.category = category;
        this.price = price;
        this.count = count;
    }

    public String getProduct() {
        return product;
    }

    public int getCount() {
        return count;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return count + " " + product + " " + getPrice();
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPrice() {
        return price * count;
    }

    public String getCategory() {
        return category;
    }
}
