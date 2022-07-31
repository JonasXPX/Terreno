package me.jonasxpx.terreno.worldedit;

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
import me.jonasxpx.terreno.Tools;
import me.jonasxpx.terreno.config.Configuration;
import me.jonasxpx.terreno.config.TimeService;
import me.jonasxpx.terreno.error.Erros;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.logging.Logger;

import static me.jonasxpx.terreno.Terreno.we;
import static me.jonasxpx.terreno.Terreno.wg;
import static me.jonasxpx.terreno.utils.MessageUtils.sendToPlayer;
import static org.bukkit.ChatColor.AQUA;

public class Edicao {

	private final Configuration configuration;

	@Inject
	public Edicao(Configuration configuration) {
		this.configuration = configuration;
	}

	public void deleteRegion(final Player player, final Tools tools) {
		if (!TimeService.contains(player)) {
			TimeService.addTime(player, 10L);
			sendToPlayer(player, Erros.CONFIRMCOMMAND, configuration);
			return;
		}

		TimeService.remove(player);

		final ApplicableRegionSet ap = wg.getRegionManager(player.getWorld())
				.getApplicableRegions(player.getLocation());
		final BukkitPlayer bukkitPlayer = new BukkitPlayer(wg, player);

		if (!ap.isOwnerOfAll(bukkitPlayer) || !player.isOp()) {
			sendToPlayer(player, Erros.NOOWNERDELETE, configuration);
			return;
		}

		try {
			if (!ap.iterator().hasNext()) {
				sendToPlayer(player, Erros.NOREGION, configuration);
				return;
			}
			final ProtectedRegion pr = ap.iterator().next();
			final CuboidRegionSelector rs = new CuboidRegionSelector(new BukkitWorld(player.getWorld()), pr.getMinimumPoint(),
					pr.getMaximumPoint());

			final PlayerManager playerManager = player.isOp()
					? new PlayerManager(pr.getOwners().getPlayers().iterator().next(), player.getWorld(), tools)
					: new PlayerManager(player, tools);

			player.sendMessage(AQUA + "Deletando terreno...");
			regenerarTerreno(rs.getRegion());
			playerManager.deleteRegion(pr.getId());
			wg.getRegionManager(player.getWorld()).removeRegion(pr.getId());
			wg.getRegionManager(player.getWorld()).save();
		} catch (Exception e) {
			sendToPlayer(player, Erros.NULLERRO, configuration);
			e.printStackTrace();
		}
		sendToPlayer(player, Erros.SUCESSDELETE, configuration);
	}

	public void createWalls(final ProtectedCuboidRegion pcr, final Player player) throws MaxChangedBlocksException {
		if (!configuration.isCriarCercado()) {
			return;
		}
		final BlockVector blockVectorX = pcr.getMinimumPoint();
		final BlockVector blockVectorY = pcr.getMaximumPoint();

		blockVectorY.setY(player.getLocation().getY());

		final CuboidRegion cr = new CuboidRegion(new BlockVector(blockVectorX.setY(player.getLocation().getY())),
				new BlockVector(blockVectorY.setY(player.getLocation().getY())));
		final EditSession e = we.createEditSession(player);
		e.makeWalls(cr, new RandomFillPattern(configuration.getCercado()));
		e.flushQueue();
	}

	private void regenerarTerreno(final Region region) {
		try {
			region.getWorld().regenerate(region,
					new EditSession(new BukkitWorld(Bukkit.getWorld(region.getWorld().getName())), -1));
		} catch (Exception e) {
			Logger.getGlobal().warning("failed to restore the region");
		}
	}

}
