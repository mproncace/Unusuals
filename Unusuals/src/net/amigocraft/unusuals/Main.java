package net.amigocraft.unusuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

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
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	public static JavaPlugin plugin;
	public static Logger log;
	public static HashMap<UUID, UnusualEffect> players = new HashMap<UUID, UnusualEffect>();
	private static final int UNUSUAL_COLOR = 5;
	private static final int EFFECT_COLOR = 7;

	public void onEnable(){
		if (!ParticleEffect.isCompatible()){
			getLogger().severe("This version of Craftbukkit is not supported! Disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		plugin = this;
		log = getLogger();

		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();

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
		ConfigurationSection cs = getConfig().getConfigurationSection("effects." + effect);
		if (cs != null){
			ParticleEffect pEffect = ParticleEffect.valueOf(cs.getString("particles"));
			if (pEffect != null){
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
				throw new IllegalArgumentException("Invalid particle type for effect \"" + effect + "\"");
		}
		else
			throw new IllegalArgumentException("Effect \"" + effect + "\" does not exist!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (label.equalsIgnoreCase("unusual")){
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
												sender.sendMessage(ChatColor.RED + "The specified effect has an invalid particle type. " +
														"Please report this to an administrator.");
											else
												sender.sendMessage(ChatColor.RED + "Invalid effect! Usage: /unusual spawn " + mat.toString() +
														" [effect name]");
										}
									}
									else
										sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /unusual spawn " + mat.toString() +
												" [effect name]");
								}
								else
									sender.sendMessage(ChatColor.RED + "Invalid material! Usage: /unusual spawn [materal] [effect name]");
							}
							else
								sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /unusual spawn [material] [effect name]");
						}
						else
							sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
					}
					else
						sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
				}
				else
					sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /unusual [args]");
			}
			else
				sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /unusual [args]");
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e){
		synchronized(players){
			if (e.getInventory() instanceof PlayerInventory){
				if (e.getSlotType() == SlotType.ARMOR && e.getSlot() == 5){ // helmet slot
					if (isUnusual(e.getCurrentItem()))
						if (!e.getViewers().isEmpty())
							players.remove(((Player)(e.getWhoClicked())).getUniqueId()); // remove the unusual effect from the player
					Main.checkForUnusual((Player)e.getWhoClicked(), e.getCursor());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e){
		checkForUnusual(e.getPlayer(), e.getPlayer().getInventory().getHelmet());
	}

	public static boolean isUnusual(ItemStack itemstack){
		if (itemstack != null &&
				itemstack.getItemMeta() != null &&
				itemstack.getItemMeta().getLore() != null &&
				!itemstack.getItemMeta().getLore().isEmpty() &&
				itemstack.getItemMeta().getDisplayName().startsWith("§" + UNUSUAL_COLOR + "Unusual ") &&
				itemstack.getItemMeta().getLore().get(0).startsWith("§" + EFFECT_COLOR + "Effect: "))
			return true;
		return false;
	}

	public static void checkForUnusual(Player player, ItemStack itemstack){
		synchronized(players){
			if (isUnusual(itemstack)){
				String effectName = itemstack.getItemMeta().getLore().get(0).replace("§" + EFFECT_COLOR +
						"Effect: ", "");// extract the effect name
				ConfigurationSection cs = Main.plugin.getConfig().getConfigurationSection("effects." + effectName);
				if (cs != null){ // make sure the effect is defined
					ParticleEffect pEffect = ParticleEffect.valueOf(cs.getString("particles")); // get the particle effect
					if (pEffect != null){ // make sure the particle effect exists
						UnusualEffect uEffect = new UnusualEffect(effectName, pEffect,
								cs.getDouble("speed"), cs.getInt("count"), cs.getDouble("radius")); // construct the effect
						players.put(player.getUniqueId(), uEffect);
					}
				}
			}
		}
	}

}
