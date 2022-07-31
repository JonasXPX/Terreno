package me.jonasxpx.terreno;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.jonasxpx.terreno.config.CommandConfiguration;
import me.jonasxpx.terreno.config.Configuration;
import me.jonasxpx.terreno.providers.ConfigurationProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Terreno extends JavaPlugin {

    public static WorldGuardPlugin wg = null;
    public static WorldEditPlugin we = null;
    public static Economy economy = null;

    private CommandConfiguration commandHandler;

    @Override
    public void onEnable() {
        wg = getWorldGuard();
        we = getWorldEdit();

        saveResource("pt_br.yml", false);
        getConfig().options().copyDefaults(true);

        Injector injector = new InjectorBuilder().addDefaultHandlers("me.jonasxpx.terreno").create();

        injector.register(FileConfiguration.class, getConfig());
        injector.register(Terreno.class, this);
        injector.registerProvider(Configuration.class, ConfigurationProvider.class);
        commandHandler = injector.getSingleton(CommandConfiguration.class);

        saveConfig();
        setupEconomy();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }

    public WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
    }

    public WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}