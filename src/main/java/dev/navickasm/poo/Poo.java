package dev.navickasm.poo;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Random;

public class Poo extends JavaPlugin implements Listener {

    private static final String KEY = "POO";

    private boolean functionalityEnabled = false;
    private double randomPercent = 1.0;
    private int despawnTicks = 10;

    private final Random random = new Random();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        functionalityEnabled = getConfig().getBoolean("enabled", false);
        randomPercent = getConfig().getDouble("chance", 1.0);
        despawnTicks = getConfig().getInt("despawn-ticks", 10);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getConfig().set("enabled", functionalityEnabled);
        getConfig().set("chance", randomPercent);
        getConfig().set("despawn-ticks", despawnTicks);
        saveConfig();
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!functionalityEnabled || !event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();

        if (random.nextDouble() < randomPercent) {
            Location l = player.getLocation();
            boolean isMeep = player.getUniqueId().toString() == "5a47b962-915c-46c6-9823-5512fb79cba2";
            ItemStack is = new ItemStack(isMeep?Material.CARROT:Material.COCOA_BEANS, 1);
            Item i = l.getWorld().dropItem(l, is);
            i.setMetadata(KEY, new FixedMetadataValue(this, true));

            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    i.remove();
                }
            }, 200);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item i = event.getItem();

        if (i.hasMetadata(KEY)) {
            List<MetadataValue> metadataValues = i.getMetadata(KEY);
            for (MetadataValue value : metadataValues) {
                if (value.getOwningPlugin() == this && value.asBoolean()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("poo")) return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /poo <toggle|chance [value%]|despawn [seconds]|cooldown [seconds]>");
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (sender.hasPermission("poo.toggle") || sender.isOp()) {
                functionalityEnabled = !functionalityEnabled;
                String status = functionalityEnabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                sender.sendMessage(ChatColor.AQUA + "Poo is now " + status + ChatColor.AQUA + ".");
                getConfig().set("enabled", functionalityEnabled);
                saveConfig();
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("chance")) {
            if (sender.hasPermission("poo.chance") || sender.isOp()) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /poo chance <value%>");
                    return true;
                }
                try {
                    double newChance = Double.parseDouble(args[1].replaceAll("%","")) / 100.0;
                    if (newChance < 0 || newChance > 1) {
                        sender.sendMessage(ChatColor.RED + "Chance must be between 0% and 100%.");
                        return true;
                    }
                    randomPercent = newChance;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Could not interpret chance value. Please use a number (e.g., 50 or 50%).");
                    return false;
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "An unexpected error occurred while setting chance.");
                    getLogger().severe("Error setting chance: " + e.getMessage());
                    return false;
                }
                sender.sendMessage(ChatColor.AQUA + "Poo chance is now " + (randomPercent*100) + ChatColor.AQUA + "%");
                getConfig().set("chance", randomPercent);
                saveConfig();
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("despawn")) {
            if (sender.hasPermission("poo.despawn") || sender.isOp()) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /poo despawn <seconds>");
                    return true;
                }
                try {
                    int newDespawnSeconds = Integer.parseInt(args[1]);
                    if (newDespawnSeconds < 0) {
                        sender.sendMessage(ChatColor.RED + "Despawn seconds cannot be negative. Use 0 to prevent despawn.");
                        return true;
                    }
                    despawnTicks = newDespawnSeconds;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Could not interpret despawn seconds. Please use a whole number.");
                    return false;
                }
                sender.sendMessage(ChatColor.AQUA + "Poo despawn set to " + despawnTicks + " seconds.");
                getConfig().set("despawn-ticks", despawnTicks);
                saveConfig();
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
            return true;
        }

        return false;
    }
}