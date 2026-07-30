package com.example.afktool.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Chuyen doi chu thuong a-z sang dang "small caps" unicode (vd a -> ᴀ, b -> ʙ...).
 * Cac ky tu co dau tieng Viet (ú, ì, ẻ, ố...) khong co dang small-caps tuong ung
 * trong unicode nen se duoc GIU NGUYEN khong doi.
 */
public final class SmallCaps {

    private static final Map<Character, Character> MAP = new HashMap<>();

    static {
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String small = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
        for (int i = 0; i < lower.length(); i++) {
            MAP.put(lower.charAt(i), small.charAt(i));
        }
    }

    private SmallCaps() {
    }

    public static String convert(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            char lower = Character.toLowerCase(c);
            Character mapped = MAP.get(lower);
            sb.append(mapped != null ? mapped : c);
        }
        return sb.toString();
    }
}
