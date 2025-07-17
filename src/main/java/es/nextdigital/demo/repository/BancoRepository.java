package es.nextdigital.demo.repository;

import es.nextdigital.demo.entity.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BancoRepository extends JpaRepository<Banco, Long> {}
