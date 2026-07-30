package com.example.afktool.managers;

import com.example.afktool.AFKTool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class ExpiryManager {

    private final AFKTool plugin;

    public ExpiryManager(AFKTool plugin) {
        this.plugin = plugin;
    }

    /** Bat dau task quet dinh ky (30 giay/lan) toan bo nguoi choi dang online. */
    public void start() {
        long periodTicks = 20L * 30;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    scanPlayer(player);
                }
            }
        }.runTaskTimer(plugin, periodTicks, periodTicks);
    }

    /** Quet toan bo tui do (36 o chinh + offhand) cua 1 nguoi choi, thu hoi cong cu het han. */
    public void scanPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        boolean changed = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;
            if (!plugin.getToolManager().isAfkTool(item)) continue;
            if (!plugin.getToolManager().isExpired(item)) continue;

            String toolId = plugin.getToolManager().getToolId(item);
            String toolName = plugin.getConfig().getString("tools." + toolId + ".display-name", toolId);
            contents[i] = null;
            changed = true;
            player.sendMessage(plugin.msg("expired").replace("%tool%", toolName));
        }

        if (changed) {
            inv.setContents(contents);
        }

        ItemStack off = inv.getItemInOffHand();
        if (plugin.getToolManager().isAfkTool(off) && plugin.getToolManager().isExpired(off)) {
            String toolId = plugin.getToolManager().getToolId(off);
            String toolName = plugin.getConfig().getString("tools." + toolId + ".display-name", toolId);
            inv.setItemInOffHand(new ItemStack(Material.AIR));
            player.sendMessage(plugin.msg("expired").replace("%tool%", toolName));
        }
    }
}
