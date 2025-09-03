<template>
  <div class="daily-ops-c" id="dailyOps">
    <div>
      <span class="mainTitle">{{ $t('operation.DailyOps.sl3u5s5cf2y0') }}</span>
      <div style="float:right">
        <a-link class="my-link">{{ $t('operation.DailyOps.sl3u5s5cf2y1') }}</a-link>&nbsp;&nbsp;
        <a-link class="my-link">{{ $t('operation.DailyOps.5mplp1xbyqc0') }}</a-link>&nbsp;&nbsp;

        <a-button class="my-button" @click="editClusterTask">{{ $t('operation.DailyOps.sl3u5s5cf2y2') }}</a-button> &nbsp;
        <a-button class="my-button blueColor" @click="goInstall">{{ $t('operation.DailyOps.sl3u5s5cf2y3') }}</a-button> &nbsp;
      </div>
    </div>
    <br><br>

    <div class="barDiv" style="display: inline-block;width: 30%;">
      <span>{{ $t('operation.DailyOps.sl3u5s5cf2y4') }}</span>
      <div :key="0" class="echart" :id="'mychart' + 0" :style="state.myChartStyle"></div>
    </div>
    <div class="barDiv" style="display: inline-block;width: 30%;">
      <span>{{ $t('operation.DailyOps.sl3u5s5cf2y5') }}</span>
      <div :key="1" class="echart" :id="'mychart' + 1" :style="state.myChartStyle"></div>
    </div>
    <div class="barDiv" style="display: inline-block;width: 30%;">
      <span>{{ $t('operation.DailyOps.sl3u5s5cf2y6') }}</span>
      <div :key="2" class="echart" :id="'mychart' + 2" :style="state.myChartStyle"></div>
    </div>
    <br><br>
    <div style="border-bottom:1px solid #cecece;">
      <a-link class="my-link" :style="clusterMenuText" @click="clusterMenu" style="border-radius:0px">{{ $t('operation.DailyOps.sl3u5s5cf2y7') }}</a-link>
      <a-link class="my-link" :style="parallelInstallTaskText" @click="parallelInstallTask" style="border-radius:0px">{{ $t('operation.DailyOps.sl3u5s5cf2y8') }}</a-link>
    </div>
    <br><br>

    <div v-if="list.pageTag === 'installTask'">
      <div style="border-bottom:1px solid #cecece;">
        <a-link class="my-link" :style="taskTable" @click="taskMenu" >{{ $t('operation.DailyOps.sl3u5s5cf2y9') }}</a-link>
        <a-link class="my-link" :style="draftBox" @click="switchDraft">{{ $t('operation.DailyOps.sl3u5s5cf210') }}</a-link>
      </div>
      <br>
      <a-button class="my-button blueColor" @click="editClusterTask">{{ $t('operation.DailyOps.sl3u5s5cf2y2') }}</a-button> &nbsp;
      <a-button class="my-button" @click="batchExecute" :seleMsg="seleMsg" v-if="list.status.status != 'DRAFT'">{{ $t('operation.DailyOps.sl3u5s5cf211') }}</a-button> &nbsp;
      <task-execute ref="batchExecuteRef" @finish="watchExecuteStatus" @ok="watchExecuteStatus"></task-execute>
      <a-popconfirm
        :content="$t('packageManage.index.5myq5c8zms40')"
        :ok-text="$t('packageManage.index.5myq5c8zn100')"
        :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
        @ok="deleteSelectedTaskId"
      >
        <a-button class="my-button">
          {{ $t('operation.DailyOps.sl3u5s5cf212') }}
        </a-button>
      </a-popconfirm>
      <br><br>

      <a-cascader
        labelInValue
        :field-names="{label:'value', value:'name', children:'children' }"
        expand-child
        v-model="selectedOptionsValue.value"
        :options="parentTags"
        @change="searchTag"
        allow-clear
        style="width: 100%"
        multiple
        :placeholder="$t('operation.DailyOps.sl3u5s5cf213')"
      ></a-cascader>

      <br><br>

      <a-table
        v-if="list.status.status !== 'DRAFT'"
        class="d-a-table-row"
        :data="list.data"
        rowKey="clusterId"
        :columns="columnsTaskMenu"
        :pagination="list.page"
        @page-change="currentPage"
        :row-selection="list.rowSelection"
        @selection-change="handleSelectedChange"
      >
        <template #clusterId="{ record }">
          <div class="clusterId">
            <a-link class="more" @click="exportClusterId(record.clusterId)">
              {{record.clusterId}}
            </a-link>
          </div>
        </template>
        <template #status="{ record }">
            <span v-if="record.status === 'PENDING'">
              {{ $t('operation.DailyOps.sl3u5s5cf214') }}
            </span>
            <span v-if="record.status === 'WAITING'">
              <a-spin :loading="true">{{ $t('operation.DailyOps.sl3u5s5cf215') }}</a-spin>
            </span>
            <span v-if="record.status === 'RUNNING'">
              <a-spin :loading="true">{{ $t('operation.DailyOps.sl3u5s5cf216') }}</a-spin>
            </span>
            <span v-if="record.status === 'SUCCESS'">
              {{ $t('operation.DailyOps.sl3u5s5cf217') }}
            </span>
            <span v-if="record.status === 'FAILED'">
              {{ $t('operation.DailyOps.sl3u5s5cf218') }}
              <a-tooltip content="This is a Tooltip" position="right">
                <icon-question-circle style="cursor: pointer;margin-left: 3px;" size="15" />
                <template #content>
                  <p>{{record.remark}}</p>
                </template>
              </a-tooltip>
            </span>
        </template>
        <template #version="{ record }">
            <span v-if="record.version === 'ENTERPRISE'">
              {{ $t('operation.DailyOps.5mplp1xc4fg0') }} {{record.versionNum}}
            </span>
          <span v-if="record.version === 'LITE'">
              {{ $t('operation.DailyOps.5mplp1xc4b40') }} {{record.versionNum}}
            </span>
          <span v-if="record.version === 'MINIMAL_LIST'">
              {{ $t('operation.DailyOps.5mplp1xc46o0') }} {{record.versionNum}}
            </span>
        </template>
        <template #clusterNodeNum="{ record }">
            <span v-if="record.deployType === 'CLUSTER' && record.clusterNodeNum > 1">
              {{ $t('operation.DailyOps.5mplp1xbzmw0') }}{{record.clusterNodeNum - 1}}{{ $t('operation.DailyOps.5mplp1xbztc0') }}
            </span>
            <span v-else>
              {{ $t('operation.DailyOps.5mplp1xc0000') }}
            </span>
        </template>
        <template #operation="{ record }">
          <div class="operationDiv">
            <span>
              <a-popconfirm
                :content="$t('operation.DailyOps.sl3u5s5cf219')"
                :ok-text="$t('packageManage.index.5myq5c8zn100')"
                :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
                @ok="record.status === 'PENDING' ? execute(record.clusterId) : reExecute(record.clusterId)"
              >
                <a-link class="more" v-if="record.status === 'PENDING'">
                  {{ $t('operation.DailyOps.sl3u5s5cf220') }}
                </a-link>
                <a-link class="more" v-else-if="record.status === 'SUCCESS'">
                </a-link>
                <a-link class="more" v-else>
                  {{ $t('operation.DailyOps.sl3u5s5cf221') }}
                </a-link>
              </a-popconfirm>
            </span>

            <a-link class="more" @click="copy(record.clusterId)">
              {{ $t('operation.DailyOps.sl3u5s5cf222') }}
            </a-link>

            <a-dropdown trigger="click">
              <a-link class="more">
                {{ $t('operation.DailyOps.sl3u5s5cf223') }}
              </a-link>
              <template #content>
                <a-doption  @click="logDownload(record)">
                  <a-space>
                    <span>
                      {{ $t('operation.DailyOps.sl3u5s5cf224') }}
                    </span>
                  </a-space>
                </a-doption>
                <a-doption  @click="editClusterTask(record)">
                  <a-space>
                    <span>
                      {{ $t('operation.DailyOps.sl3u5s5cf225') }}
                    </span>
                  </a-space>
                </a-doption>
                <a-popconfirm
                  :content="$t('packageManage.index.5myq5c8zms40')"
                  :ok-text="$t('packageManage.index.5myq5c8zn100')"
                  :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
                  @ok="singleRowDelete(record)"
                >
                  <a-button type="text" style="width: 100%; justify-content: left;">
                    <span>{{ $t('operation.DailyOps.sl3u5s5cf212') }}</span>
                  </a-button>
                </a-popconfirm>
              </template>
            </a-dropdown>
          </div>
        </template>
      </a-table>
      <a-table
        v-else
        class="d-a-table-row"
        :data="list.data"
        rowKey="clusterId"
        :columns="columnsDraft"
        :pagination="list.page"
        @page-change="currentPage"
        :row-selection="list.rowSelection"
        @selection-change="handleSelectedChange"
      >
        <template #clusterId="{ record }">
          <div class="clusterId">
            <a-link class="more" @click="exportClusterId(record.clusterId)">
              {{record.clusterId}}
            </a-link>
          </div>
        </template>

        <template #version="{ record }">
          <span v-if="record.version === 'ENTERPRISE'">
            {{ $t('operation.DailyOps.5mplp1xc4fg0') }} {{record.versionNum}}
          </span>
          <span v-if="record.version === 'LITE'">
            {{ $t('operation.DailyOps.5mplp1xc4b40') }} {{record.versionNum}}
          </span>
          <span v-if="record.version === 'MINIMAL_LIST'">
            {{ $t('operation.DailyOps.5mplp1xc46o0') }} {{record.versionNum}}
          </span>
        </template>
        <template #clusterNodeNum="{ record }">
          <span v-if="record.deployType === 'CLUSTER' && record.clusterNodeNum > 1">
            {{ $t('operation.DailyOps.5mplp1xbzmw0') }}{{record.clusterNodeNum - 1}}{{ $t('operation.DailyOps.5mplp1xbztc0') }}
          </span>
          <span v-else>
            {{ $t('operation.DailyOps.5mplp1xc0000') }}
          </span>
        </template>
        <template #operation="{ record }">
          <div class="operationDiv">
            <a-link class="more" @click="editClusterTask(record)">
              {{ $t('operation.DailyOps.sl3u5s5cf225') }}
            </a-link>

            <a-link class="more" @click="copy(record.clusterId)">
              {{ $t('operation.DailyOps.sl3u5s5cf222') }}
            </a-link>
            <a-dropdown trigger="click">
              <a-link class="more">
                {{ $t('operation.DailyOps.sl3u5s5cf223') }}
              </a-link>
              <template #content>
                <a-doption  @click="logDownload(record)">
                  <a-space>
                    <span>
                      {{ $t('operation.DailyOps.sl3u5s5cf224') }}
                    </span>
                  </a-space>
                </a-doption>
                <a-doption  @click="record.envCheckResult === 'SUCCESS' ? publish(record.clusterId) : envCheck(record.clusterId, record.hostIp, record.hostUsername)">
                  <a-space>
                    <span>
                      <span v-if="record.envCheckResult === 'SUCCESS'">
                        {{ $t('operation.DailyOps.sl3u5s5cf226') }}
                      </span>
                      <span v-else>
                        {{ $t('operation.DailyOps.sl3u5s5cf227') }}
                      </span>
                    </span>
                  </a-space>
                </a-doption>
                <a-popconfirm
                  :content="$t('packageManage.index.5myq5c8zms40')"
                  :ok-text="$t('packageManage.index.5myq5c8zn100')"
                  :cancel-text="$t('packageManage.index.5myq5c8zn7k0')"
                  @ok="singleRowDelete(record)"
                >
                  <a-button type="text" style="width: 100%; justify-content: left;">
                    <span>{{ $t('operation.DailyOps.sl3u5s5cf212') }}</span>
                  </a-button>
                </a-popconfirm>
              </template>
            </a-dropdown>
          </div>
        </template>
      </a-table>
      <div v-if="anyMenuOpen" class="overlay" @click="closeAllMenus"></div>
    </div>
    <div v-show="list.pageTag === 'clusterInfo'">
    <a-input-search
    :placeholder="$t('operation.DailyOps.sl3u5s5cf299')"
    v-model="searchClusterName"
    @search="getList"
    @press-enter="getList"
    class="search-cluster"
    max-length="255"
    allow-clear />
    <div v-show="showClusterInfo && list.pageTag === 'clusterInfo'">
      <div
        v-for="(clusterData, index) in data.clusterList"
        :key="index"
      >
        <a-spin
          class="full-w"
          :loading="clusterData.loading"
          v-if="!clusterData.isHidden"
        >
          <div class="item-c mb">
            <div class="flex-between mb">
              <div class="flex-row ft-lg ft-b">
                <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xbxl40') }}</div>
                <div class="main-color mr-s">{{ clusterData.clusterId ? clusterData.clusterId : '--' }}</div>
                <div class="main-color mr-s">{{ clusterData.version ? getVersionName(clusterData.version) : '--' }}
                </div>
                <div class="main-color">{{ clusterData.versionNum ? clusterData.versionNum : '--' }}</div>
              </div>
              <div class="flex-row">
                <div
                  class="flex-row ft-b mr"
                  v-if="clusterData.version === OpenGaussVersionEnum.ENTERPRISE"
                >
                  <icon-exclamation-circle class="label-color mr-s" />
                  <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xbyi40') }}</div>
                  <div class="label-color mr-s">{{ getWarningNum(clusterData) }}</div>
                  <div
                    class="flex-row"
                    v-if="clusterData.lastCheckAt"
                  >
                    <icon-clock-circle class="label-color mr-s" />
                    <div class="label-color mr-s">{{ $t('operation.DailyOps.lastCheckAt') }}</div>
                    <div class="label-color mr">{{ clusterData.lastCheckAt }}</div>
                  </div>

                  <a-button
                    type="text"
                    @click="showOneCheck(clusterData)"
                  >{{
                    $t('operation.DailyOps.5mplp1xbyqc0')
                  }}</a-button>
                </div>
                <icon-down
                  class="label-color open-close-c"
                  v-if="!clusterData.isShow"
                  @click="clusterData.isShow = true"
                />
                <icon-up
                  class="label-color open-close-c"
                  v-else
                  @click="clusterData.isShow = false"
                />
              </div>
            </div>
            <div v-if="clusterData.isShow">
              <!-- cluster info -->
              <div class="item-cluster-info">
                <div class="flex-row">
                  <svg-icon
                    icon-class="ops-cluster"
                    class="cluster-icon-size mr"
                  ></svg-icon>
                  <div class="flex-col-start">
                    <div class="cluster-info-screen">
                      <div class="flex-row mr mb-screen">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xbyxw0') }}:</div>
                        <a-tag :color="getClusterColor(clusterData)">{{
                          getClusterState(clusterData)
                        }}</a-tag>
                      </div>
                      <div class="flex-row mr mb-screen">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xbzew0') }}:</div>
                        <div
                          class="flex-row"
                          v-if="clusterData.deployType === 'CLUSTER' && clusterData.clusterNodes.length > 1"
                        >
                          <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xbzmw0') }}</div>
                          <div class="value-color ft-lg ft-b mr-s">{{ clusterData.clusterNodes.length - 1 }}</div>
                          <div class="label-color">{{ $t('operation.DailyOps.5mplp1xbztc0') }}</div>
                        </div>
                        <div
                          class="value-color ft-b"
                          v-else
                        >{{ $t('operation.DailyOps.5mplp1xc0000') }}</div>
                      </div>
                    </div>
                    <div class="cluster-info-screen">
                      <div class="flex-row mr mb-screen">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.else12') }}:</div>
                        <div class="label-color">{{ clusterData.clusterNodes[0].dbUser }}</div>
                      </div>
                      <div class="flex-row mb-screen">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.else13') }}:</div>
                        <div class="label-color">{{ clusterData.clusterNodes[0].dbPort }}</div>
                      </div>
                    </div>
                    <div
                      class="flex-row mb-screen"
                      v-if="clusterData.envPath"
                    >
                      <div class="label-color mr-s">{{ $t('operation.DailyOps.else17') }}:</div>
                      <div class="label-color">{{ clusterData.envPath }}</div>
                    </div>
                  </div>
                </div>
                <div class="flex-col-start">
                  <div class="label-color mb">{{ $t('operation.DailyOps.5mplp1xc0640') }}</div>
                  <div class="flex-row">
                    <a-button
                      type="outline"
                      class="mr"
                      @click="handleEnable(clusterData, index)"
                    >{{
                      $t('operation.DailyOps.5mplp1xc0c00')
                    }}</a-button>
                    <a-button
                      type="outline"
                      class="mr"
                      @click="handleStop(clusterData)"
                    >{{
                      $t('operation.DailyOps.5mplp1xc0i80')
                    }}</a-button>
                    <a-button
                      type="outline"
                      class="mr"
                      @click="handleReset(clusterData, index)"
                    >{{
                      $t('operation.DailyOps.5mplp1xc0o40')
                    }}</a-button>
                    <a-button
                      type="outline"
                      class="mr"
                      @click="handleBackupDlg(index)"
                    >{{
                      $t('operation.DailyOps.5mplp1xc0u40')
                    }}</a-button>
                    <a-popconfirm
                      :content="$t('operation.DailyOps.5mplp1xc0zo0')"
                      type="warning"
                      :ok-text="$t('operation.DailyOps.5mplp1xc1580')"
                      :cancel-text="$t('operation.DailyOps.5mplp1xc1b00')"
                      @ok="handleUninstallBefore(clusterData, index, false)"
                    >
                      <a-button
                        type="outline"
                        class="mr"
                      >{{
                        $t('operation.DailyOps.5mplp1xc1gs0')
                      }}</a-button>
                    </a-popconfirm>
                    <a-popconfirm
                      :content="$t('operation.DailyOps.5mplp1xc0zo0')"
                      type="warning"
                      :ok-text="$t('operation.DailyOps.5mplp1xc1580')"
                      :cancel-text="$t('operation.DailyOps.5mplp1xc1b00')"
                      @ok="handleUninstallBefore(clusterData, index, true)"
                    >
                      <a-button
                        type="outline"
                        class="mr"
                      >{{
                        $t('operation.DailyOps.5mplp1xc1ms0')
                      }}</a-button>
                    </a-popconfirm>
                    <a-popconfirm
                      :content="$t('operation.DailyOps.else5')"
                      type="warning"
                      :ok-text="$t('operation.DailyOps.5mplp1xc1580')"
                      :cancel-text="$t('operation.DailyOps.5mplp1xc1b00')"
                      @ok="handleDelCluster(clusterData, index)"
                    >
                      <a-button
                        type="outline"
                        class="mr"
                      >{{
                        $t('operation.DailyOps.else4')
                      }}</a-button>
                    </a-popconfirm>
                  </div>
                </div>
              </div>
              <!-- host info -->
              <div
                class="item-node-info"
                v-for="(instance, nodeIndex) in clusterData.clusterNodes"
                :key="nodeIndex"
              >
                <div class="host-c node-w">
                  <div class="flex-between mb">
                    <div class="flex-row">
                      <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc1t80') }}</div>
                      <div class="label-color">{{ instance.privateIp }}({{ instance.publicIp }})</div>
                    </div>
                    <a-dropdown @select="handleHostOper($event, index, nodeIndex)">
                      <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                      <template #content>
                        <a-doption value="start">{{ $t('operation.DailyOps.5mplp1xc25o0') }}</a-doption>
                        <a-doption value="stop">{{ $t('operation.DailyOps.5mplp1xc2b40') }}</a-doption>
                      </template>
                    </a-dropdown>
                  </div>
                  <div class="flex-row-center">
                    <svg-icon
                      icon-class="ops-host"
                      class="host-icon-size mr-lg"
                    ></svg-icon>
                    <div class="host-info-screen">
                      <div class="flex-col-start mr">
                        <div class="flex-row host-mb-screen">
                          <div class="label-color mr-s">{{ $t('operation.DailyOps.else14') }}</div>
                          <div
                            :title="instance.installPath"
                            class="path-c value-color"
                          >{{ instance.installPath }}</div>
                        </div>
                        <div class="flex-row host-mb-screen">
                          <div class="label-color mr-s">{{ $t('operation.DailyOps.else15') }}</div>
                          <div
                            :title="instance.dataPath"
                            class="path-c value-color"
                          >{{ instance.dataPath }}</div>
                        </div>
                      </div>

                      <div class="host-info">
                        <!-- <div class="flex-col-start mr">
                        <div class="flex-row mb">
                          <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc2gw0') }}</div>
                          <div class="value-color">{{ instance.privateIp }}</div>
                        </div>
                        <div class="flex-row">
                          <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc2ls0') }}</div>
                          <div class="value-color">{{ instance.publicIp }}</div>
                        </div>
                      </div> -->
                        <div class="flex-col-start mr">
                          <div class="flex-row host-mb-screen">
                            <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc2sw0') }}</div>
                            <div class="value-color" style="line-height: 25px;">{{ instance.hostname }}</div>
                          </div>
                          <div class="flex-row host-mb-screen">
                            <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc2xs0') }}</div>
                            <div class="value-color" style="line-height: 25px;">{{ instance.kernel ? instance.kernel : '-' }}{{
                              $t('operation.DailyOps.else1')
                            }}{{ instance.memorySize ? instance.memorySize : '-' }}G</div>
                          </div>
                        </div>
                        <div class="flex-col-start mr">
                          <div class="flex-row host-mb-screen">
                            <div class="label-color mr-s">CPU</div>
                            <div class="value-color" style="line-height: 25px;">{{ instance.cpu ? instance.cpu : '--' }}%</div>
                          </div>
                          <div class="flex-row host-mb-screen">
                            <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc32g0') }}</div>
                            <div class="value-color" style="line-height: 25px;">{{ instance.memory ? instance.memory : '--' }}%</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="instance-c node-w">
                  <div class="node-type">
                    <div class="content">{{ getRoleName(instance.clusterRole) }}</div>
                  </div>
                  <div class="flex-between mb">
                    <div class="flex-row">
                      <div
                        class="flex-row mr"
                        style="margin-left: 20px"
                      >
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.else2') }}{{ nodeIndex + 1 }}</div>
                        <a-tag :color="getInstanceStateColor(clusterData, instance)">{{
                          getInstanceState(clusterData, instance)
                        }}</a-tag>
                      </div>
                      <div class="flex-row mr-s">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.else16') }}</div>
                        <div class="label-color">{{ instance.installUserName }}</div>
                      </div>
                      <div class="flex-row">
                        <a-button
                          type="text"
                          class="label-color mr-s"
                          @click="handleOpenGucSetting(clusterData, instance, index)"
                        >{{ $t('operation.DailyOps.guc5cg0') }}</a-button>
                      </div>
                    </div>

                    <div
                      class="flex-row"
                      v-if="instance.cmState !== CMStateEnum.Down"
                    >
                      <a-switch
                        class="mr"
                        v-model="instance.state"
                        checked-value="true"
                        unchecked-value="false"
                        @change="handleInstanceSwitchChange($event, index, nodeIndex)"
                      >
                        <template #checked>
                          ON
                        </template>
                        <template #unchecked>
                          OFF
                        </template>
                      </a-switch>
                      <a-dropdown @select="handleInstanceOper($event, index, nodeIndex)">
                        <a-button>{{ $t('operation.DailyOps.5mplp1xc20k0') }}</a-button>
                        <template #content>
                          <a-doption
                            v-for="(itemOption, index) in getDropdownList(clusterData, instance)"
                            :value="itemOption.value"
                            :key="index"
                          >{{ itemOption.label }}</a-doption>
                        </template>
                      </a-dropdown>
                    </div>
                  </div>
                  <div class="flex-row-center">
                    <svg-icon
                      icon-class="ops-instance"
                      class="host-icon-size mr-lg"
                    ></svg-icon>
                    <div class="instance-info-screen">
                      <div class="flex-row mb mr">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc3740') }}</div>
                        <div class="value-color mr-s">{{ instance.session ? instance.session : '--' }}</div>
                        <div class="label-color">{{ $t('operation.DailyOps.else3') }}</div>
                      </div>
                      <div class="flex-row mb mr">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc3bw0') }}</div>
                        <div class="value-color mr-s">{{ instance.connectNum ? instance.connectNum : '--' }}</div>
                        <div class="label-color">{{ $t('operation.DailyOps.else3') }}</div>
                      </div>
                      <div class="flex-row mb mr">
                        <div class="label-color mr-s">{{ $t('operation.DailyOps.5mplp1xc3hs0') }}</div>
                        <div class="value-color mr-s">{{ instance.lock ? instance.lock : '--' }}</div>
                        <div class="label-color">{{ $t('operation.DailyOps.else3') }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>
    </div>
  </div>
    <div
      class="full-w full-h flex-col"
      v-if="showInit && list.pageTag === 'clusterInfo'"
    >
      <svg-icon
        icon-class="ops-empty"
        class="empty-icon-size mb"
      ></svg-icon>
      <div class="empty-content mb">{{ $t('operation.DailyOps.5mplp1xc3ss0') }}</div>
      <div class="flex-row">
        <a-button
          class="mr"
          type="outline"
          size="large"
          @click="goInstall"
        >{{
          $t('operation.DailyOps.5mplp1xc3xg0')
        }}</a-button>
        <a-button
          type="outline"
          size="large"
          @click="getList"
        >{{
          $t('operation.DailyOps.else6')
        }}</a-button>
      </div>
    </div>
    <div
      class="full-h flex-col"
      v-if="data.loading && list.pageTag === 'clusterInfo'"
    >
      <a-spin :tip="$t('operation.DailyOps.5mplp1xc4200')" />
    </div>
    <cluster-backup-dlg
      ref="backupRef"
      @finish="handleBackup"
    ></cluster-backup-dlg>
    <one-check ref="oneCheckRef"></one-check>
    <host-pwd-dlg
      ref="hostPwdRef"
      @finish="handleAllNodesPwd"
    ></host-pwd-dlg>
    <guc-setting-drawer
      ref="gucSettingRef"
      @finish="handleGucSettingComplete"
    ></guc-setting-drawer>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import {onBeforeUnmount, onMounted, reactive, ref, computed, onUnmounted, toRaw, watch} from 'vue'
import {
  clusterMonitor,
  delCluster,
  uninstallOpenGauss,
  clusterList,
  start,
  stop,
  restart,
  switchover,
  generateconf,
  build,
  clusterBackup,
  clusterTaskPage,
  copyTask,
  batchDeleteTask,
  clusterLogDownload,
  clusterEnvCheck,
  submitCluster,
  executeTask,
  checkTaskStatus,
  getVersionNum,
  reExecuteTask,
} from '@/api/ops'
import { useWinBox } from 'vue-winbox'
import 'xterm/css/xterm.css'
import { Terminal } from 'xterm'
import { FitAddon } from '@xterm/addon-fit'
import { AttachAddon } from '@xterm/addon-attach'
import Socket from '@/utils/websocket'
import ClusterBackupDlg from '@/views/monitor/operation/ClusterBackupDlg.vue'
import { ClusterRoleEnum, OpenGaussVersionEnum, CMStateEnum } from '@/types/ops/install'
import OneCheck from '@/views/ops/home/components/OneCheck.vue'
import HostPwdDlg from './HostPwdDlg.vue'
import GucSettingDrawer from './GucSettingDrawer.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import * as echarts from 'echarts';
import { Message } from '@arco-design/web-vue'
import taskExecute from './taskExecute.vue'
import axios from 'axios'
import {useRoute, useRouter} from 'vue-router';
import _ from 'lodash';
import isEqual from 'lodash/isEqual';
import {CpuArch, OS} from "@/types/os";

const data: {
  loading: boolean,
  clusterList: KeyValue[],
  socketArr: Socket<any, any>[]
} = reactive({
  clusterList: [],
  loading: false,
  socketArr: []
})


const list = reactive<KeyValue>({
  selectedDraftData:[],
  selectedDraftTaskIds:[],
  selectedDraftPage:[],
  selectedData:[],
  selectTaskIds: [],
  selectedPage:[],
  data: [],
  page: {
    total: 100,
    pageSize: 10,
    'show-total': true,
  },
  loading: false,
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true
  },
  status: {},
  mutiSearchData: {},
  os: {},
  cpuArch: {},
  openGaussVersion: {},
  openGaussVersionNum: {},
  pageTag: 'clusterInfo',
})
//并行安装任务
const parallelInstallTask = () => {
  if (data.socketArr.length) {
    data.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
  watchExecuteStatus();
  list.pageTag = 'installTask';
  if (parallelInstallTaskText.value.borderBottom === '') {
    parallelInstallTaskText.value.color = '#0077ff';
    parallelInstallTaskText.value.borderBottom = '1px solid #0077ff';
    clusterMenuText.value.color = '';
    clusterMenuText.value.borderBottom = '';
  }
}
//集群列表
const clusterMenu = () => {
  clearInterval(intervalRefresh);
  getList()
  list.pageTag = 'clusterInfo';
  if (clusterMenuText.value.borderBottom === '') {
    clusterMenuText.value.color = '#0077ff';
    clusterMenuText.value.borderBottom = '1px solid #0077ff';
    parallelInstallTaskText.value.color = '';
    parallelInstallTaskText.value.borderBottom = '';
  }
}

//并行安装任务变色
const parallelInstallTaskText = ref({
  color: '',
  borderBottom: '',
})

//集群列表变色
const clusterMenuText = ref({
  color: '#0077ff',
  borderBottom: '1px solid #0077ff', //初始边框大小及颜色
})

const route = useRoute();
const router = useRouter();

//跳转任务详情
const exportClusterId = (clusterId) => {
  router.push({ path: '/monitor/taskDetails', query: { clusterId: clusterId } });
};
//检查所有信息是否可以发布
const isPublish = (clusterId: string) => {
  console.log(clusterId);
}

//调用环境监测接口
const envCheck = (clusterId: string, hostIp: string, hostUsername: string) => {
  if (hostIp !== null || hostUsername !== null) {
    clusterEnvCheck(clusterId)
      .then((res) => {
        if (Number(res.code) === 200) {
          if (res.data.result === 'SUCCESS') {
            Message.success('clusterEnvCheck:' + res.data.result);
            getDraftListData();
          } else {
            Message.error('clusterEnvCheck:' + res.data.result);
          }
        }
      }).catch(error => {
      Message.error("checkEnv infoError:"+error);
    })
  } else {
    Message.error("please edit cluster IP!")
  }
};
//跳转新增集群页面
const editClusterTask = (record) => {
  router.push({ name:'step', params: { record: JSON.stringify(toRaw(record)) } });
}

const state = reactive({
  chartData: [
    { xData: ["CAT1", "CAT2", "CAT3", "CAT4", "CAT5"], yData: [23, 24, 18, 25, 27] },
    { xData: ["DOG1", "DOG2", "DOG3", "DOG4", "DOG5"], yData: [15, 12, 19, 20, 17] },
    { xData: ["BIRD1", "BIRD2", "BIRD3", "BIRD4", "BIRD5"], yData: [22, 28, 21, 26, 23] }
  ],
  myChartStyle: { float: "right", width: "100%", height: "344px", marginRight: "2%" },
});

const initEcharts = () => {
  state.chartData.forEach((data, index) => {
    const myChart = echarts.init(document.getElementById(`mychart${index}`));
    const option = {
      title: {
        text: '84',
        subtext: t('operation.DailyOps.sl3u5s5cf254'),
        left: 'right' // 标题的水平位置
      },
      xAxis: {
        data: data.xData
      },
      yAxis: {
        name: t('operation.DailyOps.sl3u5s5cf255'),
      },
      series: [
        {
          type: "bar",
          data: data.yData,
          barWidth: '15%' // 使用百分比来自动适应容器宽度
        }
      ]
    };
    myChart.setOption(option);
    window.addEventListener("resize", () => {
      myChart.resize();
    });
  });
}

const filter = reactive({
  name: '',
  openGaussVersion: '',
  pageNum: 1,
  pageSize: 10,
})
//发布
const publish = (clusterId: number) => {
  submitCluster(clusterId)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success(res.msg);
        getDraftListData();
      }
    })
    .catch(() => {
      console.log("publish fail!")
    })
}
//一键执行
const execute = (clusterId: number) => {
  executeTask([clusterId])
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success(res.data[clusterId]);
        checkExecuteStatus(clusterId)
      }
    })
    .catch(() => {
      console.log("execute fail!")
    })
}
//重新执行
const reExecute = (clusterId: number) => {
  reExecuteTask(clusterId)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success(res.msg);
        checkExecuteStatus(clusterId);
      }
    })
    .catch(() => {
      console.log("rexecute fail!!")
    })
    .finally(() => {
      getListData();
    })
}

