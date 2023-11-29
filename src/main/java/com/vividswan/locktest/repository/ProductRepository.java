package com.vividswan.locktest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vividswan.locktest.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	
}
