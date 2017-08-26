package celdas.fiuba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import celdas.fiuba.assets.State;
import ontology.Types.ACTIONS;
import tools.Vector2d;

public class UtililyCalculator {
	
	public static double getTheoryScore(State condition, ACTIONS action, State effect) { //
		
		HashMap<Character, ArrayList<Vector2d>> positionsIC, positionsEff;
		positionsIC = condition.getElementPositions();
		positionsEff = effect.getElementPositions();
		
		int cantidadCajasSueltasCI = 0;
		int cantidadCajasEnObjetivosCI = 0;
		
		if (positionsIC.containsKey("1"))
			cantidadCajasSueltasCI = positionsIC.get("1").size();
		
		if (positionsIC.containsKey("X"))
			cantidadCajasEnObjetivosCI = positionsIC.get("X").size();
				
		int cantidadCajasCI = cantidadCajasSueltasCI + cantidadCajasEnObjetivosCI;
		
		
		int cantidadCajasSueltasEP = 0;
		int cantidadCajasEnObjetivosEP = 0;
		
		if (positionsEff.containsKey("1"))
			cantidadCajasSueltasEP = positionsEff.get("1").size();
		
		if (positionsEff.containsKey("X"))
			cantidadCajasEnObjetivosEP = positionsEff.get("X").size();
				
		int cantidadCajasEP = cantidadCajasSueltasEP + cantidadCajasEnObjetivosEP;

		
		boolean habiaCajas = (cantidadCajasCI > 0);
		boolean hayCajas = (cantidadCajasEP > 0);
		boolean personajeSeMovio = (!(condition.isSamePosition(effect)));
		
		if (!hayCajas) {
			if (habiaCajas) {
				return 0.01;
			} else {
				if (!personajeSeMovio)
					return 0.0625;
				else
					return 0.125;
			}
		} else {
			return calcularUtilidadSiHayCajas(positionsIC, positionsEff,
					cantidadCajasSueltasCI, cantidadCajasSueltasEP, cantidadCajasEnObjetivosCI, cantidadCajasEnObjetivosEP,
					condition, effect);
		}
	}
	
