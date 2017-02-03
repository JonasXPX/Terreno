package me.jonasxpx.terreno;

import static me.jonasxpx.terreno.worldedit.Edicao.createWalls;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

import me.jonasxpx.terreno.error.Erros;

public class Tools {

	public static Map<String, Long> timeout = new HashMap<>();

	public static void createRegionForPlayer(Player player, TiposTerrenos terreno) {
		if (!hasMoney(player, terreno.getValor())) {
			Erros.NOMONEY.sendToPlayer(player);
			return;
		}
		PlayerManager pm = new PlayerManager(player);
		if (pm.getAmount() >= getPerm(player)){
			Erros.LIMITREGION.sendToPlayer(player);
			return;
		}
		String regionName = (player.getName() + "-" + pm.forNextInt(player)).toLowerCase();
		RegionManager r = Terreno.wg.getRegionManager(player.getWorld());
		ProtectedCuboidRegion pcr = new ProtectedCuboidRegion(regionName, toVector(point1(player
				.getLocation(), terreno.getTamanho())), toVector(point2(player
				.getLocation(), terreno.getTamanho())));
		if (!checkRegionNear(r, pcr)) {
			Erros.NEARREGION.sendToPlayer(player);
			return;
		}
		DefaultDomain dd = new DefaultDomain();
		dd.addPlayer(new com.sk89q.worldguard.bukkit.BukkitPlayer(Terreno.wg, player));
		pcr.setOwners(dd);
		pcr.setPriority(5);
		pcr.setFlag(DefaultFlag.USE, State.DENY);
		r.addRegion(pcr);
		pm.addNewRegion(regionName);
		if(Terreno.instance.schematic.containsKey(player.getWorld().getName())){
			Map<TiposTerrenos, String> sch = Terreno.instance.schematic.get(player.getWorld().getName());
			if(sch.containsKey(terreno)){
				SchematicManager sm = new SchematicManager(Vector.getMidpoint(r.getRegion(regionName).getMinimumPoint(), r.getRegion(regionName).getMaximumPoint()).setY(player.getLocation().getBlockY()), sch.get(terreno));
				try {
					sm.loadSchematic(new BukkitWorld(player.getWorld()));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		try {
			r.save();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		double valor;
		withdraw(player, (valor = multiplicarValor(player.getWorld().getName().toLowerCase(), terreno.getValor())));
		player.sendMessage("§b» Terreno §e" + terreno.getName()
				+ "§b adiquirido, por §e" + NumberFormat.getInstance().format(valor) + "§b Coins");
		try {
			createWalls(pcr, player);
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	}

	public static ProtectedRegion getRegion(Location loc){
		Iterator<ProtectedRegion> pr;
		if(!(pr = Terreno.wg.getRegionManager(loc.getWorld()).getApplicableRegions(loc).iterator()).hasNext())
			return null;
		return pr.next();
	}
	
	public static boolean checkRegionNear(RegionManager r, ProtectedCuboidRegion pcr) {
		try {
			if (pcr.getIntersectingRegions(Lists.newArrayList(r.getRegions()
					.values())).size() == 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void transferirDono(Player owner, Player buyer, ProtectedRegion pr){
		pr.getOwners().removePlayer(owner.getName());
		pr.setMembers(new DefaultDomain());
		pr.getOwners().addPlayer(buyer.getName());
		PlayerManager pm = new PlayerManager(owner);
		pm.deleteRegion(pr.getId());
		pm = new PlayerManager(buyer);
		pm.addNewRegion(pr.getId());
		try {
			Terreno.wg.getRegionManager(buyer.getWorld()).save();
		} catch (ProtectionDatabaseException e) {
			e.printStackTrace();
		}
	}

	
	public static boolean checkRegionNear(Player player, int area) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		Location loc1 = point1(player.getLocation(), area);
		Location loc2 = point2(player.getLocation(), area);
		if (rm.getApplicableRegionsIDs(toVector(loc1)).isEmpty()
				&& rm.getApplicableRegionsIDs(toVector(loc2)).isEmpty())
			return true;
		else
			return false;
	}

	public static String getInfo(Location loc) {
		RegionManager rm = Terreno.wg.getRegionManager(loc.getWorld());
		Iterable<ProtectedRegion> ap = rm.getApplicableRegions(loc);
		StringBuilder sb = new StringBuilder();
		for (ProtectedRegion s : ap) {
			sb.append("§b» §3[EndCraft]§b Informações sobre o terreno atual:");
			sb.append("\n");
			sb.append("§b» Dono: §e" + s.getOwners().getPlayers());
			sb.append("\n");
			if (s.getMembers().size() > 0) {
				sb.append("§b» Amigos: §e" + s.getMembers().getPlayers());
				sb.append("\n");
			}
			sb.append("§b» PVP: §e" + formatFlag(s.getFlag(DefaultFlag.PVP)));
			sb.append("\n");
			sb.append("§b» Entrada: §e" + formatFlag(s.getFlag(DefaultFlag.ENTRY)));
			sb.append("\n");
			if (s.getFlag(DefaultFlag.BLOCKED_CMDS) != null) {
				sb.append("§b» Comandos bloqueados: §e"
						+ s.getFlag(DefaultFlag.BLOCKED_CMDS));
			}
			
		}
		return sb.toString();
	}

	public static Location point1(Location loc, int blocos) {
		loc.setY(0);
		return loc.add(-blocos, 0, blocos);
	}

	public static Location point2(Location loc, int blocos) {
		loc.setY(256);
		return loc.add(blocos, 0, -blocos);
	}

	public static BlockVector toVector(Location loc) {
		return new BlockVector(loc.getX(), loc.getY(), loc.getZ());
	}

	public static void tpRegion(Player player, int ID) {
		RegionManager ap = Terreno.wg.getRegionManager(player.getWorld());
		PlayerManager pm = new PlayerManager(player);
		if(ID >= pm.getRegions().size()){
			Erros.NOREGION.sendToPlayer(player);
			return;
		}
		if (ap.hasRegion(pm.getRegions().get(ID))) {
			ProtectedRegion pr = ap.getRegion(pm.getRegions().get(ID));
			player.teleport(new Location(player.getWorld(), pr
					.getMaximumPoint().getX() + 1, player.getWorld()
					.getHighestBlockYAt(pr.getMaximumPoint().getBlockX(), pr
							.getMaximumPoint().getBlockZ()), pr
					.getMaximumPoint().getZ() + 1));
		}else{
			player.sendMessage("§cTerreno não encontrado.");
		}
	}
	public static void tpRegionOfflinePlayer(Player onlinePlayer, String offlinePlayer, String ID) {
		if(!onlinePlayer.isOp()){return;}
		RegionManager ap = Terreno.wg.getRegionManager(onlinePlayer.getWorld());
		if (ap.hasRegion(offlinePlayer + "-" + ID)) {
			ProtectedRegion pr = ap.getRegion(offlinePlayer + "-" + ID);
			onlinePlayer.teleport(new Location(onlinePlayer.getWorld(), pr
					.getMaximumPoint().getX() + 1, onlinePlayer.getWorld()
					.getHighestBlockYAt(pr.getMaximumPoint().getBlockX(), pr
							.getMaximumPoint().getBlockZ()), pr
					.getMaximumPoint().getZ() + 1));
		}
	}

	public static void blockedcmds(Player player, Bloqueaveis bq) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (ap.size() == 0 || ap.iterator().hasNext() == false) {
				player.sendMessage("§b» Nenhum terreno encontrado nesta posição");
				return;
			}
			if (!hasMoney(player, bq.getValorDesativar())) {
				player.sendMessage("§c» Você não tem dinheiro para isso!.");
				return;
			}
			if (bq == Bloqueaveis.PVP) {
				ap.iterator().next().setFlag(DefaultFlag.PVP, State.DENY);
				player.sendMessage("§b» PVP removido com sucesso!.");
				withdraw(player, bq.getValorDesativar());
				return;
			}
			if(bq == Bloqueaveis.ENTRY){
				ap.iterator().next().setFlag(DefaultFlag.ENTRY, State.DENY);
				player.sendMessage("§b» Entrada removida com sucesso!.");
				withdraw(player, bq.getValorAtivar());
				return;
			}
			Set<String> at = null;
			if (ap.getFlag(DefaultFlag.BLOCKED_CMDS) != null) {
				if (ap.getFlag(DefaultFlag.BLOCKED_CMDS).contains("/"+ bq.getNome())) {
					player.sendMessage("§b» Esse comando já esta bloqueado");
					return;
				}
				at = ap.getFlag(DefaultFlag.BLOCKED_CMDS);
			} else
				at = new HashSet<>();
			at.add("/" + bq.getNome());
			ap.iterator().next().setFlag(DefaultFlag.BLOCKED_CMDS, at);
			player.sendMessage("§b» Comando bloqueado com sucesso!.");
			withdraw(player, bq.getValorDesativar());
			save(rm);
		} else
			Erros.NOOWNER.sendToPlayer(player);
	}

	public static void allowedcmds(Player player, Bloqueaveis bq) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (ap.size() == 0 || ap.iterator().hasNext() == false) {
				Erros.NOREGION.sendToPlayer(player);
				return;
			}
			if (!hasMoney(player, bq.getValorAtivar())) {
				Erros.NOMONEY.sendToPlayer(player);
				return;
			}
			if (bq == Bloqueaveis.PVP) {
				ap.iterator().next().setFlag(DefaultFlag.PVP, State.ALLOW);
				player.sendMessage("§b» PVP ativado com sucesso!.");
				withdraw(player, bq.getValorAtivar());
				return;
			}
			if(bq == Bloqueaveis.ENTRY){
				ap.iterator().next().setFlag(DefaultFlag.ENTRY, State.ALLOW);
				player.sendMessage("§b» Entrada liberada com sucesso!.");
				withdraw(player, bq.getValorAtivar());
				return;
			}
			Set<String> at;
			
			if (ap.getFlag(DefaultFlag.BLOCKED_CMDS) == null || !ap.getFlag(DefaultFlag.BLOCKED_CMDS).contains("/"
					+ bq.getNome())) {
				player.sendMessage("§b» Esse comando já esta liberado!.");
				return;
			}
			at = ap.getFlag(DefaultFlag.BLOCKED_CMDS);
			at.remove("/" + bq.getNome());
			ap.iterator().next().setFlag(DefaultFlag.BLOCKED_CMDS, at);
			player.sendMessage("§b» Comando desbloqueado com sucesso!.");
			withdraw(player, bq.getValorAtivar());
			save(rm);
		} else
			player.sendMessage("§c» Você não é dono deste terreno!.");
	}

	private static String formatFlag(State state) {
		if (state == State.DENY)
			return "Desativado";
		return "Ativado";
	}

	public static void addMember(Player player, String name) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		if (ap.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player))) {
			if (!ap.iterator().hasNext()) {
				Erros.NOREGION.sendToPlayer(player);
				return;
			}
			DefaultDomain dm = null;
			ProtectedRegion pr;
			if ((pr = ap.iterator().next()).getMembers() != null)
				dm = ap.iterator().next().getMembers();
			else
				dm = new DefaultDomain();
			if (dm.contains(name)) {
				player.sendMessage("§c» Este jogador já esta adicionado ao seu terreno");
				return;
			}
			dm.addPlayer(name);
			pr.setMembers(dm);
			player.sendMessage("§b» Jogador §e" + name
					+ " §bAdicionado ao seu terreno!.");
			save(rm);
		} else
			Erros.NOOWNER.sendToPlayer(player);
	}

