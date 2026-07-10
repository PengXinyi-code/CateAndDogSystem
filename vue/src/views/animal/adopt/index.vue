<template>
  <div class="app-container">
    <!-- 顶部搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="宠物姓名" prop="animalName">
        <el-input
            v-model="queryParams.animalName"
            placeholder="请输入宠物姓名"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请状态" prop="status">
        <el-select style="width: 200px;" v-model="queryParams.status" placeholder="请选择申请状态">
          <el-option label="审核中" value="审核中"/>
          <el-option label="已拒绝" value="已拒绝"/>
          <el-option label="已完成" value="已完成"/>
        </el-select>
      </el-form-item>
      <el-form-item label="申请用户" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入申请用户"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
        >删除
        </el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table @row-click="clickRow" ref="tableRef" highlight-current-row
              border v-loading="loading" :data="adoptList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
      <el-table-column label="宠物姓名" align="center" prop="animalName"/>
      <el-table-column label="申请人姓名" align="center" prop="name"/>
      <el-table-column label="联系电话" align="center" prop="phone"/>
      <el-table-column label="电子邮箱" align="center" prop="email"/>
      <el-table-column label="居住地址" align="center" prop="address"/>
      <el-table-column label="职业" align="center" prop="occupation"/>
      <el-table-column label="申请理由" align="center" prop="reason"/>
      <el-table-column label="养宠经验" align="center" prop="petExperience"/>
      <el-table-column label="申请状态" align="center" prop="status">
        <template #default="scope">
          <el-tag type="primary" v-if="scope.row.status === '审核中'">{{ scope.row.status }}</el-tag>
          <el-tag type="danger" v-if="scope.row.status === '已拒绝'">{{ scope.row.status }}</el-tag>
          <el-tag type="success" v-if="scope.row.status === '已完成'">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理说明" align="center" prop="description" show-overflow-tooltip/>
      <el-table-column label="申请用户" align="center" prop="userName"/>
      <el-table-column label="申请时间" align="center" prop="createTime"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="scope.row.status === '审核中'" type="primary" @click="handleToExamine(scope.row)">
            审核
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改领养申请对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
      <el-form ref="adoptRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申请人姓名" prop="name">
          <el-input disabled v-model="form.name" placeholder="请输入申请人姓名"/>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input disabled v-model="form.phone" placeholder="请输入联系电话"/>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input disabled v-model="form.email" placeholder="请输入电子邮箱"/>
        </el-form-item>
        <el-form-item label="居住地址" prop="address">
          <el-input disabled v-model="form.address" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
        <el-form-item label="职业" prop="occupation">
          <el-input disabled v-model="form.occupation" placeholder="请输入职业"/>
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input disabled v-model="form.reason" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
        <el-form-item label="养宠经验" prop="petExperience">
          <el-input disabled v-model="form.petExperience" placeholder="请输入养宠经验"/>
        </el-form-item>
        <el-form-item label="处理说明" prop="description">
          <el-input :rows="5" type="textarea" v-model="form.description" placeholder="请输入处理说明"/>
        </el-form-item>
        <el-form-item label="审核处理" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio style="background-color: #d2f5c7" value="已完成" size="large" border>同意</el-radio>
            <el-radio style="background-color: #f6cccc" value="已拒绝" size="large" border>拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import {ref, reactive, toRefs, onMounted} from 'vue'
import {listAdopt, getAdopt, delAdopt, updateAdopt} from "@/api/animal/adopt"
import {ElMessage, ElMessageBox} from "element-plus";
import {updateAnimal} from "@/api/animal/animals.js";

