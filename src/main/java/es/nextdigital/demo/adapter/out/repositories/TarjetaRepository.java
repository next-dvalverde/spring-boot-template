package es.nextdigital.demo.adapter.out.repositories;

import es.nextdigital.demo.application.models.Cuenta;
import es.nextdigital.demo.application.models.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    Optional<Tarjeta> findByNumero(String numeroTarjeta);

    Tarjeta findByCuentaAsociadaId(Long cuentaId);
}
