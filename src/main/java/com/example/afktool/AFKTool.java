package com.example.afktool;

import com.example.afktool.commands.AFKToolCommand;
import com.example.afktool.listeners.HoeListener;
import com.example.afktool.listeners.JoinListener;
import com.example.afktool.listeners.MiningListener;
import com.example.afktool.managers.ExpiryManager;
import com.example.afktool.managers.ToolManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AFKTool extends JavaPlugin {

    private static AFKTool instance;
    private ToolManager toolManager;
    private ExpiryManager expiryManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.toolManager = new ToolManager(this);
        this.expiryManager = new ExpiryManager(this);

        AFKToolCommand executor = new AFKToolCommand(this);
        getCommand("afktool").setExecutor(executor);
        getCommand("afktool").setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new HoeListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        expiryManager.start();

        getLogger().info("AFKTool da duoc kich hoat!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AFKTool da tat.");
    }

    public static AFKTool getInstance() {
        return instance;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public ExpiryManager getExpiryManager() {
        return expiryManager;
    }

    public String msg(String path) {
        String m = getConfig().getString("messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', m);
    }

    /** Thu hoi 1 cong cu het han khoi tay nguoi choi va thong bao. */
    public void expireTool(Player player, ItemStack item) {
        String toolId = toolManager.getToolId(item);
        String toolName = toolId != null ? getConfig().getString("tools." + toolId + ".display-name", toolId) : "?";

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.isSimilar(item)) {
            if (hand.getAmount() <= 1) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                hand.setAmount(hand.getAmount() - 1);
            }
        }
        player.sendMessage(msg("expired").replace("%tool%", toolName));
    }
}
