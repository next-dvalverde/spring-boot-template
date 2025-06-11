package es.nextdigital.demo.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity()
@Table(name = "account")
@NoArgsConstructor
@Accessors(chain = true, fluent = true)
@Data
@EqualsAndHashCode
public class AccountEntity {
	@Id
	@Column(name = "iban", nullable = false)
	private String iban;

	@Column(name = "bank", nullable = false)
	private String bank;

	@Column(name = "total_credit", nullable = false)
	private BigDecimal totalCredit;

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CardEntity> cards = new ArrayList<>();

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MovementEntity> movements = new ArrayList<>();
}
