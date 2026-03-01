package com.catanio.ecommerce.application.product;

import com.catanio.ecommerce.application.category.CategoryService;
import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.catalog.Product;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.catanio.ecommerce.domain.shared.Money;
import com.catanio.ecommerce.infrastructure.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryService categoryService;

    @InjectMocks
    ProductService productService;

    Category electronics;
    UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        electronics = Category.create("Electronics", null);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        when(categoryService.findById(categoryId)).thenReturn(electronics);
        when(productRepository.existsByNameIgnoreCaseAndCategoryId("Notebook", categoryId))
            .thenReturn(false);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = productService.create(
            "Notebook", "15 inch", Money.of("2999.99"), 10, categoryId
        );

        assertThat(result.getName()).isEqualTo("Notebook");
        assertThat(result.getPrice()).isEqualTo(Money.of("2999.99"));
        assertThat(result.getStockQuantity()).isEqualTo(10);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowWhenCreatingDuplicateProductInSameCategory() {
        when(categoryService.findById(categoryId)).thenReturn(electronics);
        when(productRepository.existsByNameIgnoreCaseAndCategoryId("Notebook", categoryId))
            .thenReturn(true);

        assertThatThrownBy(() ->
            productService.create("Notebook", null, Money.of("2999.99"), 10, categoryId)
        )
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCategoryNotFoundOnCreate() {
        when(categoryService.findById(categoryId))
            .thenThrow(new ResourceNotFoundException("Category", categoryId));

        assertThatThrownBy(() ->
            productService.create("Notebook", null, Money.of("100.00"), 10, categoryId)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFindActiveProductById() {
        var id = UUID.randomUUID();
        var product = Product.create("Notebook", null, Money.of("2999.99"), 10, electronics);
        when(productRepository.findActiveById(id)).thenReturn(Optional.of(product));

        var result = productService.findById(id);

        assertThat(result.getName()).isEqualTo("Notebook");
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        var id = UUID.randomUUID();
        when(productRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Product");
    }

    @Test
    void shouldListAllActiveProductsWithNoFilter() {
        var pageable = PageRequest.of(0, 10);
        var products = List.of(
            Product.create("Notebook", null, Money.of("2999.99"), 10, electronics),
            Product.create("Mouse", null, Money.of("99.90"), 20, electronics)
        );
        when(productRepository.findAllActive(pageable))
            .thenReturn(new PageImpl<>(products));

        var filter = new ProductFilter(null, null, null, null);
        var result = productService.findAll(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(productRepository).findAllActive(pageable);
    }

    @Test
    void shouldFilterByCategoryId() {
        var pageable = PageRequest.of(0, 10);
        when(productRepository.findByCategoryId(categoryId, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        var filter = new ProductFilter(categoryId, null, null, null);
        productService.findAll(filter, pageable);

        verify(productRepository).findByCategoryId(categoryId, pageable);
        verify(productRepository, never()).findAllActive(any());
    }

    @Test
    void shouldFilterByPriceRange() {
        var pageable = PageRequest.of(0, 10);
        var min = new BigDecimal("100.00");
        var max = new BigDecimal("500.00");
        when(productRepository.findByPriceRange(min, max, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        var filter = new ProductFilter(null, null, min, max);
        productService.findAll(filter, pageable);

        verify(productRepository).findByPriceRange(min, max, pageable);
    }

    @Test
    void shouldFilterByCategoryAndPriceRange() {
        var pageable = PageRequest.of(0, 10);
        var min = new BigDecimal("100.00");
        var max = new BigDecimal("500.00");
        when(productRepository.findByCategoryIdAndPriceRange(categoryId, min, max, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        var filter = new ProductFilter(categoryId, null, min, max);
        productService.findAll(filter, pageable);

        verify(productRepository).findByCategoryIdAndPriceRange(categoryId, min, max, pageable);
    }

    @Test
    void shouldAdjustStockSuccessfully() {
        var id = UUID.randomUUID();
        var product = Product.create("Notebook", null, Money.of("2999.99"), 10, electronics);
        when(productRepository.findActiveById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.adjustStock(id, -3);

        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenStockGoesBelowZero() {
        var id = UUID.randomUUID();
        var product = Product.create("Notebook", null, Money.of("2999.99"), 5, electronics);
        when(productRepository.findActiveById(id)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.adjustStock(id, -10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldSoftDeleteProduct() {
        var id = UUID.randomUUID();
        var product = Product.create("Notebook", null, Money.of("2999.99"), 10, electronics);
        when(productRepository.findActiveById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.delete(id);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentProduct() {
        var id = UUID.randomUUID();
        when(productRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        var id = UUID.randomUUID();
        var product = Product.create("OldName", "Old desc", Money.of("100.00"), 10, electronics);
        when(productRepository.findActiveByIdWithCategory(id)).thenReturn(Optional.of(product));
        when(categoryService.findById(categoryId)).thenReturn(electronics);
        when(productRepository.existsByNameIgnoreCaseAndCategoryId("NewName", categoryId))
                .thenReturn(false);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = productService.update(id, "NewName", "New desc", Money.of("200.00"), categoryId);

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getPrice()).isEqualTo(Money.of("200.00"));
    }

    @Test
    void shouldThrowWhenUpdatingToExistingNameInSameCategory() {
        var id = UUID.randomUUID();
        var product = Product.create("OldName", null, Money.of("100.00"), 10, electronics);
        when(productRepository.findActiveByIdWithCategory(id)).thenReturn(Optional.of(product));
        when(categoryService.findById(categoryId)).thenReturn(electronics);
        when(productRepository.existsByNameIgnoreCaseAndCategoryId("ExistingName", categoryId))
                .thenReturn(true);

        assertThatThrownBy(() ->
                productService.update(id, "ExistingName", null, Money.of("100.00"), categoryId)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

}
