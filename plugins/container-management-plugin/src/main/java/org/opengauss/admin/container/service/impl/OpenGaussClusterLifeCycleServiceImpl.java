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
 * OpenGaussClusterLifeCycleServiceImpl.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/impl
 * /OpenGaussClusterLifeCycleServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodIP;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.beans.OpenGaussCluster;
import org.opengauss.admin.container.beans.OpenGaussClusterDetail;
import org.opengauss.admin.container.beans.OpenGaussClusterDto;
import org.opengauss.admin.container.beans.OpenGaussClusterSwitchoverDto;
import org.opengauss.admin.container.beans.OpenGaussClusterVo;
import org.opengauss.admin.container.beans.OpenGaussImage;
import org.opengauss.admin.container.beans.OpenGaussOperator;
import org.opengauss.admin.container.beans.OpenGaussPodVo;
import org.opengauss.admin.container.beans.kubernetes.ContainerImage;
import org.opengauss.admin.container.beans.kubernetes.CrdDefinition;
import org.opengauss.admin.container.beans.kubernetes.DBStorageTemplate;
import org.opengauss.admin.container.beans.kubernetes.DbBasicInfo;
import org.opengauss.admin.container.beans.kubernetes.EnvVar;
import org.opengauss.admin.container.beans.kubernetes.OpenGaussClusterCrd;
import org.opengauss.admin.container.beans.kubernetes.OpenGaussClusterSpec;
import org.opengauss.admin.container.beans.kubernetes.OpenGaussClusterStatus;
import org.opengauss.admin.container.beans.kubernetes.OpenGaussClusterStatusCondition;
import org.opengauss.admin.container.beans.kubernetes.ResourceList;
import org.opengauss.admin.container.config.ConstantEnum;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.container.constant.OpenGaussClusterConstants;
import org.opengauss.admin.container.exception.MarsRuntimeException;
import org.opengauss.admin.container.kubernetes.api.CrdApi;
import org.opengauss.admin.container.kubernetes.api.PodApi;
import org.opengauss.admin.container.mapper.OpenGaussClusterMapper;
import org.opengauss.admin.container.service.OpenGaussClusterLifeCycleService;
import org.opengauss.admin.container.service.OpenGaussImageService;
import org.opengauss.admin.container.service.OpenGaussOperatorService;
import org.opengauss.admin.container.service.cache.K8sClusterCacheManager;
import org.opengauss.admin.container.service.dto.ClusterCheckParams;
import org.opengauss.admin.container.util.StorageUnitConverter;
import org.opengauss.admin.container.util.VerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.opengauss.admin.container.constant.OpenGaussClusterConstants.CLUSTER_SWITCHOVER_STATUS;

/**
 * OpenGaussClusterLifeCycleServiceImpl
 *
 * @since 2024-08-29
 */
