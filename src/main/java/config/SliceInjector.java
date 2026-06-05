package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Slice注入工具类
 * 负责读取registry.json中注册的所有slice JSON文件，
 * 自动扫描并加载为SliceConfig对象，支持分层结构注入：
 * - 外层字段：name, moved, mapX, mapY, opacity
 * - text层：嵌套对象，映射到TextConfig
 * - attributes层：嵌套对象，映射到attributes Map
 * - methods层：嵌套对象，映射到methods Map
 * - event层：嵌套对象，映射到event Map
 */
public class SliceInjector {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /** slice配置文件根目录 */
    private static final String SLICE_DIR = "assets/slice/";
    /** 注册表文件路径 */
    private static final String REGISTRY_PATH = SLICE_DIR + "registry.json";

    /** 已加载的SliceConfig缓存，key为JSON文件名（不含扩展名） */
    private static final Map<String, SliceConfig> configCache = new LinkedHashMap<>();

    /** SliceConfig外层字段名集合，用于区分外层字段和嵌套层 */
    private static final Set<String> OUTER_FIELDS = Set.of(
            "name", "moved", "mapX", "mapY", "opacity"
    );

    /**
     * 加载所有注册的slice配置
     * 读取registry.json中列出的所有JSON文件，解析为SliceConfig对象
     * @return 加载的SliceConfig列表
     * @throws Exception 文件读取或解析异常
     */
    public static List<SliceConfig> loadAll() throws Exception {
        configCache.clear();

        // 1. 读取registry.json
        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        List<String> sliceFiles = (List<String>) registryMap.get("slices");

        // 2. 逐个加载slice配置文件
        for (String fileName : sliceFiles) {
            SliceConfig config = loadSliceConfig(SLICE_DIR + fileName);
            // 以文件名（去掉.json后缀）作为key
            String key = fileName.replace(".json", "");
            configCache.put(key, config);
        }

        return new ArrayList<>(configCache.values());
    }

    /**
     * 加载单个slice配置文件
     * 分层结构：
     * - 外层字段（name, moved, mapX, mapY, opacity）通过反射注入SliceConfig字段
     * - text层：嵌套对象，通过反射注入TextConfig字段
     * - attributes层：嵌套对象，直接存入attributes Map
     * - methods层：嵌套对象，直接存入methods Map
     * - event层：嵌套对象，直接存入event Map
     * @param filePath JSON文件路径
     * @return 填充好的SliceConfig对象
     * @throws Exception 文件读取或解析异常
     */
    public static SliceConfig loadSliceConfig(String filePath) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        Map<String, Object> configMap = objectMapper.readValue(json, Map.class);

        SliceConfig config = new SliceConfig();

        // 遍历所有JSON字段，分层处理
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (OUTER_FIELDS.contains(key)) {
                // 外层字段：通过反射注入SliceConfig字段
                injectField(config, key, value);
            } else if ("text".equals(key)) {
                // text层：解析嵌套对象，注入TextConfig
                injectTextConfig(config, value);
            } else if ("attributes".equals(key)) {
                // attributes层：解析嵌套对象，存入attributes Map
                injectMap(config.attributes, value);
            } else if ("methods".equals(key)) {
                // methods层：解析嵌套对象，存入methods Map
                injectMethodMap(config.methods, value);
            } else if ("event".equals(key)) {
                // event层：解析嵌套对象，存入event Map
                injectMap(config.event, value);
            }
            // 未知字段忽略
        }

        return config;
    }

    /**
     * 解析text嵌套对象，通过反射注入TextConfig字段
     * @param config 目标SliceConfig对象
     * @param value text层的JSON值（应为Map）
     */
    @SuppressWarnings("unchecked")
    private static void injectTextConfig(SliceConfig config, Object value) {
        if (!(value instanceof Map)) return;
        Map<String, Object> textMap = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : textMap.entrySet()) {
            injectTextField(config.text, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 通过反射将值注入TextConfig的指定字段
     * @param textConfig 目标TextConfig对象
     * @param fieldName 字段名
     * @param value 字段值
     */
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

    /**
     * 解析嵌套对象，存入Map<String, Object>
     * @param map 目标Map
     * @param value 嵌套层的JSON值（应为Map）
     */
    @SuppressWarnings("unchecked")
    private static void injectMap(Map<String, Object> map, Object value) {
        if (!(value instanceof Map)) return;
        Map<String, Object> sourceMap = (Map<String, Object>) value;
        map.putAll(sourceMap);
    }

    /**
     * 解析methods嵌套对象，存入Map<String, String>
     * @param map 目标Map
     * @param value methods层的JSON值（应为Map）
     */
    @SuppressWarnings("unchecked")
    private static void injectMethodMap(Map<String, String> map, Object value) {
        if (!(value instanceof Map)) return;
        Map<String, Object> sourceMap = (Map<String, Object>) value;
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }
    }

    /**
     * 通过反射将值注入SliceConfig的指定字段
     * @param config 目标SliceConfig对象
     * @param fieldName 字段名
     * @param value 字段值
     */
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

    /**
     * 通用字段赋值方法，根据字段类型自动转换
     * @param field 反射字段
     * @param obj 目标对象
     * @param value 字段值
     */
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

    /**
     * 根据名称获取已加载的SliceConfig
     * @param name slice名称（JSON文件名，不含扩展名）
     * @return 对应的SliceConfig，不存在则返回null
     */
    public static SliceConfig get(String name) {
        return configCache.get(name);
    }

    /**
     * 获取所有已加载的SliceConfig
     * @return SliceConfig列表
     */
    public static List<SliceConfig> getAll() {
        return new ArrayList<>(configCache.values());
    }

    /**
     * 根据SliceConfig创建对应的Slice实例
     * moved=true则创建MoveSlice子类实例，moved=false则创建StaticSlice子类实例
     * @param config slice配置
     * @return 是否为可移动Slice
     */
    public static boolean isMovable(SliceConfig config) {
        return config.moved;
    }

    /**
     * 重新加载所有slice配置（热更新用）
     * @return 重新加载后的SliceConfig列表
     * @throws Exception 文件读取或解析异常
     */
    public static List<SliceConfig> reload() throws Exception {
        return loadAll();
    }

    /**
     * 获取注册表中所有slice文件名列表
     * @return 文件名列表
     * @throws Exception 文件读取或解析异常
     */
    public static List<String> getRegistryList() throws Exception {
        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        return (List<String>) registryMap.get("slices");
    }
}
