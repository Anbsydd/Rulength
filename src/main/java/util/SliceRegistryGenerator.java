package util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Slice注册表自动生成工具
 * 扫描assets/slice/目录下所有.json文件（排除registry.json），
 * 自动写入registry.json的slices数组中
 */
public class SliceRegistryGenerator {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    /** slice配置文件根目录 */
    private static final String SLICE_DIR = "assets/slice/";
    /** 注册表文件路径 */
    private static final String REGISTRY_PATH = SLICE_DIR + "registry.json";
    /** 注册表文件名，扫描时排除 */
    private static final String REGISTRY_FILE = "registry.json";

    /**
     * 扫描slice目录下所有.json文件，自动生成registry.json
     * 排除registry.json自身，结果按文件名排序
     */
    public static void generate() throws Exception {
        // 1. 扫描目录下所有.json文件
        File dir = new File(SLICE_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("Slice目录不存在: " + SLICE_DIR);
        }

        List<String> jsonFiles = Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .filter(f -> f.isFile() && f.getName().endsWith(".json"))
                .filter(f -> !f.getName().equals(REGISTRY_FILE))
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        // 2. 构建registry结构
        Map<String, Object> registry = new LinkedHashMap<>();
        registry.put("slices", jsonFiles);

        // 3. 写入registry.json（美化格式）
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(registry);
        Files.write(Paths.get(REGISTRY_PATH), json.getBytes());

        System.out.println("SliceRegistryGenerator: 已注册 " + jsonFiles.size() + " 个slice → " + jsonFiles);
    }

    /**
     * 主方法，可直接运行生成registry
     */
    public static void main(String[] args) throws Exception {
        generate();
    }
}