@Slf4j
@Service
public class OpenGaussClusterLifeCycleServiceImpl implements OpenGaussClusterLifeCycleService {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z]([-a-z0-9]{0,61}[a-z0-9])?$");
    private static final String POSTGRES = "postgres";
    private static final String ARM = "arm";
    private static final String X86 = "x86";

    @Autowired
    private OpenGaussOperatorService openGaussOperatorService;

    @Autowired
    private OpenGaussImageService openGaussImageService;

    @Autowired
    private CrdApi crdApi;

    @Autowired
    private PodApi podApi;

    @Autowired
    private K8sClusterCacheManager k8sClusterCacheManager;

    @Autowired
    private OpenGaussClusterMapper openGaussClusterMapper;


    private Boolean isValidOpenGaussName(String name) {
        // 不能为空，长度不能超过50
        if (name == null || name.length() > 50) {
            return false;
        }

        // 必须字母小写，必须以字母开头，名称当中只能够包含字母、数字和中划线（-）
        boolean isNameValid = NAME_PATTERN.matcher(name).matches();
        if (!isNameValid) {
            return false;
        }

        // 检查名称中是否包含"."或"_"，以及名称是否全为数字等其他条件
        return (!name.contains(".")) && (!name.contains("_")) && (!name.matches("^\\d+$"));
    }

    private String parameterAbnormality(OpenGaussClusterDto openGaussClusterDto) {
        String openGaussName = openGaussClusterDto.getOpenGaussName();
        if (!isValidOpenGaussName(openGaussName)) {
            return "集群名字异常";
        }

        // 默认值为25400。后端限制范围在20001~65529
        Integer port = openGaussClusterDto.getPort();
        if (port < 20001 || port > 65529) {
            return "端口范围异常";
        }

        // 磁盘容量。最低500G
        Integer diskCapacity = openGaussClusterDto.getDiskCapacity();
        if (diskCapacity < 500) {
            return "磁盘容量设置异常";
        }

        // 自定义用户
        List<DbBasicInfo> dbList = openGaussClusterDto.getDbList();
        if (dbList != null && !dbList.isEmpty()) {
            List<String> forbiddenAcc = Arrays.asList("root", "zabbixjk", "inception", "bkpuser", "devops_db",
                    "operator");
            for (DbBasicInfo db : dbList) {
                String dbName = db.getDatabase();
                String account = db.getUser();
                String password = db.getPwd();
                if (POSTGRES.equalsIgnoreCase(dbName) || StringUtils.isBlank(dbName)) {
                    return "自定义数据库名不合规";
                }
                if (forbiddenAcc.contains(account) || StringUtils.isBlank(account)) {
                    return "自定义用户名不合规";
                }
                if (StringUtils.isBlank(password)) {
                    return "自定义用户密码不合规";
                }
            }
        }
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    private OpenGaussOperator getOperatorName(OpenGaussClusterDto openGaussClusterDto) {
        // 获取type
        String type = ConstantEnum.OPENGAUSS_OPERATOR_TYPE_PRD.getValue();
        if (openGaussClusterDto.getIsTest()) {
            type = ConstantEnum.OPENGAUSS_OPERATOR_TYPE_TEST.getValue();
        }

        // 获取使用量最低的operator
        List<OpenGaussOperator> operatorList =
                openGaussOperatorService.recommendedOperator(openGaussClusterDto.getK8sId(), type);
        if (operatorList.isEmpty() || operatorList.get(0).getCurrentManageQuantity() > 100) {
            log.error("Failed to get Operator information");
            throw new MarsRuntimeException("Failed to get Operator information");
        }
        return operatorList.get(0);
    }

    private String returnImagePrefix(String architecture) {
        if (ARM.equals(architecture)) {
            return ConstantEnum.ARM_IMAGE_PREFIX.getValue();
        } else if (X86.equals(architecture)) {
            return ConstantEnum.X86_IMAGE_PREFIX.getValue();
        } else {
            throw new MarsRuntimeException("Images are not supported" + architecture);
        }
    }

    private ContainerImage getContainerImage(OpenGaussClusterDto dto) {
        ContainerImage containerImage = new ContainerImage();
        containerImage.setGaussDB(returnImagePrefix(dto.getArchitecture())
                + ConstantEnum.OPENGAUSS_IMAGE.getValue() + dto.getImage());
        List<OpenGaussImage> backupImageList = openGaussImageService.selectImageList(
                "backup",
                dto.getArchitecture(),
                dto.getOs(),
                dto.getVersion(),
                true
        );
        if (backupImageList.isEmpty()) {
            log.error("Failed to get backup image");
            throw new MarsRuntimeException("Failed to get backup image");
        }
        containerImage.setGaussBackupRecovery(returnImagePrefix(dto.getArchitecture())
                + ConstantEnum.BACKUP_IMAGE.getValue() + backupImageList.get(0).getImage());
        List<OpenGaussImage> exporterImageList = openGaussImageService.selectImageList("exporter",
                dto.getArchitecture(), dto.getOs(), dto.getVersion(), true);
        if (exporterImageList.isEmpty()) {
            log.error("Failed to get exporter image");
            throw new MarsRuntimeException("Failed to get exporter image");
        }
        containerImage.setGaussExporter(returnImagePrefix(dto.getArchitecture())
                + ConstantEnum.EXPORTER_IMAGE.getValue()
                + exporterImageList.get(0).getImage());

        // cleanup 镜像
        List<OpenGaussImage> cleanupImageList =
                openGaussImageService.selectImageList(OpenGaussClusterConstants.OPENGAUSS_CLUSTER_IMAGE_TYPE_CLEANUP,
                        dto.getArchitecture(),
                        dto.getOs(), dto.getVersion(), true);
        if (cleanupImageList.isEmpty()) {
            log.error("Failed to get cleanup image");
            throw new MarsRuntimeException("Failed to get cleanup image");
        }
        String cleanupImage = cleanupImageList.get(0).getImage();
        containerImage.setGaussCleanupData(returnImagePrefix(dto.getArchitecture())
                + ConstantEnum.CLEANUP_IMAGE.getValue() + cleanupImage);
        return containerImage;
    }

    private V1ObjectMeta fillOpenGaussClusterMetadataYaml(OpenGaussClusterDto openGaussClusterDto) {
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(openGaussClusterDto.getOpenGaussName());
        metadata.setNamespace(ConstantEnum.OPENGAUSS_NAMESPACE.getValue());

        // 选择operator
        OpenGaussOperator operator = getOperatorName(openGaussClusterDto);
        if (operator == null) {
            log.error("Failed to get Operator information");
            throw new MarsRuntimeException("Failed to get Operator information");
        }
        Map<String, String> labels = new HashMap<>();
        labels.put(ConstantEnum.OPENGAUSS_OPERATOR_LABEL_KEY.getValue(), operator.getName());
        labels.put(ConstantEnum.OPENGAUSS_CMDB_SYSTEM_ID_LABEL_KEY.getValue(),
                openGaussClusterDto.getCmdbSystemId());
        metadata.setLabels(labels);
        return metadata;
    }

    private OpenGaussClusterSpec fillOpenGaussClusterSpecYaml(OpenGaussClusterDto openGaussClusterDto) {
        OpenGaussClusterSpec spec = new OpenGaussClusterSpec();
        // 集群架构
        spec.setArchitecture(openGaussClusterDto.getArchType());

        // 网络方案
        spec.setNetworkType(ConstantEnum.NETWORK_MACVLAN.getValue());

        // 集群实例数量
        spec.setDataNodeNum(openGaussClusterDto.getInstance());

        // 集群维护
        spec.setMaintenance(false);

        // 镜像
        ContainerImage containerImage = getContainerImage(openGaussClusterDto);
        spec.setImages(containerImage);

        // 资源
        Map<String, Object> requestResource = openGaussClusterDto.getRequestResource();
        Map<String, Object> limitResource = openGaussClusterDto.getLimitResource();
        ResourceList resourceList = new ResourceList();
        resourceList.setRequests(requestResource);
        resourceList.setLimits(limitResource);
        spec.setDbResources(resourceList);

        // 存储
        DBStorageTemplate dbStorageTemplate = new DBStorageTemplate();
        dbStorageTemplate.setDiskCapacity(openGaussClusterDto.getDiskCapacity() + ConstantEnum.DISKUNIT.getValue());
        dbStorageTemplate.setStorageClassName(ConstantEnum.STORAGE_CLASS_NAME_LOCAL_STORAGE.getValue());
        dbStorageTemplate.setStorageType(ConstantEnum.STORAGE_TYPE.getValue());
        spec.setDbStorage(dbStorageTemplate);

        // 端口
        spec.setDbPort(openGaussClusterDto.getPort());

        // 主机亲和标签
        spec.setNodeAffinity(openGaussClusterDto.getResourceType());

        // 环境变量
        List<EnvVar> envList = new ArrayList<>();
        EnvVar env = new EnvVar();
        env.setName("GS_PASSWORD");
        env.setValue(ConstantEnum.OPENGAUSS_ROOT_PASSWORD_PRD.getValue());
        envList.add(env);
        spec.setEnv(envList);

        // 用户自定义database、account、password
        spec.setDatabaseInfo(openGaussClusterDto.getDbList());
        return spec;
    }

    private OpenGaussClusterCrd fillOpenGaussClusterCrYaml(OpenGaussClusterDto openGaussClusterDto) {
        OpenGaussClusterCrd openGaussClusterCrd = new OpenGaussClusterCrd();

        // ApiVersion
        openGaussClusterCrd.setApiVersion(ConstantEnum.OPENGAUSS_CRD_GROUP.getValue()
                + ConstantEnum.OPENGAUSS_CRD_SLASH.getValue()
                + ConstantEnum.OPENGAUSS_CRD_VERSION.getValue());

        // Kind
        openGaussClusterCrd.setKind(ConstantEnum.OPENGAUSS_CRD_KIND.getValue());

        // Metadata
        V1ObjectMeta metadata = fillOpenGaussClusterMetadataYaml(openGaussClusterDto);
        openGaussClusterCrd.setMetadata(metadata);

        // Spec
        OpenGaussClusterSpec spec = fillOpenGaussClusterSpecYaml(openGaussClusterDto);
        openGaussClusterCrd.setSpec(spec);
        return openGaussClusterCrd;
    }

    private CrdDefinition newCrdDefinition() {
        String kind = ConstantEnum.OPENGAUSS_CRD_KIND.getValue();
        String group = ConstantEnum.OPENGAUSS_CRD_GROUP.getValue();
        String version = ConstantEnum.OPENGAUSS_CRD_VERSION.getValue();
        String plural = ConstantEnum.OPENGAUSS_CRD_PLURAL.getValue();
        String singular = ConstantEnum.OPENGAUSS_CRD_SINGULAR.getValue();
        String namespace = ConstantEnum.OPENGAUSS_NAMESPACE.getValue();
        return new CrdDefinition(kind, group, version, plural, singular, namespace);
    }

    @Override
    public String deploy(OpenGaussClusterDto openGaussClusterDto) {
        // 参数判空
        String parJudgeResult = parameterAbnormality(openGaussClusterDto);
        if (!CommonConstant.RETURN_CODE_SUCCESS.equals(parJudgeResult)) {
            return "Parameter exception:" + parJudgeResult;
        }
        if (checkExistOpenGaussClusterByName(openGaussClusterDto)) {
            return "Creation failed, cluster name is duplicated";
        }

        // 填充yaml信息
        OpenGaussClusterCrd openGaussClusterCrd;
        try {
            openGaussClusterCrd = fillOpenGaussClusterCrYaml(openGaussClusterDto);
        } catch (MarsRuntimeException e) {
            return "yaml filling exception," + e.getErrorMessage();
        }

        // 获取k8s信息
        String k8sId = openGaussClusterDto.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sId);
        if (k8sCluster == null) {
            log.error("[OpenGauss Cluster Creation]k8scluster(" + k8sId + ")Does not exist or is invalid!");
            return "OpenGaussian Cluster corresponds to a cluster(" + k8sId + ")Does not exist or is invalid！";
        }

        // 入库
        Boolean isSaveDbFlg = insertOpenGaussCluster(openGaussClusterDto);
        if (!isSaveDbFlg) {
            log.error("[OpenGauss Cluster Creation]Failed to save information to database!");
            return "Failed to save opengaussCluster cluster information！";
        }

        // 创建CR
        Boolean isCreateResult = crdApi.createCrd(k8sCluster, newCrdDefinition(), openGaussClusterCrd);
        if (!isCreateResult) {
            rollBackOpenGaussClusterInfo(openGaussClusterDto);
            return "Failed to create yaml";
        }

        // 增加operator管理数量
        String operatorName =
                openGaussClusterCrd.getMetadata().getLabels().get(ConstantEnum.OPENGAUSS_OPERATOR_LABEL_KEY.getValue());
        updateOperatorNum(operatorName, clusterType(openGaussClusterDto.getIsTest()), k8sCluster.getId(),
                ConstantEnum.OPERATOR_NUM_ADD.getValue());
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    @Override
    public String release(Integer id) {
        // 从数据库获取集群信息
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            return "Delete failed, cluster query exception";
        }

        // 查询CR是否存在
        String k8sId = openGaussCluster.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sId);
        if (k8sCluster == null) {
            log.error("[OpenGauss Cluster Creation]k8scluster(" + k8sId + ")Does not exist or is invalid!");
            return "OpenGaussian Cluster corresponds to a cluster(" + k8sId + ")Does not exist or is invalid!";
        }
        OpenGaussClusterCrd openGaussClusterCrd = crdApi.getCrdByNameAndNamespace(k8sCluster, newCrdDefinition(),
                openGaussCluster.getName(), openGaussCluster.getNamespace(), OpenGaussClusterCrd.class);
        if (openGaussClusterCrd == null) {
            log.error("query{} CR Exception", openGaussCluster.getName());
        }

        // 删除CR
        if (!crdApi.deleteCrdByName(k8sCluster, newCrdDefinition(), openGaussCluster.getName())) {
            return "delete{} CR Exception";
        }

        // 删除open_gauss_cluster表中数据
        if (openGaussClusterMapper.deleteById(id) != 1) {
            log.error("delete{}Cluster information exception", openGaussCluster.getName());
        }

        // operator管理数量减1
        String operatorName =
                openGaussClusterCrd.getMetadata().getLabels().get(ConstantEnum.OPENGAUSS_OPERATOR_LABEL_KEY.getValue());
        Boolean isTestCluster = openGaussCluster.getIsTestCluster();
        updateOperatorNum(operatorName, clusterType(isTestCluster), k8sCluster.getId(),
                ConstantEnum.OPERATOR_NUM_REDUCE.getValue());
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    private Map<String, OpenGaussClusterCrd> crMap(String k8sId) {
        Map<String, OpenGaussClusterCrd> ogcMap = new HashMap<>();
        List<K8sCluster> k8sClusters = new ArrayList<>();
        if (k8sId == null) {
            k8sClusters.addAll(k8sClusterCacheManager.listCluster());
        } else {
            k8sClusters.add(k8sClusterCacheManager.getCluster(k8sId));
        }
        log.info("Query OGC list,k8sClusters size:{}", k8sClusters.size());
        for (K8sCluster k8sCluster : k8sClusters) {
            List<OpenGaussClusterCrd> crList = crdApi.getCrdListByNamespaceAndSelector(k8sCluster,
                    newCrdDefinition(), null, null, OpenGaussClusterCrd.class);
            log.info("k8sClusters :{} result:{}", k8sCluster.getName(), crList.size());
            crList.forEach(cr -> ogcMap.put(k8sCluster.getId()
                            + ":" + cr.getMetadata().getNamespace()
                            + ":" + cr.getMetadata().getName(),
                    cr));
        }
        return ogcMap;
    }

    private Page<OpenGaussCluster> selectOgcByDb(String k8sId, String ogcName, Integer pageNum,
                                                 Integer pageSize) {
        String namespace = ConstantEnum.OPENGAUSS_NAMESPACE.getValue();
        Page<OpenGaussCluster> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OpenGaussCluster> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtils.isNotEmpty(ogcName), OpenGaussCluster::getName, ogcName)
                .eq(StringUtils.isNotEmpty(k8sId), OpenGaussCluster::getK8sId, k8sId)
                .eq(StringUtils.isNotEmpty(namespace), OpenGaussCluster::getNamespace, namespace)
                .orderByDesc(OpenGaussCluster::getCreateTime);
        openGaussClusterMapper.selectPage(page, queryWrapper);
        return page;
    }

    private Map<String, Object> conOpenGaussClusterVo(Map<String, OpenGaussClusterCrd> ogcMap,
                                                      Page<OpenGaussCluster> page) {
        List<OpenGaussClusterVo> resultList = new ArrayList<>();
        for (OpenGaussCluster openGaussCluster : page.getRecords()) {
            String key =
                    openGaussCluster.getK8sId() + ":"
                            + openGaussCluster.getNamespace() + ":"
                            + openGaussCluster.getName();
            OpenGaussClusterCrd cr = ogcMap.get(key);
            if (cr == null) {
                log.error("{} get cr error", key);
                continue;
            }
            OpenGaussClusterVo vo = new OpenGaussClusterVo();
            vo.setId(openGaussCluster.getId());
            vo.setName(openGaussCluster.getName());
            vo.setCmdbSysName(openGaussCluster.getAppName());
            K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(openGaussCluster.getK8sId());
            if (k8sCluster != null) {
                Set<String> ogcDomain = getOgcDomain(cr.getSpec().getDataNodeNum(), openGaussCluster.getName(),
                        openGaussCluster.getNamespace(), k8sCluster.getDomain());
                vo.setDomains(ogcDomain);
            } else {
                log.warn("[Details of OpenGauss Collection]k8scluster({})Does not exist or is invalid,unable to "
                        + "obtain domain information!", openGaussCluster.getK8sId());
            }
            vo.setPort(openGaussCluster.getPort());
            vo.setInstance(cr.getSpec().getDataNodeNum());
            ResourceList dbResources = cr.getSpec().getDbResources();
            vo.setResourceRequest(dbResources.getRequests());
            vo.setCpuBurstMultiple(openGaussCluster.getCpuLimit());
            vo.setCreateTime(openGaussCluster.getCreateTime());
            vo.setClusterStatus(cr.getStatus().getState());
            vo.setMaintenance(cr.getSpec().getMaintenance());
            if (cr.getSpec().getMaintenance() == null) {
                vo.setMaintenance(false);
            }
            resultList.add(vo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", resultList);
        result.put("total", page.getTotal());
        return result;
    }

    @Override
    public Map<String, Object> openGaussClusterList(String k8sId, String ogcName, Integer pageNum, Integer pageSize) {
        // crMap
        Map<String, OpenGaussClusterCrd> ogcMap = crMap(k8sId);

        // 在数据库筛选
        Page<OpenGaussCluster> page = selectOgcByDb(k8sId, ogcName, pageNum, pageSize);

        // 转换
        return conOpenGaussClusterVo(ogcMap, page);
    }

    @Override
    public OpenGaussClusterDetail openGaussClusterDetail(Integer id) {
        // 数据库查询
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            throw new MarsRuntimeException("Cluster information（" + id + "）Not in stock!");
        }

        // apiServer查询
        OpenGaussClusterCrd cr = getCrByOpenGaussCluster(openGaussCluster);

        // 组合数据
        return assemblyClusterDetails(openGaussCluster, cr);
    }

    @Override
    public List<OpenGaussPodVo> openGaussClusterPodList(Integer id) {
        // 获取openGauss信息
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            log.error("[OpenGauss cluster pod list]opengaussCluster records(id=" + id + ")non-existent!");
            throw new MarsRuntimeException("opengaussCluster records(id=" + id + ")non-existent!");
        }

        // 获取k8sCluster
        String k8sId = openGaussCluster.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sId);
        if (k8sCluster == null) {
            log.error("[OpenGauss cluster]k8scluster(" + k8sId + ")Does not exist or is invalid!");
            throw new MarsRuntimeException("opengaussCluster(" + k8sId + ")Does not exist or is invalid!");
        }

        // 获取cr信息
        OpenGaussClusterCrd cr = getCrByOpenGaussCluster(openGaussCluster);

        // 获取PodList
        String labelSelector = ConstantEnum.OPENGAUSS_NAME_LABEL_KEY.getValue() + "=" + openGaussCluster.getName();
        List<V1Pod> podList = podApi.getPodsByNamespacesAndSelector(k8sCluster, openGaussCluster.getNamespace(),
                labelSelector, null);

        // 提取数据
        return assemblyPodList(podList, cr);
    }

    private List<OpenGaussPodVo> assemblyPodList(List<V1Pod> podList, OpenGaussClusterCrd cr) {
        List<OpenGaussPodVo> podVoList = new ArrayList<>();

        // 主从模式提取
        Map<String, String> masterSlaveMap = new HashMap<>();
        if (cr != null && cr.getStatus() != null && cr.getStatus().getConditions() != null) {
            cr.getStatus().getConditions().stream().forEach(item -> {
                masterSlaveMap.put(item.getName(), item.getState());
            });
        }
        for (V1Pod v1Pod : podList) {
            OpenGaussPodVo vo = new OpenGaussPodVo();
            podVoList.add(vo);
            vo.setName(v1Pod.getMetadata().getName());
            vo.setStatus(v1Pod.getStatus().getPhase());
            vo.setMasterSlaveMode(masterSlaveMap.get(v1Pod.getMetadata().getName()));

            // 资源
            V1ResourceRequirements resources = v1Pod.getSpec().getContainers().get(0).getResources();
            Map<String, Quantity> requests = resources.getRequests();
            Map<String, Quantity> limits = resources.getLimits();
            Map<String, Object> requestResource = new HashMap<>();
            requestResource.put("cpu", StorageUnitConverter.convertCPUValue(requests.get("cpu")
                    .toSuffixedString()));
            requestResource.put("memory", StorageUnitConverter.convertToGB(
                    requests.get("memory").toSuffixedString()));
            vo.setRequestResource(requestResource);
            vo.setCpuBurst(StorageUnitConverter.convertCPUValue(limits.get("cpu").toSuffixedString()));

            // Pod IP
            List<OpenGaussPodVo> podVoList1 = getOpenGaussPodVos(v1Pod, vo, podVoList);
            if (podVoList1 != null) {
                return podVoList1;
            }
        }
        return podVoList;
    }

    @Nullable
    private static List<OpenGaussPodVo> getOpenGaussPodVos(V1Pod v1Pod, OpenGaussPodVo vo,
                                                           List<OpenGaussPodVo> podVoList) {
        List<String> podIps = new ArrayList<>();
        List<V1PodIP> v1PodIPs = v1Pod.getStatus().getPodIPs();
        if (v1PodIPs != null) {
            for (V1PodIP podIP : v1PodIPs) {
                podIps.add(podIP.getIp());
            }
        }
        vo.setPodIps(podIps);
        vo.setNodeIp(v1Pod.getStatus().getHostIP());

        // 创建时间
        OffsetDateTime creationTimestamp = v1Pod.getMetadata().getCreationTimestamp();
        Date createTime = Date.from(creationTimestamp.toLocalDateTime().toInstant(ZoneOffset.ofHours(0)));
        vo.setCreateTime(createTime);

        // 最近重启时间
        List<V1ContainerStatus> containerStatuses = v1Pod.getStatus().getContainerStatuses();
        if (CollectionUtils.isNotEmpty(containerStatuses)) {
            V1ContainerStatus v1ContainerStatus = containerStatuses.get(0);
            if (v1ContainerStatus != null) {
                return podVoList;
            }
            V1ContainerState containerState = v1ContainerStatus.getState();
            if (Objects.nonNull(containerState) && Objects.nonNull(containerState.getRunning())) {
                OffsetDateTime lastRestartTimestamp = containerState.getRunning().getStartedAt();
                Date lastRestartTime = Date.from(lastRestartTimestamp.toLocalDateTime()
                        .toInstant(ZoneOffset.ofHours(0)));
                vo.setLastRestartTime(lastRestartTime);
            }
        }
        return null;
    }

    private OpenGaussClusterCrd getCrByOpenGaussCluster(OpenGaussCluster openGaussCluster) {
        String k8sId = openGaussCluster.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sId);
        if (k8sCluster == null) {
            log.error("[OpenGauss cluster]k8scluster(" + k8sId + ")Does not exist or is invalid!");
            throw new MarsRuntimeException("[OpenGauss cluster]k8scluster("
                    + k8sId + ")Does not exist or is invalid!");
        }
        String name = openGaussCluster.getName();
        String namespace = openGaussCluster.getNamespace();
        return crdApi.getCrdByNameAndNamespace(k8sCluster, newCrdDefinition(), name, namespace,
                OpenGaussClusterCrd.class);
    }

    private OpenGaussClusterDetail assemblyClusterDetails(OpenGaussCluster openGaussCluster, OpenGaussClusterCrd cr) {
        OpenGaussClusterDetail detail = new OpenGaussClusterDetail();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(openGaussCluster.getK8sId());
        String name = openGaussCluster.getName();
        String namespace = openGaussCluster.getNamespace();
        detail.setName(name);
        detail.setNamespace(namespace);
        detail.setK8sId(openGaussCluster.getK8sId());
        detail.setK8sName(k8sCluster.getName());
        detail.setResourceType(openGaussCluster.getResourcePool());
        detail.setCmdbSysName(openGaussCluster.getAppName());
        detail.setOpenGaussImage(openGaussCluster.getImage());
        detail.setBackRecoveryImage(cr.getSpec().getImages().getGaussBackupRecovery());
        detail.setExporterImage(cr.getSpec().getImages().getGaussExporter());
        detail.setPort(openGaussCluster.getPort());
        detail.setInstance(cr.getSpec().getDataNodeNum());
        detail.setRequestResource(cr.getSpec().getDbResources().getRequests());
        detail.setScaleTimes(openGaussCluster.getCpuLimit());
        detail.setDiskCapacity(openGaussCluster.getDiskCapacity());
        detail.setCreator(openGaussCluster.getCreateUser());
        detail.setIsTestCluster(openGaussCluster.getIsTestCluster());
        detail.setIsAddMonitor(openGaussCluster.getIsAddMonitor());
        detail.setArchitecture(openGaussCluster.getArchitecture());
        detail.setArchType(openGaussCluster.getArchType());
        detail.setIsAdd4a(openGaussCluster.getIsAdd4a());
        detail.setIsAddCmdb(openGaussCluster.getIsAddCmdb());
        detail.setCreateTime(openGaussCluster.getCreateTime());
        Set<String> ogcDomain = getOgcDomain(cr.getSpec().getDataNodeNum(), name, namespace, k8sCluster.getDomain());
        detail.setDomain(ogcDomain);
        detail.setRemark(openGaussCluster.getRemark());
        detail.setClusterStatus(cr.getStatus().getState());
        return detail;
    }

    private Set<String> getOgcDomain(int instance, String openGaussClusterName, String namespace,
                                     String clusterDomain) {
        Set<String> domain = new HashSet<>();
        MessageFormat messageFormat = new MessageFormat("{0}.{1}.{2}.{3}.{4}");
        for (int i = 0; i < instance; i++) {
            String key = openGaussClusterName + "-" + i;
            String value = messageFormat.format(new String[]{key, openGaussClusterName, namespace, "svc",
                    clusterDomain});
            domain.add(value);
        }
        return domain;
    }

    private String clusterType(Boolean isTestCluster) {
        if (isTestCluster) {
            return ConstantEnum.OPENGAUSS_OPERATOR_TYPE_TEST.getValue();
        }
        return ConstantEnum.OPENGAUSS_OPERATOR_TYPE_PRD.getValue();
    }

    private void updateOperatorNum(String operatorName, String type, String k8sId, String opt) {
        OpenGaussOperator openGaussOperator = new OpenGaussOperator();
        openGaussOperator.setName(operatorName);
        openGaussOperator.setK8sClusterId(k8sId);
        openGaussOperator.setType(type);
        openGaussOperator = openGaussOperatorService.selectOne(openGaussOperator);
        if (openGaussOperatorService.quantityChange(openGaussOperator, opt) != 1) {
            log.error("Fail to change in the number of{}operator", operatorName);
        }
    }

    private Boolean checkExistOpenGaussClusterByName(OpenGaussClusterDto dto) {
        return openGaussClusterMapper.selectCount(Wrappers.<OpenGaussCluster>lambdaQuery()
                .eq(StringUtils.isNotEmpty(dto.getOpenGaussName()), OpenGaussCluster::getName,
                        dto.getOpenGaussName())) > 0;
    }

    private void rollBackOpenGaussClusterInfo(OpenGaussClusterDto dto) {
        LambdaQueryWrapper<OpenGaussCluster> deleteQueryWrapper = Wrappers.lambdaQuery();
        deleteQueryWrapper.eq(OpenGaussCluster::getK8sId, dto.getK8sId())
                .eq(OpenGaussCluster::getName, dto.getOpenGaussName());
        if (openGaussClusterMapper.delete(deleteQueryWrapper) > 0) {
            log.info("Failed to create OpenGauss cluster[{}-{}]rollback successful",
                    dto.getK8sId(), dto.getOpenGaussName());
        }
    }

    private Boolean insertOpenGaussCluster(OpenGaussClusterDto dto) {
        OpenGaussCluster openGaussCluster = new OpenGaussCluster();
        openGaussCluster.setK8sId(dto.getK8sId());
        openGaussCluster.setName(dto.getOpenGaussName());
        openGaussCluster.setNamespace(ConstantEnum.OPENGAUSS_NAMESPACE.getValue());
        openGaussCluster.setPort(dto.getPort());
        openGaussCluster.setVersion(dto.getVersion());
        openGaussCluster.setImage(dto.getImage());

        // CMDB信息
        openGaussCluster.setCmdbId("");
        openGaussCluster.setAppCode("");
        openGaussCluster.setAppName("");
        openGaussCluster.setCostCenter("");
        openGaussCluster.setCreateUser(dto.getOperator());

        // 资源
        Map<String, Object> requestResource = dto.getRequestResource();
        Map<String, Object> limitResource = dto.getLimitResource();
        String cpuRequest = String.valueOf(requestResource.get("cpu"));
        String cpuLimit = String.valueOf(limitResource.get("cpu"));
        String memRequest = String.valueOf(requestResource.get("memory"));
        openGaussCluster.setCpuRequest(cpuRequest);
        openGaussCluster.setMemRequest(memRequest);
        openGaussCluster.setArchitecture(dto.getArchitecture());
        openGaussCluster.setArchType(dto.getArchType());
        Integer cpuRequestInt = Integer.valueOf(cpuRequest);
        Integer cpuLimitInt = Integer.valueOf(cpuLimit);
        openGaussCluster.setCpuLimit(cpuLimitInt / cpuRequestInt);
        openGaussCluster.setDiskCapacity(dto.getDiskCapacity());
        openGaussCluster.setResourcePool(dto.getResourceType());
        openGaussCluster.setIsTestCluster(dto.getIsTest());

        // 默认为false，没有添加, 通过创建任务去执行
        openGaussCluster.setIsAddCmdb(false);
        openGaussCluster.setIsAddMonitor(false);
        openGaussCluster.setIsAdd4a(false);
        openGaussCluster.setRemark(dto.getRemark());
        openGaussCluster.setCreateTime(new Date());
        openGaussCluster.setUpdateTime(new Date());
        if (openGaussClusterMapper.insert(openGaussCluster) != 1) {
            log.error("Update open_gauss_cluster exception");
            return Boolean.FALSE;
        }
        log.info("insert data:{}", openGaussCluster);
        return Boolean.TRUE;
    }

    /**
     * 重启OpenGauss集群的功能说明
     *
     * @param id 集群id
     * @return 结果信息
     */
    @Override
    public String restartOpengaussCluster(Long id) {
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            log.error("[restart OpenGauss cluster]opengaussCluster records(id=" + id + ")Does not exist!");
            return "opengaussCluster records(id=" + id + ")Does not exist!";
        }
        String k8sClusterId = openGaussCluster.getK8sId();
        String opengaussName = openGaussCluster.getName();
        String namespace = openGaussCluster.getNamespace();
        if (StringUtils.isBlank(k8sClusterId)) {
            log.error("[restart OpenGauss cluster]opengaussCluster k8sClusterId Cannot be empty!");
            return "opengaussCluster k8sClusterId Cannot be empty!";
        }

        // apiServer查询 crd
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sClusterId);
        if (k8sCluster == null) {
            log.error("[restart OpenGauss cluster]k8scluster(" + k8sClusterId + ")Does not exist or is invalid!");
            return "opengaussCluster (" + k8sClusterId + ")Does not exist or is invalid！";
        }
        OpenGaussClusterCrd openGaussClusterCrd = crdApi.getCrdByNameAndNamespace(k8sCluster, newCrdDefinition(),
                opengaussName, namespace, OpenGaussClusterCrd.class);
        if (openGaussClusterCrd == null) {
            log.error("[restart OpenGauss cluster]opengaussCluster resources(name="
                    + opengaussName + ",namespace=" + namespace + ","
                    + "k8sClusterId=" + k8sClusterId + ")Does not exist!");
            return "Cluster(" + opengaussName + ")Does not exist!";
        }
        ClusterCheckParams params = getClusterCheckParams(k8sCluster, openGaussClusterCrd, openGaussCluster);
        String checkOgcState = checkOpengaussClusterState(params, true, true);
        if (!CommonConstant.RETURN_CODE_SUCCESS.equals(checkOgcState)) {
            return checkOgcState;
        }
        List<OpenGaussClusterStatusCondition> conditions = openGaussClusterCrd.getStatus().getConditions();
        StringBuffer buffer = new StringBuffer();
        for (OpenGaussClusterStatusCondition cond : conditions) {
            if ("Primary".equalsIgnoreCase(cond.getState())) {
                buffer.append(cond.getName());
            } else {
                buffer.insert(0, cond.getName() + ",");
            }
        }

        // ogc增加个 dbRollingRestartFlag: true 就会开始滚动重启；
        return getFlag(buffer, k8sCluster, opengaussName);
    }

    @NotNull
    private static ClusterCheckParams getClusterCheckParams(K8sCluster k8sCluster,
                                                            OpenGaussClusterCrd openGaussClusterCrd,
                                                            OpenGaussCluster openGaussCluster) {
        ClusterCheckParams params = new ClusterCheckParams();
        params.setK8sCluster(k8sCluster);
        params.setOpenGaussClusterCrd(openGaussClusterCrd);
        params.setOpengaussName(openGaussCluster.getName());
        params.setNamespace(openGaussCluster.getNamespace());
        return params;
    }

    @NotNull
    private String getFlag(StringBuffer buffer, K8sCluster k8sCluster, String opengaussName) {
        Map<String, Object> rootData = new HashMap<>();
        Map<String, String> anno = new HashMap<>();
        anno.put("rollingRestart/restartPods", buffer.toString());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("annotations", anno);
        rootData.put("metadata", metadata);
        Map<String, Object> spec = new HashMap<>();
        spec.put("dbRollingRestartFlag", Boolean.TRUE);
        rootData.put("spec", spec);
        Boolean isRestartRes = crdApi.updateCRDByNamespaced(k8sCluster, newCrdDefinition(), opengaussName, rootData);
        if (isRestartRes != null && isRestartRes) {
            return CommonConstant.RETURN_CODE_SUCCESS;
        } else {
            return "Restart failed!";
        }
    }

    @SuppressWarnings("all")
    private String checkOpengaussClusterState(ClusterCheckParams params, boolean isCheckRollingState,
                                              boolean isCheckClusterState) {
        OpenGaussClusterCrd openGaussClusterCrd = params.getOpenGaussClusterCrd();
        K8sCluster k8sCluster = params.getK8sCluster();
        String opengaussName = params.getOpengaussName();
        String namespace = params.getNamespace();
        OpenGaussClusterCrd tempOpenGaussClusterCrd = openGaussClusterCrd;
        if (openGaussClusterCrd == null) {
            if (crdApi == null) {
                crdApi = new CrdApi();
            }
            tempOpenGaussClusterCrd = crdApi.getCrdByNameAndNamespace(k8sCluster,
                    newCrdDefinition(), opengaussName, namespace, OpenGaussClusterCrd.class);
        }
        String k8sClusterId = k8sCluster.getId();
        if (Objects.isNull(tempOpenGaussClusterCrd)) {
            log.info("Cluster CRD resources(name={} ,namespace= {}, k8sClusterId={})Does not exist!", opengaussName,
                    namespace,
                    k8sClusterId);
            return "OpenGauss cluster CRD resource does not exist!";
        }
        OpenGaussClusterStatus status = tempOpenGaussClusterCrd.getStatus();
        if (isCheckClusterState) {
            if (status == null) {
                log.error("Cluster resources(name={},namespace={}},k8sClusterId={})Status cannot be obtained, cannot "
                                + "restart!",
                        opengaussName, namespace,
                        k8sClusterId);
                return "Cluster(" + opengaussName + ")Status cannot be obtained, this operation is not supported!";
            }
            if (!OpenGaussClusterConstants.CLUSTER_STATE_RUNNING.equalsIgnoreCase(status.getState())) {
                log.error("Cluster CRD resources(name={}},namespace={}},k8sClusterId={}})Status is({}),cannot be "
                                + "modified!", opengaussName, namespace,
                        k8sClusterId, status.getState());
                return "Cluster(" + opengaussName + ")Status is(" + status.getState() + "),This operation is not "
                        + "supported!";
            }
        }
        Boolean isDbRollingRestartFlag = tempOpenGaussClusterCrd.getSpec().getDbRollingRestartFlag();
        if (isCheckRollingState && !Objects.isNull(isDbRollingRestartFlag) && isDbRollingRestartFlag) {
            log.info("Cluster CRD resources(name={}},namespace={}},k8sClusterId={}})status is({}-{})check did not "
                            + "pass!", opengaussName, namespace,
                    k8sClusterId, status.getState(), isDbRollingRestartFlag);
            return "Cluster rolling restart,this operation is not supported!";
        }
        log.info("Cluster CRD resources(name={}},namespace={}},k8sClusterId={}})status is({}-{})Through status check!",
                opengaussName, namespace, k8sClusterId, status.getState(), isDbRollingRestartFlag);
        return CommonConstant.RETURN_CODE_SUCCESS;
    }

    /**
     * switchoverOpengaussCluster的功能说明
     *
     * @param switchoverDto switchoverDto
     * @return String
     */
    @Override
    public String switchoverOpengaussCluster(OpenGaussClusterSwitchoverDto switchoverDto) {
        Long id = switchoverDto.getId();
        if (Objects.isNull(id)) {
            return "OpenGauss cluster ID cannot be empty！";
        }
        String targetMasterPod = switchoverDto.getTargetMasterPod();
        if (StringUtils.isBlank(targetMasterPod)) {
            return "The parameter for switching nodes in the OpenGauss cluster cannot be empty！";
        }

        // 获取openGauss信息
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            return "opengauss cluster(" + id + ")does not exist!";
        }

        // 获取k8sCluster
        String clusterId = openGaussCluster.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(clusterId);
        if (k8sCluster == null) {
            return "k8sCluster(" + clusterId + ")does not exist or invalid!";
        }
        ClusterCheckParams params = getClusterCheckParams(k8sCluster, null, openGaussCluster);
        String checkOgcState = checkOpengaussClusterState(params, true, true);
        if (!CommonConstant.RETURN_CODE_SUCCESS.equals(checkOgcState)) {
            return checkOgcState;
        }
        Optional<V1Pod> targerPod = podApi.getPodsByNamespacesAndPodName(k8sCluster, openGaussCluster.getNamespace(),
                targetMasterPod);
        if (targerPod.isEmpty()) {
            return "Instances in the OpenGauss cluster(" + targetMasterPod + ")does not exist!";
        }
        String targerPodPhase = targerPod.get().getStatus().getPhase();
        if (!"Running".equalsIgnoreCase(targerPodPhase)) {
            return "Instances in the OpenGauss cluster("
                    + targetMasterPod + ")state(" + targerPodPhase + ")Cannot switch!";
        }

        // 更新crd资源
        Map<String, Object> rootData = new HashMap<>();
        Map<String, Object> spec = new HashMap<>();

        // build switchover
        Map<String, Object> dbResources = new HashMap<>();
        dbResources.put("targetMasterPod", targetMasterPod);
        dbResources.put("status", CLUSTER_SWITCHOVER_STATUS);
        spec.put("switchover", dbResources);
        rootData.put("spec", spec);
        String openGaussName = openGaussCluster.getName();
        Boolean isUpdateResult = crdApi.updateCRDByNamespaced(k8sCluster, newCrdDefinition(), openGaussName, rootData);
        if (isUpdateResult != null && isUpdateResult) {
            return CommonConstant.RETURN_CODE_SUCCESS;
        } else {
            return "Switching failed!";
        }
    }

    /**
     * 切换操作的功能说明
     *
     * @param id      数据库id
     * @param podName podName
     * @return String
     */
    @Override
    public String releaseOgcPod(Long id, String podName) {
        // 获取openGauss信息
        OpenGaussCluster openGaussCluster = openGaussClusterMapper.selectById(id);
        if (openGaussCluster == null) {
            return "opengauss cluster(" + id + ")does not exist!";
        }

        // 获取k8sCluster
        String clusterId = openGaussCluster.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(clusterId);
        if (k8sCluster == null) {
            return "k8sCluster(" + clusterId + ")does not exist or invalid!";
        }

        // 查询对应集群是否有pod
        String labelSelector = ConstantEnum.OPENGAUSS_NAME_LABEL_KEY.getValue() + "=" + openGaussCluster.getName();
        List<V1Pod> podList = podApi.getPodsByNamespacesAndSelector(k8sCluster, openGaussCluster.getNamespace(),
                labelSelector, null);
        V1Pod targetPod = null;
        if (CollectionUtil.isNotEmpty(podList)) {
            for (V1Pod v1Pod : podList) {
                if (podName.equals(v1Pod.getMetadata().getName())) {
                    targetPod = v1Pod;
                }
            }
        }
        if (Objects.isNull(targetPod)) {
            throw new MarsRuntimeException("Instances in the OpenGauss cluster("
                    + podName + ")does not exist!");
        }
        Boolean isDeleteResult = podApi.deletePodByNamespacedAndName(k8sCluster, openGaussCluster.getNamespace(),
                podName);
        if (!Objects.isNull(isDeleteResult) && isDeleteResult) {
            return CommonConstant.RETURN_CODE_SUCCESS;
        }
        return "opengauss cluster(" + id + ")delete pod(" + podName + ")failed. The result is:" + isDeleteResult;
    }

    /**
     * updateOpengaussCluster
     *
     * @param id                  id
     * @param opengaussClusterDto opengaussClusterDto
     * @return String
     */
    @Override
    @Transactional
    public String updateOpengaussCluster(Long id, OpenGaussClusterDto opengaussClusterDto) {
        // 获取openGauss信息
        OpenGaussCluster opengaussClusterPo = openGaussClusterMapper.selectById(id);
        if (opengaussClusterPo == null) {
            return "The OpenGauss cluster does not exist in the data table！";
        }
        String k8sId = opengaussClusterPo.getK8sId();
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(k8sId);
        if (k8sCluster == null) {
            return "k8sCluster(" + k8sId + ")does not exist or invalid!";
        }
        ClusterCheckParams params = getClusterCheckParams(k8sCluster, null,
                opengaussClusterPo);
        String checkOgcState = checkOpengaussClusterState(params, true, false);
        if (!CommonConstant.RETURN_CODE_SUCCESS.equals(checkOgcState)) {
            return checkOgcState;
        }
        String result = updateOpengaussClusterPo(opengaussClusterPo, opengaussClusterDto);
        if (CommonConstant.RETURN_CODE_SUCCESS.equals(result)) {
            Boolean isUpdate = updateOpengaussClusterCrd(opengaussClusterDto);
            if (!isUpdate) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "Failed to update CRD";
            }
        }
        return result;
    }

    /**
     * 更新成功后会设置dto的clusterId，namespace，opengaussName
     *
     * @param opengaussClusterPo OpenGaussCluster
     * @param dto                dto
     * @return 提示信息
     */
    private String updateOpengaussClusterPo(OpenGaussCluster opengaussClusterPo,
                                            OpenGaussClusterDto dto) {
        String version = dto.getVersion();
        String image = dto.getImage();
        boolean shouldNeedUpdateCrd = false;
        shouldNeedUpdateCrd = shouldUpdateCrd(opengaussClusterPo, dto, version, image, shouldNeedUpdateCrd);

        // 限制cpu资源
        Map<String, Object> oriRequestResource = dto.getRequestResource();
        return getString(opengaussClusterPo, dto, oriRequestResource, shouldNeedUpdateCrd);
    }

    @NotNull
    private String getString(OpenGaussCluster opengaussClusterPo, OpenGaussClusterDto dto,
                             Map<String, Object> oriRequestResource, boolean shouldNeedUpdateCrd) {
        Boolean shouldTempNeedUpdateCrd = shouldNeedUpdateCrd;
        if (oriRequestResource != null && !oriRequestResource.isEmpty()) {
            if (!oriRequestResource.containsKey("cpu") || !oriRequestResource.containsKey("memory")) {
                return "[OpenGauss Cluster Update] The instance specification "
                        + "(requestResource) cannot be empty and must include CPU and memory!";
            }
            String requestCpu = String.valueOf(oriRequestResource.get("cpu"));
            String requestMem = String.valueOf(oriRequestResource.get("memory"));
            if (!VerifyUtil.validCpu(requestCpu)) {
                return "[OpenGauss Cluster Update] Instance Specification CPU Writing Method("
                        + requestCpu + ")incorrect!";
            }
            if (!VerifyUtil.validMemory(requestMem)) {
                return "[opengauss cluster update] instance specification memory writing("
                        + requestMem + ")incorrect!";
            }
            Map<String, Object> limitResource = dto.getLimitResource();
            requestCpu = VerifyUtil.convCpu(requestCpu);
            String cpuLimit = String.valueOf(limitResource.get("cpu"));
            Integer cpuRequestInt = Integer.valueOf(requestCpu);
            Integer cpuLimitInt = Integer.valueOf(cpuLimit);
            int cpuBurstMultipleUpdate = cpuLimitInt / cpuRequestInt;
            oriRequestResource.put("cpu", requestCpu);
            if (!requestCpu.equals(opengaussClusterPo.getCpuRequest())) {
                opengaussClusterPo.setCpuRequest(requestCpu);
                opengaussClusterPo.setCpuLimit(cpuBurstMultipleUpdate);
                shouldTempNeedUpdateCrd = true;
            }
            if (!requestMem.equals(opengaussClusterPo.getMemRequest())) {
                opengaussClusterPo.setMemRequest(requestMem);
                opengaussClusterPo.setCpuLimit(cpuBurstMultipleUpdate);
                shouldTempNeedUpdateCrd = true;
            }
            Integer cpuBurstMultiple = opengaussClusterPo.getCpuLimit();
            if (!shouldNeedUpdateCrd && !cpuBurstMultiple.equals(cpuBurstMultipleUpdate)) {
                opengaussClusterPo.setCpuRequest(requestCpu);
                opengaussClusterPo.setCpuLimit(cpuBurstMultipleUpdate);
                shouldTempNeedUpdateCrd = true;
            }
            dto.setRequestResource(oriRequestResource);
        }

        // 限制memory资源
        return getMemoryString(opengaussClusterPo, dto, shouldTempNeedUpdateCrd);
    }

    @NotNull
    private String getMemoryString(OpenGaussCluster opengaussClusterPo, OpenGaussClusterDto dto,
                                   boolean shouldNeedUpdateCrd) {
        Boolean shouldTempNeedUpdateCrd = shouldNeedUpdateCrd;
        Integer diskCapacity = dto.getDiskCapacity();
        if (diskCapacity != null) {
            if (diskCapacity < 500) {
                return "[OpenGauss Cluster Update]The minimum disk capacity is 500g!";
            } else {
                if (diskCapacity.intValue() != opengaussClusterPo.getDiskCapacity().intValue()) {
                    opengaussClusterPo.setDiskCapacity(diskCapacity);
                    shouldTempNeedUpdateCrd = true;
                }
            }
        }
        String remark = dto.getRemark();
        if (remark != null) {
            opengaussClusterPo.setRemark(remark);
        }
        int updated = openGaussClusterMapper.updateById(opengaussClusterPo);
        if (updated > 0) {
            dto.setK8sId(opengaussClusterPo.getK8sId());
            dto.setOpenGaussName(opengaussClusterPo.getName());
            return shouldTempNeedUpdateCrd ? CommonConstant.RETURN_CODE_SUCCESS : CommonConstant.RETURN_CODE_FAIL;
        } else {
            return "更新失败！";
        }
    }

    private static boolean shouldUpdateCrd(OpenGaussCluster opengaussClusterPo,
                                           OpenGaussClusterDto dto,
                                           String version, String image, boolean shouldNeedUpdateCrd) {
        Boolean shouldTempNeedUpdateCrd = shouldNeedUpdateCrd;
        if (StringUtils.isNotBlank(version) && !version.equals(opengaussClusterPo.getVersion())) {
            opengaussClusterPo.setVersion(version);
        }
        if (StringUtils.isNotBlank(image) && !image.equals(opengaussClusterPo.getImage())) {
            opengaussClusterPo.setImage(image);
            shouldTempNeedUpdateCrd = true;
        }
        if (Objects.isNull(dto.getIsTest())) {
            opengaussClusterPo.setIsTestCluster(dto.getIsTest());
        }
        return shouldTempNeedUpdateCrd;
    }

    /**
     * 可以变更的属性（镜像：spec.image.gaussDB, 磁盘容量：spec.dbStorage.diskCapacity, 实例规格及爆发规格：spec.dbResources）
     *
     * @param dto dto
     * @return boolean
     */
    private Boolean updateOpengaussClusterCrd(OpenGaussClusterDto dto) {
        String clusterId = dto.getK8sId();
        String opengaussName = dto.getOpenGaussName();
        String namespace = ConstantEnum.OPENGAUSS_NAMESPACE.getValue();

        isException(clusterId, opengaussName, namespace);
        K8sCluster k8sCluster = k8sClusterCacheManager.getCluster(dto.getK8sId());
        if (k8sCluster == null) {
            throw new MarsRuntimeException("k8sCluster(" + clusterId + ")不存在或无效!");
        }
        if (crdApi == null) {
            crdApi = new CrdApi();
        }
        Map<String, Object> spec = new HashMap<>();
        String image = dto.getImage();
        if (StringUtils.isNotBlank(image)) {
            Map<String, String> images = new HashMap<>();
            images.put("gaussDB",
                    returnImagePrefix(dto.getArchitecture()) + ConstantEnum.OPENGAUSS_IMAGE.getValue() + image);
            spec.put("images", images);
        }

        // 变更时不为空
        Map<String, Object> requestResource = dto.getRequestResource();
        setDbResource(dto, requestResource, spec);
        Integer diskCapacity = dto.getDiskCapacity();
        if (diskCapacity != null) {
            if (diskCapacity < 500) {
                throw new MarsRuntimeException("[OpenGauss Cluster Update]The minimum disk capacity is 500g!");
            } else {
                Map<String, Object> dbStorage = new HashMap<>();
                dbStorage.put("diskCapacity", diskCapacity + ConstantEnum.DISKUNIT.getValue());
                spec.put("dbStorage", dbStorage);
            }
        }
        Map<String, Object> rootData = new HashMap<>();
        rootData.put("spec", spec);
        if (CollectionUtil.isNotEmpty(rootData)) {
            return crdApi.updateCRDByNamespaced(k8sCluster, newCrdDefinition(), opengaussName, rootData);
        }
        return Boolean.TRUE;
    }

    private static void setDbResource(OpenGaussClusterDto dto, Map<String, Object> requestResource, Map<String,
            Object> spec) {
        if (requestResource != null && !requestResource.isEmpty()) {
            if (!requestResource.containsKey("cpu") || !requestResource.containsKey("memory")) {
                throw new MarsRuntimeException("[OpenGauss Cluster Update] The instance specification "
                        + "(requestResource) cannot be empty and must include CPU and memory!");
            }
            String requestCpu = String.valueOf(requestResource.get("cpu"));
            String requestMem = String.valueOf(requestResource.get("memory"));
            if (!VerifyUtil.validCpu(requestCpu)) {
                throw new MarsRuntimeException("[OpenGauss Cluster Update] The instance specification "
                        + "CPU writing (" + requestCpu + ") is incorrect!");
            }
            if (!VerifyUtil.validMemory(requestMem)) {
                throw new MarsRuntimeException("[OpenGauss Cluster Update] The instance specification " + "memory "
                        + "writing ( " + requestMem + ") is incorrect!");
            }

            // dbResources
            Map<String, Object> dbResources = new HashMap<>();
            dbResources.put("requests", requestResource);
            Map<String, Object> limitResource = dto.getLimitResource();
            dbResources.put("limits", limitResource);
            spec.put("dbResources", dbResources);
        }
    }

    private static void isException
            (String clusterId, String opengaussName, String namespace) {
        if (StringUtils.isBlank(clusterId)) {
            throw new MarsRuntimeException("k8s集群Id不能为空！");
        }
        if (StringUtils.isBlank(opengaussName)) {
            throw new MarsRuntimeException("opengauss名称不能为空！");
        }
        if (StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException("opengauss分区名称不能为空！");
        }
    }
}
