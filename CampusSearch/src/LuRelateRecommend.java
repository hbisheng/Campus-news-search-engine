
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class LuRelateRecommend {
	
	final double MAX_RECOMMEND_NUM = 8;
	final double RELATE_THRESHOLD = 0.05;
	
	HashMap<Integer, ArrayList<Integer>> relate = new HashMap<Integer, ArrayList<Integer>>();
	
	void init() {
		System.out.println("Build inverted index ...");
		HashMap<Integer, ArrayList<Integer>> invertedIndex = new HashMap<Integer, ArrayList<Integer>>();
		for (Entry<Integer, HashMap<Integer, Integer>> pagework : LuBasic.tf.entrySet()) {
			int pageId = pagework.getKey();
			for (Entry<Integer, Integer> entry : pagework.getValue().entrySet()) {
				int wordId = entry.getKey();
				if (!invertedIndex.containsKey(wordId)) {
					invertedIndex.put(wordId, new ArrayList<Integer>());
				}
				invertedIndex.get(wordId).add(pageId);
				int idf = 0;
				if (LuBasic.idf.containsKey(wordId)) {
					idf = LuBasic.idf.get(wordId);
				}
				LuBasic.idf.put(wordId, idf + 1);
			}
		}
		System.out.println("Build inverted index finish !");
		
		System.out.println("Get releations ... ");
		for (Entry<Integer, ArrayList<Integer>> wordwork : invertedIndex.entrySet()) {
			int wordId = wordwork.getKey();
			ArrayList<Integer> pageList = wordwork.getValue();
			HashMap<Integer, Double> relateMap = new HashMap<Integer, Double>();
			for (int pageId : pageList) {
				HashMap<Integer, Integer> relateList = LuBasic.tf.get(pageId);
				for (Entry<Integer, Integer> relatework : relateList.entrySet()) {
					int word2Id = relatework.getKey();
					if (wordId == word2Id) continue;
					double r = 0;
					if (relateMap.containsKey(word2Id)) {
						r = relateMap.get(word2Id);
					}   
					relateMap.put(word2Id, r + 1);
				}
			}
			ArrayList<Entry<Integer, Double>> relateArray = new ArrayList<Entry<Integer, Double>>();
			for (Entry<Integer, Double> relatework : relateMap.entrySet()) {
				int word2Id = relatework.getKey();
				double r = relatework.getValue();
				relatework.setValue(r / Math.sqrt(LuBasic.idf.get(word2Id)));
				relateArray.add(relatework);  
			}   
			Collections.sort(relateArray, new Comparator<Entry<Integer, Double>>() {
				public int compare(Entry<Integer, Double> o0, Entry<Integer, Double> o1) {
					Double d0 = o0.getValue();
					Double d1 = o1.getValue();
					return d1.compareTo(d0);
				}
			});
			relate.put(wordId, new ArrayList<Integer>());
			for (Entry<Integer, Double> relatework : relateArray) {
				int word2Id = relatework.getKey();
				double r = relatework.getValue();
				if (r < RELATE_THRESHOLD) break;
				//System.out.println(indexToWord.get(wordId) + " " + indexToWord.get(word2Id) + " " + r);
				ArrayList<Integer> relateList = relate.get(wordId);
				relateList.add(word2Id);
				if (relateList.size() >= MAX_RECOMMEND_NUM) break;
			}
		}
		System.out.println("Get relations finish !");
	}
	
	ArrayList<String> find(String word, int num) {
		if (!LuBasic.s2i.containsKey(word)) {
			return new ArrayList<String>();
		}
		int wordId = LuBasic.s2i.get(word);
		//System.out.println("##########"+wordId);
		ArrayList<Integer> relateIdList = relate.get(wordId);
		ArrayList<String> relateList = new ArrayList<String>();
		for (int i = 0; i < Math.min(num, relateIdList.size()); ++i) {
			relateList.add(LuBasic.i2s.get(relateIdList.get(i)));
		}
		return relateList;
	}

	void save(String filePath) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
			for (Entry<Integer, ArrayList<Integer>> relatework : relate.entrySet()) {
				String word = LuBasic.i2s.get(relatework.getKey());
				ArrayList<Integer> relateIdList = relatework.getValue();
				writer.write(word + " ");
				writer.write(relateIdList.size() + " ");
				for (Integer word2Id : relateIdList) {
					writer.write(LuBasic.i2s.get(word2Id) + " ");
				}
				writer.write("\n");
				writer.flush();
			}
			writer.close(); 
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	void load(String filePath) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			int l = 0;
			while (true) {
				l += 1;
				String line = reader.readLine();
				System.out.println(l+"||"+line);
				if (line == null) break;
				String[] lineArray = line.split(" ");
				String word = lineArray[0];
				int wordId = LuBasic.s2i.get(word);
				
				if(word.equals("Çå»ª"))
					System.out.println("~~~~~~~~~~"+wordId);
				relate.put(wordId, new ArrayList<Integer>());
				for (int i = 2; i < lineArray.length; ++i) {
					String word2 = lineArray[i];
					int word2Id = LuBasic.s2i.get(word2);
					relate.get(wordId).add(word2Id);
				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
