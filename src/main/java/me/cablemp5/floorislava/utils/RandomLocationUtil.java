package me.cablemp5.floorislava.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class RandomLocationUtil {

    private static final ImmutableSet<Material> INVALID_MATERIALS = Sets.immutableEnumSet(Material.LAVA, Material.WATER);
    private static final Random random = new Random();

    public static Location generateRandomLocation(World world) {

        Location randomLocation = null;

        while (randomLocation == null || INVALID_MATERIALS.contains(randomLocation.getBlock().getType())) {

            int x = random.nextInt(200000)-100000;
            int z = random.nextInt(200000)-100000;
            int y = world.getHighestBlockYAt(x,z);

            randomLocation = new Location(world,x,y,z);

        }
        return randomLocation.add(0,1,0);
    }
}
