package com.vividswan.locktest.facade;

import org.springframework.stereotype.Component;

import com.vividswan.locktest.service.ProductService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class OptimizeLockProductFacade {

	private final ProductService productService;
	
	public void decrease(Long id, Long quantity) throws InterruptedException {
		// OptimisticLockException은 Transaction에서 롤백 되므로 외부에서 Version이 안 맞을 때 처리
		while (true) {
			try {
				productService.decreaseStockInOptimisticLockInTransaction(id, quantity);
				break;
			} catch (Exception e) {
				Thread.sleep(100);
			}
		}
	}

}
