package es.nextdigital.atm.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Bank {

    @Id
    private UUID id;

    // CHAR(4) en el schema: sin esto, ddl-auto=validate espera varchar y
    // rechaza el bpchar de Postgres.
    @JdbcTypeCode(SqlTypes.CHAR)
    private String code;

    private String name;

    protected Bank() {
    }

    public Bank(UUID id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
