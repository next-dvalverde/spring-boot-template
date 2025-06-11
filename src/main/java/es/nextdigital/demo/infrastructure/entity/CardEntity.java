package es.nextdigital.demo.infrastructure.entity;

import es.nextdigital.demo.domain.vo.CardType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Entity()
@Table(name = "card")
@NoArgsConstructor
@Data
@Accessors(chain = true, fluent = true)
@EqualsAndHashCode
public class CardEntity {

	@Id
	private UUID id;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private CardType type;

	@Column(name = "credit_limit", nullable = false)
	private BigDecimal limit;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_iban")
	private AccountEntity account;

}
