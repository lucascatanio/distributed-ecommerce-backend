package com.catanio.ecommerce.api;

import com.catanio.ecommerce.api.dto.PageResponse;
import com.catanio.ecommerce.api.dto.product.AdjustStockRequest;
import com.catanio.ecommerce.api.dto.product.CreateProductRequest;
import com.catanio.ecommerce.api.dto.product.ProductResponse;
import com.catanio.ecommerce.api.dto.product.UpdateProductRequest;
import com.catanio.ecommerce.application.product.ProductFilter;
import com.catanio.ecommerce.application.product.ProductService;
import com.catanio.ecommerce.domain.shared.Money;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
        @Valid @RequestBody CreateProductRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        var product = productService.create(
            request.name(),
            request.description(),
            Money.of(request.price()),
            request.stockQuantity(),
            request.categoryId()
        );
        var uri = uriBuilder.path("/api/v1/products/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(uri).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        var product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> findAll(
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        var sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        var pageable = PageRequest.of(page, Math.min(size, 100), sort);
        var filter = new ProductFilter(categoryId, name, minPrice, maxPrice);

        var productPage = productService.findAll(filter, pageable)
            .map(ProductResponse::from);

        return ResponseEntity.ok(PageResponse.from(productPage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        var product = productService.update(
            id,
            request.name(),
            request.description(),
            Money.of(request.price()),
            request.categoryId()
        );
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(
        @PathVariable UUID id,
        @Valid @RequestBody AdjustStockRequest request
    ) {
        productService.adjustStock(id, request.quantity());
        var product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
