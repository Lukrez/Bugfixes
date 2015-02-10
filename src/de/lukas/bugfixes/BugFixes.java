package de.lukas.bugfixes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

public class BugFixes extends JavaPlugin implements Listener {

	private String playerUseLogger;

	@Override
	public void onDisable() {
		this.getLogger().info(this.getDescription().getVersion() + " has been disabled.");
	}

	@Override
	public void onEnable() {
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getLogger().info(this.getDescription().getVersion() + " has been enabled.");
	}

	public void onEntityDamage(EntityDamageEvent event) {
		Entity damaged = event.getEntity();
		if (damaged.getType() == EntityType.PLAYER && damaged.getWorld().getName().equalsIgnoreCase("themenwelt")) {
			event.setDamage(0);
			event.setCancelled(true);
			if (damaged.getLocation().getY() <= -100) {
				damaged.teleport(damaged.getWorld().getSpawnLocation());
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if ((this.playerUseLogger == null) || (!this.playerUseLogger.equalsIgnoreCase(event.getPlayer().getName()))) {
			return;
		}
		File logFile = new File(getDataFolder(), "log_events.txt");
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			return;
		}

		String cmd = event.getMessage().split(" ")[0].replaceFirst("/", "");
		PluginCommand pcmd = null;
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			pcmd = ((JavaPlugin) plugin).getCommand(cmd);
			if (pcmd != null) {
				Date d = new Date();
				Player toSend = getServer().getPlayer(this.playerUseLogger);
				if (toSend != null)
					toSend.sendMessage(this.prefix + "Command: " + cmd + " von Plugin " + plugin.getName());
				ps.append(d.toString() + ": Command " + cmd + " von Plugin " + plugin.getName());
				ps.append("\r\n");
				ps.close();
				break;
			}
		}
		if (pcmd == null) {
			Date d = new Date();
			Player toSend = getServer().getPlayer(this.playerUseLogger);
			if (toSend != null)
				toSend.sendMessage(this.prefix + " " + cmd + " - " + "kein Plugin gefunden");
			ps.append(d.toString() + ": Command " + cmd + " - " + "kein Plugin gefunden");
			ps.append("\r\n");
			ps.close();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((this.playerUseLogger == null) || (!this.playerUseLogger.equalsIgnoreCase(event.getPlayer().getName()))) {
			return;
		}
		File logFile = new File(getDataFolder(), "log_events.txt");
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			return;
		}

		String plugins = "";
		boolean b = true;
		for (RegisteredListener rl : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
			if (b)
				b = false;
			else {
				plugins = plugins + ", ";
			}
			plugins = plugins + rl.getPlugin().getName();
		}
		Player toSend = getServer().getPlayer(this.playerUseLogger);
		if (toSend != null) {
			toSend.sendMessage(this.prefix + "PlayerInteract - Registered plugins: " + plugins);
		}
		Date d = new Date();
		ps.append(d + ": PlayerInteract - Registered plugins: " + plugins);
		ps.append("\r\n");
		ps.close();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		String s = "";

		for (RegisteredListener rl : event.getHandlers().getRegisteredListeners()) {
			event.getHandlerList().
		}
		
		// System.out.println(s);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		if (player.getWorld().equals(to.getWorld()))
			return;
		if (!player.hasPermission("bugfixes.worlds.access." + to.getWorld().getName().toLowerCase())) {
			event.setCancelled(true);
			player.sendMessage(this.prefix + ChatColor.RED + "Dir fehlt die benötigte Permission um diese Welt zu betreten.");
		}
	}

