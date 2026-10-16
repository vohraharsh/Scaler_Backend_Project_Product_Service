package com.scaler.harsh.productservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequestDTO {

    private String title;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
}
