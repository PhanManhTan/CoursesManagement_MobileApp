package com.example.myapplication.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    public static String formatVnd(double amount) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_VN);
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
            return decimalFormat.format(amount) + " VND";
        } catch (Exception e) {
            return String.format(LOCALE_VN, "%,.0f VND", amount);
        }
    }

    public static String formatVndRaw(double amount) {
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_VN);
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
            return decimalFormat.format(amount);
        } catch (Exception e) {
            return String.format(LOCALE_VN, "%,.0f", amount);
        }
    }
}
