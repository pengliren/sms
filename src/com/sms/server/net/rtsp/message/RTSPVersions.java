package com.sms.server.net.rtsp.message;

import com.sms.server.net.http.message.HTTPVersion;

/**
 * RTSP Versions
 * @author pengliren
 *
 */
public final class RTSPVersions {

    /**
     * RTSP/1.0
     */
    public static final HTTPVersion RTSP_1_0 = new HTTPVersion("RTSP", 1, 0, true);

    /**
     * Returns an existing or new {@link HTTPVersion} instance which matches to
     * the specified RTSP version string.  If the specified {@code text} is
     * equal to {@code "RTSP/1.0"}, {@link #RTSP_1_0} will be returned.
     * Otherwise, a new {@link HTTPVersion} instance will be returned.
     */
    public static HTTPVersion valueOf(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }

        text = text.trim().toUpperCase();
        if (text.equals("RTSP/1.0")) {
            return RTSP_1_0;
        }

        return new HTTPVersion(text, true);
    }

    private RTSPVersions() {
    }
}
