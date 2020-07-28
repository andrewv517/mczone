package survivalgames.main;

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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
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
                System.out.println(arenaName);
            }
        }

    }

    @Override
    public void onDisable() {

        for (Arena arena : arenaManager.getArenas()) {

            ConfigurationSection section = arenaConfig.getConfigurationSection("arenas." + arena.getName());
            if (section == null) {
                section = arenaConfig.createSection(arena.getName());
            }
            section.set("name", arena.getName());
            section.set("world", arena.getWorld().getUID());

            Region region = arena.getRegion();

            BlockVector3 minimum = region.getMinimumPoint();
            section.set("minimum.x", minimum.getBlockX());
            section.set("minimum.y", minimum.getBlockY());
            section.set("minimum.z", minimum.getBlockZ());

            BlockVector3 maximum = region.getMaximumPoint();
            section.set("maximum.x", maximum.getBlockX());
            section.set("maximum.y", maximum.getBlockY());
            section.set("maximum.z", maximum.getBlockZ());


        }

        saveConfig(arenaConfig, arenaConfigFile);

    }


    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
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
