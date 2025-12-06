package es.nextdigital.demo.repository;

import es.nextdigital.demo.model.Cuenta;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CuentaRepository {

    private final Map<Long, Cuenta> cuentas = new HashMap<>();

    public Cuenta findById(Long id) {
        return cuentas.get(id);
    }

    public void save(Cuenta cuenta) {
        cuentas.put(cuenta.getId(), cuenta);
    }

    public Collection<Cuenta> findAll() {
        return cuentas.values();
    }

}
