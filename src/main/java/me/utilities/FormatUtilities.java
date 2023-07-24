package me.utilities;

import org.bukkit.ChatColor;

import java.text.DecimalFormat;

public class FormatUtilities {
    public static String deleteSubstring(String originalString, String substringToDelete) {
        if (originalString == null || substringToDelete == null) {
            return originalString;
        }

        int startIndex = originalString.indexOf(substringToDelete);
        if (startIndex != -1) {
            StringBuilder resultBuilder = new StringBuilder(originalString);
            resultBuilder.replace(startIndex, startIndex + substringToDelete.length(), "");
            return resultBuilder.toString();
        }

        return originalString;
    }

    public static String color(String string) {
        if (string == null) {
            return "null";
        }
        return ChatColor.translateAlternateColorCodes('&',string);
    }

    public static String convertToAccountingFormat(int number) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(number);
    }

}
