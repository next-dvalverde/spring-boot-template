package es.nextdigital.demo.infrastructure;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.CardType;
import es.nextdigital.demo.domain.vo.Movement;
import es.nextdigital.demo.domain.vo.MovementType;
import es.nextdigital.demo.infrastructure.repository.AccountRepositoryJPA;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AccountRepositoryImpl.class)
class AccountRepositoryImplIT {

	@Autowired
	private AccountRepositoryImpl accountRepository;

	@Autowired
	private AccountRepositoryJPA jpaRepository;

	@Test
	void should_save_including_movement_and_card() {
		final var movement = new Movement(new Movement.Id(UUID.randomUUID()), MovementType.DEPOSIT,
				new Movement.Quantity(BigDecimal.TEN));
		final var card = new Card(new Card.Id(UUID.randomUUID()), CardType.CREDIT, new Card.Limit(BigDecimal.TEN),
				new Card.IsActive(true));
		final var ibanValue = "IBAN123456";
		final var account = new Account(new Account.Iban(ibanValue), new Account.Bank("MyBank"),
				new Account.TotalCredit(BigDecimal.TEN), List.of(movement), List.of(card));

		this.accountRepository.save(account);

		final var result = this.accountRepository.findById(account.getIban());
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(account);
	}

}