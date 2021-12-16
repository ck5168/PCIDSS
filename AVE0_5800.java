/*
 * 在 2007/02/05 建立       
 */
package com.cathay.av.e0.trx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.cathay.aa.bo.DTAAX020;
import com.cathay.av.e0.bo.AVE0_5800_bo;
import com.cathay.av.e0.module.AVE0_5800_mod;
import com.cathay.av.e0.module.AVE0_6000_mod;
import com.cathay.av.e0.module.AV_E0Z014;
import com.cathay.common.bo.ReturnMessage;
import com.cathay.common.exception.DataDuplicateException;
import com.cathay.common.exception.DataNotFoundException;
import com.cathay.common.exception.ModuleException;
import com.cathay.common.hr.DivData;
import com.cathay.common.hr.Unit;
import com.cathay.common.message.MessageHelper;
import com.cathay.common.service.ConfigManager;
import com.cathay.common.service.authenticate.UserObject;
import com.cathay.common.trx.UCBean;
import com.cathay.common.util.DATE;
import com.cathay.common.util.FieldOptionList;
import com.cathay.common.util.IConstantMap;
import com.cathay.common.util.STRING;
import com.cathay.common.util.page.SelectOptUtil;
import com.cathay.ed.a0.module.ED_A0Z001;
import com.cathay.ed.b0.bo.ED_B0Z001_bo;
import com.cathay.ed.b0.module.ED_B0Z001;
import com.cathay.ed.bo.DTEDG103;
import com.cathay.ed.g0.module.ED_G0Z100;
import com.cathay.ed.h0.module.ED_H0Z015;
import com.cathay.util.ReturnCode;
import com.cathay.util.Transaction;
import com.igsapp.common.trx.ServiceException;
import com.igsapp.common.trx.TxException;
import com.igsapp.wibc.dataobj.Context.RequestContext;
import com.igsapp.wibc.dataobj.Context.ResponseContext;

