package me.jonasxpx.terreno.enums;

public enum LockableType {
    FUNCTION("Função"),
    COMMAND("Comando");

    String name;
    LockableType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
