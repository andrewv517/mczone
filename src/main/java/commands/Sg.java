package commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import logic.Arena;
import logic.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import survivalgames.main.SurvivalMain;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;

public class Sg implements CommandExecutor {

    private final SurvivalMain survivalMain;
    private int timer;
    private int taskID;

    public Sg() {
        survivalMain = SurvivalMain.survivalMain;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            return true;
        }

        if (args.length == 0 && label.equalsIgnoreCase("sg")) {
            // implement help command
            sender.sendMessage(ChatColor.GOLD + "Welcome to my Survival Game plugin!");
            return true;
        }

        if (!label.equalsIgnoreCase("sg") || args.length <= 0) {
            return true;
        }


        Player player = (Player) sender;

        // implement sub-commands
        if (args[0].equalsIgnoreCase("create")) {
            if (survivalMain.getWorldEditPlugin() == null) {
                getLogger().info(ChatColor.RED + "WorldEdit is required for this plugin!");
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! Try /sg create [arena name] [initial border diameter]");
                return true;
            }

            double borderDiameter;

            try {
                borderDiameter = Double.parseDouble(args[2]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Not a valid border diameter!");
                return true;
            }

            try {
                LocalSession s = survivalMain.getWorldEditPlugin().getSession(player);
                if (s.getSelectionWorld() == null) {
                    sender.sendMessage(ChatColor.RED + "Make a selection with WorldEdit!");
                    return true;
                }
                Region reg = s.getSelection(s.getSelectionWorld());
                Region r = reg.clone();

                // implement checking if an arena already exists at those coordinates? don't feel like it rn
                Arena a = new Arena(r, args[1], borderDiameter);

                survivalMain.getArenaManager().addArena(a);
                sender.sendMessage(ChatColor.GOLD + "Arena \"" + args[1] + "\" successfully created!");
                return true;

            } catch (IncompleteRegionException e) {
                sender.sendMessage(ChatColor.RED + "Incomplete Selection!");
                return true;
            }

        } else if (args[0].equalsIgnoreCase("setspawn")) {

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! Try /sg setspawn [arena name]");
                return true;
            }

            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }

            Location spawn = player.getLocation();
            survivalMain.getArenaManager().getArena(args[1]).addSpawnPoint(spawn);

            int len = survivalMain.getArenaManager().getArena(args[1]).getSpawnPoints().size();

            sender.sendMessage(ChatColor.GOLD + "Set spawn point #" + len + " successfully at x = " +
                    (int) spawn.getX() + ", y = " + (int) spawn.getY() + ", z = " + (int) spawn.getZ());

