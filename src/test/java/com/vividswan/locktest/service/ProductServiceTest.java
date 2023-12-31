package com.vividswan.locktest.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import com.vividswan.locktest.domain.Product;
import com.vividswan.locktest.facade.NamedLockProductFacade;
import com.vividswan.locktest.facade.OptimizeLockProductFacade;
import com.vividswan.locktest.repository.ProductRepository;

@SpringBootTest
class ProductServiceTest {
	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private NamedLockProductFacade namedLockProductFacade;

	@Autowired
	private OptimizeLockProductFacade optimizeLockProductFacade;

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
		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// when
		product.decrease(1L);

		// then
		assertEquals(99, product.getQuantity());
	}

	@Test
	@DisplayName("동시에 100개가 요청 되는 코드_in_race_condition")
	public void 동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					productService.decreaseStock(1L, 1L);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();
		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// race condition에 의해 not equal
		assertNotEquals(0, product.getQuantity());
	}

	@Test
	@DisplayName("동시에 100개가 요청 되는 코드_in_synchronized")
	public void synchronized에서_동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					productService.synchronizedDecreaseStock(1L, 1L);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();
		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// synchronized에 의해 equal, 하지만 synchronized는 프로세스 단위로 lock을 제어하기 때문에 다중 서버에선 유효하지 않음
		assertEquals(0, product.getQuantity());
	}

	@Test
	@DisplayName("동시에 100개가 요청 되는 코드_in_pessimisticLock")
	public void pessimisticLock에서_동시에_100개의_요청() throws InterruptedException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					productService.decreaseStockInPessimisticLock(1L, 1L);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		stopWatch.stop();
		System.out.println("Test duration: " + stopWatch.getTotalTimeMillis() + " milliseconds");

		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// 장점 : exclusiveLock(배타 Lock)에 의해 데이터 정합성 보장
		// 장점 : 충돌이 빈번하게 일어날 때 낙관적 락보다 성능이 좋을 수 있음
		// 단점 : 별도의 Lock 잡기 때문에 성능 감소가 있을 수 있음
		assertEquals(0, product.getQuantity());
	}

	@Test
	@DisplayName("동시에 100개가 요청 되는 코드_in_optimisticLock")
	public void optimisticLock에서_동시에_100개의_요청() throws InterruptedException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					optimizeLockProductFacade.decrease(1L, 1L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		stopWatch.stop();
		System.out.println("Test duration: " + stopWatch.getTotalTimeMillis() + " milliseconds");

		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// 장점 : 별도의 Lock 잡지 않으므로 성능상 이점이 있을 수 있음 (충돌이 빈번하게 일어나지 않을 때)
		// 단점 : update 실패 시 재시도 로직을 개발자가 직접 작성해야 함
		assertEquals(0, product.getQuantity());
	}

	@Test
	@DisplayName("동시에 100개가 요청 되는 코드_in_namedLock")
	public void namedLock에서_동시에_100개의_요청() throws InterruptedException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int threadCount = 100;
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					namedLockProductFacade.decrease(1L, 1L);
				} finally {
					countDownLatch.countDown();
				}
			});
		}

		countDownLatch.await();

		stopWatch.stop();
		System.out.println("Test duration: " + stopWatch.getTotalTimeMillis() + " milliseconds");

		Product product = productRepository.findByProductId(1L)
			.orElseThrow(() -> new RuntimeException("존재하지 않는 상품"));

		// 네임드락은 주로 분산락을 구현할 때 많이 사용
		// 장점 : 비관적 락보다 타임아웃을 손쉽게 구현, 데이터 삽입 시 정합성을 맞춰야 할 때도 사용
		// 단점 : 트랜잭션 종료 시 락 해제, 세션관리 등이 복잡
		assertEquals(0, product.getQuantity());
	}
}
