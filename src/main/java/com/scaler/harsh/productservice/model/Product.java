package com.scaler.harsh.productservice.model;

import com.scaler.harsh.productservice.dto.FakeStoreProductDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Product implements Serializable {

    private Integer id;
    private String title;
    private String description;
    private double price;
    private String imageURL;
    private Category category;

}