            return true;

        } else if (args[0].equalsIgnoreCase("delete")) {

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! Try /sg delete [arena name]");
                return true;
            }

            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }

            if (survivalMain.getArenaManager().getArena(args[1]).getCenter() == null) {
                sender.sendMessage(ChatColor.RED + "Arena needs a center point!");
                return true;
            }

            if (survivalMain.getArenaManager().deleteBaseOnName(args[1])) {
                sender.sendMessage(ChatColor.GOLD + "Arena \"" + args[1] + "\" deleted successfully!");
            } else {
                sender.sendMessage(ChatColor.RED + "An unknown error has occurred");

            }
            return true;

        } else if (args[0].equalsIgnoreCase("join")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! Try /sg join [arena name]");
                return true;
            }

            if (survivalMain.getArenaManager().playerInGame(player)) {
                sender.sendMessage(ChatColor.RED + "You're already in a game!");
                return true;
            }

            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }

            Arena a = survivalMain.getArenaManager().getArena(args[1]);

            if (a.getCenter() == null) {
                sender.sendMessage(ChatColor.RED + "Arena needs a center point!");
                return true;
            }

            if (a.getPlane() == null) {
                sender.sendMessage(ChatColor.RED + "Arena needs a plane location!");
                return true;
            }

            if (!a.isFreezePeriod()) {
                player.sendMessage(ChatColor.RED + "Game is in progress!");
                return true;
            }

            int left = 0;

            for (Location location : a.getSpawnPoints().keySet()) {
                // true means someone is there
                if (!a.getSpawnPoints().get(location)) {
                    left++;
                }
            }

            a.prepareMap();

            if (left > 0) {
                for (Location location : a.getSpawnPoints().keySet()) {
                    // true means someone is there
                    if (!a.getSpawnPoints().get(location)) {
                        a.addPlayer(player);
                        player.teleport(location);
                        a.getSpawnPoints().put(location, true);
                        break;
                    }
                }
                Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " joined \"" + args[1] + "\", " +
                        --left + " spots left! type /sg join " + args[1] + " to join!");
            } else {
                player.sendMessage(ChatColor.RED + "Game is full!");
            }
            return true;

        } else if (args[0].equalsIgnoreCase("start")) {

            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! While in an arena, try /sg start");
                return true;
            }

            if (!survivalMain.getArenaManager().playerInGame(player)) {
                sender.sendMessage(ChatColor.RED + "Must be in a game to execute this command!");
                return true;
            }

            // resets spawn points to say that they are not being used
            survivalMain.getArenaManager().getArenaWithPlayer(player).getSpawnPoints().replaceAll((l, v) -> false);

            setTimer(10);
            survivalMain.getArenaManager().getArenaWithPlayer(player).prepareMap();
            startTimer(survivalMain.getArenaManager().getArenaWithPlayer(player));
            return true;
        } else if (args[0].equalsIgnoreCase("setcenter")) {

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! While inside an arena, try /sg setcenter [name of arena]");
                return true;
            }

            if (survivalMain.getArenaManager().playerInGame(player)) {
                sender.sendMessage(ChatColor.RED + "You're already in a game!");
                return true;
            }

            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }

            Location center = player.getLocation();
            Arena arena = survivalMain.getArenaManager().getArena(args[1]);
            if (!Utils.isInside(center, arena.getRegion())) {
                sender.sendMessage(ChatColor.RED + "Center point needs to be inside the arena!");
                return true;
            }

            arena.setCenter(center);
            sender.sendMessage(ChatColor.GOLD + "Center successfully set.");
            return true;

        } else if (args[0].equalsIgnoreCase("prepare")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! While inside an arena, try /sg repair [name of arena]");
            }
            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }
            Arena arena = survivalMain.getArenaManager().getArena(args[1]);
            arena.prepareMap();
            return true;
        } else if (args[0].equalsIgnoreCase("setplane")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Incorrect command usage! While inside an arena, try /sg setplane [name of arena]");
                return true;
            }

            if (survivalMain.getArenaManager().playerInGame(player)) {
                sender.sendMessage(ChatColor.RED + "You're already in a game!");
                return true;
            }

            if (!survivalMain.getArenaManager().containsBasedOnName(args[1])) {
                sender.sendMessage(ChatColor.RED + "No arena named \"" + args[1] + "\"!");
                return true;
            }

            try {
                LocalSession s = survivalMain.getWorldEditPlugin().getSession(player);
                if (s.getSelectionWorld() == null) {
                    sender.sendMessage(ChatColor.RED + "Make a selection with WorldEdit!");
                    return true;
                }
                Region reg = s.getSelection(s.getSelectionWorld());
                Region r = reg.clone();
                survivalMain.getArenaManager().getArena(args[1]).setPlane(r);

                sender.sendMessage(ChatColor.GOLD + "Plane location successfully created.");
                return true;

            } catch (IncompleteRegionException e) {
                sender.sendMessage(ChatColor.RED + "Incomplete Selection!");
                return true;
            }

        }

        sender.sendMessage(ChatColor.RED + "Not a command!");
        return true;

    }


    public void startGame(Arena arena) {
        for (Player p : arena.getPlayers()) {
            p.sendTitle(ChatColor.GOLD + "Game has started!", "Jump out of the plane and glide to a location!", 10, 100, 10);
            p.getInventory().clear();
            p.setHealth(20);
            p.setFoodLevel(20);
            p.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
        }

        arena.endFreezePeriod();

    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void stopTimer() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public void startTimer(Arena arena) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(survivalMain, () -> {
            if (timer == 0) {
                // grace period over
                startGame(arena);
                stopTimer();
                return;
            }

            if (timer <= 10) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "Game starting in " + timer + " seconds!");
            }

            timer--;

        }, 0L, 20L);

    }

}