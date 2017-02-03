package me.jonasxpx.terreno;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerManager {

	private final FileConfiguration file;
	private File local;
	
	/*
	 * ONLINE PLAYER;
	 */
	public PlayerManager(Player player)  {
		String world = player.getWorld().getName().toLowerCase();
		local = new File("plugins/Terreno/regions/"+world+"/" + player.getName().toLowerCase().charAt(0) + "/" + player.getName().toLowerCase() + ".dat");
		if(!local.exists()){
			new File("plugins/Terreno/regions/"+world+"/" + player.getName().toLowerCase().charAt(0) + "/").mkdirs();
			try {
				local.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.file = YamlConfiguration.loadConfiguration(local);
	}
	
	/*
	 * OFFLINE PLAYER.
	 */
	public PlayerManager(String player, World worlds)  {
		String world = worlds.getName().toLowerCase();
		local = new File("plugins/Terreno/regions/"+world+"/" + player.toLowerCase().charAt(0) + "/" + player.toLowerCase() + ".dat");
		if(!local.exists()){
			new File("plugins/Terreno/regions/"+world+"/" + player.toLowerCase().charAt(0) + "/").mkdirs();
			try {
				local.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.file = YamlConfiguration.loadConfiguration(local);
	}
	
	public void addNewRegion(String name){
		List<String> list = getRegions();
		list.add(name.toLowerCase());
		file.set("Terrenos", list.toArray());
		try {
			file.save(local);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public List<String> getRegions(){
		List<String> list = file.getStringList("Terrenos");
		return list;
	}
	public void setRegions(List<? extends String> regions){
		file.set("Terrenos", regions.toArray());
		try{
			file.save(local);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void deleteRegion(String ID){
		List<String> list = getRegions();
		list.remove(ID.toLowerCase());
		file.set("Terrenos", list.toArray());
		try {
			file.save(local);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean containsRegion(String name){
		if(!file.contains("Terrenos")){
			return false;
		}
		return getRegions().contains(name.toLowerCase());
	}
	
	public int getAmount(){
		if(!file.contains("Terrenos"))
			return 0;
		return getRegions().size();
	}
	

	public int forNextInt(Player p){
		for(int x=1; x <= 500; x++){
			if(!containsRegion(p.getName().toLowerCase() + "-" +Integer.toString(x)) && !Tools.contains(p.getWorld(), p.getName() + "-" + Integer.toString(x))){
				return x;
			}
		}
		return 0;
	}
}
