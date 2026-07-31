package es.nextdigital.atm.repository;

import es.nextdigital.atm.domain.Movement;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementRepository extends JpaRepository<Movement, UUID> {

    Page<Movement> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
}
