package com.sistemaganadero.util;

import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[+]?[0-9 \\-]{7,15}$");
    private static final Pattern SOLO_NUMEROS = Pattern.compile("^[0-9]+$");

    private Validators() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean emailValido(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    public static boolean telefonoValido(String s) {
        return s != null && TELEFONO.matcher(s.trim()).matches();
    }

    public static boolean numeroValido(String s) {
        return s != null && SOLO_NUMEROS.matcher(s.trim()).matches();
    }

    public static boolean precioValido(String s) {
        if (isBlank(s)) return false;
        try {
            double v = Double.parseDouble(s.trim());
            return v > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static double parsePrecio(String s) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; }
    }

    public static boolean pesoValido(String s) {
        return precioValido(s);
    }
}
