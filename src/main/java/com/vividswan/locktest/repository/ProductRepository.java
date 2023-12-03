package com.vividswan.locktest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vividswan.locktest.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findByProductId(Long productId);
}