	private final String prefix = ChatColor.GREEN + "[BugFixes] " + ChatColor.WHITE;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("bugfixes")) {
			return false;
		}
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		if (!player.hasPermission("bugfixes.manage")) {
			return true;
		}

		if (args.length == 0) {
			// @formatter:off
			sender.sendMessage(new String[] { 
					this.prefix + "/bugfixes diskinfo - Zeigt den freien und gesamten Speicher der Festplatte an" , 
					this.prefix + "/bugfixes logevent - Aktiviert oder deaktiviert den EventLogger", 
					this.prefix + "/bugfixes delentities <world name> <entity id> [chunk x:z] - Entities im Chunk oder Welt löschen",
					this.prefix + "/bugfixes countentities <world name> <entity id> [chunk x:z] - Entities im Chunk oder Welt zählen",
			});
			// @formatter:on
			return true;
		}

		if (args[0].equalsIgnoreCase("diskinfo")) {
			for (File f : File.listRoots()) {
				if (f.canRead())
					sender.sendMessage(this.prefix + "Freier Speicher: " + this.getAsMB(f.getFreeSpace()) + " MB. Insgesamt : " + this.getAsMB(f.getTotalSpace()) + " MB");
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("logevent")) {
			if (this.playerUseLogger == null) {
				this.playerUseLogger = player.getName();
				player.sendMessage(this.prefix + "Eventlogger aktiviert.");
			} else {
				this.playerUseLogger = null;
				player.sendMessage(this.prefix + "Eventlogger nun deaktiviert.");
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("listevents")) {
			String s = "InventoryClickEvent: ";
			for (RegisteredListener rl : InventoryClickEvent.getHandlerList().getRegisteredListeners()) {
				s += rl.getPlugin().getName() + ", ";
			}

			s += "\nPlayerInteractEvent: ";
			for (RegisteredListener rl : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
				s += rl.getPlugin().getName() + ", ";
			}

			player.sendMessage(s.split("\n"));
			return true;
		}

		if (args[0].equalsIgnoreCase("delentities") || args[0].equalsIgnoreCase("countentities")) {
			if (args.length != 3 && args.length != 5) {
				sender.sendMessage(this.prefix + "/bugfixes delentities <world name> <entity id> [chunk x:z]");
				sender.sendMessage(this.prefix + "/bugfixes countentities <world name> <entity id> [chunk x:z]");
				return true;
			}
			World w = this.getServer().getWorld(args[1]);
			if (w == null) {
				sender.sendMessage(this.prefix + "Keine Welt mit dem Namen vorhanden");
				return true;
			}

			int entityId = -1;
			try {
				entityId = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {

			}
			EntityType type = EntityType.fromId(entityId);
			if (type == null) {
				type = EntityType.fromName(args[2]);
				if (type == null) {
					sender.sendMessage(this.prefix + "Kein Entity mit der Id oder Namen gefunden.");
					return true;
				}
			}

			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("delentities")) {
					int counter = 0;
					for (Entity e : w.getEntities()) {
						if (e.getType() == type) {
							e.remove();
							counter++;
						}
					}

					sender.sendMessage(this.prefix + "Es wurden " + counter + " Entities vom Typ " + type.toString() + " entfernt.");
					return true;
				} else if (args[0].equalsIgnoreCase("countentities")) {
					int counter = 0;
					for (Entity e : w.getEntities()) {
						if (e.getType() == type) {
							counter++;
						}
					}

					sender.sendMessage(this.prefix + "Es wurden " + counter + " Entities vom Typ " + type.toString() + " gefunden.");
					return true;
				}
			} else {
				int x, z;
				try {
					String[] chunkCoords = args[4].split(":");
					if (chunkCoords.length != 2) {
						sender.sendMessage(this.prefix + "Ungültige Angabe. Syntax muss chunkx:chunkz sein.");
						return true;
					}
					x = Integer.parseInt(chunkCoords[0]);
					z = Integer.parseInt(chunkCoords[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage(this.prefix + "Ungültige Zahl");
					return true;
				}

				Chunk c = w.getChunkAt(x, z);
				if (c == null) {
					sender.sendMessage(this.prefix + "Chunk nicht geladen. Lade es...");
					w.loadChunk(x, z);
					c = w.getChunkAt(x, z);
					if (c == null) {
						sender.sendMessage(this.prefix + "Chunk konnte nicht geladen werden.");
						return true;
					}
				}

				if (args[0].equalsIgnoreCase("delentities")) {
					int counter = 0;
					for (Entity e : c.getEntities()) {
						if (e.getType() == type) {
							e.remove();
							counter++;
						}
					}

					sender.sendMessage(this.prefix + "Es wurden " + counter + " Entities vom Typ " + type.toString() + " entfernt.");
					return true;
				} else if (args[0].equalsIgnoreCase("countentities")) {
					int counter = 0;
					for (Entity e : c.getEntities()) {
						if (e.getType() == type) {
							counter++;
						}
					}

					sender.sendMessage(this.prefix + "Es wurden " + counter + " Entities vom Typ " + type.toString() + " gefunden.");
					return true;
				}
			}

		}
		return false;
	}

	private long getAsMB(long value) {
		return value / 1024 / 1024;
	}

}