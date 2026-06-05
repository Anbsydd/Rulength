package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Slice注入工具类
 * 负责读取registry.json中注册的所有slice JSON文件，
 * 自动扫描并加载为SliceConfig对象，支持反射机制动态注入属性
 */
public class SliceInjector {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /** slice配置文件根目录 */
    private static final String SLICE_DIR = "assets/slice/";
    /** 注册表文件路径 */
    private static final String REGISTRY_PATH = SLICE_DIR + "registry.json";

    /** 已加载的SliceConfig缓存，key为JSON文件名（不含扩展名） */
    private static final Map<String, SliceConfig> configCache = new LinkedHashMap<>();

    /** SliceConfig中定义的通用属性字段名集合，用于区分通用属性和特殊属性 */
    private static final Set<String> BASE_FIELDS = Set.of(
            "name", "moved", "width", "height",
            "opacity", "borderColor", "borderWidth", "borderRadius", "backgroundColor", "textColor"
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
     * 通用属性（name, moved, width, height）直接注入SliceConfig字段，
     * 其余属性归入extra容器作为特殊属性
     * @param filePath JSON文件路径
     * @return 填充好的SliceConfig对象
     * @throws Exception 文件读取或解析异常
     */
    public static SliceConfig loadSliceConfig(String filePath) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        Map<String, Object> configMap = objectMapper.readValue(json, Map.class);

        SliceConfig config = new SliceConfig();

        // 遍历所有JSON字段，区分通用属性和特殊属性
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (BASE_FIELDS.contains(key)) {
                // 通用属性：通过反射注入SliceConfig字段
                injectField(config, key, value);
            } else {
                // 特殊属性：存入extra容器
                config.extra.put(key, value);
            }
        }

        return config;
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

            if (field.getType() == int.class) {
                field.setInt(config, ((Number) value).intValue());
            } else if (field.getType() == double.class) {
                field.setDouble(config, ((Number) value).doubleValue());
            } else if (field.getType() == boolean.class) {
                field.setBoolean(config, Boolean.parseBoolean(value.toString()));
            } else if (field.getType() == String.class) {
                field.set(config, value.toString());
            } else {
                field.set(config, value);
            }
        } catch (Exception e) {
            System.err.println("SliceInjector: 无法注入字段 '" + fieldName + "': " + e.getMessage());
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
     * 注意：由于MoveSlice和StaticSlice是抽象类，需要由具体的子类来实例化
     * 此方法提供配置信息，由调用方根据moved字段决定创建哪种Slice
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
