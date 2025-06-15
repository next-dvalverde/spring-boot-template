package es.nextdigital.demo.adapter.out.repositories;

import es.nextdigital.demo.application.models.Banco;
import es.nextdigital.demo.application.models.Cajero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CajeroRepository extends JpaRepository<Cajero, Long> {
}
