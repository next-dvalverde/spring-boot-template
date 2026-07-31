package es.nextdigital.atm.web.dto;

import es.nextdigital.atm.service.OperationService.Receipt;
import java.math.BigDecimal;
import java.util.UUID;

// balanceAfter es null en operaciones de credito: la cuenta no se ha movido y
// el recibo no debe sugerir lo contrario.
public record ReceiptResponse(UUID operationId, BigDecimal amount, BigDecimal fee, BigDecimal balanceAfter) {

    public static ReceiptResponse from(Receipt receipt) {
        return new ReceiptResponse(receipt.operationId(), receipt.amount(), receipt.fee(), receipt.balanceAfter());
    }
}
