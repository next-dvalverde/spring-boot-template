package es.nextdigital.demo.repository;

import es.nextdigital.demo.model.Tarjeta;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TarjetaRepository {

    private final Map<Long, Tarjeta> tarjetas = new HashMap<>();

    public Tarjeta findById(Long id) {
        return tarjetas.get(id);
    }

    public void save(Tarjeta t) {
        tarjetas.put(t.getId(), t);
    }
}
