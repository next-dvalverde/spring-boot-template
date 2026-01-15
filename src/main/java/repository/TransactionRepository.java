package repository;


import entities.Account;
import entities.Transaction;
import enumerado.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    
    
    List<Transaction> findByAccountAndTypeOrderByCreatedAtDesc(Account account, TransactionType type);
    
    // Por rango de fechas
    List<Transaction> findByAccountAndCreatedAtBetween(Account account, LocalDateTime start, LocalDateTime end);
    
    // Retiros de hoy
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
           "AND t.type = 'WITHDRAWAL' " +
           "AND DATE(t.createdAt) = CURRENT_DATE")
    List<Transaction> findTodayWithdrawals(@Param("account") Account account);
    
    // Total retirado hoy
    @Query("SELECT COALESCE(SUM(ABS(t.amount)), 0) FROM Transaction t " +
           "WHERE t.account = :account " +
           "AND t.type = 'WITHDRAWAL' " +
           "AND DATE(t.createdAt) = CURRENT_DATE")
    BigDecimal getTodayWithdrawalTotal(@Param("account") Account account);
    
    // Por IBAN destino
    List<Transaction> findByDestinationIban(String destinationIban);
    
    // Por referencia
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    // Últimas N transacciones
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findLastTransactions(@Param("account") Account account, Pageable pageable);
    
    // Transacciones con comisión
    List<Transaction> findByCommissionGreaterThan(BigDecimal zero);
    
    // Transacciones desde cajeros externos
    List<Transaction> findByExternalAtmTrue();
    
    // Estadísticas
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account = :account " +
           "AND t.createdAt >= :since")
    long countTransactionsSince(@Param("account") Account account, 
                               @Param("since") LocalDateTime since);
}