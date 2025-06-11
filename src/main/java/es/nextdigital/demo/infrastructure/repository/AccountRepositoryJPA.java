package es.nextdigital.demo.infrastructure.repository;

import es.nextdigital.demo.infrastructure.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepositoryJPA extends JpaRepository<AccountEntity, String> {
	@Query("SELECT c.account FROM Card c WHERE c.id = :cardId")
	Optional<AccountEntity> findByCardId(@Param("cardId") UUID cardId);

}