package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.Card;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MoneyDepositCommandHandler {
	private final AccountRepository accountRepository;

	@CommandHandler
	@Transactional
	public Account handle(MoneyDepositCommand command) {
		final var cardId = new Card.Id(command.cardId());
		final var updatedAccount = this.accountRepository.findByCardId(cardId)
				.map(acc -> acc.deposit(cardId, command.amountToDeposit(), command.originBank()))
				.orElseThrow(() -> new IllegalArgumentException("No account found for card " + command.cardId()));

		this.accountRepository.save(updatedAccount);

		return updatedAccount;
	}

}
