package config;

public class CameraConfig {
    public double offsetX;
    public double offsetY;
    public double zoom;
    public double minZoom;
    public double maxZoom;
    public double ZOOM_STEP;
    /** 拖拽时插值速度，1.0 = 完全即时跟随鼠标 */
    public double lerpDrag;
    /** 缩放时插值速度，值越小动画越慢越丝滑 */
    public double lerpZoom;
    
    
    public CameraConfig() {
    }
}
