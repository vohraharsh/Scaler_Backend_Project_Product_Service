package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.dto.FakeStoreProductDTO;
import com.scaler.harsh.productservice.model.Product;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FakeStoreService implements ProductService {

    private RestTemplate restTemplate;
    public FakeStoreService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getProductById(Long id) {
        //Call to fakestore api
        //get the response
        //Map the response to our model
        //return the model to service layer

        ResponseEntity<FakeStoreProductDTO> response =
                restTemplate.getForEntity
                        ("https://fakestoreapi.com/products/" + id, FakeStoreProductDTO.class,id);

        if(response.getBody() == null){
            //Throw Exception
            System.out.println("Product Body is null");
        }

        FakeStoreProductDTO fakeStoreProductDTO = response.getBody();

        return fakeStoreProductDTO.toProduct(fakeStoreProductDTO);
    }

    @Override
    public Product createProduct(CreateProductRequestDTO request) {
        FakeStoreProductDTO requestDTO = FakeStoreProductDTO.from(request);

        FakeStoreProductDTO responseDTO = restTemplate.postForObject(
                "https://fakestoreapi.com/products", requestDTO, FakeStoreProductDTO.class
        );
        return responseDTO.toProduct(responseDTO);
    }

    @Override
    public List<Product> getAllProducts() {

        ResponseEntity<FakeStoreProductDTO[]> response =
                restTemplate.getForEntity(
                        "https://fakestoreapi.com/products",
                        FakeStoreProductDTO[].class
                );

        if(response.getBody() == null){
            //Throw Exception
            System.out.println("Product list is null");
        }

        FakeStoreProductDTO[] fakeStoreProductDTO = response.getBody();

        List<Product> products = new ArrayList<>();

        for (FakeStoreProductDTO dto : fakeStoreProductDTO) {
            products.add(dto.toProduct(dto));
        }

        return products;
    }

    @Override
    public Product updateProduct(Long id, CreateProductRequestDTO request) {

        FakeStoreProductDTO requestDTO = FakeStoreProductDTO.from(request);

        HttpEntity<FakeStoreProductDTO> requestEntity = new HttpEntity<>(requestDTO);

        ResponseEntity<FakeStoreProductDTO> response = restTemplate.exchange(
                "https://fakestoreapi.com/products/" + id,
                HttpMethod.PUT,
                requestEntity,
                FakeStoreProductDTO.class
        );

        FakeStoreProductDTO responseDto = response.getBody();

        return responseDto.toProduct(responseDto);
    }

    @Override
    public void deleteProduct(Long id) {

        restTemplate.delete("https://fakestoreapi.com/products/" + id);
    }
}
