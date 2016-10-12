import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class CampusServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String indexDir="E://workspace//CampusSearch//forIndex";
	private CampusSearcher search=null;
	public CampusServer(){  
		super();
		search=new CampusSearcher(new String(indexDir+"/index"));
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results,int page){
		if(results==null || results.length<(page-1)*PAGE_RESULT){
			return null;
		}
		int start=Math.max((page-1)*PAGE_RESULT, 0);
		int docnum=Math.min(results.length-start,PAGE_RESULT);
		ScoreDoc[] ret=new ScoreDoc[docnum];
		for(int i=0;i<docnum;i++){   
			ret[i]=results[start+i];
		}
		return ret;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		int page=1;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		if(queryString==null){
			System.out.println("null query");
		}else{
			System.out.println(queryString);
			System.out.println(URLDecoder.decode(queryString,"utf-8"));
			System.out.println(URLDecoder.decode(queryString,"gb2312"));
			TopDocs results=search.searchQuery(queryString, 100);
			String[] paths = null;
			String[] titles = null;
			String[] descriptions = null;
			String[] imgURL = null;
			
			
			//System.out.println(autocomplete.toString()+" "+corrections.toString()+" "+suggestions.toString());
			if (results != null) {
				ScoreDoc[] hits = showList(results.scoreDocs, page);
				if (hits != null) {
					paths = new String[hits.length];
					titles = new String[hits.length];
					descriptions = new String[hits.length];
					imgURL = new String[hits.length];
					for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
						//Document doc = search.getDoc(hits[i].doc);
						paths[i] = search.getDoc(hits[i].doc).get("path");
						titles[i] = search.getDecoratedTitle(hits[i].doc) ;
						descriptions[i] = search.getDecoratedDescription(hits[i].doc);
						imgURL[i] = search.getImageUrl(paths[i].substring(33).replace('\\', '/'));
						System.out.println(paths[i].substring(33));
						System.out.println(paths[i]+"####"+imgURL[i]);  
						//System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score + " title= " + search.getDoc(hits[i].doc).get("title"));			
						//System.out.println("TITLE:"+titles[i]);  
					}  
				} else {
					System.out.println("page null");  
				}
			}else{
				System.out.println("result null");
			}
			ArrayList<String> a1 = search.getCompletion(queryString);
			ArrayList<String> b1 = search.getCorrection(queryString);
			List<String> c1 = search.getSuggestion(queryString);
			
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("paths", paths);
			request.setAttribute("titles", titles);
			request.setAttribute("imgURL", imgURL);
			request.setAttribute("descriptions", descriptions);
			
			String [] autocomplete = new String[a1.size()];
			String [] corrections = new String[b1.size()];
			String [] suggestions = new String[c1.size()];
			for(int i = 0; i!= a1.size();i++)
				autocomplete[i] = a1.get(i);
			for(int i = 0; i!= b1.size();i++)
				corrections[i] = b1.get(i);
			for(int i = 0; i!= c1.size();i++)
				suggestions[i] = c1.get(i);
					
			request.setAttribute("autocomplete", autocomplete);
			request.setAttribute("corrections", corrections);
			request.setAttribute("suggestions", suggestions);
			
			request.getRequestDispatcher("/results.jsp").forward(request,response); 
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}
