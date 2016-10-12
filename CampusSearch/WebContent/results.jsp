<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String [] autocomplete = (String[]) request.getAttribute("autocomplete");
//String path = request.getContextPath();
//String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
//String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title> CampusSearch </title>
    <!-- bootstrap -->
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/js/bootstrap.min.js" rel="stylesheet" />
    <script src="jquery-1.11.3.min.js"></script>
    <script src="bootstrap/js/bootstrap-typeahead.js"></script>
	
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      background-size: cover;
     }
    </style>
    
</head>

<body>

<script>
	$(document).ready(function($) {
	   // Workaround for bug in mouse item selection
	   $.fn.typeahead.Constructor.prototype.blur = function() {
	      var that = this;
	      setTimeout(function () { that.hide() }, 250);
	   };
	 
	   $('#appendedInputButton').typeahead({
	      source: function(query, process) {
	         return [
	         <% for (int i = 0; i < autocomplete.length; ++i) { %>
	         	"<%= autocomplete[i] %>",
	         <% } %> ""];
	      }
	   });
	})
</script>

<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
%>

<div class = "container">
<div class = "row-fluid">
<h3 class="text-warning"><img src="p3.png" class="img-rounded" style="height:30px; width:30px; ">THU新闻搜索</h3>
</div>
<div>
  <form id="form1" name="form1" method="get" action="CampusServer" class="form-search">
    <label>
      <input autocomplete="off" data-provide="typeahead" data-items="4" name="query" value="<%=currentQuery%>" id="appendedInputButton" type="text" size="70" style = "width:400px;" data-items="4" />
    </label>
    <label>
    <input type="submit" name="Submit" value="Search"  class = "btn btn-success" />
    </label>
  </form>
</div>
<div class = "row-fluid">
	<div class = "span8">
  	<table class = "table table-hover">
  	<% 
	  	
		String [] corrections = (String[]) request.getAttribute("corrections");
		String [] suggestions = (String[]) request.getAttribute("suggestions");
		String[] imgURL = (String[]) request.getAttribute("imgURL");
	  	String[] paths=(String[]) request.getAttribute("paths");
	  	String[] titles = (String[] )request.getAttribute("titles");
	  	String[] descriptions = (String[]) request.getAttribute("descriptions");
		if(corrections!=null && corrections.length>0){
	  		%><tr><td> 你要查找的是不是： <l> <%
	  		for(int i=0; i < Math.min(corrections.length, 3); i++){
	  			if (corrections[i].equals(currentQuery)) continue;
		  		%><u><a class="text-error" href="/CampusSearch/servlet/CampusServer?query=<%= corrections[i] %>&Submit=Search">
		  			<%= corrections[i] %>
		  		</a></u>&nbsp; <%
		  	}
	  		%></l></td></tr> <%
	  	}
	  	if(paths!=null && paths.length> 0) {
	  		for(int i=0;i<paths.length;i++){
	  		%><tr><td><%=(currentPage-1)*10+i+1%>.
	  		<a href= "<%="http://"+paths[i].substring(paths[i].indexOf("news.tsinghua"))%>" target=" <%=i%>"><%= titles[i] %>
	  		</a></br>
	  		<div class="row-fluid">
	  		<%
	  		if(imgURL[i] != null)
	  		{
	  			%>
	  			<div class = "span2">
	  			<img  src="http://news.tsinghua.edu.cn<%= imgURL[i] %>">
	  			</img>
	  			</div>
	  			<div class = "span8">
		  		<%= descriptions[i] %>
		  		</div>
		  		</td></tr>
	  			<%
	  		} else {
		  		%>
		  		<div></div>
		  		<div class = "span10">
		  		<%= descriptions[i] %>
		  		</div>
		  		</td></tr>
	  		<% } %>
  		<%
  		}; 
  		%>  
	  	<%}else{ %>
	  		<tr><td>no such result</td>></tr><%
	  	}; 
  	%>
  	</table>
  	<div class = "pagination">
  	<ul>
		<%if(currentPage>1){ %>
			<li><a href="CampusServer?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a></li>
		<%}; %>
		<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
			<li><a href="CampusServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li class="disabled"><a href = ""><%=currentPage%></a></li>
		<%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
			<li><a href="CampusServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li><a href="CampusServer?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a></li>
	</ul>
	</div>
	</div>
	<div class="span4">
	<table class="table">
	<%
	if(false && autocomplete!=null && autocomplete.length>0){
	  		%><tr><td> 自动补全词：<%
	  		for(int i = 0; i < autocomplete.length;i++){
	  			if (autocomplete[i].equals(currentQuery)) continue; %>
		  		<a href="/CampusSearch/servlet/CampusServer?query=<%= autocomplete[i] %>&Submit=Search">
		  			<%= autocomplete[i] %>
		  		</a>
		  	<% }
	  		%></td></tr> <% 
	  	}
		
	  	if(suggestions!=null && suggestions.length>0){
	  		%><tr><td> <h4 class = "text-success">相关词汇：</h4><h5 class = "text-warning"> <%
		  	for(int i=0; i < suggestions.length;i++){ %>
		  		<a class = "text-warning" href="/CampusSearch/servlet/CampusServer?query=<%= suggestions[i] %>&Submit=Search">
		  		<%= suggestions[i] %> 
		  		</a>
	  			<br/>
		  	<% }
	  		%></h5></td></tr> <%
	  	}%>
	  </table>
	</div>
  </div>
</div>

</body>
