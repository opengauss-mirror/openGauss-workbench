/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.agent.repository;

import org.opengauss.admin.common.exception.ops.AgentTaskException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TableNameGenerator
 *
 * @author: wangchao
 * @Date: 2025/5/26 16:26
 * @since 7.0.0-RC2
 **/
public class TableNameGenerator {
    private static final int MAX_CORE_LENGTH = 21;
    private static final int HASH_LENGTH = 6; // 哈希长度6位
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static MessageDigest digest;
    private static final Pattern COMPILE = Pattern.compile("[a-zA-Z0-9]");

    static {
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AgentTaskException("SHA-1 algorithm not found: ", e);
        }
    }

    /**
     * build dynamic realtime table name by taskId
     *
     * @param taskId taskId
     * @return table name
     */
    public static String getRealTimeTableName(String taskId) {
        return "agent_pipe_real_" + processCore(taskId);
    }

    /**
     * build dynamic history table name by taskId
     *
     * @param taskId taskId
     * @return table name
     */
    public static String getHistoryTableName(String taskId) {
        return "agent_pipe_hist_" + processCore(taskId);
    }

    /**
     * build dynamic fingerprint table name by taskId
     *
     * @param taskId taskId
     * @return table name
     */
    public static String getFingerprintTableName(String taskId) {
        return "agent_pipe_fing_" + processCore(taskId);
    }

    /**
     * build dynamic tree table name by taskId
     *
     * @param taskId taskId
     * @return table name
     */
    public static String getTreeTableName(String taskId) {
        return "agent_pipe_tree_" + processCore(taskId);
    }

    /**
     * build dynamic tree node table name by taskId
     *
     * @param taskId taskId
     * @return table name
     */
    public static String getTreeNodeTableName(String taskId) {
        return "agent_pipe_tree_node_" + processCore(taskId);
    }

    /**
     * validate taskId
     *
     * @param taskId taskId
     */
    public static void validateTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new AgentTaskException("Task ID cannot be empty");
        }
        if (!taskId.matches("^[a-zA-Z0-9_]+$")) {
            throw new AgentTaskException("Invalid Task ID characters");
        }
    }

    private static String processCore(String taskId) {
        if (taskId.length() <= MAX_CORE_LENGTH) {
            return taskId.toLowerCase(Locale.ROOT);
        }
        int dynamicTruncateLength = MAX_CORE_LENGTH - HASH_LENGTH;
        int startIndex = Math.max(0, taskId.length() - dynamicTruncateLength);
        String truncated = taskId.substring(startIndex).toLowerCase(Locale.ROOT);
        Matcher matcher = COMPILE.matcher(truncated);
        if (matcher.find()) {
            int firstValidCharIndex = matcher.start();
            truncated = truncated.substring(firstValidCharIndex);
        }
        truncated = truncated.length() > dynamicTruncateLength
            ? truncated.substring(0, dynamicTruncateLength)
            : truncated;
        String hashSuffix = calculateHashSuffix(taskId);
        return truncated.endsWith("_") ? truncated + hashSuffix : truncated + "_" + hashSuffix;
    }

    private static String longToBase36(long value, int length) {
        StringBuilder sb = new StringBuilder();
        long tmp = value & Long.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            int index = (int) (tmp % 36);
            sb.insert(0, BASE62.charAt(index));
            tmp /= 36;
        }
        return sb.toString();
    }

    private static String calculateHashSuffix(String taskId) {
        String salt = Integer.toHexString(taskId.hashCode()).toLowerCase();
        byte[] hashBytes = digest.digest((taskId + salt + "db_salt").getBytes(StandardCharsets.UTF_8));
        byte[] last8Bytes = Arrays.copyOfRange(hashBytes, hashBytes.length - 8, hashBytes.length);
        byte[] first8Bytes = Arrays.copyOfRange(hashBytes, 0, 8);
        BigInteger combined = new BigInteger(1, concatenate(first8Bytes, last8Bytes));
        return longToBase36(combined.longValue(), HASH_LENGTH);
    }

    private static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
