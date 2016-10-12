<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
System.out.println(request.getCharacterEncoding());
response.setCharacterEncoding("utf-8");
System.out.println(response.getCharacterEncoding());
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>搜索</title>
	<!-- bootstrap -->
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />

    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      background-size: cover;
     }
    </style>
</head>
<body background="p2.jpg">
	<center>
	<div style="height:70px;margin-top:110px" >
  	</div>
  	<div style="height:102px">
  	<h1 class="text-warning"><img src="p3.png" class="img-rounded" style="height:55px; width:55px; ">THU新闻搜索</h1>
  	<form id="form1" name="form1" method="get" action="servlet/CampusServer" class="form-search" style="margin-top:20px">
    	<label>
      		<input name="query" type="text" size="50" id="appendedInputButton" style = "width:500px; height:33px"/>
    	</label>
    	<label>
    		<input class = "btn btn-success" type="submit" name="Submit" value="Search" style = "width:100px;"/>
    	</label>
   	</form>
   	</div>
   </center>
</body>
</html>
