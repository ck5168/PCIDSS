<%@ page language="java" contentType="text/html; charset=BIG5" %><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
 
<%@ include file="/html/CM/header.jsp" %> 

<!-- Import resource -->
<%@ page import="java.util.*"%>  
<%@ page import="com.ck.common.util.STRING"%>
<%@ page import="com.ck.common.util.page.SelectOptUtil"%>
<%@ page import="com.ck.common.util.FieldOptionList"%>
<%@ page import="com.ck.common.util.trx.RespCtxUtil"%>
<%@ page import="com.igsapp.wibc.dataobj.Context.ResponseContext"%>
<%@ page import="com.igsapp.wibc.dataobj.html.HttpTxContext" %>
<%@ page import="com.igsapp.wibc.dataobj.html.HttpResponseContext" %>
<%@ page import="org.apache.log4j.*"%>

<!--
AVE01680.jsp  
Kevin Chen
-->

<!-- JSP Sriptlet -->
<%!
	static Logger log = Logger.getLogger("AVE01680.jsp");
%>
<%	
	HttpTxContext ctxt = new HttpTxContext(session);
	HttpResponseContext resp = new HttpResponseContext(request);
	RespCtxUtil ctxUtil=new RespCtxUtil(resp);	
	
	String IMG_KIND_VALUE ="";
	SelectOptUtil IMG_KIND_OPT = ctxUtil.getOutputData_OptionList("IMG_KIND_OPT");	
	
	Map jvm = (Map)ctxt.getValue("jvm");						
	if(jvm!=null){
		pageContext.setAttribute("jvm",jvm);			
		IMG_KIND_VALUE = jvm.get("IMG_KIND_VALUE").toString();
	}
	
	List jvl = (List)ctxt.getValue("jvl");						
	if(jvm!=null){
		pageContext.setAttribute("jvl",jvl);
	}
%>
	<meta http-equiv="Content-Type" content="text/html; charset=big5">
	<title></title>
	<!--Javascript  css-->
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/AlertHandler.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/PageControllerObj.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/pageController.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/validation.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/Validator.js"></script>	
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/utility.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/AV/js/html.js"></script>    <!-- HTML-->	
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/ajax/prototype.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/ajax/CSRUtil.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/RPTUtil.js"></script>
	<link href="<%=cssBase%>/cm.css" rel="stylesheet" type="text/css">

<!--JavaScript -->

<script language="JavaScript" type="text/JavaScript">
html = new html();
var AVE01680 = {dispatcher: '/AVWeb/servlet/HttpDispatcher'};	//Ajax
var caseWin;

//*************************************************************************************************************

function stat(){
	
	if(document.bar1!=null){
		var a = pageYOffset+window.innerHeight-document.bar1.document.height-15;
		document.bar1.top = a;
	}
	setTimeout('stat()',2);
}
function fix(){	
	
	if(document.getElementById('bar1')!=null){
		var a=document.body.scrollTop-document.getElementById('bar1').offsetHeight+30;
		bar1.style.top = a;
	}else{
		stat();
	}
}
//******************************************************************************

function initApp(){
	
	if(window.opener == null) {
		fix();
	}
	
	displayMessage();

}
//******************************************************************************

function action_query(form, btn){

	clearAllINPUT(form);		
	var validator = new Validator();
	validator.errHandler = new AlertHandler();
	
	if(form.RCPT_NO.value == ''){
		validator.define('RCPT_NO','','please input no','string','-1','-1');
	}
	
	validator.errHandler.clear();
	if(!validator.validate()){
		validator.errHandler.display();
		return false;
	}
	form.action = '<%=dispatcher%>/AVE0_1680/query';
	form.target = '';
	submitOnce(btn);
}
//******************************************************************************

