package es.nextdigital.demo.service.impl;

import es.nextdigital.demo.dto.OperacionRetiroDTO;
import es.nextdigital.demo.entity.Cuenta;
import es.nextdigital.demo.entity.Movimiento;
import es.nextdigital.demo.entity.Tarjeta;
import es.nextdigital.demo.enums.TipoTarjeta;
import es.nextdigital.demo.repository.CuentaRepository;
import es.nextdigital.demo.repository.MovimientoRepository;
import es.nextdigital.demo.repository.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * The type Operacion service impl test.
 */
class OperacionServiceImplTest {

    @InjectMocks
    private OperacionServiceImpl operacionService;

    @Mock
    private TarjetaRepository tarjetaRepo;

    @Mock
    private CuentaRepository cuentaRepo;

    @Mock
    private MovimientoRepository movimientoRepo;

    private Tarjeta tarjetaDebito;
    private Cuenta cuenta;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setIban("ES0000000000000000000001");

        tarjetaDebito = new Tarjeta();
        tarjetaDebito.setId(1L);
        tarjetaDebito.setTipo(TipoTarjeta.DEBITO);
        tarjetaDebito.setActiva(true);
        tarjetaDebito.setLimite(new BigDecimal("500.00"));
        tarjetaDebito.setCuenta(cuenta);
    }

    /**
     * Retirar with debit card and sufficient funds should subtract amount.
     */
    @Test
    void retirar_withDebitCardAndSufficientFunds_shouldSubtractAmount() {
        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        dto.tarjetaId = 1L;
        dto.cantidad = new BigDecimal("200.00");
        dto.cajeroPropio = true;

        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjetaDebito));
        when(cuentaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimientoRepo.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));

        operacionService.retirar(dto);

        // Verificar que el saldo se ha reducido
        assertEquals(new BigDecimal("800.00"), cuenta.getSaldo());

        verify(cuentaRepo, times(1)).save(cuenta);
        verify(movimientoRepo, atLeastOnce()).save(any(Movimiento.class));
    }

    /**
     * Retirar with debit card and insufficient funds should throw.
     */
    @Test
    void retirar_withDebitCardAndInsufficientFunds_shouldThrow() {
        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        dto.tarjetaId = 1L;
        dto.cantidad = new BigDecimal("2000.00");
        dto.cajeroPropio = true;

        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjetaDebito));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> operacionService.retirar(dto));
        assertEquals("Saldo insuficiente", ex.getMessage());

        verify(cuentaRepo, never()).save(any());
        verify(movimientoRepo, never()).save(any());
    }

    /**
     * Retirar with credit card and within limit should subtract amount.
     */
    @Test
    void retirar_withCreditCardAndWithinLimit_shouldSubtractAmount() {
        tarjetaDebito.setTipo(TipoTarjeta.CREDITO);
        tarjetaDebito.setLimite(new BigDecimal("1000.00"));
        cuenta.setSaldo(new BigDecimal("500.00"));

        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        dto.tarjetaId = 1L;
        dto.cantidad = new BigDecimal("900.00");
        dto.cajeroPropio = true;

        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjetaDebito));
        when(cuentaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimientoRepo.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));

        operacionService.retirar(dto);

        BigDecimal expectedSaldo = new BigDecimal("500.00").subtract(new BigDecimal("900.00"));
        assertEquals(expectedSaldo, cuenta.getSaldo());

        verify(cuentaRepo, times(1)).save(cuenta);
    }

    /**
     * Retirar with credit card exceeding limit should throw.
     */
    @Test
    void retirar_withCreditCardExceedingLimit_shouldThrow() {
        tarjetaDebito.setTipo(TipoTarjeta.CREDITO);
        tarjetaDebito.setLimite(new BigDecimal("1000.00"));
        cuenta.setSaldo(new BigDecimal("500.00"));

        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        dto.tarjetaId = 1L;
        dto.cantidad = new BigDecimal("1100.00");
        dto.cajeroPropio = true;

        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjetaDebito));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> operacionService.retirar(dto));
        assertEquals("Límite de crédito excedido", ex.getMessage());

        verify(cuentaRepo, never()).save(any());
    }

    /**
     * Retirar with external atm should charge commission.
     */
    @Test
    void retirar_withExternalATM_shouldChargeCommission() {
        OperacionRetiroDTO dto = new OperacionRetiroDTO();
        dto.tarjetaId = 1L;
        dto.cantidad = new BigDecimal("200.00");
        dto.cajeroPropio = false;

        when(tarjetaRepo.findById(1L)).thenReturn(Optional.of(tarjetaDebito));
        when(cuentaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimientoRepo.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));

        operacionService.retirar(dto);

        // Total retirado = 200 + 2 de comisión
        BigDecimal expectedSaldo = new BigDecimal("1000.00").subtract(new BigDecimal("202.00"));
        assertEquals(expectedSaldo, cuenta.getSaldo());

        verify(movimientoRepo, times(2)).save(any(Movimiento.class)); // retiro + comisión
    }
}
