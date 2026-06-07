package config;

import java.util.HashMap;
import java.util.Map;

/**
 * Slice配置类，支持从JSON文件自动注入属性
 * 分层结构：
 * - 外层：ID（大类ID）, name, text, methods, event
 * - example层：每个实例的ID、moved、mapX、mapY、opacity、attributes
 */
public class SliceConfig {
    /** 大类ID（对应JSON外层ID字段） */
    public String ID;
    /** Slice名称 */
    public String name;
    /** 是否可移动，决定继承MoveSlice还是StaticSlice */
    public boolean moved;
    /** 地图X坐标 */
    public double mapX = 0;
    /** 地图Y坐标 */
    public double mapY = 0;
    /** 整体透明度，0=全透明，1=不透明 */
    public double opacity = 1.0;
    /** 实例小ID（对应example中的数字key，如"1", "2"） */
    public String exampleID;

    /** 文本层配置，控制按钮与文本的各项属性 */
    public TextConfig text = new TextConfig();

    /** 额外属性容器，记录每个slice独有的属性（如health, attack） */
    public Map<String, Object> attributes = new HashMap<>();

    /** 方法映射容器，记录slice可调用的方法（如 hit → attack） */
    public Map<String, String> methods = new HashMap<>();

    /** 事件容器，记录每个游戏刻需要对这个slice进行更新的事件，为空或没有则不需要更新 */
    public Map<String, Object> event = new HashMap<>();

    public SliceConfig() {
    }

    /**
     * 获取attributes中的属性值
     * @param key 属性名
     * @return 属性值，不存在则返回null
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 获取attributes中的属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public int getIntAttribute(String key, int defaultValue) {
        Object val = attributes.get(key);
        if (val == null) return defaultValue;
        return ((Number) val).intValue();
    }

    /**
     * 获取attributes中的属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public double getDoubleAttribute(String key, double defaultValue) {
        Object val = attributes.get(key);
        if (val == null) return defaultValue;
        return ((Number) val).doubleValue();
    }

    /**
     * 获取attributes中的属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public boolean getBooleanAttribute(String key, boolean defaultValue) {
        Object val = attributes.get(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取attributes中的属性值，带类型转换
     * @param key 属性名
     * @param defaultValue 默认值
     * @return 属性值，不存在则返回默认值
     */
    public String getStringAttribute(String key, String defaultValue) {
        Object val = attributes.get(key);
        if (val == null) return defaultValue;
        return val.toString();
    }

    /**
     * 获取methods中的方法映射
     * @param trigger 触发器名称（如 hit）
     * @return 对应的方法名，不存在则返回null
     */
    public String getMethod(String trigger) {
        return methods.get(trigger);
    }

    /**
     * 判断是否有游戏刻更新事件
     * @return true表示有事件需要每刻更新
     */
    public boolean hasEvent() {
        return event != null && !event.isEmpty();
    }

    @Override
    public String toString() {
        return "SliceConfig{ID='" + ID + "', exampleID='" + exampleID + "', name='" + name + "', moved=" + moved +
                ", text=" + text + ", attributes=" + attributes + ", methods=" + methods + ", event=" + event + "}";
    }

    /**
     * 文本层配置类，控制按钮与文本的各项渲染属性
     * 每个渲染层的opacity独立控制
     */
    public static class TextConfig {
        /** 字体大小（像素） */
        public double fontSize = 12;
        /** 宽度 */
        public double width = 100;
        /** 高度 */
        public double height = 50;
        /** 是否自动换行，true=文本超出宽度时自动换行 */
        public boolean wrapText = false;
        /** 文字距边框上边距（像素） */
        public double insertTop = 0;
        /** 文字距边框右边距（像素） */
        public double insertRight = 0;
        /** 文字距边框下边距（像素） */
        public double insertBottom = 0;
        /** 文字距边框左边距（像素） */
        public double insertLeft = 0;
        /** 文本层透明度，独立于外层opacity控制 */
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

        public TextConfig() {
        }

        @Override
        public String toString() {
            return "TextConfig{fontSize=" + fontSize + ", width=" + width + ", height=" + height +
                    ", wrapText=" + wrapText + ", opacity=" + opacity +
                    ", backgroundColor='" + backgroundColor + "', textColor='" + textColor + "'}";
        }
    }
}
