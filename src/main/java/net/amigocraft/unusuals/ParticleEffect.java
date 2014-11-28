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
 * Particle effects handler based on the library by minnymin3
 */

public class ParticleEffect {

	private static Class<?> packetClass = null;
	private static Constructor<?> packetConstructor = null;
	private static Field[] fields = null;
	private static boolean netty = true;
	private static Field player_connection = null;
	private static Method player_sendPacket = null;
	private static HashMap<Class<? extends Entity>, Method> handles = new HashMap<Class<? extends Entity>, Method>();

	private static boolean newParticlePacketConstructor = false;
	private static Class<Enum> enumParticle = null;
	private static Constructor<?> enumParticleConstructor = null;

	private ParticleType type;
	private double speed;
	private int count;
	private double radius;

	static {
		String vString = getVersion().replace("v", "");
		double v = 0;
		if (!vString.isEmpty()){
			String[] array = vString.split("_");
			v = Double.parseDouble(array[0] + "." + array[1]);
		}
		try {
			Main.log.info("Server major/minor version: " + v);
			if (v < 1.7) {
				Main.log.info("Hooking into pre-Netty NMS classes");
				netty = false;
				packetClass = getNmsClass("Packet63WorldParticles");
				packetConstructor = packetClass.getConstructor();
				fields = packetClass.getDeclaredFields();
			}
			else {
				Main.log.info("Hooking into Netty NMS classes");
				packetClass = getNmsClass("PacketPlayOutWorldParticles");
				if (v < 1.8){
					Main.log.info("Version is < 1.8 - using old packet constructor");
					packetConstructor = packetClass.getConstructor(String.class, float.class, float.class, float.class,
							float.class, float.class, float.class, float.class, int.class);
				}
				else { // use the new constructor for 1.8
					Main.log.info("Version is >= 1.8 - using new packet constructor");
					newParticlePacketConstructor = true;
					enumParticle = (Class<Enum>)getNmsClass("EnumParticle");
					packetConstructor = packetClass.getDeclaredConstructor(enumParticle, boolean.class, float.class,
							float.class, float.class, float.class, float.class, float.class, float.class, int.class,
							int[].class);
				}
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			Main.log.severe("Failed to initialize NMS components!");
			Main.log.severe("Cannot continue. Disabling...");
			Main.plugin.getPluginLoader().disablePlugin(Main.plugin);
		}
	}

	public ParticleEffect(ParticleType type, double speed, int count, double radius){
		this.type = type;
		this.speed = speed;
		this.count = count;
		this.radius = radius;
	}

	/**
	 * Gets the speed of the particle effect
	 *
	 * @return The speed of the particle effect
	 */
	public double getSpeed(){
		return speed;
	}

	/**
	 * Gets the number of particles in the effect
	 *
	 * @return The number of particles in the effect
	 */
	public int getCount(){
		return count;
	}

	/**
	 * Gets the radius of the particle effect
	 *
	 * @return The radius of the particle effect
	 */
	public double getRadius(){
		return radius;
	}

	/**
	 * Send a particle effect to all players
	 *
	 * @param location
	 *            The location to send the effect to
	 */
	public void sendToLocation(Location location){
		try {
			Object packet = createPacket(location);
			for (Player player : Bukkit.getOnlinePlayers()){
				sendPacket(player, packet);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	private Object createPacket(Location location) throws Exception {
		if (this.count <= 0){
			this.count = 1;
		}
		Object packet;
		if (netty) {
			if (newParticlePacketConstructor){
				Object particleType = enumParticle.getEnumConstants()[type.getId()];
				packet = packetConstructor.newInstance(particleType,
						true, (float)location.getX(), (float)location.getY(), (float)location.getZ(),
						(float)this.radius, (float)this.radius, (float)this.radius,
						(float)this.speed, this.count, new int[0]);
			}
			else {
				packet = packetConstructor.newInstance(type.getName(),
						(float)location.getX(), (float)location.getY(), (float)location.getZ(),
						(float)this.radius, (float)this.radius, (float)this.radius,
						(float)this.speed, this.count);
			}
		}
		else {
			packet = packetConstructor.newInstance();
			for (Field f : fields) {
				f.setAccessible(true);
				if (f.getName().equals("a"))
					f.set(packet, type.getName());
				else if (f.getName().equals("b"))
					f.set(packet, (float)location.getX());
				else if (f.getName().equals("c"))
					f.set(packet, (float)location.getY());
				else if (f.getName().equals("d"))
					f.set(packet, (float)location.getZ());
				else if (f.getName().equals("e"))
					f.set(packet, this.radius);
				else if (f.getName().equals("f"))
					f.set(packet, this.radius);
				else if (f.getName().equals("g"))
					f.set(packet, this.radius);
				else if (f.getName().equals("h"))
					f.set(packet, this.speed);
				else if (f.getName().equals("i"))
					f.set(packet, this.count);
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

	private static Class<?> getNmsClass(String name){
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