package es.nextdigital.demo.repository;

import es.nextdigital.demo.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * The interface Cuenta repository.
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    /**
     * Find by iban optional.
     *
     * @param iban the iban
     * @return the optional
     */
    Optional<Cuenta> findByIban(String iban);
}

