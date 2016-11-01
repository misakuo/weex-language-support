package com.taobao.weex.utils;

import java.util.regex.Pattern;

/**
 * Created by moxun on 16/10/27.
 */
public class CodeUtil {
    public static String getVarNameFromMustache(String raw) {

        raw = raw.replaceAll("\\{+", "").replaceAll("\\}+", "").trim();

        if (raw.contains(" in ")) {
            String[] tmp = raw.split("\\s+in\\s+");
            if (tmp.length == 2) {
                raw = tmp[1].trim();
            }
        } else if (raw.contains(".") || raw.contains("[")) {
            int dot = raw.indexOf('.') == -1 ? raw.length() : raw.indexOf('.');
            int bracket = raw.indexOf('[') == -1 ? raw.length() : raw.indexOf('[');

            int index = Math.min(dot, bracket);
            raw = raw.substring(0, index);
        }

        return raw;
    }

    public static String getFunctionNameFromMustache(String raw) {
        return raw.replaceAll("\\{+", "").replaceAll("\\}+", "").replaceAll("\\(.*\\)", "").trim();
    }

    public static String guessStringType(String valueString) {

        if ("null".equals(valueString)) {
            return "null";
        }

        if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
            valueString = valueString.substring(1, valueString.length() - 1);
        }

        if (valueString.startsWith("\'") && valueString.endsWith("\'")) {
            valueString = valueString.substring(1, valueString.length() - 1);
        }

        if (Pattern.compile("(true|false)").matcher(valueString).matches()) {
            return "Boolean";
        }
        if (Pattern.compile("\\d*\\.?\\d+").matcher(valueString).matches()) {
            return "Number";
        }
        return "String";
    }

    public static boolean maybeInLineExpression(String s) {
        if (s == null) {
            return false;
        }

        String[] operators = {"+", "-", "*", "/", "(", ")", "=", "!", "[", "]", "."};

        for (String op : operators) {
            if (s.contains(op)) {
                return true;
            }
        }
        return false;
    }
}
