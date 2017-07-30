package celdas.fiuba;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import celdas.fiuba.assets.DijkstraAlgorithm;
import celdas.fiuba.assets.Edge;
import celdas.fiuba.assets.Strategy;
import celdas.fiuba.assets.Graph;
import celdas.fiuba.assets.Perception;
import celdas.fiuba.assets.State;
import celdas.fiuba.assets.Vertex;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractMultiPlayer {
	
	private int posX, posY, idAgente;
	private ArrayList<Theory> teorias, teoriasPrecargadas, teoriasSinUtil;
	private ArrayList<State> knowledgeBase;
	private ArrayList<Vector2d> posicionesObjetivos;
	private ArrayList<Integer> idObjetivTomados = new ArrayList<Integer>();
	private Path pathTeorias, pathTeoriasPrecargadas;
	private Perception mapa;
	//private Gson gson;
	private State situacionAnterior = null;
	private Strategy estrategia = new Strategy();
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.mapa = new Perception(stateObs);
		//this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.teorias = new ArrayList<Theory>();
		this.teoriasPrecargadas = new ArrayList<Theory>();
		this.teoriasSinUtil = new ArrayList<Theory>();
		this.idAgente = playerID;
		//pathTeoriasPrecargadas = FileSystems.getDefault().getPath(System.getProperty("user.dir") + "/src/celdas/fiuba/teorias/TeoriasPrecargadas");
		//String stringPathTeorias = "/src/fiubaceldas/grupo02/teorias/TeoriaAgente_" + Integer.toString(this.idAgente);
		//this.pathTeorias = FileSystems.getDefault().getPath(System.getProperty("user.dir") + stringPathTeorias);
		this.posicionesObjetivos = mapa.getPosicionesObjetivos();
		this.ObtenerTeoriasPrecargadas();
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){
		this.mapa =  new Perception(stateObs);
		this.posX = (int)(stateObs.getAvatarPosition(this.idAgente).x) / this.mapa.getSpriteSizeWidthInPixels();
        this.posY = (int)(stateObs.getAvatarPosition(this.idAgente).y) / this.mapa.getSpriteSizeHeightInPixels();
		this.loadStrategies();
		this.knowledgeBase = this.cargarBaseDeConocimiento();
        
		if (this.situacionAnterior != null) {
			agregarNuevaSituacion(situacionAnterior);
		}
		State situacionActual = this.obtenerSituacionActual();
		ACTIONS ultimaAccion = stateObs.getAvatarLastAction(this.idAgente);
		if (situacionAnterior != null){
			Theory teoriaLocal = new Theory(this.teorias.size()+this.teoriasPrecargadas.size()+ 1, this.situacionAnterior, ultimaAccion, situacionActual, 1, 1, 
									calcularUtilidadTeoria(this.situacionAnterior, ultimaAccion, situacionActual));
			evaluarTeoria(teoriaLocal);
		}

		ACTIONS siguienteAccion = actualizarEstrategia(stateObs, situacionActual, this.generarGrafoDeTeorias());
		this.situacionAnterior = situacionActual;
		this.guardarTeorias();
		return siguienteAccion;
	}
	
	private double calcularUtilidadTeoria(State condicionInicial, ACTIONS accion, State efectosPredichos) { //

		if (this.sirveLaAccion(condicionInicial, accion))
			return 0.0;
		
		HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP;
		posicionesCadaTipoDeElementoCI = condicionInicial.obtenerPosicionesElementos();
		posicionesCadaTipoDeElementoEP = efectosPredichos.obtenerPosicionesElementos();
		
		int cantidadCajasSueltasCI = 0;
		int cantidadCajasEnObjetivosCI = 0;
		
		if (posicionesCadaTipoDeElementoCI.containsKey("1"))
			cantidadCajasSueltasCI = posicionesCadaTipoDeElementoCI.get("1").size();
		
		if (posicionesCadaTipoDeElementoCI.containsKey("X"))
			cantidadCajasEnObjetivosCI = posicionesCadaTipoDeElementoCI.get("X").size();
				
		int cantidadCajasCI = cantidadCajasSueltasCI + cantidadCajasEnObjetivosCI;
		
		
		int cantidadCajasSueltasEP = 0;
		int cantidadCajasEnObjetivosEP = 0;
		
		if (posicionesCadaTipoDeElementoEP.containsKey("1"))
			cantidadCajasSueltasEP = posicionesCadaTipoDeElementoEP.get("1").size();
		
		if (posicionesCadaTipoDeElementoEP.containsKey("X"))
			cantidadCajasEnObjetivosEP = posicionesCadaTipoDeElementoEP.get("X").size();
				
		int cantidadCajasEP = cantidadCajasSueltasEP + cantidadCajasEnObjetivosEP;

		
		boolean habiaCajas = (cantidadCajasCI > 0);
		boolean hayCajas = (cantidadCajasEP > 0);
		boolean personajeSeMovio = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
		
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
			return this.calcularUtilidadSiHayCajas(posicionesCadaTipoDeElementoCI, posicionesCadaTipoDeElementoEP,
					cantidadCajasSueltasCI, cantidadCajasSueltasEP, cantidadCajasEnObjetivosCI, cantidadCajasEnObjetivosEP,
					condicionInicial, efectosPredichos);
		}
	}
	
	private double calcularUtilidadSiHayCajas(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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

	private double calcularUtilidadSinObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI, 
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
	
	private double calcularUtilidadSinObjetivosCINiEP(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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

	private double calcularUtilidadSiNoSeMovieronCajas(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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
			boolean seMovioElPersonaje = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
			if (!seMovioElPersonaje)
				return 0.3125;
			else
				return (0.365 + 0.0625 / distMinimaACajasEP);
		}
	}

	private double calcularUtilidadConObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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
	
	private double calcularUtilidadSinCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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

	private double calcularUtilidadSinCajasEnObjetivosCINiEP(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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
			boolean seMovioPersonaje = (!(condicionInicial.mismaPosicionPersonaje(efectosPredichos)));
			if (!seMovioPersonaje)
				return 0.625;
			else
				return 0.6825 + 0.0625 / distMinimaCajasObjetivosEP;
		}
		
		
	}
	
	private double calcularUtilidadConCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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

	private double calcularUtilidadConUnaCajaEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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
	
	private double calcularUtilidadConDosCajasEnObjetivos(HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElementoCI,
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

	private Theory obtenerTeoriaConMayorUtilidad() {
		double utilidadMax = 0;
		Theory teoriaConUtilidadMax = null;
		
		for (Theory teoria : this.teorias) {
			if (teoria.getU() >= utilidadMax){
				utilidadMax = teoria.getU();
			}
		}
		
		if (utilidadMax == 0)
			return null;
		
		ArrayList<Theory> teoriasConUtilidadMax = new ArrayList<Theory>();
		for (Theory teoria : this.teorias) {
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
	
	private void evaluarTeoria(Theory teoriaLocal) {
		
		if (teoriaLocal != null) {					
			
			ArrayList<Theory> teoriasSimilares = this.buscarTeoriasSimilares(teoriaLocal, this.teorias);
			ArrayList<Theory> teoriasSimilaresPrecargadas = this.buscarTeoriasSimilares(teoriaLocal, this.teoriasPrecargadas);
			ArrayList<Theory> todasLasTeoriasSimilares = new ArrayList<Theory>();
			todasLasTeoriasSimilares.addAll(teoriasSimilares);
			todasLasTeoriasSimilares.addAll(teoriasSimilaresPrecargadas);
			
			boolean hayTeoriasSimilares = (todasLasTeoriasSimilares.size() > 0);			
			if (hayTeoriasSimilares) {
				Theory teoriaIgualALocal = this.buscarTeoriaIgual(teoriaLocal, todasLasTeoriasSimilares);
				if (teoriaIgualALocal != null) {
					teoriaIgualALocal.setP(teoriaIgualALocal.getP() + 1);
					for (Theory teoriaSimilar: todasLasTeoriasSimilares) {
						teoriaSimilar.setK(teoriaSimilar.getK() + 1);
					}
				} else {
					
					this.teorias.add(teoriaLocal);	
					
					State CITeoriaLocal = teoriaLocal.getSitCondicionInicial();					
					State EPTeoriaLocal = teoriaLocal.getSitEfectosPredichos();
					
					Theory teoriaSimilar = todasLasTeoriasSimilares.get(0);
					State EPTeoriaSimilar = teoriaSimilar.getSitEfectosPredichos();
					
					State EPTeoriaMutante = EPTeoriaLocal.generalizar(EPTeoriaSimilar, EPTeoriaLocal.getId()+1);
															
					for (int i = 1; i < todasLasTeoriasSimilares.size() && EPTeoriaMutante != null; i++) {
						EPTeoriaSimilar = todasLasTeoriasSimilares.get(i).getSitEfectosPredichos();
						EPTeoriaMutante = EPTeoriaMutante.generalizar(EPTeoriaSimilar, teoriaLocal.getId()+1);
					}
					
					double KTeoriasSimilares = teoriaSimilar.getK();
					double nuevoKTeoriasSimilares;
					
					if (EPTeoriaMutante != null) {
					
						double utilidadTeoriaMutante;
						if (teoriaLocal.getU() == 0.0)
							utilidadTeoriaMutante = 0.0;
						else
							utilidadTeoriaMutante = calcularUtilidadTeoria(CITeoriaLocal, teoriaLocal.getAccionComoAction(), EPTeoriaMutante);
						
						Theory teoriaMutante = new Theory(teoriaLocal.getId() + 1, 
												CITeoriaLocal, teoriaLocal.getAccionComoAction(), EPTeoriaMutante, 
												KTeoriasSimilares + 2, 1, utilidadTeoriaMutante);
						
						Theory teoriaIgualAMutante = this.buscarTeoriaIgual(teoriaMutante, todasLasTeoriasSimilares);
						
						if (teoriaIgualAMutante != null)
							nuevoKTeoriasSimilares = KTeoriasSimilares + 1;
						else
							nuevoKTeoriasSimilares = KTeoriasSimilares + 2;
						
						
						if (teoriaIgualAMutante != null) {
							teoriaIgualAMutante.setP(teoriaIgualAMutante.getP() + 1);
						} else {
							this.teorias.add(teoriaMutante);
						}
						
					} else {
						nuevoKTeoriasSimilares = KTeoriasSimilares + 1;
					}
					
					for (Theory teoria: todasLasTeoriasSimilares) {
						teoria.setK(nuevoKTeoriasSimilares);
					}
					
					teoriaLocal.setK(nuevoKTeoriasSimilares);					
									
				}
			} else {
				this.teorias.add(teoriaLocal);
				if (teoriaLocal.getU() == 0) {
					this.teoriasSinUtil.add(teoriaLocal);
				}
			}
		}
	}	
	
	private ArrayList<Theory> buscarTeoriasSimilares(Theory teoriaLocal, ArrayList<Theory> listaDeTeorias) {
		
		ArrayList<Theory> teoriasSimilares = new ArrayList<Theory>();
		State condicionInicialTeoriaLocal = teoriaLocal.getSitCondicionInicial();
		ACTIONS accionTeoriaLocal = teoriaLocal.getAccionComoAction();
		
		for (Theory teoria : listaDeTeorias) {
			if (teoria != null) {
				
				State condicionInicialTeoria = teoria.getSitCondicionInicial();
				ACTIONS accionTeoria = teoria.getAccionComoAction();									
				
				if (condicionInicialTeoria.includesElem(condicionInicialTeoriaLocal)
						&& accionTeoria == accionTeoriaLocal){
					
					teoriasSimilares.add(teoria);
				}
			}
		}	
		return teoriasSimilares;
	}

	private Theory buscarTeoriaIgual(Theory teoriaLocal, ArrayList<Theory> listaDeTeoriasSimilares) {

		State efectosPredichosTeoriaLocal = teoriaLocal.getSitEfectosPredichos();

		for (Theory teoria : listaDeTeoriasSimilares) {
			if (teoria != null) {				
				
				State efectosPredichosTeoria = teoria.getSitEfectosPredichos();
				
				if (efectosPredichosTeoria.includesElem(efectosPredichosTeoriaLocal)){
					return teoria;
				}
			}
		}	
		return null;
	}

	private ArrayList<Theory> teoriasAejecutar(ArrayList<State> caminoSituaciones) {
		ArrayList<Theory> camino = new ArrayList<Theory>();
		for (int nSitOrigen = 0; nSitOrigen < caminoSituaciones.size() - 1; nSitOrigen++) {
			int idSitOrigen = caminoSituaciones.get(nSitOrigen).getId();
			int idSitDestino = caminoSituaciones.get(nSitOrigen + 1).getId();
			
			boolean existeTeoria = false;
			for (Theory teoria: this.teorias) {
				if (teoria.getIdSitCondicionInicial() == idSitOrigen
						&& teoria.getIdSitEfectosPredichos() == idSitDestino) {
					camino.add(teoria);
					existeTeoria = true;
					break;
				}
			}
			
			if (!existeTeoria) {
				for (Theory teoria: this.teoriasPrecargadas) {
					if (teoria.getIdSitCondicionInicial() == idSitOrigen
							&& teoria.getIdSitEfectosPredichos() == idSitDestino) {
						camino.add(teoria);
						break;
					}
				}
			}
		}
		return camino;
	}
	
	private ACTIONS actualizarEstrategia(StateObservationMulti stateObs, State situacionActual, 
												Graph grafoTeoriasYSituaciones) {		
		if (estrategia.enEjecucion()) {
			if (estrategia.cumpleElPlan(situacionActual)) {
				if (!estrategia.seLlegoAlObjetivo()) {
					ACTIONS siguienteAccion = estrategia.ejecutarSiguienteAccion();
					if (!(this.sirveLaAccion(situacionActual, siguienteAccion))) {
						return siguienteAccion;
					} else {
						estrategia.reiniciar();
						return this.movimientoAleatorio(stateObs, situacionActual);
					}
				} else {
					if (estrategia.getUtilObjetiv() == 1) {
						return null;
					} else {
						this.idObjetivTomados.add(estrategia.obtenerSituacionObjetivo().getId());
						estrategia.reiniciar();
						return this.movimientoAleatorio(stateObs, situacionActual);
					}
				}
			} else {
				estrategia.reiniciar();				
				return this.movimientoAleatorio(stateObs, situacionActual);
			}
		} else {
			Theory teoriaNuevoObjetivo = obtenerTeoriaConMayorUtilidad();

			if (teoriaNuevoObjetivo == null) {
				return this.movimientoAleatorio(stateObs, situacionActual); 
			}
			
			State nuevoObjetivo = teoriaNuevoObjetivo.getSitEfectosPredichos();
			if (!(this.idObjetivTomados.contains(nuevoObjetivo.getId())))
				crearEstrategia(situacionActual, nuevoObjetivo, grafoTeoriasYSituaciones);
			
			if (estrategia.enEjecucion()) {
				ACTIONS siguienteAccion = estrategia.ejecutarSiguienteAccion();
				if (!(this.sirveLaAccion(situacionActual, siguienteAccion))) {
					return siguienteAccion;
				} else {
					estrategia.reiniciar();
					return this.movimientoAleatorio(stateObs, situacionActual);
				}
			} else {
				return this.movimientoAleatorio(stateObs, situacionActual);
			}
		}
	}
	
	private void crearEstrategia(State situacionActual, State situacionObjetivo, Graph grafoDeTeorias) {
		
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(grafoDeTeorias);
		Vertex nodoOrigen = grafoDeTeorias.getNode(situacionActual.getId());
		Vertex nodoDestino = grafoDeTeorias.getNode(situacionObjetivo.getId());
		dijkstra.execute(nodoOrigen);
		
		LinkedList<Vertex> caminoNodos = dijkstra.getPath(nodoDestino);
						
		if (caminoNodos != null)
			if (caminoNodos.size() >= 2) {
				
				ArrayList<State> caminoSituaciones = new ArrayList<State>();
				for (Vertex nodoSituacion: caminoNodos){
					for (State situacion: this.knowledgeBase) {
							
						if (situacion != null){
							int idNodo = Integer.parseInt(nodoSituacion.getId());
								
							if (idNodo == situacion.getId()) {
								caminoSituaciones.add(situacion);
								break;
							}
						}
					}
				}
				
				ArrayList<Theory> caminoTeoriasAcumplir = this.teoriasAejecutar(caminoSituaciones);
				
				caminoSituaciones.remove(0); //quitamos la primera situacion del estrategia porque es la actual
				
				ArrayList<ACTIONS> accionesARealizar = new ArrayList<ACTIONS>();
				for (Theory teoria: caminoTeoriasAcumplir)
					accionesARealizar.add(teoria.getAccionComoAction());
				
				Theory ultimaTeoriaACumplir = caminoTeoriasAcumplir.get(caminoTeoriasAcumplir.size() - 1);
				double utilidadObjetivo = ultimaTeoriaACumplir.getU();
				
				this.estrategia.setSituacionesPlan(caminoSituaciones);
				this.estrategia.setAccionesPlan(accionesARealizar);
				this.estrategia.setUtilidadObjetivo(utilidadObjetivo);			
				
			}
		 	
	}
	
	private ArrayList<State> cargarBaseDeConocimiento() {
		ArrayList<State> situacionesAcargar = new ArrayList<State>();
		HashMap<Integer,State> idSituaciones = new HashMap<Integer,State>();
		for (Theory teoria: teorias) {
			if (teoria != null) {
				
					State condicionInicial = teoria.getSitCondicionInicial();
					idSituaciones.put(condicionInicial.getId(), condicionInicial);

					State efectosPredichos = teoria.getSitEfectosPredichos();
					idSituaciones.put(efectosPredichos.getId(), efectosPredichos);
			}
		}
		for (Theory teoriaPrecargada: this.teoriasPrecargadas) {
			if (teoriaPrecargada != null) {
								
				State condicionInicial = teoriaPrecargada.getSitCondicionInicial();
				idSituaciones.put(condicionInicial.getId(), condicionInicial);

				State efectosPredichos = teoriaPrecargada.getSitEfectosPredichos();
				idSituaciones.put(efectosPredichos.getId(), efectosPredichos);
			}
		}
		
		for (State situacion: idSituaciones.values())
			situacionesAcargar.add(situacion);
		
		return situacionesAcargar;
	}
	
	public void agregarNuevaSituacion(State situacion) {
		boolean agregar = true;
		for (State s: this.knowledgeBase) {
			if (s.compare(situacion)) {
				agregar = false;
				break;
			}
		}
		if (agregar)
			this.knowledgeBase.add(situacion);
	}

	private ontology.Types.ACTIONS movimientoAleatorio(StateObservationMulti stateObs, State situacionActual){
		ArrayList<ACTIONS> accionesPosibles = stateObs.getAvailableActions(this.idAgente);
		ArrayList<ACTIONS> accionesNoPosibles = new ArrayList<>();
		
		while (accionesNoPosibles.size() != 4) {
			ACTIONS accionRandom = ACTIONS.values()[new Random().nextInt(accionesPosibles.size())];
			
			while (accionesNoPosibles.contains(accionRandom)){
				accionRandom = ACTIONS.values()[new Random().nextInt(accionesPosibles.size())];
			}
			
			if (situacionActual == null){
				return accionRandom;
			}
			
			if (this.sirveLaAccion(situacionActual, accionRandom)){
				accionesNoPosibles.add(accionRandom);
			} else {
				return accionRandom;
			}
		}
		
		return ACTIONS.ACTION_NIL;
	}
	
	boolean sirveLaAccion(State condicionInicial, ACTIONS accion) {
		for (Theory teoriaConUtilidadNula: this.teoriasSinUtil) {
			ACTIONS accionTeoriaUtilidadNula = teoriaConUtilidadNula.getAccionComoAction();
			
			if (accion.equals(accionTeoriaUtilidadNula)) {
				State CITeoriaUtilidadNula = teoriaConUtilidadNula.getSitCondicionInicial();
				
				if (CITeoriaUtilidadNula.includesElem(condicionInicial)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void loadStrategies(){
		//Type tipoArrayListTeoria = new TypeToken<ArrayList<Theory>>(){}.getType();
		
		ArrayList<Theory> teorias = new ArrayList<>(); //gson.fromJson(this.ObtenerPathDeTeorias(), tipoArrayListTeoria);
		
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader("resources/preloadedinfo.csv"))) {
			while((line = br.readLine()) != null ) {
				teorias.add(Theory.deserialize(line));
			}
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (teorias != null && teorias.size() > 0){
				this.teorias.clear();
				this.teorias.addAll(teorias);
			}
		}
		
	}
	
	private void ObtenerTeoriasPrecargadas(){
		//Type tipoArrayListTeoria = new TypeToken<ArrayList<Theory>>(){}.getType();
		ArrayList<Theory> teoriasPrecargadas = new ArrayList<>();//gson.fromJson(this.ObtenerPathDeTeoriasPrecargadas(), tipoArrayListTeoria);
		
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader("resources/preloadedinfo.csv"))) {
			while((line = br.readLine()) != null ) {
				teoriasPrecargadas.add(Theory.deserialize(line));
			}
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (teoriasPrecargadas != null && teoriasPrecargadas.size() > 0){
				this.teoriasPrecargadas.clear();
				for (Theory teoriaPrecargada: teoriasPrecargadas) {
					this.teoriasPrecargadas.add(teoriaPrecargada);
					if (teoriaPrecargada.getU() == 0) {
						this.teoriasSinUtil.add(teoriaPrecargada);
					}
				}
			}
		}
	}
	
	private String ObtenerPathDeTeorias(){
		try {
			return(new String(Files.readAllBytes(this.pathTeorias)));
		} catch (IOException e) {
		}
		
		return(null);
	}
	
	private String ObtenerPathDeTeoriasPrecargadas(){
		try {
			return(new String(Files.readAllBytes(this.pathTeoriasPrecargadas)));
		} catch (IOException e) {
		}
		
		return(null);
	}
	
	private char[][] cargarSituacionActual() {
		char[][] nivel = mapa.getLevel();
		char[][] situacion = new char[nivel.length][nivel.length];
		int columnaAgente = (int) (this.posX);
		int filaAgente = (int)(this.posY);
		int anchoMapa = mapa.getLevelWidth();
		int altoMapa = mapa.getLevelHeight();
		int filaSit = 0;
		
		for (int fila = filaAgente - 3; fila < filaAgente + 3; fila++) {
			int colSit = 0;
			for (int col = columnaAgente - 3; col < columnaAgente + 3; col++){
				Vector2d posAgente = new Vector2d(col,fila);				
				if (fila >= 0 && fila < altoMapa && col >= 0 && col < anchoMapa) {
					char simboloVisible = nivel[fila][col];
					if (simboloVisible == '1' && this.posicionesObjetivos.contains(posAgente)) {
						situacion[filaSit][colSit] = 'X';
					} else {
						if (simboloVisible == 'A' && this.posicionesObjetivos.contains(posAgente)) {
							situacion[filaSit][colSit] = 'Y';
						} else {
							if (simboloVisible == 'B' && this.posicionesObjetivos.contains(posAgente)) {
								situacion[filaSit][colSit] = 'Z';
							} else {
								situacion[filaSit][colSit] = simboloVisible;
							}							
						}
					}
				} else {
					situacion[filaSit][colSit] = '?';
				}
				colSit++;
			}
			filaSit++;
		} 
		return situacion;
	}
	
	private State obtenerSituacionActual() {
		State situacionActual = new State(this.knowledgeBase.size()+1,this.cargarSituacionActual());
		for (State s: this.knowledgeBase) {
			if (s.compare(situacionActual)) {
				return s;
			}
		}
		
		this.knowledgeBase.add(situacionActual);
		return situacionActual;
	}
	
	private Graph generarGrafoDeTeorias() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		HashMap<String, Vertex> idVertices = new HashMap<String, Vertex>();
		
		ArrayList<Edge> lazos = new ArrayList<Edge>();
		
		for (State situacion: knowledgeBase) {
			String idSituacion = Integer.toString(situacion.getId());
			Vertex verticeSituacion = new Vertex(idSituacion, idSituacion);
			vertices.add(verticeSituacion);
			idVertices.put(idSituacion, verticeSituacion);
		}
		
		for (Theory teoria: this.teorias) {
			if (teoria != null) {
				if (teoria.getU() != 0) {
					String idTeoria = Integer.toString(teoria.getId());
					String idSitOrigen = Integer.toString(teoria.getIdSitCondicionInicial());
					String idSitDestino = Integer.toString(teoria.getIdSitEfectosPredichos());
					Vertex verticeOrigen = idVertices.get(idSitOrigen);
					Vertex verticeDestino = idVertices.get(idSitDestino);
					Double peso = (teoria.getK()-teoria.getP())/teoria.getK();
					Edge aristaTeoria = new Edge(idTeoria, verticeOrigen, verticeDestino, peso);
					lazos.add(aristaTeoria);
				}
			}
		}
		return new Graph(vertices, lazos);
	}
	
	private void guardarTeorias(){
		try {
			FileOutputStream out = new FileOutputStream("resources/info_0.csv");
			for (int i = 0; i < this.teorias.size(); i++) {
				out.write(this.teorias.get(i).toString().getBytes());				
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
