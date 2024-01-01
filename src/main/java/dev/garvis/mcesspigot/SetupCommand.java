package dev.garvis.mcesspigot;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetupCommand implements CommandExecutor {

    @FunctionalInterface
    interface SetupCallback {
	void apply(String name, String broker, String topic);
    }    

    private SetupCallback callback;
    
    public SetupCommand(SetupCallback callback) {
	this.callback = callback;
    }
    
    // Return true for valid command, otherwise false
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!sender.isOp()) return false;

	// Check inputs
	if (args.length != 3) {
	    return false;
	}
	String name = args[0];
	String broker = args[1];
	String topic = args[2];
	    
	// save config
	this.callback.apply(name, broker, topic);

	sender.sendMessage("Settings updated. However, they might not change until after a restart.");
	
        return true;
    }
}
