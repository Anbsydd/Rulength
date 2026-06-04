package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置写入工具类
 * 支持将Config对象写回JSON文件，以及复制默认配置
 */
public class ConfigWriter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将Config对象写回JSON文件
     * @param filePath 目标JSON文件路径
     * @param config 配置对象
     */
    public static void writeConfig(String filePath, Object config) throws Exception {
        // 通过反射将字段转为有序Map
        Map<String, Object> map = new LinkedHashMap<>();
        for (Field field : config.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(config);
            if (value != null) {
                map.put(field.getName(), value);
            }
        }
        // 写入文件，格式化输出
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        Files.writeString(Path.of(filePath), json);
    }

    /**
     * 将defaultConfig目录中的文件复制到config目录
     * @param fileName 配置文件名（如 "cameraConfig.json"）
     */
    public static void copyDefaultToConfig(String fileName) throws Exception {
        Path source = Path.of("assets/defaultConfig/" + fileName);
        Path target = Path.of("assets/config/" + fileName);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 将所有默认配置复制到config目录
     */
    public static void copyAllDefaultsToConfig() throws Exception {
        File dir = new File("assets/defaultConfig");
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".json") && !file.getName().equals("readme.json")) {
                    copyDefaultToConfig(file.getName());
                }
            }
        }
    }
}
