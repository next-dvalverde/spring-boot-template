package es.nextdigital.demo.service;

import es.nextdigital.demo.dto.MovimientoDTO;
import es.nextdigital.demo.dto.OperacionIngresoDTO;
import es.nextdigital.demo.dto.OperacionRetiroDTO;
import es.nextdigital.demo.dto.TransferenciaDTO;

import java.util.List;

/**
 * The Interface OperacionService.
 */
public interface OperacionService {

    List<MovimientoDTO> consultarMovimientos(Long cuentaId);

    void retirar(OperacionRetiroDTO dto);

    void ingresar(OperacionIngresoDTO dto);

    void transferir(TransferenciaDTO dto);

}
