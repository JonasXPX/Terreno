package me.jonasxpx.terreno;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import me.jonasxpx.terreno.worldedit.Edicao;
import net.milkbowl.vault.economy.Economy;

public class Terreno extends JavaPlugin{

	public static WorldGuardPlugin wg = null;
	public static WorldEditPlugin we = null;
	public static Terreno instance = null;
	public double valorPequeno = 0.0;
	public double valorMedio = 0.0;
	public double valorGrande = 0.0;
	public double valorDesativarPvp = 0.0;
	public double valorAtivarPvp = 0.0;
	public double valorDesativarCmd = 0.0;
	public double valorAtivarCmd = 0.0;
	public double valorAtivarEntrada = 0.0;
	public double valorDesativarEntrada = 0.0;
	public double valorDesativarFix = 0.0;
	public double valorAtivarFix = 0.0;
	public int tamPequeno = 0;
	public int tamMedio = 0;
	public int tamGrande = 0;
	public boolean criarCercado;
	public List<BlockChance> cercado;
	public int itemCercado;
	public static Economy economy = null;
	public List<String> worlds = null;
	public Map<String, Integer> multiplicador = null;
	public Map<String, Map<TiposTerrenos, String>> schematic = null;
	public Map<String, String> lang = null;
	
	
	@Override
	public void onEnable() {
		wg = getWorldGuard();
		we = getWorldEdit();
		instance = this;
		getConfig().options().copyDefaults(true);
		saveConfig();
		loadConfig();
		setupEconomy();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player p = (Player)sender;
		if(worlds.contains(p.getWorld().getName())){
			p.sendMessage("§c» Comando bloqueado nesse mundo!, Vá para o mundo de terrenos.");
			return true;
		}
		versionConvert(p);
		if(args.length == 0){
			getCommands(p);
		}
		if(args.length == 1){
			switch(args[0].toLowerCase()){
			case "teste":
				break;
			case "info":
				p.sendMessage(Tools.getInfo(p.getLocation()));
				break;
			case "tp":
				p.sendMessage("§b» Terrenos disponíveis para você neste mundo:");
				List<String> regions = new PlayerManager(p).getRegions();
				System.out.println(regions.toString());
				if(!regions.isEmpty()){
					for(int x = 0;x < regions.size(); x++){
						p.sendMessage("§b» Terreno ID: §e" + x);
					}
					p.sendMessage("§c» use /terreno tp <ID> ");
				} else {
					p.sendMessage("§c» Você não tem terrenos.");
				}
				if(p.isOp())p.sendMessage("§c Use /terreno tp <ID> <NICK>");
				break;
			case "deletar":
				Edicao.deleteRegion(p);
				break;
			case "comprar":
				p.sendMessage("§b» Terrenos para compra:");
				System.out.println(multiplicador.toString());
				for(TiposTerrenos vl : TiposTerrenos.values()){
					p.sendMessage("§b» §e" + vl.getName() + " §b(" + vl.getTamanho() + "x"+ vl.getTamanho()+ ")" + " Valor: §e" + NumberFormat.getInstance().format(Tools.multiplicarValor(p.getWorld().getName().toLowerCase(), vl.getValor())));
				}
				p.sendMessage("§c» /terreno comprar <Tamanho>");
				break;
			case "desativar":
				p.sendMessage("§b» Disponíveis para desativação");
				for(Bloqueaveis b : Bloqueaveis.values()){
					p.sendMessage("§b» "+b.getType()+" §e" + b.getNome() + "§b Valor: §e" + NumberFormat.getInstance().format(b.getValorDesativar()));
				}
				p.sendMessage("§c» /terreno desativar <COMANDO | FUNÇÃO>");
				break;
			case "ativar":
				p.sendMessage("§b» Disponíveis para ativação");
				for(Bloqueaveis b : Bloqueaveis.values()){
					p.sendMessage("§b» " + b.getType()+ " §e" + b.getNome() + "§b Valor: §e" + NumberFormat.getInstance().format(b.getValorAtivar()));
				}
				p.sendMessage("§c» /terreno desativar <COMANDO | FUNÇÃO>");
				break;
			case "amigo":
				p.sendMessage("§b» Você pode adicionar ou remover amigos no seu terreno");
				p.sendMessage("§c» /terreno amigo <add | remover> <NICK>");
				break;
			case "adquirir":
				Venda venda = Venda.getVendaByBuyer(p);
				if(venda == null){
					p.sendMessage("§c» Nada para comprar");
					break;
				}
				venda.efetuarCompra(p);
				break;
			case "vender":
				p.sendMessage("§c» /terreno vender <nick> <valor>");
				break;
			}
		}
		if(args.length == 2){
			if(args[0].equalsIgnoreCase("tp")){
				Tools.tpRegion(p, Integer.parseInt(args[1]));
				return true;
			}
			if(args[0].equalsIgnoreCase("comprar")){
				switch(args[1].toLowerCase()){
				case "pequeno":
					Tools.createRegionForPlayer(p, TiposTerrenos.PEQUENO);
					break;
				case "medio":
					Tools.createRegionForPlayer(p, TiposTerrenos.MEDIO);
					break;
				case "grande":
					Tools.createRegionForPlayer(p, TiposTerrenos.GRANDE);
					break;
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("desativar")){
				switch(args[1].toLowerCase()){
				case "sethome":
					Tools.blockedcmds(p, Bloqueaveis.SET_HOME);
					break;
				case "tpaccept":
					Tools.blockedcmds(p, Bloqueaveis.TPACCEPT);
					break;
				case "pvp":
					Tools.blockedcmds(p, Bloqueaveis.PVP);
					break;
				case "entrada":
					Tools.blockedcmds(p, Bloqueaveis.ENTRY);
					break;
				case "fix":
					Tools.blockedcmds(p, Bloqueaveis.FIX);
					break;
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("ativar")){
				switch(args[1].toLowerCase()){
				case "sethome":
					Tools.allowedcmds(p, Bloqueaveis.SET_HOME);
					break;
				case "tpaccept":
					Tools.allowedcmds(p, Bloqueaveis.TPACCEPT);
					break;
				case "pvp":
					Tools.allowedcmds(p, Bloqueaveis.PVP);
					break;	
				case "entrada":
					Tools.allowedcmds(p, Bloqueaveis.ENTRY);
					break;
				case "fix":
					Tools.allowedcmds(p, Bloqueaveis.FIX);
					break;
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("amigo") && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remover"))){
				p.sendMessage("§c» /terreno amigo <add | remover> <NICK>");
				return true;
			}
			getCommands(p);
		}
		if(args.length == 3){
			if(args[0].equalsIgnoreCase("amigo")){
				if(args[1].equalsIgnoreCase("add")){
					Tools.addMember(p, args[2]);
				}
				if(args[1].equalsIgnoreCase("remover")){
					Tools.delMember(p, args[2]);
				}
			}
			if(args[0].equalsIgnoreCase("tp")){
				Tools.tpRegionOfflinePlayer(p, args[2], args[1]);
			}
			if(args[0].equalsIgnoreCase("vender")){
				Player buyer = Bukkit.getPlayerExact(args[1]);
				if(buyer == null){
					p.sendMessage("§c» Jogador offline.");
					return true;
				}
				if(Venda.hash.containsKey(p)){
					p.sendMessage("§c» Você já tem uma venda em aberto");
					return true;
				}
				if(Tools.getRegion(p.getLocation()) == null){
					p.sendMessage("§c» Você não esta em um terreno");
					return true;
				}
				if(!Tools.isOwner(p, Tools.getRegion(p.getLocation()))){
					p.sendMessage("§c» Você não é o dono do terreno");
					return true;
				}
				if(buyer.equals(p)){
					p.sendMessage("§c» Você não pode vender para esse jogador ");
					return true;
				}
				Venda.registrarVenda(new Venda(p, buyer, Tools.getRegion(p.getLocation()), Double.parseDouble(args[2])));
			}
		}
		return true;
	}

	public WorldGuardPlugin getWorldGuard(){
		return (WorldGuardPlugin)getServer().getPluginManager().getPlugin("WorldGuard");
	}
	public WorldEditPlugin getWorldEdit(){
		return (WorldEditPlugin)getServer().getPluginManager().getPlugin("WorldEdit");
	}
	
	private void loadConfig(){
		this.valorPequeno = getConfig().getDouble("Valores.Terreno.Pequeno");
		this.valorMedio = getConfig().getDouble("Valores.Terreno.Medio");
		this.valorGrande = getConfig().getDouble("Valores.Terreno.Grande");
		this.valorAtivarPvp = getConfig().getDouble("Valores.Funcao.AtivarPVP");
		this.valorDesativarPvp = getConfig().getDouble("Valores.Funcao.DesativarPVP");
		this.valorAtivarCmd = getConfig().getDouble("Valores.Funcao.AtivarCMD");
		this.valorDesativarCmd = getConfig().getDouble("Valores.Funcao.DesativarCMD");
		this.valorDesativarEntrada = getConfig().getDouble("Valores.Funcao.DesativarEntrada");
		this.valorAtivarEntrada = getConfig().getDouble("Valores.Funcao.AtivarEntrada");
		this.valorDesativarFix = getConfig().getDouble("Valores.Funcao.DesativarFix");
		this.valorAtivarFix = getConfig().getDouble("Valores.Funcao.AtivarFix");
		this.tamPequeno = getConfig().getInt("Tamanhos.Terrenos.Pequeno");
		this.tamMedio = getConfig().getInt("Tamanhos.Terrenos.Medio");
		this.tamGrande = getConfig().getInt("Tamanhos.Terrenos.Grande");
		this.worlds = getConfig().getStringList("Outros.MundosBloqueados");
		this.criarCercado = getConfig().getBoolean("Outros.CriarCerca");
		if(this.criarCercado){
			cercado = Lists.newArrayList();
			for(String st : getConfig().getString("Outros.IDCerca").split(",")){
				cercado.add(new BlockChance(new BaseBlock(Integer.parseInt(st)), 100));
			}
		}
		this.multiplicador = new HashMap<String, Integer>();
		this.schematic = new HashMap<String, Map<TiposTerrenos,String>>();
		for(String key : getConfig().getConfigurationSection("Outros.Multiplicador").getKeys(false))
			multiplicador.put(key.toLowerCase(), getConfig().getInt("Outros.Multiplicador." + key + ".Valor"));
		for(String world : getConfig().getConfigurationSection("Outros.Schematics").getKeys(false))
		{
			Map<TiposTerrenos, String> sch = new HashMap<TiposTerrenos, String>();
			for(String key2 : getConfig().getConfigurationSection("Outros.Schematics." + world).getKeys(false)){
				sch.put(TiposTerrenos.getByName(key2), getConfig().getString("Outros.Schematics." + world + "." + key2 + ".SchemName"));
			}
			schematic.put(world, sch);
		}
		///////
		//
		// CARREGA IDIOMA
		//
		///////
		lang = new HashMap<String, String>();
		for(String string : getConfig().getConfigurationSection("Lang.").getKeys(true)){
			System.out.println(string);
			lang.put(string, ChatColor.translateAlternateColorCodes('&', getConfig().getString("Lang." + string)));
		}
	}
	
	private void getCommands(Player p){
		p.sendMessage("§b» §3[EndCraft] §bComandos disponiveis neste local:");
		p.sendMessage("§b» /Terreno tp - §eSe teleportar para algum terreno seu.");
		if(Tools.checkRegionNear(p, 1)){
			p.sendMessage("§b» /Terreno comprar - §eComprar um terreno.");
		}else{
			p.sendMessage("§b» /Terreno info - §eVer informações desta área");
			p.sendMessage("§b» /Terreno ativar <Comando | função>");
			p.sendMessage("§b» /Terreno deletar - §eDeletar o terreno em que você esta.");
			p.sendMessage("§b» /Terreno desativar <Comando | função>");
			p.sendMessage("§b» /terreno amigo <add | remover> <NICK>");
			p.sendMessage("§b» /terreno vender <nick> <valor>");
		}
		if(p.isOp()){
			p.sendMessage("§c» /Terreno tp <ID> <NICK> - §eSe teleportar para algum terreno.");
		}
	}
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
	
	private void versionConvert(Player player){
		PlayerManager pm = new PlayerManager(player);
		List<String> format = Lists.newArrayList();
		if(pm.getRegions().isEmpty()){
			return;
		}
		if(isVersionEquals(pm.getRegions())){
			return;
		}
		for(String s : pm.getRegions()){
			try{
				format.add(player.getName() +"-" +Integer.parseInt(s));
			}catch(NumberFormatException e){
				if(s.contains(":") && s.contains(",")){
					String reg = s.split(":")[1];
					format.add(reg.split(",")[0] + "-" + reg.split(",")[1]);
				}
			}
		}
		pm.setRegions(format);
		System.out.println(format.toString());
	}
	
	private boolean isVersionEquals(List<String> rg){
		for(String s : rg){
			if(!s.contains("-")){
				return false;
			}
		}
		return true;
	}
}