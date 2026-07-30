package com.example.afktool.util;

/**
 * Tao chuoi mau gradient hex legacy dang &x&R&R&G&G&B&B cho tung ky tu cua mot chuoi text.
 * Dinh dang nay duoc Spigot/Paper ho tro truc tiep boi ChatColor.translateAlternateColorCodes.
 */
public final class GradientUtil {

    private GradientUtil() {
    }

    public static String apply(String text, String startHex, String endHex) {
        int[] start = parseHex(startHex);
        int[] end = parseHex(endHex);

        int length = text.length();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(' ');
                continue;
            }
            double ratio = length <= 1 ? 0 : (double) i / (length - 1);
            int r = (int) Math.round(start[0] + (end[0] - start[0]) * ratio);
            int g = (int) Math.round(start[1] + (end[1] - start[1]) * ratio);
            int b = (int) Math.round(start[2] + (end[2] - start[2]) * ratio);
            result.append(toHexColorTag(r, g, b)).append(c);
        }
        return result.toString();
    }

    private static int[] parseHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private static String toHexColorTag(int r, int g, int b) {
        String hex = String.format("%02X%02X%02X", r, g, b);
        StringBuilder tag = new StringBuilder("&x");
        for (char c : hex.toCharArray()) {
            tag.append('&').append(c);
        }
        return tag.toString();
    }
}
