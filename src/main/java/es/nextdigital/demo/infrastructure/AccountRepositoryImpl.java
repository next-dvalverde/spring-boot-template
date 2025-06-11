package es.nextdigital.demo.infrastructure;

import es.nextdigital.demo.domain.Account;
import es.nextdigital.demo.domain.AccountRepository;
import es.nextdigital.demo.domain.vo.Card;
import es.nextdigital.demo.domain.vo.Movement;
import es.nextdigital.demo.infrastructure.entity.AccountEntity;
import es.nextdigital.demo.infrastructure.entity.CardEntity;
import es.nextdigital.demo.infrastructure.entity.MovementEntity;
import es.nextdigital.demo.infrastructure.repository.AccountRepositoryJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {
	private final AccountRepositoryJPA accountRepositoryJPA;

	@Override
	public void save(Account account) {
		this.accountRepositoryJPA.save(this.toEntity(account));
	}

	@Override
	public Optional<Account> findById(Account.Iban iban) {
		return this.accountRepositoryJPA.findById(iban.value()).map(this::toDomain);
	}

	@Override
	public Optional<Account> findByCardId(Card.Id cardId) {
		return this.accountRepositoryJPA.findByCardId(cardId.value()).map(this::toDomain);
	}

	/* Lo ideal sería que esto estuviera en un mapper, lo hago aqui para agilizar */
	private AccountEntity toEntity(Account account) {
		final var iban = account.getIban();
		return new AccountEntity().iban(iban.value()).bank(account.getBank().value())
				.cards(account.getCards().stream().map(card -> this.toEntity(iban, card)).toList())
				.movements(account.getMovements().stream().map(mov -> this.toEntity(iban, mov)).toList())
				.totalCredit(account.getTotalCredit().value());
	}

	private MovementEntity toEntity(Account.Iban accountIban, Movement movement) {
		return new MovementEntity().id(movement.id().value()).type(movement.movementType())
				.quantity(movement.quantity().value()).account(new AccountEntity().iban(accountIban.value()));
	}

	private CardEntity toEntity(Account.Iban accountIban, Card card) {
		return new CardEntity().id(card.id().value()).limit(card.limit().value()).type(card.cardType())
				.account(new AccountEntity().iban(accountIban.value())).isActive(card.isActive().value());
	}

	/* Lo ideal sería que esto estuviera en un mapper, lo hago aqui para agilizar */
	private Account toDomain(AccountEntity accountEntity) {
		return new Account(new Account.Iban(accountEntity.iban()), new Account.Bank(accountEntity.bank()),
				new Account.TotalCredit(accountEntity.totalCredit()),
				accountEntity.movements().stream().map(this::toDomain).toList(),
				accountEntity.cards().stream().map(this::toDomain).toList());
	}

	private Movement toDomain(MovementEntity movementEntity) {
		return new Movement(new Movement.Id(movementEntity.id()), movementEntity.type(),
				new Movement.Quantity(movementEntity.quantity()));
	}

	private Card toDomain(CardEntity cardEntity) {
		return new Card(new Card.Id(cardEntity.id()), cardEntity.type(), new Card.Limit(cardEntity.limit()),
				new Card.IsActive(cardEntity.isActive()));
	}
}
