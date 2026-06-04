package config;

/**
 * 小地图配置
 */
public class MiniMapConfig {
    /** 小地图相对视口的比例（0.25 = 1/4） */
    public double sizeRatio;
    /** 小地图距视口边缘的边距（像素） */
    public double margin;
    /** 小地图背景色 */
    public String bgColor;
    /** 小地图边框颜色 */
    public String borderColor;
    /** 小地图边框宽度 */
    public double borderWidth;
    /** 小地图边框圆角 */
    public double borderRadius;
    /** 缩略图透明度 (0~1) */
    public double thumbOpacity;
    /** 缩略图图片路径 */
    public String thumbImagePath;
    /** 视口矩形框描边颜色 */
    public String viewportStrokeColor;
    /** 视口矩形框描边宽度 */
    public double viewportStrokeWidth;
    /** 视口矩形框透明度 (0~1) */
    public double viewportOpacity;
    /** 视口矩形框最小尺寸（像素） */
    public double viewportMinSize;

    public MiniMapConfig() {
    }
}
