<template xmlns:el-col="http://www.w3.org/1999/html">
  <div style="padding: 20px">
    <el-row :gutter="20">
      <el-col :span="12">
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

      <el-col :span="12">
        <div class="card">
          <div>
            <div class="title">领养申请数</div>
            <div class="value">{{adoptCount}}</div>
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
            <div style="font-size: 18px; font-width: bold;color: #333">数据统计</div>
          </div>

          <div style="padding: 20px">
            <div ref="chartRef" style="width: 100%;height: 300px"></div>
          </div>
        </div>

      </el-col>

      <el-col :span="12">
        <div class="chart-card">
          <div style="padding: 20px; border-bottom: 1px solid #eee">
            <div style="font-size: 18px; font-width: bold;color: #333">萌宠种类分布</div>
          </div>

          <div style="padding: 20px">
            <div ref="pieChartRef" style="width: 100%;height: 300px"></div>
          </div>
        </div>

      </el-col>

    </el-row>

  </div>
</template>

<script setup>
import {Medal,User} from "@element-plus/icons-vue";
import {listAnimal} from "@/api/sccour/animals.js";
//领养模块完成后取消注释
//import {listAdopt} from "@/api/sccour/adopt.js";
import * as echarts from "echarts";

const animalCount=ref(0);
const adoptCount=ref(0);

const chartRef=ref(null);
const pieChartRef=ref(null);
let chart = null;
let pieChart = null;


const getData=async ()=>{
  // 1. 先获取总条数，计算总页数
  const res = await listAnimal({ pageNum: 1, pageSize: 1000 })
  animalCount.value = res.total;

  // 2. 直接统计所有返回的 rows
  getPieCategoryData(res.rows)

  //领养模块完成后取消注释
  // //获取领养申请数量
  // await listAdopt().then(res =>{
  //   adoptCount.value=res.total
  // })

  initChart()
}

const getPieCategoryData = (animals)=>{
  const categoryMap={}
  animals.forEach(animal=>{
    console.log("动物数据：", animals)
    const category = animal.species || "未分类";
    categoryMap[category]=(categoryMap[category]||0)+1;
  })

  const categoryData=[]
  Object.keys(categoryMap).forEach((key) => {
    categoryData.push({
      name: key,
      value: categoryMap[key]
    });
  })

  initPieChart(categoryData)
}

//初始化饼状图
const initPieChart = (categoryData) => {
  if (!pieChartRef.value) return

  pieChart=echarts.init(pieChartRef.value);

  const option = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      bottom:'bottom',
    },
    series: [
      {
        name: '萌宠种类分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold',
          }
        },
        labelLine: {
          show: false,
        },
        data: categoryData,
      }
    ]
  }
  pieChart.setOption(option)
}
//初始化柱状图
const initChart = () => {
  if(!chartRef.value) return
  chart = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['总数']
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '萌宠数量',
        data: [animalCount.value],
        barGap: 0,
        type: 'bar',
      },
      {
        name: '领养数量',
        data: [adoptCount.value],
        type: 'bar',
      }
    ]
  }
  chart.setOption(option);
  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  getData()
})
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

.title{
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.value{
  font-size: 28px;
  font-weight: bold;
}

.icon{
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.animal-icon{
  background-color: #f8f6f6;
  color: #409eff;
}

.adopt-icon{
  background-color: #f8f6f6;
  color: #e12759;
}

.chart-card{
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 #e3e2e2;
}
</style>
