package celdas.fiuba.assets;

import java.util.ArrayList;
import java.util.HashMap;
import tools.Vector2d;

public class State {
	private int id;
	private String stringState;
	private char[][] map = null;
	
	public State(int id, char[][] map) {
		this.id = id;
		this.map = map;
		StringBuilder br = new StringBuilder();
		int i = 0;
        while (i < 6) {
            int j = 0;
            while (j < 6) {
                br.append(map[j][i]);
                j++;
            }
            br.append("|");
            i++;
        }
		this.stringState = br.toString();
	}
	
	public State(int id, String state) {
		this.id = id;
		this.stringState = state;
	}
	
	public int getId() {
		return id;
	}
	
	public char[][] getMap() {
		if (this.map == null) {
			this.loadMap();
		}
		return this.map;
	}
	
	public String getStringState() {
		return this.stringState;
	}
	
	public void loadMap() {
		String[] rows = this.stringState.split("\\|");
		if (rows.length < 2) {
			rows = this.stringState.split("\\n");
		}
		this.map = new char[6][6];
        int i = 0;
        while (i < 6) {
            int j = 0;
            while (j < 6) {
                this.map[j][i] = rows[j].charAt(i);
                j++;
            }
            i++;
        }
        //this.cargarSimbolos(situacionEnCasilleros);
	}
	
//	public void cargarSimbolos(char[][] casilleros) {
//		this.casilleros = new Simbolo[6][6];
//		StringBuilder sb = new StringBuilder("");
//
//		int i = 0;
//		while (i < 6) {
//			int j = 0;
//			while (j < 6) {
//
//				char simboloEnCasillero;
//				simboloEnCasillero = casilleros[i][j];
//				sb.append(simboloEnCasillero);
//
//				switch (simboloEnCasillero) {
//					case 'A':
//						this.casilleros[i][j] = new AgenteCero();
//						break;
//					case 'B':
//						this.casilleros[i][j] = new AgenteUno();
//						break;
//					case '.':
//						this.casilleros[i][j] = new CeldaVacia();
//						break;
//					case 'w':
//						this.casilleros[i][j] = new Pared();
//						break;
//					case '0':
//						this.casilleros[i][j] = new Objetivo();
//						break;
//					case '1':
//						this.casilleros[i][j] = new Uno();
//						break;
//					case 'X':
//						this.casilleros[i][j] = new CaracterX();
//						break;
//					case 'Y':
//						this.casilleros[i][j] = new CaracterY();
//						break;
//					case 'Z':
//						this.casilleros[i][j] = new CaracterZ();
//						break;
//					case 'P':
//						this.casilleros[i][j] = new MultiCajaAgenteP();
//						break;
//					case 'Q':
//						this.casilleros[i][j] = new CualquierObjetoMenosObjetivo();
//						break;
//					case '?':
//						this.casilleros[i][j] = new SignoDePregunta();
//						break;
//				}
//				j++;
//			}
//			sb.append("\n");
//			i++;
//		}
//		this.state = sb.toString();
//	}
	
	public boolean compare(State state) {
		return (this.stringState.equals(state.getStringState()));
	}
	
	public boolean includesElem(State state) {
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 6; j++)
				if (!(this.getMap()[i][j] == state.getMap()[i][j])) //same map character
					return false;
		return true;
	}
	
	public State generalizar(State situacion, int idNuevaSituacion) {
		char[][] situacionEnCasilleros = this.getMap();
		char[][] situacionPropuestaEnCasilleros = situacion.getMap();
		
		if (situacionEnCasilleros[3][3] != situacionPropuestaEnCasilleros[3][3])
			return null;
		
		int cantSimbolosRepetidos = 0;
		char[][] situacionGeneral = new char[6][6];

        int fila = 0;
        while (fila < 6) {
            int col = 0;
            while (col < 6) {
                char simbolo = situacionEnCasilleros[fila][col];
                char simboloOtraSituacion = situacionPropuestaEnCasilleros[fila][col];
                char simboloSitGeneral;

                if (!(simbolo == simboloOtraSituacion)) {
                    simboloSitGeneral = '?';
                } else {
                    simboloSitGeneral = simbolo;
                    if (simboloSitGeneral != '?' && simboloSitGeneral != 'A' && simboloSitGeneral != 'B')
                        cantSimbolosRepetidos++;
                }
                situacionGeneral[fila][col] = simboloSitGeneral;
                col++;
            }
            fila++;
        }

        if (cantSimbolosRepetidos == 0)
			return null;
		
		return (new State(idNuevaSituacion, situacionGeneral));
	}
	
	public String toString() {
		return this.stringState;
	}
	
//	public int obtenerCantElem(String elementos){
//		char[][] casilleros = this.getMap();
//		int cantidadDeElementos = 0;
//		
//		for (int fila = 0; fila < 6; fila++){
//			for (int col = 0; col < 6; col++){
//				if (casilleros[fila][col] == elementos.charAt(0)) {
//					cantidadDeElementos++;
//				}
//			}
//		}
//		return (cantidadDeElementos);
//	}
	
//	public HashMap<String, Integer> obtenerCantElemMapa() {
//		char[][] casilleros = this.getMap();
//		HashMap<String, Integer> cantidades = new HashMap<String, Integer>();
//        int fila = 0;
//        while (fila < 6) {
//            int col = 0;
//            while (col < 6) {
//                char simbolo = casilleros[fila][col];
//                if (!cantidades.containsKey(simbolo)) {
//                    cantidades.put(simbolo, 1);
//                } else {
//                    int cantidadActual = cantidades.get(simbolo);
//                    cantidades.put(simbolo, cantidadActual + 1);
//                }
//                col++;
//            }
//            fila++;
//        }
//        return cantidades;
//	}
	
	public HashMap<Character, ArrayList<Vector2d>> obtenerPosicionesElementos() {
		char[][] casilleros = this.getMap();
		HashMap<Character, ArrayList<Vector2d>> posicionesCadaTipoDeElemento = new HashMap<Character, ArrayList<Vector2d>>();
        int fila = 0;
        while (fila < 6) {
            int col = 0;
            while (col < 6) {
                Character simbolo = casilleros[fila][col];
                Vector2d posicion = new Vector2d(col,fila);
                ArrayList<Vector2d> posiciones;
                if (!posicionesCadaTipoDeElemento.containsKey(simbolo)) {
                    posiciones = new ArrayList<Vector2d>();
                    posiciones.add(posicion);
                    posicionesCadaTipoDeElemento.put(simbolo, posiciones);
                } else {
                    posiciones = posicionesCadaTipoDeElemento.get(simbolo);
                    posiciones.add(posicion);
                }
                col++;
            }
            fila++;
        }
        return posicionesCadaTipoDeElemento;
	}
	
	public boolean mismaPosicionPersonaje(State situacion) {
		char[][] casilleros, casillerosOtraSituacion;
		casilleros = this.getMap();
		casillerosOtraSituacion  = situacion.getMap();
		int i, j;
		
		i=0;
        while (i < 6) {
            j = 0;
            while (j < 6) {
                char simbolo = casilleros[i][j];
                char simboloOtraSituacion = casillerosOtraSituacion[i][j];

                boolean simboloPared;
                if ((simbolo == 'w') || (simboloOtraSituacion =='w')) simboloPared = true;
                else simboloPared = false;

                boolean simbolosDistintos;
                if ((!(simbolo == simboloOtraSituacion))) simbolosDistintos = true;
                else simbolosDistintos = false;
                if (simboloPared && simbolosDistintos)
                    return false;
                j++;
            }
            i++;
        }
        return true;
	}
}
