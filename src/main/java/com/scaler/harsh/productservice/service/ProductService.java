package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.model.Product;
import com.scaler.harsh.productservice.model.ProductDocument;

import java.util.List;

public interface ProductService {


    //This should have only functions, that to just declaration of the function,
    // it will not have the body of the function

    List<ProductDocument> searchProducts(String keyword);

    public Product getProductById(Long id);

    public Product createProduct(CreateProductRequestDTO request);

    public List<Product> getAllProducts();

    public Product updateProduct(Long id, CreateProductRequestDTO request);

    public Product deleteProduct(Long id);
}
