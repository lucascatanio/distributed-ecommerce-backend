package com.catanio.ecommerce.api;

import com.catanio.ecommerce.application.category.CategoryService;
import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CategoryService categoryService;

    @Test
    void shouldCreateCategoryAndReturn201() throws Exception {
        var category = Category.create("Electronics", "Devices");
        when(categoryService.create("Electronics", "Devices")).thenReturn(category);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Electronics", "description": "Devices" }
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Electronics"))
            .andExpect(jsonPath("$.description").value("Devices"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "" }
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.name").exists());

        verify(categoryService, never()).create(any(), any());
    }

    @Test
    void shouldReturn409WhenCategoryAlreadyExists() throws Exception {
        when(categoryService.create(any(), any()))
            .thenThrow(new BusinessException("Category already exists with name: Electronics"));

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Electronics" }
                """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Category already exists with name: Electronics"));
    }

    @Test
    void shouldFindCategoryByIdAndReturn200() throws Exception {
        var id = UUID.randomUUID();
        var category = Category.create("Books", null);
        when(categoryService.findById(id)).thenReturn(category);

        mockMvc.perform(get("/api/v1/categories/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(categoryService.findById(id))
            .thenThrow(new ResourceNotFoundException("Category", id));

        mockMvc.perform(get("/api/v1/categories/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void shouldListAllCategoriesAndReturn200() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
            Category.create("Books", null),
            Category.create("Electronics", null)
        ));

        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateCategoryAndReturn200() throws Exception {
        var id = UUID.randomUUID();
        var updated = Category.create("Updated", "New desc");
        when(categoryService.update(eq(id), eq("Updated"), eq("New desc"))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "name": "Updated", "description": "New desc" }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteCategoryAndReturn204() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(categoryService).delete(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
            .andExpect(status().isNoContent());

        verify(categoryService).delete(id);
    }

    @Test
    void shouldReturn409WhenDeletingCategoryWithActiveProducts() throws Exception {
        var id = UUID.randomUUID();
        doThrow(new BusinessException("Cannot delete category — it has 3 active product(s)"))
            .when(categoryService).delete(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Cannot delete category — it has 3 active product(s)"));
    }
}
