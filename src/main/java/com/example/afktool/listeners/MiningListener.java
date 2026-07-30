package com.example.afktool.listeners;

import com.example.afktool.AFKTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MiningListener implements Listener {

    private final AFKTool plugin;

    public MiningListener(AFKTool plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        String toolId = plugin.getToolManager().getToolId(hand);
        if (toolId == null) return;
        if (!toolId.equals("cup") && !toolId.equals("riu") && !toolId.equals("xeng")) return;

        if (plugin.getToolManager().isExpired(hand)) {
            plugin.expireTool(player, hand);
            return;
        }

        Block origin = event.getBlock();
        BlockFace facing = player.getFacing();
        List<Block> targets = getAoeBlocks(origin, facing);

        // Rieng Riu/Xeng: neu vung 3x3 co block thuoc loai chi Cup moi dao duoc (da/quang)
        // thi HUY toan bo AOE (khong dao them block nao ca, chi block goc van vo binh thuong)
        if (toolId.equals("riu") || toolId.equals("xeng")) {
            for (Block b : targets) {
                if (Tag.MINEABLE_PICKAXE.isTagged(b.getType())) {
                    player.sendMessage(plugin.msg("blocked-by-stone"));
                    return;
                }
            }
        }

        Tag<Material> requiredTag = switch (toolId) {
            case "cup" -> Tag.MINEABLE_PICKAXE;
            case "riu" -> Tag.MINEABLE_AXE;
            case "xeng" -> Tag.MINEABLE_SHOVEL;
            default -> null;
        };
        if (requiredTag == null) return;

        for (Block b : targets) {
            if (b.equals(origin)) continue; // block goc da duoc su kien vanilla xu ly
            if (b.getType() == Material.AIR) continue;
            if (!requiredTag.isTagged(b.getType())) continue;
            // Khong pha cac block co du lieu rieng (ruong, bien, dau giuong...) de tranh grief
            if (b.getState() instanceof TileState) continue;

            b.breakNaturally(hand);
        }
    }

    private List<Block> getAoeBlocks(Block origin, BlockFace facing) {
        List<Block> result = new ArrayList<>();
        int radius = plugin.getConfig().getInt("settings.radius", 1);
        Location loc = origin.getLocation();

        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                Block block = switch (facing) {
                    case NORTH, SOUTH -> loc.clone().add(a, b, 0).getBlock();
                    case EAST, WEST -> loc.clone().add(0, b, a).getBlock();
                    default -> loc.clone().add(a, 0, b).getBlock(); // UP/DOWN -> mat phang ngang XZ
                };
                result.add(block);
            }
        }
        return result;
    }
}
