/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 *
 * -------------------------------------------------------------------------
 *
 * SecurePathUtilTest.java
 *
 * IDENTIFICATION
 * base-ops/src/test/java/org/opengauss/admin/plugin/SecurePathUtilTest.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin;

import org.junit.Assert;
import org.junit.Test;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.SecurePathUtil;

/**
 * SecurePathUtil unit tests
 *
 * @author wangchao
 * @date 2026/11/20
 * @since 7.0.0-RC2
 */
public class SecurePathUtilTest {
    @Test
    public void testNormalSubPathAllowed() {
        Assert.assertTrue(SecurePathUtil.isUnder("/data/ops/upload", "/data/ops/upload/pkg/openGauss.tar"));
        Assert.assertTrue(SecurePathUtil.isUnder("/data/ops/upload", "/data/ops/upload"));
    }

    @Test
    public void testTraversalRejected() {
        Assert.assertFalse(SecurePathUtil.isUnder("/data/ops/upload", "/data/ops/upload/../etc/passwd"));
        Assert.assertFalse(SecurePathUtil.isUnder("/data/ops/upload", "/etc/passwd"));
    }

    @Test
    public void testPrefixConfusionRejected() {
        // /data/ops/upload-evil must NOT be considered under /data/ops/upload
        Assert.assertFalse(SecurePathUtil.isUnder("/data/ops/upload", "/data/ops/upload-evil/file"));
    }

    @Test
    public void testRequireUnderThrows() {
        try {
            SecurePathUtil.requireUnder("/data/ops/upload", "../../etc/passwd");
            Assert.fail("expected CustomException");
        } catch (CustomException expected) {
            Assert.assertTrue(expected.getMessage().contains("outside base directory"));
        }
    }

    @Test
    public void testRequireUnderReturnsPath() {
        Assert.assertNotNull(SecurePathUtil.requireUnder("/data/ops/upload", "/data/ops/upload/a/b.tar"));
    }

    @Test
    public void testSanitizeSegment() {
        Assert.assertEquals("5.0.1", SecurePathUtil.sanitizeSegment("5.0.1"));
        Assert.assertEquals("openGauss-5.0.1-CentOS-64bit.tar.bz2",
            SecurePathUtil.sanitizeSegment("openGauss-5.0.1-CentOS-64bit.tar.bz2"));
        for (String bad : new String[] {"../x", "a/b", "a\\b", "..", ".", "", null, "a b", "a|b"}) {
            try {
                SecurePathUtil.sanitizeSegment(bad);
                Assert.fail("expected CustomException for: " + bad);
            } catch (CustomException expected) {
                // expected
            }
        }
    }

    @Test
    public void testStrictlyUnderRejectsEqualBase() {
        // delete-type operations must never accept the base directory itself
        Assert.assertFalse(SecurePathUtil.isStrictlyUnder("/data/ops/upload", "/data/ops/upload"));
        Assert.assertTrue(SecurePathUtil.isStrictlyUnder("/data/ops/upload", "/data/ops/upload/pkg/a.tar"));
        Assert.assertFalse(SecurePathUtil.isStrictlyUnder("/data/ops/upload", "/etc/passwd"));
        try {
            SecurePathUtil.requireStrictlyUnder("/data/ops/upload", "/data/ops/upload");
            Assert.fail("expected CustomException for candidate equal to base");
        } catch (CustomException expected) {
            Assert.assertTrue(expected.getMessage().contains("outside (or equal to)"));
        }
        Assert.assertNotNull(SecurePathUtil.requireStrictlyUnder("/data/ops/upload", "/data/ops/upload/a.tar"));
    }

    @Test
    public void testSanitizeFileName() {
        // spaces, non-ASCII and printable specials are allowed (legacy DB rows keep working)
        Assert.assertEquals("openGauss 5.0.1.tar.bz2", SecurePathUtil.sanitizeFileName("openGauss 5.0.1.tar.bz2"));
        Assert.assertEquals("openGauss中文包.tar.gz", SecurePathUtil.sanitizeFileName("openGauss中文包.tar.gz"));
        Assert.assertEquals("a+b(1).tar.gz", SecurePathUtil.sanitizeFileName("a+b(1).tar.gz"));
        // separators, traversal names and control characters are rejected
        for (String bad : new String[] {null, "", "   ", ".", "..", "a/b", "a\\b", "a\0b", "a\nb", "a\tb"}) {
            try {
                SecurePathUtil.sanitizeFileName(bad);
                Assert.fail("expected CustomException for: " + bad);
            } catch (CustomException expected) {
                // expected
            }
        }
    }

    /**
     * T7: fixate the attack payloads verified during the T2/T3 rework as regression tests.
     * Each entry is a real-world path escape technique that MUST keep being rejected.
     */
    @Test
    public void testAttackPayloadsRejected() {
        String base = "/data/ops/upload";
        String[] payloads = {
            "../etc/passwd",                      // classic parent traversal
            "/etc/passwd",                        // absolute path outside base
            "/data/ops/upload/../../etc/shadow",  // nested traversal escaping base
            "..\\..\\windows\\system32",          // windows-style traversal
            "/data/ops/upload/..%2f..%2fetc",     // url-encoded traversal kept literally in segment
            "a\0b",                               // null byte injection
            "/data/ops/upload\n/etc/passwd",      // newline injection
        };
        for (String payload : payloads) {
            Assert.assertFalse("payload must be rejected: " + payload,
                SecurePathUtil.isUnder(base, payload));
        }
    }

    @Test
    public void testRelativeTraversalFromBaseRejected() {
        // a relative candidate starting inside base must not escape via ..
        Assert.assertFalse(SecurePathUtil.isUnder("/data/ops/upload", "pkg/../../etc/passwd"));
    }
}
