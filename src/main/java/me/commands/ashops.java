package me.commands;

import me.utilities.ShopUtilities;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.util.*;

import static me.relxis.AdvancedShops.plugin;
import static me.utilities.FormatUtilities.color;
import static me.utilities.ShopUtilities.isNumeric;

public class ashops implements TabExecutor {

    public static ShopUtilities shopUtilities = new ShopUtilities();
    private List<String> emptyList = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (commandSender instanceof ConsoleCommandSender) {
            if (args[0].equalsIgnoreCase("open")) {
                if (args.length == 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null || !target.getName().equalsIgnoreCase(args[2])) {
                        String message = color(plugin.getConfig().getString("Messages.Player not found"));
                        System.out.println(("§6[AdvancedShops] " + message));
                        return false;
                    }
                    shopUtilities.openShop(null, args[1], true,target);
                } else {
                    System.out.println(shopUtilities.getInvalidMessage());
                }
                return false;
            }
        }

        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (args.length == 0) {
            if (!player.hasPermission("advancedshops.help")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            shopUtilities.help(player);return false;
        } else if (args[0].equalsIgnoreCase("help")) {
            if (!player.hasPermission("advancedshops.help")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            shopUtilities.help(player);return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!player.hasPermission("advancedshops.admin")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }

            if (args.length == 3) {
                Inventory shop;

                if (!isNumeric(args[2]) || args[2].length() > 2) {
                    String message = color(plugin.getConfig().getString("Messages.Invalid size input"));
                    player.sendMessage("§6[AdvancedShops] " + message);
                    return false;
                }

                try {
                    shop = shopUtilities.createShop(args[1],Integer.parseInt(args[2]),player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (shop != null) {
                    String message = color(plugin.getConfig().getString("Messages.Shop has been created"));
                    message = message.replace("{shopName}",args[1]);
                    player.sendMessage("§6[AdvancedShops] " + message);
                }

                return false;
            } else if (args.length == 2) {
                String message = color(plugin.getConfig().getString("Messages.Shop size not specified"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            } else {
                player.sendMessage(shopUtilities.getInvalidMessage());
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("open")) {
            if (args.length == 2) {
                if (!player.hasPermission("advancedshops.open.*")) {
                    if (!player.hasPermission("advancedshops.open." + args[1])) {
                        String message = color(plugin.getConfig().getString("Messages.No permissions"));
                        player.sendMessage("§6[AdvancedShops] " + message);
                        player.sendMessage("§cPermission required: " + "§eadvancedshops.open." + args[1]);
                        return false;
                    }
                }
                shopUtilities.openShop(player, args[1], true,player);
                return false;
            } else if (args.length == 3) {
                if (!player.hasPermission("advancedshops.admin")) {
                    String message = color(plugin.getConfig().getString("Messages.No permissions"));
                    player.sendMessage("§6[AdvancedShops] " + message);
                    return false;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null || !target.getName().equalsIgnoreCase(args[2])) {
                    String message = color(plugin.getConfig().getString("Messages.Player not found"));
                    player.sendMessage(("§6[AdvancedShops] " + message));
                    return false;
                }
                shopUtilities.openShop(player, args[1], true,target);
                return false;
            } else {
                player.sendMessage(shopUtilities.getInvalidMessage());
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("edit")) {
            if (!player.hasPermission("advancedshops.admin")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            if (args.length == 2) {
                if (!plugin.getEditGUIs().containsKey(args[1])) {
                    shopUtilities.createEditGUI(player, args[1]);
                } else {
                    Inventory editGUI = plugin.getEditGUIs().get(args[1]);
                    player.openInventory(editGUI);
                }
            } else {
                player.sendMessage(shopUtilities.getInvalidMessage());
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("advancedshops.admin")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            if (args.length == 2) {
                if (plugin.getShopConfig().getString(args[1]) != null) {
                    plugin.getEditGUIs().remove(args[1]);
                    plugin.getShopConfig().set(args[1],null);
                    String message = color(plugin.getConfig().getString("Messages.Shop has been deleted"));
                    message = message.replace("{shopName}",args[1]);
                    player.sendMessage("§6[AdvancedShops] " + message);
                    try {
                        plugin.saveShopConfig();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String message = color(plugin.getConfig().getString("Messages.Shop does not exist"));
                    player.sendMessage("§6[AdvancedShops] " + message);
                }
            } else {
                player.sendMessage(shopUtilities.getInvalidMessage());
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("advancedshops.list")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            Set<String> existingShops = plugin.getShopConfig().getKeys(false);
            String title = color(plugin.getConfig().getString("Messages.Shop list title"));
            player.sendMessage("§6[AdvancedShops] " + title);
            for (String shop : existingShops) {
                String message = color(plugin.getConfig().getString("Formats.Shops in list"));
                int size = plugin.getShopConfig().getInt(shop + ".Size");
                message = message.replace("{shopName}",shop).replace("{size}",String.valueOf(size));
                player.sendMessage(message);
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("advancedshops.admin")) {
                String message = color(plugin.getConfig().getString("Messages.No permissions"));
                player.sendMessage("§6[AdvancedShops] " + message);
                return false;
            }
            for (Player individual : Bukkit.getOnlinePlayers()) {
                individual.closeInventory();
            }
            plugin.reloadConfig();
            player.sendMessage("§6[AdvancedShops] " + "§aConfiguration reloaded.");
            return false;
        }

        player.sendMessage(shopUtilities.getInvalidMessage());
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 1) {
            String[] strings = {"create","open","edit","delete","list","reload","help"};
            return Arrays.asList(strings);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("edit")) {
                return new ArrayList<>(plugin.getShopConfig().getKeys(false));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("open")) {
                return null;
            } else if (args[0].equalsIgnoreCase("create")) {
                String[] strings = {"9","18","27","36","45","54"};
                return Arrays.asList(strings);
            }
        }

        return emptyList;
    }
}
