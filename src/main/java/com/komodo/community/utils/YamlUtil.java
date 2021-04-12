package com.komodo.community.utils;

import com.alibaba.fastjson.JSONObject;
import com.komodo.community.pojo.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * @Author ZhangGJ
 * @Date 2021/04/03 12:03
 */
@Slf4j
public class YamlUtil {

    /**
     * Read yaml
     *
     * @param name
     * @param keys
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T readYaml(String name, String keys, Class<T> clazz) {
        Yaml yaml = new Yaml();
        JSONObject o = yaml.loadAs(YamlUtil.class.getClassLoader().getResourceAsStream(name),
                JSONObject.class);
        keys = keys.toLowerCase();
        String[] keysArray = keys.split("\\.");
        JSONObject result = null;
        T t;
        if (keysArray.length == 1) {
            result = o;
        } else {
            for (int i = 0; i < keysArray.length; i++) {
                if (i == keysArray.length - 1) {
                    break;
                }
                assert o != null;
                result = recursionYaml(o, keysArray[i]);
                if (result == null) {
                    log.warn("Cannot get key: " + keysArray[i]);
                    return null;
                }
            }
        }
        assert result != null;
        if (clazz.isAssignableFrom(Integer.class)) {
            t = (T) result.getInteger(keysArray[keysArray.length - 1]);
        } else if (clazz.isAssignableFrom(Long.class)) {
            t = (T) result.getLong(keysArray[keysArray.length - 1]);
        } else if (clazz.isAssignableFrom(String.class)) {
            t = (T) result.getString(keysArray[keysArray.length - 1]);
        } else if (clazz.isAssignableFrom(Boolean.class)) {
            t = (T) result.getBoolean(keysArray[keysArray.length - 1]);
        } else if (clazz.isAssignableFrom(Map.class)) {
            t = (T) result.getJSONObject(keysArray[keysArray.length - 1]);
        } else if (clazz.isAssignableFrom(ConnectionInfo.class)) {
            t = (T) result.getJSONObject(keysArray[keysArray.length - 1])
                    .toJavaObject(ConnectionInfo.class);
        } else {
            throw new RuntimeException("Unknown type!");
        }
        return t;
    }

    private static JSONObject recursionYaml(JSONObject o, String key) {
        return o.getJSONObject(key);
    }

}
