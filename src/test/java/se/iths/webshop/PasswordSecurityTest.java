package se.iths.webshop;

import org.junit.jupiter.api.Test;
import se.iths.webshop.business.entity.Customer;
import se.iths.webshop.business.entity.Person;
import se.iths.webshop.business.service.Passwords;
import se.iths.webshop.business.service.WebShopService;
import se.iths.webshop.data.repository.*;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordSecurityTest {
    @Test
    void newAccountsStoreSaltedHashesAndLoginDoesNotQueryPlaintextPasswords() {
        PersonRepository people = mock(PersonRepository.class);
        when(people.save(any(Person.class))).thenAnswer(call -> call.getArgument(0));
        WebShopService service = new WebShopService(people, mock(ProductRepository.class), mock(OrderRepository.class));
        Customer customer = new Customer("Maker", "maker@example.com", "example123");
        assertEquals("Account created!", service.registerUser(customer));
        assertNotEquals("example123", customer.getPassword());
        assertTrue(Passwords.matches("example123", customer.getPassword()));
        assertNotEquals(customer.getPassword(), Passwords.hash("example123"));
        when(people.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        assertEquals("Maker", service.loginUser(customer.getEmail(), "example123"));
        assertEquals("Wrong password or e-mail", service.loginUser(customer.getEmail(), "incorrect"));
        assertNull(service.getUser());
        assertEquals("Wrong password or e-mail", service.loginAdmin(customer.getEmail(), "example123"));
        verify(people, never()).findByEmailAndPassword(anyString(), anyString());
    }

    @Test
    void legacyLocalAccountsAreMigratedAndHostedModeRejectsPlaintext() {
        PersonRepository people = mock(PersonRepository.class);
        when(people.save(any(Person.class))).thenAnswer(call -> call.getArgument(0));
        Customer legacy = new Customer("Maker", "maker@example.com", "example123");
        when(people.findByEmail(legacy.getEmail())).thenReturn(Optional.of(legacy));
        WebShopService service = new WebShopService(people, mock(ProductRepository.class), mock(OrderRepository.class));
        org.springframework.test.util.ReflectionTestUtils.setField(service, "allowLegacyPasswords", false);
        assertEquals("Wrong password or e-mail", service.loginUser(legacy.getEmail(), "example123"));
        org.springframework.test.util.ReflectionTestUtils.setField(service, "allowLegacyPasswords", true);
        assertEquals("Maker", service.loginUser(legacy.getEmail(), "example123"));
        assertTrue(Passwords.isHash(legacy.getPassword()));
        verify(people).save(legacy);
    }
}
