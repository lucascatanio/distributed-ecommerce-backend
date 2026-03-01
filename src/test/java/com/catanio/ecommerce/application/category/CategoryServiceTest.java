package com.catanio.ecommerce.application.category;

import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.catanio.ecommerce.infrastructure.persistence.CategoryRepository;
import com.catanio.ecommerce.infrastructure.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void shouldCreateCategorySuccessfully() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = categoryService.create("Electronics", "Electronic devices");

        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowWhenCreatingDuplicateCategory() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create("Electronics", null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldFindCategoryById() {
        var id = UUID.randomUUID();
        var category = Category.create("Books", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        var result = categoryService.findById(id);

        assertThat(result.getName()).isEqualTo("Books");
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        var id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Category")
            .hasMessageContaining(id.toString());
    }

    @Test
    void shouldReturnAllCategoriesOrderedByName() {
        var categories = List.of(
            Category.create("Books", null),
            Category.create("Electronics", null)
        );
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

        var result = categoryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Books", "Electronics");
    }

    @Test
    void shouldUpdateCategoryName() {
        var id = UUID.randomUUID();
        var category = Category.create("OldName", "Old description");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("NewName")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = categoryService.update(id, "NewName", "New description");

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getDescription()).isEqualTo("New description");
    }

    @Test
    void shouldAllowUpdateWithSameName() {
        var id = UUID.randomUUID();
        var category = Category.create("Electronics", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Mesmo nome — não deve checar duplicata
        assertThatCode(() -> categoryService.update(id, "Electronics", "Updated desc"))
            .doesNotThrowAnyException();

        verify(categoryRepository, never()).existsByName(any());
    }

    @Test
    void shouldThrowWhenUpdatingToDuplicateName() {
        var id = UUID.randomUUID();
        var category = Category.create("Books", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(id, "Electronics", null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldDeleteCategoryWithNoProducts() {
        var id = UUID.randomUUID();
        var category = Category.create("EmptyCategory", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(productRepository.countActiveByCategoryId(id)).thenReturn(0L);

        categoryService.delete(id);

        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldThrowWhenDeletingCategoryWithActiveProducts() {
        var id = UUID.randomUUID();
        var category = Category.create("Electronics", null);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(productRepository.countActiveByCategoryId(id)).thenReturn(3L);

        assertThatThrownBy(() -> categoryService.delete(id))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("3 active product(s)");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldAllowClearingDescription() {
        var id = UUID.randomUUID();
        var category = Category.create("Books", "Old description");
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = categoryService.update(id, "Books", null);

        assertThat(result.getDescription()).isNull();
    }

}
