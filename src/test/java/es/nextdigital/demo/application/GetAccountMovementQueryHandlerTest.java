package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.CardType;
import es.nextdigital.demo.domain.vo.Movement;
import es.nextdigital.demo.domain.vo.MovementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GetAccountMovementQueryHandlerTest {
	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private GetAccountMovementQueryHandler sut;

	@Test
	void should_return_movements_when_account_exists() {
		final var accountIban = "ES1234567890123456789012";
		final var query = new GetAccountMovementQuery(accountIban);
		final var account = new Account(new Account.Iban(accountIban), new Account.Bank("bank"),
				new Account.TotalCredit(BigDecimal.TEN),
				List.of(new Movement(new Movement.Id(UUID.randomUUID()), MovementType.DEPOSIT,
						new Movement.Quantity(BigDecimal.TEN))),
				List.of(new Card(new Card.Id(UUID.randomUUID()), CardType.CREDIT, new Card.Limit(BigDecimal.TEN),
						new Card.IsActive(true))));

		doReturn(Optional.of(account)).when(this.accountRepository).findById(new Account.Iban(accountIban));

		final var result = this.sut.handle(query);

		assertThat(result).hasSameSizeAs(account.getMovements());
		assertThat(result.stream().findFirst().get()).isEqualTo(account.getMovements().stream().findFirst().get());
	}

	@Test
	void should_throw_when_account_doesnt_exist() {
		final var accountIban = "ES1234567890123456789012";
		final var query = new GetAccountMovementQuery(accountIban);

		doReturn(Optional.empty()).when(this.accountRepository).findById(new Account.Iban(accountIban));

		assertThatThrownBy(() -> this.sut.handle(query)).isInstanceOf(IllegalArgumentException.class);
	}
}