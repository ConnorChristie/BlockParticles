package me.chiller.blockparticles;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.chiller.blockparticles.util.NMSHelper;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class BlockParticles extends JavaPlugin
{
	private Random rand = new Random();
	
	public void onEnable()
	{
		getCommand("bp").setExecutor(this);
	}
	
	public void onDisable()
	{
		
	}
	
	public void createParticles(final String particle, final Location loc, final long endRelative)
	{
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY());
		loc.setZ(loc.getBlockZ());
		
		final long end = endRelative * 1000 + System.currentTimeMillis();
		
		new BukkitRunnable()
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run()
			{
				if (end == 0 || System.currentTimeMillis() < end)
				{
					Location clone = loc.clone().add(rand.nextFloat(), rand.nextFloat() / 3, rand.nextFloat());
					
					try
					{
						Class<?> PacketPlayOutWorldParticles = NMSHelper.importClass("net.minecraft.server._version_.PacketPlayOutWorldParticles");
						Class<?> EntityPlayer = NMSHelper.importClass("net.minecraft.server._version_.EntityPlayer");
						Class<?> Packet = NMSHelper.importClass("net.minecraft.server._version_.Packet");
						
						Class<Enum> EnumParticle = (Class<Enum>) NMSHelper.importClass("net.minecraft.server._version_.EnumParticle");
						Class<?> CraftPlayer = NMSHelper.importClass("org.bukkit.craftbukkit._version_.entity.CraftPlayer");
						
						Object packet = NMSHelper.buildInstance(PacketPlayOutWorldParticles)
							.addVersionInstance("1.8", new Class<?>[] { EnumParticle, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class })
							.addArguments("1.8", Enum.valueOf(EnumParticle, "EXPLOSION_NORMAL"), true, (float) clone.getX(), (float) clone.getY(), (float) clone.getZ(), 0, 0, 0, 0, 5, new int[] {})
							.newInstance();
						
						for (Player onlinePlayer : Bukkit.getOnlinePlayers())
						{
							Object craftPlayer = CraftPlayer.cast(onlinePlayer);
							
							Object handle = NMSHelper.buildMethod(craftPlayer)
								.addUniversalMethod("getHandle")
								.execute();
							
							Object playerConnection = EntityPlayer.getField("playerConnection").get(handle);
							
							NMSHelper.buildMethod(playerConnection)
								.addUniversalMethod("sendPacket", Packet)
								.execute(packet);
						}
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
					{
						e.printStackTrace();
					}
					
					/*
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_NORMAL, true, (float) clone.getX(), (float) clone.getY(), (float) clone.getZ(), 0, 0, 0, 0, 5);
					
					for (Player onlinePlayer : Bukkit.getOnlinePlayers())
					{
						((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
					}
					*/
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 2);
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			
			if (label.equalsIgnoreCase("blockparticles") || label.equalsIgnoreCase("bp"))
			{
				if (p.hasPermission("blockparticles.use"))
				{
					if (args.length == 2)
					{
						try
						{
							String particle = args[0];
							int end = Integer.parseInt(args[1]);
							
							createParticles(particle, p.getLocation(), end);
						} catch (Exception e)
						{
							p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Invalid end time entered");
						}
					} else if (args.length == 3)
					{
						try
						{
							Player player = Bukkit.getPlayer(args[0]);
							String particle = args[1];
							int end = Integer.parseInt(args[2]);
							
							createParticles(particle, player.getLocation(), end);
						} catch (Exception e)
						{
							p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Invalid player or end time entered");
						}
					}
				} else
				{
					p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "You do not have permission to do that!");
				}
			} else
			{
				p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Usage: /" + label + " <particle>");
			}
		}
		
		return true;
	}
}