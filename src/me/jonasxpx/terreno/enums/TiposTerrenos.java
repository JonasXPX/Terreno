package me.jonasxpx.terreno.enums;

public enum TiposTerrenos {

	PEQUENO,
	MEDIO,
	GRANDE;

	TiposTerrenos() {
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
