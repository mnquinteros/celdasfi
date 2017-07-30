package celdas.fiuba.symbols;

public class MultiCajaAgenteP extends Simbolo {

	public MultiCajaAgenteP() {
		this.simbolo = "P";
	}
	public boolean incluyeA(Simbolo otroSimbolo) {
		return ((otroSimbolo.getSimbolo().equals("A")) || (otroSimbolo.getSimbolo().equals("B"))
				|| (otroSimbolo.getSimbolo().equals("Y")) || (otroSimbolo.getSimbolo().equals("Z")) );
	}

}
