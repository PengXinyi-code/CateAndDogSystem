<template xmlns:el-col="http://www.w3.org/1999/html">
  <div>
    <!-- 顶部搜索区域 -->
    <el-form :model="queryParms" ref="queryRef":inline="true" label-width="70px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="queryParms.name"
                  placeholder="请输入分类名称"
                  clearable>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!--顶部按钮-->
    <el-row :gutter="20" style="padding: 10px 0">
      <el-col :span="1.5">
        <el-button type="primary"
                   plain
                   icon="Plus"
                   @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success"
                   plain
                   icon="Edit"
                   @click="handleUpdate"
                   :disabled="single"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger"
                   plain
                   icon="Delete"
                   @click="handleDelete"
                   :disabled="muliple"
        >删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table @row-click="clickRow" :data="categoryList" ref="tableRef" highlight-current-row border
              v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
      <el-table-column label="分类名称" align="center" prop="name"/>
      <el-table-column label="排序" align="center" prop="sort"/>
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination :total="total"
                v-model:page = "queryParms.pageNum"
                v-model:limit = "queryParms.pageSize"
                @pagination="getList"
    />

    <!--添加或修改动物分类对话框-->
    <vxe-modal :title="title" v-model="open" width="30%" show-maximize showFooter resize>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" clearable/>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number style="width: 100%;" v-model="form.sort" placeholder="请输入排序" clearable/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="open = false">取消</el-button>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import {onMounted,ref} from "vue";
import {addCategory, delCategory, getCategory, listCategory, updateCategory} from "@/api/sccour/category.js";
import Pagination from "@/components/Pagination/index.vue";
import {ElMessage, ElMessageBox} from "element-plus";

//表单名称
const title=ref('')

//表单是否打开
const open=ref(false)

//表单参数
const form=ref({})

//表单校验
const rules = ref({
  name: [
    { required: true, message: '分类名称不能为空', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '排序不能为空', trigger: 'blur' }
  ],
})

//表单重置
const reset=()=>{
  form.value={
    categoryId:null,
    name:null,
    sort:null
  }
  if (categoryRef.value) {
    categoryRef.value.resetFields()
  }
}

//提交表单
const submitForm = () => {
  categoryRef.value.validate(valid => {
    if (valid) {
      if (form.value.categoryId != null) {
        //修改时提交
        updateCategory(form.value).then(res=>{
          ElMessage.success('修改成功~')
          open.value = false
          getList()
        })
      } else {
        //新增时提交
        addCategory(form.value).then(res => {
          ElMessage.success('新增成功~')
          open.value = false
          getList()
        })
      }
    }
  })
}

//新增按钮
const handleAdd = () => {
  reset()
  open.value = true
  title.value = '添加动物信息'
}

//修改按钮
const handleUpdate=(row)=>{
  reset()
  const categoryId=row.categoryId||ids.value
  getCategory(categoryId).then(res=>{
    form.value=res.data
    open.value=true
    title.value='修改动物信息'
  })
}

//删除按钮
const handleDelete = (row) => {
  const categoryIds = row.categoryId || ids.value
  ElMessageBox.confirm(
      '是否确认删除该项数据?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        delCategory(categoryIds).then(res => {
          getList()
          ElMessage.success('删除成功~')
        })
      })
}

//组件实例
const tableRef=ref()
const queryRef=ref()
const categoryRef=ref()

//查询参数
const queryParms = ref({
  pageNum: 1,
  pageSize: 10,
  name:null
})

//列表数据
const categoryList=ref([])

//数据总数
const total=ref(0)

//加载状态
const loading=ref(false)

//当前选中的ID数
const ids = ref([])

//当前是否会选中单行
const single=ref(true)

//当前是否未选中多行
const muliple = ref(true)

//当前选中的行
const selectedRow = ref(null)

//点击当前行并获取信息
const clickRow = (row) => {
  selectedRow.value = row
  const table = tableRef.value
  //清除所有已选中的
  table.clearSelection()
  //选中当前选择的行
  table.toggleRowSelection(row,true)
}

//多选框选择数据
const handleSelectionChange = (selection) =>{
  ids.value = selection.map(item=>item.categoryId)
  single.value = selection.length != 1
  muliple.value = ! selection.length
}

//自定义序号的方法
const indxMethod = (index) => {
  let pageNum=queryParms.value.pageNum-1;
  if(pageNum!=-1&&pageNum!=0){
    return (index+1)+(pageNum*queryParms.value.pageSize);
  }
  else {
    return index + 1;
  }
}

//查询数据
const getList = () =>{
  loading.value=true
  listCategory(queryParms.value).then(res => {
    categoryList.value=res.rows
    total.value=res.total
    loading.value=false
  })
}

//搜索按钮
const handleQuery = () => {
  queryParms.value.pageNum=1
  getList()
}

//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

//当组件挂载完成之后调用
onMounted(()=>{
  getList()
})

onMounted(()=>{
  getList()
})

</script>

<style scoped lang="scss">

</style>