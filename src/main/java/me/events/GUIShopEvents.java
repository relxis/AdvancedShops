package me.events;

import me.utilities.ShopUtilities;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.relxis.AdvancedShops.*;
import static me.utilities.FormatUtilities.color;
import static me.utilities.ShopUtilities.deepCloneItemStack;

public class GUIShopEvents implements Listener {
    private HashMap<UUID,Long> coolDown = plugin.getCoolDown();
    private List<Inventory> shops = plugin.getShops();
    private ShopUtilities shopUtilities = new ShopUtilities();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!shops.contains(topInventory)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (!coolDown.containsKey(player.getUniqueId())) {
            coolDown.put(player.getUniqueId(),System.currentTimeMillis());
        } else {
            long timeElapsed = System.currentTimeMillis() - coolDown.get(player.getUniqueId());
            if (timeElapsed < 500) return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        if (event.getClickedInventory() != topInventory) return;

        String shopName = event.getView().getTitle();
        int slot = event.getSlot();
        int price = getPrice(shopName,slot);

        Sound sound = Sound.UI_BUTTON_CLICK;

        try {
            String soundString = plugin.getConfig().getString("Sound Settings.Button clicked");
            sound = Sound.valueOf(soundString);
        } catch (Exception e) {
            System.out.println("[AdvancedShops] An error was found in sound settings.");
        }

        player.playSound(player.getLocation(),sound,0.5f,1f);

        if (!isConfirmButton(item)) {
            for (ItemStack itemStack : topInventory.getContents()) {
                if (isConfirmButton(itemStack)) {
                    shopUtilities.openShop(player,shopName,false,player);
                    return;
                }
            }
            ItemStack confirmButton = getConfirmButton(item);
            topInventory.setItem(slot, confirmButton);
            return;
        }

        double playerBalance = getEconomy().getBalance(player);

        if (playerBalance < price) {
            String message = color(plugin.getConfig().getString("Messages.Insufficient funds"));
            player.sendMessage("§6[AdvancedShops] " + message);
            shopUtilities.openShop(player,shopName,false,player);
            return;
        } else if (player.getInventory().firstEmpty() == -1) {
            String message = color(plugin.getConfig().getString("Messages.Inventory full"));
            player.sendMessage("§6[AdvancedShops] " + message);
            shopUtilities.openShop(player,shopName,false,player);
            return;
        }

        getEconomy().withdrawPlayer(player,price);
        ItemStack itemToBeSold = plugin.getShopConfig().getItemStack(shopName + ".Items." + slot + ".Item");
        player.getInventory().addItem(itemToBeSold);
        shopUtilities.openShop(player,shopName,false,player);
        String itemName = itemToBeSold.getItemMeta().getDisplayName();
        if (itemName.isEmpty()) {
            itemName = itemToBeSold.getType().name();
        }

        int amount = itemToBeSold.getAmount();

        String message = color(plugin.getConfig().getString("Messages.Item purchased"));

        message = message.replace("{amount}",String.valueOf(amount)).replace("{itemName}",itemName)
                .replace("{price}",String.valueOf(price));

        player.sendMessage("§6[AdvancedShops] " + message);

        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

        try {
            String soundString = plugin.getConfig().getString("Sound Settings.Item purchased");
            sound = Sound.valueOf(soundString);
        } catch (Exception e) {
            System.out.println("[AdvancedShops] An error was found in sound settings.");
        }

        player.playSound(player.getLocation(),sound,0.5f,1f);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!shops.contains(topInventory)) return;
        shops.remove(topInventory);
    }

    public int getPrice(String shopName,int slot) {
        FileConfiguration shopConfig = plugin.getShopConfig();;
        return shopConfig.getInt(shopName + ".Items." + slot + ".Price");
    }

    public ItemStack getConfirmButton(ItemStack itemStack) {
        ItemStack confirmButton = deepCloneItemStack(itemStack);
        confirmButton.setAmount(1);

        ItemMeta meta = confirmButton.getItemMeta();
        if (meta == null) return null;
        List<String> lore = meta.getLore();
        if (lore == null) return null;

        String confirmString = color(plugin.getConfig().getString("Formats.Confirm purchase"));
        lore.add(confirmString);

        meta.setLore(lore);
        String buttonName = color(plugin.getConfig().getString("Formats.Confirm button name"));
        meta.setDisplayName(buttonName);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING,"CONFIRM_BUTTON");

        confirmButton.setType(Material.LIME_CONCRETE);
        confirmButton.setItemMeta(meta);

        return confirmButton;
    }

    public boolean isConfirmButton(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.getPersistentDataContainer().has(key,PersistentDataType.STRING)) return false;
        String string = meta.getPersistentDataContainer().get(key,PersistentDataType.STRING);
        if (string == null) return false;
        return string.equals("CONFIRM_BUTTON");
    }

}
