package com.comptalapin.ui;

import java.math.BigDecimal;
import java.util.Locale;

public final class FormatUtils {
    private FormatUtils() {}

    public static String formatAmount(BigDecimal amount) {
        return String.format(Locale.FRENCH, "%,.2f €", amount);
    }
}
