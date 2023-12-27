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
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

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
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_LEFT_SERVER");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	kafka.sendMessage(e);
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
	// you might be able to actually use the block value istead of the x,y,z
	if ( Math.floor(event.getFrom().getX()) == Math.floor(event.getTo().getX()) &&
	     Math.floor(event.getFrom().getY()) == Math.floor(event.getTo().getY()) &&
	     Math.floor(event.getFrom().getZ()) == Math.floor(event.getTo().getZ()) )
	    return;
	
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

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_ENTERED_BED");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	e.put("bedType", event.getBed().getType().toString());
	
	e.put("world", event.getBed().getLocation().getWorld().getName());
	e.put("x", event.getBed().getLocation().getX());
	e.put("y", event.getBed().getLocation().getY());
	e.put("z", event.getBed().getLocation().getZ());

	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_LEFT_BED");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	e.put("bedType", event.getBed().getType().toString());

	e.put("world", event.getBed().getLocation().getWorld().getName());
	e.put("x", event.getBed().getLocation().getX());
	e.put("y", event.getBed().getLocation().getY());
	e.put("z", event.getBed().getLocation().getZ());

	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
	if (! (event.getEntity() instanceof Player)) return;
	if (event.getDamager() instanceof Player) return; // if player damaged player, another event handles.

	Player p = (Player) event.getEntity();
	
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_DAMAGED");
	e.put("server", serverName);
	
	e.put("playerName", p.getName());
	e.put("playerUUID", p.getUniqueId().toString());
	
	e.put("x", p.getLocation().getX());
	e.put("y", p.getLocation().getY());
	e.put("z", p.getLocation().getZ());

	e.put("amount", event.getFinalDamage());
	e.put("cause", event.getCause().toString());
	e.put("damageFrom", event.getDamager().getType().toString());
	
	kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerDoesDamaged(EntityDamageByEntityEvent event) {
	if (event.getEntity() instanceof Player) return; // if player is damaged by player, another event handles
	if (! (event.getDamager() instanceof Player)) return;

	Player p = (Player) event.getDamager();
	
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_DOES_DAMAGED");
	e.put("server", serverName);
	
	e.put("playerName", p.getName());
	e.put("playerUUID", p.getUniqueId().toString());
	
	e.put("x", p.getLocation().getX());
	e.put("y", p.getLocation().getY());
	e.put("z", p.getLocation().getZ());

	e.put("amount", event.getFinalDamage());
	e.put("cause", event.getCause().toString());
	e.put("damageTo", event.getEntityType().toString());
	
	kafka.sendMessage(e);	
    }

    @EventHandler
    public void onPlayerDamagesPlayer(EntityDamageByEntityEvent event) {
	if (! (event.getEntity() instanceof Player)) return;
	if (! (event.getDamager() instanceof Player)) return;

	Player p = (Player) event.getDamager();
	Player attacked = (Player) event.getEntity();
	
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "PLAYER_DOES_DAMAGE_TO_PLAYER");
	e.put("server", serverName);
	
	e.put("playerName", p.getName());
	e.put("playerUUID", p.getUniqueId().toString());
	
	e.put("x", p.getLocation().getX());
	e.put("y", p.getLocation().getY());
	e.put("z", p.getLocation().getZ());

	e.put("attackedPlayerName", attacked.getName());
	e.put("attackedPlayerUUID", attacked.getUniqueId().toString());
	
	e.put("attackedPlayerX", attacked.getLocation().getX());
	e.put("attackedPlayerY", attacked.getLocation().getY());
	e.put("attackedPlayerZ", attacked.getLocation().getZ());
	
	e.put("amount", event.getFinalDamage());
	e.put("cause", event.getCause().toString());
	
	kafka.sendMessage(e);	
    }
    
}
