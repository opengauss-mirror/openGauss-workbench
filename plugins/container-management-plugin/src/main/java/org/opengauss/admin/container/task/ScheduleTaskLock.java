/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * ScheduleTaskLock.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/task/ScheduleTaskLock.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.task;

import org.jetbrains.annotations.NotNull;
import org.opengauss.admin.container.beans.mapper.TaskLock;
import org.opengauss.admin.container.mapper.TaskLockMapper;
import org.opengauss.admin.container.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * 分布式锁工具
 *
 * @since 2024-06-23
 */
@Component
public class ScheduleTaskLock {
    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskLock.class);

    @Value("${myProps.lock.expireInterval:3}")
    private Integer lockExpireInterval;

    @Autowired
    private TaskLockMapper taskLockMapper;

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁唯一key
     * @return 获取结果（是|否）
     */
    public boolean tryGeneralTaskLock(String lockKey) {
        TaskLock taskLock = taskLockMapper.selectById(lockKey);
        String localIp = IpUtil.getHostIp();
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, lockExpireInterval);
        Date expireDate = calendar.getTime();
        if (Objects.isNull(taskLock)) {
            // 本机抢占
            TaskLock newTaskLock = getNewTaskLock(lockKey, localIp, expireDate, now);
            int insert = taskLockMapper.insert(newTaskLock);
            if (insert > 0) {
                log.info("Attempted to obtain ({}) distributed lock for scheduled tasks, "
                        + "successfully locked:({})", lockKey, insert);
                return true;
            }
            log.info("Failed to obtain ({}) task distributed lock, "
                    + "database insertion operation failed:", lockKey);
        } else {
            Date expireTime = taskLock.getExpireTime();
            if (check(lockKey, localIp, taskLock, expireDate, expireTime)) {
                return true;
            }
            if (expireTime == null || expireTime.before(now)) {
                log.info("Machine ({}) holds ({}) task distributed lock, expired:({})",
                        taskLock.getLockObject(), lockKey, expireTime);
                TaskLock updateTaskLock = getNewTaskLock(lockKey, localIp, expireDate, null);
                int update = taskLockMapper.updateById(updateTaskLock);
                if (update > 0) {
                    log.info("The local machine ({}) has obtained an expired ({}) "
                                    + "task distributed lock held by the host ({}), "
                                    + "update expiration time:{}", localIp,
                            taskLock.getLockObject(), lockKey, expireDate);
                    return true;
                }
                log.info("The local machine ({}) failed to obtain the distributed lock of the ({}) "
                                + "task held by the host ({}). Expiration time:{}!",
                        localIp, taskLock.getLockObject(), lockKey, expireTime);
            }
            log.info("The local machine ({}) failed to obtain the distributed lock for the ({}) task, "
                            + "which is held by ({}) and has expired;{}",
                    localIp, lockKey, taskLock.getLockObject(), expireTime);
            return false;
        }
        return false;
    }

    @NotNull
    private static TaskLock getNewTaskLock(String lockKey, String localIp,
                                           Date expireDate, Date now) {
        TaskLock newTaskLock = new TaskLock();
        newTaskLock.setLockKey(lockKey);
        newTaskLock.setLockObject(localIp);
        newTaskLock.setExpireTime(expireDate);
        newTaskLock.setCreateTime(now);
        return newTaskLock;
    }

    private boolean check(String lockKey, String localIp, TaskLock taskLock,
                          Date expireDate, Date expireTime) {
        if (localIp.equalsIgnoreCase(taskLock.getLockObject())) {
            log.info("Attempt to obtain ({}) task distributed lock, "
                    + "held by the local machine ({}):", lockKey, localIp);
            TaskLock updateTaskLock = new TaskLock();
            updateTaskLock.setLockKey(lockKey);
            updateTaskLock.setExpireTime(expireDate);
            int update = taskLockMapper.updateById(updateTaskLock);
            if (update > 0) {
                log.info("Local machine ({}) holds ({}) task distributed lock, "
                        + "update expiration time: ({})", localIp, lockKey, expireDate);
                return true;
            }
            log.info("The local machine ({}) failed to obtain the expiration time of the ({}) "
                            + "held by the host ({}): {} Task distributed lock failed!",
                    localIp, taskLock.getLockObject(), lockKey,
                    expireTime);
        }
        return false;
    }
}
