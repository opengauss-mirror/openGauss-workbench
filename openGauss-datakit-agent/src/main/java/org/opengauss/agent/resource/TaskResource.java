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

package org.opengauss.agent.resource;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import retrofit2.http.Body;

import org.opengauss.agent.client.AgentServerClient;
import org.opengauss.agent.client.ServerClientFactory;
import org.opengauss.agent.common.MutinyExecutor;
import org.opengauss.agent.config.AppConfig;
import org.opengauss.agent.entity.TaskExecution;
import org.opengauss.agent.entity.task.AgentTaskVo;
import org.opengauss.agent.exception.AgentException;
import org.opengauss.agent.service.task.TaskManager;
import org.opengauss.agent.service.task.core.TaskExecutionRecordService;
import org.opengauss.agent.utils.RsaUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TaskResource
 *
 * @author: wangchao
 * @Date: 2025/2/28 20:04
 * @Description: TaskResource
 * @since 7.0.0-RC2
 **/
@Unremovable
@Slf4j
@Path("/agent")
public class TaskResource {
    @Inject
    MutinyExecutor mutinyExecutor;
    @Inject
    TaskManager taskManager;
    @Inject
    AppConfig appConfig;
    @Inject
    TaskExecutionRecordService taskExecutionRecordService;
    AgentServerClient agentServerClient;
    @Inject
    ServerClientFactory clientFactory;
    private final AtomicBoolean isCalledTask = new AtomicBoolean(false);
    private final Object taskLock = new Object();
    private ScheduledFuture<?> scheduledTask;

    /**
     * init
     */
    void init() {
        agentServerClient = clientFactory.createAgentServerClient();
        scheduledTask = mutinyExecutor.schedule(() -> {
            if (taskManager.isEmpty() || !isCalledTask.get()) {
                log.info("no task to inspect");
                try {
                    agentServerClient.taskCallbackStart(appConfig.getAgentId());
                    log.info("task callback start success");
                    isCalledTask.set(true);
                } catch (AgentException ex) {
                    log.error("task callback start failed", ex);
                }
            } else {
                cancelSchedule();
            }
        }, 5, TimeUnit.SECONDS);
        log.info("TaskResource init start task callback scheduler success");
    }

    private void cancelSchedule() {
        synchronized (taskLock) {
            if (scheduledTask != null && !scheduledTask.isDone()) {
                scheduledTask.cancel(true);
            }
        }
    }

    /**
     * stop
     *
     * @param event event
     */
    void onStop(@Observes ShutdownEvent event) {
        cancelSchedule();
    }

    /**
     * health check: other processes can call this endpoint to check if the process is still alive
     * /agent/health
     *
     * @return string
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        log.debug("agent health {}", Instant.now());
        return "success";
    }

    /**
     * get secret rsa pub key
     *
     * @return pub key
     */
    @GET
    @Path("/pubKey")
    @Produces(MediaType.APPLICATION_JSON)
    public String pubKey() {
        return RsaUtils.publicKey();
    }

    /**
     * start task
     *
     * @param taskConfig task config
     */
    @POST
    @Path("/task/start")
    @Produces(MediaType.APPLICATION_JSON)
    public void start(@Body AgentTaskVo taskConfig) {
        log.info("start task {}", taskConfig);
        isCalledTask.set(true);
        mutinyExecutor.getWorkerPool().execute(() -> {
            TaskExecution execution = taskManager.startTask(taskConfig);
            if (Objects.nonNull(execution)) {
                taskExecutionRecordService.save(execution);
            }
        });
    }

    /**
     * stop task
     *
     * @param taskId task id
     */
    @POST
    @Path("/task/stop")
    @Produces(MediaType.APPLICATION_JSON)
    public void stop(Long taskId) {
        Optional<TaskExecution> execution = taskManager.stop(taskId);
        execution.ifPresent(taskExecution -> {
            taskExecutionRecordService.save(taskExecution);
        });
    }
}
