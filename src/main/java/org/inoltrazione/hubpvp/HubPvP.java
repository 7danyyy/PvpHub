package org.inoltrazione.hubpvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class HubPvP extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();
        Bukkit.getPluginManager().registerEvents(new PvPListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    public boolean isPvPEnabled(Player player) {
        return player.hasMetadata("pvp-enabled");
    }

    public void enablePvP(Player player) {
        int timeToActivate = config.getInt("activation.time-to-activate");
        String activationMessage = config.getString("activation.activation-countdown");
        for (int i = timeToActivate; i > 0; i--) {
            final int countdown = i;
            Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', activationMessage.replace("%time%", String.valueOf(countdown)))), (timeToActivate - countdown) * 20L);
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.setMetadata("pvp-enabled", new FixedMetadataValue(this, true));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("activation.activated")));
            if (config.getBoolean("activation.execute-console-commands")) {
                for (String command : config.getStringList("activation.console-commands")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }
            equipPvPArmor(player);
        }, timeToActivate * 20L);
    }

    public void disablePvP(Player player) {
        int timeToDeactivate = config.getInt("deactivation.time-to-deactivate");
        String deactivationMessage = config.getString("deactivation.deactivation-countdown");
        for (int i = timeToDeactivate; i > 0; i--) {
            final int countdown = i;
            Bukkit.getScheduler().runTaskLater(this, () -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', deactivationMessage.replace("%time%", String.valueOf(countdown)))), (timeToDeactivate - countdown) * 20L);
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.removeMetadata("pvp-enabled", this);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("deactivation.deactivated")));
            if (config.getBoolean("deactivation.execute-console-commands")) {
                for (String command : config.getStringList("deactivation.console-commands")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }
            removePvPArmor(player);
        }, timeToDeactivate * 20L);
    }

    public boolean isSwordOnly() {
        return config.getBoolean("pvp-active.sword-only");
    }

    public ItemStack createPvpSword() {
        ItemStack sword = new ItemStack(Material.valueOf(config.getString("sword-settings.sword-material")));
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("sword-settings.sword-name")));
        meta.setLore(config.getStringList("sword-settings.lore"));
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }

    public void handleKill(Player killer, Player victim) {
        String killMessage = ChatColor.translateAlternateColorCodes('&', config.getString("kill-settings.kill-message")
                .replace("%killed_player%", victim.getName())
                .replace("%alive_player%", killer.getName()));
        Bukkit.broadcastMessage(killMessage);

        if (config.getBoolean("kill-settings.kill-commands.run-commands")) {
            for (String command : config.getStringList("kill-settings.kill-commands.console-commands")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", killer.getName()));
            }
        }
    }

    public void equipPvPArmor(Player player) {
        ItemStack helmet = createPvPArmorPiece("helmet");
        ItemStack chestplate = createPvPArmorPiece("chestplate");
        ItemStack leggings = createPvPArmorPiece("leggings");
        ItemStack boots = createPvPArmorPiece("boots");

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    public void removePvPArmor(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
    }

    private ItemStack createPvPArmorPiece(String piece) {
        String nameKey = String.format("armor-settings.%s-name", piece);
        String materialKey = String.format("armor-settings.%s-material", piece);
        ItemStack armorPiece = new ItemStack(Material.valueOf(config.getString(materialKey)));
        ItemMeta meta = armorPiece.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(nameKey)));
        armorPiece.setItemMeta(meta);
        return armorPiece;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isPvPEnabled(player)) {
            disablePvP(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int swordSlot = config.getInt("sword-settings.inventory-slot");
        ItemStack currentItem = player.getInventory().getItem(swordSlot);
        ItemStack pvpSword = createPvpSword();

        if (currentItem == null || !currentItem.isSimilar(pvpSword)) {
            player.getInventory().setItem(swordSlot, pvpSword);
        }
    }
}
