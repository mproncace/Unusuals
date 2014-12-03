package net.amigocraft.unusuals.nms;

import net.amigocraft.unusuals.ParticleType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface NmsHook {

	public Class<?> getParticlePacket();

	public Object createPacket(ParticleType type, float xPos, float yPos, float zPos,
	                           float xRad, float yRad, float zRad, float speed, int count,
	                           boolean distOverride, int[] data);

	public void sendPacket(Object packet, Player player);

	public Object getNmsEntity(Entity entity);

	public Object getConnection(Player player);

	public boolean isCompatible();

}
