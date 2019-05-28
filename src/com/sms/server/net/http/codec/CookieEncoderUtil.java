package com.sms.server.net.http.codec;

import static com.sms.server.net.http.codec.HTTPCodecUtil.DOUBLE_QUOTE;
import static com.sms.server.net.http.codec.HTTPCodecUtil.EQUALS;
import static com.sms.server.net.http.codec.HTTPCodecUtil.SEMICOLON;
import static com.sms.server.net.http.codec.HTTPCodecUtil.SP;

/**
 * Cookie Encoder Util
 * @author pengliren
 *
 */
public final class CookieEncoderUtil {

	public static String stripTrailingSeparator(StringBuilder buf) {
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }

	public static void add(StringBuilder sb, String name, String val) {
        if (val == null) {
            addQuoted(sb, name, "");
            return;
        }

        for (int i = 0; i < val.length(); i ++) {
            char c = val.charAt(i);
            switch (c) {
            case '\t': case ' ': case '"': case '(':  case ')': case ',':
            case '/':  case ':': case ';': case '<':  case '=': case '>':
            case '?':  case '@': case '[': case '\\': case ']':
            case '{':  case '}':
                addQuoted(sb, name, val);
                return;
            }
        }

        addUnquoted(sb, name, val);
    }

	public static void addUnquoted(StringBuilder sb, String name, String val) {
        sb.append(name);
        sb.append((char) EQUALS);
        sb.append(val);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

	public static void addQuoted(StringBuilder sb, String name, String val) {
        if (val == null) {
            val = "";
        }

        sb.append(name);
        sb.append((char) EQUALS);
        sb.append((char) DOUBLE_QUOTE);
        sb.append(val.replace("\\", "\\\\").replace("\"", "\\\""));
        sb.append((char) DOUBLE_QUOTE);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

	public static void add(StringBuilder sb, String name, long val) {
        sb.append(name);
        sb.append((char) EQUALS);
        sb.append(val);
        sb.append((char) SEMICOLON);
        sb.append((char) SP);
    }

    private CookieEncoderUtil() {
        // Unused
    }
}