//查询执行状态
const checkExecuteStatus = (clusterId: number) => {
  checkTaskStatus([clusterId])
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        watchExecuteStatus();
      }
    })
    .catch((error) => {
      console.log('checkExecuteStatusError:' + error)
    })
}
//草稿箱
const switchDraft = () => {
  if (draftBox.value.backgroundColor === '') {
    draftBox.value.backgroundColor = '#0077ff';
    draftBox.value.color = 'white';
    taskTable.value.backgroundColor = '';
    taskTable.value.color = '';
  }
  filter.pageNum = 1
  list.status = {"status":"DRAFT"};
  getListData();
}
//任务列表
const taskMenu = () => {
  if (taskTable.value.backgroundColor === '') {
    taskTable.value.backgroundColor = '#0077ff';
    taskTable.value.color = 'white';
    draftBox.value.backgroundColor = '';
    draftBox.value.color = '';
  }
  filter.pageNum = 1
  list.status = {};
  getListData();
}
//页码跳转
const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

//任务列表变色
const taskTable = ref({
  backgroundColor: '#0077ff', // 初始背景色为默认值
  color: 'white'
})
//草稿箱变色
const draftBox = ref({
  backgroundColor: '', // 初始背景色为默认值
})

//定时器3秒一次刷新
let intervalRefresh: NodeJS.Timeout | null = null

