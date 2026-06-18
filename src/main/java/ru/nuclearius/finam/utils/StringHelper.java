package ru.nuclearius.finam.utils;

import org.apache.commons.lang3.StringUtils;

public class StringHelper {
    public static String transliterate(String text) {
        if (StringUtils.isEmpty(text))
            return null;
        return StringUtils.replaceChars(
                text.toLowerCase(),
                "qwertyuiop[]asdfghjkl;'zxcvbnm,.",
                "йцукенгшщзхъфывапролджэячсмитьбю");
    }
}
