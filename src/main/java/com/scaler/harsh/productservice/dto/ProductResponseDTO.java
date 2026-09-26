package com.scaler.harsh.productservice.dto;

import com.scaler.harsh.productservice.model.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDTO {

    private Integer id;
    private String title;
    private String description;
    private double price;
    private String imageURL;

    private Category category;
}
