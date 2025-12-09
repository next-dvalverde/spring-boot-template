package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class TransferOperation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String bankAccountTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getBankAccountTo() {
        return bankAccountTo;
    }

    public void setBankAccountTo(String bankAccountTo) {
        this.bankAccountTo = bankAccountTo;
    }

    @Override
    public String toString() {
        return "TransferOperation [id=" + id + ", account=" + account + ", bankAccountTo=" + bankAccountTo + "]";
    }
}
