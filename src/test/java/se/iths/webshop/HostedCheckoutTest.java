package se.iths.webshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.webshop.data.repository.ProductRepository;
import se.iths.webshop.data.repository.OrderRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:hosted-checkout;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=update", "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-hosted.sql", "app.seed-products=true"})
@AutoConfigureMockMvc
class HostedCheckoutTest {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;

    @Test
    void guestCanRegisterLoginAndPlaceOneOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/browse").session(session)).andExpect(status().isOk());
        String token = (String) session.getAttribute("csrfToken");
        String product = products.findAll().get(0).getId().toString();
        mvc.perform(post("/browse/add").session(session).param("_csrf", token)
                .param("id", product).param("count", "2"))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(post("/register-user").session(session).param("_csrf", token)
                .param("name", "Demo Maker").param("email", "hosted-check@example.com")
                .param("password", "ExamplePass123"))
                .andExpect(status().isOk());
        mvc.perform(post("/login").session(session).param("_csrf", token)
                .param("email", "hosted-check@example.com").param("password", "ExamplePass123"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/cart").session(session)).andExpect(status().isOk());
        mvc.perform(post("/checkout").session(session).param("_csrf", token)).andExpect(status().isOk());
        mvc.perform(post("/place-order").session(session).param("_csrf", token))
                .andExpect(redirectedUrl("/order-confirmation"));
        mvc.perform(get("/order-confirmation").session(session)).andExpect(status().isOk());
        mvc.perform(get("/order-confirmation").session(session)).andExpect(status().isOk());
        assertEquals(1, orders.count());
    }
}

