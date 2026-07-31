package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Atm {

    @Id
    private UUID id;

    private UUID bankId;

    private String location;

    protected Atm() {
    }

    public Atm(UUID id, UUID bankId, String location) {
        this.id = id;
        this.bankId = bankId;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBankId() {
        return bankId;
    }

    public String getLocation() {
        return location;
    }
}
