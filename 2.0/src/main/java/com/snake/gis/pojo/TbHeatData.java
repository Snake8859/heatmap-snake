package com.snake.gis.pojo;

/**
 * 热力图数据表
 */
public class TbHeatData {

    //纬度
    private  String lat;

    //经度
    private  String lng;

    //权重
    private  String count;



    public  String  getLat(){
        return  this.lat;
    };
    public  void  setLat(String lat){
        this.lat=lat;
    }

    public  String  getLng(){
        return  this.lng;
    };
    public  void  setLng(String lng){
        this.lng=lng;
    }

    public  String  getCount(){
        return  this.count;
    };
    public  void  setCount(String count){
        this.count=count;
    }

    @Override
    public String toString() {
        return "TbHeatData{" +
                "lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", count='" + count + '\'' +
                '}';
    }
}
