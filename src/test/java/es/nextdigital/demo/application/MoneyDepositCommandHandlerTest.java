package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.CardType;
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
class MoneyDepositCommandHandlerTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private MoneyDepositCommandHandler sut;

	@Test
	void should_throw_when_account_not_found_for_cardId() {
		final var cardId = new Card.Id(UUID.randomUUID());
		final var command = new MoneyDepositCommand(cardId.value(), BigDecimal.ONE, "MyBank");

		doReturn(Optional.empty()).when(this.accountRepository).findByCardId(cardId);

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void should_throw_when_card_not_found_in_account() {
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of());
		final var cardId = new Card.Id(UUID.randomUUID());
		final var command = new MoneyDepositCommand(cardId.value(), BigDecimal.ONE, "MyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findByCardId(cardId);

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Card not found");
	}

	@Test
	void should_throw_when_card_isnt_active() {
		final var card = new Card(new Card.Id(UUID.randomUUID()), CardType.CREDIT,
				new Card.Limit(BigDecimal.valueOf(1000)), new Card.IsActive(false));
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of(card));
		final var cardId = card.id();
		final var command = new MoneyDepositCommand(cardId.value(), BigDecimal.valueOf(100), "MyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findByCardId(cardId);

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("not active");
	}

	@Test
	void should_throw_when_card_is_different_bank() {
		final var card = new Card(new Card.Id(UUID.randomUUID()), CardType.CREDIT,
				new Card.Limit(BigDecimal.valueOf(1000)), new Card.IsActive(true));
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("NOTMyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of(card));
		final var cardId = card.id();
		final var command = new MoneyDepositCommand(cardId.value(), BigDecimal.valueOf(100), "MyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findByCardId(cardId);

		assertThatThrownBy(() -> this.sut.handle(command)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Bank should be the same");
	}

	@Test
	void should_deposit_money_requested() {
		final var card = new Card(new Card.Id(UUID.randomUUID()), CardType.CREDIT,
				new Card.Limit(BigDecimal.valueOf(1000)), new Card.IsActive(true));
		final var account = new Account(new Account.Iban("ES1234567890"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of(card));
		final var cardId = card.id();
		final var command = new MoneyDepositCommand(cardId.value(), BigDecimal.valueOf(100), "MyBank");

		doReturn(Optional.of(account)).when(this.accountRepository).findByCardId(cardId);

		final var result = this.sut.handle(command);

		assertThat(result.getMovements()).hasSize(1);
		assertThat(result.getMovements().get(0).movementType()).isEqualTo(MovementType.DEPOSIT);
		assertThat(result.getMovements().get(0).quantity().value().compareTo(command.amountToDeposit())).isZero();
		assertThat(result.getTotalCredit().value().compareTo(BigDecimal.valueOf(110))).isZero();
	}

}