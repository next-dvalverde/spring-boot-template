package es.nextdigital.demo.infrastructure.entity;

import es.nextdigital.demo.domain.vo.MovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Entity()
@Table(name = "movement")
@Accessors(chain = true, fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class MovementEntity {

	@Id
	private UUID id;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private MovementType type;

	@Column(name = "quantity", nullable = false)
	private BigDecimal quantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_iban")
	private AccountEntity account;
}
