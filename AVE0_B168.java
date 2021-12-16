package com.ck.av.e0.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ck.av.a0.module.AVA0_0350_mod;
import com.ck.av.bo.DTAVE101;
import com.ck.av.e0.module.AV_E0Z014;
import com.ck.av.module.AV_BatchBean;
import com.ck.common.exception.DataNotFoundException;
import com.ck.common.exception.ModuleException;
import com.ck.common.service.ConfigManager;
import com.ck.common.util.DATE;
import com.ck.common.util.FieldOptionList;
import com.ck.common.util.STRING;
import com.ck.common.util.batch.CountManager;
import com.ck.common.util.batch.ErrorLog;
import com.ck.common.util.db.DBUtil;
import com.ck.common.util.mail.MailSender;
import com.ck.crypto.ext.CryptoFactory;
import com.ck.crypto.ext.CryptoService;
import com.ck.util.Transaction;
import com.igsapp.db.BatchQueryDataSet;
import com.igsapp.db.DBException;
import com.igsapp.db.DataSet;

/**
 * <pre>
 *  一.  程式功能概要說明：
 *      1.  程式功能：整批掃描影像歸檔作業
 *      2.  程式名稱：AVE0_B168.java
 *      3.  作業方式：BATCH
 *      4.  概要說明：整批掃描影像歸檔作業，每工作日晚上，將各單位做完裝箱確認的影像，從AIX搬到NAS。
 *  
 *  二.  程式架構圖：
 *  
 *  三.  相關檔案（TABLE）：
 *      1.  DBAV.DTAVE102   掃瞄批次處理紀錄
 * </pre>
 * 
 * @since 2015/07/30
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AVE0_B168 extends AV_BatchBean { // 繼承BatchBean

    private static final Logger log = Logger.getLogger(AVE0_B168.class); //Logger

    private boolean isDebug = log.isDebugEnabled();

    private static final String JOB_NAME = "JAAVDE001"; // 作業名稱 

    private static final String PROGRAM = "AVE0_B168"; // 程式名稱

    private static final String BUSINESS = "AV"; // 業務別

    private static final String SUBSYSTEM = "E0"; // 次系統名稱

    private static final String PERIOD = "天"; // 執行週期

    private static final int FETCH_SIZE = 0; // 分批執行的件數, 若設為 0 則不分批, 僅會執行一次, 請注意資料量的大小

    private static final boolean isAUTO_WRITELOG = false; // 設為 true 由父類別計數及寫錯誤訊息, false 請使用 ErrorLog 及CountManager 自行寫錯誤訊息及計數

    private CountManager cntMgr; //記錄件數物件

    private BatchQueryDataSet bqds_QUERY_1; //查詢的物件1

    private ErrorLog errorLog; //錯誤訊息記錄物件

    private static final String INPUT_CNT = "讀取DTAVE102件數";

    private static final String OUTPUT_CNT = "搬移成功件數";

    private static final String ERROR_CNT = "搬移失敗件數";

    private static final String WRONG_CNT = "搬移異常件數";

    private static final String QUERY_MAIL_COUNT = "信件寄送數";

    private static final String E100_OUTPUT_CNT = "DTAVE100搬移成功件數";

    /*紀錄件數變數*/
    private int intINPUT_CNT = 0;

    private int intOUTPUT_CNT = 0;

    private int intERROR_CNT = 0;

    private int intWRONG_CNT = 0;

    private int intE100_OUTPUT_CNT = 0;

    //設定錯誤筆數
    private int TIF_ERROR_CNT = 0;

    //設定批號影像實體檔搬檔是否正確(boolean值)
    private boolean IS_ERROR_BCH_MOVE = false;

    //設定錯誤訊息
    private String ERROR_MSG = "";

    private static final String SQL_QUERY_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_QUERY_1";

    private static final String SQL_UPDATE_2 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_2";

    private static final String SQL_UPDATE_3 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_3";

    private static final String SQL_DELETE_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_DELETE_1";

    //------------------------------------------------------------------------------
    public AVE0_B168() throws Exception {

        // 設定父類別的建構子, 傳入 true 由父類別計數及寫錯誤訊息
        super(FETCH_SIZE, JOB_NAME, PROGRAM, BUSINESS, SUBSYSTEM, PERIOD, log, isAUTO_WRITELOG);
        cntMgr = new CountManager(JOB_NAME, PROGRAM, BUSINESS, SUBSYSTEM, PERIOD);

        bqds_QUERY_1 = getBatchQueryDataSet();

        cntMgr.createCountType(INPUT_CNT);
        cntMgr.createCountType(OUTPUT_CNT);
        cntMgr.createCountType(ERROR_CNT);
        cntMgr.createCountType(WRONG_CNT);
        cntMgr.createCountType(QUERY_MAIL_COUNT);
        cntMgr.createCountType(E100_OUTPUT_CNT);

        errorLog = new ErrorLog(JOB_NAME, PROGRAM, BUSINESS, SUBSYSTEM);

    }

    //------------------------------------------------------------------------------
    /**
     * 執行批次作業
     */
    public void execute(String args[]) {

        try {

            //4.1   讀取DTAVE102上傳紀錄中，已歸檔(DTAVE102.BCH_STS ='T')。
            try {
                String IMG_KIND_QUERY = "";
                String BCH_NO_QUERY = "";
                if (ArrayUtils.isEmpty(args)) {
                    IMG_KIND_QUERY = null;
                    BCH_NO_QUERY = null;
                } else if (args.length == 1) {
                    IMG_KIND_QUERY = args[0];
                    BCH_NO_QUERY = null;
                } else {
                    IMG_KIND_QUERY = args[0];
                    BCH_NO_QUERY = args[1];
                }
                if (StringUtils.isNotBlank(IMG_KIND_QUERY)) {
                    bqds_QUERY_1.setField("IMG_KIND", IMG_KIND_QUERY);
                }
                if (StringUtils.isNotBlank(BCH_NO_QUERY)) {
                    bqds_QUERY_1.setField("BCH_NO", BCH_NO_QUERY);
                }
                searchAndRetrieve(bqds_QUERY_1, SQL_QUERY_1);
                intINPUT_CNT = bqds_QUERY_1.getTotalCount();
            } catch (Exception e) {
                errorLog.addErrorLog("讀取DTAVE102異常", e.getMessage());
                log.fatal("讀取DTAVE102異常", e);
                throw e;
            }

            //2.3   使用ConfigManager 讀取 相關設定值，設定值存放在設定檔：
            ///\\AV_SRC\\...\\config\\...\\fileStore.properties

            String AVE0_IMG_HOME = ConfigManager.getProperty("AVE0_IMG_HOME");

            if (log.isInfoEnabled()) {
                log.info("讀取設定檔參數如下----->");
                log.info("AVE0_IMG_HOME:[" + AVE0_IMG_HOME + "]");
            }

            Set<String> IMG_DVD_NO_SET = new HashSet<String>();
            try {

                for (prepareFetch(); fetchData(bqds_QUERY_1); goNext()) { //分批執行

                    while (bqds_QUERY_1.next()) {

                        try {
                            //要上傳的檔案位置
                            String DIV_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("DIV_NO"));
                            String IMG_KIND = STRING.objToStrNoNull(bqds_QUERY_1.getField("IMG_KIND"));
                            String BCH_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("BCH_NO"));
                            String SCAN_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("SCAN_NO"));
                            String FILE_CNT = STRING.objToStrNoNull(bqds_QUERY_1.getField("FILE_CNT"));
                            String OPR_ID = STRING.objToStrNoNull(bqds_QUERY_1.getField("OPR_ID"));
                            String E110_IMG_PATH = STRING.objToStrNoNull(bqds_QUERY_1.getField("E110_IMG_PATH"));
                            String G102_IMG_DVD_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("G102_IMG_DVD_NO"));
                            //上傳檔案 的 來源位置  
                            String sourceLoc = AVE0_IMG_HOME + "/" + DIV_NO + "/" + IMG_KIND + "/" + BCH_NO;
                            String IMG_DVD_NO = IMG_KIND + DATE.getTodayYearAndMonth(); //ex:AB16801507
                            String targetLoc = E110_IMG_PATH + IMG_DVD_NO + "/" + BCH_NO;

                            boolean ExistsDvdNo = false; //是否已有光碟片號
                            if (StringUtils.isNotBlank(G102_IMG_DVD_NO)) {
                                targetLoc = E110_IMG_PATH + G102_IMG_DVD_NO + "/" + BCH_NO;
                                ExistsDvdNo = true;
                                log.fatal("IMG_KIND=" + IMG_KIND + ", BCH_NO=" + BCH_NO + ", 已有光碟片號[" + G102_IMG_DVD_NO + "]");
                            }

                            //新增光碟片號檔
                            DTAVE101 dtave101Bo;
                            if (!IMG_DVD_NO_SET.contains(IMG_DVD_NO)) {
                                try {
                                    dtave101Bo = this.queryDTAVE101(IMG_DVD_NO);
                                } catch (DataNotFoundException dnfe) {
                                    dtave101Bo = new DTAVE101();
                                    dtave101Bo.setIMG_DVD_NO(IMG_DVD_NO);
                                    
                                    //PCIDSS: 加密Server 作法TEST, STAG 比照 PROD 用IP
                                    dtave101Bo.setIMG_PATH(E110_IMG_PATH);
                                    
                                    dtave101Bo.setIMG_DVD_SIZE("0");
                                    dtave101Bo.setCRT_DATE(DATE.getDBTimeStamp());
                                    dtave101Bo.formatToDB();
                                    DBUtil.insertVO(dtave101Bo, "新增影像儲存路徑檔發生錯誤");
                                    IMG_DVD_NO_SET.add(IMG_DVD_NO);
                                }
                            }

                            targetLoc = targetLoc.substring(1);
                            targetLoc = STRING.replace(targetLoc, "cxlsvr67", "nas67"); 
                            targetLoc = STRING.replace(targetLoc, "cxlsvr69", "nas69"); 
                            targetLoc = STRING.replace(targetLoc, "\\", "/");

                            //回饋影像歸檔完成資訊給上游
                            if ("AT03".equals(IMG_KIND)) {
                                Transaction.begin();
                                try {
                                    this.STEP02(sourceLoc, BCH_NO, IMG_KIND);
                                    Transaction.commit();
                                } catch (Exception e) {
                                    log.fatal("", e);
                                    Transaction.rollback();
                                    setExitCode(ERROR);
                                }
                            }

                            try {
                                //將影像檔案搬到NAS
                                moveFile(sourceLoc, targetLoc, FILE_CNT, IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
                            } catch (Exception e) {
                                log.fatal("", e);
                                StringBuilder sbError = new StringBuilder("IMG_KIND=").append(IMG_KIND).append(", BCH_NO=").append(BCH_NO).append(", 搬檔到NAS發生異常。").append("</br>");
                                log.fatal(sbError.toString(), e);
                                ERROR_MSG = new StringBuilder(ERROR_MSG).append(sbError).append("</br>").toString();
                                intERROR_CNT++;
                            }

                            if (!ExistsDvdNo) {
                                try {
                                    //回壓光碟片號
                                    this.updateDTEDG102(IMG_KIND, BCH_NO, IMG_DVD_NO);
                                } catch (Exception e) {
                                    log.fatal("UPDATE DTEDG102異常：" + "文件分類碼=" + IMG_KIND + "歸檔批號=" + BCH_NO, e);
                                    intWRONG_CNT++;
                                    continue; //繼續處理下一筆DTAVE102
                                }
                            }

                            //回壓DTAVE102狀態為已歸檔完成
                            try {
                                if (IS_ERROR_BCH_MOVE) {
                                    IS_ERROR_BCH_MOVE = false;
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "N", IMG_KIND);
                                } else {
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "Y", IMG_KIND);
                                }
                            } catch (Exception e) {
                                log.fatal("UPDATE DTAVE102異常：" + "文件分類碼=" + IMG_KIND + "歸檔批號=" + BCH_NO, e);
                                intWRONG_CNT++;
                                continue; //繼續處理下一筆DTAVE102
                            }

                            intOUTPUT_CNT = intOUTPUT_CNT + 1;
                        } catch (Exception e) {
                            intERROR_CNT = intERROR_CNT + 1;
                            log.fatal("批次執行錯誤", e);
                            ERROR_MSG = new StringBuilder(ERROR_MSG).append("批次執行錯誤").append("</br>").toString();
                            
                        }
                    }
                }
            } finally {
                System.gc();
            }

            //刪除一個月前已搬檔完成的DTAVE102
            try {
                this.deleteDTAVE102();
            } catch (Exception e) {
                log.fatal("DELETE DETAVE102異常", e);
                ERROR_MSG = new StringBuilder(ERROR_MSG).append("DELETE DETAVE102異常").append("</br>").toString();
                intERROR_CNT++;
                
            }

        } catch (Exception e) {
            log.fatal("", e);
            errorLog.addErrorLog("批次作業有誤", e.getMessage());
            setExitCode(ERROR); //設定預定回傳給Control_M的訊息 , 請依需求調整

        } finally {

            try {
                cntMgr.addCountNumber(INPUT_CNT, intINPUT_CNT);
                cntMgr.addCountNumber(OUTPUT_CNT, intOUTPUT_CNT);
                cntMgr.addCountNumber(ERROR_CNT, intERROR_CNT);
                cntMgr.addCountNumber(WRONG_CNT, intWRONG_CNT);
                cntMgr.addCountNumber(E100_OUTPUT_CNT, intE100_OUTPUT_CNT);
                cntMgr.writeLog();
                errorLog.getErrorCountAndWriteErrorMessage();
            } catch (Exception e) {
                log.error("", e);
            }

            log.fatal(cntMgr);//除錯

            if (bqds_QUERY_1 != null) {
                try {
                    bqds_QUERY_1.close();
                } catch (DBException e) {
                    log.error("", e);
                }
            }

            if (intWRONG_CNT > 0 || intERROR_CNT > 0 || TIF_ERROR_CNT > 0) {
                sendMail("影像搬檔異常通知(AVE0_B168)", ERROR_MSG);
            } else {
                sendMail("影像搬檔成功通知(AVE0_B168)", "");
            }

            
            printExitCode(getExitCode());//回傳給 Control_M 的訊息代碼 , 程式終止

        }

    }

    /**
     * 將指定路徑內所有的檔案 搬移到 指定的路徑下
     */
    private void moveFile(String sourcePath, String absPath, String FILE_CNT, String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws Exception {
        File sourceFile = new File(sourcePath);
        File absPathFile = new File(absPath);

        
            log.fatal("STEP 03 moveFile() 搬移檔案 sourceFile (來源路徑):" + sourceFile);
            log.fatal("STEP 03 moveFile() 搬移檔案 absPathFile (目的地路徑):" + absPathFile);
        

        //將讀取的檔案 搬移到BAK目錄下
        FileUtils.copyDirectory(sourceFile, absPathFile, false); //複製

        if (isDebug) {
            log.debug("STEP 03 moveFile() 複製檔案 完成");
        }

        //檢查 tif 檔 數量 與 DTA單FILE_CNT 是否相同
        TIF_FileFilter x = new TIF_FileFilter("TIF");
        String[] files3 = absPathFile.list(x); //取得符合的檔名
        
        
            log.fatal("STEP 03 moveFile() 比對檔案數量 開始");
            log.fatal("STEP 03 moveFile() absPathFile 目的地檔案 TIF檔案數量:" + files3.length);
            log.fatal("STEP 03 moveFile() FILE_CNT 檔案 DB 紀錄TIF檔案數量:" + FILE_CNT);
            log.fatal("STEP 03 moveFile() 比對檔案數量 結束");
        

        //PCIDSS 2020-10-15
        // 取得加解密工具及金鑰名取得工具
        CryptoService cryptoSvc = CryptoFactory.defaultCryptoService();
        
        // 加密多筆檔案 ex: src.tif --> src.tif.enc
        StringBuilder sb = new StringBuilder();
        for (String sfiles3 : files3) {
            log.fatal("****** sfiles3: " + sfiles3);
            String absPathfiles3 = sb.append(absPath).append("/").append(sfiles3).toString();
            sb.setLength(0);
            log.fatal("****** absPathfiles3(加密檔名後加,enc): " + absPathfiles3);
            File tifFile = new File(absPathfiles3);
            // 加密檔案
            File encryptedFile = new File(sb.append(absPathfiles3).append(".enc").toString());
            sb.setLength(0);
            
            if(encryptedFile.exists()) {
                //代表前1次批次作業已成功加密(加密檔案已存在), 刪掉重新放到NAS上的來源檔
                log.fatal("前1次批次作業已成功加密(加密檔案已存在): "+encryptedFile);
                if(tifFile.exists()) {
                    tifFile.delete();
                    log.fatal("刪掉重新放到NAS上的來源檔: "+tifFile);
                }
                
                continue;
            }
            cryptoSvc.encryptFile(tifFile, encryptedFile);
                                   
            boolean valid = cryptoSvc.validateChecksum(tifFile, encryptedFile);
            //再把未加密檔案刪除
            if(valid) {
                tifFile.delete(); 
                log.fatal("STEP 03 moveFile() 加密目的地路徑檔案 完成");
            }else {
                log.fatal("STEP 03 moveFile() 原始檔和加密檔不一致");
                throw new ModuleException("原始檔: " + absPathfiles3 + " ,和加密檔不一致, 暫不刪除!" ); 
            }
                       

        }        

        intE100_OUTPUT_CNT += files3.length;

        File[] files1 = sourceFile.listFiles();
        File[] files2 = absPathFile.listFiles();
        File ErrorFile = new File(new StringBuilder().append(ConfigManager.getProperty("AVE0_IMG_HOME")).append("/ERROR/").append(DIV_NO).append("/").append(IMG_KIND).append("/").append(BCH_NO).append("/").toString());
        log.error("FILE_CNT --->" + FILE_CNT);
        log.error("files3.length --->" + files3.length);
        if (!FILE_CNT.equals(Integer.toString(files3.length))) {
            /*檢查兩邊檔案數量是否相同，如果不一致，
             * 就將來源路徑資料夾COPY一份到到AIX的/傳入參數.單位代號/傳入參數.文件代碼/下 */
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("STEP 03 搬移檔案.TIF檔與FILE_CNT數量不同。").append(",sourcePath:").append(sourcePath).append(",absPathFile:").append(absPathFile).append(",DB FILE_CNT:").append(FILE_CNT)
                    .append(",實際檔案數量(absPathFile):").append(Integer.toString(files3.length)).append("</br>").toString();
            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            //修改DTAVE102 CHECK_CODE(影像實體異常檢查碼)
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else if (files1.length != files2.length) {
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("來源AIX 檔案數量與目的端NAS檔案數量不同。").append(",sourcePath:").append(sourcePath).append(",來源AIX 檔案數量(sourcePath):").append(files1.length).append(",absPathFile:").append(absPathFile)
                    .append(",目的端NAS檔案數量(absPathFile):").append(files2.length).append("</br>").toString();

            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            //修改DTAVE102 CHECK_CODE(影像實體異常檢查碼)
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else {
            //檢查兩邊檔案數量是否相同，如果一致，就可以砍掉來源路徑資料夾
            if (files1.length == files2.length) {
                for (File file : files1) {
                    //逐筆處理files1
                    FileUtils.forceDelete(file);
                }
            }
            FileUtils.deleteDirectory(sourceFile);
        }

    }

    /**
     * 移檔錯誤動作
     * @param IMG_KIND
     * @param BCH_NO
     * @param DIV_NO
     * @param OPR_ID
     * @throws ModuleException
     */
    private void ErrSet(String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws ModuleException {
        //組傳入Map
        Map reqMap = new HashMap();
        reqMap.put("IMG_KIND", IMG_KIND);
        reqMap.put("BCH_NO", BCH_NO);
        reqMap.put("DIV_NO", DIV_NO);
        reqMap.put("CHECK_CODE", "4");
        new AV_E0Z014().updateCHECK_CODE(reqMap);

        IS_ERROR_BCH_MOVE = true;
        TIF_ERROR_CNT++;
        ERROR_MSG = new StringBuilder(ERROR_MSG).append(DIV_NO).append(",文件代碼:").append(IMG_KIND).append(",歸檔 批號:").append(BCH_NO).append(",作業人員:").append(OPR_ID).append("</br>").toString();
    }

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

    private void updateDTEDG102(String IMG_KIND, String BATCH_NO, String IMG_DVD_NO) throws ModuleException {
        DataSet ds = Transaction.getDataSet();
        ds.setField("IMG_KIND", IMG_KIND);
        ds.setField("BCH_NO", BATCH_NO);
        ds.setField("IMG_DVD_NO", IMG_DVD_NO);
        DBUtil.executeUpdate(ds, SQL_UPDATE_2);
    }

    private void updateDTAVE102(String DIV_NO, String SCAN_NO, String FILE_DATE, String BCH_STS, String IMG_KIND) throws ModuleException {

        DataSet ds = Transaction.getDataSet();

        //更新欄位
        ds.setField("FILE_DATE", FILE_DATE);
        ds.setField("BCH_STS", BCH_STS);
        ds.setField("IMG_KIND", IMG_KIND);

        //where條件
        ds.setField("DIV_NO", DIV_NO);
        ds.setField("SCAN_NO", SCAN_NO);

        DBUtil.executeUpdate(ds, SQL_UPDATE_3, "更新DTAVE102發生錯誤");
    }

    private DTAVE101 queryDTAVE101(String IMG_DVD_NO) throws ModuleException {
        DTAVE101 qurBo = new DTAVE101();
        qurBo.setIMG_DVD_NO(IMG_DVD_NO);
        List outList = DBUtil.retrieveVOsWithUR(qurBo, "查詢DTAVE101查無資料");
        return (DTAVE101) outList.get(0);
    }

    /**
     * 清除期限內
     * @throws ModuleException
     */
    private void deleteDTAVE102() throws ModuleException {
        try {
            DataSet ds = Transaction.getDataSet();
            String DEL_DATE = DATE.addDate(DATE.getDBDate(), 0, -1, 0);
            ds.setField("FILE_DATE", DEL_DATE);
            DBUtil.executeUpdate(ds, SQL_DELETE_1);
        } catch (DataNotFoundException e) {
            //It's OK
        }
    }

    private void STEP02(String FILE_PATH, String BCH_NO, String IMG_KIND) throws Exception {

        File rec_update_file = new File(FILE_PATH + File.separator + "REC_UPDATE.txt");

        if (rec_update_file.exists() == false) {
            throw new ModuleException("[訊息]:" + "BCH_NO:" + BCH_NO + "," + "IMG_KIND:" + IMG_KIND + "," + rec_update_file + "檔案(file.exists()=" + rec_update_file.exists() + ")不存在");
        }

        String line1;
        BufferedReader rd1;
        for (rd1 = new BufferedReader(new FileReader(rec_update_file)); (line1 = rd1.readLine()) != null;) {

            if (StringUtils.isBlank(line1)) {
                break; //空白行 就結束 
            }
            String[] strArr = STRING.split(line1, ",");

            if ("AT03".equalsIgnoreCase(strArr[1])) {

                this.doUpdateDTAV0050(strArr[6].substring(0, 10));
            }
        }
        if (rd1 != null) {
            rd1.close(); //關閉fileread
        }
    }

    /**
     * 更新主檔案件，從受理改為D，配合12碼簽收回條需求
     * @param  RCPT_NO 單據號碼
     * 
     */
    private void doUpdateDTAV0050(String RCPT_NO) {
        //更新主檔狀態為D
        AVA0_0350_mod mod = new AVA0_0350_mod();
        try {
            mod.updateDTAV0050_D(RCPT_NO, "AVE0_B004");
        } catch (DBException dbex) {
            log.fatal("無法更新DTAV0050 狀態為D，案件編號" + RCPT_NO + " 錯誤訊息：" + dbex.getErrorMessage());
        }

    }

    private void sendMail(String title, String MSG) {
        MultiKeyMap mk = new MultiKeyMap();
        List<String> RCV_LIST = new ArrayList();
        RCV_LIST.addAll(FieldOptionList.getFieldOptions("AV", "AVE0_B168_RCV", FieldOptionList.NAME_KEY_CODE_VALUE).keySet());
        //List<String> RCV_MAIL_LIST = new ArrayList<String>();
        
        int mailcount = 0;
        //String RCV_EMAIL = "";
        for (String RCV_EMAIL : RCV_LIST) {
            try {

                if (StringUtils.isNotBlank(RCV_EMAIL)) {
                    log.fatal("\n\n RCV_EMAIL-->" + RCV_EMAIL);
                    mk.put(RCV_EMAIL, MailSender.DEFAULT_MAIL, RCV_EMAIL);
                    mailcount++;
                } else {
                    log.fatal("空白的email:" + RCV_EMAIL);
                }
            } catch (Exception ex) {

                log.fatal("錯誤的email:", ex);
            }

        }

        //寄發Email
        try {
            cntMgr.addCountNumber(QUERY_MAIL_COUNT, mailcount);
            if (StringUtils.isNotBlank(MSG)) {
                MailSender.sendHtmlMail(mk, title, MSG);
            } else {
                MailSender.sendHtmlMail(mk, title, "詳情請見批次LOG");
            }
        } catch (Exception e) {
            log.error("發送郵件失敗", e);
        }
    }

    //-----------------------------------------------------------------------------
}
