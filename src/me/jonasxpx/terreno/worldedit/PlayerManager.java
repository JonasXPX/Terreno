package me.jonasxpx.terreno.worldedit;

import me.jonasxpx.terreno.Tools;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PlayerManager {

	private final FileConfiguration file;
	private final File local;
	private final Tools tools;
	private static final String REGION_PATH = "plugins/Terreno/regions/";
	private static final String TERRENOS_KEY = "Terrenos";

	public PlayerManager(Player player, Tools tools) {
		this(player.getName(), player.getWorld(), tools);
	}

	public PlayerManager(final String player, final World worlds, final Tools tools) {
		this.tools = tools;
		try {
			final String world = worlds.getName().toLowerCase();
			final char firstChar = player.toLowerCase().charAt(0);
			local = new File(String.format("%s%s%s%s%s%s.dat", REGION_PATH, world, File.separator, firstChar, File.separator, player.toLowerCase()));
			if (!local.exists()) {
				final File file = new File(String.format("%s%s%s%s%s", REGION_PATH, world, File.separator, firstChar, File.separator));
				if (!file.exists()) {
					file.mkdirs();
				}

				local.createNewFile();
			}
			this.file = YamlConfiguration.loadConfiguration(local);
		} catch (IOException e) {
			throw new RuntimeException("Failed to handler offline player managment");
		}
	}

	public void addNewRegion(String name) {
		List<String> list = getRegions();
		list.add(name.toLowerCase());
		file.set(TERRENOS_KEY, list.toArray());
		try {
			file.save(local);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getRegions() {
		return file.getStringList(TERRENOS_KEY);
	}

	public void setRegions(List<? extends String> regions) {
		file.set(TERRENOS_KEY, regions.toArray());
		try {
			file.save(local);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteRegion(String ID) {
		List<String> list = getRegions();
		list.remove(ID.toLowerCase());
		file.set(TERRENOS_KEY, list.toArray());
		try {
			file.save(local);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean containsRegion(String name) {
		if (!file.contains(TERRENOS_KEY)) {
			return false;
		}
		return getRegions().contains(name.toLowerCase());
	}

	public int getAmount() {
		if (!file.contains(TERRENOS_KEY))
			return 0;
		return getRegions().size();
	}

	public int forNextInt(Player p) {
		for (int x = 1; x <= 500; x++) {
			if (!containsRegion(p.getName().toLowerCase() + "-" + x)
					&& !tools.contains(p.getWorld(), p.getName() + "-" + x)) {
				return x;
			}
		}
		return 0;
	}
}
