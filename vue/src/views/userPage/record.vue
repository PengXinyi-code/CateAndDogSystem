<template>
  <div class="record-page">
    <div class="page-header">
      <p>在这里您可以查看所有的领养申请记录和状态</p>
    </div>

    <!-- 申请列表 -->
    <div v-if="adoptList.length > 0" class="requests-list">
      <div
          v-for="adopt in adoptList"
          :key="adopt.adoptId"
          class="request-card"
      >
        <div class="request-header">
          <h3 class="animal-name">{{ adopt.animalName }}</h3>
          <el-tag type="primary" v-if="adopt.status === '审核中'">{{ adopt.status }}</el-tag>
          <el-tag type="danger" v-if="adopt.status === '已拒绝'">{{ adopt.status }}</el-tag>
          <el-tag type="success" v-if="adopt.status === '已完成'">{{ adopt.status }}</el-tag>
        </div>

        <div class="request-details">
          <div class="detail-row">
            <span class="label">申请时间:</span>
            <span class="value">{{ adopt.createTime }}</span>
          </div>

          <div class="detail-row">
            <span class="label">申请人:</span>
            <span class="value">{{ adopt.name }}</span>
          </div>

          <div class="detail-row">
            <span class="label">联系电话:</span>
            <span class="value">{{ adopt.phone }}</span>
          </div>

          <div class="detail-row">
            <span class="label">电子邮箱:</span>
            <span class="value">{{ adopt.email }}</span>
          </div>

          <div class="detail-row">
            <span class="label">居住地址:</span>
            <span class="value">{{ adopt.address }}</span>
          </div>

          <div class="detail-row">
            <span class="label">职业:</span>
            <span class="value">{{ adopt.occupation }}</span>
          </div>

          <div class="detail-row">
            <span class="label">申请理由:</span>
            <span class="value">{{ adopt.reason }}</span>
          </div>

          <div class="detail-row">
            <span class="label">宠物经验:</span>
            <span class="value">{{ adopt.petExperience }}</span>
          </div>
        </div>

        <div class="request-footer">
          <div class="footer-info">
                <span class="process-info">
                  处理说明: {{ adopt.description }}
                </span>
          </div>
          <div class="footer-actions">
            <el-button
                v-if="adopt.status === '审核中'"
                @click="cancelRequest(adopt.adoptId)"
                type="danger"
                plain
                size="small"
            >
              撤销申请
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无领养申请记录">
        <el-button type="primary" @click="goToAdopt">去领养宠物</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {listAdopt, revoke} from "@/api/sccour/adopt.js";

// 使用 useRouter
const router = useRouter()

// // 模拟领养申请数据
// const adoptList = ref([
//   {
//     adoptId: 1,
//     animalName: '汤圆',
//     status: '审核中',
//     createTime: '2025-12-05T09:15:00',
//     name: '张三',
//     phone: '13800138000',
//     email: 'zhangsan@example.com',
//     address: '北京市朝阳区某某街道123号',
//     occupation: '软件工程师',
//     reason: '家里有小朋友，希望给孩子一个陪伴成长的伙伴',
//     petExperience: '3-5年',
//     description: '您的申请已被管理员接受，工作人员将在3个工作日内与您联系安排后续事宜'
//   },
//   {
//     adoptId: 2,
//     animalName: '小白',
//     status: '已拒绝',
//     createTime: '2025-12-10T14:30:00',
//     name: '张三',
//     phone: '13800138000',
//     email: 'zhangsan@example.com',
//     address: '北京市朝阳区某某街道123号',
//     occupation: '软件工程师',
//     reason: '想要一只猫咪陪伴自己',
//     petExperience: '1-3年',
//     description: '经审核，您的申请未通过，因为您目前的居住环境不适合养猫'
//   },
//   {
//     adoptId: 3,
//     animalName: '大黄',
//     status: '已完成',
//     createTime: '2025-11-20T16:45:00',
//     name: '张三',
//     phone: '13800138000',
//     email: 'zhangsan@example.com',
//     address: '北京市朝阳区某某街道123号',
//     occupation: '软件工程师',
//     reason: '一直想养一只忠诚的狗狗陪伴自己',
//     petExperience: '3-5年',
//     description: '领养已完成，感谢您的爱心'
//   }
// ])

// 撤销申请
const cancelRequest = (adoptId) => {
  ElMessageBox.confirm(
      '确定要撤销此领养申请吗？',
      '撤销申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        revoke(adoptId).then(res => {
          getList()
          ElMessage.success('申请已撤销')
        })
      })
}

// 跳转到领养页面
const goToAdopt = () => {
  router.push('/user/animal')
}

// 领养申请数据
const adoptList = ref([])

const getList = () => {
  listAdopt({pageNum: 1, pageSize: 100}).then(res => {
    adoptList.value = res.rows || []
  }).catch(err => {
    console.error('获取申请列表失败:', err)
    adoptList.value = []
  })
}

// 组件挂载时获取数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.record-page {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.page-header {
  text-align: center;
  font-size: 20px;
  color: #64748b;
}

.requests-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.request-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.request-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.animal-name {
  font-size: 22px;
  margin: 0;
  color: #2c3e50;
}

.request-details {
  padding: 20px;
}

.detail-row {
  display: flex;
  margin-bottom: 12px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.label {
  width: 100px;
  font-weight: bold;
  color: #2c3e50;
}

.value {
  flex: 1;
  color: #64748b;
}

.request-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.footer-info {
  flex: 1;
}

.process-info {
  font-size: 14px;
  color: #64748b;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}
</style>
