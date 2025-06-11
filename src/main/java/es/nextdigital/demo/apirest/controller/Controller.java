package es.nextdigital.demo.apirest.controller;

import es.nextdigital.demo.apirest.dto.*;
import es.nextdigital.demo.application.AccountTransferCommand;
import es.nextdigital.demo.application.GetAccountMovementQuery;
import es.nextdigital.demo.application.MoneyDepositCommand;
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

	@RequestMapping(method = RequestMethod.POST, value = "/card/{cardId}/deposits", produces = {
			"application/json"}, consumes = {"application/json"})
	ResponseEntity<MoneyDepositResponseDTO> moneyDepositPost(@PathVariable("cardId") String cardId,
			@RequestBody MoneyDepositRequestDTO moneyDepositRequestDTO) {
		final var result = (Account) this.commandGateway.sendAndWait(new MoneyDepositCommand(UUID.fromString(cardId),
				moneyDepositRequestDTO.quantityToDeposit(), moneyDepositRequestDTO.bank()));
		final var response = new MoneyDepositResponseDTO(result.getIban().value());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/account/{accountId}/transfers", produces = {
			"application/json"}, consumes = {"application/json"})
	ResponseEntity<AccountTransferResponseDTO> moneyDepositPost(@PathVariable("accountId") String accountId,
			@RequestBody AccountTransferRequestDTO accountTransferRequestDTO) {
		final var result = (Account) this.commandGateway
				.sendAndWait(new AccountTransferCommand(accountId, accountTransferRequestDTO.quantityToTransfer(),
						accountTransferRequestDTO.destinationIban(), accountTransferRequestDTO.destinationBank()));
		final var response = new AccountTransferResponseDTO(result.getIban().value());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}
}