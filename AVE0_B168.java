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


@SuppressWarnings({ "unchecked", "rawtypes" })
public class AVE0_B168 extends AV_BatchBean { 

    private static final Logger log = Logger.getLogger(AVE0_B168.class); 

    private boolean isDebug = log.isDebugEnabled();

    private static final String JOB_NAME = "JAAVDE001"; 

    private static final String PROGRAM = "AVE0_B168"; 
    
    private static final String BUSINESS = "AV"; 

    private static final String SUBSYSTEM = "E0"; 

    private static final String PERIOD = "Day"; 

    private static final int FETCH_SIZE = 0; 

    private static final boolean isAUTO_WRITELOG = false; 

    private CountManager cntMgr; 

    private BatchQueryDataSet bqds_QUERY_1; 

    private ErrorLog errorLog; 

    private static final String INPUT_CNT = "DTAVE102count";

    private static final String OUTPUT_CNT = "move_sucess";

    private static final String ERROR_CNT = "move_fail";

    private static final String WRONG_CNT = "move_error";

    private static final String QUERY_MAIL_COUNT = "mail_count";

    private static final String E100_OUTPUT_CNT = "DTAVE100move_sucess";

    
    private int intINPUT_CNT = 0;

    private int intOUTPUT_CNT = 0;

    private int intERROR_CNT = 0;

    private int intWRONG_CNT = 0;

    private int intE100_OUTPUT_CNT = 0;

    
    private int TIF_ERROR_CNT = 0;

    
    private boolean IS_ERROR_BCH_MOVE = false;

    
    private String ERROR_MSG = "";

    private static final String SQL_QUERY_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_QUERY_1";

    private static final String SQL_UPDATE_2 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_2";

    private static final String SQL_UPDATE_3 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_3";

    private static final String SQL_DELETE_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_DELETE_1";

