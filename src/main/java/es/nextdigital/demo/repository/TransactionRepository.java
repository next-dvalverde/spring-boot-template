package es.nextdigital.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.nextdigital.demo.model.Transaction;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
}