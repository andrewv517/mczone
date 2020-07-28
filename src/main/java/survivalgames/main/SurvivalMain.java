package survivalgames.main;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import commands.Sg;
import listeners.BlockAction;
import listeners.DeathAction;
import listeners.MoveAction;
import logic.Arena;
import logic.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class SurvivalMain extends JavaPlugin {

    public static SurvivalMain survivalMain;
    private WorldEditPlugin worldEditPlugin;
    private ArenaManager arenaManager;
    private static final String arenaConfigFile = "arenas.yml";
    private YamlConfiguration arenaConfig;

    @Override
    public void onEnable() {
        survivalMain = this;
        this.arenaManager = new ArenaManager();

        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().
                getPlugin("WorldEdit");


        Objects.requireNonNull(this.getCommand("sg")).setExecutor(new Sg());

        getServer().getPluginManager().registerEvents(new BlockAction(), this);
        getServer().getPluginManager().registerEvents(new MoveAction(), this);
        getServer().getPluginManager().registerEvents(new DeathAction(), this);

        arenaConfig = loadConfig(arenaConfigFile);

        ConfigurationSection arenaConfigurationSection = arenaConfig.getConfigurationSection("arenas");
        if (arenaConfigurationSection != null) {
            for (String arenaName : arenaConfigurationSection.getKeys(false)) {

                try {

                    ConfigurationSection config = arenaConfigurationSection.getConfigurationSection(arenaName);
                    World world = Bukkit.getWorld(UUID.fromString(config.getString("world")));
                    double borderSize = config.getDouble("borderSize");

                    BlockVector3 minimum = BlockVector3.at(config.getInt("minimum.x"), config.getInt("minimum.y"), config.getInt("minimum.z"));
                    BlockVector3 maximum = BlockVector3.at(config.getInt("maximum.x"), config.getInt("maximum.y"), config.getInt("maximum.z"));
                    Region region = new CuboidRegion(BukkitAdapter.adapt(world), minimum, maximum);

                    Arena arena = new Arena(region, arenaName, borderSize);
                    for (String spawnpoint : config.getConfigurationSection("spawnpoints").getKeys(false)) {
                        Location location = new Location(world,
                                config.getDouble("spawnpoints." + spawnpoint + ".x"),
                                config.getDouble("spawnpoints." + spawnpoint + ".y"),
                                config.getDouble("spawnpoints." + spawnpoint + ".z")
                        );
                        arena.addSpawnPoint(location);
                    }

                    for (String s : config.getConfigurationSection("fallenBlocks").getKeys(false)) {
                        Location location = new Location(world,
                                config.getDouble("fallenBlocks." + s + ".x"),
                                config.getDouble("fallenBlocks." + s + ".y"),
                                config.getDouble("fallenBlocks." + s + ".z")
                        );
                        arena.addFallenBlock(location);
                    }

                    for (String s : config.getConfigurationSection("explodedBlocks").getKeys(false)) {
                        Location location = new Location(world,
                                config.getDouble("explodedBlocks.location." + s + ".x"),
                                config.getDouble("explodedBlocks.location." + s + ".y"),
                                config.getDouble("explodedBlocks.location." + s + ".z")
                        );
                        arena.addExplodedBlock(location.getBlock(), Bukkit.createBlockData(config.getString("explodedBlocks." + s + ".data")));
                    }

                    arena.setRedeployLocation(new Location(world,
                            config.getDouble("redeploy.x"),
                            config.getDouble("redeploy.y"),
                            config.getDouble("redeploy.z"))
                    );

                    arena.setCenter(new Location(world,
                            config.getDouble("center.x"),
                            config.getDouble("center.y"),
                            config.getDouble("center.z"))
                    );

                    BlockVector3 planeMin = BlockVector3.at(
                            config.getInt("plane.min.x"), config.getInt("plane.min.y"), config.getInt("plane.min.z")
                    );
                    BlockVector3 planeMax = BlockVector3.at(
                            config.getInt("plane.max.x"), config.getInt("plane.max.y"), config.getInt("plane.max.z")
                    );

                    arena.setPlane(new CuboidRegion(BukkitAdapter.adapt(world), planeMin, planeMax));

                    getArenaManager().addArena(arena);

                    getLogger().log(Level.INFO, "Loaded arena " + arenaName);

                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Failed to load arena " + arenaName, e);
                }


            }
        }

    }

    @Override
    public void onDisable() {

        for (Arena arena : arenaManager.getArenas()) {
            arenaManager.saveArena(arena);
        }

        saveConfig(arenaConfig, arenaConfigFile);

    }


    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public YamlConfiguration getArenaConfig() {
        return arenaConfig;
    }

    public YamlConfiguration loadConfig(String path) {

        File file = new File(getDataFolder(), path);
        YamlConfiguration config;
        getLogger().log(Level.INFO, "Loading configuration file " + path);
        if (!file.exists()) {
            getLogger().log(Level.WARNING, "No configuration file found for " + path + ", creating one.");
            Reader defaultReader = new InputStreamReader(getResource(path));
            config = YamlConfiguration.loadConfiguration(defaultReader);
            try {
                config.save(file);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not create config " + path + ".yml", e);
            }
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }

        return config;

    }

    public void saveConfig(YamlConfiguration config, String path) {

        getLogger().log(Level.INFO, "Saving configuration file " + path);

        try {
            config.save(new File(getDataFolder(), path));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config " + path, e);
        }

    }

}
