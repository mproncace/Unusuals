/*
 * This file is part of Unusuals, licensed under the MIT license (MIT).
 *
 * Copyright (c) 2014-2018 Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
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
package net.caseif.unusuals;

import com.google.common.collect.ImmutableSet;
import net.caseif.unusuals.handlers.BukkitParticleHandler;
import net.caseif.unusuals.handlers.IParticleHandler;
import net.caseif.unusuals.handlers.NmsParticleHandler;
import net.caseif.unusuals.nms.CraftBukkitHook;
import net.caseif.unusuals.typeprovider.BukkitParticleTypeProvider;
import net.caseif.unusuals.typeprovider.IParticleTypeProvider;
import net.caseif.unusuals.typeprovider.NmsParticleTypeProvider;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {

    private static final ImmutableSet<Material> HEADWEAR = ImmutableSet.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
            Material.GOLD_HELMET, Material.DIAMOND_HELMET, Material.PUMPKIN,
            Material.SKULL_ITEM
    );
    public static JavaPlugin plugin;
    public static Logger log;
    private static HashMap<UUID, UnusualEffect> players = new HashMap<UUID, UnusualEffect>();
    private static final ChatColor UNUSUAL_COLOR = ChatColor.DARK_PURPLE;
    private static final ChatColor EFFECT_COLOR = ChatColor.GRAY;

    static IParticleHandler handler;
    private static IParticleTypeProvider typeProvider;

    private static HashMap<String, UnusualEffect> effects = new HashMap<String, UnusualEffect>();

    public void onEnable() {
        plugin = this;
        log = getLogger();

        try {
            Class.forName("org.bukkit.Particle");
            handler = new BukkitParticleHandler();
            typeProvider = new BukkitParticleTypeProvider();
        } catch (ClassNotFoundException ex) {
            try {
                Class.forName("org.bukkit.craftbukkit.Main");
                handler = new NmsParticleHandler(new CraftBukkitHook());
                typeProvider = new NmsParticleTypeProvider();
            } catch (ClassNotFoundException ex2) {
                log.severe("Incompatible server software! Cannot continue, disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        if (plugin == null || !plugin.isEnabled()) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        saveDefaultConfig();

        // updater
        if (getConfig().getBoolean("enable-updater")) {
            new Updater(this, 80091, this.getFile(), Updater.UpdateType.DEFAULT, true);
        }

        // submit metrics
        if (getConfig().getBoolean("enable-metrics")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException ex) {
                log.warning("Failed to enable plugin metrics!");
            }
        }

        ConfigurationSection cs = getConfig().getConfigurationSection("effects");
        if (cs != null) {
            String[] nonSections = new String[]{"particles", "speed", "count", "radius"};
            for (String k : cs.getKeys(false)) { // root effects
                ConfigurationSection effectCs = cs.getConfigurationSection(k);
                if (effectCs != null) {
                    String effectName = effectCs.getName();

                    List<ParticleEffect> pEffects = new ArrayList<ParticleEffect>();
                    ParticleEffect eff = parseEffect(effectCs);
                    if (eff != null) {
                        pEffects.add(eff);
                    }
                    keyLoop:
                    for (String subKey : effectCs.getKeys(true)) { // subkeys of effects
                        for (String ns : nonSections) {
                            if (subKey.endsWith(ns)) {
                                continue keyLoop;
                            }
                        }
                        ConfigurationSection subCs = effectCs.getConfigurationSection(subKey);
                        if (subCs != null) {
                            ParticleEffect subEff = parseEffect(subCs);
                            if (subEff != null) {
                                pEffects.add(subEff);
                            }
                        }
                    }
                    if (pEffects.isEmpty()) {
                        getLogger().warning("Incomplete unusual effect definition for " + effectName + ", ignoring.");
                        continue;
                    }
                    effects.put(effectName, new UnusualEffect(effectName, pEffects));
                }
            }
        }
        log.info("Loaded " + effects.size() + " effects");

        for (Player p : Bukkit.getOnlinePlayers()) {
            checkForUnusual(p, p.getInventory().getHelmet());
        }

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (UUID p : players.keySet()) {
                    Player pl = Bukkit.getPlayer(p);
                    if (pl != null) {
                        players.get(p).display(pl);
                    } else {
                        players.remove(p);
                    }
                }
            }
        }, 0L, getConfig().getLong("effect-interval"));

        log.info(this + " has been enabled!");
    }

    private ParticleEffect parseEffect(ConfigurationSection subCs) {
        if (subCs.contains("particles") &&
                subCs.contains("speed") &&
                subCs.contains("count") &&
                subCs.contains("radius")) {
            Object type = typeProvider.getTypeFromId(subCs.getString("particles"));
            if (type != null) {
                return new ParticleEffect(type, (float) subCs.getDouble("speed"),
                        subCs.getInt("count"), (float) subCs.getDouble("radius"));
            }
        }
        return null;
    }

    public void onDisable() {
        log.info(this + " has been disabled!");
        log = null;
        plugin = null;
    }

    private ItemStack createUnusual(Material type, String effect) {
        UnusualEffect uEffect = effects.get(effect);
        if (uEffect == null) {
            throw new IllegalArgumentException("Effect \"" + effect + "\" does not exist!");
        }

        ItemStack is = new ItemStack(type, 1);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(UNUSUAL_COLOR + "Unusual " + WordUtils.capitalize(type.toString().toLowerCase().replace("_", " ")));
        List<String> lore = new ArrayList<String>();
        lore.add(EFFECT_COLOR + "Effect: " + effect);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("unusual") || label.equalsIgnoreCase("unusuals")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("spawn")) {
                    if (sender instanceof Player) {
                        if (sender.hasPermission("unusual.spawn")) {
                            if (args.length > 1) {
                                Material mat = Material.matchMaterial(args[1]);
                                if (mat != null) {
                                    if (!HEADWEAR.contains(mat)) {
                                        sender.sendMessage(ChatColor.RED + "Item must be headwear!");
                                        return true;
                                    }

                                    if (args.length > 2) {
                                        StringBuilder effectName = new StringBuilder();
                                        for (int i = 2; i < args.length; i++) {
                                            effectName.append(args[i]).append(i < args.length - 1 ? " " : "");
                                        }
                                        try {
                                            ((Player) sender).getInventory().addItem(createUnusual(mat, effectName.toString()));
                                            sender.sendMessage(ChatColor.DARK_PURPLE + "Enjoy your Unusual!");
                                        } catch (IllegalArgumentException ex) {
                                            if (ex.getMessage().contains("particle")) {
                                                sender.sendMessage(ChatColor.RED +
                                                        "The specified effect has an invalid particle type. " +
                                                        "Please report this to an administrator.");
                                            } else {
                                                sender.sendMessage(ChatColor.RED +
                                                        "Invalid effect! Usage: /unusual spawn " + mat.toString() +
                                                        " [effect name]");
                                            }
                                        }
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "Too few arguments! Usage: /unusual spawn " +
                                                mat.toString() +
                                                " [effect name]");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED +
                                            "Invalid material! Usage: /unusual spawn [materal] [effect name]");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED +
                                        "Too few arguments! Usage: /unusual spawn [material] [effect name]");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("unsuual.reload")) {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        reloadConfig();
                        Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("Unusuals"));
                        sender.sendMessage(ChatColor.GREEN + "[Unusuals] Successfully reloaded!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid arguments! Usage: /unusual [args]");
                }
            } else {
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "This server is running Unusuals v" + plugin.getDescription().getVersion() + " by Maxim Roncace");
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            if (event.getSlotType() == SlotType.ARMOR &&
                    ((event.getInventory().getType() == InventoryType.PLAYER && event.getSlot() == 5) || // wtf minecraft
                            (event.getInventory().getType() == InventoryType.CRAFTING && event.getSlot() == 39))) {
                if (!Main.checkForUnusual((Player) event.getWhoClicked(), event.getCursor())) {
                    players.remove(event.getWhoClicked().getUniqueId()); // remove the unusual effect
                }
            } else {
                if (event.getClick().isShiftClick() && HEADWEAR.contains(event.getCurrentItem().getType())) {
                    if (event.getSlotType() == SlotType.ARMOR) {
                        players.remove(event.getWhoClicked().getUniqueId());
                    } else {
                        Main.checkForUnusual((Player) event.getWhoClicked(), event.getCurrentItem());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkForUnusual(event.getPlayer(), event.getPlayer().getInventory().getHelmet());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        players.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        checkForUnusual(event.getPlayer(), event.getPlayer().getInventory().getHelmet());
    }

    private static boolean isUnusual(ItemStack itemstack) {
        return itemstack != null &&
                itemstack.getItemMeta() != null &&
                itemstack.getItemMeta().getLore() != null &&
                !itemstack.getItemMeta().getLore().isEmpty() &&
                itemstack.getItemMeta().getDisplayName().startsWith(UNUSUAL_COLOR + "Unusual ") &&
                itemstack.getItemMeta().getLore().get(0).startsWith(EFFECT_COLOR + "Effect: ");
    }

    private static boolean checkForUnusual(Player player, ItemStack itemstack) {
        if (isUnusual(itemstack)) {
            String effectName = itemstack.getItemMeta().getLore().get(0).replace(EFFECT_COLOR +
                    "Effect: ", "");// extract the effect name
            UnusualEffect uEffect = effects.get(effectName);
            if (uEffect != null) { // make sure the effect is loaded
                players.put(player.getUniqueId(), uEffect);
                return true;
            }
        }
        return false;
    }

}