//定时器函数
const watchExecuteStatus = () => {
  intervalRefresh = setInterval(() => {
    getListData()
  }, 3000)
}

//初始化任务列表
const initTaskMenu = () => {
  list.mutiSearchData = {
    "os": list.os.os ? list.os.os : null,
    "cpuArch": list.cpuArch.cpuArch ? list.cpuArch.cpuArch : null,
    "openGaussVersion": list.openGaussVersion.openGaussVersion ? list.openGaussVersion.openGaussVersion : null,
    "openGaussVersionNum": list.openGaussVersionNum.openGaussVersionNum ? list.openGaussVersionNum.openGaussVersionNum : null,
    "status": list.status.status ? list.status.status : null
  };
  clusterTaskPage(list.mutiSearchData, filter)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.rows;
        list.page.total = res.total;
        let flag = false;
        list.data.forEach(item => {
          if (item.status === 'RUNNING' || item.status === 'WAITING') {
            flag = true;
          }
        })
        if (!flag) {
          clearInterval(intervalRefresh)
        }
      }
    }).catch(error => {
    Message.error("initTaskMenu infoError:"+error);
  });
}

//调后端任务列表接口填充数据
const getListData = () => {
  initTaskMenu()
}
//调后端草稿接口填充数据
const getDraftListData = () => {
  list.status = {"status":"DRAFT"};
  initTaskMenu()
}

