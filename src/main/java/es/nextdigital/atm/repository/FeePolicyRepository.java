package es.nextdigital.atm.repository;

import es.nextdigital.atm.domain.FeeOperationType;
import es.nextdigital.atm.domain.FeePolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FeePolicyRepository extends JpaRepository<FeePolicy, UUID> {

    // C-8: cuando las tarifas se versionen, este es el metodo cuyo WHERE gana
    // un filtro por fecha. Nada mas cambia.
    Optional<FeePolicy> findByBankCodeAndOperationType(String bankCode, FeeOperationType operationType);
}
