package com.catanio.ecommerce.infrastructure.persistence;

import com.catanio.ecommerce.domain.catalog.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // produto ativo por ID — usado em GET /products/{id}
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findActiveById(@Param("id") UUID id);

    // produtos ativos com paginação — GET /products
    @Query(
        value = "SELECT p FROM Product p WHERE p.deletedAt IS NULL",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL"
    )
    Page<Product> findAllActive(Pageable pageable);

    // filtra por categoria — GET /products?categoryId=
    @Query(
        value = "SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL"
    )
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    // nome — GET /products?name=
    // LOWER + CONCAT evita index funcional miss; suave para Sprint 2
    @Query(
        value = "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.deletedAt IS NULL",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.deletedAt IS NULL"
    )
    Page<Product> findByNameContaining(@Param("name") String name, Pageable pageable);

    // filtra por faixa de preço — GET /products?minPrice=&maxPrice=
    @Query(
        value = "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.deletedAt IS NULL",
        countQuery = "SELECT COUNT(p) FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.deletedAt IS NULL"
    )
    Page<Product> findByPriceRange(
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    // filtro por categoria + faixa de preço — GET /products?categoryId=&minPrice=&maxPrice=
    @Query(
        value = """
            SELECT p FROM Product p
            WHERE p.category.id = :categoryId
              AND p.price BETWEEN :minPrice AND :maxPrice
              AND p.deletedAt IS NULL
            """,
        countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.category.id = :categoryId
              AND p.price BETWEEN :minPrice AND :maxPrice
              AND p.deletedAt IS NULL
            """
    )
    Page<Product> findByCategoryIdAndPriceRange(
        @Param("categoryId") UUID categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    // Verifica duplicata de nome dentro da mesma categoria
    // Usado em validação antes de criar/atualizar produto
    boolean existsByNameIgnoreCaseAndCategoryId(String name, UUID categoryId);

    // Conta produtos ativos numa categoria
    // Usado antes de deletar uma categoria
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.deletedAt IS NULL")
    long countActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findActiveByIdWithCategory(@Param("id") UUID id);

}
