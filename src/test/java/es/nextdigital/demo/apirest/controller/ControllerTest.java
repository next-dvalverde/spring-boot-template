package es.nextdigital.demo.apirest.controller;

import es.nextdigital.demo.application.GetAccountMovementQuery;
import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.vo.Movement;
import es.nextdigital.demo.domain.vo.MovementType;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
class ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommandGateway commandGateway;

	@MockBean
	private QueryGateway queryGateway;

	@Test
	void getMovements_shouldReturnListOfMovements() throws Exception {
		final var accountIban = "iban123";
		final var mockMovement = new Movement(new Movement.Id(UUID.randomUUID()), MovementType.DEPOSIT,
				new Movement.Quantity(BigDecimal.TEN));
		when(this.queryGateway.query(new GetAccountMovementQuery(accountIban),
				ResponseTypes.multipleInstancesOf(Movement.class)))
				.thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of(mockMovement)));

		this.mockMvc.perform(get("/api/movements/" + accountIban)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].movementType").value("DEPOSIT"))
				.andExpect(jsonPath("$[0].quantity").value(BigDecimal.TEN));
	}

	@Test
	void moneyWithdrawalPost_shouldReturnCreatedResponse() throws Exception {
		final UUID cardId = UUID.randomUUID();
		final Account account = new Account(new Account.Iban("iban123"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of());

		when(this.commandGateway.sendAndWait(any())).thenReturn(account);

		this.mockMvc
				.perform(
						post("/api/card/" + cardId + "/withdrawals").contentType(MediaType.APPLICATION_JSON).content("""
								{
								  "quantityToWithdraw": 50,
								  "bank": "BANK1"
								}
								"""))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.accountIban").value(account.getIban().value()));
	}

	@Test
	void moneyDepositPost_shouldReturnCreatedResponse() throws Exception {
		final UUID cardId = UUID.randomUUID();
		final Account account = new Account(new Account.Iban("iban123"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of());

		when(this.commandGateway.sendAndWait(any())).thenReturn(account);

		this.mockMvc
				.perform(post("/api/card/" + cardId + "/deposits").contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "quantityToDeposit": 150,
						  "bank": "BANK2"
						}
						""")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountIban").value(account.getIban().value()));
	}

	@Test
	void moneyTransferPost_shouldReturnCreatedResponse() throws Exception {
		final var accountId = "acc123";
		final var account = new Account(new Account.Iban("iban123"), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(), List.of());

		when(this.commandGateway.sendAndWait(any())).thenReturn(account);

		this.mockMvc
				.perform(post("/api/account/" + accountId + "/transfers").contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "quantityToTransfer": 200,
								  "destinationIban": "iban789",
								  "destinationBank": "BANK3"
								}
								"""))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.accountIban").value(account.getIban().value()));
	}

	@Test
	void handleIllegalArgumentException_shouldReturnBadRequest() throws Exception {
		when(this.commandGateway.sendAndWait(any())).thenThrow(new IllegalArgumentException("Invalid input"));

		this.mockMvc.perform(post("/api/card/" + UUID.randomUUID() + "/withdrawals")
				.contentType(MediaType.APPLICATION_JSON).content("""
						{
						  "quantityToWithdraw": 0,
						  "bank": "BANK1"
						}
						""")).andExpect(status().isBadRequest()).andExpect(content().string("Invalid input"));
	}
}