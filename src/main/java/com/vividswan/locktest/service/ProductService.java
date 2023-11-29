package com.vividswan.locktest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vividswan.locktest.domain.Product;
import com.vividswan.locktest.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

	private ProductRepository productRepository;

	@Transactional
	public void decreaseStock(Long id, Long quantity) {
		Optional<Product> productById = productRepository.findById(id);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

}
