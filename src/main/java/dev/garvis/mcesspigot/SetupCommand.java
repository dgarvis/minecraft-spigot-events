package dev.garvis.mcesspigot;

import dev.garvis.mcesspigot.KafkaManager;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetupCommand implements CommandExecutor {

    private KafkaManager kafka;
    
    public SetupCommand(KafkaManager kafka) {
	this.kafka = kafka;
    }
    
    // Return true for valid command, otherwise false
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!sender.isOp()) return false;

	// connect to kafka

	// save config

	// Update server name somehow?
	
        return true;
    }
}
