package entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import enumerado.TransactionType;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // Relación con Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    // Relación con Card 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;
    
    // Tipo de transacción
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType type;
    
    // Monto (positivo para ingresos, negativo para retiros)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    // Saldo después de la transacción
    @Column(name = "balance_after", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceAfter;
    
    // Descripción legible para el usuario
    @Column(length = 255)
    private String description;
    
    // Fecha y hora de la transacción
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Para transferencias: IBAN destino
    @Column(name = "destination_iban", length = 34)
    private String destinationIban;
    
    // Para transferencias: nombre del beneficiario
    @Column(name = "destination_name", length = 100)
    private String destinationName;
    
    // Comisión aplicada (si es cajero externo o transferencia externa)
    @Column(precision = 10, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;
    
    // Si la operación se hizo en cajero del mismo banco o externo
    @Column(name = "is_external_atm")
    private boolean externalAtm = false;
    
    // Referencia de la transacción (para conciliación)
    @Column(name = "transaction_reference", unique = true, length = 50)
    private String transactionReference;
    
    // Estado de la transacción
    @Column(length = 20)
    private String status = "COMPLETED"; // COMPLETED, PENDING, FAILED, CANCELLED
    
    // Para auditoría: qué cajero se usó (si es operación en cajero)
    @Column(name = "atm_id")
    private String atmId;
    
    // Para auditoría: ubicación del cajero
    @Column(name = "atm_location", length = 200)
    private String atmLocation;
    
    // ===== CONSTRUCTORES =====
    
    public Transaction() {
        // Constructor vacío para JPA
    }
    
    // Constructor básico para retiro/depósito
    public Transaction(Account account, Card card, TransactionType type, 
                      BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.account = account;
        this.card = card;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    // Constructor para transferencias
    public Transaction(Account account, TransactionType type, BigDecimal amount,
                      BigDecimal balanceAfter, String description,
                      String destinationIban, String destinationName) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.destinationIban = destinationIban;
        this.destinationName = destinationName;
        this.createdAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    // Constructor con comisión (cajero externo)
    public Transaction(Account account, Card card, TransactionType type,
                      BigDecimal amount, BigDecimal balanceAfter, 
                      String description, BigDecimal commission, boolean externalAtm) {
        this.account = account;
        this.card = card;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.commission = commission;
        this.externalAtm = externalAtm;
        this.createdAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    
    public static Transaction createWithdrawal(Account account, Card card, 
                                              BigDecimal amount, BigDecimal balanceAfter,
                                              boolean isExternalAtm, BigDecimal commission) {
        String desc = isExternalAtm ? 
            "Retiro en cajero externo" : "Retiro en cajero propio";
        
        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            desc += " (Comisión: " + commission + "€)";
        }
        
        Transaction transaction = new Transaction(
            account, card, TransactionType.WITHDRAWAL, 
            amount.negate(), // Los retiros son negativos
            balanceAfter, desc, commission, isExternalAtm
        );
        
        transaction.generateReference();
        return transaction;
    }
    
    public static Transaction createDeposit(Account account, Card card,
                                           BigDecimal amount, BigDecimal balanceAfter) {
        Transaction transaction = new Transaction(
            account, card, TransactionType.DEPOSIT,
            amount, balanceAfter, "Ingreso en cajero"
        );
        
        transaction.generateReference();
        return transaction;
    }
    
    public static Transaction createTransferOut(Account account, BigDecimal amount,
                                               BigDecimal balanceAfter, 
                                               String destinationIban, 
                                               String destinationName,
                                               BigDecimal commission) {
        String desc = "Transferencia a " + destinationName + " (" + destinationIban + ")";
        
        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            desc += " (Comisión: " + commission + "€)";
        }
        
        Transaction transaction = new Transaction(
            account, TransactionType.TRANSFER_OUT,
            amount.negate(), balanceAfter, desc,
            destinationIban, destinationName
        );
        
        transaction.setCommission(commission);
        transaction.generateReference();
        return transaction;
    }
    
    public static Transaction createTransferIn(Account account, BigDecimal amount,
                                              BigDecimal balanceAfter,
                                              String originIban, String originName) {
        String desc = "Transferencia desde " + originName + " (" + originIban + ")";
        
        Transaction transaction = new Transaction(
            account, TransactionType.TRANSFER_IN,
            amount, balanceAfter, desc,
            originIban, originName
        );
        
        transaction.generateReference();
        return transaction;
    }
    
    private void generateReference() {
    	// Generar una referencia única para la transacción
        this.transactionReference = "TXN-" + 
            LocalDateTime.now().getYear() + "-" +
            String.format("%08d", Math.abs(this.hashCode()));
    }
    
    
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public Card getCard() {
        return card;
    }
    
    public void setCard(Card card) {
        this.card = card;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getDestinationIban() {
        return destinationIban;
    }
    
    public void setDestinationIban(String destinationIban) {
        this.destinationIban = destinationIban;
    }
    
    public String getDestinationName() {
        return destinationName;
    }
    
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }
    
    public BigDecimal getCommission() {
        return commission;
    }
    
    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
    
    public boolean isExternalAtm() {
        return externalAtm;
    }
    
    public void setExternalAtm(boolean externalAtm) {
        this.externalAtm = externalAtm;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAtmId() {
        return atmId;
    }
    
    public void setAtmId(String atmId) {
        this.atmId = atmId;
    }
    
    public String getAtmLocation() {
        return atmLocation;
    }
    
    public void setAtmLocation(String atmLocation) {
        this.atmLocation = atmLocation;
    }
    
    // ===== MÉTODOS DE NEGOCIO =====
    
    public boolean isWithdrawal() {
        return this.type == TransactionType.WITHDRAWAL;
    }
    
    public boolean isDeposit() {
        return this.type == TransactionType.DEPOSIT;
    }
    
    public boolean isTransfer() {
        return this.type == TransactionType.TRANSFER_IN || 
               this.type == TransactionType.TRANSFER_OUT;
    }
    
    public BigDecimal getNetAmount() {
        // Monto neto (después de comisiones)
        if (this.commission.compareTo(BigDecimal.ZERO) > 0) {
            return this.amount.subtract(this.commission);
        }
        return this.amount;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + type +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
