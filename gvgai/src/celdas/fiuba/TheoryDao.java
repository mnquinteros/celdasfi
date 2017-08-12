package celdas.fiuba;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TheoryDao {
	
	private static String PATH_INFO = "resources/agent0.csv";
	private static String PATH_PRELOADED = "resources/preloadedinfo_.csv";
	
	public static void save(List<Theory> teorias) {
		try {
			FileOutputStream out = new FileOutputStream(PATH_INFO);
			for (int i = 0; i < teorias.size(); i++) {
				out.write(teorias.get(i).toString().getBytes());				
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getPreloadedTheories(List<Theory> preloadedTheories, List<Theory> uselessTheories) {
		
		ArrayList<Theory> teoriasPrecargadas = new ArrayList<>();
		String line = "";
		try(BufferedReader br = new BufferedReader(new FileReader(PATH_PRELOADED))) {
			while((line = br.readLine()) != null ) {
				teoriasPrecargadas.add(Theory.deserialize(line));
			}
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (teoriasPrecargadas != null && teoriasPrecargadas.size() > 0){
				preloadedTheories.clear();
				for (Theory teoriaPrecargada: teoriasPrecargadas) {
					preloadedTheories.add(teoriaPrecargada);
					if (teoriaPrecargada.getU() == 0) {
						uselessTheories.add(teoriaPrecargada);
					}
				}
			}
		}
	}
	
	public static void loadTheories(List<Theory> result) {
		ArrayList<Theory> teorias = new ArrayList<>(); //gson.fromJson(this.ObtenerPathDeTeorias(), tipoArrayListTeoria);
		
		String line = ""; //aca estaban las teorias del agente_0
		try(BufferedReader br = new BufferedReader(new FileReader(PATH_INFO))) {
			while((line = br.readLine()) != null ) {
				teorias.add(Theory.deserialize(line));
			}
		} catch (IOException e) {
			// TODO: handle exception
		} finally {
			if (teorias != null && teorias.size() > 0){
				result.clear();
				result.addAll(teorias);
			}
		}
	}

}
