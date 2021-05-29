package me.cablemp5.floorislava.commands;

import me.cablemp5.floorislava.Main;
import me.cablemp5.floorislava.utils.RandomLocationUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Pattern;

public class StartCommand implements TabExecutor {

    private final Pattern numPattern = Pattern.compile("\\d+");
    private static final Set<Material> LIQUID_MATERIALS = new HashSet<>(Arrays.asList(Material.LAVA, Material.WATER, Material.SEAGRASS, Material.KELP, Material.TALL_SEAGRASS));
    private final Set<String> OVERLAY_OPTIONS = new HashSet<>(Arrays.asList("none", "basic", "advanced"));
    private final String INSUFFICIENT_ARGS = Main.PLUGIN_NAME + ChatColor.RED + "Insufficient or unknown arguments. Try " + ChatColor.AQUA + "/floorislava help" + ChatColor.RED + " for a list of commands";

    private List<Player> playersAlive = new ArrayList<>();
    private double borderSize = 100;
    private int initHeight = 50;
    private int risePeriod = 10;
    private Material riseBlock = Material.LAVA;
    private String overlayString = "basic";
    private int displayHeight;
    private boolean gameInProgress = false;
    private boolean startAtPos = false;
    private int playersToStart = 2;
    private int initSpawnLimit;

    private Location startPosition;
    private Player player;

    private final Main main;
    public StartCommand(Main main) {
        this.main = main;
    }

