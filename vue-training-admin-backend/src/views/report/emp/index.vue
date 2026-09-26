<script setup>
import { onMounted } from 'vue';
import * as echarts from 'echarts';
import { queryEmpPositionApi, queryEmpGenderApi } from '@/api/report';

//钩子函数
onMounted(() => {
  initPositionChart();
  initGenderChart();
  window.addEventListener('resize', resizeCharts);
})

//图表实例(窗口缩放时用)
let positionChart = null;
let genderChart = null;
const resizeCharts = () => {
  if(positionChart) positionChart.resize();
  if(genderChart) genderChart.resize();
}

//员工职位统计 - 柱状图
const initPositionChart = async () => {
  const result = await queryEmpPositionApi();
  if(!result.code) return;
  renderBar(result.data.positions, result.data.counts);
}

//员工性别统计 - 饼图
const initGenderChart = async () => {
  const result = await queryEmpGenderApi();
  if(!result.code) return;
  renderPie(result.data);
}

//渲染柱状图
function renderBar(positions, counts){
  positionChart = echarts.init(document.getElementById('container1'));
  positionChart.setOption({
    title: { text: '员工职位统计', subText: '', textStyle: { fontSize: 20 }, left: 'center' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    tooltip: {},
    xAxis: { data: positions },
    yAxis: {},
    series: [{
      name: '人数',
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#ffbf61' },
          { offset: 1, color: '#dd5f85' }
        ])
      }
    }]
  })
}

//渲染饼图
function renderPie(list){
  genderChart = echarts.init(document.getElementById('container2'));
  genderChart.setOption({
    title: { text: '员工性别统计', subText: '', textStyle: { fontSize: 20 }, left: 'center' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    tooltip: { trigger: 'item' },
    legend: { top: '10%', left: 'center' },
    series: [{
      name: '性别',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      top: '5%',
      itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
      data: list
    }]
  })
}
</script>

<template>
  <div class="report_container" id="container1"></div>
  <div class="report_container" id="container2"></div>
</template>

<style scoped>
.report_container {
  width: 49%;
  height: 90%;
  float: left;
  margin-left: 5px;
}
#container1 {
  border-right: 1px dashed #ccc;
}
</style>
