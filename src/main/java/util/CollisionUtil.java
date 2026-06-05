package util;

import game.slice.Slice;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.HashMap;
import java.util.Map;

/**
 * 碰撞检测工具类
 * 支持：
 * 1. AABB矩形预判（快速排除不相交物体）
 * 2. 像素级Alpha通道精确检测（PNG不规则物体轮廓碰撞）
 * 3. 兼容MoveSlice和StaticSlice的不同坐标变换体系
 * 4. 像素数据缓存，避免重复读取PNG
 *
 * 使用流程：
 * 1. 对于纯文本/按钮Slice → 自动按矩形碰撞处理
 * 2. 对于图片型Slice → 先调用 registerPixelData() 注册像素数据
 * 3. 调用 checkCollision(a, b) 检测碰撞
 */
public class CollisionUtil {

    private CollisionUtil() {}

    /** 默认Alpha透明度阈值，Alpha > 此值视为不透明（solid） */
    private static final double ALPHA_THRESHOLD = 0.1;

    /** 像素数据缓存，key为图片路径 */
    private static final Map<String, PixelData> pixelCache = new HashMap<>();

    // ==================== 公开API ====================

    /**
     * 检测两个Slice是否发生碰撞
     * <p>
     * 检测策略（两阶段）：
     * 阶段1 — AABB矩形预判：计算两个Slice在场景坐标系中的包围盒，
     *         不相交则直接返回false（快速排除）
     * 阶段2 — 像素级精确检测：如果矩形相交，且两个Slice都注册了
     *         PNG像素数据，则逐像素检查重叠区域内的Alpha通道，
     *         只有两个Slice在同一个像素点都不透明时才判定为碰撞
     * <p>
     * 特例：纯按钮/文本Slice（无像素数据）在矩形相交时直接判定为碰撞
     *
     * @param a 参与碰撞检测的Slice
     * @param b 参与碰撞检测的Slice
     * @return true 表示发生碰撞
     */
    public static boolean checkCollision(Slice a, Slice b) {
        // 1. 获取场景坐标中的边界
        Bounds boundsA = getSceneBounds(a);
        Bounds boundsB = getSceneBounds(b);

        // 2. AABB矩形预判：不相交则直接返回false
        if (!boundsA.intersects(boundsB)) {
            return false;
        }

        // 3. 获取两个slice的像素数据
        PixelData pixelA = getPixelData(a);
        PixelData pixelB = getPixelData(b);

        // 4. 如果两个都没有像素数据（纯按钮/文本），矩形相交即碰撞
        if (pixelA == null && pixelB == null) {
            return true;
        }

        // 5. 像素级精确检测
        return checkPixelCollision(a, b, boundsA, boundsB, pixelA, pixelB);
    }

    /**
     * 获取Slice在场景坐标系中的边界（Bounding Box）
     * <p>
     * 自动处理 MoveSlice 和 StaticSlice 的坐标变换差异：
     * - MoveSlice 的 translateX/Y 包含 zoom 和 offset
     * - StaticSlice 的 translateX/Y 仅包含 multiX/Y
     * localToScene() 会将所有 Node 级联变换转换到场景坐标空间
     *
     * @param slice 目标Slice
     * @return 场景坐标中的边界
     */
    public static Bounds getSceneBounds(Slice slice) {
        return slice.localToScene(slice.getBoundsInLocal());
    }