function action_view(form, btn, FILE_PATH, isDownload){

	if(FILE_PATH == ''){
		alert('Query first');
		return false;
	}
	
	// isUsingJNLP ==> ${isUsingJNLP}
	<c:choose>
		<c:when test='${jvm.IS_PDF eq "Y"}'>
			new Ajax.Request( '<%=dispatcher%>/AVE0_1680/showPDF',
			{ 
				parameters: { 
					OPUNIT : '${jvm.OPUNIT}', 
					EMPID : '${jvm.EMPID}', 
					EMPNAME : '${jvm.EMPNAME}', 
					IMGPATH : '${jvm.IMGPATH}', 
					IMGFILENAME : '${jvm.IMGFILENAME}',
					IMG_KEY : '${jvm.IMG_KEY}',
					IMG_KIND_LINK : '${jvm.IMG_KIND_LINK}',
					RCPT_NO_LINK : '${jvm.RCPT_NO_LINK}',
					IS_PDF : '${jvm.IS_PDF}',
					isDownload : isDownload
				},
				onSuccess: function( response ) {
					var resp = response.responseJSON;
					if(CSRUtil.isSuccess(resp)){
								
						
						if (isDownload == 'T') {
							if(resp.fileFullPath && ("" + resp.fileFullPath).match('^http[s]?:\/\/') ){
								window.open(resp.fileFullPath);
							}else{
								RPTUtil.download({'downloadFileName': resp.encryptMarkedFileName, 'downloadFileFullPath': resp.encryptMarkedFileFullPath});
							}
							return;
						}
						
						delete resp['encryptMarkedFileFullPath'];
						
						var markedFileFullPath = resp.markedFileFullPath;
								
						var before_action_params = {
							FILE_NAME : markedFileFullPath.substring(markedFileFullPath.lastIndexOf('/') + 1),
							FILE_PATH : resp.fileFullPath,
							OPUNIT : '${jvm.OPUNIT}',
							EMPID : '${jvm.EMPID}',
							EMPNAME : '${jvm.EMPNAME}',
							IS_PDF : '${jvm.IS_PDF}',
							IMG_KEY : '${jvm.IMG_KEY}',
							IMG_KIND_LINK : '${jvm.IMG_KIND_LINK}',
							ENABLE_DOWNLOAD : '${jvm.ENABLE_DOWNLOAD}'
						};
						
						
						var encryptTempFileFullPath = resp['encryptTempFileFullPath'];
						
						var oForm = document.getElementById('pdfAction');
						if(!oForm){
							oForm = document.createElement("form");
							oForm.id='pdfAction';
							oForm.name='pdfAction';
							oForm.action='/ZRWeb/AppletServlet';
							oForm.method="post";
							oForm.style.display="none";
				
							var currentDate = new Date();
							var windowName = '_RPTUtil_window_'+currentDate.getFullYear()+(currentDate.getMonth()+1)+currentDate.getDate()+currentDate.getHours()+currentDate.getMinutes()+currentDate.getSeconds()+currentDate.getMilliseconds();
							var windowOpenAttrs = 'status=no,toolbar=no,menubar=no,resizable=yes,scrollbars=yes,fullscreen=yes';
							window.open('',windowName,windowOpenAttrs,true);
							oForm.setAttribute('target', windowName);
							document.body.appendChild(oForm);
				
						}else{
							oForm.innerHTML = "";
						}

						oForm.appendChild(RPTUtil.createHiddenElementByPrintParameter('encryptTempFileFullPath', encryptTempFileFullPath));
						
						oForm.submit();	
						
					}
				}
			});
		</c:when>
		<c:otherwise>	
			new Ajax.Request( '<%=dispatcher%>/AVE0_1680/showTIF',
			{ 
				parameters: { 
					OPUNIT : '${jvm.OPUNIT}', 
					EMPID : '${jvm.EMPID}', 
					EMPNAME : '${jvm.EMPNAME}', 
					IMGPATH : '${jvm.IMGPATH}', 
					IMGFILENAME : '${jvm.IMGFILENAME}',
					IMG_KEY : '${jvm.IMG_KEY}',
					IMG_KIND_LINK : '${jvm.IMG_KIND_LINK}',
					RCPT_NO_LINK : '${jvm.RCPT_NO_LINK}',
					IS_OLD : '${jvm.IS_OLD}',
					IS_PDF : '${jvm.IS_PDF}',
					isDownload : isDownload
				},
				onSuccess: function( response ) {
					var resp = response.responseJSON;
					if(CSRUtil.isSuccess(resp)){
								
						
						if (isDownload == 'T') {
							if(resp.fileFullPath && ("" + resp.fileFullPath).match('^http[s]?:\/\/') ){
								window.open(resp.fileFullPath);
							}else{
								RPTUtil.download({'downloadFileName': resp.encryptMarkedFileName, 'downloadFileFullPath': resp.encryptMarkedFileFullPath});
							}
							return;
						}
						
						var markedFileFullPath = resp.markedFileFullPath;
														
						var params = {
							FILE_NAME : markedFileFullPath.substring(markedFileFullPath.lastIndexOf('/') + 1),
							FILE_PATH : resp.fileFullPath,
							OPUNIT : '${jvm.OPUNIT}',
							EMPID : '${jvm.EMPID}',
							EMPNAME : '${jvm.EMPNAME}',
							IS_PDF : '${jvm.IS_PDF}',
							IMG_KEY : '${jvm.IMG_KEY}',
							IMG_KIND_LINK : '${jvm.IMG_KIND_LINK}',
							RCPT_NO_LINK : '${jvm.RCPT_NO_LINK}',
							IS_OLD : '${jvm.IS_OLD}',
							ENABLE_DOWNLOAD : '${jvm.ENABLE_DOWNLOAD}'
						};
						var beforeActions = [];
						beforeActions.push({
							'url' : '<%=dispatcher%>/AVE0_1680/downloadTIF',
							'parameters' : Object.toJSON(params),
							'IS_PDF' : '${jvm.IS_PDF}'
						});
						
						delete resp['encryptMarkedFileFullPath'];
						<c:choose>
						<c:when test="${not isUsingJNLP}">
						RPTUtil.executeSecurityPrintHandler(resp, true, beforeActions);
						</c:when>
						<c:otherwise>
						<%--PCIDSS: JNLP to HTML5--%>
					    //resp['submitParam'] = { 'JNLP':'Y' };
						RPTUtil.executeSecurityPrintHandler(resp, true, beforeActions);
						</c:otherwise>
						</c:choose>
					}
				}
			});
		</c:otherwise>
	</c:choose>
}

