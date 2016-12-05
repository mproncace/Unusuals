/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Maxim Roncacé
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.amigocraft.unusuals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import net.amigocraft.unusuals.nms.CraftBukkitHook;
import net.amigocraft.unusuals.nms.NmsHook;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public static JavaPlugin plugin;
	public static Logger log;
	public static HashMap<UUID, UnusualEffect> players = new HashMap<UUID, UnusualEffect>();
	private static final int UNUSUAL_COLOR = 5;
	private static final int EFFECT_COLOR = 7;

	public static NmsHook hook;

	private static HashMap<String, UnusualEffect> effects = new HashMap<String, UnusualEffect>();

	public void onEnable(){
		plugin = this;
		log = getLogger();

		try {
			Class.forName("org.bukkit.craftbukkit.Main");
			hook = new CraftBukkitHook();
		}
		catch (ClassNotFoundException ex){
			log.severe("Incompatible server software! Cannot continue, disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		hook.isCompatible();
		if (plugin == null || !plugin.isEnabled())
			return;

		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();

		// updater
		if (getConfig().getBoolean("enable-updater")){
			new Updater(this, 80091, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}

		// submit metrics
		if (getConfig().getBoolean("enable-metrics")){
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			}
			catch (IOException ex){
				log.warning("Failed to enable plugin metrics!");
			}
		}

		ConfigurationSection cs = getConfig().getConfigurationSection("effects");
		if (cs != null) {
			String[] nonSections = new String[]{"particles", "effects", "speed", "count"};
			for (String k : cs.getKeys(false)) { // root effects
				ConfigurationSection effect = cs.getConfigurationSection(k);
				if (effect != null) {
					List<ParticleEffect> pEffects = new ArrayList<ParticleEffect>();
					if (effect.contains("particles") &&
							effect.contains("speed") &&
							effect.contains("count") &&
							effect.contains("radius")) {
						ParticleType type = ParticleType.valueOf(effect.getString("particles"));
						if (type != null) {
							ParticleEffect pEffect = new ParticleEffect(type, (float)effect.getDouble("speed"),
									effect.getInt("count"), (float)effect.getDouble("radius"));
							pEffects.add(pEffect);
						}
					}
					keyLoop: // HOLY CRAP HOW DID I NOT KNOW ABOUT THIS FOR TWO AND A HALF YEARS
					for (String subKey : effect.getKeys(true)) { // subkeys of effects
						for (String ns : nonSections) {
							if (k.endsWith(ns)) {
								continue keyLoop;
							}
						}
						ConfigurationSection subCs = effect.getConfigurationSection(subKey);
						if (subCs != null) {
							if (subCs.contains("particles") &&
									subCs.contains("speed") &&
									subCs.contains("count") &&
									subCs.contains("radius")) {
								ParticleType type = ParticleType.valueOf(subCs.getString("particles"));
								if (type != null) {
									ParticleEffect pEffect = new ParticleEffect(type, (float)subCs.getDouble("speed"),
											subCs.getInt("count"), (float)subCs.getDouble("radius"));
									pEffects.add(pEffect);
								}
							}
						}
					}
					effects.put(effect.getName(), new UnusualEffect(effect.getName(), pEffects));
				}
			}
		}
		log.info("Loaded " + effects.size() + " effects");

		for (Player p : Bukkit.getOnlinePlayers()){
			checkForUnusual(p, p.getInventory().getHelmet());
		}

		Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			public void run(){
				synchronized(players){
					for (UUID p : players.keySet()){
						Player pl = Bukkit.getPlayer(p);
						if (pl != null)
							players.get(p).display(pl);
						else
							players.remove(p);
					}
				}
			}
		}, 0L, getConfig().getLong("effect-interval"));

		log.info(this + " has been enabled!");
	}

	public void onDisable(){
		log.info(this + " has been disabled!");
		log = null;
		plugin = null;
	}

	public ItemStack createUnusual(Material type, String effect){
		UnusualEffect uEffect = effects.get(effect);
		if (effect != null){
			ItemStack is = new ItemStack(type, 1);
			ItemMeta meta = is.getItemMeta();
			meta.setDisplayName("§" + UNUSUAL_COLOR + "Unusual " + WordUtils.capitalize(type.toString().toLowerCase().replace("_", " ")));
			List<String> lore = new ArrayList<String>();
			lore.add("§" + EFFECT_COLOR + "Effect: " + effect);
			meta.setLore(lore);
			is.setItemMeta(meta);
			return is;
		}
		else
			throw new IllegalArgumentException("Effect \"" + effect + "\" does not exist!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("unusual") || label.equalsIgnoreCase("unusuals")){
			if (args.length > 0){
				if (args[0].equalsIgnoreCase("spawn")){
					if (sender instanceof Player){
						if (sender.hasPermission("unusual.spawn")){
							if (args.length > 1){
								Material mat = Material.matchMaterial(args[1]);
								if (mat != null){
									if (args.length > 2){
										String effectName = "";
										for (int i = 2; i < args.length; i++)
											effectName += args[i] + (i < args.length - 1 ? " " : "");
										try {
											((Player)sender).getInventory().addItem(createUnusual(mat, effectName));
											sender.sendMessage(ChatColor.DARK_PURPLE + "Enjoy your Unusual!");
										}
										catch (IllegalArgumentException ex){
											if (ex.getMessage().contains("particle"))
												sender.sendMessage(ChatColor.RED +
														"The specified effect has an invalid particle type. " +
														"Please report this to an administrator.");
											else
												sender.sendMessage(ChatColor.RED +
														"Invalid effect! Usage: /unusual spawn " + mat.toString() +
														" [effect name]");
										}
									}
									else
										sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /unusual spawn " +
												mat.toString() +
												" [effect name]");
								}
								else
									sender.sendMessage(ChatColor.RED +
											"Invalid material! Usage: /unusual spawn [materal] [effect name]");
							}
							else
								sender.sendMessage(ChatColor.RED +
										"Too few arguments! Usage: /unusual spawn [material] [effect name]");
						}
						else
							sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					}
					else
						sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
				}
				else if (args[0].equalsIgnoreCase("reload")){
					if (sender.hasPermission("unsuual.reload")){
						Bukkit.getPluginManager().disablePlugin(plugin);
						reloadConfig();
						Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("Unusuals"));
						sender.sendMessage(ChatColor.GREEN + "[Unusuals] Successfully reloaded!");
					}
					else
						sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				}
				else
					sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /unusual [args]");
			}
			else
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "This server is running Unusuals v" + plugin.getDescription().getVersion() + " by Maxim Roncace");
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e){
		synchronized(players){
			if (e.getInventory().getHolder() instanceof Player){
				if (e.getSlotType() == SlotType.ARMOR &&
						((e.getInventory().getType() == InventoryType.PLAYER && e.getSlot() == 5) || // wtf minecraft
								(e.getInventory().getType() == InventoryType.CRAFTING && e.getSlot() == 39))){
					if (isUnusual(e.getCurrentItem())) {
						players.remove(((e.getWhoClicked())).getUniqueId()); // remove the unusual effect
					}
					else {
						Main.checkForUnusual((Player) e.getWhoClicked(), e.getCursor());
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreativeInventoryEvent(InventoryCreativeEvent e){
		synchronized(players){
			if (e.getInventory().getHolder() instanceof Player){
				if (e.getSlotType() == SlotType.ARMOR &&
						((e.getInventory().getType() == InventoryType.PLAYER && e.getSlot() == 5) || // wtf minecraft
								(e.getInventory().getType() == InventoryType.CRAFTING && e.getSlot() == 39))){
					if (isUnusual(e.getCurrentItem())) {
						players.remove(((e.getWhoClicked())).getUniqueId()); // remove the unusual effect
					}
					else {
						Main.checkForUnusual((Player) e.getWhoClicked(), e.getCursor());
					}
				}
			}
		}
	}
	
	
	

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e){
		checkForUnusual(e.getPlayer(), e.getPlayer().getInventory().getHelmet());
	}

	public static boolean isUnusual(ItemStack itemstack){
		return itemstack != null &&
				itemstack.getItemMeta() != null &&
				itemstack.getItemMeta().getLore() != null &&
				!itemstack.getItemMeta().getLore().isEmpty() &&
				itemstack.getItemMeta().getDisplayName().startsWith("§" + UNUSUAL_COLOR + "Unusual ") &&
				itemstack.getItemMeta().getLore().get(0).startsWith("§" + EFFECT_COLOR + "Effect: ");
	}

	public static void checkForUnusual(Player player, ItemStack itemstack){
		synchronized(players){
			if (isUnusual(itemstack)){
				String effectName = itemstack.getItemMeta().getLore().get(0).replace("§" + EFFECT_COLOR +
						"Effect: ", "");// extract the effect name
				UnusualEffect uEffect = effects.get(effectName);
				if (uEffect != null){ // make sure the effect is loaded
					players.put(player.getUniqueId(), uEffect);
				}
			}
		}
	}

}
