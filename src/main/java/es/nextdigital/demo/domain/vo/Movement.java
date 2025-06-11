package es.nextdigital.demo.domain.vo;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

public record Movement(@NonNull Id id, @NonNull MovementType movementType, @NonNull Quantity quantity) {

	public static Movement ofWithdrawal(BigDecimal amount) {
		return new Movement(new Movement.Id(UUID.randomUUID()), MovementType.WITHDRAWAL, new Movement.Quantity(amount));
	}

	public record Id(@NonNull UUID value) {

	}

	public record Quantity(@NonNull BigDecimal value) {

	}
}