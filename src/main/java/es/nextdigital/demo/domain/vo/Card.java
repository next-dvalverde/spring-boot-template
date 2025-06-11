package es.nextdigital.demo.domain.vo;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

public record Card(@NonNull Id id, @NonNull CardType cardType, @NonNull Limit limit, @NonNull IsActive isActive) {

	public record Id(@NonNull UUID value) {

	}
	public record Limit(@NonNull BigDecimal value) {

	}

	public record IsActive(@NonNull boolean value) {

	}
}