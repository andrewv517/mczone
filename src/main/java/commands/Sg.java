package commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import logic.Arena;
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
                Region r = s.getSelection(s.getSelectionWorld());

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
            fillChests(player.getWorld(), survivalMain.getArenaManager().getArenaWithPlayer(player));
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
            if (!isInside(center, arena.getRegion())) {
                sender.sendMessage(ChatColor.RED + "Center point needs to be inside the arena!");
                return true;
            }

            arena.setCenter(center);
            sender.sendMessage(ChatColor.GOLD + "Center successfully set.");
            return true;

        }

        sender.sendMessage(ChatColor.RED + "Not a command!");
        return true;

    }

    public boolean isInside(Location location, Region region) {

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return inBetween(location.getBlockX(), min.getBlockX(), max.getBlockX()) &&
                inBetween(location.getBlockY(), min.getBlockY(), max.getBlockY()) &&
                inBetween(location.getBlockZ(), min.getBlockZ(), max.getBlockZ());

    }

    public boolean inBetween(int test, int x, int y) {
        return x <= test && y >= test;
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

    public void fillChests(World world, Arena arena) {


        // 40% chance of food
        Material[] food = {Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.COOKED_PORKCHOP};

        // 20% chance of armor
        Material[] armor = {Material.LEATHER_CHESTPLATE, Material.LEATHER_BOOTS, Material.LEATHER_HELMET,
                Material.LEATHER_LEGGINGS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS,
                Material.IRON_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
                Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_HELMET, Material.GOLDEN_HELMET,
                Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS};

        // 20% chance of weapon
        Material[] weapon = {Material.WOODEN_SWORD, Material.STONE_AXE, Material.BOW, Material.ARROW,
                Material.FISHING_ROD, Material.IRON_SWORD, Material.STONE_SWORD};

        // 15% chance of materials(lapis, diamonds, sticks, xp bottles)
        Material[] materials = {Material.IRON_INGOT, Material.DIAMOND, Material.STICK, Material.EXPERIENCE_BOTTLE, Material.LAPIS_LAZULI};

        // 5% chance of really good stuff(golden apples, etc.)
        Material[] op = {Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.GOLDEN_APPLE};

        Random random = new Random();

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState entity : chunk.getTileEntities()) {
                if (entity instanceof Chest && isInside(entity.getLocation(), arena.getRegion())) {
                    Chest chest = (Chest) entity;
                    Inventory inventory = chest.getBlockInventory();
                    inventory.clear();
                    for (int i = 0; i < inventory.getSize() / 5; i++) {

                        double num = random.nextDouble();

                        if (num <= 0.4) {
                            inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(food[random.nextInt(food.length)]));
                        } else if (num <= 0.6) {
                            inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(armor[random.nextInt(armor.length)]));
                        } else if (num <= 0.8) {
                            inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(weapon[random.nextInt(weapon.length)]));
                        } else if (num <= 0.95) {
                            inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(materials[random.nextInt(materials.length)]));
                        } else {
                            inventory.setItem(random.nextInt(inventory.getSize()), new ItemStack(op[random.nextInt(op.length)]));
                        }

                    }
                }
            }
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "Chests refilled");
    }
}