package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.ClickType;

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

	// Let's calculate the amount of it crafted.
	// https://www.spigotmc.org/threads/how-to-get-amount-of-item-crafted.377598/#post-3896072
	ItemStack craftedItem = event.getInventory().getResult();
	Inventory inventory = event.getInventory();
	ClickType clickType = event.getClick();
	int realAmount = craftedItem.getAmount();
	if (clickType.isShiftClick()) {
	    int lowerAmount = craftedItem.getMaxStackSize() + 1000;
	    for (ItemStack actualItem : inventory.getContents()){
		// If slot is not air and lower amount is higher than this slot amonut and it's not the reipe amount
		if (!actualItem.getType().isAir() && lowerAmount > actualItem.getAmount() && !actualItem.getType().equals(craftedItem.getType())) {
		    lowerAmount = actualItem.getAmount();
		}		
	    }
	    realAmount = lowerAmount * craftedItem.getAmount();
	}
	
	e.put("amount", realAmount);

	kafka.sendMessage(e);
    }

}
