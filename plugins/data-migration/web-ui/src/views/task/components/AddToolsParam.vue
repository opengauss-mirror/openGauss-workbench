<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="visible"
    :title="$t('components.ToolsParamsConfig.5q0toolspa19')"
    :modal-style="{ width: '650px' }"
    @cancel="close"
  >
    <template #footer>
      <div class="flex1">
        <div>
          <a-button class="mr" @click="close">{{
            $t("components.ToolsParamsConfig.5q0toolspa13")
          }}</a-button>
          <a-button type="primary" @click="submit">{{
            $t("components.ToolsParamsConfig.5q0toolspa14")
          }}</a-button>
        </div>
      </div>
    </template>
    <a-form
      :model="data.formData"
      :rules="rules"
      ref="formRef"
      :label-col="{ style: { width: '90px' } }"
      :disabled="data.disabled"
    >
      <a-form-item
        field="paramKey"
        :label="$t('components.ToolsParamsConfig.5q0toolspa15')"
      >
        <a-input v-model.trim="data.formData.paramKey"></a-input>
      </a-form-item>

      <a-form-item
        :label="$t('components.ToolsParamsConfig.5q0toolspa17')"
        field="paramValueType"
      >
        <a-select v-model="data.formData.paramValueType" :placeholder="$t('components.ToolsParamsConfig.5q0toolspa25')">
          <a-option value=1>{{$t('components.ToolsParamsConfig.5q0toolspa21')}}</a-option>
          <a-option value=2>{{$t('components.ToolsParamsConfig.5q0toolspa23')}}</a-option>
          <a-option value=3>{{$t('components.ToolsParamsConfig.5q0toolspa22')}}</a-option>
          <a-option value=4>{{$t('components.ToolsParamsConfig.5q0toolspa24')}}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        :label="$t('components.ToolsParamsConfig.5q0toolspa16')"
        field="paramValue"
      >
        <a-input v-model.trim="data.formData.paramValue"></a-input>
      </a-form-item>
      <a-form-item
        :label="$t('components.ToolsParamsConfig.5q0toolspa18')"
        field="desc"
      >
        <a-input v-model.trim="data.formData.paramDesc"></a-input>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
  
<script setup >
import { ref, reactive, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import { saveToolsParams,hasParamKey } from "@/api/task";
const emits = defineEmits(["update:open"]);
const { t } = useI18n();
const visible = ref(false);
watch(visible, (v) => {
  emits("update:open", v);
});

const formRef = ref(null)
const submit = async () => {
  try{
    const res =  await formRef.value?.validate()
    if(res){
     return
    }
    visible.value = false;
    try {
      await saveToolsParams(data.formData)
      props.flushTools();
    }catch(e){
      props.flushTools();
    }
  }catch(err){
    console.log(err)
  }
};
const close = () => {
  visible.value = false;
  formRef.value?.resetFields()
};

const clearCacheParams = () => {
  data.formData.paramKey="",
  data.formData.paramValue="",
  data.formData.paramValueType="",
  data.formData.paramDesc=""
};

watch(
  () => props.open,
  (v) => {
    if (v) {
      data.formData.portalHostID=props.hostId;
      data.formData.configId=props.configId;
      clearCacheParams();
    }
    visible.value = v;
  }
);

const props = defineProps({
  open: Boolean,
  configId: Number,
  hostId: String,
  flushTools:{
    type:Function,
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
  paramKey:[
    {
      required: true,
      trigger:"change",
      message:"请输入参数名称",
    },
    {
      validator:async (value,cb)=>{
        const {configId,portalHostID}= data.formData
        const  res = await hasParamKey(value,configId,portalHostID)
        if(res.data){
          cb(t("components.ToolsParamsConfig.5q0toolspa30"))
        }else{
          cb()
        }
      }
    },
  ],
  paramValue:[
    {required: true,
      'validate-trigger':'blur',
      message:"请输入参数值",
    },
    {
      validator:(value,cb)=>{
        switch (data.formData.paramValueType) {
            case '1':
              cb()
              break;
            case '2':
            const decimalReg=/^\d+(\.\d+)?$/;
              if(!decimalReg.test(value)){
                cb(t('components.ToolsParamsConfig.5q0toolspa32'))
              }
              break;
            case '3':
              if(value!=='true'&&value!=='false'){
                cb(t('components.ToolsParamsConfig.5q0toolspa33'))
              }
              break;
            case '4':            
              const listReg= /^(.*?,.*?(,|$))/;
              if(!listReg.test(value)){
                cb(t('components.ToolsParamsConfig.5q0toolspa34'))
               }
              break;
            default:
              cb(t('components.ToolsParamsConfig.5q0toolspa35'))
              break;
            }
        }
    },
  ],
  paramValueType:[
    {
      required: true,
      message:"请输入参数类型",
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
  