package me.jonasxpx.terreno;

public enum Bloqueaveis {

	SET_HOME("sethome", Terreno.instance.valorAtivarCmd, Terreno.instance.valorDesativarCmd, "Comando"),
	TPACCEPT("tpaccept", Terreno.instance.valorAtivarCmd, Terreno.instance.valorDesativarCmd, "Comando"),
	PVP("pvp", Terreno.instance.valorAtivarPvp, Terreno.instance.valorDesativarPvp, "Função"),
	ENTRY("entrada", Terreno.instance.valorAtivarEntrada, Terreno.instance.valorDesativarEntrada, "Função"),
	FIX("fix", Terreno.instance.valorAtivarFix, Terreno.instance.valorDesativarFix, "Comando");
	
	private String nome;
	private double valor;
	private double valorDesativar;
	private String type;
	
	public String getNome(){
		return nome;
	}
	
	public String getType(){
		return type;
	}
	
	public double getValorAtivar(){
		return valor;
	}
	public double getValorDesativar(){
		return valorDesativar;
	}
	
	private Bloqueaveis(String nome, double valorAtivar, double valorDesativar, String type) {
		this.nome = nome;
		this.valor = valorAtivar;
		this.valorDesativar = valorDesativar;
		this.type = type;
	}
}
