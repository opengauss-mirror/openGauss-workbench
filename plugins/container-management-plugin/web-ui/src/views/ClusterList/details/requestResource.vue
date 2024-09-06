<template>
  <a-modal v-model:visible="visible" :on-before-ok="confirm" @close="close" :width="600" okText="修改">
    <template #title>
      修改规格
    </template>
    <a-form ref="formRef" :model="formData" :rules="rules">
      <a-form-item label="实例规格" field="size" required>
        <radio-item v-model="formData.size" value="normal" style="margin-right: 8px;width: 100px;">
          <div style="margin-bottom: 20px;">一般性能</div>
          <div>CPU:4Core</div>
          <div>内存:16GB</div>
        </radio-item>
        <radio-item v-model="formData.size" value="high" style="margin-right: 8px;width: 100px;">
          <div style="margin-bottom: 20px;">高强性能</div>
          <div>CPU:8Core</div>
          <div>内存:32GB</div>
        </radio-item>
        <radio-item v-model="formData.size" value="super" style="margin-right: 8px;width: 100px;">
          <div style="margin-bottom: 20px;">超强性能</div>
          <div>CPU:16Core</div>
          <div>内存:64GB</div>
        </radio-item>
        <radio-item v-model="formData.size" value="custom" style="width: 120px;">
          <div style="margin-bottom: 5px;">自定义</div>
          <a-input-number v-model="formData.customSize.cpu" :min="1" :precision="0" size="mini"
            style="margin-bottom: 4px;">
            <template #suffix>
              Core
            </template>
          </a-input-number>
          <a-input-number v-model="formData.customSize.memory" :min="1" :precision="0" size="mini">
            <template #suffix>
              GB
            </template>
          </a-input-number>
        </radio-item>
      </a-form-item>
      <a-form-item label="爆发模式" field="outburst">
        <a-switch v-model="formData.outburst" size="small" />
      </a-form-item>
      <a-form-item label="爆发倍率" field="rate" v-if="formData.outburst">
        <radio-item v-model="formData.rate" :value="2" style="margin-right: 8px;width: 100px;">
          2X
          <template #bottom>
            <div class="size-bottom">
              CPU:{{ requestResourceCpu * 2 }}C
            </div>
          </template>
        </radio-item>
        <radio-item v-model="formData.rate" :value="3" style="margin-right: 8px;width: 100px;">
          3X
          <template #bottom>
            <div class="size-bottom">
              CPU:{{ requestResourceCpu * 3 }}C
            </div>
          </template>
        </radio-item>
        <radio-item v-model="formData.rate" :value="4" style="margin-right: 8px;width: 100px;">
          4X
          <template #bottom>
            <div class="size-bottom">
              CPU:{{ requestResourceCpu * 4 }}C
            </div>
          </template>
        </radio-item>
        <radio-item v-model="formData.rate" value="custom" style="width: 100px;height: 68.85px;">
          自定义
          <template #bottom>
            <div class="size-bottom" v-show="formData.rate === 'custom'" style="width:100%;">
              <a-input-number v-model="formData.customRate" :min="2" :precision="0" size="mini">
                <template #suffix>
                  X
                </template>
              </a-input-number>
            </div>
          </template>
        </radio-item>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
import { computed, ref, reactive } from 'vue';
import { editCluster } from '@/api/clusterlist';
import { useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
const emits = defineEmits(['refresh']);

const route = useRoute();
const visible = ref(false);
const formRef = ref(null);
const formData = reactive({
  size: 'normal',
  customSize: {
    cpu: 2,
    memory: 4
  },
  outburst: false,
  rate: 2,
  customRate: 2,
});
const rules = {
  size: {
    validator: (value, cb) => {
      let msg = '';
      const emptyList = [undefined, null, '', '0', 0];
      if (
        value === 'custom' &&
        (emptyList.includes(formData.customSize.cpu) || emptyList.includes(formData.customSize.memory))
      ) {
        msg = '请输入大于0的值';
      }
      cb(msg);
    }
  },
  rate: {
    validator: (value, cb) => {
      let msg = '';
      const emptyList = [undefined, null, ''];
      if (value === 'custom' && emptyList.includes(formData.customRate)) {
        msg = '请输入大于1的值';
      }
      cb(msg);
    }
  }
}
const requestResourceCpu = computed(() => {
  let res = 0;
  switch (formData.size) {
    case 'normal':
      res = 4;
      break;
    case 'high':
      res = 8;
      break;
    case 'super':
      res = 16;
      break;
    default:
      res = formData.customSize.cpu || 0;
  }
  return res;
})
function open(detailsData) {
  visible.value = true;
  const { requestResource, scaleTimes } = detailsData;

  if (requestResource) {
    if (requestResource.cpu == 4 && requestResource.memory == '16Gi')
      formData.size = 'normal';
    else if (requestResource.cpu == 8 && requestResource.memory == '32Gi')
      formData.size = 'high';
    else if (requestResource.cpu == 16 && requestResource.memory == '64Gi')
      formData.size = 'super';
    else {
      formData.size = 'custom';
      formData.customSize = {
        cpu: +requestResource.cpu,
        memory: +requestResource.memory?.replace(/[a-zA-Z]+/, '')
      }
    }
  }

  if (+scaleTimes !== 1) {
    formData.outburst = true;
    if ([2, 3, 4].includes(+scaleTimes)) {
      formData.rate = +scaleTimes;
    } else {
      formData.rate = 'custom';
      formData.customRate = +scaleTimes;
    }
  }
}
async function confirm() {
  const props = await formRef.value.validate();
  if (props !== undefined) return false;
  const body = { id: route.query.id };
  if (formData.size === 'custom') {
    body.requestResource = { cpu: formData.customSize.cpu, memory: formData.customSize.memory + 'Gi' };
  } else {
    switch (formData.size) {
      case 'normal':
        body.requestResource = { cpu: 4, memory: '16Gi' };
        break;
      case 'high':
        body.requestResource = { cpu: 8, memory: '32Gi' };
        break;
      case 'super':
        body.requestResource = { cpu: 16, memory: '64Gi' };
        break;
    }
  }
  if (formData.outburst) {
    body.limitResource = formData.rate === 'custom'
      ? { memory: body.requestResource.memory, cpu: formData.customRate * requestResourceCpu.value }
      : { memory: body.requestResource.memory, cpu: formData.rate * requestResourceCpu.value }
  } else {
    body.limitResource = body.requestResource;
  }
  const res = await editCluster(body);
  if (res.code == 200) {
    emits('refresh');
    Message.success('规格修改完成')
  }
  return res.code == 200
}
function close() {
  formRef.value.resetFields();
  formData.customRate = 2;
  formData.customSize = { cpu: 2, memory: 4 };
}
defineExpose({ open })
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
</style>