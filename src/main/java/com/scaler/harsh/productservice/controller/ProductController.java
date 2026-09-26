package com.scaler.harsh.productservice.controller;

import com.scaler.harsh.productservice.dto.ProductResponseDTO;
import com.scaler.harsh.productservice.model.Product;
import com.scaler.harsh.productservice.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    private ProductService svc;

    public ProductController(ProductService svc) {
        this.svc = svc;
    }

    @PostMapping("/product")
    public void createProduct(){

    }

    @GetMapping("/products")
    public List<ProductResponseDTO> getAllProducts(){

        List<Product> products = svc.getAllProducts();
        List<ProductResponseDTO> response = new ArrayList<>();

        for (Product product : products) {
            response.add(convertToProductResponseDTO(product));
        }

        return response;
    }

    @GetMapping("/product/{id}")
    public ProductResponseDTO getProductById(@PathVariable ("id") Long id){

        //Validation = If id = null, throw exception
        //Call service layer
        //model to DTO conversion in controller

        Product product = svc.getProductById(id);
        ProductResponseDTO response = convertToProductResponseDTO(product);
        return response;
    }

    private void validationParam(Long id){
        if (id == null){
            System.out.println("Product id is null");
        }
    }

    private ProductResponseDTO convertToProductResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setCategory(product.getCategory());
        dto.setId(product.getId());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setTitle(product.getTitle());
        dto.setImageURL(product.getImageURL());
        return dto;
    }

    @DeleteMapping("/product/{id}")
    public void deleteProduct(@PathVariable ("id") Long id){

    }

}
