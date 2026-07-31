package es.nextdigital.atm.domain;

/**
 * Estructura ISO 13616 + digito de control MOD 97-10 (ISO 7064), a mano:
 * meter una libreria por una funcion de 15 lineas no se justifica.
 */
public final class IbanValidator {

    private IbanValidator() {
    }

    public static void validate(String iban) {
        if (iban == null || !iban.matches("[A-Z]{2}\\d{2}[A-Z0-9]{11,30}")) {
            throw new BusinessException(ErrorCode.INVALID_IBAN, "Malformed IBAN");
        }
        if (mod97(iban) != 1) {
            throw new BusinessException(ErrorCode.INVALID_IBAN, "IBAN checksum is not valid");
        }
    }

    /**
     * Codigo de entidad de un IBAN espanol: posiciones 5-8. NO es el prefijo —
     * el prefijo son pais + digitos de control y compararlo compara ruido.
     * Solo se usa cuando la cuenta destino no existe en nuestra base.
     */
    public static String entityCode(String iban) {
        return iban.substring(4, 8);
    }

    // Mover los 4 primeros caracteres al final, letras A=10..Z=35, resto mod 97
    // incremental — sin BigInteger.
    private static int mod97(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (int i = 0; i < rearranged.length(); i++) {
            char c = rearranged.charAt(i);
            int value = Character.isLetter(c) ? c - 'A' + 10 : c - '0';
            remainder = value >= 10
                    ? (remainder * 100 + value) % 97
                    : (remainder * 10 + value) % 97;
        }
        return remainder;
    }
}