const queryRef = ref()
const adoptRef = ref()
const adoptList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const selectedRow = ref(null)
const tableRef = ref()

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    animalId: null,
    status: null,
    userId: null,
    animalName: null,
    userName: null
  },
  rules: {
    name: [
      {required: true, message: "申请人姓名不能为空", trigger: "blur"}
    ],
    phone: [
      {required: true, message: "联系电话不能为空", trigger: "blur"}
    ],
    email: [
      {required: true, message: "电子邮箱不能为空", trigger: "blur"}
    ],
    address: [
      {required: true, message: "居住地址不能为空", trigger: "blur"}
    ],
    occupation: [
      {required: true, message: "职业不能为空", trigger: "blur"}
    ],
    reason: [
      {required: true, message: "申请理由不能为空", trigger: "blur"}
    ],
    petExperience: [
      {required: true, message: "养宠经验不能为空", trigger: "blur"}
    ],
    description: [
      {required: true, message: "处理说明不能为空", trigger: "blur"}
    ]
  }
})

const {queryParams, form, rules} = toRefs(data)

const clickRow = (row) => {
  selectedRow.value = row;
  const table = tableRef.value;
  table.clearSelection();
  table.toggleRowSelection(row, true);
}

const indexMethod = (index) => {
  let pageNum = queryParams.value.pageNum - 1;
  if ((pageNum !== -1 && pageNum !== 0)) {
    return (index + 1) + (pageNum * queryParams.value.pageSize);
  } else {
    return (index + 1)
  }
}

const getList = () => {
  loading.value = true
  listAdopt(queryParams.value).then(response => {
    adoptList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(err => {
    console.error('获取列表失败:', err)
    loading.value = false
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  form.value = {
    adoptId: null,
    animalId: null,
    name: null,
    phone: null,
    email: null,
    address: null,
    occupation: null,
    reason: null,
    petExperience: null,
    status: null,
    description: null,
    userId: null,
    createTime: null
  }
  if (adoptRef.value) {
    adoptRef.value.resetFields()
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.adoptId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 审核 - 打开对话框 */
const handleToExamine = (row) => {
  reset()
  const _adoptId = row.adoptId || ids.value
  console.log('=== 点击审核 ===')
  console.log('申请ID:', _adoptId)
  getAdopt(_adoptId).then(response => {
    console.log('后端返回的数据:', response.data)
    console.log('animalId:', response.data.animalId)
    console.log('animalId类型:', typeof response.data.animalId)
    form.value = response.data
    form.value.description = null
    open.value = true
    title.value = "审核领养申请"
  }).catch(err => {
    console.error('获取申请详情失败:', err)
    ElMessage.error('获取申请详情失败')
  })
}

/** 提交审核 */
const submitForm = () => {
  console.log('=== 提交审核 ===')
  console.log('form.value:', form.value)
  console.log('animalId:', form.value.animalId)

  if (!form.value.animalId) {
    ElMessage.error('缺少宠物ID，无法审核')
    console.error('animalId 为空！')
    return
  }

  adoptRef.value.validate(valid => {
    if (valid) {
      if (form.value.status === '已完成') {
        const item = {
          id: form.value.animalId,
          status: '已领养'
        }
        console.log('更新动物参数(同意):', item)
        updateAnimal(item).then(() => {
          updateAdopt(form.value).then(() => {
            ElMessage.success("审核成功")
            open.value = false
            getList()
          })
        }).catch(err => {
          console.error('updateAnimal 错误:', err)
          ElMessage.error('更新动物状态失败')
        })
      } else if (form.value.status === '已拒绝') {
        const item = {
          id: form.value.animalId,
          status: '可领养'
        }
        console.log('更新动物参数(拒绝):', item)
        updateAnimal(item).then(() => {
          updateAdopt(form.value).then(() => {
            ElMessage.success("审核成功")
            open.value = false
            getList()
          })
        }).catch(err => {
          console.error('updateAnimal 错误:', err)
          ElMessage.error('更新动物状态失败')
        })
      }
    }
  })
}

/** 删除 */
const handleDelete = (row) => {
  const _adoptIds = row.adoptId || ids.value
  ElMessageBox.confirm('是否确认删除该项数据？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delAdopt(_adoptIds)
  }).then(() => {
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>