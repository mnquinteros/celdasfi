package celdas.fiuba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

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
	private Perception mapa;
	private State situacionAnterior = null;
	private Strategy estrategia = new Strategy();
	
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
		this.mapa = new Perception(stateObs);
		this.teorias = new ArrayList<Theory>();
		this.teoriasPrecargadas = new ArrayList<Theory>();
		this.teoriasSinUtil = new ArrayList<Theory>();
		this.idAgente = playerID;
		this.posicionesObjetivos = mapa.getPosicionesObjetivos();
		TheoryDao.getPreloadedTheories(this.teoriasPrecargadas, this.teoriasSinUtil);
	}
	
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer){
		this.mapa =  new Perception(stateObs);
		this.posX = (int)(stateObs.getAvatarPosition(this.idAgente).x) / this.mapa.getSpriteSizeWidthInPixels();
        this.posY = (int)(stateObs.getAvatarPosition(this.idAgente).y) / this.mapa.getSpriteSizeHeightInPixels();
		
		TheoryDao.loadTheories(this.teorias);
        this.knowledgeBase = this.cargarBaseDeConocimiento();
        
		if (this.situacionAnterior != null) {
			agregarNuevaSituacion(situacionAnterior);
		}
		State situacionActual = this.obtenerSituacionActual();
		ACTIONS ultimaAccion = stateObs.getAvatarLastAction(this.idAgente);
		if (situacionAnterior != null) {
			Theory teoriaLocal = null;
			if (sirveLaAccion(situacionAnterior, ultimaAccion)) {
				teoriaLocal = new Theory(this.teorias.size()+this.teoriasPrecargadas.size()+ 1, this.situacionAnterior, ultimaAccion, situacionActual, 1, 1, 
									UtililyCalculator.calcularUtilidadTeoria(this.situacionAnterior, ultimaAccion, situacionActual));
			} else {
				teoriaLocal = new Theory(this.teorias.size()+this.teoriasPrecargadas.size()+ 1, this.situacionAnterior, ultimaAccion, situacionActual, 1, 1, 
						0.0);
			}
			evaluarTeoria(teoriaLocal);
		}

		ACTIONS siguienteAccion = actualizarEstrategia(stateObs, situacionActual, this.generarGrafoDeTeorias());
		this.situacionAnterior = situacionActual;
		TheoryDao.save(this.teorias);
		return siguienteAccion;
	}
	
	private void evaluarTeoria(Theory teoriaLocal) {
		
		if (teoriaLocal != null) {					
			
			ArrayList<Theory> todasLasTeoriasSimilares = getAllSimilarTheories(teoriaLocal);
			
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
						if (teoriaLocal.getU() == 0.0) {
							utilidadTeoriaMutante = 0.0;
						} else {
							if ( sirveLaAccion(CITeoriaLocal, teoriaLocal.getAccionComoAction()) ) {
								utilidadTeoriaMutante = UtililyCalculator.calcularUtilidadTeoria(CITeoriaLocal, teoriaLocal.getAccionComoAction(), EPTeoriaMutante);
							} else {
								utilidadTeoriaMutante = 0.0;
							}
						}
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
	
	private ArrayList<Theory> getAllSimilarTheories(Theory teoriaLocal) {
		
		ArrayList<Theory> teoriasSimilares = this.buscarTeoriasSimilares(teoriaLocal, this.teorias);
		ArrayList<Theory> teoriasSimilaresPrecargadas = this.buscarTeoriasSimilares(teoriaLocal, this.teoriasPrecargadas);
		ArrayList<Theory> todasLasTeoriasSimilares = new ArrayList<Theory>();
		todasLasTeoriasSimilares.addAll(teoriasSimilares);
		todasLasTeoriasSimilares.addAll(teoriasSimilaresPrecargadas);
		
		return todasLasTeoriasSimilares;
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
			Theory teoriaNuevoObjetivo = UtililyCalculator.obtenerTeoriaConMayorUtilidad(this.teorias);

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
		HashSet<ACTIONS> accionesNoPosibles = new HashSet<>();
		
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
		//accionesPosibles.removeAll(accionesNoPosibles);
		//return accionesPosibles.get(0);
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
}
