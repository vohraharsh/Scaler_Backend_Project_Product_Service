package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.exceptions.ProductServiceException;
import com.scaler.harsh.productservice.model.Category;
import com.scaler.harsh.productservice.model.Product;
import com.scaler.harsh.productservice.model.ProductDocument;
import com.scaler.harsh.productservice.repository.CategoryRepository;
import com.scaler.harsh.productservice.repository.ProductRepository;
import com.scaler.harsh.productservice.repository.ProductSearchRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
public class SelfProductService implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchRepository productSearchRepository;

    public SelfProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductSearchRepository productSearchRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSearchRepository = productSearchRepository;
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new ProductServiceException("Product not found for id: " + id, HttpStatus.NOT_FOUND);
        }
        return product.get();
    }

    @Override
    public Product createProduct(CreateProductRequestDTO request) {
        Category category = categoryRepository.findByTitle(request.getCategory());
        if (category == null) {
            category = new Category();
            category.setTitle(request.getCategory());
            category = categoryRepository.save(category);
        }

        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageURL(request.getImageUrl());
        product.setCategory(category);

        product = productRepository.save(product);
        syncToElasticsearch(product);
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, CreateProductRequestDTO request) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ProductServiceException("Product not found for id: " + id, HttpStatus.NOT_FOUND);
        }

        Category category = categoryRepository.findByTitle(request.getCategory());
        if (category == null) {
            category = new Category();
            category.setTitle(request.getCategory());
            category = categoryRepository.save(category);
        }

        Product product = existing.get();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageURL(request.getImageUrl());
        product.setCategory(category);

        product = productRepository.save(product);
        syncToElasticsearch(product);
        return product;
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public Product deleteProduct(Long id) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ProductServiceException("Product not found for id: " + id, HttpStatus.NOT_FOUND);
        }
        Product product = existing.get();
        productRepository.deleteById(id);
        productSearchRepository.deleteById(id);
        return product;
    }

    @Override
    public List<ProductDocument> searchProducts(String keyword) {
        return productSearchRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    private void syncToElasticsearch(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setId(product.getId());
        doc.setTitle(product.getTitle());
        doc.setDescription(product.getDescription());
        doc.setPrice(product.getPrice());
        doc.setCategory(product.getCategory().getTitle());
        doc.setImageURL(product.getImageURL());
        productSearchRepository.save(doc);
    }

    @Override
    public List<Product> getProductsByCategory(String categoryTitle) {
        return productRepository.findByCategoryTitle(categoryTitle);
    }
}