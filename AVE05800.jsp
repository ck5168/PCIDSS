<%@ page language="java" contentType="text/html; charset=BIG5" %>
   
<%@ include file="/html/CM/header.jsp" %> 

<!-- Import resource，新增請放下面  -->
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
程式：AVE05800.jsp 
功能：影像歸檔
完成：2007/01/11
更新：
-->

<!-- JSP Sriptlet ，新增請放下面的-->
<%!
	static Logger log = Logger.getLogger("AVE05800.jsp");
%>
<%
	
	HttpTxContext ctxt = new HttpTxContext(session);
	HttpResponseContext resp = new HttpResponseContext(request);
	RespCtxUtil ctxUtil=new RespCtxUtil(resp);	
	
	String status = (String)resp.getOutputData("status");				// 頁面 Button 按鈕控制
	String IMG_KIND_VALUE ="";
	String BCHNO_VALUE = "";
	SelectOptUtil IMG_KIND_OPT = ctxUtil.getOutputData_OptionList("IMG_KIND_OPT");	//文件代碼
	SelectOptUtil BCHNO_OPT = ctxUtil.getOutputData_OptionList("BCHNO_OPT");		//歸檔批號
	
	Map jvm = (Map)ctxt.getValue("jvm");						//畫面查詢結果
	if(jvm!=null){
		pageContext.setAttribute("jvm",jvm);			
		IMG_KIND_VALUE = jvm.get("IMG_KIND_VALUE").toString();
		BCHNO_VALUE = jvm.get("BCHNO_VALUE").toString();
	}
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>

	<meta http-equiv="Content-Type" content="text/html; charset=big5">
	<title></title>
	<!--匯入外部Javascript 與 css, 新增請放下面-->
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/AlertHandler.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/PageControllerObj.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/pageController.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/validation.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/Validator.js"></script>	
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/utility.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/ajax/prototype.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/ajax/CSRUtil.js"></script>
	<script language="JavaScript" src="<%=htmlBase%>/AV/js/html.js"></script>    <!-- HTML  的公用程式-->
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/HotKey.js"></script><!--功能鍵的使用-->
	<script language="JavaScript" src="<%=htmlBase%>/CM/js/ui/popupWin.js"></script>
	<link href="<%=cssBase%>/cm.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#F0FBC6" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad='initApp();' onResize='fix()' onScroll="fix()" > 


<!--此網頁自建使用的JavaScript 放下面 -->

