package net.amigocraft.unusuals;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public static JavaPlugin plugin;
	public static Logger log;
	
	public void onEnable(){
		if (!ParticleEffect.isCompatible()){
			getLogger().severe("This version of Craftbukkit is not supported! Disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		plugin = this;
		log = getLogger();
		log.info(this + " has been enabled!");
	}
	
	public void onDisable(){
		log.info(this + " has been disabled!");
		log = null;
		plugin = null;
	}
	
}
