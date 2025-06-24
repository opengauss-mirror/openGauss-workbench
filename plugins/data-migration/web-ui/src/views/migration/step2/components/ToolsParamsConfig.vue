<template>
  <div>
    <el-drawer v-model="visible" size="50%" :with-header="false" destroy-on-close>
      <template  #header>
        <div class="title-con">
          <span class="params-title"> {{$t("components.ToolsParamsConfig.5q0toolspar0")}} </span>
          <span class="tips-item">
            {{ $t('components.ToolsParamsConfig.5q0toolspa26') }}<b>{{ form.changeNumber }}</b>
            {{ $t('components.ToolsParamsConfig.5q0toolspa27') }}
          </span>
        </div>
      </template>
      <div class="config-con">
        <div class="basic-config-con">
          <el-collapse  v-model="activeCollapse" accordion :border="false" class="custom-collapse">
            <el-collapse-item name="1" :style="customStyle">
              <template #title>
                <div style="display: flex; align-items: center" >
                  {{ $t("components.ToolsParamsConfig.5q0toolspar9") }}
                  <el-button size="mini" type="text" @click="handleToolsParams(9)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form.portalData" style="overflow-x: auto;" :hide-required-asterisk="true">
                <el-table :data="form.portalData" border :row-class-name="basicRowClass" class="tableClass">
                  <el-table-column
                    :label="$t('components.ParamsConfig.5q0aazsppog0')"
                    prop="paramKey"
                    width="200"
                    show-overflow-tooltip
                  />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`portalData.${$index}.paramChangeValue`">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)" />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')"
                    prop="paramValue" width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')"
                    prop="paramDesc" width="300" show-overflow-tooltip/>
                  <el-table-column
                    :label="$t('components.ToolsParamsConfig.5q0toolspa20')"
                    width="100" show-overflow-tooltip
                  >
                    <template #default="{ row }">
                      <el-popconfirm
                        :title="$t('components.ToolsParamsConfig.5q0toolspa28')"
                        :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                        :cancel-button-text="$t('step3.index.5q093f8y9zg5')"
                        @confirm="deleteToolsParams(row)"
                      >
                        <template #reference>
                          <el-button size="small" type="text">
                            <el-icon><Delete /></el-icon>
                          </el-button>
                        </template>
                      </el-popconfirm>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse :v-model="activeCollapse" accordion :bordered="false" class="custom-collapse">
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div style="display: flex; align-items: center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar1") }}
                  <el-button size="small" type="text" @click="handleToolsParams(1)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef" class="tableClass" :hide-required-asterisk="true">
                <el-table :data="form.chameleonData" border :row-class="basicRowClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')"
                    prop="paramKey" width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')"
                    width="300" show-overflow-tooltip>
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`chameleonData.${$index}.paramChangeValue`">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')"
                    prop="paramValue" width="300" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')"
                    prop="paramDesc" width="300" show-overflow-tooltip
                  />
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')"
                    width="100"
                  >
                    <template #default="{ row }">
                      <el-form-item>
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')"
                          :confirm-button-text="$t('step3.index.5q093f8y9zg4')" :cancel-button-text="$t('step3.index.5q093f8y9zg5')"
                          @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="text">
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div style="display: flex; align-items: center" >
                  {{ $t("components.ToolsParamsConfig.5q0toolspar2") }}
                  <el-button size="small" type="text" @click="handleToolsParams(2)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef">
                <el-table :data="form.dataCheckApplicationData" :row-class-name="basicRowClass"
                  border class="tableClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')"
                    prop="paramKey" width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`dataCheckApplicationData.${$index}.paramChangeValue`" class="hide-asterisk">
                      <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="200" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100">
                    <template #default="{ row }">
                      <el-form-item class="hide-asterisk">
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                          :cancel-button-text="$t('step3.index.5q093f8y9zg5')" @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="text">
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div style="display: flex; align-items: center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar3") }}
                  <el-button size="small" type="text" @click.stop="handleToolsParams(3)">
                  <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef">
                <el-table :data="form.dataCheckApplicationSinkData" border
                          :row-class-name="basicRowClass" class="tableClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`dataCheckApplicationSinkData.${$index}.paramChangeValue`" class="hide-asterisk">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100">
                    <template #default="{ row }">
                      <el-form-item class="hide-asterisk">
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                          :cancel-button-text="$t('step3.index.5q093f8y9zg5')" @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="text">
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div class="collapse-header">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar4") }}
                  <el-button size="small" type="text" @click="handleToolsParams(4)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef">
                <el-table :data="form.dataCheckApplicationSourceData" border
                  :row-class-name="basicRowClass" class="tableClass" style="width: 100%">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`dataCheckApplicationSourceData.${$index}.paramChangeValue`" class="hide-asterisk">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)" clearable/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100" align="center">
                    <template #default="{ row }">
                      <el-form-item class="hide-asterisk">
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                          :cancel-button-text="$t('step3.index.5q093f8y9zg5')" @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="danger" text>
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div class="flex items-center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar5") }}
                  <el-button size="small" type="text" @click.stop="handleToolsParams(5)" >
                  <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef">
                <el-table :data="form.debeziumMysqlSinkData" border :row-class-name="basicRowClass"
                  class="tableClass" style="width: 100%">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`debeziumMysqlSinkData.${$index}.paramChangeValue`" class="hide-asterisk" >
                      <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)" clearable />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100" align="center" >
                  <template #default="{ row }">
                    <el-form-item class="hide-asterisk">
                      <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                        :cancel-button-text="$t('step3.index.5q093f8y9zg5')" @confirm="deleteToolsParams(row)">
                      <template #reference>
                        <el-button size="small" type="danger" text>
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </template>
                      </el-popconfirm>
                    </el-form-item>
                  </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion >
            <el-collapse-item name="1" :class="customStyle">
              <template #title>
                <div class="flex items-center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar6") }}
                  <el-button size="small" type="text" @click="handleToolsParams(6)" >
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="basicFormRef">
                <el-table :data="form.debeziumMysqlSourceData" border
                  :row-class-name="basicRowClass" class="tableClass" style="width: 100%">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`debeziumMysqlSinkData.${$index}.paramChangeValue`" class="hide-asterisk" >
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)" clearable/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip />
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')"
                    width="100" align="center">
                  <template #default="{ row }">
                    <el-form-item class="hide-asterisk">
                      <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" :confirm-button-text="$t('step3.index.5q093f8y9zg4')"
                        :cancel-button-text="$t('step3.index.5q093f8y9zg5')" @confirm="deleteToolsParams(row)" >
                      <template #reference>
                        <el-button size="small" type="danger" text>
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </template>
                      </el-popconfirm>
                    </el-form-item>
                  </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="7" :class="customStyle">
              <template #title>
                <div class="flex items-center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar7") }}
                  <el-button size="small" type="text" @click="handleToolsParams(7)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="sinkFormRef">
                <el-table :data="form.debeziumOpengaussSinkData" border
                  :row-class-name="basicRowClass" class="tableClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip
                  />
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300"
                  >
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`debeziumOpengaussSinkData.${$index}.paramChangeValue`" class="hide-asterisk">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100" align="center">
                    <template #default="{ row }">
                      <el-form-item class="hide-asterisk">
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="danger" text>
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
          <el-divider/>
          <el-collapse v-model="activeCollapse" accordion>
            <el-collapse-item name="8" :class="customStyle">
              <template #title>
                <div class="flex items-center">
                  {{ $t("components.ToolsParamsConfig.5q0toolspar8") }}
                  <el-button size="small" type="text" @click="handleToolsParams(8)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                </div>
              </template>
              <el-form :model="form" ref="sourceFormRef">
                <el-table :data="form.debeziumOpengaussSourceData" border
                  :row-class-name="basicRowClass" class="tableClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                    width="200" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')" width="300">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`debeziumOpengaussSourceData.${$index}.paramChangeValue`" class="hide-asterisk">
                        <el-input v-model.trim="row.paramChangeValue" @change="changeRecord(row)"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa11')" prop="paramValue"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                    width="300" show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ToolsParamsConfig.5q0toolspa20')" width="100" align="center">
                    <template #default="{ row }">
                      <el-form-item class="hide-asterisk">
                        <el-popconfirm :title="$t('components.ToolsParamsConfig.5q0toolspa28')" @confirm="deleteToolsParams(row)">
                          <template #reference>
                            <el-button size="small" type="danger" text>
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </template>
                        </el-popconfirm>
                      </el-form-item>
                    </template>
                  </el-table-column>
                </el-table>
              </el-form>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
      <template #footer>
        <div class="footer-con">
          <el-button type="primary" @click="saveParams()">
            {{ $t("components.ParamsConfig.5q0aazspq4g0") }}
          </el-button>
        </div>
      </template>
    </el-drawer>
    <add-tools-params
      v-model:open="addToolsParamsDlg.visible"
      :configId="addToolsParamsDlg.configId"
      :hostId="hostId"
      :flushTools="getToolsParams"
    />
  </div>
