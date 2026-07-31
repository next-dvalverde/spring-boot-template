package es.nextdigital.atm.repository;

import es.nextdigital.atm.domain.Atm;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtmRepository extends JpaRepository<Atm, UUID> {
}
