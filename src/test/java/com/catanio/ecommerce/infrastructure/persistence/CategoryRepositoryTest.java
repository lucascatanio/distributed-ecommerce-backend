package com.catanio.ecommerce.infrastructure.persistence;

import com.catanio.ecommerce.domain.catalog.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

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
    CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindById() {
        var category = Category.create("Electronics", "Electronic devices");
        var saved = categoryRepository.save(category);

        var found = categoryRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldFindByName() {
        categoryRepository.save(Category.create("Books", "All books"));

        var found = categoryRepository.findByName("Books");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Books");
    }

    @Test
    void shouldReturnEmptyWhenNameNotFound() {
        var found = categoryRepository.findByName("NonExistent");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistsByName() {
        categoryRepository.save(Category.create("Sports", null));

        assertThat(categoryRepository.existsByName("Sports")).isTrue();
        assertThat(categoryRepository.existsByName("Music")).isFalse();
    }

    @Test
    void shouldReturnAllCategoriesOrderedByName() {
        categoryRepository.save(Category.create("Sports", null));
        categoryRepository.save(Category.create("Books", null));
        categoryRepository.save(Category.create("Electronics", null));

        var categories = categoryRepository.findAllByOrderByNameAsc();

        assertThat(categories).hasSize(3);
        assertThat(categories).extracting("name")
            .containsExactly("Books", "Electronics", "Sports");
    }
}
