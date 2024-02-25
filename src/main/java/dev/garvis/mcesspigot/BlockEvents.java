package dev.garvis.mcesspigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public BlockEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "BLOCK_BROKEN");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        e.put("blockType", event.getBlock().getType().toString());

        e.put("x", event.getBlock().getX());
        e.put("y", event.getBlock().getY());
        e.put("z", event.getBlock().getZ());

        kafka.sendMessage(e);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "BLOCK_PLACED");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        e.put("blockType", event.getBlockPlaced().getType().toString());

        e.put("x", event.getBlockPlaced().getX());
        e.put("y", event.getBlockPlaced().getY());
        e.put("z", event.getBlockPlaced().getZ());

        e.put("placedAgainst", event.getBlockAgainst().getType().toString());

        kafka.sendMessage(e);
    }

}
