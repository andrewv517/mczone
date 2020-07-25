package logic;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ArenaExplosion {

    private Location location;
    private float power;

    public ArenaExplosion(Location location, float power) {
        this.location = location;
        this.power = power;
    }

    public ArenaExplosion(Location location) {

        if (location.getWorld() == null) {
            return;
        }

        location.getWorld().createExplosion(0.0, 0.0, 0.0, 4F, true, true);

    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    private void explode() {
        location.getWorld().createExplosion(location, 4F, true, true);
    }

}
