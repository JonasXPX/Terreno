package me.jonasxpx.terreno.config;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.BlockChance;
import me.jonasxpx.terreno.data.Price;
import me.jonasxpx.terreno.enums.Bloqueaveis;
import me.jonasxpx.terreno.enums.TiposTerrenos;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.String.format;

public class ConfigurationImpl implements Configuration {

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
	private String defaultLang;
	private List<String> worlds = new ArrayList<>();
	public final Map<String, Integer> multiplicador = new HashMap<>();
	public final Map<String, Map<TiposTerrenos, String>> schematic = new HashMap<>();
	public final Map<String, String> lang = new HashMap<>();

	private final FileConfiguration config;

	public ConfigurationImpl(FileConfiguration config) {
		this.config = config;
	}

	@Override
	public void initConfig() {
		worlds.clear();
		multiplicador.clear();
		schematic.clear();
		lang.clear();
		loadFile();

		this.defaultLang = getConfig().getString("default-lang");
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
		this.setWorlds(getConfig().getStringList("Outros.MundosBloqueados"));
		this.criarCercado = getConfig().getBoolean("Outros.CriarCerca");

		if (this.criarCercado) {
			cercado = Lists.newArrayList();
			for (String st : getConfig().getString("Outros.IDCerca").split(",")) {
				cercado.add(new BlockChance(new BaseBlock(Integer.parseInt(st)), 100));
			}
		}
		for (String key : getConfig().getConfigurationSection("Outros.Multiplicador").getKeys(false))
			multiplicador.put(key.toLowerCase(), getConfig().getInt("Outros.Multiplicador." + key + ".Valor"));
		for (String world : getConfig().getConfigurationSection("Outros.Schematics").getKeys(false)) {
			Map<TiposTerrenos, String> sch = new EnumMap<>(TiposTerrenos.class);
			for (String key2 : getConfig().getConfigurationSection("Outros.Schematics." + world).getKeys(false)) {
				sch.put(TiposTerrenos.getByName(key2),
						getConfig().getString("Outros.Schematics." + world + "." + key2 + ".SchemName"));
			}
			schematic.put(world, sch);
		}

		final YamlConfiguration langConfiguration = YamlConfiguration
				.loadConfiguration(new File(format("plugins/Terreno/%s.yml", defaultLang.toLowerCase(Locale.ROOT))));

		for (String string : langConfiguration.getConfigurationSection("lang.").getKeys(true)) {
			Logger.getGlobal().info(string);
			lang.put(string, ChatColor.translateAlternateColorCodes('&', langConfiguration.getString("lang." + string)));
		}
	}

