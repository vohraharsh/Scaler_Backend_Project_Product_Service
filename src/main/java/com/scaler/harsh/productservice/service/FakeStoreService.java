package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.dto.FakeStoreProductDTO;
import com.scaler.harsh.productservice.exceptions.ProductServiceException;
import com.scaler.harsh.productservice.model.Product;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

@Service
public class FakeStoreService implements ProductService {

    private RestTemplate restTemplate;
    public FakeStoreService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        //Call to FakeStore API
        //get the response
        //Map the response to our model
        //return the model to service layer

        ResponseEntity<FakeStoreProductDTO> response;

        try {
            response = restTemplate.getForEntity(
                    "https://fakestoreapi.com/products/" + id,FakeStoreProductDTO.class, id);
        } catch (RestClientException e) {
            throw new ProductServiceException("Could not reach FakeStoreAPI",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        if(response.getBody() == null) {
            throw new ProductServiceException("Product not found for id: " + id,
                    HttpStatus.NOT_FOUND);
        }

        FakeStoreProductDTO fakeStoreProductDTO = response.getBody();

        return fakeStoreProductDTO.toProduct(fakeStoreProductDTO);
    }

    @Override
    public Product createProduct(CreateProductRequestDTO request) {
        FakeStoreProductDTO requestDTO = FakeStoreProductDTO.from(request);

        FakeStoreProductDTO responseDTO;

        try {
            responseDTO = restTemplate.postForObject(
                    "https://fakestoreapi.com/products", requestDTO, FakeStoreProductDTO.class);
        }  catch (RestClientException e) {
            throw new ProductServiceException("Could not reach FakeStoreAPI"
            ,HttpStatus.SERVICE_UNAVAILABLE);
        }


        if(responseDTO == null){
            throw new ProductServiceException("Product Creation Failed",
                    HttpStatus.BAD_REQUEST);
        }

        return responseDTO.toProduct(responseDTO);
    }

    @Override
    public List<Product> getAllProducts() {

        ResponseEntity<FakeStoreProductDTO[]> response;

        try{
            response = restTemplate.getForEntity(
                    "https://fakestoreapi.com/products",
                    FakeStoreProductDTO[].class
            );
        } catch (RestClientException e) {
            throw new ProductServiceException("Could not reach FakeStoreAPI",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        if(response.getBody() == null){
            throw new ProductServiceException("Product List Not Found",
                    HttpStatus.NOT_FOUND);
        }

        FakeStoreProductDTO[] fakeStoreProductDTO = response.getBody();

        List<Product> products = new ArrayList<>();

        for (FakeStoreProductDTO dto : fakeStoreProductDTO) {
            products.add(dto.toProduct(dto));
        }

        return products;
    }

    @Override
    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, CreateProductRequestDTO request) {

        FakeStoreProductDTO requestDTO = FakeStoreProductDTO.from(request);

        HttpEntity<FakeStoreProductDTO> requestEntity = new HttpEntity<>(requestDTO);

        ResponseEntity<FakeStoreProductDTO> response;

        try {
        response = restTemplate.exchange(
                "https://fakestoreapi.com/products/" + id,
                HttpMethod.PUT,
                requestEntity,
                FakeStoreProductDTO.class
        ); } catch (RestClientException e) {
            throw new ProductServiceException("Could not reach FakeStoreAPI",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        FakeStoreProductDTO responseDto = response.getBody();

        if(responseDto == null){
            throw new ProductServiceException("Product not found for id: " + id,
                    HttpStatus.NOT_FOUND);
        }

        return responseDto.toProduct(responseDto);
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public Product deleteProduct(Long id) {

        ResponseEntity<FakeStoreProductDTO> response;

        try {
            response = restTemplate.exchange("https://fakestoreapi.com/products/" + id,
                    HttpMethod.DELETE, null, FakeStoreProductDTO.class);
        } catch (RestClientException e) {
            throw new ProductServiceException("Could not reach FakeStoreAPI",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        FakeStoreProductDTO responseDto = response.getBody();

        if(responseDto == null){
            throw new ProductServiceException("Product not found for id: " + id,
                    HttpStatus.NOT_FOUND);
        }
        return responseDto.toProduct(responseDto);
    }
}