//挂载柱状图和集群列表数据
onMounted(() => {
  watch(() => route.path, (newPath, oldPath) => {
    if (newPath !== oldPath) {
      console.log('Route changed from', oldPath, 'to', newPath);
      if (route.params.status !== 'DRAFT') {
        taskMenu();
        console.log(route.params.status)
      } else {
        switchDraft();
        console.log(route.params.status)
      }
    }
  });
  init()
  initEcharts();
  getListData();
});
onUnmounted(() => {
  clearInterval(intervalRefresh);
});
//集群列表表头
const columnsTaskMenu = computed(() => [
  {title: t('operation.DailyOps.sl3u5s5cf244'), dataIndex: 'clusterId', width: 130, slotName: 'clusterId',},
  {title: t('operation.ClusterBackupDlg.5mplmzbrntg0'), dataIndex: 'clusterName',width: 110, },
  {title: t('operation.DailyOps.sl3u5s5cf245'), dataIndex: 'status', width: 70, slotName: 'status',},
  {title: t('operation.DailyOps.sl3u5s5cf246'), dataIndex: 'hostIp', width: 80,},
  {title: t('enterprise.NodeConfig.5mpme7w6bak0'), width: 60, dataIndex: 'hostUsername'},
  {title: t('enterprise.ClusterConfig.5mpm3ku3iz80'), width: 50, dataIndex: 'databasePort', slotName: 'databasePort',},
  {title: t('operation.DailyOps.sl3u5s5cf230'), dataIndex: 'os', width: 60,},
  {title: t('operation.DailyOps.sl3u5s5cf231'), dataIndex: 'cpuArch',width: 50, },
  {title: t('operation.DailyOps.sl3u5s5cf247'), width: 30, dataIndex: 'version', slotName: 'version',},
  {title: t('operation.DailyOps.5mplp1xbzew0'), dataIndex: 'clusterNodeNum', width: 80, slotName: 'clusterNodeNum',},
  {title: t('operation.DailyOps.sl3u5s5cf248'), dataIndex: 'createTime', width: 110,
    sortable: {sortDirections: ['ascend', 'descend']}
  },
  {title: t('operation.DailyOps.sl3u5s5cf249'), dataIndex: 'updateTime', width: 110,
    sortable: {sortDirections: ['ascend', 'descend']}
  },
  {title: t('operation.DailyOps.5mplp1xc20k0'), slotName: 'operation', width: 100},
])

//草稿箱表头
const columnsDraft = computed(() => [
  {title: t('operation.DailyOps.sl3u5s5cf244'), dataIndex: 'clusterId', width: 130, slotName: 'clusterId',},
  {title: t('operation.DailyOps.sl3u5s5cf246'), dataIndex: 'hostIp', width: 80,},
  {title: t('enterprise.NodeConfig.5mpme7w6bak0'), width: 50, dataIndex: 'hostUsername'},
  {title: t('operation.DailyOps.sl3u5s5cf230'), dataIndex: 'os', width: 70,},
  {title: t('operation.DailyOps.sl3u5s5cf231'), dataIndex: 'cpuArch',width: 70, },
  {title: t('operation.ClusterBackupDlg.5mplmzbrntg0'), dataIndex: 'clusterName',width: 110, },
  {title: t('operation.DailyOps.sl3u5s5cf247'), width: 100, dataIndex: 'version', slotName: 'version',},
  {title: t('operation.DailyOps.5mplp1xbzew0'), dataIndex: 'clusterNodeNum', width: 50, slotName: 'clusterNodeNum',},
  {title: t('enterprise.ClusterConfig.5mpm3ku3iz80'), dataIndex: 'databasePort', width: 50},
  {title: t('operation.DailyOps.sl3u5s5cf235'), dataIndex: 'envPath', width: 150},
  {title: t('operation.DailyOps.5mplp1xc20k0'), slotName: 'operation', width: 100},
])

