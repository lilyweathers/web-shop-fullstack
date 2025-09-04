package se.iths.webshop.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import se.iths.webshop.business.entity.*;
import se.iths.webshop.business.model.Cart;
import se.iths.webshop.business.model.CartItem;
import se.iths.webshop.data.repository.OrderRepository;
import se.iths.webshop.data.repository.PersonRepository;
import se.iths.webshop.data.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope
public class WebShopService {
    @Autowired
    PersonRepository personRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    public Cart cart;

    Person user;

    public WebShopService() {
        cart = new Cart();
    }

    public WebShopService(PersonRepository personRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.personRepository = personRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public List<Person> getUsers() {
        return personRepository.findAll();
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public List<CartItem> getCartItems() {
        return cart.getItems();
    }

    public Product getProduct(long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        return optionalProduct.orElse(null);
    }

    public CustomerOrder getOrder(long id) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(id);
        return optionalOrder.orElse(null);
    }

    public List<CustomerOrder> getUserOrders() {
        return ((Customer) user).getOrders();
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (!categories.contains(product.getCategory())) {
                categories.add(product.getCategory());
            }
        }
        return categories;
    }

    public List<Product> searchProductsByName(String text) {
        return productRepository.findAll().stream().filter(product -> product.getName().toLowerCase().contains(text.toLowerCase())).toList();
    }

    public List<Product> searchProductsByCategory(String category) {
        return productRepository.findAll().stream().filter(product -> product.getCategory().toLowerCase().contains(category.toLowerCase())).toList();
    }

    public Person getUser() {
        return user;
    }

    public Cart getCart() {
        return cart;
    }

    public String registerAdmin(Employee employee) {
        Optional<Person> optionalPerson = personRepository.findByEmail(employee.getEmail());
        if (optionalPerson.isEmpty()) {
            user = personRepository.save(employee);
            return "Account created!";

        } else return "Account already exists!";
    }

    public String registerUser(Customer customer) {
        Optional<Person> optionalPerson = personRepository.findByEmail(customer.getEmail());
        if (optionalPerson.isEmpty()) {
                user = personRepository.save(customer);
                return "Account created!";

        } else return "Account already exists!";
    }

    public String loginUser(String email, String password) {
        Optional<Person> optionalPerson = personRepository.findByEmailAndPassword(email, password);
        if (optionalPerson.isPresent()  && optionalPerson.get() instanceof Customer) {
            if (optionalPerson.get().getPassword().equals(password)) {
                user = optionalPerson.get();
                return optionalPerson.get().getName();
            }
        }
        return "Wrong password or email!";
    }

    public String loginAdmin(String email, String password) {
        Optional<Person> optionalPerson = personRepository.findByEmailAndPassword(email, password);
        if (optionalPerson.isPresent() && optionalPerson.get() instanceof Employee) {
            if (optionalPerson.get().getPassword().equals(password)) {
                user = optionalPerson.get();
                return "Admin";
            }
        }
        return "Wrong password or email!";
    }

    public String addProductToCart(long id, int count) {
        Product product = getProduct(id);
        if (cart.addProduct(product, count).isPresent()) {
            return product.getName() + " added to cart!";
        }
        return "Product already in cart!";
    }

    public void updateCartItem(String name, int count) {
        cart.updateCartItem(name, count);
    }

    public double getCartTotal() {
        return cart.getTotal();
    }
    public String clearCart(){
        cart.empty();
        return "Cart emptied";
    }

    public String logoutUser() {
        user = null;
        return "Logged out!";
    }

    public String addProduct(Product product) {
        productRepository.save(product);
        return product.getName() + " added!";
    }

    public String removeProduct(long id) {
        productRepository.delete(productRepository.findById(id).get());
        return "Product " + id + " was deleted!";
    }

    public CustomerOrder checkout() {;
        ArrayList list = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            OrderLine orderLine = new OrderLine(product.getName(), product.getCategory(), product.getPrice(), cartItem.getCount());
            list.add(orderLine);
        }
        CustomerOrder order = new CustomerOrder();
        order.setItems(list);
        order.setCustomer((Customer) user);
        ((Customer) user).addOrder(order);
        orderRepository.save(order);
        user = personRepository.save(user);
        return order;
    }

    public List<CustomerOrder> getPendingOrders() {
        return orderRepository.findAll().stream().filter(order -> !order.isConfirmed() && !order.isProcessed() && !order.isCanceled()).toList();
    }

    public List<CustomerOrder> getConfirmedOrders() {
        return orderRepository.findAll().stream().filter(order -> order.isConfirmed() && !order.isProcessed() && !order.isCanceled()).toList();
    }

    public List<CustomerOrder> getDispatchedOrders() {
        return orderRepository.findAll().stream().filter(order -> !order.isConfirmed() && order.isProcessed() && !order.isCanceled()).toList();
    }

    public List<CustomerOrder> getCanceledOrders() {
        return orderRepository.findAll().stream().filter(order -> !order.isConfirmed() && !order.isProcessed() && order.isCanceled()).toList();
    }

    public void confirmOrder(long id) {
        CustomerOrder order = getOrder(id);
        order.setProcessed(false);
        order.setConfirmed(true);
        orderRepository.save(order);

    }

    public void processOrder(long id) {
        CustomerOrder order = getOrder(id);
        order.setConfirmed(false);
        order.setProcessed(true);
        orderRepository.save(order);

    }

    public void cancelOrder(long id) {
        CustomerOrder order = getOrder(id);
        order.setConfirmed(false);
        order.setProcessed(false);
        order.setCanceled(true);
        orderRepository.save(order);

    }

    // REST CRUD Operations

    public Product createProduct (Product product) {
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(long id){
        return productRepository.findById(id).get();
    }

    public List<Product> getProductsByName(String text) {
        return productRepository.findAll().stream().filter(product -> product.getName().toLowerCase().contains(text.toLowerCase())).toList();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findAll().stream().filter(product -> product.getCategory().toLowerCase().contains(category.toLowerCase())).toList();
    }

    public Product updateProductName(long id, String name){
        Product product = productRepository.findById(id).get();
        product.setName(name);
        return productRepository.save(product);
    }

    public Product updatedProductCategory(long id, String category) {
        Product product = productRepository.findById(id).get();
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product updateProductPrice(long id, double price){
        Product product = productRepository.findById(id).get();
        product.setPrice(price);
        return productRepository.save(product);
    }

    public Product updateProductDescription(long id, String description){
        Product product = productRepository.findById(id).get();
        product.setDescription(description);
        return productRepository.save(product);
    }
    public List<Product> deleteProduct(long id) {
        productRepository.delete(productRepository.findById(id).get());
        return productRepository.findAll();
    }
}
