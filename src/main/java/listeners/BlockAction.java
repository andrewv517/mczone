package listeners;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import logic.Arena;
import logic.ArenaExplosion;
import logic.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import survivalgames.main.SurvivalMain;

import java.util.Objects;

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
                Block block = event.getBlockPlaced();
                if (block.getType() == Material.TNT) {
                    block.setType(Material.AIR);
                    Objects.requireNonNull(block.getLocation().getWorld())
                            .spawn(Utils.offsetLocation(block.getLocation(), 0.5f, 0.5f, 0.5f), TNTPrimed.class);
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Cannot place blocks inside arena!");
                }
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public boolean onBlockExplode(BlockExplodeEvent event) {

        for (Block block : event.blockList()) {
            Arena arena = survivalMain.getArenaManager().getArenaWithLocation(block.getLocation());
            if (arena != null) {
                if (block.getType() == Material.GLASS) {
                    event.setCancelled(true);
                } else {
                    if (!arena.getExplodedBlocks().containsKey(block) && !arena.getFallenBlocks().contains(block.getLocation())) {
                        arena.addExplodedBlock(block, block.getBlockData());
                    }
                    // might need to set block to air first as to avoid the entity from instantly stopping
                    FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation(), block.getBlockData());
                    fallingBlock.setVelocity(new Vector(Math.random() - 0.5, Math.random() * 0.5 + 0.5, Math.random() - 0.5));
                    fallingBlock.setDropItem(false);
                }
            }
        }

        return false;

    }

    @EventHandler
    public boolean onEntityExplode(EntityExplodeEvent event) {

        Location location = event.getLocation();
        Arena arena = survivalMain.getArenaManager().getArenaWithLocation(location);
        if (arena != null) {
            if (event.getEntityType() == EntityType.PRIMED_TNT) {
                event.setCancelled(true);
                new ArenaExplosion(Utils.offsetLocation(location, 0.5f, 0.5f, 0.5f)).explode();
            }
        }

        return true;

    }

    @EventHandler
    public boolean onEntityChangeBlockEvent(EntityChangeBlockEvent event) {

        // consider that this might cause issues if a block of sand falls during map construction

        Entity entity = event.getEntity();
        Arena arena = survivalMain.getArenaManager().getArenaWithLocation(entity.getLocation());
        if (arena != null) {
            if (entity instanceof FallingBlock) {
                arena.addFallenBlock(entity.getLocation().getBlock().getLocation()); // get integer location
            }
        }

        return true;

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
