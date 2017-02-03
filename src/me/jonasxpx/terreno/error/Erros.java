package me.jonasxpx.terreno.error;

import me.jonasxpx.terreno.Terreno;

import org.bukkit.entity.Player;

public enum Erros {

	LIMITREGION(Terreno.instance.lang.get("LIMITREGION")),
	NOMONEY(Terreno.instance.lang.get("NOMONEY")),
	NEARREGION(Terreno.instance.lang.get("NEARREGION")),
	NOREGION(Terreno.instance.lang.get("NOREGION")),
	CONFIRMCOMMAND(Terreno.instance.lang.get("CONFIRMCOMMAND")),
	NULLERRO(Terreno.instance.lang.get("NULLERRO")),
	SUCESSDELETE(Terreno.instance.lang.get("SUCESSDELETE")),
	NOOWNERDELETE(Terreno.instance.lang.get("NOOWNERDELETE")),
	NOOWNER(Terreno.instance.lang.get("NOOWNER"));
	
	String string;
	
	private Erros(String erro){this.string = erro;}
	
	public void sendToPlayer(Player player){
		player.sendMessage(this.string);
	}
	
	
}
