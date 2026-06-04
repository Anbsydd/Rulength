package game.slice;

/**
 * 坐标转换接口：地图坐标 ↔ 屏幕轨迹坐标
 * 由 Slice 子类实现，根据是否跟随地图缩放提供不同的转换逻辑
 */
public interface Coordinatable {
    /** 屏幕轨迹坐标 → 地图坐标 X */
    double finalTraToMapX(double traX);
    /** 屏幕轨迹坐标 → 地图坐标 Y */
    double finalTraToMapY(double traY);
    /** 地图坐标 → 屏幕轨迹坐标 X */
    double finalMapToTraX(double mapX);
    /** 地图坐标 → 屏幕轨迹坐标 Y */
    double finalMapToTraY(double mapY);
}