    //------------------------------------------------------------------------------
    public AVE0_B168() throws Exception {
        
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
    
    public void execute(String args[]) {

        try {

            
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
                errorLog.addErrorLog("DTAVE102 Error", e.getMessage());
                log.fatal("DTAVE102 Error", e);
                throw e;
            }

            
            ///\\AV_SRC\\...\\config\\...\\fileStore.properties

            String AVE0_IMG_HOME = ConfigManager.getProperty("AVE0_IMG_HOME");

            if (log.isInfoEnabled()) {                
                log.info("AVE0_IMG_HOME:[" + AVE0_IMG_HOME + "]");
            }

            Set<String> IMG_DVD_NO_SET = new HashSet<String>();
            try {

                for (prepareFetch(); fetchData(bqds_QUERY_1); goNext()) { 
                    while (bqds_QUERY_1.next()) {

                        try {
                            
                            String DIV_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("DIV_NO"));
                            String IMG_KIND = STRING.objToStrNoNull(bqds_QUERY_1.getField("IMG_KIND"));
                            String BCH_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("BCH_NO"));
                            String SCAN_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("SCAN_NO"));
                            String FILE_CNT = STRING.objToStrNoNull(bqds_QUERY_1.getField("FILE_CNT"));
                            String OPR_ID = STRING.objToStrNoNull(bqds_QUERY_1.getField("OPR_ID"));
                            String E110_IMG_PATH = STRING.objToStrNoNull(bqds_QUERY_1.getField("E110_IMG_PATH"));
                            String G102_IMG_DVD_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("G102_IMG_DVD_NO"));                            
                            String sourceLoc = AVE0_IMG_HOME + "/" + DIV_NO + "/" + IMG_KIND + "/" + BCH_NO;
                            String IMG_DVD_NO = IMG_KIND + DATE.getTodayYearAndMonth(); 
                            String targetLoc = E110_IMG_PATH + IMG_DVD_NO + "/" + BCH_NO;

                            boolean ExistsDvdNo = false; 
                            if (StringUtils.isNotBlank(G102_IMG_DVD_NO)) {
                                targetLoc = E110_IMG_PATH + G102_IMG_DVD_NO + "/" + BCH_NO;
                                ExistsDvdNo = true;
                                log.fatal("IMG_KIND=" + IMG_KIND + ", BCH_NO=" + BCH_NO + ", CD_NO[" + G102_IMG_DVD_NO + "]");
                            }
                            
                            DTAVE101 dtave101Bo;
                            if (!IMG_DVD_NO_SET.contains(IMG_DVD_NO)) {
                                try {
                                    dtave101Bo = this.queryDTAVE101(IMG_DVD_NO);
                                } catch (DataNotFoundException dnfe) {
                                    dtave101Bo = new DTAVE101();
                                    dtave101Bo.setIMG_DVD_NO(IMG_DVD_NO);
                                    
                                    //PCIDSS: 
                                    dtave101Bo.setIMG_PATH(E110_IMG_PATH);
                                    
                                    dtave101Bo.setIMG_DVD_SIZE("0");
                                    dtave101Bo.setCRT_DATE(DATE.getDBTimeStamp());
                                    dtave101Bo.formatToDB();
                                    DBUtil.insertVO(dtave101Bo, "PATH ERROR");
                                    IMG_DVD_NO_SET.add(IMG_DVD_NO);
                                }
                            }

                            targetLoc = targetLoc.substring(1);
                            targetLoc = STRING.replace(targetLoc, "cxlsvr67", "nas67"); 
                            targetLoc = STRING.replace(targetLoc, "cxlsvr69", "nas69"); 
                            targetLoc = STRING.replace(targetLoc, "\\", "/");
                            
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
                                moveFile(sourceLoc, targetLoc, FILE_CNT, IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
                            } catch (Exception e) {
                                log.fatal("", e);
                                StringBuilder sbError = new StringBuilder("IMG_KIND=").append(IMG_KIND).append(", BCH_NO=").append(BCH_NO).append(", NAS_ERROR").append("</br>");
                                log.fatal(sbError.toString(), e);
                                ERROR_MSG = new StringBuilder(ERROR_MSG).append(sbError).append("</br>").toString();
                                intERROR_CNT++;
                            }

                            if (!ExistsDvdNo) {
                                try {
                                    
                                    this.updateDTEDG102(IMG_KIND, BCH_NO, IMG_DVD_NO);
                                } catch (Exception e) {
                                    log.fatal("UPDATE ERROR¡G" + "DOC_NO=" + IMG_KIND + "BCH_NO=" + BCH_NO, e);
                                    intWRONG_CNT++;
                                    continue; 
                                }
                            }
                            
                            try {
                                if (IS_ERROR_BCH_MOVE) {
                                    IS_ERROR_BCH_MOVE = false;
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "N", IMG_KIND);
                                } else {
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "Y", IMG_KIND);
                                }
                            } catch (Exception e) {
                                log.fatal("UPDATE ERROR¡G" + "DOC_NO=" + IMG_KIND + "BCH_NO=" + BCH_NO, e);
                                intWRONG_CNT++;
                                continue; 
                            }

