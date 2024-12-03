<template>
  <a-modal
    v-model:visible="visible"
    :on-before-ok="confirm"
    @close="close"
    :width="600"
    okText="创建"
    unmount-on-close
  >
    <template #title> 创建集群 </template>
    <a-form ref="formRef" :model="formData" :rules="rules">
      <a-form-item label="所属域" field="k8sId">
        <a-select
          v-model="formData.k8sId"
          @change="k8sIdChange"
          placeholder="请选择所属域"
          popup-container=".select-comp-container"
        >
          <a-option v-for="domain of props.domainList" :value="domain.id">{{
            domain.name
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-show="formData.k8sId"
        label="资源池类型"
        field="resourceType"
      >
        <a-select
          v-model="formData.resourceType"
          placeholder="请选择资源池类型"
          popup-container=".select-comp-container"
        >
          <a-option v-for="rt of resourceTypeList" :value="rt">{{
            rt
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="集群名称" field="openGaussName">
        <a-input
          v-model="formData.openGaussName"
          placeholder="请输入集群名称"
          :max-length="32"
        />
      </a-form-item>
      <a-form-item label="集群架构" field="archType">
        <a-select
          v-model="formData.archType"
          placeholder="请选择集群架构"
          popup-container=".select-comp-container"
        >
          <a-option value="merge">集成</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="集群版本" field="version">
        <a-select
          v-model="formData.version"
          placeholder="请选择集群版本"
          popup-container=".select-comp-container"
        >
          <a-option>5.0.0</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="镜像" field="image">
        <a-select
          v-model="formData.image"
          placeholder="请选择镜像"
          popup-container=".select-comp-container"
        >
          <a-option v-for="image of imageList" :value="image.image">{{
            image.image
          }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="端口号" field="port">
        <a-input-number
          v-model="formData.port"
          :min="20001"
          :max="65529"
          :precision="0"
          placeholder="请输入端口号"
        />
      </a-form-item>
      <a-form-item label="字符集" field="charset">
        <a-select
          v-model="formData.charset"
          placeholder="请选择字符集"
          popup-container=".select-comp-container"
        >
          <a-option>UTF-8</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="集群规模" field="instance">
        <a-select
          v-model="formData.instance"
          placeholder="请选择集群规模"
          popup-container=".select-comp-container"
        >
          <a-option :value="3">三节点</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="实例规格" field="size" required>
        <radio-item
          v-model="formData.size"
          value="normal"
          style="margin-right: 8px; width: 100px"
        >
          <div style="margin-bottom: 20px">一般性能</div>
          <div>CPU:4Core</div>
          <div>内存:16GB</div>
        </radio-item>
        <radio-item
          v-model="formData.size"
          value="high"
          style="margin-right: 8px; width: 100px"
        >
          <div style="margin-bottom: 20px">高强性能</div>
          <div>CPU:8Core</div>
          <div>内存:32GB</div>
        </radio-item>
        <radio-item
          v-model="formData.size"
          value="super"
          style="margin-right: 8px; width: 100px"
        >
          <div style="margin-bottom: 20px">超强性能</div>
          <div>CPU:16Core</div>
          <div>内存:64GB</div>
        </radio-item>
        <radio-item v-model="formData.size" value="custom" style="width: 120px">
          <div style="margin-bottom: 5px">自定义</div>
          <a-input-number
            v-model="formData.customSize.cpu"
            :min="1"
            :precision="0"
            size="mini"
            style="margin-bottom: 4px"
          >
            <template #suffix> Core </template>
          </a-input-number>
          <a-input-number
            v-model="formData.customSize.memory"
            :min="1"
            :precision="0"
            size="mini"
          >
            <template #suffix> GB </template>
          </a-input-number>
        </radio-item>
      </a-form-item>
      <a-form-item label="爆发模式" field="outburst">
        <a-switch v-model="formData.outburst" size="small" />
      </a-form-item>
      <a-form-item label="爆发倍率" field="rate" v-if="formData.outburst">
        <radio-item
          v-model="formData.rate"
          :value="2"
          style="margin-right: 8px; width: 100px"
        >
          2X
          <template #bottom>
            <div class="size-bottom">CPU:{{ requestResourceCpu * 2 }}C</div>
          </template>
        </radio-item>
        <radio-item
          v-model="formData.rate"
          :value="3"
          style="margin-right: 8px; width: 100px"
        >
          3X
          <template #bottom>
            <div class="size-bottom">CPU:{{ requestResourceCpu * 3 }}C</div>
          </template>
        </radio-item>
        <radio-item
          v-model="formData.rate"
          :value="4"
          style="margin-right: 8px; width: 100px"
        >
          4X
          <template #bottom>
            <div class="size-bottom">CPU:{{ requestResourceCpu * 4 }}C</div>
          </template>
        </radio-item>
        <radio-item
          v-model="formData.rate"
          value="custom"
          style="width: 100px; height: 68.85px"
        >
          自定义
          <template #bottom>
            <div
              class="size-bottom"
              v-show="formData.rate === 'custom'"
              style="width: 100%"
            >
              <a-input-number
                v-model="formData.customRate"
                :min="2"
                :precision="0"
                size="mini"
              >
                <template #suffix> X </template>
              </a-input-number>
            </div>
          </template>
        </radio-item>
      </a-form-item>
      <a-form-item label="存储容量" field="diskCapacity">
        <a-input-number
          v-model="formData.diskCapacity"
          :min="500"
          :precision="0"
          placeholder="请输入存储容量"
        >
          <template #suffix> G </template>
        </a-input-number>
      </a-form-item>
      <a-form-item label="测试集群" field="isTest">
        <a-switch v-model="formData.isTest" size="small" />
      </a-form-item>
      <a-form-item label="备注" field="remark">
        <a-textarea
          v-model="formData.remark"
          :max-length="100"
          placeholder="请输入备注"
        />
      </a-form-item>
    </a-form>
    <div class="cm-modal-title">数据库信息</div>
    <a-form ref="DBFormRef" :model="DBFormData" :rules="rulesDB">
      <a-form-item label="数据库名称" field="database">
        <a-input
          v-model="DBFormData.database"
          @input="databaseInput"
          :max-length="31"
          placeholder="请输入数据库名称"
        />
        <template #extra>
          <div>不能创建postgres系统库</div>
        </template>
      </a-form-item>
      <a-form-item label="访问账号" field="user">
        <a-input
          v-model="DBFormData.user"
          @change="userInput"
          placeholder="请输入访问账号"
        />
        <template #extra>
          <div>用户名不能是root,operator等已有用户</div>
        </template>
      </a-form-item>
      <a-form-item label="密码" field="pwd">
        <a-input-password v-model="DBFormData.pwd" placeholder="请输入密码" />
      </a-form-item>
    </a-form>
    <div style="text-align: right">
      <a-link style="justify-content: flex-end" @click="addDB">添加</a-link>
    </div>
    <div v-for="(db, index) in formData.dbList" class="db-info-content">
      <a-descriptions :column="1">
        <a-descriptions-item label="数据库名称" :span="1">
          {{ db.database }}
        </a-descriptions-item>
        <a-descriptions-item label="访问账号" :span="1">
          {{ db.user }}
        </a-descriptions-item>
        <a-descriptions-item label="密码" :span="1">
          {{ db.pwd }}
        </a-descriptions-item>
      </a-descriptions>
      <icon-close-circle-fill @click="removeDB(index)" class="arco-icon" />
    </div>
  </a-modal>
</template>
<script setup>
import { reactive, ref, defineProps, watch, computed } from "vue";
import { imageListData, newCluster } from "@/api/clusterlist.js";
import { Message } from "@arco-design/web-vue";

const props = defineProps(["domainList", "systemList"]);
const emits = defineEmits(["refresh"]);

const visible = ref(false);
const formRef = ref(null);
const formData = reactive({
  k8sId: "",
  resourceType: "",
  // cmdbSystemId: '',
  openGaussName: "",
  archType: "merge",
  version: "5.0.0",
  image: "",
  port: 25400,
  charset: "UTF-8",
  size: "normal",
  instance: 3,
  customSize: {
    cpu: 2,
    memory: 4,
  },
  outburst: false,
  rate: 2,
  customRate: 2,
  diskCapacity: 500,
  isTest: false,
  remark: "",
  dbList: [],
});
const rules = {
  k8sId: { required: true, message: "请选择所属域" },
  resourceType: { required: true, message: "请选择资源池类型" },
  openGaussName: [
    { required: true, message: "请输入集群名称" },
    {
      validator: (value, cb) => {
        let msg = "";
        if (!/^[a-z][a-z0-9\-]*$/.test(value)) {
          msg = "请输入小写字母开头，包含小写字母数字-的集群名称";
        }
        cb(msg);
      },
    },
  ],
  archType: { required: true, message: "请选择集群架构" },
  version: { required: true, message: "请选择集群版本" },
  image: { required: true, message: "请选择镜像" },
  port: { required: true, message: "请输入端口号" },
  charset: { required: true, message: "请选择字符集" },
  instance: { required: true, message: "请选择集群规模" },
  size: {
    validator: (value, cb) => {
      let msg = "";
      const emptyList = [undefined, null, "", "0", 0];
      if (
        value === "custom" &&
        (emptyList.includes(formData.customSize.cpu) ||
          emptyList.includes(formData.customSize.memory))
      ) {
        msg = "请输入大于0的值";
      }
      cb(msg);
    },
  },
  rate: {
    validator: (value, cb) => {
      let msg = "";
      const emptyList = [undefined, null, ""];
      if (value === "custom" && emptyList.includes(formData.customRate)) {
        msg = "请输入大于1的值";
      }
      cb(msg);
    },
  },
  diskCapacity: { required: true, message: "请输入存储容量" },
};
const requestResourceCpu = computed(() => {
  let res = 0;
  switch (formData.size) {
    case "normal":
      res = 4;
      break;
    case "high":
      res = 8;
      break;
    case "super":
      res = 16;
      break;
    default:
      res = formData.customSize.cpu || 0;
  }
  return res;
});
const DBFormRef = ref(null);
const DBFormData = reactive({
  database: "",
  user: "",
  pwd: "",
});

const rulesDB = {
  database: [
    {
      required: true,
      message: "请输入数据库名称",
    },
    {
      validator: (value, cb) => {
        let msg = "";
        if (!/^[a-z][a-z0-9_]*$/.test(value)) {
          msg =
            "请输入数据库名称（以小写字母开头，仅包含小写字母数字下划线，最长32个字符）";
        } else if (formData.dbList.find((db) => db.database === value)) {
          msg = "数据库名称已存在，不能新增";
        }
        cb(msg);
      },
    },
  ],
  user: [
    {
      required: true,
      message: "请输入访问账号",
    },
    {
      validator: (value, cb) => {
        let msg = "";
        const excludeList = [
          "root",
          "zabbixjk",
          "inception",
          "bkpuser",
          "devops_db",
          "operator",
        ];
        if (excludeList.includes(value)) {
          msg = "不能创建已存在账户";
        } else if (value === DBFormData.database) {
          msg = "访问账号不能与数据库名称相同";
        }
        cb(msg);
      },
    },
  ],
  pwd: [
    {
      required: true,
      message: "请输入密码",
    },
    {
      validator: (value, cb) => {
        let msg = "";
        if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*\W)[\w\W]{15,16}$/.test(value)) {
          msg = "请输入密码（包含字母数字和特殊符号，长度在15到16）";
        }
        cb(msg);
      },
    },
  ],
};
const resourceTypeList = ref([]);
const imageList = ref([]);
// 监听获取镜像列表
watch([() => formData.resourceType, () => formData.version], () => {
  if (formData.resourceType && formData.version) {
    const rtInfo = formData.resourceType.split("-");
    const params = {
      version: formData.version,
      os: rtInfo[1],
      architecture: rtInfo[0],
    };
    if (params.architecture === "openeuler") params.architecture = "openEuler";
    formData.image = "";
    imageListData(params).then((res) => {
      imageList.value = res.data.data || [];
      if (res.data.data?.length) formData.image = res.data.data[0].image;
    });
  }
});

// 切换所属域名
const k8sIdChange = () => {
  if (formData.k8sId) {
    const domainData = props.domainList.find(
      (domain) => domain.id === formData.k8sId
    );
    if (typeof domainData?.resourcePool == "string") {
      resourceTypeList.value = domainData.resourcePool.split(",");
    }
    if (resourceTypeList.value.length) {
      formData.resourceType = resourceTypeList.value[0];
    }
  }
};
// 添加数据库
const addDB = () => {
  DBFormRef.value.validate((props) => {
    if (props === undefined) {
      formData.dbList.push({
        ...DBFormData,
      });
      DBFormRef.value.resetFields();
      syncable = true;
    }
  });
};
const removeDB = (index) => {
  formData.dbList.splice(index, 1);
};
let syncable = true; // 是否同步输入，未进行账户输入时开启同步
const databaseInput = (dbText) => {
  if (syncable) DBFormData.user = dbText ? "og_" + dbText : "";
};
const userInput = () => {
  syncable = false;
};

const confirm = async () => {
  const props = await formRef.value.validate();
  if (props !== undefined) return false;

  const rtdata = formData.resourceType.split("-");
  const body = {
    k8sId: formData.k8sId,
    resourceType: formData.resourceType,
    openGaussName: formData.openGaussName,
    archType: formData.archType,
    architecture: rtdata[0],
    os: rtdata[1],
    version: formData.version,
    image: formData.image,
    port: formData.port,
    charset: formData.charset,
    instance: formData.instance,
    diskCapacity: formData.diskCapacity,
    isTest: formData.isTest,
    remark: formData.remark,
    dbList: formData.dbList,
  };
  if (body.os === "openeuler") body.os = "openEuler";
  if (formData.size === "custom") {
    body.requestResource = {
      cpu: formData.customSize.cpu,
      memory: formData.customSize.memory + "Gi",
    };
  } else {
    switch (formData.size) {
      case "normal":
        body.requestResource = { cpu: 4, memory: "16Gi" };
        break;
      case "high":
        body.requestResource = { cpu: 8, memory: "32Gi" };
        break;
      case "super":
        body.requestResource = { cpu: 16, memory: "64Gi" };
        break;
    }
  }
  if (formData.outburst) {
    body.limitResource =
      formData.rate === "custom"
        ? {
            memory: body.requestResource.memory,
            cpu: formData.customRate * requestResourceCpu.value,
          }
        : {
            memory: body.requestResource.memory,
            cpu: formData.rate * requestResourceCpu.value,
          };
  } else {
    body.limitResource = body.requestResource;
  }
  const res = await newCluster(body);
  if (res.code == 200) {
    emits("refresh");
    Message.success("集群创建完成");
  }
  return res.code == 200;
};
const open = () => {
  visible.value = true;
};
const close = () => {
  visible.value = false;
  syncable = true;
  formRef.value.resetFields();
  DBFormRef.value.resetFields();
  formData.dbList = [];
  formData.customRate = 2;
  formData.customSize = { cpu: 2, memory: 4 };
};
defineExpose({ open });
</script>
<style lang="less" scoped>
.size-bottom {
  color: var(--color-text-2);
  font-size: 12px;
  text-align: center;
  height: 28px;
  line-height: 28px;
  margin-top: 4px;
}

.db-info-content {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .arco-icon {
    cursor: pointer;
    color: #f53f3f;
  }
}
</style>
