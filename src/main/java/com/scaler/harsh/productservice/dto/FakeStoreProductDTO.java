package com.scaler.harsh.productservice.dto;

import com.scaler.harsh.productservice.model.Category;
import com.scaler.harsh.productservice.model.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FakeStoreProductDTO {


    private int id;
    private String title;
    private String description;
    private String category;
    private String price;
    private String image;

    public Product toProduct(FakeStoreProductDTO dto) {

        Product product = new Product();
        Category category = new Category();
        category.setTitle(dto.getCategory());
        product.setCategory(category);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setId(dto.getId());
        product.setImageURL(dto.getImage());
        product.setPrice(Double.valueOf(dto.getPrice()));

        return  product;
    }

    public static FakeStoreProductDTO from(CreateProductRequestDTO request) {
        FakeStoreProductDTO dto = new FakeStoreProductDTO();
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setCategory(request.getCategory());
        dto.setPrice(String.valueOf(request.getPrice()));
        dto.setImage(request.getImageUrl());

        return dto;
    }
}
