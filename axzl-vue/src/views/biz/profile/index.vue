<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="请选择性别" clearable style="width: 240px">
          <el-option label="男" value="0" />
          <el-option label="女" value="1" />
          <el-option label="未知" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="档案状态" clearable style="width: 240px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:profile:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:profile:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:profile:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['biz:profile:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['biz:profile:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="profileList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="档案ID" align="center" key="profileId" prop="profileId" v-if="columns.profileId.visible" width="100" />
      <el-table-column label="用户ID" align="center" key="userId" prop="userId" v-if="columns.userId.visible" width="100" />
      <el-table-column label="用户昵称" align="center" key="nickName" prop="nickName" v-if="columns.nickName.visible" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="出生日期" align="center" key="birthday" prop="birthday" v-if="columns.birthday.visible" width="120">
        <template #default="scope">
          <span>{{ parseTime(scope.row.birthday, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" key="gender" prop="gender" v-if="columns.gender.visible" width="80">
        <template #default="scope">
          <dict-tag :options="sys_user_sex" :value="scope.row.gender" />
        </template>
      </el-table-column>
      <el-table-column label="血型" align="center" key="bloodType" prop="bloodType" v-if="columns.bloodType.visible" width="80" />
      <el-table-column label="身高(cm)" align="center" key="height" prop="height" v-if="columns.height.visible" width="100" />
      <el-table-column label="体重(kg)" align="center" key="weight" prop="weight" v-if="columns.weight.visible" width="100" />
      <el-table-column label="过敏史" align="center" key="allergyHistory" prop="allergyHistory" v-if="columns.allergyHistory.visible" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="既往病史" align="center" key="medicalHistory" prop="medicalHistory" v-if="columns.medicalHistory.visible" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="家族病史" align="center" key="familyHistory" prop="familyHistory" v-if="columns.familyHistory.visible" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="生活习惯" align="center" key="lifestyle" prop="lifestyle" v-if="columns.lifestyle.visible" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" key="status" prop="status" v-if="columns.status.visible" width="80">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" key="remark" prop="remark" v-if="columns.remark.visible" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" key="createTime" prop="createTime" v-if="columns.createTime.visible" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" key="updateTime" prop="updateTime" v-if="columns.updateTime.visible" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-tooltip content="查看" placement="top">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
          </el-tooltip>
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:profile:edit']"></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:profile:remove']"></el-button>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 查看健康档案详情对话框 -->
    <el-dialog title="健康档案详情" v-model="viewOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="档案编号">{{ viewForm.profileId }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ viewForm.nickName }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <dict-tag :options="sys_user_sex" :value="viewForm.gender" />
        </el-descriptions-item>
        <el-descriptions-item label="血型">{{ viewForm.bloodType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身高(cm)">{{ viewForm.height || '-' }}</el-descriptions-item>
        <el-descriptions-item label="体重(kg)">{{ viewForm.weight || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ parseTime(viewForm.birthday, '{y}-{m}-{d}') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="sys_normal_disable" :value="viewForm.status" />
        </el-descriptions-item>
        <el-descriptions-item label="过敏史" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.allergyHistory || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="既往病史" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.medicalHistory || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="家族病史" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.familyHistory || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="生活习惯" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.lifestyle || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewForm.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(viewForm.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加或修改健康档案对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form :model="form" :rules="rules" ref="profileRef" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请选择用户" readonly @click="handleSelectUser" style="cursor: pointer;">
                <template #append>
                  <el-button icon="Search" @click="handleSelectUser">选择</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option label="男" value="0" />
                <el-option label="女" value="1" />
                <el-option label="未知" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择出生日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血型" prop="bloodType">
              <el-select v-model="form.bloodType" placeholder="请选择血型" clearable>
                <el-option label="A型" value="A" />
                <el-option label="B型" value="B" />
                <el-option label="AB型" value="AB" />
                <el-option label="O型" value="O" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number v-model="form.height" :min="0" :max="300" :precision="2" placeholder="请输入身高" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number v-model="form.weight" :min="0" :max="500" :precision="2" placeholder="请输入体重" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="过敏史" prop="allergyHistory">
              <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="请输入过敏史，多个用逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="既往病史" prop="medicalHistory">
              <el-input v-model="form.medicalHistory" type="textarea" :rows="2" placeholder="请输入既往病史" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="家族病史" prop="familyHistory">
              <el-input v-model="form.familyHistory" type="textarea" :rows="2" placeholder="请输入家族病史" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="生活习惯" prop="lifestyle">
              <el-input v-model="form.lifestyle" type="textarea" :rows="2" placeholder="请输入生活习惯，如饮食、运动等" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户选择对话框 -->
    <el-dialog title="选择用户" v-model="userDialogVisible" width="800px" append-to-body>
      <el-form :model="userQueryParams" ref="userQueryRef" :inline="true" label-width="68px">
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="userQueryParams.userName" placeholder="请输入用户名称" clearable style="width: 200px" @keyup.enter="handleUserQuery" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input v-model="userQueryParams.phonenumber" placeholder="请输入手机号码" clearable style="width: 200px" @keyup.enter="handleUserQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleUserQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetUserQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="userLoading" :data="userList" @row-click="confirmSelectUser" highlight-current-row>
        <el-table-column label="用户ID" align="center" prop="userId" width="100" />
        <el-table-column label="用户名称" align="center" prop="userName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="用户昵称" align="center" prop="nickName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="手机号码" align="center" prop="phonenumber" width="120" />
        <el-table-column label="邮箱" align="center" prop="email" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="userTotal > 0" :total="userTotal" v-model:page="userQueryParams.pageNum" v-model:limit="userQueryParams.pageSize" @pagination="getUserList" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeUserDialog">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 健康档案导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :on-change="handleFileChange" :on-remove="handleFileRemove" :auto-upload="false" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的健康档案数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" underline="never" style="font-size: 12px; vertical-align: baseline" @click="importTemplateAction">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="HealthProfile">
import { listProfile, getProfile, delProfile, addProfile, updateProfile, exportProfile, importTemplate, importData } from "@/api/biz/profile"
import { listUser } from "@/api/system/user"
import { getToken } from "@/utils/auth"
import type { UserProfile, ProfileQueryParams, ProfileFormData } from '@/types/api/biz/profile'
import type { SysUser, UserQueryParams } from '@/types'
import type { TableShowColumns } from '@/types/api/common'

const { proxy } = getCurrentInstance()
const { sys_user_sex, sys_normal_disable } = proxy.useDict("sys_user_sex", "sys_normal_disable")

const profileList = ref<UserProfile[]>([])
const open = ref<boolean>(false)
const viewOpen = ref<boolean>(false)
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const ids = ref<number[]>([])
const single = ref<boolean>(true)
const multiple = ref<boolean>(true)
const total = ref<number>(0)
const title = ref<string>("")
const dateRange = ref<string[]>([])
const viewForm = ref<UserProfile>({})

// 列显隐信息
const columns = ref<Record<string, TableShowColumns>>({
  profileId: { label: '档案ID', visible: false },
  userId: { label: '用户ID', visible: false },
  nickName: { label: '用户昵称', visible: true },
  birthday: { label: '出生日期', visible: false },
  gender: { label: '性别', visible: true },
  bloodType: { label: '血型', visible: true },
  height: { label: '身高(cm)', visible: true },
  weight: { label: '体重(kg)', visible: true },
  allergyHistory: { label: '过敏史', visible: false },
  medicalHistory: { label: '既往病史', visible: false },
  familyHistory: { label: '家族病史', visible: false },
  lifestyle: { label: '生活习惯', visible: false },
  status: { label: '状态', visible: true },
  remark: { label: '备注', visible: false },
  createTime: { label: '创建时间', visible: false },
  updateTime: { label: '更新时间', visible: false }
})

/*** 用户导入参数 */
const upload = reactive({
  // 是否显示弹出层（用户导入）
  open: false,
  // 弹出层标题（用户导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的用户数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/biz/profile/importData"
})

// 用户选择相关
const userDialogVisible = ref<boolean>(false)
const userList = ref<SysUser[]>([])
const userLoading = ref<boolean>(false)
const userTotal = ref<number>(0)
const userQueryParams = reactive<UserQueryParams>({
  pageNum: 1,
  pageSize: 10,
  userName: undefined,
  phonenumber: undefined
})

const data = reactive({
  form: {} as ProfileFormData & { nickName?: string },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    gender: undefined,
    status: undefined
  } as ProfileQueryParams,
  rules: {
    nickName: [{ required: true, message: "用户不能为空", trigger: "blur" }],
    gender: [{ required: true, message: "性别不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询健康档案列表 */
function getList() {
  loading.value = true
  listProfile(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    profileList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection: UserProfile[]) {
  ids.value = selection.map(item => item.profileId!)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加健康档案"
}

/** 查看按钮操作 */
function handleView(row?: UserProfile) {
  const profileId = row?.profileId
  getProfile(profileId!).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 修改按钮操作 */
function handleUpdate(row?: UserProfile) {
  reset()
  const profileId = row?.profileId || ids.value[0]
  getProfile(profileId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改健康档案"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["profileRef"].validate((valid: boolean) => {
    if (valid) {
      // 创建提交数据，不包含nickName字段
      const submitData: ProfileFormData = {
        profileId: form.value.profileId,
        userId: form.value.userId,
        birthday: form.value.birthday,
        gender: form.value.gender,
        bloodType: form.value.bloodType,
        height: form.value.height,
        weight: form.value.weight,
        allergyHistory: form.value.allergyHistory,
        medicalHistory: form.value.medicalHistory,
        familyHistory: form.value.familyHistory,
        lifestyle: form.value.lifestyle,
        status: form.value.status,
        remark: form.value.remark
      }
      
      if (submitData.profileId != undefined) {
        updateProfile(submitData).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProfile(submitData).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row?: UserProfile) {
  const profileIds = row?.profileId || ids.value
  proxy.$modal.confirm('是否确认删除健康档案编号为"' + profileIds + '"的数据项？').then(function () {
    return delProfile(profileIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 表单重置 */
function reset() {
  form.value = {
    profileId: undefined,
    userId: undefined,
    nickName: undefined,
    birthday: undefined,
    gender: undefined,
    bloodType: undefined,
    height: undefined,
    weight: undefined,
    allergyHistory: undefined,
    medicalHistory: undefined,
    familyHistory: undefined,
    lifestyle: undefined,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("profileRef")
}

/** 选择用户 */
function handleSelectUser() {
  // 打开用户选择对话框
  userDialogVisible.value = true
  // 重置用户查询参数
  userQueryParams.value = {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined
  }
  getUserList()
}

/** 获取用户列表 */
function getUserList() {
  userLoading.value = true
  listUser(userQueryParams.value).then(response => {
    userList.value = response.rows
    userTotal.value = response.total
    userLoading.value = false
  })
}

/** 搜索用户 */
function handleUserQuery() {
  userQueryParams.value.pageNum = 1
  getUserList()
}

/** 重置用户搜索 */
function resetUserQuery() {
  proxy.resetForm("userQueryRef")
  handleUserQuery()
}

/** 选择用户确认 */
function confirmSelectUser(row: SysUser) {
  form.value.userId = row.userId
  form.value.nickName = row.nickName
  userDialogVisible.value = false
  proxy.$modal.msgSuccess(`已选择用户：${row.nickName}`)
}

/** 关闭用户选择对话框 */
function closeUserDialog() {
  userDialogVisible.value = false
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("biz/profile/export", {
    ...queryParams.value,
  }, `profile_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "健康档案导入"
  upload.open = true
  upload.selectedFile = null
}

/** 下载模板操作 */
function importTemplateAction() {
  proxy.download("biz/profile/importTemplate", {
  }, `profile_template_${new Date().getTime()}.xlsx`)
}

/**文件上传中处理 */
const handleFileUploadProgress = (event: any, file: any, fileList: any[]) => {
  upload.isUploading = true
}

/** 文件选择处理 */
const handleFileChange = (file: any, fileList: any[]) => {
  upload.selectedFile = file
}

/** 文件删除处理 */
const handleFileRemove = (file: any, fileList: any[]) => {
  upload.selectedFile = null
}

/** 文件上传成功处理 */
const handleFileSuccess = (response: any, file: any, fileList: any[]) => {
  upload.open = false
  upload.isUploading = false
  proxy.$refs["uploadRef"].handleRemove(file)
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

/** 提交上传文件 */
function submitFileForm() {
  const file = upload.selectedFile
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 'xls'或'xlsx'的文件。")
    return
  }
  proxy.$refs["uploadRef"].submit()
}

onMounted(() => {
  getList()
})
</script>
