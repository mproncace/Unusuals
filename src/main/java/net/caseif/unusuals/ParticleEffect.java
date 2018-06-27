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