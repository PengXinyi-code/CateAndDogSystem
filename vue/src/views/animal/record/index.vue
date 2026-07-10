<template>
  <div class="app-container">
    <h2>领养记录</h2>

    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="动物名称" prop="animalName">
        <el-input v-model="queryParams.animalName" placeholder="请输入动物名称" clearable />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="recordList" border>
      <el-table-column label="动物名称" prop="animalName" width="120" />
      <el-table-column label="动物ID" prop="animalId" width="150" />
      <el-table-column label="领养人姓名" prop="userName" width="120" />
      <el-table-column label="领养人ID" prop="userId" width="100" />
      <el-table-column label="联系电话" prop="phone" width="120" />
      <el-table-column label="电子邮箱" prop="email" width="180" />
      <el-table-column label="居住地址" prop="address" width="200" />
      <el-table-column label="职业/学院" prop="occupation" width="150" />
      <el-table-column label="领养时间" prop="adoptTime" width="180" />
      <el-table-column label="备注" prop="remark" width="150" />
    </el-table>

    <!-- 分页 -->
    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, toRefs, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const recordList = ref([])
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    animalName: null,
    phone: null
  }
})

const { queryParams } = toRefs(data)

const getList = () => {
  loading.value = true
  request({
    url: '/api/adoptions/record/list',
    method: 'get',
    params: queryParams.value
  }).then(response => {
    recordList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.value.animalName = null
  queryParams.value.phone = null
  handleQuery()
}

onMounted(() => {
  getList()
})
</script>