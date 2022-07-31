package me.jonasxpx.terreno.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class TimeService {

	private static final Map<UUID, Long> timeout = new HashMap<>();
	
	public static void addTime(final Player player, final Long time) {
		timeout.put(player.getUniqueId(), (System.currentTimeMillis() / 1000) + time);
	}

	public static void remove(final Player player) {
		timeout.remove(player.getUniqueId());
	}
	
	public static boolean contains(final Player player) {
		return timeout.containsKey(player.getUniqueId());
	}
}
