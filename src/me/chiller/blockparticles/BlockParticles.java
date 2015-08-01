package me.chiller.blockparticles;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.chiller.blockparticles.util.NMSHelper;

@SuppressWarnings("rawtypes")
public class BlockParticles extends JavaPlugin
{
	private Random rand = new Random();
	
	private List<BlockParticleEffect> effects = new ArrayList<BlockParticleEffect>();
	
	private Class<?> PacketPlayOutWorldParticles;
	private Class<?> EntityPlayer;
	private Class<?> Packet;
	
	private Class<Enum> EnumParticle;
	private Class<?> CraftPlayer;
	
	@SuppressWarnings("unchecked")
	public void onEnable()
	{
		ConfigurationSerialization.registerClass(BlockParticleEffect.class);
		
		try
		{
			PacketPlayOutWorldParticles = NMSHelper.importClass("net.minecraft.server._version_.PacketPlayOutWorldParticles");
			EntityPlayer = NMSHelper.importClass("net.minecraft.server._version_.EntityPlayer");
			Packet = NMSHelper.importClass("net.minecraft.server._version_.Packet");
			
			EnumParticle = (Class<Enum>) NMSHelper.importClass("net.minecraft.server._version_.EnumParticle");
			CraftPlayer = NMSHelper.importClass("org.bukkit.craftbukkit._version_.entity.CraftPlayer");
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		loadEffects();
		
		getCommand("bp").setExecutor(this);
	}
	
	public void onDisable()
	{
		
	}
	
	@SuppressWarnings("unchecked")
	private void loadEffects()
	{
		effects = (List<BlockParticleEffect>) getConfig().get("effects", effects);
		
		for (BlockParticleEffect effect : effects)
		{
			createParticles(effect, false);
		}
	}
	
	private void saveEffects()
	{
		getConfig().set("effects", effects);
		saveConfig();
	}
	
	public void createParticles(final BlockParticleEffect effect, boolean addToList)
	{
		if (addToList)
		{
			effects.add(effect);
			saveEffects();
		}
		
		new BukkitRunnable()
		{
			@SuppressWarnings("unchecked")
			public void run()
			{
				if (effect.getDuration() == 0 || System.currentTimeMillis() < effect.getDuration())
				{
					effect.resetLocation();
					
					Location particleLoc = effect.getLocation().add(rand.nextFloat(), rand.nextFloat() / 3, rand.nextFloat());
					
					try
					{
						Object packet = NMSHelper.buildInstance(PacketPlayOutWorldParticles)
							.addUniversalInstance(new Class<?>[] { EnumParticle, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class })
							.newInstance(Enum.valueOf(EnumParticle, effect.getName()), true, (float) particleLoc.getX(), (float) particleLoc.getY(), (float) particleLoc.getZ(), 0, 0, 0, 0, 5, new int[] {});
						
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
					} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 2);
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			
			if (label.equalsIgnoreCase("blockparticles") || label.equalsIgnoreCase("bp"))
			{
				if (p.hasPermission("blockparticles.use"))
				{
					try
					{
						if (args.length == 3)
						{
							Player player = Bukkit.getPlayer(args[0]);
							String particle = args[1].replace(" ", "_").toUpperCase();
							int end = Integer.parseInt(args[2]);
							
							Enum.valueOf(EnumParticle, particle);
							
							createParticles(new BlockParticleEffect(player.getLocation(), particle, end), true);
							
							p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_GREEN + "Playing " + particle + " at " + player.getName() + "'s feet for" + (end == 0 ? "ever" : (" " + end + " seconds")));
						} else if (args.length == 2)
						{
							String particle = args[0].replace(" ", "_").toUpperCase();
							int end = Integer.parseInt(args[1]);
							
							Enum.valueOf(EnumParticle, particle);
							
							createParticles(new BlockParticleEffect(p.getLocation(), particle, end), true);
							
							p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_GREEN + "Playing " + particle + " at your feet for" + (end == 0 ? "ever" : (" " + end + " seconds")));
						} else
						{
							p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Usage: /" + label + " [player] <particle> <lasts_for|0=infinite>");
						}
					} catch (NullPointerException e)
					{
						p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "The player you entered could not be found");
					} catch (NumberFormatException e)
					{
						p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Invalid end time entered");
					} catch (IllegalArgumentException e)
					{
						p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Invalid particle name entered, valid names: " + ChatColor.DARK_AQUA + StringUtils.join(EnumParticle.getEnumConstants(), ChatColor.AQUA + ", " + ChatColor.DARK_AQUA).toLowerCase());
					}
				} else
				{
					p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "You do not have permission to do that!");
				}
			} else
			{
				p.sendMessage(ChatColor.GOLD + "[BlockParticles] " + ChatColor.DARK_RED + "Usage: /" + label + " [player] <particle> <lasts_for|0=infinite>");
			}
		}
		
		return true;
	}
}