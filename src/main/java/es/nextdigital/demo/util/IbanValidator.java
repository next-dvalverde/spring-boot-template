package es.nextdigital.demo.util;

public class IbanValidator {

    public static boolean validarIBAN(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        return iban.matches("[A-Z]{2}[0-9A-Z]{13,32}");
    }
}