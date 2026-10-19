package com.scaler.harsh.productservice.controller;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.dto.ProductResponseDTO;
import com.scaler.harsh.productservice.exceptions.ProductServiceException;
import com.scaler.harsh.productservice.model.Product;
import com.scaler.harsh.productservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.scaler.harsh.productservice.model.Category;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductController {

    private ProductService svc;

    public ProductController(ProductService svc) {
        this.svc = svc;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody CreateProductRequestDTO request) {

        if(request.getTitle() == null || request.getTitle().isBlank()){
            throw new ProductServiceException("Title cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        if(request.getCategory() == null || request.getCategory().isBlank()){
            throw new ProductServiceException("Category cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        if(request.getPrice() <= 0){
            throw new ProductServiceException("Price must be positive",
                    HttpStatus.BAD_REQUEST);
        }

        Product product = svc.createProduct(request);
        ProductResponseDTO response = convertToProductResponseDTO(product);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

    @GetMapping("/products/{id}")
    public ProductResponseDTO getProductById(@PathVariable ("id") Long id){

        if(id <= 0){
            throw new ProductServiceException("Invalid Product ID",
                    HttpStatus.BAD_REQUEST);
        }

        //Validation = If id = null, throw exception
        //Call service layer
        //model to DTO conversion in controller

        Product product = svc.getProductById(id);
        ProductResponseDTO response = convertToProductResponseDTO(product);
        return response;
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

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable ("id") Long id){

        if(id <= 0){
            throw new ProductServiceException("Invalid Product ID",
                    HttpStatus.BAD_REQUEST);
        }

        svc.deleteProduct(id);
        return "Product with id " + id + " deleted successfully";
    }

    @PutMapping("/products/{id}")
    public ProductResponseDTO updateProduct(@PathVariable ("id") Long id,
                                            @RequestBody CreateProductRequestDTO request) {

        if(id <= 0){
            throw new ProductServiceException("Invalid Product ID",
                    HttpStatus.BAD_REQUEST);
        }


        if(request.getTitle() == null || request.getTitle().isBlank()){
            throw new ProductServiceException("Title cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        if(request.getCategory() == null || request.getCategory().isBlank()){
            throw new ProductServiceException("Category cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        if(request.getPrice() <= 0){
            throw new ProductServiceException("Price must be positive",
                    HttpStatus.BAD_REQUEST);
        }

        Product product = svc.updateProduct(id, request);
        return convertToProductResponseDTO(product);
    }

    @GetMapping("/products/search")
    public List<ProductResponseDTO> searchProducts(@RequestParam("keyword") String keyword) {
        List<com.scaler.harsh.productservice.model.ProductDocument> results = svc.searchProducts(keyword);
        List<ProductResponseDTO> response = new ArrayList<>();
        for (com.scaler.harsh.productservice.model.ProductDocument doc : results) {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(doc.getId());
            dto.setTitle(doc.getTitle());
            dto.setDescription(doc.getDescription());
            dto.setPrice(doc.getPrice());
            dto.setImageURL(doc.getImageURL());
            response.add(dto);

            Category category = new Category();
            category.setTitle(doc.getCategory());
            dto.setCategory(category);
        }
        return response;
    }

}
