package com.tools.monitor.util;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * JsonUtilData
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class JsonUtilData {
    /**
     * objectToJsonFile
     *
     * @param finalPath finalPath
     * @param object object
     */
    public static void objectToJsonFile(String finalPath, Object object) {
        String json = JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
        try {
            FileOutputStream fos = new FileOutputStream(new File(finalPath));
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(json);
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException exception) {
            log.error("object--->jsonFile--->{}", "Entity class conversion json failed");
        }
    }

    /**
     * jsonFileToObject
     *
     * @param finalPath finalPath
     * @param targetClass targetClass
     * @return t
     * @param <T>
     */
    public static <T> T jsonFileToObject(String finalPath, Class<T> targetClass) {
        String jsonString;
        File file = new File(".", finalPath);
        if (!FileUtil.isNotEmpty(file)) {
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
            T object = JSON.parseObject(jsonString, targetClass);
            return object;
        } catch (IOException exception) {
            log.error("json-->object-->fail{}", "json conversion Entity class failed");
        }
        return null;
    }
}
