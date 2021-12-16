package com.ck.av.e0.trx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ck.av.e0.bo.AV_E0Z004_bo;
import com.ck.av.e0.module.AVE0_1680_mod;
import com.ck.av.e0.module.AV_E0Z004;
import com.ck.av.e0.module.AV_E0Z008;
import com.ck.av.e0.module.AV_E0Z009;
import com.ck.common.bo.ReturnMessage;
import com.ck.common.exception.DataNotFoundException;
import com.ck.common.exception.ErrorInputException;
import com.ck.common.exception.ModuleException;
import com.ck.common.hr.PersonnelData;
import com.ck.common.im.util.VOTool;
import com.ck.common.message.MessageHelper;
import com.ck.common.service.ConfigManager;
import com.ck.common.service.authenticate.UserObject;
import com.ck.common.trx.UCBean;
import com.ck.common.util.FieldOptionList;
import com.ck.common.util.IConstantMap;
import com.ck.common.util.PDFTools;
import com.ck.common.util.STRING;
import com.ck.common.util.TIFTools;
import com.ck.common.util.PDFTools.WatermarkAttr;
import com.ck.common.util.TIFTools.TIFAttr;
import com.ck.common.util.page.SelectOptUtil;
import com.ck.crypto.ext.CryptoFactory;
import com.ck.crypto.ext.CryptoService;
import com.ck.ed.b0.bo.ED_B0Z001_bo;
import com.ck.ed.b0.module.ED_B0Z001;
import com.ck.rpt.RptUtils;
import com.ck.util.ReturnCode;
import com.ck.util.Transaction;
import com.igsapp.common.trx.ServiceException;
import com.igsapp.common.trx.TxException;
import com.igsapp.wibc.dataobj.Context.RequestContext;
import com.igsapp.wibc.dataobj.Context.ResponseContext;
import com.igsapp.wibc.dataobj.html.HttpRequestData;
import com.lowagie.text.DocumentException;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;

