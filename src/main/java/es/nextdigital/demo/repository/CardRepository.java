package es.nextdigital.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextdigital.demo.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
}
