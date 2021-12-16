/*
 * �b 2007/02/05 �إ�       
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

    �@�B  �{���\�෧�z�G
            �{���\��    �v���k�ɧ@�~
            �{���W��    AVE0_5800
            �@�~�覡    ONLINE
            ���n����    �v���k�ɸ�Ʒs�W/��s����
            �B�z�H��    ��F����/�A�Ȥ��߸g��
    
    �G�B  �ϥμҲ�
            ����  ���廡��    CLASS   METHOD
            1.      �c���B�帹�y���������� DTEDG101    qryBoxNO
        getBoxNO
    
    �T�B  �ϥ��ɮ�
            ����  ���廡��    �ɮצW��
            1   ���˧妸�B�z����    DTAVE102
            2   ��������   DTEDG100


    AVE0_5800.java
    @author jefftseng
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AVE0_5800 extends UCBean {

    private static final String CLASS_NAME = AVE0_5800.class.getName();

    private static Logger log = Logger.getLogger(CLASS_NAME);

    private boolean isDebug = log.isDebugEnabled();

    private ResponseContext resp; //��TxBean�{���X�@�Ϊ� ResponseContext

    private ReturnMessage msg; //��TxBean�{���X�@�Ϊ� ReturnMessage

    private UserObject user; //��TxBean�{���X�@�Ϊ� UserObject

    private String UserID = ""; //�@�~�H��ID

    private String UserName = ""; //�@�~�H���m�W

    private String DivNo = ""; //�@�~�H�����N��

    private String DivName = ""; //�@�~�H�����W��

    private AVE0_5800_mod ave0_5800_mod;//�� TxBean �{���X�@�Ϊ� Module

    //  private AV_E0Z003 ave0z003_mod;     //�� TxBean �{���X�@�Ϊ� Module

    private ED_G0Z100 ed_g0z100_mod; //�� TxBean �{���X�@�Ϊ� Module

    private ED_B0Z001 ed_b0z001; //�� TxBean �{���X�@�Ϊ� Module

    private AVE0_6000_mod ave0_6000_mod;//�� TxBean �{���X�@�Ϊ� Module

    private AV_E0Z014 av_e0z014_mod; //�� TxBean �{���X�@�Ϊ� Module

    //------------------------------------------------------------------------------
    /** 
    * �мg�����O��start()�H�j��C��Dispatcher�I�smethod������{���۩w����l�ʧ@ 
    */
    public ResponseContext start(RequestContext req) throws TxException, ServiceException {
        super.start(req); // �@�w�n invoke super.start() �H�����v�����ˮ� 
        initApp(req); // �I�s�۩w����l�ʧ@
        return null;
    }

    //------------------------------------------------------------------------------
    /**
    * �_�l�@�Ϊ��ʧ@
    */
    private void initApp(RequestContext req) {

        // �إ� ��TxBean �q�Ϊ�����
        resp = this.newResponseContext();
        msg = new ReturnMessage();
        user = this.getUserObject(req);

        // ���N ReturnMessage �� reference �[�� response coontext
        resp.addOutputData(IConstantMap.ErrMsg, msg);

        // �j�����ʧ@���O�ۦP�A�p�G�ݭn���P���b�]�w�@��
        resp.setResponseCode("result");

        //�]�w �@�~�H�� �@���ܼƭ�
        UserID = user.getEmpID();
        UserName = user.getEmpName();
        DivNo = user.getOpUnit();
        DivName = user.getDivShortName();

        //��l�Ʀ@�μҲ�
        ave0_5800_mod = new AVE0_5800_mod();
        //      ave0z003_mod = new AV_E0Z003(); 
        ed_g0z100_mod = new ED_G0Z100();
        ed_b0z001 = new ED_B0Z001();
        ave0_6000_mod = new AVE0_6000_mod();
        av_e0z014_mod = new AV_E0Z014();
    }

    //------------------------------------------------------------------------------
    /**
    * ��l�ƭ���
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrompt(RequestContext req) throws TxException {
        try {

            this.setOpt(); //���͵e���ݭn��SelectOption

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
    * �d��
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

            this.setOpt(); //���͵e���ݭn��SelectOption
            if (isDebug) {
                log.debug("-------debug --���͵e���ݭn��SelectOption OK");
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
                msg.setMsgDesc("�d���k�ɧ帹���\");
            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("BCHNO_OPT", new SelectOptUtil());
                this.setStatus("prompt");
                msg.setMsgDesc("�d�L�k�ɧ帹");
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
    * Ū���ɮ� 
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doReadfile(RequestContext req) throws TxException {

        try {
            //Ū��JSP�����Ѽ�
            String DIV_NO = req.getParameter("DIV_NO").trim();
            String OPR_ID = req.getParameter("OPR_ID");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String BCHNO_VALUE = req.getParameter("BCHNO_OPT");

            //�NJSP�����ݭn�Ѽ� �s��Map ��iSession
            Map jvm = new ListOrderedMap();
            jvm.put("DIV_NO", DIV_NO);
            jvm.put("OPR_ID", OPR_ID);
            jvm.put("USER_ID", UserID);
            jvm.put("IMG_KIND_VALUE", IMG_KIND_VALUE);
            jvm.put("BCHNO_VALUE", BCHNO_VALUE);
            this.putValue("jvm", jvm);

            this.setOpt(); //���͵e���ݭn�� ���N�XSelectOption

            //�����k�ɧ帹SelectOption
            try {
                this.setBCHNOOpt(jvm.get("DIV_NO").toString(), jvm.get("IMG_KIND_VALUE").toString(), "S", OPR_ID);
                this.setStatus("query");
            } catch (DataNotFoundException dnfe) {
                resp.addOutputData("BCHNO_OPT", new SelectOptUtil());
                this.putValue("jvl", new ArrayList());
                this.setStatus("prompt");
                msg.setMsgDesc("�d�L�k�ɧ帹");
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
            //spec 2.3.1 Ū���G�Y����������AA02���Ρ�AA03���Ρ�AA04���Ρ�AA05���h�̩ҿ��k�ɧ帹�Τ�����Ū���̿����DTAVE100 ��ܨ���s
            if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                // made by Dai You-Chun 0990712 �t�X�s�������ץ�Ū��DTAVE100
                fileList = ave0_5800_mod.queryDTAVE100(jvm.get("IMG_KIND_VALUE").toString(), BCH_NO);
                if (fileList.size() == 0) {
                    msg.setReturnCode(ReturnCode.ERROR);
                    msg.setMsgDesc("�L�v���k�ɽs��");
                    this.putValue("jvl", new ArrayList());
                    this.setStatus("prompt");
                    return resp;
                }
            } else {
                //spec 2.3Ū���W�Ǫ� FILEREC.DAT
                File filerecDat = this.getFile(jvm.get("DIV_NO").toString(), jvm.get("IMG_KIND_VALUE").toString(), BCH_NO, "FILEREC.DAT");

                if (!filerecDat.exists()) {
                    msg.setReturnCode(ReturnCode.ERROR);
                    msg.setMsgDesc("�v���ɮץؿ��LFILEREC.DAT");
                    this.putValue("jvl", new ArrayList());
                    this.setStatus("prompt");
                    return resp;
                }

                //Ū���ɮפ��e ��ѨC�@�� �HMap �s��bList
                fileList = this.fileToList(filerecDat, BCH_NO);
            }

            //�զX�e���v���k�ɽs��  TextArea �һݤ�r
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
                        IMG_KEY = "��" + IMG_KEY + "��";
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

            String YEAR = DATE.getDBDate(); //�褸�~��X
            YEAR = YEAR.substring(2, 4);

            //�˽c���X 
            String BOX_SER_NO = "";

            if (StringUtils.isBlank(FieldOptionList.getName("ED", "VIRTUAL_BOX_NO", IMG_KIND_VALUE))) {

                BOX_SER_NO = ed_g0z100_mod.qryBoxNO(IMG_KIND_VALUE, DIV_NO, YEAR);

                this.setStatus("readfile");
            } else {
                // ���o�����c��
                BOX_SER_NO = "B" + DIV_NO + FieldOptionList.getName("ED", "VIRTUAL_BOX_NO", IMG_KIND_VALUE);

                this.setStatus("readfilevirtualbox");
            }
            jvm.put("BOX_SER_NO", BOX_SER_NO);

            jvm.put("BOX_SER_NO_SHOW", IMG_KIND_VALUE + ":" + BOX_SER_NO.substring(0, 8) + "-" + BOX_SER_NO.substring(8, 10) + "-"
                    + BOX_SER_NO.substring(10, 15));

            this.putValue("jvl", fileList);

            msg.setMsgDesc("�d�ߧ���");
            //          this.setStatus("readfile");

        } catch (Exception e) {
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
        }
        return resp;
    }

    //------------------------------------------------------------------------------    
    /**
    * �C�L�c�����X
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrintBox(RequestContext req) throws TxException {

        try {
            //Ū��JSP�����Ѽ�
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
    * �C�L�帹���X
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrintBch(RequestContext req) throws TxException {

        try {
            //Ū��JSP�����Ѽ�
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
    * �ʽc�i�� (for Ajax �I�s)
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doClose(RequestContext req) throws TxException {
        try {
            //Ū��JSP�����Ѽ�
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String DIV_NO = req.getParameter("DIV_NO");
            String BOX_SER_NO = req.getParameter("BOX_SER_NO");
            String YEAR = DATE.getDBDate(); //�褸�~��X
            YEAR = YEAR.substring(2, 4);

            try {
                //�����q�O
                Map USER_Map = new ED_H0Z015().getSUB_CPY_ID(user);
                String SUB_CPY_ID = MapUtils.getString(USER_Map, "SUB_CPY_ID");
                //3.2   insert DBED.DTEDG103 (�˽c�J�w�O��)�A��줺�e�p�U�G(960509��)
                ave0_5800_mod.insDTEDG103(IMG_KIND_VALUE, BOX_SER_NO, UserID, user.getEmpName(), DIV_NO, SUB_CPY_ID);

                //              ave0_5800_mod.insEDC0Z001(IMG_KIND_VALUE, BOX_SER_NO, DivNo, UserID, UserName); 

                ave0_5800_mod.insED(IMG_KIND_VALUE, BOX_SER_NO, user);

                BOX_SER_NO = ed_g0z100_mod.getBoxNO(IMG_KIND_VALUE, DIV_NO, YEAR);

                resp.addOutputData("BOX_SER_NO", BOX_SER_NO);

                resp.addOutputData("BOX_SER_NO_SHOW", IMG_KIND_VALUE + ":" + BOX_SER_NO.substring(0, 8) + "-" + BOX_SER_NO.substring(8, 10)
                        + "-" + BOX_SER_NO.substring(10, 15));

                resp.addOutputData("CHK_DATA", "");
                msg.setMsgDesc("�i������");
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
    * �H ���N�X �P  ���y�帹 Ū��DTEDG102 ���o �k�ɧ帹(for Ajax �I�s)
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doGetBCHNO(RequestContext req) throws TxException {

        try {
            //Ū��JSP�����Ѽ�
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
                resp.addOutputData("EXE_MSG", "�d�L���");
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
    * �ˬd��� �bDTAAX020�O�_�s�b
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doCheckRCPT(RequestContext req) throws TxException {
    	StringBuilder sb = new StringBuilder();
        try {
            //Ū��JSP�����Ѽ�
            String DIV_NO = req.getParameter("DIV_NO");
            String BCH_NO = req.getParameter("BCH_NO");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            String BARCODE = "";
            
            try {

                //                if(!"AA02".equals(IMG_KIND_VALUE) && !"AA03".equals(IMG_KIND_VALUE)
                //                        && !"AA04".equals(IMG_KIND_VALUE) && !"AA05".equals(IMG_KIND_VALUE) && !"AT03".equals(IMG_KIND_VALUE)
                //                        && !"AT01".equals(IMG_KIND_VALUE) && !"AT02".equals(IMG_KIND_VALUE) && !"AT04".equals(IMG_KIND_VALUE) && !"AT05".equals(IMG_KIND_VALUE)){
                if (StringUtils.isBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                    //Ū�� FILEREC.DAT
                    File filerecDat = this.getFile(DIV_NO, IMG_KIND_VALUE, BCH_NO, "FILEREC.DAT");

                    //Ū��FILEREC.DAT�ɮפ��e ��ѨC�@�� �HMap �s��bList
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
                            		sb.append(BARCODE).append("�B");
                            	}
                            } else {
                            	try{
                            		ave0_5800_mod.queryDTAAX020_2(IMG_KIND, BARCODE);
                            	}catch(DataNotFoundException dnfe){
                            		sb.append(BARCODE).append("�B");
                            	}
                            }
                        }
                    }
                }
                if(sb.length() > 0){
                	throw new DataNotFoundException("�d�L���");
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
    * �˽c�T�{
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doConfirm(RequestContext req) throws TxException {
        try {

            //Ū��JSP�����Ѽ� 
            String DIV_NO = req.getParameter("DIV_NO");
            String BCH_NO = req.getParameter("BCH_NO");
            String BOX_SER_NO = req.getParameter("BOX_SER_NO");
            String BCHNO_VALUE = req.getParameter("BCHNO_OPT");
            String IMG_KIND_VALUE = req.getParameter("IMG_KIND_OPT");
            // made by Dai You-Chun 1000305 �˽c�T�{�s�W�帹���e�y�z
            String BCH_MEMO = req.getParameter("BCH_MEMO");
            BigDecimal totSize = new BigDecimal("0");
            List fileList = new ArrayList();
            Transaction.begin();

            // ���o�ڸ��|
            String AVE0_IMG_HOME = ConfigManager.getProperty("AVE0_IMG_HOME");

            // ���o�ӭ���|
            File sourcePath = this.getSourcePath(AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCH_NO);

            // ���o FILEREC.DAT �ɮ׸��|
            File filerecDat = new File(sourcePath, "FILEREC.DAT");

            // �ŧi REC_UPDATE.TXT �ɮ׸��|
            File filerecStr = null;
            //            if("AA02".equals(IMG_KIND_VALUE) || "AA03".equals(IMG_KIND_VALUE)
            //                    || "AA04".equals(IMG_KIND_VALUE) || "AA05".equals(IMG_KIND_VALUE) || "AT03".equals(IMG_KIND_VALUE)
            //                    || "AT01".equals(IMG_KIND_VALUE) || "AT02".equals(IMG_KIND_VALUE) || "AT04".equals(IMG_KIND_VALUE) || "AT05".equals(IMG_KIND_VALUE)){
            if (StringUtils.isNotBlank(FieldOptionList.getName("ED", "EDA0_OF_IMGKIND", IMG_KIND_VALUE))) {
                fileList = ave0_5800_mod.queryDTAVE100(IMG_KIND_VALUE, BCH_NO);
            } else {
                //Ū��FILEREC.DAT�ɮפ��e ��ѨC�@�� �HMap �s��bList
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
                                FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                                MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                                    "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
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
                String[] files0 = sourcePath.list(x0); //���o�ŦX���ɦW
                int COUNT_TIF0 = files0 == null ? 0 : files0.length;
                if (COUNT_TIF0 != COUNT_index0) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // �d�߼v���帹����
                Map reqMap = new HashMap();
                reqMap.put("DIV_NO", DIV_NO);
                reqMap.put("IMG_KIND", IMG_KIND_VALUE);
                reqMap.put("BCH_NO", BCHNO_VALUE);
                int FILE_CNT = Integer.parseInt(av_e0z014_mod.queryDTAVE102(reqMap).get("FILE_CNT").toString());
                if (FILE_CNT != COUNT_index0) {
                    boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, "3");

                    if (moveSuccess) {
                        Transaction.commit();
                        FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // ���oREC_UPDATE.TXT�ɮ׸��|
                //                String filerecStr = ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND_VALUE
                //                        + File.separator + BCH_NO + File.separator + "REC_UPDATE.txt";
                filerecStr = new File(sourcePath, "REC_UPDATE.txt");

                //�g�JREC_UPDATE.txt �� Writer
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filerecStr)));

                //�Ȧs�g�JREC_UPDATE��Buffer
                StringBuffer sb = new StringBuffer();

                String prevSET_NO = ""; //�����ո��X��

                //�e���h���ո��ۦP���� �n�g���@��
                for (int i = 0; i < fileList.size(); i++) {

                    Map fileMap = (Map) fileList.get(i);
                    String IMG_KIND = fileMap.get("IMG_KIND").toString();
                    String BARCODE = fileMap.get("BARCODE").toString();
                    String ORG_BARCODE = fileMap.get("BARCODE").toString();

                    totSize = totSize.add(this.getFileSize(DIV_NO, IMG_KIND, BCH_NO, ORG_BARCODE));

                    if (i == 0) {
                        //�Ĥ@��

                        //�Ϥ��O�_���P�@�檺���X�X��
                        prevSET_NO = fileMap.get("SET_NO").toString();

                        //TODO
                        //                      if(!IMG_KIND_VALUE.equals("AB02")){
                        if (StringUtils.isBlank(FieldOptionList.getName("ED", "MULTI_DOC_SAME_BARCODE", IMG_KIND_VALUE))) {
                            String IMG_KEY = "";
                            /*
                             * �ק���:2018-05-22
                             * ���D�渹:20180522-0058
                             * �n�@�I�榬���~�ݭn��sDTAAX020���ک�����
                             * by ���秱
                             */
                            if (StringUtils.isBlank(FieldOptionList.getName("AA", "IMGKIND_NO_RCPT", IMG_KIND))){
                                try {
                                    DTAAX020 dtaax020Bo = ave0_5800_mod.updateDTAAX020(UserID, DivNo, DATE.getDBTimeStamp(), IMG_KIND, BARCODE);
                                    IMG_KEY = dtaax020Bo.getIMG_KEY();
                                    // made by Dai You-Chun 1000526
                                    // �θӳ��SIGN_TIME�L�ȡA
                                    if (STRING.isNull(dtaax020Bo.getSIGN_TIME())) {
                                        ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                                .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                    }
                                } catch (DataNotFoundException dnfe) {
                                    ave0_5800_mod.insertDTAVR003(IMG_KIND, BARCODE, BCH_NO, BOX_SER_NO, fileMap.get("SCAN_DATE_TIME")
                                            .toString(), fileMap.get("SCAN_DIV_NO").toString());
                                }
                            }

                            //spec 4.2�s�W DTAVE100   
                            //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, "");
                                log.error("PCIDSS: insDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc"); 

                            }

                        } else {

                            //���N�X��AB02

                            String IMG_KEY = "";
                            String PRE_KEY = "";
                            try {

                                //�Y�����X�ݦ۰ʥ[�y����������(��X)
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

                            //spec 4.2�s�W DTAVE100   
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, PRE_KEY);
                                log.error("PCIDSS: insDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");                                

                            }

                        }

                        sb = sb.append(Integer.toString(i + 1) + "," + IMG_KIND + "," + DivNo + "," + UserID + ","
                                + fileMap.get("SCAN_DATE_TIME").toString() + "," + fileMap.get("IMG_PAGES").toString() + "," + BARCODE);

                        //�ק�TIF�ɦW
                        if (!ORG_BARCODE.equals(BARCODE)) {
                            this.renameTIF(DIV_NO, IMG_KIND, BCH_NO, ORG_BARCODE, BARCODE);
                        }

                        if (i == (fileList.size() - 1)) {
                            //�̫�@��
                            pw.println(sb.toString());
                            sb.delete(0, (sb.length())); //�M��sb���        
                        }
                        continue;
                    }

                    if (fileMap.get("SET_NO").toString().equals(prevSET_NO)) {
                        //�P�@�ո��n�g�A�P�@��(append���X)
                        sb = sb.append("," + BARCODE);
                    } else {

                        //                      if(!IMG_KIND_VALUE.equals("AB02")){
                        if (StringUtils.isBlank(FieldOptionList.getName("ED", "MULTI_DOC_SAME_BARCODE", IMG_KIND_VALUE))) {
                            String IMG_KEY = "";
                            /*
                             * �ק���:2018-05-22
                             * ���D�渹:20180522-0058
                             * �n�@�I�榬���~�ݭn��sDTAAX020���ک�����
                             * by ���秱
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

                            //spec 4.2�s�W DTAVE100    
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, "");
                                log.error("PCIDSS: insDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            }

                        } else {
                            //���N�X��AB02

                            String IMG_KEY = "";
                            String PRE_KEY = "";

                            try {

                                //�Y�����X�ݦ۰ʥ[�y����������(��X)
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

                            //spec 4.2�s�W DTAVE100   
                          //PCIDSS 2020-11-03
                            try {
                                ave0_5800_mod.insDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY, PRE_KEY);
                                log.error("PCIDSS: insDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            } catch (DataDuplicateException dde) {
                                ave0_5800_mod.updDTAVE100(IMG_KIND_VALUE, BARCODE, DIV_NO, UserID, BCH_NO, BARCODE + ".TIF.enc",
                                    Integer.toString(i + 1), IMG_KEY);
                                log.error("PCIDSS: updDTAVE100 TIF �ɮץ[�K�����ɦW .TIF.enc");

                            }
                            //�ק�TIF�ɦW
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
                        //�̫�@��
                        pw.println(sb.toString());
                        sb.delete(0, (sb.length())); //�M��sb���        
                    }

                    prevSET_NO = fileMap.get("SET_NO").toString();
                }

                //����
                pw.flush();
                pw.close();

                // ���oREC_UPDATE.TXT�ɮ׸��|
                //File rec_updateFile = new File(sourcePath, "REC_UPDATE.TXT");

                // ���o�ӷ����|�U�Ҧ� TIF �ɵ���
                TIF_FileFilter x = new TIF_FileFilter("TIF");
                String[] files = sourcePath.list(x);
                int COUNT_TIF1 = files == null ? 0 : files.length;

                String line = "";
                BufferedReader rd;
                int COUNT_index1 = 0;
                for (rd = new BufferedReader(new FileReader(filerecStr)); StringUtils.isNotBlank((line = rd.readLine()));) {

                    String[] lineArr = STRING.split(line, ",");

                    if (lineArr.length >= 7) {
                        // ���X�s���S��B�z
                        String BARCODE = lineArr[6];
                        if ("AC07".equals(IMG_KIND_VALUE) && BARCODE.length() == 10) {
                            BARCODE = "0" + BARCODE;
                        }

                        // �P�_�ɮ׬O�_�s�b
                        boolean hasFile = this.chkImgFile(DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE, BARCODE);
                        if (!hasFile) {
                            boolean moveSuccess = av_e0z014_mod.doErrorMove(sourcePath, AVE0_IMG_HOME, DIV_NO, IMG_KIND_VALUE, BCHNO_VALUE,
                                "3");

                            if (moveSuccess) {
                                rd.close();
                                Transaction.commit();
                                FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                                MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                                    "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
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
                        FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }

                // �d�߼v���帹����
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
                        FileUtils.cleanDirectory(sourcePath); // �M�Ũӷ����
                        MessageHelper.setReturnMessage(msg, ReturnCode.ERROR,
                            "�ӵ��帹�i��b�W�Ǯɵo�Ϳ��~�A�Х��ܧ帹�R���@�~�e���i��R���A�A���s�ܼv���W�ǧ@�~�e�����s�W���ɮ�(index.dat)�C");
                        this.putValue("jvl", new ArrayList());
                        this.setStatus("query");
                        return resp;
                    }
                }
            }
            //spec 4.4 �n�O�帹�k�ɬ��� DTEDG102
            ave0_5800_mod.updDTEDG102(IMG_KIND_VALUE, BCH_NO, Integer.toString(fileList.size()), DIV_NO, UserID, BOX_SER_NO, UserID,
                totSize.toString(), BCH_MEMO);

            //spec 4.5 ��s�n�����y��B�z����
            ave0_5800_mod.updateDTAVE102(IMG_KIND_VALUE, BCHNO_VALUE, UserID);

            //���b�̫᪺�e���A��Disabled �˽c�T�{���s
            MessageHelper.setReturnMessage(msg, ReturnCode.OK, "�˽c�T�{����");
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
    // �H�U��private Method    
    //------------------------------------------------------------------------------
    /**
    * ���o��lPrompt�ϥΪ�JSP�e����Map
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
     * ���͵e���һݪ��U�Ե����� ���N�X, �}��~��
     */
    private void setOpt() throws Exception {

        //���N�X SelectOpt
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
     * ���͵e���һݪ��U�Ե����� ���y�帹
     */
    private void setBCHNOOpt(String DIV_NO, String IMG_KIND_VALUE, String BCH_STS, String OPR_ID) throws Exception {

        SelectOptUtil BCHNO_OPT = new SelectOptUtil();

        //�k�ɧ帹 SelectOpt
        List bchnoList;

        bchnoList = ave0_5800_mod.queryDTAVE102(DIV_NO, IMG_KIND_VALUE, BCH_STS, OPR_ID);
        boolean IS_CHECK_IMG_KIND = StringUtils.isNotBlank(FieldOptionList.getName("AV", "IS_CHECK_IMG_KIND", IMG_KIND_VALUE));
        /*
                   �ק���:2018-11-05
                  �߮׳渹180809000841 �|�p�ѥX�Ǫѵ��M�����I���t�νվ�@�~ 
       by ���秱
                  ���X�Ǫѵ��M,�Y��DJ01 �Y����O�����x�����帹������
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
                throw new DataNotFoundException("�d�L�k�ɧ帹");
            }
        }else{//�DDJ01
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
    * ���o �v���ɸ��| �U�� �ɮ�
    * �]�w�� �b AV_SRC\\usr\\cxlcs\\config\\customconfig\\fileStore.prop
    */
    private File getFile(String DIV_NO, String IMG_KIND, String SCANNO, String FILE_NAME) throws ModuleException {

        File path = new File(ConfigManager.getProperty("AVE0_IMG_HOME") + File.separator + DIV_NO + File.separator + IMG_KIND
                + File.separator + SCANNO + File.separator + FILE_NAME);

        return path;

    }

    //------------------------------------------------------------------------------    
    /*
     * �ɮפ��e ��� �� List �s��
     */
    private List fileToList(File filerecDat, String BCH_NO) throws FileNotFoundException, IOException {

        //Ū���ɮפ��e ��ѨC�@�� �HMap �s��bList
        List fileList = new ArrayList();
        String line = "";
        BufferedReader rd;
        int row_count = 1;
        for (rd = new BufferedReader(new FileReader(filerecDat)); StringUtils.isNotBlank((line = rd.readLine()));) {

            String[] lineArr = STRING.split(line, ",");

            //�p�G�P�@����X���h�� �ݭn��Ѧh��
            if (lineArr.length >= 6) {
                //�@�����X
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //���N�X
                outMap.put("SCAN_DIV_NO", lineArr[1]); //���y���N��
                outMap.put("SCAN_OPR_ID", lineArr[2]); //���y�@�~�H��
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //���y����P�ɶ�
                outMap.put("IMG_PAGES", lineArr[4]); //�v������                  
                outMap.put("BARCODE", lineArr[5]); //���X�s��
                outMap.put("SET_NO", Integer.toString(row_count)); //�P�O�βո�
                outMap.put("SEQ_NO", "1"); //�P�O�ΧǸ�
                outMap.put("COUNT_NO", "1"); //�P�O�� ���X�����X
                outMap.put("BCH_NO", BCH_NO);

                fileList.add(outMap);
            }
            if (lineArr.length >= 7) {
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //���N�X
                outMap.put("SCAN_DIV_NO", lineArr[1]); //���y���N��
                outMap.put("SCAN_OPR_ID", lineArr[2]); //���y�@�~�H��
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //���y����P�ɶ�
                outMap.put("IMG_PAGES", lineArr[4]); //�v������                  
                outMap.put("BARCODE", lineArr[6]); //���X�s��
                outMap.put("SET_NO", Integer.toString(row_count)); //�P�O�βո�
                outMap.put("SEQ_NO", "2"); //�P�O�ΧǸ�
                outMap.put("COUNT_NO", "2"); //�P�O�� ���X�����X
                outMap.put("BCH_NO", BCH_NO);
                fileList.add(outMap);

            }
            if (lineArr.length >= 8) {
                Map outMap = new TreeMap();
                outMap.put("IMG_KIND", lineArr[0]); //���N�X
                outMap.put("SCAN_DIV_NO", lineArr[1]); //���y���N��
                outMap.put("SCAN_OPR_ID", lineArr[2]); //���y�@�~�H��
                outMap.put("SCAN_DATE_TIME", lineArr[3]); //���y����P�ɶ�
                outMap.put("IMG_PAGES", lineArr[4]); //�v������                  
                outMap.put("BARCODE", lineArr[7]); //���X�s��
                outMap.put("SET_NO", Integer.toString(row_count)); //�P�O�βո�
                outMap.put("SEQ_NO", "3"); //�P�O�ΧǸ�
                outMap.put("COUNT_NO", "3"); //�P�O�� ���X�����X
                outMap.put("BCH_NO", BCH_NO);

                fileList.add(outMap);
            }
            row_count++;
        }
        rd.close(); //����Ū��
        return fileList;
    }

    //------------------------------------------------------------------------------    
    /*
     * ����e�����s
     */
    private void setStatus(String stsCode) {
        // name ���ݩM�����W�� element name �@��

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
     * �ק�TIF�ɦW
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

    //------------------------------------------------------------------------------
    /**
     *  �ˮ� �v���ɬO�_�s�b
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
        //�Ӹ�P�N�Ѧ]�����X���D�A�䤣����N���X�Ĥ@�X�h���A����A�����h��W�ɤW�Ĥ@�X0
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
     * ���o �v���s����|
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
