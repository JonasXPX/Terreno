package me.jonasxpx.terreno;

import static me.jonasxpx.terreno.Terreno.economy;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class Venda {
	
	public static Map<Player, Venda> hash = new HashMap<>();

	private Player owner;
	private ProtectedRegion pr;
	private double valor;
	private Player buyer;
	private World currentWorld;
	
	
	public Venda(Player owner, Player buyer, ProtectedRegion pr, double valor){
		this.owner = owner;
		this.pr = pr;
		this.valor = valor;
		this.buyer = buyer;
		currentWorld = owner.getWorld();
		owner.sendMessage("§b» Venda adicionada, aguardando a compra do jogador, valido até o servidor reiniciar ou você deslogar");
		buyer.sendMessage("§b» Um terreno foi adicionado a venda para você, no valor de §f" + NumberFormat.getInstance().format(valor) + "§b Digite §f/terreno adquirir §bpara comprar");
	}
	
	public void efetuarCompra(Player buyPlayer){
		if(buyPlayer != getBuyer()){
			buyPlayer.sendMessage("§c» Você não pode comprar este terreno.");
			return;
		}
		
		if(getOwner().isOnline() == false){
			getBuyer().sendMessage("§c» O dono do terreno precisa estar online.");
			return;
		}
		
		if(economy.getBalance(getBuyer()) <= valor){
			getBuyer().sendMessage("§c» Você não tem dinheiro para comprar este terreno!");
			return;
		}
		if(getBuyer().getWorld() != currentWorld){
			getBuyer().sendMessage("§c» Você precisa estar no mesmo mundo do terreno.");
			return;
		}
		
		Tools.transferirDono(getOwner(), getBuyer(), pr);
		economy.withdrawPlayer(getBuyer(), valor);
		economy.depositPlayer(getOwner(), valor);
		hash.remove(getOwner());
		buyPlayer.sendMessage("§b» Terreno adquirido!.");
		owner.sendMessage("§b» O jogador " + buyPlayer.getName() + " comprou o terreno.");
	}
	
	public boolean isEqualsRegion(ProtectedRegion pr){
		return pr.equals(this.pr);
	}

	public Player getBuyer(){
		return buyer;
	}
	
	public Player getOwner(){
		return owner;
	}
	
	
	/* METODOS ESTATICOS */
	
	public static void registrarVenda(Venda venda){
		hash.put(venda.getOwner(), venda);
	}
	
	public static Venda getVendaByBuyer(Player buyer){
		Venda v = null;
		for(Venda venda : hash.values()){
			if(buyer.equals(venda.getBuyer())){
				v = venda;
				break;
			}
		}
		return v;
	}
}
