package com.vividswan.locktest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
	public void decreaseStockInPessimisticLock(Long productId, Long quantity) {
		Optional<Product> productById = productRepository.findByProductIdInPessimisticLock(productId);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

	@Transactional
	public void decreaseStockInOptimisticLockInTransaction(Long productId, Long quantity) {
		Optional<Product> productById = productRepository.findByProductIdInOptimisticLock(productId);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}

	// "Propagation.REQUIRES_NEW"는 해당 메소드를 새로운 트랜잭션으로 시작하도록 지시
	// lock -> 비즈니스 로직 -> unlock -> 비즈니스 로직 commit으로 되는 것을 방지하고, lock -> REQUIRES_NEW (비즈니스 로직 & commit) -> unlock을 하기 위해
	// 같은 클래스 파일에선 트랜잭션이 독립적으로 실행되지 않으므로 facade에서 호출
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void decreaseStockRequiresNew(Long productId, Long quantity) {
		Optional<Product> productById = productRepository.findByProductId(productId);

		if (productById.isEmpty()) {
			throw new RuntimeException("It's a non-existent product.");
		}

		productById.get().decrease(quantity);
	}
}
