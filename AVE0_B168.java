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
 *  �@.  �{���\�෧�n�����G
 *      1.  �{���\��G��屽�y�v���k�ɧ@�~
 *      2.  �{���W�١GAVE0_B168.java
 *      3.  �@�~�覡�GBATCH
 *      4.  ���n�����G��屽�y�v���k�ɧ@�~�A�C�u�@��ߤW�A�N�U��찵���˽c�T�{���v���A�qAIX�h��NAS�C
 *  
 *  �G.  �{���[�c�ϡG
 *  
 *  �T.  �����ɮס]TABLE�^�G
 *      1.  DBAV.DTAVE102   ���˧妸�B�z����
 * </pre>
 * 
 * @since 2015/07/30
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AVE0_B168 extends AV_BatchBean { // �~��BatchBean

    private static final Logger log = Logger.getLogger(AVE0_B168.class); //Logger

    private boolean isDebug = log.isDebugEnabled();

    private static final String JOB_NAME = "JAAVDE001"; // �@�~�W�� 

    private static final String PROGRAM = "AVE0_B168"; // �{���W��

    private static final String BUSINESS = "AV"; // �~�ȧO

    private static final String SUBSYSTEM = "E0"; // ���t�ΦW��

    private static final String PERIOD = "��"; // ����g��

    private static final int FETCH_SIZE = 0; // ������檺���, �Y�]�� 0 �h������, �ȷ|����@��, �Ъ`�N��ƶq���j�p

    private static final boolean isAUTO_WRITELOG = false; // �]�� true �Ѥ����O�p�Ƥμg���~�T��, false �Шϥ� ErrorLog ��CountManager �ۦ�g���~�T���έp��

    private CountManager cntMgr; //�O����ƪ���

    private BatchQueryDataSet bqds_QUERY_1; //�d�ߪ�����1

    private ErrorLog errorLog; //���~�T���O������

    private static final String INPUT_CNT = "Ū��DTAVE102���";

    private static final String OUTPUT_CNT = "�h�����\���";

    private static final String ERROR_CNT = "�h�����ѥ��";

    private static final String WRONG_CNT = "�h�����`���";

    private static final String QUERY_MAIL_COUNT = "�H��H�e��";

    private static final String E100_OUTPUT_CNT = "DTAVE100�h�����\���";

    /*��������ܼ�*/
    private int intINPUT_CNT = 0;

    private int intOUTPUT_CNT = 0;

    private int intERROR_CNT = 0;

    private int intWRONG_CNT = 0;

    private int intE100_OUTPUT_CNT = 0;

    //�]�w���~����
    private int TIF_ERROR_CNT = 0;

    //�]�w�帹�v�������ɷh�ɬO�_���T(boolean��)
    private boolean IS_ERROR_BCH_MOVE = false;

    //�]�w���~�T��
    private String ERROR_MSG = "";

    private static final String SQL_QUERY_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_QUERY_1";

    private static final String SQL_UPDATE_2 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_2";

    private static final String SQL_UPDATE_3 = "com.ck.av.e0.batch.AVE0_B168.SQL_UPDATE_3";

    private static final String SQL_DELETE_1 = "com.ck.av.e0.batch.AVE0_B168.SQL_DELETE_1";

    //------------------------------------------------------------------------------
    public AVE0_B168() throws Exception {

        // �]�w�����O���غc�l, �ǤJ true �Ѥ����O�p�Ƥμg���~�T��
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
     * ����妸�@�~
     */
    public void execute(String args[]) {

        try {

            //4.1   Ū��DTAVE102�W�Ǭ������A�w�k��(DTAVE102.BCH_STS ='T')�C
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
                errorLog.addErrorLog("Ū��DTAVE102���`", e.getMessage());
                log.fatal("Ū��DTAVE102���`", e);
                throw e;
            }

            //2.3   �ϥ�ConfigManager Ū�� �����]�w�ȡA�]�w�Ȧs��b�]�w�ɡG
            ///\\AV_SRC\\...\\config\\...\\fileStore.properties

            String AVE0_IMG_HOME = ConfigManager.getProperty("AVE0_IMG_HOME");

            if (log.isInfoEnabled()) {
                log.info("Ū���]�w�ɰѼƦp�U----->");
                log.info("AVE0_IMG_HOME:[" + AVE0_IMG_HOME + "]");
            }

            Set<String> IMG_DVD_NO_SET = new HashSet<String>();
            try {

                for (prepareFetch(); fetchData(bqds_QUERY_1); goNext()) { //�������

                    while (bqds_QUERY_1.next()) {

                        try {
                            //�n�W�Ǫ��ɮצ�m
                            String DIV_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("DIV_NO"));
                            String IMG_KIND = STRING.objToStrNoNull(bqds_QUERY_1.getField("IMG_KIND"));
                            String BCH_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("BCH_NO"));
                            String SCAN_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("SCAN_NO"));
                            String FILE_CNT = STRING.objToStrNoNull(bqds_QUERY_1.getField("FILE_CNT"));
                            String OPR_ID = STRING.objToStrNoNull(bqds_QUERY_1.getField("OPR_ID"));
                            String E110_IMG_PATH = STRING.objToStrNoNull(bqds_QUERY_1.getField("E110_IMG_PATH"));
                            String G102_IMG_DVD_NO = STRING.objToStrNoNull(bqds_QUERY_1.getField("G102_IMG_DVD_NO"));
                            //�W���ɮ� �� �ӷ���m  
                            String sourceLoc = AVE0_IMG_HOME + "/" + DIV_NO + "/" + IMG_KIND + "/" + BCH_NO;
                            String IMG_DVD_NO = IMG_KIND + DATE.getTodayYearAndMonth(); //ex:AB16801507
                            String targetLoc = E110_IMG_PATH + IMG_DVD_NO + "/" + BCH_NO;

                            boolean ExistsDvdNo = false; //�O�_�w�����Ф���
                            if (StringUtils.isNotBlank(G102_IMG_DVD_NO)) {
                                targetLoc = E110_IMG_PATH + G102_IMG_DVD_NO + "/" + BCH_NO;
                                ExistsDvdNo = true;
                                log.fatal("IMG_KIND=" + IMG_KIND + ", BCH_NO=" + BCH_NO + ", �w�����Ф���[" + G102_IMG_DVD_NO + "]");
                            }

                            //�s�W���Ф�����
                            DTAVE101 dtave101Bo;
                            if (!IMG_DVD_NO_SET.contains(IMG_DVD_NO)) {
                                try {
                                    dtave101Bo = this.queryDTAVE101(IMG_DVD_NO);
                                } catch (DataNotFoundException dnfe) {
                                    dtave101Bo = new DTAVE101();
                                    dtave101Bo.setIMG_DVD_NO(IMG_DVD_NO);
                                    
                                    //PCIDSS: �[�KServer �@�kTEST, STAG ��� PROD ��IP
                                    dtave101Bo.setIMG_PATH(E110_IMG_PATH);
                                    
                                    dtave101Bo.setIMG_DVD_SIZE("0");
                                    dtave101Bo.setCRT_DATE(DATE.getDBTimeStamp());
                                    dtave101Bo.formatToDB();
                                    DBUtil.insertVO(dtave101Bo, "�s�W�v���x�s���|�ɵo�Ϳ��~");
                                    IMG_DVD_NO_SET.add(IMG_DVD_NO);
                                }
                            }

                            targetLoc = targetLoc.substring(1);
                            targetLoc = STRING.replace(targetLoc, "cxlsvr67", "nas67"); 
                            targetLoc = STRING.replace(targetLoc, "cxlsvr69", "nas69"); 
                            targetLoc = STRING.replace(targetLoc, "\\", "/");

                            //�^�X�v���k�ɧ�����T���W��
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
                                //�N�v���ɮ׷h��NAS
                                moveFile(sourceLoc, targetLoc, FILE_CNT, IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
                            } catch (Exception e) {
                                log.fatal("", e);
                                StringBuilder sbError = new StringBuilder("IMG_KIND=").append(IMG_KIND).append(", BCH_NO=").append(BCH_NO).append(", �h�ɨ�NAS�o�Ͳ��`�C").append("</br>");
                                log.fatal(sbError.toString(), e);
                                ERROR_MSG = new StringBuilder(ERROR_MSG).append(sbError).append("</br>").toString();
                                intERROR_CNT++;
                            }

                            if (!ExistsDvdNo) {
                                try {
                                    //�^�����Ф���
                                    this.updateDTEDG102(IMG_KIND, BCH_NO, IMG_DVD_NO);
                                } catch (Exception e) {
                                    log.fatal("UPDATE DTEDG102���`�G" + "�������X=" + IMG_KIND + "�k�ɧ帹=" + BCH_NO, e);
                                    intWRONG_CNT++;
                                    continue; //�~��B�z�U�@��DTAVE102
                                }
                            }

                            //�^��DTAVE102���A���w�k�ɧ���
                            try {
                                if (IS_ERROR_BCH_MOVE) {
                                    IS_ERROR_BCH_MOVE = false;
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "N", IMG_KIND);
                                } else {
                                    this.updateDTAVE102(DIV_NO, SCAN_NO, DATE.getDBTimeStamp(), "Y", IMG_KIND);
                                }
                            } catch (Exception e) {
                                log.fatal("UPDATE DTAVE102���`�G" + "�������X=" + IMG_KIND + "�k�ɧ帹=" + BCH_NO, e);
                                intWRONG_CNT++;
                                continue; //�~��B�z�U�@��DTAVE102
                            }

                            intOUTPUT_CNT = intOUTPUT_CNT + 1;
                        } catch (Exception e) {
                            intERROR_CNT = intERROR_CNT + 1;
                            log.fatal("�妸������~", e);
                            ERROR_MSG = new StringBuilder(ERROR_MSG).append("�妸������~").append("</br>").toString();
                            
                        }
                    }
                }
            } finally {
                System.gc();
            }

            //�R���@�Ӥ�e�w�h�ɧ�����DTAVE102
            try {
                this.deleteDTAVE102();
            } catch (Exception e) {
                log.fatal("DELETE DETAVE102���`", e);
                ERROR_MSG = new StringBuilder(ERROR_MSG).append("DELETE DETAVE102���`").append("</br>").toString();
                intERROR_CNT++;
                
            }

        } catch (Exception e) {
            log.fatal("", e);
            errorLog.addErrorLog("�妸�@�~���~", e.getMessage());
            setExitCode(ERROR); //�]�w�w�w�^�ǵ�Control_M���T�� , �Ш̻ݨD�վ�

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

            log.fatal(cntMgr);//����

            if (bqds_QUERY_1 != null) {
                try {
                    bqds_QUERY_1.close();
                } catch (DBException e) {
                    log.error("", e);
                }
            }

            if (intWRONG_CNT > 0 || intERROR_CNT > 0 || TIF_ERROR_CNT > 0) {
                sendMail("�v���h�ɲ��`�q��(AVE0_B168)", ERROR_MSG);
            } else {
                sendMail("�v���h�ɦ��\�q��(AVE0_B168)", "");
            }

            
            printExitCode(getExitCode());//�^�ǵ� Control_M ���T���N�X , �{���פ�

        }

    }

    /**
     * �N���w���|���Ҧ����ɮ� �h���� ���w�����|�U
     */
    private void moveFile(String sourcePath, String absPath, String FILE_CNT, String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws Exception {
        File sourceFile = new File(sourcePath);
        File absPathFile = new File(absPath);

        
            log.fatal("STEP 03 moveFile() �h���ɮ� sourceFile (�ӷ����|):" + sourceFile);
            log.fatal("STEP 03 moveFile() �h���ɮ� absPathFile (�ت��a���|):" + absPathFile);
        

        //�NŪ�����ɮ� �h����BAK�ؿ��U
        FileUtils.copyDirectory(sourceFile, absPathFile, false); //�ƻs

        if (isDebug) {
            log.debug("STEP 03 moveFile() �ƻs�ɮ� ����");
        }

        //�ˬd tif �� �ƶq �P DTA��FILE_CNT �O�_�ۦP
        TIF_FileFilter x = new TIF_FileFilter("TIF");
        String[] files3 = absPathFile.list(x); //���o�ŦX���ɦW
        
        
            log.fatal("STEP 03 moveFile() ����ɮ׼ƶq �}�l");
            log.fatal("STEP 03 moveFile() absPathFile �ت��a�ɮ� TIF�ɮ׼ƶq:" + files3.length);
            log.fatal("STEP 03 moveFile() FILE_CNT �ɮ� DB ����TIF�ɮ׼ƶq:" + FILE_CNT);
            log.fatal("STEP 03 moveFile() ����ɮ׼ƶq ����");
        

        //PCIDSS 2020-10-15
        // ���o�[�ѱK�u��Ϊ��_�W���o�u��
        CryptoService cryptoSvc = CryptoFactory.defaultCryptoService();
        
        // �[�K�h���ɮ� ex: src.tif --> src.tif.enc
        StringBuilder sb = new StringBuilder();
        for (String sfiles3 : files3) {
            log.fatal("****** sfiles3: " + sfiles3);
            String absPathfiles3 = sb.append(absPath).append("/").append(sfiles3).toString();
            sb.setLength(0);
            log.fatal("****** absPathfiles3(�[�K�ɦW��[,enc): " + absPathfiles3);
            File tifFile = new File(absPathfiles3);
            // �[�K�ɮ�
            File encryptedFile = new File(sb.append(absPathfiles3).append(".enc").toString());
            sb.setLength(0);
            
            if(encryptedFile.exists()) {
                //�N��e1���妸�@�~�w���\�[�K(�[�K�ɮפw�s�b), �R�����s���NAS�W���ӷ���
                log.fatal("�e1���妸�@�~�w���\�[�K(�[�K�ɮפw�s�b): "+encryptedFile);
                if(tifFile.exists()) {
                    tifFile.delete();
                    log.fatal("�R�����s���NAS�W���ӷ���: "+tifFile);
                }
                
                continue;
            }
            cryptoSvc.encryptFile(tifFile, encryptedFile);
                                   
            boolean valid = cryptoSvc.validateChecksum(tifFile, encryptedFile);
            //�A�⥼�[�K�ɮקR��
            if(valid) {
                tifFile.delete(); 
                log.fatal("STEP 03 moveFile() �[�K�ت��a���|�ɮ� ����");
            }else {
                log.fatal("STEP 03 moveFile() ��l�ɩM�[�K�ɤ��@�P");
                throw new ModuleException("��l��: " + absPathfiles3 + " ,�M�[�K�ɤ��@�P, �Ȥ��R��!" ); 
            }
                       

        }        

        intE100_OUTPUT_CNT += files3.length;

        File[] files1 = sourceFile.listFiles();
        File[] files2 = absPathFile.listFiles();
        File ErrorFile = new File(new StringBuilder().append(ConfigManager.getProperty("AVE0_IMG_HOME")).append("/ERROR/").append(DIV_NO).append("/").append(IMG_KIND).append("/").append(BCH_NO).append("/").toString());
        log.error("FILE_CNT --->" + FILE_CNT);
        log.error("files3.length --->" + files3.length);
        if (!FILE_CNT.equals(Integer.toString(files3.length))) {
            /*�ˬd�����ɮ׼ƶq�O�_�ۦP�A�p�G���@�P�A
             * �N�N�ӷ����|��Ƨ�COPY�@�����AIX��/�ǤJ�Ѽ�.���N��/�ǤJ�Ѽ�.���N�X/�U */
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("STEP 03 �h���ɮ�.TIF�ɻPFILE_CNT�ƶq���P�C").append(",sourcePath:").append(sourcePath).append(",absPathFile:").append(absPathFile).append(",DB FILE_CNT:").append(FILE_CNT)
                    .append(",����ɮ׼ƶq(absPathFile):").append(Integer.toString(files3.length)).append("</br>").toString();
            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            //�ק�DTAVE102 CHECK_CODE(�v�����鲧�`�ˬd�X)
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else if (files1.length != files2.length) {
            ERROR_MSG = new StringBuilder(ERROR_MSG).append("�ӷ�AIX �ɮ׼ƶq�P�ت���NAS�ɮ׼ƶq���P�C").append(",sourcePath:").append(sourcePath).append(",�ӷ�AIX �ɮ׼ƶq(sourcePath):").append(files1.length).append(",absPathFile:").append(absPathFile)
                    .append(",�ت���NAS�ɮ׼ƶq(absPathFile):").append(files2.length).append("</br>").toString();

            log.fatal(ERROR_MSG);
            FileUtils.copyDirectory(sourceFile, ErrorFile, false); 
            //�ק�DTAVE102 CHECK_CODE(�v�����鲧�`�ˬd�X)
            ErrSet(IMG_KIND, BCH_NO, DIV_NO, OPR_ID);
        } else {
            //�ˬd�����ɮ׼ƶq�O�_�ۦP�A�p�G�@�P�A�N�i�H�屼�ӷ����|��Ƨ�
            if (files1.length == files2.length) {
                for (File file : files1) {
                    //�v���B�zfiles1
                    FileUtils.forceDelete(file);
                }
            }
            FileUtils.deleteDirectory(sourceFile);
        }

    }

    /**
     * ���ɿ��~�ʧ@
     * @param IMG_KIND
     * @param BCH_NO
     * @param DIV_NO
     * @param OPR_ID
     * @throws ModuleException
     */
    private void ErrSet(String IMG_KIND, String BCH_NO, String DIV_NO, String OPR_ID) throws ModuleException {
        //�նǤJMap
        Map reqMap = new HashMap();
        reqMap.put("IMG_KIND", IMG_KIND);
        reqMap.put("BCH_NO", BCH_NO);
        reqMap.put("DIV_NO", DIV_NO);
        reqMap.put("CHECK_CODE", "4");
        new AV_E0Z014().updateCHECK_CODE(reqMap);

        IS_ERROR_BCH_MOVE = true;
        TIF_ERROR_CNT++;
        ERROR_MSG = new StringBuilder(ERROR_MSG).append(DIV_NO).append(",���N�X:").append(IMG_KIND).append(",�k�� �帹:").append(BCH_NO).append(",�@�~�H��:").append(OPR_ID).append("</br>").toString();
    }

    /**
     * �w�q�L�o�ɦW��k
     *   @param  name    �D�ɦW�]�t���Ǧr��
     *   @param  ext     ���ɦW
     */
    private static class TIF_FileFilter implements FilenameFilter {

        private String ext;

        public TIF_FileFilter(String ext) {//�إߪ̤�k

            this.ext = ext;
        }

        public boolean accept(File dir, String filename) {//accept��k
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

        //��s���
        ds.setField("FILE_DATE", FILE_DATE);
        ds.setField("BCH_STS", BCH_STS);
        ds.setField("IMG_KIND", IMG_KIND);

        //where����
        ds.setField("DIV_NO", DIV_NO);
        ds.setField("SCAN_NO", SCAN_NO);

        DBUtil.executeUpdate(ds, SQL_UPDATE_3, "��sDTAVE102�o�Ϳ��~");
    }

    private DTAVE101 queryDTAVE101(String IMG_DVD_NO) throws ModuleException {
        DTAVE101 qurBo = new DTAVE101();
        qurBo.setIMG_DVD_NO(IMG_DVD_NO);
        List outList = DBUtil.retrieveVOsWithUR(qurBo, "�d��DTAVE101�d�L���");
        return (DTAVE101) outList.get(0);
    }

    /**
     * �M��������
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
            throw new ModuleException("[�T��]:" + "BCH_NO:" + BCH_NO + "," + "IMG_KIND:" + IMG_KIND + "," + rec_update_file + "�ɮ�(file.exists()=" + rec_update_file.exists() + ")���s�b");
        }

        String line1;
        BufferedReader rd1;
        for (rd1 = new BufferedReader(new FileReader(rec_update_file)); (line1 = rd1.readLine()) != null;) {

            if (StringUtils.isBlank(line1)) {
                break; //�ťզ� �N���� 
            }
            String[] strArr = STRING.split(line1, ",");

            if ("AT03".equalsIgnoreCase(strArr[1])) {

                this.doUpdateDTAV0050(strArr[6].substring(0, 10));
            }
        }
        if (rd1 != null) {
            rd1.close(); //����fileread
        }
    }

    /**
     * ��s�D�ɮץ�A�q���z�אּD�A�t�X12�Xñ���^���ݨD
     * @param  RCPT_NO ��ڸ��X
     * 
     */
    private void doUpdateDTAV0050(String RCPT_NO) {
        //��s�D�ɪ��A��D
        AVA0_0350_mod mod = new AVA0_0350_mod();
        try {
            mod.updateDTAV0050_D(RCPT_NO, "AVE0_B004");
        } catch (DBException dbex) {
            log.fatal("�L�k��sDTAV0050 ���A��D�A�ץ�s��" + RCPT_NO + " ���~�T���G" + dbex.getErrorMessage());
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
                    log.fatal("�ťժ�email:" + RCV_EMAIL);
                }
            } catch (Exception ex) {

                log.fatal("���~��email:", ex);
            }

        }

        //�H�oEmail
        try {
            cntMgr.addCountNumber(QUERY_MAIL_COUNT, mailcount);
            if (StringUtils.isNotBlank(MSG)) {
                MailSender.sendHtmlMail(mk, title, MSG);
            } else {
                MailSender.sendHtmlMail(mk, title, "�Ա��Ш��妸LOG");
            }
        } catch (Exception e) {
            log.error("�o�e�l�󥢱�", e);
        }
    }

    //-----------------------------------------------------------------------------
}
