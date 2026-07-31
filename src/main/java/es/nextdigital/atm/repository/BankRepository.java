package es.nextdigital.atm.repository;

import es.nextdigital.atm.domain.Bank;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, UUID> {
}
