package com.snake.gis.utils;

/**
 * 百度坐标（BD09）、国测局坐标（火星坐标，GCJ02）、和WGS84坐标系之间的转换的工具
 *
 * 参考 https://github.com/wandergis/coordtransform 实现的Java版本
 * @author geosmart
 */
public class CoordinateTransformUtil {
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    // π
    static double pi = 3.1415926535897932384626;
    // 长半轴
    static double a = 6378245.0;
    // 扁率
    static double ee = 0.00669342162296594323;

    /**
     * 百度坐标系(BD-09)转WGS坐标
     *
     * @param lng 百度坐标纬度
     * @param lat 百度坐标经度
     * @return WGS84坐标数组
     */
    public static double[] bd09towgs84(double lng, double lat) {
        double[] gcj = bd09togcj02(lng, lat);
        double[] wgs84 = gcj02towgs84(gcj[0], gcj[1]);
        return wgs84;
    }

    /**
     * WGS坐标转百度坐标系(BD-09)
     *
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 百度坐标数组
     */
    public static double[] wgs84tobd09(double lng, double lat) {
        double[] gcj = wgs84togcj02(lng, lat);
        double[] bd09 = gcj02tobd09(gcj[0], gcj[1]);
        return bd09;
    }

    /**
     * 火星坐标系(GCJ-02)转百度坐标系(BD-09)
     *
     * 谷歌、高德——>百度
     * @param lng 火星坐标经度
     * @param lat 火星坐标纬度
     * @return 百度坐标数组
     */
    public static double[] gcj02tobd09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_pi);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[] { bd_lng, bd_lat };
    }

    /**
     * 百度坐标系(BD-09)转火星坐标系(GCJ-02)
     *
     * 百度——>谷歌、高德
     * @param bd_lon 百度坐标纬度
     * @param bd_lat 百度坐标经度
     * @return 火星坐标数组
     */
    public static double[] bd09togcj02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[] { gg_lng, gg_lat };
    }

    /**
     * WGS84转GCJ02(火星坐标系)
     *
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 火星坐标数组
     */
    public static double[] wgs84togcj02(double lng, double lat) {
        if (out_of_china(lng, lat)) {
            return new double[] { lng, lat };
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] { mglng, mglat };
    }

    /**
     * GCJ02(火星坐标系)转GPS84
     *
     * @param lng 火星坐标系的经度
     * @param lat 火星坐标系纬度
     * @return WGS84坐标数组
     */
    public static double[] gcj02towgs84(double lng, double lat) {
        if (out_of_china(lng, lat)) {
            return new double[] { lng, lat };
        }
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = lat / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[] { lng * 2 - mglng, lat * 2 - mglat };
    }

    /**
     * 纬度转换
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * pi) + 320 * Math.sin(lat * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度转换
     *
     * @param lng
     * @param lat
     * @return
     */
    public static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * pi) + 20.0 * Math.sin(2.0 * lng * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * pi) + 40.0 * Math.sin(lng / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * pi) + 300.0 * Math.sin(lng / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 判断是否在国内，不在国内不做偏移
     *
     * @param lng
     * @param lat
     * @return
     */
    public static boolean out_of_china(double lng, double lat) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        } else if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        double[] result = CoordinateTransformUtil.gcj02towgs84(112.93763449390312,28.173944046866307);
        System.out.println("["+result[0]+","+result[1]+"]");
    }
}