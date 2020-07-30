package listeners;

import logic.Arena;
import logic.Utils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import survivalgames.main.SurvivalMain;

import java.util.List;
import java.util.Objects;

public class DeathAction implements Listener {

    private final SurvivalMain survivalMain;
    private int timer;
    private int taskID;
    private int gulagID;
    private int gulagTimer;

    public DeathAction() {
        this.survivalMain = SurvivalMain.survivalMain;
    }


    @EventHandler
    public boolean onPlayerDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return false;
        }

        Player p = (Player) event.getEntity();

        if (survivalMain.getArenaManager().getArenaWithPlayer(p) != null && p.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            handleDeath(p);
            return true;
        }

        return false;

    }

    public void handleDeath(Player player) {

        Arena arena = survivalMain.getArenaManager().getArenaWithPlayer(player);
        List<Player> gulag = arena.getPlayersInGulagMatch();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);

        if (gulag.contains(player)) {
            arena.getPlayersInGulagMatch().remove(player);
            Player other = arena.getPlayersInGulagMatch().get(0);

            arena.getPlayersInGulagMatch().clear();

            Bukkit.broadcastMessage(ChatColor.GOLD + other.getName() + " is back in!");
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " is out! " + (arena.getPlayers().size() - 1) + " players remain!");
            arena.removePlayer(player);
            player.setGameMode(GameMode.SPECTATOR);


            arena.addPlayerToPastGulag(other);
            other.getInventory().clear();
            other.setHealth(20);
            other.setFoodLevel(20);
            other.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
            //teleport
            other.teleport(arena.getRedeployLocation());
            other.sendTitle(ChatColor.GOLD + "You have 20 seconds to re-deploy!", "Your elytra will be removed after", 10, 60, 10);

            setTimer(20);
            startTimer(other);

            if (arena.getPlayersInGulag().size() >= 2 && arena.getPlayersInGulagMatch().isEmpty()) {
                Location side1 = new Location(player.getWorld(), 147, 43, -569);
                Location side2 = new Location(player.getWorld(), 147, 43, -598);

                arena.getPlayersInGulag().get(0).teleport(side1);
                arena.getPlayersInGulag().get(1).teleport(side2);

                arena.getPlayersInGulag().get(0).getInventory().clear();
                arena.getPlayersInGulag().get(1).getInventory().clear();

                arena.getPlayersInGulagMatch().add(arena.getPlayersInGulag().get(0));
                arena.getPlayersInGulagMatch().add(arena.getPlayersInGulag().get(1));

                arena.getPlayersInGulag().remove(0);
                arena.getPlayersInGulag().remove(0);


                for (Player p : arena.getPlayersInGulagMatch()) {
                    p.getInventory().clear();
                    p.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
                    p.getInventory().setItem(1, new ItemStack(Material.BOW));
                    p.getInventory().setItem(2, new ItemStack(Material.ARROW, 10));
                    p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                    p.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
                    p.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                    p.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                    p.setHealth(20);
                    p.setFoodLevel(20);
                }
            }
            return;
        }

        Location location = player.getLocation();
        for (ItemStack item : player.getInventory()) {
            if (item != null && !item.getType().equals(Material.ELYTRA)) {
                player.getWorld().dropItemNaturally(location, item);
            }
        }


        if ((arena.getPlayersInGulag().size() == 1 ||
                arena.getPlayers().size() - (arena.getPlayersInGulag().size() + arena.getPlayersInGulagMatch().size()) >= 3)
                && !arena.getPastGulag().contains(player)) {

            Bukkit.broadcastMessage(ChatColor.GOLD + "" + player.getName() + " is going to the gulag!");
            if (arena.getPlayersInGulag().size() == 0) {
                startGulagTimer(player);
            }
            arena.addPlayerToGulag(player);
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "" + player.getName() + " died!");
            player.getInventory().clear();
            player.setFireTicks(1);
            player.setGameMode(GameMode.SPECTATOR);
            arena.removePlayer(player);
        }


    }

    public void startGulagTimer(Player p) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        gulagTimer = 120;
        Arena arena = survivalMain.getArenaManager().getArenaWithPlayer(p);
        gulagID = scheduler.scheduleSyncRepeatingTask(survivalMain, () -> {
            if (gulagTimer == 0) {
                arena.addPlayerToPastGulag(p);
                arena.getPlayersInGulag().remove(p);
                p.getInventory().clear();
                p.setHealth(20);
                p.setFoodLevel(20);
                p.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
                //teleport
                p.teleport(arena.getRedeployLocation());
                p.sendTitle(ChatColor.GOLD + "You have 20 seconds to re-deploy!", "You will be killed if you do not jump!", 10, 60, 10);
                setTimer(20);
                startTimer(p);
                stopGulagTimer();
                return;
            }

            if (gulagTimer == 60 || gulagTimer == 120) {
                p.sendMessage(ChatColor.GOLD + "If no one is sent to the gulag in " + gulagTimer / 60 +  " minute(s), you will be redeployed!");
            }

            if (survivalMain.getArenaManager().getArenaWithPlayer(p) == null || survivalMain.getArenaManager().getArenaWithPlayer(p).getPlayersInGulagMatch().contains(p)) {
                stopGulagTimer();
                return;
            }

            gulagTimer--;

        }, 0L, 20L);
    }

    public void stopGulagTimer() {
        Bukkit.getScheduler().cancelTask(gulagID);
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        if (survivalMain.getArenaManager().getArenaWithPlayer(p) == null) return;

        Arena arena = survivalMain.getArenaManager().getArenaWithPlayer(p);
        if (arena.getPlayersInGulagMatch().contains(p)) {
            handleDeath(p);
        } else {
            arena.removePlayer(p);
        }
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + p.getName() + " has left! They have been eliminated");
        p.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void stopTimer() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public void startTimer(Player p) {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        taskID = scheduler.scheduleSyncRepeatingTask(survivalMain, () -> {
            if (timer == 0) {
                if (p.getInventory().getChestplate() != null && p.getInventory().getChestplate().getType().equals(Material.ELYTRA)) {
                    p.getInventory().setChestplate(new ItemStack(Material.AIR));
                }

                if (p.getInventory().contains(Material.ELYTRA)) {
                    p.getInventory().remove(Material.ELYTRA);
                }

                Arena arena = survivalMain.getArenaManager().getArenaWithPlayer(p);

                if (arena != null && Utils.isStrictlyInside(p.getLocation(), arena.getPlane())) {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "" + p.getName() + " did not jump in time!");
                    p.damage(20);
                }
                stopTimer();
                return;
            }

            if (timer == 30 || timer == 20 || timer == 10 || timer <= 5) {
                p.sendMessage(ChatColor.GOLD + "" + timer + " more seconds to jump!");
            }

            timer--;

        }, 0L, 20L);

    }

}
