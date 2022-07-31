package me.jonasxpx.terreno.error;

public enum Erros {

	LIMITREGION("LIMITREGION"),
	NOMONEY("NOMONEY"),
	NEARREGION("NEARREGION"),
	NOREGION("NOREGION"),
	CONFIRMCOMMAND("CONFIRMCOMMAND"),
	NULLERRO("NULLERRO"),
	SUCESSDELETE("SUCESSDELETE"),
	NOOWNERDELETE("NOOWNERDELETE"),
	NOOWNER("NOOWNER");

	
	String string;
	
	Erros(String erro){this.string = erro;}

	public String getString() {
		return string;
	}

	
}
