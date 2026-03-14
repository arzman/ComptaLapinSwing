package com.comptalapin.ui;

import java.math.BigDecimal;

public final class FormatUtils {
    private FormatUtils() {}

    public static String formatAmount(BigDecimal amount) {
        return String.format("%,.2f €", amount);
    }
}
