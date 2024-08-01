package org.inoltrazione.hubpvp;


import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final HubPvP plugin;

    public InventoryListener(HubPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();


        if (isPvPItem(currentItem) || isPvPItem(cursorItem)) {
            event.setCancelled(true);
        }


        if (event.getHotbarButton() != -1) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (isPvPItem(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (isPvPItem(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isPvPItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    private boolean isPvPItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();
        return displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sword-settings.sword-name"))) ||
                displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("armor-settings.helmet-name"))) ||
                displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("armor-settings.chestplate-name"))) ||
                displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("armor-settings.leggings-name"))) ||
                displayName.equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("armor-settings.boots-name")));
    }
}