function closeImg(){
	caseWin = null;
}
//*****************************************************************************

function action_link(IMG_KIND, DATA_KIND, RCPT_NO, IMG_KEY){


	var form = document.main;
	var btn = document.getElementById('query_btn');
	form.action = '<%=dispatcher%>/AVE0_1680/query?RCPT_NO_LINK=' + RCPT_NO
												+ '&IMG_KIND_LINK=' + IMG_KIND
												+ '&IMG_KEY_LINK=' + IMG_KEY
												+ '&DATA_KIND_LINK=' + DATA_KIND
												+ '&LINK_TYPE=<c:out value='${jvm.LINK_TYPE}' />';
	form.target = '';
	submitOnce(btn);
	return true;

}
//*****************************************************************************

function clearAllINPUT(form){
	if (document.all || document.getElementById){
	
		for (i=0;i<form.length;i++){
			var tempobj=form.elements[i]
			if(tempobj.type.toLowerCase()=='text'){
				tempobj.style.backgroundColor='#FFFFFF';
			}
		}
	}
}
//******************************************************************************
function changeQueryText(IMG_KIND) {
	var queryText = document.getElementById("queryText");
	var queryText2 = document.getElementById("queryText2");
	if(queryText){
		if(IMG_KIND == "AB90" || IMG_KIND == "AB92") {
			queryText.innerHTML = "(APPLY ID)";
		} else if(IMG_KIND == "AB93") {
			queryText.innerHTML = "(AUTH ID)";
		} else if(IMG_KIND == "AB94") {
			queryText.innerHTML = "(INSURE ID)";
		} else if(IMG_KIND == "AB95" || IMG_KIND == "AB91" || IMG_KIND == "AB09" ) {		
			queryText.innerHTML = "(PNO)";
		} else if(IMG_KIND == "AB02") {		
			queryText.innerHTML = "(RNO)";
		} else {
			queryText.innerHTML = "";
		}	
	}
	if(queryText2){
		if(IMG_KIND == "AB90" || IMG_KIND == "AB92") {
			queryText2.innerHTML = "(APLID)";
		} else if(IMG_KIND == "AB93") {
			queryText2.innerHTML = "(AUTHID)";
		} else if(IMG_KIND == "AB94") {
			queryText2.innerHTML = "(INSUREID)";
		} else if(IMG_KIND == "AB95" || IMG_KIND == "AB91" || IMG_KIND == "AB09" ) {		
			queryText2.innerHTML = "(PNO)";
		} else if(IMG_KIND == "AB02") {		
			queryText2.innerHTML = "(RNO)";
		} else {
			queryText2.innerHTML = "";
		}
	}
	
}
</script>
</head>

