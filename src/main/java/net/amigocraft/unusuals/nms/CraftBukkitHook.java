package net.amigocraft.unusuals.nms;

import net.amigocraft.unusuals.Main;
import net.amigocraft.unusuals.ParticleEffect;
import net.amigocraft.unusuals.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class CraftBukkitHook implements NmsHook {

	private static HashMap<Class<? extends Entity>, Method> handles = new HashMap<Class<? extends Entity>, Method>();

	private Class<?> particlePacket;
	private Constructor<?> packetConstructor;
	private static Field[] packetFields;
	private boolean netty = true;
	private boolean newPacketConstructor = false;

	private static Class<Enum> enumParticle;
	private static Constructor<?> enumParticleConstructor;

	private static Field player_connection;
	private static Method player_sendPacket;

	public CraftBukkitHook(){
		Main.log.info("This server appears to be running Craftbukkit (or some modification of it)");
		Main.log.info("Loading appropriate hooks...");
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
				particlePacket = getNmsClass("Packet63WorldParticles");
				packetConstructor = particlePacket.getConstructor();
				packetFields = particlePacket.getDeclaredFields();
			}
			else {
				Main.log.info("Hooking into Netty NMS classes");
				particlePacket = getNmsClass("PacketPlayOutWorldParticles");
				if (v < 1.8){
					Main.log.info("Version is < 1.8 - using old packet constructor");
					packetConstructor = particlePacket.getConstructor(String.class, float.class, float.class, float.class,
							float.class, float.class, float.class, float.class, int.class);
				}
				else { // use the new constructor for 1.8
					Main.log.info("Version is >= 1.8 - using new packet constructor");
					newPacketConstructor = true;
					enumParticle = (Class<Enum>)getNmsClass("EnumParticle");
					packetConstructor = particlePacket.getDeclaredConstructor(enumParticle, boolean.class, float.class,
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

	public Class<?> getParticlePacket(){
		return particlePacket;
	}

	@Override
	public Object createPacket(ParticleType type, float xPos, float yPos, float zPos,
	                           float xRad, float yRad, float zRad, float speed, int count,
	                           boolean distOverride, int[] data){
		try {
			Object packet;
			if (netty){
				if (newPacketConstructor){
					Object particleType = enumParticle.getEnumConstants()[type.getId()];
					packet = packetConstructor.newInstance(particleType, distOverride, xPos, yPos, zPos,
							xRad, yRad, zRad, speed, count, data);
				}
				else {
					packet = packetConstructor.newInstance(type.getName(),
							xPos, yPos, zPos, xRad, yRad, zRad, speed, count);
					packet = packetConstructor.newInstance(type.getName(), xPos, yPos, zPos,
							xRad, yRad, zRad, speed, count);
				}
			}
			else {
				packet = packetConstructor.newInstance();
				for (Field f : packetFields){
					f.setAccessible(true);
					if (f.getName().equals("a"))
						f.set(packet, type.getName());
					else if (f.getName().equals("b"))
						f.set(packet, xPos);
					else if (f.getName().equals("c"))
						f.set(packet, yPos);
					else if (f.getName().equals("d"))
						f.set(packet, zPos);
					else if (f.getName().equals("e"))
						f.set(packet, xRad);
					else if (f.getName().equals("f"))
						f.set(packet, yRad);
					else if (f.getName().equals("g"))
						f.set(packet, zRad);
					else if (f.getName().equals("h"))
						f.set(packet, speed);
					else if (f.getName().equals("i"))
						f.set(packet, count);
				}
			}
			return packet;
		}
		catch(Exception ex){
			ex.printStackTrace();
			Main.log.severe("An exception occurred while creating a packet");
		}
		return null;
	}

	public Constructor<?> getParticlePacketConstructor(){
		return packetConstructor;
	}

	public void sendPacket(Object packet, Player player){
		try {
			Object connection = getConnection(player);
			player_sendPacket.invoke(connection, packet);
		}
		catch (IllegalAccessException ex){
			ex.printStackTrace();
			Main.log.severe("Failed to send packet to player " + player.getName());
		}
		catch (InvocationTargetException ex){
			ex.printStackTrace();
			Main.log.severe("Failed to send packet to player " + player.getName());
		}
	}

	public Object getNmsEntity(Entity entity){
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

	public Object getConnection(Player player){
		try {
			if (player_connection == null){
				player_connection = getNmsEntity(player).getClass().getField("playerConnection");
				for (Method m : player_connection.get(getNmsEntity(player)).getClass().getMethods()){
					if (m.getName().equalsIgnoreCase("sendPacket")){
						player_sendPacket = m;
					}
				}
			}
			return player_connection.get(getNmsEntity(player));
		}
		catch (IllegalAccessException ex){
			ex.printStackTrace();
			Main.log.severe("Failed to get connection for player " + player.getName());
		}
		catch (NoSuchFieldException ex){
			ex.printStackTrace();
			Main.log.severe("Failed to get connection for player " + player.getName());
		}
		return null;
	}

	public boolean isCompatible(){
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

	private String getVersion(){
		String[] array = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
		if (array.length == 4)
			return array[3] + ".";
		return "";
	}

	private Class<?> getNmsClass(String name){
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

}
