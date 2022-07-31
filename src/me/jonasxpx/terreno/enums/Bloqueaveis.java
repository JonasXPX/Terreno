package me.jonasxpx.terreno.enums;

public enum Bloqueaveis {

    SET_HOME(LockableType.COMMAND),
    TP_ACCEPT(LockableType.COMMAND),
    PVP(LockableType.FUNCTION),
    ENTRY(LockableType.FUNCTION),
    FIX(LockableType.COMMAND);

    LockableType lockableType;

    Bloqueaveis(LockableType lockableType) {
        this.lockableType = lockableType;
    }

    public LockableType getLockableType() {
        return lockableType;
    }
}
