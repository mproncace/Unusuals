package net.amigocraft.unusuals;

import org.bukkit.entity.Player;

public class UnusualEffect {

	private String name;
	private ParticleEffect effect;
	private double speed;
	private int count;
	private double radius;
	
	public UnusualEffect(String name, ParticleEffect effect, double speed, int count, double radius){
		this.name = name;
		this.effect = effect;
		this.speed = speed;
		this.count = count;
		this.radius = radius;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public ParticleEffect getEffect(){
		return effect;
	}
	
	public void setEffect(ParticleEffect effect){
		this.effect = effect;
	}
	
	public double getSpeed(){
		return speed;
	}
	
	public void setSpeed(double speed){
		this.speed = speed;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public double getRadius(){
		return radius;
	}
	
	public void setRadius(int radius){
		this.radius = radius;
	}
	
	public void display(Player player){
		ParticleEffect.sendToLocation(effect, player.getLocation().add(0, 2.15, 0), (float)radius, (float)radius, (float)radius, (float)speed, count);
	}
	
}
