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
                br.append(map[i][j]);
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
	}
	
	public boolean compare(State state) {
		if (state.getStringState() == null) {
			System.out.println("State.compare() st is null");
		}
		return (this.stringState.equals(state.getStringState()));
	}
	
	public boolean includesElem(State state) {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				char elem = this.getMap()[i][j];
				char other = state.getMap()[i][j];
				if (elem == '?') {
					//do nothing
				} else if (elem == 'P') {
				  if (!( other == 'A' || other == 'B' || other == 'Y' || other == 'Z')) {
						return false;
					}
				} else if (elem =='Q') {
					if (!(other == '0')) {
						return false;
					}
				} else if (!(elem == other)) {//same map character
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isSamePosition(State situacion) {
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

                boolean simboloPared = (simbolo == 'w') || (simboloOtraSituacion =='w');
                boolean simbolosDistintos = simbolo != simboloOtraSituacion;
                if (simboloPared && simbolosDistintos)
                    return false;
                j++;
            }
            i++;
        }
        return true;
	}

	
	public State generalizar(State situacion, int idNuevaSituacion) {
		char[][] situacionEnCasilleros = this.getMap();
		char[][] situacionPropuestaEnCasilleros = situacion.getMap();

		if (situacionEnCasilleros != situacionPropuestaEnCasilleros)
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

                if ( simbolo != simboloOtraSituacion ) {
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
		
	public HashMap<Character, ArrayList<Vector2d>> getElementPositions() {
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
	
}
