package me.jonasxpx.terreno;

public enum TiposTerrenos {

	PEQUENO(Terreno.instance.tamPequeno, "Pequeno", Terreno.instance.valorPequeno),
	MEDIO(Terreno.instance.tamMedio, "Medio", Terreno.instance.valorMedio),
	GRANDE(Terreno.instance.tamGrande, "Grande", Terreno.instance.valorGrande);
	
	private int tamanho;
	private String nome;
	private double valor;
	public int getTamanho(){
		return tamanho;
	}
	public String getName(){
		return nome;
	}
	public double getValor(){
		return valor;
	}
	
	public double getValor(int multiplicador){
		return valor * multiplicador;
	}
	
	private TiposTerrenos(int tamanho, String nome, double valor) {
		this.tamanho = tamanho;
		this.nome = nome;
		this.valor = valor;
	}

	public static TiposTerrenos getByName(String name){
		switch (name.toLowerCase()) {
		case "pequeno":
			return PEQUENO;
		case "medio":
			return MEDIO;
		case "grande":
			return GRANDE;
		default:
			return null;
		}
	}
}
