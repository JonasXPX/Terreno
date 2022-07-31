package me.jonasxpx.terreno.utils;

import me.jonasxpx.terreno.config.Configuration;
import me.jonasxpx.terreno.error.Erros;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendMessage(final String message, final Player player) {
        final String finalMessage = message.replaceAll("\\$([\\w])", ChatColor.COLOR_CHAR + "$1");
        player.sendMessage(finalMessage);
    }

    public static void sendToPlayer(final Player player, final Erros erros, Configuration configuration){
        player.sendMessage(configuration.getLang().get(erros.name()));
    }
}
