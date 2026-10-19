package com.scaler.harsh.productservice.repository;

import com.scaler.harsh.productservice.model.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}