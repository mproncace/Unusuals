package net.amigocraft.unusuals;

public enum ParticleType {

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

	private String name;
	private int id;

	ParticleType(String name, int id){
		this.name = name;
		this.id = id;
	}

	/**
	 * Gets the name of the particle effect
	 *
	 * @return The name of the particle effect
	 */
	String getName(){
		return name;
	}

	/**
	 * Gets the ID of the particle effect
	 *
	 * @return The ID of the particle effect
	 */
	int getId(){
		return id;
	}
}
