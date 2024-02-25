package dev.garvis.mcesspigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.net.http.WebSocket;

public class SleepEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public SleepEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_ENTERED_BED");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        e.put("bedType", event.getBed().getType().toString());

        e.put("world", event.getBed().getLocation().getWorld().getName());
        e.put("x", event.getBed().getLocation().getX());
        e.put("y", event.getBed().getLocation().getY());
        e.put("z", event.getBed().getLocation().getZ());

        kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_LEFT_BED");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        e.put("bedType", event.getBed().getType().toString());

        e.put("world", event.getBed().getLocation().getWorld().getName());
        e.put("x", event.getBed().getLocation().getX());
        e.put("y", event.getBed().getLocation().getY());
        e.put("z", event.getBed().getLocation().getZ());

        kafka.sendMessage(e);
    }
}