<script language="JavaScript" type="text/JavaScript">
var AVE05800 = {dispatcher: '/AVWeb/servlet/HttpDispatcher'};	//Ajax
html = new html();
//******************************************************************************
<!--修正畫面-->
function stat(){
	//註:firefox 3 已經不支援window.onresize，暫不處理
	if(document.bar1!=null){
		var a = pageYOffset+window.innerHeight-document.bar1.document.height-15;
		document.bar1.top = a;
	}
	setTimeout('stat()',2);
}
function fix(){
	//瀏覽器偵測 navigator.appName 內部編碼名稱...不要再使用
	//appName 瀏覽器的正式名稱
	//navigator.appName IE 傳回 Microsoft Internet Explorer, IE11 傳回 Netscape
	//Firefox, Safari, Chrome 均傳回 Netscape；Opera 傳回 Opera	
	
	if(document.getElementById('bar1')!=null){
		var a=document.body.scrollTop-document.getElementById('bar1').offsetHeight+30;
		bar1.style.top = a;
	}else{
		stat();
	}
}
//******************************************************************************
<!--網頁初始化-->
function initApp(){
	if(window.opener == null) {
		fix();
	}
	enableElements('<%=status%>');
	displayMessage();
	AddHotKey();
}
//******************************************************************************
<!--查詢-->
function action_query(form, btn){

	clearAllINPUT(form);		
	var validator = new Validator();
	validator.errHandler = new AlertHandler();
	
	if(form.DIV_NO.value == ''){
		validator.define("DIV_NO","","請輸入正確單位代號","string","-1","-1");
	}
	
	validator.errHandler.clear();
	if(!validator.validate()){
		validator.errHandler.display();
		return false;
	}
	if(form.OPR_ID.value == '' || form.OPR_ID.value != '<c:out value="${jvm.USER_ID}" />'){
		var agree=confirm("您所查詢的條件可能包括非本人影像上傳件。\n"
							+ "請注意同一歸檔批號，不可兩人同時做裝箱確認。\n"
							+ "按『確定』執行查詢作業，按『取消』停止查詢作業"
							);
		if (agree == false){
			return false;
		}
	}
	
	form.action = "<%=dispatcher%>/AVE0_5800/query";
	form.target = "";
	submitOnce(btn);
	RemoveHotKey();
	return true;	
}
//******************************************************************************
<!--讀取檔案-->
function action_readfile(form, btn){

	
	if(form.DIV_NO.value != form.DIV_NO_HIDDEN.value
		|| getIMG_KIND_OPT() != form.IMG_KIND_HIDDEN.value
		|| form.OPR_ID.value != form.OPR_ID_HIDDEN.value){
		alert('查詢歸檔批號鍵值改變，請重新 查詢歸檔批號 再做 讀取檔案');
		return false;
	}

	form.action = "<%=dispatcher%>/AVE0_5800/readfile";
	form.target = "";
	submitOnce(btn);
	return true;	
}
//******************************************************************************
<!--封箱進號-->
function action_close(form, btn){

	
	if(form.DIV_NO.value != form.DIV_NO_HIDDEN.value
		|| getIMG_KIND_OPT() != form.IMG_KIND_HIDDEN.value){
		alert('查詢歸檔批號鍵值改變，請重新 查詢歸檔批號 再做 封箱進號');
		return false;
	}

	//3.3	執行Step 5 列印箱號條碼。(960903新增)
	action_print_box(form, btn);
		 					
	new Ajax.Request(
		AVE05800.dispatcher + '/AVE0_5800/close', 
		{
			parameters:Form.serialize("main"),
		 	onSuccess: function(XHT, resp){
			
		 		if(CSRUtil.isSuccess(resp)){
		 			if(resp.CHK_DATA != ''){
		 				alert(resp.CHK_DATA);
		 				return false;
		 			}else{
		 				form.BOX_SER_NO.value = resp.BOX_SER_NO;
		 				form.BOX_SER_NO_SHOW.value = resp.BOX_SER_NO_SHOW;
		 				
		 				$('close_btn').disabled = true;
		 				
		 			}
					
				}
		 	}
		}	
	);	
}
//******************************************************************************
<!--裝箱確認-->
function action_confirm(form, btn){

	
	if(form.DIV_NO.value != form.DIV_NO_HIDDEN.value
		|| getIMG_KIND_OPT() != form.IMG_KIND_HIDDEN.value
		|| getBCHNO() != form.BCHNO_HIDDEN.value){
		alert('查詢歸檔批號鍵值改變，請重新 查詢歸檔批號 讀取檔案 再做 裝箱確認');
		return false;
	}

	if('<c:out value="${jvm.DB_OPR_ID}" />' != '<c:out value="${jvm.USER_ID}" />'){
		var agree=confirm("您所處理的歸檔批號為[<c:out value="${jvm.DB_OPR_ID}" />]上傳，非本人影像上傳件。\n"
							+ "請注意同一歸檔批號，不可兩人同時做裝箱確認。\n"
							+ "按『確定』執行裝箱確認作業，按『取消』停止裝箱確認作業" 
							);
		if (agree == false){
			return false;
		}
	}

	new Ajax.Request(
		AVE05800.dispatcher + '/AVE0_5800/checkRCPT', 
		{
			parameters:Form.serialize("main"),
		 	onSuccess: function(XHT, resp){
			
		 		if(CSRUtil.isSuccess(resp)){
		 			if(resp.EXE_MSG != 'OK'){
						alert(resp.EXE_MSG);
						return false;
					}else{

						if(resp.IS_FIND == 'N'){
							var agree=confirm('1. 提醒此筆單據(' 
													+ getIMG_KIND_OPT() 
													+ ',' 
													+  resp.BARCODE 
													+ ')未完成點交點收,按[確定]按鈕仍可繼續影像歸檔'
													+'\n \n \n'+'2. 後續請至「文件回收報表/異常報表(多件)」作業確認 ');
							if (!agree){
								return false;
							}		
							
							new Ajax.Request(
								AVE05800.dispatcher + '/AVE0_5800/confirm', 
								{
									parameters:Form.serialize("main"),
								 	onSuccess: function(XHT, resp){
									
								 		if(CSRUtil.isSuccess(resp)){
								 			if(resp.EXE_MSG != ''){
												alert(resp.EXE_MSG);
												return false;
											}else{
											
												var windowParams="toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,"
																+ "resizable=no,width=520,height=200,left=200,top=200"; 	
											   	window.open(
											   			'<%=dispatcher%>/AVE0_5800/printbch?newTxRequest=false&syscode=AV&subcode=AVE0'+
											   			'&BCH_NO='+ form.BCH_NO.value +
											   			'&BCH_NO_SHOW=' + form.BCH_NO_SHOW.value
											   			, 'subwin'
											   			, windowParams); 
											   	
											   	//顯示作業訊息
											   	var msgBoard = CSRUtil.getMsgBoard();
												msgBoard.innerHTML='裝箱確認完成';
												
												//disabled 確認鈕 檔案讀取鈕
												$('readfile_btn').disabled = true;
												$('confirm_btn').disabled = true;
						 						$('BCHNO_OPT_REMOVE').value = getBCHNO_OPT();
											}
										}
								 	}
								}	
							);											
						}else{
							new Ajax.Request(
								AVE05800.dispatcher + '/AVE0_5800/confirm', 
								{
									parameters:Form.serialize("main"),
								 	onSuccess: function(XHT, resp){
									
								 		if(CSRUtil.isSuccess(resp)){
								 			if(resp.EXE_MSG != ''){
												alert(resp.EXE_MSG);
												return false;
											}else{
											
												var windowParams="toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,"
																+ "resizable=no,width=520,height=200,left=200,top=200"; 	
											   	window.open(
											   			'<%=dispatcher%>/AVE0_5800/printbch?newTxRequest=false&syscode=AV&subcode=AVE0'+
											   			'&BCH_NO='+ form.BCH_NO.value +
											   			'&BCH_NO_SHOW=' + form.BCH_NO_SHOW.value
											   			, 'subwin'
											   			, windowParams); 
											   	
											   	//顯示作業訊息
											   	var msgBoard = CSRUtil.getMsgBoard();
												msgBoard.innerHTML='裝箱確認完成';
												
												//disabled 確認鈕 檔案讀取鈕
												$('readfile_btn').disabled = true;
												$('confirm_btn').disabled = true;
						 						$('BCHNO_OPT_REMOVE').value = getBCHNO_OPT();
											}
										}
								 	}
								}	
							);									
						}
	
					}
				}
		 	}
		}	
	);		
	
	


		   			
}

