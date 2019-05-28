package com.sms.server.compression;

import com.sms.server.util.jzlib.JZlib;
import com.sms.server.util.jzlib.ZStream;

/**
 * Utility methods used by {@link ZlibEncoder} and {@link ZlibDecoder}.
 */
final class ZlibUtil {

    static void fail(ZStream z, String message, int resultCode) {
        throw exception(z, message, resultCode);
    }

    static CompressionException exception(ZStream z, String message, int resultCode) {
        return new CompressionException(message + " (" + resultCode + ")" +
                (z.msg != null? ": " + z.msg : ""));
    }

    static Enum<?> convertWrapperType(ZlibWrapper wrapper) {
        Enum<?> convertedWrapperType;
        switch (wrapper) {
        case NONE:
            convertedWrapperType = JZlib.W_NONE;
            break;
        case ZLIB:
            convertedWrapperType = JZlib.W_ZLIB;
            break;
        case GZIP:
            convertedWrapperType = JZlib.W_GZIP;
            break;
        case ZLIB_OR_NONE:
            convertedWrapperType = JZlib.W_ZLIB_OR_NONE;
            break;
        default:
            throw new Error();
        }
        return convertedWrapperType;
    }

    private ZlibUtil() {
    }
}
