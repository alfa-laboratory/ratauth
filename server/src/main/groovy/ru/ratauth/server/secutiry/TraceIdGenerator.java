package ru.ratauth.server.secutiry;

import java.util.Random;

/**
 * Sleuth-like traceId generator
 */
public class TraceIdGenerator {
    private TraceIdGenerator() { }

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static Random random = new Random();

    public static String generateValue() {
        char[] result = new char[16];
        writeHexLong(result, random.nextLong());
        return new String(result);
    }

    private static void writeHexLong(char[] data, long v) {
        writeHexByte(data, 0,  (byte) ((v >>> 56L) & 0xff));
        writeHexByte(data, 2,  (byte) ((v >>> 48L) & 0xff));
        writeHexByte(data, 4,  (byte) ((v >>> 40L) & 0xff));
        writeHexByte(data, 6,  (byte) ((v >>> 32L) & 0xff));
        writeHexByte(data, 8,  (byte) ((v >>> 24L) & 0xff));
        writeHexByte(data, 10, (byte) ((v >>> 16L) & 0xff));
        writeHexByte(data, 12, (byte) ((v >>> 8L) & 0xff));
        writeHexByte(data, 14, (byte)  (v & 0xff));
    }

    private static void writeHexByte(char[] data, int pos, byte b) {
        data[pos] = HEX_DIGITS[(b >> 4) & 0xf];
        data[pos + 1] = HEX_DIGITS[b & 0xf];
    }
}
