package com.komodo.community.utils;

import com.alibaba.fastjson.JSONObject;
import com.komodo.community.pojo.DataBaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @Author ZhangGJ
 * @Date 2021/04/03 12:03
 */
@Slf4j
public class YamlUtil {

    private static final String FILE_URL = "src/main/resources/config-database.yml";

    /**
     * 读取yaml
     *
     * @param keys
     * @param clazz
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T readYaml(String keys, Class<T> clazz) {
        Yaml yaml = new Yaml();
        JSONObject o = null;
        try {
            o = yaml.loadAs((new FileInputStream(FILE_URL)), JSONObject.class);
        } catch (FileNotFoundException e) {
            log.error("Error configuration: Config-database.yml");
            return null;
        }
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
        } else if (clazz.isAssignableFrom(DataBaseInfo.class)) {
            t = (T) result.getJSONObject(keysArray[keysArray.length - 1])
                    .toJavaObject(DataBaseInfo.class);
        } else {
            throw new RuntimeException("Unknown type!");
        }
        return t;
    }

    private static JSONObject recursionYaml(JSONObject o, String key) {
        return o.getJSONObject(key);
    }

}
