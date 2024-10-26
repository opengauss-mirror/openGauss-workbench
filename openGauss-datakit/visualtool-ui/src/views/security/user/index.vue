<template>
  <div class="app-container" id="userManage">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="userName" :label="$t('user.index.5m6ni29gbi40')">
            <a-input v-model="form.userName" allow-clear :placeholder="$t('user.index.5m6ni29gdag0')" style="width: 180px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item field="phonenumber" :label="$t('user.index.5m6ni29gdhw0')">
            <a-input v-model="form.phonenumber" allow-clear :placeholder="$t('user.index.5m6ni29gdog0')" style="width: 180px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item field="status" :label="$t('user.index.5m6ni29gdrk0')">
            <a-select v-model="form.status" allow-clear :placeholder="$t('user.index.5m6ni29gdus0')" style="width: 150px;" @change="getList(true)">
              <a-option value="0">{{$t('user.index.5m6ni29gdxs0')}}</a-option>
              <a-option value="1">{{$t('user.index.5m6ni29ge1s0')}}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="outline" @click="getList(true)">
              <template #icon>
                <icon-search />
              </template>
              <template #default>{{$t('user.index.5m6ni29gglk0')}}</template>
            </a-button>
            <a-button style="margin-left: 10px;" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{$t('user.index.5m6ni29ggtw0')}}</template>
            </a-button>
          </a-form-item>
        </a-form>
        <div class="btn-con">
          <a-button type="primary" @click="addUser">
            <template #icon>
              <icon-plus />
            </template>
            <template #default>{{$t('user.index.5m6ni29ggx40')}}</template>
          </a-button>
        </div>
      </div>
      <div class="table-con">
        <a-table :data="tableData" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
          <template #columns>
            <a-table-column :title="$t('user.index.5m6ni29ghe80')" data-index="userName"></a-table-column>
            <a-table-column :title="$t('user.index.5m6ni29ghlc0')" data-index="nickName" align="center">
              <template #cell="{ record }">
                {{ record.userId !== 1 ? record.nickName : $t('user.index.5obuyt8kr2o0') }}
              </template>
            </a-table-column>
            <a-table-column :title="$t('user.index.5m6ni29gdhw0')" data-index="phonenumber" align="center"></a-table-column>
            <a-table-column :title="$t('user.index.5msnsk9mm9k0')" data-index="roleName" align="center">
              <template #cell="{ record }">
                {{ record.userId !== 1 ? record.roleName : $t('user.index.5obuyt8kr2o0') }}
              </template>
            </a-table-column>
            <a-table-column :title="$t('user.index.5m6ni29gdrk0')" data-index="status" align="center">
              <template #cell="{ record }">
                <div class="cell-con">
                  <a-switch v-model="record.status" :disabled="record.userId === 1" checked-color="#00B42A" checked-value="0" unchecked-value="1" size="small" @change="statusChange(record)" />
                  <span class="txt">{{record.status === '0' ? $t('user.index.5m6ni29gdxs0') : $t('user.index.5m6ni29ge1s0')}}</span>
                </div>
              </template>
            </a-table-column>
            <a-table-column :title="$t('user.index.5m6ni29ghrk0')" data-index="createTime" align="center"></a-table-column>
            <a-table-column :title="$t('user.index.5m6ni29ghug0')" align="center" :width="250">
              <template #cell="{ record }">
                <a-button
                  v-if="record.userId !== 1"
                  size="mini"
                  type="text"
                  @click="editUserInfo(record)"
                >
                  <template #icon>
                    <icon-edit />
                  </template>
                  <template #default>{{$t('user.index.5m6ni29h4i80')}}</template>
                </a-button>
                <a-popconfirm v-if="record.userId !== 1" :content="$t('user.index.5m6ni29h4yc0')" @ok="deleteUser(record)">
                  <a-button
                    size="mini"
                    type="text"
                  >
                    <template #icon>
                      <icon-delete />
                    </template>
                    <template #default>{{$t('user.index.5m6ni29h7pc0')}}</template>
                  </a-button>
                </a-popconfirm>
                <a-button
                  v-if="record.userId !== 1"
                  size="mini"
                  type="text"
                  @click="editUserCode(record)"
                >
                  <template #icon>
                    <icon-refresh />
                  </template>
                  <template #default>{{$t('user.index.5m6ni29h80k0')}}</template>
                </a-button>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </div>
    </div>

    <!-- add or edit -->
    <edit-user v-model:open="editUserVisible" :options="currentEditUser" @ok="getList" />

    <!-- reset -->
    <edit-code v-model:open="editCodeVisible" :options="currentEditUser" />
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { getUserList, userDelete, changeStatus } from '@/api/user'
  import EditUser from './components/EditUser.vue'
  import EditCode from './components/EditCode.vue'

  const form = reactive({
    userName: undefined,
    phonenumber: undefined,
    status: undefined
  })

  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10
  })
  const pagination = reactive({
    total: 0,
    current: 1,
    pageSize: 10
  })
  const tableData = ref([])
  const editUserVisible = ref<boolean>(false)
  const currentEditUser = ref({})
  const editCodeVisible = ref<boolean>(false)

  const getList = (fresh?: any) => {
    if (fresh) {
      queryParams.pageNum = 1
      pagination.current = 1
    }
    getUserList({
      ...queryParams,
      ...form
    }).then((res: any) => {
      tableData.value = res.rows
      pagination.total = res.total
    })
  }

  const resetQuery = () => {
    queryParams.pageNum = 1
    queryParams.pageSize = 10
    pagination.current = 1
    form.userName = undefined
    form.phonenumber = undefined
    form.status = undefined
    getList()
  }

  const pageChange = (current: number) => {
    queryParams.pageNum = current
    pagination.current = current
    getList()
  }

  const addUser = () => {
    currentEditUser.value = {}
    editUserVisible.value = true
  }

  const editUserInfo = (value: any) => {
    currentEditUser.value = value
    editUserVisible.value = true
  }

  const deleteUser = (value: any) => {
    userDelete(value.userId).then(() => {
      Message.success('Delete success')
      getList()
    })
  }

  const statusChange = (value: any) => {
    changeStatus({
      userId: value.userId,
      status: value.status
    }).then(() => {
      getList()
    })
  }

  const editUserCode = (value: any) => {
    currentEditUser.value = value
    editCodeVisible.value = true
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .search-con {
      padding: 16px 0 8px;
      margin: 0 20px;
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid var(--color-border-2);
    }
    .table-con {
      margin-top: 30px;
      padding: 0 20px 30px;
      .cell-con {
        display: flex;
        align-items: center;
        justify-content: center;
        .txt {
          margin-left: 5px;
        }
      }
    }
  }
}
</style>
