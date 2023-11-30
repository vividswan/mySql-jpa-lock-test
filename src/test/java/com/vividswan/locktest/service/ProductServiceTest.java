package com.vividswan.locktest.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vividswan.locktest.domain.Product;
import com.vividswan.locktest.repository.ProductRepository;

@SpringBootTest
class ProductServiceTest {
	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	public void before() {
		productRepository.save(new Product(1L, 100L));
	}

	@AfterEach
	public void after() {
		productRepository.deleteAll();
	}

	@Test
	@DisplayName("재고가 하나 감소하는 간단한 테스트 코드")
	public void 재고_감소() {
		// given
		Product product = productRepository.findById(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// when
		product.decrease(1L);

		// then
		assertEquals(99, product.getQuantity());
	}
}
