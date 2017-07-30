package celdas.fiuba.symbols;

public class CualquierObjetoMenosObjetivo extends Simbolo {

	public CualquierObjetoMenosObjetivo() {
		this.simbolo = "Q";
	}
	
	@Override
	public boolean incluyeA(Simbolo otroSimbolo) {
		return (!(otroSimbolo.getSimbolo().equals("0")));
	}

}
