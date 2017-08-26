package celdas.fiuba;

import celdas.fiuba.assets.State;
import ontology.Types.ACTIONS;

public class Theory {
	private int id;
	private String condicionInicial;
	private int idSitCondicionInicial;
	private String accion;
	private String efectosPredichos;
	private int idSitEfectosPredichos;
	private double k;
	private double p;
	private double u;
	
	public Theory() {
	}
	
	public Theory(int id, String condicionInicial, int idSitCondicionInicial, String accion, 
				  String efectosPredichos, int idSitEfectosPredichos, double k, 
				  double p, double u) {
		this.setId(id);
		this.setCondicionInicial(condicionInicial);
		this.setIdSitCondicionInicial(idSitCondicionInicial);
		this.setAccion(accion);
		this.setEfectosPredichos(efectosPredichos);
		this.setIdSitEfectosPredichos(idSitEfectosPredichos);
		this.setK(k);
		this.setP(p);
		this.setU(u);
	}

	public Theory(int id, State condicionInicial, ACTIONS accion, 
				  State efectosPredichos, double k, 
				  double p, double u) {
		this.setId(id);
		this.setCondicionInicial(condicionInicial);
		this.setIdSitCondicionInicial(condicionInicial.getId());
		this.setEfectosPredichos(efectosPredichos);
		this.setIdSitEfectosPredichos(efectosPredichos.getId());
		this.setAccion(accion);
		this.setK(k);
		this.setP(p);
		this.setU(u);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getCondicionInicial() {
		return condicionInicial;
	}

	public void setCondicionInicial(String condicionInicial) {
		this.condicionInicial = condicionInicial;
	}
	
	public void setCondicionInicial(State condicionInicial) {
		setCondicionInicial(condicionInicial.getStringState());// obtenerSituacionComoString(condicionInicial));
	}
	
	public String getEfectosPredichos() {
		return efectosPredichos;
	}

	public void setEfectosPredichos(String efectosPredichos) {
		this.efectosPredichos = efectosPredichos;
	}
	
	public void setEfectosPredichos(State efectosPredichos) {
		this.setEfectosPredichos(efectosPredichos.getStringState());// obtenerSituacionComoString(efectosPredichos));
	}
	
//	private String obtenerSituacionComoString(State situacion) {
//		char[][] casillerosSituacion = situacion.getMap();
//		String situacionString = "";
//		for (int fila = 0; fila < 6; fila++) {
//			for (int col = 0; col < 6; col++) {
//				situacionString += casillerosSituacion[fila][col];
//			}
//			situacionString += "|";
//		}
//		return situacionString;
//	}
	
	public int getIdSitCondicionInicial() {
		return idSitCondicionInicial;
	}

	public void setIdSitCondicionInicial(int idSitCondicionInicial) {
		this.idSitCondicionInicial = idSitCondicionInicial;
	}

	public String getAccionComoString() {		
		return (this.accion);
	}
	
	public ontology.Types.ACTIONS getAccionComoAction() {
		if (this.accion.equals("up")){
			return (ACTIONS.ACTION_UP);
		}
		
		if (this.accion.equals("down")){
			return (ACTIONS.ACTION_DOWN);
		}
		
		if (this.accion.equals("left")){
			return (ACTIONS.ACTION_LEFT);
		}
		
		if (this.accion.equals("right")){
			return (ACTIONS.ACTION_RIGHT);
		}
		
		return (ACTIONS.ACTION_NIL);
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}
	
	public void setAccion(ontology.Types.ACTIONS accion) {
		if (accion.equals(ACTIONS.ACTION_UP)){
			this.accion = "up";
		}
		
		if (accion.equals(ACTIONS.ACTION_DOWN)){
			this.accion = "down";
		}
		
		if (accion.equals(ACTIONS.ACTION_LEFT)){
			this.accion = "left";
		}
		
		if (accion.equals(ACTIONS.ACTION_RIGHT)){
			this.accion = "right";
		}
		
		if (accion.equals(ACTIONS.ACTION_NIL)){
			this.accion = "nil";
		}
	}

	
	public int getIdSitEfectosPredichos() {
		return idSitEfectosPredichos;
	}

	public void setIdSitEfectosPredichos(int idSitEfectosPredichos) {
		this.idSitEfectosPredichos = idSitEfectosPredichos;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}

	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public double getU() {
		return u;
	}

	public void setU(double u) {
		this.u = u;
	}
	
	public State getSitCondicionInicial() {
		return new State(this.idSitCondicionInicial, this.condicionInicial);
	}
	
	public State getSitEfectosPredichos() {
		return new State(this.idSitEfectosPredichos, this.efectosPredichos);
	}	

}
