package com.vividswan.locktest.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Long productId;

	@Getter
	private Long quantity;

	public Product(Long productId, Long quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public void decrease(Long quantity) {
		if (this.quantity < quantity) {
			throw new RuntimeException("It's more requests than the current quantity.");
		}
		this.quantity -= quantity;
	}
}
