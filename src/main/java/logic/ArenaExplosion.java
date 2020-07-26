package logic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;

public class ArenaExplosion {

    private Location location;
    private float power;

    public ArenaExplosion(Location location, float power) throws NullPointerException {

        if (location.getWorld() == null) {
            throw new NullPointerException("World cannot be null");
        }

        this.location = location;
        this.power = power;
    }

    public ArenaExplosion(Location location) throws NullPointerException {

        if (location.getWorld() == null) {
            throw new NullPointerException("World cannot be null");
        }

        this.location = location;
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

    public void explode() {
        Objects.requireNonNull(location.getWorld()).createExplosion(location, 4F, true, true);
    }

}
