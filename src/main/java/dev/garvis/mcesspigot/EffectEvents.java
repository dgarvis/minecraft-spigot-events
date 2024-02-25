package dev.garvis.mcesspigot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

public class EffectEvents implements Listener {

    private final KafkaManagerV2 kafka;

    public EffectEvents(KafkaManagerV2 kafka) {
        this.kafka = kafka;
    }

    @EventHandler
    public void onPlayerPotionEffect(EntityPotionEffectEvent event) {
        if (! (event.getEntity() instanceof Player)) return;
        Player p = (Player) event.getEntity();

        KafkaManagerV2.Message e = this.kafka.new Message();
        e.put("eventType", "PLAYER_POTION_EFFECT");
        e.put("playerName", p.getName());
        e.put("playerUUID", p.getUniqueId().toString());

        e.put("action", event.getAction().toString());
        e.put("cause", event.getCause().toString());

        PotionEffect effect = event.getNewEffect();
        if (effect == null)
            effect = event.getOldEffect();

        //e.put("type", effect.getKey().toString());
        e.put("type", effect.getType().getKey().toString());
        e.put("duration", effect.getDuration());
        e.put("amplifier", effect.getAmplifier());

        kafka.sendMessage(e);
    }
}
