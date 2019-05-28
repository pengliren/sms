package com.sms.server.util;

import java.util.Formatter;

public class StringUtil {

	private StringUtil() {
        // Unused.
    }

    public static final String NEWLINE;

    static {
        String newLine = null;

        try {
            newLine = new Formatter().format("%n").toString();
        } catch (Exception e) {
            newLine = "\n";
        }

        NEWLINE = newLine;
    }

    /**
     * Strip an Object of it's ISO control characters.
     *
     * @param value
     *          The Object that should be stripped. This objects toString method will
     *          called and the result passed to {@link #stripControlCharacters(String)}.
     * @return {@code String}
     *          A new String instance with its hexadecimal control characters replaced
     *          by a space. Or the unmodified String if it does not contain any ISO
     *          control characters.
     */
    public static String stripControlCharacters(Object value) {
        if (value == null) {
            return null;
        }

        return stripControlCharacters(value.toString());
    }

    /**
     * Strip a String of it's ISO control characters.
     *
     * @param value
     *          The String that should be stripped.
     * @return {@code String}
     *          A new String instance with its hexadecimal control characters replaced
     *          by a space. Or the unmodified String if it does not contain any ISO
     *          control characters.
     */
    public static String stripControlCharacters(String value) {
        if (value == null) {
            return null;
        }

        boolean hasControlChars = false;
        for (int i = value.length() - 1; i >= 0; i --) {
            if (Character.isISOControl(value.charAt(i))) {
                hasControlChars = true;
                break;
            }
        }

        if (!hasControlChars) {
            return value;
        }

        StringBuilder buf = new StringBuilder(value.length());
        int i = 0;

        // Skip initial control characters (i.e. left trim)
        for (; i < value.length(); i ++) {
            if (!Character.isISOControl(value.charAt(i))) {
                break;
            }
        }

        // Copy non control characters and substitute control characters with
        // a space.  The last control characters are trimmed.
        boolean suppressingControlChars = false;
        for (; i < value.length(); i ++) {
            if (Character.isISOControl(value.charAt(i))) {
                suppressingControlChars = true;
                continue;
            } else {
                if (suppressingControlChars) {
                    suppressingControlChars = false;
                    buf.append(' ');
                }
                buf.append(value.charAt(i));
            }
        }

        return buf.toString();
    }
}
