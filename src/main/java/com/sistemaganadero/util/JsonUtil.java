package com.sistemaganadero.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Utilidad mínima para serializar Map / Object a JSON sin dependencias externas.
 * Solo soporta: String, Number, Boolean, null, LocalDate, LocalDateTime, Map, Collection y arrays.
 * Para objetos arbitrarios usa reflection sobre métodos getXxx() / isXxx().
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static String toJson(Object o) {
        StringBuilder sb = new StringBuilder();
        write(sb, o);
        return sb.toString();
    }

    /** Convierte un objeto JavaBean en un Map<String,Object> usando reflection. */
    public static Map<String, Object> toMap(Object bean) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (bean == null) return out;
        for (Method m : bean.getClass().getMethods()) {
            if (Modifier.isStatic(m.getModifiers())) continue;
            if (m.getParameterCount() != 0) continue;
            String name = m.getName();
            String prop;
            if (name.startsWith("get") && name.length() > 3 && !name.equals("getClass")) {
                prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
            } else if (name.startsWith("is") && name.length() > 2) {
                prop = Character.toLowerCase(name.charAt(2)) + name.substring(3);
            } else continue;
            try {
                Object value = m.invoke(bean);
                if (value != null && value.getClass().getName().startsWith("com.sistemaganadero.model.")
                        && !(value instanceof Enum<?>)) {
                    // Nested model object — recurse one level
                    out.put(prop, toMap(value));
                } else if (value instanceof Enum<?> e) {
                    out.put(prop, e.name());
                } else {
                    out.put(prop, value);
                }
            } catch (Exception ignored) { /* skip property */ }
        }
        return out;
    }

    private static void write(StringBuilder sb, Object v) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof String s) { sb.append('"').append(escape(s)).append('"'); return; }
        if (v instanceof Number || v instanceof Boolean) { sb.append(v); return; }
        if (v instanceof LocalDate ld) { sb.append('"').append(ld).append('"'); return; }
        if (v instanceof LocalDateTime ldt) { sb.append('"').append(ldt).append('"'); return; }
        if (v instanceof Enum<?> e) { sb.append('"').append(e.name()).append('"'); return; }
        if (v instanceof Map<?, ?> m) { writeMap(sb, m); return; }
        if (v instanceof Collection<?> c) { writeArr(sb, c); return; }
        if (v.getClass().isArray()) {
            List<Object> tmp = new ArrayList<>();
            int n = java.lang.reflect.Array.getLength(v);
            for (int i = 0; i < n; i++) tmp.add(java.lang.reflect.Array.get(v, i));
            writeArr(sb, tmp);
            return;
        }
        // Fallback — toString quoted
        sb.append('"').append(escape(String.valueOf(v))).append('"');
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> m) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':');
            write(sb, e.getValue());
        }
        sb.append('}');
    }

    private static void writeArr(StringBuilder sb, Collection<?> c) {
        sb.append('[');
        boolean first = true;
        for (Object v : c) {
            if (!first) sb.append(',');
            first = false;
            write(sb, v);
        }
        sb.append(']');
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"'  -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }
}
