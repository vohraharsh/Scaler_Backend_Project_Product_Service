package com.scaler.harsh.productservice.service;

import com.scaler.harsh.productservice.dto.CreateProductRequestDTO;
import com.scaler.harsh.productservice.exceptions.ProductServiceException;
import com.scaler.harsh.productservice.model.Category;
import com.scaler.harsh.productservice.model.Product;
import com.scaler.harsh.productservice.model.ProductDocument;
import com.scaler.harsh.productservice.repository.CategoryRepository;
import com.scaler.harsh.productservice.repository.ProductRepository;
import com.scaler.harsh.productservice.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductSearchRepository productSearchRepository;

    private SelfProductService selfProductService;

    @BeforeEach
    void setUp() {
        selfProductService = new SelfProductService(productRepository, categoryRepository, productSearchRepository);
    }

    private Category buildCategory(Long id, String title) {
        Category category = new Category();
        category.setId(id);
        category.setTitle(title);
        return category;
    }

    private Product buildProduct(Long id, String title, String description, double price, String imageURL, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageURL(imageURL);
        product.setCategory(category);
        return product;
    }

    private CreateProductRequestDTO buildRequest(String title, String description, double price, String category, String imageUrl) {
        CreateProductRequestDTO dto = new CreateProductRequestDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setCategory(category);
        dto.setImageUrl(imageUrl);
        return dto;
    }

    // ---------- getProductById ----------
    @Test
    void getProductById_found_returnsProduct() {
        Category category = buildCategory(1L, "Electronics");
        Product product = buildProduct(10L, "Phone", "A phone", 499.99, "http://img/phone.png", category);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        Product result = selfProductService.getProductById(10L);

        assertEquals(10L, result.getId());
        assertEquals("Phone", result.getTitle());
        assertEquals(499.99, result.getPrice());
    }

    @Test
    void getProductById_notFound_throws404() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductServiceException ex = assertThrows(ProductServiceException.class,
                () -> selfProductService.getProductById(99L));

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
        assertTrue(ex.getMessage().contains("99"));
    }

    // ---------- createProduct ----------
    @Test
    void createProduct_existingCategory_doesNotCreateNewCategory() {
        Category existingCategory = buildCategory(1L, "Electronics");
        CreateProductRequestDTO request = buildRequest("Phone", "A phone", 499.99, "Electronics", "http://img/phone.png");

        when(categoryRepository.findByTitle("Electronics")).thenReturn(existingCategory);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        Product result = selfProductService.createProduct(request);

        assertEquals(10L, result.getId());
        assertEquals("Phone", result.getTitle());
        assertEquals(existingCategory, result.getCategory());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createProduct_newCategory_createsAndSavesCategoryFirst() {
        CreateProductRequestDTO request = buildRequest("Chair", "A chair", 149.99, "Furniture", "http://img/chair.png");

        when(categoryRepository.findByTitle("Furniture")).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(20L);
            return p;
        });

        Product result = selfProductService.createProduct(request);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertEquals("Furniture", categoryCaptor.getValue().getTitle());

        assertEquals(5L, result.getCategory().getId());
        assertEquals("Furniture", result.getCategory().getTitle());
    }

    @Test
    void createProduct_mapsAllFieldsFromRequest() {
        Category category = buildCategory(1L, "Electronics");
        CreateProductRequestDTO request = buildRequest("Phone", "A great phone", 599.50, "Electronics", "http://img/phone2.png");

        when(categoryRepository.findByTitle("Electronics")).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        selfProductService.createProduct(request);
        verify(productRepository).save(productCaptor.capture());

        Product saved = productCaptor.getValue();
        assertEquals("Phone", saved.getTitle());
        assertEquals("A great phone", saved.getDescription());
        assertEquals(599.50, saved.getPrice());
        assertEquals("http://img/phone2.png", saved.getImageURL());
        assertEquals(category, saved.getCategory());
    }

    @Test
    void createProduct_syncsToElasticsearchWithCorrectFields() {
        Category category = buildCategory(1L, "Electronics");
        CreateProductRequestDTO request = buildRequest("Phone", "A phone", 499.99, "Electronics", "http://img/phone.png");

        when(categoryRepository.findByTitle("Electronics")).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        selfProductService.createProduct(request);

        ArgumentCaptor<ProductDocument> docCaptor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productSearchRepository).save(docCaptor.capture());

        ProductDocument doc = docCaptor.getValue();
        assertEquals(10L, doc.getId());
        assertEquals("Phone", doc.getTitle());
        assertEquals("A phone", doc.getDescription());
        assertEquals(499.99, doc.getPrice());
        assertEquals("Electronics", doc.getCategory());
        assertEquals("http://img/phone.png", doc.getImageURL());
    }

    // ---------- getAllProducts ----------
    @Test
    void getAllProducts_delegatesToRepository() {
        Product p1 = buildProduct(1L, "A", "descA", 10.0, "imgA", null);
        Product p2 = buildProduct(2L, "B", "descB", 20.0, "imgB", null);
        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> result = selfProductService.getAllProducts();

        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    // ---------- updateProduct ----------
    @Test
    void updateProduct_notFound_throws404() {
        when(productRepository.findById(50L)).thenReturn(Optional.empty());
        CreateProductRequestDTO request = buildRequest("X", "Y", 1.0, "Cat", "img");

        ProductServiceException ex = assertThrows(ProductServiceException.class,
                () -> selfProductService.updateProduct(50L, request));

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_existingCategory_updatesFieldsAndKeepsCategory() {
        Category category = buildCategory(1L, "Electronics");
        Product existing = buildProduct(10L, "OldTitle", "OldDesc", 100.0, "oldImg", category);
        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByTitle("Electronics")).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductRequestDTO request = buildRequest("NewTitle", "NewDesc", 150.0, "Electronics", "newImg");

        Product result = selfProductService.updateProduct(10L, request);

        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewDesc", result.getDescription());
        assertEquals(150.0, result.getPrice());
        assertEquals("newImg", result.getImageURL());
        assertEquals(category, result.getCategory());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateProduct_newCategory_createsCategoryAndReassigns() {
        Category oldCategory = buildCategory(1L, "Electronics");
        Product existing = buildProduct(10L, "OldTitle", "OldDesc", 100.0, "oldImg", oldCategory);
        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByTitle("Furniture")).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductRequestDTO request = buildRequest("NewTitle", "NewDesc", 150.0, "Furniture", "newImg");

        Product result = selfProductService.updateProduct(10L, request);

        assertEquals("Furniture", result.getCategory().getTitle());
        assertEquals(5L, result.getCategory().getId());
    }

    @Test
    void updateProduct_syncsToElasticsearch() {
        Category category = buildCategory(1L, "Electronics");
        Product existing = buildProduct(10L, "OldTitle", "OldDesc", 100.0, "oldImg", category);
        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByTitle("Electronics")).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductRequestDTO request = buildRequest("NewTitle", "NewDesc", 150.0, "Electronics", "newImg");
        selfProductService.updateProduct(10L, request);

        ArgumentCaptor<ProductDocument> docCaptor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productSearchRepository).save(docCaptor.capture());
        assertEquals("NewTitle", docCaptor.getValue().getTitle());
        assertEquals(10L, docCaptor.getValue().getId());
    }

    // ---------- deleteProduct ----------
    @Test
    void deleteProduct_notFound_throws404() {
        when(productRepository.findById(77L)).thenReturn(Optional.empty());

        ProductServiceException ex = assertThrows(ProductServiceException.class,
                () -> selfProductService.deleteProduct(77L));

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatus());
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void deleteProduct_found_deletesFromBothRepositoriesAndReturnsProduct() {
        Category category = buildCategory(1L, "Electronics");
        Product existing = buildProduct(10L, "Phone", "A phone", 499.99, "img", category);
        when(productRepository.findById(10L)).thenReturn(Optional.of(existing));

        Product result = selfProductService.deleteProduct(10L);

        assertEquals(10L, result.getId());
        verify(productRepository).deleteById(10L);
        verify(productSearchRepository).deleteById(10L);
    }

    // ---------- searchProducts ----------
    @Test
    void searchProducts_delegatesToSearchRepositoryWithSameKeywordForBothFields() {
        ProductDocument doc = new ProductDocument();
        doc.setId(1L);
        doc.setTitle("Phone");
        when(productSearchRepository.findByTitleContainingOrDescriptionContaining("phone", "phone"))
                .thenReturn(List.of(doc));

        List<ProductDocument> result = selfProductService.searchProducts("phone");

        assertEquals(1, result.size());
        verify(productSearchRepository).findByTitleContainingOrDescriptionContaining("phone", "phone");
    }

    // ---------- getProductsByCategory ----------
    @Test
    void getProductsByCategory_delegatesToRepository() {
        Product p1 = buildProduct(1L, "Phone", "desc", 100.0, "img", null);
        when(productRepository.findByCategoryTitle("Electronics")).thenReturn(List.of(p1));

        List<Product> result = selfProductService.getProductsByCategory("Electronics");

        assertEquals(1, result.size());
        verify(productRepository).findByCategoryTitle("Electronics");
    }
}