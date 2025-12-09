package es.nextdigital.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.nextdigital.demo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);

}
