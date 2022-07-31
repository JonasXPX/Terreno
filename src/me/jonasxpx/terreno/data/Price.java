package me.jonasxpx.terreno.data;

public class Price {

    private Double active;
    private Double desactive;

    public Price(Double active, Double desactive) {
        this.active = active;
        this.desactive = desactive;
    }

    public Price() {
    }

    public Double getActive() {
        return active;
    }

    public void setActive(Double active) {
        this.active = active;
    }

    public Double getDesactive() {
        return desactive;
    }

    public void setDesactive(Double desactive) {
        this.desactive = desactive;
    }
}
