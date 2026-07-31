package es.nextdigital.atm.web.dto;

import es.nextdigital.atm.domain.Movement;
import es.nextdigital.atm.domain.MovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// amount siempre positivo; la direccion la da type (signOf, PLAN.md &sect;2).
// El extracto expone la convencion, no un signo precalculado.
public record MovementResponse(UUID id, UUID operationId, MovementType type, BigDecimal amount,
                               boolean settlesAccount, BigDecimal balanceAfter, String description,
                               String counterpartyIban, Instant createdAt) {

    public static MovementResponse from(Movement movement) {
        return new MovementResponse(movement.getId(), movement.getOperationId(), movement.getType(),
                movement.getAmount(), movement.isSettlesAccount(), movement.getBalanceAfter(),
                movement.getDescription(), movement.getCounterpartyIban(), movement.getCreatedAt());
    }
}
