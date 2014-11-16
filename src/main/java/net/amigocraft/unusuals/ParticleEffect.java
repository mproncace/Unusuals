package net.amigocraft.unusuals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Particle Effects Lib
 *
 * @author minnymin3
 *
 * This class has been greatly rewritten for this plugin for efficiency.
 */

public enum ParticleEffect {

	HUGE_EXPLODE("hugeexplosion", 0),
	LARGE_EXPLODE("largeexplode", 1),
	FIREWORK_SPARK("fireworksSpark", 2),
	AIR_BUBBLE("bubble", 3),
	SUSPEND("suspend", 4),
	DEPTH_SUSPEND("depthSuspend", 5),
	TOWN_AURA("townaura", 6),
	CRITICAL_HIT("crit", 7),
	MAGIC_CRITICAL_HIT("magicCrit", 8),
	MOB_SPELL("mobSpell", 9),
	MOB_SPELL_AMBIENT("mobSpellAmbient", 10),
	SPELL("spell", 11),
	INSTANT_SPELL("instantSpell", 12),
	PURPLE_SPARKLE("witchMagic", 13),
	NOTE_BLOCK("note", 14),
	ENDER("portal", 15),
	ENCHANTMENT_TABLE("enchantmenttable", 16),
	EXPLODE("explode", 17),
	FIRE("flame", 18),
	LAVA_SPARK("lava", 19),
	FOOTSTEP("footstep", 20),
	SPLASH("splash", 21),
	SMOKE("largesmoke", 22),
	CLOUD("cloud", 23),
	REDSTONE_DUST("reddust", 24),
	SNOWBALL_HIT("snowballpoof", 25),
	DRIP_WATER("dripWater", 26),
	DRIP_LAVA("dripLava", 27),
	SNOW_DIG("snowshovel", 28),
	SLIME("slime", 29),
	HEART("heart", 30),
	ANGRY_VILLAGER("angryVillager", 31),
	GREEN_SPARKLE("happyVillager", 32),
	ICONCRACK("iconcrack", 33),
	TILECRACK("tilecrack", 34);

	private static Class<?> packetClass = null;
	private static Constructor<?> packetConstructor = null;
	private static Field[] fields = null;
	private static boolean netty = true;
	private static Field player_connection = null;
	private static Method player_sendPacket = null;
	private static HashMap<Class<? extends Entity>, Method> handles = new HashMap<Class<? extends Entity>, Method>();

	private String name;
	private int id;

