
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class LuBasic {
	
	static HashMap<Integer, HashMap<Integer, Integer>> tf = new HashMap<Integer, HashMap<Integer, Integer>>();
	static HashMap<Integer, Integer> idf = new HashMap<Integer, Integer>();
	static HashMap<String, Integer> s2i = new HashMap<String, Integer>();
	static HashMap<Integer, String> i2s = new HashMap<Integer, String>();
	static int icnt = 0;
	static int pcnt = 0;
	
	static ArrayList<String> getFilePathList(String path) {
		File[] fileList = (new File(path)).listFiles();
		ArrayList<String> filePathList = new ArrayList<String>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				ArrayList<String> sonFilePathList = getFilePathList(file.getAbsolutePath());
				filePathList.addAll(sonFilePathList);
			} else if (file.isFile()) {
				filePathList.add(file.getAbsolutePath());
			}
		}
		return filePathList;
	}
	
	static ArrayList<String> getSpecificFilePathList(ArrayList<String> filePathList, String type) {
		ArrayList<String> specificFilePathList = new ArrayList<String>();
		for (String filePath : filePathList) {
			File file = new File(filePath);
			String fileName = file.getName();
			String fileType = getFileType(fileName);
			if (fileType.toLowerCase().equals(type)) {
				specificFilePathList.add(filePath);
			}
		}
		return specificFilePathList;
	}
	
	static String getFileType(String fileName) {
		int pos = fileName.lastIndexOf('.');
		return fileName.substring(pos + 1);
	}

	static void add(String word, int freq, int docId) {
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9') return;
		}
		int wordId;
		if (s2i.containsKey(word)) {
			wordId = s2i.get(word);
		} else {
			s2i.put(word, icnt);
			i2s.put(icnt, word);
			idf.put(icnt, 0);
			wordId = icnt++;
		}
		if (!tf.containsKey(docId)) {
			tf.put(docId, new HashMap<Integer, Integer>());
		}
		idf.put(wordId, idf.get(wordId) + 1);
		tf.get(docId).put(wordId, freq);
	}
}
