package dev.garvis.mcesspigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.http.WebSocket;

public class ChatEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public ChatEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_JOINED_SERVER");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_LEFT_SERVER");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

        kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "CHAT_MESSAGE_PUBLISHED");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());
        e.put("message", event.getMessage());

        kafka.sendMessage(e);
    }

    @EventHandler
    public void onPlayerDied(PlayerDeathEvent event) {
        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_DIED");
        e.put("playerName", event.getEntity().getName());
        e.put("playerUUID", event.getEntity().getUniqueId().toString());

        e.put("message", event.getDeathMessage());

        e.put("world", event.getEntity().getLocation().getWorld().getName());
        e.put("x", event.getEntity().getLocation().getX());
        e.put("y", event.getEntity().getLocation().getY());
        e.put("z", event.getEntity().getLocation().getZ());

        kafka.sendMessage(e);
    }
}
