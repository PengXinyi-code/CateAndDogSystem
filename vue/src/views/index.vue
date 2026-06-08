<template xmlns:el-col="http://www.w3.org/1999/html">
  <div style="padding: 20px">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="card">
          <div>
            <div class="title">动物总数</div>
            <div class="value">{{animalCount}}</div>
          </div>
          <div class="icon animal-icon">
            <el-icon><Medal/></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="card">
          <div>
            <div class="title">审核通过档案数</div>
            <div class="value">{{approvedCount}}</div>
          </div>
          <div class="icon approved-icon">
            <el-icon><CircleCheck/></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="card">
          <div>
            <div class="title">待审核档案数</div>
            <div class="value">{{pendingCount}}</div>
          </div>
          <div class="icon pending-icon">
            <el-icon><Clock/></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="card">
          <div>
            <div class="title">领养申请数</div>
            <div class="value">{{adoptApplyCount}}</div>
          </div>
          <div class="icon adopt-icon">
            <el-icon><User/></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <div class="chart-card">
          <div style="padding: 20px; border-bottom: 1px solid #eee">
            <div style="font-size: 18px; font-weight: bold;color: #333">领养统计</div>
          </div>
          <div style="padding: 20px">
            <div ref="chartRef" style="width: 100%;height: 350px"></div>
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="chart-card">
          <div style="padding: 20px; border-bottom: 1px solid #eee">
            <div style="font-size: 18px; font-weight: bold;color: #333">种类状态分布</div>
          </div>
          <div style="padding: 20px">
            <div ref="pieChartRef" style="width: 100%;height: 350px"></div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { Medal, User, Clock, CircleCheck } from "@element-plus/icons-vue";
import { listAnimal, getAnimalStats } from "@/api/sccour/animals.js";
// 新增：导入领养申请接口（请确保接口路径与你的后端匹配）
import { listAdopt } from "@/api/sccour/adopt.js";
import * as echarts from "echarts";

const animalCount = ref(0);
const approvedCount = ref(0);
const pendingCount = ref(0);
// 新增：领养申请数初始值（仅新增这一行，不改动其他变量）
const adoptApplyCount = ref(0);

const adoptedCat = ref(0);
const adoptedDog = ref(0);
const unAdoptedCat = ref(0);
const unAdoptedDog = ref(0);

const chartRef = ref(null);
const pieChartRef = ref(null);
let chart = null;
let pieChart = null;

const getData = async () => {
  const [res, statsRes] = await Promise.all([
    listAnimal({ pageNum: 1, pageSize: 1000 }),
    getAnimalStats()
  ]);
  const stats = statsRes.data || {};
  const statValue = (name) => Number(stats[name] ?? stats[name.toUpperCase()] ?? 0);
  animalCount.value = statValue('total');
  approvedCount.value = statValue('approved');
  pendingCount.value = statValue('pending');
  countAll(res.rows);

  // 新增：调用你提供的listAdopt接口，获取领养申请总数（关键修改）
  try {
    // 传分页参数，和动物列表保持一致
    const adoptRes = await listAdopt({ pageNum: 1, pageSize: 1000 });
    // 取后端返回的total（分页总数），这就是领养申请的总数量
    adoptApplyCount.value = adoptRes.total;
    console.log("领养申请总数：", adoptApplyCount.value); // 控制台打印，方便调试
  } catch (error) {
    // 接口请求失败时的兜底处理
    console.error("获取领养申请数失败：", error);
    adoptApplyCount.value = 0; // 显示0，避免页面空白
  }

  renderChart();
};

const countAll = (rows) => {
  adoptedCat.value = 0;
  adoptedDog.value = 0;
  unAdoptedCat.value = 0;
  unAdoptedDog.value = 0;

  rows.forEach(item => {
    // 统计猫狗领养状态
    const isAdopt = item.isAdopted === true;
    if (item.species === "猫") {
      isAdopt ? adoptedCat.value++ : unAdoptedCat.value++;
    }
    if (item.species === "狗") {
      isAdopt ? adoptedDog.value++ : unAdoptedDog.value++;
    }
  });
};

const renderChart = () => {
  initChart();
  initPieChart();
};

// 柱状图：调整配色和显示
const initChart = () => {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value);

  const adoptTotal = adoptedCat.value + adoptedDog.value;
  const unAdoptTotal = unAdoptedCat.value + unAdoptedDog.value;

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['已领养', '未领养'] },
    yAxis: {
      type: 'value',
      max: Math.ceil(animalCount.value / 10) * 10
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: [adoptTotal, unAdoptTotal],
        itemStyle: {
          color: function(params) {
            return params.dataIndex === 0 ? '#64c5dc' : '#db94ef'
          }
        },
        label: { show: true, position: 'top', fontSize: 16 }
      }
    ]
  };
  chart.setOption(option);
};

// 饼图
const initPieChart = () => {
  if (!pieChartRef.value) return;
  pieChart = echarts.init(pieChartRef.value);

  const pieData = [
    { name: '已领养猫', value: adoptedCat.value, itemStyle: { color: '#4FC3F7' } },
    { name: '未领养猫', value: unAdoptedCat.value, itemStyle: { color: '#0288D1' } },
    { name: '已领养狗', value: adoptedDog.value, itemStyle: { color: '#81C784' } },
    { name: '未领养狗', value: unAdoptedDog.value, itemStyle: { color: '#388E3C' } },
  ];

  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    legend: { bottom: 'bottom', textStyle: { fontSize: 13 } },
    series: [{
      name: '分布',
      type: 'pie',
      radius: ['40%', '75%'],
      data: pieData,
      label: { show: true, formatter: '{b}: {c}', fontSize: 14 }
    }]
  };
  pieChart.setOption(option);
};

onMounted(() => {
  getData();
});
onActivated(() => {
  getData();
});
</script>

<style scoped>
.card{
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 120px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 #e3e2e2;
}
.title{ font-size: 16px; color: #666; margin-bottom: 10px; }
.value{ font-size: 28px; font-weight: bold; }
.icon{
  width: 60px; height: 60px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px;
}
.animal-icon{ background: #f8f6f6; color: #409eff; }
.approved-icon{ background: #f0f9eb; color: #67c23a; }
.pending-icon{ background: #fff7e6; color: #faad14; }
.adopt-icon{ background: #f8f6f6; color: #e12759; }
.chart-card{
  background: #fff; border-radius: 4px;
  box-shadow: 0 2px 12px 0 #e3e2e2;
}
</style>
