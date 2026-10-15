package se.iths.webshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import se.iths.webshop.business.entity.CustomerOrder;
import se.iths.webshop.business.entity.OrderLine;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.model.CartItem;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.service.WebShopService;
import se.iths.webshop.ui.controller.WebShopController;

import static org.mockito.Mockito.*;

@Controller
public class WebShopControllerTest {

    @Mock
    final private WebShopService webShopService = mock(WebShopService.class);

    final private WebShopController webShopController = new WebShopController();
    @Mock
    Model model;

    @Mock
    BindingResult br = mock(BindingResult.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webShopController.setWebShopService(webShopService);
    }

    @Test
    public void shouldCombineSearchAndMaterialFilters() {
        when(webShopService.getUser()).thenReturn(new Customer());
        Product blueLinen = new Product("Blue linen", "Linen", 300.0, "Soft woven fabric", "");
        Product whiteLinen = new Product("White linen", "Linen", 200.0, "Natural fabric", "");
        Product blueSilk = new Product("Blue silk", "Silk", 400.0, "Smooth fabric", "");
        when(webShopService.getProducts()).thenReturn(List.of(blueLinen, whiteLinen, blueSilk));
        ExtendedModelMap results = new ExtendedModelMap();

        assertEquals("shop/browse", webShopController.browse(results, " BLUE ", "Linen", "name"));
        assertEquals(List.of(blueLinen), results.get("products"));
        assertEquals("BLUE", results.get("q"));
        webShopController.browse(results, "woven", "", "name");
        assertEquals(List.of(blueLinen), results.get("products"));
        webShopController.browse(results, "no match", "", "name");
        assertEquals(List.of(), results.get("products"));
    }

    @Test
    public void shouldSortPublicBrowseResultsByPrice() {
        when(webShopService.getProducts()).thenReturn(List.of());
        ExtendedModelMap guest = new ExtendedModelMap();
        assertEquals("shop/browse", webShopController.browse(guest, "", "", "name"));
        assertEquals(false, guest.get("authenticated"));
        when(webShopService.getUser()).thenReturn(new Customer());
        Product linen = new Product("Linen", "Linen", 300.0, "", "");
        Product silk = new Product("Silk", "Silk", 400.0, "", "");
        when(webShopService.getProducts()).thenReturn(List.of(linen, silk));
        ExtendedModelMap results = new ExtendedModelMap();
        webShopController.browse(results, "", "", "price-desc");
        assertEquals(List.of(silk, linen), results.get("products"));
        webShopController.browse(results, "", "", "price-asc");
        assertEquals(List.of(linen, silk), results.get("products"));
    }

