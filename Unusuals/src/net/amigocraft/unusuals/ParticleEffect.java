package net.amigocraft.unusuals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Particle Effects Lib
 * 
 * @author minnymin3
 * 
 */

public enum ParticleEffect {

	HUGE_EXPLODE("hugeexplosion", 0), LARGE_EXPLODE("largeexplode", 1), FIREWORK_SPARK("fireworksSpark", 2), AIR_BUBBLE("bubble", 3),
	SUSPEND("suspend", 4), DEPTH_SUSPEND("depthSuspend", 5), TOWN_AURA("townaura", 6), CRITICAL_HIT("crit", 7),
	MAGIC_CRITICAL_HIT("magicCrit", 8), MOB_SPELL("mobSpell", 9), MOB_SPELL_AMBIENT("mobSpellAmbient", 10), SPELL("spell", 11),
	INSTANT_SPELL("instantSpell", 12), PURPLE_SPARKLE("witchMagic", 13), NOTE_BLOCK("note", 14), ENDER("portal", 15),
	ENCHANTMENT_TABLE("enchantmenttable", 16), EXPLODE("explode", 17), FIRE("flame", 18), LAVA_SPARK("lava", 19),
	FOOTSTEP("footstep", 20), SPLASH("splash", 21), SMOKE("largesmoke", 22), CLOUD("cloud", 23), REDSTONE_DUST("reddust", 24),
	SNOWBALL_HIT("snowballpoof", 25), DRIP_WATER("dripWater", 26), DRIP_LAVA("dripLava", 27), SNOW_DIG("snowshovel", 28),
	SLIME("slime", 29), HEART("heart", 30), ANGRY_VILLAGER("angryVillager", 31), GREEN_SPARKLE("happyVillager", 32),
	ICONCRACK("iconcrack", 33), TILECRACK("tilecrack", 34);

	private String name;
	private int id;

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
		Class<?> packetClass = null;
		Object packet = null;
		String vString = getVersion().replace("v", "");
		float v = 0;
		if (!vString.equals("")){
			String[] array = vString.split("_");
			v = Float.parseFloat(array[0] + "." + array[1]);
		}
		if (v < 1.7){
			packetClass = getCraftClass("Packet63WorldParticles");
			packet = packetClass.getConstructor().newInstance();
			for (Field f : packetClass.getDeclaredFields()){
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
		else {
			packetClass = getCraftClass("PacketPlayOutWorldParticles");
			packet = packetClass.getConstructor(String.class, float.class, float.class, float.class, float.class,
					float.class, float.class, float.class, int.class).newInstance(effect.name, (float) location.getX(),
							(float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, count);
		}
		return packet;
	}

	private static void sendPacket(Player p, Object packet) throws Exception {
		Object eplayer = getHandle(p);
		Field playerConnectionField = eplayer.getClass().getField("playerConnection");
		Object playerConnection = playerConnectionField.get(eplayer);
		for (Method m : playerConnection.getClass().getMethods()){
			if (m.getName().equalsIgnoreCase("sendPacket")){
				m.invoke(playerConnection, packet);
				return;
			}
		}
	}

	private static Object getHandle(Entity entity){
		try {
			Method entity_getHandle = entity.getClass().getMethod("getHandle");
			Object nms_entity = entity_getHandle.invoke(entity);
			return nms_entity;
		}
		catch (Exception ex){
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