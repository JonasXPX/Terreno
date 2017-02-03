package me.jonasxpx.terreno.worldedit;

import static me.jonasxpx.terreno.Terreno.we;
import static me.jonasxpx.terreno.Terreno.wg;
import static me.jonasxpx.terreno.Tools.addTime;
import static me.jonasxpx.terreno.Tools.timeout;
import me.jonasxpx.terreno.PlayerManager;
import me.jonasxpx.terreno.Terreno;
import me.jonasxpx.terreno.error.Erros;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class Edicao {
	
	/**
	 * 
	 * Deleta uma região e a regenera.
	 * 
	 * @param player
	 */
	public static void deleteRegion(Player player) {
		if (!timeout.containsKey(player.getName())) {
			addTime(player, 10L);
			Erros.CONFIRMCOMMAND.sendToPlayer(player);
			return;
		}
		timeout.remove(player.getName());
		ApplicableRegionSet ap = wg.getRegionManager(player.getWorld())
				.getApplicableRegions(player.getLocation());
		if (ap.isOwnerOfAll(new BukkitPlayer(wg, player)) || player.isOp()) {
			try {
				if (!ap.iterator().hasNext()) {
					Erros.NOREGION.sendToPlayer(player);
					return;
				}
				ProtectedRegion pr = ap.iterator().next();
				CuboidRegionSelector rs = new CuboidRegionSelector(new BukkitWorld(player
						.getWorld()), pr.getMinimumPoint(), pr
						.getMaximumPoint());
				PlayerManager pm = player.isOp() ? new PlayerManager(pr.getOwners().getPlayers().iterator().next(), player.getWorld()) : new PlayerManager(player);
				player.sendMessage("§b» Deletando terreno...");
				regenerarTerreno(rs.getRegion());
				pm.deleteRegion(pr.getId());
				wg.getRegionManager(player.getWorld()).removeRegion(pr.getId());
				wg.getRegionManager(player.getWorld()).save();
			} catch (Exception e) {
				Erros.NULLERRO.sendToPlayer(player);
				e.printStackTrace();
			}
			Erros.SUCESSDELETE.sendToPlayer(player);
		} else{
			Erros.NOOWNERDELETE.sendToPlayer(player);
		}
	}
	
	public static void createWalls(ProtectedCuboidRegion pcr, Player player) throws MaxChangedBlocksException {
		if(!Terreno.instance.criarCercado){
			return;
		}
		BlockVector p1 = pcr.getMinimumPoint();
		BlockVector p2 = pcr.getMaximumPoint();
		p2.setY(player.getLocation().getY());
		CuboidRegion cr = new CuboidRegion(new BlockVector(p1.setY(player
				.getLocation().getY())), new BlockVector(p2.setY(player
				.getLocation().getY())));
		EditSession e = we.createEditSession(player);
		e.makeWalls(cr, new RandomFillPattern(Terreno.instance.cercado));
		e.flushQueue();
	}

	/**
	 * 
	 * Faz um regeneração de acordo com a seed do mapa na
	 * região marcada
	 * 
	 * @param region
	 */
	private static void regenerarTerreno(Region region){
		try{
			region.getWorld().regenerate(region, new EditSession(new BukkitWorld(Bukkit.getWorld(region.getWorld().getName())), -1));
		}catch(Exception e){}
	}
	
}
