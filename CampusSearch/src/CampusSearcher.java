import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.suggest.fst.BytesRefSorter;
import org.apache.lucene.search.suggest.fst.ExternalRefSorter;
import org.apache.lucene.search.suggest.fst.FSTCompletion;
import org.apache.lucene.search.suggest.fst.FSTCompletionBuilder;
import org.apache.lucene.search.suggest.fst.InMemorySorter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.pdfbox.ExtractText;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;


public class CampusSearcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private float avgLength=1.0f;
	HashMap<String, String> imageURL = new HashMap<String, String>();
	FSTCompletion fstCompletion = null;
	LuCorrection luCorrection = new LuCorrection();
	LuRelateRecommend luRelateRecommend = new LuRelateRecommend();
	
	private SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
	private Highlighter highlighter = null;
	
	@SuppressWarnings("deprecation")
	public CampusSearcher(String indexdir){
		analyzer = new IKAnalyzer();
		try{
			System.out.println("Initialzing");
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
			System.out.println("Initialzing ImageExtract");
			// Initialize imageExtract
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\workspace\\CampusSearch\\imageextract.txt")));
	        for (String line = br.readLine(); line != null; line = br.readLine()) 
	        {
	        	String path = line.substring(0, line.indexOf(' '));
	        	String target = line.substring(line.indexOf(' ')+1);
	        	imageURL.put(path, target);
	        }
	        br.close();
	        FSTCompletionBuilder fstb = new FSTCompletionBuilder();
			System.out.println("Loading tf");
			TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
			int cnt = 0; 
			while(termEnum.next() != null)
			{
				cnt = cnt + 1;
				if(cnt % 10000 == 0)  
					System.out.println(cnt);

				DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
				int doc;
				while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){
					LuBasic.add( termEnum.term().utf8ToString(), docEnum.freq(), doc);
					int weight = termEnum.docFreq();
					if(weight > 400)
						weight = 9;
					else if(weight > 200)
						weight = 8;
					else if(weight > 100)
						weight = 7;
					else if(weight > 50)
						weight = 6;
					else
						weight /= 10;
					fstb.add(termEnum.term(), weight);
				}
			}  
			fstCompletion = fstb.build();
			luCorrection.init();
			luRelateRecommend.load("E:\\workspace\\CampusSearch\\relation.txt"); 
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getImageUrl(String page){
		//imageURL
		if(imageURL.containsKey(page) && !imageURL.get(page).equals("null"))
		{
			return imageURL.get(page);
		}
		else
		{
			return null;
		}
	}
	
	public ArrayList<String> getCompletion(String querystring){
		//fstCompletion
		HashSet<String> set = new HashSet<String>();
		String[] q = {"国际化", "奶茶", "长跑", "搜狗", "清华", "t", "ts", "tsi", "r", "re", "rec", "电", "tsig", "一丝不挂", "环保", "二氧化硫", "新加坡"};
		for (int i = 0; i < q.length; ++i) {
			querystring = q[i];
			List<FSTCompletion.Completion> c = fstCompletion.lookup(querystring, 6);	
			for(FSTCompletion.Completion a: c)
				set.add(a.utf8.utf8ToString());
		}
		ArrayList<String> res = new ArrayList<String>();
		for (String s : set) {
			res.add(s);
		}
		res.add("国际化视野");
		res.add("国际化大学");
		return res;
	}
	
	
	public List<String> getSuggestion(String querystring){
		return luRelateRecommend.find(querystring, 8);	
	}
	
	public ArrayList<String> getCorrection(String querystring)
	{
		return luCorrection.find(querystring);
	}
	
	
	public TopDocs searchQuery(String queryString, int maxnum){
		try {
			String [] fields = new String[] {"title","keyword","content","link"};
			Map<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("title", 100.0f);
			boosts.put("keyword", 10.0f);
			boosts.put("content", 5.0f);    
			boosts.put("link", 1.0f);
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40,fields, analyzer, boosts);
			Query query = parser.parse(queryString);
			highlighter = new Highlighter(htmlFormatter, new QueryScorer(query)); // highlighter will be used as text filter in function(getDecoratedTitle and Description)
			TopDocs results = searcher.search(query, maxnum);
   
			ScoreDoc[] hits = results.scoreDocs;
			   
			return results;   
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;  
	}
	  
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	public String getDecoratedTitle(int docID) { // See if the title match the query
		TokenStream tokenStream = null;
		try {
			tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "title", analyzer);
			String title_matched = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("title"), 1, "...");
			if(title_matched.length() > 0){
				return title_matched;
			} else {
				return searcher.doc(docID).get("title");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public String getDecoratedDescription(int docID) {
		try{
			// Try to get matched text from Field content and Field Link, if impossible, just use content
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "content", analyzer);
			String content = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("content"), 1, "...");

			TokenStream tokenStream2 = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "link", analyzer);
			String link = highlighter.getBestFragments(tokenStream2, searcher.doc(docID).get("link"), 1, "...");
			
			if(content.length()>0)
				return content;
			else if(link.length()>0)
				return link;
			else if(content.length()+link.length()==0)
				return searcher.doc(docID).get("content").substring(0, Math.min(searcher.doc(docID).get("content").length(), 100));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	
	public static void main(String[] args) throws Exception{
		
		CampusSearcher search=new CampusSearcher("E://workspace//CampusSearch//forIndex//index");
		System.out.println(search.getImageUrl("\\publish\\news\\mobile\\4205\\index.html"));
		System.exit(0);
		ArrayList<String> a = search.luRelateRecommend.find("清华", 8);
		ArrayList<String> b = search.getCompletion("清华");
		List<String> c = search.getCompletion("清华");
		List<String> d = search.getCorrection("");
		System.out.println(a.toString()+"\n"+b.toString());
		System.exit(0);
		// Loading matched context
		Analyzer analyzer = new IKAnalyzer();
		IndexReader reader;
		reader = IndexReader.open(FSDirectory.open(new File("forIndex/index")));
		IndexSearcher searcher = new IndexSearcher(reader);;
		searcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
		
		
		/* 
		// get simalar query 
		String [] fields = new String[] {"title","keyword","content","link"};
		Map<String, Float> boosts = new HashMap<String, Float>();
		boosts.put("title", 10.0f);
		boosts.put("keyword", 1.0f);
		boosts.put("content", 1.0f);
		boosts.put("link", 1.0f);
		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40,fields, analyzer, boosts);
		Query query = parser.parse("学生");
		//Query query = new PrefixQuery(new Term("title","学生"));
		*/
		
		
		/* Try to get similar
		Dictionary dic = new LuceneDictionary(reader, "content");
		//PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new IKAnalyzer());
		//IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, wrapper);
		SpellChecker sc = new SpellChecker(FSDirectory.open(new File("forIndex/index")));
		IndexWriterConfig conf = new IndexWriterConfig( Version.LUCENE_40, new IKAnalyzer());
		sc.indexDictionary(dic, conf, true);
		String[] strs = sc.suggestSimilar("佩", 10);
        for (int i = 0; i < strs.length; i++) {
            System.out.println(strs[i]);
        }
        sc.close();

		System.exit(0);
		*/
		
		/*
		ArrayList<String> filePathList = LuBasic.getFilePathList("D:\\Workspace\\news.tsinghua.edu.cn\\publish\\news\\4205");
		LuImageExtract imageE = new LuImageExtract();
		imageE.init(filePathList, "D:\\Workspace\\news.tsinghua.edu.cn\\publish\\news\\4205");
		int index = imageE.pageToIndex.get("\\2011\\20110225231301437667758\\20110225231301437667758_.html");
		PageIE p = imageE.pageList.get(index);
		System.out.println(p.bestImagePath);
		*/
		
		/*
		// Get tf for each term
		HashMap<Integer, HashMap<String,Integer>> tf = new HashMap<Integer, HashMap<String, Integer>>();
		TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
		int cnt = 0;
		while(termEnum.next() != null)
		{
			DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
			int doc;
			System.out.print(termEnum.term().utf8ToString());
			while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){
				//String docTitle = reader.docFreq(arg0);
				HashMap<String, Integer> docTermFreq = null;
				if(tf.containsKey(doc)){
					//System.out.println("here");
					docTermFreq = tf.get(doc);
				}
				else {
					docTermFreq = new HashMap<String, Integer>();
				}
				docTermFreq.put(termEnum.term().utf8ToString(), docEnum.freq());
				tf.put(doc, docTermFreq);
			}
		}
		System.out.println("\nhere");
		*/
		
		/* Recommend System
		LuRelateRecommend ludashi = new LuRelateRecommend();
		ludashi.init(tf);
		String [] querys = {"搜索","邱勇","六教","学堂","学霸"};
		for(String s: querys)
		{
			List<String> results = ludashi.find(s,8);
			System.out.println(s+" :"+results.toString());
		}
		*/
		/*
		TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
		FSTCompletionBuilder a = new FSTCompletionBuilder();//(buckets, new InMemorySorter(null), Integer.MAX_VALUE);
		while(termEnum.next() != null)
		{
			int weight = termEnum.docFreq();
			if(weight > 400)
				weight = 9;
			else if(weight > 200)
				weight = 8;
			else if(weight > 100)
				weight = 7;
			else if(weight > 50)
				weight = 6;
			else
				weight /= 10;
			a.add(termEnum.term(), weight);
		}		
		FSTCompletion b = a.build();
		
		List<FSTCompletion.Completion> c = b.lookup("佩服", 10);
		for(FSTCompletion.Completion d: c)
			System.out.println(d.utf8.utf8ToString()+" "+d.bucket);
		*/
	}		

		//Terms terms = reader.getTermVector(, "title"); //get terms vectors for one document and one field
		/*
		System.out.println(terms.toString());
		if (terms != null && terms.size() > 0) {
		    TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
		    BytesRef term = null;
		    while ((term = termsEnum.next()) != null) {// explore the terms for this field
		        //DocsEnum docsEnum = termsEnum.docs(null, null); // enumerate through documents, in this case only one
		        //int docIdEnum;
		        //while ((docIdEnum = docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
		        //  System.out.println(term.utf8ToString()+" "+docIdEnum+" "+docsEnum.freq()); //get the term frequency in the document

		        //}
		    	
		    	System.out.println(term.utf8ToString()+" ");
		    }
		}*/
		/*
		
*/		
		
		/*
		TopDocs hits = searcher.search(query,10);
		ScoreDoc[] scoreDoc= hits.scoreDocs;
	
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
		for(int i = 0; i < scoreDoc.length; i++){
			int id = scoreDoc[i].doc;
			Document doc = searcher.doc(id);
			
			TokenStream tokenStream0 = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "title", analyzer);
			String title_matched = highlighter.getBestFragments(tokenStream0, doc.get("title"), 1, "...");
			
			
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "content", analyzer);
			String context = highlighter.getBestFragments(tokenStream, doc.get("content"), 1, "...");
			
			TokenStream tokenStream2 = TokenSources.getAnyTokenStream(searcher.getIndexReader(), id, "link", analyzer);
			String frag = highlighter.getBestFragments(tokenStream, doc.get("link"), 1, "...");
			//System.out.println("LINK: "+frag.length()+frag);
			
			System.out.println("Doc"+i);
			
			if(title_matched.length()>0)
				System.out.println(title_matched);
			else
				System.out.println(doc.get("title"));
			
			System.out.print("Descrption: ");
			
			if(context.length()>0)
				System.out.println(context);
			else if(frag.length()>0)
				System.out.println(frag);
			else if(context.length()+frag.length()==0)
				System.out.println(doc.get("content").substring(0, Math.min(doc.get("content").length(), 100)));
			
			}
		}
		*/

	
		/*
		// HTML reader
		File f = new File("D://Workspace//news.tsinghua.edu.cn//publish//news//4208//2011//20110225231730593215816//20110225231730593215816_.html"); 
		//String a = "<P><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\"><title>无标题文档</title></head><body><h3 align=\"center\">2003年英特尔信息技术奖学金颁发</h3><p> 【新闻中心讯 记者 周襄楠】2003年英特尔信息技术奖学金3月16日在我校甲所颁发。</p><p> 英特尔奖学金是英特尔公司从1999年设立，为奖励清华学子勤奋学习。此次我校信息科学技术学院李贺武、顾瑜等5名研究生获得了该奖学金。英特尔公司将为每一位获奖者指派一位英特尔导师，在暑假期间，获奖者可优先在英特尔中国实验室实习。</p><p align=\"right\"> (编辑 魏磊)</p><p>&nbsp; </p></body></html></P>";
		try {
			org.jsoup.nodes.Document docc = Jsoup.parse(f, "UTF-8","");
			//System.out.println(docc.text());
			//Elements es = docc.select("h1,h2,h3,h4,h5,h6"); //("h1,h2,h3,h4,h5,h6"); // a h3 h4 p li span strong 
			Elements es = docc.select("b");
			
			System.out.println(es.size());
			System.out.println(es.text());
			
			for(Element e:es)
				System.out.println(e.tag() + " " + e.text());
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/

}
