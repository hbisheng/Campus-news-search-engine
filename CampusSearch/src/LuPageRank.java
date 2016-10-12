
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LuPageRank {
	
	final int MAX_ITER_TIMES = 30;
	final double ALPHA = 0.15;
	
	ArrayList<PagePR> pageList = new ArrayList<PagePR>();
	HashMap<String, Integer> pageToIndex = new HashMap<String, Integer>();
	
	public void init(ArrayList<String> filePathList, String baseDir) {
		baseDir = baseDir.replace('\\', '/');
		int indexCnt = 0;
		for (String filePath : filePathList) {
			PagePR page = new PagePR();
			pageList.add(page);
			page.filePath = filePath;
			String filePathFormatted = filePath.replace('\\', '/');
			page.pageName = filePathFormatted.replace(baseDir, "");
			pageToIndex.put(page.pageName, indexCnt++);
		}
		
		System.out.println("Load pages ...");
		for (PagePR page : pageList) {
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
			
			Pattern pattern = Pattern.compile("href=\"(.*?)\"");
			Matcher matcher = pattern.matcher(html);
			while (matcher.find()) {
				String outName = matcher.group(1);
				if (!pageToIndex.containsKey(outName)) continue;
				int outIndex = pageToIndex.get(outName);
				PagePR outPage = pageList.get(outIndex);
				page.outList.add(outPage);
				outPage.inList.add(page);
			}
		}
		System.out.println("Load pages finish !");
		

		System.out.println("Calculate page rank ...");
		int n = pageList.size();
		for (PagePR page : pageList) {
			page.pageRank = 1.0 / n;
		}
		for (int iterTimes = 0; iterTimes < MAX_ITER_TIMES; ++iterTimes) {
			System.out.println("Iter " + iterTimes);
			double noOutPageRankSum = 0;
			for (PagePR page : pageList) {
				if (page.outList.size() > 0) {
					double x = (1 - ALPHA) * page.pageRank / page.outList.size();
					for (PagePR outPage : page.outList) {
						outPage.newPageRank += x;
					}
				} else {
					noOutPageRankSum += page.pageRank;
				}
			}
			for (PagePR page : pageList) {
				page.pageRank = ALPHA / n + page.newPageRank + (1 - ALPHA) * noOutPageRankSum / n;
				page.newPageRank = 0;
			}
		}
		System.out.println("Calculate page rank finish !");
	}
	
	public void print() {
		ArrayList<PagePR> q = (ArrayList<PagePR>)pageList.clone();
		Collections.sort(q, new Comparator<PagePR>() {
			public int compare(PagePR o0, PagePR o1) {
				Double d0 = o0.pageRank;
				Double d1 = o1.pageRank;
				return d1.compareTo(d0);
			}
		});
		for (PagePR page : q) {
			System.out.println(page.pageName + " " + page.pageRank);
		}
	}
}

class PagePR {
	public String filePath;
	public String pageName;
	public ArrayList<PagePR> inList = new ArrayList<PagePR>();
	public ArrayList<PagePR> outList = new ArrayList<PagePR>();
	public double pageRank = 0;
	public double newPageRank = 0;
}
