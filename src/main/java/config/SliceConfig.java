package config;

import java.util.HashMap;
import java.util.Map;

/**
 * Slice配置类，支持从JSON文件自动注入属性
 * 通用属性：name, moved, width, height
 * 特殊属性：存储在 extra 字段中，由反射机制动态访问
 */
public class SliceConfig {
    /** Slice名称 */
    public String name;
    /** 是否可移动，决定继承MoveSlice还是StaticSlice */
    public boolean moved;
    /** 宽度 */
    public double width;
    /** 高度 */
    public double height;
    /** 透明度，0=全透明，1=不透明 */
    public double opacity = 1.0;
    /** 边框颜色（CSS颜色值） */
    public String borderColor = "transparent";
    /** 边框宽度（像素） */
    public double borderWidth = 0;
    /** 边框圆角（像素） */
    public double borderRadius = 0;
    /** 背景颜色（CSS颜色值） */
    public String backgroundColor = "transparent";
    /** 文字颜色（CSS颜色值） */
    public String textColor = "black";

    /** 特殊属性容器，存储每种Slice独有的属性 */
    public Map<String, Object> extra = new HashMap<>();

    public SliceConfig() {
    }

    /**
     * 获取特殊属性值
     * @param key 属性名
     * @return 属性值，不存在则返回null
     */
    public Object getExtra(String key) {
        return extra.get(key);
    }

    /**
     * 获取特殊属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public int getIntExtra(String key, int defaultValue) {
        Object val = extra.get(key);
        if (val == null) return defaultValue;
        return ((Number) val).intValue();
    }

    /**
     * 获取特殊属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public double getDoubleExtra(String key, double defaultValue) {
        Object val = extra.get(key);
        if (val == null) return defaultValue;
        return ((Number) val).doubleValue();
    }

    /**
     * 获取特殊属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public boolean getBooleanExtra(String key, boolean defaultValue) {
        Object val = extra.get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取特殊属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public String getStringExtra(String key, String defaultValue) {
        Object val = extra.get(key);
        if (val == null) return defaultValue;
        return val.toString();
    }

    @Override
    public String toString() {
        return "SliceConfig{name='" + name + "', moved=" + moved + ", width=" + width + ", height=" + height + ", extra=" + extra + "}";
    }
}
