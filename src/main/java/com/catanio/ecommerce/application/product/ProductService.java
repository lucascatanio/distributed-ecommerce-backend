package com.catanio.ecommerce.application.product;

import com.catanio.ecommerce.application.category.CategoryService;
import com.catanio.ecommerce.domain.catalog.Product;
import com.catanio.ecommerce.domain.exception.BusinessException;
import com.catanio.ecommerce.domain.exception.ResourceNotFoundException;
import com.catanio.ecommerce.domain.shared.Money;
import com.catanio.ecommerce.infrastructure.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public Product create(String name, String description, Money price,
                          int stockQuantity, UUID categoryId) {
        log.debug("Creating product: name={}, categoryId={}", name, categoryId);

        var category = categoryService.findById(categoryId);

        if (productRepository.existsByNameIgnoreCaseAndCategoryId(name, categoryId)) {
            throw new BusinessException(
                    "Product '%s' already exists in category '%s'"
                            .formatted(name, category.getName())
            );
        }

        var product = Product.create(name, description, price, stockQuantity, category);
        var saved = productRepository.save(product);

        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public Page<Product> findAll(ProductFilter filter, Pageable pageable) {
        boolean hasCategory = filter.hasCategoryFilter();
        boolean hasName = filter.hasNameFilter();
        boolean hasPrice = filter.hasPriceFilter();

        // filtro combinado: categoria + preço
        if (hasCategory && hasPrice && !hasName) {
            return productRepository.findByCategoryIdAndPriceRange(
                    filter.categoryId(), filter.minPrice(), filter.maxPrice(), pageable
            );
        }

        // filtro por categoria somente
        if (hasCategory) {
            return productRepository.findByCategoryId(filter.categoryId(), pageable);
        }

        // filtro por nome somente
        if (hasName) {
            return productRepository.findByNameContaining(filter.name(), pageable);
        }

        // filtro por preço somente
        if (hasPrice) {
            return productRepository.findByPriceRange(
                    filter.minPrice(), filter.maxPrice(), pageable
            );
        }

        // sem filtro
        return productRepository.findAllActive(pageable);
    }

    @Transactional
    public Product update(UUID id, String name, String description,
                          Money price, UUID categoryId) {
        log.debug("Updating product: id={}", id);

        var product = productRepository.findActiveByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        var category = categoryService.findById(categoryId);

        boolean nameOrCategoryChanged =
                !product.getName().equalsIgnoreCase(name) ||
                        !product.getCategory().getId().equals(categoryId);

        if (nameOrCategoryChanged &&
                productRepository.existsByNameIgnoreCaseAndCategoryId(name, categoryId)) {
            throw new BusinessException(
                    "Product '%s' already exists in category '%s'"
                            .formatted(name, category.getName())
            );
        }

        product.updateDetails(name, description, price);
        var updated = productRepository.save(product);

        log.info("Product updated: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional
    public void adjustStock(UUID id, int quantity) {
        log.debug("Adjusting stock: productId={}, quantity={}", id, quantity);

        var product = findById(id);
        product.adjustStock(quantity);
        productRepository.save(product);

        log.info("Stock adjusted: productId={}, newStock={}", id, product.getStockQuantity());
    }

    @Transactional
    public void delete(UUID id) {
        log.debug("Soft deleting product: id={}", id);

        var product = findById(id);
        product.softDelete();
        productRepository.save(product);

        log.info("Product soft deleted: id={}", id);
    }
}
