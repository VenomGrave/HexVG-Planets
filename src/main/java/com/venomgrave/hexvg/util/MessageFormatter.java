package com.venomgrave.hexvg.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MessageFormatter {

    private MessageFormatter() {}

    public static final String PREFIX = ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "HexVG" + ChatColor.WHITE + "-" + ChatColor.GOLD + "Planets" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;

    public static String colorize(String msg) {
        if (msg == null) return "";
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String strip(String msg) {
        if (msg == null) return "";
        return ChatColor.stripColor(colorize(msg));
    }

    public static String info(String msg) {
        return PREFIX + ChatColor.GRAY + "▶ " + colorize(msg);
    }

    public static String ok(String msg) {
        return PREFIX + ChatColor.GREEN + "✔ " + colorize(msg);
    }

    public static String err(String msg) {
        return PREFIX + ChatColor.RED + "✖ " + colorize(msg);
    }

    public static String warn(String msg) {
        return PREFIX + ChatColor.YELLOW + "⚠ " + colorize(msg);
    }

    public static String debug(String msg) {
        return ChatColor.DARK_GRAY + "[debug] " + ChatColor.GRAY + msg;
    }


    public static void sendInfo(CommandSender s, String msg)  { s.sendMessage(info(msg));  }
    public static void sendOk(CommandSender s, String msg)    { s.sendMessage(ok(msg));    }
    public static void sendErr(CommandSender s, String msg)   { s.sendMessage(err(msg));   }
    public static void sendWarn(CommandSender s, String msg)  { s.sendMessage(warn(msg));  }

    public static String header(String title) {
        String t = ChatColor.GOLD + " " + strip(title) + " ";
        int fill = Math.max(0, 50 - t.length());
        String line = ChatColor.DARK_AQUA + "══";
        String right = ChatColor.DARK_AQUA + "═".repeat(Math.max(2, fill));
        return line + ChatColor.GOLD + t + right;
    }

    public static String separator() {
        return ChatColor.DARK_AQUA + "═".repeat(52);
    }


    public static String kv(String key, Object value) {
        return ChatColor.GRAY + "  " + ChatColor.AQUA + key + ChatColor.DARK_GRAY + ": " + ChatColor.WHITE + value;
    }

    public static String kv(String key, Object value, ChatColor valueColor) {
        return ChatColor.GRAY + "  " + ChatColor.AQUA + key + ChatColor.DARK_GRAY + ": " + valueColor + value;
    }


    public static String bar(double progress, int width, ChatColor filled, ChatColor empty) {
        progress = Math.max(0, Math.min(1, progress));
        int filledCount = (int) Math.round(progress * width);
        int emptyCount  = width - filledCount;
        return filled + "█".repeat(filledCount) + empty  + "░".repeat(emptyCount);
    }

    public static String bar(double progress, int width) {
        return bar(progress, width, ChatColor.GREEN, ChatColor.DARK_GRAY);
    }

    public static String barLabeled(String label, double progress, int width) {
        int pct = (int) Math.round(progress * 100);
        ChatColor color = progress > 0.6 ? ChatColor.GREEN :
                progress > 0.3 ? ChatColor.YELLOW : ChatColor.RED;
        return ChatColor.GRAY + label + " " + ChatColor.DARK_GRAY + "[" +
                bar(progress, width, color, ChatColor.DARK_GRAY) +
                ChatColor.DARK_GRAY + "] " + color + pct + "%";
    }

    private static final int DEFAULT_PAGE_SIZE = 8;

    public static void sendPage(CommandSender sender, List<String> items,
                                String title, int page, int pageSize) {
        if (items == null || items.isEmpty()) {
            sender.sendMessage(info("&7(brak wyników)"));
            return;
        }

        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        page = Math.max(1, Math.min(page, totalPages));
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, items.size());

        sender.sendMessage(header(title + " &8[" + page + "/" + totalPages + "]"));
        for (int i = from; i < to; i++) {
            sender.sendMessage(items.get(i));
        }
        if (totalPages > 1) {
            sender.sendMessage(ChatColor.DARK_GRAY + "Strona " + page + "/" + totalPages + " – użyj &7/" + "planetslist [strona]&8 aby przejść dalej.");
        }
    }

    public static void sendPage(CommandSender sender, List<String> items,
                                String title, int page) {
        sendPage(sender, items, title, page, DEFAULT_PAGE_SIZE);
    }

    public static List<String> formatNumbered(Collection<String> items,
                                              ChatColor indexColor, ChatColor itemColor) {
        List<String> result = new ArrayList<>();
        int i = 1;
        for (String item : items) {
            result.add(indexColor + String.valueOf(i++) + ". " + itemColor + item);
        }
        return result;
    }

    public static List<String> formatNumbered(Collection<String> items) {
        return formatNumbered(items, ChatColor.GRAY, ChatColor.WHITE);
    }

    public static String formatDuration(long seconds) {
        if (seconds < 0) return "0s";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        sb.append(s).append("s");
        return sb.toString().trim();
    }

    public static String formatNumber(long n) {
        return String.format("%,d", n).replace(',', ' ');
    }

    public static String formatCoords(double x, double y, double z) {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }
}