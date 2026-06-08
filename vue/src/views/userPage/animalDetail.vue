<template>
  <div class="detail-container">
    <div class="detail-header">
      <el-page-header @back="goBack" content="宠物详情">
      </el-page-header>
    </div>

    <div class="detail-content">
      <div class="animal-detail-card">
        <div class="animal-image-section">
          <img :src="resolveImageUrl(animal.imageUrl)" :alt="animal.name" class="animal-main-image">
        </div>

        <div class="animal-info-section">
          <div class="animal-basic-info">
            <h1 class="animal-name">{{ animal.name }}</h1>
            <div class="animal-meta"></div>
          </div>

          <div class="animal-description">
            <h3>介绍</h3>
            <p>{{ animal.description }}</p>
          </div>

        <div class="animal-actions">
          <el-button type="primary" size="large" @click="applyAdopt">
            申请领养
          </el-button>
          <el-button size="large" @click="router.push('/user/record')">
            查看我的申请
          </el-button>
        </div>
      </div>
    </div>
    </div>

    <!-- 领养申请表单对话框 -->
    <vxe-modal v-model="open" title="申请领养" width="500px" show-maximize showFooter resize>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="申请人姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入您的姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入您的联系电话" />
        </el-form-item>

        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入您的电子邮箱" />
        </el-form-item>

        <el-form-item label="居住地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入您的详细居住地址" type="textarea" />
        </el-form-item>

        <el-form-item label="学院专业" prop="occupation">
          <el-input v-model="form.occupation" placeholder="请输入您的学院和专业" />
        </el-form-item>

        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="form.reason" placeholder="请简要说明申请领养的理由" type="textarea" />
        </el-form-item>

        <el-form-item label="宠物经验" prop="petExperience">
          <el-select v-model="form.petExperience" placeholder="请选择您的宠物饲养经验" style="width: 100%">
            <el-option label="无经验" value="无经验"></el-option>
            <el-option label="1年以下" value="1年以下"></el-option>
            <el-option label="1-3年" value="1-3年"></el-option>
            <el-option label="3-5年" value="3-5年"></el-option>
            <el-option label="5年以上" value="5年以上"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="open = false">取消</el-button>
          <el-button type="primary" @click="submitAdoptionForm">提交申请</el-button>
        </span>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {ElMessage} from 'element-plus'
import {getAnimal} from "@/api/sccour/animals.js";
import {addAdopt} from "@/api/sccour/adopt.js";
import {resolveImageUrl} from "@/utils/image";

const baseUrl = import.meta.env.VITE_APP_BASE_API

const router = useRouter()
const route = useRoute()

// 领养申请表单相关
const open = ref(false)
const formRef = ref()

const form = ref({
  animalId: null,
  name: null,
  phone: null,
  email: null,
  address: null,
  occupation: null,
  reason: null,
  petExperience: null
})

const rules = {
  name: [
    { required: true, message: '请输入您的姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入您的联系电话', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入您的电子邮箱', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入您的详细居住地址', trigger: 'blur' }
  ],
  occupation: [
    { required: true, message: '请输入您的学院和专业', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请简要说明申请领养的理由', trigger: 'blur' }
  ],
  petExperience: [
    { required: true, message: '请选择您的宠物饲养经验', trigger: 'change' }
  ]
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 申请领养
const applyAdopt = () => {
  form.value = {
    animalId: id.value,
    name: null,
    phone: null,
    email: null,
    address: null,
    occupation: null,
    reason: null,
    petExperience: null
  }
  // 打开领养申请表单对话框
  open.value = true
}

// 提交领养申请表单
const submitAdoptionForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      addAdopt(form.value).then(res => {
        ElMessage.success('申请已提交，我们会尽快联系您！')
        open.value = false
        // 重置表单
        formRef.value.resetFields()
        //跳转到我的申请页面
        router.push('/user/record')
      })
    }
  })
}

// 萌宠信息
const animal = ref({})

const id = ref('')

onMounted(() => {
  id.value = route.params.id
  getAnimal(id.value).then(res => {
    animal.value = res.data
  })
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.animal-detail-card {
  display: flex;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.animal-image-section {
  flex: 1;
  min-height: 500px;
}

.animal-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.animal-info-section {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.animal-name {
  font-size: 32px;
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.animal-status {
  margin-bottom: 10px;
}

.animal-description h3,
.animal-extra-info h3 {
  font-size: 20px;
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.animal-description p {
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

.animal-extra-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.animal-extra-info li {
  display: flex;
  margin-bottom: 10px;
  color: #64748b;
}

.label {
  font-weight: bold;
  width: 100px;
  display: inline-block;
}

.animal-actions {
  display: flex;
  gap: 15px;
  margin-top: 10px;
}


.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
