package se.iths.webshop.business.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<OrderLine> items;
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    private boolean confirmed;
    private boolean processed;
    private boolean canceled;

    public CustomerOrder() {
        confirmed = false;
        processed = false;
    }

    public String getOrderItemsAsString() {
        StringBuilder sb = new StringBuilder("");
        for (OrderLine orderItem : items) {
            sb.append(orderItem.getName()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    public String getName() {
        return "Order no. " + getId();
    }
    public String getTotalPrice() {
        double sum = 0;
        for (OrderLine orderItem : items) {
            sum = sum + orderItem.getPrice();
        }
        return "Total price: " + sum + " SEK";
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public void setItems(List<OrderLine> items) {
        this.items = items;
    }

    public void addItem(OrderLine orderItem) {
        items.add(orderItem);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
