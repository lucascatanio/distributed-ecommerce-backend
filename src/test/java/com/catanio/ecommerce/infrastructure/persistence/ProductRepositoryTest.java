package com.catanio.ecommerce.infrastructure.persistence;

import com.catanio.ecommerce.domain.catalog.Category;
import com.catanio.ecommerce.domain.catalog.Product;
import com.catanio.ecommerce.domain.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("ecommerce_test")
        .withUsername("ecommerce")
        .withPassword("ecommerce123");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    Category electronics;
    Category books;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        electronics = categoryRepository.save(Category.create("Electronics", null));
        books = categoryRepository.save(Category.create("Books", null));
    }

    @Test
    void shouldFindActiveById() {
        var product = productRepository.save(
            Product.create("Notebook", null, Money.of("2999.99"), 10, electronics)
        );

        var found = productRepository.findActiveById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Notebook");
    }

    @Test
    void shouldNotFindSoftDeletedProduct() {
        var product = Product.create("OldPhone", null, Money.of("500.00"), 5, electronics);
        product.softDelete();
        productRepository.save(product);

        var found = productRepository.findActiveById(product.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllActiveWithPagination() {
        productRepository.save(Product.create("Notebook", null, Money.of("2999.99"), 10, electronics));
        productRepository.save(Product.create("Mouse", null, Money.of("99.90"), 20, electronics));

        var deletedProduct = Product.create("Deleted", null, Money.of("100.00"), 5, electronics);
        deletedProduct.softDelete();
        productRepository.save(deletedProduct);

        var pageable = PageRequest.of(0, 10);
        var page = productRepository.findAllActive(pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("name")
            .containsExactlyInAnyOrder("Notebook", "Mouse");
    }

    @Test
    void shouldFindByCategoryId() {
        productRepository.save(Product.create("Notebook", null, Money.of("2999.99"), 10, electronics));
        productRepository.save(Product.create("Mouse", null, Money.of("99.90"), 20, electronics));
        productRepository.save(Product.create("Clean Code", null, Money.of("89.90"), 15, books));

        var pageable = PageRequest.of(0, 10);
        var page = productRepository.findByCategoryId(electronics.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("name")
            .containsExactlyInAnyOrder("Notebook", "Mouse");
    }

    @Test
    void shouldFindByNameContaining() {
        productRepository.save(Product.create("Notebook Pro", null, Money.of("3999.99"), 5, electronics));
        productRepository.save(Product.create("Notebook Air", null, Money.of("2999.99"), 5, electronics));
        productRepository.save(Product.create("Mouse", null, Money.of("99.90"), 20, electronics));

        var pageable = PageRequest.of(0, 10);
        var page = productRepository.findByNameContaining("notebook", pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldFindByPriceRange() {
        productRepository.save(Product.create("Budget Mouse", null, Money.of("49.90"), 30, electronics));
        productRepository.save(Product.create("Mid Mouse", null, Money.of("149.90"), 20, electronics));
        productRepository.save(Product.create("Pro Mouse", null, Money.of("499.90"), 10, electronics));

        var pageable = PageRequest.of(0, 10);
        var page = productRepository.findByPriceRange(
            new BigDecimal("50.00"),
            new BigDecimal("200.00"),
            pageable
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getName()).isEqualTo("Mid Mouse");
    }

    @Test
    void shouldReturnPageSortedByPrice() {
        productRepository.save(Product.create("Expensive", null, Money.of("999.00"), 5, electronics));
        productRepository.save(Product.create("Cheap", null, Money.of("49.00"), 5, electronics));
        productRepository.save(Product.create("Mid", null, Money.of("299.00"), 5, electronics));

        var pageable = PageRequest.of(0, 10, Sort.by("price").ascending());
        var page = productRepository.findAllActive(pageable);

        assertThat(page.getContent()).extracting("name")
            .containsExactly("Cheap", "Mid", "Expensive");
    }

    @Test
    void shouldCountActiveByCategoryId() {
        productRepository.save(Product.create("Notebook", null, Money.of("2999.99"), 10, electronics));
        productRepository.save(Product.create("Mouse", null, Money.of("99.90"), 20, electronics));

        var deleted = Product.create("Deleted", null, Money.of("100.00"), 5, electronics);
        deleted.softDelete();
        productRepository.save(deleted);

        long count = productRepository.countActiveByCategoryId(electronics.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCheckExistsByNameAndCategory() {
        productRepository.save(Product.create("Notebook", null, Money.of("2999.99"), 10, electronics));

        assertThat(productRepository.existsByNameIgnoreCaseAndCategoryId("Notebook", electronics.getId())).isTrue();
        assertThat(productRepository.existsByNameIgnoreCaseAndCategoryId("notebook", electronics.getId())).isTrue();
        assertThat(productRepository.existsByNameIgnoreCaseAndCategoryId("Notebook", books.getId())).isFalse();
    }
}
