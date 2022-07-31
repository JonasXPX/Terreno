package me.jonasxpx.terreno;

import static me.jonasxpx.terreno.Terreno.economy;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import me.jonasxpx.terreno.data.GenericPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.inject.Inject;


public class Venda {
	
	public static final Map<GenericPlayer, Venda> currentSelling = new HashMap<>();

	private final GenericPlayer owner;
	private final GenericPlayer customer;
	private final ProtectedRegion pr;
	private final double valor;
	private final World currentWorld;

	@Inject
	private Tools tools;
	
	public Venda(Player owner, Player buyer, ProtectedRegion pr, double valor){
		this.owner = new GenericPlayer(owner);
		this.customer = new GenericPlayer(buyer);
		this.pr = pr;
		this.valor = valor;
		currentWorld = owner.getWorld();
		owner.sendMessage("§b» Venda adicionada, aguardando a compra do jogador, valido até o servidor reiniciar ou você deslogar");
		buyer.sendMessage("§b» Um terreno foi adicionado a venda para você, no valor de §f" + NumberFormat.getInstance().format(valor) + "§b Digite §f/terreno adquirir §bpara comprar");
	}
	
	public void efetuarCompra(Player buyPlayer){
		final GenericPlayer customerGenericPlayer = new GenericPlayer(buyPlayer);
		final Player customerPlayer = customerGenericPlayer.getPlayer();

		if(customerGenericPlayer != getCustomer()){
			customerPlayer.sendMessage("§c» Você não pode comprar este terreno.");
			return;
		}
		
		if(!getOwner().getPlayer().isOnline()){
			customerPlayer.sendMessage("§c» O dono do terreno precisa estar online.");
			return;
		}
		
		if(economy.getBalance(customerPlayer) <= valor){
			customerPlayer.sendMessage("§c» Você não tem dinheiro para comprar este terreno!");
			return;
		}
		if(customerPlayer.getWorld() != currentWorld){
			customerPlayer.sendMessage("§c» Você precisa estar no mesmo mundo do terreno.");
			return;
		}
		
		tools.transferirDono(getOwner().getPlayer(), customerPlayer, pr);
		economy.withdrawPlayer(customerPlayer, valor);
		economy.depositPlayer(getOwner().getPlayer(), valor);
		currentSelling.remove(getOwner());
		buyPlayer.sendMessage("§b» Terreno adquirido!.");
		getOwner().getPlayer().sendMessage("§b» O jogador " + buyPlayer.getName() + " comprou o terreno.");
	}
	
	public boolean isEqualsRegion(ProtectedRegion pr){
		return pr.equals(this.pr);
	}

	public GenericPlayer getCustomer(){
		return customer;
	}
	
	public GenericPlayer getOwner(){
		return owner;
	}

	public static void registrarVenda(Venda venda){
		currentSelling.put(venda.getOwner(), venda);
	}
	
	public static Venda getVendaByBuyer(Player customer){
		return currentSelling.get(new GenericPlayer(customer));
	}
}
