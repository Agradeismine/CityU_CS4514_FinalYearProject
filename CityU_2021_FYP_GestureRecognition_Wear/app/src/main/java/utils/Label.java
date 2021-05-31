package utils;

public enum Label {
    Red(0xFFFF0000),
    Green(0xFF00FF00),
    Blue(0xFF0000FF),
    Gray(0xFF808080),
    Orange(0xFFFFA500),
    Pink(0xFFFFC0CB),
    Purple(0xFF6A0DAD);

    public final int color;

    Label(int color) {
        this.color = color;
    }
}
