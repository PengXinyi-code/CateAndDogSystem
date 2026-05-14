<template>
  <div>
    <!-- 顶部搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="动物名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入动物名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="种类" prop="species">
        <el-select v-model="queryParams.species" placeholder="请选择种类" clearable style="width: 200px">
          <el-option label="猫" value="猫" />
          <el-option label="狗" value="狗" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="审核状态" clearable style="width: 200px">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部按钮 -->
    <el-row :gutter="20" style="padding: 10px 0">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" @click="handleUpdate" :disabled="single">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleDelete" :disabled="multiple">删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table
        @row-click="clickRow"
        :data="animalList"
        ref="tableRef"
        highlight-current-row
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" :index="indexMethod" />

      <el-table-column label="动物图片" align="center" width="100">
        <template #default="scope">
          <el-image
              v-if="scope.row.imageUrl"
              :src="handleImageSrc(scope.row.imageUrl)"
              :preview-src-list="[handleImageSrc(scope.row.imageUrl)]"
              style="width: 50px; height: 50px; border-radius: 4px; object-fit: cover;"
              fit="cover"
          />
          <span v-else style="color: #999; font-size: 12px;">无图片</span>
        </template>
      </el-table-column>

      <el-table-column label="动物名称" align="center" prop="name" />
      <el-table-column label="种类" align="center" prop="species">
        <template #default="scope">
          <el-tag :type="scope.row.species === '猫' ? 'success' : (scope.row.species === '狗' ? 'primary' : 'info')">
            {{ scope.row.species }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发现位置" align="center" prop="location" show-overflow-tooltip />

      <!-- 新增：介绍列 -->
      <el-table-column label="介绍" align="center" prop="description" show-overflow-tooltip width="200" />

      <el-table-column label="发现时间" align="center" prop="firstFoundTime" width="160" />

      <el-table-column label="领养状态" align="center" prop="isAdopted">
        <template #default="scope">
          <el-tag :type="scope.row.isAdopted ? 'success' : 'info'">
            {{ scope.row.isAdopted ? '已领养' : '待领养' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="审核状态" align="center" prop="status">
        <template #default="scope">
          <el-tag
              :type="scope.row.status === 'approved' ? 'success'
              : scope.row.status === 'rejected' ? 'danger'
              : 'warning'"
              size="small"
          >
            {{ scope.row.status === 'approved' ? '已通过'
              : scope.row.status === 'rejected' ? '已拒绝'
                  : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click.stop="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click.stop="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改动物档案对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="animalRef" :model="form" :rules="rules" label-width="80px">

        <el-form-item label="动物图片" prop="imageUrl">
          <el-upload
              class="animal-uploader"
              :action="baseUrl + '/api/common/upload'"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              accept="image/*"
          >
            <el-image
                v-if="form.imageUrl"
                :src="handleImageSrc(form.imageUrl)"
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
          <el-input v-model="form.name" placeholder="请输入动物名称" />
        </el-form-item>

        <el-form-item label="种类" prop="species">
          <el-select v-model="form.species" placeholder="请选择种类" style="width: 100%">
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="发现时间" prop="firstFoundTime">
          <el-date-picker
              clearable
              v-model="form.firstFoundTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择发现时间"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="发现位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入发现位置" />
        </el-form-item>

        <!-- 新增：介绍输入框 -->
        <el-form-item label="介绍" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入动物介绍" />
        </el-form-item>

        <el-form-item label="是否领养" prop="isAdopted">
          <el-switch
              v-model="form.isAdopted"
              active-text="是"
              inactive-text="否"
              :active-value="true"
              :inactive-value="false"
          />
        </el-form-item>

        <el-form-item label="审核状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择审核状态" style="width: 100%">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { Plus } from "@element-plus/icons-vue";
import { listAnimal, getAnimal, delAnimal, addAnimal, updateAnimal } from "@/api/sccour/animals.js";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();

const animalList = ref([]);
const total = ref(0);
const loading = ref(false);
const title = ref("");
const open = ref(false);

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  name: null,
  species: null,
  status: null
});

const form = ref({});

const rules = ref({
  name: [{ required: true, message: "动物名称不能为空", trigger: "blur" }],
  species: [{ required: true, message: "种类不能为空", trigger: "change" }],
  location: [{ required: true, message: "发现位置不能为空", trigger: "blur" }],
  firstFoundTime: [{ required: true, message: "发现时间不能为空", trigger: "blur" }]
});

const ids = ref([]);
const single = ref(true);
const multiple = ref(true);

const tableRef = ref();
const queryRef = ref();
const animalRef = ref();

const baseUrl = import.meta.env.VITE_APP_BASE_API;

const handleImageSrc = (src) => {
  if (!src) return '';
  if (src.startsWith('http')) return src;
  if (src.startsWith('/uploads')) {
    return baseUrl + src;
  }
  return baseUrl + '/uploads/images/' + src;
};

const getList = () => {
  loading.value = true;
  listAnimal(queryParams.value).then(response => {
    animalList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryRef.value.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

const clickRow = (row) => {
  tableRef.value.clearSelection();
  tableRef.value.toggleRowSelection(row, true);
};

const indexMethod = (index) => {
  const pageNum = queryParams.value.pageNum - 1;
  return pageNum > 0 ? index + 1 + pageNum * queryParams.value.pageSize : index + 1;
};

const cancel = () => {
  open.value = false;
  reset();
};

const reset = () => {
  form.value = {
    id: null,
    name: null,
    species: null,
    firstFoundTime: null,
    location: null,
    description: null,
    isAdopted: false,
    status: 'pending',
    imageUrl: null
  };
  if (animalRef.value) {
    animalRef.value.resetFields();
  }
};

const handleAdd = () => {
  reset();
  open.value = true;
  title.value = "添加动物档案";
};

const handleUpdate = (row) => {
  reset();
  const id = row.id || ids.value[0];
  getAnimal(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改动物档案";
  });
};

const handleDelete = (row) => {
  const animalIds = row.id ? row.id : ids.value.join(',');
  ElMessageBox.confirm('是否确认删除选中的动物档案?', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    return delAnimal(animalIds);
  }).then(() => {
    getList();
    ElMessage.success("删除成功");
  }).catch(() => {});
};

const submitForm = () => {
  animalRef.value.validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateAnimal(form.value).then(() => {
          ElMessage.success("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addAnimal(form.value).then(() => {
          ElMessage.success("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
};

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) { ElMessage.error('只能上传图片文件！'); return false; }
  if (!isLt5M)  { ElMessage.error('图片大小不能超过 5MB！'); return false; }
  return true;
};

const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    form.value.imageUrl = res.url;
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(res.msg || '图片上传失败');
  }
};

onMounted(() => {
  getList();

  if (route.query.fromRecognition === 'true' && route.query.imageUrl) {
    reset();
    form.value.imageUrl = route.query.imageUrl;
    open.value = true;
    title.value = "添加动物档案";
    ElMessage.success('已自动填充识别图片，请完善动物信息');
  }
});
</script>

<style scoped>
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