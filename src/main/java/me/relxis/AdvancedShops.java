package me.relxis;

import me.commands.ashops;
import me.events.GUIShopEvents;
import me.events.GUIEditEvents;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class AdvancedShops extends JavaPlugin {
    public static AdvancedShops plugin;
    public static NamespacedKey key;
    private File shopConfigFile;
    private FileConfiguration shopConfig;
    private HashMap<String,Inventory> editGUIs = new HashMap<>();
    private HashMap<Player,ItemStack> chatInputs = new HashMap<>();
    private HashMap<Player,Inventory> editPriceMap = new HashMap<>();
    private HashMap<UUID,Long> coolDown = new HashMap<>();
    private List<Inventory> shops = new ArrayList<>();
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        for (Player individual : Bukkit.getOnlinePlayers()) {
            individual.closeInventory();
        }

        saveDefaultConfig();
        getConfig().options().copyDefaults();

        plugin = this;

        key = new NamespacedKey(this,"AdvancedShops");
        createShopConfig();
        getCommand("advancedshops").setExecutor(new ashops());
        getServer().getPluginManager().registerEvents(new GUIShopEvents(),this);
        getServer().getPluginManager().registerEvents(new GUIEditEvents(),this);

    }

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    public HashMap<String, Inventory> getEditGUIs() {
        return editGUIs;
    }

    public FileConfiguration getShopConfig() {
        return this.shopConfig;
    }

    public List<Inventory> getShops() {
        return shops;
    }

    public void saveShopConfig() throws IOException {
        shopConfig.save(shopConfigFile);
    }

    private void createShopConfig() {
        shopConfigFile = new File(getDataFolder(), "shops.yml");
        if (!shopConfigFile.exists()) {
            shopConfigFile.getParentFile().mkdirs();
            saveResource("shops.yml", false);
        }

        shopConfig = new YamlConfiguration();
        try {
            shopConfig.load(shopConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Player, Inventory> getEditPriceMap() {return editPriceMap;}
    public HashMap<Player, ItemStack> getChatInputs() {
        return chatInputs;
    }
    public HashMap<UUID, Long> getCoolDown() {
        return coolDown;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }


    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(command.getLabel().equals("test-permission")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(perms.has(player, "example.plugin.awesome")) {
                sender.sendMessage("You are awesome!");
            } else {
                sender.sendMessage("You suck!");
            }
            return true;
        } else {
            return false;
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }


}