                            intOUTPUT_CNT = intOUTPUT_CNT + 1;
                        } catch (Exception e) {
                            intERROR_CNT = intERROR_CNT + 1;
                            log.fatal("BATCH_ERROR", e);
                            ERROR_MSG = new StringBuilder(ERROR_MSG).append("BATCH_ERROR").append("</br>").toString();
                            
                        }
                    }
                }
            } finally {
                System.gc();
            }
            
            try {
                this.deleteDTAVE102();
            } catch (Exception e) {
                log.fatal("DELETE ERROR", e);
                ERROR_MSG = new StringBuilder(ERROR_MSG).append("DELETE ERROR").append("</br>").toString();
                intERROR_CNT++;
                
            }

        } catch (Exception e) {
            log.fatal("", e);
            errorLog.addErrorLog("BATCH ERROR", e.getMessage());
            setExitCode(ERROR); 

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

            log.fatal(cntMgr);

            if (bqds_QUERY_1 != null) {
                try {
                    bqds_QUERY_1.close();
                } catch (DBException e) {
                    log.error("", e);
                }
            }

            if (intWRONG_CNT > 0 || intERROR_CNT > 0 || TIF_ERROR_CNT > 0) {
                sendMail("ERROR", ERROR_MSG);
            } else {
                sendMail("SUCCESS", "");
            }

            
            printExitCode(getExitCode());

        }

    }
    
    private void moveFile(String sourcePath, String absPath, String FILE_CNT, String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws Exception {
        File sourceFile = new File(sourcePath);
        File absPathFile = new File(absPath); 
       
        FileUtils.copyDirectory(sourceFile, absPathFile, false); 

        if (isDebug) {
            log.debug("STEP 03 moveFile() DONE");
        }

        
        TIF_FileFilter x = new TIF_FileFilter("TIF");
        String[] files3 = absPathFile.list(x);           

        //PCIDSS 2020-10-15
        
        CryptoService cryptoSvc = CryptoFactory.defaultCryptoService();
        
        
        StringBuilder sb = new StringBuilder();
        for (String sfiles3 : files3) {
            log.fatal("****** sfiles3: " + sfiles3);
            String absPathfiles3 = sb.append(absPath).append("/").append(sfiles3).toString();
            sb.setLength(0);
            log.fatal("****** absPathfiles3(enc): " + absPathfiles3);
            File tifFile = new File(absPathfiles3);
            
            File encryptedFile = new File(sb.append(absPathfiles3).append(".enc").toString());
            sb.setLength(0);
            
            if(encryptedFile.exists()) {
                
                log.fatal("encryptedFile exist: "+encryptedFile);
                if(tifFile.exists()) {
                    tifFile.delete();
                    log.fatal("Delete File: "+tifFile);
                }
                
                continue;
            }
            cryptoSvc.encryptFile(tifFile, encryptedFile);
                                   
            boolean valid = cryptoSvc.validateChecksum(tifFile, encryptedFile);
            
            if(valid) {
                tifFile.delete();                 
            }else {                
                throw new ModuleException("Origin File: " + absPathfiles3 + " ,not the same with decripted file!" ); 
            }
                       

        }        

        intE100_OUTPUT_CNT += files3.length;

        File[] files1 = sourceFile.listFiles();
        File[] files2 = absPathFile.listFiles();
        File ErrorFile = new File(new StringBuilder().append(ConfigManager.getProperty("AVE0_IMG_HOME")).append("/ERROR/").append(DIV_NO).append("/").append(IMG_KIND).append("/").append(BCH_NO).append("/").toString());
        log.error("FILE_CNT --->" + FILE_CNT);
        log.error("files3.length --->" + files3.length);
        if (!FILE_CNT.equals(Integer.toString(files3.length))) {
            
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("STEP 03 .TIF FILE_CNT ERROR ").append(",sourcePath:").append(sourcePath).append(",absPathFile:").append(absPathFile).append(",DB FILE_CNT:").append(FILE_CNT)
                    .append(",Actual(absPathFile):").append(Integer.toString(files3.length)).append("</br>").toString();
            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else if (files1.length != files2.length) {
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("AIX NAS Not the same¡C").append(",sourcePath:").append(sourcePath).append(",AIX(sourcePath):").append(files1.length).append(",absPathFile:").append(absPathFile)
                    .append(",NAS(absPathFile):").append(files2.length).append("</br>").toString();

            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else {
            
            if (files1.length == files2.length) {
                for (File file : files1) {                    
                    FileUtils.forceDelete(file);
                }
            }
            FileUtils.deleteDirectory(sourceFile);
        }

    }

    /**
     * ErrSet
     * @param IMG_KIND
     * @param BCH_NO
     * @param DIV_NO
     * @param OPR_ID
     * @throws ModuleException
     */
    private void ErrSet(String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws ModuleException {
        
        Map reqMap = new HashMap();
        reqMap.put("IMG_KIND", IMG_KIND);
        reqMap.put("BCH_NO", BCH_NO);
        reqMap.put("DIV_NO", DIV_NO);
        reqMap.put("CHECK_CODE", "4");
        new AV_E0Z014().updateCHECK_CODE(reqMap);

        IS_ERROR_BCH_MOVE = true;
        TIF_ERROR_CNT++;
        ERROR_MSG = new StringBuilder(ERROR_MSG).append(DIV_NO).append(",FILE_NO:").append(IMG_KIND).append(",BATCH_NO:").append(BCH_NO).append(",OP:").append(OPR_ID).append("</br>").toString();
    }

    
    private static class TIF_FileFilter implements FilenameFilter {

        private String ext;

        public TIF_FileFilter(String ext) {

            this.ext = ext;
        }

        public boolean accept(File dir, String filename) {
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
        
        ds.setField("FILE_DATE", FILE_DATE);
        ds.setField("BCH_STS", BCH_STS);
        ds.setField("IMG_KIND", IMG_KIND);
        
        ds.setField("DIV_NO", DIV_NO);
        ds.setField("SCAN_NO", SCAN_NO);

        DBUtil.executeUpdate(ds, SQL_UPDATE_3, "UPDATE ERROR");
    }

    private DTAVE101 queryDTAVE101(String IMG_DVD_NO) throws ModuleException {
        DTAVE101 qurBo = new DTAVE101();
        qurBo.setIMG_DVD_NO(IMG_DVD_NO);
        List outList = DBUtil.retrieveVOsWithUR(qurBo, "NO DATA");
        return (DTAVE101) outList.get(0);
    }

    
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
            throw new ModuleException("[MEMO]:" + "BCH_NO:" + BCH_NO + "," + "IMG_KIND:" + IMG_KIND + "," + rec_update_file + "(file.exists()=" + rec_update_file.exists() + ")Not exist");
        }

        String line1;
        BufferedReader rd1;
        for (rd1 = new BufferedReader(new FileReader(rec_update_file)); (line1 = rd1.readLine()) != null;) {

            if (StringUtils.isBlank(line1)) {
                break;  
            }
            String[] strArr = STRING.split(line1, ",");

            if ("AT03".equalsIgnoreCase(strArr[1])) {

                this.doUpdateDTAV0050(strArr[6].substring(0, 10));
            }
        }
        if (rd1 != null) {
            rd1.close(); 
        }
    }

    
    private void doUpdateDTAV0050(String RCPT_NO) {
        
        AVA0_0350_mod mod = new AVA0_0350_mod();
        try {
            mod.updateDTAV0050_D(RCPT_NO, "AVE0_B004");
        } catch (DBException dbex) {
            log.fatal("Status: D¡ANO" + RCPT_NO + " ERROR¡G" + dbex.getErrorMessage());
        }

    }

    private void sendMail(String title, String MSG) {
        MultiKeyMap mk = new MultiKeyMap();
        List<String> RCV_LIST = new ArrayList();
        RCV_LIST.addAll(FieldOptionList.getFieldOptions("AV", "AVE0_B168_RCV", FieldOptionList.NAME_KEY_CODE_VALUE).keySet());
        
        
        int mailcount = 0;
        
        for (String RCV_EMAIL : RCV_LIST) {
            try {

                if (StringUtils.isNotBlank(RCV_EMAIL)) {
                    log.fatal("\n\n RCV_EMAIL-->" + RCV_EMAIL);
                    mk.put(RCV_EMAIL, MailSender.DEFAULT_MAIL, RCV_EMAIL);
                    mailcount++;
                } else {
                    log.fatal("empty email:" + RCV_EMAIL);
                }
            } catch (Exception ex) {

                log.fatal("error email:", ex);
            }

        }

        
        try {
            cntMgr.addCountNumber(QUERY_MAIL_COUNT, mailcount);
            if (StringUtils.isNotBlank(MSG)) {
                MailSender.sendHtmlMail(mk, title, MSG);
            } else {
                MailSender.sendHtmlMail(mk, title, "BATCH LOG");
            }
        } catch (Exception e) {
            log.error("MAIL ERROR", e);
        }
    }

    //-----------------------------------------------------------------------------
}
