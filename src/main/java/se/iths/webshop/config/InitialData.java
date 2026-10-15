package se.iths.webshop.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.iths.webshop.business.entity.Employee;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.service.Passwords;
import se.iths.webshop.data.repository.PersonRepository;
import se.iths.webshop.data.repository.ProductRepository;

@Component
public class InitialData implements ApplicationRunner {
    private final PersonRepository people;
    private final ProductRepository products;
    @Value("${app.bootstrap-admin-email:}") private String adminEmail;
    @Value("${app.bootstrap-admin-password:}") private String adminPassword;
    @Value("${app.seed-products:false}") private boolean seedProducts;

    public InitialData(PersonRepository people, ProductRepository products) {
        this.people = people;
        this.products = products;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminEmail.isBlank() || !adminPassword.isBlank()) {
            if (adminEmail.isBlank() || adminPassword.length() < 12 || adminPassword.length() > 20) {
                throw new IllegalStateException("Set ADMIN_EMAIL and an ADMIN_PASSWORD of 12 to 20 characters.");
            }
            var existing = people.findByEmail(adminEmail);
            if (existing.isPresent() && !(existing.get() instanceof Employee)) {
                throw new IllegalStateException("The bootstrap admin email already belongs to a customer.");
            }
            if (existing.isEmpty()) {
                people.save(new Employee("Studio Admin", adminEmail, Passwords.hash(adminPassword)));
            }
        }
        if (seedProducts && products.count() == 0) {
            products.saveAll(List.of(
                new Product("White linen", "Linen", 300.0, "White linen fabric", "/images/linen_texture.jpg"),
                new Product("Grey knit", "Knit", 250.0, "Soft grey knitted fabric", "/images/knit_texture.jpg"),
                new Product("Beige wool", "Wool", 200.0, "Warm beige wool fabric", "/images/wool_texture.jpg"),
                new Product("Black silk", "Silk", 400.0, "Smooth black silk fabric", "/images/black_fabric.jpg"),
                new Product("Animal fur", "Fur", 300.0, "Leopard fur fabric", "/images/animal_print.jpg"),
                new Product("Gold silk", "Silk", 250.0, "Soft gold silk fabric", "/images/silky_brown_fabric.jpg"),
                new Product("Terry cloth", "Cloth", 200.0, "Terry cloth fabric", "/images/gray_texture.jpg"),
                new Product("Pink cloth", "Cloth", 400.0, "Vivid pink cloth fabric", "/images/pink_fabric.jpg"),
                new Product("Blue cloth", "Cloth", 250.0, "Dark blue cloth fabric", "/images/dark_blue_fabric.jpg")
            ));
        }
    }
}
