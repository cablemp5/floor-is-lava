package me.cablemp5.floorislava.listeners;

import me.cablemp5.floorislava.Main;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class DeathListener implements Listener {

    private final Main main;

    public DeathListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (main.getStartFloorIsLava().getGameInProgress()) {

            Player player = event.getEntity();
            List<Player> players = main.getStartFloorIsLava().getPlayersAlive();

            players.remove(player);
            main.getStartFloorIsLava().setPlayersAlive(players);

            if (main.getStartFloorIsLava().getPlayersAlive().size() == main.getStartFloorIsLava().getPlayersToStart()) {
                for (Player p: player.getWorld().getPlayers()) {
                    if (main.getStartFloorIsLava().getPlayersToStart() > 1) {
                        p.sendMessage(Main.PLUGIN_NAME + ChatColor.GREEN + main.getStartFloorIsLava().getPlayersAlive().get(0).getName() + " wins!");
                    } else {
                        p.sendMessage(Main.PLUGIN_NAME + ChatColor.GREEN + "Test over");
                    }
                }
            }
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}
