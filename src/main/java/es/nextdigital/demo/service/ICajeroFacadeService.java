package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Banco;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface ICajeroFacadeService {
    @Transactional
    void retirarDinero(String numeroTarjeta, String pinHash, BigDecimal cantidad, Banco bancoCajero);

    @Transactional
    void ingresarDinero(String numeroTarjeta, String pinHash, BigDecimal cantidad, Banco bancoCajero);

    @Transactional
    void transferir(String numeroTarjeta, String pinHash, String ibanDestino, BigDecimal cantidad);
}