//////////////////////////////////////////////////////////操作里面的弹窗
const showMenus = reactive(new Array(list.data.length).fill(false));

// 响应式数据，用于跟踪是否有任何菜单是打开的
const anyMenuOpen = ref(false);

// 方法：切换指定行的菜单显示状态
const toggleMenu = (index) => {
  showMenus[index] = !showMenus[index];
  anyMenuOpen.value = anyMenuOpen.value || showMenus[index];
  showMenus.forEach((show, i) => {
    if (i !== index) {
      showMenus[i] = false;
    }
  });
};

// 方法：处理指定行菜单项的点击事件
const handleMenuClick = (index, option) => {
  console.log('Clicked on row', index, 'with option:', option);
  showMenus[index] = false;
  anyMenuOpen.value = showMenus.some(isOpen => isOpen);
};

// 方法：关闭所有菜单
const closeAllMenus = () => {
  showMenus.fill(false);
  anyMenuOpen.value = false;
};

let globalClickHandler = (event) => {
  if (!event.target.closest('.menuToggle') && !event.target.closest('.context-menu')) {
    closeAllMenus();
  }
};

onMounted(() => {
  document.addEventListener('click', globalClickHandler);
});

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', globalClickHandler);
});
//
//搜索框
const initOs = [OS.OPEN_EULER, OS.CENTOS]
const initArch = [CpuArch.X86_64, CpuArch.AARCH64]
const initVersion = [OpenGaussVersionEnum.MINIMAL_LIST, OpenGaussVersionEnum.LITE, OpenGaussVersionEnum.ENTERPRISE]
const init = () => {
  fetchOs()
  fetchArch()
  fetchVersion()
  fetchVersionNum()
  getListData()
}
const fetchOs = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'os') {
      initOs.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
const fetchArch = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'cpuArch') {
      initArch.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
const fetchVersion = () => {
  parentTags.value.forEach(tag => {
    if (tag.name === 'openGaussVersion') {
      initVersion.forEach(child => {
        const newChild = {
          name: child,
          disabled: false
        }
        tag.children.push(newChild)
      })
    }
  })
}
let countVersionNum = 0
const fetchVersionNum = () => {
  getVersionNum().then(res => {
    if (Number(res.code) === 200) {
      parentTags.value.forEach(tag => {
        if (tag.name === 'openGaussVersionNum') {
          res.data.forEach(child => {
            const newChild = {
              name: child,
              disabled: false
            }
            tag.children.push(newChild)
            countVersionNum = countVersionNum + 1
          })
        }
      })
    }
  }) .catch(error => {
    console.error(error)
  })
}

const parentTags = ref([
  { name: 'os',
    value:'操作系统',
    children: [],
    disabled: false
  },
  { name: 'cpuArch',
    value:'cpu架构',
    children: [],
    disabled: false
  },
  { name: 'openGaussVersion',
    value:'openGauss版本',
    children: [],
    disabled: false
  },
  { name: 'openGaussVersionNum',
    value:'版本号',
    children: [],
    disabled: false
  },
])
const selectedOptionsValue = reactive({value:''})
const searchFormData = new FormData
const searchTag = (inputValue: any) => {
  const entries = searchFormData.entries()
  for (const [key, value] of entries) {
    searchFormData.delete(key)
  }
  const selectedTagGroups =  ref<string[]>([])
  const alertFlag = new Map()
  alertFlag.set('os', 2)
  alertFlag.set('cpuArch', 2)
  alertFlag.set('openGaussVersion', 3)
  alertFlag.set('openGaussVersionNum', countVersionNum)
  const alertNum = new Map([...alertFlag])
  list.os = {}
  list.cpuArch = {}
  list.openGaussVersion = {}
  list.openGaussVersionNum = {}
  selectedOptionsValue.value = inputValue
  const osFlag = ref(0)
  const cpuArchFlag = ref(0)
  const openGaussVersionFlag = ref(0)
  const openGaussVersionNumFlag = ref(0)
  inputValue.forEach(item => {
      parentTags.value.forEach(tag => {
        if (tag.children.some(piece => piece.name.includes(item))){
          let tempname = alertFlag.get(tag.name) - 1
          console.log(tempname)
          alertFlag.set(tag.name, tempname)
          console.log(tag.name, item)
          console.log(tag.children)
          if (tag.name === 'os') {
            osFlag.value += 1
            list.os = {"os":item}
          }
          if (tag.name === 'cpuArch') {
            cpuArchFlag.value += 1;
            list.cpuArch = {"cpuArch":item}
          }
          if (tag.name === 'openGaussVersion') {
            openGaussVersionFlag.value += 1;
            list.openGaussVersion = {"openGaussVersion":item}
          }
          if (tag.name === 'openGaussVersionNum') {
            openGaussVersionNumFlag.value += 1;
            list.openGaussVersionNum = {"openGaussVersionNum":item}
          }
          if(osFlag.value === alertNum.get("os")) {list.os = {"os":null}}
          if(cpuArchFlag.value === alertNum.get("cpuArch")) {list.cpuArch = {"cpuArch":null}}
          if(openGaussVersionFlag.value === alertNum.get("openGaussVersion")) {list.openGaussVersion = {"openGaussVersion":null}}
          if(openGaussVersionNumFlag.value === alertNum.get("openGaussVersionNum")) {list.openGaussVersionNum = {"openGaussVersionNum":null}}
          searchFormData.append(tag.name, item)
          tag.disabled = true
          selectedTagGroups.value.push(tag.name)
        }
      })
    })
  parentTags.value.forEach(tag => {
    if (alertFlag.get(tag.name)  == 0) {
      Message.error(t('operation.DailyOps.sl3u5s5cf250') + tag.name + t('operation.DailyOps.sl3u5s5cf251'))
      let tempnum = alertNum.get(tag.name)
      selectedOptionsValue.value.splice( -tempnum)
    }
    if (selectedTagGroups.value.includes(tag.name)) {
      tag.disabled = false
      tag.children.forEach(item =>{
        if (item.name !== searchFormData.get(tag.name)){
          item.disabled = true
        }
      })
    } else {
      tag.disabled = false
      tag.children.forEach(item => item.disabled = false)
    }
  })
  if(osFlag.value === alertNum.get("os")){
    parentTags.value.forEach(tag => {
      if (tag.name === 'os') {
        tag.disabled = false
        tag.children.forEach(item =>{
          item.disabled = false
        })
      }
    })
  }
  if(cpuArchFlag.value === alertNum.get("cpuArch")){
    parentTags.value.forEach(tag => {
      if (tag.name === 'cpuArch') {
        tag.disabled = false
        tag.children.forEach(item =>{
          item.disabled = false
        })
      }
    })
  }
  if(openGaussVersionFlag.value === alertNum.get("openGaussVersion")){
    parentTags.value.forEach(tag => {
      if (tag.name === 'openGaussVersion') {
        tag.disabled = false
        tag.children.forEach(item =>{
          item.disabled = false
        })
      }
    })
  }
  if(openGaussVersionNumFlag.value === alertNum.get("openGaussVersionNum")){
    parentTags.value.forEach(tag => {
      if (tag.name === 'openGaussVersionNum') {
        tag.disabled = false
        tag.children.forEach(item =>{
          item.disabled = false
        })
      }
    })
  }
  getListData()
}
//
//复制
const copy = (clusterId: KeyValue) => {
  copyTask(clusterId)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        getListData();
      }
    }).catch(error => {
    console.error("copyTask error:"+error);
  });
}

//删除单行
const singleRowDelete = (record: KeyValue) => {
  batchDeleteTask([record.clusterId])
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        Message.success("delete" + record.clusterId + "success!")
      }
    }).catch(error => {
    console.error("DeleteTask error:"+error);
  }).finally(() => {
    if (list.status.status !== 'DRAFT') {
      getListData();
    } else {
      getDraftListData();
    }
  })
}
//日志下载
const logDownload = (record: KeyValue) => {
  console.log('logDownload');
  console.log(record.clusterId);
  clusterLogDownload(record.clusterId)
    .then((res)=>{
      if (res) {
        const blob = new Blob([res]);
        const link = document.createElement("a");
        const URL = window.URL || window.webkitURL;
        let herf = URL.createObjectURL(blob);
        link.href = herf;
        link.download = "OPERATE_LOG_"+record.clusterName+".log";
        link.click();
        window.URL.revokeObjectURL(herf);
      }
    }).catch((err)=>{
    console.log("error:"+err);
  })
}
//选中复选框并填充进一个数组selectedHostIds和selectedData中
const handleSelectedChange = (keys: (string | number)[]) => {
  if (list.status.status !== 'DRAFT') {
    list.selectTaskIds = keys;
    list.selectTaskIds.forEach((clusterId: string | number) => {
      const findOne = list.data.find((item: KeyValue) => { return item.clusterId === clusterId})
      if (!list.selectedPage.some(item => _.isEqual(item, findOne)) && findOne) {
        list.selectedPage.push(findOne)
      }
    })
    list.selectedData = [];
    list.selectTaskIds.forEach((clusterId: string | number) => {
      const findOne = list.selectedPage.find(item => { return item.clusterId === clusterId})
      if (findOne) {
        list.selectedData.push(findOne)
      }
    })
  } else {
    list.selectedDraftTaskIds = keys;
    list.selectedDraftTaskIds.forEach((clusterId: string | number) => {
      const findOne = list.data.find((item: KeyValue) => { return item.clusterId === clusterId})
      if (!list.selectedDraftPage.some(item => _.isEqual(item, findOne)) && findOne) {
        list.selectedDraftPage.push(findOne)
      }
    })
    list.selectedDraftData = [];
    list.selectedDraftTaskIds.forEach((clusterId: string | number) => {
      const findOne = list.selectedDraftPage.find(item => { return item.clusterId === clusterId})
      if (findOne) {
        list.selectedDraftData.push(findOne)
      }
    })
  }

}

