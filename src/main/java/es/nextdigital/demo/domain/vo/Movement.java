package es.nextdigital.demo.domain.vo;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

public record Movement(@NonNull Id id, @NonNull MovementType movementType, @NonNull Quantity quantity) {

	public static Movement ofWithdrawal(BigDecimal amountToWithdraw) {
		return new Movement(new Movement.Id(UUID.randomUUID()), MovementType.WITHDRAWAL,
				new Movement.Quantity(amountToWithdraw));
	}

	public static Movement ofDeposit(BigDecimal amountToDeposit) {
		return new Movement(new Movement.Id(UUID.randomUUID()), MovementType.DEPOSIT,
				new Movement.Quantity(amountToDeposit));
	}

	public static Movement ofTransferOut(BigDecimal amountToTransfer) {
		return new Movement(new Movement.Id(UUID.randomUUID()), MovementType.TRANSFER_OUT,
				new Movement.Quantity(amountToTransfer));
	}

	public record Id(@NonNull UUID value) {

	}

	public record Quantity(@NonNull BigDecimal value) {

	}
}