	private void loadFile() {
		try {
			config.load(new File("plugins/Terreno/config.yml"));
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public double getValorPequeno() {
		return valorPequeno;
	}

	@Override
	public void setValorPequeno(double valorPequeno) {
		this.valorPequeno = valorPequeno;
	}

	@Override
	public double getValorMedio() {
		return valorMedio;
	}

	@Override
	public void setValorMedio(double valorMedio) {
		this.valorMedio = valorMedio;
	}

	@Override
	public double getValorGrande() {
		return valorGrande;
	}

	@Override
	public void setValorGrande(double valorGrande) {
		this.valorGrande = valorGrande;
	}

	@Override
	public double getValorDesativarPvp() {
		return valorDesativarPvp;
	}

	@Override
	public void setValorDesativarPvp(double valorDesativarPvp) {
		this.valorDesativarPvp = valorDesativarPvp;
	}

	@Override
	public double getValorAtivarPvp() {
		return valorAtivarPvp;
	}

	@Override
	public void setValorAtivarPvp(double valorAtivarPvp) {
		this.valorAtivarPvp = valorAtivarPvp;
	}

	@Override
	public double getValorDesativarCmd() {
		return valorDesativarCmd;
	}

	@Override
	public void setValorDesativarCmd(double valorDesativarCmd) {
		this.valorDesativarCmd = valorDesativarCmd;
	}

	@Override
	public double getValorAtivarCmd() {
		return valorAtivarCmd;
	}

	@Override
	public void setValorAtivarCmd(double valorAtivarCmd) {
		this.valorAtivarCmd = valorAtivarCmd;
	}

	@Override
	public double getValorAtivarEntrada() {
		return valorAtivarEntrada;
	}

	@Override
	public void setValorAtivarEntrada(double valorAtivarEntrada) {
		this.valorAtivarEntrada = valorAtivarEntrada;
	}

	@Override
	public double getValorDesativarEntrada() {
		return valorDesativarEntrada;
	}

	@Override
	public void setValorDesativarEntrada(double valorDesativarEntrada) {
		this.valorDesativarEntrada = valorDesativarEntrada;
	}

	@Override
	public double getValorDesativarFix() {
		return valorDesativarFix;
	}

	@Override
	public void setValorDesativarFix(double valorDesativarFix) {
		this.valorDesativarFix = valorDesativarFix;
	}

	@Override
	public double getValorAtivarFix() {
		return valorAtivarFix;
	}

	@Override
	public void setValorAtivarFix(double valorAtivarFix) {
		this.valorAtivarFix = valorAtivarFix;
	}

	@Override
	public int getTamPequeno() {
		return tamPequeno;
	}

	@Override
	public void setTamPequeno(int tamPequeno) {
		this.tamPequeno = tamPequeno;
	}

	@Override
	public int getTamMedio() {
		return tamMedio;
	}

	@Override
	public void setTamMedio(int tamMedio) {
		this.tamMedio = tamMedio;
	}

	@Override
	public int getTamGrande() {
		return tamGrande;
	}

	@Override
	public void setTamGrande(int tamGrande) {
		this.tamGrande = tamGrande;
	}

	@Override
	public boolean isCriarCercado() {
		return criarCercado;
	}

	@Override
	public void setCriarCercado(boolean criarCercado) {
		this.criarCercado = criarCercado;
	}

	@Override
	public List<BlockChance> getCercado() {
		return cercado;
	}

	@Override
	public void setCercado(List<BlockChance> cercado) {
		this.cercado = cercado;
	}

	@Override
	public Map<String, Integer> getMultiplicador() {
		return multiplicador;
	}

	@Override
	public Map<String, Map<TiposTerrenos, String>> getSchematic() {
		return schematic;
	}

	@Override
	public Map<String, String> getLang() {
		return lang;
	}

	private FileConfiguration getConfig() {
		return this.config;
	}

	@Override
	public List<String> getWorlds() {
		return worlds;
	}

	@Override
	public void setWorlds(List<String> worlds) {
		this.worlds = worlds;
	}

	@Override
	public Double getPriceByType(TiposTerrenos tiposTerrenos) {
		switch (tiposTerrenos) {
			case PEQUENO:
				return getValorPequeno();
			case MEDIO:
				return getValorMedio();
			case GRANDE:
				return getValorGrande();
		}

		return null;
	}

	@Override
	public Integer getSizeByType(TiposTerrenos tiposTerrenos) {
		switch (tiposTerrenos) {
			case PEQUENO:
				return getTamPequeno();
			case MEDIO:
				return getTamMedio();
			case GRANDE:
				return getTamGrande();
		}

		return null;
	}

	@Override
	public String getLockableNameByType(Bloqueaveis bloqueaveis) {
		switch (bloqueaveis) {
			case FIX:
				return lang.get("fix-label");
			case PVP:
				return lang.get("pvp-label");
			case ENTRY:
				return lang.get("entry-label");
			case SET_HOME:
				return lang.get("sethome-label");
			case TP_ACCEPT:
				return lang.get("tpaccept-label");
		}
		return null;
	}

	@Override
	public Price getLockablePricesByType(Bloqueaveis bloqueaveis) {
		final Price price = new Price();
		switch (bloqueaveis) {
			case FIX:
				price.setActive(getValorAtivarFix());
				price.setDesactive(getValorDesativarFix());
				return price;
			case PVP:
				price.setActive(getValorAtivarPvp());
				price.setDesactive(getValorDesativarPvp());
				return price;
			case ENTRY:
				price.setActive(getValorAtivarEntrada());
				price.setDesactive(getValorDesativarEntrada());
				return price;
			case SET_HOME:
			case TP_ACCEPT:
				price.setActive(getValorAtivarCmd());
				price.setDesactive(getValorDesativarCmd());
				return price;
		}

		return price;
	}

}
