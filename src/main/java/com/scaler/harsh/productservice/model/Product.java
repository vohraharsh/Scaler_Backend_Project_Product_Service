package com.scaler.harsh.productservice.model;

import com.scaler.harsh.productservice.dto.FakeStoreProductDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {

    private Integer id;
    private String title;
    private String description;
    private double price;
    private String imageURL;
    private Category category;

}
