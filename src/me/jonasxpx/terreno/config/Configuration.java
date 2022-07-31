package me.jonasxpx.terreno.config;

import com.sk89q.worldedit.patterns.BlockChance;
import me.jonasxpx.terreno.enums.Bloqueaveis;
import me.jonasxpx.terreno.enums.TiposTerrenos;
import me.jonasxpx.terreno.data.Price;

import java.util.List;
import java.util.Map;

public interface Configuration {

    void initConfig();

    double getValorPequeno();

    void setValorPequeno(double valorPequeno);

    double getValorMedio();

    void setValorMedio(double valorMedio);

    double getValorGrande();

    void setValorGrande(double valorGrande);

    double getValorDesativarPvp();

    void setValorDesativarPvp(double valorDesativarPvp);

    double getValorAtivarPvp();

    void setValorAtivarPvp(double valorAtivarPvp);

    double getValorDesativarCmd();

    void setValorDesativarCmd(double valorDesativarCmd);

    double getValorAtivarCmd();

    void setValorAtivarCmd(double valorAtivarCmd);

    double getValorAtivarEntrada();

    void setValorAtivarEntrada(double valorAtivarEntrada);

    double getValorDesativarEntrada();

    void setValorDesativarEntrada(double valorDesativarEntrada);

    double getValorDesativarFix();

    void setValorDesativarFix(double valorDesativarFix);

    double getValorAtivarFix();

    void setValorAtivarFix(double valorAtivarFix);

    int getTamPequeno();

    void setTamPequeno(int tamPequeno);

    int getTamMedio();

    void setTamMedio(int tamMedio);

    int getTamGrande();

    void setTamGrande(int tamGrande);

    boolean isCriarCercado();

    void setCriarCercado(boolean criarCercado);

    List<BlockChance> getCercado();

    void setCercado(List<BlockChance> cercado);

    Map<String, Integer> getMultiplicador();

    Map<String, Map<TiposTerrenos, String>> getSchematic();

    Map<String, String> getLang();

    List<String> getWorlds();

    void setWorlds(List<String> worlds);

    Double getPriceByType(TiposTerrenos tiposTerrenos);

    Integer getSizeByType(TiposTerrenos tiposTerrenos);

    String getLockableNameByType(Bloqueaveis bloqueaveis);

    Price getLockablePricesByType(Bloqueaveis bloqueaveis);
}
