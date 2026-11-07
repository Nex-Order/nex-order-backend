package com.aceleradev.nexorder.entities;

import com.aceleradev.nexorder.commons.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_product")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    public Product() {
    }

    public Product(Long id, String name, Double price, ProductCategory category) {
        this.id = id;
        setName(name);
        setPrice(price);
        setCategory(category);
    }
}
