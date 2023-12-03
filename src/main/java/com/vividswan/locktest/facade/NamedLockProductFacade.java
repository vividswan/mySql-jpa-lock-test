package com.vividswan.locktest.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vividswan.locktest.repository.ProductRepository;
import com.vividswan.locktest.service.ProductService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NamedLockProductFacade {

	private final ProductRepository productRepository;

	private final ProductService productService;

	@Transactional
	public void decrease(Long id, Long quantity) {
		try {
			productRepository.getLock(id.toString());
			productService.decreaseStockRequiresNew(id, quantity);
		} finally {
			productRepository.releaseLock(id.toString());
		}
	}

}