</template>

<script setup>
import {reactive, ref, watch} from "vue";
import {listToolsParams, modToolsParams, removeToolsParams} from "@/api/task";
import {useI18n} from "vue-i18n";
import {TOOLS_CONFIG_TYPE} from "@/utils/constants";
import addToolsParams from "./AddToolsParam.vue";
import {Delete, Plus} from '@element-plus/icons-vue'
import showMessage from "@/utils/showMessage";

const activeCollapse = ref('0')
const addToolsParamsDlg = reactive({
  visible: false,
  configId: "",
});

const basicRowClass = (row) => {
  if(data.has(row.row.id)){
    return 'row-changed'
  }
  return ''
}


const handleToolsParams = (configId) => {
  addToolsParamsDlg.configId = configId;
  addToolsParamsDlg.visible = true;
};

const { t } = useI18n();

const props = defineProps({
  open: Boolean,
  hostId: String,
});

const form = reactive({
  chameleonData: [],
  dataCheckApplicationData: [],
  dataCheckApplicationSinkData: [],
  dataCheckApplicationSourceData: [],
  debeziumMysqlSinkData: [],
  debeziumMysqlSourceData: [],
  debeziumOpengaussSinkData: [],
  debeziumOpengaussSourceData: [],
  portalData: [],
  changeNumber: 0,
});

