package util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {

    private static final Map<String, Image> cache = new HashMap<>();

    /**
     * 从文件路径加载图片并缩放至指定大小
     * @param path 图片文件路径
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的 Image 对象
     */
    public static Image load(String path, double width, double height) {
        String key = path + "@" + width + "x" + height;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Image image = new Image(path, width, height, true, true);
        cache.put(key, image);
        return image;
    }

    /**
     * 从文件路径加载原始大小图片
     * @param path 图片文件路径
     * @return 原始大小的 Image 对象
     */
    public static Image load(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        Image image = new Image(path);
        cache.put(path, image);
        return image;
    }

    /**
     * 从 InputStream 加载图片并缩放至指定大小
     * @param inputStream 图片输入流
     * @param width 目标宽度
     * @param height 目标高度
     * @param cacheKey 缓存键（可为 null，为 null 则不缓存）
     * @return 缩放后的 Image 对象
     */
    public static Image load(InputStream inputStream, double width, double height, String cacheKey) {
        if (cacheKey != null) {
            String key = cacheKey + "@" + width + "x" + height;
            if (cache.containsKey(key)) {
                return cache.get(key);
            }
            Image image = new Image(inputStream, width, height, true, true);
            cache.put(key, image);
            return image;
        }

        return new Image(inputStream, width, height, true, true);
    }

    /**
     * 创建 ImageView 并缩放至指定大小
     * @param path 图片文件路径
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的 ImageView 对象
     */
    public static ImageView createImageView(String path, double width, double height) {
        return new ImageView(load(path, width, height));
    }

    /**
     * 创建 ImageView，保持原始大小
     * @param path 图片文件路径
     * @return 原始大小的 ImageView 对象
     */
    public static ImageView createImageView(String path) {
        return new ImageView(load(path));
    }

    /**
     * 按比例缩放图片，使图片适应指定的最大宽高（保持宽高比，不超出边界）
     * @param path 图片文件路径
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩放后的 Image 对象
     */
    public static Image loadFit(String path, double maxWidth, double maxHeight) {
        Image original = load(path);
        double scale = Math.min(maxWidth / original.getWidth(), maxHeight / original.getHeight());
        double targetWidth = original.getWidth() * scale;
        double targetHeight = original.getHeight() * scale;

        String key = path + "@fit" + maxWidth + "x" + maxHeight;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Image image = new Image(path, targetWidth, targetHeight, true, true);
        cache.put(key, image);
        return image;
    }

    /**
     * 清除所有缓存
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * 清除指定路径的缓存
     * @param path 图片文件路径
     */
    public static void clearCache(String path) {
        cache.keySet().removeIf(key -> key.startsWith(path));
    }
}