<body bgcolor="#F0FBC6" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad='initApp();' onResize='fix()' onScroll="fix()" > 


	<center>	
				
		<span id="bar1" style="position: absolute; left: 0; top: 0; width: 100%; z-index: 9; visibility: visible">
			<table width="100%" height="30" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="4" background="<%=imageBase%>/CM/border_01.gif"><img src="<%=imageBase%>/CM/border_01.gif" width="4" height="12">
					</td>
					<td valign="top" bgcolor="#F0FBC6">
						<table width="100%" border="0" cellpadding="0" cellspacing="0" background="<%=imageBase%>/CM/border_03.gif">
							<tr>
								<td height="4"><img src="<%=imageBase%>/CM/border_03.gif" width="12"	height="4"></td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="2" cellspacing="0"	class="subTitle">
							<tr>
								<td width="20" height="24"><div align="center"><font size="-5">●</font></div></td>
								<td><b>IMAGE</b></td>
								<td><div align="right">NO：AVE01680</div></td>
							</tr>
						</table>
					</td>
					<td width="4" background="<%=imageBase%>/CM/border_02.gif"><img src="<%=imageBase%>/CM/border_02.gif" width="4" height="12"></td>
					<td width="5" class="tbBox"><img src="<%=imageBase%>/CM/ecblank.gif"	width="5" height="1"></td>
				</tr>
			</table>
		</span>
		
		<!-- Bolck_1_ End-->
		
		
		
		<table width="100%" height="30" border="0" cellpadding="0" cellspacing="0">
			<tr>	<td>&nbsp;</td></tr>
		</table>
		
		<!-- Bolck_2_End-->
		
		
		<table width="100%" height="100%" border="0" cellpadding="0"	cellspacing="0">
			<tr>
				<td width="4" background="<%=imageBase%>/CM/border_01.gif"><img src="<%=imageBase%>/CM/border_01.gif" width="4" height="12"></td>
				<td width="100%" valign="top" bgcolor="#F0FBC6">
					<table width="100%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td valign="top">					
								<table width=97% border=0 align=center cellpadding="5" cellspacing="1" bgcolor="#003366" >									
									<tbody>
										<tr>
											<td class="tbBox2"><img src="<%=imageBase%>/CM/icon_dot11.gif" width="18" height="16">IMG</td>
										</tr>
										<tr bgcolor=#FFFFFF>
											<td >
										
												
	
												<form name="main" id="main" method="post" action="">

													<input name="LINK_TYPE" type="hidden" value="<c:out value="${jvm.LINK_TYPE}" />" >																

													<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
													
														<tr>
														
													<c:choose>
														<c:when test="${jvm.LINK_TYPE == 'N'}">	
												
															<td class="tbYellow" width="15%">FILENO</td>
															<td class="tbYellow2" >
																<select name="IMG_KIND_OPT" id="IMG_KIND_OPT" TABINDEX="1" class="textBox2" onChange="changeQueryText(this.value)">                  	
																	<%=IMG_KIND_OPT.getOptionHtml(IMG_KIND_VALUE)%>
																</select>																																												
															</td>
															<td class="tbYellow" width="15%">FILENO<span id="queryText"/></td>
															<td class="tbYellow2" >																
																<input name="RCPT_NO" type="text" class="textBox2" value="<c:out value="${jvm.RCPT_NO}" />" size="20" maxlength="30" onblur="html.toUppercase(this)">																
															</td>
																
														</c:when>																																										
														<c:otherwise>												
														
															<td class="tbYellow" width="15%">FILENO</td>
															<td class="tbYellow2" >
																<select name="IMG_KIND_OPT" id="IMG_KIND_OPT" TABINDEX="1" class="textBox2" onChange="" >                  	
																	<%=IMG_KIND_OPT.getOptionHtml(IMG_KIND_VALUE)%>
																</select>																																												
															</td>
															<td class="tbYellow" width="15%">FILENO<span id="queryText2" name="queryText2"/></td>
															<td class="tbYellow2" >																
																<input id="RCPT_NO" name="RCPT_NO" type="text" class="textBox2" value="<c:out value="${jvm.RCPT_NO}" />" size="20" maxlength="14" onblur="html.toUppercase(this)" readOnly>																
															</td>
																										
														</c:otherwise>			
													</c:choose>			
												
															<td class="tbYellow2">
																	<input id="query_btn" type="button" class="button" value="QUERY" onClick="action_query(this.form, this)" >
																</td>
															</tr>		
														</table>
													
													<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
														<tr>
															<td class="tbYellow" >QUERYLIST</td>
														</tr>		
														<c:forEach var="viewData" items="${jvl}" varStatus="status" >  														
															<ck:even><TR ID='ROWID' align="center" class='tbYellow2'> </ck:even>
															<ck:odd> <TR ID='ROWID' align="center" class='tbBlue3'> </ck:odd>
																<td align="left"><a href="#" onClick="action_link('<c:out value="${viewData.IMG_KIND}" />','<c:out value="${viewData.DATA_KIND}" />','<c:out value="${viewData.RCPT_NO}" />','<c:out value="${viewData.IMG_KEY}" />');"><c:out value="${viewData.RCPT_NO}" /></a></td>					
															</tr>
														</c:forEach>
													</table>													

													<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
													
													<c:choose>
														<c:when test="${jvm.LINK_TYPE == 'N'}">
													
															<tr>
																<td class="tbBlue2" width="15%">FNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.IMG_KIND_LINK}" /></td>															
																<td class="tbBlue2" width="15%">RNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.RCPT_NO_LINK}" /></td>	
																<td class="tbBlue2" width="15%">BNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.BOXNO}" /></td>										
															</tr>
															<tr>
																<td class="tbBlue2" >RDIV</td>
																<td class="tbBlue3" ><c:out value="${jvm.PROCDIVNO}" /></td>															
																<td class="tbBlue2" >RNAME</td>
																<td class="tbBlue3" ><c:out value="${jvm.FILEOPERATOR}" /></td>	
																<td class="tbBlue2" >RDATE</td>
																<td class="tbBlue3" ><ck:calendar type="ROC" isInput="false" value="${jvm.FILEDATE}"/></td>															
															</tr>		
															<tr>														
																<td class="tbBlue2" >RNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.BCHNO}" /></td>															
																<td class="tbBlue2" >FILENAME</td>
																<td class="tbBlue3" ><c:out value="${jvm.IMGFILENAME}" /></td>															
																<td class="tbBlue2" >NOTE</td>
																<td class="tbBlue3" ><c:out value="${jvm.MEMOTEXT}" /></td>		
															</tr>					
															<tr>
																<td class="tbBlue2" >CNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.DVDNO}" /></td>															
																<td class="tbBlue2" >BDATE</td>
																<td class="tbBlue3" ><ck:calendar type="ROC" isInput="false" value="${jvm.BOXDATE}"/></td>															
																<td class="tbBlue2" >BOXNAME</td>
																<td class="tbBlue3" ><c:out value="${jvm.BOXOPERATOR}" /></td>	
															</tr>		
															<tr>
																<td class="tbBlue2" width="15%">FILE</td>
																<td class="tbBlue3" colspan="5"><c:out value="${jvm.IMGPATH}" /></td>															
															</tr>																													

														</c:when>
																																													
														<c:otherwise>
														
															<tr>
																<td class="tbBlue2" >FDATE</td>
																<td class="tbBlue3" ><ck:calendar type="ROC" isInput="false" value="${jvm.FILEDATE}"/></td>															
																<td class="tbBlue2" >FNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.BCHNO}" /></td>															
																<td class="tbBlue2" >FILENAME</td>
																<td class="tbBlue3" ><c:out value="${jvm.IMGFILENAME}" /></td>
															</tr>		
															<tr>														
																<td class="tbBlue2" >CNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.DVDNO}" /></td>															
																<td class="tbBlue2" >PDATE</td>
																<td class="tbBlue3" ><ck:calendar type="ROC" isInput="false" value="${jvm.BOXDATE}"/></td>															
																<td class="tbBlue2" >BOXNO</td>
																<td class="tbBlue3" ><c:out value="${jvm.BOXNO}" /></td>		
															</tr>						
															<tr>
																<td class="tbBlue2" width="15%">IMG</td>
																<td class="tbBlue3" colspan="5"><c:out value="${jvm.IMGPATH}" /></td>															
															</tr>																													
												
														</c:otherwise>			
													</c:choose>
													
														<tr>
															<td class="tbBlue3" colspan="6" align="center">
																<input id="view_btn" type="button" class="button" value="OPEN" onClick="action_view(this.form, this, '<c:out value="${jvm.IMGPATH}" />')" />
																
																<input id="download_btn" type="button" class="button" value="DOWNLOAD" onClick="action_view(this.form, this, '<c:out value="${jvm.IMGPATH}" />', 'T')" />
															</td>
														</tr>																	
													</table>
												</form>
												
												<!-- Bolck_4_End-->
											</td>
										</tr>
									</tbody>
								</table>
			<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
			  <tr>
				<td width="10%" bgcolor="#FFFFFF">
					<font color="red">
&nbsp;&nbsp;&nbsp;&nbsp;NOTE：<BR>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<BR>
					</font>
				</td>
			</tr>
		</table>
							</td>
						</tr>
					</table>
				</td>
				<td width="4" background="<%=imageBase%>/CM/border_02.gif"><img src="<%=imageBase%>/CM/border_02.gif" width="4" height="12"></td>
				<td width="5" class="tbBox"><img src="<%=imageBase%>/CM/ecblank.gif" width="5" height="1"></td>
			</tr>
		</table>
		<!-- Bolck_3_End-->
	</center>
	<%@ include file="/html/CM/msgDisplayer.jsp" %>
</BODY>
</HTML>
