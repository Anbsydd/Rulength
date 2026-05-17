package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigLoader {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 从 JSON 文件读取配置，创建对应类的对象并自动赋值，最后返回该对象
     * @param filePath JSON 文件路径
     * @return 赋值完成的类实例对象
     * @throws Exception 各种异常
     */
    public static <T> T loadConfig(String filePath, Class<T> clazz) throws Exception {
        // 1. 读取文件内容
        String json = new String(Files.readAllBytes(Paths.get(filePath)));
        
        // 2. JSON 转 Map
        Map<String, Object> configMap = objectMapper.readValue(json, Map.class);
        
        // 3. 创建一个新对象
        T instance = clazz.getDeclaredConstructor().newInstance();
        
        // 4. 反射遍历所有字段，自动赋值（支持任意字段名）
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (configMap.containsKey(fieldName)) {
                Object value = configMap.get(fieldName);
                field.setAccessible(true);
                
                // 处理 int 类型
                if (field.getType() == int.class) {
                    field.setInt(instance, ((Number) value).intValue());
                }
                // 处理 double 类型
                else if (field.getType() == double.class) {
                    field.setDouble(instance, ((Number) value).doubleValue());
                }
                // 处理 boolean 类型
                else if (field.getType() == boolean.class) {
                    field.setBoolean(instance, Boolean.parseBoolean(value.toString()));
                }
                // 其他类型直接赋值
                else {
                    field.set(instance, value);
                }
            }
        }
        
        // 5. 返回 填好值的对象
        return instance;
    }
}