<template>
  <div class="app-container" id="userCenter">
    <div class="main-bd">
      <div class="user-info-con">
        <div class="user-avatar-con">
          <img class="user-avatar" :src="avatar || defaultAvatar" alt="">
          <img class="photo-icon" src="~@/assets/images/common/photo.png" alt="">
          <div class="upload-con">
            <a-upload
              ref="uploadDom"
              :file-list="fileList"
              :action="upload.url"
              :limit="1"
              accept="image/*"
              :headers="upload.headers"
              :show-file-list="false"
              name="avatarfile"
              @change="handleFileChange"
              @progress="handleFileUploadProgress"
              @success="handleFileSuccess"
            >
              <template #upload-button>
                <div class="upload-info">{{$t('ucenter.index.5m6nkbklgtk0')}}</div>
              </template>
            </a-upload>
          </div>
        </div>
        <div class="user-info">
          <div class="user-name">
            <span class="name">{{ nickName }}</span>
            <span class="icon-con" @click="editUserInfo">
              <icon-edit />
            </span>
          </div>
          <div class="info-con">
            <div class="info-item">
              <span class="icon-con">
                <icon-user />
              </span>
              <span class="txt">{{ userName }}</span>
            </div>
            <div class="info-item">
              <span class="icon-con">
                <icon-mobile />
              </span>
              <span class="txt">{{ phonenumber }}</span>
            </div>
            <div class="info-item">
              <span class="icon-con">
                <icon-email />
              </span>
              <span class="txt">{{ email }}</span>
            </div>
          </div>
          <div class="info-con">
            <div class="info-item">
              <span class="icon-con">
                <icon-idcard />
              </span>
              <span class="txt1">{{ remark }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- user edit -->
    <edit-info v-model:open="editInfoVisible" :options="currentEditUser" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed } from 'vue'
  import { useUserStore } from '@/store'
  import { getToken } from '@/utils/auth'
  import { Message } from '@arco-design/web-vue'
  import avatarImg from '@/assets/images/default-avatar.png'
  import EditInfo from './components/EditInfo.vue'

  const userStore = useUserStore()
  const currentEditUser = ref({})
  const editInfoVisible = ref<boolean>(false)
  const upload = reactive({
    open: false,
    isUploading: true,
    headers: { Authorization: 'Bearer ' + getToken() },
    url: '/system/user/profile/avatar'
  })
  const uploadDom = ref<any>(null)
  const fileList = ref<any>([])

  const defaultAvatar = ref<any>(avatarImg)
  const avatar = computed(() => userStore.avatar)
  const userName = computed(() => userStore.userName)
  const nickName = computed(() => userStore.nickName)
  const phonenumber = computed(() => userStore.phonenumber)
  const email = computed(() => userStore.email)
  const remark = computed(() => userStore.remark)

  const editUserInfo = () => {
    currentEditUser.value = {
      userId: userStore.userId,
      userName: userStore.userName,
      nickName: userStore.nickName,
      phonenumber: userStore.phonenumber,
      email: userStore.email
    }
    editInfoVisible.value = true
  }

  const handleFileChange = () => {
    upload.isUploading = false
  }

  const handleFileUploadProgress = () => {
    upload.isUploading = true
  }

  const handleFileSuccess = (fileItem: any) => {
    upload.open = false
    upload.isUploading = true
    fileList.value = []
    if (fileItem.response.code === 200) {
      Message.success('Upload success')
      userStore.info()
    } else {
      Message.error(fileItem.response.msg)
    }
  }
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    min-height: auto;
    .user-info-con {
      height: 200px;
      display: flex;
      justify-content: center;
      align-items: center;
      .user-avatar-con {
        position: relative;
        cursor: pointer;
        .user-avatar {
          width: 100px;
          height: 100px;
          border-radius: 50%;
        }
        .photo-icon {
          position: absolute;
          right: 0;
          bottom: 0;
          width: 32px;
          height: 32px;
          border-radius: 50%;
        }
        .upload-con {
          position: absolute;
          top: 0;
          cursor: pointer;
          .upload-info {
            background: rgba(0, 0, 0, 0.5);
            opacity: 0;
            height: 100px;
            line-height: 100px;
            width: 100px;
            border-radius: 50%;
            text-align: center;
            color: #fff;
            transition: all 0.3s ease-in-out;
            &:hover {
              opacity: 1;
            }
          }
        }
      }
      .user-info {
        margin-left: 25px;
        .user-name {
          margin-bottom: 8px;
          .name {
            font-size: 20px;
            color: var(--color-text-1);
            margin-right: 13px;
          }
          .icon-con {
            cursor: pointer;
            color: rgb(var(--primary-6));
          }
        }
        .info-con {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          .info-item {
            margin-right: 23px;
            .icon-con {
              color: var(--color-text-2);
              margin-right: 3px;
            }
            .txt {
              font-size: 16px;
              color: var(--color-text-1);
            }
            .txt1 {
              font-size: 14px;
              color: var(--color-text-1);
            }
          }
        }
      }
    }
  }
}
</style>
