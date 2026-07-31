package es.nextdigital.atm.service;

import es.nextdigital.atm.domain.Account;
import es.nextdigital.atm.domain.BusinessException;
import es.nextdigital.atm.domain.Card;
import es.nextdigital.atm.domain.ErrorCode;
import es.nextdigital.atm.domain.Movement;
import es.nextdigital.atm.repository.AccountRepository;
import es.nextdigital.atm.repository.CardRepository;
import es.nextdigital.atm.repository.MovementRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovementService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    public MovementService(CardRepository cardRepository, AccountRepository accountRepository,
                           MovementRepository movementRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> accountsOfHolder(SessionContext session) {
        return accountRepository.findByCustomerId(holderOf(session));
    }

    /**
     * Unica excepcion a "la cuenta sale del token": el titular puede consultar
     * cualquiera de SUS cuentas. Ajena o inexistente responden el mismo 404
     * (B-9): un 403 confirmaria que la cuenta existe — oraculo de enumeracion.
     */
    @Transactional(readOnly = true)
    public Page<Movement> movementsOf(SessionContext session, String accountId, int page, int size) {
        UUID holderId = holderOf(session);
        Account account = accountRepository
                .findById(Uuids.parse(accountId, ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"))
                .filter(a -> a.getCustomerId().equals(holderId))
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        return movementRepository.findByAccountIdOrderByCreatedAtDesc(account.getId(), pageRequest);
    }

    private UUID holderOf(SessionContext session) {
        Card card = cardRepository.findById(session.cardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND, "Card not found"));
        CardGuards.requireOperational(card);
        Account cardAccount = accountRepository.findById(card.getAccountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        return cardAccount.getCustomerId();
    }
}
