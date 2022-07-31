package me.jonasxpx.terreno.config;

import me.jonasxpx.terreno.Terreno;
import me.jonasxpx.terreno.Tools;
import me.jonasxpx.terreno.Venda;
import me.jonasxpx.terreno.data.GenericPlayer;
import me.jonasxpx.terreno.data.Price;
import me.jonasxpx.terreno.enums.Bloqueaveis;
import me.jonasxpx.terreno.enums.TiposTerrenos;
import me.jonasxpx.terreno.worldedit.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static me.jonasxpx.terreno.utils.MessageUtils.sendMessage;
import static org.bukkit.ChatColor.RED;

public class CommandConfiguration {

    private final Tools tools;

    private final Configuration configuration;

    private final Terreno terreno;

    @Inject
    public CommandConfiguration(Tools tools, Configuration configuration, Terreno terreno) {
        this.tools = tools;
        this.configuration = configuration;
        this.terreno = terreno;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (configuration == null) {
            sendMessage("failed to load config", player);
            return false;
        }

        if (configuration.getWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(RED + "Comando bloqueado nesse mundo!, Vá para o mundo de terrenos.");
            return true;
        }

        if (args.length == 0) {
            getCommands(player);
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "reload":
                    terreno.reloadConfig();
                    configuration.initConfig();
                    sendMessage("$cConfiguração recarregada.", player);
                    break;
                case "info":
                    sendMessage(tools.getInfo(player.getLocation()), player);
                    break;
                case "tp":
                    sendMessage("$b Terrenos disponíveis para você neste mundo:", player);
                    List<String> regions = new PlayerManager(player, tools).getRegions();
                    if (!regions.isEmpty()) {
                        for (int x = 0; x < regions.size(); x++) {
                            sendMessage("$b Terreno ID: $c" + x, player);
                        }
                        sendMessage("$c use /terreno tp <ID> ", player);
                    } else {
                        sendMessage("$c Você não tem terrenos.", player);
                    }
                    if (player.isOp())
                        sendMessage("$c Use /terreno tp <ID> <NICK>", player);
                    break;
                case "deletar":
                    tools.deleteRegion(player);
                    break;
                case "comprar":
                    sendMessage("$bLotes disponíveis para compra:", player);
                    for (TiposTerrenos tiposTerrenos : TiposTerrenos.values()) {
                        final int size = configuration.getSizeByType(tiposTerrenos);
                        final Double price = configuration.getPriceByType(tiposTerrenos);
                        final String worldName = player.getWorld().getName().toLowerCase(Locale.ROOT);
                        final String formattedPrice = NumberFormat.getInstance().format(tools.multiplicarValor(worldName, price));

                        sendMessage(String.format("$b» $e%s $b(%dx%d) Valor: $e%s", tiposTerrenos.name(), size, size, formattedPrice), player);
                    }

                    sendMessage("$c/terreno comprar <tamanho>", player);
                    break;
                case "desativar":
                    sendMessage("$b Disponíveis para desativação", player);
                    for (Bloqueaveis b : Bloqueaveis.values()) {
                        final Price price = configuration.getLockablePricesByType(b);
                        sendMessage("$b " + b.getLockableType().getName() + " $e" + configuration.getLockableNameByType(b) + "$b Valor: $e"
                                + NumberFormat.getInstance().format(price.getDesactive()), player);
                    }
                    sendMessage("$c /terreno desativar <COMANDO | FUNÇÃO>", player);
                    break;
                case "ativar":
                    sendMessage("$c Disponíveis para ativação", player);
                    for (Bloqueaveis b : Bloqueaveis.values()) {
                        final Price price = configuration.getLockablePricesByType(b);
                        sendMessage("$b " + b.getLockableType().getName() + " $e" + configuration.getLockableNameByType(b) + "$b Valor: $e"
                                + NumberFormat.getInstance().format(price.getActive()), player);
                    }
                    sendMessage("$c /terreno desativar <COMANDO | FUNÇÃO>", player);
                    break;
                case "amigo":
                    sendMessage("$b Você pode adicionar ou remover amigos no seu terreno", player);
                    sendMessage("$c /terreno amigo <add | remover> <NICK>", player);
                    break;
                case "adquirir":
                    Venda venda = Venda.getVendaByBuyer(player);
                    if (venda == null) {
                        sendMessage("$c Nada para comprar", player);
                        break;
                    }
                    venda.efetuarCompra(player);
                    break;
                case "vender":
                    sendMessage("$c /terreno vender <nick> <valor>", player);
                    break;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("tp")) {
                tools.teleportPlayerToRegion(player, Integer.parseInt(args[1]));
                return true;
            }
            if (args[0].equalsIgnoreCase("comprar")) {
                switch (args[1].toLowerCase()) {
                    case "pequeno":
                        tools.createRegionForPlayer(player, TiposTerrenos.PEQUENO);
                        break;
                    case "medio":
                        tools.createRegionForPlayer(player, TiposTerrenos.MEDIO);
                        break;
                    case "grande":
                        tools.createRegionForPlayer(player, TiposTerrenos.GRANDE);
                        break;
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("desativar")) {
                switch (args[1].toLowerCase()) {
                    case "sethome":
                        tools.blockedCommands(player, Bloqueaveis.SET_HOME);
                        break;
                    case "tpaccept":
                        tools.blockedCommands(player, Bloqueaveis.TP_ACCEPT);
                        break;
                    case "pvp":
                        tools.blockedCommands(player, Bloqueaveis.PVP);
                        break;
                    case "entrada":
                        tools.blockedCommands(player, Bloqueaveis.ENTRY);
                        break;
                    case "fix":
                        tools.blockedCommands(player, Bloqueaveis.FIX);
                        break;
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("ativar")) {
                switch (args[1].toLowerCase()) {
                    case "sethome":
                        tools.allowedCommands(player, Bloqueaveis.SET_HOME);
                        break;
                    case "tpaccept":
                        tools.allowedCommands(player, Bloqueaveis.TP_ACCEPT);
                        break;
                    case "pvp":
                        tools.allowedCommands(player, Bloqueaveis.PVP);
                        break;
                    case "entrada":
                        tools.allowedCommands(player, Bloqueaveis.ENTRY);
                        break;
                    case "fix":
                        tools.allowedCommands(player, Bloqueaveis.FIX);
                        break;
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("amigo")
                    && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remover"))) {
                sendMessage("$c /terreno amigo <add | remover> <NICK>", player);
                return true;
            }
            getCommands(player);
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("amigo")) {
                if (args[1].equalsIgnoreCase("add")) {
                    tools.addMember(player, args[2]);
                }
                if (args[1].equalsIgnoreCase("remover")) {
                    tools.delMember(player, args[2]);
                }
            }
            if (args[0].equalsIgnoreCase("tp")) {
                tools.teleportPlayerToRegion(player, args[2], args[1]);
            }
            if (args[0].equalsIgnoreCase("vender")) {
                Player buyer = Bukkit.getPlayerExact(args[1]);
                if (buyer == null) {
                    sendMessage("$cJogador offline.", player);
                    return true;
                }
                if (Venda.currentSelling.containsKey(new GenericPlayer(player))) {
                    sendMessage("$c Você já tem uma venda em aberto", player);
                    return true;
                }
                if (tools.getRegion(player.getLocation()) == null) {
                    sendMessage("$c Você não esta em um terreno", player);
                    return true;
                }
                if (!tools.isOwner(player, tools.getRegion(player.getLocation()))) {
                    sendMessage("$c Você não é o dono do terreno", player);
                    return true;
                }
                if (buyer.equals(player)) {
                    sendMessage("$c Você não pode vender para esse jogador ", player);
                    return true;
                }
                Venda.registrarVenda(
                        new Venda(player, buyer, tools.getRegion(player.getLocation()), Double.parseDouble(args[2])));
            }
        }
        return true;
    }

    private void getCommands(Player p) {
        sendMessage("$b $3[EndCraft] $bComandos disponiveis neste local:", p);
        sendMessage("$b» /Terreno tp - $eSe teleportar para algum terreno seu.", p);
        if (tools.checkRegionNear(p, 1)) {
            sendMessage("$b» /Terreno comprar - $eComprar um terreno.", p);
        } else {
            sendMessage("$b» /Terreno info - $eVer informações desta área", p);
            sendMessage("$b» /Terreno ativar <Comando | função>", p);
            sendMessage("$b» /Terreno deletar - $eDeletar o terreno em que você esta.", p);
            sendMessage("$b» /Terreno desativar <Comando | função>", p);
            sendMessage("$b» /terreno amigo <add | remover> <NICK>", p);
            sendMessage("$b» /terreno vender <nick> <valor>", p);
        }
        if (p.isOp()) {
            sendMessage("$c» /Terreno tp <ID> <NICK> - $eSe teleportar para algum terreno.", p);
        }
    }
}
