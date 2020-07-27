package logic;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;

public class Utils {

    // y-level doesn't matter
    public static boolean isInside(Location location, Region region) {

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return inBetween(location.getBlockX(), min.getBlockX(), max.getBlockX())  &&
                inBetween(location.getBlockZ(), min.getBlockZ(), max.getBlockZ());

    }

    // y-level does matter
    public static boolean isStrictlyInside(Location location, Region region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        return inBetween(location.getBlockX(), min.getBlockX(), max.getBlockX())  &&
                inBetween(location.getBlockY(), min.getBlockY(), max.getBlockY()) &&
                inBetween(location.getBlockZ(), min.getBlockZ(), max.getBlockZ());

    }

    public static boolean inBetween(int test, int x, int y) {
        return x <= test && y >= test;
    }

    public static Location offsetLocation(Location loc, float dx, float dy, float dz, float dYaw, float dPitch) {
        return new Location(loc.getWorld(), loc.getX() + dx, loc.getY() + dy, loc.getZ() + dz,
                loc.getYaw() + dYaw, loc.getPitch() + dPitch);
    }

    public static Location offsetLocation(Location loc, float dx, float dy, float dz) {
        return offsetLocation(loc, dz, dy, dz, 0, 0);
    }

}
