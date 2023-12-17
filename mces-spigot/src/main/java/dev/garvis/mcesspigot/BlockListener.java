package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;
import java.util.HashMap;

public class BlockListener implements Listener {

    private KafkaManager kafka;
    private String serverName;
    
    public BlockListener(String serverName, KafkaManager kafka) {
	this.kafka = kafka;
	this.serverName = serverName;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "BLOCK_BROKEN");
	e.put("playerName", event.getPlayer().getName());
	e.put("playerUUID", event.getPlayer().getUniqueId().toString());
	e.put("server", serverName);

	e.put("blockType", event.getBlock().getType().toString());

	e.put("x", event.getBlock().getX());
	e.put("y", event.getBlock().getY());
	e.put("z", event.getBlock().getZ());
	
	kafka.sendMessage(e);
    }
}
