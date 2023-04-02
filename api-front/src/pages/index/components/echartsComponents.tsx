
import '@umijs/max';
import React, {useEffect} from 'react';
import * as echarts from "echarts";
export type Props = {
  xAxisData:[],
  seriesDataUser:[],
  seriesDataInterface:[],
  seriesDataOrder:[]
};
const Echarts: React.FC<Props> = (props) => {
  const {xAxisData,seriesDataUser,seriesDataInterface,seriesDataOrder} = props
  useEffect(() => {
    if (xAxisData ){
      //数据存在再进行渲染
      const myChart = echarts.init(document.getElementById('main')!);
      // 绘制图表
      myChart.setOption({
        title : {
          text: '最 近 一 周 数 据 走 势',
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['新增用户数', '新增接口数', '新增成交量']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '新增用户数',
            type: 'line',
            stack: 'Total',
            data: seriesDataUser
          },
          {
            name: '新增接口数',
            type: 'line',
            stack: 'Total',
            data: seriesDataInterface,
            color: 'red',
            lineStyle: { color: 'red' }// 修改线条颜色
          },
          {
            name: '新增成交量',
            type: 'line',
            stack: 'Total',
            data: seriesDataOrder
          },
        ]
      });
    }
  })
  return (
    <div id="main" style={{width: '100%',height:400,marginTop:20}}></div>
  );
};
export default Echarts;