    @Test
    public void shouldKeepBrowseFiltersAfterAddingToBag() {
        when(webShopService.getUser()).thenReturn(new Customer());
        when(webShopService.addProductToCart(1L, 2)).thenReturn("Added!");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/browse?q=blue%20linen&category=Linen&sort=price-asc",
                webShopController.addFromBrowse(1L, 2, "blue linen", "Linen", "price-asc", redirect, new MockHttpSession()));
        assertEquals("Added!", redirect.getFlashAttributes().get("message"));
        verify(webShopService).addProductToCart(1L, 2);
    }

    @Test
    public void shouldResumeGuestAdditionAfterSuccessfulLoginOnlyOnce() {
        MockHttpSession session = new MockHttpSession();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/login", webShopController.addFromBrowse(7L, 3, "blue", "Linen", "price-desc", redirect, session));
        verify(webShopService, never()).addProductToCart(anyLong(), anyInt());

        when(webShopService.loginUser("maker@example.com", "wrong")).thenReturn(new String("Wrong password or e-mail"));
        ExtendedModelMap failed = new ExtendedModelMap();
        assertEquals("login", webShopController.loginUser(failed, "maker@example.com", "wrong", session, redirect));
        assertEquals(true, failed.get("pendingAddition"));
        verify(webShopService, never()).addProductToCart(anyLong(), anyInt());

        when(webShopService.loginUser("maker@example.com", "correct")).thenReturn("Maker");
        when(webShopService.getUser()).thenReturn(new Customer("Maker", "maker@example.com", "correct"));
        when(webShopService.addProductToCart(7L, 3)).thenReturn("Fabric added to cart!");
        assertEquals("redirect:/browse?q=blue&category=Linen&sort=price-desc",
                webShopController.loginUser(model, "maker@example.com", "correct", session, redirect));
        assertEquals("Fabric added to cart!", redirect.getFlashAttributes().get("message"));
        assertEquals("redirect:/home", webShopController.loginUser(model, "maker@example.com", "correct", session, redirect));
        verify(webShopService, times(1)).addProductToCart(7L, 3);
    }

    @Test
    public void shouldKeepPendingAdditionsSeparateBetweenVisitors() {
        MockHttpSession first = new MockHttpSession();
        MockHttpSession second = new MockHttpSession();
        webShopController.addFromBrowse(1L, 2, "", "", "name", new RedirectAttributesModelMap(), first);
        ExtendedModelMap otherVisitor = new ExtendedModelMap();
        webShopController.login(otherVisitor, second);
        assertEquals(false, otherVisitor.get("pendingAddition"));
        ExtendedModelMap originalVisitor = new ExtendedModelMap();
        webShopController.login(originalVisitor, first);
        assertEquals(true, originalVisitor.get("pendingAddition"));
    }

    @Test
    public void shouldRejectInvalidGuestQuantityWithoutSavingAnAddition() {
        MockHttpSession session = new MockHttpSession();
        assertEquals("redirect:/browse?q=&category=&sort=name", webShopController.addFromBrowse(
                7L, 0, "", "", "name", new RedirectAttributesModelMap(), session));
        ExtendedModelMap login = new ExtendedModelMap();
        webShopController.login(login, session);
        assertEquals(false, login.get("pendingAddition"));
    }

    @Test
    public void shouldRedirectPlacedOrderToConfirmation() {
        Customer customer = new Customer("Maker", "maker@example.com", "test1");
        CustomerOrder order = new CustomerOrder();
        order.setId(42L);
        when(webShopService.getUser()).thenReturn(customer);
        when(webShopService.getCartItems()).thenReturn(List.of(new CartItem(
                new Product("Linen", "Linen", 300.0, "White linen", ""), 1)));
        when(webShopService.checkout()).thenReturn(order);
        MockHttpSession session = new MockHttpSession();

        assertEquals("redirect:/order-confirmation", webShopController.placeOrder(model, session));
        assertEquals(42L, session.getAttribute("placedOrderId"));
        verify(webShopService).clearCart();
    }

    @Test
    public void shouldNotPlaceAnEmptyOrder() {
        when(webShopService.getUser()).thenReturn(new Customer());
        when(webShopService.getCartItems()).thenReturn(List.of());

        assertEquals("redirect:/cart", webShopController.placeOrder(model, new MockHttpSession()));
        verify(webShopService, never()).checkout();
    }

    @Test
    public void shouldShowReceiptOnRefreshWithoutPlacingAnotherOrder() {
        Customer customer = new Customer("Maker", "maker@example.com", "test1");
        customer.setId(1L);
        CustomerOrder order = new CustomerOrder();
        order.setId(42L);
        order.setCustomer(customer);
        order.setItems(List.of(new OrderLine("White linen", "Linen", 300.0, 2)));
        when(webShopService.getUser()).thenReturn(customer);
        when(webShopService.getOrder(42L)).thenReturn(order);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("placedOrderId", 42L);
        ExtendedModelMap receipt = new ExtendedModelMap();

        assertEquals("shop/confirmed", webShopController.orderConfirmation(receipt, session));
        assertEquals(order, receipt.get("order"));
        assertEquals("shop/confirmed", webShopController.orderConfirmation(receipt, session));
        verify(webShopService, never()).checkout();
    }

    @Test
    public void shouldNotShowAnotherCustomersReceipt() {
        Customer customer = new Customer();
        customer.setId(1L);
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        CustomerOrder order = new CustomerOrder();
        order.setCustomer(otherCustomer);
        when(webShopService.getUser()).thenReturn(customer);
        when(webShopService.getOrder(42L)).thenReturn(order);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("placedOrderId", 42L);

        assertEquals("redirect:/orders", webShopController.orderConfirmation(model, session));
        verify(model, never()).addAttribute(eq("order"), any());
    }

    @Test
    public void shouldRequireLoginAndAnOrderForConfirmation() {
        assertEquals("redirect:/login", webShopController.orderConfirmation(model, new MockHttpSession()));
        when(webShopService.getUser()).thenReturn(new Customer());
        assertEquals("redirect:/orders", webShopController.orderConfirmation(model, new MockHttpSession()));
    }

    @Test
    public void shouldRenderReceiptAndBrowsePages() throws Exception {
        Customer customer = new Customer("Maker", "maker@example.com", "test1");
        customer.setId(1L);
        CustomerOrder order = new CustomerOrder();
        order.setId(42L);
        order.setCustomer(customer);
        order.setItems(List.of(new OrderLine("White linen", "Linen", 300.0, 2),
                new OrderLine("Soft wool", "Wool", 200.0, 1)));
        when(webShopService.getUser()).thenReturn(customer);
        when(webShopService.getOrder(42L)).thenReturn(order);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("placedOrderId", 42L);

        FileTemplateResolver templates = new FileTemplateResolver();
        templates.setPrefix("src/main/resources/templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        var mvc = MockMvcBuilders.standaloneSetup(webShopController).setViewResolvers(views).build();
        String html = mvc.perform(get("/order-confirmation").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("#42"));
        assertTrue(html.contains("White linen"));
        assertTrue(html.contains("600.00"));
        assertTrue(html.contains("800.0 SEK"));
        Files.createDirectories(Path.of("target"));
        Files.writeString(Path.of("target/order-confirmation-preview.html"), html);

        when(webShopService.getProducts()).thenReturn(List.of(
                new Product("White linen", "Linen", 300.0, "Soft fabric", "/images/linen_texture.jpg")));
        when(webShopService.getCategories()).thenReturn(List.of("Linen", "Silk"));
        String browseHtml = mvc.perform(get("/browse").param("q", "linen").param("category", "Linen").param("sort", "price-asc"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(browseHtml.contains("White linen"));
        assertTrue(browseHtml.contains("1 fabric found"));
        assertTrue(browseHtml.contains("/browse/add"));
        Files.writeString(Path.of("target/browse-preview.html"), browseHtml);
        when(webShopService.getUser()).thenReturn(null);
        String guestHtml = mvc.perform(get("/browse")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(guestHtml.contains("Create account"));
        org.junit.jupiter.api.Assertions.assertFalse(guestHtml.contains("Log out"));
        MockHttpSession guestSession = new MockHttpSession();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/browse/add")
                .session(guestSession).param("id", "7").param("count", "3"))
                .andExpect(status().is3xxRedirection());
        String loginHtml = mvc.perform(get("/login").session(guestSession)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(loginHtml.contains("Log in to add your selected fabric"));
        for (String removedRoute : List.of("/search", "/search/", "/category", "/category/", "/category/Linen", "/edit-profile", "/admin-edit-profile")) {
            mvc.perform(get(removedRoute)).andExpect(status().isNotFound());
        }
        for (String removedRoute : List.of("/edit-profile", "/admin-edit-profile")) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(removedRoute))
                    .andExpect(status().isNotFound());
        }
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

        webShopController.loginUser(model,"test@test.com", "test1", new MockHttpSession(), new RedirectAttributesModelMap());

        verify(webShopService, times(1)).loginUser(anyString(),anyString());
    }

    @Test
    public void shouldCallServiceToLoginAdmin() {
        when(webShopService.registerAdmin(any(Employee.class))).thenReturn("");

        webShopController.loginAdmin(model,"test@test.com", "test1");

        verify(webShopService, times(1)).loginAdmin(anyString(),anyString());
    }
}
