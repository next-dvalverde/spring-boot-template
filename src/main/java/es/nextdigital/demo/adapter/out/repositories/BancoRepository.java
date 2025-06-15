package es.nextdigital.demo.adapter.out.repositories;

import es.nextdigital.demo.application.models.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BancoRepository extends JpaRepository<Banco, Long> {
}
