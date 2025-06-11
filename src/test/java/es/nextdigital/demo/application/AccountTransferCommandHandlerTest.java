package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.MovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AccountTransferCommandHandlerTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountTransferCommandHandler sut;

	@Test
	void should_throw_when_account_not_found_for_accountId() {
		final var command = new AccountTransferCommand("ES1234567890", BigDecimal.ONE, "ES99 2100 0418 4502 0005 1332",
				"MyBank");

		doReturn(Optional.empty()).when(this.accountRepository).findById(new Account.Iban(command.accountIban()));

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No account found for iban");
	}

	@Test
	void should_throw_when_destination_iban_not_valid() {
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of());

		final var command = new AccountTransferCommand(account.getIban().value(), BigDecimal.ONE, "ES1234567890",
				"MyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findById(new Account.Iban(command.accountIban()));

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid destinationBank format");
	}

	@Test
	void should_throw_when_transfer_amount_minus_rate_smaller_than_available() {
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.valueOf(100)), List.of(), List.of());

		final var command = new AccountTransferCommand(account.getIban().value(), BigDecimal.valueOf(100),
				"ES99 2100 0418 4502 0005 1332", "NOTMyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findById(new Account.Iban(command.accountIban()));

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Amount to transfer exceeds total credit");
	}

	@Test
	void should_work_when_limit_is_okay() {
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.valueOf(200)), List.of(), List.of());

		final var command = new AccountTransferCommand(account.getIban().value(), BigDecimal.valueOf(100),
				"ES99 2100 0418 4502 0005 1332", "NOTMyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findById(new Account.Iban(command.accountIban()));

		final var result = this.sut.handle(command);

		assertThat(result.getMovements()).hasSize(1);
		assertThat(result.getMovements().get(0).movementType()).isEqualTo(MovementType.TRANSFER_OUT);
		assertThat(result.getMovements().get(0).quantity().value().compareTo(BigDecimal.valueOf(102))).isZero();
		assertThat(result.getTotalCredit().value().compareTo(BigDecimal.valueOf(98))).isZero();
	}
}