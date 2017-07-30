package celdas.fiuba.symbols;

public class SignoDePregunta extends Simbolo {

	public SignoDePregunta() {
		this.simbolo = new String("?");
	}

	public boolean incluyeA(Simbolo simbolo) {
		return true;
	}

}
