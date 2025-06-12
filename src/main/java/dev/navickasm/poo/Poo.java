package dev.navickasm.poo;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

public class Poo extends JavaPlugin implements Listener {

    private static final String KEY = "POO";

    private boolean functionalityEnabled = false;

    private double randomPercent = 1.0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        functionalityEnabled = getConfig().getBoolean("enabled", false);
        randomPercent = getConfig().getDouble("chance", 1.0);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getConfig().set("enabled", functionalityEnabled);
        getConfig().set("chance", randomPercent);
        saveConfig();
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (functionalityEnabled && event.isSneaking()) {
            if (Math.random() < randomPercent) {
                Location l = event.getPlayer().getLocation();
                ItemStack is = new ItemStack(Material.COCOA_BEANS, 1);
                Item i = l.getWorld().dropItem(l, is);
                i.setMetadata(KEY, new FixedMetadataValue(this, true));
            }
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
            sender.sendMessage(ChatColor.RED + "Usage: /poo <toggle|chance [value]>");
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

        return false;
    }
}