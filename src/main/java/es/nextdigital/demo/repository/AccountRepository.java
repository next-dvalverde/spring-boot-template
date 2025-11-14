package es.nextdigital.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.nextdigital.demo.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}