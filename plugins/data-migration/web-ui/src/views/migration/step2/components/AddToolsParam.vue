<template>
  <el-dialog
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    v-model="visible"
    :title="$t('components.ToolsParamsConfig.5q0toolspa19')"
    width="650px"
    @close="close"
  >
    <template #footer>
      <div class="flex1">
        <div>
          <el-button class="mr" @click="close">{{
              $t("components.ToolsParamsConfig.5q0toolspa13")
            }}
          </el-button>
          <el-button type="primary" @click="submit">{{
              $t("components.ToolsParamsConfig.5q0toolspa14")
            }}
          </el-button>
        </div>
      </div>
    </template>
    <el-form
      :model="data.formData"
      :rules="rules"
      ref="formRef"
      :label-width="'90px'"
      :disabled="data.disabled"
    >
      <el-form-item
        prop="paramKey"
        :label="$t('components.ToolsParamsConfig.5q0toolspa15')"
      >
        <el-input v-model.trim="data.formData.paramKey" maxlength="100"></el-input>
      </el-form-item>

      <el-form-item
        prop="paramValueType"
        :label="$t('components.ToolsParamsConfig.5q0toolspa17')"
      >
        <el-select v-model="data.formData.paramValueType"
                   :placeholder="$t('components.ToolsParamsConfig.5q0toolspa25')">
          <el-option :value="1" :label="$t('components.ToolsParamsConfig.5q0toolspa21')"></el-option>
          <el-option :value="2" :label="$t('components.ToolsParamsConfig.5q0toolspa23')"></el-option>
          <el-option :value="3" :label="$t('components.ToolsParamsConfig.5q0toolspa22')"></el-option>
          <el-option :value="4" :label="$t('components.ToolsParamsConfig.5q0toolspa24')"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item
        prop="paramValue"
        :label="$t('components.ToolsParamsConfig.5q0toolspa16')"
      >
        <el-input v-model.trim="data.formData.paramValue" maxlength="100"></el-input>
      </el-form-item>
      <el-form-item
        prop="desc"
        :label="$t('components.ToolsParamsConfig.5q0toolspa18')"
      >
        <el-input v-model.trim="data.formData.paramDesc" maxlength="100"></el-input>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import {ref, reactive, watch, onMounted} from "vue";
import {useI18n} from "vue-i18n";
import {saveToolsParams, hasParamKey} from "@/api/task";

const emits = defineEmits(["update:open"]);
const {t} = useI18n();
const visible = ref(false);
watch(visible, (v) => {
  emits("update:open", v);
});

const formRef = ref(null)
const submit = async () => {
  try {
    const res = await formRef.value?.validate()
    if (res) {
      return
    }
    visible.value = false;
    try {
      await saveToolsParams(data.formData)
      props.flushTools();
    } catch (e) {
      props.flushTools();
    }
  } catch (err) {
    console.log(err)
  }
};
const close = () => {
  visible.value = false;
  formRef.value?.resetFields()
};

const clearCacheParams = () => {
  data.formData.paramKey = "",
    data.formData.paramValue = "",
    data.formData.paramValueType = "",
    data.formData.paramDesc = ""
};

watch(
  () => props.open,
  (v) => {
    if (v) {
      data.formData.portalHostID = props.hostId;
      data.formData.configId = props.configId;
      clearCacheParams();
    }
    visible.value = v;
  }
);

const props = defineProps({
  open: Boolean,
  configId: Number,
  hostId: String,
  flushTools: {
    type: Function,
    default: null
  }
});

const data = reactive({
  show: false,
  title: t("components.ToolsParamsConfig.5q0toolspa19"),
  showButton: "show",
  loading: false,
  formData: {
    paramKey: "",
    paramValue: "",
    paramValueType: "",
    paramDesc: "",
    configId: "",
    portalHostID: "",
  },

  disabled: false,
});

const rules = {
  paramKey: [
    {
      required: true,
      trigger: "blur",
      message: t("step2.components.AddToolsParam.paramNameMsg"),
    },
    {
      validator: (rule, value) => {
        return new Promise(async (resolve, reject) => {
          const {paramKey, configId, portalHostID} = data.formData;
          try {
            const res = await hasParamKey(paramKey, configId, portalHostID);
            if (res.data) {
              reject(new Error(t("components.ToolsParamsConfig.5q0toolspa30")));
            } else {
              resolve();
            }
          } catch (error) {
            reject(new Error(t("step2.components.AddToolsParam.errorMsg")));
          }
        });
      },
      trigger: "change"
    }
  ],
  paramValue: [
    {
      required: true,
      trigger: "blur",
      message: t("step2.components.AddToolsParam.paramValueMsg"),
    },
    {
      validator: (rule, value, callback) => {
        switch (data.formData.paramValueType) {
          case 1:
            callback();
            break;
          case 2:
            const decimalReg = /^\d+(\.\d+)?$/;
            if (!decimalReg.test(value)) {
              callback(new Error(t('components.ToolsParamsConfig.5q0toolspa32')));
            } else {
              callback();
            }
            break;
          case 3:
            if (value !== 'true' && value !== 'false') {
              callback(new Error(t('components.ToolsParamsConfig.5q0toolspa33')));
            } else {
              callback();
            }
            break;
          case 4:
            const listReg = /^[^,]+(,[^,]+)*$/;
            if (!listReg.test(value)) {
              callback(new Error(t('components.ToolsParamsConfig.5q0toolspa34')));
            } else {
              callback();
            }
            break;
          default:
            callback(new Error(t('components.ToolsParamsConfig.5q0toolspa35')));
        }
      },
      trigger: "blur"
    }
  ],
  paramValueType: [
    {
      required: true,
      message: t("step2.components.AddToolsParam.paramTypeMsg"),
    },
  ]
}
const open = (configId) => {
  data.show = true;
  data.disabled = false;
};
</script>
<style>
.flex1 {
  display: flex;
  justify-content: center;
}
</style>
