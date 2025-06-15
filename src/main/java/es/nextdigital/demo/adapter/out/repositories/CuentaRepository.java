package es.nextdigital.demo.adapter.out.repositories;

import es.nextdigital.demo.application.models.Cajero;
import es.nextdigital.demo.application.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByIban(String ibanDestino);
}
