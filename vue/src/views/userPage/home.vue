<template>
  <div>
    <div class="hero-section">
      <el-carousel :interval="5000"
                   arrow="always"
                   indicator-position="none"
                   height="100%"
                   autoplay
      >
        <el-carousel-item v-for="banner in bannerList" :key="banner.bannerId">
          <img :src="baseUrl + banner.image" alt="" class="carousel-image">
        </el-carousel-item>
      </el-carousel>
      <div class="hero-bg-overlay"/>
      <div class="hero-glass-card">
        <span class="badge">💗 每一个生命都值得被温柔以待</span>
        <div class="hero-btn">
          <el-button class="custom-btn btn-filled" @click="toSection()">
            寻找你的缘分
          </el-button>
        </div>
        <h1 class="hero-title">用爱终止流浪<br>给他们一个温暖的家</h1>
        <p class="hero-desc">
          每一份领养, 都是爱的延续
        </p>

        <div class="recognition-section">
          <p class="recognition-title">📷 上传动物照片，智能识别</p>
          <el-upload
              class="recognition-upload"
              drag
              action="#"
              :http-request="handleRecognition"
              :before-upload="beforeUpload"
              :show-file-list="false"
              accept="image/*"
          >
            <div v-if="!recognitionResult" class="upload-placeholder">
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <p class="upload-text">点击或拖拽上传图片</p>
              <p class="upload-hint">支持 JPG、PNG 格式</p>
            </div>
            <div v-else class="recognition-result">
              <img :src="previewImage" class="preview-image" />
              <div class="result-info">
                <div v-if="recognitionResult.matched" class="matched-info">
                  <h3 class="result-title">
                    <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
                    识别成功
                  </h3>
                  <p class="animal-name">名称：{{ recognitionResult.animal.name }}</p>
                  <p>种类：{{ recognitionResult.animal.species }}</p>
                  <p>位置：{{ recognitionResult.animal.location }}</p>
                  <p>领养状态：{{ recognitionResult.animal.isAdopted ? '已领养' : '待领养' }}</p>
                  <el-button type="primary" size="small" @click.stop="resetRecognition">
                    重置
                  </el-button>
                </div>
                <div v-else class="not-matched-info">
                  <h3 class="result-title">
                    <el-icon color="#e6a23c"><WarningFilled /></el-icon>
                    未匹配到动物
                  </h3>
                  <p class="result-message">{{ recognitionResult.message }}</p>
                  <el-button type="warning" size="small" @click.stop="goToCreateProfile">
                    新建档案
                  </el-button>
                </div>
              </div>
            </div>
          </el-upload>
        </div>
      </div>
    </div>

    <div class="content-section" id="adopt-section">
      <div class="section-header">
        <h2 class="section-title">遇见你的<span class="hand-write">Soulmate</span></h2>
      </div>

      <div class="animals-grid">
        <div v-for="animal in animalList"
             :key="animal.animalId"
             class="pet-card"
             @click="gotoDetail(animal.animalId)"
        >
          <div class="pet-img-wrapper">
            <img :src="baseUrl + animal.image" alt="">
            <div class="pet-status-badge">
              {{ animal.status }}
            </div>
          </div>
          <div class="pet-info">
            <div class="pet-header">
              <h3>{{ animal.name }}</h3>
              <span class="gender-icon" :class="animal.gender === '公' ? 'male' : 'female' ">
                <el-icon>
                  <Male v-if="animal.gender === '公'"/>
                  <Female v-else/>
                </el-icon>
              </span>
            </div>
            <div class="pet-tags">
              <span class="tag">{{ animal.age }}</span>
            </div>
            <p class="pet-desc">{{ animal.description }}</p>
          </div>
        </div>
      </div>

      <div class="center-action">
        <el-button class="custom-btn btn-ghost"
                   icon="ArrowRight"
                   @click="router.push('/user/animal')"
        >
          浏览全部萌宠
        </el-button>
      </div>

    </div>

    <el-dialog title="添加动物档案" v-model="addDialogVisible" width="500px" append-to-body>
      <el-form ref="animalFormRef" :model="addForm" :rules="addRules" label-width="80px">
        <el-form-item label="动物图片" prop="imageUrl">
          <el-upload
              class="animal-uploader"
              :action="baseUrl + '/api/common/upload'"
              :show-file-list="false"
              :before-upload="beforeUploadImage"
              :on-success="handleUploadSuccess"
              accept="image/*"
          >
            <el-image
                v-if="addForm.imageUrl"
                :src="baseUrl + addForm.imageUrl"
                style="width: 120px; height: 120px; border-radius: 8px; object-fit: cover;"
                fit="cover"
            />
            <div v-else class="upload-placeholder">
              <el-icon style="font-size: 28px; color: #c0c4cc;"><Plus /></el-icon>
              <div style="font-size: 12px; color: #8c939d; margin-top: 6px;">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="动物名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入动物名称" />
        </el-form-item>
        <el-form-item label="种类" prop="species">
          <el-select v-model="addForm.species" placeholder="请选择种类" style="width: 100%">
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现时间" prop="firstFoundTime">
          <el-date-picker
              clearable
              v-model="addForm.firstFoundTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择发现时间"
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发现位置" prop="location">
          <el-input v-model="addForm.location" placeholder="请输入发现位置" />
        </el-form-item>
        <el-form-item label="是否领养" prop="isAdopted">
          <el-switch
              v-model="addForm.isAdopted"
              active-text="是"
              inactive-text="否"
              :active-value="true"
              :inactive-value="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAddForm">确定</el-button>
          <el-button @click="cancelAdd">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {listBanner} from "@/api/sccour/banner.js";
