package me.jonasxpx.terreno.providers;

import me.jonasxpx.terreno.config.Configuration;
import me.jonasxpx.terreno.config.ConfigurationImpl;
import org.bukkit.configuration.file.FileConfiguration;

import javax.inject.Inject;
import javax.inject.Provider;

public class ConfigurationProvider implements Provider<Configuration> {

    @Inject
    private FileConfiguration fileConfiguration;

    @Override
    public Configuration get() {
        final ConfigurationImpl configuration = new ConfigurationImpl(fileConfiguration);
        configuration.initConfig();
        return configuration;
    }
}
