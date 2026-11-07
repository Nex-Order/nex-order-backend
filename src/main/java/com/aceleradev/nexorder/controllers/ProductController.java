package com.aceleradev.nexorder.controllers;

import com.aceleradev.nexorder.dtos.product.ProductDto;
import com.aceleradev.nexorder.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> findAll(){
        List<ProductDto> products = productService.findAll();
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id){
        ProductDto product = productService.findById(id);
        return ResponseEntity.ok().body(product);
    }

    @PostMapping
    public ResponseEntity createProduct(@RequestBody ProductDto productDto){
        productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto){
        productService.updateProduct(id, productDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.ok("Deletado com sucesso!");
    }

}
