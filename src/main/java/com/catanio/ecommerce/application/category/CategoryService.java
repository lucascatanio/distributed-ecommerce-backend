package com.catanio.ecommerce.application.category;

import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.catanio.ecommerce.infrastructure.persistence.CategoryRepository;
import com.catanio.ecommerce.infrastructure.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Category create(String name, String description) {
        log.debug("Creating category: {}", name);

        if (categoryRepository.existsByName(name)) {
            throw new BusinessException("Category already exists with name: " + name);
        }

        var category = Category.create(name, description);
        var saved = categoryRepository.save(category);

        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Category findById(UUID id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Category update(UUID id, String newName, String newDescription) {
        log.debug("Updating category: id={}", id);

        var category = findById(id);

        boolean nameChanged = !category.getName().equalsIgnoreCase(newName);
        if (nameChanged && categoryRepository.existsByName(newName)) {
            throw new BusinessException("Category already exists with name: " + newName);
        }

        category.update(newName, newDescription);
        var updated = categoryRepository.save(category);

        log.info("Category updated: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        log.debug("Deleting category: id={}", id);

        var category = findById(id);

        long activeProducts = productRepository.countActiveByCategoryId(id);
        if (activeProducts > 0) {
            throw new BusinessException(
                "Cannot delete category '%s' — it has %d active product(s)"
                    .formatted(category.getName(), activeProducts)
            );
        }

        categoryRepository.delete(category);
        log.info("Category deleted: id={}, name={}", id, category.getName());
    }
}
