<template>
  <div class="animal-page">
    <div class="page-header">
      <h1 class="page-title">动物档案</h1>
      <div class="filter-section">
        <div class="filter-group">
          <span class="filter-label">名称搜索：</span>
          <el-input
              v-model="nameSearch"
              placeholder="请输入动物名称"
              clearable
              style="width: 200px"
              @clear="handleFilterChange"
              @keyup.enter="handleFilterChange"
          >
            <template #append>
              <el-button @click="handleFilterChange">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="filter-group">
          <span class="filter-label">种类筛选：</span>
          <el-radio-group v-model="speciesFilter" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="猫">猫咪</el-radio-button>
            <el-radio-button label="狗">狗狗</el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">领养状态：</span>
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
                :type="animal.species === '猫' ? 'success' : 'primary'"
                class="species-badge"
            >
              {{ animal.species }}
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
import { listAnimal } from '@/api/sccour/animals'
import { Picture, Location, Clock } from '@element-plus/icons-vue'

const baseUrl = import.meta.env.VITE_APP_BASE_API

const animalList = ref([])
const total = ref(0)
const loading = ref(false)
const nameSearch = ref('')
const speciesFilter = ref('')
const adoptFilter = ref('')

const queryParams = ref({
  pageNum: 1,
  pageSize: 8,
  name: null,
  species: null,
  isAdopted: null,
  status: 'approved'
})

const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http')) return imageUrl
  return baseUrl + imageUrl
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

const getList = () => {
  loading.value = true
  const params = { ...queryParams.value }
  if (nameSearch.value) {
    params.name = nameSearch.value
  } else {
    delete params.name
  }
  if (speciesFilter.value) {
    params.species = speciesFilter.value
  } else {
    delete params.species
  }
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

const handleSizeChange = (val) => {
  queryParams.value.pageSize = val
  getList()
}

const handlePageChange = (val) => {
  queryParams.value.pageNum = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.animal-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: calc(100vh - 70px);
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 20px;
  padding-left: 15px;
  border-left: 4px solid #d79358;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.animals-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.animal-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.animal-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  height: 200px;
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
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.species-badge,
.adopt-badge {
  font-size: 12px;
}

.card-info {
  padding: 15px;
}

.animal-name {
  font-size: 18px;
  color: #333;
  margin: 0 0 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.animal-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

.meta-item .el-icon {
  font-size: 14px;
  color: #999;
}

.empty-state {
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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
    flex-direction: column;
    gap: 15px;
  }
}

@media (max-width: 600px) {
  .animals-grid {
    grid-template-columns: 1fr;
  }
}
</style>
