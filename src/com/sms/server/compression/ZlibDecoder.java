package com.sms.server.compression;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.util.jzlib.JZlib;
import com.sms.server.util.jzlib.ZStream;

public class ZlibDecoder {

    private final ZStream z = new ZStream();
    private byte[] dictionary;
    private volatile boolean finished;

    /**
     * Creates a new instance with the default wrapper ({@link ZlibWrapper#ZLIB}).
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public ZlibDecoder() {
        this(ZlibWrapper.ZLIB);
    }

    /**
     * Creates a new instance with the specified wrapper.
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public ZlibDecoder(ZlibWrapper wrapper) {
        if (wrapper == null) {
            throw new NullPointerException("wrapper");
        }

        synchronized (z) {
            int resultCode = z.inflateInit(ZlibUtil.convertWrapperType(wrapper));
            if (resultCode != JZlib.Z_OK) {
                ZlibUtil.fail(z, "initialization failure", resultCode);
            }
        }
    }

    /**
     * Creates a new instance with the specified preset dictionary. The wrapper
     * is always {@link ZlibWrapper#ZLIB} because it is the only format that
     * supports the preset dictionary.
     *
     * @throws CompressionException if failed to initialize zlib
     */
    public ZlibDecoder(byte[] dictionary) {
        if (dictionary == null) {
            throw new NullPointerException("dictionary");
        }
        this.dictionary = dictionary;

        synchronized (z) {
            int resultCode;
            resultCode = z.inflateInit(JZlib.W_ZLIB);
            if (resultCode != JZlib.Z_OK) {
                ZlibUtil.fail(z, "initialization failure", resultCode);
            }
        }
    }

    /**
     * Returns {@code true} if and only if the end of the compressed stream
     * has been reached.
     */
    public boolean isClosed() {
        return finished;
    }

    public IoBuffer decode(IoBuffer msg) throws Exception {

        synchronized (z) {
            try {
                // Configure input.
            	byte[] in = new byte[msg.remaining()];
                msg.get(in);
                z.next_in = in;
                z.next_in_index = 0;
                z.avail_in = in.length;

                // Configure output.
                byte[] out = new byte[(int)(in.length * 1.001) + 1 + 12];
                IoBuffer decompressed = IoBuffer.allocate(out.length).setAutoExpand(true);
                z.next_out = out;
                z.next_out_index = 0;
                z.avail_out = out.length;


                loop: for (;;) {
                    // Decompress 'in' into 'out'
                    int resultCode = z.inflate(JZlib.Z_SYNC_FLUSH);
                    if (z.next_out_index > 0) {
                        decompressed.put(out, 0, z.next_out_index);
                        z.avail_out = out.length;
                    }
                    z.next_out_index = 0;

                    switch (resultCode) {
                    case JZlib.Z_NEED_DICT:
                        if (dictionary == null) {
                            ZlibUtil.fail(z, "decompression failure", resultCode);
                        } else {
                            resultCode = z.inflateSetDictionary(dictionary, dictionary.length);
                            if (resultCode != JZlib.Z_OK) {
                                ZlibUtil.fail(z, "failed to set the dictionary", resultCode);
                            }
                        }
                        break;
                    case JZlib.Z_STREAM_END:
                        finished = true; // Do not decode anymore.
                        z.inflateEnd();
                        break loop;
                    case JZlib.Z_OK:
                        break;
                    case JZlib.Z_BUF_ERROR:
                        if (z.avail_in <= 0) {
                            break loop;
                        }
                        break;
                    default:
                        ZlibUtil.fail(z, "decompression failure", resultCode);
                    }
                }

                decompressed.flip();
                return decompressed;
            } finally {
                // Deference the external references explicitly to tell the VM that
                // the allocated byte arrays are temporary so that the call stack
                // can be utilized.
                // I'm not sure if the modern VMs do this optimization though.
                z.next_in = null;
                z.next_out = null;
            }
        }
    }
}
