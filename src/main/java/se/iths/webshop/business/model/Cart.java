package se.iths.webshop.business.model;

import se.iths.webshop.business.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {

    List<CartItem> items;

    public Cart () {
        this.items = new ArrayList<>();
    }

    public Cart (List<CartItem> list) {
        this.items = new ArrayList<>();
        items.addAll(list);
    }
    public Optional<CartItem> findByName(String name) {
        for (CartItem cartItem : items) {
            Product product = cartItem.getProduct();
            if (product.getName().toLowerCase().equals(name.toLowerCase())) {
                return Optional.of(cartItem);
            }
        }
        return Optional.empty();
    }

    public Optional<CartItem> findByProduct(Product product) {
        for (CartItem cartItem : items) {
            Product foundProduct = cartItem.getProduct();
            if (product.equals(foundProduct)) {
                return Optional.of(cartItem);
            }
        }
        return Optional.empty();
    }

    public Optional<Product> addProduct(Product product, int count) {
        Optional<CartItem> orderLine = findByProduct(product);
        if (orderLine.isEmpty()) {
            getItems().add(new CartItem(product, count));
            return Optional.of(product);
        }
        return Optional.empty();
    }

    public void updateCartItem(String name, int count) {
        Optional<CartItem> orderLine = findByName(name);
        if (orderLine.isPresent()) {
            if (count == 0) {
                getItems().remove(orderLine.get());
            } else {
                orderLine.get().setCount(count);
            }
        }
    }

    public double getTotal() {
        double sum = 0;
        for (CartItem cartItem : getItems()) {
            sum = sum + cartItem.getPrice();
        }
        return sum;
    }

    public List<CartItem> getItems () {
        return items;
    }

    public void empty() {
        items.clear();
    }
}
