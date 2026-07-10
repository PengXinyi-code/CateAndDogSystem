<template>
  <div class="animal-page">
    <div class="page-header">
      <h1 class="page-title">动物档案</h1>
      <div class="filter-section">
        <div class="filter-group">
          <span class="filter-label">名称</span>
          <el-input
              v-model="nameSearch"
              placeholder="请输入动物名称"
              clearable
              class="filter-control"
              @clear="handleFilterChange"
              @keyup.enter="handleFilterChange"
          >
            <template #append>
              <el-button @click="handleFilterChange">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="filter-group">
          <span class="filter-label">分类</span>
          <el-select
              v-model="categoryFilter"
              placeholder="猫 / 狗"
              clearable
              class="filter-control filter-control--short"
              @change="handleCategoryChange"
          >
            <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
          </el-select>
        </div>
        <div class="filter-group">
          <span class="filter-label">品种</span>
          <el-select
              v-model="breedFilter"
              placeholder="请选择品种"
              clearable
              class="filter-control"
              :disabled="!categoryFilter"
              @change="handleFilterChange"
          >
            <el-option v-for="item in breedOptions" :key="item.breedId" :label="item.name" :value="item.breedId" />
          </el-select>
        </div>
        <div class="filter-group">
          <span class="filter-label">领养状态</span>
          <el-radio-group v-model="adoptFilter" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="待领养">待领养</el-radio-button>
            <el-radio-button label="已领养">已领养</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="animals-grid">
      <div
          v-for="animal in animalList"
          :key="animal.id"
          class="animal-card"
          @click="gotoDetail(animal.id)"
      >
        <div class="card-image">
          <el-image
              :src="getImageUrl(animal.imageUrl)"
              fit="cover"
              class="animal-image"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="card-badges">
            <el-tag
                :type="animal.categoryId === 'cat' ? 'success' : 'primary'"
                class="category-badge"
            >
              {{ animal.categoryName }}
            </el-tag>
            <el-tag
                :type="animal.isAdopted ? 'success' : 'warning'"
                class="adopt-badge"
            >
              {{ animal.isAdopted ? '已领养' : '待领养' }}
            </el-tag>
          </div>
        </div>
        <div class="card-info">
          <h3 class="animal-name">{{ animal.name }}</h3>
          <div v-if="animal.breedName" class="animal-breed">{{ animal.breedName }}</div>
          <div class="animal-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ animal.location }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(animal.firstFoundTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && animalList.length === 0" class="empty-state">
      <el-empty description="暂无动物档案" />
    </div>

    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[8, 16, 24]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAnimal } from '@/api/animal/animals'
import { listCategory } from '@/api/animal/category'
import { listBreed } from '@/api/animal/breed'
import { Picture, Location, Clock } from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {resolveImageUrl} from '@/utils/image'

const baseUrl = import.meta.env.VITE_APP_BASE_API

const animalList = ref([])
const total = ref(0)
const loading = ref(false)
const nameSearch = ref('')
const categoryFilter = ref('')
const breedFilter = ref('')
const adoptFilter = ref('')
const categoryOptions = ref([])
const breedOptions = ref([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 8,
  name: null,
  categoryId: null,
  breedId: null,
  isAdopted: null,
  status: 'approved'
})

const getImageUrl = (imageUrl) => {
  return resolveImageUrl(imageUrl, baseUrl)
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

const router = useRouter()

const gotoDetail=(id) =>{
  router.push('/user/animalDetail/'+ id)
}

const getList = () => {
  loading.value = true
  const params = { ...queryParams.value }
  if (nameSearch.value) {
    params.name = nameSearch.value
  } else {
    delete params.name
  }
  if (categoryFilter.value) {
    params.categoryId = categoryFilter.value
  } else {
    delete params.categoryId
  }
  if (breedFilter.value) {
    params.breedId = breedFilter.value
  } else {
    delete params.breedId
  }
  // 确保始终只显示审核通过的动物
  params.status = 'approved'
  if (adoptFilter.value === '待领养') {
    params.isAdopted = false
  } else if (adoptFilter.value === '已领养') {
    params.isAdopted = true
  } else {
    delete params.isAdopted
  }
  listAnimal(params).then(response => {
    animalList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

const handleFilterChange = () => {
  queryParams.value.pageNum = 1
  getList()
}

const loadBreedOptions = (categoryId) => {
  if (!categoryId) {
    breedOptions.value = []
    return Promise.resolve()
  }
  return listBreed({ pageNum: 1, pageSize: 100, categoryId, enabled: true }).then(response => {
    breedOptions.value = response.rows || []
  })
}

const handleCategoryChange = (categoryId) => {
  breedFilter.value = ''
  breedOptions.value = []
  loadBreedOptions(categoryId).then(() => {
    handleFilterChange()
  })
}

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getList()
}

const handlePageChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

onMounted(() => {
  listCategory({ pageNum: 1, pageSize: 100, enabled: true }).then(response => {
    categoryOptions.value = response.rows || []
  })
  getList()
})
</script>

<style scoped>
.animal-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 5px 5px;
  min-height: calc(100vh - 70px);
}

.page-header {
  margin-bottom: 10px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin: 0 0 8px;
  padding-left: 10px;
  border-left: 4px solid #d79358;
  line-height: 1.25;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.filter-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.filter-control {
  width: 176px;
}

.filter-control--short {
  width: 118px;
}

.animals-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 8px;
}

.animal-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.animal-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  height: 185px;
  overflow: hidden;
}

.animal-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #f5f5f5;
  color: #ccc;
  font-size: 48px;
}

.card-badges {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.category-badge,
.adopt-badge {
  font-size: 11px;
}

.card-info {
  padding: 9px 10px 10px;
}

.animal-name {
  font-size: 15px;
  color: #333;
  margin: 0 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.25;
}

.animal-breed {
  color: #999;
  font-size: 12px;
  margin-bottom: 6px;
  line-height: 1.2;
}

.animal-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 12px;
  line-height: 1.2;
  min-width: 0;
}

.meta-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-item .el-icon {
  flex: 0 0 auto;
  font-size: 13px;
  color: #999;
}

.empty-state {
  padding: 30px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

@media (max-width: 1200px) {
  .animals-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .animals-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .filter-section {
    align-items: stretch;
    gap: 15px;
  }

  .filter-group {
    width: 100%;
  }

  .filter-control,
  .filter-control--short {
    flex: 1;
    width: auto;
  }
}

@media (max-width: 600px) {
  .animals-grid {
    grid-template-columns: 1fr;
  }
}
</style>
