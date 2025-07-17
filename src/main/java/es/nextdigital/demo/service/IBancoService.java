package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Banco;

import java.math.BigDecimal;

public interface IBancoService {

    boolean esMismoBanco(String ibanOrigen, String ibanDestino);

    boolean esCajeroPropio(Banco bancoEmisor, Banco bancoCajero);

    BigDecimal calcularComisionTransferencia(boolean mismoBanco, BigDecimal cantidad);

    BigDecimal calcularComisionCajeroAjeno(Banco bancoCajero, BigDecimal cantidad);

}
