package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CajeroFacadeServiceTest {

    @Mock private ITarjetaService tarjetaService;
    @Mock private ICuentaService cuentaService;
    @Mock private IBancoService bancoService;
    @Mock private IMovimientoService movimientoService;

    @InjectMocks
    private CajeroFacadeService cajeroFacadeService;

    private Banco bancoEmisor;
    private Banco bancoCajero;
    private Tarjeta tarjeta;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        bancoEmisor = new Banco();
        bancoEmisor.setId(1L);

        bancoCajero = new Banco();
        bancoCajero.setId(2L);

        cuenta = new Cuenta();
        cuenta.setIban("ES0001");
        cuenta.setSaldo(new BigDecimal("100"));

        tarjeta = new Tarjeta();
        tarjeta.setNumero("1234");
        tarjeta.setTipo(Tarjeta.TarjetaTipo.DEBITO);
        tarjeta.setBancoEmisor(bancoEmisor);
        tarjeta.setCuentaAsociada(cuenta);
        tarjeta.setLimiteRetirada(new BigDecimal("500"));
    }

    @Test
    void retirarDinero_ConComisionExitoso() {
        BigDecimal cantidad = new BigDecimal("50");

        when(tarjetaService.obtenerTarjeta("1234")).thenReturn(tarjeta);
        doNothing().when(tarjetaService).validarTarjetaActiva(tarjeta);
        doNothing().when(tarjetaService).validarPin(tarjeta, "pinhash");
        doNothing().when(tarjetaService).validarLimiteRetirada(tarjeta, cantidad);
        when(bancoService.esCajeroPropio(bancoEmisor, bancoCajero)).thenReturn(false);
        when(bancoService.calcularComisionCajeroAjeno(bancoCajero, cantidad)).thenReturn(new BigDecimal("2"));
        when(cuentaService.tieneSaldoSuficiente(cuenta, cantidad.add(new BigDecimal("2")))).thenReturn(true);
        doNothing().when(cuentaService).descontarSaldo(cuenta, cantidad.add(new BigDecimal("2")));
        doNothing().when(movimientoService).registrarMovimiento(any(), any(), any(), any());

        assertDoesNotThrow(() -> cajeroFacadeService.retirarDinero("1234", "pinhash", cantidad, bancoCajero));

        verify(cuentaService).descontarSaldo(cuenta, new BigDecimal("52")); // 50 + 2
        verify(movimientoService).registrarMovimiento(cuenta, cantidad.negate(), Movimiento.TipoMovimiento.RETIRADA, "Retirada en cajero");
        verify(movimientoService).registrarMovimiento(cuenta, new BigDecimal("-2"), Movimiento.TipoMovimiento.COMISION, "Comisión cajero ajeno");
    }

    @Test
    void retirarDinero_SaldoInsuficiente_ThrowsException() {
        BigDecimal cantidad = new BigDecimal("1000");

        when(tarjetaService.obtenerTarjeta("1234")).thenReturn(tarjeta);
        doNothing().when(tarjetaService).validarTarjetaActiva(tarjeta);
        doNothing().when(tarjetaService).validarPin(tarjeta, "pinhash");
        doNothing().when(tarjetaService).validarLimiteRetirada(tarjeta, cantidad);
        when(bancoService.esCajeroPropio(bancoEmisor, bancoCajero)).thenReturn(true);
        when(cuentaService.tieneSaldoSuficiente(cuenta, cantidad)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cajeroFacadeService.retirarDinero("1234", "pinhash", cantidad, bancoCajero));
        assertEquals("Saldo insuficiente", ex.getMessage());
    }
}
