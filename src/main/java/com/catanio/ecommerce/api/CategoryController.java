package com.catanio.ecommerce.api;

import com.catanio.ecommerce.api.dto.category.CategoryResponse;
import com.catanio.ecommerce.api.dto.category.CreateCategoryRequest;
import com.catanio.ecommerce.api.dto.category.UpdateCategoryRequest;
import com.catanio.ecommerce.application.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
        @Valid @RequestBody CreateCategoryRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        var category = categoryService.create(request.name(), request.description());
        var uri = uriBuilder.path("/api/v1/categories/{id}").buildAndExpand(category.getId()).toUri();
        return ResponseEntity.created(uri).body(CategoryResponse.from(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
        var category = categoryService.findById(id);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        var categories = categoryService.findAll()
            .stream()
            .map(CategoryResponse::from)
            .toList();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateCategoryRequest request
    ) {
        var category = categoryService.update(id, request.name(), request.description());
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}
