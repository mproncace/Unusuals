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

public enum ParticleType {

    EXPLOSION_NORMAL("explode", 0, 17),
    EXPLOSION_LARGE("largeexplode", 1, 1),
    EXPLOSION_HUGE("hugeexplosion", 2, 0),
    FIREWORKS_SPARK("fireworksSpark", 3, 2),
    WATER_BUBBLE("bubble", 4, 3),
    WATER_SPLASH("splash", 5, 21),
    WATER_WAKE("wake", 6, -1),
    SUSPENDED("suspended", 7, 4),
    SUSPENDED_DEPTH("depthsuspend", 8, 5),
    CRIT("crit", 9, 7),
    CRIT_MAGIC("magicCrit", 10, 8),
    SMOKE_NORMAL("smoke", 11, -1),
    SMOKE_LARGE("largesmoke", 12, 22),
    SPELL("spell", 13, 11),
    SPELL_INSTANT("instantSpell", 14, 12),
    SPELL_MOB("mobSpell", 15, 9),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, 10),
    SPELL_WITCH("witchMagic", 17, 13),
    DRIP_WATER("dripWater", 18, 27),
    DRIP_LAVA("dripLava", 19, 28),
    VILLAGER_ANGRY("angryVillager", 20, 31),
    VILLAGER_HAPPY("happyVillager", 21, 32),
    TOWN_AURA("townaura", 22, 6),
    NOTE("note", 23, 24),
    PORTAL("portal", 24, 15),
    ENCHANTMENT_TABLE("enchantmenttable", 25, 16),
    FLAME("flame", 26, 18),
    LAVA("lava", 27, 19),
    FOOTSTEP("footstep", 28, 20),
    CLOUD("cloud", 29, 23),
    REDSTONE("reddust", 30, 24),
    SNOWBALL("snowballpoof", 31, 25),
    SNOW_SHOVEL("snowshovel", 32, 28),
    SLIME("slime", 33, 29),
    HEART("heart", 34, 30),
    BARRIER("barrier", 35, -1),
    WATER_DROP("droplet", 39, -1),
    ITEM_TAKE("take", 40, -1),
    MOB_APPEARANCE("mobappearance", 41, -1);

    private String name;
    private int id;
    private int legacyId;

    ParticleType(String name, int id, int legacyId) {
        this.name = name;
        this.id = id;
        this.legacyId = legacyId;
    }

    /**
     * Gets the name of the particle effect
     *
     * @return The name of the particle effect
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the ID of the particle effect
     *
     * @return The ID of the particle effect
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the legacy ID (pre-1.8) of the particle effect
     *
     * @return the legacy ID of the particle effect (or -1 if introduced after 1.7)
     */
    public int getLegacyId() {
        return legacyId;
    }
}
