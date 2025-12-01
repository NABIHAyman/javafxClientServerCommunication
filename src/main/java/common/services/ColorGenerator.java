package common.services;

import java.util.Random;

public class ColorGenerator {
    private static final Random random = new Random();
    private static final int MIN_COLOR_VALUE = 100;
    private static final int MAX_COLOR_VALUE = 255;

    private ColorGenerator() {}

    public static String generateRandomColor() {
        int r = random.nextInt(MAX_COLOR_VALUE - MIN_COLOR_VALUE + 1) + MIN_COLOR_VALUE;
        int g = random.nextInt(MAX_COLOR_VALUE - MIN_COLOR_VALUE + 1) + MIN_COLOR_VALUE;
        int b = random.nextInt(MAX_COLOR_VALUE - MIN_COLOR_VALUE + 1) + MIN_COLOR_VALUE;
        return String.format("#%02X%02X%02X", r, g, b);
    }
}


