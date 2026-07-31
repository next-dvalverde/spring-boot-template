package es.nextdigital.atm.domain;

/**
 * No es MovementType: una politica de TRANSFER aplica a la operacion entera,
 * cuyo apunte sera TRANSFER_OUT. Mezclar ambos enums invitaria a buscar la
 * tarifa por tipo de apunte, que no es la clave.
 */
public enum FeeOperationType {
    WITHDRAWAL, TRANSFER
}
