package dev.garvis.mcesspigot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public DamageEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (! (event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof Player) return; // if player damaged player, another event handles.

        Player p = (Player) event.getEntity();

        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_DAMAGED");

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

        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_DOES_DAMAGED");

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

        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_DOES_DAMAGE_TO_PLAYER");

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
