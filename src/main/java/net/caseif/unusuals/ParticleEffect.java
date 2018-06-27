package net.caseif.unusuals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParticleEffect {

    private ParticleType type;
    private float speed;
    private int count;
    private float radius;

    public ParticleEffect(ParticleType type, float speed, int count, float radius) {
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
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the number of particles in the effect
     *
     * @return The number of particles in the effect
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets the radius of the particle effect
     *
     * @return The radius of the particle effect
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Send a particle effect to all players
     *
     * @param location The location to send the effect to
     */
    public void sendToLocation(Location location) {
        try {
            Object packet = Main.hook.createPacket(type,
                    (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                    radius, radius, radius, speed, count, false, new int[0]);
            for (Player player : Bukkit.getOnlinePlayers()) {
                Main.hook.sendPacket(packet, player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}