//删除多选框选中的多行
const deleteSelectedTaskId = () => {
  if (list.selectedData.length > 0 && list.status.status !== 'DRAFT') {
    batchDeleteTask(list.selectTaskIds)
      .then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          Message.success("success!")
        }
      }).catch(error => {
      Message.error("DeleteTask error:"+error);
    }).finally(() => {
      taskMenu();
    })
  } else if (list.selectedDraftData.length > 0 && list.status.status === 'DRAFT') {
    batchDeleteTask(list.selectedDraftTaskIds)
      .then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          Message.success("success!")
        }
      }).catch(error => {
      Message.error("DeleteTask error:"+error);
    }).finally(() => {
      switchDraft();
    })
  }else {
    Message.warning(t('operation.DailyOps.sl3u5s5cf252'))
  }
};

//批量执行弹窗
const batchExecuteRef = ref<null | InstanceType<typeof taskExecute>>(null)

const batchExecute = () => {
  if (list.selectedData.length > 0) {
    console.log(list.selectTaskIds)
    batchExecuteRef.value?.open(list.selectTaskIds, list.selectedData)
  } else {
    Message.warning(t('operation.DailyOps.sl3u5s5cf253'))
  }
}

onMounted(() => {
  getList()
})

onBeforeUnmount(() => {
  if (data.socketArr.length) {
    data.socketArr.forEach((item: Socket<any, any>) => {
      if (item) {
        item.destroy()
      }
    })
  }
})

const goInstall = () => {
  window.$wujie?.props.methods.jump({
    name: 'Static-pluginBase-opsOpsInstall'
  })
}

const showClusterInfo = computed(() => {
  const normalClusters = data.clusterList.filter((item: KeyValue) => {
    return item.isHidden === false
  })
  return normalClusters.length > 0 && !data.loading
})

const showInit = computed(() => {
  const normalClusters = data.clusterList.filter((item: KeyValue) => {
    return item.isHidden === false
  })
  return normalClusters.length === 0 && !data.loading
})

const searchClusterName = ref('')

const getList = () => new Promise(resolve => {
  data.loading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      data.clusterList = []
      if(data.socketArr.length){
        data.socketArr.forEach((item: Socket<any, any>) => {
        item?.destroy()
      })
      }
      data.socketArr = []
      res.data.forEach((item: KeyValue, index: number) => {
        if(item.clusterId.indexOf(searchClusterName.value) === -1){
          return;
        }
        item.isHidden = false
        item.isShow = true
        item.state = -1
        item.loading = false
        if (item.version === 'MINIMAL_LIST' && item.deployType === 'CLUSTER' && item.clusterNodes.length < 2) {
          const slaveNode = JSON.parse(JSON.stringify(item.clusterNodes[0]))
          slaveNode.clusterRole = ClusterRoleEnum.SLAVE
          item.clusterNodes.push(slaveNode)
        }
        openWebSocket(item, index)
        data.clusterList.push(item)
      })
    } else resolve(false)
  }).finally(() => {
    data.loading = false
  })
})

const getWarningNum = (clusterData: KeyValue) => {
  if (clusterData.checkSummary && Object.keys(clusterData.checkSummary).length) {
    const checkResult = clusterData.checkSummary
    return checkResult.ERROR + checkResult.NG + checkResult.WARNING
  } else {
    return 0
  }
}

const oneCheckRef = ref<null | InstanceType<typeof OneCheck>>(null)
const showOneCheck = (clusterData: KeyValue) => {
  oneCheckRef.value?.open(clusterData.clusterId)
}

const getVersionName = (version: string) => {
  switch (version) {
    case 'MINIMAL_LIST':
      return t('operation.DailyOps.5mplp1xc46o0')
    case 'LITE':
      return t('operation.DailyOps.5mplp1xc4b40')
    default:
      return t('operation.DailyOps.5mplp1xc4fg0')
  }
}

const getRoleName = (type: ClusterRoleEnum) => {
  switch (type) {
    case ClusterRoleEnum.MASTER:
      return t('operation.DailyOps.5mplp1xc4jw0')
    case ClusterRoleEnum.SLAVE:
      return t('operation.DailyOps.5mplp1xbztc0')
    case ClusterRoleEnum.CASCADE:
      return t('operation.DailyOps.5mplp1xc4oc0')
    case ClusterRoleEnum.PENDING:
      return t('operation.DailyOps.else7')
    case ClusterRoleEnum.UNKNOWN:
      return t('operation.DailyOps.else8')
    case ClusterRoleEnum.DOWN:
      return t('operation.DailyOps.else9')
    case ClusterRoleEnum.ABNORMAL:
      return t('operation.DailyOps.else10')
  }
}

// open webSocket
const openWebSocket = (data: KeyValue, clusterIndex: number) => {
  if (data.version === 'MINIMAL_LIST' && data.deployType === 'CLUSTER') {
    // mini cluster
    // data.clusterNodes[0].state = -1
    // openHostWebSocket(data, data.clusterNodes[0], clusterIndex, 0)
    data.clusterNodes.forEach((item: KeyValue, index: number) => {
      item.state = 'false'
      item.nodeState = -1
      item.cmState = ''
      // open websocket
      openHostWebSocket(data, item, clusterIndex, index, item.clusterRole)
    })
  } else {
    data.clusterNodes.forEach((item: KeyValue, index: number) => {
      item.state = 'false'
      item.nodeState = -1
      item.cmState = ''
      // open websocket
      openHostWebSocket(data, item, clusterIndex, index)
    })
  }
}

const openHostWebSocket = (clusterData: KeyValue, nodeData: KeyValue, clusterIndex: number, index: number, clusterRole: ClusterRoleEnum = ClusterRoleEnum.MASTER) => {
  const socketKey = new Date().getTime()
  const param = {
    clusterId: clusterData.clusterId,
    hostId: nodeData.hostId,
    privateIp: nodeData.privateIp,
    role: clusterRole,
    businessId: 'monitor_ops_' + clusterData.clusterId + '_' + nodeData.hostId + '_' + clusterRole + '_' + socketKey
  }
  const websocket = new Socket({ url: `${param.businessId}` })
  websocket.onopen(() => {
    // clusterIndex is not reliable
  const currentCluster = data.clusterList.filter(item => item.clusterId === clusterData.clusterId)[0]
    clusterMonitor(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        currentCluster.clusterNodes[index].state = 'false'
        if (clusterData.version !== OpenGaussVersionEnum.ENTERPRISE) {
          currentCluster.clusterNodes[index].nodeState = 'false'
        }
        websocket.destroy()
      } else {
        // websocket push socketArr
        data.socketArr.push(websocket)
      }
    }).catch(() => {
      currentCluster.clusterNodes[index].state = 'false'
      if (clusterData.version !== OpenGaussVersionEnum.ENTERPRISE) {
        currentCluster.clusterNodes[index].nodeState = 'false'
      }
      websocket.destroy()
    })
  })
  websocket.onclose(() => {
  const currentCluster = data.clusterList.filter(item => item.clusterId === clusterData.clusterId)[0]
    if (currentCluster) {
      currentCluster.clusterNodes[index].state = 'false'
      if (clusterData.version !== OpenGaussVersionEnum.ENTERPRISE) {
        currentCluster.clusterNodes[index].nodeState = 'false'
      }
    }
  })
  websocket.onmessage((messageData: any) => {
    const currentCluster = data.clusterList.filter(item => item.clusterId === clusterData.clusterId)[0]
    if (currentCluster && !currentCluster.loading) {
      const eventData = JSON.parse(messageData)
      currentCluster.clusterNodes[index].nodeState = eventData.state
      // reset instance nodeState and nodeRole
      setInstanceState(currentCluster, currentCluster.clusterNodes[index])
      currentCluster.clusterNodes[index].cpu = eventData.cpu
      currentCluster.clusterNodes[index].memory = eventData.memory
      currentCluster.clusterNodes[index].lock = eventData.lock
      currentCluster.clusterNodes[index].session = eventData.session
      currentCluster.clusterNodes[index].connectNum = eventData.connectNum
      currentCluster.clusterNodes[index].kernel = eventData.kernel
      if (eventData.memorySize) {
        if (!isNaN(Number(eventData.memorySize))) {
          currentCluster.clusterNodes[index].memorySize = (Number(eventData.memorySize) / 1024 / 1024).toFixed(2)
        }
      }
      // if (clusterData.version === 'MINIMAL_LIST' && clusterData.deployType === 'CLUSTER') {
      //   // if cluster is minimal and cluster, node default is first
      //   Object.assign(data.clusterList[clusterIndex].clusterNodes[index + 1], data.clusterList[clusterIndex].clusterNodes[index])
      //   data.clusterList[clusterIndex].clusterNodes[index + 1].clusterRole = ClusterRoleEnum.SLAVE
      // }
    }
  })
}

const setInstanceState = (clusterData: KeyValue, instanceData: KeyValue) => {
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    if (instanceData.nodeState && instanceData.nodeState !== -1) {
      const stateObj = JSON.parse(instanceData.nodeState)
      const currentHostname = instanceData.hostname
      if (stateObj && currentHostname) {
        // set node switch state
        if (stateObj.nodeState[currentHostname] === 'Normal') {
          instanceData.state = 'true'
        } else {
          instanceData.state = 'false'
        }
        // set all nodes state
        clusterData.clusterNodes.forEach((item: KeyValue) => {
          item.nodeState = instanceData.nodeState
        })
        // set node cm_state
        instanceData.cmState = stateObj.cmState[currentHostname]
        // set node role
        if (stateObj.nodeRole[currentHostname]) {
          switch (stateObj.nodeRole[currentHostname]) {
            case 'Primary':
            case 'Normal':
              instanceData.clusterRole = ClusterRoleEnum.MASTER
              break
            case 'Standby':
              instanceData.clusterRole = ClusterRoleEnum.SLAVE
              break
            case 'Cascade Standby':
              instanceData.clusterRole = ClusterRoleEnum.CASCADE
              break
            case 'Pending':
              instanceData.clusterRole = ClusterRoleEnum.PENDING
              break
            case 'Unknown':
              instanceData.clusterRole = ClusterRoleEnum.UNKNOWN
              break
            case 'Down':
              instanceData.clusterRole = ClusterRoleEnum.DOWN
              break
            case 'Abnormal':
              instanceData.clusterRole = ClusterRoleEnum.ABNORMAL
              break
          }
        }
      }
    }
  } else {
    instanceData.state = instanceData.nodeState
  }
}

