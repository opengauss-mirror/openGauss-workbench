<template>
  <div class="app-container" id="roleManage">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="roleName" :label="$t('role.index.5m6nlovf8ik0')">
            <a-input v-model="form.roleName" allow-clear :placeholder="$t('role.index.5m6nlovfao80')" style="width: 180px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item field="status" :label="$t('role.index.5m6nlovfawc0')">
            <a-select v-model="form.status" allow-clear :placeholder="$t('role.index.5m6nlovfb040')" style="width: 180px;" @change="getList(true)">
              <a-option value="0">{{$t('role.index.5m6nlovfb3c0')}}</a-option>
              <a-option value="1">{{$t('role.index.5m6nlovfb700')}}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="outline" @click="getList(true)">
              <template #icon>
                <icon-search />
              </template>
              <template #default>{{$t('role.index.5m6nlovfba00')}}</template>
            </a-button>
            <a-button style="margin-left: 10px;" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{$t('role.index.5m6nlovfbdc0')}}</template>
            </a-button>
          </a-form-item>
        </a-form>
        <div class="btn-con">
          <a-button type="primary" @click="addRole">
            <template #icon>
              <icon-plus />
            </template>
            <template #default>{{$t('role.index.5m6nlovfbgw0')}}</template>
          </a-button>
        </div>
      </div>
      <div class="table-con">
        <a-table :data="tableData" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
          <template #columns>
            <a-table-column :title="$t('role.index.5m6nlovf8ik0')" data-index="roleName" :width="180">
              <template #cell="{ record }">
                {{ record.roleId !== 1 ? record.roleName : $t('user.index.5obuyt8kr2o0') }}
              </template>
            </a-table-column>
            <a-table-column :title="$t('role.index.5m6nlovfawc0')" data-index="status" align="center">
              <template #cell="{ record }">
                <div class="cell-con">
                  <a-switch v-model="record.status" :disabled="record.roleId === 1" checked-color="#00B42A" checked-value="0" unchecked-value="1" size="small" @change="statusChange(record)" />
                  <span class="txt">{{record.status === '0' ? $t('role.index.5m6nlovfb3c0') : $t('role.index.5m6nlovfbjw0')}}</span>
                </div>
              </template>
            </a-table-column>
            <a-table-column :title="$t('role.index.5m6nlovfbmw0')" data-index="createTime" :width="300"></a-table-column>
            <a-table-column :title="$t('role.index.5m6nlovfbqc0')" align="center" :width="200">
              <template #cell="{ record }">
                <a-button
                  v-if="record.roleId !== 1"
                  size="mini"
                  type="text"
                  @click="editRoleInfo(record)"
                >
                  <template #icon>
                    <icon-edit />
                  </template>
                  <template #default>{{$t('role.index.5m6nlovfbtc0')}}</template>
                </a-button>
                <a-popconfirm v-if="record.roleId !== 1" :content="$t('role.index.5m6nlovfbwg0')" @ok="deleteRole(record)">
                  <a-button
                    size="mini"
                    type="text"
                  >
                    <template #icon>
                      <icon-delete />
                    </template>
                    <template #default>{{$t('role.index.5m6nlovfc000')}}</template>
                  </a-button>
                </a-popconfirm>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </div>
    </div>

    <!-- add or edit -->
    <edit-role v-model:open="editRoleVisible" :options="currentEditRole" @ok="getList" />
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref, onMounted } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { getRoleList, changeStatus, roleDelete } from '@/api/role'
  import EditRole from './components/EditRole.vue'

  const form = reactive({
    roleName: undefined,
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
  const editRoleVisible = ref<boolean>(false)
  const currentEditRole = ref({})

  const getList = (fresh?: any) => {
    if (fresh) {
      queryParams.pageNum = 1
      pagination.current = 1
    }
    getRoleList({
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
    form.roleName = undefined
    form.status = undefined
    getList()
  }

  const pageChange = (current: number) => {
    queryParams.pageNum = current
    pagination.current = current
    getList()
  }

  const statusChange = (value: any) => {
    changeStatus({
      roleId: value.roleId,
      status: value.status
    }).then(() => {
      getList()
    })
  }

  const addRole = () => {
    currentEditRole.value = {}
    editRoleVisible.value = true
  }

  const editRoleInfo = (value: any) => {
    currentEditRole.value = value
    editRoleVisible.value = true
  }

  const deleteRole = (value: any) => {
    roleDelete(value.roleId).then(() => {
      Message.success('Delete success')
      getList()
    })
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
