package es.nextdigital.demo.apirest.controller;

import es.nextdigital.demo.apirest.dto.MoneyWithdrawalRequestDTO;
import es.nextdigital.demo.apirest.dto.MoneyWithdrawalResponseDTO;
import es.nextdigital.demo.apirest.dto.MovementDTO;
import es.nextdigital.demo.application.GetAccountMovementQuery;
import es.nextdigital.demo.application.MoneyWithdrawalCommand;
import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.vo.Movement;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class Controller {
	private final QueryGateway queryGateway;
	private final CommandGateway commandGateway;

	@RequestMapping(method = RequestMethod.GET, value = "/movements/{accountIban}", produces = {"application/json"})
	ResponseEntity<List<MovementDTO>> getMovements(@PathVariable("accountIban") String accountIban) {
		final var result = this.queryGateway
				.query(new GetAccountMovementQuery(accountIban), ResponseTypes.multipleInstancesOf(Movement.class))
				.join().stream().map(this::getMovementDTO).toList();
		return ResponseEntity.ok(result);
	}

	private MovementDTO getMovementDTO(Movement mov) {
		return new MovementDTO(mov.movementType().name(), mov.quantity().value());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/card/{cardId}/withdrawals", produces = {
			"application/json"}, consumes = {"application/json"})
	ResponseEntity<MoneyWithdrawalResponseDTO> moneyWithdrawalPost(@PathVariable("cardId") String cardId,
			@RequestBody MoneyWithdrawalRequestDTO moneyWithdrawalRequestDTO) {
		final var result = (Account) this.commandGateway.sendAndWait(new MoneyWithdrawalCommand(UUID.fromString(cardId),
				moneyWithdrawalRequestDTO.quantityToWithdraw(), moneyWithdrawalRequestDTO.bank()));
		final var response = new MoneyWithdrawalResponseDTO(result.getIban().value());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}