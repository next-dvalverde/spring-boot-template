package es.nextdigital.demo.domain;

import java.util.Optional;

public interface AccountRepository {
	void save(Account account);
	Optional<Account> findById(Account.Iban iban);
}
