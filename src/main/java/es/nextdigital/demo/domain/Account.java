package es.nextdigital.demo.domain;

import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.Movement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.ArrayList;
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
	private List<Movement> movements;

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

		if (!card.isActive().value())
			throw new IllegalArgumentException("Card is not active");
		if (card.isLimitExceeded(finalAmountToWithdraw))
			throw new IllegalArgumentException("Amount to withdraw exceeds card limit");
		if (card.isDebitCard() && this.isAmountToSubstractBiggerThanTotalCredit(finalAmountToWithdraw))
			throw new IllegalArgumentException("Amount to withdraw exceeds total credit");

		this.totalCredit = new TotalCredit(this.totalCredit.value().subtract(finalAmountToWithdraw));
		this.movements = this.addMovement(Movement.ofWithdrawal(finalAmountToWithdraw));
		return this;
	}

	private List<Movement> addMovement(Movement movement) {
		final List<Movement> updatedMovements = new ArrayList<>(this.movements);
		updatedMovements.add(movement);
		return updatedMovements;
	}

	private BigDecimal getFinalAmountToWithdrawDependingOnBank(BigDecimal amountToWithdraw, String originBank) {
		return this.getBank().isSameBank(originBank)
				? amountToWithdraw
				: amountToWithdraw.multiply(new BigDecimal("1.02"));
	}

	private boolean isAmountToSubstractBiggerThanTotalCredit(BigDecimal amountToWithdraw) {
		return this.getTotalCredit().value().compareTo(amountToWithdraw) < 0;
	}

	public Account deposit(Card.Id cardId, BigDecimal amountToDeposit, String originBank) {
		final var card = this.getCardFromAccount(this, cardId)
				.orElseThrow(() -> new IllegalArgumentException("Card not found"));

		if (!card.isActive().value())
			throw new IllegalArgumentException("Card is not active");

		if (!this.getBank().isSameBank(originBank))
			throw new IllegalArgumentException("Bank should be the same as the card's bank");

		this.totalCredit = new TotalCredit(this.totalCredit.value().add(amountToDeposit));
		this.movements = this.addMovement(Movement.ofDeposit(amountToDeposit));
		return this;
	}

	private Optional<Card> getCardFromAccount(Account account, Card.Id cardId) {
		return account.getCards().stream().filter(card -> card.id().equals(cardId)).findFirst();
	}

	public Account transfer(Iban iban, Bank destinationBank, BigDecimal amountToTransfer) {
		if (!iban.isFormatValidForSpain())
			throw new IllegalArgumentException("Invalid destinationBank format");
		final var finalAmountToTransfer = this.getFinalAmountToTransferDependingOnBank(destinationBank,
				amountToTransfer);
		if (this.isAmountToSubstractBiggerThanTotalCredit(finalAmountToTransfer))
			throw new IllegalArgumentException("Amount to transfer exceeds total credit");
		this.totalCredit = new TotalCredit(this.totalCredit.value().subtract(finalAmountToTransfer));
		this.movements = this.addMovement(Movement.ofTransferOut(finalAmountToTransfer));
		return this;
	}

	private BigDecimal getFinalAmountToTransferDependingOnBank(Bank destinationBank, BigDecimal amountToTransfer) {
		return this.bank.isSameBank(destinationBank.value)
				? amountToTransfer
				: amountToTransfer.add(amountToTransfer.multiply(new BigDecimal("0.02")));
	}

	public record Iban(@NonNull String value) {
		public boolean isFormatValidForSpain() {
			if (this.value == null)
				return false;
			final String trimmed = this.value.replaceAll("\\s+", "").toUpperCase();
			return trimmed.matches("^ES\\d{22}$");

		}
	}

	public record Bank(@NonNull String value) {
		public boolean isSameBank(String anotherBank) {
			return this.value().equalsIgnoreCase(anotherBank);
		}
	}

	public record TotalCredit(@NonNull BigDecimal value) {
	}
}
