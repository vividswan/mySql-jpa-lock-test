package com.vividswan.locktest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vividswan.locktest.domain.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findByProductId(Long productId);

	// 비관적 락은 조회 시 쿼리문에서 "for update"
	// org.hibernate.SQL   : select p1_0.id,p1_0.product_id,p1_0.quantity from product p1_0 where p1_0.product_id=? for update
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value = "select p from Product p where p.productId = :productId")
	Optional<Product> findByProductIdInPessimisticLock(@Param("productId") Long productId);

	// 낙관적 락은 조회 시 쿼리문에서 "version" 조회
	// org.hibernate.SQL   : update product set product_id=?,quantity=?,version=? where id=? and version=?
	@Lock(LockModeType.OPTIMISTIC)
	@Query(value = "select p from Product p where p.productId = :productId")
	Optional<Product> findByProductIdInOptimisticLock(@Param("productId") Long productId);
}
