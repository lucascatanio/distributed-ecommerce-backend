package com.catanio.ecommerce.api;

import com.catanio.ecommerce.application.product.ProductFilter;
import com.catanio.ecommerce.application.product.ProductService;
import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.catalog.Product;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.catanio.ecommerce.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService productService;

    Category electronics;
    Product notebook;

    @BeforeEach
    void setUp() {
        electronics = Category.create("Electronics", null);
        notebook = Product.create("Notebook", "15 inch", Money.of("2999.99"), 10, electronics);
    }

    @Test
    void shouldCreateProductAndReturn201() throws Exception {
        when(productService.create(any(), any(), any(), anyInt(), any())).thenReturn(notebook);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Notebook",
                      "description": "15 inch",
                      "price": 2999.99,
                      "stockQuantity": 10,
                      "categoryId": "%s"
                    }
                """.formatted(UUID.randomUUID())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Notebook"))
            .andExpect(jsonPath("$.price").value(2999.99))
            .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    void shouldReturn400WhenPriceIsZero() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Notebook",
                      "price": 0,
                      "stockQuantity": 10,
                      "categryId": "%s"
                    }
                """.formatted(UUID.randomUUID())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.price").exists());

        verify(productService, never()).create(any(), any(), any(), anyInt(), any());
    }

    @Test
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.name").exists())
            .andExpect(jsonPath("$.fields.price").exists())
            .andExpect(jsonPath("$.fields.categoryId").exists());
    }

    @Test
    void shouldFindProductByIdAndReturn200() throws Exception {
        var id = UUID.randomUUID();
        when(productService.findById(id)).thenReturn(notebook);

        mockMvc.perform(get("/api/v1/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Notebook"))
            .andExpect(jsonPath("$.stockQuantity").value(10))
            .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(productService.findById(id))
            .thenThrow(new ResourceNotFoundException("Product", id));

        mockMvc.perform(get("/api/v1/products/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldListProductsWithPaginationAndReturn200() throws Exception {
        var page = new PageImpl<>(List.of(notebook));
        when(productService.findAll(any(ProductFilter.class), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].name").value("Notebook"))
            .andExpect(jsonPath("$.pagination.totalElements").value(1))
            .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    void shouldReturn204WhenDeletingProduct() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(productService).delete(id);

        mockMvc.perform(delete("/api/v1/products/{id}", id))
            .andExpect(status().isNoContent());

        verify(productService).delete(id);
    }

    @Test
    void shouldReturn409WhenCreatingDuplicateProduct() throws Exception {
        when(productService.create(any(), any(), any(), anyInt(), any()))
            .thenThrow(new BusinessException("Product 'Notebook' already exists in category 'Electronics'"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Notebook",
                      "price": 2999.99,
                      "stockQuantity": 10,
                      "categoryId": "%s"
                    }
                """.formatted(UUID.randomUUID())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Product 'Notebook' already exists in category 'Electronics'"));
    }

    @Test
    void shouldAdjustStockAndReturn200() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(productService).adjustStock(id, -3);
        when(productService.findById(id)).thenReturn(notebook);

        mockMvc.perform(patch("/api/v1/products/{id}/stock", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "quantity": -3 }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Notebook"));
    }
}
