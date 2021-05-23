package me.cablemp5.floorislava;

import me.cablemp5.floorislava.commands.StartCommand;
import me.cablemp5.floorislava.listeners.DeathListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    public static final String PLUGIN_NAME = (ChatColor.GOLD + "[FloorIsLava] " + ChatColor.WHITE);

    private StartCommand startFloorIsLava;
    private DeathListener deathListener;

    @Override
    public void onEnable() {

        this.startFloorIsLava = new StartCommand(this);
        this.deathListener = new DeathListener(this);

        System.out.println("[FloorIsLava] Enabled Plugin!");
        getServer().getPluginManager().registerEvents(deathListener,this);
        Objects.requireNonNull(getCommand("floorislava")).setExecutor(startFloorIsLava);
    }

    public StartCommand getStartFloorIsLava() {
        return startFloorIsLava;
    }

    public DeathListener getDeathListener() {
        return deathListener;
    }


}