/**
    AVE01680 �v���վ\

        �@.  �{���\�෧�n����:
        1.  �{���W��:UCAVE0_1680
        2.  �@�~�覡:ONLINE
        3.  ���n����:
        3.1 �ϥγ��G�U���C
        3.2 �ϥΥت��G�վ\�w�k�ɤ��ɮסC
        
    �G.  �ϥμҲ�
        1. AV_E0Z004

    AVE0_1680.java
    @author kevin Chen
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AVE0_1680 extends UCBean {

    private static final String CLASS_NAME = AVE0_1680.class.getName();

    private static final Logger log = Logger.getLogger(CLASS_NAME);

    private ResponseContext resp; //��TxBean�{���X�@�Ϊ� ResponseContext

    private ReturnMessage msg; //��TxBean�{���X�@�Ϊ� ReturnMessage

    private UserObject user; //��TxBean�{���X�@�Ϊ� UserObject

    private AVE0_1680_mod ave0_1680_mod; //�� TxBean �{���X�@�Ϊ� Module

    private AV_E0Z004 ave0z004_mod; //�� TxBean �{���X�@�Ϊ� Module

    private ED_B0Z001 ed_b0z001; //�� TxBean �{���X�@�Ϊ� Module

    /** 
    * �мg�����O��start()�H�j��C��Dispatcher�I�smethod������{���۩w����l�ʧ@ 
    */
    public ResponseContext start(RequestContext req) throws TxException, ServiceException {
        super.start(req); // �@�w�n invoke super.start() �H�����v�����ˮ� 
        initApp(req); // �I�s�۩w����l�ʧ@
        return null;
    }

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

        //��l�Ʀ@�μҲ�
        ave0_1680_mod = new AVE0_1680_mod();
        ave0z004_mod = new AV_E0Z004();
        ed_b0z001 = new ED_B0Z001();
        
        //�]�w�ӳ��O�_�ϥ�JNLP�w��TIFF
        try {
        	//����:�w�p2019-03-08 �����q�אּ��OpenJDK�}�Ҽv��(�O�N���y�g��έ쥻�覡�վ\)
            //String isUsingJNLP = FieldOptionList.getName("AV", "AVE0_JNLP_TEST_DIV" , user.getUserDivNo());
            //resp.addOutputData("isUsingJNLP", StringUtils.isNotBlank(isUsingJNLP));
        	boolean isEditable = this.checkEditable(user.getRoles());//�ˬd�O�_��RLAV100�O�N���y�g��
        	
        	if(isEditable){//�O�N���y�g��έ쥻�覡�վ\
        		String IS_RLAV100 = FieldOptionList.getName("AV", "IS_RLAV100_OPEN_JDK", "RLAV100");//�Ȧ�O�N��OpenJDK
        		if(StringUtils.isNotBlank(IS_RLAV100)){//�Ȧ�O�N��OpenJDK
        			resp.addOutputData("isUsingJNLP", true);
        		}else{
        			resp.addOutputData("isUsingJNLP", false);
        		}
        	}else{//��س���OpenJDK
        		resp.addOutputData("isUsingJNLP", true);
        	}

        } catch (Exception e) {
        	log.debug("",e);
        }
    }

    /**
    * ��l�ƭ���
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doPrompt(RequestContext req) throws TxException {
    	
        try {

            this.setOpt();

            Map jvm = this.getPromptMap();

            this.putValue("jvm", jvm);
            this.putValue("jvl", new ArrayList());
        } catch (Exception e) {
            throw new TxException(e.getMessage());
        }
        
        return resp;
    }

    /**
    * �d��
    * 
    * �~���s���i�Ӫ��d�� �жǤJ�U�C�Ѽ�
    * LINK_TYPE = "Y"(�O�_���~���s��)
    * IMG_KIND_OPT  (������)
    * RCPT_NO       (��ڧǸ�)
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doQuery(RequestContext req) throws TxException {
        try {

            //�O�_����L���� �s���i�� ���d�� (Y:�~���s��, N:����AVE0_1680�{���d��)
            String LINK_TYPE = STRING.objToStrNoNull(req.getParameter("LINK_TYPE"));

            //�d�����
            String IMG_KIND_OPT = req.getParameter("IMG_KIND_OPT");
            String RCPT_NO = req.getParameter("RCPT_NO");

            //�d�߲M�� Hyper Link 
            String IMG_KIND_LINK = STRING.objToStrNoNull(req.getParameter("IMG_KIND_LINK"));
            String RCPT_NO_LINK = STRING.objToStrNoNull(req.getParameter("RCPT_NO_LINK"));
            String IMG_KEY_LINK = STRING.objToStrNoNull(req.getParameter("IMG_KEY_LINK"));
            String DATA_KIND_LINK = STRING.objToStrNoNull(req.getParameter("DATA_KIND_LINK"));

            //�d�ߥi�ର query_btn �� �d�߲M�� Hyper Link, �d�߭n�Τ��PKey�h�d
            List jvl = new ArrayList();
            List jvl_t = new ArrayList();
            String queryRCPT_NO = "";
            String queryIMG_KIND = "";
            String queryIMG_KEY = "";
            if (!"".equals(RCPT_NO_LINK)) {
                queryRCPT_NO = RCPT_NO_LINK;
                queryIMG_KIND = IMG_KIND_LINK;
                queryIMG_KEY = IMG_KEY_LINK;
            } else {
                queryRCPT_NO = RCPT_NO;
                queryIMG_KIND = IMG_KIND_OPT;
            }

            //AB02 ���i�ର�h��,��L���浧, ���ͬd�߲M��
            Map IMGKIND_QUERY_MUTI_LENGTH_Map = FieldOptionList.getName("AV", "IMGKIND_QUERY_MUTI_LENGTH");

            //�O�_���¼v�����ɤ��N�X
            boolean IS_OLD_AV = StringUtils.isNotBlank(FieldOptionList.getName("AV", "OLD_AV", queryIMG_KIND));
            log.error("IS_OLD_AV: "+IS_OLD_AV);
            //if("AB02".equals(IMG_KIND_OPT)||("AT03".equals(IMG_KIND_OPT)&&queryRCPT_NO.length()>=10)){
            if (IMGKIND_QUERY_MUTI_LENGTH_Map.containsKey(IMG_KIND_OPT)
                    && (queryRCPT_NO.length() >= Integer.parseInt(IMGKIND_QUERY_MUTI_LENGTH_Map.get(IMG_KIND_OPT).toString()))) {

                //�h��
                try {
                    jvl = ave0_1680_mod.queryDTAVE100(IMG_KIND_OPT, RCPT_NO);
                    Map tmpMap = (Map) jvl.get(0);

                    if (!RCPT_NO_LINK.equals("")) {
                        //�p�G �O��Hyper Link �I�L�Ӫ� ������M��Ĥ@���A�אּ Hyper Link ���w
                        queryRCPT_NO = RCPT_NO_LINK;
                        queryIMG_KIND = IMG_KIND_LINK;
                    } else {
                        //���Ĥ@�����G ��@ �e���U�� �d�ߵ��G�� �d��Key��       
                        queryRCPT_NO = tmpMap.get("RCPT_NO").toString();
                    }
                    if (StringUtils.isBlank(DATA_KIND_LINK)) {
                        DATA_KIND_LINK = "E100";
                    }
                } catch (DataNotFoundException dnfe) {
                    log.debug("�d�L��Ƶ������`", dnfe);
                }

                //AT10�BAT11�����ɸ�ƻݭn�μҽk�d��
                if (IS_OLD_AV) {
                    try {
                        List E104_LIST = new AV_E0Z009().queryDTAVE104(IMG_KIND_OPT, RCPT_NO);
                        for (int i = 0; i < E104_LIST.size(); i++) {
                            Map dataMap = (Map) E104_LIST.get(i);
                            dataMap.put("DATA_KIND", "E104");
                        }
                        jvl.addAll(E104_LIST);
                        if (!"E100".equals(DATA_KIND_LINK)) {
                            DATA_KIND_LINK = "E104";

                            if (!RCPT_NO_LINK.equals("")) {
                                //�p�G �O��Hyper Link �I�L�Ӫ� ������M��Ĥ@���A�אּ Hyper Link ���w
                                queryRCPT_NO = RCPT_NO_LINK;
                                queryIMG_KIND = IMG_KIND_LINK;
                            } else {
                                //���Ĥ@�����G ��@ �e���U�� �d�ߵ��G�� �d��Key��       
                                queryRCPT_NO = MapUtils.getString(((Map) E104_LIST.get(0)), "RCPT_NO", "");
                            }
                        }
                    } catch (DataNotFoundException ex) {
                        //It's OK
                        log.debug("�d�L��Ƶ������`", ex);
                    }
                }

            } else if (IS_OLD_AV) {
                try {
                    jvl = ave0_1680_mod.queryDTAVE104(IMG_KIND_OPT, RCPT_NO);
                    for (int i = 0; i < jvl.size(); i++) {
                        Map dataMap = (Map) jvl.get(i);
                        dataMap.put("DATA_KIND", "E104");
                    }
                    Map tmpMap = (Map) jvl.get(0);

                    if (!RCPT_NO_LINK.equals("")) {
                        //�p�G �O��Hyper Link �I�L�Ӫ� ������M��Ĥ@���A�אּ Hyper Link ���w
                        queryRCPT_NO = RCPT_NO_LINK;
                        queryIMG_KIND = IMG_KIND_LINK;
                    } else {
                        //���Ĥ@�����G ��@ �e���U�� �d�ߵ��G�� �d��Key��       
                        queryRCPT_NO = tmpMap.get("RCPT_NO").toString();
                    }

                } catch (DataNotFoundException dnfe) {
                    log.debug("�d�L��Ƶ������`", dnfe);
                }

                try {
                    //2014-12-24 update: AB08�BAB09������]�����@��妸���y�y�{�A�ҥH�ٻݭn��DTAVE100����ơC
                    //2015-12-31 update: AT10�BAT11�s�󨫾�屽�y�C
                    if ("AB08".equals(IMG_KIND_OPT) || "AB09".equals(IMG_KIND_OPT) || "AT10".equals(IMG_KIND_OPT)
                            || "AT11".equals(IMG_KIND_OPT)) {
                        List<Map> DTAVE100_LIST = new AV_E0Z009().queryDTAVE100_2(IMG_KIND_OPT, RCPT_NO);
                        for (Map dataMap : DTAVE100_LIST) {
                            dataMap.put("DATA_KIND", "E100");
                        }
                        if (jvl.size() == 0) {
                            if (StringUtils.isBlank(RCPT_NO_LINK)) {
                                queryRCPT_NO = DTAVE100_LIST.get(0).get("RCPT_NO").toString();
                                DATA_KIND_LINK = "E100";
                            }
                        }
                        jvl.addAll(DTAVE100_LIST);
                    }
                } catch (DataNotFoundException dnfe) {
                    log.debug("�d�L��Ƶ������`", dnfe);
                }

            } else {
                //�浧
                Map dataMap = new TreeMap();
                dataMap.put("RCPT_NO", RCPT_NO);
                dataMap.put("IMG_KIND", IMG_KIND_OPT);
                jvl.add(dataMap);
            }
            if ("AC02".equals(IMG_KIND_OPT) || "AC03".equals(IMG_KIND_OPT)) {
                try {
                    jvl_t = ave0_1680_mod.queryDTAVE100("AC07", RCPT_NO);
                    jvl.addAll(jvl_t);
                } catch (DataNotFoundException dnfe) {//�L�Ӹ�i���P�N�ѵ������`
                    log.debug("�L�Ӹ�i���P�N�ѵ������`", dnfe);
                }
            }

            this.putValue("jvl", jvl);

            this.setOpt();

            try {
                if ("AT03".equals(IMG_KIND_OPT) && queryRCPT_NO.length() < 10) {
                    throw new ModuleException("���ڸ��X���פ���10�X�A�нT�{");
                }

                AV_E0Z004_bo outBo = new AV_E0Z004_bo();

                if (IS_OLD_AV) {
                    try {
                        log.error("queryRCPT_NO: "+queryRCPT_NO);
                        if ("E100".equals(DATA_KIND_LINK)) {
                            log.error("*** Call ave0z004_mod.queryImgPath ***");
                            outBo = ave0z004_mod.queryImgPath(queryIMG_KIND, queryRCPT_NO);
                        } else {
                            log.error("*** Call ave0_1680_mod.queryImgPath4OLD ***");
                            log.error("queryIMG_KEY: "+queryIMG_KEY);
                            outBo = ave0_1680_mod.queryImgPath4OLD(queryIMG_KIND, queryRCPT_NO, queryIMG_KEY);
                        }
                    } catch (DataNotFoundException dnfe) {
                        //2014-12-24 update: AB08�BAB09
                        //2015-12-31 update: AT10�BAT11
                        log.error("*** DataNotFoundException ***");
                        if ("AB08".equals(queryIMG_KIND) || "AB09".equals(queryIMG_KIND) || "AT10".equals(IMG_KIND_OPT)
                                || "AT11".equals(IMG_KIND_OPT)) {
                            log.error("*** Call ave0z004_mod.queryImgPath ***");
                            outBo = ave0z004_mod.queryImgPath(queryIMG_KIND, queryRCPT_NO);
                        }
                    }
                } else {
                    log.error("*** Call ave0z004_mod.queryImgPath ***");
                    outBo = ave0z004_mod.queryImgPath(queryIMG_KIND, queryRCPT_NO);
                }
                log.error("******** outBo.getImgPath: "+outBo.getImgPath());
                Map jvm = new TreeMap();
                jvm.put("LINK_TYPE", LINK_TYPE);
                jvm.put("IMG_KIND_VALUE", IMG_KIND_OPT);
                jvm.put("RCPT_NO", RCPT_NO);
                jvm.put("IMG_KIND_LINK", queryIMG_KIND);
                jvm.put("RCPT_NO_LINK", queryRCPT_NO);
                jvm.put("PROCDIVNO", outBo.getProcDivno());
                jvm.put("FILEDATE", outBo.getFileDate());
                jvm.put("FILEOPERATOR", outBo.getFileOperator());
                jvm.put("BCHNO", outBo.getBchNo());
                jvm.put("IMGFILENAME", outBo.getImgFileName());
                jvm.put("MEMOTEXT", outBo.getMemoText());
                jvm.put("DVDNO", outBo.getDvdNo());
                jvm.put("BOXDATE", outBo.getBoxDate());
                jvm.put("BOXOPERATOR", outBo.getBoxOperator());
                jvm.put("BOXNO", outBo.getBoxNo());

                jvm.put("OPUNIT", user.getOpUnit());//�ާ@���N��
                jvm.put("EMPID", user.getEmpID());//�ާ@�H��ID
                jvm.put("EMPNAME", user.getEmpName());//�ާ@�H���m�W
                //PCIDSS: pdf.enc �P�_               
                
                //�P�_���ɦW�榡�O�_��PDF��
                log.error("IS_PDF: "+outBo.getImgFileName().toUpperCase().contains(".PDF"));
                jvm.put("IS_PDF", outBo.getImgFileName().toUpperCase().contains(".PDF") ? "Y" : "N");
                
                jvm.put("ENABLE_DOWNLOAD", StringUtils.isNotBlank(FieldOptionList.getName("AV", "IMGKIND_WO_DOWNLOAD", IMG_KIND_OPT)) ? "N"
                        : "Y");

                jvm.put("IMGPATH", outBo.getImgPath());

                if (IS_OLD_AV) {
                    jvm.put("IMG_KEY", outBo.getReturnCode());
                    jvm.put("IS_OLD", "Y");
                }

                this.putValue("jvm", jvm);

                msg.setMsgDesc("�d�ߧ���");
            } catch (ModuleException me) {
                Map jvm = this.getPromptMap();
                jvm.put("LINK_TYPE", LINK_TYPE);
                jvm.put("IMG_KIND_VALUE", IMG_KIND_OPT);
                jvm.put("RCPT_NO", RCPT_NO);
                jvm.put("OPUNIT", user.getOpUnit());//�ާ@���N��
                jvm.put("EMPID", user.getEmpID());//�ާ@�H��ID
                jvm.put("EMPNAME", user.getEmpName());//�ާ@�H���m�W

                this.putValue("jvm", jvm);
                this.putValue("jvl", new ArrayList());
                msg.setMsgDesc(me.getMessage());
                msg.setReturnCode(ReturnCode.ERROR);
                log.fatal("(2)." + me.getMessage(), me);
            }

        } catch (Exception e) {
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
            log.fatal("(1)." + e.getMessage(), e);
        }
        return resp;
    }

    /**
     * �H TIF Viewer �}�ҳ���
     * @param req
     * @return
     */
    public ResponseContext doShowTIF(RequestContext req) {
        return doShowPage(req);
    }

    /**
     * �H PDF Viewer �}�ҳ���
     * @param req
     * @return
     */
    public ResponseContext doShowPDF(RequestContext req) {
        return doShowPage(req);
    }

    /**
     * ����}�ҳ���
     * @param req
     * @return
     */
    private ResponseContext doShowPage(RequestContext req) {

        try {
            String IMGFILENAME = STRING.objToStrNoNull(req.getParameter("IMGFILENAME"));
            String IMGPATH = STRING.objToStrNoNull(req.getParameter("IMGPATH"));
            String OPUNIT = STRING.objToStrNoNull(req.getParameter("OPUNIT"));
            String EMPID = STRING.objToStrNoNull(req.getParameter("EMPID"));
            String EMPNAME = STRING.objToStrNoNull(req.getParameter("EMPNAME"));
            String IMG_KIND = "", RCPT_NO = ""; //, ENABLE_DOWNLOAD = "";
            String IMG_KEY = STRING.objToStrNoNull(req.getParameter("IMG_KEY"));
            String IS_OLD = STRING.objToStrNoNull(req.getParameter("IS_OLD"));
            String IMG_KIND_LINK = STRING.objToStrNoNull(req.getParameter("IMG_KIND_LINK"));
            String RCPT_NO_LINK = STRING.objToStrNoNull(req.getParameter("RCPT_NO_LINK"));
            //2018-09-27 �P�_�O�_���U���v��
            String isDownload = STRING.objToStrNoNull(req.getParameter("isDownload"));
            log.error("IMGFILENAME: "+IMGFILENAME);
            log.error("IMGPATH: "+IMGPATH);
            String[] arr_IMG_KIND = IMGPATH.split("/");
            if (arr_IMG_KIND.length > 4) {
                //                IMG_KIND = arr_IMG_KIND[arr_IMG_KIND.length - 3].substring(0, 4);
                IMG_KIND = IMG_KIND_LINK;
            }

            try {
                RCPT_NO = IMGFILENAME.substring(0, IMGFILENAME.indexOf('.'));
            } catch (Exception e) {
                RCPT_NO = "";
            }

            if ("Y".equals(IS_OLD)) {
                IMG_KIND = IMG_KIND_LINK;
                RCPT_NO = RCPT_NO_LINK;
            }
            Map<String, String> userMap = new HashMap<String, String>();
            userMap.put("OPUNIT", OPUNIT);//�ާ@���N��
            userMap.put("EMPID", EMPID);//�ާ@�H��ID
            userMap.put("EMPNAME", EMPNAME);//�ާ@�H���m�W
            userMap.put("IMG_KEY", IMG_KEY);

            Transaction.begin();
            try {

                //2018-09-27 �d�s�d��LOG�ACall�v���վ\�O���ɲ��ʺ��@�Ҳ�.�s�W�v���վ\�O����k
                new AV_E0Z008().insert(IMG_KIND, RCPT_NO, "T".equals(isDownload) ? "2" : "1", userMap, IS_OLD);

                Transaction.commit();

            } catch (Exception e) {
                Transaction.rollback();
                throw e;
            }

            String IS_PDF = req.getParameter("IS_PDF");
            log.error("IS_PDF: "+IS_PDF);
            //            IMGPATH = "D:/temp/saveFile/" + IMGFILENAME;

            Map map = FieldOptionList.getName("AV", "CXLSVR_IP");
            Map REPLACE_URL_MAP = FieldOptionList.getName("AV", "CXLSVR_IP_TO_NAS");
            Map REPLACE_TYPE_MAP = FieldOptionList.getName("AV", "AVE0_B019");
            log.error("REPLACE_URL_MAP: "+REPLACE_URL_MAP);
            log.error("REPLACE_TYPE_MAP: "+REPLACE_TYPE_MAP);
            boolean isContains = false; //IMGPATH���O�_�]�tIP

            for (String key : (Set<String>) map.keySet()) {
                if (IMGPATH.indexOf(MapUtils.getString(map, key)) >= 0) {
                    isContains = true;
                    break;
                }
            }
            Map param = new HashMap();
            if ("Y".equals(IS_PDF)) {

                if (isContains) {
                    for (String key : (Set<String>) REPLACE_URL_MAP.keySet()) {
                        if (IMGPATH.indexOf(key) >= 0) {
                            IMGPATH = IMGPATH.replace(key, MapUtils.getString(REPLACE_URL_MAP, key));
                            break;
                        }
                    }
                }
                log.error("IMGPATH: "+IMGPATH);
                // �}�� PDF
                StringBuilder sb = new StringBuilder();
                //commons.properties �]�w
                String MARKED_FILE_PATH = sb.append(ConfigManager.getProperty("com.ck.util.jasper.JasperUtils.jasperReportSaveRoot"))
                        .append(IMGFILENAME).toString();
                sb.setLength(0);
                log.error("MARKED_FILE_PATH: "+MARKED_FILE_PATH);
                String EMAIL = "";
                try {
                    EMAIL = new PersonnelData().getByEmployeeID(user.getEmpID(), true).getEmail();
                } catch (Exception e) {
                    log.fatal("���oEmployee����", e);

                }
                if (StringUtils.isBlank(EMAIL)) {
                    EMAIL = user.getEmpID();
                }
                //PCIDSS
                Map<String, String> parameters = new HashMap<String, String>();
                if(IMGFILENAME.contains(".enc")) {
                    //PCIDSS ���ѱK, �A�[�B���L
                    log.error("************ pdf.enc �[�K ************");
                    CryptoService cryptoSvc = CryptoFactory.defaultCryptoService();
                    //IMGFILENAME: I000077887.pdf.enc --> I000077887.pdf
                    IMGFILENAME = IMGFILENAME.substring(0,IMGFILENAME.length()-4);
                    //MARKED_FILE_PATH: /home/jasperreport/I000077887.pdf.enc --> /home/jasperreport/I000077887.pdf
                    MARKED_FILE_PATH = MARKED_FILE_PATH.substring(0,MARKED_FILE_PATH.length()-4);
                    log.error("MARKED_FILE_PATH: "+MARKED_FILE_PATH);
                    //�ѱK�ɦW UUID ex: /home/jasperreport/I000077887.pdf.enc --> /home/jasperreport/I000077887_uuid.pdf
                    //���h��.pdf, �A�[�W_uuid, �b�[�^.pdf
                    String DECRYPT_FILE_PATH= MARKED_FILE_PATH.substring(0,MARKED_FILE_PATH.length()-4);
                    UUID uuid = UUID.randomUUID();
                    DECRYPT_FILE_PATH = sb.append(DECRYPT_FILE_PATH).append("_").append(uuid).append(".pdf").toString();
                    log.error("DECRYPT_FILE_PATH: "+DECRYPT_FILE_PATH);
                    File Decrypt_File = new File(DECRYPT_FILE_PATH);
                    cryptoSvc.decryptFile(new File(IMGPATH), Decrypt_File);
                    //�A�θѱK�ɥ[�B���L
                    createMarkedPDF(DECRYPT_FILE_PATH, MARKED_FILE_PATH, EMAIL);
                    //�R���ѱK��                    
                    try {
                        boolean result = Files.deleteIfExists(Decrypt_File.toPath());
                        if (result) {
                            log.error("Decrypt_File is deleted");
                        } else {
                            log.error("Decrypt_File does not exists");
                        }
                    } catch (IOException e) {
                        log.error("", e);
                    }
                    //PCIDSS: AppletServlet �P�_
                    //IMGPATH: /home/bpmusr/images/BSIMG/CSRAV/AB02/AB02MI0001/HMI020201200001/I000077887.pdf.enc --> /home/bpmusr/images/BSIMG/CSRAV/AB02/AB02MI0001/HMI020201200001/I000077887.pdf
                    IMGPATH = IMGPATH.substring(0,IMGPATH.length()-4);
                    //MARKED_FILE_PATH: /home/jasperreport/I000077681.pdf
                    log.error("MARKED_FILE_PATH: "+MARKED_FILE_PATH);
                    //param.put("fileFullPath", IMGPATH);
                    param.put("fileFullPath", MARKED_FILE_PATH);
                    param.put("forceBy", "PDFViewer");
                    String fileFullPathJSON = VOTool.toJSON(param);
                    parameters.put("fileFullPath", fileFullPathJSON);
                    
                }else {
                    log.error("************ pdf �¥� �D �[�K ************");
                    createMarkedPDF(IMGPATH, MARKED_FILE_PATH, EMAIL);
                    parameters.put("fileFullPath", IMGPATH);
                }                   
                
                log.error("IMGFILENAME: "+IMGFILENAME);
                parameters.put("downloadFileName", IMGFILENAME.substring(0, IMGFILENAME.lastIndexOf('.')));                
                parameters.put("markedFileName", IMGFILENAME);
                parameters.put("markedFileFullPath", MARKED_FILE_PATH);

                RptUtils.cryptoSecurityPrintParameToResp(parameters, resp);
                log.error("IMGPATH: "+IMGPATH);
                resp.addOutputData("fileFullPath", IMGPATH);

            } else {

                String replacePath = "";

                //�P�_�O�_��FTP�U�����|�A�Y�O�h���o��۹���|
                boolean IS_MERGE_IMG = false; //�O�_�����ɤJ�ҥ�v���X�֪�������
                if (REPLACE_TYPE_MAP.containsKey(IMG_KIND)) {
                    IS_MERGE_IMG = true;
                    for (String key : (Set<String>) REPLACE_URL_MAP.keySet()) {
                        if (IMGPATH.indexOf(key) >= 0) {
                            replacePath = IMGPATH.replace(key, MapUtils.getString(REPLACE_URL_MAP, key));
                            break;
                        }
                    }
                }

                String FileName = IMGFILENAME.substring(0, IMGFILENAME.lastIndexOf('.'));

                // �}�� TIF 
                Map<String, String> parameters = new HashMap<String, String>();
                //pcidss 
                //Decrypt_Server_IP �B�z--> ��cxlsvr67, cxlsvr69 �Ҵ���Decrypt_Server_HostName
                String Decrypt_Server_HostName = FieldOptionList.getName("AV", "IMAGE_READ_SERVER", "DecryptServer");
                log.error("*** Decrypt_Server_HostName: "+Decrypt_Server_HostName);
                if (isContains) {                    
                    //http --> https
                    IMGPATH = "https:" + IMGPATH;
                    log.error("IMGPATH: "+IMGPATH);
                    log.error("replacePath: "+replacePath);
                     
                    
                    for (String CxlSvr_IP : (Set<String>) REPLACE_URL_MAP.keySet()) {
                        
                        log.error("*** �쥻CxlSvr_IP: "+CxlSvr_IP);
                        if (IMGPATH.indexOf(CxlSvr_IP) >= 0) {
                            IMGPATH = IMGPATH.replace(CxlSvr_IP, Decrypt_Server_HostName);
                            break; 
                        }
                    }
                    log.error("***** After replace IMGPATH with Decrypt_Server_HostName: "+IMGPATH);
                    log.error("IS_MERGE_IMG: "+IS_MERGE_IMG);
                    if (IS_MERGE_IMG) {
                        //PCIDSS 2020-11-03 ���|��ip, ����nas67, nas69 
                        //File file = new File(replacePath.substring(1));
                                               
                        File file = new File(IMGPATH);
                        //RenderedOp op = JAI.create("fileload", file.getAbsolutePath());
                        //log.error("AbsolutePath: "+file.getAbsolutePath());
                        URL url = new URL(IMGPATH);
                        RenderedOp op = JAI.create("url", url);
                        log.error("op: "+op);                     
                        int compression = getCompression(op);
                        log.error("compression: "+compression);
                        //�Y���Y�榡����CCIT T6 �άO PickBits�h�ഫ�����Y�榡
                        if (TIFFEncodeParam.COMPRESSION_GROUP4 != compression && TIFFEncodeParam.COMPRESSION_PACKBITS != compression) {
                            log.error("in if Case: compression: "+compression);                            
                            File outFile = RptUtils.createTempFile("temp2.tif");
                            TIFAttr tifAttr = new TIFAttr();
                            tifAttr.setDPI(300);
                            tifAttr.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);

                            TIFTools.transTIFCompression(file, outFile, tifAttr);

                            FileUtils.copyFile(outFile, file);

                            outFile.delete();
                        }
                    }

                } else {
                    log.error(" TIF IMGPATH (���tIP): "+IMGPATH);                        
                    //PCIDSS
                    File inFile;
                    RenderedOp op;
                    if (IMGPATH.contains(".enc")) {
                        log.error(" TIF IMGPATH (�D�ȥ��W�ǥ[�KCase): " + IMGPATH);
                        //http --> https
                        IMGPATH = "https://" + IMGPATH;
                        //SA: ���v���ɱN /home/bpmusr/images/BSIMG ���N�� image-read server ����}
                        String ReplacePath = FieldOptionList.getName("AV", "IMAGE_READ_SERVER", "ReplacePath");
                        log.error("*** �D�ȥ��W�ǥ[�KCase ReplacePath: " + ReplacePath);
                        IMGPATH = IMGPATH.replace(ReplacePath, Decrypt_Server_HostName);
                        log.error("***** After replace �D�ȥ��W�ǥ[�KCase with Decrypt_Server_HostName: " + IMGPATH);
                        inFile = new File(IMGPATH);
                        URL url = new URL(IMGPATH);
                        op = JAI.create("url", url);
                    } else {
                        inFile = new File(IMGPATH);
                        op = JAI.create("fileload", inFile.getAbsolutePath());
                    }

                    int compression = getCompression(op);

                    //�Y���Y�榡����CCIT T6 �άO PickBits�h�ഫ�����Y�榡
                    if (TIFFEncodeParam.COMPRESSION_GROUP4 != compression && TIFFEncodeParam.COMPRESSION_PACKBITS != compression) {

                        File outFile = RptUtils.createTempFile("temp.tif");

                        TIFAttr tifAttr = new TIFAttr();
                        tifAttr.setDPI(300);
                        tifAttr.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);

                        TIFTools.transTIFCompression(inFile, outFile, tifAttr);

                        FileUtils.copyFile(outFile, inFile);

                        outFile.delete();
                    }
                }

                System.gc();
                //PCIDSS: AppletServlet �P�_
                
                param.put("fileFullPath", IMGPATH);
                param.put("forceBy", "TIFViewer");
                String fileFullPathJSON = VOTool.toJSON(param);
                log.error("fileFullPathJSON: "+fileFullPathJSON);
                //parameters.put("fileFullPath", IMGPATH);
                parameters.put("fileFullPath", fileFullPathJSON);
                parameters.put("downloadFileName", FileName);
                parameters.put("markedFileName", IMGFILENAME);
                parameters.put("markedFileFullPath", IMGPATH);                
                log.error("parameters: "+parameters);
                RptUtils.cryptoSecurityPrintParameToResp(parameters, resp);

                resp.addOutputData("fileFullPath", IMGPATH);
            }

        } catch (Exception e) {
            log.fatal("����}�ҳ�����", e);
            msg.setReturnCode(ReturnCode.ERROR);
            msg.setMsgDesc(e.getMessage());
            MessageHelper.setReturnMessage(msg, ReturnCode.ERROR, "����}�ҳ�����", e, req);
        }

        return resp;
    }

    /**
     * ���oTIF�����Y�榡
     * @param op
     * @return
     * @throws Exception
     */
    private int getCompression(RenderedOp op) throws Exception {
        int TAG_COMPRESSION = 259;
        TIFFDirectory dir = (TIFFDirectory) op.getProperty("tiff_directory");
        if (dir.isTagPresent(TAG_COMPRESSION)) {
            TIFFField compField = dir.getField(TAG_COMPRESSION);
            return compField.getAsInt(0);
        }
        return 0;
    }

    private void createMarkedPDF(String TEMP_FILE_PATH, String MARKED_FILE_PATH, String watermarkWord) throws DocumentException,
            IOException {

        WatermarkAttr watermarkAttr = new WatermarkAttr();
        watermarkAttr.setWord(watermarkWord);
        watermarkAttr.setFull(true);
        watermarkAttr.setSize(15f);

        PDFTools.addWatermark(TEMP_FILE_PATH, MARKED_FILE_PATH, watermarkAttr);
    }

    /**
    * �U��
    * @param req
    * @return
    * @throws TxException
    */
    public ResponseContext doDownloadTIF(RequestContext req) throws TxException {

        try {
            log.error("****** doDownloadTIF ******");
            Map actionParam = VOTool.jsonToMap(req.getParameter("actionParam"));

            doDowndLog(actionParam);

            msg.setReturnCode(ReturnCode.OK);

        } catch (ErrorInputException eie) {
            log.error(eie);
            msg.setMsgDesc(eie.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        } catch (ModuleException me) {
            if (me.getRootException() == null) {
                log.error("", me);
            } else {
                log.error(me.getMessage(), me.getRootException());
            }

            msg.setMsgDesc("�U�����ѡG" + me.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        } catch (Exception e) {
            log.error("�U������", e);
            msg.setMsgDesc("�U�����ѡG" + e.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        }

        return resp;
    }

    /**
     * �U��
     * @param req
     * @return
     * @throws TxException
     */
    public ResponseContext doDownloadTIF2(RequestContext req) throws TxException {
        log.error("****** doDownloadTIF2 ******");
        BufferedInputStream in = null;
        OutputStream out1 = null;
        InputStream fis = null;

        try {

            String IS_PDF = req.getParameter("IS_PDF");

            String FILE_NAME = req.getParameter("FILE_NAME"); // �s�ɪ��ɦW
            String FILE_PATH = req.getParameter("FILE_PATH"); // �U���ɹ�ڪ����|�ɦW            

            if (!"N".equals(IS_PDF)) {
                // IS_PDF = "" => �P�_���ɦW�O�_�� tif�A�O�h�i��U���A�_�h�ߥX�ҥ~
                // IS_PDF = "Y" => ��ܬOPDF�A�����ߨҥ~
                if ("Y".equals(IS_PDF) || (!FILE_NAME.endsWith(".tif") && !FILE_NAME.endsWith(".TIF"))) {
                    throw new ErrorInputException("�U�� Tif �ɮ榡���~�A�ɦW�G" + FILE_NAME);
                }
            }

            // �U�� TIF
            log.fatal("==========AVE01680 Download �D���~ download: start ");

            /* jsp �\�� : ���� download �� �\��  */
            //2018-11-07 �G�վ�v�����|�A�Y���|�]�tIP�A�hschema���[�Whttp:�A�_�h�H�۹���|��ܧY�i(ex:/nas69/DJ01/DJ01201810/H9R072021800120/107102400023.TIF)
            Map map = FieldOptionList.getName("AV", "CXLSVR_IP");
            boolean isContains = false; //IMGPATH���O�_�]�tIP

            for (String key : (Set<String>) map.keySet()) {
                if (FILE_PATH.indexOf(MapUtils.getString(map, key)) >= 0) {
                    isContains = true;
                    break;
                }
            }
            String urlStr = FILE_PATH;
            URL fileURL = null;
            if(isContains) {
                urlStr = "http:" + FILE_PATH;
                fileURL = new URL(urlStr);
            }else {
                File inFile = new File(urlStr);
                fileURL = inFile.toURL();
            }

            log.fatal("==========AVE01680 Download �D���~ FILE_NAME:" + FILE_NAME);
            log.fatal("==========AVE01680 Download �D���~ urlStr:" + urlStr);

            //URL fileURL = new URL(urlStr);

            HttpRequestData requestData = (HttpRequestData) req.getData();
            HttpServletResponse response = (HttpServletResponse) requestData.getHttpResponseData().getData();

            response.setContentType("image/tiff");
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(FILE_NAME.getBytes("Big5"), "ISO8859_1"));

            fileURL.openConnection();

            log.fatal("==========AVE01680 Download �D���~ fileURL.openConnection(): start 1");

            fis = fileURL.openStream();

            in = new BufferedInputStream(fis);

            log.fatal("==========AVE01680 Download �D���~ fileURL.openConnection(): start 2");
            out1 = response.getOutputStream();

            log.debug("out1 = response.getOutputStream(): start");
            int aRead = 0;
            while ((aRead = in.read()) != -1 & in != null) {
                out1.write(aRead);
            }

            out1.flush();

            // �g�J DB log
            Map actionParam = new HashMap();
            actionParam.put("FILE_NAME", FILE_NAME);
            actionParam.put("FILE_PATH", FILE_PATH);
            actionParam.put("IMG_KIND_LINK", req.getParameter("IMG_KIND_LINK"));
            actionParam.put("OPUNIT", req.getParameter("OPUNIT"));
            actionParam.put("EMPID", req.getParameter("EMPID"));
            actionParam.put("EMPNAME", req.getParameter("EMPNAME"));

            // log
            doDowndLog(actionParam);

            log.fatal("==========AVE01680 Download �D���~ download: end");

        } catch (ErrorInputException eie) {

            log.error(eie);
            msg.setMsgDesc(eie.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);

        } catch (ModuleException me) {

            if (me.getRootException() == null) {
                log.error(me);
            } else {
                log.error(me.getMessage(), me.getRootException());
            }

            msg.setMsgDesc("�U�����ѡG" + me.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);

        } catch (Exception e) {

            log.error("�U������", e);
            msg.setMsgDesc("�U�����ѡG" + e.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);

        } finally {

            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    log.error("�U������", e);
                }
            }

            if (out1 != null) {
                try {
                    out1.close();
                } catch (Exception e) {
                    log.error("�U������", e);
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("�U������", e);
                }
            }
        }

        return resp;
    }

    /**
     * 
     * @param req
     * @return
     * @throws TxException
     */
    public ResponseContext doDownloadPDF(RequestContext req) throws TxException {

        try {

            Map actionParam = VOTool.jsonToMap(req.getParameter("actionParam"));

            doDowndLog(actionParam);

            msg.setReturnCode(ReturnCode.OK);

        } catch (ErrorInputException eie) {
            log.error(eie);
            msg.setMsgDesc(eie.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        } catch (ModuleException me) {
            if (me.getRootException() == null) {
                log.error("", me);
            } else {
                log.error(me.getMessage(), me.getRootException());
            }

            msg.setMsgDesc("�U�����ѡG" + me.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        } catch (Exception e) {
            log.error("�U������", e);
            msg.setMsgDesc("�U�����ѡG" + e.getMessage());
            msg.setReturnCode(ReturnCode.ERROR);
        }

        return resp;
    }

    /**
     * 
     * @param req
     * @return
     * @throws Exception
     */
    private ResponseContext doDowndLog(Map actionParam) throws Exception {

        String FILE_NAME = MapUtils.getString(actionParam, "FILE_NAME"); // �s�ɪ��ɦW
        String FILE_PATH = MapUtils.getString(actionParam, "FILE_PATH"); // �U���ɹ�ڪ����|�ɦW
        String IMG_KEY = MapUtils.getString(actionParam, "IMG_KEY", "");
        String IS_OLD = MapUtils.getString(actionParam, "IS_OLD", "");
        String IMG_KIND_LINK = MapUtils.getString(actionParam, "IMG_KIND_LINK", "");
        String RCPT_NO_LINK = MapUtils.getString(actionParam, "RCPT_NO_LINK", "");

        if (StringUtils.isBlank(FILE_PATH)) {
            throw new ErrorInputException("�U���ɹ�ڪ����|�ɦW����J");
        }

        if (StringUtils.isBlank(FILE_NAME)) {
            throw new ErrorInputException("�s�ɪ��ɦW����J");
        }

        String OPUNIT = MapUtils.getString(actionParam, "OPUNIT");
        String EMPID = MapUtils.getString(actionParam, "EMPID");
        String EMPNAME = MapUtils.getString(actionParam, "EMPNAME");
        String IMG_KIND = "";
        String RCPT_NO = "";

        String[] arr_IMG_KIND = FILE_PATH.split("/");

        if (arr_IMG_KIND.length > 4) {
            //            IMG_KIND = arr_IMG_KIND[arr_IMG_KIND.length - 3].substring(0, 4);
            IMG_KIND = IMG_KIND_LINK;
        }

        try {
            RCPT_NO = FILE_NAME.substring(0, FILE_NAME.indexOf('.'));
        } catch (Exception e) {
            RCPT_NO = "";
        }

        if ("Y".equals(IS_OLD)) {
            IMG_KIND = IMG_KIND_LINK;
            RCPT_NO = RCPT_NO_LINK;
        }

        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("OPUNIT", OPUNIT);//�ާ@���N��
        userMap.put("EMPID", EMPID);//�ާ@�H��ID
        userMap.put("EMPNAME", EMPNAME);//�ާ@�H���m�W
        userMap.put("IMG_KEY", IMG_KEY);

        Transaction.begin();
        try {

            new AV_E0Z008().insert(IMG_KIND, RCPT_NO, "2", userMap, IS_OLD);

            Transaction.commit();

        } catch (Exception e) {
            Transaction.rollback();
            throw e;
        }

        return resp;
    }

    /**
    * ���o��lPrompt�ϥΪ�JSP�e����Map
    * @param req
    * @return
    * @throws TxException
    */
    private Map getPromptMap() throws TxException, ModuleException {

        Map jvm = new ListOrderedMap();

        jvm.put("LINK_TYPE", "N");
        jvm.put("IMG_KIND_VALUE", "");
        jvm.put("RCPT_NO", "");
        jvm.put("IMG_KIND_LINK", "");
        jvm.put("RCPT_NO_LINK", "");
        jvm.put("PROCDIVNO", "");
        jvm.put("FILEDATE", "");
        jvm.put("FILEOPERATOR", "");
        jvm.put("BCHNO", "");
        jvm.put("IMGFILENAME", "");
        jvm.put("MEMOTEXT", "");
        jvm.put("DVDNO", "");
        jvm.put("BOXDATE", "");
        jvm.put("BOXOPERATOR", "");
        jvm.put("BOXNO", "");
        jvm.put("IMGPATH", "");

        return jvm;
    }

    /*
     * ���͵e���һݪ��U�Ե����� ���N�X, �}��~��
     */
    private void setOpt() throws Exception {
        //���N�X SelectOpt
        SelectOptUtil IMG_KIND_OPT = new SelectOptUtil();
        try {

            List<ED_B0Z001_bo> tmplist = ed_b0z001.getImgKindList(user.getRoles());
            for (int i = 0; i < tmplist.size(); i++) {
                ED_B0Z001_bo bo = (ED_B0Z001_bo) tmplist.get(i);
                IMG_KIND_OPT.addOption(bo.getIMG_KIND(), bo.getIMG_KIND() + " " + bo.getIMG_NAME());
            }
        } catch (Exception e) {
            log.debug(e);
        }
        resp.addOutputData("IMG_KIND_OPT", IMG_KIND_OPT);
    }
    
    /**
     * �ˬd�O�_��RLAV100�O�N���y�g��
     * @param hs
     * @return
     */
    private boolean checkEditable(Hashtable hs){
        boolean isEditable = false;
        Set setRole = hs.keySet();
        // Check ���� �O�_�� RLAV100;
        for (Iterator iter = setRole.iterator(); iter.hasNext();) {
            String strRole = (String) iter.next();
            if (strRole.equals("RLAV100")){
                isEditable = true;
                break;
            }
        }
        return isEditable;
    }
}
