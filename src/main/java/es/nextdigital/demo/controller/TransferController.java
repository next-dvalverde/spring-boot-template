package es.nextdigital.demo.controller;

import es.nextdigital.demo.model.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import es.nextdigital.demo.repository.AccountRepository;
import es.nextdigital.demo.repository.TransactionRepository;
import es.nextdigital.demo.repository.CardRepository;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @PostMapping
    public String transfer(@RequestParam String fromIban,
                           @RequestParam String toIban,
                           @RequestParam BigDecimal amount,
                           @RequestParam Long cardId,
                           @RequestParam(defaultValue = "true") boolean sameBank) {

        Account from = accountRepository.findAll().stream()
                .filter(a -> a.getIban().equals(fromIban))
                .findFirst().orElseThrow(() -> new RuntimeException("Origin account not found"));

        Account to = accountRepository.findAll().stream()
                .filter(a -> a.getIban().equals(toIban))
                .findFirst().orElseThrow(() -> new RuntimeException("Destination account not found"));

        // Verify active card
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        if (card.getStatus() != CardStatus.ACTIVE)
            throw new RuntimeException("Card is not active");

        // fee if it is another bank
        BigDecimal finalAmount = amount;
        if (!sameBank) {
            BigDecimal commission = amount.multiply(BigDecimal.valueOf(0.01)); // 1% fee
            finalAmount = amount.add(commission);
        }

        if (from.getBalance().compareTo(finalAmount) < 0)
            throw new RuntimeException("Insufficient balance");

        // Update amounts
        from.setBalance(from.getBalance().subtract(finalAmount));
        //Reciver receives the amount without fees
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        // Register transactions
        Transaction out = new Transaction();
        out.setAccount(from);
        out.setAmount(finalAmount);
        out.setType(TransactionType.TRANSFER_OUT);
        out.setTargetIban(toIban);
        transactionRepository.save(out);

        Transaction in = new Transaction();
        in.setAccount(to);
        in.setAmount(amount);
        in.setType(TransactionType.TRANSFER_IN);
        in.setTargetIban(fromIban);
        transactionRepository.save(in);

        return "Transfer completed successfully";
    }

}
