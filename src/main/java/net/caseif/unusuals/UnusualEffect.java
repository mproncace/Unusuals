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

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.util.List;

public class UnusualEffect {

    private String name;
    private List<ParticleEffect> particles;

    public UnusualEffect(String name, ParticleEffect particle) {
        this(name, Lists.asList(particle, new ParticleEffect[1]));
    }

    public UnusualEffect(String name, List<ParticleEffect> particleEffects) {
        this.name = name;
        this.particles = particleEffects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParticleEffect> getParticleEffects() {
        return particles;
    }

    public void setParticleEffects(List<ParticleEffect> particles) {
        this.particles = particles;
    }

    public void display(Player player) {
        for (ParticleEffect pe : particles) {
            pe.sendToLocation(player.getLocation().add(0, 2.15, 0));
        }
    }

}