//******************************************************************************
<!--變更 歸檔批號-->
function action_chgBCHNO_OPT(){
	
	//回復 檔案讀取前狀態
	$('readfile_btn').disabled = false;
	$('print_bchno_btn').disabled = true;
	$('print_boxserno_btn').disabled = true;
	$('close_btn').disabled = true;
	$('confirm_btn').disabled = true;
	
	$('BCH_NO_SHOW').value = '';
	$('BCH_NO').value = '';
	$('BOX_SER_NO_SHOW').value = '';
	$('BOX_SER_NO').value = '';
	$('textarea').value = '';
			
	//移除Option 中 已經 確認過的
	for(var i=0; i<$('BCHNO_OPT').length ; i++){
		if($('BCHNO_OPT')[i].value == $('BCHNO_OPT_REMOVE').value){
			$('BCHNO_OPT').remove(i);
			break;
		}
	}	
	
}
//******************************************************************************
<!--讀取歸檔批號 下拉Option 值-->
function getBCHNO_OPT(){
	for(var i=0; i<$('BCHNO_OPT').length ; i++){
		if($('BCHNO_OPT')[i].selected == true){
			return $('BCHNO_OPT')[i].value;
		}
	}
}
//******************************************************************************
<!--取得文件代碼-->
function getIMG_KIND_OPT(){
	var IMG_KIND_OPT = document.getElementsByName('IMG_KIND_OPT');

	for(var i=0; i< IMG_KIND_OPT.length; i++){
		if(IMG_KIND_OPT[i].selected = true){
			return IMG_KIND_OPT[i].value;
		}
	}
}
//******************************************************************************
<!--取得歸檔批號-->
function getBCHNO(){
	var BCHNO_OPT = document.getElementsByName('BCHNO_OPT');

	for(var i=0; i< BCHNO_OPT.length; i++){
		if(BCHNO_OPT[i].selected = true){
			return BCHNO_OPT[i].value;
		}
	}
}
//******************************************************************************
<!--列印箱號-->
function action_print_box(form, btn){

	if(form.DIV_NO.value != form.DIV_NO_HIDDEN.value
		|| getIMG_KIND_OPT() != form.IMG_KIND_HIDDEN.value
		|| getBCHNO() != form.BCHNO_HIDDEN.value){
		alert('查詢歸檔批號鍵值改變，請重新 查詢歸檔批號 讀取檔案 再做 列印箱號');
		return false;
	}
	
	var windowParams="toolbar=no,location=no,status=yes,menubar=no,scrollbars=no,"
						+ "resizable=no,width=520,height=200,left=200,top=200"; 	
   	window.open(
   			'<%=dispatcher%>/AVE0_5800/printbox?newTxRequest=false&syscode=AV&subcode=AVE0'+
   			'&BOX_SER_NO='+ form.BOX_SER_NO.value +
   			'&BOX_SER_NO_SHOW=' + form.BOX_SER_NO_SHOW.value
   			, 'subwin'
   			, windowParams); 
}
//******************************************************************************
<!--列印批號條碼-->
function action_print_bch(form, btn){

	if(form.DIV_NO.value != form.DIV_NO_HIDDEN.value
		|| getIMG_KIND_OPT() != form.IMG_KIND_HIDDEN.value
		|| getBCHNO() != form.BCHNO_HIDDEN.value){
		alert('查詢歸檔批號鍵值改變，請重新 查詢歸檔批號 讀取檔案 再做 批號條碼');
		return false;
	}
	
	var windowParams="toolbar=no,location=no,status=no,menubar=no,scrollbars=no,"
						+ "resizable=no,width=520,height=200,left=200,top=200"; 
						
	var opts = {};		

	opts.parameters = { 'newTxRequest': false, 
	                    'syscode' : 'AV', 
					    'subcode' : 'AVE0', 
					    'BCH_NO' : form.BCH_NO.value,
						'BCH_NO_SHOW' : form.BCH_NO_SHOW.value,
						'DATA_LIST' : form.DATA_LIST.value };
						
	opts.attributes = { 'toolbar' : 'no',
						'location' : 'no',
						'menubar' : 'no',
						'scrollbars' : 'no',
						'resizable' : 'no',
						'width' : '520',
						'height' : '200',
						'left' : '200',
						'top' : '200' }
						
	opts.windowName = 'subwin';

	popupWin.windowOpen('<%=dispatcher%>/AVE0_5800/printbch', opts);					
}
//******************************************************************************
<!--只能輸入數字-->
function numberOnly(){
	return (event.keyCode > 47 && event.keyCode < 58 );
}
//******************************************************************************
<!--將FORM 中 所有INPUT 欄位顏色回復 -->
function clearAllINPUT(form){
	if (document.all || document.getElementById){
	/* 取出指定 form 中所有的input type=text 清除顏色為白色 */
		for (i=0;i<form.length;i++){
			var tempobj=form.elements[i]
			if(tempobj.type.toLowerCase()=="text"){
				tempobj.style.backgroundColor='#FFFFFF';
			}
		}
	}
}
//******************************************************************************
<!--熱鍵功能-->
function AddHotKey(){
	HotKeys.addHotKey(Keys.F2   , new ButtonAction(document.forms[0].query_btn));		
}
//******************************************************************************
<!--刪除熱鍵功能-->
function RemoveHotKey(){
	HotKeys.removeHotKey(Keys.F2   , new ButtonAction(null));		
}
//******************************************************************************
</script>

	<center>
	
		<!-- Bolck_1_Start 表單的開頭 功能名稱 畫面編號-->
		
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
								<td><b>影像歸檔作業</b></td>
								<td><div align="right">畫面編號：AVE05800</div></td>
							</tr>
						</table>
					</td>
					<td width="4" background="<%=imageBase%>/CM/border_02.gif"><img src="<%=imageBase%>/CM/border_02.gif" width="4" height="12"></td>
					<td width="5" class="tbBox"><img src="<%=imageBase%>/CM/ecblank.gif"	width="5" height="1"></td>
				</tr>
			</table>
		</span>
		
		<!-- Bolck_1_ End-->
		
		<!-- Bolck_2_Start 空白區塊 版面跳行-->
		
		<table width="100%" height="30" border="0" cellpadding="0" cellspacing="0">
			<tr>	<td>&nbsp;</td></tr>
		</table>
		
		<!-- Bolck_2_End-->
		
		<!-- Bolck_3_Start 表單主功能區塊-->
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
											<td class="tbBox2"><img src="<%=imageBase%>/CM/icon_dot11.gif" width="18" height="16">影像歸檔作業</td>
										</tr>
										<tr bgcolor=#FFFFFF>
											<td >
										
												<!-- Bolck_4_Start 輸入區塊-->
										
												<form name="main" id="main" method="post" action="">

													<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
														<tr>
															<td width="15%" class="tbYellow">單位代號</td>
															<td class="tbYellow2">
																<input name="DIV_NO" type="text" class="textBox2" value="<c:out value="${jvm.DIV_NO}" />" size="10" maxlength="8" onblur="html.toUppercase(this)">
																<input name="DIV_NO_HIDDEN" type="hidden" value="<c:out value="${jvm.DIV_NO}" />" >
															</td>
															<td width="15%" class="tbYellow">影像上傳人員</td>
															<td class="tbYellow2">
																<input name="OPR_ID" type="text" class="textBox2" value="<c:out value="${jvm.OPR_ID}" />" size="12" maxlength="10" onblur="html.toUppercase(this)">
																<input name="OPR_ID_HIDDEN" type="hidden" value="<c:out value="${jvm.OPR_ID}" />" >															
															</td>
															<td width="10%" class="tbYellow2" rowspan="2">
																<input name="query_btn" id="query_btn" type="button" class="button" value="F2查詢歸檔批號" onClick="action_query(this.form, this)" disabled>
															</td>
														</tr>
														<tr>
															<td class="tbYellow">文件代碼</td>
															<td class="tbYellow2" colspan="3">
																<select name="IMG_KIND_OPT" id="IMG_KIND_OPT" TABINDEX="1" class="textBox2" onChange="">                  	
																	<%=IMG_KIND_OPT.getOptionHtml(IMG_KIND_VALUE)%>
																</select>
																<input name="IMG_KIND_HIDDEN" type="hidden" value="<c:out value="${jvm.IMG_KIND_VALUE}" />" >
																
															</td>
														</tr>
														<tr>
															<td class="tbYellow">歸檔批號</td>
															<td class="tbYellow2"  colspan="3">
																<select name="BCHNO_OPT" id="BCHNO_OPT" TABINDEX="1" class="textBox2" onChange="action_chgBCHNO_OPT()">            	
																	<%=BCHNO_OPT.getOptionHtml(BCHNO_VALUE)%>
																</select>
																<input name="BCHNO_HIDDEN" type="hidden" value="<c:out value="${jvm.BCHNO_VALUE}" />" >
																<input name="BCHNO_OPT_REMOVE" id="BCHNO_OPT_REMOVE" type="hidden" value="" >
															</td>
															<td width="10%" rowspan="2" class="tbYellow2">
																<input id="readfile_btn" name="readfile_btn" type="button" class="button" value="讀取檔案" onClick="action_readfile(this.form, this)" disabled>
															</td>
															</tr>	
															</td>
														</tr>
													</table>	
													<table width="100%" border="0" cellpadding="0" cellspacing="1" class="tbBox2">
														<tr>
															<td width="15%" class="tbYellow">歸檔批號</td>
															<td colspan="2" class="tbYellow2">
																<input name="BCH_NO_SHOW"  id="BCH_NO_SHOW" type="text" class="textBoxDisable" value="<c:out value="${jvm.BCH_NO_SHOW}" />" size="35" maxlength="35" readOnly>
																<input name="BCH_NO" id="BCH_NO" type="hidden" value="<c:out value="${jvm.BCH_NO}" />" >
																<input id="print_bchno_btn" name="print_bchno_btn" type="button" class="button" value="列印批號條碼" onClick="action_print_bch(this.form, this)" disabled>
															</td>
														</tr>
														<tr>
															<td width="15%" class="tbYellow">批號內容描述</td>
															<td colspan="2" class="tbYellow2">
																<input name="BCH_MEMO" id="BCH_MEMO" type="text" class="textBox2" size="20" maxlength="25"/> 
															</td>
														</tr>
														<tr >
															<td class="tbYellow">裝箱號碼</td>
															<td colspan="2" class="tbYellow2">
																<input name="BOX_SER_NO_SHOW"  id="BOX_SER_NO_SHOW" type="text" class="textBoxDisable" value="<c:out value="${jvm.BOX_SER_NO_SHOW}" />" size="35" maxlength="35" readOnly>
																<input name="BOX_SER_NO" id="BOX_SER_NO" type="hidden" value="<c:out value="${jvm.BOX_SER_NO}" />" >
																<input name="confirm_btn" id="confirm_btn" type="button" class="button" value="裝箱確認" onClick="action_confirm(this.form, this)" disabled>
																<input name="print_boxserno_btn" id="print_boxserno_btn" type="button" class="button" value="列印箱號" onClick="action_print_box(this.form, this)" disabled>
																<input name="close_btn" id="close_btn" type="button" class="button" value="封箱進號" onClick="action_close(this.form, this)" disabled>
 <a href="/CkDocs/AV/AA_AVE05800Menu.doc">影像歸檔列印問題處理</a>
															</td>
														</tr>
														<tr >
															<td valign="top" class="tbYellow">影像歸檔編號</td>
															<td colspan="2" class="tbYellow2">
																<textarea name="textarea" id="textarea" cols="80" rows="10" class="textBox2" readOnly><c:out value="${jvm.DATA_STR}" /></textarea>
																<input name="DATA_LIST" id="DATA_LIST" type="hidden" value="<c:out value="${jvm.DATA_LIST}" />" >
															</td>
														</tr>
													</table>																		
												</form>
												
												<!-- Bolck_4_End-->
											</td>
										</tr>
									</tbody>
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
	<%@ include file="/html/CM/msgDisplayer.jsp" %><!--顯示接收到的錯誤訊息-->
</BODY>
</HTML>