	public static void save(RegionManager rm){
		try {
			rm.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void delMember(Player player, String name) {
		RegionManager rm = Terreno.wg.getRegionManager(player.getWorld());
		ApplicableRegionSet ap = rm.getApplicableRegions(player.getLocation());
		if(!isOwner(player, ap)){
			Erros.NOOWNER.sendToPlayer(player);
			return;
		}
		if (!ap.iterator().hasNext()) {
			player.sendMessage("§b» Nenhum terreno encontrado nesta posição");
			return;
		}
		DefaultDomain dm = null;
		ProtectedRegion pr;
		if ((pr = ap.iterator().next()).getMembers() != null)
			dm = ap.iterator().next().getMembers();
		else
			dm = new DefaultDomain();
		if (!dm.contains(name)) {
			player.sendMessage("§c» Este jogador não esta adicionado ao seu terreno");
			return;
		}
		dm.removePlayer(name);
		pr.setMembers(dm);
		player.sendMessage("§b» Jogador §e" + name
				+ " §bremovido do seu terreno!.");
		save(rm);
			
	}

	private static void withdraw(Player player, double money) {
		Terreno.economy.withdrawPlayer(player, money);
	}

	private static boolean hasMoney(Player player, double money) {
		if (Terreno.economy.getBalance(player) >= money)
			return true;
		else
			return false;
	}

	public static void addTime(Player player, Long time) {
		timeout.put(player.getName(), (System.currentTimeMillis() / 1000)
				+ time);
	}
	
	public static double multiplicarValor(String world, double valor){
		return Terreno.instance.multiplicador.get(world.toLowerCase()) == null ? valor : Terreno.instance.multiplicador.get(world.toLowerCase()) * valor;
	}
	private static int getPerm(Player p){
		int max = -2;
		
		int maxlimit = 255;
		
		if(p.hasPermission("terreno.max.*"))
		{
			return 9999;
		}
		else
		{
			for(int ctr = 0; ctr < maxlimit; ctr++)
			{
				if(p.hasPermission("terreno.max." + ctr))
				{
					max = ctr;
				}
			}
		
		}
		return max;
	}

	public static boolean isOwner(Player player, ProtectedRegion pr){
		return pr.getOwners().getPlayers().contains(player.getName().toLowerCase());
	}
	public static boolean isOwner(Player player, ApplicableRegionSet apr){
		return apr.isOwnerOfAll(new BukkitPlayer(Terreno.wg, player));
	}
	
	public static boolean contains(World world, String name){
		if(Terreno.wg.getRegionManager(world).getRegionExact(name) == null){
			return false;
		}
		return true;
	}

}
