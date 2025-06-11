package es.nextdigital.demo.domain;

import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.Movement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Getter
@EqualsAndHashCode
public class Account extends AbstractAggregateRoot {
	@NonNull
	private final Iban iban;

	@NonNull
	private final Bank bank;
	@NonNull
	private final List<Card> cards;
	@NonNull
	private final List<Movement> movements;
	@NonNull
	private TotalCredit totalCredit;

	public Account(@NonNull Iban iban, @NonNull Bank bank, @NonNull TotalCredit totalCredit,
			@NonNull List<Movement> movements, @NonNull List<Card> cards) {
		this.iban = iban;
		this.bank = bank;
		this.totalCredit = totalCredit;
		this.movements = movements;
		this.cards = cards;
	}

	public Account withdraw(final Card.Id cardId, final BigDecimal amountToWithdraw, final String originBank) {
		final var card = this.getCardFromAccount(this, cardId)
				.orElseThrow(() -> new IllegalArgumentException("Card not found"));

		final var finalAmountToWithdraw = this.getFinalAmountToWithdrawDependingOnBank(amountToWithdraw, originBank);

		if (card.isLimitExceeded(finalAmountToWithdraw))
			throw new IllegalArgumentException("Amount to withdraw exceeds card limit");
		if (card.isDebitCard() && this.isAmountToWithdrawBiggerThanTotalCredit(finalAmountToWithdraw))
			throw new IllegalArgumentException("Amount to withdraw exceeds total credit");
		if (!card.isActive().value())
			throw new IllegalArgumentException("Card is not active");

		this.totalCredit = new TotalCredit(this.totalCredit.value().subtract(finalAmountToWithdraw));
		this.movements.add(Movement.ofWithdrawal(finalAmountToWithdraw));
		return this;
	}

	private BigDecimal getFinalAmountToWithdrawDependingOnBank(BigDecimal amountToWithdraw, String originBank) {
		return this.getBank().isSameBank(originBank)
				? amountToWithdraw
				: amountToWithdraw.multiply(new BigDecimal("1.02"));
	}

	private boolean isAmountToWithdrawBiggerThanTotalCredit(BigDecimal amountToWithdraw) {
		return this.getTotalCredit().value().compareTo(amountToWithdraw) < 0;
	}

	private Optional<Card> getCardFromAccount(Account account, Card.Id cardId) {
		return account.getCards().stream().filter(card -> card.id().equals(cardId)).findFirst();
	}
	public record Iban(@NonNull String value) {
	}

	public record Bank(@NonNull String value) {
		public boolean isSameBank(String anotherBank) {
			return this.value().equalsIgnoreCase(anotherBank);
		}
	}

	public record TotalCredit(@NonNull BigDecimal value) {
	}
}
