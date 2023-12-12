<template>
  <div>
    <a-drawer v-model:visible="visible" width="50vw" unmount-on-close>
      <template #title>
        <div class="title-con">
          <span class="params-title">{{
            $t("components.ToolsParamsConfig.5q0toolspar0")
          }}</span>
           <span class="tips-item"
            >{{ $t('components.ToolsParamsConfig.5q0toolspa26') }}<b>{{ form.changeNumber }}</b
            >{{ $t('components.ToolsParamsConfig.5q0toolspa27') }}</span
          >
        </div>
      </template>
      <div class="config-con">
        <div class="basic-config-con">
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                key="1"
                :style="customStyle">
                <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar9") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(9)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form.portalData" style="overflow-x: auto;">
                  <a-table
                    :data="form.portalData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`portalData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>

                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item key="1" :style="customStyle">
                <template #header>
                  <div style="display: flex; align-items: center">
                    {{ $t("components.ToolsParamsConfig.5q0toolspar1") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(1)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef" class="tableClass">
                  <a-table
                    :data="form.chameleonData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`chameleonData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                key="1"
                :style="customStyle">
                 <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar2") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(2)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.dataCheckApplicationData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`dataCheckApplicationData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar3')"
                key="1"
                :style="customStyle">
                <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar3") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(3)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.dataCheckApplicationSinkData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`dataCheckApplicationSinkData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar4')"
                key="1"
                :style="customStyle">
                <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar4") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(4)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.dataCheckApplicationSourceData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`dataCheckApplicationSourceData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar5')"
                key="1"
                :style="customStyle"
              >
              <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar5") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(5)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.debeziumMysqlSinkData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`debeziumMysqlSinkData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar6')"
                key="1"
                :style="customStyle"
              >
              <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar6") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(6)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.debeziumMysqlSourceData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`debeziumMysqlSourceData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar7')"
                key="1"
                :style="customStyle"
              >
              <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar7") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(7)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.debeziumOpengaussSinkData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`debeziumOpengaussSinkData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
            <a-collapse
              :default-active-key="[1]"
              accordion
              :bordered="false"
              expand-icon-position="right"
              :show-expand-icon="true">
              <a-collapse-item
                :header="$t('components.ToolsParamsConfig.5q0toolspar8')"
                key="1"
                :style="customStyle"
              >
              <template #header>
                  <div style="display: flex; align-items: center" >
                    {{ $t("components.ToolsParamsConfig.5q0toolspar8") }}
                    <a-button
                      size="mini"
                      type="text"
                      @click="handleToolsParams(8)"
                    >
                      <template #icon>
                        <icon-plus />
                      </template>
                    </a-button>
                  </div>
                </template>
                <a-form :model="form" ref="basicFormRef">
                  <a-table
                    :data="form.debeziumOpengaussSourceData"
                    :hoverable="false"
                    :bordered="true"
                    :pagination="false"
                    :row-class="basicRowClass"
                    class="tableClass"
                  >
                    <template #columns>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppog0')"
                        data-index="paramKey"
                        :width="200"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazsppr80')"
                        :width="300"
                      >
                        <template #cell="{ record, rowIndex }">
                          <a-form-item
                            hide-asterisk
                            :field="`debeziumOpengaussSourceData.${rowIndex}.paramChangeValue`"
                            @change="changeRecord(record)"
                          >
                            <a-input v-model.trim="record.paramChangeValue" />
                          </a-form-item>
                        </template>
                      </a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa11')"
                        data-index="paramValue"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ParamsConfig.5q0aazspptw0')"
                        data-index="paramDesc"
                        :width="300"
                        ellipsis
                        tooltip
                      ></a-table-column>
                      <a-table-column
                        :title="$t('components.ToolsParamsConfig.5q0toolspa20')"
                        :width="100"
                        ellipsis
                        tooltip
                      >
                        <template #cell="{ record }">
                          <a-form-item hide-asterisk>
                            <a-popconfirm 
                            :content="$t('components.ToolsParamsConfig.5q0toolspa28')"
                             type="warning"
                            :ok-text="$t('step3.index.5q093f8y9zg4')"
                            :cancel-text="$t('step3.index.5q093f8y9zg5')"
                            @ok="deleteToolsParams(record)"
                            >
                            <a-button
                              size="mini"
                             type="text"
                            >
                              <template #icon>
                                <icon-delete />
                              </template>
                            </a-button>
                          </a-popconfirm> 
                          </a-form-item>
                        </template>
                      </a-table-column>
                    </template>
                  </a-table>
                </a-form>
              </a-collapse-item>
            </a-collapse>
        </div>
      </div>

      <template #footer>
        <div class="footer-con">
          <a-button type="primary" @click="saveParams()">{{
            $t("components.ParamsConfig.5q0aazspq4g0")
          }}</a-button>
        </div>
      </template>
    </a-drawer>
    <add-tools-params
      v-model:open="addToolsParamsDlg.visible"
      :configId="addToolsParamsDlg.configId"
      :hostId="hostId"
      :flushTools="getToolsParams"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from "vue";
import { listToolsParams, modToolsParams, removeToolsParams } from "@/api/task";
import { mergeObjectArray } from "@/utils";
import { useI18n } from "vue-i18n";
import { TOOLS_CONFIG_TYPE } from "@/utils/constants";
import addToolsParams from "./AddToolsParam.vue";

const addToolsParamsDlg = reactive({
  visible: false,
  configId: "",
});

const basicRowClass = (row) => {
  if(data.has(row.raw.id)){
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
  modToolsParams(changeToolsParams.value);
  visible.value = false;
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
  max-width: 100%;  /* 设置宽度 */
  max-height: 800px;  /* 设置最大高度 */
  overflow-y: auto; 
}
</style>
