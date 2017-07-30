package celdas.fiuba.symbols;

public abstract class Simbolo {
	
	protected String simbolo;
	
	public String getSimbolo() {
		return this.simbolo;
	}
	
	public boolean compararCon(Simbolo simbolo) {
		return this.simbolo.equals(simbolo.getSimbolo());
	}
	
	public boolean incluyeA(Simbolo simbolo){
		return this.compararCon(simbolo);
	}
}
