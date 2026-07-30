package com.example.afktool.listeners;

import com.example.afktool.AFKTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final AFKTool plugin;

    public JoinListener(AFKTool plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getExpiryManager().scanPlayer(event.getPlayer());
    }
}
