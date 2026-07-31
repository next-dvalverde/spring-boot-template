package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.Account;
import es.nextdigital.atm.domain.AmountPolicy;
import es.nextdigital.atm.domain.Atm;
import es.nextdigital.atm.domain.Bank;
import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.CardType;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.domain.FeeCalculator;
import es.nextdigital.atm.domain.FeeOperationType;
import es.nextdigital.atm.domain.FeeRule;
import es.nextdigital.atm.domain.Movement;
import es.nextdigital.atm.domain.MovementType;
import es.nextdigital.atm.domain.WithdrawalPolicy;
import es.nextdigital.atm.repository.AccountRepository;
import es.nextdigital.atm.repository.AtmRepository;
import es.nextdigital.atm.repository.BankRepository;
import es.nextdigital.atm.repository.CardRepository;
import es.nextdigital.atm.repository.FeePolicyRepository;
import es.nextdigital.atm.repository.MovementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationService {

    public record Receipt(UUID operationId, BigDecimal amount, BigDecimal fee, BigDecimal balanceAfter) {
    }

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final AtmRepository atmRepository;
    private final BankRepository bankRepository;
    private final FeePolicyRepository feePolicyRepository;
    private final MovementRepository movementRepository;

    public OperationService(CardRepository cardRepository, AccountRepository accountRepository,
                            AtmRepository atmRepository, BankRepository bankRepository,
                            FeePolicyRepository feePolicyRepository, MovementRepository movementRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.atmRepository = atmRepository;
        this.bankRepository = bankRepository;
        this.feePolicyRepository = feePolicyRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public Receipt withdraw(SessionContext session, BigDecimal requestedAmount) {
        Card card = requireCard(session);
        CardGuards.requireOperational(card);
        AmountPolicy.requirePositive(requestedAmount);
        BigDecimal amount = requestedAmount.setScale(2, RoundingMode.UNNECESSARY);

        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        Atm atm = atmRepository.findById(session.atmId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ATM_NOT_FOUND, "ATM not found"));

        // Cajero de otro banco: la comision es del banco del cajero (A-3).
        BigDecimal fee = FeeCalculator.forWithdrawal(atm.getBankId(), account.getBankId(), amount,
                withdrawalRuleOf(atm));
        UUID operationId = UUID.randomUUID();

        if (card.getType() == CardType.DEBIT) {
            WithdrawalPolicy.checkDebit(amount, fee, card.getWithdrawalLimit(), account.getBalance());
            account.debit(amount);
            movementRepository.save(new Movement(UUID.randomUUID(), account.getId(), card.getId(),
                    atm.getId(), operationId, MovementType.WITHDRAWAL, amount, true,
                    account.getBalance(), "Cash withdrawal", null));
            if (fee.signum() > 0) {
                account.debit(fee);
                movementRepository.save(new Movement(UUID.randomUUID(), account.getId(), card.getId(),
                        atm.getId(), operationId, MovementType.FEE, fee, true,
                        account.getBalance(), "Withdrawal fee", null));
            }
            return new Receipt(operationId, amount, fee, account.getBalance());
        }

        WithdrawalPolicy.checkCredit(amount, fee, card.getWithdrawalLimit(),
                card.getCreditUsed(), card.getCreditLimit());
        card.useCredit(amount.add(fee));
        // Apuntes de credito: settlesAccount = false y balanceAfter NULL — no
        // liquidan contra el saldo y repetirlo seria mentir en el extracto.
        movementRepository.save(new Movement(UUID.randomUUID(), account.getId(), card.getId(),
                atm.getId(), operationId, MovementType.WITHDRAWAL, amount, false,
                null, "Cash withdrawal (credit)", null));
        if (fee.signum() > 0) {
            movementRepository.save(new Movement(UUID.randomUUID(), account.getId(), card.getId(),
                    atm.getId(), operationId, MovementType.FEE, fee, false,
                    null, "Withdrawal fee (credit)", null));
        }
        return new Receipt(operationId, amount, fee, null);
    }

    @Transactional
    public Receipt deposit(SessionContext session, BigDecimal requestedAmount) {
        Card card = requireCard(session);
        CardGuards.requireOperational(card);
        AmountPolicy.requirePositive(requestedAmount);
        BigDecimal amount = requestedAmount.setScale(2, RoundingMode.UNNECESSARY);

        // B-5: un ingreso con tarjeta de credito seria una amortizacion (C-7).
        if (card.getType() == CardType.CREDIT) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE,
                    "Deposits are not supported for credit cards");
        }
        Account account = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        Atm atm = atmRepository.findById(session.atmId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ATM_NOT_FOUND, "ATM not found"));
        if (!atm.getBankId().equals(account.getBankId())) {
            throw new BusinessException(ErrorCode.FOREIGN_ATM_DEPOSIT_NOT_ALLOWED,
                    "Deposits are only allowed at ATMs of the account's bank");
        }

        account.credit(amount);
        UUID operationId = UUID.randomUUID();
        movementRepository.save(new Movement(UUID.randomUUID(), account.getId(), card.getId(),
                atm.getId(), operationId, MovementType.DEPOSIT, amount, true,
                account.getBalance(), "Cash deposit", null));
        return new Receipt(operationId, amount, BigDecimal.ZERO.setScale(2), account.getBalance());
    }

    private Card requireCard(SessionContext session) {
        return cardRepository.findById(session.cardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
    }

    private FeeRule withdrawalRuleOf(Atm atm) {
        Bank atmBank = bankRepository.findById(atm.getBankId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ATM_NOT_FOUND, "ATM not found"));
        return feePolicyRepository
                .findByBankCodeAndOperationType(atmBank.getCode(), FeeOperationType.WITHDRAWAL)
                .map(policy -> new FeeRule(policy.getFixedAmount(), policy.getPercentage()))
                .orElse(null);
    }
}
