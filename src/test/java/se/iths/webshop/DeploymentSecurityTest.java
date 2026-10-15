package se.iths.webshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import se.iths.webshop.business.entity.*;
import se.iths.webshop.business.service.WebShopService;
import se.iths.webshop.config.TemplateSettings;
import se.iths.webshop.config.WebSecurity;
import se.iths.webshop.ui.controller.WebShopController;
import se.iths.webshop.ui.controller.rest.WebShopRestController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeploymentSecurityTest {
    private WebShopService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(WebShopService.class);
        WebShopController shop = new WebShopController();
        shop.setWebShopService(service);
        WebShopRestController api = new WebShopRestController();
        ReflectionTestUtils.setField(api, "webShopService", service);
        mvc = MockMvcBuilders.standaloneSetup(shop, api)
                .addInterceptors(new WebSecurity(service, false))
                .setControllerAdvice(new TemplateSettings())
                .setSingleView((model, request, response) -> response.getWriter().write("rendered"))
                .build();
    }

    @Test
    void publicCannotRegisterAdminsOrModifyProducts() throws Exception {
        mvc.perform(get("/register-admin")).andExpect(status().isNotFound());
        mvc.perform(post("/register-admin")).andExpect(status().isNotFound());
        mvc.perform(get("/admin-view")).andExpect(status().isForbidden());
        mvc.perform(post("/rs/product/create")).andExpect(status().isForbidden());
        mvc.perform(delete("/rs/product/delete/id/1")).andExpect(status().isForbidden());
        verify(service, never()).createProduct(any());
        verify(service, never()).deleteProduct(anyLong());
    }

    @Test
    void loginRequiresTheSessionsCsrfToken() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login").session(session)).andExpect(status().isOk());
        mvc.perform(post("/login").session(session).param("email", "maker@example.com").param("password", "wrong"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/login").session(session).param("email", "maker@example.com").param("password", "wrong")
                .param("_csrf", "not-the-token")).andExpect(status().isForbidden());
        verify(service, never()).loginUser(anyString(), anyString());
        when(service.loginUser(anyString(), anyString())).thenReturn("Wrong password or e-mail");
        mvc.perform(post("/login").session(session).param("email", "maker@example.com").param("password", "wrong")
                .param("_csrf", (String) session.getAttribute("csrfToken"))).andExpect(status().isOk());
        verify(service).loginUser("maker@example.com", "wrong");
    }

    @Test
    void customersCannotViewOrCancelSomeoneElsesOrder() throws Exception {
        Customer customer = new Customer(); customer.setId(1L);
        Customer other = new Customer(); other.setId(2L);
        CustomerOrder order = new CustomerOrder(); order.setId(42L); order.setCustomer(other);
        when(service.getUser()).thenReturn(customer);
        when(service.getOrder(42L)).thenReturn(order);
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login").session(session));
        String token = (String) session.getAttribute("csrfToken");
        mvc.perform(post("/order").session(session).param("id", "42").param("_csrf", token))
                .andExpect(status().isNotFound());
        mvc.perform(post("/order-cancel").session(session).param("id", "42").param("_csrf", token))
                .andExpect(status().isNotFound());
        verify(service, never()).cancelOrder(anyLong());
    }

    @Test
    void dispatchedOrdersCannotBeCancelledAndGetRequestsCannotMutateTheBag() throws Exception {
        Customer customer = new Customer(); customer.setId(1L);
        CustomerOrder order = new CustomerOrder(); order.setCustomer(customer); order.setProcessed(true);
        when(service.getUser()).thenReturn(customer);
        when(service.getOrder(42L)).thenReturn(order);
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/login").session(session));
        mvc.perform(post("/order-cancel").session(session).param("id", "42")
                .param("_csrf", (String) session.getAttribute("csrfToken"))).andExpect(status().isConflict());
        mvc.perform(get("/clear-cart")).andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/logout")).andExpect(status().isMethodNotAllowed());
        verify(service, never()).cancelOrder(anyLong());
        verify(service, never()).clearCart();
    }
}
