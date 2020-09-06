package com.snake.gis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.snake.gis.pojo.TbHeatData;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.List;
import java.util.Set;

/**
 * 热力图数据爬虫
 *      腾讯位置大数据平台 区域热力图
 *      岳麓山为例
 */
public class HeatMapGeoJsonUtil {

    private static CloseableHttpClient httpClient =null;
    private static CloseableHttpResponse response = null;
    private static HttpGet httpGet = null;
    //岳麓山中心经纬度
    private final static double center_lat  = 28.183744046866305;
    private final static double center_lng = 112.93703449390313;

    public static JSONObject getHeatDataByTime(String datetime, String hour){
        //请求url
        String url = "https://heat.qq.com/api/getHeatDataByTime.php?region_id=1619&datetime="+datetime+"+"+hour+"%3A00%3A00&sub_domain=";
        //构造httpClient和httpGet对象
        httpClient = HttpClients.createDefault();
        httpGet = new HttpGet(url);

        //构建GeoJson
        //FeatureCollection
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type","FeatureCollection");
        //坐标系属性
        JSONObject crs = new JSONObject();
        crs.put("type", "name");
        JSONObject properties = new JSONObject();
        properties.put("name", "ESPG:4326");
        crs.put("properties", properties);
        featureCollection.put("crs", crs);

        //System.out.println(featureCollection);

        try {
            //发起请求
            response =  httpClient.execute(httpGet);
            //解析响应
            if(response.getStatusLine().getStatusCode()==200) {
                //响应json字符串
                String content = EntityUtils.toString(response.getEntity(), "utf-8");
                //转为json对象
                JSONObject data = (JSONObject) JSON.parseObject(content);
                //取出所有键
                Set<String> keys = data.keySet();
                //要素集合
                JSONArray features = new JSONArray();
                for (String key:keys) {//解密坐标
                        //取出权重
                        Integer weight = (Integer) data.get(key);
                        //取出坐标（gcj-02）
                        String[] split = key.split(",");
                        Double  lat = (10000*center_lat+Integer.parseInt(split[0]))/10000; //解码后纬度
                        Double  lng = (10000*center_lng+Integer.parseInt(split[1]))/10000; //解码后经度
                        //坐标转换（gcj-02 to wgs84）
                        double[] result = CoordinateTransformUtil.gcj02towgs84(lng,lat);

                        //创建单个要素
                        JSONObject feature = new JSONObject();
                        feature.put("type", "Feature");
                        //单个要素的空间信息
                        JSONObject geometry = new JSONObject();
                        geometry.put("type","Point");
                        geometry.put("coordinates",result);

                        //单个要素的属性信息
                        JSONObject desc = new JSONObject();
                        desc.put("count",weight);
                        desc.put("datetime",datetime+"-"+hour);

                        feature.put("geometry",geometry);
                        feature.put("properties",desc);
                        features.add(feature);
                }
                featureCollection.put("features",features);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return featureCollection;
    }

    public static void main(String[] args) {
        System.out.println(getHeatDataByTime("2020-06-01","06"));
    }


}
