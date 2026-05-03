<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img" title="点击上传头像" class="img-circle img-lg" />
    <el-dialog :title="title" v-model="open" width="800px" append-to-body @opened="modalOpened" @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <VueCropper
            v-if="visible"
            ref="cropperRef"
            :img="options.img"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleChange"
          >
            <el-button>
              选择
              <el-icon class="el-icon--right"><Upload /></el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button :icon="Plus" @click="changeScale(1)" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button :icon="Minus" @click="changeScale(-1)" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button :icon="RefreshLeft" @click="rotateLeft()" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button :icon="RefreshRight" @click="rotateRight()" />
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button type="primary" @click="uploadImg()">提 交</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { VueCropper } from 'vue-cropper'
import { Upload, Plus, Minus, RefreshLeft, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { uploadAvatar } from '@/api/system/user'
import 'vue-cropper/dist/index.css'

const userStore = useUserStore()

const cropperRef = ref<InstanceType<typeof VueCropper>>()
const open = ref(false)
const visible = ref(false)
const title = ref('修改头像')
const uploading = ref(false)

const options = reactive({
  img: userStore.avatar as string,
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: 'png' as const,
  filename: 'avatar',
  previews: {} as { url?: string; img?: string }
})

function editCropper() {
  open.value = true
}

function modalOpened() {
  visible.value = true
}

function rotateLeft() {
  cropperRef.value?.rotateLeft()
}

function rotateRight() {
  cropperRef.value?.rotateRight()
}

function changeScale(num: number) {
  cropperRef.value?.changeScale(num)
}

function handleChange(uploadFile: UploadFile) {
  if (!uploadFile.raw) return
  if (!uploadFile.raw.type.startsWith('image/')) {
    ElMessage.error('文件格式错误，请上传图片类型，如：JPG、PNG')
    return
  }
  const reader = new FileReader()
  reader.readAsDataURL(uploadFile.raw)
  reader.onload = () => {
    options.img = reader.result as string
    options.filename = uploadFile.name
  }
}

function uploadImg() {
  cropperRef.value?.getCropBlob((data: Blob) => {
    if (!data) return

    uploading.value = true
    const formData = new FormData()
    formData.append('avatarfile', data, options.filename)

    uploadAvatar(formData).then(response => {
      open.value = false
      options.img = import.meta.env.VITE_APP_BASE_API + response.imgUrl
      userStore.avatar = options.img
      ElMessage.success('修改成功')
      visible.value = false
    }).catch(() => {
      ElMessage.error('头像上传失败')
    }).finally(() => {
      uploading.value = false
    })
  })
}

function realTime(data: { url?: string; img?: string }) {
  options.previews = data
}

function closeDialog() {
  options.img = userStore.avatar as string
  visible.value = false
}
</script>

<style lang="scss" scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: "+";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
