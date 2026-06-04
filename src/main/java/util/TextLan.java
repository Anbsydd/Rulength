package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语言配置工具类
 * 
 * 从 assets/textLan/ 目录加载语言文件，
 * 提供键值对查找，缺失时直接返回键本身。
 */
public class TextLan {

    private static final String LANG_DIR = "assets/textLan/";
    private static final Map<String, String> textMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 加载指定语言文件
     * @param langFileName 语言文件名（如 "Simplified Chinese.json"）
     */
    public static void load(String langFileName) {
        try {
            String json = new String(Files.readAllBytes(Path.of(LANG_DIR + langFileName)));
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<>() {});
            textMap.clear();
            textMap.putAll(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据键获取文本，缺失时直接返回键本身
     * @param key 文本键
     * @return 对应的文本值，缺失则返回键
     */
    public static String get(String key) {
        return textMap.getOrDefault(key, key);
    }
}
