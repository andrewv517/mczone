package listeners;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import logic.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import survivalgames.main.SurvivalMain;

public class BlockAction implements Listener {

    private final SurvivalMain survivalMain;

    public BlockAction() {
        this.survivalMain = SurvivalMain.survivalMain;
    }

    @EventHandler
    public boolean onBlockBreak(BlockBreakEvent event) {

        if (survivalMain.getArenaManager().getArenaWithPlayer(event.getPlayer()) != null
                && (survivalMain.getArenaManager().getArenaWithPlayer(event.getPlayer()).getPlayersInGulag().contains(event.getPlayer()) ||
                survivalMain.getArenaManager().getArenaWithPlayer(event.getPlayer()).getPlayersInGulagMatch().contains(event.getPlayer()))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Cannot break blocks inside arena!");
            return true;
        }

        for (Arena a : survivalMain.getArenaManager().getArenas()) {
            if (isInside(event.getBlock().getLocation(), a.getRegion())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Cannot break blocks inside arena!");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public boolean onBlockPlace(BlockPlaceEvent event) {
        for (Arena a : survivalMain.getArenaManager().getArenas()) {
            if (isInside(event.getBlock().getLocation(), a.getRegion())) {

                // TODO: only do this if game is playing

                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Cannot place blocks inside arena!");
                return true;
            }
        }
        return false;
    }

    // tests if location is inside region
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

}
