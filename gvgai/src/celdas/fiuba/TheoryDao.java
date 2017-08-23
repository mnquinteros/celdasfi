package celdas.fiuba;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


public class TheoryDao {
	
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static String PREFIX_PATH_INFO = "resources/agent";
	private static String PATH_PRELOADED = "resources/preloadedinfo.json";
	
	public static void save(Integer id, List<Theory> teorias) {
		try {
			FileOutputStream out = new FileOutputStream(PREFIX_PATH_INFO + id + ".json");
			out.write("[\n".getBytes());
			
			for (int i = 0; i < teorias.size(); i++) {
				out.write(gson.toJson(teorias.get(i)).getBytes());
				
				if (i != teorias.size()-1) {
					out.write((",\n").getBytes());	
				}
			}
			
			out.write("\n]".getBytes());
			out.close();

//			FileOutputStream out = new FileOutputStream(PATH_INFO);
//			for (int i = 0; i < teorias.size(); i++) {
//				out.write(teorias.get(i).toString().getBytes());				
//			}
//			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getPreloadedTheories(List<Theory> preloadedTheories, List<Theory> uselessTheories) {
		
		Type arrayListTheoryType = new TypeToken<ArrayList<Theory>>(){}.getType();
		ArrayList<Theory> theoriesFromFile = gson.fromJson(getJsonText(PATH_PRELOADED), arrayListTheoryType);
		
		if (theoriesFromFile != null && theoriesFromFile.size() > 0){
			preloadedTheories.clear();
			for (Theory teoriaPrecargada: theoriesFromFile) {
				preloadedTheories.add(teoriaPrecargada);
				if (teoriaPrecargada.getU() == 0) {
					uselessTheories.add(teoriaPrecargada);
				}
			}
		}

		
//		ArrayList<Theory> teoriasPrecargadas = new ArrayList<>();
//		String line = "";
//		try(BufferedReader br = new BufferedReader(new FileReader(PATH_PRELOADED))) {
//			while((line = br.readLine()) != null ) {
//				teoriasPrecargadas.add(Theory.deserialize(line));
//			}
//		} catch (IOException e) {
//			// TODO: handle exception
//		} finally {
//			if (teoriasPrecargadas != null && teoriasPrecargadas.size() > 0){
//				preloadedTheories.clear();
//				for (Theory teoriaPrecargada: teoriasPrecargadas) {
//					preloadedTheories.add(teoriaPrecargada);
//					if (teoriaPrecargada.getU() == 0) {
//						uselessTheories.add(teoriaPrecargada);
//					}
//				}
//			}
//		}
	}
	
	public static void loadTheories(Integer id, List<Theory> result) {
		
		Type arrayListTheoryType = new TypeToken<ArrayList<Theory>>(){}.getType();
		ArrayList<Theory> dynamicTheories = gson.fromJson(getJsonText(PREFIX_PATH_INFO + id + ".json"), arrayListTheoryType);
		
		if (dynamicTheories != null && dynamicTheories.size() > 0){
			result.clear();
			result.addAll(dynamicTheories);
		}
		
//		ArrayList<Theory> teorias = new ArrayList<>(); //gson.fromJson(this.ObtenerPathDeTeorias(), tipoArrayListTeoria);
//		
//		String line = ""; //aca estaban las teorias del agente_0
//		try(BufferedReader br = new BufferedReader(new FileReader(PATH_INFO))) {
//			while((line = br.readLine()) != null ) {
//				teorias.add(Theory.deserialize(line));
//			}
//		} catch (IOException e) {
//			// TODO: handle exception
//		} finally {
//			if (teorias != null && teorias.size() > 0){
//				result.clear();
//				result.addAll(teorias);
//			}
//		}
	}
	
	private static String getJsonText(String path) {
		try {
			return(new String(Files.readAllBytes(Paths.get(path))));
		} catch (IOException e) {
		}
		
		return(null);	
	}
}
