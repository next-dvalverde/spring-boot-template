package es.nextdigital.demo.domain;

import es.nextdigital.demo.domain.vo.Card;

import java.util.Optional;

public interface AccountRepository {
	void save(Account account);

	Optional<Account> findById(Account.Iban iban);

	Optional<Account> findByCardId(Card.Id cardId);
}
