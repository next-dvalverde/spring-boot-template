package es.nextdigital.demo.domain;

import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.Movement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.List;

@Getter
@EqualsAndHashCode
public class Account extends AbstractAggregateRoot {
	@NonNull
	private final Iban iban;

	@NonNull
	private final Bank bank;

	@NonNull
	private final TotalCredit totalCredit;

	@NonNull
	private final List<Card> cards;

	@NonNull
	private final List<Movement> movements;

	public Account(@NonNull Iban iban, @NonNull Bank bank, @NonNull TotalCredit totalCredit,
			@NonNull List<Movement> movements, @NonNull List<Card> cards) {
		this.iban = iban;
		this.bank = bank;
		this.totalCredit = totalCredit;
		this.movements = movements;
		this.cards = cards;
	}

	public record Iban(@NonNull String value) {
	}

	public record Bank(@NonNull String value) {
	}

	public record TotalCredit(@NonNull BigDecimal value) {
	}
}
