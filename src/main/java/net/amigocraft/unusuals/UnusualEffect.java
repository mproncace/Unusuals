package net.amigocraft.unusuals;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.util.List;

public class UnusualEffect {

	private String name;
	private List<ParticleEffect> particles;

	public UnusualEffect(String name, ParticleEffect particle){
		this(name, Lists.asList(particle, new ParticleEffect[1]));
	}

	public UnusualEffect(String name, List<ParticleEffect> particleEffects){
		this.name = name;
		this.particles = particleEffects;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public List<ParticleEffect> getParticleEffects(){
		return particles;
	}

	public void setParticleEffects(List<ParticleEffect> particles){
		this.particles = particles;
	}

	public void display(Player player){
		for (ParticleEffect pe : particles)
			pe.sendToLocation(player.getLocation().add(0, 2.15, 0));
	}

}
