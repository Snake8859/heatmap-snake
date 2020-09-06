# heatmap-snake
基于腾讯位置大数据平台-区域热力图可视化系统。

以岳麓山为例

> 若改为其他景区，先确定该景区的人流量数据在腾讯位置大数据平台内存在，接着需要修改HeatMapUtil.java内的center_lat和center_lng变量为相应景区的中心经纬度，该经纬度可以通过相应地图API（高德或者腾讯）查询获取。

## 版本说明

### 1.0

按小时获取，返回格式为普通经纬度或者geojson。

【以2020-07-26 12点为例】

- http://localhost:8865/map/generateHeatData?dateTime=2020-07-26&hour=12  （12点数据，普通经纬度格式）
- http://localhost:8865/map/generateHeatDataGeoJson?dateTime=2020-07-26&hour=12 （12点数据，普通经纬度格式）

核心类：HeatMapUtil.java和HeatMapGeoJsonUtil.java

### 2.0

按小时或者天获取，返回格式为普通经纬度或者geojson。

- 按小时获取：【以2020-07-26 12点为例】

  - http://localhost:8865/map/generateHeatDataByTime?dateTime=2020-07-26&hour=12 （12点数据，普通经纬度格式）

  - http://localhost:8865/map/generateHeatDataGeoJsonByTime?dateTime=2020-07-26&hour=12 （12点数据，geojson格式）

- 按天获取：【以2020-07-26为例】
  - http://localhost:8865/map/generateHeatDataByDate?date=2020-07-26 （普通经纬度格式）
  - http://localhost:8865/map/generateHeatDataGeoJsonByDate?date=2020-07-26 （geojson格式）

核心类：HeatMapUtil.java和HeatMapGeoJsonUtil.java

## 效果

![demo](https://raw.githubusercontent.com/Snake8859/heatmap-snake/master/images/demo.jpg)

