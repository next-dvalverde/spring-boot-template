package repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import entities.Account;
import entities.Card;
import entities.Customer;
import enumerado.CardType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    
    // BÚSQUEDAS BÁSICAS
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByAccount(Account account);
    List<Card> findByAccountId(UUID accountId);
    
    // ESTADO DE LA TARJETA
    List<Card> findByActiveTrue();
    List<Card> findByActiveFalse();
    Optional<Card> findByCardNumberAndActiveTrue(String cardNumber);
    
    // PRIMERA ACTIVACIÓN 
    List<Card> findByActiveTrueAndPinChangedFalse();
    
    // POR TIPO DE TARJETA
    List<Card> findByType(CardType type);
    List<Card> findByTypeAndActiveTrue(CardType type);
    
    // TARJETAS DE UN CLIENTE (a través de sus cuentas)
    @Query("SELECT c FROM Card c WHERE c.account.customer = :customer")
    List<Card> findByCustomer(@Param("customer") Customer customer);
    
    @Query("SELECT c FROM Card c WHERE c.account.customer.id = :customerId")
    List<Card> findByCustomerId(@Param("customerId") UUID customerId);
    
    // VERIFICACIONES
    boolean existsByCardNumber(String cardNumber);
    boolean existsByCardNumberAndActiveTrue(String cardNumber);
    
    // CONSULTAS PARA CAJERO AUTOMÁTICO
    @Query("SELECT c FROM Card c WHERE c.cardNumber = :cardNumber " +
           "AND c.active = true AND c.pinChanged = true")
    Optional<Card> findActiveAndReadyCard(@Param("cardNumber") String cardNumber);
    
    // CONSULTA PARA VALIDAR RETIRO (incluye límites)
    @Query("SELECT c FROM Card c WHERE c.id = :cardId " +
           "AND c.active = true " +
           "AND (c.type = 'DEBIT' OR " +
           "     (c.type = 'CREDIT' AND c.creditLimit IS NOT NULL))")
    Optional<Card> findValidCardForWithdrawal(@Param("cardId") UUID cardId);
    
    // ESTADÍSTICAS
    long countByType(CardType type);
    long countByActiveTrue();
    long countByActiveTrueAndType(CardType type);
    
    // TARJETAS POR LÍMITE DE CRÉDITO
    @Query("SELECT c FROM Card c WHERE c.type = 'CREDIT' " +
           "AND c.creditLimit >= :minLimit AND c.creditLimit <= :maxLimit")
    List<Card> findCreditCardsByLimitRange(
        @Param("minLimit") BigDecimal minLimit,
        @Param("maxLimit") BigDecimal maxLimit);
}