const nodeTemp = reactive<KeyValue>({
  clusterData: {},
  index: 0,
  force: false
})
const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const handleUninstallBefore = (clusterData: KeyValue, index: number, force: boolean) => {
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    const result: KeyValue[] = []
    clusterData.clusterNodes.forEach((item: KeyValue) => {
      if (!item.isRemember) {
        const temp = {
          privateIp: item.privateIp,
          publicIp: item.publicIp,
          hostId: item.hostId,
          rootPassword: '',
          isRemember: item.isRemember
        }
        result.push(temp)
      }
    })
    if (result.length) {
      nodeTemp.clusterData = clusterData
      nodeTemp.index = index
      nodeTemp.force = force
      hostPwdRef.value?.open(result)
    } else {
      handleUninstall(clusterData, index, force)
    }
  } else {
    handleUninstall(clusterData, index, force)
  }
}

const handleAllNodesPwd = (rootPasswords: KeyValue) => {
  handleUninstall(nodeTemp.clusterData, nodeTemp.index, nodeTemp.force, rootPasswords)
}

const handleUninstall = (clusterData: KeyValue, index: number, force: boolean, rootPasswords?: KeyValue) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `uninstall_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `uninstall_${clusterData.clusterId}_${socketKey}`,
      force: force,
      rootPasswords: rootPasswords
    }
    uninstallOpenGauss(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'uninstall')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      if (force) {
        data.clusterList[index].isHidden = true
      } else {
        const flag = Number(messageData.split(':')[1])
        if (flag === 0) {
          data.clusterList[index].isHidden = true

        }
      }
      clusterData.loading = false
      webSocket.destroy()
    }
  })
}

const handleDelCluster = (clusterData: KeyValue, index: number) => {
  clusterData.loading = true
  delCluster(clusterData.clusterId).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.clusterList[index].isHidden = true
    }
  }).finally(() => {
    clusterData.loading = false
  })
}

const handleEnable = (clusterData: KeyValue, index: number) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `start_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `start_${clusterData.clusterId}_${socketKey}`
    }
    start(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'start')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('enable success')
        openWebSocket(clusterData, index)
      } else {
        term.writeln('enable failed')
      }
    }
  })
}

const handleStop = (clusterData: KeyValue) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `stop_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `stop_${clusterData.clusterId}_${socketKey}`
    }
    stop(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'stop')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        // if enterprise set nodes cm_state Down
        stopSuccessBefore(clusterData)
        term.writeln('stop success')
      } else {
        term.writeln('stop failed')
      }
    }
  })
}

const stopSuccessBefore = (clusterData: KeyValue) => {
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    const tempNodeState: {
      nodeState: KeyValue,
      cluster_state: string
    } = {
      nodeState: {},
      cluster_state: 'Unavailable'
    }
    clusterData.clusterNodes.forEach((item: KeyValue) => {
      item.cmState = CMStateEnum.Down
      tempNodeState.nodeState[item.hostname] = 'Manually stopped'
      item.nodeState = JSON.stringify(tempNodeState)
    })
  }
}

const handleReset = (clusterData: KeyValue, index: number) => {
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `restart_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `restart_${clusterData.clusterId}_${socketKey}`
    }
    restart(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'restart')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('reset success')
        openWebSocket(clusterData, index)
      } else {
        term.writeln('reset failed')
      }
    }
  })
}

const backupRef = ref<null | InstanceType<typeof ClusterBackupDlg>>(null)
const backupClusterIndex = ref()
const handleBackupDlg = (clusterIndex: number) => {
  backupClusterIndex.value = clusterIndex
  backupRef.value?.open(data.clusterList[clusterIndex].clusterId,
    data.clusterList[clusterIndex].clusterNodes[0].installUserName)
}

