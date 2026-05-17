package config;

public class StageConfig {
    public String title;
    public int width;
    public int height;
    public String fullScreenExitHint;
    
    public StageConfig() {
    }
    
    @Override
    public String toString() {
        return "StageConfig{" +
                "title='" + title + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", fullScreenExitHint='" + fullScreenExitHint + '\'' +
                '}';
    }
}
