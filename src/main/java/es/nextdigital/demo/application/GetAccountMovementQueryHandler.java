package es.nextdigital.demo.application;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.Movement;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GetAccountMovementQueryHandler {
	private final AccountRepository accountRepository;

	@QueryHandler
	public List<Movement> handle(GetAccountMovementQuery query) {
		return this.accountRepository.findById(new Account.Iban(query.accountIban())).map(Account::getMovements)
				.orElseThrow(() -> new IllegalArgumentException("Account not found"));
	}
}
