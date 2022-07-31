package me.jonasxpx.terreno;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.jonasxpx.terreno.config.Configuration;
import me.jonasxpx.terreno.data.Price;
import me.jonasxpx.terreno.enums.Bloqueaveis;
import me.jonasxpx.terreno.enums.TiposTerrenos;
import me.jonasxpx.terreno.error.Erros;
import me.jonasxpx.terreno.worldedit.Edicao;
import me.jonasxpx.terreno.worldedit.PlayerManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static me.jonasxpx.terreno.utils.MessageUtils.sendMessage;
import static me.jonasxpx.terreno.utils.MessageUtils.sendToPlayer;

public class Tools {

	@Inject
	private Configuration configuration;

	@Inject
	private Edicao edicao;

	public void createRegionForPlayer(final Player player, final TiposTerrenos terreno) {
		final Double price = configuration.getPriceByType(terreno);
		if (!hasMoney(player, price)) {
			sendToPlayer(player, Erros.NOMONEY, configuration);
			return;
		}

		final PlayerManager playerManager = new PlayerManager(player, this);
		if (playerManager.getAmount() >= getPerm(player)) {
			sendToPlayer(player, Erros.LIMITREGION, configuration);
			return;
		}

		final Integer size = configuration.getSizeByType(terreno);
		final String regionName = (player.getName() + "-" + playerManager.forNextInt(player)).toLowerCase();
		final RegionManager regionManager = Terreno.wg.getRegionManager(player.getWorld());
		final BlockVector blockVectorX = toVector(point1(player.getLocation(), size));
		final BlockVector blockVectorY = toVector(point2(player.getLocation(), size));
		final ProtectedCuboidRegion cuboidRegion = new ProtectedCuboidRegion(regionName, blockVectorX, blockVectorY);

		if (!checkRegionNear(regionManager, cuboidRegion)) {
			sendToPlayer(player, Erros.NEARREGION, configuration);
			return;
		}

		final DefaultDomain defaultDomain = new DefaultDomain();
		defaultDomain.addPlayer(new BukkitPlayer(Terreno.wg, player));
		
		cuboidRegion.setOwners(defaultDomain);
		cuboidRegion.setPriority(5);
		cuboidRegion.setFlag(DefaultFlag.USE, State.DENY);
		regionManager.addRegion(cuboidRegion);
		playerManager.addNewRegion(regionName);
		
		if (configuration.getSchematic().containsKey(player.getWorld().getName())) {
			Map<TiposTerrenos, String> sch = configuration.getSchematic().get(player.getWorld().getName());
			if (sch.containsKey(terreno)) {
				SchematicManager sm = new SchematicManager(
						Vector.getMidpoint(regionManager.getRegion(regionName).getMinimumPoint(),
								regionManager.getRegion(regionName).getMaximumPoint()).setY(player.getLocation().getBlockY()),
						sch.get(terreno));
				try {
					sm.loadSchematic(new BukkitWorld(player.getWorld()));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

		try {
			regionManager.save();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		final double valor = multiplicarValor(player.getWorld().getName().toLowerCase(), price);
		withdraw(player, valor);
		sendMessage("$bTerreno $e" + terreno.name() + "$b adiquirido, por $e"
				+ NumberFormat.getInstance().format(valor) + "$b Coins", player);
		try {
			edicao.createWalls(cuboidRegion, player);
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	}

	public ProtectedRegion getRegion(Location loc) {
		Iterator<ProtectedRegion> pr = Terreno.wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc).iterator();
		if (!pr.hasNext())
			return null;
		return pr.next();
	}

	public boolean checkRegionNear(RegionManager r, ProtectedCuboidRegion pcr) {
		try {
			if (pcr.getIntersectingRegions(Lists.newArrayList(r.getRegions().values())).isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void transferirDono(Player owner, Player buyer, ProtectedRegion pr) {
		pr.getOwners().removePlayer(owner.getName());
		pr.setMembers(new DefaultDomain());
		pr.getOwners().addPlayer(buyer.getName());
		PlayerManager pm = new PlayerManager(owner, this);
		pm.deleteRegion(pr.getId());
		pm = new PlayerManager(buyer, this);
		pm.addNewRegion(pr.getId());
		try {
			Terreno.wg.getRegionManager(buyer.getWorld()).save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
		}
	}

	public boolean checkRegionNear(Player player, int area) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		Location loc1 = point1(player.getLocation(), area);
		Location loc2 = point2(player.getLocation(), area);
		return rm.getApplicableRegionsIDs(toVector(loc1)).isEmpty()
				&& rm.getApplicableRegionsIDs(toVector(loc2)).isEmpty();
	}

	public String getInfo(Location loc) {
		RegionManager rm = Terreno.wg.getRegionManager(loc.getWorld());
		Iterable<ProtectedRegion> ap = rm.getApplicableRegions(loc);
		StringBuilder sb = new StringBuilder();
		for (ProtectedRegion s : ap) {
			sb.append("$b$3[EndCraft]$b Informaçães sobre o terreno atual:");
			sb.append("\n");
			sb.append("$bDono: $e").append(s.getOwners().getPlayers());
			sb.append("\n");
			if (s.getMembers().size() > 0) {
				sb.append("$bAmigos: $e").append(s.getMembers().getPlayers());
				sb.append("\n");
			}
			sb.append("$bPVP: $e").append(formatFlag(s.getFlag(DefaultFlag.PVP)));
			sb.append("\n");
			sb.append("$bEntrada: $e").append(formatFlag(s.getFlag(DefaultFlag.ENTRY)));
			sb.append("\n");
			if (s.getFlag(DefaultFlag.BLOCKED_CMDS) != null) {
				sb.append("$bComandos bloqueados: $e").append(s.getFlag(DefaultFlag.BLOCKED_CMDS));
			}

		}
		return sb.toString();
	}

	private Location point1(Location loc, int blocos) {
		loc.setY(0);
		return loc.add(-blocos, 0, blocos);
	}

	private Location point2(Location loc, int blocos) {
		loc.setY(256);
		return loc.add(blocos, 0, -blocos);
	}

	private BlockVector toVector(Location loc) {
		return new BlockVector(loc.getX(), loc.getY(), loc.getZ());
	}

	public void teleportPlayerToRegion(Player player, int regionId) {
		RegionManager ap = Terreno.wg.getRegionManager(player.getWorld());
		PlayerManager pm = new PlayerManager(player, this);
		if (regionId >= pm.getRegions().size()) {
			sendToPlayer(player, Erros.NOREGION, configuration);
			return;
		}
		if (ap.hasRegion(pm.getRegions().get(regionId))) {
			ProtectedRegion pr = ap.getRegion(pm.getRegions().get(regionId));
			player.teleport(new Location(
					player.getWorld(), pr.getMaximumPoint().getX() + 1, player.getWorld()
							.getHighestBlockYAt(pr.getMaximumPoint().getBlockX(), pr.getMaximumPoint().getBlockZ()),
					pr.getMaximumPoint().getZ() + 1));
		} else {
			sendMessage("$cTerreno não encontrado.", player);
		}
	}

	public void teleportPlayerToRegion(Player onlinePlayer, String offlinePlayer, String regionId) {
		if (!onlinePlayer.isOp()) {
			return;
		}
		RegionManager ap = Terreno.wg.getRegionManager(onlinePlayer.getWorld());
		if (ap.hasRegion(offlinePlayer + "-" + regionId)) {
			ProtectedRegion pr = ap.getRegion(offlinePlayer + "-" + regionId);
			onlinePlayer.teleport(new Location(
					onlinePlayer.getWorld(), pr.getMaximumPoint().getX() + 1, onlinePlayer.getWorld()
							.getHighestBlockYAt(pr.getMaximumPoint().getBlockX(), pr.getMaximumPoint().getBlockZ()),
					pr.getMaximumPoint().getZ() + 1));
		}
	}

	public void blockedCommands(Player player, Bloqueaveis lockable) {
		final RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		final ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		final Price price = configuration.getLockablePricesByType(lockable);
		final String lockableName = configuration.getLockableNameByType(lockable);

		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (ap.size() == 0 || !ap.iterator().hasNext()) {
				sendMessage("$bNenhum terreno encontrado nesta posição", player);
				return;
			}
			if (!hasMoney(player, price.getDesactive())) {
				sendMessage("$cVocê não tem dinheiro para isso!.", player);
				return;
			}
			if (lockable == Bloqueaveis.PVP) {
				ap.iterator().next().setFlag(DefaultFlag.PVP, State.DENY);
				sendMessage("$bPVP removido com sucesso!.", player);
				withdraw(player, price.getDesactive());
				return;
			}
			if (lockable == Bloqueaveis.ENTRY) {
				ap.iterator().next().setFlag(DefaultFlag.ENTRY, State.DENY);
				sendMessage("$bEntrada removida com sucesso!.", player);
				withdraw(player, price.getActive());
				return;
			}
			Set<String> at = null;
			if (ap.getFlag(DefaultFlag.BLOCKED_CMDS) != null) {
				if (ap.getFlag(DefaultFlag.BLOCKED_CMDS).contains("/" + lockableName)) {
					sendMessage("$bEsse comando já esta bloqueado", player);
					return;
				}
				at = ap.getFlag(DefaultFlag.BLOCKED_CMDS);
			} else
				at = new HashSet<>();
			at.add("/" + lockableName);
			ap.iterator().next().setFlag(DefaultFlag.BLOCKED_CMDS, at);
			sendMessage("$bComando bloqueado com sucesso!.", player);
			withdraw(player, price.getDesactive());
			save(rm);
		} else
			sendToPlayer(player, Erros.NOOWNER, configuration);
	}

	public void allowedCommands(Player player, Bloqueaveis bloqueaveis) {
		final RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		final ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		final String name = configuration.getLockableNameByType(bloqueaveis);
		final Price price = configuration.getLockablePricesByType(bloqueaveis);
		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (ap.size() == 0 || !ap.iterator().hasNext()) {
				sendToPlayer(player, Erros.NOREGION, configuration);
				return;
			}
			if (!hasMoney(player, price.getActive())) {
				sendToPlayer(player, Erros.NOMONEY, configuration);
				return;
			}
			if (bloqueaveis == Bloqueaveis.PVP) {
				ap.iterator().next().setFlag(DefaultFlag.PVP, State.ALLOW);
				sendMessage("$bPVP ativado com sucesso!.", player);
				withdraw(player, price.getActive());
				return;
			}
			if (bloqueaveis == Bloqueaveis.ENTRY) {
				ap.iterator().next().setFlag(DefaultFlag.ENTRY, State.ALLOW);
				sendMessage("$bEntrada liberada com sucesso!.", player);
				withdraw(player, price.getActive());
				return;
			}
			Set<String> at;

			if (ap.getFlag(DefaultFlag.BLOCKED_CMDS) == null
					|| !ap.getFlag(DefaultFlag.BLOCKED_CMDS).contains("/" + name)) {
				sendMessage("$bEsse comando já esta liberado!.", player);
				return;
			}
			at = ap.getFlag(DefaultFlag.BLOCKED_CMDS);
			at.remove("/" + name);
			ap.iterator().next().setFlag(DefaultFlag.BLOCKED_CMDS, at);
			sendMessage("$bComando desbloqueado com sucesso!.", player);
			withdraw(player, price.getActive());
			save(rm);
		} else
			sendMessage("$cVocê não é dono deste terreno!.", player);
	}

	private String formatFlag(State state) {
		if (state == State.DENY)
			return "Desativado";
		return "Ativado";
	}

	public void addMember(Player player, String name) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (!ap.iterator().hasNext()) {
				sendToPlayer(player, Erros.NOREGION, configuration);
				return;
			}
			DefaultDomain dm = null;
			ProtectedRegion pr;
			if ((pr = ap.iterator().next()).getMembers() != null)
				dm = ap.iterator().next().getMembers();
			else
				dm = new DefaultDomain();
			if (dm.contains(name)) {
				sendMessage("$cEste jogador jé esta adicionado ao seu terreno", player);
				return;
			}
			dm.addPlayer(name);
			pr.setMembers(dm);
			sendMessage("$bJogador $e" + name + " $bAdicionado ao seu terreno!.", player);
			save(rm);
		} else
			sendToPlayer(player, Erros.NOOWNER, configuration);
	}

	private void save(RegionManager rm) {
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delMember(Player player, String name) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet protectedRegions = rm.getApplicableRegions(player.getLocation());
		if (!isOwner(player, protectedRegions)) {
			sendToPlayer(player, Erros.NOOWNER, configuration);
			return;
		}
		if (!protectedRegions.iterator().hasNext()) {
			sendMessage("$bNenhum terreno encontrado nesta posição", player);
			return;
		}
		DefaultDomain defaultDomain = null;
		ProtectedRegion pr;
		if ((pr = protectedRegions.iterator().next()).getMembers() != null)
			defaultDomain = protectedRegions.iterator().next().getMembers();
		else
			defaultDomain = new DefaultDomain();
		if (!defaultDomain.contains(name)) {
			sendMessage("$cEste jogador não esta adicionado ao seu terreno", player);
			return;
		}
		defaultDomain.removePlayer(name);
		pr.setMembers(defaultDomain);
		sendMessage("$bJogador $e" + name + " $bremovido do seu terreno!.", player);
		save(rm);
	}

	public void deleteRegion(final Player player) {
		edicao.deleteRegion(player, this);
	}

	private void withdraw(Player player, double money) {
		Terreno.economy.withdrawPlayer(player, money);
	}

	private boolean hasMoney(Player player, double money) {
		return Terreno.economy.getBalance(player) >= money;
	}

	public double multiplicarValor(String world, double valor) {
		return configuration.getMultiplicador().get(world.toLowerCase()) == null ? valor
				: configuration.getMultiplicador().get(world.toLowerCase()) * valor;
	}

	private int getPerm(Player p) {
		int max = -2;

		int maxlimit = 255;

		if (p.hasPermission("terreno.max.*")) {
			return 9999;
		} else {
			for (int ctr = 0; ctr < maxlimit; ctr++) {
				if (p.hasPermission("terreno.max." + ctr)) {
					max = ctr;
				}
			}

		}
		return max;
	}

	public boolean isOwner(Player player, ProtectedRegion pr) {
		return pr.getOwners().getPlayers().contains(player.getName().toLowerCase());
	}

	public boolean isOwner(Player player, ApplicableRegionSet apr) {
		return apr.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player));
	}

	public boolean contains(World world, String name) {
		return Terreno.wg.getRegionManager(world).getRegionExact(name) != null;
	}

}
