package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Tarjeta;
import java.math.BigDecimal;

public interface ITarjetaService {

    Tarjeta obtenerTarjeta(String numeroTarjeta);

    void validarTarjetaActiva(Tarjeta tarjeta);

    void validarPin(Tarjeta tarjeta, String pinPlano);

    void activarTarjeta(String numeroTarjeta, String pinPlano);

    void cambiarPin(String numeroTarjeta, String pinAntiguo, String pinNuevo);

    void validarLimiteRetirada(Tarjeta tarjeta, BigDecimal cantidad);

    void cambiarLimiteRetirada(String numeroTarjeta, BigDecimal nuevoLimite);

    void consumirCredito(Tarjeta tarjeta, BigDecimal cantidadCredito);

}

