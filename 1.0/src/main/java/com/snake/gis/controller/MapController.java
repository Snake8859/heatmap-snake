package com.snake.gis.controller;

import com.alibaba.fastjson.JSONObject;
import com.snake.gis.pojo.Result;
import com.snake.gis.pojo.TbHeatData;
import com.snake.gis.utils.HeatMapGeoJsonUtil;
import com.snake.gis.utils.HeatMapUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地图控制层
 */
@CrossOrigin
@RestController
@RequestMapping("/map")
public class MapController {

    /**
     * 生成热力图数据
     * @param dataTime
     * @param hour
     * @return
     */
    @RequestMapping("generateHeatData")
    public Result generateHeatData(@RequestParam(value = "dateTime",required = true)String dataTime,
                                   @RequestParam(value = "hour",required = true) String hour){
        List<TbHeatData> list = HeatMapUtil.getHeatDataByTime(dataTime,hour);
        if(list!=null&&list.size()>0){
            return Result.build("200","热力图数据生成",list);
        }
        return Result.build("201","热力图数据生成失败");
    }

    /**
     * 生成热力图数据
     * @param dataTime
     * @param hour
     * @return
     */
    @RequestMapping("generateHeatDataGeoJson")
    public Result generateHeatDataGeoJson(@RequestParam(value = "dateTime",required = true)String dataTime,
                                   @RequestParam(value = "hour",required = true) String hour){
        JSONObject geoJson = HeatMapGeoJsonUtil.getHeatDataByTime(dataTime,hour);
        if(geoJson!=null){
            return Result.build("200","热力图数据生成",geoJson);
        }
        return Result.build("201","热力图数据生成失败");
    }



}
