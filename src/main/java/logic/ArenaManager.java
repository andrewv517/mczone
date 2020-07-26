package logic;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaManager {

    List<Arena> arenas;

    public ArenaManager() {
        this.arenas = new ArrayList<>();
    }

    public void addArena(Arena arena) {
        this.arenas.add(arena);
    }

    public List<Arena> getArenas() {
        return this.arenas;
    }

    public boolean containsBasedOnName(String name) {
        for (Arena a : this.arenas) {
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public Arena getArena(String name) {
        for (Arena a : this.arenas) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    public boolean deleteBaseOnName(String name) {
        if (!containsBasedOnName(name)) return false;

        getArena(name).stopTimer();
        getArena(name).stopWorldBorderTimer();
        Objects.requireNonNull(getArena(name).getCenter().getWorld()).getWorldBorder().setSize(30000000);
        this.arenas.remove(getArena(name));
        return true;

    }

    public boolean playerInGame(Player player) {

        if (this.getArenas().isEmpty()) {
            return false;
        }

        for (Arena a : this.arenas) {
            for (Player p : a.getPlayers()) {
                if (p.equals(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Arena getArenaWithPlayer(Player player) {
        if (playerInGame(player)) {
            for (Arena a : this.arenas) {
                for (Player p : a.getPlayers()) {
                    if (p.equals(player)) {
                        return a;
                    }
                }
            }
        }
        return null;
    }

    public Arena getArenaWithLocation(Location loc) {
        for (Arena arena : this.arenas) {
            if (Utils.isInside(loc, arena.getRegion())) {
                return arena;
            }
        }
        return null;
    }

}
