package me.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.commands.ashops.shopUtilities;
import static me.relxis.AdvancedShops.plugin;
import static me.utilities.FormatUtilities.*;

public class ShopUtilities {
    private FileConfiguration shopConfig = plugin.getShopConfig();

    public void help(Player player) {


        player.sendMessage("§6[AdvancedShops] §aversion 1.0.0");
        player.sendMessage("§6- §e/ashops help §a- Open the help page.");
        player.sendMessage("§6- §e/ashops create <shopName> <size> §a- Create a shop with the specified name.");
        player.sendMessage("§6- §e/ashops open <shopName> §a- Open an existing shop.");
        player.sendMessage("§6- §e/ashops open <shopName> <player> §a- Open an existing shop for the specified player.");
        player.sendMessage("§6- §e/ashops edit <shopName> §a- Edit the contents of an existing shop.");
        player.sendMessage("§6- §e/ashops delete <shopName> §a- Delete an existing shop.");
        player.sendMessage("§6- §e/ashops list §a- List all existing shops.");
    }

    public String getInvalidMessage() {
        String message = color(plugin.getConfig().getString("Messages.Invalid arguments"));
        return "§6[AdvancedShops] " + message;
    }

    public Inventory createShop(String shopName,int size,Player owner) throws IOException {

        if (size % 9 != 0 || size > 54 || size < 9) {
            String message = color(plugin.getConfig().getString("Messages.Invalid size input"));
            owner.sendMessage("§6[AdvancedShops] " + message);
            return null;
        }

        Set<String> existingShops = shopConfig.getKeys(false);

        if (existingShops.contains(shopName)) {
            String message = color(plugin.getConfig().getString("Messages.Shop already exists"));
            owner.sendMessage("§6[AdvancedShops] " + message);
            return null;
        }

        if (shopName.length() > 15) {
            String message = color(plugin.getConfig().getString("Messages.Shop name exceeds the length limit"));
            owner.sendMessage("§6[AdvancedShops] " + message);
            return null;
        }

        Inventory shop = Bukkit.createInventory(owner,size,shopName);
        shopConfig.set(shopName,null);
        saveShop(shopName,shop.getContents());
        return shop;
    }

    public void saveShop(String shopName,ItemStack[] contents) throws IOException {

        for (int i = 0;i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null) {
                item = new ItemStack(Material.AIR);
            }

            int price = 0;

            if (item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;
                List<String> lore = meta.getLore();
                if (lore == null) continue;
                String priceString = lore.get(lore.size() - 5);
                price = Integer.parseInt(deleteSubstring(priceString,"§ePrice: §a"));
            }

            shopUtilities.removeAdditionalLore(item);

            shopConfig.set(shopName + ".Size",contents.length);
            shopConfig.set(shopName + ".Items." + i + ".Price",price);
            shopConfig.set(shopName + ".Items." + i + ".Item",item);
        }
        plugin.saveShopConfig();
    }

    public void openShop(Player player,String shopName,boolean viaCommand,Player target) {
        if (shopConfig.getString(shopName) == null) {
            String message = color(plugin.getConfig().getString("Messages.Shop does not exist"));
            player.sendMessage("§6[AdvancedShops] " + message);
            return;
        }

        int size = shopConfig.getInt(shopName + ".Size");
        Inventory shop = Bukkit.createInventory(player,size,shopName);

        Set<String> slots = shopConfig.getConfigurationSection(shopName + ".Items").getKeys(false);

        for (String slot : slots) {
            ItemStack originalItem = shopConfig.getItemStack(shopName + ".Items." + slot + ".Item");
            ItemStack item = deepCloneItemStack(originalItem);

            int intSlot = Integer.parseInt(slot);
            int price = shopConfig.getInt(shopName + ".Items." + slot + ".Price");
            addAdditionalLore(item,price,false);

            shop.setItem(intSlot,item);
        }

        plugin.getShops().add(shop);
        target.openInventory(shop);

        if (viaCommand) {
            Sound sound = Sound.BLOCK_NOTE_BLOCK_PLING;

            try {
                String soundString = plugin.getConfig().getString("Sound Settings.Open shop");
                sound = Sound.valueOf(soundString);
            } catch (Exception e) {
                System.out.println("[AdvancedShops] An error was found in sound settings");
            }

            target.playSound(target.getLocation(), sound, 0.5f, 1f);
        }
    }

    public void createEditGUI(Player player, String shopName) {
        if (shopConfig.getString(shopName) == null) {
            String message = color(plugin.getConfig().getString("Messages.Shop does not exist"));
            player.sendMessage("§6[AdvancedShops] " + message);
            return;
        }

        int size = shopConfig.getInt(shopName + ".Size");
        Inventory editGUI = Bukkit.createInventory(player,size,"Editing: " + shopName);

        Set<String> slots = shopConfig.getConfigurationSection(shopName + ".Items").getKeys(false);

        for (String slot : slots) {
            ItemStack originalItem = shopConfig.getItemStack(shopName + ".Items." + slot + ".Item");
            ItemStack item = deepCloneItemStack(originalItem);

            int intSlot = Integer.parseInt(slot);
            int price = shopConfig.getInt(shopName + ".Items." + slot + ".Price");
            if (price == -1) price = 0;
            addAdditionalLore(item,price,true);

            editGUI.setItem(intSlot,item);
        }

        plugin.getEditGUIs().put(shopName,editGUI);

        player.openInventory(editGUI);

        Sound sound = Sound.BLOCK_NOTE_BLOCK_PLING;

        try {
            String soundString = plugin.getConfig().getString("Sound Settings.Open shop");
            sound = Sound.valueOf(soundString);
        } catch (Exception e) {
            System.out.println("[AdvancedShops] An error was found in sound settings");
        }

        player.playSound(player.getLocation(), sound, 0.5f, 1f);
    }

    public static void addAdditionalLore(ItemStack item,int price,boolean bool) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String pricePrefix = "§ePrice: §a";
        String amountPrefix = "§eAmount: §a";

        if (bool) {
            lore.add("§7--------------------------");
            lore.add(pricePrefix + price);
            lore.add(amountPrefix + item.getAmount());
            lore.add("§7--------------------------");
            lore.add("§eRight click to edit the price.");
            lore.add("§eLeft click to remove the item.");
        } else {
            List<String> itemLore = plugin.getConfig().getStringList("Formats.Item lore");
            String priceString = convertToAccountingFormat(price);
            itemLore.replaceAll(s -> color(s.replace("{price}",priceString).replace("{amount}",String.valueOf(item.getAmount()))));
            lore.addAll(itemLore);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

    }

    public static void removeAdditionalLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) return;

        int lastIndex = lore.size() - 1;
        for (int i = 0; i < 6; i++) {
            lore.remove(lastIndex - i);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static ItemStack deepCloneItemStack(ItemStack original) {
        if (original == null) return null;

        ItemStack newItem = new ItemStack(original);

        ItemMeta originalMeta = original.getItemMeta();
        if (originalMeta != null) {
            ItemMeta newMeta = originalMeta.clone();
            newItem.setItemMeta(newMeta);
        }

        return newItem;
    }

    public static ItemStack[] deepCloneItemStackArray(ItemStack[] original) {
        if (original == null) return null;

        ItemStack[] newContents = new ItemStack[original.length];

        for (int i = 0; i < original.length; i++) {
            ItemStack originalItem = original[i];
            ItemStack newItem = (originalItem != null) ? new ItemStack(originalItem) : null;
            newContents[i] = deepCloneItemStack(newItem);
        }

        return newContents;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }

}
