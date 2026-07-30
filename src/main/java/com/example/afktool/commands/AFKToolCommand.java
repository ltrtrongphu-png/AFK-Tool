package com.example.afktool.commands;

import com.example.afktool.AFKTool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AFKToolCommand implements CommandExecutor, TabCompleter {

    private final AFKTool plugin;
    private static final List<String> TOOL_IDS = Arrays.asList("cup", "riu", "xeng", "cuoc");

    public AFKToolCommand(AFKTool plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("afktool.admin")) {
            sender.sendMessage(ChatColor.RED + "Ban khong co quyen dung lenh nay.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGive(sender, args);
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Da reload config.yml cua AFKTool!");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Dung: /afktool give <player> <cup|riu|xeng|cuoc> [so_luong]");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Khong tim thay nguoi choi: " + args[1]);
            return;
        }

        String toolId = args[2].toLowerCase();
        if (!TOOL_IDS.contains(toolId)) {
            sender.sendMessage(ChatColor.RED + "Loai cong cu khong hop le. Chon: cup, riu, xeng, cuoc");
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ignored) {
            }
        }

        ItemStack item = plugin.getToolManager().createTool(toolId);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Khong the tao cong cu " + toolId + " (kiem tra config.yml).");
            return;
        }
        item.setAmount(amount);

        for (ItemStack extra : target.getInventory().addItem(item).values()) {
            target.getWorld().dropItem(target.getLocation(), extra);
        }

        String toolName = plugin.getConfig().getString("tools." + toolId + ".display-name", toolId);
        target.sendMessage(plugin.msg("given").replace("%tool%", toolName));
        sender.sendMessage(ChatColor.GREEN + "Da tang " + amount + "x " + toolName + " cho " + target.getName());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AFKTool ===");
        sender.sendMessage(ChatColor.AQUA + "/afktool give <player> <cup|riu|xeng|cuoc> [so_luong]");
        sender.sendMessage(ChatColor.AQUA + "/afktool reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("afktool.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return filter(Arrays.asList("give", "reload"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(TOOL_IDS, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String low = input.toLowerCase();
        List<String> result = new ArrayList<>(options.stream()
                .filter(s -> s.toLowerCase().startsWith(low))
                .collect(Collectors.toList()));
        Collections.sort(result);
        return result;
    }
}
