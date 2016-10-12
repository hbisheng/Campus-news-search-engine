import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuImageExtract {
	
	final double TFIDF_THRESHOLD = 0.1;
	
	ArrayList<PageIE> pageList = new ArrayList<PageIE>();
	HashMap<String, Integer> pageToIndex = new HashMap<String, Integer>();
	HashMap<String, Double> imageToIdf = new HashMap<String, Double>();
	
	public void init(ArrayList<String> filePathList, String baseDir) {
		baseDir = baseDir.replace('\\', '/');
		int indexCnt = 0;
		for (String filePath : filePathList) {
			PageIE page = new PageIE();
			pageList.add(page);
			page.filePath = filePath;
			String filePathFormatted = filePath.replace('\\', '/');
			page.pageName = filePathFormatted.replace(baseDir, "");
			pageToIndex.put(page.pageName, indexCnt++);
		}

		System.out.println("Load pages ...");
		for (PageIE page : pageList) {
			String html = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(page.filePath)));
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					html += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				System.out.println("Read " + page.filePath + " error!");
			}
			
			Pattern pattern = Pattern.compile("<img.*?src=\"(.*?)\"");
			Matcher matcher = pattern.matcher(html);
			while (matcher.find()) {
				String imagePath = matcher.group(1);
				double tf = 1;
				if (page.imageToTf.containsKey(imagePath)) {
					tf = page.imageToTf.get(imagePath) + 1;
				}
				page.imageToTf.put(imagePath, tf);
			}
			for (String imagePath : page.imageToTf.keySet()) {
				double idf = 1;
				if (imageToIdf.containsKey(imagePath)) {
					idf = imageToIdf.get(imagePath) + 1;
				}
				imageToIdf.put(imagePath, idf);
			}
		}
		System.out.println("Load pages finish !");

		System.out.println("Get best picture ...");
		for (PageIE page : pageList) {
			double bestTfidf = -1;
			String bestImagePath = null;
			for (Entry<String, Double> entry : page.imageToTf.entrySet()) {
				String imagePath = entry.getKey();
				double tf = entry.getValue();
				double idf = imageToIdf.get(imagePath);
				if (tf / idf > bestTfidf) {
					bestTfidf = tf / idf;
					bestImagePath = imagePath;
				}
			}
			if (bestTfidf < TFIDF_THRESHOLD) {
				bestImagePath = null;
			}
			//System.out.println(bestTfidf + " " + bestImagePath);
			page.bestImagePath = bestImagePath;
		}
		System.out.println("Get best picture finishi !");
	}

	public void print() {
		for (PageIE page : pageList) {
			System.out.println(page.pageName + " " + page.bestImagePath);
		}
	}
}

class PageIE {
	public HashMap<String, Double> imageToTf = new HashMap<String, Double>();
	public String filePath;
	public String pageName;
	public String bestImagePath;
}
