package dev.garvis.mcesspigot;

import org.bukkit.plugin.java.JavaPlugin;

public class MCESSpigotPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
	getLogger().info("onEnable");
    }

    @Override
    public void onDisable() {
	getLogger().info("onDisable");
    }
}
