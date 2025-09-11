package se.iths.webshop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.entity.Person;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.service.WebShopService;
import se.iths.webshop.data.repository.OrderRepository;
import se.iths.webshop.data.repository.PersonRepository;
import se.iths.webshop.data.repository.ProductRepository;
import se.iths.webshop.data.repository.implementation.OrderRepositoryImplementation;
import se.iths.webshop.data.repository.implementation.PersonRepositoryImplementation;
import se.iths.webshop.data.repository.implementation.ProductRepositoryImplementation;

public class WebShopServiceTest {

    @Mock
    private PersonRepository personRepository = mock(PersonRepositoryImplementation.class);
    @Mock
    private ProductRepository productRepository = mock(ProductRepositoryImplementation.class);
    @Mock
    private OrderRepository orderRepository = mock(OrderRepositoryImplementation.class);
    private WebShopService webShopService = new WebShopService(personRepository, productRepository, orderRepository);


    @Test
    public void shouldRegisterCustomer() {
        String registration = webShopService.registerUser(new Customer("Customer", "test@test.com", "test1"));

        assertEquals("Account created!", registration);
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    public void shouldRegisterEmployee() {
        String registration = webShopService.registerAdmin(new Employee("Admin", "admin@admin.com", "admin"));

        assertEquals("Account created!", registration);
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    public void shouldLoginCustomer() {
        Customer customer = new Customer("Customer", "test@test.com", "test1");

        when(personRepository.findByEmailAndPassword("test@test.com", "test1")).thenReturn(Optional.of(customer));
        webShopService.loginUser("test@test.com", "test1");

        assertEquals(customer, webShopService.getUser());
    }

    @Test
    public void shouldLoginEmployee() {
        Employee employee = new Employee("Admin", "admin@admin.com", "admin");

        when(personRepository.findByEmailAndPassword("admin@admin.com", "admin")).thenReturn(Optional.of(employee));
        webShopService.loginAdmin("admin@admin.com", "admin");

        assertEquals(employee, webShopService.getUser());
    }

    @Test
    public void shouldAddProduct() {
        String message = webShopService.addProduct(new Product("iPhone", "Phone", 11000.0, "The latest iPhone model" ,""));

        assertEquals("iPhone added!", message);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void shouldFindProduct() {

        Product product = new Product("iPhone", "Phone", 11000.0, "The latest iPhone modell","");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product assertProduct = webShopService.getProduct(1L);
        assertEquals(assertProduct, product);
        verify(productRepository, times(1)).findById(1L);

    }

    @Test
    public void shouldRemoveProduct() {
        Product product = new Product("iPhone", "Phone", 11000.0, "The latest iPhone modell","");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        String message = webShopService.removeProduct(1L);

        assertEquals("Product 1 was deleted!", message);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    public void shouldReturnSearchedProductsList() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("iPhone", "Phone", 11000.0, "The latest iPhone modell",""));
        list.add(new Product("Galaxy 22", "Phone", 9000.0, "The latest Samsung Galaxy",""));
        when(productRepository.findAll()).thenReturn(list);

        List<Product> assertedList = webShopService.searchProductsByName("IPHONE");

        assertEquals(1, assertedList.size());
        assertEquals("iPhone", assertedList.get(0).getName());
    }

    @Test
    public void shouldReturnProductsByCategory() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("iPhone", "Phone", 11000.0, "The latest iPhone modell",""));
        list.add(new Product("MacBook", "Laptop", 9000.0, "The latest MacBook",""));
        when(productRepository.findAll()).thenReturn(list);

        List<Product> assertedList = webShopService.searchProductsByCategory("LAPTOP");

        assertEquals(1, assertedList.size());
        assertEquals("Laptop", assertedList.get(0).getCategory());
    }
}