	public static double calcularUtilidadSiHayCajas(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
								HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
								int cantidadCajasSueltasCI, int cantidadCajasSueltasEP,
								int cantidadCajasEnObjetivosCI, int cantidadCajasEnObjetivosEP,
								State condicionInicial, State efectosPredichos) { //
		
		int cantidadPersEnObjetivosEP = 0;
		int cantidadObtetivosLibresEP = 0;
		
		if (posicionesCadaTipoDeElementoEP.containsKey("Y"))
			cantidadPersEnObjetivosEP = posicionesCadaTipoDeElementoEP.get("Y").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("Z"))
			cantidadPersEnObjetivosEP += posicionesCadaTipoDeElementoEP.get("Z").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("0"))
			cantidadObtetivosLibresEP = posicionesCadaTipoDeElementoEP.get("0").size();
		
		int cantidadObjetivosEP = cantidadObtetivosLibresEP + cantidadCajasEnObjetivosEP + cantidadPersEnObjetivosEP;
		boolean hayObjetivos = (cantidadObjetivosEP > 0);
		
		if (!hayObjetivos)
			return calcularUtilidadSinObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
											cantidadCajasEnObjetivosCI, condicionInicial, efectosPredichos);
		else
			return calcularUtilidadConObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasEP, cantidadCajasEnObjetivosCI, cantidadCajasEnObjetivosEP, 
					condicionInicial, efectosPredichos);
	}

	public static double calcularUtilidadSinObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI, 
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, int cantidadCajasEnObjetivosCI,
			State condicionInicial, State efectosPredichos) { //
		
		int cantidadPersEnObjetivosCI = 0;
		int cantidadObtetivosLibresCI = 0;
		
		if (posicionesCadaTipoDeElementoCI.containsKey("Y"))
			cantidadPersEnObjetivosCI = posicionesCadaTipoDeElementoCI.get("Y").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("Z"))
			cantidadPersEnObjetivosCI += posicionesCadaTipoDeElementoCI.get("Z").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("0"))
			cantidadObtetivosLibresCI = posicionesCadaTipoDeElementoCI.get("0").size();
		
		int cantidadObjetivosCI = cantidadObtetivosLibresCI + cantidadCajasEnObjetivosCI + cantidadPersEnObjetivosCI;
		boolean habiaObjetivos = (cantidadObjetivosCI > 0);
		
		if (habiaObjetivos)
			return 0.1865;
		else
			return calcularUtilidadSinObjetivosCINiEP(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					condicionInicial, efectosPredichos);
	}
	
	public static double calcularUtilidadSinObjetivosCINiEP(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			State condicionInicial, State efectosPredichos) { //
		
		
		ArrayList<Vector2d> posicionesCajasSueltasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			posicionesCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1");
		
		ArrayList<Vector2d> posicionesCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1");
		
		boolean seMovioAlgunaCaja = false;
		if (posicionesCajasSueltasCI == null) {
			seMovioAlgunaCaja = true;
		} else {
			if (posicionesCajasSueltasCI.size() != posicionesCajasSueltasEP.size()) {
				seMovioAlgunaCaja = true;
			} else {
				for (Vector2d posicionCajaCI: posicionesCajasSueltasCI) {
					if (!(posicionesCajasSueltasEP.contains(posicionCajaCI))) {
						seMovioAlgunaCaja = true;
						break;
					}
				}
			}
		}
		
		if (!seMovioAlgunaCaja)
			return calcularUtilidadSiNoSeMovieronCajas(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					posicionesCajasSueltasCI, posicionesCajasSueltasEP,
					condicionInicial, efectosPredichos);
		else
			return 0.4365;
	}

	public static double calcularUtilidadSiNoSeMovieronCajas(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			ArrayList<Vector2d> posicionesCajasSueltasCI, ArrayList<Vector2d> posicionesCajasSueltasEP,
			State condicionInicial, State efectosPredichos) { //
		
		Vector2d posicionPersonaje = new Vector2d(3,3);
		
		double distMinimaACajasCI = 100;
		for (Vector2d posicionCajaCI: posicionesCajasSueltasCI) {
			double distanciaACaja = posicionPersonaje.dist(posicionCajaCI);
			if (distanciaACaja < distMinimaACajasCI)
				distMinimaACajasCI = distanciaACaja;
		}
		
		double distMinimaACajasEP = 100;
		for (Vector2d posicionCajaEP: posicionesCajasSueltasEP) {
			double distanciaACaja = posicionPersonaje.dist(posicionCajaEP);
			if (distanciaACaja < distMinimaACajasEP)
				distMinimaACajasEP = distanciaACaja;
		}
		
		boolean seAlejoDeLasCajas = (distMinimaACajasCI < distMinimaACajasEP);
		if (seAlejoDeLasCajas) {
			return 0.25;
		} else {
			boolean seMovioElPersonaje = (!(condicionInicial.isSamePosition(efectosPredichos)));
			if (!seMovioElPersonaje)
				return 0.3125;
			else
				return (0.365 + 0.0625 / distMinimaACajasEP);
		}
	}

	public static double calcularUtilidadConObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasSueltasEP, int cantidadCajasEnObjetivosCI,
			int cantidadCajasEnObjetivosEP, State condicionInicial,
			State efectosPredichos) { //

		
		ArrayList<Vector2d> posicionesCajasSueltasEP = null;
		if (posicionesCadaTipoDeElementoEP.containsKey("1"))
			posicionesCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1");
		
		ArrayList<Vector2d> posicionesObjetivosSinCajasEP = null;
		if (posicionesCadaTipoDeElementoEP.containsKey("0"))
			posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("0");
		if (posicionesCadaTipoDeElementoEP.containsKey("Y")) {
			if (posicionesObjetivosSinCajasEP == null)
				posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("Y");
			else
				posicionesObjetivosSinCajasEP.addAll(posicionesCadaTipoDeElementoEP.get("Y"));
		}
		if (posicionesCadaTipoDeElementoEP.containsKey("Z")) {
			if (posicionesObjetivosSinCajasEP == null)
				posicionesObjetivosSinCajasEP = posicionesCadaTipoDeElementoEP.get("Z");
			else
				posicionesObjetivosSinCajasEP.addAll(posicionesCadaTipoDeElementoEP.get("Z"));
		}
		
		boolean hayCajasEnObjetivos = (cantidadCajasEnObjetivosEP > 0);
		if (!hayCajasEnObjetivos)
			return calcularUtilidadSinCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasEnObjetivosCI, condicionInicial, efectosPredichos ,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
		else
			return calcularUtilidadConCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasEP, cantidadCajasEnObjetivosEP,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
	}	
	
	public static double calcularUtilidadSinCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasEnObjetivosCI, State condicionInicial,
			State efectosPredichos,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
		
		boolean habiaCajasEnObjetivos = (cantidadCajasEnObjetivosCI > 0);
		if (habiaCajasEnObjetivos)
			return 0.5;
		else
			return calcularUtilidadSinCajasEnObjetivosCINiEP(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					condicionInicial, efectosPredichos,
					posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP);
			
	}

	public static double calcularUtilidadSinCajasEnObjetivosCINiEP(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			State condicionInicial, State efectosPredichos,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
			
		boolean aumentoDistanciaCajasObjetivos = false;		
		
		double distMinimaCajasObjetivosEP = 100;		
		for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
			for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
				double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
				if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
					distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
			}
		}
		
		
		ArrayList<Vector2d> posicionesCajasSueltasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			posicionesCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1");
		
		ArrayList<Vector2d> posicionesObjetivosSinCajasCI = null;
		if (posicionesCadaTipoDeElementoCI.containsKey("0"))
			posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("0");
		if (posicionesCadaTipoDeElementoCI.containsKey("Y")) {
			if (posicionesObjetivosSinCajasCI == null)
				posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("Y");
			else
				posicionesObjetivosSinCajasCI.addAll(posicionesCadaTipoDeElementoCI.get("Y"));
		}
		if (posicionesCadaTipoDeElementoCI.containsKey("Z")) {
			if (posicionesObjetivosSinCajasCI == null)
				posicionesObjetivosSinCajasCI = posicionesCadaTipoDeElementoCI.get("Z");
			else
				posicionesObjetivosSinCajasCI.addAll(posicionesCadaTipoDeElementoCI.get("Z"));
		}
		
		
		double distMinimaCajasObjetivosCI = 100;
		if (!(posicionesCajasSueltasCI == null || posicionesObjetivosSinCajasCI == null)) {
			
			for (Vector2d posicionCajaCI: posicionesCajasSueltasCI){
				for (Vector2d posicionObjetivoCI: posicionesObjetivosSinCajasCI) {
					double distanciaCajaObjetivo = posicionCajaCI.dist(posicionObjetivoCI);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosCI)
						distMinimaCajasObjetivosCI = distanciaCajaObjetivo;
				}
			}			
			
			aumentoDistanciaCajasObjetivos = (distMinimaCajasObjetivosEP > distMinimaCajasObjetivosCI);
		}
		
		if (aumentoDistanciaCajasObjetivos) {
			return 0.5625;
		} else {
			boolean seMovioPersonaje = (!(condicionInicial.isSamePosition(efectosPredichos)));
			if (!seMovioPersonaje)
				return 0.625;
			else
				return 0.6825 + 0.0625 / distMinimaCajasObjetivosEP;
		}
		
		
	}
	
	public static double calcularUtilidadConCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP,
			int cantidadCajasSueltasEP, int cantidadCajasEnObjetivosEP,
			ArrayList<Vector2d> posicionesCajasSueltasEP, ArrayList<Vector2d> posicionesObjetivosSinCajasEP) { //
		
		if (cantidadCajasEnObjetivosEP != 3){
			if (cantidadCajasEnObjetivosEP != 2)
				return calcularUtilidadConUnaCajaEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP, 
						posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP, cantidadCajasSueltasEP);
			else
				return calcularUtilidadConDosCajasEnObjetivos(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
						posicionesCajasSueltasEP, posicionesObjetivosSinCajasEP, cantidadCajasSueltasEP);
		} else {
			return 1;
		}
	}

	public static double calcularUtilidadConUnaCajaEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, ArrayList<Vector2d> posicionesCajasSueltasEP,
			ArrayList<Vector2d> posicionesObjetivosSinCajasEP, int cantidadCajasSueltasEP) {
		
		boolean hayCajasSueltas = (cantidadCajasSueltasEP > 0);
		if (!hayCajasSueltas)
			return 0.65;
		else {
			double distMinimaCajasObjetivosEP = 100;
			for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
				for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
					double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
						distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
				}
			}
			return 0.8125 + 0.0625 / distMinimaCajasObjetivosEP;
		}
	}
	
	public static double calcularUtilidadConDosCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
			HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoEP, ArrayList<Vector2d> posicionesCajasSueltasEP,
			ArrayList<Vector2d> posicionesObjetivosSinCajasEP, int cantidadCajasSueltasEP) {
		
		boolean hayCajasSueltas = (cantidadCajasSueltasEP > 0);
		if (!hayCajasSueltas)
			return 0.865;
		else {
			double distMinimaCajasObjetivosEP = 100;
			for (Vector2d posicionCajaEP: posicionesCajasSueltasEP){
				for (Vector2d posicionObjetivoEP: posicionesObjetivosSinCajasEP) {
					double distanciaCajaObjetivo = posicionCajaEP.dist(posicionObjetivoEP);
					if (distanciaCajaObjetivo < distMinimaCajasObjetivosEP)
						distMinimaCajasObjetivosEP = distanciaCajaObjetivo;
				}
			}
			return 0.9365 + 0.0625 / distMinimaCajasObjetivosEP;
		}
	}
	
	public static Theory obtenerTeoriaConMayorUtilidad(List<Theory> teorias) {
		double utilidadMax = 0;
		Theory teoriaConUtilidadMax = null;
		
		for (Theory teoria : teorias) {
			if (teoria.getU() >= utilidadMax){
				utilidadMax = teoria.getU();
			}
		}
		
		if (utilidadMax == 0)
			return null;
		
		ArrayList<Theory> teoriasConUtilidadMax = new ArrayList<Theory>();
		for (Theory teoria : teorias) {
			if (teoria.getU() == utilidadMax){
				teoriasConUtilidadMax.add(teoria);
			}
		}
		
		double maxPorcentajeExitos = 0;
		for (Theory teoria : teoriasConUtilidadMax) {
			double porcentajeExitos = teoria.getP() / teoria.getK();
			if (porcentajeExitos >= maxPorcentajeExitos){
				maxPorcentajeExitos = porcentajeExitos;
				teoriaConUtilidadMax = teoria;
			}
		}
		
		return (teoriaConUtilidadMax);
	}

}
