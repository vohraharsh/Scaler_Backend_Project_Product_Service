package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.model.Product;

import java.util.List;

public interface ProductService {


    //This should have only functions, that to just declaration of the function,
    // it will not have the body of the function

    public Product getProductById(Long id);

    public void createProduct();

    public List<Product> getAllProducts();
}