    /**
     * 注册图片的像素数据到缓存
     * <p>
     * 加载指定路径的PNG图片，读取每个像素的Alpha通道，
     * 以boolean[]形式缓存，供像素级碰撞检测使用
     *
     * @param imagePath 图片路径（同 ImageManager 使用的路径格式）
     * @return true 表示加载成功
     */
    public static boolean registerPixelData(String imagePath) {
        if (pixelCache.containsKey(imagePath)) return true;
        try {
            Image image = ImageManager.load(imagePath);
            if (image == null || image.isError()) {
                // 尝试直接文件路径加载
                image = new Image("file:" + imagePath);
                if (image.isError()) {
                    System.err.println("CollisionUtil: 无法加载图片 '" + imagePath + "'");
                    return false;
                }
            }
            PixelData data = readPixelData(image);
            if (data == null) return false;
            pixelCache.put(imagePath, data);
            return true;
        } catch (Exception e) {
            System.err.println("CollisionUtil: 加载像素数据异常 '" + imagePath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * 手动注册外部构建的像素数据
     * <p>
     * 用于非文件图片来源（如程序生成的纹理、Canvas绘制的内容等）
     *
     * @param key 缓存键名（需与后续getPixelData中使用的key一致）
     * @param width 图片宽度（像素）
     * @param height 图片高度（像素）
     * @param alphaMask boolean[]，长度为 width*height，true=该像素不透明
     */
    public static void registerPixelData(String key, int width, int height, boolean[] alphaMask) {
        pixelCache.put(key, new PixelData(width, height, alphaMask));
    }

    /**
     * 清除指定路径的像素缓存
     * @param imagePath 图片路径
     */
    public static void clearPixelCache(String imagePath) {
        pixelCache.keySet().removeIf(key -> key.startsWith(imagePath));
    }

    /**
     * 清除所有像素缓存（全局重置）
     */
    public static void clearAllCache() {
        pixelCache.clear();
    }

    // ==================== 内部实现 ====================

    /**
     * 获取Slice关联的像素数据
     * <p>
     * 当前策略（可根据需要扩展）：
     * - 如果Slice有注册的像素数据（通过 registerPixelData 注册），则返回
     * - 否则返回 null，视作纯按钮/文本Slice，按矩形碰撞处理
     * <p>
     * TODO: 待图片层实现后，此方法应从 SliceConfig 中读取图片路径
     *       自动查找缓存，实现全自动匹配
     */
    private static PixelData getPixelData(Slice slice) {
        // 目前图片层尚未实现，这里作为预留扩展点
        // 未来实现步骤：
        // 1. 从 SliceConfig 中获取该 slice 的图片路径
        // 2. 如果路径存在，从 pixelCache 中根据路径查找 PixelData
        // 3. 如果 cache 未命中，自动调用 registerPixelData 加载
        return null;
    }

    /**
     * 像素级碰撞检测
     * <p>
     * 在两个Slice的AABB重叠区域内，逐像素检测Alpha通道。
     * 只有当两个Slice在同一个像素位置都不透明时，才判定为碰撞。
     * <p>
     * 性能优化：
     * - 仅在AABB预判通过后执行
     * - 仅扫描重叠区域，而不是遍历整个图片
     * - 像素数据只加载一次，后续检测直接从缓存读取
     *
     * @param a Slice A
     * @param b Slice B
     * @param boundsA Slice A的场景坐标边界
     * @param boundsB Slice B的场景坐标边界
     * @param pixelA Slice A的像素数据（可为null）
     * @param pixelB Slice B的像素数据（可为null）
     * @return true 表示发生像素级碰撞
     */
    private static boolean checkPixelCollision(Slice a, Slice b,
                                                Bounds boundsA, Bounds boundsB,
                                                PixelData pixelA, PixelData pixelB) {
        // 计算重叠区域（场景坐标）
        double overlapMinX = Math.max(boundsA.getMinX(), boundsB.getMinX());
        double overlapMinY = Math.max(boundsA.getMinY(), boundsB.getMinY());
        double overlapMaxX = Math.min(boundsA.getMaxX(), boundsB.getMaxX());
        double overlapMaxY = Math.min(boundsA.getMaxY(), boundsB.getMaxY());

        int startX = (int) Math.floor(overlapMinX);
        int startY = (int) Math.floor(overlapMinY);
        int endX = (int) Math.ceil(overlapMaxX);
        int endY = (int) Math.ceil(overlapMaxY);

        // 遍历重叠区域每个像素
        for (int sceneX = startX; sceneX < endX; sceneX++) {
            for (int sceneY = startY; sceneY < endY; sceneY++) {
                boolean alphaA = isSolidAtScenePoint(a, pixelA, sceneX, sceneY);
                boolean alphaB = isSolidAtScenePoint(b, pixelB, sceneX, sceneY);
                if (alphaA && alphaB) {
                    // 两个slice在该像素点都不透明 → 发生碰撞
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断在场景坐标 (sceneX, sceneY) 处，指定slice是否为不透明
     * <p>
     * 处理流程：
     * 1. 将场景坐标转换为slice的本地坐标（sceneToLocal）
     * 2. 检查坐标是否在slice的BoundsInLocal范围内
     * 3. 如果slice没有像素数据（纯按钮），直接视为不透明
     * 4. 如果有像素数据，将本地坐标映射到图片纹理坐标，查询Alpha
     *
     * @param slice 目标slice
     * @param pixelData slice的像素数据，null表示纯按钮全solid
     * @param sceneX 场景X坐标
     * @param sceneY 场景Y坐标
     * @return true 表示该像素位置slice不透明
     */
    private static boolean isSolidAtScenePoint(Slice slice, PixelData pixelData, int sceneX, int sceneY) {
        // 将场景坐标转换为slice的本地坐标
        // sceneToLocal 自动处理 Node 的所有变换（translate/scale/rotate）
        // 因此 MoveSlice 和 StaticSlice 在此处无需特殊处理
        Point2D localPoint = slice.sceneToLocal(sceneX + 0.5, sceneY + 0.5);
        double localX = localPoint.getX();
        double localY = localPoint.getY();

        // 检查坐标是否在slice本地边界内
        Bounds localBounds = slice.getBoundsInLocal();
        if (!localBounds.contains(localX, localY)) {
            return false;
        }

        // 如果没有像素数据（纯按钮/文本slice），视为全solid
        if (pixelData == null) {
            return true;
        }

        // 将本地坐标映射到图片纹理坐标
        // 假设图片填满整个Slice的边界范围
        double localWidth = localBounds.getWidth();
        double localHeight = localBounds.getHeight();
        if (localWidth <= 0 || localHeight <= 0) return false;

        int imgX = (int) ((localX / localWidth) * pixelData.width);
        int imgY = (int) ((localY / localHeight) * pixelData.height);

        // 边界保护
        imgX = Math.max(0, Math.min(imgX, pixelData.width - 1));
        imgY = Math.max(0, Math.min(imgY, pixelData.height - 1));

        return pixelData.getAlpha(imgX, imgY);
    }

    /**
     * 从 JavaFX Image 对象读取所有像素的Alpha通道
     * <p>
     * 逐像素调用 getArgb(x, y) 读取ARGB值，
     * 提取Alpha通道并与阈值比较，生成 boolean[] 掩码。
     * 仅加载一次，随后缓存在 pixelCache 中。
     *
     * @param image 已加载的JavaFX Image对象
     * @return PixelData 对象，包含 width/height/alphaMask
     */
    private static PixelData readPixelData(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        if (w <= 0 || h <= 0) return null;

        PixelReader reader = image.getPixelReader();
        if (reader == null) return null;

        boolean[] alpha = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xFF;
                alpha[y * w + x] = (a / 255.0) > ALPHA_THRESHOLD;
            }
        }

        return new PixelData(w, h, alpha);
    }

    // ==================== 内部数据结构 ====================

    /**
     * 像素数据缓存单元
     * <p>
     * 存储图片的宽度、高度和Alpha通道 boolean[] 掩码。
     * true = 该像素不透明（solid），false = 透明。
     * 每次像素级碰撞检测时，只需根据 UV 坐标索引此数组即可，
     * 无需重复读取PNG文件。
     */
    public static class PixelData {
        public final int width;
        public final int height;
        /** 每个像素是否不透明，true=不透明(solid)，false=透明 */
        private final boolean[] alphaMask;

        public PixelData(int width, int height, boolean[] alphaMask) {
            this.width = width;
            this.height = height;
            this.alphaMask = alphaMask;
        }

        /**
         * 获取指定像素位置是否不透明
         * @param x 图片X坐标（0 ≤ x < width）
         * @param y 图片Y坐标（0 ≤ y < height）
         * @return true 表示该像素不透明
         */
        public boolean getAlpha(int x, int y) {
            return alphaMask[y * width + x];
        }
    }
}
