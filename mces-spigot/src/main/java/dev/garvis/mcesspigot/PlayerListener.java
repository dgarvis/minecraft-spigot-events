package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.HashMap;

public class PlayerListener implements Listener {

    private KafkaManager kafka;
    private String serverName;
    
    public PlayerListener(String serverName, KafkaManager kafka) {
	this.kafka = kafka;
	this.serverName = serverName;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
	Map<String, String> e = new HashMap<String, String>();
	e.put("eventType", "PLAYER_JOINED_SERVER");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
	Map<String, String> e = new HashMap<String, String>();
	e.put("eventType", "CHAT_MESSAGE_PUBLISHED");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);
	e.put("message", event.getMessage());

	kafka.sendMessage(e);
    }
}
