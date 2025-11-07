package cn.com.sysnet.smis.hd.service.impl;

import cn.com.sysnet.common.ConstFmt;
import cn.com.sysnet.smis.gx.action.SalesInfoAction;
import cn.com.sysnet.smis.gx.dao.DevelopCardInfoMaintenanceDao;
import cn.com.sysnet.smis.hd.dao.RecommendChangeDao;
import cn.com.sysnet.smis.hd.model.*;
import cn.com.sysnet.smis.hd.service.RecommendChangeService;
import cn.com.sysnet.smis.share.common.RefereeEnum;
import cn.com.sysnet.smis.tx.model.PolicyPerdaybaseInfo;
import cn.com.sysnet.smis.tx.model.T04AgencyPoundage;
import cn.com.sysnet.smis.xq.dao.SalesInfoXQDao;
import cn.com.sysnet.smis.xq.model.NewPolicyGovernance;
import cn.com.sysnet.smis.yb.dao.StaffInfoDao;
import cn.com.sysnet.smis.yb.model.StaffInfo;
import cn.com.sysnet.util.UtilNumber;
import cn.com.sysnet.util.UtilString;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.tsis.util.HttpClientUtil;
import cn.com.tsis.util.JudgeServerUrlUtil;
import cn.com.tsis.util.NullUtils;
import cn.com.tsis.util.QualityUrlUtil;
import com.alibaba.fastjson.JSON;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import cn.com.sysnet.common.ConstResult;
import cn.com.sysnet.smis.gx.dao.ibatis.SalesInfoSpecialMaintenanceDaoiBatis;
import cn.com.sysnet.smis.gx.model.InsuranceInfo;
import cn.com.sysnet.smis.hd.dao.SalesInfoDao;
import cn.com.sysnet.smis.hd.model.PrintSalaryBill;
import cn.com.sysnet.smis.hd.model.SalesInfo;
import cn.com.sysnet.smis.hd.model.SalesinfoRelatives;
import cn.com.sysnet.smis.hd.model.WagesPrint;
import cn.com.sysnet.smis.hd.service.SalesInfoService;
import cn.com.sysnet.smis.share.common.CodeTypeConst;
import cn.com.sysnet.smis.share.common.ConstantEnum;
import cn.com.sysnet.smis.share.common.DataConst;
import cn.com.sysnet.smis.share.common.JobNumEnums.ChannelNum;
import cn.com.sysnet.smis.share.common.JobNumEnums.MessageType;
import cn.com.sysnet.smis.share.common.JobNumEnums.ResponseType;
import cn.com.sysnet.smis.share.dao.BusinessManagerDao;
import cn.com.sysnet.smis.share.dao.CodecodeDao;
import cn.com.sysnet.smis.share.dao.DealDao;
import cn.com.sysnet.smis.share.dao.IdGenerator;
import cn.com.sysnet.smis.share.dao.ImportValidateDao;
import cn.com.sysnet.smis.share.dao.PublicFunctionDao;
import cn.com.sysnet.smis.share.dao.RankdefDao;
import cn.com.sysnet.smis.share.dao.SalesPrepareHrDAO;
import cn.com.sysnet.smis.share.dao.TaskManagerDao;
import cn.com.sysnet.smis.share.excel.ExportSalesInfo;
import cn.com.sysnet.smis.share.model.CodecodeInfo;
import cn.com.sysnet.smis.share.model.PublicFunction;
import cn.com.sysnet.smis.share.model.RankdefInfo;
import cn.com.sysnet.smis.share.model.UploadLog;
import cn.com.sysnet.smis.share.model.UserInfo;
import cn.com.sysnet.smis.share.service.PublicFunctionManager;
import cn.com.sysnet.smis.share.service.PublicMethodManager;
import cn.com.sysnet.smis.share.service.XmlLogService;
import cn.com.sysnet.smis.share.service.impl.ExportDataServiceImpl;
import cn.com.sysnet.smis.share.util.JarUtils;
import cn.com.sysnet.smis.share.util.SalesCodeUtils;
import cn.com.sysnet.smis.share.webservice.client.jobnum.UnifiedJobNumberService;
import cn.com.sysnet.smis.share.webservice.client.pub.TransResult;
import cn.com.sysnet.smis.share.webservice.model.request.RequestModel;
import cn.com.sysnet.smis.share.webservice.model.response.ResponseModel;
import cn.com.sysnet.smis.xq.model.SalesInfoXQ;
import cn.com.sysnet.smis.xq.model.SalesMoveInfo;
import cn.com.sysnet.util.UtilDate;
import cn.com.sysnet.util.UtilLog;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SalesInfoServiceImpl implements SalesInfoService {

	private SalesInfoDao salesInfoDao_hd;
	private IdGenerator idGenerator;
	private CodecodeDao codecodeDao;
	private RankdefDao rankdefDao;
	private DealDao dealDao;
	private TaskManagerDao taskManagerDao;
	private ImportValidateDao importValidateDao;
	private BusinessManagerDao businessManagerDao;
	private PublicFunctionManager publicFunctionManager;
	private UnifiedJobNumberService unifiedJobNumberServiceImpl; // M by wang_gq 集团工号
	private PublicMethodManager publicMethodManagerImpl; // R275
	private PublicFunctionDao publicFunctionDao;
	private XmlLogService xmlLogService;// a by ni_f L824
	private SalesPrepareHrDAO salesPrepareHrDAO;
	private DevelopCardInfoMaintenanceDao developCardInfoMaintenanceDao;
	private SalesInfoXQDao salesInfoXQDao;
	private StaffInfoDao staffInfoDao_yb;

	public StaffInfoDao getStaffInfoDao_yb() {
		return staffInfoDao_yb;
	}

	public void setStaffInfoDao_yb(StaffInfoDao staffInfoDao_yb) {
		this.staffInfoDao_yb = staffInfoDao_yb;
	}

	public SalesInfoXQDao getSalesInfoXQDao() {
		return salesInfoXQDao;
	}



	public void setSalesInfoXQDao(SalesInfoXQDao salesInfoXQDao) {
		this.salesInfoXQDao = salesInfoXQDao;
	}
	public void setDevelopCardInfoMaintenanceDao(DevelopCardInfoMaintenanceDao developCardInfoMaintenanceDao) {
		this.developCardInfoMaintenanceDao = developCardInfoMaintenanceDao;
	}

	public DevelopCardInfoMaintenanceDao getDevelopCardInfoMaintenanceDao() {
		return developCardInfoMaintenanceDao;
	}
	private RecommendChangeService recommendChangeService;//a by qsw for L2382

	public RecommendChangeService getRecommendChangeService() {
		return recommendChangeService;
	}

	public void setRecommendChangeService(RecommendChangeService recommendChangeService) {
		this.recommendChangeService = recommendChangeService;
	}
	/****************************add liu_yl for L1396合同制人员录入添加五险一金 on 20210508 start*********************/
	private SalesInfoSpecialMaintenanceDaoiBatis salesInfoSpecialMaintenanceDao;
	public SalesInfoSpecialMaintenanceDaoiBatis getSalesInfoSpecialMaintenanceDao() {
		return salesInfoSpecialMaintenanceDao;
	}
	public void setSalesInfoSpecialMaintenanceDao(
			SalesInfoSpecialMaintenanceDaoiBatis salesInfoSpecialMaintenanceDao) {
		this.salesInfoSpecialMaintenanceDao = salesInfoSpecialMaintenanceDao;
	}
	/****************************add liu_yl for L1396合同制人员录入添加五险一金 on 20210508 end*********************/

	public SalesPrepareHrDAO getSalesPrepareHrDAO() {
		return salesPrepareHrDAO;
	}

	public void setSalesPrepareHrDAO(SalesPrepareHrDAO salesPrepareHrDAO) {
		this.salesPrepareHrDAO = salesPrepareHrDAO;
	}

	public XmlLogService getXmlLogService() {
		return xmlLogService;
	}

	public void setXmlLogService(XmlLogService xmlLogService) {
		this.xmlLogService = xmlLogService;
	}

	public void setPublicFunctionDao(PublicFunctionDao publicFunctionDao) {
		this.publicFunctionDao = publicFunctionDao;
	}

	public PublicMethodManager getPublicMethodManagerImpl() {
		return publicMethodManagerImpl;
	}

	public void setPublicMethodManagerImpl(PublicMethodManager publicMethodManagerImpl) {
		this.publicMethodManagerImpl = publicMethodManagerImpl;
	}

	public void setUnifiedJobNumberServiceImpl(UnifiedJobNumberService unifiedJobNumberServiceImpl) {
		this.unifiedJobNumberServiceImpl = unifiedJobNumberServiceImpl;
	}

	public SalesInfo getInfo(SalesInfo salesInfo) {
		return salesInfoDao_hd.getInfo(salesInfo);
	}

	public void setPublicFunctionManager(PublicFunctionManager publicFunctionManager) {
		this.publicFunctionManager = publicFunctionManager;
	}

	public void setBusinessManagerDao(BusinessManagerDao businessManagerDao) {
		this.businessManagerDao = businessManagerDao;
	}

	public void setTaskManagerDao(TaskManagerDao taskManagerDao) {
		this.taskManagerDao = taskManagerDao;
	}

	public void setDealDao(DealDao dealDao) {
		this.dealDao = dealDao;
	}

	public void setImportValidateDao(ImportValidateDao importValidateDao) {
		this.importValidateDao = importValidateDao;
	}

	public void setRankdefDao(RankdefDao rankdefDao) {
		this.rankdefDao = rankdefDao;
	}

	public void setCodecodeDao(CodecodeDao codecodeDao) {
		this.codecodeDao = codecodeDao;
	}

	public void setSalesInfoDao_hd(SalesInfoDao salesInfoDao_hd) {
		this.salesInfoDao_hd = salesInfoDao_hd;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public SalesInfo getRecommendName(String channel_id, String recommend_id) {
		return salesInfoDao_hd.getRecommendName(channel_id, recommend_id);
	}

	private RecommendChangeDao recommendChangeDao;

	public void setRecommendChangeDao(RecommendChangeDao recommendChangeDao) {
		this.recommendChangeDao = recommendChangeDao;
	}

	public SalesInfo getNameById(String channel_id, String team_id) {
		SalesInfo info = salesInfoDao_hd.getNameById(channel_id, team_id);
		if (info != null) {
			String leader_type = info.getLeader_type();
			if (leader_type != null && !"".equals(leader_type)) {
				if (leader_type.equals(CodeTypeConst.CODE_TYPE_TYPE_NQ)) {
					info.setLeader_name(info.getManager_name());
				}
			}
			// 判断入参leaderId是否为7位，如果为7位就转换成10位 a by ge_xd for R275 on 20140819
			if (info.getLeader_id() != null && !"".equals(info.getLeader_id())
					&& info.getLeader_id().trim().length() == 7) {

				String leader_id = publicMethodManagerImpl.getSalesCode(info.getLeader_id());
				info.setLeader_id(leader_id);

			}
		}
		return info;
	}

	public String queryIdNo(String channel_id, String id_type, String id_no) {
		String flag = "";
		List<SalesInfo> list = salesInfoDao_hd.queryIdNo(channel_id, id_type, id_no);

		if (list != null && list.size() > 0) {
			SalesInfo sales = list.get(0);
			flag = sales.getId_no();
		}

		return flag;
	}

	// 互动人员录入(普通);
	public SalesInfo insertSalesInfo(SalesInfo salesInfo) {

		String workspace_id = publicFunctionManager
				.getVersionBeanByTeam(salesInfo.getChannel_id(), "", salesInfo.getTeam_id()).getWorkspace_id();
		String base_version_id = publicFunctionManager
				.getVersionBeanByTeam(salesInfo.getChannel_id(), "", salesInfo.getTeam_id()).getBase_version_id();

		// 判断入参salesId是否为10位，如果为10位就转换成7位 a by ge_xd for R275 on 20140819
		if (salesInfo.getRecommend_id() != null && !"".equals(salesInfo.getRecommend_id())
				&& salesInfo.getRecommend_id().trim().length() == 10) {

			String recommend_id = publicMethodManagerImpl.getSalesID(salesInfo.getRecommend_id());
			salesInfo.setRecommend_id(recommend_id);

		}
		if (salesInfo.getLeader_id() != null && !"".equals(salesInfo.getLeader_id())
				&& salesInfo.getLeader_id().trim().length() == 10) {
			String leader_id = publicMethodManagerImpl.getSalesID(salesInfo.getLeader_id());
			salesInfo.setLeader_id(leader_id);
		}

		salesInfo.setWorkspace_id(workspace_id);
		salesInfo.setBase_version_id(base_version_id);
		//OB改造 ld 20240816
		salesInfo.setObranch_id(salesInfo.getBranch_id().substring(0, 3)+"0000");
		// A by ni_f L824 begin
		String sales_code = salesInfo.getSales_id();
		salesInfo.setSalesCode(sales_code);
		String sales_id = salesInfoDao_hd.getSalesId(sales_code);
		salesInfo.setSales_id(sales_id);
		RequestModel m = null;
		if (salesInfo.getFlag() != null && !"temp".equals(salesInfo.getFlag())) { // 暂存不获取集团工号 M by wang_gq

			m = new RequestModel();
			m.setMessageType("01");
			m.setUniSalesCod(null);
			m.setSalesNam(salesInfo.getSales_name());
			m.setDateBirthd(salesInfo.getBirthday());
			m.setSexCod(salesInfo.getSex());
			m.setManOrgCod(salesInfo.getBranch_id());
			m.setManOrgNam(salesInfo.getBranch_name());
			m.setIdtypCod(salesInfo.getId_type());
			m.setIdNo(salesInfo.getId_no());
			String fixed_line = salesInfo.getFixed_line();
			String mobile = salesInfo.getMobile();
			String phs = salesInfo.getPhs();
			String tel = "";
			if (mobile != null && !mobile.equals("")) {
				tel = mobile;
			} else {
				if (phs != null && !phs.equals("")) {
					tel = phs;
				}
			}
			if ("".equals(tel)) {
				tel = fixed_line;
			}
			m.setSalesTel(fixed_line);
			m.setSalesMob(tel);

//				m.setSalesMail("N");
			m.setSalesAddr(salesInfo.getHome_address());
			m.setEducationCod(salesInfo.getEducation());
			m.setQualifiNo(salesInfo.getQualify_id());
			m.setCertifiNo(salesInfo.getDevelop_id());
			m.setContractNo(salesInfo.getContract_id());
			m.setStatusCod(salesInfo.getStat());
			m.setChannelCod(salesInfo.getChannel_id());
			m.setEntrantDate(salesInfo.getProbation_date().replace("-", ""));
			m.setIsCrosssale("1");
			m.setSalesTypCod(salesInfo.getEmploy_kind());
			// 设置团队ID
			m.setTeamId(salesInfo.getTeam_id());
			// 全渠道人员录入增加对上传集团的维护 start
			m = new RequestModel();
			m.setMessageType(MessageType.SUBMIT_SALES_INFO.getCode());
			m.setUniSalesCod(salesInfo.getSalesCode());
			m.setSalesNam(salesInfo.getSales_name());
			m.setDateBirthd(salesInfo.getBirthday());
			m.setSexCod(salesInfo.getSex());
			m.setManOrgCod(salesInfo.getBranch_id());
			PublicFunction publicFunction = publicFunctionDao.getBranchInfoById(salesInfo.getBranch_id());
			m.setManOrgNam(publicFunction.getBranch_name());
			m.setIdtypCod(salesInfo.getId_type());
			m.setIdNo(salesInfo.getId_no());
			m.setSalesMob(salesInfo.getMobile());
			m.setSalesAddr(salesInfo.getHome_address());
			m.setEducationCod(salesInfo.getEducation());
			// 处理离职时间
			// 默认当前日期
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = dateFormat.format(new Date());
			m.setDemissionDate(date);
			// m.setStatusCod(salesInfo1.getStat());
			m.setChannelCod(salesInfo.getChannel_id());
			m.setEntrantDate(UtilDate.fmtDate(new Date()).replace("-", ""));
			m.setIsCrosssale("1");
			m.setStatusCod("1");
			m.setSalesTypCod(salesInfo.getEmploy_kind());
			// ---电话号码处理开始
			m.setSalesMob(salesInfo.getMobile());
			ResponseModel rl;
			/* TransResult tr = null; */
			m.setMessageType("02");
			UploadLog uplog = new UploadLog();
			uplog.setUpload_type("12");
			uplog.setChannel_id(salesInfo.getChannel_id());
			uplog.setBranch_id(salesInfo.getBranch_id());
			uplog.setObj_id(salesInfo.getSales_id());
			uplog.setOperate_type(DataConst.Data_Operate_Type_Update);
			m.setTeamType(salesInfo.getTeam_type());
			try {
				rl = unifiedJobNumberServiceImpl.getJobNumber(m);
				uplog.setRtcode(rl.getErrCode());
				uplog.setRtinfo(rl.getErrDesc());

				if (rl != null && ResponseType.SALES_UPDATE_SUCCESS.equals(rl.getResponseType())) {
					String a = "销售人员数据维护上报集团成功！";
					uplog.setIs_upload(DataConst.Status_Valid);
					dealDao.updateUploadLog(uplog);
				} else {
					// 生成失败
					String a = "销售人员数据维护上报集团失败！";
					/*
					 * String strLog = "人员["+salesInfo.getChannel_id()+"]["+salesInfo.getSales_id()+
					 * "]信息上传成功0000000000:"+tr.getRtInfo().getValue()+"  "+a; UtilLog.debug(a);
					 */
					uplog.setIs_upload(DataConst.Status_Invalid);
					dealDao.updateUploadLog(uplog);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 全渠道人员录入增加对上传集团的维护 end
			/*
			 * //上线发开 ResponseModel rm=unifiedJobNumberServiceImpl.getJobNumber(m); if
			 * (rm==null || rm.getSalesCode()==null ||
			 * ResponseType.MESSAGE_FORMAT_ERROR.equals(rm.getResponseType())) { //生成失败
			 * if(rm!=null){ if(!"".equals(rm.getErrDesc())){ String esd = rm.getErrDesc();
			 * int info=-1; try{ info = esd.indexOf("已入职，统一工号"); }catch
			 * (NullPointerException e){ info=-1; } // a by guo_cz for 集团工号问题 on 20180828
			 * String code3=""; if(info>0){ int ii = esd.indexOf(",姓名"); int iii = ii-10;
			 * String sales_codeinner = esd.substring(iii, ii); code3 =
			 * sales_codeinner.substring(0, 1); if("3".equals(code3)){ String id_no =
			 * salesInfo.getId_no(); XmlLogInfo staffnum =
			 * salesInfoDao_hd.staffnumInfo(id_no); if(staffnum == null){
			 * salesInfo.setSalesCode(sales_codeinner); } } } } }
			 * salesInfo.setFlag("false&"+rm.getErrDesc()); // a by guo_cz for L661 on
			 * 20180904 salesInfoDao_hd.deleteSalesInfo(salesInfo);
			 * salesInfo.setIs_flag("temp");
			 * salesInfoDao_hd.insertSalesInfo(salesInfo);//插入人员表 return salesInfo; //生成失败
			 * }else { salesInfo.setSalesCode(rm.getSalesCode()); } } catch (Exception e) {
			 * e.printStackTrace(); salesInfo.setFlag("false"); // a by guo_cz for L661 on
			 * 20180904 salesInfoDao_hd.deleteSalesInfo(salesInfo);
			 * salesInfo.setIs_flag("temp");
			 * salesInfoDao_hd.insertSalesInfo(salesInfo);//插入人员表 return salesInfo; //生成失败 }
			 */
			// 获取人员ID
			// salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(),salesInfo.getBranch_id()));
			// a by ni_f L824 去除生产salesid逻辑
			// ni_f L685
			// A by ni_f L824 end
			if (salesInfo.getFlag_query() == null || "".equals(salesInfo.getFlag_query())) {
				salesInfo.setFixedlineDate(null);
			} else {
				Date da = new Date();
				salesInfo.setFixedlineDate(da);
			}

			salesInfoDao_hd.insertSalesInfo(salesInfo);// 插入人员表
			//a by qsw for L2382 人员录入维护推荐关系表及处理多代关系
			recommendChangeService.handler(salesInfo.getRecommend_id(),salesInfo.getSales_id(), RefereeEnum.ZYCL,salesInfo.getUser_id());
			//a by mqj for L3246 人员录入维护跨四级管辖关系
			recommendChangeService.handlerCrossBr4Manage(salesInfo.getRecommend_id(),salesInfo.getSales_id(), RefereeEnum.ZYCL,salesInfo.getUser_id());

			/****************************add liu_yl for L1396合同制人员录入添加五险一金 on 20210508 start*********************/
			if(salesInfo.getEmploy_kind()!=null && !salesInfo.getEmploy_kind().equals("") && salesInfo.getEmploy_kind().equals("1")){
				String sysdateYYYYMM = salesInfoSpecialMaintenanceDao.getSysDate();//系统时间（月）
				List<InsuranceInfo> insuranceBranchIdInfo = salesInfoSpecialMaintenanceDao.getBranchId(salesInfo.getBranch_id(),salesInfo.getChannel_id());//机构五险一金配置
				if(insuranceBranchIdInfo != null){//已配置
					for(InsuranceInfo sf : insuranceBranchIdInfo) {
						sf.setMaintain_way("02");//个人维护
						sf.setEnd_month(sysdateYYYYMM);
						sf.setSales_id(sales_id);
						salesInfoSpecialMaintenanceDao.updateBaseInfo(sf);//插入主表
						break;
					}
				}
			}
			/****************************add liu_yl for L1396合同制人员录入添加五险一金 on 20210508 end*********************/

			salesInfoDao_hd.insertQualifyCardInfo(salesInfo);// 插入转业资格证表
			//定义人员id
			sales_id = salesInfo.getSales_id();
			//获取执业证应急开关状态
			int key = developCardInfoMaintenanceDao.getZYZKeyStat(sales_id);
			if(key == 1){//开关打开时插入执业证信息
				//L2210 add by lh 人员入司成功后将原有保存执业证信息的逻辑去掉，移动到执业证登记成功后直接进行保存
				salesInfoDao_hd.insertDevelopCardInfo(salesInfo);
			}
            //2208 add by lh 人员入司成功后将原有保存执业证信息的逻辑去掉，移动到执业证登记成功后直接进行保存
			//salesInfoDao_hd.insertDevelopCardInfo(salesInfo);// 插入展业证书号,发证日期和截止日期

			salesInfoDao_hd.insertQualifyInfo(salesInfo);// 插入资格信息

			//salesInfoDao_hd.insertSalesAssurerInfo(salesInfo);// 插入保证人信息

			//salesInfoDao_hd.insertEscortoperation(salesInfo);// 插入护航行动
			//插入内担保人信息
			if (salesInfo.getAssurer_id_no_2() != null && !("").equals(salesInfo.getAssurer_id_no_2())) {
				salesInfo.setStat(DataConst.Status_Valid);
				salesInfo.setOperate_type("1");
				salesInfo.setAssurer_no(1);
				int i = salesInfoDao_hd.querySalesAssurer1(salesInfo);
				if(i>0){
					salesInfo.setOperate_type("2");
					salesInfoDao_hd.updateAssurerInfo1(salesInfo);
				}else{
					salesInfoDao_hd.insertAssurerInfo1(salesInfo);
				}
			}
			//插入外担保人信息
			if (salesInfo.getAssurer_id_no() != null && !("").equals(salesInfo.getAssurer_id_no())) {
				salesInfo.setStat(DataConst.Status_Valid);
				salesInfo.setOperate_type("1");
				salesInfo.setAssurer_no(2);
				int i = salesInfoDao_hd.querySalesAssurer1(salesInfo);
				if(i>0){
					salesInfo.setOperate_type("2");
					salesInfoDao_hd.updateAssurerInfo2(salesInfo);
				}else{
					salesInfoDao_hd.insertAssurerInfo2(salesInfo);
				}
			}
			//紧急联系人信息更新与保存
			if(salesInfoDao_hd.emergencycontactCount(salesInfo) >0){
				salesInfoDao_hd.updateemergencycontact(salesInfo);
			}else{
				salesInfoDao_hd.insertEmergencycontact(salesInfo);
			}
			if("".equals(salesInfo.getAssurer_id_2()) || salesInfo.getAssurer_id_2() == null){
				List<SalesInfo> info = salesInfoDao_hd.assurerCode(sales_id);
				if(info.size()>0){
					salesInfo.setAssurer_id_2(info.get(0).getAssurer_code());
				}
			}
			salesInfoDao_hd.updateT02depositinfo_SALES_ID_By(salesInfo);// 根据渠道、证件类型和证件号码更新t02depositinfo中的SALES_ID
			// a by shiyawei for L479 增员处理育成关系
			//TODO 测试注释
			businessManagerDao.crtTrainRelationHD_2016(salesInfo.getChannel_id(), salesInfo.getBranch_id(),
					salesInfo.getTeam_id(), salesInfo.getRecommend_id(), salesInfo.getSales_id(),
					salesInfo.getUser_id());
			// 插入接口日志
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					CodeTypeConst.CODE_CODE_UPLOAD_SALES_ADD, salesInfo.getUser_id(), salesInfo.getModuleIdCur());
//			 A by chen_zw for L247 on 20151021 代码段二
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					ConstantEnum.UploadMobilePlatformType.UPLOAD_MOBILE_02_2.getCode(), salesInfo.getUser_id(),
					salesInfo.getModuleIdCur());
			//插入互联网核心日志
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					CodeTypeConst.CODE_CODE_UPLOAD_SALES_ADD_FORINTERNET, salesInfo.getUser_id(), salesInfo.getModuleIdCur());

			// 备份表
			businessManagerDao.backupTable("T02SALESINFO", salesInfo.getModuleIdCur(), null, salesInfo.getChannel_id(),
					salesInfo.getSales_id(), salesInfo.getUser_id(), DataConst.Data_Operate_Type_Insert);

			salesInfoDao_hd.insertSaleImg(salesInfo);//a by liulei for 江苏执业证接口 20210715
		} else {
//ni_f L685
			if (salesInfo.getFlag_query() == null || "".equals(salesInfo.getFlag_query())) {
				salesInfo.setFixedlineDate(null);
			} else {
				Date da = new Date();
				salesInfo.setFixedlineDate(da);
			}
			salesInfoDao_hd.insertSalesInfo(salesInfo);// 插入人员表
		}

		// 判断入参salesId是否为7位，如果为7位就转换成10位 a by ge_xd for R275 on 20140819
		if (salesInfo.getRecommend_id() != null && !"".equals(salesInfo.getRecommend_id())
				&& salesInfo.getRecommend_id().trim().length() == 7) {

			String recommend_id = publicMethodManagerImpl.getSalesCode(salesInfo.getRecommend_id());
			salesInfo.setRecommend_id(recommend_id);

		}
		if (salesInfo.getLeader_id() != null && !"".equals(salesInfo.getLeader_id())
				&& salesInfo.getLeader_id().trim().length() == 7) {

			String leader_id = publicMethodManagerImpl.getSalesCode(salesInfo.getLeader_id());
			salesInfo.setLeader_id(leader_id);

		}

		// 删除序列号
		if (m != null && !StringUtils.isEmpty(salesInfo.getSalesCode()))
			unifiedJobNumberServiceImpl.deleteSid(m);

		//20220524 增加派驻机构是否为空判断
		if(salesInfo.getAccredit_org()!= null && !"".equals(salesInfo.getAccredit_org())){
			//a by wzj for L1003 增加派驻机构 on 2020-8-26 16:36:19 start
			salesInfo.setAccredit_org(publicFunctionDao.getBranchInfoById(salesInfo.getAccredit_org()).getBranch_name());
			//a by wzj for L1003 增加派驻机构 on 2020-8-26 16:36:19 end
		}
		return salesInfo;
	}

	// a by ni_f L824 增加删除功能
	public boolean deleteStaffInfo(SalesInfo salesInfo1) {
		TransResult tr = null;
		UploadLog uplog = new UploadLog();
		uplog.setUpload_type("15");
		uplog.setChannel_id(salesInfo1.getChannel_id());
		uplog.setBranch_id(salesInfo1.getBranch_id());
		uplog.setObj_id(salesInfo1.getSales_id());
		uplog.setOperate_type(DataConst.Data_Operate_Type_Update);
		String a = "";
		RequestModel m = null;
		try {
			m = new RequestModel();
			m.setMessageType(MessageType.SUBMIT_SALES_INFO.getCode());
			m.setUniSalesCod(salesInfo1.getSalesCode());
			m.setSalesNam(salesInfo1.getSales_name());
			m.setDateBirthd(salesInfo1.getBirthday());
			m.setSexCod(salesInfo1.getSex());
			m.setManOrgCod(salesInfo1.getBranch_id());
			PublicFunction publicFunction = publicFunctionDao.getBranchInfoById(salesInfo1.getBranch_id());
			m.setManOrgNam(publicFunction.getBranch_name());
			m.setIdtypCod(salesInfo1.getId_type());
			m.setIdNo(salesInfo1.getId_no());
			m.setSalesMob(salesInfo1.getMobile());
			m.setSalesAddr(salesInfo1.getHome_address());
			m.setEducationCod(salesInfo1.getEducation());
			// 处理离职时间
			// 默认当前日期
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = dateFormat.format(new Date());
			m.setDemissionDate(date);
			// m.setStatusCod(salesInfo1.getStat());
			m.setChannelCod(salesInfo1.getChannel_id());
			m.setEntrantDate(UtilDate.fmtDate(new Date()).replace("-", ""));
			m.setIsCrosssale("1");
			m.setStatusCod("2");
			m.setSalesTypCod(salesInfo1.getEmploy_kind());
			// ---电话号码处理开始
			String mobile = salesInfo1.getMobile();
			m.setSalesMob(mobile);
			// 如果是互动渠道则设置团队类型
			if (ChannelNum.HD.getLocal().equals(m.getChannelCod())) {
				m.setTeamType(salesInfo1.getTeam_type());
			}
			// ---电话号码处理结束
			ResponseModel rl;
			// 增加人员修改插入日志 A by ge_xd for R275 on 20140829
			xmlLogService.saveXmlLogObject(salesInfo1.getSales_id(), salesInfo1.getId_type(), salesInfo1.getId_no(),
					salesInfo1.getId_type() + ":" + salesInfo1.getId_no(), "unifiedJobNumberServiceImpl");
			try {
				rl = unifiedJobNumberServiceImpl.getJobNumber(m);
				uplog.setRtcode(rl.getErrCode());
				uplog.setRtinfo(rl.getErrDesc());
				if (rl != null && ResponseType.SALES_UPDATE_SUCCESS.equals(rl.getResponseType())) {
					a = "销售人员数据维护上报集团成功！";
					uplog.setIs_upload(DataConst.Status_Valid);
					dealDao.updateUploadLog(uplog);
					salesInfoDao_hd.deleteSalesInfo_prepare(salesInfo1);// 删除人员表
				} else {
					// 生成失败
					a = "销售人员数据维护上报集团失败！";
					String strLog = "人员[" + salesInfo1.getChannel_id() + "][" + salesInfo1.getSales_id()
							+ "]信息上传成功0000000000:" + "  " + a;
					UtilLog.debug(strLog);
					uplog.setIs_upload(DataConst.Status_Invalid);
					dealDao.updateUploadLog(uplog);
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				unifiedJobNumberServiceImpl.deleteSid(m);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// z_hb 添加人员 上载异常数据记录 2016 7 14 bug 4202
			xmlLogService.saveXmlLogObject(e.getMessage(), salesInfo1.getId_type(), salesInfo1.getId_no(),
					salesInfo1.getId_type() + ":" + salesInfo1.getId_no(), "unifiedJobNumberServiceImpl");

			return false;
		}
		return true;
	}

	public boolean deleteStaffInfodirct(SalesInfo salesInfo1){
		try {
			salesInfoDao_hd.deleteSalesInfo_prepare(salesInfo1);// 删除人员表
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void insertSalesInfoPrepare_cgdata(StaffInfo salesInfo){
		// 生成7位ID (1000000-9999999)
		UUID uuid1 = UUID.randomUUID();
		long combined1 = uuid1.getMostSignificantBits() ^ uuid1.getLeastSignificantBits();
		int id = (int)(Math.abs(combined1) % 9_000_000L) + 1_000_000;
		String sales_id = String.valueOf(id);

		// 生成10位Code (1000000000-9999999999)
		UUID uuid2 = UUID.randomUUID();
		long combined2 = uuid2.getMostSignificantBits() ^ uuid2.getLeastSignificantBits();
		long code = Math.abs(combined2) % 9_000_000_000L + 1_000_000_000L;
		String sales_code = String.valueOf(code);

		salesInfo.setSalesCode(sales_code);//草稿数据工号
		salesInfo.setSales_id(sales_id);
		salesInfo.setStatus_pre("0");
		NullUtils.replaceNulls(salesInfo);
		salesInfoDao_hd.insertSalesInfo_prepareNew(salesInfo);// 插入人员表
		staffInfoDao_yb.insertsalesinfoPrepareNew(salesInfo);


	}

	public void updateSalesPrepare_YBCG(StaffInfo salesInfo){
		salesInfo.setStatus_pre("0");
		NullUtils.replaceNulls(salesInfo);
		salesInfoDao_hd.updateSalesPrepare_YBCG(salesInfo);// 插入人员表
		//插入内外担保人表
		staffInfoDao_yb.insertsalesinfoPrepareNew(salesInfo);
	}
	// 预入司新增 shiyawei
	public StaffInfo insertSalesInfoPrepareNew(StaffInfo salesInfo) {

		RequestModel m = null;
		try {
			m = new RequestModel();

			m.setMessageType("01");
			m.setUniSalesCod(null);
			m.setSalesNam(salesInfo.getSales_name());
			m.setDateBirthd(salesInfo.getBirthday());
			m.setSexCod(salesInfo.getSex());
			m.setManOrgCod(salesInfo.getBranch_id());
			m.setManOrgNam(salesInfo.getBranch_Name());
			m.setIdtypCod(salesInfo.getId_type());
			m.setIdNo(salesInfo.getId_no());
			String fixed_line = salesInfo.getFixed_line();
			String mobile = salesInfo.getMobile();
			String phs = salesInfo.getPhs();
			String tel = "";
			if (mobile != null && !mobile.equals("")) {
				tel = mobile;
			} else {
				if (phs != null && !phs.equals("")) {
					tel = phs;
				}
			}
			if ("".equals(tel)) {
				tel = fixed_line;
			}
			m.setSalesTel(fixed_line);
			m.setSalesMob(tel);

//					m.setSalesMail("N");
			m.setSalesAddr(salesInfo.getHome_address());
			m.setEducationCod(salesInfo.getEducation());
			m.setQualifiNo(salesInfo.getQualify_id());
			m.setCertifiNo(salesInfo.getDevelop_id());
			m.setContractNo(salesInfo.getContract_id());
			m.setStatusCod(salesInfo.getStat());
			m.setChannelCod(salesInfo.getChannel_id());
			m.setEntrantDate(salesInfo.getProbation_date().replace("-", ""));
			m.setIsCrosssale("1");
			m.setSalesTypCod(salesInfo.getEmploy_kind());
			// 设置团队ID
			m.setTeamId(salesInfo.getTeam_id());

			ResponseModel rm = unifiedJobNumberServiceImpl.getJobNumber(m);
			/*
			 * ResponseModel rm=new ResponseModel(); rm.setSid("396L002201906302962974");
			 * rm.setIdtypCod(""); rm.setIdNo("370213198307112828"); rm.setData("true");
			 */
			if (rm != null && (ResponseType.SALES_UPDATE_SUCCESS.equals(rm.getResponseType())
					|| ResponseType.REQUEST_JOBNUM_SUCCESS.equals(rm.getResponseType()))) {
				if (ResponseType.SALES_UPDATE_SUCCESS.equals(rm.getResponseType())) {
					// salesInfo.setSalesCode(m.getUniSalesCod());
					/*salesInfo.setSalesCode(salesInfo.getOrg_sales_code());
					salesInfo.setSales_id(salesInfo.getOld_sales_id());
					System.out.println("用老的工号：" + salesInfo.getOrg_sales_code() + "id:" + salesInfo.getOld_sales_id());*/
					/**
					 * @Package cn.com.sysnet.smis.hd.service.impl
					 * @author gb.z
					 * @date 2020/7/23 9:33
					 * @version V1.0
					 * @Copyright © 2020 新致（北京）有限公司
					 * L1241(协2020-1054)个险2020基本法
					 */
					System.out.println("生成新的工号：" + rm.getSalesCode());
					salesInfo.setSalesCode(rm.getSalesCode());
					salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));
				} else {
					System.out.println("生成新的工号：" + rm.getSalesCode());
					salesInfo.setSalesCode(rm.getSalesCode());
					salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));
				}
			} else {
				// by guo_cz for 预入司时在集团有，预入司表中没有添加 on 20190712
				String esd = rm.getErrDesc();
				String code = "";
				int info = -1;
				try {
					info = esd.indexOf("已入职，统一工号");
				} catch (NullPointerException e) {
					info = -1;
				}
				if (info > 0) {
					int ii = esd.indexOf(",姓名");
					int iii = ii - 10;
					String sales_codeinner = esd.substring(iii, ii);
					code = sales_codeinner.substring(0, 1);
					salesInfo.setSalesCode(sales_codeinner);

				}

				String id_noflag = esd.substring(3, 21);
				int branch = esd.indexOf("中国人民人寿保险股份有限公司");
				String branch_id1 = esd.substring(branch + 14, branch + 16);
				String branch_id2 = salesInfoDao_hd.getPrepareBranch_idFlag(branch_id1);
				String StrFlag = salesInfoDao_hd.getPrepareId_noFlag(id_noflag, branch_id2);
				if ("1".equals(StrFlag) || !"3".equals(code)) {
					System.out.println("返回失败信息123：" + rm.getErrDesc());
					salesInfo.setFlag("false&" + rm.getErrDesc());
					return salesInfo; // 生成失败
				} else {

						salesInfo.setSales_id(
								idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("错误信息：" + e.getMessage());
			salesInfo.setFlag("false");
			return salesInfo; // 生成失败
		}

		salesInfo.setSales_type(null);
		salesInfo.setBranch_id_four(null);
		salesInfo.setStatus_pre("1");
		salesInfoDao_hd.insertSalesInfo_prepareNew(salesInfo);// 插入人员表
		//插入内担保人信息
		staffInfoDao_yb.insertsalesinfoPrepareNew(salesInfo);

		/*******************************L1536 2021/07/30 caoh add start**************************************************/
		//插入近亲属相关信息到 t02salesinfo_relatives
		String salesRelativeJson = salesInfo.getSalesRelativeJson();
		String channelId = salesInfo.getChannel_id();
		String branchId = salesInfo.getBranch_id();
		String team_id = salesInfo.getTeam_id();
		String id_no = salesInfo.getId_no();
		String id_type = salesInfo.getId_type();

		String salesName = salesInfo.getSales_name();
		String salesId = salesInfo.getSales_id();
		List<SalesinfoRelatives> salesinfoRelativesList = new ArrayList<SalesinfoRelatives>();
		if(StringUtils.isNotEmpty(salesRelativeJson)){
			salesRelativeJson = salesRelativeJson.substring(0,salesRelativeJson.length()-1);
			String[] salesRelativeArr = salesRelativeJson.split(";");
			if(salesRelativeArr!=null && salesRelativeArr.length > 0){
				for (String salesRelativeTr:salesRelativeArr) {
					SalesinfoRelatives salesinfoRelative = new SalesinfoRelatives();
					String[] salesRelativeTds = salesRelativeTr.split(":");
					if(salesRelativeTds!=null&&salesRelativeTds.length>0){
						String close_relatives_name = salesRelativeTds[0];
						String close_relatives_sex = salesRelativeTds[1];
						String close_relatives_id_type = salesRelativeTds[2];
						String close_relatives_id_no = salesRelativeTds[3];
						String close_relatives_birthday = salesRelativeTds[4];
						String close_relatives_rela = salesRelativeTds[5];

						salesinfoRelative.setChannel_id(channelId);
						salesinfoRelative.setBranch_id(branchId);
						salesinfoRelative.setTeam_id(team_id);
						salesinfoRelative.setId_no(id_no);
						salesinfoRelative.setId_type(id_type);
						salesinfoRelative.setSales_id(salesId);
						salesinfoRelative.setSales_name(salesName);
						salesinfoRelative.setClose_relatives_name(close_relatives_name);
						salesinfoRelative.setClose_relatives_sex(close_relatives_sex);
						salesinfoRelative.setClose_relatives_id_type(close_relatives_id_type);
						salesinfoRelative.setClose_relatives_id_no(close_relatives_id_no);
						salesinfoRelative.setClose_relatives_birthday(close_relatives_birthday);
						salesinfoRelative.setClose_relatives_rela(close_relatives_rela);
						salesinfoRelative.setStat("1");
					}
					salesInfoDao_hd.insertSalesinfoRelative(salesinfoRelative);
				}
			}
		}
		/*******************************L1536 2021/07/30 caoh add end************************************************/

		/****************************L1536  2021/07/30 caoh add start*******************************************/
		//插入近亲属相关信息表

		/*******************************L1536  2021/07/30 caoh add start**************************************/

		// 删除序列号
		if (m != null && !StringUtils.isEmpty(salesInfo.getSalesCode()))
			unifiedJobNumberServiceImpl.deleteSid(m);

		return salesInfo;
	}
	public SalesInfo insertSalesInfoPrepare(SalesInfo salesInfo) {

		RequestModel m = null;
		try {
			m = new RequestModel();
			if ("01".equals(salesInfo.getChannel_id()) && salesInfo.getOrg_sales_code() != null
					&& !"".equals(salesInfo.getOrg_sales_code()) && salesInfo.getBranch_id4() != null
					&& !"".equals(salesInfo.getBranch_id4()) && salesInfo.getBranch_id() != null
					&& !"".equals(salesInfo.getBranch_id()) && salesInfo.getOld_sales_id() != null
					&& !"".equals(salesInfo.getOld_sales_id())) {
				if (salesInfo.getBranch_id().substring(0, 3).equals(salesInfo.getBranch_id4().substring(0, 3))) {
//					m.setMessageType("02");
//					m.setUniSalesCod(salesInfo.getOrg_sales_code());
					m.setMessageType("01");
					m.setUniSalesCod(null);
				} else {
					m.setMessageType("01");
					m.setUniSalesCod(null);
				}
			} else {
				m.setMessageType("01");
				m.setUniSalesCod(null);
			}
			m.setSalesNam(salesInfo.getSales_name());
			m.setDateBirthd(salesInfo.getBirthday());
			m.setSexCod(salesInfo.getSex());
			m.setManOrgCod(salesInfo.getBranch_id());
			m.setManOrgNam(salesInfo.getBranch_name());
			m.setIdtypCod(salesInfo.getId_type());
			m.setIdNo(salesInfo.getId_no());
			String fixed_line = salesInfo.getFixed_line();
			String mobile = salesInfo.getMobile();
			String phs = salesInfo.getPhs();
			String tel = "";
			if (mobile != null && !mobile.equals("")) {
				tel = mobile;
			} else {
				if (phs != null && !phs.equals("")) {
					tel = phs;
				}
			}
			if ("".equals(tel)) {
				tel = fixed_line;
			}
			m.setSalesTel(fixed_line);
			m.setSalesMob(tel);

//					m.setSalesMail("N");
			m.setSalesAddr(salesInfo.getHome_address());
			m.setEducationCod(salesInfo.getEducation());
			m.setQualifiNo(salesInfo.getQualify_id());
			m.setCertifiNo(salesInfo.getDevelop_id());
			m.setContractNo(salesInfo.getContract_id());
			m.setStatusCod(salesInfo.getStat());
			m.setChannelCod(salesInfo.getChannel_id());
			m.setEntrantDate(salesInfo.getProbation_date().replace("-", ""));
			m.setIsCrosssale("1");
			m.setSalesTypCod(salesInfo.getEmploy_kind());
			// 设置团队ID
			m.setTeamId(salesInfo.getTeam_id());

			ResponseModel rm = unifiedJobNumberServiceImpl.getJobNumber(m);
			/*
			 * ResponseModel rm=new ResponseModel(); rm.setSid("396L002201906302962974");
			 * rm.setIdtypCod(""); rm.setIdNo("370213198307112828"); rm.setData("true");
			 */
			if (rm != null && (ResponseType.SALES_UPDATE_SUCCESS.equals(rm.getResponseType())
					|| ResponseType.REQUEST_JOBNUM_SUCCESS.equals(rm.getResponseType()))) {
				if (ResponseType.SALES_UPDATE_SUCCESS.equals(rm.getResponseType())) {
					// salesInfo.setSalesCode(m.getUniSalesCod());
					/*salesInfo.setSalesCode(salesInfo.getOrg_sales_code());
					salesInfo.setSales_id(salesInfo.getOld_sales_id());
					System.out.println("用老的工号：" + salesInfo.getOrg_sales_code() + "id:" + salesInfo.getOld_sales_id());*/
					/**
					 * @Package cn.com.sysnet.smis.hd.service.impl
					 * @author gb.z
					 * @date 2020/7/23 9:33
					 * @version V1.0
					 * @Copyright © 2020 新致（北京）有限公司
					 * L1241(协2020-1054)个险2020基本法
					 */
					System.out.println("生成新的工号：" + rm.getSalesCode());
					salesInfo.setSalesCode(rm.getSalesCode());
					salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));
				} else {
					System.out.println("生成新的工号：" + rm.getSalesCode());
					salesInfo.setSalesCode(rm.getSalesCode());
					salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));

				}
			} else {
				// by guo_cz for 预入司时在集团有，预入司表中没有添加 on 20190712
				String esd = rm.getErrDesc();
				String code = "";
				int info = -1;
				try {
					info = esd.indexOf("已入职，统一工号");
				} catch (NullPointerException e) {
					info = -1;
				}
				if (info > 0) {
					int ii = esd.indexOf(",姓名");
					int iii = ii - 10;
					String sales_codeinner = esd.substring(iii, ii);
					code = sales_codeinner.substring(0, 1);
					salesInfo.setSalesCode(sales_codeinner);

				}

				String id_noflag = esd.substring(3, 21);
				int branch = esd.indexOf("中国人民人寿保险股份有限公司");
				String branch_id1 = esd.substring(branch + 14, branch + 16);
				String branch_id2 = salesInfoDao_hd.getPrepareBranch_idFlag(branch_id1);
				String StrFlag = salesInfoDao_hd.getPrepareId_noFlag(id_noflag, branch_id2);
				if ("1".equals(StrFlag) || !"3".equals(code)) {
					System.out.println("返回失败信息123：" + rm.getErrDesc());
					salesInfo.setFlag("false&" + rm.getErrDesc());
					return salesInfo; // 生成失败
				} else {
					if ("01".equals(salesInfo.getChannel_id())) {
						if ("".equals(salesInfo.getOrg_sales_code()) || salesInfo.getOrg_sales_code() == null) {
							salesInfo.setSales_id(
									idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));
						} else {
							salesInfo.setSalesCode(salesInfo.getOrg_sales_code());
							salesInfo.setSales_id(salesInfo.getOld_sales_id());
						}
					} else {
						salesInfo.setSales_id(
								idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("错误信息：" + e.getMessage());
			salesInfo.setFlag("false");
			return salesInfo; // 生成失败
		}

		salesInfo.setSales_type(null);
		salesInfo.setBranch_id4(null);
 
		salesInfoDao_hd.insertSalesInfo_prepare(salesInfo);// 插入人员表
		
		//插入介绍人信息到外担保人表 by wxy for L1620 
		if(!"".equals(salesInfo.getIntroduce_id())&&"".equals(salesInfo.getRecommend_id())&&"1".equals(salesInfo.getIs_comprehensive())){
			SalesInfo introduceInfo = salesInfoDao_hd.getIntroduceName(salesInfo);
			if(introduceInfo!=null){
				salesInfo.setAssurer_name(salesInfo.getIntroduce_name());
				salesInfo.setAssurer_id_no(introduceInfo.getId_no());
				salesInfo.setAssurer_id_type(introduceInfo.getId_type());
				salesInfo.setMobile(null);
				salesInfoDao_hd.insertAssurerInfo2(salesInfo);
			}
		}

		/*******************************L1536 2021/07/30 caoh add start**************************************************/
		//插入近亲属相关信息到 t02salesinfo_relatives
		String salesRelativeJson = salesInfo.getSalesRelativeJson();
		String channelId = salesInfo.getChannel_id();
		String branchId = salesInfo.getBranch_id();
		String team_id = salesInfo.getTeam_id();
		String id_no = salesInfo.getId_no();
		String id_type = salesInfo.getId_type();

		String salesName = salesInfo.getSales_name();
		String salesId = salesInfo.getSales_id();
		List<SalesinfoRelatives> salesinfoRelativesList = new ArrayList<SalesinfoRelatives>();
		if(StringUtils.isNotEmpty(salesRelativeJson)){
			salesRelativeJson = salesRelativeJson.substring(0,salesRelativeJson.length()-1);
			String[] salesRelativeArr = salesRelativeJson.split(";");
			if(salesRelativeArr!=null && salesRelativeArr.length > 0){
				for (String salesRelativeTr:salesRelativeArr) {
					SalesinfoRelatives salesinfoRelative = new SalesinfoRelatives();
					String[] salesRelativeTds = salesRelativeTr.split(":");
					if(salesRelativeTds!=null&&salesRelativeTds.length>0){
						String close_relatives_name = salesRelativeTds[0];
						String close_relatives_sex = salesRelativeTds[1];
						String close_relatives_id_type = salesRelativeTds[2];
						String close_relatives_id_no = salesRelativeTds[3];
						String close_relatives_birthday = salesRelativeTds[4];
						String close_relatives_rela = salesRelativeTds[5];

						salesinfoRelative.setChannel_id(channelId);
						salesinfoRelative.setBranch_id(branchId);
						salesinfoRelative.setTeam_id(team_id);
						salesinfoRelative.setId_no(id_no);
						salesinfoRelative.setId_type(id_type);
						salesinfoRelative.setSales_id(salesId);
						salesinfoRelative.setSales_name(salesName);
						salesinfoRelative.setClose_relatives_name(close_relatives_name);
						salesinfoRelative.setClose_relatives_sex(close_relatives_sex);
						salesinfoRelative.setClose_relatives_id_type(close_relatives_id_type);
						salesinfoRelative.setClose_relatives_id_no(close_relatives_id_no);
						salesinfoRelative.setClose_relatives_birthday(close_relatives_birthday);
						salesinfoRelative.setClose_relatives_rela(close_relatives_rela);
						salesinfoRelative.setStat("1");
					}
					salesInfoDao_hd.insertSalesinfoRelative(salesinfoRelative);
				}
			}
		}
		/*******************************L1536 2021/07/30 caoh add end************************************************/

		/****************************L1536  2021/07/30 caoh add start*******************************************/
		//插入近亲属相关信息表

		/*******************************L1536  2021/07/30 caoh add start**************************************/

		// 删除序列号
		if (m != null && !StringUtils.isEmpty(salesInfo.getSalesCode()))
			unifiedJobNumberServiceImpl.deleteSid(m);

		return salesInfo;
	}

	// 互动人员录入(特色)
	public SalesInfo insertSalesInfo_ts(SalesInfo salesInfo) {
		String workspace_id = publicFunctionManager
				.getVersionBeanByTeam(salesInfo.getChannel_id(), "", salesInfo.getTeam_id()).getWorkspace_id();
		String base_version_id = publicFunctionManager
				.getVersionBeanByTeam(salesInfo.getChannel_id(), "", salesInfo.getTeam_id()).getBase_version_id();
		//OB改造 ld 20240816
		salesInfo.setObranch_id(salesInfo.getBranch_id().substring(0, 3)+"0000");
		salesInfo.setWorkspace_id(workspace_id);
		salesInfo.setBase_version_id(base_version_id);
		if (salesInfo.getFlag() != null && !"temp".equals(salesInfo.getFlag())) { // 暂存不获取集团工号 M by wang_gq
			try {
				RequestModel m = new RequestModel();
				m.setMessageType("01");
				m.setUniSalesCod(null);
				m.setSalesNam(salesInfo.getSales_name());
				m.setDateBirthd(salesInfo.getBirthday());
				m.setSexCod(salesInfo.getSex());
				m.setManOrgCod(salesInfo.getBranch_id());
				m.setManOrgNam(salesInfo.getBranch_name());
				m.setIdtypCod(salesInfo.getId_type());
				m.setIdNo(salesInfo.getId_no());
				String fixed_line = salesInfo.getFixed_line();
				String mobile = salesInfo.getMobile();
				String phs = salesInfo.getPhs();
				if (fixed_line == null && mobile != null) {
					fixed_line = mobile;
				} else if (fixed_line == null && phs != null) {
					fixed_line = phs;
				}
				if (mobile == null) {
					mobile = fixed_line;
				}
				m.setSalesTel(fixed_line);
				m.setSalesMob(mobile);
//				m.setSalesMail("N");
				m.setSalesAddr(salesInfo.getHome_address());
				m.setEducationCod(salesInfo.getEducation());
				m.setQualifiNo(salesInfo.getQualify_id());
				m.setCertifiNo(salesInfo.getDevelop_id());
				m.setContractNo(salesInfo.getContract_id());
				m.setStatusCod(salesInfo.getStat());
				m.setChannelCod(salesInfo.getChannel_id());
				m.setEntrantDate(salesInfo.getProbation_date().replace("-", ""));
				m.setIsCrosssale("1");
				m.setSalesTypCod(salesInfo.getEmploy_kind());
				ResponseModel rm = unifiedJobNumberServiceImpl.getJobNumber(m);
				if (rm == null || rm.getSalesCode() == null
						|| ResponseType.MESSAGE_FORMAT_ERROR.equals(rm.getResponseType())) { // 生成失败
					salesInfo.setFlag("false");
					return salesInfo; // 生成失败
				} else {
					salesInfo.setSalesCode(rm.getSalesCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
				salesInfo.setFlag("false");
				return salesInfo; // 生成失败
			}
			salesInfo.setSales_id(idGenerator.getSalesID(salesInfo.getChannel_id(), salesInfo.getBranch_id()));

			salesInfoDao_hd.insertSalesInfo_ts(salesInfo);

			salesInfoDao_hd.updateT02depositinfo_SALES_ID_By(salesInfo);// 根据渠道、证件类型和证件号码更新t02depositinfo中的SALES_ID
			// 插入接口日志
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					CodeTypeConst.CODE_CODE_UPLOAD_SALES_ADD, salesInfo.getUser_id(), salesInfo.getModuleIdCur());
			// A by chen_zw for L247 on 20151021 代码段二
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					ConstantEnum.UploadMobilePlatformType.UPLOAD_MOBILE_02_2.getCode(), salesInfo.getUser_id(),
					salesInfo.getModuleIdCur());
            //插入互联网核心日志
			dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
					CodeTypeConst.CODE_CODE_UPLOAD_SALES_ADD_FORINTERNET, salesInfo.getUser_id(), salesInfo.getModuleIdCur());
			
			// 备份表
			businessManagerDao.backupTable("T02SALESINFO", salesInfo.getModuleIdCur(), null, salesInfo.getChannel_id(),
					salesInfo.getSales_id(), salesInfo.getUser_id(), DataConst.Data_Operate_Type_Insert);
		} else {
			salesInfoDao_hd.insertSalesInfo_ts(salesInfo);
		}

		return salesInfo;
	}

	// 人员维护查询
	public String querySalesInfo(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.querySalesInfoCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.querySalesInfo(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {
				String stat = "";
				String rank = "";
				String plan_name = ""; // a by guo_cz for L574 on 20180408
				String team_type = codecodeDao.queryCodeName(info.getTeam_type(), DataConst.Channel_ID_Hd,
						CodeTypeConst.CODE_TYPE_ORGANIZATION_TYPE);// 查询组织类别名称；

				if (info.getStat() != null && !"".equals(info.getStat())) {
					stat = codecodeDao.queryCodeName(info.getStat(), DataConst.Channel_ID_Hd,
							CodeTypeConst.CODE_TYPE_STAFF_STATUS);
				}

				if (info.getRank() != null && !"".equals(info.getRank())) {
					// 职级方法调整
					// rank = rankdefDao.queryRanName(DataConst.Channel_ID_Hd,
					// info.getRank(),info.getTeam_type());//根据组织类型查询对应的业务职级
					rank = rankdefDao.queryRankName(DataConst.Channel_ID_Hd, info.getRank(), info.getBase_version_id());
				}
				// a by guo_cz for L574 on 20180408
				if (info.getPlan_name() != null && !"".equals(info.getPlan_name())) {
					if ("0".equals(info.getPlan_name()) || info.getPlan_name() == "0") {
						plan_name = "";
					} else {
						plan_name = codecodeDao.queryCodeName(info.getPlan_name(), DataConst.Channel_ID_Hd,
								CodeTypeConst.CODE_TYPE_MARITAL_PLAN);
					}

				}
				//add by chengyy  for L2188 start
				String styles = salesInfo.getStyles();
				if("001".equals(styles)) {
					styles = "金钻会员";
				}else if("002".equals(styles)) {
					styles = "银钻会员";
				}else if("003".equals(styles)) {
					styles = "钻石会员";
				}else if("004".equals(styles)) {
					styles = "";
				}else {
					styles = info.getStyles();
				}
				//add by chengyy  for L2188 end
				// M by li_chx for R303集团统一工号3期 on 20141106
				// L541 互动组织归并 添加team_id 查询参数 qin_ly 2018年2月2日
				json += "{id:'" + info.getSales_id() + "',params:'" + info.getBranch_id() + "&team_type="
						+ info.getTeam_type()+"&sales_star="+info.getSales_star() + "&team_id=" + info.getTeam_id() + "&isexcperson="
						+ info.getIsexcperson() + "&isDiaMem=" + info.getIsdiamem() + "&diaMem_Level="+ info.getDiamem_level()
						+ "&diamond_styles="+styles//add by chengyy  for L2188
						+ "&golddia_level=" +info.getGolddia_level()//add by chengyy  for L2188
						+ "&silverdia_level=" +info.getSilverdia_level()//add by chengyy  for L2188
					    + "&isStar=" + info.getIsStar()+"&star_Level=" + info.getStar_level()
						//A by lc for L2630 追加字段“主管代码”、“主管姓名” begin
						+ "&leader_id=" + info.getLeader_id()+"&leader_name=" + info.getLeader_name()+"&leader_code=" + info.getLeader_code()
						//A by lc for L2630 追加字段“主管代码”、“主管姓名” end
						+ "',sales_id:'" + info.getSalesCode() + "',sales_name:'"
						+ info.getSales_name() + "',rank:'" + rank + "',team_id:'" + info.getTeam_id() + "',team_name:'"
						+ info.getTeam_name() + "',team_type:'" + team_type + "',plan_name:'" + plan_name
						+ "',plan_time:'" + info.getPlan_time()
						+ "',isStar:'" + info.getIsStar() + "',star_Level:'"+info.getStar_level()
						+ "',stat:'" + stat + "',isexcperson:'"+ info.getIsexcperson() 
						+ "',isDiaMem:'" + info.getIsdiamem()+ "',diaMem_Level:'" + info.getDiamem_level()
						+ "',is_silverdiamond:'"+info.getIs_silverdiamond()+"',silverdia_level:'"+info.getSilverdia_level()//add by chengyy  for L2188
						+ "',is_golddiamond:'"+info.getIs_golddiamond()+"',golddia_level:'"+info.getGolddia_level()//add by chengyy  for L2188
						+"',is_pioneer:'"+info.getIs_pioneer()//a by qsw for L2498是否万明销售先锋
						+"',plannerLevel:'"+info.getPlannerlevel()
						//A by lc for L2630 追加字段“主管代码”、“主管姓名” begin
						+"',leader_id:'"+info.getLeader_id()+"',leader_name:'"+info.getLeader_name()+"',leader_code:'"+info.getLeader_code()
						//A by lc for L2630 追加字段“主管代码”、“主管姓名” end
						+ "',action:'维护'},";// a by guo_cz for L574 on 20180408

			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("人员维护查询 = " + json);

		return json;
	}

	/**
	 * 人员信息查询
	 */
	public String querySales(int limit, int start, SalesInfo salesInfo) {

		// 判断入参salesId是否为10位，如果为10位就转换成7位 a by ge_xd for R275 on 20140819
		if (salesInfo.getSales_id() != null && !"".equals(salesInfo.getSales_id())
				&& salesInfo.getSales_id().trim().length() == 10) {

			String salesId = publicMethodManagerImpl.getSalesID(salesInfo.getSales_id());
			salesInfo.setSales_id(salesId);

		}
		int count = salesInfoDao_hd.querySalesCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.querySales(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {
				String stat = info.getStat();
				String rank = info.getRank();
				String team_type = info.getTeam_type();
				String team_type_num = info.getTeam_type_num();
				//a by wzj for L1540 start
				String property_networks=info.getProperty_networks();
				if(StringUtils.isEmpty(info.getAccredit_org()) && "n".equals(info.getItem_type()))
					property_networks="-";
				//a by wzj for L1540 end

				/**
				 * String team_type =
				 * codecodeDao.queryCodeName(info.getTeam_type(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_ORGANIZATION_TYPE);//查询组织类别名称；
				 *
				 * if(info.getStat()!=null&&!"".equals(info.getStat())) { stat =
				 * codecodeDao.queryCodeName(info.getStat(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_STAFF_STATUS);
				 * }
				 */
				if (info.getRank() != null && !"".equals(info.getRank())) {
					// rank = rankdefDao.queryRanName(DataConst.Channel_ID_Hd,
					// info.getRank(),info.getTeam_type());//根据组织类型查询对应的业务职级
				}

				String status = info.getStat();
				//m by qsw for L2325 on 2023-08-03
				if (status.equals(DataConst.Sales_Status_Dimission)||status.equals("解约")) {
					info.setAction("详细信息,合同下载,历史入司查询");
				} else {
					int count1 = salesInfoDao_hd.querySalesInfoMovePhotoCount(info.getSalesCode());
					if (count1 > 0) {
						info.setAction("详细信息,合同下载,历史入司查询");
					} else {
						info.setAction("详细信息,历史入司查询");
					}
				}
				int count2 = salesInfoXQDao.querySalesDimissCount(info.getSalesCode(), info.getSales_id() );
				if(count2>0){
					info.setAction("详细信息,合同下载,历史入司查询");
				}
				json += "{id:'" + info.getSales_id() + "',params:'" + info.getBranch_id() + "&team_type="
						+ info.getTeam_type() + "&isexcperson=" + info.getIsexcperson() + "&sales_star=" + info.getSales_star() + "&team_type_num="
						+ team_type_num + "',sales_id:'" + info.getSales_id() + "',sales_name:'" + info.getSales_name()
						+ "',version_name:'" + info.getVersion_name() + "',rank:'" + rank +
						//a by wzj for L1003 增加管理归属机构 on 2020-8-31 15:52:08 start
						"',mbranch_id:'"+info.getMbranch_id()+
						"',mbranch_name:'"+info.getMbranch_name()+
						//a by wzj for L1003 增加管理归属机构 on 2020-8-31 15:52:08 end
						"',team_id:'" + // a by
																											// guo_cz
																											// for L574
																											// on
																											// 20180408
						info.getTeam_id() + "',team_name:'" + info.getTeam_name() + "',team_type:'" + team_type
						+"',is_comprehensive:'" + info.getIs_comprehensive() 
						//a by hbl for L1846 start
						+ "',commissioner_code:'" + info.getCommissioner_code()
						+"',commissioner_name:'" + info.getCommissioner_name()
						//a by hbl for L1846 end
						+ "',introduce_id:'" + info.getIntroduce_id()
						+"',introduce_name:'" + info.getIntroduce_name()
						+ "',plan_name:'" + info.getPlan_name() + "',plan_time:'" + info.getPlan_time()
						+ "',isexcperson:'" + info.getIsexcperson() + "',isDiaMem:'" + info.getIsdiamem()
						+ "',diaMem_Level:'" + info.getDiamem_level()
						+"',isStar:'" + info.getIsStar()+ "',star_Level:'" + info.getStar_level()
						+ "',stat:'" + stat + "',deduction:'" + info.getDeduction()
						+ "',combine_score:'" + info.getCombine_score()
						+ "',score_date:'" + info.getScore_date()
						//a by wzj for L1540 start
						+ "',property_networks:'"+property_networks
						//a by wzj for L1540 end
						+ "',is_full_time_education:'"+info.getIs_full_time_education() //L2113 增加字段
						+ "',is_whitelist:'"+info.getIs_whitelist()
						+ "',isDiaMem:'" + info.getIsdiamem()+ "',diaMem_Level:'" + info.getDiamem_level()//add by chengyy  for L2188
						+ "',is_silverdiamond:'"+info.getIs_silverdiamond()+"',silverdia_level:'"+info.getSilverdia_level()//add by chengyy  for L2188
						+ "',is_golddiamond:'"+info.getIs_golddiamond()+"',golddia_level:'"+info.getGolddia_level()//add by chengyy  for L2188
						+"',is_pioneer:'"+info.getIs_pioneer()//a by qsw for L2498是否万名销售先锋
						+"',plannerLevel:'"+info.getPlannerlevel()
						+ "',action:'" + info.getAction() + "',salesCode:'" + info.getSalesCode()
						//L2047 添加 执业证编号、预授权资质、已授权资质、资质颁发日期、待考资质、考试状态
						+ "',prof_no:'" + info.getProf_no()
						+ "',preauthorization_level:'" + info.getPreauthorization_level()
						+ "',certificate_type:'" + info.getCertificate_type()
						+ "',certificate_releasedate:'" + info.getCertificate_releasedate()
						+ "',exam_subject:'" + info.getExam_subject()
						+ "',exam_state:'" + info.getExam_state()
						+ "',dismiss_time:'" + info.getDismiss_time()//a by qsw L2893
						//L2047 添加 执业证编号、预授权资质、已授权资质、资质颁发日期、待考资质、考试状态
						+ "',branch_id2:'" + info.getBranch_id2()
						+ "',branch_id3:'" + info.getBranch_id3()
						+ "',branch_id:'" + info.getBranch_id()
						+ "',branch_name:'" + info.getBranch_name()
						+ "',branch_name2:'" + info.getBranch_name2()
						+ "',branch_name3:'" + info.getBranch_name3()
						+ "',leader_id:'" + info.getLeader_id()
						+ "',leader_name:'" + info.getLeader_name()
						+ "',enter_rank:'" + info.getEnter_rank()
						+ "',probation_date:'" + info.getProbation_date()
						+ "',first_probation_dates:'" + info.getFirst_probation_dates()
						+ "',assess_start_date:'" + info.getAssess_start_date()
						+ "',sales_type:'" + info.getSales_type()
						+ "',accredit_org_name:'" + info.getAccredit_org_name()
						+ "',accredit_org:'" + info.getAccredit_org()
						+ "',employ_kind:'" + info.getEmploy_kind()
						+ "',sex:'" + info.getSex()
						+ "',education:'" + info.getEducation()
						+ "',political_stat:'" + info.getPolitical_stat()
						+ "',partyMemberVerified:'" + info.getPartyMemberVerified()
						+ "',partyAffiliationTransferred:'" + info.getPartyAffiliationTransferred()
						+ "'},";

			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("人员维护查询 = " + json);

		return json;
	}
	//2047,L2380 产险营销员分级信息导入 查询
	public String querySalesImportInfo(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.querySalesImportCount(salesInfo);
		List<SalesInfo> list = salesInfoDao_hd.querySalesImport(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			String branchIdCur = list.get(0).getOpt_branchid_2().substring(0,3);
			for (SalesInfo info : list) {
				json += "{id:'" + info.getSales_id()
						+ "',branch_id1:'" + info.getBranch_id1() + "',branch_name1:'"+info.getBranch_name1()
						+ "',branch_id2:'" + info.getBranch_id2() + "',branch_name2:'"+info.getBranch_name2()
						+ "',branch_id3:'" + info.getBranch_id3() + "',branch_name3:'"+info.getBranch_name3()
						+ "',branch_id:'" + info.getBranch_id() + "',branch_name:'"+info.getBranch_name()
						+ "',sales_code:'" + info.getSales_code()
						+ "',inside_id:'" + info.getInside_id()
						+ "',sales_name:'" + info.getSales_name()
						+ "',prof_no:'" + info.getProf_no()
						+ "',preauthorization_level:'"
						+ (((!"153".equals(branchIdCur))&&("未定级".equals(info.getPreauthorization_level())))?"":info.getPreauthorization_level())
						+ "',preauthorization_lapsedate:'"
						+ (((!"153".equals(branchIdCur))&&("未定级".equals(info.getPreauthorization_level())))?"":info.getPreauthorization_lapsedate())
						+ "',certificate_type:'"
                        + (((!"153".equals(branchIdCur))&&("未定级".equals(info.getCertificate_type())))?"":info.getCertificate_type())
						+ "',certificate_releasedate:'"
                        + (((!"153".equals(branchIdCur))&&("未定级".equals(info.getCertificate_type())))?"":info.getCertificate_releasedate())
						+ "',people_sign:'" + info.getPeople_sign()
						+ "'},";
			}
			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("产险营销员分级信息查询 = " + json);

		return json;
	}
	//2380
	public String querySalesImportInfoP(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.querySalesImportCountP(salesInfo);
		List<SalesInfo> list = salesInfoDao_hd.querySalesImportP(limit, start, salesInfo);
		//查询司龄，set一下
		//把一次入司的人员 list1 查询出来
		//把二次入司的正常人员 list2Normal 查询出来
		//把二次入司的异常人员 list2Abnormal 查询出来

//		String flag ="";
//		for (SalesInfo info:list){
//			for (SalesInfo info1:list1){
//				break;
//			}
//			if (flag.equals("")){
//				for (SalesInfo info2Normal:list2Normal){
//
//					break;
//				}
//			}
//
//			if (flag.equals("")){
//				for (SalesInfo info2Abnormal:list2Abnormal){
//
//					break;
//				}
//			}
//		}

		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {
				if ("1530000".equals(info.getBranch_id2())){
					if ("0".equals(info.getPreauthorization_level())||"".equals(info.getPreauthorization_level())){
						info.setPreauthorization_level("未定级");
					}
					if ("0".equals(info.getCertificate_type())||"".equals(info.getCertificate_type())){
						info.setCertificate_type("未定级");
					}


				}
				//以身份证为维度获取首次入司日期  ，增加最近一次入司日期、是否二次入司和首次入司日期
				SalesInfo sh = salesInfoDao_hd.querySalesInfoProbation(info.getId_no());

				json += "{id:'" + info.getSales_id()
//						+ "',branch_id1:'" + info.getBranch_id1() + "',branch_name1:'"+info.getBranch_name1()
						+ "',branch_id2:'" + info.getBranch_id2() + "',branch_name2:'"+info.getBranch_name2()
						+ "',branch_id3:'" + info.getBranch_id3() + "',branch_name3:'"+info.getBranch_name3()
						+ "',branch_id:'" + info.getBranch_id() + "',branch_name:'"+info.getBranch_name()
						+ "',sales_code:'" + info.getSales_code() + "',sales_name:'" + info.getSales_name()
						+ "',prof_no:'" + info.getProf_no()
						+ "',preauthorization_level:'"
//						+ info.getPreauthorization_level()
						+ (("1370000".equals(info.getBranch_id2()))?"":info.getPreauthorization_level())
						+ "',preauthorization_lapsedate:'"
						+ ((info.getPreauthorization_level()==null||"".equals(info.getPreauthorization_level())
						||"1370000".equals(info.getBranch_id2()))?"":info.getPreauthorization_lapsedate())
						+ "',certificate_releasedate:'" + info.getCertificate_releasedate()
						+ "',certificate_type:'" + info.getCertificate_type()
						+ "',is_next_condition_morality:'" + info.getIs_next_condition_morality()
						+ "',is_next_knowledge:'" + info.getIs_next_knowledge()
						+ "',special_result:'" + info.getSpecial_result()
						+ "',primary_time:'" + info.getPrimary_time()
						+ "',middle_time:'" + info.getMiddle_time()
						+ "',high_time:'" + info.getHigh_time()
						+ "',special_time:'" + info.getSpecial_time()
						+ "',education:'" + info.getEducation()
						+ "',com_age:'" + info.getCom_age()
						+ "',is_major:'" + info.getIs_major()
						+ "',major_name:'" + info.getMajor_name()
						+ "',Quality_penalty_time:'" + info.getQuality_penalty_time()
						+ "',special_result:'" + info.getSpecial_result()
						+ "',rank_name:'" + info.getRank_name()
						+ "',is_entry:'" + info.getIs_entry()
						+ "',stat:'" + info.getStat();
				        //a qsw for L2930
				        if ("01".equals(salesInfo.getChannel_id())||"05".equals(salesInfo.getChannel_id())||"08".equals(salesInfo.getChannel_id())||"09".equals(salesInfo.getChannel_id())) {
							if ("0000".equals(salesInfo.getBranch_id().substring(3))&&("初级".equals(info.getCertificate_type())||"中级".equals(info.getCertificate_type()))&&
									(!"113".equals(info.getBranch_id2().substring(0,3))&&!"137".equals(info.getBranch_id2().substring(0,3))&&!"145".equals(info.getBranch_id2().substring(0,3))&&!"153".equals(info.getBranch_id2().substring(0,3)))){
				        	json+= "',ac:'<a href=\"salesInfo_hd.do?method=salesInfoInitModify&branch_name2=" + info.getBranch_name2()
				        			+ "&branch_name3=" + info.getBranch_name3() + "&branch_name4="+info.getBranch_name()+"&sales_code=" +info.getSales_code() + "&sales_name=" + info.getSales_name()
				        			+ "&certificate_type=" + info.getCertificate_type() + "&education=" +info.getEducation() + "&is_major=" +info.getIs_major()+ "&branch_id2=" +info.getBranch_id2()+ "\">" + "修改资质等级" + "</a>";
				        }else{
				        	json+= "',ac:'" + "修改资质等级";
				        }}
						if (sh != null ){
							json += "',first_probation_dates:'" + sh.getFirst_probation_dates()
									+ "',new_probation_date:'" + sh.getNew_probation_date();
						}else{
							json += "',first_probation_dates:'" + ""
									+ "',new_probation_date:'" + "";
						}

						json += "'},";
			}
			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("营销员分级信息查询 = " + json);

		return json;
	}
	//营销员资质分级修改 a qsw L2930
	public String modifyP(SalesInfo salesInfo) {
		String res = "";

		FormFile file = (FormFile)salesInfo.getFile();
		if (file!=null&&!"".equals(file)) {
			if (file.getFileSize() > 5242880) {
				res = "目前支持最大上传文件为5M";
				return res;
			}

			//2.存储附件
			try {

				/*String url = "http://" + QualityUrlUtil.url
						+ "/selfOfEntry/staffInfoUpload" + "?apikey="
						+ QualityUrlUtil.apikey;*/
				String url = codecodeDao.queryCodeName(CodeTypeConst.CODE_TYPE_URLS_01,CodeTypeConst.CODE_TYPE_URLS_CHANNEL_TYPE,CodeTypeConst.CODE_TYPE_URLS);
				String[] params = { "channel_id=" + salesInfo.getChannel_id(),
						"user_id=" + salesInfo.getUser_id(),
						"branch_id=" + salesInfo.getBranchId(),
						"new_file_name=" + salesInfo.getVersion_name() ,
						"pic_file_url=" + "qualificationRg/"};
				String result_save = HttpClientUtil.PostMethodForInStream(url, file.getInputStream()
						, file.getFileName(), params);
				JSONObject json_result_save = JSONObject.fromObject(result_save);
				String code = (String) json_result_save.get("code");
				String msg = (String) json_result_save.get("msg");
				String dataInfo = (String) json_result_save.get("dataInfo");

				if (code.equals("0")) {//上载成功
					salesInfoDao_hd.updateP(salesInfo);//更新资质等级表
					salesInfoDao_hd.insertP(salesInfo);//插入轨迹表
				} else {
					res = "证明资料上传失败，修改失败！";
				}
			} catch (Exception e) {
				System.out.println("文件上载接口异常，修改失败！");
				e.printStackTrace();
				res = "文件上载接口异常，修改失败！";
			}
		}else{
			salesInfoDao_hd.updateP(salesInfo);//更新资质等级表
			salesInfoDao_hd.insertP(salesInfo);//插入轨迹表
		}
		return res;
	}

	@Override
	public String queryIdaInfo(SalesInfo salesInfo) {
		int count = salesInfoDao_hd.queryIdaCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryIdaInfo(salesInfo);
		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				SalesInfo info = list.get(i);
				json += "{" + "branch_id2:'" + info.getBranch_id2()
						+ "',branch_id3:'" + info.getBranch_id3()
						+ "',branch_id4:'" + info.getBranch_id()
						+ "',branch_name2:'" + info.getBranch_name2()
						+ "',branch_name3:'" + info.getBranch_name3()
						+ "',branch_name4:'" + info.getBranch_name()
						+ "',sales_code:'" + info.getSales_code()
						+ "',sales_name:'" + info.getSales_name()
						+ "',team_id:'" + info.getTeam_id()
						+ "',team_name:'" + info.getTeam_name()
						+ "',rank:'" + info.getRank()
						+ "',stat:'" + info.getStat()
						+ "',fyc:'" + info.getFyc()
						+ "',piece:'" + info.getPiece()
						+ "',year:'" + salesInfo.getYear()
						+ "',isreached:'" + info.getIsreached()
						+" '}" ;
				if (i != list.size() - 1) {
					json += ",";
				}


			}

			json += "]}";
			}


		return json;
	}

	@Override
	public String queryKyKeep(SalesInfo salesInfo) {
		int count = salesInfoDao_hd.queryKykeepCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryKykeepInfo(salesInfo);
		String evaluation_result = "";
		/*String protect_end = "0";*/
		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				SalesInfo info = list.get(i);
				String abc="disabled=\"disabled\"";
				/*if(list.get(i).getProtect_end() != null && !"".equals(list.get(i).getProtect_end())){
					protect_end = list.get(i).getProtect_end();
				}*/
				if(salesInfo.getStat_month().equals(info.getYear_month() )&& salesInfo.getStat_month().equals(salesInfo.getStat_yearmonth())
				/*&& !salesInfo.getStat_month().equals(protect_end)*/){//评定佣金月佣金确认后至月底，逾期将一律无法录入系统
					//非系统当前佣金月时
					//保护期结束后的第一个月不能特殊维持
					evaluation_result="<select id=\"evaluation_result"+i+"\"><option value=0 selected = \"selected\">未达标</option> <option value=1 >达标</option> </select>";
				}else{
					evaluation_result="<select id=\"evaluation_result\" "+abc+"><option value=0 >未达标</option><option value=1 >达标</option> </select>";
				}
				json += "{" + "id:'" +info.getSales_code()+"&"+info.getBranch_id2()
						+"&"+info.getBranch_id()+"&"+info.getCon_month()+"&"+salesInfo.getChannel_id()
						+"&"+info.getIs_keep_achievement()
						+ "',params:'p&sales_id="+ info.getSales_id()
						+ "',branch_id2:'" + info.getBranch_id3()
						+ "',branch_id3:'" + info.getBranch_id3()
						+ "',branch_id:'" + info.getBranch_id()
						+ "',branch_name2:'" + info.getBranch_name2()
						+ "',branch_name3:'" + info.getBranch_name3()
						+ "',branch_name:'" + info.getBranch_name()
						+ "',sales_code:'" + info.getSales_code()
						+ "',sales_name:'" + info.getSales_name()
						+ "',team_id:'" + info.getTeam_id()
						+ "',stat_month:'" + info.getStat_month()
						+ "',team_name:'" + info.getTeam_name()
						+ "',plannerlevel:'" + info.getPlannerlevel()
						+ "',con_month:'" + info.getCon_month()
						+ "',evaluation_result:'" + evaluation_result
						+ "',reasons:'" + info.getReasons()
						+"',selectStat:'"+"selectStat"+i
						+ "',user_id:'" + salesInfo.getUser_id()
						+" '}" ;
				if (i != list.size() - 1) {
					json += ",";
				}

			}

			json += "]}";
		}

System.out.println(json);
		return json;
	}
	@Override
	public String queryKy(SalesInfo salesInfo) {
		int count = salesInfoDao_hd.queryKyCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryKyInfo(salesInfo);

		String json = "{totalCount:" + count + ",root:[";
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				SalesInfo info = list.get(i);
				json += "{" + "id:'" +info.getSales_code()+"&"+info.getBranch_id2()
						+"&"+info.getBranch_id()+"&"+info.getCon_month()+"&"+salesInfo.getChannel_id()
						+ "',params:'p&sales_id="+ info.getSales_id()
						+ "',branch_id2:'" + info.getBranch_id3()
						+ "',branch_id3:'" + info.getBranch_id3()
						+ "',branch_id:'" + info.getBranch_id()
						+ "',branch_name2:'" + info.getBranch_name2()
						+ "',branch_name3:'" + info.getBranch_name3()
						+ "',branch_name:'" + info.getBranch_name()
						+ "',sales_code:'" + info.getSales_code()
						+ "',sales_name:'" + info.getSales_name()
						+ "',team_id:'" + info.getTeam_id()
						+ "',stat_month:'" + info.getStat_month()
						+ "',team_name:'" + info.getTeam_name()
						+ "',plannerlevel:'" + info.getPlannerlevel()
						+ "',rank:'" + info.getRank_name()
						+ "',stat:'" + info.getStat()
						+ "',protect_start:'" + info.getProtect_start()
						+ "',protect_end:'" + info.getProtect_end()
						+" '}" ;
				if (i != list.size() - 1) {
					json += ",";
				}
				json = json.replace("null", "");
			}


		}
		 json += "]}";
     json= json.replace("\"", "\\\"");

		return json;
	}
	@Override
	public String saveKyKeep(String params) {
		String[] policynos = params.split(",");
		SalesInfo salesinfo =new SalesInfo();
		for(int i=0;i<policynos.length;i++){
			String[] arr = policynos[i].split("&");
			salesinfo.setSales_code(arr[0]);
			salesinfo.setBranch_id2(arr[1]);
			salesinfo.setBranch_id(arr[2]);
			String monthStr = "0";
			// 查找"月"字位置
			if (arr[3] != null || !"".equals(arr[3])) {
				int index = arr[3].indexOf("月");
				if (index > 0) {
					 monthStr = arr[3].substring(0, index);
					 monthStr = String.valueOf(Integer.parseInt(monthStr)+1);
				}
			}
			salesinfo.setCon_month(monthStr);
			salesinfo.setChannel_id(arr[4]);
			salesinfo.setIs_keep_achievement(arr[5]);
			String evaluation_result = "0";
			String entitlementTiers = "";
			//防止业务员选择内容同时包含达成和未达成的情况
			if (arr[6] != null || !"".equals(arr[6])) {
				if(arr[6].equals("1")){//达成
					evaluation_result = "2";
					if(salesinfo.getIs_keep_achievement() == "2"){//如果保护期评定结果未健康养老规划师，那么权益就是null
						entitlementTiers =String.valueOf( Integer.parseInt(salesinfo.getIs_keep_achievement()) -1); //规划师权益等级
					}
				}
			}
			salesinfo.setEvaluation_result(evaluation_result);
			salesinfo.setEntitlementTiers(entitlementTiers);
			int currentConMonth = Integer.parseInt(salesinfo.getCon_month());
			// 对值进行加1操作
			currentConMonth++;
			// 将加1后的整数值转回字符串并设置回对象
			salesinfo.setCon_month(String.valueOf(currentConMonth));
			if(evaluation_result == "2"){
				salesInfoDao_hd.saveKyKeep(salesinfo);  //更新评定结果表
				salesInfoDao_hd.saveKyKeep1(salesinfo); //更新人员表
				salesInfoDao_hd.saveKyKeep2(salesinfo); //更新人员备份表
			}
		}
		return "提交成功！";
	}

	@Override
	public String getStatMonth(String channel_id, String branch_id) {
		return salesInfoDao_hd.queryStatmonth(branch_id, channel_id);
	}

	// a by shiyawei for l824
	public String querySalesPrepare(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.queryPrepareSalesCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryPrepareSales(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {

				// a by ni_f 添加删除功能
				json += "{id:'" + info.getSales_id() + "',params:'" + info.getSalesCode()
						+ "',sales_id:'"+ info.getSales_id() + "',sales_name:'" + info.getSales_name()
						+ "',employ_kind:'" + info.getEmploy_kind()
				/*update liu_yl forL1611服营新军 on 20211230 start*/
						+ "',id_type:'"+ info.getId_type() + "',id_no:'" + info.getId_no() + "',salesCode:'"
						+ info.getSalesCode() + 
						"',combine_score:'"+ info.getCombine_score();
				if((null==info.getCombine_score()||"".equals(info.getCombine_score())) && ("05".equals(salesInfo.getChannel_id())||"01".equals(salesInfo.getChannel_id())|| "09".equals(salesInfo.getChannel_id()))) {//add by qx for L2411 新军
					json +="',action:'删除,重新获取E测得分";
				}else {
					json +="',action:'删除";
				}
				json += "'},";
				/*update liu_yl forL1611服营新军 on 20211230 end*/
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		return json;
	}

	public String querySalesPrepareNew(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.queryPrepareSalesCountNew(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryPrepareSalesNew(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {

				// a by ni_f 添加删除功能
				json += "{id:'" + info.getSales_id() + "',params:'" + info.getSalesCode()
						+ "&id_no="+info.getId_no()+ "&status_pre="+info.getStatus_pre()+"&sales_code="+info.getSalesCode()
						+ "',sales_id:'"+ (info.getStatus_pre()!=null&&"0".equals(info.getStatus_pre())?"" : info.getSales_id()) + "',sales_name:'" + info.getSales_name()
						+ "',employ_kind:'" + info.getEmploy_kind()
						+ "',branch_id:'" + info.getBranch_id()
						+ "',branch_name:'" + info.getBranch_name()
						+ "',status_pre:'" +( info.getStatus_pre().equals("1")?"定稿":"草稿")
						+ "',service_branch_name:'" + info.getService_branch_name()
						/*update liu_yl forL1611服营新军 on 20211230 start*/
						+ "',id_type:'"+ info.getId_type() + "',id_no:'" + info.getId_no() + "',salesCode:'"
						+ (info.getStatus_pre()!=null&&"0".equals(info.getStatus_pre())?"" : info.getSalesCode()) +
						"',combine_score:'"+ info.getCombine_score();
						if( "0".equals(info.getStatus_pre())){
							json +="',action:'删除,编辑";
						}else{
							//任务状态（1：暂存，2：审核中，3：审核不通过，4：审核通过）
							//如果为空或者为null，没关联上，说明没有登记任务 ，允许删除
							//如果为暂存 ，允许删除
							if (info.getTask_stat() == null || "".equals(info.getTask_stat()) || "1".equals(info.getTask_stat())){
								json +="',action:'删除,查看明细";
							}else{
								json +="',action:'查看明细";
							}
						}
//				if((null==info.getCombine_score()||"".equals(info.getCombine_score())) && ("05".equals(salesInfo.getChannel_id())||"01".equals(salesInfo.getChannel_id())|| "09".equals(salesInfo.getChannel_id()))) {//add by qx for L2411 新军
//					json +="',action:'删除,重新获取E测得分";
//				}else {
//					json +="',action:'删除";
//				}
				json += "'},";
				/*update liu_yl forL1611服营新军 on 20211230 end*/
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		return json;
	}


	// 互动人员维护根据sales_id查询SalesInfo
	public SalesInfo getSalesById(String sales_id, UserInfo user) {
		// by yang_z for L591
		if (user.getChannelId().equals("05")) {
			return salesInfoDao_hd.getSalesById1(sales_id, user.getChannelId(), user.getBranchIdCur());
			/*L2411 add by qx*/
		}else if(user.getChannelId().equals("09")){
			return salesInfoDao_hd.getSalesById2(sales_id, user.getBranchIdCur());
		} else {
			return salesInfoDao_hd.getSalesById(sales_id, user.getChannelId());
		}
	}
	public SalesInfo getSalesByIdbranchid(String sales_id, UserInfo user) {
		// by yang_z for L591
			return salesInfoDao_hd.getSalesByIdbranchid(sales_id, user.getChannelId());

	}


	// 互动人员维护根据sales_id查询QualifyCardInfo
	public SalesInfo getQualifyCardInfoById(String sales_id) {
		return salesInfoDao_hd.getQualifyCardInfoById(sales_id);
	}

	// 互动人员维护根据sales_id查询DevelopCardInfo
	public SalesInfo getDevelopCardInfoById(String sales_id) {
		return salesInfoDao_hd.getDevelopCardInfoById(sales_id);
	}

	// 互动人员维护根据sales_id查询QualifyInfo
	public List<SalesInfo> getQualifyInfoById(String sales_id) {
		return salesInfoDao_hd.getQualifyInfoById(sales_id);
	}

	// 互动人员维护根据sales_id查询SalesAssurer
	public SalesInfo getSalesAssurerById(String sales_id) {
		return salesInfoDao_hd.getSalesAssurerById(sales_id);
	}

	// 人员特别信息修改
    public int staffInfoMaintainSpecialModify(SalesInfo salesInfo) throws Exception {

		// L610 将推荐人10位代码 转换成7位代码 qin_ly start
		String recommend_code = salesInfo.getRecommend_code();
		if (recommend_code != null && !"".equals(recommend_code) && recommend_code.trim().length() == 10) {
			recommend_code = publicMethodManagerImpl.getSalesID(recommend_code);
			salesInfo.setRecommend_code(recommend_code);
		}
		String leader_code = salesInfo.getLeader_code();
		if (leader_code != null && !"".equals(leader_code) && leader_code.trim().length() == 10) {
			String leader_id = publicMethodManagerImpl.getSalesID(leader_code);
			salesInfo.setLeader_id(leader_id);
		}
		// L610 将推荐人10位代码 转换成7位代码 qin_ly end
		// add by shiyawei for L479 育成关系 begin
		cn.com.sysnet.smis.gx.model.SalesInfo info = publicFunctionDao.getVersionBeanBySales(salesInfo.getChannel_id(),
				salesInfo.getSales_id());

        // L2838 判断人保e学是否通过 start
		HashMap<String, String> map = new HashMap<>();
		String oldRank = salesInfoDao_hd.queryRankById(salesInfo.getSales_id());
		map.put("channel_id", salesInfo.getChannel_id());
		map.put("sales_id", salesInfo.getSales_id());
		map.put("branch_id",salesInfo.getBranch_id());
		map.put("rank",oldRank);
		map.put("confirm_rank",salesInfo.getRank());
		String flag = publicFunctionDao.checkETrainSwitchSpecial(map);
		if("0".equals(flag)){
			// 如果原职级为见习专员 调整后为初级一级专员
			String isTrain = publicFunctionDao.queryETrainBySalesIdAndRank(map);
			if ("H02".equals(salesInfo.getRank()) && "H01".equals(oldRank)) {
				if (!"1".equals(isTrain))  {
					throw new Exception("综金渠道新人衔接培训”或培训失效，不允许调整至初级专员一级！");
				}
			}

			if ("H06".equals(salesInfo.getRank()) && "H06".compareToIgnoreCase(oldRank) > 0){
				if (!"1".equals(isTrain)){
					throw new Exception("该人员未通过“综金渠道转正提升培训”或“综金渠道区域经理任职培训”或培训失效，不允许调整至区域经理！");
				}
			}
		}
        // L2838 判断人保e学是否通过 end




		Integer isRank = salesInfoDao_hd.isRank(salesInfo.getSales_id(), salesInfo.getRank(), info.getRank());
		String base_version_id = info.getBase_version_id();
		// L541 组织归并
		String post_rank = salesInfoDao_hd.getRankPost(info.getRank(), base_version_id);// 原职级
		String post_rank_new = salesInfoDao_hd.getRankPost(salesInfo.getRank(), base_version_id);// 调整后职级
		salesInfoDao_hd.updateSalesById(salesInfo);// 修改人员表人员信息
		// 更新跨四级机构轨迹表
		salesInfoDao_hd.updateTrace();
		//a by qsw for L2382升降级处理育成关系 satrt
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		if ("1".equals(post_rank) && "0".equals(post_rank_new)){
			//组织归并-降级
			salesInfoDao_hd.downRank_hd(salesInfo.getSales_id(), info.getRank(), salesInfo.getLeader_code());//M by lc for L2630 添加主管代码
			this.getRecommendDown(salesInfo);
		}

		//升职处理育成
		if ("0".equals(post_rank) && "1".equals(post_rank_new)){
			//组织归并-升级
			//原组管id
			String leader_id = salesInfoDao_hd.queryLeaderId(salesInfo.getSales_id());
			salesInfoDao_hd.upRank(salesInfo.getSales_id(),info.getRank());
			this.getRecommendUp(salesInfo,base_version_id,leader_id);
		}
		//a by qsw for L2382升降级处理育成关系 end
		//a by qsw for L2382换推荐人处理推荐人多代关系start
		String recommendOld = salesInfoDao_hd.getRecommendOld(salesInfo.getSales_id());
		if (recommendOld==null||!recommendOld.equals(salesInfo.getRecommend_code())) {
			recommendChangeService.handler(salesInfo.getRecommend_code(),salesInfo.getSales_id(),RefereeEnum.XXWH,salesInfo.getUser_id());
	//		recommendChangeService.handlerCrossBr4Manage(salesInfo.getRecommend_code(),salesInfo.getSales_id(),RefereeEnum.XXWH,salesInfo.getUser_id());
		}
		//a by qsw for L2382换推荐人处理推荐人多代关系end
		// 查询跨级晋升降级 原上级
		String teamIdParent = salesInfoDao_hd.getTeamIdParent(salesInfo.getSales_id(), salesInfo.getChannel_id());
		// 通过team_id leader_id查,查不到不到，给赋值。因为查不到是刚晋升组长级别的人员，上级组织是人员表对应的team_id
		if ("".equals(teamIdParent) || teamIdParent == null) {
			teamIdParent = salesInfo.getTeam_id();
		}
		// 互动大个险支持 qin_ly 2019年8月2日 start
		String Is_duty_date = salesInfo.getIs_duty_date();// 是否更新任职日期 1是 0否
		if (!info.getRank().equals(salesInfo.getRank())) {// 原职级与调整后职级不一样,职级发生改变,任职日期发生改变
			salesInfo.setFlag(Is_duty_date);// 修改任职日期标识
		}
		// 互动大个险支持 qin_ly 2019年8月2日 end
		// 屏蔽组织归并 by feng_wy on 20190616
		//A by lc for L2630 on 20240701 begin
		if (post_rank_new.equals(post_rank)){
			if(StringUtils.isNotEmpty(salesInfo.getLeader_code())
					&& !"0000000".equals(salesInfo.getLeader_code())){
				//销售人员指定了一个跨四级的主管后，在t02_br4spanSalesRa_Hd插入一条数据
				int cnt = salesInfoDao_hd.getCountOverRelation(salesInfo.getSales_id(),"1",null);
				if(cnt == 0){
					// salesInfoDao_hd.saveOverRelation(salesInfo.getSales_id(), salesInfo.getLeader_code());
					recommendChangeService.handlerCrossBr4Manage(salesInfo.getLeader_id(),salesInfo.getSales_id(),RefereeEnum.XXWH,salesInfo.getUser_id());

				}
			}
		}
		//A by lc for L2630 on 20240701 end

//		  if(isRank == 1){ //L541 区总监降级为部主管 组织归并逻辑
//		  if(!post_rank.equals(post_rank_new)){ //异动前和异动后组织级别没有变化 不涉及组织归并 //降级
//		  salesInfoDao_hd.downRank_hd(salesInfo.getSales_id(), info.getRank());
//		 //salesInfoDao_hd.trainrelationRankDown(salesInfo.getSales_id(),info.getRank(
////		  ),salesInfo.getTeam_id()); }
//		  salesInfoDao_hd.trainrelationRankDown(salesInfo.getSales_id(),info.getRank(),teamIdParent);
//		  }else if(isRank == 2){ //L541 销售人员晋升 主管 指定组织主管
//		  if(!post_rank.equals(post_rank_new)){
//		 salesInfoDao_hd.upRank_new(salesInfo.getSales_id(),salesInfo.getTeam_id()); }
//		  //晋升
//			   salesInfoDao_hd.upRank(salesInfo.getSales_id(),info.getRank());
//		  salesInfoDao_hd.trainrelationRankUp(salesInfo.getSales_id(), info.getRank(),
//		  teamIdParent); }


		// A by guo_cz for L425 end
		// L458 wang_d start 晋升组长/组长降级 本人team_id 会发生变化，故重新获取本人的team_id
		SalesInfo sales = this.initSpecialModify(salesInfo.getChannel_id(), salesInfo.getSales_id());
		if (sales != null) {
			salesInfo.setTeam_id(sales.getTeam_id());
		}
		// L458 wang_d end
		// add by shiyawei for L479 end
		// 插入接口日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD, salesInfo.getUser_id(), salesInfo.getModuleIdCur());

		// A BY liu_k for L247 on 20151021 代码段二
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				ConstantEnum.UploadMobilePlatformType.UPLOAD_MOBILE_02_2.getCode(), salesInfo.getUser_id(),
				salesInfo.getModuleIdCur());
		//互联网核心日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD_FORINTERNET, salesInfo.getUser_id(), salesInfo.getModuleIdCur());

		
		//a by hbl for L1846 start 保存互动专员轨迹表
		SalesInfo  checkSlaes= salesInfoDao_hd.getOldCommissioner(salesInfo);
		if(checkSlaes.getCommissioner_code() == null){
			checkSlaes.setCommissioner_code("");
		}
		if(checkSlaes.getCommissioner_name() == null){
			checkSlaes.setCommissioner_name("");
		}
		if(checkSlaes.getIntroduce_id() == null){
			checkSlaes.setIntroduce_id("");
		}
		if(checkSlaes.getIntroduce_name() == null){
			checkSlaes.setIntroduce_name("");
		}
		if( !checkSlaes.getCommissioner_code().equals(salesInfo.getCommissioner_code())
			|| !checkSlaes.getCommissioner_name().equals(salesInfo.getCommissioner_name())
			|| !checkSlaes.getIntroduce_id().equals(salesInfo.getIntroduce_id())
			|| !checkSlaes.getIntroduce_name().equals(salesInfo.getIntroduce_name())){//校验是否修改了财险专员和介绍人信息,有修改则保存轨迹表
			salesInfo.setOld_commissioner_code(checkSlaes.getCommissioner_code());
			salesInfo.setOld_commissioner_name(checkSlaes.getCommissioner_name());
			salesInfo.setOld_introduce_id(checkSlaes.getIntroduce_id());
			salesInfo.setOld_introduce_name(checkSlaes.getIntroduce_name());
			
			salesInfoDao_hd.saveCXZYtrance(salesInfo);
		}
		//a by qsw for yunwei on 2023-07-04 start
		if(checkSlaes.getIs_comprehensive() == null){
			checkSlaes.setIs_comprehensive("");
		}
		if( !checkSlaes.getIs_comprehensive().equals(salesInfo.getIs_comprehensive())){
			salesInfo.setOld_is_comprehensive(checkSlaes.getIs_comprehensive());
			salesInfoDao_hd.saveCSGJtrance(salesInfo);
		}
		//a by qsw for yunwei on 2023-07-04 end
		if(salesInfo.getIsexcperson() == null){ //修改保存bug
			salesInfo.setIsexcperson("");
		}
		//a by hbl for L1846 start

		return salesInfoDao_hd.staffInfoMaintainSpecialModify(salesInfo);
	}
	//a by qsw for L2382降职处理育成
	public void getRecommendDown(SalesInfo salesInfo) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		String dat = format.format(new Date());
		SalesInfo info2 = new SalesInfo();
		info2.setSales_id(salesInfo.getSales_id());
		info2.setStatus(3);
		info2.setStop_date(dat);
		info2.setRemark("职级调整降级断裂");
		salesInfoDao_hd.updateRecommend_hd(info2);
		// 2、3..代育成断裂
		List<SalesInfo> trainDown = salesInfoDao_hd.getTrainDown(salesInfo.getSales_id());
		for (int i = 0; i < trainDown.size(); i++) {
			SalesInfo in = new SalesInfo();
			in.setSales_id(salesInfo.getSales_id());
			in.setTrain_id(trainDown.get(i).getTrain_id());
			in.setStatus(3);
			in.setStop_date(dat);
			in.setRemark("职级调整降级断裂");
			salesInfoDao_hd.updateRecommendDown(in);
		}
		List<SalesInfo> trainUp = salesInfoDao_hd.getTrainUp(salesInfo.getSales_id());
		for (int j = 0; j < trainUp.size(); j++) {
			SalesInfo inf = new SalesInfo();
			inf.setSales_id(trainUp.get(j).getSales_id());
			inf.setTrain_id(salesInfo.getSales_id());
			inf.setStatus(3);
			inf.setStop_date(dat);
			inf.setRemark("职级调整降级断裂");
			salesInfoDao_hd.updateRecommendDown(inf);
		}
	}

	//a by qsw for L2382升职处理育成
	public void getRecommendUp(SalesInfo salesInfo,String base_version_id,String leader_id) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//查询最新人员信息
		SalesInfo salesNew = salesInfoDao_hd.getSalesInfo2(salesInfo.getSales_code());
		//求原育成人看该人员是不是之前有断裂的育成关系（即先降职后升职）
		/*String id = salesInfoDao_hd.queryEnd(salesInfo.getSales_id());
		if (id!=null&&!id.isEmpty()) {
			cn.com.sysnet.smis.gx.model.SalesInfo s = publicFunctionDao.getVersionBeanBySales(salesInfo.getChannel_id(), id);
			String post = salesInfoDao_hd.getRankPost(s.getRank(), base_version_id);//查原育成人职级
			List<SalesInfo> trainUpp = salesInfoDao_hd.getTrainUp(id);//查原育成人的育成人
			if ("1".equals(post)) {
				SalesInfo info5 = new SalesInfo();
				info5.setSales_id(id);
				info5.setTrain_id(salesInfo.getSales_id());
				info5.setTrain_type("1");
				info5.setNum(1);
				info5.setCreate_date(format.format(new Date()));
				info5.setStatus(1);
				info5.setConfirm_date(format.format(new Date()));
				info5.setTeam_id(salesInfo.getTeam_id());
				info5.setUser_id(salesInfo.getUser_id());
				info5.setInsert_time(format.format(new Date()));
				info5.setOperate_type("1");
				info5.setRemark("人员信息特别维护处理多代育成关系");
				salesInfoDao_hd.insertTrainRation(info5);
				for (int i = 0; i < trainUpp.size(); i++) {
					SalesInfo info6 = new SalesInfo();
					info6.setSales_id(trainUpp.get(i).getSales_id());
					info6.setTrain_id(salesInfo.getSales_id());
					info6.setTrain_type("1");
					info6.setNum(trainUpp.get(i).getNum() + 1);
					info6.setCreate_date(format.format(new Date()));
					info6.setStatus(1);
					info6.setConfirm_date(format.format(new Date()));
					info6.setTeam_id(salesInfo.getTeam_id());
					info6.setUser_id(salesInfo.getUser_id());
					info6.setInsert_time(format.format(new Date()));
					info6.setOperate_type("1");
					info6.setRemark("人员信息特别维护处理多代育成关系");
					salesInfoDao_hd.insertTrainRation(info6);
				}
			}
		}*/
		/*else*/ if (leader_id!=null&&!leader_id.isEmpty()&&!"0000000".equals(leader_id)&&!leader_id.equals(salesInfo.getSales_id())) {

			//查组主管的育成人
			List<SalesInfo> trainUp = salesInfoDao_hd.getTrainUp(leader_id);
			//建立该升职人员和组主管的直接育成
			SalesInfo info7 = new SalesInfo();
			info7.setSales_id(leader_id);
			info7.setTrain_id(salesInfo.getSales_id());
			info7.setTrain_type("1");
			info7.setNum(1);
			info7.setCreate_date(format.format(new Date()));
			info7.setStatus(1);
			info7.setConfirm_date(format.format(new Date()));
			info7.setTeam_id(salesNew.getTeam_id());
			info7.setUser_id(salesNew.getUser_id());
			info7.setInsert_time(format.format(new Date()));
			info7.setOperate_type("1");
			info7.setRemark("人员信息特别维护升职处理育成关系");
			salesInfoDao_hd.insertTrainRation(info7);
			//建立该升职人员和组主管的育成人的多代关系
			for (int i = 0; i < trainUp.size(); i++) {
				SalesInfo info4 = new SalesInfo();
				info4.setSales_id(trainUp.get(i).getSales_id());
				info4.setTrain_id(salesInfo.getSales_id());
				info4.setTrain_type("1");
				info4.setNum(trainUp.get(i).getNum() + 1);
				info4.setCreate_date(format.format(new Date()));
				info4.setStatus(1);
				info4.setConfirm_date(format.format(new Date()));
				info4.setTeam_id(salesNew.getTeam_id());
				info4.setUser_id(salesNew.getUser_id());
				info4.setInsert_time(format.format(new Date()));
				info4.setOperate_type("1");
				info4.setRemark("人员信息特别维护升职处理育成关系");
				salesInfoDao_hd.insertTrainRation(info4);
			}
			//原先没有断掉的育成  并且没有主管  若推荐人为主管则与推荐人建立育成
		}else{
			cn.com.sysnet.smis.gx.model.SalesInfo s = publicFunctionDao.getVersionBeanBySales(salesInfo.getChannel_id(), salesInfo.getRecommend_code());
			if(s != null){
				String post1 = salesInfoDao_hd.getRankPost(s.getRank(), base_version_id);//查推荐人职级
				if("1".equals(post1)){
					//查推荐人的育成人
					List<SalesInfo> trainUp = salesInfoDao_hd.getTrainUp(salesInfo.getRecommend_code());
					//建立该升职人员和推荐人的直接育成
					SalesInfo info7 = new SalesInfo();
					info7.setSales_id(salesInfo.getRecommend_code());
					info7.setTrain_id(salesInfo.getSales_id());
					info7.setTrain_type("1");
					info7.setNum(1);
					info7.setCreate_date(format.format(new Date()));
					info7.setStatus(1);
					info7.setConfirm_date(format.format(new Date()));
					info7.setTeam_id(salesNew.getTeam_id());
					info7.setUser_id(salesNew.getUser_id());
					info7.setInsert_time(format.format(new Date()));
					info7.setOperate_type("1");
					info7.setRemark("人员信息特别维护升职处理育成关系");
					salesInfoDao_hd.insertTrainRation(info7);
					//建立该升职人员和组主管的育成人的多代关系
					for (int i = 0; i < trainUp.size(); i++) {
						SalesInfo info4 = new SalesInfo();
						info4.setSales_id(trainUp.get(i).getSales_id());
						info4.setTrain_id(salesInfo.getSales_id());
						info4.setTrain_type("1");
						info4.setNum(trainUp.get(i).getNum() + 1);
						info4.setCreate_date(format.format(new Date()));
						info4.setStatus(1);
						info4.setConfirm_date(format.format(new Date()));
						info4.setTeam_id(salesNew.getTeam_id());
						info4.setUser_id(salesNew.getUser_id());
						info4.setInsert_time(format.format(new Date()));
						info4.setOperate_type("1");
						info4.setRemark("人员信息特别维护升职处理育成关系");
						salesInfoDao_hd.insertTrainRation(info4);
					}
				}
			}
		}
	}
	public SalesInfo initSpecialModify(String channel_id, String salesId) {
		return salesInfoDao_hd.initSpecialModify(channel_id, salesId);
	}

	// 人员银行账号查询
	public List<SalesInfo> queryBankAccountById(String sales_id) {
		return salesInfoDao_hd.queryBankAccountById(sales_id);
	}

	// 人员银行账号新增
	public void addBankAccount(SalesInfo salesInfo) {
		salesInfoDao_hd.addBankAccount(salesInfo);
	}

	// 人员银行账号修改
	public int updateBankAccount(SalesInfo salesInfo) {
		return salesInfoDao_hd.updateBankAccount(salesInfo);
	}

	// 人员修改
	public SalesInfo updateSalesInfo(SalesInfo salesInfo) {
		// A by li_chx for R303集团统一工号3期 on 20141106 start
		String sales_id = "";
		String recommend_id = "";
		String leader_id = "";
		if (!salesInfo.getSales_id().equals("") && salesInfo.getSales_id() != null) {
			sales_id = publicMethodManagerImpl.getSalesID(salesInfo.getSales_id());
			if (sales_id == null || sales_id.equals("")) {
				sales_id = "N";
			}
		}
		if (!salesInfo.getRecommend_id().equals("") && salesInfo.getRecommend_id() != null) {
			recommend_id = publicMethodManagerImpl.getSalesID(salesInfo.getRecommend_id());
			if (recommend_id == null || recommend_id.equals("")) {
				recommend_id = "N";
			}
		}
		if (!salesInfo.getLeader_id().equals("") && salesInfo.getLeader_id() != null) {
			leader_id = publicMethodManagerImpl.getSalesID(salesInfo.getLeader_id());
			if (leader_id == null || leader_id.equals("")) {
				leader_id = "N";
			}
		}
		salesInfo.setSales_id(sales_id);
		salesInfo.setRecommend_id(recommend_id);
		salesInfo.setLeader_id(leader_id);
		// 对执业证发证日期进行格式的转化String to Date
		if (salesInfo.getGive_date() != null && !salesInfo.getGive_date().equals("")) {
			salesInfo.setGive_date_(UtilDate.defmtDate(salesInfo.getGive_date().trim()));
		}

		// 对执业证截止日期进行格式的转化String to Date
		if (salesInfo.getValid_date() != null && !salesInfo.getValid_date().equals("")) {
			salesInfo.setValid_date_(UtilDate.defmtDate(salesInfo.getValid_date().trim()));
		}
		// A by li_chx for R303集团统一工号3期 on 20141106 end
		salesInfoDao_hd.updateSalesInfo(salesInfo);
		// salesInfoDao_hd.insertDevelopCardInfo(salesInfo);

		// 插入接口日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD, salesInfo.getUser_id(), salesInfo.getModuleIdCur());

		// A BY liu_k for L247 on 20151021 代码段二
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				ConstantEnum.UploadMobilePlatformType.UPLOAD_MOBILE_02_2.getCode(), salesInfo.getUser_id(),
				salesInfo.getModuleIdCur());
       //插入互联网核心日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD_FORINTERNET, salesInfo.getUser_id(), salesInfo.getModuleIdCur());

		// 备份表
		businessManagerDao.backupTable("T02SALESINFO", salesInfo.getModuleIdCur(), null, salesInfo.getChannel_id(),
				salesInfo.getSales_id(), salesInfo.getUser_id(), DataConst.Data_Operate_Type_Update);

		return salesInfo;
	}

	// 人员修改（特色）
	public SalesInfo updateTeser(SalesInfo salesInfo) {
		salesInfoDao_hd.updateTeser(salesInfo);
		// 插入接口日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD, salesInfo.getUser_id(), salesInfo.getModuleIdCur());
		// A BY liu_k for L247 on 20151021 代码段二
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				ConstantEnum.UploadMobilePlatformType.UPLOAD_MOBILE_02_2.getCode(), salesInfo.getUser_id(),
				salesInfo.getModuleIdCur());
        //插入互联网核心日志
		dealDao.insertUploadLog(salesInfo.getChannel_id(), salesInfo.getBranch_id(), salesInfo.getSales_id(), "",
				CodeTypeConst.CODE_CODE_UPLOAD_SALES_UPD_FORINTERNET, salesInfo.getUser_id(), salesInfo.getModuleIdCur());
		
		// 备份表
		businessManagerDao.backupTable("T02SALESINFO", salesInfo.getModuleIdCur(), null, salesInfo.getChannel_id(),
				salesInfo.getSales_id(), salesInfo.getUser_id(), DataConst.Data_Operate_Type_Update);

		return salesInfo;
	}

	// 判断是否第二次入司
	public List<SalesInfo> getIsResigned(SalesInfo salesInfo) {
		return salesInfoDao_hd.getIsResigned(salesInfo);
	}

	// 判断是否第二次入司shiyawei 预入司表
	public List<SalesInfo> getIsPrepare(SalesInfo salesInfo) {
		return salesInfoDao_hd.getIsPrepare(salesInfo);
	}

	// 判断是否第二次入司
	public List<SalesInfo> getModifyIsResigned(SalesInfo salesInfo) {
		return salesInfoDao_hd.getModifyIsResigned(salesInfo);
	}

	public boolean getAssurerNum(SalesInfo salesInfo) {
		boolean result = false;
		List<SalesInfo> list = salesInfoDao_hd.getIsResigned(salesInfo);
		if (list != null && list.size() > 0) {
			String stat = list.get(0).getStat(); // 人员状态
			if (CodeTypeConst.Sales_Status_Dimission.equals(stat)) {
				int count = salesInfoDao_hd.getAssurerNum(salesInfo);
				if (count < 10) {
					result = true;
				}
			}
		} else { // 不是本系统中的营销员
			int count = salesInfoDao_hd.getAssurerNum(salesInfo);
			if (count < 10) {
				result = true;
			}
		}
		return result;
	}

	// 查询工资条打印
	public String queryBranchById(int limit, int start, SalesInfo salesInfo) {

		int count = salesInfoDao_hd.queryBranchByIdCount(salesInfo);

		List<SalesInfo> list = salesInfoDao_hd.queryBranchById(limit, start, salesInfo);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (SalesInfo info : list) {

				String stat_yearmonth = "";
				int print_times = 0;

				if (info.getPrint_times() != null && !"".equals(info.getPrint_times()))
					print_times = info.getPrint_times();
				if (info.getStat_yearmonth() != null && !"".equals(info.getStat_yearmonth()))
					stat_yearmonth = info.getStat_yearmonth().substring(0, 4) + "-"
							+ info.getStat_yearmonth().substring(4, info.getStat_yearmonth().length());

				json += "{id:'" + info.getBranch_id() + "',branch_id:'" + info.getBranch_id() + "',branch_name:'"
						+ info.getBranch_name() + "',print_times:'" + print_times + "',stat_yearmonth:'"
						+ stat_yearmonth + "'},";
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("机构查询 = " + json);

		return json;
	}

	/**
	 * 修改机构打印工资条次数
	 */
	public void printPDF(String branchId, SalesInfo salesInfo) {
		salesInfo.setBranch_id(branchId);

		int print_times = salesInfoDao_hd.queryPrintTimes(salesInfo);// 查询机构打印次数

		print_times = print_times + 1;

		salesInfo.setPrint_times(print_times);

		salesInfoDao_hd.updatePrintTimes(salesInfo);// 修改机构打印次数

	}

	public List<WagesPrint> queryWagesPrint(WagesPrint print) {
		return salesInfoDao_hd.queryWagesPrint(print);
	}

	/**
	 * 得到当前系统时间
	 */
	public String getDateTime() {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date currentTime = new java.util.Date();// 得到当前系统时间
		String getDateTime = formatter.format(currentTime); // 将日期时间格式化

		String date = getDateTime.substring(0, 10);
		String time = getDateTime.substring(11, getDateTime.length());
		date = date.replace("-", "");
		time = time.replace(":", "");
		getDateTime = date + time;

		return getDateTime;
	}

	/**
	 * 检查导入数据是否正确
	 */
	public String checkData(InputStream fileStream, UserInfo user) throws BiffException, IOException {
		String validateResult = "";

		Workbook wb = Workbook.getWorkbook(fileStream);
		Sheet s = wb.getSheet(0);// 第1个sheet
		int row = s.getRows();// 总行数

		// 验证导入数据是否正确
		if (row > 1) {
			for (int i = 1; i < row; i++) {
				if (s.getCell(0, i).getContents() != null && !"".equals(s.getCell(0, i).getContents())) {
					if (!importValidateDao.querySalesInfoNoDimission(DataConst.Channel_ID_Hd,
							s.getCell(0, i).getContents().trim(), s.getCell(1, i).getContents(), user))// 验证互动经理代码以及互动经理姓名是否存在
						validateResult += i + 1 + ",";
					else if (s.getCell(2, i).getContents() != null && !"".equals(s.getCell(2, i).getContents())) {
						if (salesInfoDao_hd.queryPolicyByNo(s.getCell(2, i).getContents().trim()))// 验证保单号是否已经存在
							validateResult += i + 1 + ",";
						else if (s.getCell(3, i).getContents() == null || "".equals(s.getCell(3, i).getContents())
								|| s.getCell(4, i).getContents() == null || "".equals(s.getCell(4, i).getContents()))// 验证产品代码或产品名称是否为空
							validateResult += i + 1 + ",";
						else if (s.getCell(5, i).getContents() != null && !"".equals(s.getCell(5, i).getContents())) {
							if (!importValidateDao.validateDate(s.getCell(5, i).getContents()))// 验证承包日期格式是否正确
								validateResult += i + 1 + ",";
							else if (s.getCell(6, i).getContents() != null
									&& !"".equals(s.getCell(6, i).getContents())) {
								if (!importValidateDao.validateShortFigure(s.getCell(6, i).getContents()))// 验证规模保费是否正确
									validateResult += i + 1 + ",";
								else if (s.getCell(7, i).getContents() != null
										&& !"".equals(s.getCell(7, i).getContents())) {
									if (importValidateDao.validateFigure(s.getCell(7, i).getContents()))// 验证手续费比率是否为数字类型
									{
										if (!(new Double(s.getCell(7, i).getContents()) >= 0
												&& new Double(s.getCell(7, i).getContents()) <= 1))// 验证手续费比率是否在0-1之间
											validateResult += i + 1 + ",";
										else if (s.getCell(2, i).getContents().trim().length() > 30)// 验证保单号长度是否大于30
											validateResult += i + 1 + ",";
										else if (s.getCell(3, i).getContents().trim().length() > 10)// 验证产品代码长度是否大于30
											validateResult += i + 1 + ",";
									} else
										validateResult += i + 1 + ","; // 如果手续费比率不为数字类型
								} else
									validateResult += i + 1 + ","; // 如果手续费比率为空
							} else
								validateResult += i + 1 + ","; // 如果规模保费为空
						} else
							validateResult += i + 1 + ",";// 如果承包日期为空
					} else
						validateResult += i + 1 + ","; // 如果保单号为空
				} else
					validateResult += i + 1 + ","; // 如果互动经理代码为空
			}

			// 验证Excel文档中保单号是否有重复
			if ("".equals(validateResult)) {
				for (int i = 1; i < row; i++) {
					String policyNo = s.getCell(2, i).getContents();

					for (int j = 1; j < row; j++) {
						if (i != j && policyNo.equals(s.getCell(2, j).getContents()))
							validateResult += i + 1 + ",";
					}
				}
			}

			// 如果验证数据正确则先显示导入数据
			if ("".equals(validateResult)) {
				validateResult = "";
				for (int i = 1; i < row; i++) {

					validateResult += s.getCell(0, i).getContents() + "," + s.getCell(1, i).getContents() + ","
							+ s.getCell(2, i).getContents() + "," + s.getCell(3, i).getContents() + ","
							+ s.getCell(4, i).getContents() + "," + s.getCell(5, i).getContents() + ","
							+ s.getCell(6, i).getContents() + "," + s.getCell(7, i).getContents() + ","
							+ String.valueOf(importValidateDao.doubleMul(new Double(s.getCell(6, i).getContents()),
									new Double(s.getCell(7, i).getContents())))
							+ "@";
				}
				validateResult = validateResult.substring(0, validateResult.length() - 1);
				validateResult = validateResult.replace("null", "");

				validateResult += "Y";// 设置验证数据成功的标志

			} else {
				validateResult = validateResult.substring(0, validateResult.length() - 1);

				validateResult += "N";// 设置验证数据失败的标志
			}
		}

		return validateResult;

	}

	/**
	 * 业绩导入
	 */
	public void importData(String importData) {
		String imData[] = importData.split(",");

		Double prem_rate = 0.0; // 互动规模保费比率
		Double allowance_rate = 0.0;// 互动津贴比率

		WagesPrint print = salesInfoDao_hd.queryRate(DataConst.Channel_ID_Hd,
				CodeTypeConst.CODE_CODE_CROSS_ACHIEVEMENT_HD_LIFE2PROPERTY);// 查询互动比率

		if (print != null && !"".equals(print)) {
			prem_rate = print.getPrem_rate();
			allowance_rate = print.getAllowance_rate();
		}

		for (int i = 0; i < imData.length; i++) {

			WagesPrint wprint = new WagesPrint();
			wprint.setChannel_id(DataConst.Channel_ID_Hd);

			String grade[] = imData[i].split("&");

			if (grade[0] != null && !"".equals(grade[0])) {
				// M by zhengyang for R309集团工号四期 on20141211 begin
				if (grade[0].trim().length() == 10) {
					wprint.setSales_id(SalesCodeUtils.getSalesID(grade[0]));// 互动经理代码
				} else {
					wprint.setSales_id(grade[0]);// 互动经理代码
				}
				// end
			}

			wprint.setBusiness_type("01");// 暂时默认业务类型为"01";

			if (grade[2] != null && !"".equals(grade[2])) {
				wprint.setPolicy_no(grade[2]);// 保单号
			}

			if (grade[3] != null && !"".equals(grade[3])) {
				wprint.setPrd_code(grade[3]);// 产品代码(险种代码)
			}

			if (grade[4] != null && !"".equals(grade[4])) {
				wprint.setPrd_name(grade[4]);// 产品名称
			}

			if (grade[5] != null && !"".equals(grade[5])) {
				wprint.setIssue_date(UtilDate.defmtDate(grade[5]));// 承包日期
				wprint.setStat_month(grade[5].substring(0, 7).replace("-", ""));
			}
			if (grade[6] != null && !"".equals(grade[6])) {
				wprint.setPremium(new Double((grade[6])));// 规模保费
				wprint.setPrem_cross(new Double((grade[6])) * prem_rate);// 互动保费(寿代产规模保费)=规模保费*互动规模保费比率
			}
			if (grade[6] != null && !"".equals(grade[6])) {
				wprint.setPoundage_amount(new Double((grade[6])) * (new Double((grade[7]))));// 手续费金额=规模保费*手续费比率
				wprint.setAllowance_cross(new Double((grade[6])) * (new Double((grade[7]))) * allowance_rate);// 互动开拓津贴(寿代产互动开拓津贴)=手续费金额*互动津贴比率
			}
			if (grade[7] != null && !"".equals(grade[7])) {
				wprint.setPoundage_rate(new Double((grade[7])));// 手续费比率
			}

			salesInfoDao_hd.insertPolicyperdaybase(wprint);
		}
	}

	/**
	 * 查询业绩导入
	 */
	public String queryPolicyperdaybase(int limit, int start) {

		int count = salesInfoDao_hd.queryPolicyCount(DataConst.Channel_ID_Hd);

		List<WagesPrint> list = salesInfoDao_hd.queryPolicyperdaybase(limit, start, DataConst.Channel_ID_Hd);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (WagesPrint print : list) {
				String issue_date = "";
				if (print.getIssue_date() != null && !"".equals(print.getIssue_date())) {
					issue_date = UtilDate.fmtDate(print.getIssue_date());
				}

				json += "{id:'" + print.getSales_id() + "',sales_id:'" + print.getSales_id() + "',sales_name:'"
						+ print.getSales_name() + "',policy_no:'" + print.getPolicy_no() + "',prd_code:'"
						+ print.getPrd_code() + "',prd_name:'" + print.getPrd_name() + "',issue_date:'" + issue_date
						+ "',premium:'" + print.getPremium() + "',poundage_rate:'" + print.getPoundage_rate()
						+ "',poundage_amount:'" + print.getPoundage_amount() + "'},";
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("业绩导入查询 = " + json);

		return json;
	}

	/**
	 * 查询业绩导入
	 */
	public String queryPolicyList(int limit, int start, WagesPrint wprint) {

		int count = salesInfoDao_hd.queryPolicyListCount(wprint);

		List<WagesPrint> list = salesInfoDao_hd.queryPolicyList(limit, start, wprint);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (WagesPrint print : list) {
				String issue_date = "";
				if (print.getIssue_date() != null && !"".equals(print.getIssue_date())) {
					issue_date = UtilDate.fmtDate(print.getIssue_date());
				}

				// 上载时间
				String inserttime = "";
				if (print.getInsert_time() != null && !"".equals(print.getInsert_time())) {
					inserttime = UtilDate.fmtDate(print.getInsert_time());
				}

				json += "{id:'" + print.getSales_id() + "',branch_id:'" + print.getBranch_id() + "',branch_name:'"
						+ print.getBranch_name() + "',team_id:'" + print.getTeam_id() + "',team_name:'"
						+ print.getTeam_name() + "',sales_id:'" + print.getSales_code() + // M by wang_gq for
																							// R309集团统一工号4期 on 20141210
						"',sales_name:'" + print.getSales_name() + "',policy_no:'" + print.getPolicy_no()
						+ "',prd_code:'" + print.getPrd_code() + "',prd_name:'" + print.getPrd_name() + "',issue_date:'"
						+ issue_date + "',poundage_rate:'" + print.getPoundage_rate() + "',poundage_amount:'"
						+ print.getPoundage_amount() + "',premium:'" + print.getPremium() + "',allowance_cross:'"
						+ print.getAllowance_cross() + "',insert_time:'" + inserttime + "',prem_cross:'"
						+ print.getPrem_cross() + "'},";
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("业绩查询 = " + json);

		return json;
	}

	public String queryPolicyListGx(int limit, int start, WagesPrint wprint) {

		int count = salesInfoDao_hd.queryPolicyListCountGx(wprint);

		List<WagesPrint> list = salesInfoDao_hd.queryPolicyListGx(limit, start, wprint);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (WagesPrint print : list) {
				String issue_date = "";
				if (print.getIssue_date() != null && !"".equals(print.getIssue_date())) {
					issue_date = UtilDate.fmtDate(print.getIssue_date());
				}

				// 上载时间
				String inserttime = "";
				if (print.getInsert_time() != null && !"".equals(print.getInsert_time())) {
					inserttime = UtilDate.fmtDate(print.getInsert_time());
				}

				json += "{id:'" + print.getSales_id() + "',branch_id:'" + print.getBranch_id() + "',branch_name:'"
						+ print.getBranch_name() + "',team_id:'" + print.getTeam_id() + "',team_name:'"
						+ print.getTeam_name() + "',sales_id:'" + print.getSales_code() + // M by wang_gq for
																							// R309集团统一工号4期 on 20141210
						"',sales_name:'" + print.getSales_name() + "',policy_no:'" + print.getPolicy_no()
						+ "',prd_code:'" + print.getPrd_code() + "',prd_name:'" + print.getPrd_name() + "',issue_date:'"
						+ issue_date + "',poundage_rate:'" + print.getPoundage_rate() + "',poundage_amount:'"
						+ print.getPoundage_amount() + "',premium:'" + print.getPremium() + "',allowance_cross:'"
						+ print.getAllowance_cross() + "',insert_time:'" + inserttime + "',prem_cross:'"
						+ print.getPrem_cross() + "'},";
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		System.out.println("业绩查询 = " + json);

		return json;
	}

	/**
	 * 导出EXCEL文件(按分类导出所有组织类型的人员)
	 */
	@SuppressWarnings("unchecked")
	public List<List<String>> exportExcel(SalesInfo salesInfo) {
		// 判断入参salesId是否为10位，如果为10位就转换成7位 a by ge_xd for 集团工号 on 20141016
		if (salesInfo.getSales_id() != null && !"".equals(salesInfo.getSales_id())
				&& salesInfo.getSales_id().trim().length() == 10) {

			String sales_id = publicMethodManagerImpl.getSalesID(salesInfo.getSales_id());
			salesInfo.setSales_id(sales_id);

		}
		List<List<String>> list = new ArrayList();

		String columns = "channel_id,branch_id,branch_name,team_id,team_name,team_type,salesCode,sales_id,sales_name,sex,qualify_id,rank,employ_kind,stat,probation_date,assess_start_date,leader_id,leader_name,sales_type,comp_sales_id,comp_branch_id,comp_branch_name";

		List<CodecodeInfo> codelist = codecodeDao.queryCode(CodeTypeConst.CODE_TYPE_ORGANIZATION_TYPE,
				DataConst.Channel_ID_Hd);

		for (CodecodeInfo team_type : codelist) {
			List<String> sheet = new ArrayList();

			salesInfo.setChannel_id(DataConst.Channel_ID_Hd);
			salesInfo.setTeam_type(team_type.getCodecode());

			List<ExportSalesInfo> sale = salesInfoDao_hd.exportExcel(salesInfo);

			sheet.add(columns);

			if (sale != null && sale.size() > 0) {
				for (ExportSalesInfo sales : sale) {
					String sex = "";
					String rank = sales.getRank();
					String employ_kind = "";
					String stat = "";
					String probation_date = "";
					String assess_start_date = "";
					String sales_type = "";
					String comp_sales_id = "";
					String comp_branch_id = "";
					String comp_branch_name = "";

					comp_sales_id = sales.getComp_sales_id();
					comp_branch_id = sales.getComp_branch_id();
					if (comp_sales_id != null && !"".equals(comp_sales_id)) {

						comp_branch_name = publicFunctionManager.getCompBranchName(comp_branch_id);
					}

					if (sales.getSex() != null && !"".equals(sales.getSex())) {
						if (DataConst.CODE_TYPE_SEX_MAN.equals(sales.getSex()))
							sex = "男";
						else if (DataConst.CODE_TYPE_SEX_WOMAN.equals(sales.getSex()))
							sex = "女";
					}

					if (sales.getRank() != null && !"".equals(sales.getRank()))
						// rank = rankdefDao.queryRanName(DataConst.Channel_ID_Hd,
						// sales.getRank(),sales.getTeam_type());//根据组织类型查询对应的业务职级

						if (sales.getEmploy_kind() != null && !"".equals(sales.getEmploy_kind()))

							if (team_type.getCodecode().equals("3"))
								employ_kind = codecodeDao.queryCodeName(sales.getEmploy_kind().trim(),
										DataConst.Channel_ID_Hd, CodeTypeConst.CODE_TYPE_SAFFE_NATURE);// 全面代理组织(特色)
							else
								employ_kind = codecodeDao.queryCodeName(sales.getEmploy_kind().trim(),
										DataConst.Channel_ID_Hd, CodeTypeConst.CODE_TYPE_NATURE_EMPLYMENT);// 全面代理组织(普通,试点)

					// if(sales.getStat()!=null&&!"".equals(sales.getStat()))
					// stat =
					// codecodeDao.queryCodeName(sales.getStat().trim(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_STAFF_STATUS);
					stat = sales.getStat();

					if (sales.getProbationDate() != null && !"".equals(sales.getProbationDate()))
						probation_date = UtilDate.fmtDate(sales.getProbationDate());

					if (sales.getAssess_start_date() != null && !"".equals(sales.getAssess_start_date())) {
						assess_start_date = UtilDate.fmtDate(sales.getAssess_start_date());
					}

					// 人员类别
					// if(sales.getSales_type()!=null&&!"".equals(sales.getSales_type()))
					// sales_type =
					// codecodeDao.queryCodeName(sales.getSales_type().trim(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_AGENT_SUB_CATE);
					sales_type = sales.getSales_type();

					String leader_type = sales.getLeader_type();
					if (leader_type != null && !"".equals(leader_type)) {
						if (leader_type.equals(CodeTypeConst.CODE_TYPE_TYPE_NQ))// 如果主管是内勤类型则提取内勤主管姓名
						{
							sales.setLeader_name(sales.getManager_name());
						}
					}
					String data = sales.getChannel_id() + "," + sales.getBranch_id() + "," + sales.getBranch_name()
							+ "," + sales.getTeam_id() + "," + sales.getTeam_name() + "," + team_type.getCodename()
							+ "," + sales.getSalesCode() + "," + sales.getSales_id() + "," + sales.getSales_name() + ","
							+ sex + "," + sales.getQualify_id() + "," + rank + "," + employ_kind + "," + stat + ","
							+ probation_date + "," + assess_start_date + "," + sales.getLeader_id() + ","
							+ sales.getLeader_name() + "," + sales_type + "," + comp_sales_id + "," + comp_branch_id
							+ "," + comp_branch_name;

					data = data.replace("null", "");
					sheet.add(data);
				}
			}
			list.add(sheet);
		}

		return list;
	}

	/**
	 * 导出EXCEL文件(导出所选组织类型的人员)
	 */
	@SuppressWarnings("unchecked")
	public List<String> exportSales(SalesInfo salesInfo) { // a by guo_cz for L408 begin
		String columns = "channel_id,branch_id,branch_name,team_id,team_name,team_type,is_comprehensive,"
				+ "commissioner_code,commissioner_name,introduce_id,introduce_name,plan_name,plan_time,property_networks,"
				+ ("04".equals(salesInfo.getChannel_id())?"is_full_time_education,":"")//L2113 综金导出添加全日制字段
				+"isexcperson,salesCode,sales_id,sales_name,"
				+ "sex,qualify_id,prof_no,preauthorization_level,certificate_type,certificate_releasedate,exam_subject,exam_state,rank," +//a by lxl for L2047 导出-在“资格证书号”列后方增加列“执业证编号”、“预授权资质、已授权资质、资质颁发日期、待考资质、考试状态” on 20221207
				//a by wzj for L1003 导出-在“业务职级”依次增加“管理归属机构代码”、“管理归属机构名称”、“派驻机构代码”、“派驻机构名称”字段 on 2020-9-5 13:40:25 start
				("04".equals(salesInfo.getChannel_id())?"mbranch_id,mbranch_name,accredit_org,accredit_org_name,":"")+
				//a by wzj for L1003 导出-在“业务职级”依次增加“管理归属机构代码”、“管理归属机构名称”、“派驻机构代码”、“派驻机构名称”字段 on 2020-9-5 13:40:25 end
				"employ_kind,stat,probation_date,assess_start_date,leader_id,leader_name,sales_type,"
				+ "comp_sales_id,comp_branch_id,comp_branch_name,first_probation_dates,org_sales_code,isStar,star_Level,"
				//a by chengyy for L2188 在“星级数量”后面增加“是否钻石会员、钻石会员等级、是否银钻会员、银钻会员等级、是否金钻会员、金钻会员等级” start
				+("04".equals(salesInfo.getChannel_id())?"isDiaMem,diaMem_Level,is_silverdiamond,silverdia_level,is_golddiamond,golddia_level,":"")
				//a by chengyy for L2188 在“星级数量”后面增加“是否钻石会员、钻石会员等级、是否银钻会员、银钻会员等级、是否金钻会员、金钻会员等级” end
				+ "is_whitelist";// a
																																// by
																																// guo_cz
																																// for
																																// L574
																																// on
																																// 20180408
		// a by guo_cz for L408 end
		List<String> sheet = new ArrayList();

		salesInfo.setChannel_id(DataConst.Channel_ID_Hd);

		List<ExportSalesInfo> sale = salesInfoDao_hd.exportExcel(salesInfo);

		sheet.add(columns);

		if (sale != null && sale.size() > 0) {
			for (ExportSalesInfo sales : sale) {
				String sex = "";
				String rank = sales.getRank();
				String employ_kind = "";
				String stat = "";
				String probation_date = "";
				String assess_start_date = "";
				String team_type = "";
				String sales_type = "";
				String comp_sales_id = "";
				String comp_branch_id = "";
				String comp_branch_name = "";
				// a by guo_cz for L408 begin
				String first_probation_dates = "";
				// a by guo_cz for L408 end
				String is_comprehensive = "";
				String plan_name = sales.getPlan_name(); // a by guo_cz for L574 on 20180408
				String org_sales_code = sales.getOrg_sales_code();
				if (org_sales_code != null && !"".equals(org_sales_code)) { //add by hbl for 处理导出错位问题
					org_sales_code = org_sales_code.replace(",", "、");
				}
				comp_sales_id = sales.getComp_sales_id();
				comp_branch_id = sales.getComp_branch_id();
				if (comp_sales_id != null && !"".equals(comp_sales_id)) {

					comp_branch_name = publicFunctionManager.getCompBranchName(comp_branch_id);
				}

				if (sales.getSex() != null && !"".equals(sales.getSex())) {
					if (DataConst.CODE_TYPE_SEX_MAN.equals(sales.getSex()))
						sex = "男";
					else if (DataConst.CODE_TYPE_SEX_WOMAN.equals(sales.getSex()))
						sex = "女";
				}

				if (sales.getRank() != null && !"".equals(sales.getRank()))
					// rank = rankdefDao.queryRankName(DataConst.Channel_ID_Hd,
					// sales.getRank().trim());

					if (sales.getEmploy_kind() != null && !"".equals(sales.getEmploy_kind()))

						if ("3".equals(salesInfo.getTeam_type()))
							employ_kind = codecodeDao.queryCodeName(sales.getEmploy_kind().trim(),
									DataConst.Channel_ID_Hd, CodeTypeConst.CODE_TYPE_SAFFE_NATURE);// 全面代理组织(特色)
						else
							employ_kind = codecodeDao.queryCodeName(sales.getEmploy_kind().trim(),
									DataConst.Channel_ID_Hd, CodeTypeConst.CODE_TYPE_NATURE_EMPLYMENT);// 全面代理组织(普通,试点)

				// if(sales.getStat()!=null&&!"".equals(sales.getStat()))
				// stat =
				// codecodeDao.queryCodeName(sales.getStat().trim(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_STAFF_STATUS);
				stat = sales.getStat();

				if (sales.getProbationDate() != null && !"".equals(sales.getProbationDate()))
					probation_date = UtilDate.fmtDate(sales.getProbationDate());

				if (sales.getAssess_start_date() != null && !"".equals(sales.getAssess_start_date())) {
					assess_start_date = UtilDate.fmtDate(sales.getAssess_start_date());
				}

				
				if (sales.getTeam_type() != null && !"".equals(sales.getTeam_type())) {
					team_type = codecodeDao.queryCodeName(sales.getTeam_type(), DataConst.Channel_ID_Hd,
							CodeTypeConst.CODE_TYPE_ORGANIZATION_TYPE);
				}
				// a by guo_cz for L408 begin
				if (sales.getV_first_probation_date() != null && !"".equals(sales.getV_first_probation_date())) {
					first_probation_dates = UtilDate.fmtDate(sales.getV_first_probation_date());
				}
				// a by guo_cz for L408 end
				// 人员类别
				// if(sales.getSales_type()!=null&&!"".equals(sales.getSales_type()))
				// sales_type =
				// codecodeDao.queryCodeName(sales.getSales_type().trim(),DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_AGENT_SUB_CATE);
				sales_type = sales.getSales_type();

				String leader_type = sales.getLeader_type();
				if (leader_type != null && !"".equals(leader_type)) {
					if (leader_type.equals(CodeTypeConst.CODE_TYPE_TYPE_NQ))// 如果主管是内勤类型则提取内勤主管姓名
					{
						sales.setLeader_name(sales.getManager_name());
					}
				}
                //a by wzj for L1540 start
                String property_networks=sales.getProperty_networks();
                if(StringUtils.isEmpty(sales.getAccredit_org()) && "n".equals(sales.getHas_cx()))
                    property_networks="-";
                //a by wzj for L1540 end
				String data = sales.getChannel_id() + "," + sales.getBranch_id() + "," + sales.getBranch_name() + ","

						+ sales.getTeam_id() + "," + sales.getTeam_name() + "," + team_type + "," 
						+sales.getIs_comprehensive() 
						+","+ sales.getCommissioner_code() +","+ sales.getCommissioner_name()
						+","+ sales.getIntroduce_id() +","+ sales.getIntroduce_name() +","+ plan_name + ","
						+ sales.getPlan_time() + "," +property_networks+","
						+ ("04".equals(salesInfo.getChannel_id())?(sales.getIs_full_time_education() + ","):"") // 是否全日制学历 L2113 综金人员
						+ sales.getIsexcperson() + "," + sales.getSalesCode() + ","
						+ sales.getSales_id() + "," + sales.getSales_name() + "," + sex + "," + sales.getQualify_id()
						//L2047 添加 执业证编号、预授权资质、已授权资质、资质颁发日期、待考资质、考试状态
						+ "," + sales.getProf_no() + ","+ sales.getPreauthorization_level() + ","+ sales.getCertificate_type()
						+ ","+ sales.getCertificate_releasedate() + ","+ sales.getExam_subject() + ","+ sales.getExam_state()
						//L2047 添加 执业证编号、预授权资质、已授权资质、资质颁发日期、待考资质、考试状态
						+ "," + rank + ","
						//a by wzj for L1003 导出-在“业务职级”依次增加“管理归属机构代码”、“管理归属机构名称”、“派驻机构代码”、“派驻机构名称”字段 on 2020-9-5 13:40:25 start
						+("04".equals(salesInfo.getChannel_id())?(sales.getMbranch_id()+","+sales.getMbranch_name()+","+sales.getAccredit_org()+","+sales.getAccredit_org_name()+","):"")
						//a by wzj for L1003 导出-在“业务职级”依次增加“管理归属机构代码”、“管理归属机构名称”、“派驻机构代码”、“派驻机构名称”字段 on 2020-9-5 13:40:25 end
						+ employ_kind + "," + stat + "," + probation_date + "," + assess_start_date
						+ "," + sales.getLeader_id() + "," + sales.getLeader_name() + "," + sales_type
						// a by guo_cz for L408 begin
						+ "," + comp_sales_id + "," + comp_branch_id + "," + comp_branch_name + ","
						+ first_probation_dates + "," + org_sales_code + "," + sales.getIsStar() + ","
						+ sales.getStar_Level() + "," 
						//a by chengyy for L2188 在“星级数量”后面增加“是否钻石会员、钻石会员等级、是否银钻会员、银钻会员等级、是否金钻会员、金钻会员等级” start
						+ ("04".equals(salesInfo.getChannel_id())?(sales.getIsDiaMem()+ "," + sales.getDiaMem_Level() + "," + sales.getIs_silverdiamond() + "," + sales.getSilverdia_level() + "," + sales.getIs_golddiamond() + "," + sales.getGolddia_level() + ","):"") 
						//a by chengyy for L2188 在“星级数量”后面增加“是否钻石会员、钻石会员等级、是否银钻会员、银钻会员等级、是否金钻会员、金钻会员等级” end 
						+ sales.getIs_whitelist();
				// a by guo_cz for L408 end
				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}

	/**
	 * L2047,L2380产险营销员分级信息导入 导出EXCEL文件
	 */
	@SuppressWarnings("unchecked")
	public List<String> salesImportInfoExport(SalesInfo salesInfo) {
		String columns = "branch_id1,branch_name1,branch_id2,branch_name2,branch_id3,branch_name3,branch_id,branch_name," +
				"sales_code,inside_id,sales_name,prof_no,preauthorization_level,preauthorization_lapsedate," +
				"certificate_type,certificate_releasedate,people_sign";
		List<String> sheet = new ArrayList();

		salesInfo.setChannel_id(DataConst.Channel_ID_Hd);

		List<ExportSalesInfo> sale = salesInfoDao_hd.salesImportInfoExportExcel(salesInfo);

		sheet.add(columns);

		if (sale != null && sale.size() > 0) {
			String branchIdCur = sale.get(0).getOpt_branchid_2().substring(0,3);
			for (ExportSalesInfo sales : sale) {
				String data =
						sales.getBranch_id1() + "," + sales.getBranch_name1() + ","
						+sales.getBranch_id2() + "," + sales.getBranch_name2() + ","
						+sales.getBranch_id3() + "," + sales.getBranch_name3() + ","
						+sales.getBranch_id() + "," + sales.getBranch_name() + ","
								+ sales.getSalesCode() + ","
								+ sales.getInside_id() + ","
								+ sales.getSales_name() + ","
								+ sales.getProf_no() + ","
//								+ sales.getPreauthorization_level()
								+ (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getPreauthorization_level())))?"":sales.getPreauthorization_level())+ ","
//								+ ","+ sales.getPreauthorization_lapsedate()
								+ (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getPreauthorization_level())))?"":sales.getPreauthorization_lapsedate())
								+ ","
                                + (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getCertificate_type())))?"":sales.getCertificate_type())
								+ ","+
                                (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getCertificate_type())))?"":sales.getCertificate_releasedate())
								+ ","+ sales.getPeople_sign()
						;

				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}
	public List<String> salesImportInfoExportAnother(SalesInfo salesInfo) {
		String columns = "branch_id1,branch_name1,branch_id2,branch_name2,branch_id3,branch_name3,branch_id,branch_name," +
				"sales_code,inside_id,sales_name,prof_no,certificate_type";
		List<String> sheet = new ArrayList();

		salesInfo.setChannel_id(DataConst.Channel_ID_Hd);

		List<ExportSalesInfo> sale = salesInfoDao_hd.salesImportInfoExportExcel(salesInfo);

		sheet.add(columns);

		if (sale != null && sale.size() > 0) {
			String branchIdCur = sale.get(0).getOpt_branchid_2().substring(0,3);
			for (ExportSalesInfo sales : sale) {
				String data =
						sales.getBranch_id1() + "," + sales.getBranch_name1() + ","
						+sales.getBranch_id2() + "," + sales.getBranch_name2() + ","
						+sales.getBranch_id3() + "," + sales.getBranch_name3() + ","
						+sales.getBranch_id() + "," + sales.getBranch_name() + ","
								+ sales.getSalesCode() + ","
								+ sales.getInside_id() + ","
								+ sales.getSales_name() + ","
								+ sales.getProf_no() + ","
                                + (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getCertificate_type())))?"":sales.getCertificate_type())
                                + ","+
                                (((!"153".equals(branchIdCur))&&("未定级".equals(sales.getCertificate_type())))?"":sales.getCertificate_releasedate())
						;

				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}
	//2380
	public List<String> salesImportInfoExportP(SalesInfo salesInfo) {
		String columns = "branch_id2,branch_name2,branch_id3,branch_name3,branch_id,branch_name,sales_code,sales_name,prof_no," +
                "preauthorization_level," +
				"preauthorization_lapsedate," +
				"certificate_type,certificate_releasedate";

		List<String> sheet = new ArrayList();

//		salesInfo.setChannel_id(DataConst.Channel_ID_Hd);

		List<ExportSalesInfo> sale = salesInfoDao_hd.salesImportInfoExportExcelP(salesInfo);

		sheet.add(columns);

		if (sale != null && sale.size() > 0) {
			for (ExportSalesInfo sales : sale) {
				if ("1530000".equals(sales.getBranch_id2())){
					if ("0".equals(sales.getPreauthorization_level())||"".equals(sales.getPreauthorization_level())){
						sales.setPreauthorization_level("未定级");
					}
					if ("0".equals(sales.getCertificate_type())||"".equals(sales.getCertificate_type())){
						sales.setCertificate_type("未定级");
					}


				}
				String data =
						sales.getBranch_id2() + "," + sales.getBranch_name2() + ","
						+sales.getBranch_id3() + "," + sales.getBranch_name3() + ","
						+sales.getBranch_id() + "," + sales.getBranch_name() + ","
								+ sales.getSales_code() + "," + sales.getSales_name() + ","
								+ sales.getProf_no() + ","+
                                sales.getPreauthorization_level() + ","
								+ ((sales.getPreauthorization_level()==null||"".equals(sales.getPreauthorization_level()))?"":sales.getPreauthorization_lapsedate())+ ","
//								+ sales.getPreauthorization_lapsedate()+","
                                +sales.getCertificate_type() + ","+ sales.getCertificate_releasedate();
				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}
	public List<String> salesImportInfoExportPAnother(SalesInfo salesInfo) {
		//'branch_id2','branch_name2','branch_id3','branch_name3',
		//                    'branch_id','branch_name','sales_code','sales_name','prof_no',
		//                    '当前资质等级','初级评定时间','中级评定时间','高级评定时间','特级评定时间','司龄',
		//                    '学历','是否相关专业','相关职业职称','最近品质扣分的时间','是否满足下一等级基本条件和专业道德',
		//                    '是否满足下一等级专业知识','特级综合评审结果'
		String columns = "branch_id2,branch_name2,branch_id3,branch_name3,branch_id,branch_name,sales_code,sales_name,prof_no," +
				"certificate_type," +
				"primary_time,middle_time,high_time,special_time,com_age,education,is_major,major_name," +
				"Quality_penalty_time,is_next_condition_morality,is_next_knowledge,special_result";

		List<String> sheet = new ArrayList();

//		salesInfo.setChannel_id(DataConst.Channel_ID_Hd);

		List<ExportSalesInfo> sale = salesInfoDao_hd.salesImportInfoExportExcelP(salesInfo);

		sheet.add(columns);

		if (sale != null && sale.size() > 0) {
			for (ExportSalesInfo sales : sale) {
				if ("1530000".equals(sales.getBranch_id2())){
					if ("0".equals(sales.getPreauthorization_level())||"".equals(sales.getPreauthorization_level())){
						sales.setPreauthorization_level("未定级");
					}
					if ("0".equals(sales.getCertificate_type())||"".equals(sales.getCertificate_type())){
						sales.setCertificate_type("未定级");
					}


				}
				String data =
						sales.getBranch_id2() + "," + sales.getBranch_name2() + ","
						+sales.getBranch_id3() + "," + sales.getBranch_name3() + ","
						+sales.getBranch_id() + "," + sales.getBranch_name() + ","
								+ sales.getSales_code() + "," + sales.getSales_name() + ","
								+ sales.getProf_no() + "," +sales.getCertificate_type() + ","

								+ sales.getPrimary_time() + "," +sales.getMiddle_time() + ","
								+ sales.getHigh_time() + "," +sales.getSpecial_time() + ","
								+ sales.getCom_age() + "," +sales.getEducation() + ","
								+ sales.getIs_major() + "," +sales.getMajor_name() + ","
								+ sales.getQuality_penalty_time() + "," +sales.getIs_next_condition_morality() + ","
								+ sales.getIs_next_knowledge() + "," +sales.getSpecial_result();
				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}
	//L2047 产险营销员分级信息导入 人员代码查人员姓名
	public SalesInfo getSalesByCode(String sales_code) {

		return salesInfoDao_hd.getSalesByCode(sales_code);
	}
	//L2047 产险营销员分级信息导入 四级机构代码查询四级机构名称
	public SalesInfo getSalesByBi(String branch_id) {

		return salesInfoDao_hd.getSalesByBi(branch_id);
	}
	//L2047，2380 产险营销员分级信息导入 导入校验
	public String salesInfoImportCheckData(InputStream fileStream,String branchId, String channel_id,String branch_id22, String branch_name22)throws BiffException, IOException {

		boolean flag = true;
		StringBuffer sb = new StringBuffer("");
		SalesInfo confignew = null;
		SalesInfo configold = null;
		if (!"".equals(branch_id22)){
			//判断该二级机构有无配置
			confignew = salesInfoDao_hd.countConfig(branch_id22);
			configold = salesInfoDao_hd.countConfig2(branch_id22);
			if (confignew==null){
				flag = true;
				sb.append("因该分公司没有配置，暂不支持导入!");
				return sb.toString();
			}
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String transition_start_new = confignew.getTransition_start();
		String transition_time_new = confignew.getTransition_time();
		String transition_start_old = configold.getTransition_start();
		String transition_time_old  = configold.getTransition_time();
		Date date_transition_start_old = null;
		try {
			 date_transition_start_old = format.parse(transition_start_old);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Date currentDate = new Date();
		// 创建Calendar对象，设置时间为注册日期
		Calendar calendar = Calendar.getInstance();



		Workbook wb = Workbook.getWorkbook(fileStream);
		Sheet s = wb.getSheet(0);//第1个sheet
		int row = s.getRows();//总行数
		List<SalesInfo> dataList =new ArrayList<SalesInfo>();//记录合格数据
		//验证导入数据是否正确
		if (row > 1) {
			boolean check = checkTemplate(s);
			if(!check){
				sb.append("N");
				return sb.toString();
			}
			List<String> salesCodes = new ArrayList<String>();
			for (int i = 1; i < row; i++) {
				// 开始验证
				String branch_id1 = "";
				String branch_name1 = s.getCell(0, i).getContents();
				String branch_id2 = s.getCell(1, i).getContents();
				String branch_name2 = s.getCell(2, i).getContents();
				String branch_id3 = s.getCell(3, i).getContents();
				String branch_name3 = s.getCell(4, i).getContents();
				String branch_id = s.getCell(5, i).getContents();
				String branch_name = s.getCell(6, i).getContents();
				String sales_code = s.getCell(7, i).getContents();
				String inside_id = s.getCell(8, i).getContents();
				String sales_name = s.getCell(9, i).getContents();
				String prof_no = s.getCell(10, i).getContents();
				String preauthorization_level = s.getCell(11, i).getContents();
				String certificate_type = s.getCell(12, i).getContents();
				String certificate_releasedate = s.getCell(13, i).getContents();
				String people_sign = s.getCell(14, i).getContents();
				int sc = sales_code.length();
				String scc = String.valueOf(sc);
				if (salesCodes.contains(sales_code)){
					sb.append("第" + (i+1) + "行，导入数据中存在重复的人员，请核对！<br/>");
				}
				salesCodes.add(sales_code);
				//根据新老人标识判断
				calendar.setTime(date_transition_start_old);
				if (!"N".equals(transition_time_old)&&"2".equals(people_sign)){
					calendar.add(Calendar.MONTH, Integer.parseInt(transition_time_old));
				}else if (!"N".equals(transition_time_new)&&"1".equals(people_sign)){
					calendar.add(Calendar.MONTH, Integer.parseInt(transition_time_new));
				}
				// 获取过渡期结束日期
				Date newDate = calendar.getTime();
//				if (UtilString.trim(branch_id1).equals("")|| UtilString.trim(branch_id1)==null) {
//					flag = false;
//					sb.append("第" + (i+1) + "行，一级机构代码不可为空，请核对！<br/>");
//				}

//				if (UtilString.trim(branch_id2).equals("")|| UtilString.trim(branch_id2)==null) {
//					flag = false;
//					sb.append("第" + (i+1) + "行，二级机构代码不可为空，请核对！<br/>");
//				}
/*				if (UtilString.trim(branch_id3).equals("")|| UtilString.trim(branch_id3)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，三级机构代码不可为空，请核对！<br/>");
				}
				if (UtilString.trim(branch_id).equals("")|| UtilString.trim(branch_id)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，四级机构代码不可为空，请核对！<br/>");
				}*/
				if (UtilString.trim(branch_name1).equals("")|| UtilString.trim(branch_name1)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，一级机构名称不可为空，请核对！<br/>");
				}else if (!UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")&&!UtilString.trim(branch_name1).equals("中国人民健康保险股份有限公司")){
					flag = false;
					sb.append("第" + (i+1) + "行，一级机构仅可为中国人民财产保险股份有限公司或中国人民健康保险股份有限公司，请导入正确的一级机构名称！<br/>");
				}
				/*else if (!branch_name1.contains("财产")&&!branch_name1.contains("健康")){
					flag = false;
					sb.append("第" + (i+1) + "行，请导入中国人民财产保险股份有限公司或中国人民健康保险股份有限公司，请核对！<br/>");
				}*/
				if (UtilString.trim(branch_id2).equals("")|| UtilString.trim(branch_id2)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构代码不可为空，请核对！<br/>");
				}else if(UtilString.trim(branch_id2).length()!=8||branch_id2.contains(" ")){
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构代码须为8位的财险或健康险二级机构代码，且不得包含空格，请核对！<br/>");
				}else if (UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")){
					String branch_id2_cx = queryCXBranchId2 (branch_id2);
					if(UtilString.trim(branch_id2_cx).equals("")|| UtilString.trim(branch_id2_cx)==null){
						flag = false;
						sb.append("第" + (i+1) + "行，二级机构代码错误，请导入财险机构代码，请核对！<br/>");
					}
				}
				if (UtilString.trim(branch_name2).equals("")|| UtilString.trim(branch_name2)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构名称不可为空，请核对！<br/>");
				}
				if (UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")) {
					branch_name2 = queryCXBranchId2 (branch_id2);
				}
/*				if (UtilString.trim(branch_name3).equals("")|| UtilString.trim(branch_name3)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，三级机构名称不可为空，请核对！<br/>");
				}
				if (UtilString.trim(branch_name).equals("")|| UtilString.trim(branch_name)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，四级机构名称不可为空，请核对！<br/>");
				}*/
				if ((UtilString.trim(sales_code).equals("")|| UtilString.trim(sales_code)==null )|| !scc.equals("10")) {
					flag = false;
					sb.append("第" + (i+1) + "行，集团统一工号不可为空且必须为10位数字，请核对！<br/>");
				}
				if (UtilString.trim(sales_name).equals("")|| UtilString.trim(sales_name)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，人员姓名不可为空，请核对！<br/>");
				}
				if (UtilString.trim(prof_no).equals("")|| UtilString.trim(prof_no)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，执业证编号不可为空，请核对！<br/>");
				}

				if (UtilString.trim(certificate_type).equals("")|| UtilString.trim(certificate_type)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，已授权资质不可为空，请核对！<br/>");
				}
				String user_branchId=branchId.substring(1,3);
				//“四级机构代码+省代码”调取手续费系统【交叉销售人员信息查询接口】校验导入的产险人员信息准确性
//				int count = salesInfoDao_hd.checkCount(sales_code,user_branchId);
//				if (count != 1) {
//					flag = false;
//					sb.append("第" + (i+1) + "行，产险人员信息不存在，请核对！<br/>");
//				}
				//改为    通过“集团统一工号”调取集团统一工号平台【工号查询接口】校验导入的产险人员信息准确性
				RequestModel m=new RequestModel();
				m.setUniSalesCod(sales_code);
				m.setMessageType("01");
				m.setManOrgCod(branch_id22);
				ResponseModel rm = null;

				System.out.println("getJobNumberP_start");
				try {
					rm = unifiedJobNumberServiceImpl.getJobNumberP(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("getJobNumberP_end");
				String uniSalesCod = rm.getSalesCode();
				String id_no = " ";
				String idtyp_cod = " ";
				if (uniSalesCod==null||"".equals(uniSalesCod)){
					flag = false;
					sb.append("第" + (i+1) + "行，产险人员信息不存在，请核对！<br/>");
				}else{
					branch_id1 = rm.getComp_cod();
					id_no = rm.getId_no();
					idtyp_cod = rm.getIdtyp_cod();
				}
				//预授权资质代码(云南)	0-无，1-初级，2-中级，3-高级，4-过期 preauthorization_level
				//预授权资质代码(河北)	2-中级
				String preauthorizationLevel = "'0','1','2','3','4'";
				//已授权资质代码(云南) 没有则给0-无，530101-初级，530102-中级，530103-高级 certificate_type
				//已授权资质代码(河北) 1-初级，2-中级，3-高级
				String certificateType = "'0','530101','530102','530103'";
				if("13".equals(user_branchId)){
					preauthorizationLevel="2";
					certificateType="'1','2','3'";
				}
				//	录入的内容必须是系统中存在的“预授权资质代码”
				if (!preauthorizationLevel.contains(preauthorization_level)) {
					flag = false;
					sb.append("第" + (i+1) + "行，录入的预授权资质代码不存在，请核对！<br/>");
				}
				//	录入的内容必须是系统中存在的“已授权资质代码
				if (!certificateType.contains(certificate_type)) {
					flag = false;
					sb.append("第" + (i+1) + "行，录入的已授权资质代码不存在，请核对！<br/>");
				}
				// 正则表达式: 四位数年份/两位数月份/两位数日期
				String regex = "^\\d{4}/\\d{2}/\\d{2}$";
				if (UtilString.trim(certificate_releasedate).equals("")|| UtilString.trim(certificate_releasedate)==null||!certificate_releasedate.matches(regex) ) {
					flag = false;
					sb.append("第" + (i+1) + "行，已授权资质颁发日期不可为空且格式必须为YYYY/MM/DD，请核对！<br/>");
				}
				if (UtilString.trim(people_sign).equals("")|| UtilString.trim(people_sign)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，新老人标识不可为空，请核对！<br/>");
				}else if (!"2".equals(people_sign)&&!"1".equals(people_sign)){
					flag = false;
					sb.append("第" + (i+1) + "行，录入的新老人标识代码不存在，请核对！！<br/>");
				}else if ("2".equals(people_sign)){
					//说明新老人标识正常，开始判断
					//若营销员导入为新人，则预授权资质可为空。
					//若营销员导入为老人，且处于过渡期，则预授权资质不可空，否则系统提示：第N行，预授权资质不可为空，请核对！
					//若营销员导入为老人，且不在过渡期内，则预授权资质可为空。
					//过渡期结束时间newDate
					if ("N".equals(transition_time_new)||"N".equals(transition_time_old)||newDate.compareTo(currentDate)>0){
						//过渡期内
						if (UtilString.trim(preauthorization_level).equals("")|| UtilString.trim(preauthorization_level)==null ) {
							flag = false;
							sb.append("第" + (i+1) + "行，预授权资质不可为空，请核对！<br/>");
						}
					}
				}
				if(flag){
					SalesInfo salesInfo = new SalesInfo();
					salesInfo.setSales_code(sales_code);
					salesInfo.setBranch_id1(branch_id1);
					salesInfo.setBranch_name1(branch_name1);
					salesInfo.setBranch_id2(branch_id2);
					salesInfo.setBranch_name2(branch_name2);
					salesInfo.setBranch_id3(branch_id3);
					salesInfo.setBranch_name3(branch_name3);
					salesInfo.setBranch_id(branch_id);
					salesInfo.setBranch_name(branch_name);
					salesInfo.setSales_code(sales_code);
					salesInfo.setSales_name(sales_name);
					salesInfo.setProf_no(prof_no);
					salesInfo.setPreauthorization_level(preauthorization_level);
					salesInfo.setCertificate_type(certificate_type);
					salesInfo.setInside_id(inside_id);
					salesInfo.setCertificate_releasedate(certificate_releasedate);
					salesInfo.setBranchIdCur(branchId);
					salesInfo.setOpt_branchid_2(branch_id22);
					salesInfo.setPeople_sign(people_sign);
					salesInfo.setCard_no(id_no);
					if(idtyp_cod.equals("11")){
						salesInfo.setCard_type("0");
					}else if(idtyp_cod.equals("14")){
						salesInfo.setCard_type("1");
					}else if(idtyp_cod.equals("15")){
						salesInfo.setCard_type("2");
					}else if(idtyp_cod.equals("23")){
						salesInfo.setCard_type("10");
					}else{
						salesInfo.setCard_type("8");
					}
//					//查询产险人员证件号与类型进行转码
//					SalesInfo info =salesInfoDao_hd.queryCard(salesInfo);
//					salesInfo.setCard_no(info.getId_no());
//					if(info.getIdtyp_cod().equals("11")){
//						salesInfo.setCard_type("0");
//					}else if(info.getIdtyp_cod().equals("14")){
//						salesInfo.setCard_type("1");
//					}else if(info.getIdtyp_cod().equals("15")){
//						salesInfo.setCard_type("2");
//					}else if(info.getIdtyp_cod().equals("23")){
//						salesInfo.setCard_type("10");
//					}else{
//						salesInfo.setCard_type("8");
//					}
					//若人员已经存在分级数据，则先将已存在的分级信息置为失效，保存最新的分级数据
					dataList.add(salesInfo);
				}
			}
			if("".equals(sb.toString())){
				for(SalesInfo Info:dataList){
					salesInfoDao_hd.updateImportData(Info);
					salesInfoDao_hd.salesInfoImportData(Info);
				}

				sb.append("Y");
			}

		}
		return sb.toString();
	}
	public String salesInfoImportCheckDataAnother(InputStream fileStream,String branchId, String channel_id,String branch_id22, String branch_name22)throws BiffException, IOException {

		boolean flag = true;
		StringBuffer sb = new StringBuffer("");

		Workbook wb = Workbook.getWorkbook(fileStream);
		Sheet s = wb.getSheet(0);//第1个sheet
		int row = s.getRows();//总行数
		List<SalesInfo> dataList =new ArrayList<SalesInfo>();//记录合格数据
		//验证导入数据是否正确
		if (row > 1) {
			boolean check = checkTemplateAnother(s);
			if(!check){
				sb.append("N");
				return sb.toString();
			}
			List<String> salesCodes = new ArrayList<String>();
			for (int i = 1; i < row; i++) {
				// 开始验证
				String branch_id1 = "";
				String branch_name1 = s.getCell(0, i).getContents();
				String branch_id2 = s.getCell(1, i).getContents();
				String branch_name2 = s.getCell(2, i).getContents();
				String branch_id3 = s.getCell(3, i).getContents();
				String branch_name3 = s.getCell(4, i).getContents();
				String branch_id = s.getCell(5, i).getContents();
				String branch_name = s.getCell(6, i).getContents();
				String sales_code = s.getCell(7, i).getContents();
				String inside_id = s.getCell(8, i).getContents();
				String sales_name = s.getCell(9, i).getContents();
				String prof_no = s.getCell(10, i).getContents();
				String certificate_type = s.getCell(11, i).getContents();
				int sc = sales_code.length();
				String scc = String.valueOf(sc);
				if (salesCodes.contains(sales_code)){
					sb.append("第" + (i+1) + "行，导入数据中存在重复的人员，请核对！<br/>");
				}
				salesCodes.add(sales_code);
				if (UtilString.trim(branch_name1).equals("")|| UtilString.trim(branch_name1)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，一级机构名称不可为空，请核对！<br/>");
				}else if (!UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")&&!UtilString.trim(branch_name1).equals("中国人民健康保险股份有限公司")){
					flag = false;
					sb.append("第" + (i+1) + "行，一级机构仅可为中国人民财产保险股份有限公司或中国人民健康保险股份有限公司，请导入正确的一级机构名称！<br/>");
				}
				/*else if (!branch_name1.contains("财产")&&!branch_name1.contains("健康")){
					flag = false;
					sb.append("第" + (i+1) + "行，请导入中国人民财产保险股份有限公司或中国人民健康保险股份有限公司，请核对！<br/>");
				}*/
				if (UtilString.trim(branch_id2).equals("")|| UtilString.trim(branch_id2)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构代码不可为空，请核对！<br/>");
				}else if(UtilString.trim(branch_id2).length()!=8||branch_id2.contains(" ")){
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构代码须为8位的财险或健康险二级机构代码，且不得包含空格，请核对！<br/>");
				}else if (UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")){
					String branch_id2_cx = queryCXBranchId2 (branch_id2);
					if(UtilString.trim(branch_id2_cx).equals("")|| UtilString.trim(branch_id2_cx)==null){
						flag = false;
						sb.append("第" + (i+1) + "行，二级机构代码错误，请导入财险机构代码，请核对！<br/>");
					}
				}
				if (UtilString.trim(branch_name2).equals("")|| UtilString.trim(branch_name2)==null) {
					flag = false;
					sb.append("第" + (i+1) + "行，二级机构名称不可为空，请核对！<br/>");
				}
				if (UtilString.trim(branch_name1).equals("中国人民财产保险股份有限公司")) {
					branch_name2 = queryCXBranchId2 (branch_id2);
				}
				if ((UtilString.trim(sales_code).equals("")|| UtilString.trim(sales_code)==null )|| !scc.equals("10")) {
					flag = false;
					sb.append("第" + (i+1) + "行，集团统一工号不可为空且必须为10位数字，请核对！<br/>");
				}
				if (UtilString.trim(sales_name).equals("")|| UtilString.trim(sales_name)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，人员姓名不可为空，请核对！<br/>");
				}
				if (UtilString.trim(prof_no).equals("")|| UtilString.trim(prof_no)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，执业证编号不可为空，请核对！<br/>");
				}

				if (UtilString.trim(certificate_type).equals("")|| UtilString.trim(certificate_type)==null ) {
					flag = false;
					sb.append("第" + (i+1) + "行，当前资质等级不可为空，请核对！<br/>");
				}else if (!certificate_type.equals("0")&&!certificate_type.equals("1")
						&&!certificate_type.equals("2")&&!certificate_type.equals("3")&&!certificate_type.equals("5")){
					flag = false;
					sb.append("第" + (i+1) + "行，录入的当前资质等级代码不存在，请核对！<br/>");
				}
				String user_branchId=branchId.substring(1,3);
				//“四级机构代码+省代码”调取手续费系统【交叉销售人员信息查询接口】校验导入的产险人员信息准确性
//				int count = salesInfoDao_hd.checkCount(sales_code,user_branchId);
//				if (count != 1) {
//					flag = false;
//					sb.append("第" + (i+1) + "行，产险人员信息不存在，请核对！<br/>");
//				}
				//改为    通过“集团统一工号”调取集团统一工号平台【工号查询接口】校验导入的产险人员信息准确性
				RequestModel m=new RequestModel();
				m.setUniSalesCod(sales_code);
				m.setMessageType("01");
				m.setManOrgCod(branch_id22);
				ResponseModel rm = null;

				try {
					rm = unifiedJobNumberServiceImpl.getJobNumberP(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String uniSalesCod = rm.getSalesCode();
				String id_no = " ";
				String idtyp_cod = " ";
				if (uniSalesCod==null||"".equals(uniSalesCod)){
					flag = false;
					sb.append("第" + (i+1) + "行，产险人员信息不存在，请核对！<br/>");
				}else{
					branch_id1 = rm.getComp_cod();
					id_no = rm.getId_no();
					idtyp_cod = rm.getIdtyp_cod();
				}
				//预授权资质代码(云南)	0-无，1-初级，2-中级，3-高级，4-过期 preauthorization_level
				//预授权资质代码(河北)	2-中级
				String preauthorizationLevel = "'0','1','2','3','4'";
				//已授权资质代码(云南) 没有则给0-无，530101-初级，530102-中级，530103-高级 certificate_type
				//已授权资质代码(河北) 1-初级，2-中级，3-高级
				String certificateType = "'0','530101','530102','530103'";
				if("13".equals(user_branchId)){
					preauthorizationLevel="2";
					certificateType="'1','2','3'";
				}
				if(flag){
					SalesInfo salesInfo = new SalesInfo();
					salesInfo.setSales_code(sales_code);
					salesInfo.setBranch_id1(branch_id1);
					salesInfo.setBranch_name1(branch_name1);
					salesInfo.setBranch_id2(branch_id2);
					salesInfo.setBranch_name2(branch_name2);
					salesInfo.setBranch_id3(branch_id3);
					salesInfo.setBranch_name3(branch_name3);
					salesInfo.setBranch_id(branch_id);
					salesInfo.setBranch_name(branch_name);
					salesInfo.setSales_code(sales_code);
					salesInfo.setSales_name(sales_name);
					salesInfo.setProf_no(prof_no);
					salesInfo.setCertificate_type(certificate_type);
					salesInfo.setInside_id(inside_id);
					salesInfo.setBranchIdCur(branchId);
					salesInfo.setOpt_branchid_2(branch_id22);
					salesInfo.setCard_no(id_no);
					if(idtyp_cod.equals("11")){
						salesInfo.setCard_type("0");
					}else if(idtyp_cod.equals("14")){
						salesInfo.setCard_type("1");
					}else if(idtyp_cod.equals("15")){
						salesInfo.setCard_type("2");
					}else if(idtyp_cod.equals("23")){
						salesInfo.setCard_type("10");
					}else{
						salesInfo.setCard_type("8");
					}
//					//查询产险人员证件号与类型进行转码
//					SalesInfo info =salesInfoDao_hd.queryCard(salesInfo);
//					if (info!=null){
//						salesInfo.setCard_no(info.getId_no());
//						if(info.getIdtyp_cod().equals("11")){
//							salesInfo.setCard_type("0");
//						}else if(info.getIdtyp_cod().equals("14")){
//							salesInfo.setCard_type("1");
//						}else if(info.getIdtyp_cod().equals("15")){
//							salesInfo.setCard_type("2");
//						}else if(info.getIdtyp_cod().equals("23")){
//							salesInfo.setCard_type("10");
//						}else{
//							salesInfo.setCard_type("8");
//						}
//					}
//					if (salesInfo.getCard_type()==null){
//						salesInfo.setCard_type(" ");
//					}
//					if (salesInfo.getCard_no()==null){
//						salesInfo.setCard_no(" ");
//					}
					//若人员已经存在分级数据，则先将已存在的分级信息置为失效，保存最新的分级数据
					dataList.add(salesInfo);
				}
			}
			if("".equals(sb.toString())){
				for(SalesInfo Info:dataList){
					salesInfoDao_hd.updateImportData(Info);
					salesInfoDao_hd.salesInfoImportDataAnother(Info);
				}

				sb.append("Y");
			}

		}
		return sb.toString();
	}
	public String salesInfoImportCheckDataP(InputStream fileStream,String branchId, String channel_id,String branch_id22, String branch_name22)throws BiffException, IOException {

		long startMilliSeconds = System.currentTimeMillis();
		boolean flag = true;
		StringBuffer sb = new StringBuffer("");
		SalesInfo config = null;
		if (!"".equals(branch_id22)&&!"137".equals(branch_id22.substring(0,3))&&!"145".equals(branch_id22.substring(0,3))){
			//判断该二级机构有无配置
			config = salesInfoDao_hd.countConfig(branch_id22);
			if (config==null){
				flag = true;
				sb.append("因该分公司没有配置，暂不支持导入!");
				return sb.toString();
			}
		}


		Workbook wb = Workbook.getWorkbook(fileStream);
		Sheet s = wb.getSheet(0);//第1个sheet
		int row = s.getRows();//总行数
		List<SalesInfo> dataList =new ArrayList<SalesInfo>();//记录合格数据
		List<String> listSalesCode =new ArrayList<>();//存储合格所有工号
		//验证导入数据是否正确
		if (row > 1) {
			boolean check = checkTemplateP(s);
			if(!check){
				sb.append("N");
				return sb.toString();
			}
			List<String> salesCodes = new ArrayList<String>();
			List<Map<String,String>> salesList = new ArrayList<>();
			List<SalesInfo> dataListTem =new ArrayList<SalesInfo>();
			//先插入临时表
            //批次id
            String uuid = UUID.randomUUID().toString();
			for (int i = 1; i < row; i++) {
				HashMap<String,String> map = new HashMap<>();
				SalesInfo salesInfoTem = new SalesInfo();
				map.put("sales_code",s.getCell(0, i).getContents());
				map.put("sales_name", s.getCell(1, i).getContents());
				map.put("prof_no", s.getCell(2, i).getContents());
				map.put("preauthorization_level", s.getCell(3, i).getContents());
//				map.put("preauthorization_lapsedate", s.getCell(4, i).getContents());
				map.put("certificate_type", s.getCell(4, i).getContents());
				map.put("certificate_releasedate", s.getCell(5, i).getContents());
				map.put("tempRow",Integer.toString(i));
				map.put("uuid",uuid);
				salesList.add(map);

				salesInfoTem.setSales_code(s.getCell(0, i).getContents());
				salesInfoTem.setSales_name(s.getCell(1, i).getContents());
				salesInfoTem.setProf_no(s.getCell(2, i).getContents());
				salesInfoTem.setPreauthorization_level(s.getCell(3, i).getContents());
				salesInfoTem.setCertificate_type(s.getCell(4, i).getContents());
				salesInfoTem.setCertificate_releasedate(s.getCell(5, i).getContents());
				salesInfoTem.setTempRow(Integer.toString(i));
				salesInfoTem.setUuid(uuid);
				dataListTem.add(salesInfoTem);



			}
			//插入，包括人员表中的id_no,id_type,probation_date,sales_code,branch_id,channel_id 执业证表中的执业证id 注册时间，注意银保是另一张表
			//插入之后进行更新，将人员表，执业证表的字段更新到临时表
			if ("03".equals(channel_id)){
				salesInfoDao_hd.insertTempYB(dataListTem);
			}else {
				salesInfoDao_hd.insertTemp(dataListTem);
			}

			//然后把这批次的数据查出来，用来校验，替换那两个耗时长的sql，如果匹配不正确或者为空，那就提示错误
            List<SalesInfo> SalesListTemp = salesInfoDao_hd.selectTemp(uuid);
            //如果都没有问题，先将这批次的人员已有数据置为失效，再将临时表中的数据复制到分级表中，然后将临时表数据该批次删除


			//过渡期这个再用一次查询  查寿险所有
			List<SalesInfo> SalesListStartandITime = salesInfoDao_hd.selectStartandITime();
            //判断是总公司导入还是分公司导入
//			if ("".equals(branch_id22)){
//
//			}else{
//
//			}

//			long startMilliSeconds1 = System.currentTimeMillis();
//			System.out.println("startMilliSeconds1:"+(startMilliSeconds1-startMilliSeconds)/1000);
			//在验证之前将河北数据查出，作为判断根据，不再访问数据库
//			String branchlike = "1130000";
			//取当前登录机构的省机构id
			String branchlike = branch_id22;
			//1 人员表 sales_code,branch_id,id_type,id_no channel_id
//			List<SalesInfo> hbSalesList = new ArrayList<SalesInfo>();
//			if ("".equals(branch_id22)){
//				hbSalesList = salesInfoDao_hd.queryhbSalesListAll(channel_id);
//			}else{
//				hbSalesList = salesInfoDao_hd.queryhbSalesList(channel_id,branchlike);
//			}
//			//判断人员工号是否有误
//			List<SalesInfo> allSalesList = salesInfoDao_hd.queryhbSalesList(channel_id,"");
			//2 执业证表 执业证号 注册时间 人员代码 根据渠道划分表
//			List<SalesInfo> hbZyzList = salesInfoDao_hd.queryhbZyzList(channel_id,branchlike);
			//3 机构表 id name
			List<SalesInfo> hbBranchList = salesInfoDao_hd.queryhbBranchList(branchlike);

			//根据二级机构查询开始时间还有持续时间 branch_id22
//			List<SalesInfo> infoList = salesInfoDao_hd.queryt_levelconfig(branch_id22);
//			long startMilliSeconds2 = System.currentTimeMillis();
//			System.out.println("startMilliSeconds2:"+(startMilliSeconds2-startMilliSeconds1)/1000);
			String transition_start_old = "";
			Date date_transition_start_old =null;
			String transition_time_old = "";

			String transition_start_new = "";
			Date date_transition_start_new=null;
			String transition_time_new = "";
			for (int i = 0; i < salesList.size(); i++) {
				// 开始验证
				String sales_code = salesList.get(i).get("sales_code");
				String sales_name = salesList.get(i).get("sales_name");
				String prof_no = salesList.get(i).get("prof_no");
				String preauthorization_level = salesList.get(i).get("preauthorization_level");
//				String preauthorization_lapsedate = salesList.get(i).get("preauthorization_lapsedate");
				String certificate_type = salesList.get(i).get("certificate_type");
				String certificate_releasedate = salesList.get(i).get("certificate_releasedate");

				String prof_noTime  =  "";
				String card_type  =  "";
				String card_no  =  "";
				String probation_date  =  "";
				int sc = sales_code.length();
				String scc = String.valueOf(sc);
				String branch_id2 = "";
				String branch_name2 = "";
				String branch_id3 = "";
				String branch_name3 = "";
				String branch_name4 = "";
				String branch_id4 = "";
				String channelIdQuery = "";
				String flagCon = "";
				if (salesCodes.contains(sales_code)){
					sb.append("第" + (i+2) + "行，导入数据中存在重复的人员，请核对！<br/>");
				}
				salesCodes.add(sales_code);
				//根据salescode查询机构
				String branchIdCurrent = "";
				String sales_nameTem = "";
				if ((UtilString.trim(sales_code).equals("")|| UtilString.trim(sales_code)==null )|| !scc.equals("10")) {
					flag = false;
					sb.append("第" + (i+2) + "行，人员工号不可为空且必须为10位数字，请核对！<br/>");
				}else {


					for (SalesInfo salesInfoSales : SalesListTemp){
						if(sales_code.equals(salesInfoSales.getSales_code())){
							branchIdCurrent = salesInfoSales.getBranch_id();
							card_no = salesInfoSales.getId_no();
							card_type = salesInfoSales.getId_type();
							probation_date = salesInfoSales.getProbation_date();
							channelIdQuery = salesInfoSales.getChannel_id();
							flagCon = salesInfoSales.getTransition_start();
//							if ("".equals(branch_id22)){
//								infoList = salesInfoDao_hd.queryt_levelconfig(branchIdCurrent.substring(0,3)+"0000");
//							}
							sales_nameTem = salesInfoSales.getSales_name();

							break;
						}
					}
					//检验是否有配置表
					if (!"145".equals(branch_id22.substring(0,3))&&!"137".equals(branch_id22.substring(0,3))&&("".equals(branch_id22))&&(flagCon==null||"".equals(flagCon))){
						flag = false;
						sb.append("第" + (i+2) + "行，因该人员所在分公司没有配置，暂不支持导入！<br/>");
						continue;
					}
					//将二三四级机构名称存在表里

					if (!"".equals(branch_id22)&&(branchIdCurrent==null||"".equals(branchIdCurrent)||!branch_id22.substring(0,3).equals(branchIdCurrent.substring(0,3)))){
						flag = false;
						sb.append("第" + (i+2) + "行，人员工号不属于"+branch_name22+"，请核对！<br/>");
					}else if (!(branchIdCurrent==null||"".equals(branchIdCurrent))){
						Map<String,String> treeMap = salesInfoDao_hd.getBranchTree(branchIdCurrent);
						branch_id2 = treeMap.get("branch_id2");
						branch_name2 = treeMap.get("branch_name2");
						branch_id3 = treeMap.get("branch_id3");
						branch_name3 = treeMap.get("branch_name3");
						branch_id4 = treeMap.get("branch_id4");
						branch_name4 = treeMap.get("branch_name4");

					}
				}
				if ((UtilString.trim(sales_name).equals("")|| UtilString.trim(sales_name)==null )) {
					flag = false;
					sb.append("第" + (i+2) + "行，人员姓名不可为空且必须和人员工号的对应关系保持一致，请核对！<br/>");
				}else if(!sales_name.equals(sales_nameTem)){
					flag = false;
					sb.append("第" + (i+2) + "行，人员姓名不可为空且必须和人员工号的对应关系保持一致，请核对！<br/>");
				}
				if (UtilString.trim(prof_no).equals("")|| UtilString.trim(prof_no)==null ) {
					flag = false;
					sb.append("第" + (i + 2) + "行，执业证编号不可为空，请核对！<br/>");
				}

				//判断人员是否为登录渠道的人员
				//根据salescode查询渠道
//				String channelIdQuery = salesInfoDao_hd.queryChannelIdByCode(sales_code);
//				String channelIdQuery = "";
//				for (SalesInfo salesInfoSales : allSalesList){
//					if (sales_code.equals(salesInfoSales.getSales_code())){
//						channelIdQuery = salesInfoSales.getChannel_id();
//						break;
//					}
//				}
				if (channelIdQuery==null
				||!channel_id.equals(channelIdQuery) ) {
					flag = false;
					if (!((UtilString.trim(sales_code).equals("")|| UtilString.trim(sales_code)==null )|| !scc.equals("10"))){

						sb.append("第" + (i + 2) + "行，人员工号有误，请核对！<br/>");
					}
				}else {
					if (UtilString.trim(prof_no).equals("")|| UtilString.trim(prof_no)==null ) {

					}else if(!((UtilString.trim(sales_code).equals("")|| UtilString.trim(sales_code)==null )|| !scc.equals("10"))){
						//校验是否正确，银保用另一张表
						String proQuery = "";
						for (SalesInfo salesInfoZyz : SalesListTemp){
							if (sales_code.equals(salesInfoZyz.getSales_code())){
								proQuery = salesInfoZyz.getDevelop_id();
								prof_noTime = salesInfoZyz.getGive_date();
								break;
							}
						}
						if(prof_no==null||!prof_no.equals(proQuery)){
							flag = false;
							sb.append("第" + (i+2) + "行，人员执业证编号有误，请核对！<br/>");
						}

					}
					if (!"137".equals(branch_id22.substring(0,3))){
					//查询执业证的注册时间，银保用另一张表
					//在上面的代码中，若执业证号不为空校验是否正确的时候 将注册时间也进行了赋值，此处就不用了
					//转为date类型，比较大小
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						try {
							//改为入司时间
							Date dateProf_no = format.parse(probation_date);
							//改为功能上线，待定
//						//根据二级机构查询开始时间还有持续时间 branch_id22
//						List<SalesInfo> infoList = salesInfoDao_hd.queryt_levelconfig(branch_id22);
							//找到老人过渡期
							for (SalesInfo info:SalesListStartandITime){
								if ("2".equals(info.getPeople_sign())&&(branchIdCurrent.substring(0,3)+"0000").equals(info.getBranch_id())){
									transition_start_old = info.getTransition_start();
									date_transition_start_old = format.parse(transition_start_old);
									transition_time_old = info.getTransition_time();
									break;
								}
							}
							//找到新人过渡期
							for (SalesInfo info:SalesListStartandITime){
								if ("1".equals(info.getPeople_sign())&&(branchIdCurrent.substring(0,3)+"0000").equals(info.getBranch_id())){
									transition_start_new = info.getTransition_start();
									date_transition_start_new = format.parse(transition_start_new);
									transition_time_new = info.getTransition_time();
									break;
								}
							}
//						String transition_start = info.getTransition_start();
//						Date date_transition_start = format.parse(transition_start);
//						String transition_time = info.getTransition_time();
							Date date20231101 = format.parse("2023-11-01");
							Date currentDate = new Date();
							if (dateProf_no.compareTo(date20231101)<0){
								//老人，判断是否在过渡期内
								// 创建Calendar对象，设置时间为注册日期
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(date_transition_start_old);

								// 将日期加上一年
//							calendar.add(Calendar.YEAR, 1);
								//现在不确定是多少了
								calendar.add(Calendar.MONTH, Integer.parseInt(transition_time_old));
								// 获取加上一年后的日期
								Date newDate = calendar.getTime();
								if (newDate.compareTo(currentDate)>0){
									//过渡期结束时间大于当前系统时间，说明还在过渡期内，此时预授权资质不可为空
									if (UtilString.trim(preauthorization_level).equals("")|| UtilString.trim(preauthorization_level)==null ) {
										flag = false;
										sb.append("第" + (i+2) + "行，预授权资质不可为空，请核对！<br/>");
									}else{
										if(!"2".equals(preauthorization_level)){
											flag = false;
											sb.append("第" + (i+2) + "行，人员预授权资质代码有误，请核对！<br/>");

										}
									}
								}else{
									//不在过渡期内，可以为空，但是不能有误
									if(!"".equals(UtilString.trim(preauthorization_level))&&!"2".equals(preauthorization_level)){
										flag = false;
										sb.append("第" + (i+2) + "行，人员预授权资质代码有误，请核对！<br/>");

									}
								}

							}else{
								//新人
								Calendar calendar = Calendar.getInstance();
								calendar.setTime(date_transition_start_new);

								// 将日期加上一年
//							calendar.add(Calendar.YEAR, 1);
								//现在不确定是多少了
								calendar.add(Calendar.MONTH, Integer.parseInt(transition_time_new));
								// 获取加上一年后的日期
								Date newDate = calendar.getTime();
								if (newDate.compareTo(currentDate)>0){
									if (UtilString.trim(preauthorization_level).equals("")|| UtilString.trim(preauthorization_level)==null ) {
										flag = false;
										sb.append("第" + (i+2) + "行，预授权资质不可为空，请核对！<br/>");
									}else{
										//不在过渡期内，可以为空，但是不能有误
										if(!"2".equals(preauthorization_level)){
											flag = false;
											sb.append("第" + (i+2) + "行，人员预授权资质代码有误，请核对！<br/>");
										}
									}
								}
//							if (UtilString.trim(preauthorization_level).equals("")|| UtilString.trim(preauthorization_level)==null ) {
//
//							}
//							else{
//								if(!"2".equals(preauthorization_level)){
//									flag = false;
//									sb.append("第" + (i+2) + "行，人员预授权资质代码有误，请核对！<br/>");
//
//								}
//							}
							}


						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					}









				if (UtilString.trim(certificate_type).equals("")|| UtilString.trim(certificate_type)==null ) {
					flag = false;
					sb.append("第" + (i+2) + "行，已授权资质不可为空，请核对！<br/>");
				}else{
					if(!("1".equals(certificate_type)||"2".equals(certificate_type)||"3".equals(certificate_type))){
						flag = false;
						sb.append("第" + (i+2) + "行，人员已授权资质代码有误，请核对！<br/>");

					}
				}
				// 正则表达式: 四位数年份/两位数月份/两位数日期
				String regex = "^\\d{4}/\\d{2}/\\d{2}$";
				if (UtilString.trim(certificate_releasedate).equals("")|| UtilString.trim(certificate_releasedate)==null||!certificate_releasedate.matches(regex) ) {
					flag = false;
					sb.append("第" + (i+2) + "行，已授权资质颁发日期不可为空且格式必须为YYYY/MM/DD，请核对！<br/>");
					// 解析日期字符串，如果解析成功则表示格式正确
//					try {
//						SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
//						format.setLenient(false); // 设置为严格模式，不符合格式的日期将抛出异常
//						format.parse(certificate_releasedate);
//					} catch (Exception e) {
//
//					}
				}

				if(flag){
					SalesInfo salesInfo = new SalesInfo();
					salesInfo.setBranch_id2(branch_id2);
					salesInfo.setBranch_name2(branch_name2);
					salesInfo.setBranch_id3(branch_id3);
					salesInfo.setBranch_name3(branch_name3);
					salesInfo.setBranch_id(branch_id4);
					salesInfo.setBranch_name(branch_name4);
					salesInfo.setSales_code(sales_code);
					salesInfo.setSales_name(sales_name);
					salesInfo.setProf_no(prof_no);
					salesInfo.setPreauthorization_level(preauthorization_level);
//					salesInfo.setPreauthorization_lapsedate(preauthorization_lapsedate);
					salesInfo.setCertificate_type(certificate_type);
					salesInfo.setCertificate_releasedate(certificate_releasedate);
					if (prof_noTime!=null&&!"".equals(prof_noTime)){
						prof_noTime = prof_noTime.replaceAll("/", "-");
					}

					salesInfo.setRegistration_time(prof_noTime);
					//查询证件号和证件类型
					//在校验人员工号十位数的地方 将 no type 进行了赋值，此处不需要查询

					salesInfo.setCard_type(card_type);
					salesInfo.setCard_no(card_no);
					salesInfo.setChannel_id(channel_id);
					//若人员已经存在分级数据，则先将已存在的分级信息置为失效，保存最新的分级数据
					//先获取人员原先的万能险销售资质
					String isUniversalInsurance = salesInfoDao_hd.getIsUniversalInsurance(sales_code);
					salesInfo.setIs_universal_insurance(isUniversalInsurance);
					dataList.add(salesInfo);
					listSalesCode.add(sales_code);
				}
			}
//			long startMilliSeconds3 = System.currentTimeMillis();
//			System.out.println("startMilliSeconds3:"+(startMilliSeconds3-startMilliSeconds2)/1000);
			if("".equals(sb.toString())){
				int size = listSalesCode.size();
				int num =  size/200;
				if (size%200>0){
					num++;
				}
				List<String> listTem = null;
				for (int i=0;i<num;i++){
					//num为需要更新的次数
					//第一次200先，先拼接200个,取出前二百循环拼接，执行更新，进行下一次拼接更新
					String data = "";

					if (i==num-1){
						listTem = listSalesCode.subList(i * 200, size);
					}else {
						listTem = listSalesCode.subList(i * 200, (i + 1) * 200);
					}

					for (String code : listTem){
						data = data+"'"+code +"',";
					}
					//去掉最后的逗号
					data = data.substring(0, data.length() - 1);
					//执行更新，使用in（）
					salesInfoDao_hd.updateImportDataP(data);


				}

				salesInfoDao_hd.salesInfoImportDataP(dataList);

				sb.append("Y");
			}
			salesInfoDao_hd.deleteTem(uuid);
		}

		return sb.toString();
	}
	//校验是否为模板文件
	public boolean checkTemplate(Sheet s){
		boolean template = false;
		try{
			String branch_name1 = s.getCell(0, 0).getContents();
			String branch_id2 = s.getCell(1, 0).getContents();
			String branch_name2 = s.getCell(2, 0).getContents();
			String branch_id3 = s.getCell(3, 0).getContents();
			String branch_name3 = s.getCell(4, 0).getContents();
			String branch_id = s.getCell(5, 0).getContents();
			String branch_name = s.getCell(6, 0).getContents();
			String sales_code = s.getCell(7, 0).getContents();
			String inside_id = s.getCell(8, 0).getContents();
			String sales_name = s.getCell(9, 0).getContents();
			String prof_no = s.getCell(10, 0).getContents();
			String preauthorization_level = s.getCell(11, 0).getContents();
			String certificate_type = s.getCell(12, 0).getContents();
			String certificate_releasedate = s.getCell(13, 0).getContents();
			String people_sign = s.getCell(14, 0).getContents();
			if(branch_name1.equals("一级机构名称") &&branch_id2.equals("二级机构代码") &&branch_name2.equals("二级机构名称")
					&&branch_id3.equals("三级机构代码")
					&&branch_name3.equals("三级机构名称") &&branch_id.equals("四级机构代码") && branch_name.equals("四级机构名称")
					&& sales_code.equals("集团统一工号")&& inside_id.equals("公司内部工号")
					&& sales_name.equals("人员姓名")&& prof_no.equals("执业证编号") && preauthorization_level.equals("预授权资质")
					&& certificate_type.equals("已授权资质")&& people_sign.equals("新老人标识")&& certificate_releasedate.equals("已授权资质颁发日期")){
				template = true;
			}
		}catch (Exception e){
			return template;
		}
		return template;
	};//校验是否为模板文件
	public boolean checkTemplateAnother(Sheet s){
		boolean template = false;
		try{
			String branch_name1 = s.getCell(0, 0).getContents();
			String branch_id2 = s.getCell(1, 0).getContents();
			String branch_name2 = s.getCell(2, 0).getContents();
			String branch_id3 = s.getCell(3, 0).getContents();
			String branch_name3 = s.getCell(4, 0).getContents();
			String branch_id = s.getCell(5, 0).getContents();
			String branch_name = s.getCell(6, 0).getContents();
			String sales_code = s.getCell(7, 0).getContents();
			String inside_id = s.getCell(8, 0).getContents();
			String sales_name = s.getCell(9, 0).getContents();
			String prof_no = s.getCell(10, 0).getContents();
			String certificate_type = s.getCell(11, 0).getContents();
			if(branch_name1.equals("一级机构名称") &&branch_id2.equals("二级机构代码") &&branch_name2.equals("二级机构名称")
					&&branch_id3.equals("三级机构代码")
					&&branch_name3.equals("三级机构名称") &&branch_id.equals("四级机构代码") && branch_name.equals("四级机构名称")
					&& sales_code.equals("集团统一工号")&& inside_id.equals("公司内部工号")
					&& sales_name.equals("人员姓名")&& prof_no.equals("执业证编号")
					&& certificate_type.equals("当前资质等级")){
				template = true;
			}
		}catch (Exception e){
			return template;
		}
		return template;
	};	//校验是否为模板文件
	public boolean checkTemplateP(Sheet s){
		boolean template = false;
		try{
			String sales_code = s.getCell(0, 0).getContents();
			String sales_name = s.getCell(1, 0).getContents();
			String prof_no = s.getCell(2, 0).getContents();
			String preauthorization_level = s.getCell(3, 0).getContents();
//			String preauthorization_lapsedate = s.getCell(4, 0).getContents();
			String certificate_type = s.getCell(4, 0).getContents();
			String certificate_releasedate = s.getCell(5, 0).getContents();
			if(sales_code.equals("人员工号") && sales_name.equals("人员姓名") && prof_no.equals("执业证编号")
					&& preauthorization_level.equals("预授权资质")
//					&& preauthorization_lapsedate.equals("预授权日期")
					&& certificate_type.equals("已授权资质")
					&& certificate_releasedate.equals("已授权资质颁发日期")){
				template = true;
			}
		}catch (Exception e){
			return template;
		}
		return template;
	};

	/**
	 * @author lgcheng 根据查询条件导出寿代产业绩
	 */
	public List<String> exportPolicy(WagesPrint wprint) {
		List<String> sheet = new ArrayList<String>();
		// 定义导出数据列
		String columns = "branch_id,branch_name,team_id,team_name,sales_id,sales_name,policy_no,prd_code,prd_name,issue_date,premium,prem_cross,allowance_cross";

		sheet.add(columns);

		List<WagesPrint> list = salesInfoDao_hd.exportPolicy(wprint);

		if (list != null && list.size() > 0) {
			for (WagesPrint wp : list) {
				String issue_date = "";
				if (wp.getIssue_date() != null && !"".equals(wp.getIssue_date())) {
					issue_date = UtilDate.fmtDate(wp.getIssue_date());
				}

				String colData = wp.getBranch_id() + "," + wp.getBranch_name() + "," + wp.getTeam_id() + ","
						+ wp.getTeam_name() + "," + wp.getSales_code() // M by wang_gq for R309集团统一工号4期 on 20141210
						+ "," + wp.getSales_name() + "," + wp.getPolicy_no() + "," + wp.getPrd_code() + ","
						+ wp.getPrd_name() + "," + issue_date + "," + wp.getPremium() + "," + wp.getPrem_cross() + ","
						+ wp.getAllowance_cross();

				sheet.add(colData);
			}
		}

		return sheet;
	}

	/**
	 * 个险寿代产明细导出 lpd 20191112
	 */
	public List<String> exportPolicyGx(WagesPrint wprint) {
		List<String> sheet = new ArrayList<String>();
		// 定义导出数据列
		String columns = "branch_id,branch_name,team_id,team_name,sales_id,sales_name,policy_no,prd_code,prd_name,issue_date,premium,poundage_rate,poundage_amount,insert_time";

		sheet.add(columns);

		List<WagesPrint> list = salesInfoDao_hd.exportPolicyGx(wprint);

		if (list != null && list.size() > 0) {
			for (WagesPrint wp : list) {
				String issue_date = "";
				if (wp.getIssue_date() != null && !"".equals(wp.getIssue_date())) {
					issue_date = UtilDate.fmtDate(wp.getIssue_date());
				}
				// 上载时间
				String inserttime = "";
				if (wp.getInsert_time() != null && !"".equals(wp.getInsert_time())) {
					inserttime = UtilDate.fmtDate(wp.getInsert_time());
				}
				String colData = wp.getBranch_id() + "," + wp.getBranch_name() + "," + wp.getTeam_id() + ","
						+ wp.getTeam_name() + "," + wp.getSales_code() // M by wang_gq for R309集团统一工号4期 on 20141210
						+ "," + wp.getSales_name() + "," + wp.getPolicy_no() + "," + wp.getPrd_code() + ","
						+ wp.getPrd_name() + "," + issue_date + "," + wp.getPremium() + "," + wp.getPoundage_rate()
						+ "," + wp.getPoundage_amount() + "," + inserttime;

				sheet.add(colData);
			}
		}

		return sheet;
	}

	/**
	 * 检查团险业绩导入数据是否正确
	 */
	public String checkTXData(InputStream fileStream, UserInfo user) throws BiffException, IOException {
		String validateResult = "";

		Workbook wb = Workbook.getWorkbook(fileStream);
		Sheet s = wb.getSheet(0);// 第1个sheet
		int row = s.getRows();// 总行数

		// 验证导入数据是否正确
		if (row > 1) {
			for (int i = 1; i < row; i++) {

				// 验证业务人员代码和姓名是否存在
				if (s.getCell(0, i).getContents() != null && !"".equals(s.getCell(0, i).getContents())) {
					if (!importValidateDao.querySalesInfoNoDimission(DataConst.Channel_ID_Tx,
							s.getCell(0, i).getContents().trim(), s.getCell(1, i).getContents(), user))// 验证业务人员代码以及姓名是否存在
					{
						validateResult += i + 1 + ",";
						break;
					}
				} else {
					validateResult += i + 1 + ","; // 如果业务人员代码为空
					break;
				}

				// 验证保单号
				if (s.getCell(2, i).getContents() != null && !"".equals(s.getCell(2, i).getContents())) {
					if (salesInfoDao_hd.queryPolicyByNo(s.getCell(2, i).getContents().trim()))// 验证保单号是否已经存在
					{
						validateResult += i + 1 + ",";
						break;
					}
				} else {
					validateResult += i + 1 + ","; // 如果保单号为空
					break;
				}

				// 验证产品类别代码
				if (s.getCell(3, i).getContents() != null && !"".equals(s.getCell(3, i).getContents()))// 验证产品类别是否为空
				{
					String pre_type = codecodeDao.queryCodeName(s.getCell(3, i).getContents().trim(),
							DataConst.Channel_ID_Tx, CodeTypeConst.CODE_TYPE_GROUP_PRD_TYPE);
					if (pre_type == null || "".equals(pre_type)) {
						validateResult += i + 1 + ",";
						break;
					}
				} else {
					validateResult += i + 1 + ","; // 如果产品类别代码为空
					break;
				}

				// 验证承包日期
				if (s.getCell(6, i).getContents() != null && !"".equals(s.getCell(6, i).getContents())) {
					if (!importValidateDao.validateDate(s.getCell(6, i).getContents()))// 验证承包日期格式是否正确
					{
						validateResult += i + 1 + ",";
						break;
					} else {
						// 根据人员ID查询人员所在机构
						SalesInfo sales = importValidateDao.getSalesInfo(s.getCell(0, i).getContents().trim());

						if (sales != null) {
							// 查询人员所在机构的佣金月
							String stat_month = taskManagerDao.getFmtStatMonth(DataConst.Channel_ID_Tx,
									sales.getBranch_id());
							String date = s.getCell(6, i).getContents().trim().substring(0, 10);
							// 比较承包日期是否大于等于当前佣金月
							if (!importValidateDao.compareDate(date, stat_month + "-01")) {
								validateResult += i + 1 + ",";
								break;
							}
						}
					}
				} else {
					validateResult += i + 1 + ",";// 如果承包日期为空
					break;
				}

				// 验证规模保费
				if (s.getCell(7, i).getContents() != null && !"".equals(s.getCell(7, i).getContents())) {
					if (!importValidateDao.validateShortFigure(s.getCell(7, i).getContents()))// 验证规模保费是否正确
					{
						validateResult += i + 1 + ",";
						break;
					}
				} else {
					validateResult += i + 1 + ",";// 如果规模保费为空
					break;
				}

				// 验证津贴比例
				if (s.getCell(8, i).getContents() != null && !"".equals(s.getCell(8, i).getContents())) {
					if (importValidateDao.validateFigure(s.getCell(8, i).getContents()))// 验证津贴比例是否为数字类型
					{
						if (!(new Double(s.getCell(8, i).getContents()) >= 0
								&& new Double(s.getCell(8, i).getContents()) <= 1))// 验证津贴比例是否在0-1之间
							validateResult += i + 1 + ",";
					} else {
						validateResult += i + 1 + ",";// 如果津贴比例不是数字
						break;
					}
				} else {
					validateResult += i + 1 + ",";// 如果津贴比例为空
					break;
				}

				// 验证折标系数
				if (s.getCell(9, i).getContents() != null && !"".equals(s.getCell(9, i).getContents())) {
					if (importValidateDao.validateFigure(s.getCell(9, i).getContents()))// 验证折标系数是否为数字类型
					{
						if (!(new Double(s.getCell(9, i).getContents()) >= 0
								&& new Double(s.getCell(9, i).getContents()) <= 1))// 验证折标系数是否在0-1之间
							validateResult += i + 1 + ",";
					} else {
						validateResult += i + 1 + ",";// 如果折标系数不是数字
						break;
					}
				} else {
					validateResult += i + 1 + ",";// 如果折标系数为空
					break;
				}
			}

			// 验证Excel文档中保单号是否有重复
			if ("".equals(validateResult)) {
				for (int i = 1; i < row; i++) {
					String policyNo = s.getCell(2, i).getContents();

					for (int j = 1; j < row; j++) {
						if (i != j && policyNo.equals(s.getCell(2, j).getContents()))
							validateResult += i + 1 + ",";
					}
				}
			}

			// 如果验证数据正确则先显示导入数据
			if ("".equals(validateResult)) {
				validateResult = "";
				for (int i = 1; i < row; i++) {
					validateResult += s.getCell(0, i).getContents() + "," + s.getCell(1, i).getContents() + ","
							+ s.getCell(2, i).getContents() + "," + s.getCell(3, i).getContents() + ","
							+ s.getCell(4, i).getContents() + "," + s.getCell(5, i).getContents() + ","
							+ s.getCell(6, i).getContents() + "," + s.getCell(7, i).getContents() + ","
							+ s.getCell(8, i).getContents() + "," + s.getCell(9, i).getContents() + "@";
				}
				validateResult = validateResult.substring(0, validateResult.length() - 1);
				validateResult = validateResult.replace("null", "");

				validateResult += "Y";// 设置验证数据成功的标志

			} else {
				validateResult = validateResult.substring(0, validateResult.length() - 1);

				validateResult += "N";// 设置验证数据失败的标志
			}
		}

		return validateResult;

	}

	/**
	 * 团险业绩导入
	 */
	public void importTXData(String importData) {
		String imData[] = importData.split(",");

		for (int i = 0; i < imData.length; i++) {

			WagesPrint wprint = new WagesPrint();
			wprint.setChannel_id(DataConst.Channel_ID_Tx);

			Double allowance_rate = 0.0;// 津贴比例
			Double prem2std_factor = 0.0; // 折标系数

			String grade[] = imData[i].split("&");

			if (grade[0] != null && !"".equals(grade[0])) {
				// wprint.setSales_id(grade[0].trim());//业务人员代码
				// M by shen_zz for R303集团统一工号3期 on 20141222 begin
				wprint.setSales_id(SalesCodeUtils.getSalesID(grade[0].trim()));// 业务人员代码
				// M by shen_zz for R303集团统一工号3期 on 20141222 end
			}

			wprint.setBusiness_type("01");// 暂时默认业务类型为"01";

			if (grade[2] != null && !"".equals(grade[2])) {
				wprint.setPolicy_no(grade[2].trim());// 保单号
			}

			if (grade[3] != null && !"".equals(grade[3])) {
				wprint.setPrd_type(grade[3].trim());// 产品类别代码
			}

			if (grade[4] != null && !"".equals(grade[4])) {
				wprint.setPrd_code(grade[4].trim());// 产品代码(险种代码)
			}

			if (grade[5] != null && !"".equals(grade[5])) {
				wprint.setPrd_name(grade[5].trim());// 产品名称
			}

			if (grade[6] != null && !"".equals(grade[6])) {
				wprint.setIssue_date(UtilDate.defmtDate(grade[6].trim()));// 承包日期
				wprint.setStat_yearmonth(grade[6].substring(0, 4) + grade[6].substring(5, 7));
			}
			if (grade[7] != null && !"".equals(grade[7])) {
				wprint.setPremium(new Double(grade[7].trim()));// 规模保费
			}

			if (grade[8] != null && !"".equals(grade[8])) {
				allowance_rate = new Double(grade[8].trim());
				wprint.setAllowance_rate(new Double(grade[8].trim()));// 津贴比例
			}

			if (grade[9] != null && !"".equals(grade[9])) {
				prem2std_factor = new Double(grade[9].trim());
				wprint.setPrem2std_factor(new Double(grade[9].trim()));// 折标系数
			}
			if (grade[7] != null && !"".equals(grade[7])) {
				wprint.setAllowance(new Double((grade[7].trim())) * allowance_rate);// 业务津贴 ＝ 规模保费×津贴比例；
				wprint.setStand_premium(new Double((grade[7].trim())) * prem2std_factor);// 标准保费 ＝ 规模保费×折标系数。
			}

			salesInfoDao_hd.insertTXPolicyperdaybase(wprint);
		}
	}

	/**
	 * 查询团险业绩导入
	 */
	public String queryTXPolicyList(int limit, int start, WagesPrint wprint) {

		int count = salesInfoDao_hd.queryTXPolicyListCount(wprint);

		List<WagesPrint> list = salesInfoDao_hd.queryTXPolicyList(limit, start, wprint);

		String json = "{totalCount:" + count + ",root:[";

		if (list != null && list.size() > 0) {
			for (WagesPrint print : list) {
				String issue_date = "";
				String prd_type = "";// 产品类别
				if (print.getIssue_date() != null && !"".equals(print.getIssue_date())) {
					issue_date = UtilDate.fmtDate(print.getIssue_date());
				}
				if (print.getPrd_type() != null && !"".equals(print.getPrd_type()))
					prd_type = codecodeDao.queryCodeName(print.getPrd_type(), DataConst.Channel_ID_Tx,
							CodeTypeConst.CODE_TYPE_GROUP_PRD_TYPE);
				// A by shen_zz for R309集团工号四期 on20141217 begin
				String sales_id = SalesCodeUtils.getSalesCode(print.getSales_id());
				if (sales_id == null || "N".equals(sales_id)) {
					sales_id = "";
				}
				print.setSales_id(sales_id);
				// A by shen_zz for R309集团工号四期 on20141217 end

				json += "{id:'" + print.getSales_id() + "',branch_id:'" + print.getBranch_id() + "',branch_name:'"
						+ print.getBranch_name() + "',team_id:'" + print.getTeam_id() + "',team_name:'"
						+ print.getTeam_name() + "',sales_id:'" + print.getSales_id() + "',sales_name:'"
						+ print.getSales_name() + "',policy_no:'" + print.getPolicy_no() + "',prd_type:'" + prd_type
						+ "',prd_code:'" + print.getPrd_code() + "',prd_name:'" + print.getPrd_name() + "',stat_month:'"
						+ print.getStat_month() + "',issue_date:'" + issue_date + "',allowance_rate:'"
						+ print.getAllowance_rate() + "',prem2std_factor:'" + print.getPrem2std_factor() + "',premium:'"
						+ print.getPremium() + "',stand_premium:'" + print.getStand_premium() + "',allowance:'"
						+ print.getAllowance() + "'},";
			}

			json = json.substring(0, json.length() - 1);
			json = json.replace("null", "");
		}

		json += "]}";

		return json;
	}

	/**
	 * 团险业绩查询导出
	 */
	@SuppressWarnings("unchecked")
	public List<String> exportTXPolicyList(WagesPrint wprint) {
		String columns = "branch_id,branch_name,team_id,team_name,sales_id,sales_name,policy_no,prd_type,prd_code,prd_name,issue_date,premium,allowance_rate,"
				+ "prem2std_factor,allowance,stand_premium,stat_month";

		List<String> sheet = new ArrayList();
		sheet.add(columns);

		int start = 0;
		int limit = salesInfoDao_hd.queryTXPolicyListCount(wprint);

		List<WagesPrint> list = salesInfoDao_hd.queryTXPolicyList(limit, start, wprint);

		if (list != null && list.size() > 0) {
			for (WagesPrint print : list) {
				String issue_date = "";
				String prd_type = "";// 产品类别
				if (print.getIssue_date() != null && !"".equals(print.getIssue_date())) {
					issue_date = UtilDate.fmtDate(print.getIssue_date());
				}
				if (print.getPrd_type() != null && !"".equals(print.getPrd_type()))
					prd_type = codecodeDao.queryCodeName(print.getPrd_type(), DataConst.Channel_ID_Tx,
							CodeTypeConst.CODE_TYPE_GROUP_PRD_TYPE);
				// A by shen_zz for R309集团工号四期 on20141217 begin
				String sales_id = SalesCodeUtils.getSalesCode(print.getSales_id());
				if (sales_id == null || "N".equals(sales_id)) {
					sales_id = "";
				}
				print.setSales_id(sales_id);
				// A by shen_zz for R309集团工号四期 on20141217 end
				String data = print.getBranch_id() + "," + print.getBranch_name() + "," + print.getTeam_id() + ","
						+ print.getTeam_name() + "," + print.getSales_id() + "," + print.getSales_name() + ","
						+ print.getPolicy_no() + "," + prd_type + "," + print.getPrd_code() + "," + print.getPrd_name()
						+ "," + issue_date + "," + print.getPremium() + "," + print.getAllowance_rate() + ","
						+ print.getPrem2std_factor() + "," + print.getAllowance() + "," + print.getStand_premium() + ","
						+ print.getStat_month();

				data = data.replace("null", "");
				sheet.add(data);
			}
		}

		return sheet;
	}

	/**
	 * 通过基本法查询职级
	 *
	 * @param
	 * @return
	 */
	public List<RankdefInfo> queryRank(Map<String, String> map) {

		return salesInfoDao_hd.queryRank(map);
	}

	/**
	 * 得到导出Excel文件的列标题
	 */
	public String[] getLables(SalesInfo salesInfo) {
		String lables[] = new String[100];
		int i = 10;
		lables[0] = "互动经理代码";
		if (StringUtils.isNotEmpty(salesInfo.getOld_sales_id())) {
			i = 11;
			lables[1] = salesInfo.getOld_sales_id();
			lables[2] = "互动经理姓名";
			lables[3] = "二级机构代码";
			lables[4] = "二级机构名称";
			lables[5] = "三级机构代码";
			lables[6] = "三级机构名称";
			lables[7] = "四级机构代码";
			lables[8] = "四级机构名称";
			lables[9] = "职场代码";
			lables[10] = "职场名称";
		} else {

			lables[1] = "互动经理姓名";
			lables[2] = "二级机构代码";
			lables[3] = "二级机构名称";
			lables[4] = "三级机构代码";
			lables[5] = "三级机构名称";
			lables[6] = "四级机构代码";
			lables[7] = "四级机构名称";
			lables[8] = "职场代码";
			lables[9] = "职场名称";

		}
		if (StringUtils.isNotEmpty(salesInfo.getId_type())) {
			lables[i] = salesInfo.getId_type();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getId_no())) {
			lables[i] = salesInfo.getId_no();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getSex())) {
			lables[i] = salesInfo.getSex();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getBirthday())) {
			lables[i] = salesInfo.getBirthday();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getNation())) {
			lables[i] = salesInfo.getNation();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getEducation())) {
			lables[i] = salesInfo.getEducation();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getMajor())) {
			lables[i] = salesInfo.getMajor();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getDomicile())) {
			lables[i] = salesInfo.getDomicile();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getHome_address())) {
			lables[i] = salesInfo.getHome_address();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getHome_zipcode())) {
			lables[i] = salesInfo.getHome_zipcode();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getMobile())) {
			lables[i] = salesInfo.getMobile();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getFixed_line())) {
			lables[i] = salesInfo.getFixed_line();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getPhs())) {
			lables[i] = salesInfo.getPhs();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getHealth_stat())) {
			lables[i] = salesInfo.getHealth_stat();
			i++;
		}
		// A by qinliyang for L613 begin
		if (StringUtils.isNotEmpty(salesInfo.getPlan_name())) {
			lables[i] = salesInfo.getPlan_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getPlan_remark())) {
			lables[i] = salesInfo.getPlan_remark();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getPlan_time())) {
			lables[i] = salesInfo.getPlan_time();
			i++;
		}
		// A by qinliyang for L613 end
		if (StringUtils.isNotEmpty(salesInfo.getRemark())) {
			lables[i] = salesInfo.getRemark();
			i++;
		}
		// A by qinliyang for L613 begin
		if (StringUtils.isNotEmpty(salesInfo.getIs_rural_networks())) {
			lables[i] = salesInfo.getIs_rural_networks();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getRural_networks_id())) {
			lables[i] = salesInfo.getRural_networks_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_appoint_commissioner())) {
			lables[i] = salesInfo.getIs_appoint_commissioner();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getProperty_networks())) {
			lables[i] = salesInfo.getProperty_networks();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getTeam_id_qu())) {
			lables[i] = salesInfo.getTeam_id_qu();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getTeam_name_qu())) {
			lables[i] = salesInfo.getTeam_name_qu();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getTeam_id_bu())) {
			lables[i] = salesInfo.getTeam_id_bu();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getTeam_name_bu())) {
			lables[i] = salesInfo.getTeam_name_bu();
			i++;
		}
		// A by qinliyang for L613 end
		if (StringUtils.isNotEmpty(salesInfo.getTeam_id2())) {
			lables[i] = salesInfo.getTeam_id2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getTeam_name2())) {
			lables[i] = salesInfo.getTeam_name2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getLeader_id())) {
			lables[i] = salesInfo.getLeader_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getOld_leader_id())) {
			lables[i] = salesInfo.getOld_leader_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getLeader_name())) {
			lables[i] = salesInfo.getLeader_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getRecommend_id())) {
			lables[i] = salesInfo.getRecommend_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getOld_recommend_id())) {
			lables[i] = salesInfo.getOld_recommend_id();
			i++;
		}
		// a by wang_gy for L790
		if (StringUtils.isNotEmpty(salesInfo.getIsexcperson())) {
			lables[i] = salesInfo.getIsexcperson();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIsdiamem())) {
			lables[i] = salesInfo.getIsdiamem();
			i++;
		}
		// add by li_br L813 begin
		if (StringUtils.isNotEmpty(salesInfo.getAccount_id())) {
			lables[i] = salesInfo.getAccount_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAccount_name())) {
			lables[i] = salesInfo.getAccount_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getBank_id())) {
			lables[i] = salesInfo.getBank_id();
			i++;
		}
		// add by li_br L813 end
		if (StringUtils.isNotEmpty(salesInfo.getRecommend_name())) {
			lables[i] = salesInfo.getRecommend_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getEmploy_kind())) {
			lables[i] = salesInfo.getEmploy_kind();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getBase_version_id())) {
			lables[i] = salesInfo.getBase_version_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getRank2())) {
			lables[i] = salesInfo.getRank2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getStat2())) {
			lables[i] = salesInfo.getStat2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getProbation_date2())) {
			lables[i] = salesInfo.getProbation_date2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssess_start_date())) {
			lables[i] = salesInfo.getAssess_start_date();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getLeft_date2())) {
			lables[i] = salesInfo.getLeft_date2();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_resigned())) {
			lables[i] = salesInfo.getIs_resigned();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_green_passport())) {
			lables[i] = salesInfo.getIs_green_passport();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_full_time())) {
			lables[i] = salesInfo.getIs_full_time();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_violatediscipline_record())) {
			lables[i] = salesInfo.getIs_violatediscipline_record();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_same_vocation())) {
			lables[i] = salesInfo.getIs_same_vocation();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getBiz_years1())) {
			lables[i] = salesInfo.getBiz_years1();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getOld_job())) {
			lables[i] = salesInfo.getOld_job();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getOld_company())) {
			lables[i] = salesInfo.getOld_company();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getIs_qualicert())) {
			lables[i] = salesInfo.getIs_qualicert();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getWork_date())) {
			lables[i] = salesInfo.getWork_date();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getQualify_id())) {
			lables[i] = salesInfo.getQualify_id();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getDevelop_id())) {
			lables[i] = salesInfo.getDevelop_id();
			i++;
		}
		// A by j_yc for 运维bug5923 begin
		if (StringUtils.isNotEmpty(salesInfo.getGive_date())) {
			lables[i] = salesInfo.getGive_date();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getValid_date())) {
			lables[i] = salesInfo.getValid_date();
			i++;
		}
		// A by j_yc for 运维bug5923 end
		if (StringUtils.isNotEmpty(salesInfo.getIs_sellqualify())) {
			lables[i] = salesInfo.getIs_sellqualify();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_name())) {
			lables[i] = salesInfo.getAssurer_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_id_type())) {
			lables[i] = salesInfo.getAssurer_id_type();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_id_no())) {
			lables[i] = salesInfo.getAssurer_id_no();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_home_phone())) {
			lables[i] = salesInfo.getAssurer_home_phone();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_unit_name())) {
			lables[i] = salesInfo.getAssurer_unit_name();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getAssurer_unit_address())) {
			lables[i] = salesInfo.getAssurer_unit_address();
			i++;
		}
		// A by j_yc for L243 begin
		if (StringUtils.isNotEmpty(salesInfo.getSalesQualificationA())) {
			lables[i] = salesInfo.getSalesQualificationA();
			i++;
		}
		if (StringUtils.isNotEmpty(salesInfo.getSalesQualificationB())) {
			lables[i] = salesInfo.getSalesQualificationB();
			i++;
		}
		// A by j_yc for L243 end
		String temp[] = new String[i];
		for (int j = 0; j < i; j++) {
			if (StringUtils.isNotEmpty(lables[j])) {
				temp[j] = lables[j];
			}
		}
		return temp;
	}

	/**
	 * 人员信息导出
	 *
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map exprotDown(Map map) {
		try {
			String tempDirectory = (String) map.get("tempDirectory");
			String fileName = (String) map.get("fileName");

			System.out.println("页面 exportDateExcel.jsp 开始时间 :" + System.currentTimeMillis() / 1001);
			String newJarFile = tempDirectory + "/" + fileName + "_" + getDateTime() + "_" + ".rar";

			File file = new File(newJarFile);
			if (!file.exists())
				file.createNewFile();

			map.put("newJarFile", newJarFile);

			String[] deleteStrNames = writeSalesExce(map);

			map.put("deleteStrNames", deleteStrNames);

			System.out.println("页面 exportDateExcel.jsp 结束时间 :" + System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public String[] writeSalesExce(Map<String, Object> map) throws RowsExceededException, WriteException {
		ExportDataServiceImpl exportDataServiceImpl = (ExportDataServiceImpl) map.get("exportDataServiceImpl");

		SalesInfo salesInfo = (SalesInfo) map.get("salesInfo");
		String[] lables = (String[]) map.get("lables"); // 表头
		String tempFilePath = (String) map.get("tempDirectory"); // 写出的文件路径

		String newJarFile = (String) map.get("newJarFile");
		List<String> listNewFiles = new ArrayList<String>();

		int row = 0;

		FileOutputStream fileOut;

		int MaxRecord = Integer.parseInt((String) map.get("MaxRecord"));

		List<String> gridData = new ArrayList<String>();
		String filePath = "";
		List<String> fileName = new ArrayList<String>();
		int pagenum = 0;

		int count = 0; // 需要导出的总记录数

		int OnceRecord = Integer.parseInt((String) map.get("OnceRecord")); // 每次查询的最大记录数

		int number = 0; // 如大于MaxRecord 则 分number次查询
		int end = 0; // 查询结束位置
		int start = 0; // 查询开始位置
		count = salesInfoDao_hd.exportSalesInfoCount(salesInfo); // 获取需要导出记录的总记录数

		try {

			if (count < OnceRecord) {

				fileOut = new FileOutputStream(newJarFile);
				BufferedOutputStream bos = new BufferedOutputStream(fileOut);
				end = count;

				salesInfo.setEnd(end);
				salesInfo.setStart(start);
				gridData = exportSalesInfo(salesInfo); // 如果小于MaxRecord 则直接查询

				pagenum = listNewFiles.size() + 1; // Excel标题页数

				filePath = exportDataServiceImpl.CreatFile(newJarFile, lables, tempFilePath, pagenum); // 创建带有表头的Excel文件
																										// 返回文件路径文件名

				File file = new File(filePath);

				/****************************************/
				Map<String, Object> mapWrite = new HashMap<String, Object>();
				mapWrite.put("tempFilePath", tempFilePath);
				mapWrite.put("MaxRecord", MaxRecord);
				mapWrite.put("start", start);
				mapWrite.put("filePath", filePath);
				mapWrite.put("pagenum", pagenum);

				listNewFiles.add(filePath);

				fileName = exportDataServiceImpl.writeSalesExc(bos, file, gridData, lables, mapWrite); // 写出记录
				if (fileName.size() > 1) {
					listNewFiles.addAll(fileName.subList(1, fileName.size())); // 则将文件名添加到集合里
				}

				JarUtils.antzip((String[]) listNewFiles.toArray(new String[] {}), bos); // 压缩文件
				bos.close();
			} else {

				/*************** 记录过多,需要分次查询时 ******************************/

				fileOut = new FileOutputStream(newJarFile);
				BufferedOutputStream bos = new BufferedOutputStream(fileOut);
				number = count / OnceRecord; // 查询的次数
				end = count % OnceRecord; // 取得不足OnceRecord 数量的尾数..先行导出

				for (int i = 0; i <= number; i++) {
					salesInfo.setStart(start);
					salesInfo.setEnd(end);

					gridData.clear();
					gridData = exportSalesInfo(salesInfo);

					if (start > MaxRecord) {
						row = start % MaxRecord;
					} else { // 获取记录应写出的行数
						row = start;
					}

					if (row == 0) { // 如果第一次创建记录,或者正好写满一个文件,则创建一个新的Excel
						pagenum = listNewFiles.size() + 1;
						filePath = exportDataServiceImpl.CreatFile(newJarFile, lables, tempFilePath, pagenum);
						listNewFiles.add(filePath);
					}
					File file = new File(filePath);

					Map<String, Object> mapWrite = new HashMap<String, Object>();
					mapWrite.put("tempFilePath", tempFilePath);
					mapWrite.put("MaxRecord", MaxRecord);
					mapWrite.put("start", start);
					mapWrite.put("filePath", filePath);
					mapWrite.put("pagenum", pagenum);

					fileName = exportDataServiceImpl.writeSalesExc(bos, file, gridData, lables, mapWrite);

					listNewFiles.addAll(fileName.subList(1, fileName.size())); // 将新文件名添加到压缩的集合里
					filePath = fileName.get(fileName.size() - 1);
					if (fileName.size() != 1) {
						pagenum += fileName.size() - 1;
					}

					start = end;
					end += OnceRecord;

				}
				JarUtils.antzip((String[]) listNewFiles.toArray(new String[] {}), bos);
				bos.close();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		listNewFiles.add(newJarFile);
		return ((String[]) listNewFiles.toArray(new String[] {}));
	}

	private List<String> exportSalesInfo(SalesInfo salesInfo) {
		List<String> sales = new ArrayList<String>();

		List<SalesInfo> list = salesInfoDao_hd.exportSalesInfo(salesInfo);

		// String sysdate = UtilDate.fmtDate(publicFunctionDao.GetSysDate());//得到当前系统时间

		if (list != null && !"".equals(list)) {
			for (SalesInfo sl : list) {
				sales.add(sl.getSales_id());
				if (StringUtils.isNotEmpty(salesInfo.getOld_sales_id())) {
					sales.add(sl.getOld_sales_id());
				}
				sales.add(sl.getSales_name());
				/*
				 * sales.add(sl.getBranch_id()); sales.add(sl.getBranch_name());
				 */
				sales.add(sl.getBranch_id2());
				sales.add(sl.getBranch_name2());
				sales.add(sl.getBranch_id3());
				sales.add(sl.getBranch_name3());
				sales.add(sl.getBranch_id4());
				sales.add(sl.getBranch_name4());
				sales.add(sl.getWorkspace_id());
				sales.add(sl.getWorkspace_name());

				if (StringUtils.isNotEmpty(salesInfo.getId_type())) {
					sales.add(sl.getId_type());
				}
				if (StringUtils.isNotEmpty(salesInfo.getId_no())) {
					sales.add(sl.getId_no());
				}
				if (StringUtils.isNotEmpty(salesInfo.getSex())) {
					sales.add(sl.getSex());
				}
				if (StringUtils.isNotEmpty(salesInfo.getBirthday())) {
					sales.add(sl.getBirthday());
				}
				if (StringUtils.isNotEmpty(salesInfo.getNation())) {
					sales.add(sl.getNation());
				}
				if (StringUtils.isNotEmpty(salesInfo.getEducation())) {
					sales.add(sl.getEducation());
				}
				if (StringUtils.isNotEmpty(salesInfo.getMajor())) {
					sales.add(sl.getMajor());
				}
				if (StringUtils.isNotEmpty(salesInfo.getDomicile())) {
					sales.add(sl.getDomicile());
				}
				if (StringUtils.isNotEmpty(salesInfo.getHome_address())) {
					sales.add(sl.getHome_address());
				}
				if (StringUtils.isNotEmpty(salesInfo.getHome_zipcode())) {
					sales.add(sl.getHome_zipcode());
				}
				if (StringUtils.isNotEmpty(salesInfo.getMobile())) {
					sales.add(sl.getMobile());
				}
				if (StringUtils.isNotEmpty(salesInfo.getFixed_line())) {
					sales.add(sl.getFixed_line());
				}
				if (StringUtils.isNotEmpty(salesInfo.getPhs())) {
					sales.add(sl.getPhs());
				}
				if (StringUtils.isNotEmpty(salesInfo.getHealth_stat())) {
					sales.add(sl.getHealth_stat());
				}
				// A by qinliyang for L613 begin
				if (StringUtils.isNotEmpty(salesInfo.getPlan_name())) {
					sales.add(sl.getPlan_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getPlan_remark())) {
					sales.add(sl.getPlan_remark());
				}
				if (StringUtils.isNotEmpty(salesInfo.getPlan_time())) {
					sales.add(sl.getPlan_time());
				}
				// A by qinliyang for L613 end
				if (StringUtils.isNotEmpty(salesInfo.getRemark())) {
					sales.add(sl.getRemark());
				}
				// A by qinliyang for L613 begin
				if (StringUtils.isNotEmpty(salesInfo.getIs_rural_networks())) {
					sales.add(sl.getIs_rural_networks());
				}
				if (StringUtils.isNotEmpty(salesInfo.getRural_networks_id())) {
					sales.add(sl.getRural_networks_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_appoint_commissioner())) {
					sales.add(sl.getIs_appoint_commissioner());
				}
				if (StringUtils.isNotEmpty(salesInfo.getProperty_networks())) {
					sales.add(sl.getProperty_networks());
				}
				if (StringUtils.isNotEmpty(salesInfo.getTeam_id_qu())) {
					sales.add(sl.getTeam_id_qu());
				}
				if (StringUtils.isNotEmpty(salesInfo.getTeam_name_qu())) {
					sales.add(sl.getTeam_name_qu());
				}
				if (StringUtils.isNotEmpty(salesInfo.getTeam_id_bu())) {
					sales.add(sl.getTeam_id_bu());
				}
				if (StringUtils.isNotEmpty(salesInfo.getTeam_name_bu())) {
					sales.add(sl.getTeam_name_bu());
				}
				// A by qinliyang for L613 end
				if (StringUtils.isNotEmpty(salesInfo.getTeam_id2())) {
					sales.add(sl.getTeam_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getTeam_name2())) {
					sales.add(sl.getTeam_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getLeader_id())) {
					sales.add(sl.getLeader_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getOld_leader_id())) {
					sales.add(sl.getOld_leader_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getLeader_name())) {
					sales.add(sl.getLeader_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getRecommend_id())) {
					sales.add(sl.getRecommend_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getOld_recommend_id())) {
					sales.add(sl.getOld_recommend_id());
				}
				// a by wang_gy for L790
				if (StringUtils.isNotEmpty(salesInfo.getIsexcperson())) {
					sales.add(sl.getIsexcperson());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIsdiamem())) {
					sales.add(sl.getIsdiamem());// A by wang_gy for L790
				}
				// add by li_br L813 begin
				if (StringUtils.isNotEmpty(salesInfo.getAccount_id())) {
					sales.add(sl.getAccount_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAccount_name())) {
					sales.add(sl.getAccount_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getBank_id())) {
					sales.add(sl.getBank_id());
				}
				// add by li_br L813 end
				if (StringUtils.isNotEmpty(salesInfo.getRecommend_name())) {
					sales.add(sl.getRecommend_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getEmploy_kind())) {
					sales.add(sl.getEmploy_kind());
				}
				if (StringUtils.isNotEmpty(salesInfo.getBase_version_id())) {
					sales.add(sl.getBase_version_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getRank2())) {
					sales.add(sl.getRank());
				}
				if (StringUtils.isNotEmpty(salesInfo.getStat2())) {
					sales.add(sl.getStat());
				}
				if (StringUtils.isNotEmpty(salesInfo.getProbation_date2())) {
					sales.add(sl.getProbation_date());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssess_start_date())) {
					sales.add(sl.getAssess_start_date());
				}
				if (StringUtils.isNotEmpty(salesInfo.getLeft_date2())) {
					sales.add(sl.getLeft_date());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_resigned())) {
					sales.add(sl.getIs_resigned());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_green_passport())) {
					sales.add(sl.getIs_green_passport());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_full_time())) {
					sales.add(sl.getIs_full_time());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_violatediscipline_record())) {
					sales.add(sl.getIs_violatediscipline_record());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_same_vocation())) {
					sales.add(sl.getIs_same_vocation());
				}
				if (StringUtils.isNotEmpty(salesInfo.getBiz_years1())) {
					sales.add(sl.getBiz_years() + "");
				}
				if (StringUtils.isNotEmpty(salesInfo.getOld_job())) {
					sales.add(sl.getOld_job());
				}
				if (StringUtils.isNotEmpty(salesInfo.getOld_company())) {
					sales.add(sl.getOld_company());
				}
				if (StringUtils.isNotEmpty(salesInfo.getIs_qualicert())) {
					sales.add(sl.getIs_qualicert());
				}
				if (StringUtils.isNotEmpty(salesInfo.getWork_date())) {
					sales.add(sl.getWork_date());
				}
				if (StringUtils.isNotEmpty(salesInfo.getQualify_id())) {
					sales.add(sl.getQualify_id());
				}
				if (StringUtils.isNotEmpty(salesInfo.getDevelop_id())) {
					sales.add(sl.getDevelop_id());
				}
				// A j_yc for bug5932 begin
				if (StringUtils.isNotEmpty(salesInfo.getGive_date())) {
					sales.add(sl.getGive_date());
				}
				if (StringUtils.isNotEmpty(salesInfo.getValid_date())) {
					sales.add(sl.getValid_date());
				}
				// A j_yc for bug5932 end
				if (StringUtils.isNotEmpty(salesInfo.getIs_sellqualify())) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("sales_id", sl.getSales_id());
					map.put("channel_id", salesInfo.getChannel_id());
					sales.add(salesInfoDao_hd.queryIsSellQualify(map));
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_name())) {
					sales.add(sl.getAssurer_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_id_type())) {
					sales.add(sl.getAssurer_id_type());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_id_no())) {
					sales.add(sl.getAssurer_id_no());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_home_phone())) {
					sales.add(sl.getAssurer_home_phone());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_unit_name())) {
					sales.add(sl.getAssurer_unit_name());
				}
				if (StringUtils.isNotEmpty(salesInfo.getAssurer_unit_address())) {
					sales.add(sl.getAssurer_unit_address());
				}
				// A by j_yc for L243 begin
				if (StringUtils.isNotEmpty(salesInfo.getSalesQualificationA())) {
					sales.add(sl.getSalesQualificationA());
				}
				if (StringUtils.isNotEmpty(salesInfo.getSalesQualificationB())) {
					sales.add(sl.getSalesQualificationB());
				}
				// A by j_yc for L243 begin
			}
		}
		return sales;
	}

	// 10位转7位方法
	public String getSalesID(String byId) {
		if (byId != null && !"".equals(byId) && byId.trim().length() == 10) {
			byId = publicMethodManagerImpl.getSalesID(byId);
		}
		return byId;
	}

	/**
	 * 人员暂存录入，执业证号检验（是否存在）
	 */
	public boolean checkDevelopId(String develop_id) {
		// TODO Auto-generated method stub
		boolean results = false;
		int count = salesInfoDao_hd.getDevelopId(develop_id);
		if (count > 0) {
			results = true;
		}
		return results;
	}

	/**
	 * @Description L394导出工资条
	 * @author j_yc
	 * @date 2017年4月21日
	 * @param wprint
	 * @return
	 */
	public List<String> exportExcel(WagesPrint wprint) {
		// M by j_yc for L547 begin
		String columns = "";
		String col = "";
		if ("4110".equals(wprint.getBase_version_id())) {
			columns = "rownum,stat_month,sales_code,sales_name,branch_id3,branch_name3,branch_id4,branch_name4,team_id,team_name,rank_name,snywsr,jlyj,xnywsr,cxljj,hdfwjt,tjjt,ydlcjt,ydlcjtbf,ydgwjt,ydgwjtbf,bgljt,otherT,jstz,beforeS,taxes,shtz,afterS";
			col = "序号,计佣年月,互动经理代码,互动经理姓名,三级机构代码,三级机构名称,四级机构代码,四级机构名称,营业组代码,营业组名称,当前职级,首年业务收入,激励佣金,续年业务收入,持续率奖金,互动服务津贴,推荐津贴,月度理财津贴,月度理财津贴补发,月度岗位津贴,月度岗位津贴补发,部管理津贴,其他税前调整,计税调整,税前合计,税费,税后调整,税后应发金额";
		} else if ("4120".equals(wprint.getBase_version_id())) {
			columns = "rownum,stat_month,sales_code,sales_name,branch_id3,branch_name3,branch_id4,branch_name4,team_id,team_name,rank_name,cndyj,jlyj2,xndfwjt,cxljj2,xrxljt,zyjt,ktjt,ydjxjj,jdjxjj,zgljt,bgljt2,zycjt,bycjt,fwjt,newtalentjt,newtalentyf,zshyjj,zshyjjbf,otherT,jstz,beforeS,taxes,shtz,afterS";
			col = "序号,计佣年月,互动经理代码,互动经理姓名,三级机构代码,三级机构名称,四级机构代码,四级机构名称,营业组代码,营业组名称,当前职级,初年度佣金,激励佣金,续年度服务津贴,持续率奖金,新人训练津贴,增员津贴,开拓津贴,月度绩效奖金,季度绩效奖金,组管理津贴,部管理津贴,组育成津贴,部育成津贴,职务津贴,新人优才津贴本月计提金额,新人优才津贴本月应发金额,钻石会员奖金,钻石会员奖金补发,其他税前调整,计税调整,税前合计,税费,税后调整,税后应发金额";
		}
		List<String> sheet = new ArrayList<String>();
		sheet.add(columns);

		List<PrintSalaryBill> list = salesInfoDao_hd.exportExcel(wprint);

		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				String data = "";
				if ("4110".equals(wprint.getBase_version_id())) {
					data = list.get(i).getRownum() + "," + list.get(i).getStat_month() + ","
							+ list.get(i).getSales_code() + "," + list.get(i).getSales_name() + ","
							+ list.get(i).getBranch_id3() + "," + list.get(i).getBranch_name3() + ","
							+ list.get(i).getBranch_id4() + "," + list.get(i).getBranch_name4() + ","
							+ list.get(i).getTeam_id() + "," + list.get(i).getTeam_name() + ","
							+ list.get(i).getRank_name() + "," + list.get(i).getSnywsr() + "," + list.get(i).getJlyj()
							+ "," + list.get(i).getXnywsr() + "," + list.get(i).getCxljj() + ","
							+ list.get(i).getHdfwjt() + "," + list.get(i).getTjjt() + "," + list.get(i).getYdlcjt()
							+ "," + list.get(i).getYdlcjtbf() + "," + list.get(i).getYdgwjt() + ","
							+ list.get(i).getYdgwjtbf() + "," + list.get(i).getBgljt() + "," + list.get(i).getOtherT()
							+ "," + list.get(i).getJstz() + "," + list.get(i).getBeforeS() + ","
							+ list.get(i).getTaxes() + "," + list.get(i).getShtz() + "," + list.get(i).getAfterS();
				} else if ("4120".equals(wprint.getBase_version_id())) {
					data = list.get(i).getRownum() + "," + list.get(i).getStat_month() + ","
							+ list.get(i).getSales_code() + "," + list.get(i).getSales_name() + ","
							+ list.get(i).getBranch_id3() + "," + list.get(i).getBranch_name3() + ","
							+ list.get(i).getBranch_id4() + "," + list.get(i).getBranch_name4() + ","
							+ list.get(i).getTeam_id() + "," + list.get(i).getTeam_name() + ","
							+ list.get(i).getRank_name() + "," + list.get(i).getCndyj() + "," + list.get(i).getJlyj2()
							+ "," + list.get(i).getXndfwjt() + "," + list.get(i).getCxljj2() + ","
							+ list.get(i).getXrxljt() + "," + list.get(i).getZyjt() + "," + list.get(i).getKtjt() + ","
							+ list.get(i).getYdjxjj() + "," + list.get(i).getJdjxjj() + "," + list.get(i).getZgljt()
							+ "," + list.get(i).getBgljt2() + "," + list.get(i).getZycjt() + ","
							+ list.get(i).getBycjt() + "," + list.get(i).getFwjt() + "," + list.get(i).getNewtalentjt()// A
																														// by
																														// wang_gy
																														// for
																														// L790
							+ "," + list.get(i).getNewtalentyf()// A by wang_gy for L790
							+ "," + list.get(i).getZshyjj()// A by wang_gy for L790
							+ "," + list.get(i).getZshyjjbf()// A by wang_gy for L790
							+ "," + list.get(i).getOtherT() + "," + list.get(i).getJstz() + ","
							+ list.get(i).getBeforeS() + "," + list.get(i).getTaxes() + "," + list.get(i).getShtz()
							+ "," + list.get(i).getAfterS();
				}

				data = data.replace("null", "");
				sheet.add(data);
				if (i < list.size() - 1) {
					sheet.add(col);
				}
			}
		}

		return sheet;
	}

	// L402 判断 该推荐时是否在操作用户权限内
	@Override
	public String getBranch(String branch_id, String branchId) {

		return salesInfoDao_hd.getBranch(branch_id, branchId);
	}

	// 查询职级的层级
	@Override
	public String getRankPost(String rank, String base_version_id) {

		return salesInfoDao_hd.getRankPost(rank, base_version_id);
	}

	// L554
	@Override
	public String getTeam1(String recommend_id, String team_lvl) {

		return salesInfoDao_hd.getTeam1(recommend_id, team_lvl);
	}

	@Override
	public String getTeam2(String team_id, String team_lvl) {

		return salesInfoDao_hd.getTeam2(team_id, team_lvl);
	}

	@Override
	public String getworkspace_id1(String recommend_id) {
		return salesInfoDao_hd.getworkspace_id1(recommend_id);
	}

	@Override
	public String getworkspace_id2(String team_id) {
		return salesInfoDao_hd.getworkspace_id2(team_id);
	}

	/**
	 * 人员录入，农网代码校验 613 shiyawei
	 */
	public Map<String, String> checkRuralNetworksId(String rural_networks_id, String branch_id_str, String branch_id) {
		// TODO Auto-generated method stub
		String msgStr = "";
		String remark = "";

		SalesInfo info = salesInfoDao_hd.quyRuralNetworksInfo(rural_networks_id, branch_id);
		if (info == null) {
			remark = "failure";
			msgStr = "农网代码不存在，请录入正确的农网代码！";
		} else {
			int i = branch_id_str.indexOf(info.getBranch_id());
			if (i < 0) {

				remark = "failure";
				msgStr = "农网代码不存在，请录入正确的农网代码！";
			} else {
				remark = "success";
			}
		}
		Map<String, String> map = new HashMap<String, String>();

		map.put("remark", remark);
		map.put("msgStr", msgStr);
		return map;
	}

	/* add by lwj reason:手机号重复校验 2018年12月28日16:29:23 */
	/*
	 * public int checkMobile(String mobile) { int count =
	 * salesInfoDao_hd.checkMobile(mobile); return count; }
	 */
	// add by ni_f for L685查询手机号码是否存在大于等于两次
	public int getMobile(SalesInfo salesInfo) {
		int count = 0;
		count = salesInfoDao_hd.getMobile(salesInfo);
		return count;
	}
	public int getMobilePhone(SalesInfo salesInfo) {
		int count = 0;
		count = salesInfoDao_hd.getMobilePhone(salesInfo);
		return count;
	}

	//获取机构名成
	public String  getBranchName(String sales_id){
		
		SalesInfo salesInfo=new SalesInfo();
		SalesInfo salesInfo1=new SalesInfo();
		//三级名称
		salesInfo=salesInfoDao_hd.getBranchName3(sales_id);
		//二级机构
		salesInfo1=salesInfoDao_hd.getBranchName2(salesInfo.getBranch_id());
		
	    String branchName=salesInfo1.getBranch_name2()+salesInfo.getBranch_name3();
		return branchName;
	}
	
	public String getChannelId(String  mobile){
		String channelId=salesInfoDao_hd.getChannelId(mobile);
		return channelId;
		
	}
	// a by ni_f for L685 验证手机号已存在营销员工号 20190320
	public String getScode(SalesInfo salesInfo) {
		String list = salesInfoDao_hd.getScode(salesInfo);
		return list;
	}

	// a by ni_f for L685 验证座机号与当前营销员是否匹配 20190320
	public int getScodef(SalesInfo salesInfo) {
		int list = salesInfoDao_hd.getScodef(salesInfo);
		return list;
	}

	/**
	 * a by ni_f for L685 验证座机号使用次数
	 *
	 * @param salesInfo
	 * @return
	 */
	@Override
	public int getFixedline(SalesInfo salesInfo) {
		int count = 0;
		count = salesInfoDao_hd.getFixedline(salesInfo);
		return count;
	}

	/**
	 * a by ni_f for L685 验证手机号更改时间
	 *
	 * @param salesInfo
	 * @return
	 */
	@Override
	public String getMobileDate(SalesInfo salesInfo) {
		String count = "";
		count = salesInfoDao_hd.getMobileDate(salesInfo);
		return count;
	}

	/**
	 * a by ni_f for L685 获取上次手机号录入时间
	 *
	 * @param salesInfo
	 * @return
	 */
	@Override
	public Date MobileDate(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.MobileDate(salesInfo);
	}

	/**
	 * a by ni_f for L685 验证座机号更改时间
	 *
	 * @param salesInfo
	 * @return
	 */
	@Override
	public String getFixedlineDate(SalesInfo salesInfo) {
		String count = "";
		count = salesInfoDao_hd.getFixedlineDate(salesInfo);
		return count;
	}

	/**
	 * a by ni_f for L685 获取上次座机号录入时间
	 *
	 * @param salesInfo
	 * @return
	 */
	@Override
	public Date FixedlineDate(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.FixedlineDate(salesInfo);
	}

	/**
	 * a by ni_f L824 根据人员代码获取人员信息
	 */
	@Override
	public SalesInfo getSalesInfo(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getSalesInfo(salesInfo);
	}

	/**
	 * a by ni_f L824 查询省市
	 */
	@Override
	public String getnative(String sales_native_3lvl) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getnative(sales_native_3lvl);
	}

	/**
	 * a by ni_f L824 查询职级
	 */
	@Override
	public String getRankName(String version, String channel_ID_Hd, String rank) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getRankName(version, channel_ID_Hd, rank);
	}

	/**
	 * a by ni_f L824 查询机构classid
	 */
	@Override
	public String getBranchClass(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getBranchClass(salesInfo);
	}

	/**
	 * a by ni_f L824 根据人员代码获取人员信息(删除按钮)
	 */
	@Override
	public SalesInfo getSalesInfos(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getSalesInfos(salesInfo);
	}

	/**
	 * 返回所有渠道在职人员使用此银行卡账户的人员信息 add by li_br L814
	 */
	@Override
	public List<SalesInfo> repeatAccountInfo(String account_id, String sales_id) {
		return salesInfoDao_hd.repeatAccountInfo(account_id, sales_id);
	}

	/**
	 * a by zhang_gb 查询机构branch_id
	 */
	@Override
	public String getBranch_id(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getBranch_id(salesInfo);
	}

	// L911 获取组织下管理序列的人员人数
	public String getManager_num(SalesInfoXQ salesInfoXQ) {
		int manager_num = salesInfoDao_hd.getManager_num(salesInfoXQ);
		if (manager_num > 0) {
			return "no";
		}
		return "yes";
	}

	/**
	 * 推送删除信息报文
	 *
	 * @author gb.z
	 * @param sales_code
	 * @serialData 2019/12/05
	 * @return openid
	 */
	public SalesMoveInfo getOpenIdMessage(String sales_code) {

		return salesInfoDao_hd.getOpenIdMessage(sales_code);
	}
	//a by wzj for L1003（协2019-2668）关于四级机构互动专员队伍上收系统改造的需求 start
	public List<HashMap> getAccreditOrg(String id,String type,String channelId){
		if("1".equals(type))
			return salesInfoDao_hd.getAccreditOrgByRecommendId(id,channelId);
		else return salesInfoDao_hd.getAccreditOrgByTeamId(id,channelId);
	}
	public String residentAreaQuerySales(int limit, int start, SalesInfo salesInfo){
		List<HashMap> list = salesInfoDao_hd.residentAreaQuerySales(limit, start, salesInfo);
		HashMap jg=new HashMap();
//		int end=start+limit;
//		end=(end>list.size())?list.size():end;
		jg.put("totalCount",salesInfoDao_hd.residentAreaQuerySalesCount(salesInfo));
		jg.put("root",list); //list.subList(start,end)
		for(HashMap line:(List<HashMap>)jg.get("root")){
//			if("".equals(line.get("ACCREDIT_ORG"))) line.put("ACCREDIT_ORG_NAME","");
//			else line.put("ACCREDIT_ORG_NAME","");
//			line.put("RANK",rankdefDao.queryRankName(DataConst.Channel_ID_Hd, (String)line.get("RANK"), (String)line.get("BASE_VERSION_ID")));
//			line.put("STAT",codecodeDao.queryCodeName((String)line.get("STAT"), DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_STAFF_STATUS));
//			line.put("TEAM_TYPE",codecodeDao.queryCodeName((String)line.get("TEAM_TYPE"), DataConst.Channel_ID_Hd,CodeTypeConst.CODE_TYPE_ORGANIZATION_TYPE));
			//a by wzj for L1540 start
			if(StringUtils.isNotEmpty((String)line.get("ACCREDIT_ORG")) || "y".equals(line.get("HAS_CX"))) {
				line.put("resident_stat", "已指派");
			}else{
				line.put("resident_stat", "未指派");
				line.put("PROPERTY_NETWORKS","-");
			}
			//a by wzj for L1540 end
			line.put("params",line.toString().replaceAll(", ","&").replace("{","").replace("}","").replaceAll("null",""));
			line.put("action","财险机构指定");
		}
		JSONObject jsonObject=JSONObject.fromObject(jg);
		return jsonObject.toString().replaceAll("null","\"\"");
	}
	public String getPropertyInsuranceOrg(String branchId){
		List<HashMap> list=salesInfoDao_hd.getPropertyInsuranceOrg(branchId);
		return JSONArray.fromObject(list).toString();
	}
	public String getPropertyInsurance3Org(String dm,String dm3){
		Map jg=new HashMap();
		jg.put("mc",salesInfoDao_hd.getPropertyInsurance3Org(dm3));
		jg.put("dm",dm); //这个dm是页面元素id，用来定位到所属3级机构的td标签，然后赋值
		return JSONObject.fromObject(jg).toString();
	}
	public String savePropertyInsuranceOrg(String dm,String sales_code,String team_id,String channel_id,String branch_id){
	    String jg="0";
	    HashMap param=new HashMap();
	    param.put("sales_code",sales_code);
		param.put("team_id",team_id);
		param.put("channel_id",channel_id);
		param.put("branch_id",branch_id);
		param.put("dms", Arrays.asList(dm.split(",")));
		param.put("stat","1");
		param.put("remark","");
		salesInfoDao_hd.savePropertyInsuranceOrg(param);

		//获取用户已经保存的财险机构个数
		List<HashMap> list = salesInfoDao_hd.getUserPropertyInsuranceOrg(param);
		int count = list == null ? 0: list.size();
		//更新t02salesinfo 服务财险网点数据字段
		param.put("netWorks",count);
		salesInfoDao_hd.updateSalesNetworks(param);

		jg="1";
	    return jg;
    }
    public String getUserPropertyInsuranceOrg(String sales_code,String team_id,String channel_id,String branch_id){
		HashMap param=new HashMap();
		param.put("sales_code",sales_code);
		param.put("team_id",team_id);
		param.put("channel_id",channel_id);
		param.put("branch_id",branch_id);
		return JSONArray.fromObject(salesInfoDao_hd.getUserPropertyInsuranceOrg(param)).toString();
	}
	public List getPropertyInsuranceOrgExportData(String sales_code,String team_id,String channel_id,String branch_id){
		List sheet = new ArrayList();
		sheet.add("jgdm,jgmc,jgmm");
		HashMap param=new HashMap();
		param.put("sales_code",sales_code);
		param.put("team_id",team_id);
		param.put("channel_id",channel_id);
		param.put("branch_id",branch_id);
		List<HashMap> data=salesInfoDao_hd.getUserPropertyInsuranceOrg(param);
		for(HashMap line:data){
			String JGDM = line.get("JGDM").toString();
			String JGMM = salesInfoDao_hd.getPropertyInsurance3Org(JGDM.substring(0,JGDM.length()-2)+"00");
			if(JGMM == null) {
				JGMM = "";
			}
			sheet.add(line.get("JGDM")+","+line.get("JGMC")+","+JGMM);
		}

		return sheet;
	}
	//a by wzj for L1003（协2019-2668）关于四级机构互动专员队伍上收系统改造的需求 end


	public void ins_t_hrcontract_query(Map map){
		salesPrepareHrDAO.del_t_hrcontract_query(map);
		salesPrepareHrDAO.ins_t_hrcontract_query(map);
		salesPrepareHrDAO.ins_t_xmllog_request(map);
		salesPrepareHrDAO.ins_t_xmllog_response(map);
	}

	@Override
	public SalesInfo getMessage(String channelId, String salesCode) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getMessage(channelId, salesCode);
	}
	@Override
	public int isPrepare(String salesCode) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.isPrepare(salesCode);
	}
	public boolean getAssurerNum2(SalesInfo salesInfo){
		boolean result = false;
		int count = salesInfoDao_hd.getIsResigned2(salesInfo);
		if(count > 0){
			result = true;
		}
		return result;
	}
	public SalesInfo getEscortoperationById(String sales_id){
		return salesInfoDao_hd.getEscortoperationById(sales_id);
	}
	@Override
	public String isAssuerOut(String idType, String idNo,String salesId) {
		String resultInfo = "";
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		try{
			count1 = salesInfoDao_hd.AssurerOutCount(idType, idNo,SalesCodeUtils.toLocalCode(salesId));
			if(count1 >= 10){
				resultInfo =ConstResult.AJAX_RESULT_FAILURE +"该外担保人担保人数已达10人，请重新输入！";
			}else{
				count2 = salesInfoDao_hd.isPrepareInfo(idType, idNo);
				if(count2>0){
					resultInfo = "2" +"预入司成功未正式入司的不能做外担保人";
				}else{
					count3 = salesInfoDao_hd.SalesInfoCount(idType, idNo);
					if(count3 > 0){
						resultInfo = "3" +"外担保人不能为公司在职人员，请重新输入！";
					}else{
						resultInfo = ConstResult.AJAX_RESULT_SUCCESS;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			resultInfo = "3" +"此人不能作为外担保人，请重新输入！";
		}finally{
			return resultInfo;
		}

	}
	@Override
	public String checkIdNoRepeat(String idType, String idNo) {

		int count1 = 0;
		int count2 = 0;
		String resultInfo = "";
		try{
			count1 = salesInfoDao_hd.isPrepareInfo(idType, idNo);
			if(count1 >0){

				resultInfo = ConstResult.AJAX_RESULT_FAILURE +"预入司成功未正式入司的不能做内担保人";
			}else{
				count2 = salesInfoDao_hd.SalesInfoCount(idType, idNo);
				if(count2>0){
					List<SalesInfo> list = salesInfoDao_hd.checkAssurerName(idType, idNo);
					resultInfo = ConstResult.AJAX_RESULT_SUCCESS+"#"+list.get(0).getSalesCode()+"#"+list.get(0).getSales_name();
				}else{
					resultInfo = ConstResult.AJAX_RESULT_FAILURE +"内担保人必须为公司在职人员，请重新输入！";
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			resultInfo = ConstResult.AJAX_RESULT_FAILURE +"此人不能作为内担保人，请重新输入！";
		}finally{
			return resultInfo;
		}
	}
	@Override
	public SalesInfo getAssuer(String channelId, String salesCode) {

		return salesInfoDao_hd.getAssuer(channelId, salesCode);
	}
	@Override
	public SalesInfo getAssuerMove(String channelId, String salesCode) {

		return salesInfoDao_hd.getAssuerMove(channelId, salesCode);
	}


	//代理制销售人员从银保渠道离司后再次入司银保渠道的时间间隔不得少于半年 a by lizuochao L1514 20210617
	@Override
	public int checkSalesinfoDismissDate(SalesInfo salesInfo) {
		return salesInfoDao_hd.checkSalesinfoDismissDate(salesInfo);
	}

	@Override
	public Map<String,Integer> getAgeLimit(String channelId, String teamId, String workspaceId) {
		return salesInfoDao_hd.getAgeLimit(channelId, teamId, workspaceId);
	}
	// add by syy for L1864优化  20220927
	@Override
	public  String getParano(String channelId, String teamId,String workspaceId) {
		return salesInfoDao_hd.getParano(channelId, teamId,workspaceId);
	}
	
	//a by liulei for 江苏执业证接口  插入日志 20210714
	public void insertJSLog(Map map) {
		salesInfoDao_hd.insertJSLog(map);
	}

	/*
			L1536 2021/08/18 caoh add
	 */
	@Override
	public String existsRole(Map<String, String> params) {
		//某个渠道下的  ’近亲属相关信息采集‘  菜单拥有权限的角色
		List<String> roles = salesInfoDao_hd.getModuleIdByCondition(params);
		//当前用户拥有的权限
		String userRoleId = params.get("roleId");
		String flag = "0";
		for (String role:roles) {
			if(StringUtils.isNotEmpty(userRoleId) && userRoleId.contains(role)){
				flag = "1";
			}
		}
		return flag;
	}
	
	/*
	 * L1536 2021/09/24 caoh add
	 * @see cn.com.sysnet.smis.hd.service.SalesInfoService#existsBranch(java.util.Map)
	 */
	@Override
	public String existsBranch(Map<String, String> params) {
		// TODO Auto-generated method stub
		 int count = salesInfoDao_hd.existsBranch(params);
		 if(count > 0){
			 return "1";
		 }
		 return "0";
	}
	/*L1532 mwl 202110 特殊年龄入司*/
	@Override
	public Map<String, Integer> getIsValid_tx(Map<String, String> para) {
		return salesInfoDao_hd.getIsValid_tx(para);
	}


	
	@Override
	public String getSendVerfyCodeBranch(SalesInfo sale) {
		// TODO Auto-generated method stub
		String sendVerfyCodeBranch = salesInfoDao_hd.getSendVerfyCodeBranch(sale);
		return sendVerfyCodeBranch;
	}
	@Override
	public boolean selectVerifyCode2(SalesInfo salesInfo) {
		// TODO Auto-generated method stub
		List<SalesInfo> s = salesInfoDao_hd.selectVerifyCode2(salesInfo);

		if(!s.isEmpty()){
			return true;
		}else{
			return false;
		}
		
	}


	@Override
	public SalesInfo getGondsman(SalesInfo sales) {
		return salesInfoDao_hd.getGondsman(sales);
	}

	@Override
	public int checkQualification_stat(SalesInfo salesinfo1) {
		int count = salesInfoDao_hd.checkQualification_stat(salesinfo1);
		return count;
	}

	@Override
	public int checkSalesinfoMove(String sales_code) {
		int count = salesInfoDao_hd.checkSalesinfoMove(sales_code);
		return count;
	}

	@Override
	public int getComprehensive(SalesInfo salesInfo) {
		int count = 0;
		count = salesInfoDao_hd.getComprehensive(salesInfo);
		return count;
	}
	@Override
	public SalesInfo getIntroduceName(SalesInfo salesInfo) {
		return salesInfoDao_hd.getIntroduceName(salesInfo);
	}
	@Override
	public SalesInfo getSalesPreper(String sales_code) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getSalesPreper(sales_code);
	}
	/*add liu_yl forL1611服营新军 on 20220411 start*/
	@Override
	//通过渠道、人员代码、手机号修改人员预入司表E测得分和获取E测得分时间
	public void updateSalesCombinescore(SalesInfo para) {
		salesInfoDao_hd.updateSalesCombinescore(para);
	}
	@Override
	//通过渠道、人员代码、请求类型查询调用接口的时间表信息
	public SalesInfo selectSalesBerforetimeInterface(SalesInfo para) {
		return salesInfoDao_hd.selectSalesBerforetimeInterface(para);
	}
	@Override
	//插入调用接口的时间表信息
	public void insertSalesBerforetimeInterface(SalesInfo para) {
		salesInfoDao_hd.insertSalesBerforetimeInterface(para);
	}
	@Override
	//通过渠道、人员代码、请求类型修改调用接口的时间表的请求时间
	public void updateSalesBerforetimeInterface(SalesInfo para) {
		salesInfoDao_hd.updateSalesBerforetimeInterface(para);
	}
	/*add liu_yl forL1611服营新军 on 20220411 end*/
	@Override
	public SalesInfo getCxzyName(SalesInfo salesInfo) {
		/*added by mqj for L2714 调用接口获取财险专员姓名*/

		RequestModel m=new RequestModel();
		m.setUniSalesCod(salesInfo.getCommissioner_code());
		m.setMessageType("01");
		m.setManOrgCod(salesInfo.getBranch_id2());
		ResponseModel rm = null;

		System.out.println("getJobNumberP_start");
		try {
			rm = unifiedJobNumberServiceImpl.getJobNumberP(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("getJobNumberP_end");
		salesInfo.setCommissioner_name(rm.getSalesNam());
		return salesInfo;
	}
	@Override
	public String getIsexcperson(String sales_id) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getIsexcperson(sales_id);
	}
		
	/*add syy for L1831  on 20220713 start*/
	@Override
	public List<SalesInfoXQ> getRecommendRouteType(SalesInfoXQ para){
		return salesInfoDao_hd.getRecommendRouteType(para);
		
	} // 查询所属路线
	
	@Override
	public List<SalesInfoXQ> getRecommendBranchIWP(SalesInfoXQ para){
		return salesInfoDao_hd.getRecommendBranchIWP(para);
		
	}  //    查询组织归属的机构是否IWP
	/*add syy for L1831  on 20220713 end*/
	
	public String getLastDate(String sales_code, String channel_id) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getLastDate(sales_code,channel_id);
	}
	//a by qsw for yunwei on 20230704
	public String getDate(String sales_code, String channel_id) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getDate(sales_code,channel_id);
	}
	@Override
	public List<SalesInfo> getBranch222(String branch_id1) {
		return salesInfoDao_hd.getBranch222(branch_id1);
	}

	@Override
	public List<SalesInfo> getBranch333(String branch_id2) {
		return salesInfoDao_hd.getBranch333(branch_id2);
	}

	@Override
	public List<SalesInfo> getBranch444(String branch_id3) {
		return salesInfoDao_hd.getBranch444(branch_id3);
	}

	public int getHrSwitch(String branch_id, String channel_id) {
		// TODO Auto-generated method stub
		return salesInfoDao_hd.getHrSwitch(branch_id,channel_id);
	}
	@Override
	public List<CodecodeInfo> queryCodeInfo(String codetype,String channeltype){
		return codecodeDao.queryCode(codetype, channeltype);
	}

	@Override
	public SalesInfo getBranchTreeInfo(SalesInfo salesinfo1) {
		return salesInfoDao_hd.getBranchTreeInfo(salesinfo1);
	}
	public String insertDevelopTask(T02developtask t02developtask) {
		String result = "";
		try {
			//查询业务员最新一条执业证登记任务
			List<T02developtask> list = salesInfoDao_hd.selectDeveloptask(t02developtask);
			//插入登记任务前需要先校验是否有在途中的任务
			if (null != list && list.size() > 0) {
				T02developtask task = list.get(0);
				if ("1".equals(task.getTask_stat())) {
					result = "已有在途中的任务，请到执业登记菜单进行完成执业证登记！";
				} else if ("2".equals(task.getTask_stat())) {
					result = "已有在途中的任务，请到执业登记菜单查看任务状态！";
				} else {
					//获取执业证信息
					T02developtask develop = salesInfoDao_hd.selectDevelop(t02developtask);
					//有执业证
					if (null != develop && !"".equals(develop.getSales_id())) {
						//验证执业证是否有效
						if ("1".equals(develop.getStauts())) {
							result = "已有有效执业证号，不允许重复登记！";
						} else {
							develop.setTask_id(t02developtask.getTask_id());
							develop.setTask_stat("1");//1：暂存，2：审核中，3：审核不通过，4：审核通过
							develop.setTask_type("dj");//dj:登记任务，bg：变更任务，zx：注销任务
							develop.setInfo_type(t02developtask.getInfo_type());//数据来源
							develop.setStauts("1");//是否有效
							develop.setCreate_user_id(t02developtask.getCreate_user_id());//创建人
							develop.setCreate_user_name(t02developtask.getCreate_user_name());//创建人名称
							//插入新任务前将历史stauts为1的任务改为0
							salesInfoDao_hd.updateTaskStauts(develop);
							//插入任务信息
							salesInfoDao_hd.insertDevelopTask(develop);
						}
					} else {//无执业证，需要获取人员基本信息
						//获取预入司人员信息
						T02developtask sales = salesInfoDao_hd.selectSalesPreInfo(t02developtask);
						if (null != sales && !"".equals(sales.getSales_id())) {
							sales.setTask_id(t02developtask.getTask_id());
							sales.setTask_id(t02developtask.getTask_id());
							sales.setTask_stat("1");//1：暂存，2：审核中，3：审核不通过，4：审核通过
							sales.setTask_type("dj");//dj:登记任务，bg：变更任务，zx：注销任务
							sales.setInfo_type(t02developtask.getInfo_type());//数据来源
							sales.setStauts("1");//是否有效
							sales.setCreate_user_id(t02developtask.getCreate_user_id());//创建人
							sales.setCreate_user_name(t02developtask.getCreate_user_name());//创建人名称
							//插入新任务前将历史stauts为1的任务改为0
							salesInfoDao_hd.updateTaskStauts(sales);
							//插入任务信息
							salesInfoDao_hd.insertDevelopTask(sales);
							result = "";
						} else {
							result = "未查到业务员信息，请核实填写是否有误！";
						}
					}
				}
			} else {
				//执业证任务表中暂无登记任务
				//获取预入司人员信息
				T02developtask sales = salesInfoDao_hd.selectSalesPreInfo(t02developtask);
				if (null != sales && !"".equals(sales.getSales_id())) {
					sales.setTask_id(t02developtask.getTask_id());
					sales.setTask_id(t02developtask.getTask_id());
					sales.setTask_stat("1");//1：暂存，2：审核中，3：审核不通过，4：审核通过
					sales.setTask_type("dj");//dj:登记任务，bg：变更任务，zx：注销任务
					sales.setInfo_type(t02developtask.getInfo_type());//数据来源
					sales.setStauts("1");//是否有效
					sales.setCreate_user_id(t02developtask.getCreate_user_id());//创建人
					sales.setCreate_user_name(t02developtask.getCreate_user_name());//创建人名称
					//插入新任务前将历史stauts为1的任务改为0
					salesInfoDao_hd.updateTaskStauts(sales);
					//插入任务信息
					salesInfoDao_hd.insertDevelopTask(sales);
					result = "";
				} else {
					result = "未查到业务员信息，请核实填写是否有误！";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			result = "自动生成执业证登记暂存任务失败！";
		} finally {
			return result;
		}
	}
	public String getSeq_develop_task_id(){
		return salesInfoDao_hd.getSeq_develop_task_id();
	}
	//a by qsw for L2325 on 2023-08-02 历史入司记录查询
	@Override
	public String queryHistory(SalesInfo info) {
		String json = "";
		try {
			int count = salesInfoDao_hd.queryHistoryCount(info);

			List<SalesInfo> monAllowanceInfoList = salesInfoDao_hd.queryHistory(info);
			json = "{totalCount:" + count + ",root:[";
			for (int i = 0; i < monAllowanceInfoList.size(); i++) {

				SalesInfo list = monAllowanceInfoList.get(i);

				json += "{" + "sales_id:'" + list.getSales_id()
						+ "',sales_name:'" + list.getSales_name()
						+ "',channel_id:'" + list.getChannel_id()
						+ "',branch_id:'" + list.getBranch_id()
						+ "',branch_name:'" + list.getBranch_name()
						+ "',rank_name:'" + list.getRank_name()
						+ "',probation_date:'" + list.getProbation_date()
						+ "',dismiss_date:'" + list.getDismiss_date2()
						+" '}" ;

				if (i != monAllowanceInfoList.size() - 1) {
					json += ",";
				}
			}

			json += "]}";

			json = json.replaceAll("null", "");
			return json;


		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return "";
	}
	@Override
	public String getSalesNameP(String channel_id, String sales_code) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("channel_id", channel_id);
		param.put("sales_code", sales_code);
		return salesInfoDao_hd.getSalesNameP(param);
	}
	//a by qsw for L2382
	public PublicFunction getSalesNameHD(PublicFunction publicFunction,
										 boolean ignore) {

		String team_id = publicFunction.getTeam_id();
		String channel_id = publicFunction.getChannel_id();
		String branch_id_str = publicFunction.getBranchStr();
		String branch_id = publicFunction.getBranch_id();
		String msrStr = "";
		int countU = salesInfoDao_hd.getCountU(SalesCodeUtils.getSalesID(publicFunction.getSales_code()),SalesCodeUtils.getSalesID(publicFunction.getSales_id()));
		PublicFunction info = publicFunctionDao.getSalesNameQX(publicFunction.getSales_id());
		if(info != null){
			if (publicFunction.getSales_id().equals(publicFunction.getSales_code())||countU!=0){
				msrStr = "该推荐关系成环，请重新填写推荐人代码！";
				info.setRemark("failure");
			} else if(info.getChannel_id().equals(channel_id)){
				int i = 0;
				if (branch_id_str!=null)
					i=branch_id_str.indexOf(info.getBranch_id());
				if(i>=0){
					if(UtilString.trim(branch_id).equals("")){
						if(info.getTeam_id().equals(team_id)){
							info.setRemark("success");

						}else if(team_id==null||team_id.equals("")){

							info.setRemark("success");

						}else{
							msrStr = "输入的人员代码不属于本组织，请重新输入。";
							info.setRemark("failure");
						}
					}else{
						if(info.getBranch_id().equals(branch_id)){
							if(info.getTeam_id().equals(team_id)){

								info.setRemark("success");
							}else if(team_id==null||team_id.equals("")){
								info.setRemark("success");
							}else{
								msrStr = "输入的人员代码不属于本组织，请重新输入。";
								info.setRemark("failure");
							}
						}else{
							msrStr = "此推荐人代码所属机构不在操作权限范围内";
							info.setRemark("failure");
						}
					}

				}else{
					msrStr = "此推荐人代码所属机构不在操作权限范围内";
					info.setRemark("failure");
				}
			}else{
				//msrStr = "输入的人员代码不属于本渠道，请重新输入.......";
				msrStr = "推荐人代码输入错误，请重新输入！";
				info.setRemark("failure");
			}
			info.setMsrStr(msrStr);
			return info;
		}else{
			return null;
		}
	}
	public int getCountU(String sales_id,String recommend_id) {
		return salesInfoDao_hd.getCountU(sales_id,recommend_id);
	}
	@Override
	public String querybranchNameById(String branch_id22) {
		return salesInfoDao_hd.querybranchNameById(branch_id22);
	}
	public String queryCXBranchName2(String branch_id2) {
		return salesInfoDao_hd.queryCXBranchName2(branch_id2);
	}
	public String queryCXBranchId2(String branch_id2) {
		return salesInfoDao_hd.queryCXBranchId2(branch_id2);
	}

	@Override
	public List<SalesInfo> getBranch111() {
		return salesInfoDao_hd.getBranch111();
	}

    @Override
    public List<SalesInfo> getAllBranchId2() {
        return salesInfoDao_hd.getAllBranchId2();
    }

    public int getClassId(String branch_id) {
		return salesInfoDao_hd.getClassId(branch_id);
	}
	public String getBranchId4(String sales_id) {
		return salesInfoDao_hd.getBranchId4(sales_id);
	}
	//L2382ld
	public String getBranchId3ByTeamId(String team_id) {
		return salesInfoDao_hd.getBranchId3ByTeamId(team_id);
	}
	public String getBranchIdParent(String branch_id) {
		return salesInfoDao_hd.getBranchIdParent(branch_id);
	}
	public TeamInfo queryTeamInfo(String channel_id,String team_id){
		return salesInfoDao_hd.queryTeamInfo(channel_id,team_id);
	}
	public List<HashMap> getworkspaceinfobybranch(String branch_id) {
		return salesInfoDao_hd.getworkspaceinfobybranch(branch_id);
	}

	@Override
	public SalesInfo getSecondEntry(SalesInfo salesInfo) {
		return salesInfoDao_hd.getSecondEntry(salesInfo);
		}

	@Override
	public  String getSecondEntryParano(String channelId, String teamId,String workspaceId) {
		return salesInfoDao_hd.getSecondEntryParano(channelId, teamId,workspaceId);
	}

	@Override
	public  int checkiIsChannelId(String sales_id) {
		return salesInfoDao_hd.checkiIsChannelId(sales_id);
	}

	/**
	 * 主管校验
	 * @param salesInfo
	 * @return java.lang.String
	 * @author xz_lc
	 * @date 2024/7/11 15:38
	 */
	@Override
	public String checkLeader(SalesInfo salesInfo) {

		String res = "";

		//1、获取主管的人员信息、机构、在职状态、当前职务和层级
		String leader_code = salesInfo.getLeader_code();
		SalesInfo leader = salesInfoDao_hd.getLeaderInfo(leader_code);

		//2、获取销售人员的信息、机构、当前职务和层级、所属主管
		String sales_id = salesInfo.getSales_id();
		SalesInfo sales = salesInfoDao_hd.getSalesInfo2(sales_id);

		//3、销售人员是否有推荐关系信息 t02_recommend_ralation
		int count = recommendChangeService.getCountRecomRelation(sales.getSales_id());

		// 获取销售人员的一代推荐人id
		int countCrossBr4ByLeader = 1;
		String recommendId = salesInfoDao_hd.getRecommendOld(sales.getSales_id());
		if (recommendId != null) {
			countCrossBr4ByLeader = recommendChangeDao.getCountCrossBr4(leader.getLeader_id(),recommendId);
		}

		//修改页面销售人员职务层级
		String post_lvl= getRankPost(salesInfo.getRank(),sales.getBase_version_id());

		if(leader_code.equals(sales_id)){
			leader.setPost_lvl(post_lvl);
		}
		// 页面职级调整降层级
		if("0".equals(post_lvl) && "1".equals(sales.getPost_lvl())){
			sales.setLeader_id("0000000");
		}
		//页面调整晋升层级
		if("1".equals(post_lvl) && "0".equals(sales.getPost_lvl())){
			sales.setLeader_id(sales.getSales_id());
		}

		//1.该主管与互动经理在同一个渠道同一个三级机构下，否则提示“主管代码不可跨三级机构，请重新填写！”
		if(!leader.getBranch_id3().equals(sales.getBranch_id3())){
			res = "主管代码不可跨三级机构，请重新填写！";
		}else if("2".equals(leader.getStat())){
			res = "该主管已离职，请重新填写！";
		}else if(!"1".equals(leader.getPost_lvl())){
			res = "该主管的当前职务为非主管层级，请重新填写！";
		}else if(!"0".equals(post_lvl) && !sales_id.equals(leader_code)){
			res = "该互动经理的当前职务为主管层级，不可指定主管！";
		}else if(!"0000000".equals(sales.getLeader_id())){
			res = "该被推荐人在当前机构已有主管，不允许跨四级机构指定主管！";
		}else if(leader.getBranch_id4().equals(sales.getBranch_id4())
				&& !leader.getTeam_id().equals(sales.getTeam_id())){
			res = "该互动经理与主管在同一个四级机构下，不可跨组织指定主管！";
		}else if(countCrossBr4ByLeader == 0){
			res = "该互动经理有推荐人，该主管与推荐人不存在管辖关系，不能操作该互动经理指定主管";
		}else if(leader_code.equals(sales_id) && !"1".equals(post_lvl)){
			res = "该主管的当前职务为非主管层级，请重新填写！";
		}else{
			//返回空字符串
		}

		return res;
	}

	/**
	 * 获取跨四级管辖关系个数
	 * @param sales_id
	 * @param stat
	 * @param norecommend
	 * @return int
	 * @author xz_lc
	 * @date 2024/7/11 15:39
	 */
	@Override
	public int getCountOverRelation(String sales_id, String stat, String norecommend) {
		int count = salesInfoDao_hd.getCountOverRelation(sales_id, stat, norecommend);
		return count;
	}


}