const visible = ref(false);

function addInputGroup() {
  inputValue.value.push("");
}

const customStyle = ref({
  borderRadius: '6px',
  marginBottom: '18px',
  border: 'none',
  overflow: 'hidden',
})


const changeToolsParams = ref([]);

const data = new Map();

const emits = defineEmits(["update:open"]);

const saveParams = () => {
  data.forEach((value, key) => changeToolsParams.value.push(value));
  modToolsParams(changeToolsParams.value)
    .then((res) => {
      if (Number(res.code) === 200) {
        visible.value = false;
      }
    })
    .catch((error) => {
      console.log(error)
      showMessage('error', 'Save error')
    })
};

const deleteToolsParams = (record) => {
  removeToolsParams(record.id).then(()=>{
    getToolsParams();
  }) .catch(() => {
    getToolsParams();
  });
};

const changeRecord = (record) => {
  data.set(record.id, record);
  form.changeNumber=data.size;
  form.portalData=[...form.portalData];
  form.chameleonData=[...form.chameleonData];
  form.dataCheckApplicationData=[...form.dataCheckApplicationData];
  form.dataCheckApplicationSinkData=[...form.dataCheckApplicationSinkData];
  form.dataCheckApplicationSourceData=[...form.dataCheckApplicationSourceData];
  form.debeziumMysqlSinkData=[...form.debeziumMysqlSinkData];
  form.debeziumMysqlSourceData=[...form.debeziumMysqlSourceData];
  form.debeziumOpengaussSinkData=[...form.debeziumOpengaussSinkData];
  form.debeziumOpengaussSourceData=[...form.debeziumOpengaussSourceData];

};

const getToolsParams = () => {
  listToolsParams(props.hostId).then((res) => {
    form.chameleonData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.CHAMELEON_CONFIG
    );
    form.dataCheckApplicationData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DATA_CHECK_APPLICATION
    );
    form.dataCheckApplicationSinkData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DATA_CHECK_APPLICATION_SINK
    );
    form.dataCheckApplicationSourceData = res.data.filter(
      (item) =>
        item.configId === TOOLS_CONFIG_TYPE.DATA_CHECK_APPLICATION_SOURCE
    );
    form.debeziumMysqlSinkData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DEBEZIUM_MYSQL_SINK
    );
    form.debeziumMysqlSourceData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DEBEZIUM_MYSQL_SOURCE
    );
    form.debeziumOpengaussSinkData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DEBEZIUM_OPENGAUSS_SINK
    );
    form.debeziumOpengaussSourceData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.DEBEZIUM_OPENGAUSS_SOURCE
    );
    form.portalData = res.data.filter(
      (item) => item.configId === TOOLS_CONFIG_TYPE.PORTAL_DATA
    );
  });
};


watch(visible, (v) => {
  emits("update:open", v);
});

watch(
  () => props.open,
  (v) => {
    if (v) {
      changeToolsParams.value = [];
      data.clear();
      getToolsParams();
      form.changeNumber=0;
      addToolsParamsDlg.visible = false
    }
    visible.value = v;
  }
);
</script>
<style lang="less" scoped>
.title-con {
  width: 540px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .params-info {
    font-size: 14px;
    font-weight: normal;
    b {
      color: rgb(var(--primary-6));
    }
  }
}
.config-con {
  .diy-info-con {
    margin-bottom: 10px;
  }
  .basic-config-con {
    .basic-title {
      padding-left: 13px;
      margin-bottom: 10px;
    }
    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }
  .super-config-con {
    margin-top: 10px;
    :deep(.arco-collapse-item-content) {
      padding-left: 0;
      padding-right: 0;
      background-color: transparent;
      .arco-collapse-item-content-box {
        padding: 0;
      }
    }
    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }
}
:deep(.arco-form-item) {
  margin-bottom: 0;
}
:deep(.arco-col-5) {
  flex: none;
  width: auto;
}

.tableClass {
  max-width: 100%;
  max-height: 800px;
  overflow-y: auto;
}

.custom-collapse .el-collapse-item__header {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.custom-collapse .el-collapse-item__arrow {
  margin-right: 8px;
  margin-left: auto;
}

.el-collapse {
  --el-collapse-border-color: none;
}
.hide-asterisk .el-form-item__label::after {
  content: none !important;
}

.tableClass .el-table__row {
  line-height: 2.2;
}

.el-table .el-form-item {
  margin-bottom: 0;
}
</style>
