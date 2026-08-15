package com.aceleradev.nexorder.dtos.product;

import com.aceleradev.nexorder.commons.enums.ProductCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private ProductCategory category;
}
