package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.Map;
import java.util.HashMap;

public class InventoryListener implements Listener {

    private KafkaManager kafka;
    private String serverName;
    
    public InventoryListener(String serverName, KafkaManager kafka) {
	this.kafka = kafka;
	this.serverName = serverName;
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
	if (! (event.getWhoClicked() instanceof Player)) {
	    return;
	}
	
	Player player = (Player)event.getWhoClicked();
	
	Map<String, Object> e = new HashMap<String, Object>();
	e.put("eventType", "ITEM_CRAFTED");
	e.put("playerName", player.getName());
	e.put("playerUUID", player.getUniqueId().toString());
	e.put("server", serverName);

	e.put("item", event.getRecipe().getResult().getType().toString());
	e.put("amount", event.getRecipe().getResult().getAmount());

	kafka.sendMessage(e);
    }

}
