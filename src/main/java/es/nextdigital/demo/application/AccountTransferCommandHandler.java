package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AccountTransferCommandHandler {
	private final AccountRepository accountRepository;

	@CommandHandler
	@Transactional
	public Account handle(AccountTransferCommand command) {
		final var updatedAccount = this.accountRepository.findById(new Account.Iban(command.accountIban()))
				.map(account -> account.transfer(new Account.Iban(command.destinationIban()),
						new Account.Bank(command.destinationBank()), command.amountToTransfer()))
				.orElseThrow(() -> new IllegalArgumentException("No account found for iban " + command.accountIban()));

		this.accountRepository.save(updatedAccount);

		return updatedAccount;
	}

}
