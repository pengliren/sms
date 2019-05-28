package com.sms.server.net.http.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Version
 * @author pengliren
 *
 */
public class HTTPVersion implements Comparable<HTTPVersion> {

	private static final Pattern VERSION_PATTERN =
        Pattern.compile("(\\S+)/(\\d+)\\.(\\d+)");

    /**
     * HTTP/1.0
     */
    public static final HTTPVersion HTTP_1_0 = new HTTPVersion("HTTP", 1, 0, false);

    /**
     * HTTP/1.1
     */
    public static final HTTPVersion HTTP_1_1 = new HTTPVersion("HTTP", 1, 1, true);

    /**
     * Returns an existing or new {@link HTTPVersion} instance which matches to
     * the specified protocol version string.  If the specified {@code text} is
     * equal to {@code "HTTP/1.0"}, {@link #HTTP_1_0} will be returned.  If the
     * specified {@code text} is equal to {@code "HTTP/1.1"}, {@link #HTTP_1_1}
     * will be returned.  Otherwise, a new {@link HTTPVersion} instance will be
     * returned.
     */
    public static HTTPVersion valueOf(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        text = text.trim().toUpperCase();
        if (text.equals("HTTP/1.1")) {
            return HTTP_1_1;
        }
        if (text.equals("HTTP/1.0")) {
            return HTTP_1_0;
        }
        return new HTTPVersion(text, true);
    }

    private final String protocolName;
    private final int majorVersion;
    private final int minorVersion;
    private final String text;
    private final boolean keepAliveDefault;

    /**
     * Creates a new HTTP version with the specified version string.  You will
     * not need to create a new instance unless you are implementing a protocol
     * derived from HTTP, such as
     * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
     * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>.
     *
     * @param keepAliveDefault
     *        {@code true} if and only if the connection is kept alive unless
     *        the {@code "Connection"} header is set to {@code "close"} explicitly.
     */
    public HTTPVersion(String text, boolean keepAliveDefault) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        text = text.trim().toUpperCase();
        if (text.length() == 0) {
            throw new IllegalArgumentException("empty text");
        }

        Matcher m = VERSION_PATTERN.matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException("invalid version format: " + text);
        }

        protocolName = m.group(1);
        majorVersion = Integer.parseInt(m.group(2));
        minorVersion = Integer.parseInt(m.group(3));
        this.text = protocolName + '/' + majorVersion + '.' + minorVersion;
        this.keepAliveDefault = keepAliveDefault;
    }

    /**
     * Creates a new HTTP version with the specified protocol name and version
     * numbers.  You will not need to create a new instance unless you are
     * implementing a protocol derived from HTTP, such as
     * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
     * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>
     *
     * @param keepAliveDefault
     *        {@code true} if and only if the connection is kept alive unless
     *        the {@code "Connection"} header is set to {@code "close"} explicitly.
     */
    public HTTPVersion(
            String protocolName, int majorVersion, int minorVersion,
            boolean keepAliveDefault) {
        if (protocolName == null) {
            throw new NullPointerException("protocolName");
        }

        protocolName = protocolName.trim().toUpperCase();
        if (protocolName.length() == 0) {
            throw new IllegalArgumentException("empty protocolName");
        }

        for (int i = 0; i < protocolName.length(); i ++) {
            if (Character.isISOControl(protocolName.charAt(i)) ||
                Character.isWhitespace(protocolName.charAt(i))) {
                throw new IllegalArgumentException("invalid character in protocolName");
            }
        }

        if (majorVersion < 0) {
            throw new IllegalArgumentException("negative majorVersion");
        }
        if (minorVersion < 0) {
            throw new IllegalArgumentException("negative minorVersion");
        }

        this.protocolName = protocolName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        text = protocolName + '/' + majorVersion + '.' + minorVersion;
        this.keepAliveDefault = keepAliveDefault;
    }

    /**
     * Returns the name of the protocol such as {@code "HTTP"} in {@code "HTTP/1.0"}.
     */
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * Returns the name of the protocol such as {@code 1} in {@code "HTTP/1.0"}.
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Returns the name of the protocol such as {@code 0} in {@code "HTTP/1.0"}.
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Returns the full protocol version text such as {@code "HTTP/1.0"}.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns {@code true} if and only if the connection is kept alive unless
     * the {@code "Connection"} header is set to {@code "close"} explicitly.
     */
    public boolean isKeepAliveDefault() {
        return keepAliveDefault;
    }

    /**
     * Returns the full protocol version text such as {@code "HTTP/1.0"}.
     */
    @Override
    public String toString() {
        return getText();
    }

    @Override
    public int hashCode() {
        return (getProtocolName().hashCode() * 31 + getMajorVersion()) * 31 +
               getMinorVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HTTPVersion)) {
            return false;
        }

        HTTPVersion that = (HTTPVersion) o;
        return getMinorVersion() == that.getMinorVersion() &&
               getMajorVersion() == that.getMajorVersion() &&
               getProtocolName().equals(that.getProtocolName());
    }

    @Override
    public int compareTo(HTTPVersion o) {
        int v = getProtocolName().compareTo(o.getProtocolName());
        if (v != 0) {
            return v;
        }

        v = getMajorVersion() - o.getMajorVersion();
        if (v != 0) {
            return v;
        }

        return getMinorVersion() - o.getMinorVersion();
    }
}
