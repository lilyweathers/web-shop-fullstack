package se.iths.webshop;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.model.Cart;
import se.iths.webshop.business.model.CartItem;

public class CartTest {

    private List<Product> products = List.of(
            new Product("iPhone", "Phone", 11000.0, "The latest iPhone model",""),
            new Product("MacBook", "Laptop", 24000.0, "The latest MacBook","")
    );

    private List<CartItem> mockingRepository = List.of(
            new CartItem(products.get(0), 1),
            new CartItem(products.get(1), 2)
    );
    private Cart cart = new Cart(mockingRepository);

    @Order(1)
    @Test
    public void shouldAddProduct() {
        Product product = new Product("Galaxy 22", "Phone", 9000.0, "The latest Samsung Galaxy", "");

        cart.addProduct(product, 2);

        assertEquals(3, cart.getItems().size());
    }

    @Order(2)
    @Test
    public void shouldReturnTotal() {
        assertEquals(59000.0, cart.getTotal());
    }

    @Order(3)
    @Test
    public void shouldUpdateQuantity() {
        cart.updateCartItem("iPhone", 3);

        assertEquals(81000.0, cart.getTotal());
    }

    @Order(4)
    @Test
    public void shouldRemoveProduct() {
        cart.updateCartItem("iPhone", 0);

        assertEquals(1, cart.getItems().size());
    }

    @Order(5)
    @Test
    public void shouldFindByName() {
        assertEquals(products.get(0).getName(), cart.findByName("IPHONE").get().getProduct().getName());
    }

    @Order(6)
    @Test
    public void shouldFindByProduct() {
        assertEquals(products.get(0), cart.findByProduct(products.get(0)).get().getProduct());
    }
}
