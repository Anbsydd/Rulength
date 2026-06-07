package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Slice注入工具类
 * <p>
 * 1. assets/slice/ 中存储默认 slice 定义（平面结构，不含 mapX/Y）
 * 2. saves/default/map/ 中存储地图实例定义，每个实例引用默认 slice 的 ID
 * 3. 加载地图时：找到定义 -> 克隆 -> 叠加实例专属属性 -> 注入额外 attributes
 *
 * Example map JSON:
 * <pre>
 * { "slice": [
 *   { "id": 1, "definition": "Player", "mapX": 200, "mapY": 100 },
 *   { "id": 2, "definition": "Test",  "mapX": -200, "mapY": 100,
 *     "attributes": { "health": 100 } }
 * ]}
 * </pre>
 */
public class SliceInjector {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SLICE_DIR = "assets/slice/";
    private static final String REGISTRY_PATH = SLICE_DIR + "registry.json";

    /** 默认 slice 缓存，key=ID（与文件名一致） */
    private static final Map<String, SliceConfig> defaultSlices = new LinkedHashMap<>();
    /** 地图实例 map ID -> config，由 loadMap 填充 */
    private static final Map<Integer, SliceConfig> mapInstances = new LinkedHashMap<>();

    /**
     * 加载所有注册的默认 slice 配置（仅缓存，不展开实例）
     */
    public static List<SliceConfig> loadAll() throws Exception {
        defaultSlices.clear();

        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        List<String> sliceFiles = (List<String>) registryMap.get("slices");

        for (String fileName : sliceFiles) {
            SliceConfig cfg = loadSliceDef(SLICE_DIR + fileName);
            if (cfg != null) {
                defaultSlices.put(cfg.ID, cfg);
            }
        }
        return new ArrayList<>(defaultSlices.values());
    }

    /**
     * 加载单个 slice 默认配置文件（平面结构）
     */
    public static SliceConfig loadSliceDef(String filePath) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        Map<String, Object> configMap = objectMapper.readValue(json, Map.class);

        SliceConfig cfg = new SliceConfig();
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "ID" -> cfg.ID = value.toString();
                case "name" -> cfg.name = value.toString();
                case "moved" -> cfg.moved = Boolean.parseBoolean(value.toString());
                case "mapX" -> cfg.mapX = ((Number) value).doubleValue();
                case "mapY" -> cfg.mapY = ((Number) value).doubleValue();
                case "opacity" -> cfg.opacity = ((Number) value).doubleValue();
                case "attributes" -> injectMap(cfg.attributes, value);
                case "text" -> injectTextConfig(cfg, value);
                case "methods" -> injectMethodMap(cfg.methods, value);
                case "event" -> injectMap(cfg.event, value);
            }
        }
        return cfg;
    }

    /**
     * 加载地图文件，将定义展开为实例列表
     *
     * @param mapPath 地图 JSON 文件路径，如 "saves/default/map/test.json"
     * @return 实例列表（已在 mapInstances 中缓存）
     * @throws Exception 文件读取或解析异常
     */
    public static List<SliceConfig> loadMap(String mapPath) throws Exception {
        mapInstances.clear();

        String json = new String(Files.readAllBytes(Paths.get(mapPath)));
        Map<String, Object> root = objectMapper.readValue(json, Map.class);
        List<Map<String, Object>> entries = (List<Map<String, Object>>) root.get("slice");
        if (entries == null) return Collections.emptyList();

        List<SliceConfig> results = new ArrayList<>();
        for (Map<String, Object> entry : entries) {
            Object defIdObj = entry.get("definition");
            if (defIdObj == null) continue;
            String defId = defIdObj.toString();

            SliceConfig def = defaultSlices.get(defId);
            if (def == null) {
                System.err.println("SliceInjector: 地图引用了未定义的slice ID=" + defId + "，跳过");
                continue;
            }

            // 克隆默认配置
            SliceConfig cfg = cloneDef(def);

            // 叠加实例专属字段
            Object idObj = entry.get("id");
            if (idObj != null) cfg.mapId = ((Number) idObj).intValue();

            if (entry.containsKey("mapX")) cfg.mapX = ((Number) entry.get("mapX")).doubleValue();
            if (entry.containsKey("mapY")) cfg.mapY = ((Number) entry.get("mapY")).doubleValue();
            if (entry.containsKey("opacity")) cfg.opacity = ((Number) entry.get("opacity")).doubleValue();

            // 叠加 attributes（合并到已有默认值上）
            Object attrObj = entry.get("attributes");
            if (attrObj instanceof Map) {
                injectMap(cfg.attributes, attrObj);
            }

            mapInstances.put(cfg.mapId, cfg);
            results.add(cfg);
        }
        return results;
    }

    /** 从默认定义克隆出一个新 SliceConfig */
    private static SliceConfig cloneDef(SliceConfig def) {
        SliceConfig cfg = new SliceConfig();
        cfg.ID = def.ID;
        cfg.name = def.name;
        cfg.moved = def.moved;
        cfg.mapX = def.mapX;
        cfg.mapY = def.mapY;
        cfg.opacity = def.opacity;
        cfg.text = def.text;                         // 共享
        cfg.attributes.putAll(def.attributes);
        cfg.methods.putAll(def.methods);
        cfg.event.putAll(def.event);
        return cfg;
    }

    // ==================== 注入方法 ====================

    @SuppressWarnings("unchecked")
    private static void injectTextConfig(SliceConfig config, Object value) {
        if (!(value instanceof Map)) return;
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
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

    public static SliceConfig getDefault(String id) {
        return defaultSlices.get(id);
    }

    public static Map<Integer, SliceConfig> getMapInstances() {
        return new LinkedHashMap<>(mapInstances);
    }

    public static List<SliceConfig> getAll() {
        return new ArrayList<>(defaultSlices.values());
    }

    public static List<SliceConfig> reload() throws Exception {
        return loadAll();
    }

    public static List<String> getRegistryList() throws Exception {
        String registryJson = new String(Files.readAllBytes(Paths.get(REGISTRY_PATH)));
        Map<String, Object> registryMap = objectMapper.readValue(registryJson, Map.class);
        return (List<String>) registryMap.get("slices");
    }
}