const handleBackup = (backupData: KeyValue) => {
  const clusterData = data.clusterList[backupClusterIndex.value]
  clusterData.loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `backup_${clusterData.clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: clusterData.clusterId,
      backupPath: backupData.backupPath,
      remark: backupData.remark,
      wsConnectType: 'COMMAND_EXEC',
      businessId: `backup_${clusterData.clusterId}_${socketKey}`
    }
    clusterBackup(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        webSocket.destroy()
      } else {
        createXterm(clusterData.clusterId)
        createWinbox(clusterData, 'backup')
        initTerm(term, webSocket, clusterData.clusterId)
      }
    }).catch(() => {
      clusterData.loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      clusterData.loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln('backup success')
      } else {
        term.writeln('backup failed')
      }
    }
  })
}

const createXterm = (idName: string) => {
  const div = document.createElement('div')
  const divId = document.createAttribute('id')
  divId.value = idName
  div.setAttributeNode(divId)
  const divClass = document.createAttribute('class')
  divClass.value = 'xterm'
  div.setAttributeNode(divClass)
  const styleClass = document.createAttribute('style')
  div.setAttributeNode(styleClass)
  div.style.marginTop = '35px'
}

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 40,
    cols: 200,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

const initTerm = (term: Terminal, socket: Socket<any, any> | null, xtermId: string) => {
  if (socket) {
    const attachAddon = new AttachAddon(socket.ws)
    const fitAddon = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById(xtermId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
  } else {
    const fitAddon = new FitAddon()
    term.loadAddon(fitAddon)
    term.open(document.getElementById(xtermId) as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
  }
}

const createWinbox = (clusterData: KeyValue, type?: string) => {
  const createWindow = useWinBox()
  createWindow({
    id: clusterData.clusterId,
    title: clusterData.clusterId + '_' + type,
    mount: document.getElementById(clusterData.clusterId),
    class: ['custom-winbox', 'no-full', 'no-max'],
    background: '#1D2129',
    x: 'center',
    y: 'center',
    width: '50%',
    height: '50%',
    onClose: function () {
      clusterData.loading = false
    },
    onminimize: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox', 'custom-winbox-mini')
        this.window?.setAttribute('class', newClass)
      }
    },
    onrestore: function () {
      const oldClass = this.window?.getAttribute('class')
      if (oldClass) {
        const newClass = oldClass.replace('custom-winbox-mini', 'custom-winbox')
        this.window?.setAttribute('class', newClass)
      }
    }
  })
}

// host oper
const handleHostOper = (type: any, clusterIndex: number, nodeIndex: number) => {
  handleInstanceOper(type, clusterIndex, nodeIndex)
}

// instance start or stop
const handleInstanceSwitchChange = (type: any, clusterIndex: number, nodeIndex: number) => {
  let operType = 'start'
  if (type === 'false') {
    operType = 'stop'
  }
  handleInstanceOper(operType, clusterIndex, nodeIndex, true)
}

// instance oper
const handleInstanceOper = (type: any, clusterIndex: number, nodeIndex: number, isSwitch = false) => {
  data.clusterList[clusterIndex].loading = true
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const webSocket = new Socket({ url: `${type}_instance_${data.clusterList[clusterIndex].clusterId}_${socketKey}` })
  webSocket.onopen(() => {
    const param = {
      clusterId: data.clusterList[clusterIndex].clusterId,
      nodeIds: [data.clusterList[clusterIndex].clusterNodes[nodeIndex].nodeId],
      hostId: data.clusterList[clusterIndex].clusterNodes[nodeIndex].hostId,
      role: data.clusterList[clusterIndex].clusterNodes[nodeIndex].clusterRole,
      businessId: `${type}_instance_${data.clusterList[clusterIndex].clusterId}_${socketKey}`
    }
    const method = getInstanceMethod(type, param)
    method?.then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        // Restore the original status if the stop/start fails
        if (isSwitch) {
          if (type === 'start') {
            data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
          } else if (type === 'stop') {
            data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
          }
        }
        webSocket.destroy()
      } else {
        createXterm(data.clusterList[clusterIndex].clusterId)
        createWinbox(data.clusterList[clusterIndex], type)
        initTerm(term, webSocket, data.clusterList[clusterIndex].clusterId)
      }
    }).catch(() => {
      if (isSwitch) {
        if (type === 'start') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
        } else if (type === 'stop') {
          data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
        }
      }
      data.clusterList[clusterIndex].loading = false
      webSocket.destroy()
    })
  })
  webSocket.onmessage((messageData: any) => {
    if (messageData.indexOf('FINAL_EXECUTE_EXIT_CODE') > -1) {
      data.clusterList[clusterIndex].loading = false
      webSocket.destroy()
      // if success open Socket
      const flag = Number(messageData.split(':')[1])
      if (flag === 0) {
        // success
        term.writeln(type + ' success')
        if (type === 'restart' || type === 'start' || type === 'build') {
          openHostWebSocket(data.clusterList[clusterIndex], data.clusterList[clusterIndex].clusterNodes[nodeIndex], clusterIndex, nodeIndex, data.clusterList[clusterIndex].clusterNodes[nodeIndex].clusterRole)
        }
      } else {
        if (isSwitch) {
          if (type === 'start') {
            data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'false'
          } else if (type === 'stop') {
            data.clusterList[clusterIndex].clusterNodes[nodeIndex].state = 'true'
          }
        }
        term.writeln(type + ' failed')
      }
    }
  })
}

const getInstanceMethod = (type: string, param: KeyValue) => {
  const reqParam = {
    clusterId: param.clusterId,
    nodeIds: param.nodeIds,
    role: param.role,
    businessId: param.businessId
  }
  const enterpriseParam = {
    clusterId: param.clusterId,
    hostId: param.hostId,
    businessId: param.businessId
  }
  switch (type) {
    case 'start':
      return start(reqParam)
    case 'stop':
      return stop(reqParam)
    case 'restart':
      return restart(reqParam)
    case 'switch':
      return switchover(enterpriseParam)
    case 'config':
      return generateconf(enterpriseParam)
    case 'build':
      return build(enterpriseParam)
  }
}

const getClusterState = (clusterData: KeyValue) => {
  // all nodes is ok, cluster is ok
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    if (clusterData.clusterNodes[0].nodeState) {
      const stateObj = JSON.parse(clusterData.clusterNodes[0].nodeState)
      return enterpriseClusterState(stateObj)
    }
  } else if (clusterData.clusterNodes.length) {
    const findOkNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
      return item.nodeState === 'true'
    })
    if (findOkNodes.length) {
      return t('operation.DailyOps.5mplp1xbz600')
    } else {
      const findHasCheckNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
        return item.nodeState !== -1
      })
      if (findHasCheckNodes.length) {
        return t('operation.DailyOps.5mplp1xc4x80')
      } else {
        return t('operation.DailyOps.5mplp1xc51s0')
      }
    }
  }
  return t('operation.DailyOps.5mplp1xc51s0')
}

const getClusterColor = (clusterData: KeyValue) => {
  // all nodes is ok, cluster is ok
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    if (clusterData.clusterNodes[0].nodeState) {
      const stateObj = JSON.parse(clusterData.clusterNodes[0].nodeState)
      return enterpriseClusterStateColor(stateObj)
    }
  } else if (clusterData.clusterNodes.length) {
    const findOkNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
      return item.nodeState === 'true'
    })
    if (findOkNodes.length) {
      return 'green'
    } else {
      const findHasCheckNodes = clusterData.clusterNodes.filter((item: KeyValue) => {
        return item.nodeState !== -1
      })
      if (findHasCheckNodes.length) {
        return 'red'
      } else {
        return 'gray'
      }
    }
  }
  return 'gray'
}

const enterpriseClusterState = (stateObj: KeyValue) => {
  switch (stateObj.cluster_state) {
    case 'Normal':
      return t('operation.DailyOps.5mplp1xbz600')
    case 'Unavailable':
      return t('operation.DailyOps.5mplp1xc4x80')
    case 'Degraded':
      return t('operation.DailyOps.nodeField1')
    default:
      return t('operation.DailyOps.5mplp1xc51s0')
  }
}

const enterpriseClusterStateColor = (stateObj: KeyValue) => {
  switch (stateObj.cluster_state) {
    case 'Normal':
      return 'green'
    case 'Unavailable':
    case 'Degraded':
      return 'red'
    default:
      return 'gray'
  }
}

const getInstanceState = (clusterData: KeyValue, instanceData: KeyValue) => {
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    if (instanceData.nodeState === -1) {
      return t('operation.DailyOps.5mplp1xc51s0')
    } else if (instanceData.nodeState && instanceData.nodeState !== 'false') {
      const stateObj = JSON.parse(instanceData.nodeState)
      return getCurrentInstanceState(stateObj, instanceData)
    } else {
      return t('operation.DailyOps.5mplp1xc4x80')
    }
  } else {
    if (instanceData.nodeState === -1) {
      return t('operation.DailyOps.5mplp1xc51s0')
    } else {
      if (instanceData.nodeState === 'true') {
        return t('operation.DailyOps.5mplp1xbz600')
      } else {
        return t('operation.DailyOps.5mplp1xc4x80')
      }
    }
  }
}
const getCurrentInstanceState = (stateObj: KeyValue, instanceData: KeyValue) => {
  const currentHostname = instanceData.hostname
  if (stateObj && currentHostname) {
    switch (stateObj.nodeState[currentHostname]) {
      case 'Normal':
        return t('operation.DailyOps.nodeState1')
      case 'Need repair(Disconnected)':
        return t('operation.DailyOps.nodeState2')
      case 'Starting':
        return t('operation.DailyOps.nodeState3')
      case 'Wait promoting':
        return t('operation.DailyOps.nodeState4')
      case 'Promoting':
        return t('operation.DailyOps.nodeState5')
      case 'Demoting':
        return t('operation.DailyOps.nodeState6')
      case 'Building':
        return t('operation.DailyOps.nodeState7')
      case 'Catchup':
        return t('operation.DailyOps.nodeState8')
      case 'Coredump':
        return t('operation.DailyOps.nodeState9')
      case 'Unknown':
        return t('operation.DailyOps.nodeState10')
      case 'Manually stopped':
        return t('operation.DailyOps.else11')
    }
  }
  return t('operation.DailyOps.5mplp1xc51s0')
}

const getInstanceStateColor = (clusterData: KeyValue, instanceData: any) => {
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE) {
    if (instanceData.nodeState === -1) {
      return 'gray'
    } else if (instanceData.nodeState && instanceData.nodeState !== 'false') {
      return 'green'
    } else {
      return 'red'
    }
  } else {
    if (instanceData.nodeState === -1) {
      return 'gray'
    } else {
      if (instanceData.nodeState === 'true') {
        return 'green'
      } else {
        return 'red'
      }
    }
  }
}

const getDropdownList = (clusterData: KeyValue, nodeData: KeyValue) => {
  const result = [
    { label: t('operation.DailyOps.5mplp1xc0c00'), value: 'start' },
    { label: t('operation.DailyOps.5mplp1xc0i80'), value: 'stop' },
    { label: t('operation.DailyOps.5mplp1xc0o40'), value: 'restart' }
  ]
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE && nodeData.clusterRole === ClusterRoleEnum.MASTER) {
    result.push({ label: t('operation.DailyOps.5mplp1xc5cg0'), value: 'config' })
  }
  if (clusterData.version === OpenGaussVersionEnum.ENTERPRISE && nodeData.clusterRole === ClusterRoleEnum.SLAVE) {
    result.push({ label: t('operation.DailyOps.5mplp1xc56k0'), value: 'switch' })
    result.push({ label: t('operation.DailyOps.5mplp1xc5cg0'), value: 'config' })
    result.push({ label: t('operation.DailyOps.nodeBuild'), value: 'build' })
  }
  return result
}


const gucSettingRef = ref<null | InstanceType<typeof GucSettingDrawer>>(null)
const handleOpenGucSetting = (clusterData: KeyValue, instance: KeyValue, index: number) => {
  gucSettingRef.value?.open(clusterData, instance, index)
}
const handleGucSettingComplete = (clusterData: KeyValue, clusterIndex: number) => {
  openWebSocket(clusterData, clusterIndex)
}

</script>

<style lang="less" scoped>
.main-color {
  color: rgb(var(--primary-6));
}

.daily-ops-c {
  padding: 20px 20px 0px;
  overflow-y: auto;
  height: calc(100vh - 130px);
  .search-cluster{
    width:25%;
    margin-left:20px;
  }
  .empty-icon-size {
    width: 100px;
    height: 100px;
  }

  .empty-content {
    font-weight: bold;
    color: var(--color-neutral-4);
  }
  .mainTitle {
    font-size: 20px;
    color: var(--color-neutral-10)
  }
  .item-c {
    padding: 20px;
    border-radius: 8px;
    // background-color: white;

    .item-cluster-info {
      border: 1px solid #f2f3f5;
      // background-color: #f8f9fa;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px;

      @media screen and (max-width: 1700px) {
        .cluster-info-screen {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
        }

        .mb-screen {
          margin-bottom: 10px;
        }
      }

      @media screen and (min-width: 1700px) {
        .cluster-info-screen {
          display: flex;
          align-items: center;
        }

        .mb-screen {
          margin-bottom: 20px;
        }
      }

    }

    .host-c {
      padding: 20px;
      border: 1px solid #f2f3f5;
      border-right: 0px;

      @media screen and (max-width: 1700px) {
        .host-info-screen {
          display: flex;
          flex-direction: column;
          align-items: flex-start
        }

        .path-c {}

        .host-mb-screen {
          margin-bottom: 10px;
        }
      }

      @media screen and (min-width: 1700px) {
        .host-info-screen {
          display: flex;
          align-items: flex-start
        }

        .path-c {
          line-height: 25px;
          width: 150px;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .host-mb-screen {
          margin-bottom: 20px;
        }
      }

      .host-info {
        display: flex;
        align-items: flex-start
      }

    }

    .instance-c {
      padding: 20px;
      border: 1px dashed #00b42a;
      position: relative;

      .node-type {
        position: absolute;
        top: 0;
        left: 0;
        border: 24px solid;
        border-color: #00b42a transparent transparent #00b42a;

        .content {
          color: white;
          margin-top: -18px;
          margin-left: -18px;
        }
      }

      @media screen and (max-width: 1500px) {
        .instance-info-screen {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
        }
      }

      @media screen and (min-width: 1500px) {
        .instance-info-screen {
          display: flex;
          align-items: center;
        }
      }

    }

    .open-close-c {
      cursor: pointer;
    }

    .simple-w {
      width: 33.3%;
    }

    .node-w {
      width: 50%;
    }

    .item-node-info {
      display: flex;
    }
  }

  .cluster-icon-size {
    width: 100px;
    height: 100px;
  }

  .host-icon-size {
    height: 80px;
    width: 80px;
  }
}

.xterm {
  width: 100%;
  height: 80%;
}

.echart {
  float: left;
  width: 100%;
  height: 400px;
  box-sizing: border-box; /* 确保 padding 和 border 不会增加额外宽度 */
}
.barDiv{
  margin-right: 20px;
  span {
    color: var(--color-neutral-10)
  }
}


.my-button{
  border: 1px solid rgb(206, 206, 206);
}
.my-button:hover{
  color:white;
  background-color: rgb(51 112 232);
}
.blueColor {
  background-color: #0077ff;
  color: white;
}
.more{
  color: #3291fe;
}
.my-link {
  height: 100%;
  padding: 0px 20px;
  cursor: pointer;
  color: var(--color-neutral-10);
}

</style>
