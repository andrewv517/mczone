package logic;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;

public class Utils {

    public static boolean isInside(Location location, Region region) {

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return inBetween(location.getBlockX(), min.getBlockX(), max.getBlockX()) &&
                inBetween(location.getBlockY(), min.getBlockY(), max.getBlockY()) &&
                inBetween(location.getBlockZ(), min.getBlockZ(), max.getBlockZ());

    }

    public static boolean inBetween(int test, int x, int y) {
        return x <= test && y >= test;
    }

}
