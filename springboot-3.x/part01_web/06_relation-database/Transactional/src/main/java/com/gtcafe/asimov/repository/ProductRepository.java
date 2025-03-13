package com.gtcafe.asimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gtcafe.asimov.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}