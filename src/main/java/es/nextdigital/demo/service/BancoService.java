package es.nextdigital.demo.service;

import es.nextdigital.demo.entity.Banco;
import es.nextdigital.demo.repository.BancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BancoService implements IBancoService {

    private final BancoRepository bancoRepository;

    public BigDecimal calcularComisionCajeroAjeno(Banco bancoCajero, BigDecimal cantidad) {
        BigDecimal comisionBase = cantidad.multiply(BigDecimal.valueOf(0.01));
        return comisionBase.compareTo(BigDecimal.valueOf(2)) < 0 ?
                BigDecimal.valueOf(2) : comisionBase;
    }

    public BigDecimal calcularComisionTransferencia(boolean mismoBanco, BigDecimal cantidad) {
        if (mismoBanco) return BigDecimal.ZERO;

        BigDecimal comision = cantidad.multiply(BigDecimal.valueOf(0.005));
        return comision.compareTo(BigDecimal.valueOf(1)) < 0 ?
                BigDecimal.valueOf(1) : comision;
    }

    public boolean esMismoBanco(String ibanOrigen, String ibanDestino) {
        String codigoBancoOrigen = ibanOrigen.substring(4, 8);
        String codigoBancoDestino = ibanDestino.substring(4, 8);
        return codigoBancoOrigen.equals(codigoBancoDestino);
    }

    public boolean esCajeroPropio(Banco bancoEmisor, Banco bancoCajero) {
        if (bancoEmisor == null || bancoCajero == null) {
            return false;
        }
        return bancoEmisor.getId().equals(bancoCajero.getId());
    }
}

