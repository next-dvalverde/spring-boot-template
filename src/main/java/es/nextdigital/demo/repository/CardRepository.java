package es.nextdigital.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.nextdigital.demo.model.Card;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
}