	static {
		String vString = getVersion().replace("v", "");
		float v = 0;
		if (!vString.equals("")){
			String[] array = vString.split("_");
			v = Float.parseFloat(array[0] + "." + array[1]);
		}
		try {
			if (v < 1.7) {
				netty = false;
				packetClass = getCraftClass("Packet63WorldParticles");
				packetConstructor = packetClass.getConstructor();
				fields = packetClass.getDeclaredFields();
			}
			else {
				packetClass = getCraftClass("PacketPlayOutWorldParticles");
				packetConstructor = packetClass.getConstructor(String.class, float.class, float.class, float.class, float.class,
						float.class, float.class, float.class, int.class);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			Main.log.severe("Failed to initialize NMS components!");
			Main.log.severe("Cannot continue. Disabling...");
			Main.plugin.getPluginLoader().disablePlugin(Main.plugin);
		}
	}

	ParticleEffect(String name, int id){
		this.name = name;
		this.id = id;
	}

	/**
	 * Gets the name of the Particle Effect
	 *
	 * @return The particle effect name
	 */
	String getName(){
		return name;
	}

	/**
	 * Gets the id of the Particle Effect
	 *
	 * @return The id of the Particle Effect
	 */
	int getId(){
		return id;
	}

	/**
	 * Send a particle effect to a player
	 *
	 * @param effect
	 *            The particle effect to send
	 * @param player
	 *            The player to send the effect to
	 * @param location
	 *            The location to send the effect to
	 * @param offsetX
	 *            The x range of the particle effect
	 * @param offsetY
	 *            The y range of the particle effect
	 * @param offsetZ
	 *            The z range of the particle effect
	 * @param speed
	 *            The speed (or color depending on the effect) of the particle
	 *            effect
	 * @param count
	 *            The count of effects
	 */
	public static void sendToPlayer(ParticleEffect effect, Player player, Location location, float offsetX, float offsetY,
									float offsetZ, float speed, int count){
		try {
			Object packet = createPacket(effect, location, offsetX, offsetY, offsetZ, speed, count);
			sendPacket(player, packet);
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * Send a particle effect to all players
	 *
	 * @param effect
	 *            The particle effect to send
	 * @param location
	 *            The location to send the effect to
	 * @param offsetX
	 *            The x range of the particle effect
	 * @param offsetY
	 *            The y range of the particle effect
	 * @param offsetZ
	 *            The z range of the particle effect
	 * @param speed
	 *            The speed (or color depending on the effect) of the particle
	 *            effect
	 * @param count
	 *            The count of effects
	 */
	public static void sendToLocation(ParticleEffect effect, Location location, float offsetX, float offsetY, float offsetZ, float speed, int count){
		try {
			Object packet = createPacket(effect, location, offsetX, offsetY, offsetZ, speed, count);
			for (Player player : Bukkit.getOnlinePlayers()){
				sendPacket(player, packet);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private static Object createPacket(ParticleEffect effect, Location location, float offsetX, float offsetY,
									   float offsetZ, float speed, int count) throws Exception {
		if (count <= 0){
			count = 1;
		}
		Object packet = null;
		if (netty) {
			packet = packetConstructor.newInstance(effect.name, (float)location.getX(),
					(float)location.getY(), (float)location.getZ(), offsetX, offsetY, offsetZ, speed, count);
		}
		else {
			packet = packetConstructor.newInstance();
			for (Field f : fields) {
				f.setAccessible(true);
				if (f.getName().equals("a"))
					f.set(packet, effect.name);
				else if (f.getName().equals("b"))
					f.set(packet, (float)location.getX());
				else if (f.getName().equals("c"))
					f.set(packet, (float)location.getY());
				else if (f.getName().equals("d"))
					f.set(packet, (float)location.getZ());
				else if (f.getName().equals("e"))
					f.set(packet, offsetX);
				else if (f.getName().equals("f"))
					f.set(packet, offsetY);
				else if (f.getName().equals("g"))
					f.set(packet, offsetZ);
				else if (f.getName().equals("h"))
					f.set(packet, speed);
				else if (f.getName().equals("i"))
					f.set(packet, count);
			}
		}
		return packet;
	}

	private static void sendPacket(Player p, Object packet) throws Exception {
		if (player_connection == null){
			player_connection = getHandle(p).getClass().getField("playerConnection");
			for (Method m : player_connection.get(getHandle(p)).getClass().getMethods()){
				if (m.getName().equalsIgnoreCase("sendPacket")){
					player_sendPacket = m;
				}
			}
		}
		player_sendPacket.invoke(player_connection.get(getHandle(p)), packet);
	}

	private static Object getHandle(Entity entity){
		try {
			if (handles.get(entity.getClass()) != null)
				return handles.get(entity.getClass()).invoke(entity);
			else {
				Method entity_getHandle = entity.getClass().getMethod("getHandle");
				handles.put(entity.getClass(), entity_getHandle);
				return entity_getHandle.invoke(entity);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private static Class<?> getCraftClass(String name){
		String version = getVersion();
		String className = "net.minecraft.server." + version + name;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		}
		catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		return clazz;
	}

	private static String getVersion(){
		String[] array = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
		if (array.length == 4)
			return array[3] + ".";
		return "";
	}

	public static boolean isCompatible(){
		try {
			Class.forName("net.minecraft.server." + getVersion() + "Packet63WorldParticles");
		}
		catch (ClassNotFoundException e){
			try {
				Class.forName("net.minecraft.server." + getVersion() + "PacketPlayOutWorldParticles");
			}
			catch (ClassNotFoundException ex){
				return false;
			}
		}
		return true;
	}

}