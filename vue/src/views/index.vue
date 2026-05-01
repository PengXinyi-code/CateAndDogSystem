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
            <div style="font-size: 18px;font-width: bold;color: #333">数据统计</div>
          </div>

          <div style="padding: 20px">
            <div ref="chartRef" style="width: 100%;height: 200px"></div>
          </div>
        </div>

      </el-col>

    </el-row>

  </div>
</template>

<script setup>
import {Medal,User} from "@element-plus/icons-vue";
import {listAnimal} from "@/api/sccour/animals.js";
//import {listAdopt} from "@/api/sccour/adopt.js"
import * as echarts from "echarts";

const animalCount=ref(0);
const adoptCount=ref(0);

const chartRef=ref(null);
let chart = null;

//查询数据
const getData=async ()=>{
  //获取动物总数
  await listAnimal().then(res =>{
    animalCount.value=res.total
  })

  //获取领养申请数量
  await listAdopt().then(res =>{
    adoptCount.value=res.total
  })
  //初始化图表
  initChart()
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

}

onMounted(() => {
  getdata()
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
