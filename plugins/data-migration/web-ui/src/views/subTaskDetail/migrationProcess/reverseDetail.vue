<template>
  <div>
    <el-dialog
      v-model="reverseVisible"
      :title="$t('detail.index.5qofnua1tf40')"
      width="800"
      modal-class="detailMaster"
      class="detailDialog"
      :append-to-body="true"
      :before-close="closeDialog"
    >
      <div class="reverseBody">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="cur-config">
              <div class="config-title">
                {{ $t('detail.index.5qofnua40ig0') }}
              </div>
              <el-alert class="config-item" :type="reverseConfig.replacationPermise ? 'success' : 'error'" :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofnua4a4g0') }}
                </template>
                {{
                  reverseConfig.replacationPermise
                    ? $t('detail.index.5qofnua4abs0')
                    : $t('detail.index.5qofnua4ag40')
                }}
              </el-alert>
              <el-alert class="config-item" :type="reverseConfig.sslValue === 'NULL' ||
                      reverseConfig.walLevelValue === 'NULL'
                      ? 'error'
                      : 'success'
                    " :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofom5ifnk0') }}
                </template>
                <p>
                  {{
                    reverseConfig.sslValue === 'NULL'
                      ? $t('detail.index.5qofqisf3jc0')
                      : `ssl=${reverseConfig.sslValue};`
                  }}
                </p>
                <p>
                  {{
                    reverseConfig.walLevelValue === 'NULL'
                      ? $t('detail.index.5qofqisf68w0')
                      : `wal_level=${reverseConfig.walLevelValue};`
                  }}
                </p>
              </el-alert>
              <el-alert class="config-item" :type="reverseConfig.rolcanlogin === 'false' ||
                      reverseConfig.rolreplication === 'false'
                      ? 'error'
                      : 'success'
                    "  :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofpdnmtkk0') }}
                </template>
                <el-descriptions :data="replicationData" size="medium" layout="vertical" bordered />
                <el-descriptions
                  title=""
                  direction="vertical"
                  :column="2"
                  border>
                  <el-descriptions-item v-for="(item, index) in replicationData" :label="item.label">
                    {{ item.value }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-alert>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="correct-config">
              <div class="config-title">
                {{ $t('detail.index.5qofnua4akg0') }}
              </div>
              <el-alert class="config-item" type="success" :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofnua4a4g0') }}
                </template>
                host replication {{ reverseConfig.dbUser }} 0.0.0.0/0 sha256
                host replication {{ reverseConfig.dbUser }} ::/0 sha256
              </el-alert>
              <el-alert class="config-item" type="success" :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofom5ifnk0') }}
                </template>
                <p>ssl=on;</p>
                <p>wal_level=logical;</p>
              </el-alert>
              <el-alert class="config-item" type="success" :show-icon="true" :closable="false">
                <template #title>
                  {{ $t('detail.index.5qofpdnmtkk0') }}
                </template>
                <el-descriptions
                  title=""
                  direction="vertical"
                  :column="2"
                  border>
                  <el-descriptions-item label="rolcanlogin">'t'</el-descriptions-item>
                  <el-descriptions-item label="rolreplication">'t'</el-descriptions-item>
                </el-descriptions>
              </el-alert>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import {ref} from "vue";
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const props = defineProps({
  reverseConfig: {
    type: Object,
  },
  replicationData: {
    type: Array,
  }
})
const emits = defineEmits(['closeDialog'])
const reverseVisible = ref(true)

const closeDialog = () => {
//   Triggers the parent component to shut down
  emits('closeDialog')
}
</script>
<style lang="less" scoped>
    .reverseBody {
      .cur-config, .correct-config {
        display: flex;
        gap: 10px;
        flex-direction: column;
        .config-item {
          min-height: 76px;
        }
      }
      max-height: 450px !important;
      margin-bottom: 20px;
    }
</style>
