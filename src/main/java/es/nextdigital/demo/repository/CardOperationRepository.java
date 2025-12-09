package es.nextdigital.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextdigital.demo.model.Account;
import es.nextdigital.demo.model.CardOperation;

public interface CardOperationRepository extends JpaRepository<CardOperation, Long> {
    List<CardOperation> findByAccount(Account account);
}