/**

    一、  程式功能概述：
            程式功能    影像歸檔作業
            程式名稱    AVE0_5800
            作業方式    ONLINE
            概要說明    影像歸檔資料新增/更新紀錄
            處理人員    行政中心/服務中心經辦
    
    二、  使用模組
            項次  中文說明    CLASS   METHOD
            1.      箱號、批號流水號控制檔 DTEDG101    qryBoxNO
        getBoxNO
    
    三、  使用檔案
            項次  中文說明    檔案名稱
            1   掃瞄批次處理紀錄    DTAVE102
            2   文件分類表   DTEDG100


    AVE0_5800.java
    @author jefftseng
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AVE0_5800 extends UCBean {

    private static final String CLASS_NAME = AVE0_5800.class.getName();

    private static Logger log = Logger.getLogger(CLASS_NAME);

    private boolean isDebug = log.isDebugEnabled();

    private ResponseContext resp; //此TxBean程式碼共用的 ResponseContext

    private ReturnMessage msg; //此TxBean程式碼共用的 ReturnMessage

    private UserObject user; //此TxBean程式碼共用的 UserObject

    private String UserID = ""; //作業人員ID

    private String UserName = ""; //作業人員姓名

    private String DivNo = ""; //作業人員單位代號

    private String DivName = ""; //作業人員單位名稱

    private AVE0_5800_mod ave0_5800_mod;//此 TxBean 程式碼共用的 Module

    //  private AV_E0Z003 ave0z003_mod;     //此 TxBean 程式碼共用的 Module

    private ED_G0Z100 ed_g0z100_mod; //此 TxBean 程式碼共用的 Module

    private ED_B0Z001 ed_b0z001; //此 TxBean 程式碼共用的 Module

    private AVE0_6000_mod ave0_6000_mod;//此 TxBean 程式碼共用的 Module

    private AV_E0Z014 av_e0z014_mod; //此 TxBean 程式碼共用的 Module

    //------------------------------------------------------------------------------
    /** 
    * 覆寫父類別的start()以強制每次Dispatcher呼叫method都執行程式自定的初始動作 
    */
    public ResponseContext start(RequestContext req) throws TxException, ServiceException {
        super.start(req); // 一定要 invoke super.start() 以執行權限的檢核 
        initApp(req); // 呼叫自定的初始動作
        return null;
    }

    //------------------------------------------------------------------------------
    /**
    * 起始共用的動作
    */
    private void initApp(RequestContext req) {

        // 建立 此TxBean 通用的物件
        resp = this.newResponseContext();
        msg = new ReturnMessage();
        user = this.getUserObject(req);

        // 先將 ReturnMessage 的 reference 加到 response coontext
        resp.addOutputData(IConstantMap.ErrMsg, msg);

        // 大部分動作都是相同，如果需要不同的在設定一次
        resp.setResponseCode("result");

        //設定 作業人員 共用變數值
        UserID = user.getEmpID();
        UserName = user.getEmpName();
        DivNo = user.getOpUnit();
        DivName = user.getDivShortName();

        //初始化共用模組
        ave0_5800_mod = new AVE0_5800_mod();
        //      ave0z003_mod = new AV_E0Z003(); 
        ed_g0z100_mod = new ED_G0Z100();
        ed_b0z001 = new ED_B0Z001();
        ave0_6000_mod = new AVE0_6000_mod();
        av_e0z014_mod = new AV_E0Z014();
    }

    //------------------------------------------------------------------------------
    /**
    * 初始化頁面
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrompt(RequestContext req) throws TxException {
        try {

            this.setOpt(); //產生畫面需要的SelectOption

            Map jvm = this.getPromptMap();

            this.putValue("jvm", jvm);

            resp.addOutputData("BCHNO_OPT", new SelectOptUtil());

            this.putValue("jvl", new ArrayList());

            this.setStatus("prompt");

        } catch (Exception e) {
            throw new TxException(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------
    /**
    * 查詢
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doQuery(RequestContext req) throws TxException {
        try {

            String DIV_NO = req.getParameter("DIV_NO");
            String OPR_ID = req.getParameter("OPR_ID");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");

            if (isDebug) {
                log.debug("-------debug --AVE0_5800.doQuery--DIV_NO:" + DIV_NO);
                log.debug("-------debug --AVE0_5800.doQuery--IMG_KIND_VALUE:" + IMG_KIND_VALUE);
            }

            this.setOpt(); //產生畫面需要的SelectOption
            if (isDebug) {
                log.debug("-------debug --產生畫面需要的SelectOption OK");
            }
            Map jvm = new ListOrderedMap();

            jvm.put("DIV_NO", DIV_NO);
            jvm.put("OPR_ID", OPR_ID);
            jvm.put("USER_ID", UserID);
            jvm.put("IMG_KIND_VALUE", IMG_KIND_VALUE);
            jvm.put("BCHNO_VALUE", "");
            jvm.put("BCH_NO", "");
            jvm.put("BCH_NO_SHOW", "");
            this.putValue("jvm", jvm);

            try {
                if (isDebug) {
                    log.debug("-------debug --this.setBCHNOOpt Start");
                }
                this.setBCHNOOpt(jvm.get("DIV_NO").toString(), jvm.get("IMG_KIND_VALUE").toString(), "S", OPR_ID);
                if (isDebug) {
                    log.debug("-------debug --this.setBCHNOOptEnd");
                }
                this.setStatus("query");
                msg.setMsgDesc("查詢歸檔批號成功");
            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("BCHNO_OPT", new SelectOptUtil());
                this.setStatus("prompt");
                msg.setMsgDesc("查無歸檔批號");
                msg.setReturnCode(ReturnCode.ERROR);
            }
            this.putValue("jvl", new ArrayList());

        } catch (Exception e) {
            if (isDebug) {
                log.debug("-------debug --Exception:" + e);
            }
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * 讀取檔案 
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doReadfile(RequestContext req) throws TxException {

        try {
            //讀取JSP頁面參數
            String DIV_NO = req.getParameter("DIV_NO").trim();
            String OPR_ID = req.getParameter("OPR_ID");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String BCHNO_VALUE = req.getParameter("BCHNO_OPT");

            //將JSP頁面需要參數 存放Map 放進Session
            Map jvm = new ListOrderedMap();
            jvm.put("DIV_NO", DIV_NO);
            jvm.put("OPR_ID", OPR_ID);
            jvm.put("USER_ID", UserID);
            jvm.put("IMG_KIND_VALUE", IMG_KIND_VALUE);
            jvm.put("BCHNO_VALUE", BCHNO_VALUE);
            this.putValue("jvm", jvm);

            this.setOpt(); //產生畫面需要的 文件代碼SelectOption

            //產生歸檔批號SelectOption
            try {
                this.setBCHNOOpt(jvm.get("DIV_NO").toString(), jvm.get("IMG_KIND_VALUE").toString(), "S", OPR_ID);
                this.setStatus("query");
            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("BCHNO_OPT", new SelectOptUtil());
                this.putValue("jvl", new ArrayList());
                this.setStatus("prompt");
                msg.setMsgDesc("查無歸檔批號");
                msg.setReturnCode(ReturnCode.ERROR);
                return resp;
            }

            String DB_OPR_ID = ave0_5800_mod.queryDTAVE102_OPR_ID(DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE);
            jvm.put("DB_OPR_ID", DB_OPR_ID);

            String BCH_NO = BCHNO_VALUE;

            jvm.put("BCH_NO", BCH_NO);
            jvm.put("BCH_NO_SHOW",
                IMG_KIND_VALUE + ":" + BCH_NO.substring(0, 8) + "-" + BCH_NO.substring(8, 10) + "-" + BCH_NO.substring(10, 15));

            List fileList = new ArrayList();

            //            if("AA02".equals(IMG_KIND_VALUE) || "AA03".equals(IMG_KIND_VALUE)
            //                    || "AA04".equals(IMG_KIND_VALUE) || "AA05".equals(IMG_KIND_VALUE) || "AT03".equals(IMG_KIND_VALUE)
            //                    || "AT01".equals(IMG_KIND_VALUE) || "AT02".equals(IMG_KIND_VALUE) || "AT04".equals(IMG_KIND_VALUE) || "AT05".equals(IMG_KIND_VALUE)){
            //spec 2.3.1 讀取：若文件分類為’AA02’或’AA03’或’AA04’或’AA05’則依所選歸檔批號及文件分類讀取依選取的DTAVE100 顯示其受編
            if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                // made by Dai You-Chun 0990712 配合新契約文件修正讀取DTAVE100
                fileList = ave0_5800_mod.queryDTAVE100(jvm.get("IMG_KIND_VALUE").toString(), BCH_NO);
                if (fileList.size() == 0) {
                    msg.setReturnCode(ReturnCode.ERROR);
                    msg.setMsgDesc("無影像歸檔編號");
                    this.putValue("jvl", new ArrayList());
                    this.setStatus("prompt");
                    return resp;
                }
            } else {
                //spec 2.3讀取上傳的 FILEREC.DAT
                File filerecDat = this.getFile(jvm.get("DIV_NO").toString(), jvm.get("IMG_KIND_VALUE").toString(), BCH_NO, "FILEREC.DAT");

                if (!filerecDat.exists()) {
                    msg.setReturnCode(ReturnCode.ERROR);
                    msg.setMsgDesc("影像檔案目錄無FILEREC.DAT");
                    this.putValue("jvl", new ArrayList());
                    this.setStatus("prompt");
                    return resp;
                }

                //讀取檔案內容 拆解每一筆 以Map 存放在List
                fileList = this.fileToList(filerecDat, BCH_NO);
            }

            //組合畫面影像歸檔編號  TextArea 所需文字
            String DATA_STR = "";
            String DATA_LIST = "";
            for (int i = 0; i < fileList.size(); i++) {
                Map fileMap = (Map) fileList.get(i);

                String IMG_KEY = "";
                //                if("AA02".equals(IMG_KIND_VALUE) || "AA03".equals(IMG_KIND_VALUE)
                //                        || "AA04".equals(IMG_KIND_VALUE) || "AA05".equals(IMG_KIND_VALUE)
                //                        || "AT01".equals(IMG_KIND_VALUE) || "AT02".equals(IMG_KIND_VALUE)
                //                        || "AT04".equals(IMG_KIND_VALUE) || "AT05".equals(IMG_KIND_VALUE)){
                if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                    IMG_KEY = ObjectUtils.toString(fileMap.get("IMG_KEY"));
                    if (StringUtils.isNotBlank(IMG_KEY)) {
                        IMG_KEY = "﹝" + IMG_KEY + "﹞";
                    }
                }

                String SER = "000" + Integer.toString(i + 1);
                DATA_STR = DATA_STR + SER.substring(SER.length() - 3, SER.length()) + ":  " + fileMap.get("BARCODE").toString() + IMG_KEY
                        + "\n";

                if (i == fileList.size() - 1) {
                    DATA_LIST += fileMap.get("BARCODE").toString();
                } else {
                    DATA_LIST += fileMap.get("BARCODE").toString() + ";";
                }
            }
            jvm.put("DATA_STR", DATA_STR);
            jvm.put("DATA_LIST", DATA_LIST);

            String YEAR = DATE.getDBDate(); //西元年兩碼
            YEAR = YEAR.substring(2, 4);

            //裝箱號碼 
            String BOX_SER_NO = "";

            if (StringUtils.isBlank(FieldOptionList.getName("ED", "VIRTUAL_BOX_NO", IMG_KIND_VALUE))) {

                BOX_SER_NO = ed_g0z100_mod.qryBoxNO(IMG_KIND_VALUE, DIV_NO, YEAR);

                this.setStatus("readfile");
            } else {
                // 取得虛擬箱號
                BOX_SER_NO = "B" + DIV_NO + FieldOptionList.getName("ED", "VIRTUAL_BOX_NO", IMG_KIND_VALUE);

                this.setStatus("readfilevirtualbox");
            }
            jvm.put("BOX_SER_NO", BOX_SER_NO);

            jvm.put("BOX_SER_NO_SHOW", IMG_KIND_VALUE + ":" + BOX_SER_NO.substring(0, 8) + "-" + BOX_SER_NO.substring(8, 10) + "-"
                    + BOX_SER_NO.substring(10, 15));

            this.putValue("jvl", fileList);

            msg.setMsgDesc("查詢完成");
            //          this.setStatus("readfile");

        } catch (Exception e) {
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * 列印箱號條碼
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrintBox(RequestContext req) throws TxException {

        try {
            //讀取JSP頁面參數
            String BOX_SER_NO_SHOW = req.getParameter("BOX_SER_NO_SHOW");

            //ABA0:B9004500-07-00005    
            String BOX_SER_NO = req.getParameter("BOX_SER_NO_SHOW");
            String[] arr1 = STRING.split(BOX_SER_NO, ":");
            String IMG_KIND = arr1[0];

            String[] arr2 = STRING.split(arr1[1], "-");

            Map jvm = new TreeMap();
            jvm.put("IMG_KIND", IMG_KIND);
            jvm.put("IMG_NAME", ave0_5800_mod.queryDTEDG100(IMG_KIND));
            jvm.put("BOX_SER_NO_0", arr2[0].substring(0, 1));
            jvm.put("BOX_SER_NO_1", arr2[0].substring(1, arr2[0].length()));
            jvm.put("BOX_SER_NO_2", arr2[1]);
            jvm.put("BOX_SER_NO_3", arr2[2]);
            jvm.put("BOX_SER_NO", BOX_SER_NO);
            jvm.put("BOX_SER_NO_SHOW", BOX_SER_NO_SHOW);
            this.putValue("jvm", jvm);

        } catch (Exception e) {

            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * 列印批號條碼
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrintBch(RequestContext req) throws TxException {

        try {
            //讀取JSP頁面參數
            String BCH_NO = req.getParameter("BCH_NO");

            //ABA0:H9004500-07-00003        
            String BCH_NO_SHOW = req.getParameter("BCH_NO_SHOW");
            String[] arr1 = STRING.split(BCH_NO_SHOW, ":");
            String IMG_KIND = arr1[0];

            String[] arr2 = STRING.split(arr1[1], "-");

            Map jvm = new TreeMap();
            jvm.put("IMG_KIND", IMG_KIND);
            jvm.put("IMG_NAME", ave0_5800_mod.queryDTEDG100(IMG_KIND));
            jvm.put("BCH_NO_0", arr2[0].substring(0, 1));
            jvm.put("BCH_NO_1", arr2[0].substring(1, arr2[0].length()));
            jvm.put("BCH_NO_2", arr2[1]);
            jvm.put("BCH_NO_3", arr2[2]);

            jvm.put("BCH_NO", BCH_NO);
            jvm.put("BCH_NO_SHOW", BCH_NO_SHOW);
            try {
                jvm.put("RCPT_LIST", ave0_5800_mod.queryDTAVE100(IMG_KIND, BCH_NO));
            } catch (Exception e) {
                List RCPT_LIST = new ArrayList();
                String DATA_LIST = req.getParameter("DATA_LIST");
                String[] DATA_LIST_ARR = STRING.split(DATA_LIST, ";");
                for (String BARCODE : DATA_LIST_ARR) {
                    AVE0_5800_bo bo = new AVE0_5800_bo();
                    bo.setBARCODE(BARCODE);
                    RCPT_LIST.add(bo);
                }
                jvm.put("RCPT_LIST", RCPT_LIST);
            }

            this.putValue("jvm", jvm);

        } catch (Exception e) {

            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------        
    /**
    * 封箱進號 (for Ajax 呼叫)
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doClose(RequestContext req) throws TxException {
        try {
            //讀取JSP頁面參數
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String DIV_NO = req.getParameter("DIV_NO");
            String BOX_SER_NO = req.getParameter("BOX_SER_NO");
            String YEAR = DATE.getDBDate(); //西元年兩碼
            YEAR = YEAR.substring(2, 4);

            try {
                //取公司別
                Map USER_Map = new ED_H0Z015().getSUB_CPY_ID(user);
                String SUB_CPY_ID = MapUtils.getString(USER_Map, "SUB_CPY_ID");
                //3.2   insert DBED.DTEDG103 (裝箱入庫記錄)，欄位內容如下：(960509修)
                ave0_5800_mod.insDTEDG103(IMG_KIND_VALUE, BOX_SER_NO, UserID, user.getEmpName(), DIV_NO, SUB_CPY_ID);

                //              ave0_5800_mod.insEDC0Z001(IMG_KIND_VALUE, BOX_SER_NO, DivNo, UserID, UserName); 

                ave0_5800_mod.insED(IMG_KIND_VALUE, BOX_SER_NO, user);

                BOX_SER_NO = ed_g0z100_mod.getBoxNO(IMG_KIND_VALUE, DIV_NO, YEAR);

                resp.addOutputData("BOX_SER_NO", BOX_SER_NO);

                resp.addOutputData("BOX_SER_NO_SHOW", IMG_KIND_VALUE + ":" + BOX_SER_NO.substring(0, 8) + "-" + BOX_SER_NO.substring(8, 10)
                        + "-" + BOX_SER_NO.substring(10, 15));

                resp.addOutputData("CHK_DATA", "");
                msg.setMsgDesc("進號完成");
            } catch (Exception e) {
                resp.addOutputData("CHK_DATA", e.getMessage());
                resp.addOutputData("BOX_SER_NO", BOX_SER_NO);
            }
        } catch (Exception e) {

            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * 以 文件代碼 與  掃描批號 讀取DTEDG102 取得 歸檔批號(for Ajax 呼叫)
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doGetBCHNO(RequestContext req) throws TxException {

        try {
            //讀取JSP頁面參數
            String IMG_KIND = req.getParameter("IMG_KIND");
            String BOX_NO = req.getParameter("BOX_NO");

            try {
                String BOX_DATE = "";
                String BCH_NO_STR = "";
                List outList = ave0_5800_mod.queryDTEDG102(IMG_KIND, BOX_NO);

                try {
                    DTEDG103 dtedg103Bo = ave0_5800_mod.queryDTEDG103(IMG_KIND, BOX_NO);
                    BOX_DATE = dtedg103Bo.getSEAL_DATE();
                    if (!BOX_DATE.equals("")) {
                        BOX_DATE = DATE.toROCFormat(DATE.timestampToDate(BOX_DATE).toString(), "/");
                    }
                } catch (DataNotFoundException dnfe) {
                    BOX_DATE = "";
                }

                for (int i = 0; i < outList.size(); i++) {
                    Map outMap = (Map) outList.get(i);
                    //if(i==0){
                    //  BOX_DATE  = STRING.objToStrNoNull(outMap.get("BOX_DATE"));  
                    //  if(!BOX_DATE.equals("")){
                    //      BOX_DATE = DATE.toROCFormat(
                    //              DATE.timestampToDate(BOX_DATE).toString(),"/"); 
                    //  }
                    //  
                    //}                 
                    BCH_NO_STR = BCH_NO_STR + STRING.objToStrNoNull(outMap.get("BCH_NO")) + STRING.objToStrNoNull(outMap.get("BCH_MEMO"))
                            + ",";

                }
                if (BCH_NO_STR.length() > 0) {
                    BCH_NO_STR = BCH_NO_STR.substring(0, BCH_NO_STR.length() - 1);
                }

                resp.addOutputData("BCH_NO_STR", BCH_NO_STR);
                resp.addOutputData("BOX_DATE", BOX_DATE);

                resp.addOutputData("EXE_MSG", "OK");

            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("EXE_MSG", "查無資料");
                resp.addOutputData("BCH_NO_STR", "");
                resp.addOutputData("BOX_DATE", "");
            } catch (Exception e) {
                resp.addOutputData("EXE_MSG", e.getMessage());
                resp.addOutputData("BCH_NO_STR", "");
            }
        } catch (Exception e) {

            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------
    /**
    * 檢查文件 在DTAAX020是否存在
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doCheckRCPT(RequestContext req) throws TxException {
    	StringBuilder sb = new StringBuilder();
        try {
            //讀取JSP頁面參數
            String DIV_NO = req.getParameter("DIV_NO");
            String BCH_NO = req.getParameter("BCH_NO");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String BARCODE = "";
            
            try {

                //                if(!"AA02".equals(IMG_KIND_VALUE) && !"AA03".equals(IMG_KIND_VALUE)
                //                        && !"AA04".equals(IMG_KIND_VALUE) && !"AA05".equals(IMG_KIND_VALUE) && !"AT03".equals(IMG_KIND_VALUE)
                //                        && !"AT01".equals(IMG_KIND_VALUE) && !"AT02".equals(IMG_KIND_VALUE) && !"AT04".equals(IMG_KIND_VALUE) && !"AT05".equals(IMG_KIND_VALUE)){
                if (StringUtils.isBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                    //讀取 FILEREC.DAT
                    File filerecDat = this.getFile(DIV_NO, IMG_KIND_VALUE, BCH_NO, "FILEREC.DAT");

                    //讀取FILEREC.DAT檔案內容 拆解每一筆 以Map 存放在List
                    List fileList = this.fileToList(filerecDat, BCH_NO);

                    for (int i = 0; i < fileList.size(); i++) {

                        Map fileMap = (Map) fileList.get(i);
                        String IMG_KIND = fileMap.get("IMG_KIND").toString();
                        BARCODE = fileMap.get("BARCODE").toString();

                        //                      if(IMG_KIND_VALUE.equals("AB02")){  
                        if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND_VALUE))) {
                            if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "MULTI_DOC_SAME_BARCODE", IMG_KIND_VALUE))) {
                            	try{
                            		ave0_5800_mod.findDTAAX020(IMG_KIND, BARCODE);
                            	}catch(DataNotFoundException dnfe){
                            		sb.append(BARCODE).append("、");
                            	}
                            } else {
                            	try{
                            		ave0_5800_mod.queryDTAAX020_2(IMG_KIND, BARCODE);
                            	}catch(DataNotFoundException dnfe){
                            		sb.append(BARCODE).append("、");
                            	}
                            }
                        }
                    }
                }
                if(sb.length() > 0){
                	throw new DataNotFoundException("查無資料");
                }
                resp.addOutputData("EXE_MSG", "OK");
                resp.addOutputData("IS_FIND", "Y");
                resp.addOutputData("BARCODE", "");
            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("EXE_MSG", "OK");
                resp.addOutputData("IS_FIND", "N");
                resp.addOutputData("BARCODE", sb.substring(0, sb.length()-1));
            } catch (Exception e) {
                resp.addOutputData("EXE_MSG", e.getMessage());
                resp.addOutputData("IS_FIND", "");
                resp.addOutputData("BARCODE", "");
            }
        } catch (Exception e) {
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * 裝箱確認
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doConfirm(RequestContext req) throws TxException {
        try {

            //讀取JSP頁面參數 
            String DIV_NO = req.getParameter("DIV_NO");
            String BCH_NO = req.getParameter("BCH_NO");
            String BOX_SER_NO = req.getParameter("BOX_SER_NO");
            String BCHNO_VALUE = req.getParameter("BCHNO_OPT");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            // made by Dai You-Chun 1000305 裝箱確認新增批號內容描述
            String BCH_MEMO = req.getParameter("BCH_MEMO");
            BigDecimal totSize = new BigDecimal("0");
            List fileList = new ArrayList();
            Transaction.begin();

            // 取得根路徑
            String AVE0_IMG_HOME = ConfigManager.getProperty("AVE0_IMG_HOME");

            // 取得來原路徑
            File sourcePath = this.getSourcePath(AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCH_NO);

            // 取得 FILEREC.DAT 檔案路徑
            File filerecDat = new File(sourcePath, "FILEREC.DAT");

            // 宣告 REC_UPDATE.TXT 檔案路徑
            File filerecStr = null;
            //            if("AA02".equals(IMG_KIND_VALUE) || "AA03".equals(IMG_KIND_VALUE)
            //                    || "AA04".equals(IMG_KIND_VALUE) || "AA05".equals(IMG_KIND_VALUE) || "AT03".equals(IMG_KIND_VALUE)
            //                    || "AT01".equals(IMG_KIND_VALUE) || "AT02".equals(IMG_KIND_VALUE) || "AT04".equals(IMG_KIND_VALUE) || "AT05".equals(IMG_KIND_VALUE)){
            if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                fileList = ave0_5800_mod.queryDTAVE100(IMG_KIND_VALUE, BCH_NO);
            } else {
                //讀取FILEREC.DAT檔案內容 拆解每一筆 以Map 存放在List
                fileList = this.fileToList(filerecDat, BCH_NO);

                String line0 = "";
                BufferedReader rd0;
                int COUNT_index0 = 0;
                for (rd0 = new BufferedReader(new FileReader(filerecDat)); StringUtils.isNotBlank((line0 = rd0.readLine()));) {
                    String[] lineArr = STRING.split(line0, ",");

                    if (lineArr.length >= 6) {
                        String BAR_CODE = lineArr[5];
                        if ("AC07".equals(IMG_KIND_VALUE) && BAR_CODE.length() == 10) {
                            BAR_CODE = "0" + BAR_CODE;
                        }

                        boolean hasFile = this.chkImgFile(DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, BAR_CODE);
                        if (!hasFile) {
                            boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE,
                                "3");

                            if (moveSuccess) {
                                rd0.close();
                                Transaction.commit();
                                FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                                MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                                    "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                                this.putValue("jvl", new ArrayList());
                                this.setStatus("query");
                                return resp;
                            }
                        }
                    }
                    COUNT_index0++;
                }
                rd0.close();

                TIF_FileFilter x0 = new TIF_FileFilter("TIF");
                String[] files0 = sourcePath.list(x0); //取得符合的檔名
                int COUNT_TIF0 = files0 == null ? 0 : files0.length;
                if (COUNT_TIF0 != COUNT_index0) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // 查詢影像批號紀錄
                Map reqMap = new HashMap();
                reqMap.put("DIV_NO", DIV_NO);
                reqMap.put("IMG_KIND", IMG_KIND_VALUE);
                reqMap.put("BCH_NO", BCHNO_VALUE);
                int FILE_CNT = Integer.parseInt(av_e0z014_mod.queryDTAVE102(reqMap).get("FILE_CNT").toString());
                if (FILE_CNT != COUNT_index0) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // 取得REC_UPDATE.TXT檔案路徑
                //                String filerecStr = ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND_VALUE
                //                        + File.separator + BCH_NO + File.separator + "REC_UPDATE.txt";
                filerecStr = new File(sourcePath, "REC_UPDATE.txt");

                //寫入REC_UPDATE.txt 的 Writer
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filerecStr)));

                //暫存寫入REC_UPDATE的Buffer
                StringBuffer sb = new StringBuffer();

                String prevSET_NO = ""; //紀錄組號旗標

                //畫面多筆組號相同的話 要寫成一筆
                for (int i = 0; i < fileList.size(); i++) {

                    Map fileMap = (Map) fileList.get(i);
                    String IMG_KIND = fileMap.get("IMG_KIND").toString();
                    String BARCODE = fileMap.get("BARCODE").toString();
                    String ORG_BARCODE = fileMap.get("BARCODE").toString();

                    totSize = totSize.add(this.getFileSize(DIV_NO, IMG_KIND, BCH_NO, ORG_BARCODE));

                    if (i == 0) {
                        //第一筆

                        //區分是否為同一行的條碼旗標
                        prevSET_NO = fileMap.get("SET_NO").toString();

                        //TODO
                        //                      if(!IMG_KIND_VALUE.equals("AB02")){
                        if (StringUtils.isBlank(FieldOptionList.getName("ED", "MULTI_DOC_SAME_BARCODE", IMG_KIND_VALUE))) {
                            String IMG_KEY = "";
                            /*
                             * 修改日期:2018-05-22
                             * 問題單號:20180522-0058
                             * 要作點交收的才需要更新DTAAX020收據明細檔
                             * by 葉緗妤
                             */
                            if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))){
                                try {
                                    DTAAX020 dtaax020Bo = ave0_5800_mod.updateDTAAX020(UserID, DivNo, DATE.getDBTimeStamp(), IMG_KIND, BARCODE);
                                    IMG_KEY = dtaax020Bo.getIMG_KEY();
                                    // made by Dai You-Chun 1000526
                                    // 或該單據SIGN_TIME無值，
                                    if (STRING.isNull(dtaax020Bo.getSIGN_TIME())) {
                                        ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                                .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                    }
                                } catch (DataNotFoundException dnfe) {
                                    ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                            .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                }
                            }

                            //spec 4.2新增 DTAVE100   
                            //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, "");
                                log.error("PCIDSS: insDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF 檔案加密成副檔名 .TIF.enc"); 

                            }

                        } else {

                            //文件代碼為AB02

                            String IMG_KEY = "";
                            String PRE_KEY = "";
                            try {

                                //若為條碼需自動加流水號的書類(兩碼)
                                if (StringUtils.isNotBlank(FieldOptionList.getName("AV", "AUTO_SEQ_IMG", IMG_KIND))) {
                                    IMG_KEY = BARCODE;
                                    PRE_KEY = BARCODE;
                                    List jvl = new ArrayList();
                                    int serNo = 0;
                                    try {
                                        jvl = ave0_6000_mod.queryDTAVE100(IMG_KIND, BARCODE);
                                        Map tmpMap = (Map) jvl.get(jvl.size() - 1);
                                        String tmpBarCode = tmpMap.get("RCPT_NO").toString();
                                        String[] barCodeStrs = STRING.split(tmpBarCode, "-");
                                        String oldSeq = barCodeStrs[barCodeStrs.length - 1];
                                        if (StringUtils.isBlank(oldSeq)) {
                                            oldSeq = "0";
                                        }
                                        serNo = Integer.parseInt(oldSeq);
                                    } catch (Exception e) {
                                        // do nothing
                                    }
                                    serNo = serNo + 1;
                                    BARCODE = IMG_KEY + "-" + STRING.fillCharFromLeftExt(Integer.toString(serNo), 2, '0');
                                } else if (StringUtils.isNotBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))) {
                                    throw new DataNotFoundException();

                                } else {

                                    BARCODE = ave0_5800_mod.queryDTAAX020(IMG_KIND, BARCODE);

                                    DTAAX020 dtaax020Bo = ave0_5800_mod.updateDTAAX020(UserID, DivNo, DATE.getDBTimeStamp(), IMG_KIND,
                                        BARCODE);

                                    IMG_KEY = dtaax020Bo.getIMG_KEY();
                                }

                            } catch (DataNotFoundException dnfe) {
                                if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))) {
                                    ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                            .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                } else {
                                    IMG_KEY = BARCODE;
                                    List jvl = new ArrayList();
                                    int serNo = 0;
                                    try {
                                        jvl = ave0_6000_mod.queryDTAVE100(IMG_KIND, BARCODE);
                                        Map tmpMap = (Map) jvl.get(jvl.size() - 1);
                                        String tmpBarCode = tmpMap.get("RCPT_NO").toString();
                                        //                                        log.fatal("BarCode => " + tmpBarCode);  
                                        serNo = Integer.parseInt(tmpBarCode.substring(tmpBarCode.length() - 4, tmpBarCode.length()));
                                        //                                        log.fatal("tmpBarCode.length() => " + tmpBarCode.length());  
                                    } catch (Exception e) {
                                        // do nothing
                                    }
                                    serNo = serNo + 1;
                                    //                                    log.fatal("serNo => " + serNo); 
                                    BARCODE = IMG_KEY + STRING.fillCharFromLeftExt(Integer.toString(serNo), 4, '0');
                                    ave0_5800_mod.insertDTAAX020(IMG_KIND, BARCODE, IMG_KEY, user);
                                }

                            }

                            //spec 4.2新增 DTAVE100   
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, PRE_KEY);
                                log.error("PCIDSS: insDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");                                

                            }

                        }

                        sb = sb.append(Integer.toString(i + 1) + "," + IMG_KIND + "," + DivNo + "," + UserID + ","
                                + fileMap.get("SCAN_DATE_TIME").toString() + "," + fileMap.get("IMG_PAGES").toString() + "," + BARCODE);

                        //修改TIF檔名
                        if (!ORG_BARCODE.equals(BARCODE)) {
                            this.renameTIF(DIV_NO, IMG_KIND, BCH_NO, ORG_BARCODE, BARCODE);
                        }

                        if (i == (fileList.size() - 1)) {
                            //最後一筆
                            pw.println(sb.toString());
                            sb.delete(0, (sb.length())); //清空sb資料        
                        }
                        continue;
                    }

                    if (fileMap.get("SET_NO").toString().equals(prevSET_NO)) {
                        //同一組號要寫再同一行(append條碼)
                        sb = sb.append("," + BARCODE);
                    } else {

                        //                      if(!IMG_KIND_VALUE.equals("AB02")){
                        if (StringUtils.isBlank(FieldOptionList.getName("ED", "MULTI_DOC_SAME_BARCODE", IMG_KIND_VALUE))) {
                            String IMG_KEY = "";
                            /*
                             * 修改日期:2018-05-22
                             * 問題單號:20180522-0058
                             * 要作點交收的才需要更新DTAAX020收據明細檔
                             * by 葉緗妤
                             */
                            if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))){
                                try {
                                    DTAAX020 dtaax020Bo = ave0_5800_mod.updateDTAAX020(UserID, DivNo, DATE.getDBTimeStamp(), IMG_KIND, BARCODE);
                                    IMG_KEY = dtaax020Bo.getIMG_KEY();
                                } catch (DataNotFoundException dnfe) {
                                    ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                            .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                }
                            }

                            //spec 4.2新增 DTAVE100    
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, "");
                                log.error("PCIDSS: insDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            }

                        } else {
                            //文件代碼為AB02

                            String IMG_KEY = "";
                            String PRE_KEY = "";

                            try {

                                //若為條碼需自動加流水號的書類(兩碼)
                                if (StringUtils.isNotBlank(FieldOptionList.getName("AV", "AUTO_SEQ_IMG", IMG_KIND))) {
                                    IMG_KEY = BARCODE;
                                    PRE_KEY = BARCODE;
                                    List jvl = new ArrayList();
                                    int serNo = 0;
                                    try {
                                        jvl = ave0_6000_mod.queryDTAVE100(IMG_KIND, BARCODE);
                                        Map tmpMap = (Map) jvl.get(jvl.size() - 1);
                                        String tmpBarCode = tmpMap.get("RCPT_NO").toString();
                                        String[] barCodeStrs = STRING.split(tmpBarCode, "-");
                                        String oldSeq = barCodeStrs[barCodeStrs.length - 1];
                                        if (StringUtils.isBlank(oldSeq)) {
                                            oldSeq = "0";
                                        }
                                        serNo = Integer.parseInt(oldSeq);
                                    } catch (Exception e) {
                                        // do nothing
                                    }
                                    serNo = serNo + 1;
                                    BARCODE = IMG_KEY + "-" + STRING.fillCharFromLeftExt(Integer.toString(serNo), 2, '0');
                                } else if (StringUtils.isNotBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))) {
                                    throw new DataNotFoundException();

                                } else {
                                    BARCODE = ave0_5800_mod.queryDTAAX020(IMG_KIND, BARCODE);

                                    DTAAX020 dtaax020Bo = ave0_5800_mod.updateDTAAX020(UserID, DivNo, DATE.getDBTimeStamp(), IMG_KIND,
                                        BARCODE);

                                    IMG_KEY = dtaax020Bo.getIMG_KEY();
                                }

                            } catch (DataNotFoundException dnfe) {
                                if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))) {
                                    ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                            .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                } else {
                                    IMG_KEY = BARCODE;
                                    List jvl = new ArrayList();
                                    int serNo = 0;
                                    try {
                                        jvl = ave0_6000_mod.queryDTAVE100(IMG_KIND, BARCODE);
                                        Map tmpMap = (Map) jvl.get(jvl.size() - 1);
                                        String tmpBarCode = tmpMap.get("RCPT_NO").toString();
                                        //                                        log.fatal("BarCode => " + tmpBarCode); 
                                        serNo = Integer.parseInt(tmpBarCode.substring(tmpBarCode.length() - 4, tmpBarCode.length()));

                                    } catch (Exception e) {
                                        //do nothing
                                    }
                                    serNo = serNo + 1;
                                    //                                    log.fatal("serNo => " + serNo); 
                                    BARCODE = IMG_KEY + STRING.fillCharFromLeftExt(Integer.toString(serNo), 4, '0');
                                    ave0_5800_mod.insertDTAAX020(IMG_KIND, BARCODE, IMG_KEY, user);
                                }
                            }

                            //spec 4.2新增 DTAVE100   
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, PRE_KEY);
                                log.error("PCIDSS: insDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF 檔案加密成副檔名 .TIF.enc");

                            }
                            //修改TIF檔名
                            if (!ORG_BARCODE.equals(BARCODE)) {
                                this.renameTIF(DIV_NO, IMG_KIND, BCH_NO, ORG_BARCODE, BARCODE);
                            }
                        }

                        pw.println(sb.toString());
                        sb.delete(0, (sb.length()));
                        sb = sb.append(Integer.toString(i + 1) + "," + IMG_KIND + "," + DivNo + "," + UserID + ","
                                + fileMap.get("SCAN_DATE_TIME").toString() + "," + fileMap.get("IMG_PAGES").toString() + "," + BARCODE);

                    }

                    if (i == (fileList.size() - 1)) {
                        //最後一筆
                        pw.println(sb.toString());
                        sb.delete(0, (sb.length())); //清空sb資料        
                    }

                    prevSET_NO = fileMap.get("SET_NO").toString();
                }

                //關檔
                pw.flush();
                pw.close();

                // 取得REC_UPDATE.TXT檔案路徑
                //File rec_updateFile = new File(sourcePath, "REC_UPDATE.TXT");

                // 取得來源路徑下所有 TIF 檔筆數
                TIF_FileFilter x = new TIF_FileFilter("TIF");
                String[] files = sourcePath.list(x);
                int COUNT_TIF1 = files == null ? 0 : files.length;

                String line = "";
                BufferedReader rd;
                int COUNT_index1 = 0;
                for (rd = new BufferedReader(new FileReader(filerecStr)); StringUtils.isNotBlank((line = rd.readLine()));) {

                    String[] lineArr = STRING.split(line, ",");

                    if (lineArr.length >= 7) {
                        // 條碼編號特殊處理
                        String BARCODE = lineArr[6];
                        if ("AC07".equals(IMG_KIND_VALUE) && BARCODE.length() == 10) {
                            BARCODE = "0" + BARCODE;
                        }

                        // 判斷檔案是否存在
                        boolean hasFile = this.chkImgFile(DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, BARCODE);
                        if (!hasFile) {
                            boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE,
                                "3");

                            if (moveSuccess) {
                                rd.close();
                                Transaction.commit();
                                FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                                MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                                    "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                                this.putValue("jvl", new ArrayList());
                                this.setStatus("query");
                                return resp;
                            }
                        }
                    }

                    COUNT_index1++;
                }
                rd.close();

                if (COUNT_TIF1 != COUNT_index1) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        rd.close();
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // 查詢影像批號紀錄
                Map reqMap1 = new HashMap();
                reqMap1.put("DIV_NO", DIV_NO);
                reqMap1.put("IMG_KIND", IMG_KIND_VALUE);
                reqMap1.put("BCH_NO", BCHNO_VALUE);
                int FILE_CNT1 = Integer.parseInt(new AV_E0Z014().queryDTAVE102(reqMap1).get("FILE_CNT").toString());
                if (FILE_CNT1 != COUNT_index1) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        rd.close();
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // 清空來源資料
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "該筆批號可能在上傳時發生錯誤，請先至批號刪除作業畫面進行刪除，再重新至影像上傳作業畫面重新上傳檔案(index.dat)。");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }
            }
            //spec 4.4 登記批號歸檔紀錄 DTEDG102
            ave0_5800_mod.updDTEDG102(IMG_KIND_VALUE, BCH_NO, Integer.toString(fileList.size()), DIV_NO, UserID, BOX_SER_NO, UserID,
                totSize.toString(), BCH_MEMO);

            //spec 4.5 更新登錄掃描批處理紀錄
            ave0_5800_mod.updateDTAVE102(IMG_KIND_VALUE, BCHNO_VALUE, UserID);

            //停在最後的畫面，但Disabled 裝箱確認按鈕
            MessageHelper.setReturnMessage(msg, ReturnCode.OK, "裝箱確認完成");
            resp.addOutputData("EXE_MSG", "");
            Transaction.commit();

        } catch (Exception e) {
            log.fatal("AV confirm error:"+e,e);
            Transaction.rollback();
            String MSG = e.getMessage();
            MessageHelper.setReturnMessage(msg, ReturnCode.ERROR, MSG);
            resp.addOutputData("EXE_MSG", MSG);
            msg.setMsgDesc(MSG);
        }
        return resp;
    }

    //------------------------------------------------------------------------------
    // 以下為private Method    
    //------------------------------------------------------------------------------
    /**
    * 取得初始Prompt使用的JSP畫面用Map
    * @param req
    * @return
    * @throws TxException
    */
    private Map getPromptMap() throws TxException, ModuleException {

        Map jvm = new ListOrderedMap();

        jvm.put("DIV_NO", DivNo);
        jvm.put("OPR_ID", UserID);
        jvm.put("USER_ID", UserID);
        jvm.put("IMG_KIND_VALUE", "ABA0");
        jvm.put("BCHNO_VALUE", "");
        jvm.put("DATA_STR", "");
        jvm.put("BCH_NO", "");
        jvm.put("BCH_NO_SHOW", "");
        jvm.put("BOX_SER_NO", "");
        return jvm;
    }

    //------------------------------------------------------------------------------    
    /*
     * 產生畫面所需的下拉視窗值 文件代碼, 開單年月
     */
    private void setOpt() throws Exception {

        //文件代碼 SelectOpt
        SelectOptUtil IMG_KIND_OPT = new SelectOptUtil();
        //      List imgList = ave0_5800_mod.queryDTEDG100();
        //      
        //      for(int i=0; i< imgList.size(); i++){
        //          Map imgMap = (Map)imgList.get(i); 
        //
        //          IMG_KIND_OPT.addOption(imgMap.get("IMG_KIND").toString(),
        //                                  imgMap.get("SHOW_NAME").toString());
        //      }
        //      resp.addOutputData("IMG_KIND_OPT", IMG_KIND_OPT);       
        try {
            DivData dd = new DivData();
            Unit unit;
            unit = dd.getUnit(user.getOpUnit());
            if (unit.isSvcenter()) {

                List<ED_B0Z001_bo> tmplist = ed_b0z001.getImgKindList(user.getRoles());

                for (int i = 0; i < tmplist.size(); i++) {

                    ED_B0Z001_bo bo = (ED_B0Z001_bo) tmplist.get(i);

                    if (StringUtils.isNotBlank(FieldOptionList.getName("AV", "IMGKIND_SCANBYSVC", bo.getIMG_KIND()))) {
                        IMG_KIND_OPT.addOption(bo.getIMG_KIND(), bo.getIMG_KIND() + " " + bo.getIMG_NAME());

                    }
                }
            } else {
                List imgList = ave0_5800_mod.queryDTEDG100();
                for (int i = 0; i < imgList.size(); i++) {
                    Map imgMap = (Map) imgList.get(i);
                    if (StringUtils.isBlank(FieldOptionList.getName("AV", "IMGKIND_SCAN_WO_NAS", imgMap.get("IMG_KIND").toString()))) {
                        IMG_KIND_OPT.addOption(imgMap.get("IMG_KIND").toString(), imgMap.get("SHOW_NAME").toString());
                    }
                }
            }
            resp.addOutputData("IMG_KIND_OPT", IMG_KIND_OPT);
        } catch (Exception e) {
            log.fatal(e);
        }
    }

    //------------------------------------------------------------------------------     
    /*
     * 產生畫面所需的下拉視窗值 掃描批號
     */
    private void setBCHNOOpt(String DIV_NO, String IMG_KIND_VALUE, String BCH_STS, String OPR_ID) throws Exception {

        SelectOptUtil BCHNO_OPT = new SelectOptUtil();

        //歸檔批號 SelectOpt
        List bchnoList;

        bchnoList = ave0_5800_mod.queryDTAVE102(DIV_NO, IMG_KIND_VALUE, BCH_STS, OPR_ID);
        boolean IS_CHECK_IMG_KIND = StringUtils.isNotBlank(FieldOptionList.getName("AV", "IS_CHECK_IMG_KIND", IMG_KIND_VALUE));
        /*
                   修改日期:2018-11-05
                  立案單號180809000841 會計股出納股裁撤之應付票系統調整作業 
       by 葉緗妤
                  應出納股裁撤,若為DJ01 若紅綠燈文件倉儲有結批號的控制
        */
        if(IS_CHECK_IMG_KIND){//DJ01
            Map aMap = new ED_A0Z001().getDTEDA001(Transaction.getDataSet(),DIV_NO,IMG_KIND_VALUE);
            String strCOUNT = "";
            if(aMap != null){
                strCOUNT = aMap.get("COUNT").toString();
            }
            for (int i = 0; i < bchnoList.size(); i++) {
                Map imgMap = (Map) bchnoList.get(i);
                String strBCH_NO = imgMap.get("BCH_NO").toString();
                
                String theBCH_CNT = FieldOptionList.getName("ED", "BATCH_COUNT", IMG_KIND_VALUE);
                if (isDebug) {
                    log.debug("------debug imgMap DJ:" + imgMap.toString());
                }
                if(aMap != null){
                    String com_BCH_NO = MapUtils.getString(aMap, "BCH_NO");
                    if(!com_BCH_NO.equals(strBCH_NO)){
                        BCHNO_OPT.addOption(strBCH_NO, strBCH_NO);
                    }else if(com_BCH_NO.equals(strBCH_NO)){
                        if(theBCH_CNT.equals(strCOUNT)){
                            BCHNO_OPT.addOption(strBCH_NO, strBCH_NO);
                        }
                    }
                }
            }
            if(BCHNO_OPT.size() == 0){
                throw new DataNotFoundException("查無歸檔批號");
            }
        }else{//非DJ01
            for (int i = 0; i < bchnoList.size(); i++) {
                Map imgMap = (Map) bchnoList.get(i);
                if (isDebug) {
                    log.debug("------debug imgMap:" + imgMap.toString());
                }
                BCHNO_OPT.addOption(imgMap.get("BCH_NO").toString(), imgMap.get("BCH_NO").toString());
            }
        }
        
        resp.addOutputData("BCHNO_OPT", BCHNO_OPT);

    }

    //------------------------------------------------------------------------------     
    /*
    * 取得 影像檔路徑 下的 檔案
    * 設定檔 在 AV_SRC\\usr\\cxlcs\\config\\customconfig\\fileStore.prop
    */
    private File getFile(String DIV_NO, String IMG_KIND, String SCANNO, String FILE_NAME) throws ModuleException {

        File path = new File(ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND
                + File.separator + SCANNO + File.separator + FILE_NAME);

        return path;

    }

    //------------------------------------------------------------------------------    
    /*
     * 檔案內容 拆解 為 List 存放
     */
    private List fileToList(File filerecDat, String BCH_NO) throws FileNotFoundException, IOException {

        //讀取檔案內容 拆解每一筆 以Map 存放在List
        List fileList = new ArrayList();
        String line = "";
        BufferedReader rd;
        int row_count = 1;
        for (rd = new BufferedReader(new FileReader(filerecDat)); StringUtils.isNotBlank((line = rd.readLine()));) {

            String[] lineArr = STRING.split(line, ",");

            //如果同一行條碼有多筆 需要拆解多筆
            if (lineArr.length >= 6) {
                //一筆條碼
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //文件代碼
                outMap.put("SCAN_DIV_NO", lineArr[1]); //掃描單位代號
                outMap.put("SCAN_OPR_ID", lineArr[2]); //掃描作業人員
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //掃描日期與時間
                outMap.put("IMG_PAGES", lineArr[4]); //影像頁數                  
                outMap.put("BARCODE", lineArr[5]); //條碼編號
                outMap.put("SET_NO", Integer.toString(row_count)); //判別用組號
                outMap.put("SEQ_NO", "1"); //判別用序號
                outMap.put("COUNT_NO", "1"); //判別用 有幾筆條碼
                outMap.put("BCH_NO", BCH_NO);

                fileList.add(outMap);
            }
            if (lineArr.length >= 7) {
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //文件代碼
                outMap.put("SCAN_DIV_NO", lineArr[1]); //掃描單位代號
                outMap.put("SCAN_OPR_ID", lineArr[2]); //掃描作業人員
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //掃描日期與時間
                outMap.put("IMG_PAGES", lineArr[4]); //影像頁數                  
                outMap.put("BARCODE", lineArr[6]); //條碼編號
                outMap.put("SET_NO", Integer.toString(row_count)); //判別用組號
                outMap.put("SEQ_NO", "2"); //判別用序號
                outMap.put("COUNT_NO", "2"); //判別用 有幾筆條碼
                outMap.put("BCH_NO", BCH_NO);
                fileList.add(outMap);

            }
            if (lineArr.length >= 8) {
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //文件代碼
                outMap.put("SCAN_DIV_NO", lineArr[1]); //掃描單位代號
                outMap.put("SCAN_OPR_ID", lineArr[2]); //掃描作業人員
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //掃描日期與時間
                outMap.put("IMG_PAGES", lineArr[4]); //影像頁數                  
                outMap.put("BARCODE", lineArr[7]); //條碼編號
                outMap.put("SET_NO", Integer.toString(row_count)); //判別用組號
                outMap.put("SEQ_NO", "3"); //判別用序號
                outMap.put("COUNT_NO", "3"); //判別用 有幾筆條碼
                outMap.put("BCH_NO", BCH_NO);

                fileList.add(outMap);
            }
            row_count++;
        }
        rd.close(); //關閉讀檔
        return fileList;
    }

    //------------------------------------------------------------------------------    
    /*
     * 控制畫面按鈕
     */
    private void setStatus(String stsCode) {
        // name 必需和頁面上的 element name 一樣

        if ("prompt".equals(stsCode)) {
            resp.addOutputData("status", "query_btn");
        } else if ("query".equals(stsCode)) {
            resp.addOutputData("status", "query_btn,readfile_btn");
        } else if ("readfile".equals(stsCode)) {
            resp.addOutputData("status", "query_btn,readfile_btn,print_bchno_btn" + ",print_boxserno_btn,close_btn,confirm_btn");
        } else if ("readfilevirtualbox".equals(stsCode)) {
            resp.addOutputData("status", "query_btn,readfile_btn,print_bchno_btn" + ",print_boxserno_btn,confirm_btn");
        }

    }

    //------------------------------------------------------------------------------    
    /*
     * 修改TIF檔名
     */
    private void renameTIF(String DIV_NO, String IMG_KIND, String BCH_NO, String ORG_BARCODE, String BARCODE) {

        String orgfile = ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND + File.separator
                + BCH_NO + File.separator + ORG_BARCODE + ".TIF";

        String newfile = ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND + File.separator
                + BCH_NO + File.separator + BARCODE + ".TIF";

        File orgFile = new File(orgfile);
        orgFile.renameTo(new File(newfile));
    }

    //  ------------------------------------------------------------------------------

    private BigDecimal getFileSize(String DIV_NO, String IMG_KIND, String BCH_NO, String ORG_BARCODE) {

        String orgfile = ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND + File.separator
                + BCH_NO + File.separator + ORG_BARCODE + ".TIF";

        File orgFile = new File(orgfile);
        BigDecimal size = new BigDecimal(orgFile.length());
        size = size.divide(new BigDecimal("1024"), BigDecimal.ROUND_UP);
        return size;
    }

    //------------------------------------------------------------------------------    
    /**
        * 定義過濾檔名方法
        *   @param  name    主檔名包含哪些字串
        *   @param  ext     副檔名
        */
    private static class TIF_FileFilter implements FilenameFilter {

        private String ext;

        public TIF_FileFilter(String ext) {//建立者方法

            this.ext = ext;
        }

        public boolean accept(File dir, String filename) {//accept方法
            boolean blnString = true;
            if (ext != null) {
                blnString &= (filename.endsWith("." + ext) || filename.endsWith("." + ext.toUpperCase()));
            }
            return blnString;
        }
    }

    //------------------------------------------------------------------------------
    /**
     *  檢核 影像檔是否存在
     * @param DIV_NO
     * @param IMG_KIND
     * @param SCANNO
     * @param IMG_FNAME
     * @return
     * @throws ModuleException
     */
    private boolean chkImgFile(String DIV_NO, String IMG_KIND, String SCANNO, String IMG_FNAME) throws ModuleException {

        File path = new File(ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND
                + File.separator + SCANNO + File.separator + IMG_FNAME + ".TIF");
        //個資同意書因為條碼問題，找不到文件將條碼第一碼去掉再找文件，有文件則更名補上第一碼0
        if (!path.exists() && "AC07".equals(IMG_KIND)) {
            path = new File(ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND
                    + File.separator + SCANNO + File.separator + IMG_FNAME.substring(1) + ".TIF");
            if (path.exists()) {
                path.renameTo(new File(ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND
                        + File.separator + SCANNO + File.separator + IMG_FNAME + ".TIF"));
                return true;
            }
        }
        return path.exists();
    }

    /**
     * 取得 影像存放路徑
     * @param AVE0_IMG_HOME
     * @param DIV_NO
     * @param IMG_KIND
     * @param BCH_NO
     * @return
     */
    private File getSourcePath(String AVE0_IMG_HOME, String DIV_NO, String IMG_KIND, String BCH_NO) {
        StringBuilder sb = new StringBuilder();
        sb.append(AVE0_IMG_HOME).append(File.separator).append(DIV_NO).append(File.separator).append(IMG_KIND).append(File.separator)
                .append(BCH_NO);
        return new File(sb.toString());
    }
}
