package com.snake.gis.utils;

import java.util.Map;
import java.util.stream.Collectors;

public class MapKeyUtil {
    // =========== 根据模糊的key匹配Map的值  =================
    /**
     * 从map中查询想要的map项，根据key
     */
    public static Map<String, Object> parseMapForFilter(Map<String, Object> map, String filters) {
        if (map == null) {
            return null;
        } else {
            map = map.entrySet().stream()
                    .filter((e) -> checkKey(e.getKey(),filters))
                    .collect(Collectors.toMap(
                            (e) -> (String) e.getKey(),
                            (e) -> e.getValue()
                    ));
        }
        return map;
    }

    /**
     * 通过indexof匹配想要查询的字符
     */
    private static boolean checkKey(String key,String filters) {
        if (key.indexOf(filters)>-1){
            return true;
        }else {
            return false;
        }
    }
}
