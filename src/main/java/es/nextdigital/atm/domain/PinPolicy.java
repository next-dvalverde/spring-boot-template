package es.nextdigital.atm.domain;

/**
 * B-7: sin esta regla, 0000 y 1234 pasan — y son los primeros que prueba
 * cualquiera que encuentre una tarjeta.
 */
public final class PinPolicy {

    private PinPolicy() {
    }

    public static void validateNewPin(String newPin, String currentPin) {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new BusinessException(ErrorCode.INVALID_PIN_FORMAT, "PIN must be exactly 4 digits");
        }
        if (newPin.equals(currentPin)) {
            throw new BusinessException(ErrorCode.INVALID_PIN_FORMAT, "New PIN must differ from the current one");
        }
        if (isTrivial(newPin)) {
            throw new BusinessException(ErrorCode.INVALID_PIN_FORMAT, "PIN must not be a trivial sequence");
        }
    }

    private static boolean isTrivial(String pin) {
        boolean allSame = true;
        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < pin.length(); i++) {
            int step = pin.charAt(i) - pin.charAt(i - 1);
            allSame &= step == 0;
            ascending &= step == 1;
            descending &= step == -1;
        }
        return allSame || ascending || descending;
    }
}