import {listAnimal, addAnimal} from "@/api/sccour/animals.js";
import {useRouter} from "vue-router";
import {Female, House, Male, UploadFilled, CircleCheckFilled, WarningFilled, Plus} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request";

const baseUrl = import.meta.env.VITE_APP_BASE_API

const toSection = () => {
  const el = document.getElementById(`adopt-section`)
  if (el) el.scrollIntoView({behavior: 'smooth', block: 'start'})
}

const bannerList = ref([])
const animalList = ref([])
const recognitionResult = ref(null)
const previewImage = ref('')

const router = useRouter()

const gotoDetail = (animalId) => {
  router.push('/user/animalDetail/' + animalId)
}

const viewAnimalDetail = (animalId) => {
  router.push('/user/animal/' + animalId)
}

const addDialogVisible = ref(false)
const animalFormRef = ref(null)
const addForm = ref({
  name: null,
  species: null,
  firstFoundTime: null,
  location: null,
  isAdopted: false,
  status: 'pending',
  imageUrl: null
})

const addRules = ref({
  name: [{ required: true, message: "动物名称不能为空", trigger: "blur" }],
  species: [{ required: true, message: "种类不能为空", trigger: "change" }],
  location: [{ required: true, message: "发现位置不能为空", trigger: "blur" }],
  firstFoundTime: [{ required: true, message: "发现时间不能为空", trigger: "blur" }]
})

const goToCreateProfile = () => {
  if (recognitionResult.value && !recognitionResult.value.matched && previewImage.value) {
    const formData = new FormData()
    const blob = dataURLtoBlob(previewImage.value)
    formData.append('file', blob, 'recognition_' + Date.now() + '.jpg')
    
    request({
      url: '/api/common/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then(response => {
      if (response.code === 200) {
        addForm.value.imageUrl = response.url
        addDialogVisible.value = true
      } else {
        ElMessage.error('图片上传失败')
        addDialogVisible.value = true
      }
    }).catch(error => {
      ElMessage.error('图片上传失败：' + (error.message || '未知错误'))
      addDialogVisible.value = true
    })
  } else {
    addDialogVisible.value = true
  }
}

const dataURLtoBlob = (dataurl) => {
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}

const beforeUploadImage = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    addForm.value.imageUrl = res.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

const submitAddForm = () => {
  animalFormRef.value.validate(valid => {
    if (valid) {
      addAnimal(addForm.value).then(() => {
        ElMessage.success("新增成功")
        addDialogVisible.value = false
        resetAddForm()
      })
    }
  })
}

const cancelAdd = () => {
  addDialogVisible.value = false
  resetAddForm()
}

const resetAddForm = () => {
  addForm.value = {
    name: null,
    species: null,
    firstFoundTime: null,
    location: null,
    isAdopted: false,
    status: 'pending',
    imageUrl: null
  }
  if (animalFormRef.value) {
    animalFormRef.value.resetFields()
  }
}

