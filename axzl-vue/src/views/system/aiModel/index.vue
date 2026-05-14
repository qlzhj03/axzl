<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="模型名称" prop="modelName">
        <el-input
          v-model="queryParams.modelName"
          placeholder="请输入模型名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="提供商" prop="provider">
        <el-select v-model="queryParams.provider" placeholder="请选择提供商" clearable style="width: 200px">
          <el-option label="DeepSeek" value="deepseek" />
          <el-option label="OpenAI" value="openai" />
          <el-option label="Anthropic" value="anthropic" />
          <el-option label="本地模型" value="local" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item label="模型类型" prop="modelType">
        <el-select v-model="queryParams.modelType" placeholder="请选择模型类型" clearable style="width: 200px">
          <el-option label="对话模型" value="text" />
          <el-option label="Embedding模型" value="embedding" />
          <el-option label="图像模型" value="image" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option label="正常" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:aiModel:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:aiModel:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:aiModel:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:aiModel:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="aiModelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="模型名称" align="center" prop="modelName" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="提供商" align="center" prop="provider" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.provider === 'deepseek'" type="success">DeepSeek</el-tag>
          <el-tag v-else-if="scope.row.provider === 'openai'" type="primary">OpenAI</el-tag>
          <el-tag v-else-if="scope.row.provider === 'anthropic'" type="warning">Anthropic</el-tag>
          <el-tag v-else-if="scope.row.provider === 'local'" type="info">本地</el-tag>
          <el-tag v-else>{{ scope.row.provider }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模型类型" align="center" prop="modelType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.modelType === 'text'" type="primary">对话</el-tag>
          <el-tag v-else-if="scope.row.modelType === 'embedding'" type="success">Embedding</el-tag>
          <el-tag v-else-if="scope.row.modelType === 'image'" type="warning">图像</el-tag>
          <el-tag v-else>{{ scope.row.modelType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认模型" align="center" prop="isDefault" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="success" effect="dark">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="启用状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['system:aiModel:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="温度" align="center" prop="temperature" width="80" />
      <el-table-column label="最大Token" align="center" prop="maxTokens" width="100" />
      <el-table-column label="显示顺序" align="center" prop="sortOrder" width="100" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="150" />
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:aiModel:edit']">修改</el-button>
          <el-button link type="primary" icon="CircleCheck" @click="handleSetDefault(scope.row)" v-hasPermi="['system:aiModel:switch']" v-if="scope.row.isDefault !== 1">设为默认</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:aiModel:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改AI大模型配置对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="aiModelRef" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="请输入模型名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提供商" prop="provider">
              <el-select v-model="form.provider" placeholder="请选择提供商" style="width: 100%">
                <el-option label="DeepSeek" value="deepseek" />
                <el-option label="OpenAI" value="openai" />
                <el-option label="Anthropic" value="anthropic" />
                <el-option label="本地模型" value="local" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="模型类型" prop="modelType">
              <el-select v-model="form.modelType" placeholder="请选择模型类型" style="width: 100%">
                <el-option label="对话模型" value="text" />
                <el-option label="Embedding模型" value="embedding" />
                <el-option label="图像模型" value="image" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="API基础URL" prop="apiBaseUrl">
          <el-input v-model="form.apiBaseUrl" placeholder="请输入API基础URL" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="form.apiKey" type="password" show-password placeholder="请输入API密钥" />
        </el-form-item>
        <el-divider content-position="left">模型参数配置</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="温度" prop="temperature">
              <el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Top-p" prop="topP">
              <el-input-number v-model="form.topP" :min="0" :max="1" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大Token" prop="maxTokens">
              <el-input-number v-model="form.maxTokens" :min="1" :max="128000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="Top-K" prop="topK">
              <el-input-number v-model="form.topK" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="频率惩罚" prop="frequencyPenalty">
              <el-input-number v-model="form.frequencyPenalty" :min="-2" :max="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="存在惩罚" prop="presencePenalty">
              <el-input-number v-model="form.presencePenalty" :min="-2" :max="2" :step="0.1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">其他配置</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="是否默认" prop="isDefault">
              <el-radio-group v-model="form.isDefault">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="额外配置(JSON)" prop="configExtra">
          <el-input v-model="form.configExtra" type="textarea" :rows="3" placeholder='请输入JSON格式的额外配置，例如：{"dimensions": 1024}' />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiModel" lang="ts">
import type { AIModelConfig, AIModelQueryParams } from '@/types/api/system/aiModel'
import { listAiModel, getAiModel, delAiModel, addAiModel, updateAiModel, switchDefaultModel } from "@/api/system/aiModel"

const { proxy } = getCurrentInstance() as any

const aiModelList = ref<AIModelConfig[]>([])
const open = ref<boolean>(false)
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const ids = ref<number[]>([])
const single = ref<boolean>(true)
const multiple = ref<boolean>(true)
const total = ref<number>(0)
const title = ref<string>("")

const data = reactive({
  form: {} as AIModelConfig,
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    modelName: undefined,
    provider: undefined,
    modelType: undefined,
    status: undefined
  } as AIModelQueryParams,
  rules: {
    modelName: [{ required: true, message: "模型名称不能为空", trigger: "blur" }],
    provider: [{ required: true, message: "提供商不能为空", trigger: "change" }],
    modelType: [{ required: true, message: "模型类型不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询AI大模型配置列表 */
function getList() {
  loading.value = true
  listAiModel(queryParams.value).then(response => {
    aiModelList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    modelName: undefined,
    provider: undefined,
    apiBaseUrl: undefined,
    apiKey: undefined,
    modelType: "text",
    temperature: 0.7,
    maxTokens: 4096,
    topP: 1.0,
    topK: 0,
    frequencyPenalty: 0.0,
    presencePenalty: 0.0,
    isDefault: 0,
    sortOrder: 0,
    configExtra: undefined,
    status: 1,
    remark: undefined
  }
  proxy.resetForm("aiModelRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection: AIModelConfig[]) {
  ids.value = selection.map(item => item.id!)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加AI大模型配置"
}

/** 修改按钮操作 */
function handleUpdate(row: AIModelConfig) {
  reset()
  const id = row.id || ids.value[0]
  getAiModel(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改AI大模型配置"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["aiModelRef"].validate((valid: boolean) => {
    if (valid) {
      if (form.value.id != undefined) {
        updateAiModel(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addAiModel(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row: AIModelConfig) {
  const modelIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除AI大模型配置编号为"' + modelIds + '"的数据项？').then(function () {
    return delAiModel(modelIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 设为默认模型 */
function handleSetDefault(row: AIModelConfig) {
  proxy.$modal.confirm('是否确认将模型"' + row.modelName + '"设为默认模型？').then(function () {
    return switchDefaultModel(row.id!)
  }).then(() => {
    proxy.$modal.msgSuccess("设置成功")
    getList()
  }).catch(() => {})
}

/** 状态修改 */
function handleStatusChange(row: AIModelConfig) {
  const text = row.status === 1 ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.modelName + '"模型吗？').then(function () {
    return updateAiModel(row)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === 1 ? 0 : 1
  })
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/aiModel/export", {
    ...queryParams.value
  }, `ai_model_${new Date().getTime()}.xlsx`)
}

getList()
</script>
