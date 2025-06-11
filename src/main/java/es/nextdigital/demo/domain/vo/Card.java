package es.nextdigital.demo.domain.vo;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

public record Card(@NonNull Id id, @NonNull CardType cardType, @NonNull Limit limit, @NonNull IsActive isActive) {

	public boolean isLimitExceeded(BigDecimal amount) {
		return this.limit().value().compareTo(amount) < 0;
	}

	public boolean isCreditCard() {
		return this.cardType().equals(CardType.CREDIT);
	}

	public boolean isDebitCard() {
		return this.cardType().equals(CardType.DEBIT);
	}

	public record Id(@NonNull UUID value) {

	}

	public record Limit(@NonNull BigDecimal value) {

	}

	public record IsActive(@NonNull Boolean value) {
	}
}