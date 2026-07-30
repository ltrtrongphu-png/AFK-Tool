package com.example.afktool.listeners;

import com.example.afktool.AFKTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class HoeListener implements Listener {

    private final AFKTool plugin;

    private static final Set<Material> TILLABLE = EnumSet.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH, Material.ROOTED_DIRT
    );

    public HoeListener(AFKTool plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        String toolId = plugin.getToolManager().getToolId(hand);
        if (!"cuoc".equals(toolId)) return;

        if (plugin.getToolManager().isExpired(hand)) {
            plugin.expireTool(player, hand);
            event.setCancelled(true);
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null || !TILLABLE.contains(clicked.getType())) return;

        event.setCancelled(true);

        int radius = plugin.getConfig().getInt("settings.radius", 1);
        Location loc = clicked.getLocation();
        int tilled = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block b = loc.clone().add(x, 0, z).getBlock();
                if (TILLABLE.contains(b.getType())) {
                    b.setType(Material.FARMLAND);
                    tilled++;
                }
            }
        }

        if (tilled > 0) {
            player.playSound(player.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 1.0f);
        }
    }
}
