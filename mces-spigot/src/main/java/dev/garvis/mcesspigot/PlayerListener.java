package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
	Map<String, Object> e = new HashMap<String, Object>();
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
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "CHAT_MESSAGE_PUBLISHED");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);
	e.put("message", event.getMessage());

	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_MOVED");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	e.put("fromWorld", event.getFrom().getWorld().getName());
	e.put("fromX", event.getFrom().getX());
	e.put("fromY", event.getFrom().getY());
	e.put("fromZ", event.getFrom().getZ());

	e.put("toWorld", event.getTo().getWorld().getName());
	e.put("toX", event.getTo().getX());
	e.put("toY", event.getTo().getY());
	e.put("toZ", event.getTo().getZ());

	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerDied(PlayerDeathEvent event) {
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_DIED");
	e.put("playerName", event.getEntity().getName());
	e.put("playerUUID", event.getEntity().getUniqueId().toString());
	e.put("server", serverName);

	e.put("message", event.getDeathMessage());
	
	e.put("world", event.getEntity().getLocation().getWorld().getName());
	e.put("x", event.getEntity().getLocation().getX());
	e.put("y", event.getEntity().getLocation().getY());
	e.put("z", event.getEntity().getLocation().getZ());

	kafka.sendMessage(e);
    }
}