const query = ref({
  pageNum: 1,
  pageSize: 4,
  status: '可领养'
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file)
  return true
}

const handleRecognition = (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  request({
    url: '/api/recognition/identify',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(response => {
    recognitionResult.value = response
    if (response.matched) {
      ElMessage.success('识别成功！')
    } else {
      ElMessage.warning(response.message || '未匹配到动物')
    }
  }).catch(error => {
    ElMessage.error('识别失败：' + (error.message || '未知错误'))
    recognitionResult.value = null
  })
}

const resetRecognition = () => {
  recognitionResult.value = null
  previewImage.value = ''
}

onMounted(() => {
  listBanner().then(res => {
    bannerList.value = res.rows
  })

  listAnimal(query.value).then(res => {
    animalList.value = res.rows
  })
})

</script>

<style scoped>
.hero-section {
  position: relative;
  height: 93vh;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-carousel) {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

:deep(.el-carousel__container) {
  height: 100%;
}

:deep(.el-carousel__item) {
  height: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: relative;
}

.hero-bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0.1) 100%);
}

.hero-glass-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  padding: 40px 60px;
  border-radius: 30px;
  border: 1px solid #FFFFFF4C;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 700px;
  color: #fff;
  text-align: left;
}

.badge {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
  margin-bottom: 20px;
}

.hero-btn {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 28px;
  line-height: 1.3;
  margin-bottom: 12px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.hero-desc {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 25px;
  line-height: 1.5;
}

.recognition-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(8px);
}

.recognition-title {
  font-size: 16px;
  margin: 0 0 15px;
  text-align: center;
  font-weight: 600;
}

.recognition-upload {
  width: 100%;
}

.recognition-upload :deep(.el-upload-dragger) {
  width: 100%;
  padding: 20px 15px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px dashed rgba(255, 255, 255, 0.4);
  border-radius: 12px;
}

.recognition-upload :deep(.el-upload-dragger:hover) {
  border-color: #ff8e6e;
  background: rgba(255, 255, 255, 0.15);
}

.upload-placeholder {
  text-align: center;
}

.upload-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 8px 0 4px;
}

.upload-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.recognition-result {
  text-align: center;
}

.preview-image {
  width: 100%;
  max-height: 150px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}

.result-info {
  color: #fff;
}

.result-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 16px;
}

.matched-info .animal-name {
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0 4px;
}

.matched-info p,
.not-matched-info p {
  font-size: 13px;
  opacity: 0.85;
  margin: 4px 0;
}

.result-message {
  margin-bottom: 10px !important;
}

.custom-btn {
  padding: 12px 32px;
  border-radius: 50px;
  border: none;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-filled {
  background: #ff8e6e;
  color: #ffffff;
  box-shadow: 0 10px 20px rgba(255, 142, 110, 0.4);
}

.btn-filled:hover {
  background: #e66a4c;
  transform: translateY(-2px);
}

.content-section {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 50px;
}

.section-title {
  font-size: 36px;
  color: #2c3e50;
  margin-bottom: 15px;
}

.hand-write {
  font-family: 'Zcool KuaiLe', cursive;
  color: #ff8e6e;
}

.animals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 30px;
}

.pet-card {
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.pet-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.pet-img-wrapper {
  height: 240px;
  position: relative;
  overflow: hidden;
}

.pet-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.pet-card:hover .pet-img-wrapper img {
  transform: scale(1.05);
}

.pet-status-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  color: #2c3e50;
}

.pet-info {
  padding: 20px;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.pet-header h3 {
  font-size: 20px;
  margin: 0;
}

.gender-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #ffffff;
}

.gender-icon.male {
  background: #89CFF0;
}

.gender-icon.female {
  background: #FFB7B2;
}

.pet-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  background: #f8f9fa;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  color: #64748b;
}

.pet-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.center-action {
  text-align: center;
  margin-top: 50px;
}

.btn-ghost {
  background: transparent;
  border: 2px solid #64748b;
  color: #64748b;
}

.btn-ghost:hover {
  transform: translateY(-2px);
}

.animal-uploader :deep(.el-upload) {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.2s;
  display: block;
}
.animal-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.upload-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
