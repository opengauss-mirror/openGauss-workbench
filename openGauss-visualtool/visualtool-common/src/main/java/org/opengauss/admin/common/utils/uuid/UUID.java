/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * UUID.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/uuid/UUID.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.uuid;

import org.opengauss.admin.common.exception.UtilException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provide a Universal Unique Identifier
 *
 * @author xielibo
 */
public final class UUID implements java.io.Serializable, Comparable<UUID> {
    private static final long serialVersionUID = -1185015143654744140L;

    /**
     * SecureRandom
     */
    private static class Holder {
        static final SecureRandom NUMBER_GENERATOR = getSecureRandom();
    }

    /**
     * Up to 64 significant bits
     */
    private final long mostSigBits;

    /**
     * Least significant 64 bits
     */
    private final long leastSigBits;

    /**
     * structure
     */
    private UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    /**
     * Constructs a new UUID using the specified data。
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs.
     * The UUID is generated using a cryptographically thread-local pseudo-random number generator.
     */
    public static UUID fastUuid() {
        return randomUuid(false);
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs.
     * The UUID is generated using a cryptographically strong pseudorandom number generator.
     *
     */
    public static UUID randomUuid() {
        return randomUuid(true);
    }

    /**
     * Get a static factory for type 4 (pseudo-randomly generated) UUIDs.
     * The UUID is generated using a cryptographically strong pseudorandom number generator.
     *
     */
    public static UUID randomUuid(boolean isSecure) {
        final Random ng = isSecure ? Holder.NUMBER_GENERATOR : getRandom();

        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */
        return new UUID(randomBytes);
    }


    /**
     * Returns the least significant 64 bits of this UUID's 128-bit value.
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128-bit value.
     *
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * version
     *
     */
    public int version() {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (int) ((mostSigBits >> 12) & 0x0f);
    }

    /**
     * variant
     *
     */
    public int variant() {
        // This field is composed of a varying number of bits.
        // 0 - - Reserved for NCS backward compatibility
        // 1 0 - The IETF aka Leach-Salz variant (used by this class)
        // 1 1 0 Reserved, Microsoft backward compatibility
        // 1 1 1 Reserved for future definition.
        return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
    }

    /**
     * timestamp
     */
    public long timestamp() throws UnsupportedOperationException {
        checkTimeBase();
        return (mostSigBits & 0x0FFFL) << 48//
                | ((mostSigBits >> 16) & 0x0FFFFL) << 32//
                | mostSigBits >>> 32;
    }

    /**
     * clockSequence
     */
    public int clockSequence() throws UnsupportedOperationException {
        checkTimeBase();
        return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
    }

    /**
     * node
     */
    public long node() throws UnsupportedOperationException {
        checkTimeBase();
        return leastSigBits & 0x0000FFFFFFFFFFFFL;
    }

    /**
     * toString
     */
    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * toString
     */
    public String toString(boolean isSimple) {
        final StringBuilder builder = new StringBuilder(isSimple ? 32 : 36);
        // time_low
        builder.append(digits(mostSigBits >> 32, 8));
        if (false == isSimple) {
            builder.append('-');
        }
        // time_mid
        builder.append(digits(mostSigBits >> 16, 4));
        if (false == isSimple) {
            builder.append('-');
        }
        // time_high_and_version
        builder.append(digits(mostSigBits, 4));
        if (false == isSimple) {
            builder.append('-');
        }
        // variant_and_sequence
        builder.append(digits(leastSigBits >> 48, 4));
        if (false == isSimple) {
            builder.append('-');
        }
        // node
        builder.append(digits(leastSigBits, 12));

        return builder.toString();
    }

    /**
     * get hashCode
     *
     * @return UUID
     */
    @Override
    public int hashCode() {
        long hilo = mostSigBits ^ leastSigBits;
        return ((int) (hilo >> 32)) ^ (int) hilo;
    }

    /**
     * equals
     */
    @Override
    public boolean equals(Object obj) {
        if ((null == obj) || (obj.getClass() != UUID.class)) {
            return false;
        }
        UUID id = (UUID) obj;
        return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
    }

    // Comparison Operations

    /**
     * compareTo
     */
    @Override
    public int compareTo(UUID val) {
        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        return (this.mostSigBits < val.mostSigBits ? -1 : //
                (this.mostSigBits > val.mostSigBits ? 1 : //
                        (this.leastSigBits < val.leastSigBits ? -1 : //
                                (this.leastSigBits > val.leastSigBits ? 1 : //
                                        0))));
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Private method start

    /**
     * digits
     *
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * checkTimeBase
     */
    private void checkTimeBase() {
        if (version() != 1) {
            throw new UnsupportedOperationException("Not a time-based UUID");
        }
    }

    /**
     * getSecureRandom
     */
    public static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new UtilException(e);
        }
    }

    /**
     * getRandom
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }
}
