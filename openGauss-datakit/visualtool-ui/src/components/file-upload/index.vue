<template >
    <div>
        <el-upload drag action="" multiple  :before-upload="beforeUpload" :limit="1"
            v-model:fileList="fileList" ref="uploadRef" :onProgress="handleProgress" :http-request="doUpload"
            :on-exceed="handleExceed" :on-change="changeFile" :accept="props.accept">
            <div class="upload-box">
                <div class="upload-add">
                    <el-icon class="upload-btn-icon">
                        <IconUpload></IconUpload>
                    </el-icon>
                    <div class="upload-tip">
                      {{$t('manage.PluginInstall.dragMsg')}}
                        <div style="margin-top: 16px">
                            <el-button type="primary" size="small">{{$t('manage.PluginInstall.clickToUpload')}}</el-button>
                        </div>
                    </div>
                    <div class="upload-tip">{{ props.tips }}</div>
                </div>
            </div>
        </el-upload>
        <el-progress :percentage="props.percentage"
            v-if="fileList?.length && props.percentage > 0 && props.percentage <= 100"></el-progress>
    </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { IconUpload } from '@computing/opendesign-icons'
import { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { genFileId } from 'element-plus'
import showMessage from '@/hooks/showMessage'

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadInstance[]>([])

interface propsType {
    percentage?: number,
    accept?: string,
    sizeLimit?: number,
    tips: string
}
const props = withDefaults(defineProps<propsType>(), {
    percentage: 0
})

const emit = defineEmits(['changeFile'])
const changeFile: UploadProps['onChange'] = (uploadFile) => {
    emit('changeFile', uploadFile)
}

const handleProgress = () => {

}

const doUpload = () => {

}
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
    // since some file don't contain MIME type, judge type by suffix
    const suffix = rawFile.name?.slice(rawFile.name?.lastIndexOf('.'))
    if (props.accept) {
        const typeList = props.accept.split(',')
        if (!typeList.some(value => value === suffix)) {
            showMessage('error',  t('transcribe.index.fileTypeRestriction'))
            return false
        }
    }
    if (props.sizeLimit && rawFile.size / 1024 / 1024 > props.sizeLimit) {
        showMessage('error', t('transcribe.index.fileSizeLimit'))
        return false
    }

}


const handleExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value!.clearFiles();
    const file = files[0] as UploadRawFile;
    file.uid = genFileId();
    uploadRef.value!.handleStart(file)
}
</script>
<style lang="less" scoped>
.upload-box {
    width: 400px;
    height: 200px;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .upload-add {
        .upload-btn-icon {
            font-size: 64px;
            color: var(--o-text-color-tertiary);
        }

        .upload-tip {
            width: 100%;
            font-size: var(--o-font-size-info);
            color: var(--o-text-color-tertiary);

        }
    }
}
</style>
