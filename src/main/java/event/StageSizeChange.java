package event;

public record StageSizeChange(double width, double height, double multiX, double multiY,
                              double oldWidth, double oldHeight, double oldMultiX, double oldMultiY) {
}