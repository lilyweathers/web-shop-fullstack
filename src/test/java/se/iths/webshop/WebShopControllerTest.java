package se.iths.webshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.service.WebShopService;
import se.iths.webshop.ui.controller.WebShopController;

import static org.mockito.Mockito.*;

@Controller
public class WebShopControllerTest {

    @Mock
    private WebShopService webShopService = mock(WebShopService.class);

    private WebShopController webShopController = new WebShopController();
    @Mock
    Model model;

    @Mock
    BindingResult br = mock(BindingResult.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        webShopController.setWebShopService(webShopService);
    }

    @Test
    public void shouldCallServiceToRegisterNewUser() {
        when(webShopService.registerUser(any(Customer.class))).thenReturn("");

        webShopController.registerUser(model,new Customer("Customer", "test@test.com", "test1"), br);

        verify(webShopService, times(1)).registerUser(any(Customer.class));
    }

    @Test
    public void shouldCallServiceToRegisterNewAdmin() {
        when(webShopService.registerAdmin(any(Employee.class))).thenReturn("");

        webShopController.registerAdmin(model,new Employee("Admin", "admin@admin.com", "admin"), br);

        verify(webShopService, times(1)).registerAdmin(any(Employee.class));
    }

    @Test
    public void shouldCallServiceToLoginUser() {
        when(webShopService.registerUser(any(Customer.class))).thenReturn("");

        webShopController.loginUser(model,"test@test.com", "test1");

        verify(webShopService, times(1)).loginUser(anyString(),anyString());
    }

    @Test
    public void shouldCallServiceToLoginAdmin() {
        when(webShopService.registerAdmin(any(Employee.class))).thenReturn("");

        webShopController.loginAdmin(model,"test@test.com", "test1");

        verify(webShopService, times(1)).loginAdmin(anyString(),anyString());
    }
}
