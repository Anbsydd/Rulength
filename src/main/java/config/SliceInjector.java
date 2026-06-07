package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Slice注入工具类
 * 负责读取registry.json中注册的所有slice JSON文件，
 * 自动扫描并加载为SliceConfig对象列表。
 *
 * 每份JSON文件定义一个大类（共享text/methods/event），
 * 内部example层以数字为key定义多个实例，每个实例展开为一个SliceConfig：
 * <pre>
 * { "ID": "t2", "name": "测试测试2",
 *   "text": {...},
 *   "methods": {},
 *   "event": {},
 *   "example": {
 *     "1": { "mapX": 500, "mapY": 100, "moved": false, "opacity": 1,
 *            "attributes": { "health": 100, "attack": 10 } },
 *     "2": { ... }
 *   }
 * }
 * </pre>
 */
public class SliceInjector {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SLICE_DIR = "assets/slice/";
    private static final String REGISTRY_PATH = SLICE_DIR + "registry.json";

    /** 已加载的SliceConfig缓存，key为JSON文件名（不含扩展名），value为该文件的全部实例 */
    private static final Map<String, List<SliceConfig>> configCache = new LinkedHashMap<>();

    /**
     * 加载所有注册的slice配置
     * 每个JSON文件中的example层会被展开为多个SliceConfig
     */
    public static List<SliceConfig> loadAll() throws Exception {
        configCache.clear();

        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        List<String> sliceFiles = (List<String>) registryMap.get("slices");

        List<SliceConfig> allConfigs = new ArrayList<>();
        for (String fileName : sliceFiles) {
            List<SliceConfig> configs = loadSliceConfig(SLICE_DIR + fileName);
            String key = fileName.replace(".json", "");
            configCache.put(key, configs);
            allConfigs.addAll(configs);
        }
        return allConfigs;
    }

    /**
     * 加载单个slice配置文件，返回该文件所有example实例
     * 外层字段（ID, name, text, methods, event）作为共享模板，
     * example中每个数字key展开为一个SliceConfig
     */
    public static List<SliceConfig> loadSliceConfig(String filePath) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        Map<String, Object> configMap = objectMapper.readValue(json, Map.class);

        // 提取共享层
        SliceConfig template = new SliceConfig();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "ID" -> template.ID = value.toString();
                case "name" -> template.name = value.toString();
                case "text" -> injectTextConfig(template, value);
                case "methods" -> injectMethodMap(template.methods, value);
                case "event" -> injectMap(template.event, value);
                // "example" 和未知字段忽略
            }
        }

        // 展开 example 层
        Object exampleObj = configMap.get("example");
        if (!(exampleObj instanceof Map)) return Collections.emptyList();

        Map<String, Object> exampleMap = (Map<String, Object>) exampleObj;
        List<SliceConfig> results = new ArrayList<>();

        for (Map.Entry<String, Object> entry : exampleMap.entrySet()) {
            String exampleKey = entry.getKey();  // "1", "2" ...
            Object exampleValue = entry.getValue();
            if (!(exampleValue instanceof Map)) continue;

            // 从模板克隆
            SliceConfig cfg = cloneTemplate(template);
            cfg.exampleID = exampleKey;

            Map<String, Object> instMap = (Map<String, Object>) exampleValue;
            for (Map.Entry<String, Object> instEntry : instMap.entrySet()) {
                String instKey = instEntry.getKey();
                Object instVal = instEntry.getValue();
                if ("attributes".equals(instKey)) {
                    injectMap(cfg.attributes, instVal);
                } else {
                    injectField(cfg, instKey, instVal);
                }
            }
            results.add(cfg);
        }
        return results;
    }

    /** 从模板克隆出一个新的SliceConfig（浅拷贝字段） */
    private static SliceConfig cloneTemplate(SliceConfig template) {
        SliceConfig cfg = new SliceConfig();
        cfg.ID = template.ID;
        cfg.name = template.name;
        cfg.moved = template.moved;
        cfg.mapX = template.mapX;
        cfg.mapY = template.mapY;
        cfg.opacity = template.opacity;
        // text 共享引用（只读）
        cfg.text = template.text;
        // methods/event 浅拷贝 Map
        cfg.methods.putAll(template.methods);
        cfg.event.putAll(template.event);
        return cfg;
    }

    // ==================== 注入方法 ====================

    @SuppressWarnings("unchecked")
    private static void injectTextConfig(SliceConfig config, Object value) {
        if (!(value instanceof Map)) return;
        Map<String, Object> textMap = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : textMap.entrySet()) {
            injectTextField(config.text, entry.getKey(), entry.getValue());
        }
    }

    private static void injectTextField(SliceConfig.TextConfig textConfig, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = SliceConfig.TextConfig.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            setFieldValue(field, textConfig, value);
        } catch (NoSuchFieldException e) {
            System.err.println("SliceInjector: TextConfig中不存在字段 '" + fieldName + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("SliceInjector: 无法注入TextConfig字段 '" + fieldName + "': " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void injectMap(Map<String, Object> map, Object value) {
        if (!(value instanceof Map)) return;
        map.putAll((Map<String, Object>) value);
    }

    @SuppressWarnings("unchecked")
    private static void injectMethodMap(Map<String, String> map, Object value) {
        if (!(value instanceof Map)) return;
        Map<String, Object> sourceMap = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }
    }

    private static void injectField(SliceConfig config, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = SliceConfig.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            setFieldValue(field, config, value);
        } catch (NoSuchFieldException e) {
            System.err.println("SliceInjector: SliceConfig中不存在字段 '" + fieldName + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("SliceInjector: 无法注入字段 '" + fieldName + "': " + e.getMessage());
        }
    }

    private static void setFieldValue(java.lang.reflect.Field field, Object obj, Object value) throws IllegalAccessException {
        if (field.getType() == int.class) {
            field.setInt(obj, ((Number) value).intValue());
        } else if (field.getType() == double.class) {
            field.setDouble(obj, ((Number) value).doubleValue());
        } else if (field.getType() == boolean.class) {
            field.setBoolean(obj, Boolean.parseBoolean(value.toString()));
        } else if (field.getType() == String.class) {
            field.set(obj, value.toString());
        } else {
            field.set(obj, value);
        }
    }

    // ==================== 查询 ====================

    /**
     * 根据文件名（不含扩展名）获取该大类下的第一个实例
     */
    public static SliceConfig get(String name) {
        List<SliceConfig> list = configCache.get(name);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * 获取所有已加载的SliceConfig
     */
    public static List<SliceConfig> getAll() {
        List<SliceConfig> all = new ArrayList<>();
        for (List<SliceConfig> list : configCache.values()) {
            all.addAll(list);
        }
        return all;
    }

    /**
     * 重新加载所有slice配置
     */
    public static List<SliceConfig> reload() throws Exception {
        return loadAll();
    }

    public static List<String> getRegistryList() throws Exception {
        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        return (List<String>) registryMap.get("slices");
    }
}
