package com.example.afktool.managers;

import com.example.afktool.AFKTool;
import com.example.afktool.util.GradientUtil;
import com.example.afktool.util.SmallCaps;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ToolManager {

    private final AFKTool plugin;
    private final NamespacedKey toolIdKey;
    private final NamespacedKey expiryKey;

    public ToolManager(AFKTool plugin) {
        this.plugin = plugin;
        this.toolIdKey = new NamespacedKey(plugin, "afktool_id");
        this.expiryKey = new NamespacedKey(plugin, "afktool_expiry");
    }

    public ItemStack createTool(String toolId) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("tools." + toolId);
        if (section == null) return null;

        Material material = Material.matchMaterial(section.getString("material", "NETHERITE_PICKAXE"));
        if (material == null) material = Material.NETHERITE_PICKAXE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String rawName = section.getString("display-name", toolId);
        String smallCapsName = SmallCaps.convert(rawName);
        String startHex = plugin.getConfig().getString("settings.gradient.start", "5CE1E6");
        String endHex = plugin.getConfig().getString("settings.gradient.end", "FFFFFF");
        String coloredName = GradientUtil.apply(smallCapsName, startHex, endHex);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', coloredName));

        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        long durationDays = plugin.getConfig().getLong("settings.duration-days", 3);
        long expiry = System.currentTimeMillis() + durationDays * 24L * 60L * 60L * 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lore.add(ChatColor.translateAlternateColorCodes('&', "&8Hết hạn: &7" + sdf.format(new Date(expiry))));
        meta.setLore(lore);

        if (section.getBoolean("unbreakable", true)) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        ConfigurationSection enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String enchantKey : enchantSection.getKeys(false)) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantKey.toLowerCase()));
                if (enchantment == null) {
                    plugin.getLogger().warning("Khong tim thay enchantment: " + enchantKey + " (bo qua).");
                    continue;
                }
                int level = enchantSection.getInt(enchantKey, 1);
                meta.addEnchant(enchantment, level, true);
            }
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(toolIdKey, PersistentDataType.STRING, toolId);
        meta.getPersistentDataContainer().set(expiryKey, PersistentDataType.LONG, expiry);

        item.setItemMeta(meta);
        return item;
    }

    public String getToolId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(toolIdKey, PersistentDataType.STRING);
    }

    public Long getExpiry(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(expiryKey, PersistentDataType.LONG);
    }

    public boolean isExpired(ItemStack item) {
        Long expiry = getExpiry(item);
        return expiry != null && System.currentTimeMillis() >= expiry;
    }

    public boolean isAfkTool(ItemStack item) {
        return getToolId(item) != null;
    }
}
