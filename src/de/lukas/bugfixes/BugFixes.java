package de.lukas.bugfixes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import net.minecraft.server.v1_7_R1.Village;
import net.minecraft.server.v1_7_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

	@EventHandler(ignoreCancelled = true)
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
					toSend.sendMessage("[BugFixes] Command " + cmd + " - plugin " + plugin.getName());
				ps.append(d.toString() + ": Command " + cmd + " - plugin " + plugin.getName());
				ps.append("\r\n");
				ps.close();
				break;
			}
		}
		if (pcmd == null) {
			Date d = new Date();
			Player toSend = getServer().getPlayer(this.playerUseLogger);
			if (toSend != null)
				toSend.sendMessage("[BugFixes] " + cmd + " - " + "no plugin found");
			ps.append(d.toString() + ": Command " + cmd + " - " + "no plugin found");
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
			toSend.sendMessage("[BugFixes] PlayerInteract - Registered plugins: " + plugins);
		}
		Date d = new Date();
		ps.append(d + ": PlayerInteract - Registered plugins: " + plugins);
		ps.append("\r\n");
		ps.close();
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!this.bool_enity_ce)
			return;

		Entity e = event.getRightClicked();
		if (e == null)
			return;

		this.entity_ce_tp = event.getRightClicked();
		event.getPlayer().sendMessage("Sie haben ein Entity vom Typ " + event.getRightClicked().getType() + " ausgewählt.");
	}

	private Entity entity_ce_tp = null;
	private boolean bool_enity_ce = false;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("showsysteminfos.use") && command.getName().equalsIgnoreCase("sysinfos")) {
			for (File f : File.listRoots()) {
				if (f.canRead())
					sender.sendMessage(getAsMB(f.getFreeSpace()) + " / " + getAsMB(f.getTotalSpace()) + " MB");
			}
			return true;
		}

		if (sender.isOp() && sender instanceof Player && command.getName().equalsIgnoreCase("bugfixes")) {
			Player player = (Player) sender;

			if (args.length < 2) {
				sender.sendMessage("/bugfixes entity <choose>");
				sender.sendMessage("/bugfixes entity <tp> <world, x, y , z>");
				sender.sendMessage("/bugfixes entity <tp> <here>");
				return true;
			}

			if (args[0].equalsIgnoreCase("entity") && args[1].equalsIgnoreCase("choose")) {
				this.bool_enity_ce = true;
				sender.sendMessage("Bitte einen Rechtsklick auf das Entity machen.");
				return true;
			}
			if (args[0].equalsIgnoreCase("entity") && args[1].equalsIgnoreCase("tp")) {
				if (this.entity_ce_tp == null) {
					sender.sendMessage("[BugFixes] Kein Entity ausgewählt, bitte wählen sie ein Entity aus.");
					return true;
				}

				if (args.length == 3 && args[2].equalsIgnoreCase("here")) {
					// this.entity_ce_tp.teleport(player.getLocation());
					/*if (player.getLocation().getWorld().getName().equalsIgnoreCase(player.getLocation().getWorld().getName()))
						this.entity_ce_tp.teleport(player.getLocation());
					else {*/
						/*CraftEntity craftEntity = (CraftEntity) this.entity_ce_tp;
						craftEntity.getHandle().teleportTo(player.getLocation(), false);*/
						
						double x = player.getLocation().getX(), y = player.getLocation().getY(), z = player.getLocation().getZ();
						float yaw = player.getLocation().getYaw(), pitch = player.getLocation().getPitch();
						
						// net.minecraft.server.v1_7_R1.WorldServer nativeOldWorld = ((CraftWorld) this.entity_ce_tp.getWorld()).getHandle();
						net.minecraft.server.v1_7_R1.WorldServer nativeNewWorld = ((CraftWorld) player.getWorld()).getHandle();
						
						net.minecraft.server.v1_7_R1.Entity nativeEntity = ((CraftEntity) this.entity_ce_tp).getHandle();
						
						nativeEntity.world.removeEntity(nativeEntity);
						nativeEntity.dead = false;
						nativeEntity.world = nativeNewWorld;
						nativeEntity.setLocation(x, y, z, yaw, pitch);
						nativeEntity.world.addEntity(nativeEntity);
					// }
					sender.sendMessage("[BugFixes] Entity wurde zu deiner aktuellen Position teleportiert.");
					return true;
				}
				if (args.length == 6) {
					World world = this.getServer().getWorld(args[2]);
					if (world == null) {
						player.sendMessage("[BugFixes] Es gibt keine Welt mit diesem Namen.");
						return true;
					}

					double x = 0, y = 0, z = 0;
					try {
						x = Double.parseDouble(args[3]);
						y = Double.parseDouble(args[4]);
						z = Double.parseDouble(args[5]);
					} catch (NumberFormatException e) {
						player.sendMessage("[BugFixes] Error: Ungültige Zahlenwerte!");
						return true;
					}

					Location loc = new Location(world, x, y, z);
					// this.entity_ce_tp.teleport(loc);
					if (args[2].equalsIgnoreCase(player.getLocation().getWorld().getName()))
						this.entity_ce_tp.teleport(player.getLocation());
					else {
						CraftEntity craftEntity = (CraftEntity) this.entity_ce_tp;
						craftEntity.getHandle().teleportTo(loc, false);
					}
					sender.sendMessage("[BugFixes] Entity wurde zu den angebeben Koordinaten teleportiert.");
					return true;
				}
			}

			return true;
		}

		if (((sender instanceof Player)) && (command.getName().equalsIgnoreCase("logevent")) && (sender.isOp())) {
			Player player = (Player) sender;
			if (this.playerUseLogger == null) {
				this.playerUseLogger = player.getName();
				player.sendMessage("Logger aktiviert.");
			} else {
				this.playerUseLogger = null;
				player.sendMessage("Logger nun deaktiviert.");
			}
			return true;
		}

		if (sender instanceof Player == false && (command.getName().equalsIgnoreCase("delentities") || command.getName().equalsIgnoreCase("countentities"))) {
			if (args.length != 2 && args.length != 4) {
				sender.sendMessage("/delentities <world name> <entity id> [chunk x:z]");
				sender.sendMessage("/countentities <world name> <entity id> [chunk x:z]");
				return true;
			}

			World w = this.getServer().getWorld(args[0]);
			if (w == null) {
				sender.sendMessage("Keine Welt mit dem Namen vorhanden");
				return true;
			}

			int entityId = -1;
			try {
				entityId = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {

			}
			EntityType type = EntityType.fromId(entityId);
			if (type == null) {
				type = EntityType.fromName(args[1]);
				if (type == null) {
					sender.sendMessage("Kein Entity mit der Id oder Namen gefunden.");
					return true;
				}
			}

			if (args.length == 2) {
				if (command.getName().equals("delentities")) {
					int counter = 0;
					for (Entity e : w.getEntities()) {
						if (e.getType() == type) {
							e.remove();
							counter++;
						}
					}

					sender.sendMessage("Es wurden " + counter + " Entities vom Typ " + type.toString() + " entfernt.");
					return true;
				} else if (command.getName().equals("countentities")) {
					int counter = 0;
					for (Entity e : w.getEntities()) {
						if (e.getType() == type) {
							counter++;
						}
					}

					sender.sendMessage("Es wurden " + counter + " Entities vom Typ " + type.toString() + " gefunden.");
					return true;
				}
			} else {
				int x, z;
				try {
					String[] chunkCoords = args[3].split(":");
					if (chunkCoords.length != 2) {
						sender.sendMessage("Ungï¿½ltige Angabe. Syntax muss chunkx:chunkz sein.");
						return true;
					}
					x = Integer.parseInt(chunkCoords[0]);
					z = Integer.parseInt(chunkCoords[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage("Ungï¿½ltige Zahl");
					return true;
				}

				Chunk c = w.getChunkAt(x, z);
				if (c == null) {
					sender.sendMessage("Chunk nicht geladen. Lade es...");
					w.loadChunk(x, z);
					c = w.getChunkAt(x, z);
					if (c == null) {
						sender.sendMessage("Chunk konnte nicht geladen werden.");
						return true;
					}
				}

				if (command.getName().equals("delentities")) {
					int counter = 0;
					for (Entity e : c.getEntities()) {
						if (e.getType() == type) {
							e.remove();
							counter++;
						}
					}

					sender.sendMessage("Es wurden " + counter + " Entities vom Typ " + type.toString() + " entfernt.");
					return true;
				} else if (command.getName().equals("countentities")) {
					int counter = 0;
					for (Entity e : c.getEntities()) {
						if (e.getType() == type) {
							counter++;
						}
					}

					sender.sendMessage("Es wurden " + counter + " Entities vom Typ " + type.toString() + " gefunden.");
					return true;
				}
			}
		}

		if (sender instanceof Player == false && command.getName().equalsIgnoreCase("cleanup")) {
			if (args.length == 0) {
				sender.sendMessage("Vermisse Pfad.");
				return true;
			}

			File listNames = new File(this.getDataFolder(), "names.txt");
			if (!listNames.exists()) {
				sender.sendMessage("Nicht vorhandene Playerliste oder leere Liste.");
				return true;
			}

			String directory = "";
			for (int i = 0; i < args.length; i++) {
				System.out.println("args[" + i + "] = " + args[i]);
				directory += args[i].replace("\\", File.separator).replace("/", File.separator);
			}

			File fileDir = new File(directory);
			System.out.println(fileDir.getPath());
			if (!fileDir.exists()) {
				sender.sendMessage("Dieses Verzeichnis existiert nicht. Evtl. vertippt?");
				return true;
			}

			try {
				this.cleanUp(sender, fileDir);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			return true;
		}

		return false;
	}

	private void cleanUp(CommandSender sender, File from) throws IOException, InvalidConfigurationException {
		File listNames = new File(this.getDataFolder(), "names.txt");
		if (!listNames.exists() || listNames.length() == 0) {
			sender.sendMessage("Nicht vorhandene Playerliste oder leere Liste.");
			return;
		}
		File fileTo = new File(from.getParent(), from.getName() + "_delete");
		fileTo.mkdir();

		BufferedReader br = new BufferedReader(new FileReader(listNames));
		ArrayList<String> players = new ArrayList<String>();
		String line = "";
		while ((line = br.readLine()) != null) {
			players.add(line);
		}
		br.close();

		int hasMoved = 0, notMoved = 0;
		for (File fileName : from.listFiles()) {
			if (fileName.isDirectory())
				continue;

			String s = fileName.getName().substring(0, fileName.getName().lastIndexOf("."));
			if (!players.contains(s)) {
				if (fileName.renameTo(new File(fileTo, fileName.getName())))
					hasMoved++;
				else
					notMoved++;
			}
		}
		if (notMoved == 0)
			sender.sendMessage("Es wurden " + hasMoved + " Dateien verschoben.");
		else
			sender.sendMessage("Es konnten " + notMoved + " Dateien nicht verschoben werden.");
	}

	private long getAsMB(long value) {
		return value / 1024 / 1024;
	}

}