    public List<Player> getPlayersAlive() {
        return this.playersAlive;
    }
    public void setPlayersAlive(List<Player> playersAlive) {this.playersAlive = playersAlive; }
    public boolean getGameInProgress() { return this.gameInProgress; }
    public int getPlayersToStart() {
        return playersToStart;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            player = (Player) sender;

            switch (args.length) {
                case 0: {
                    TextComponent component = new TextComponent();
                    component.setText(Main.PLUGIN_NAME + "A floor is lava minigame plugin! Try" + ChatColor.AQUA + " /floorislava start " + ChatColor.WHITE + "to start a game");
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/floorislava start"));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text(ChatColor.AQUA + "/floorislava start")));
                    player.spigot().sendMessage(component);
                    break;
                }
                case 1: {
                    switch (args[0]) {
                        case "start": {
                            playersToStart = 2; startAtPos = false; startGame(); break;
                        }
                        case "end": {
                            endGame(); break;
                        }
                        case "reset": {
                            reset(); break;
                        }
                        case "help": {
                            help(); break;
                        }
                        default: {
                            player.sendMessage(INSUFFICIENT_ARGS);
                            break;
                        }
                    }
                    break;
                }
                case 2: {
                    if (args[0].equals("get")) {
                        switch (args[1]) {
                            case "bordersize": {
                                player.sendMessage(Main.PLUGIN_NAME + "bordersize is " + ChatColor.AQUA + borderSize);
                                break;
                            }
                            case "delay": {
                                player.sendMessage(Main.PLUGIN_NAME + "delay is " + ChatColor.AQUA + risePeriod);
                                break;
                            }
                            case "startheight": {
                                player.sendMessage(Main.PLUGIN_NAME + "startheight is " + ChatColor.AQUA + initHeight);
                                break;
                            }
                            case "overlay": {
                                player.sendMessage(Main.PLUGIN_NAME + "overlay is " + ChatColor.AQUA + overlayString);
                                break;
                            }
                            case "risingblock": {
                                player.sendMessage(Main.PLUGIN_NAME + "risingblock is " + ChatColor.AQUA + (riseBlock.equals(Material.LAVA) ? "lava":"void"));
                                break;
                            }
                            default: {
                                player.sendMessage(INSUFFICIENT_ARGS);}
                        }
                    } else if (args[0].equals("start")) {
                        if (args[1].equals("singleplayer") || args[1].equals("multiplayer")) {
                            playersToStart = args[1].equals("singleplayer") ? 1 : 2;
                            startGame();
                        } else {
                            player.sendMessage(INSUFFICIENT_ARGS);
                        }
                    } else {
                        player.sendMessage(INSUFFICIENT_ARGS);
                    }
                    break;
                }
                case 3: {
                    if (args[0].equals("set")) {
                        switch (args[1]) {
                            case "risingblock": {
                                if (args[2].equals("lava")) {
                                    riseBlock = Material.LAVA;
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "Set risingBlock to lava");
                                } else if (args[2].equals("void")) {
                                    riseBlock = Material.AIR;
                                    initHeight = 0;
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "Set risingBlock to " + ChatColor.AQUA + "void" + ChatColor.RESET +" and startheight to " + ChatColor.AQUA + "0");
                                } else {
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "risingblock can only be void or lava");
                                }
                                break;
                            }
                            case "bordersize": {
                                if (numPattern.matcher(args[2]).matches() && Integer.parseInt(args[2]) > 0) {
                                    int size = Integer.parseInt(args[2]);
                                    if (size%2 != 0) {
                                        player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "bordersize must be an even number");
                                    } else {
                                        borderSize = size;
                                        player.sendMessage(Main.PLUGIN_NAME + "Set worldBorder size to " + ChatColor.AQUA + borderSize);
                                        if (borderSize >= 150) {
                                            player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "A bordersize greater than 150 may cause lag!");
                                        }
                                    }
                                } else {
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "Input an integer greater than 0 for bordersize");
                                }
                                break;
                            }
                            case "delay": {
                                if (numPattern.matcher(args[2]).matches() && Integer.parseInt(args[2]) < 1) {
                                    risePeriod = Integer.parseInt(args[2]);
                                    player.sendMessage(Main.PLUGIN_NAME + "Set delay to " + ChatColor.AQUA + risePeriod + " seconds");
                                } else {
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "Input an integer greater or equal to 1 for delay");
                                }
                                break;
                            }
                            case "startheight": {
                                if (numPattern.matcher(args[2]).matches() && (Integer.parseInt(args[2]) >= 0 && Integer.parseInt(args[2]) < 256)) {
                                    initHeight = Integer.parseInt(args[2]);
                                    player.sendMessage(Main.PLUGIN_NAME + "Set startheight to " + ChatColor.AQUA + initHeight);
                                } else {
                                    player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "Input an integer greater or equal to 0 and less than 256 for starting height");
                                }
                                break;
                            }
                            case "overlay": {
                                if (OVERLAY_OPTIONS.contains(args[2])) {
                                    overlayString = args[2];
                                }
                                player.sendMessage(Main.PLUGIN_NAME + (OVERLAY_OPTIONS.contains(args[2]) ? Main.PLUGIN_NAME + "Set overlay to " + ChatColor.AQUA + overlayString : Main.PLUGIN_NAME + ChatColor.RED + "overlay must be none, basic, or advanced"));
                                break;
                            }
                            default: {
                                player.sendMessage(INSUFFICIENT_ARGS);
                                break;
                            }
                        }
                    } else if (args[0].equals("start")) {
                        if (args[1].equals("singleplayer") || args[1].equals("multiplayer")) {
                            playersToStart = args[1].equals("singleplayer") ? 1 : 2;
                            if ((args[2].equals("randomlocation") || args[2].equals("currentlocation"))) {
                                startAtPos = args[2].equals("currentlocation");
                                startGame();
                            } else {
                                player.sendMessage(INSUFFICIENT_ARGS);
                            }
                            startGame();
                        } else {
                            player.sendMessage(INSUFFICIENT_ARGS);
                        }
                    } else {
                        player.sendMessage(INSUFFICIENT_ARGS);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        switch (args.length) {
            case 1: {
                return Arrays.asList("get", "set", "end", "start", "reset","help");
            }
            case 2: {
                if (args[0].equals("set") || args[0].equals("get")) {
                    return Arrays.asList("bordersize", "delay", "startheight", "overlay","risingblock");
                } else if (args[0].equals("start")) {
                    return Arrays.asList("singleplayer","multiplayer");
                }
            }
            case 3: {
                if (args[0].equals("set")) {
                    switch (args[1]) {
                        case "bordersize":
                            return Arrays.asList("50", "100", "200");
                        case "delay":
                            return Arrays.asList("3", "5", "10");
                        case "startheight":
                            return Arrays.asList("0", "30", "60");
                        case "overlay":
                            return Arrays.asList("basic", "advanced", "none");
                        case "risingblock":
                            return Arrays.asList("lava","void");
                    }
                } else if (args[0].equals("start")) {
                    return Arrays.asList("currentlocation","randomlocation");
                }
            }
        }
        return null;
    }

    public void startGame() {

        playersAlive = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (!gameInProgress && playersAlive.size() >= playersToStart) {

            gameInProgress = true;
            World world = Bukkit.getServer().getWorlds().get(0);
            startPosition = (startAtPos ? new Location(world,player.getLocation().getX(),world.getHighestBlockYAt((int)player.getLocation().getX(),(int)player.getLocation().getZ()),player.getLocation().getBlockZ()) : RandomLocationUtil.generateRandomLocation(world));

            initSpawnLimit = world.getMonsterSpawnLimit();
            world.setMonsterSpawnLimit((int)(Math.pow(borderSize,2)/512));
            world.setSpawnLocation(startPosition);
            world.getWorldBorder().setCenter(startPosition);
            world.getWorldBorder().setSize(borderSize);

            for (Player p: playersAlive) {
                p.teleport(startPosition);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue());
                p.setFoodLevel(20);
                p.getInventory().clear();
                p.sendMessage(Main.PLUGIN_NAME + ChatColor.GREEN + "Game started!");
            }

            startGameLoop();

        } else if (!gameInProgress) {
            player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "You need at least " + playersToStart + " players on the server to start the game");
        } else {
            player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "There is already a game in progress");
        }
    }

    public void endGame()  {

        if (gameInProgress) {

            gameInProgress = false;

            World world = Bukkit.getServer().getWorlds().get(0);
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

            for (Player p: players) {
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage(Main.PLUGIN_NAME + ChatColor.GREEN + "Game ended!");
                TextComponent component = new TextComponent();
                component.setText(Main.PLUGIN_NAME + ChatColor.AQUA + "Click here to play again!");
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/floorislava start"));
                p.spigot().sendMessage(component);
            }

            world.setMonsterSpawnLimit(initSpawnLimit);
            world.getWorldBorder().setCenter(new Location(world,0,0,0));
            world.getWorldBorder().setSize(30000000);
            new Location(world,world.getSpawnLocation().getX(),world.getSpawnLocation().getY()-1,world.getSpawnLocation().getZ()).getBlock().setType(Material.BARRIER);

        } else {
            player.sendMessage(Main.PLUGIN_NAME + ChatColor.RED + "There is not a game in progress");
        }
    }

    public void help() {
        player.sendMessage(Main.PLUGIN_NAME + "FloorIsLava Commands");
        player.sendMessage("-------------------------------------");
        player.sendMessage(ChatColor.GOLD + "/floorislava start" + ChatColor.RESET +  " | Starts the game");
        player.sendMessage(ChatColor.GOLD + "/floorislava end" + ChatColor.RESET + " | Ends the game");
        player.sendMessage(ChatColor.GOLD + "/floorislava set <setting> <value>" + ChatColor.RESET + " | Sets a game setting");
        player.sendMessage(ChatColor.GOLD + "/floorislava get <setting>" + ChatColor.RESET + " | Gets a game setting");
        player.sendMessage(ChatColor.GOLD + "/floorislava reset" + ChatColor.RESET + " | Resets game settings");
        player.sendMessage("-------------------------------------");
    }

    public void reset() {

        gameInProgress = false;

        World world = Bukkit.getServer().getWorlds().get(0);
        world.getWorldBorder().setCenter(new Location(world,0,0,0));
        world.getWorldBorder().setSize(30000000);

        borderSize = 100;
        initHeight = 50;
        risePeriod = 10;
        riseBlock = Material.LAVA;
        overlayString = "basic";
        startAtPos = false;

        player.sendMessage(Main.PLUGIN_NAME + "All settings reverted to default");

    }

    public void startGameLoop() {

        displayHeight = initHeight;

        new BukkitRunnable() {

            final List<Player> players = playersAlive;
            final Material block = riseBlock;

            @Override
            public void run() {
                if (!gameInProgress) {
                    this.cancel();
                } else {
                    if (playersAlive.size() == playersToStart - 1) {
                        endGame();
                    } else {

                        Player highest = players.get(0), lowest = players.get(0);

                        for (Player p : players) {
                            if (p.getLocation().getBlockY() > highest.getLocation().getBlockY()) {
                                highest = p;
                            } else if (p.getLocation().getBlockY() < lowest.getLocation().getBlockY()) {
                                lowest = p;
                            }
                        }

                        for (Player p : players) {
                            String yLevelIndicator = block.equals(Material.LAVA) ? "LAVA Y-LEVEL: " + ChatColor.RED : "VOID Y-LEVEL: " + ChatColor.DARK_PURPLE;
                            if (overlayString.equals("basic")) {
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.BOLD + yLevelIndicator + "" + ChatColor.BOLD + displayHeight + "↑"));
                            } else if (overlayString.equals("advanced")) {
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent((ChatColor.BOLD + yLevelIndicator + "" + ChatColor.BOLD + displayHeight + "↑" + ChatColor.RESET + "" + ChatColor.GRAY + " | " + ChatColor.RESET + "" + ChatColor.BOLD + "HIGHEST: " + ChatColor.AQUA + "" + ChatColor.BOLD + highest.getName() + ChatColor.RESET + "" + ChatColor.GRAY + " | " + ChatColor.RESET + "" + ChatColor.BOLD +  "LOWEST: " + ChatColor.GOLD + "" + ChatColor.BOLD + lowest.getName())));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(main,60L,10L);

        new BukkitRunnable() {

            final Location location = startPosition;
            final double radius = borderSize/2;
            int currentHeight = initHeight;
            final Material block = riseBlock;

            final Location topl = new Location(location.getWorld(), location.getX() - radius + 0.5,0,location.getZ() - radius + 0.5);
            final Location topr = new Location(location.getWorld(), location.getX() + radius - 0.5,0,location.getZ() - radius + 0.5);
            final Location bottoml = new Location(location.getWorld(), location.getX() - radius + 0.5,0,location.getZ() + radius - 0.5);

            @Override
            public void run() {
                if (!gameInProgress){
                    this.cancel();
                } else {
                    if (!(currentHeight > player.getWorld().getMaxHeight())) {
                        displayHeight = currentHeight;
                        for (int x = topl.getBlockX(); x < topr.getBlockX()+1; x++) {

                            for (int z = topl.getBlockZ(); z < bottoml.getBlockZ()+1;z++) {
                                new Location(location.getWorld(), x, currentHeight,z).getBlock().setType(block);
                            }

                            List<Location> walls = Arrays.asList(
                                    new Location(location.getWorld(),x, currentHeight,topl.getZ()-1), //front wall
                                    new Location(location.getWorld(),x, currentHeight,bottoml.getZ()+1), //back wall
                                    new Location(location.getWorld(),topl.getX()-1, currentHeight,topl.getZ()+(x-topl.getBlockX())), //left wall
                                    new Location(location.getWorld(),topr.getX()+1, currentHeight,topr.getZ()+(x-topl.getBlockX())) //right wall
                            );

                            for (Location loc: walls) {
                                if (LIQUID_MATERIALS.contains(loc.getBlock().getType())) {
                                    loc.getBlock().setType(Material.BARRIER);
                                }
                            }
                        }
                        currentHeight++;
                    }
                }
            }
        }.runTaskTimer(main,60L,(long)risePeriod*20);
    }
}