

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class LuCorrection {
	
	final int Q = 2;
	final double ED_RATIO = 0.2;
	
	HashMap<String, ArrayList<Integer>> inverted = new HashMap<String, ArrayList<Integer>>();
	
	void init() {
		System.out.println("Build inverted index ...");
		for (Entry<String, Integer> wordwork : LuBasic.s2i.entrySet()) {
			String word = wordwork.getKey();
			int wordId = wordwork.getValue();
			for (int i = 0; i + Q <= word.length(); ++i) {
				String gram = word.substring(i, i + Q);
				if (!inverted.containsKey(gram)) {
					inverted.put(gram, new ArrayList<Integer>());
				}
				inverted.get(gram).add(wordId);
			}
		}
		System.out.println("Build inverted index finish !");
	}

	ArrayList<String> find(String word) {
		int ed = (int) Math.ceil(word.length() * ED_RATIO);
		int t = Math.max((int)Math.ceil(word.length() - Q + 1 - ed * Q), 1);
		//System.out.print(ed + " " + t + " ");
		HashMap<Integer, Double> count = new HashMap<Integer, Double>();
		for (int i = 0; i + Q < word.length(); ++i) {
			String gram = word.substring(i, i + Q);
			if (!inverted.containsKey(gram))
				continue;
			for (int word2Id : inverted.get(gram)) {
				double c = 0;
				if (count.containsKey(word2Id)) {
					c = count.get(word2Id);
				}
				count.put(word2Id, c + 1);
			}
		}
		ArrayList<Entry<Integer, Double>> similarList = new ArrayList<Entry<Integer, Double>>();
		for (Entry<Integer, Double> similarwork : count.entrySet()) {
			int word2Id = similarwork.getKey();
			double times = similarwork.getValue();
			if (times < t) continue;
			String word2 = LuBasic.i2s.get(word2Id);
			if (editDistance(word, word2, ed) > ed) continue;
			similarwork.setValue(1.0 * LuBasic.idf.get(word2Id));
			similarList.add(similarwork);
		}
		Collections.sort(similarList, new Comparator<Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> o0, Entry<Integer, Double> o1) {
				Double d0 = o0.getValue();
				Double d1 = o1.getValue();
				return d1.compareTo(d0);
			}
		});
		ArrayList<String> resultList = new ArrayList<String>();
		for (Entry<Integer, Double> similarwork : similarList) {
			resultList.add(LuBasic.i2s.get(similarwork.getKey()));
		}
		return resultList;
	}
	
	int editDistance(String a, String b, int ed) {
		if (Math.abs(a.length() - b.length()) > ed) return ed + 1;
		int f[][] = new int[a.length() + 1][b.length() + 1];
		for (int i = 0; i <= a.length(); ++i)
			for (int j = 0; j <= b.length(); ++j)
				f[i][j] = ed + 1;
		f[0][0] = 0;
		for (int i = 0; i <= a.length(); ++i) {
			int js = Math.max(0, i - ed);
			int jt = Math.min(b.length(), i + ed);
			for (int j = js; j <= jt; ++j) {
				if (f[i][j] + Math.abs(i - j) > ed) continue;
				if (i < a.length() && j < b.length()) {
					int d = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
					f[i + 1][j + 1] = Math.min(f[i + 1][j + 1], f[i][j] + d);
				}
				if (i < a.length()) {
					f[i + 1][j] = Math.min(f[i + 1][j], f[i][j] + 1);
				}
				if (j < b.length()) {
					f[i][j + 1] = Math.min(f[i][j + 1], f[i][j] + 1);
				}
			}
		}
		return f[a.length()][b.length()];
	}
}