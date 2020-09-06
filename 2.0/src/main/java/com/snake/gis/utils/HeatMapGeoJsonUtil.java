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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 热力图数据爬虫
 *      腾讯位置大数据平台 区域热力图
 *      岳麓山为例
 *      GeoJson模式
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
                        desc.put("datetime",datetime);

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

    public static JSONObject getHeatDataByDate(String date) {
        //请求url
        //https://heat.qq.com/api/getHeatDataByDate.php?region_id=1619&date=2020-07-24&sub_domain=
        String url = "https://heat.qq.com/api/getHeatDataByDate.php?region_id=1619&date="+date+"&sub_domain=";
        //构造httpClient和httpGet对象
        httpClient = HttpClients.createDefault();
        httpGet = new HttpGet(url);
        //结果集 JSON格式
        JSONObject parseJson = null;

        try {
            //发起请求
            response = httpClient.execute(httpGet);
            //解析响应
            if (response.getStatusLine().getStatusCode() == 200) {
                //响应json字符串
                String content = EntityUtils.toString(response.getEntity(), "utf-8");
                //转为json对象
                JSONObject data = (JSONObject) JSON.parseObject(content);
                //json对象转为map
                Map<String,Object> dataMap = (Map)data;

                //解析结果集
                Map<String,Object> parseMap = new HashMap<String, Object>();
                //解析24小时的人流数据
                for(int i = 0 ; i<24 ; i++){
                    //该小时内人流数据
                    Map<String,Integer> hourDataResult = new HashMap<String, Integer>();
                    //模糊键查询
                    String dimKey = date +" " +String.format("%02d",i);
                    Map<String,Object> timeData = MapKeyUtil.parseMapForFilter(dataMap,dimKey);
                    for(Map.Entry<String,Object> entry : timeData.entrySet()){
                        String[] c = entry.getValue().toString().split("\\|"); //每个时间点的信息以|分割 注意|的转义
                        for(String e : c){ //遍历结果
                            String[] t = e.split(","); //每个点坐标和权重信息以，分割
                            if(t.length != 0){
                                int n  = 1; //若无第三个值，则默认权重为1
                                if(t.length == 3){ //若有三个值，则第三个值为权重
                                    n = Integer.parseInt(t[2]);
                                }
                                String coordinate = (t[0] + "," + t[1]).trim();
                                if(hourDataResult.containsKey(coordinate)){//位置重复：累计一小时内重复位置的权重
                                    int newN = hourDataResult.get(coordinate) + n;
                                    hourDataResult.put(coordinate,newN);
                                }
                                else { //位置首次出现
                                    hourDataResult.put(coordinate,n);
                                }
                            }
                        }
                    }
                    //写入结果集
                    String reusltKey = dimKey + ":00:00";
                    parseMap.put(reusltKey,hourDataResult);
                }

                //解密结果集
                Map<String,Object> resultsMap = new HashMap<String, Object>();
                //解密24小时的人流量数据
                for(Map.Entry<String,Object> entry:parseMap.entrySet()){
                    Map<String,Integer> hourDataResult = (Map<String, Integer>) entry.getValue();
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
                    //要素集合
                    JSONArray features = new JSONArray();
                    for(Map.Entry<String,Integer> entry1 : hourDataResult.entrySet()){
                        //取出权重
                        int weight = entry1.getValue();
                        //取出坐标
                        String coordinate[] = entry1.getKey().split(",");
                        Double  lat = (10000*center_lat+Integer.parseInt(coordinate[0]))/10000; //解码后纬度
                        Double  lng = (10000*center_lng+Integer.parseInt(coordinate[1]))/10000; //解码后经度
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

                        feature.put("geometry",geometry);
                        feature.put("properties",desc);
                        features.add(feature);
                    }
                    featureCollection.put("features",features);
                    //写入结果集
                    String reusltKey = entry.getKey();
                    resultsMap.put(reusltKey,featureCollection);
                }

                //map站json
                parseJson = (JSONObject) JSON.toJSON(resultsMap);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return parseJson;
    }



    public static void main(String[] args) {
        //System.out.println(getHeatDataByTime("2020-06-30","12"));
        //System.out.println(getHeatDataByDate("2020-07-23"));
    }


}
