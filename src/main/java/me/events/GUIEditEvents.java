package me.events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static me.commands.ashops.shopUtilities;
import static me.relxis.AdvancedShops.plugin;
import static me.utilities.FormatUtilities.*;
import static me.utilities.ShopUtilities.*;

public class GUIEditEvents implements Listener {
    private HashMap<String,Inventory> editGUIs = plugin.getEditGUIs();
    private HashMap<Player,ItemStack> chatInputs = plugin.getChatInputs();
    private HashMap<Player,Inventory> editPrice = plugin.getEditPriceMap();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!editGUIs.containsValue(topInventory)) return;
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();
        if (currentItem == null) return;
        Player player = (Player) event.getWhoClicked();

        Sound sound = Sound.UI_BUTTON_CLICK;

        try {
            String soundString = plugin.getConfig().getString("Sound Settings.Button clicked");
            sound = Sound.valueOf(soundString);
        } catch (Exception e) {
            System.out.println("[AdvancedShops] An error was found in sound settings.");
        }

        player.playSound(player.getLocation(),sound,0.5f,1f);

        if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
            if (clickedInventory != topInventory) return;
            currentItem = event.getCurrentItem();
            chatInputs.put(player,currentItem);
            String text = color(plugin.getConfig().getString("Messages.Request price input"));
            player.sendMessage("§6[AdvancedShops] " + text);
            editPrice.put(player,topInventory);
            player.closeInventory();
        } else if (event.getAction().equals(InventoryAction.PICKUP_ALL)) {

            Inventory playerInventory = event.getWhoClicked().getInventory();
            int slot = event.getSlot();

            if (clickedInventory == topInventory) {
                if (playerInventory.firstEmpty() == -1) return;
                topInventory.setItem(slot,null);
                removeAdditionalLore(currentItem);

                playerInventory.setItem(playerInventory.firstEmpty(),currentItem);
            } else if (clickedInventory == playerInventory) {
                if (topInventory.firstEmpty() == -1) return;

                playerInventory.setItem(slot,null);
                addAdditionalLore(currentItem,0,true);
                topInventory.setItem(topInventory.firstEmpty(),currentItem);
            }

        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) throws IOException {
        Inventory topInventory = event.getView().getTopInventory();
        if (!editGUIs.containsValue(topInventory)) return;
        String shopName = event.getView().getTitle();
        shopName = deleteSubstring(shopName,"Editing: ");
        ItemStack[] itemStacks = shopUtilities.deepCloneItemStackArray(topInventory.getContents());

        shopUtilities.saveShop(shopName, itemStacks);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!chatInputs.containsKey(player)) return;
        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            String text = color(plugin.getConfig().getString("Messages.Price input cancelled"));
            player.sendMessage("§6[AdvancedShops] " + text);
            chatInputs.remove(player);
            return;
        }

        if (isNumeric(message)) {
            int price = 0;
            try {
                price = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                String exceptionMessage = color(plugin.getConfig().getString("Messages.Price input exceeds the limit"));
                player.sendMessage("§6[AdvancedShops] " + exceptionMessage);
                String text = color(plugin.getConfig().getString("Messages.Request price input"));
                player.sendMessage("§6[AdvancedShops] " + text);
                return;
            }
            if (price <= 0) {
                String invalidPriceMessage = color(plugin.getConfig().getString("Messages.Invalid price"));
                player.sendMessage("§6[AdvancedShops] " + invalidPriceMessage);
                String text = color(plugin.getConfig().getString("Messages.Request price input"));
                player.sendMessage("§6[AdvancedShops] " + text);
            } else {
                ItemStack item = chatInputs.get(player);
                setPrice(item,price);
                String text = color(plugin.getConfig().getString("Messages.Price has been set"));
                text = text.replace("{price}",convertToAccountingFormat(price));
                player.sendMessage("§6[AdvancedShops] " + text);
                chatInputs.remove(player);
                Inventory editGUI = editPrice.get(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(editGUI);
                    }
                }.runTask(plugin);
                editPrice.remove(player);
            }
        } else {
            String invalidPriceMessage = color(plugin.getConfig().getString("Messages.Invalid price"));
            player.sendMessage("§6[AdvancedShops] " + invalidPriceMessage);
            String text = color(plugin.getConfig().getString("Messages.Request price input"));
            player.sendMessage("§6[AdvancedShops] " + text);
        }

    }

    public void setPrice(ItemStack item,int price) {

        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) return;

        String pricePrefix = "§ePrice: §a";
        lore.set(lore.size() - 5,pricePrefix + price);
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

}
