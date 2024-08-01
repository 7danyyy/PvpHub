package org.inoltrazione.hubpvp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PvPListener implements Listener {

    private final HubPvP plugin;

    public PvPListener(HubPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sword-settings.sword-name")))) {
                switch (event.getAction()) {
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        if (plugin.isPvPEnabled(player)) {
                            plugin.disablePvP(player);
                        } else {
                            plugin.enablePvP(player);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            if (!plugin.isPvPEnabled(damager)) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("pvp-unactive.denied-hit-message").replace("%target%", target.getName())));
                event.setCancelled(true);
                return;
            }

            if (plugin.isSwordOnly() && !isHoldingPvpSword(damager)) {
                damager.sendMessage(ChatColor.RED + "You can only attack with the PvP sword!");
                event.setCancelled(true);
                return;
            }

            if (!plugin.isPvPEnabled(target)) {
                damager.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("pvp-unactive.not-in-pvp-message").replace("%target%", target.getName())));
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            plugin.handleKill(killer, victim);
        }
    }

    private boolean isHoldingPvpSword(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        return itemInHand != null && itemInHand.getType() == plugin.createPvpSword().getType() && itemInHand.getItemMeta().getDisplayName().equals(plugin.createPvpSword().getItemMeta().getDisplayName());
    }
}
