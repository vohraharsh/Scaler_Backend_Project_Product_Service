package com.scaler.harsh.productservice.model;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Category implements Serializable {

    private Long id;
    private String title;

}
