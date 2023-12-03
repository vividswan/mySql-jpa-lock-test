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

	private final ProductRepository productRepository;

	@Transactional
	public void decreaseStock(Long productId, Long quantity) {
		Optional<Product> productById = productRepository.findByProductId(productId);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

	// 프록시 패턴에 의해 관련 메소드를 래핑한 클래스에서 트랜잭션을 끝내서 DB에 저장하기 전 다른 쓰레드가 synchronized 메서드에 접근 가능하기 때문에 @Transaction이 있으면 제대로 동작하지 않음
	public synchronized void synchronizedDecreaseStock(Long productId, Long quantity) {
		Optional<Product> productById = productRepository.findByProductId(productId);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
		productRepository.saveAndFlush(productById.get()); // 트랜잭션을 안쓰므로
	}

	@Transactional
	public void decreaseStockInPessimisticLock(Long id, Long quantity) {
		Optional<Product> productById = productRepository.findByProductIdIdInPessimisticLock(id);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

	@Transactional
	public void decreaseStockInOptimisticLockInTransaction(Long id, Long quantity) {
		Optional<Product> productById = productRepository.findByProductIdInOptimisticLock(id);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

}
