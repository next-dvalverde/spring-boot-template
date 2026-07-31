package es.nextdigital.atm.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.nextdigital.atm.AbstractIntegrationTest;
import es.nextdigital.atm.domain.MovementType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Los 7 escenarios de PLAN.md &sect;6, en orden deliberado: el estado fluye de
 * uno al siguiente (la tarjeta que se activa en el 1 es la que agota el saldo
 * en el 2) y las dos invariantes se recalculan desde los saldos sembrados
 * despues de cada operacion de dinero, de modo que ningun escenario depende de
 * cifras absolutas que otro pueda haber movido.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndScenariosTest extends AbstractIntegrationTest {

    // Fixture de V2__seed.sql
    private static final String CARD_DEBIT = "4001000000000001";
    private static final String PIN_DEBIT = "1975";
    private static final String CARD_CREDIT = "4001000000000002";
    private static final String PIN_CREDIT = "2846";
    private static final String CARD_NEW = "4001000000000003";
    private static final String PIN_ENVELOPE = "4821";
    private static final String PIN_NEW = "8642";
    private static final String ATM_OWN = "50000000-0000-0000-0000-000000000001";
    private static final String ATM_FOREIGN = "50000000-0000-0000-0000-000000000002";
    private static final String ACCOUNT_A1 = "30000000-0000-0000-0000-000000000001";
    private static final String ACCOUNT_A2 = "30000000-0000-0000-0000-000000000002";
    private static final String ACCOUNT_B1 = "30000000-0000-0000-0000-000000000003";
    private static final String CARD_CREDIT_ID = "40000000-0000-0000-0000-000000000002";
    private static final String IBAN_A2 = "ES7515000001000000000002";
    private static final String IBAN_EXTERNAL = "GB82WEST12345698765432";
    private static final Map<String, String> SEEDED_BALANCES = Map.of(
            ACCOUNT_A1, "1000.00",
            ACCOUNT_A2, "500.00",
            ACCOUNT_B1, "250.00");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @Order(1)
    void scenario1_newCardLifecycle() throws Exception {
        // Operar sin activar: abrir sesion ya es operar, y la guarda 2 corta.
        postSession(CARD_NEW, PIN_ENVELOPE, ATM_OWN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CARD_NOT_ACTIVATED"));

        mockMvc.perform(post("/api/v1/cards/activation").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(CARD_NEW, PIN_ENVELOPE, ATM_OWN)))
                .andExpect(status().isOk());

        // Reactivar es un error distinto de "no activada" (B-11).
        mockMvc.perform(post("/api/v1/cards/activation").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(CARD_NEW, PIN_ENVELOPE, ATM_OWN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CARD_ALREADY_ACTIVE"));

        String token = tokenExpecting(CARD_NEW, PIN_ENVELOPE, ATM_OWN, true);

        // El PIN del sobre no opera: solo deja llegar al cambio de PIN.
        withdraw(token, "20.00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PIN_CHANGE_REQUIRED"));

        mockMvc.perform(put("/api/v1/cards/pin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\":\"%s\",\"newPin\":\"%s\"}".formatted(PIN_ENVELOPE, PIN_NEW)))
                .andExpect(status().isNoContent());

        JsonNode receipt = json(withdraw(token, "20.00").andExpect(status().isOk()));
        assertMoney(new BigDecimal("0.00"), receipt.get("fee").decimalValue());
        assertAllInvariants();
    }

    @Test
    @Order(2)
    void scenario2_debitWithdrawal() throws Exception {
        String token = token(CARD_DEBIT, PIN_DEBIT, ATM_OWN);
        BigDecimal before = balanceOf(ACCOUNT_A1);

        JsonNode receipt = json(withdraw(token, "100.00").andExpect(status().isOk()));
        assertMoney(before.subtract(new BigDecimal("100.00")), receipt.get("balanceAfter").decimalValue());
        assertEquals(1, movementsOf(receipt.get("operationId").asText()).size());
        assertAllInvariants();

        // Por encima del limite por operacion, aunque haya saldo.
        withdraw(token, "500.01")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LIMIT_EXCEEDED"));

        // Sin saldo: la tarjeta del escenario 1 dejo su cuenta en 480.
        String lowBalanceToken = token(CARD_NEW, PIN_NEW, ATM_OWN);
        withdraw(lowBalanceToken, "490.00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));

        // Un rechazo no mueve dinero.
        assertAllInvariants();
    }

    @Test
    @Order(3)
    void scenario3_foreignAtmWithdrawalCreatesFeeMovement() throws Exception {
        String token = token(CARD_DEBIT, PIN_DEBIT, ATM_FOREIGN);
        BigDecimal before = balanceOf(ACCOUNT_A1);

        JsonNode receipt = json(withdraw(token, "100.00").andExpect(status().isOk()));
        // Tarifa del banco del cajero: 1.50 + 1% de 100.00 = 2.50.
        assertMoney(new BigDecimal("2.50"), receipt.get("fee").decimalValue());
        assertMoney(before.subtract(new BigDecimal("102.50")), receipt.get("balanceAfter").decimalValue());

        // Dos apuntes enlazados por operationId: el extracto explica la comision.
        List<Map<String, Object>> lines = movementsOf(receipt.get("operationId").asText());
        assertEquals(Set.of("WITHDRAWAL", "FEE"), typesOf(lines));
        assertAllInvariants();
    }

    @Test
    @Order(4)
    void scenario4_creditWithdrawalDoesNotTouchTheAccount() throws Exception {
        String token = token(CARD_CREDIT, PIN_CREDIT, ATM_OWN);
        BigDecimal accountBefore = balanceOf(ACCOUNT_A1);
        BigDecimal usedBefore = creditUsedOf(CARD_CREDIT_ID);

        JsonNode receipt = json(withdraw(token, "200.00").andExpect(status().isOk()));
        assertTrue(receipt.get("balanceAfter").isNull());

        assertMoney(accountBefore, balanceOf(ACCOUNT_A1));
        assertMoney(usedBefore.add(new BigDecimal("200.00")), creditUsedOf(CARD_CREDIT_ID));

        // Sus apuntes no liquidan: settles_account = false y balance_after NULL.
        for (Map<String, Object> line : movementsOf(receipt.get("operationId").asText())) {
            assertEquals(false, line.get("settles_account"));
            assertEquals(null, line.get("balance_after"));
        }

        // Tope de credito respetado: 200 usados + 400 > 500.
        withdraw(token, "400.00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CREDIT_LIMIT_EXCEEDED"));
        assertAllInvariants();
    }

    @Test
    @Order(5)
    void scenario5_deposits() throws Exception {
        String token = token(CARD_DEBIT, PIN_DEBIT, ATM_OWN);
        BigDecimal before = balanceOf(ACCOUNT_A1);

        JsonNode receipt = json(deposit(token, "50.00").andExpect(status().isOk()));
        assertMoney(before.add(new BigDecimal("50.00")), receipt.get("balanceAfter").decimalValue());
        assertAllInvariants();

        // En cajero ajeno no se ingresa efectivo.
        String foreignToken = token(CARD_DEBIT, PIN_DEBIT, ATM_FOREIGN);
        deposit(foreignToken, "50.00")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FOREIGN_ATM_DEPOSIT_NOT_ALLOWED"));

        // Con credito seria una amortizacion, y eso es C-7.
        String creditToken = token(CARD_CREDIT, PIN_CREDIT, ATM_OWN);
        deposit(creditToken, "50.00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPERATION_NOT_SUPPORTED_FOR_CARD_TYPE"));
        assertAllInvariants();
    }

    @Test
    @Order(6)
    void scenario6_transfers() throws Exception {
        String token = token(CARD_DEBIT, PIN_DEBIT, ATM_OWN);
        BigDecimal originBefore = balanceOf(ACCOUNT_A1);
        BigDecimal targetBefore = balanceOf(ACCOUNT_A2);

        // Interna (mismo banco): sin comision, origen y destino cuadran.
        JsonNode receipt = json(transfer(token, IBAN_A2, "100.00", "rent").andExpect(status().isOk()));
        assertMoney(new BigDecimal("0.00"), receipt.get("fee").decimalValue());
        assertMoney(originBefore.subtract(new BigDecimal("100.00")), balanceOf(ACCOUNT_A1));
        assertMoney(targetBefore.add(new BigDecimal("100.00")), balanceOf(ACCOUNT_A2));
        List<Map<String, Object>> lines = movementsOf(receipt.get("operationId").asText());
        assertEquals(Set.of("TRANSFER_OUT", "TRANSFER_IN"), typesOf(lines));
        assertAllInvariants();

        transfer(token, "ES0000000000000000000000", "10.00", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_IBAN"));

        // Externa: comision del banco origen (2.50 + 0.25% de 100 = 2.75) y
        // solo se registra la salida (C-5).
        BigDecimal beforeExternal = balanceOf(ACCOUNT_A1);
        JsonNode external = json(transfer(token, IBAN_EXTERNAL, "100.00", null).andExpect(status().isOk()));
        assertMoney(new BigDecimal("2.75"), external.get("fee").decimalValue());
        assertMoney(beforeExternal.subtract(new BigDecimal("102.75")), balanceOf(ACCOUNT_A1));
        List<Map<String, Object>> externalLines = movementsOf(external.get("operationId").asText());
        assertEquals(Set.of("TRANSFER_OUT", "FEE"), typesOf(externalLines));
        assertEquals(IBAN_EXTERNAL, externalLines.stream()
                .filter(l -> l.get("type").equals("TRANSFER_OUT"))
                .findFirst().orElseThrow().get("counterparty_iban"));
        assertAllInvariants();
    }

    @Test
    @Order(7)
    void scenario7_movementsAndOwnership() throws Exception {
        String token = token(CARD_DEBIT, PIN_DEBIT, ATM_OWN);

        // El titular ve sus dos cuentas, y solo las suyas.
        JsonNode accounts = json(mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
        assertEquals(Set.of(ACCOUNT_A1, ACCOUNT_A2),
                accounts.findValues("id").stream().map(JsonNode::asText).collect(Collectors.toSet()));

        // El extracto de A1 acumula todos los tipos de salida y el de A2 el de
        // entrada; amount siempre positivo, el signo lo cuenta type.
        JsonNode a1 = json(mockMvc.perform(get("/api/v1/accounts/{id}/movements", ACCOUNT_A1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
        Set<String> a1Types = a1.get("content").findValues("type").stream()
                .map(JsonNode::asText).collect(Collectors.toSet());
        assertTrue(a1Types.containsAll(Set.of("WITHDRAWAL", "FEE", "DEPOSIT", "TRANSFER_OUT")));
        JsonNode a2 = json(mockMvc.perform(get("/api/v1/accounts/{id}/movements", ACCOUNT_A2)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()));
        assertTrue(a2.get("content").findValues("type").stream()
                .map(JsonNode::asText).collect(Collectors.toSet()).contains("TRANSFER_IN"));

        // Paginacion.
        json(mockMvc.perform(get("/api/v1/accounts/{id}/movements", ACCOUNT_A1)
                        .param("page", "0").param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)));

        // Cuenta de otro titular: 404, no 403 — el mismo error que si no
        // existiera (B-9). Un 403 confirmaria que existe.
        mockMvc.perform(get("/api/v1/accounts/{id}/movements", ACCOUNT_B1)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    // ---- helpers ----

    private String credentials(String cardNumber, String pin, String atmId) {
        return "{\"cardNumber\":\"%s\",\"pin\":\"%s\",\"atmId\":\"%s\"}".formatted(cardNumber, pin, atmId);
    }

    private ResultActions postSession(String cardNumber, String pin, String atmId) throws Exception {
        return mockMvc.perform(post("/api/v1/sessions").contentType(MediaType.APPLICATION_JSON)
                .content(credentials(cardNumber, pin, atmId)));
    }

    private String token(String cardNumber, String pin, String atmId) throws Exception {
        return tokenExpecting(cardNumber, pin, atmId, false);
    }

    private String tokenExpecting(String cardNumber, String pin, String atmId,
                                  boolean pinChangeRequired) throws Exception {
        JsonNode body = json(postSession(cardNumber, pin, atmId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinChangeRequired").value(pinChangeRequired)));
        return body.get("token").asText();
    }

    private ResultActions withdraw(String token, String amount) throws Exception {
        return operation(token, "withdrawals", "{\"amount\":" + amount + "}");
    }

    private ResultActions deposit(String token, String amount) throws Exception {
        return operation(token, "deposits", "{\"amount\":" + amount + "}");
    }

    private ResultActions transfer(String token, String iban, String amount, String concept) throws Exception {
        String body = concept == null
                ? "{\"targetIban\":\"%s\",\"amount\":%s}".formatted(iban, amount)
                : "{\"targetIban\":\"%s\",\"amount\":%s,\"concept\":\"%s\"}".formatted(iban, amount, concept);
        return operation(token, "transfers", body);
    }

    private ResultActions operation(String token, String path, String body) throws Exception {
        return mockMvc.perform(post("/api/v1/operations/" + path)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private JsonNode json(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private BigDecimal balanceOf(String accountId) {
        return jdbc.queryForObject("SELECT balance FROM account WHERE id = ?::uuid",
                BigDecimal.class, accountId);
    }

    private BigDecimal creditUsedOf(String cardId) {
        return jdbc.queryForObject("SELECT credit_used FROM card WHERE id = ?::uuid",
                BigDecimal.class, cardId);
    }

    private List<Map<String, Object>> movementsOf(String operationId) {
        return jdbc.queryForList("SELECT * FROM movement WHERE operation_id = ?::uuid", operationId);
    }

    private Set<String> typesOf(List<Map<String, Object>> lines) {
        return lines.stream().map(l -> (String) l.get("type")).collect(Collectors.toSet());
    }

    /**
     * Las dos aserciones transversales de PLAN.md &sect;6, recalculadas desde
     * los saldos sembrados sobre TODO el historial: valen en cualquier punto de
     * la suite, muevan lo que muevan los escenarios anteriores. El signo sale
     * de signOf — el mismo switch del dominio, no una copia en SQL.
     */
    private void assertAllInvariants() {
        SEEDED_BALANCES.forEach((accountId, seeded) -> {
            BigDecimal settled = jdbc.queryForList(
                            "SELECT type, amount FROM movement WHERE account_id = ?::uuid AND settles_account = TRUE",
                            accountId).stream()
                    .map(this::signedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertMoney(new BigDecimal(seeded).add(settled), balanceOf(accountId));
        });
        for (String cardId : jdbc.queryForList("SELECT id::text FROM card", String.class)) {
            BigDecimal drawn = jdbc.queryForList(
                            "SELECT type, amount FROM movement WHERE card_id = ?::uuid AND settles_account = FALSE",
                            cardId).stream()
                    .map(this::signedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertMoney(drawn.negate(), creditUsedOf(cardId));
        }
    }

    private BigDecimal signedAmount(Map<String, Object> movement) {
        MovementType type = MovementType.valueOf((String) movement.get("type"));
        return ((BigDecimal) movement.get("amount"))
                .multiply(BigDecimal.valueOf(MovementType.signOf(type)));
    }

    private void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }
}
