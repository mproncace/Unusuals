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
package net.caseif.unusuals.handlers;

import net.caseif.unusuals.Main;
import net.caseif.unusuals.ParticleEffect;
import net.caseif.unusuals.ParticleType;
import net.caseif.unusuals.nms.NmsHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NmsParticleHandler implements IParticleHandler {

    private final NmsHook hook;

    public NmsParticleHandler(NmsHook hook) {
        this.hook = hook;

        if (!hook.isCompatible()) {
            throw new UnsupportedOperationException("Plugin not compatible with this server.");
        }
    }
    
    @Override
    public void sendToLocation(ParticleEffect effect, Location loc) {
        try {
            Object packet = hook.createPacket((ParticleType) effect.getType(),
                    (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
                    effect.getRadius(), effect.getRadius(), effect.getRadius(), effect.getSpeed(), effect.getCount(),
                    false, new int[0]);
            for (Player player : Main.getOnlinePlayers()) {
                hook.sendPacket(packet, player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
