package celdas.fiuba.assets;

import java.util.ArrayList;

import ontology.Types.ACTIONS;

public class Strategy {
	
	ArrayList<ACTIONS> accionesEstrategia = new ArrayList<ACTIONS>();
	ArrayList<State> situacionesEstrategia = new ArrayList<State>();
	int valUltimaAccion = -1;
	double utilObjetiv = -1;
	
	public ACTIONS ejecutarSiguienteAccion() {
		ACTIONS accionAEjecutar;
		if (valUltimaAccion < accionesEstrategia.size() - 1) {
			accionAEjecutar = accionesEstrategia.get(valUltimaAccion + 1);
			valUltimaAccion ++;
		} else {
			accionAEjecutar = ACTIONS.ACTION_NIL;			
		}
		return accionAEjecutar;
	}
	
	public double getUtilObjetiv() {
		return this.utilObjetiv;
	}
	
	public boolean enEjecucion() {
		return (utilObjetiv > 0);
	}
	
	public boolean seLlegoAlObjetivo() {
		return valUltimaAccion == accionesEstrategia.size() - 1;
	}
	
	public boolean cumpleElPlan(State situacionActual) {
		if (valUltimaAccion >= 0)
			return (situacionesEstrategia.get(valUltimaAccion).includesElem(situacionActual));
		return false;
	}
	
	public State obtenerSituacionObjetivo() {
		if (situacionesEstrategia.size() > 0)
			return situacionesEstrategia.get(situacionesEstrategia.size() - 1);
		return null;
	}
	
	public void inicializarEstrategia(ArrayList<State> situacionesEstrategia,
			ArrayList<ACTIONS> accionesDeEstrategia, double utilidadObjetivo) {
		this.reiniciar();
		
		for (State situacion: situacionesEstrategia)
			this.situacionesEstrategia.add(situacion);
		
		for (ACTIONS accionPlan: accionesDeEstrategia)
			this.accionesEstrategia.add(accionPlan);
		
		this.utilObjetiv = utilidadObjetivo;
		
	}
	
	public void reiniciar() {
		situacionesEstrategia.clear();
		accionesEstrategia.clear();
		valUltimaAccion = -1;
		utilObjetiv = -1;
	}

	public void setSituacionesPlan(ArrayList<State> caminoSituaciones) {
		this.situacionesEstrategia = caminoSituaciones;
		
	}

	public void setAccionesPlan(ArrayList<ACTIONS> accionesARealizar) {
		this.accionesEstrategia = accionesARealizar;
		
	}

	public void setUtilidadObjetivo(double utilidadObjetivo) {
		this.utilObjetiv = utilidadObjetivo;
		
	}
}
