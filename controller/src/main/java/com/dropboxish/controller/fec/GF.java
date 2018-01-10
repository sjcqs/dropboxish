package com.dropboxish.controller.fec;

/**
 * Created by satyan on 1/10/18.
 *
 */
class GF {
    static final int FIELD_SIZE = 256;
    static final int GENERATOR = 29;
    private static GF instance = null;

    private final short[] LOG_TABLE = generateLogTable(GENERATOR);
    private final byte[] INV_LOG_TABLE = generateInvLogTable(LOG_TABLE);

    private GF() {
    }

    public static GF getInstance() {
        if (instance == null) {
            instance = new GF();
        }

        return instance;
    }

    private short[] generateLogTable(int generator) {
        short[] table = new short[FIELD_SIZE];
        // init
        for (int i = 0; i < table.length; i++) {
            table[i] = -1;
        }

        int x = 1;
        for (int i = 0; i < FIELD_SIZE - 1; i++) {
            if (table[x] != -1) {
                throw new RuntimeException("Error generating the logarithm table.");
            }
            table[x] = (short) i;

            x = (x << 1);
            if (x >= FIELD_SIZE) {
                x = (x - FIELD_SIZE) ^ GENERATOR;
            }
        }
        return table;
    }

    private byte[] generateInvLogTable(short[] logTable) {
        byte[] table = new byte[FIELD_SIZE * 2 - 2];
        for (int i = 1; i < FIELD_SIZE; i++) {
            int log = logTable[i];
            table[log] = (byte) i;
            table[log - 1 + FIELD_SIZE] = (byte) i;
        }
        return table;
    }

    /**
     * Sum of two elements in the Galois field (a XOR)
     *
     * @param a first element
     * @param b second element
     * @return sum of the two element
     */
    public byte sum(byte a, byte b) {
        return (byte) ((a ^ b));
    }

    /**
     * Difference of two element in the Galois field (a XOR)
     *
     * @param a first element
     * @param b second element
     * @return difference of the two element
     */
    public byte diff(byte a, byte b) {
        return (byte) ((a ^ b));
    }

    /**
     * Multiplication of two element in the Galois field
     *
     * @param a first element
     * @param b second element
     * @return multiplication
     */
    public byte mul(byte a, byte b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int x = LOG_TABLE[a & 0xff];
        int y = LOG_TABLE[b & 0xff];
        return INV_LOG_TABLE[x + y];
    }

    /**
     * Division of two element in the Galois field
     *
     * @param a first element
     * @param b second element
     * @return division
     */
    public byte div(byte a, byte b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero.");
        }
        if (a == 0) {
            return 0;
        }
        int x = LOG_TABLE[a & 0xff];
        int y = LOG_TABLE[b & 0xff];
        return INV_LOG_TABLE[(x - y) + FIELD_SIZE - 1];
    }

    /**
     * Power of an element in the Galois field (a^n)
     *
     * @param a first element
     * @param n power
     * @return a^n
     */
    public byte pow(byte a, int n) {
        if (n == 0) {
            return 1;
        }
        if (a == 0) {
            return 0;
        }
        if (n < 0){
            n = FIELD_SIZE - 1 + n;
        }
        int x = LOG_TABLE[a & 0xff];
        int log = x * n;
        while (log >= FIELD_SIZE - 1) {
            log -= FIELD_SIZE - 1;
        }
        return INV_LOG_TABLE[log];
    }


}
