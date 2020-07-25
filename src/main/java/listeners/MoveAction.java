package listeners;

import logic.Arena;
import logic.ArenaManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import survivalgames.main.SurvivalMain;

import java.util.List;

public class MoveAction implements Listener {

    private final SurvivalMain survivalMain;

    public MoveAction() {
        this.survivalMain = SurvivalMain.survivalMain;
    }

    @EventHandler
    public boolean onPlayerMove(PlayerMoveEvent event) {
        if ((event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) &&
                survivalMain.getArenaManager().playerInGame(event.getPlayer()) &&
                survivalMain.getArenaManager().getArenaWithPlayer(event.getPlayer()).isFreezePeriod()) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @EventHandler
    public boolean onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) return false;

        Player damager = (Player) event.getDamager();
        Player other = (Player) event.getEntity();

        if ((survivalMain.getArenaManager().playerInGame(damager) || survivalMain.getArenaManager().playerInGame(other))
                && !(survivalMain.getArenaManager().playerInGame(damager) && survivalMain.getArenaManager().playerInGame(other))) {
            event.setCancelled(true);
            return true;
        }

        Arena arena = survivalMain.getArenaManager().getArenaWithPlayer(damager);

        if (arena.isGracePeriod() || arena.isFreezePeriod()) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "No PvP during grace period!");
            return true;
        }

        if (arena.getPlayersInGulag().contains(damager) || arena.getPlayersInGulag().contains(other)) {
            event.setCancelled(true);
            damager.sendMessage(ChatColor.RED + "No PvP while waiting in the gulag!");
            return true;
        }

        return false;

    }


}
