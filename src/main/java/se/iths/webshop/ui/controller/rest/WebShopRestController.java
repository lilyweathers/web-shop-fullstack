package se.iths.webshop.ui.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import se.iths.webshop.business.entity.Product;
import se.iths.webshop.business.service.WebShopService;

@RestController
public class WebShopRestController {

    @Autowired
    WebShopService webShopService;

    @PostMapping("/rs/product/create")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.accepted().body(webShopService.createProduct(product));
    }
    @PostMapping("/rs/product/create/name/{name}/category/{category}/price/{price}/description/{description}")
    public ResponseEntity<Product> createProduct(@Valid @PathVariable String name, @PathVariable String category, @PathVariable double price, @PathVariable String description, @PathVariable String img) {
        return ResponseEntity.accepted().body(webShopService.createProduct(new Product(name, category, price, description, img)));
    }


    @GetMapping("/rs/product/all")
    public List<Product> getAllProducts() {
        return webShopService.getAllProducts();
    }

    @GetMapping("/rs/product/id/{id}")
    public Product getProductById(@PathVariable long id) {
        return webShopService.getProductById(id);
    }

    @GetMapping("/rs/product/name/{name}")
    public List<Product> getProductByName(@PathVariable String name) {
        return webShopService.getProductsByName(name);
    }

    @GetMapping("/rs/product/category/{category}")
    public List<Product> getProductByCategory(@PathVariable String category) {
        return webShopService.getProductsByCategory(category);
    }

    @PutMapping("/rs/product/update/id/{id}/name/{name}")
    public Product updateProductName(@PathVariable long id, @PathVariable String name) {
        return webShopService.updateProductName(id, name);
    }

    @PutMapping("/rs/product/update/id/{id}/category/{category}")
    public Product updateProductCategory(@PathVariable long id, @PathVariable String category) {
        return webShopService.updatedProductCategory(id, category);
    }

    @PutMapping("/rs/product/update/id/{id}/price/{price}")
    public Product updateProductPrice(@PathVariable long id, @PathVariable double price) {
        return webShopService.updateProductPrice(id, price);
    }

    @PutMapping("/rs/product/update/id/{id}/description/{description}")
    public Product updateProductDescription(@PathVariable long id, @PathVariable String description) {
        return webShopService.updateProductDescription(id, description);
    }

    @DeleteMapping("/rs/product/delete/id/{id}")
    public List<Product> deleteProduct(@PathVariable long id) {
        return webShopService.deleteProduct(id);
    }
}
