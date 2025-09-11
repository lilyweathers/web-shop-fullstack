package se.iths.webshop.business.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @Column(name = "name")
    private String name;
    @NotBlank
    @Column(name = "category")
    private String category;

    @NotNull
    @Min(value = 0)
    @Column(name = "price")
    private Double price;
    @Column(name = "description")
    private String description;
     @Column(name = "img")
    private String img;

    public Product() {}

    public Product(String name, String category, Double price, String description, String img) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.img = img;
    }

    public String getDescription() {
        return description;
    }



    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

        public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) && Objects.equals(name, product.name) && Objects.equals(category, product.category) && Objects.equals(price, product.price) && Objects.equals(description, product.description);
    }
}
