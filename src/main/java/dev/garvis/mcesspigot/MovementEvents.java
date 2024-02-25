package dev.garvis.mcesspigot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public MovementEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // you might be able to actually use the block value istead of the x,y,z
        if ( Math.floor(event.getFrom().getX()) == Math.floor(event.getTo().getX()) &&
                Math.floor(event.getFrom().getY()) == Math.floor(event.getTo().getY()) &&
                Math.floor(event.getFrom().getZ()) == Math.floor(event.getTo().getZ()) )
            return;

        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_MOVED");
        e.put("playerName", event.getPlayer().getName());
        e.put("playerUUID", event.getPlayer().getUniqueId().toString());

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

}
