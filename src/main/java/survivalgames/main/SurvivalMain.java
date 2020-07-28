package survivalgames.main;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import commands.Sg;
import listeners.BlockAction;
import listeners.DeathAction;
import listeners.MoveAction;
import logic.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Objects;

public final class SurvivalMain extends JavaPlugin {

    public static SurvivalMain survivalMain;
    private WorldEditPlugin worldEditPlugin;
    private ArenaManager arenaManager;

    public static void main(String[] args) {
    }

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


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }
}
