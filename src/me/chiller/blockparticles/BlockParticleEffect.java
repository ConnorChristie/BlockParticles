package me.chiller.blockparticles;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class BlockParticleEffect implements ConfigurationSerializable
{
	private Location loc;
	private String name;
	
	private long duration;
	
	public BlockParticleEffect(Location loc, String name, long duration)
	{
		this.loc = loc;
		this.name = name;
		
		this.duration = duration;
		
		resetLocation();
		
		if (duration != 0)
		{
			this.duration = duration * 1000 + System.currentTimeMillis();
		}
	}
	
	public void resetLocation()
	{
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY());
		loc.setZ(loc.getBlockZ());
	}
	
	public Location getLocation()
	{
		return loc;
	}
	
	public String getName()
	{
		return name;
	}
	
	public long getDuration()
	{
		return duration;
	}
	
	public BlockParticleEffect(Map<String, Object> map)
	{
		this.loc = (Location) map.get("location");
		this.name = (String) map.get("name");
		
		Object dur = map.get("duration");
		
		this.duration = (dur instanceof Integer) ? ((Integer) dur) : ((Long) dur);
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("location", loc);
		map.put("name", name);
		
		map.put("duration", duration);
		
		return map;
	}
}