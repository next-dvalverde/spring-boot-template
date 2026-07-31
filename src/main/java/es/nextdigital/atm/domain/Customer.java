package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Customer {

    @Id
    private UUID id;

    private String name;

    private String documentId;

    protected Customer() {
    }

    public Customer(UUID id, String name, String documentId) {
        this.id = id;
        this.name = name;
        this.documentId = documentId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocumentId() {
        return documentId;
    }
}
