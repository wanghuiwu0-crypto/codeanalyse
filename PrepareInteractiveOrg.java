<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:directive.page import="cn.com.sysnet.smis.share.common.CodeTypeConst"/>
<jsp:directive.page import="cn.com.sysnet.smis.share.common.DataConst"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="cn.com.sysnet.util.UtilConfig,cn.com.sysnet.smis.hd.model.SalesInfo,cn.com.sysnet.common.ConstResult,cn.com.sysnet.smis.share.model.UserInfo,cn.com.sysnet.smis.share.common.LengthConst,cn.com.sysnet.common.ConstFlag"%>
<%
	String locationPath = UtilConfig.getProperty("LocationPath"); 
	SalesInfo salesInfo=(SalesInfo)request.getAttribute("salesInfo");//a by shiyawei for 613
   UserInfo user=(UserInfo)request.getSession().getAttribute("user");
	String branch_id = user.getBranchIdCur();//机构ID
	String channel_id = user.getChannelId();//机构ID
	    //A by macj for BUG882  on 2013.01.05 begin
		String isOpen = (String)request.getAttribute("isOpen");
		//A by macj for BUG882  on 2013.01.05 end
	//2043绩优路线开关
	String performanceSwitch = (String)request.getAttribute("performanceSwitch");
%>

<html>
  <head>
	
    <title>人员预入司</title>
    <link href="<%=locationPath%>/resource/images/style.css" rel="stylesheet" type="text/css"></link>
	<script type="text/javascript" src="<%=locationPath%>/resource/script/httprequest.js"></script>
	<script type="text/javascript" src="<%=locationPath%>/resource/script/share.js"></script>
	<link rel="stylesheet" type="text/css"
	href="<%=locationPath%>/resource/css/yb.css" />
	<script type="text/javascript" src="<%=locationPath%>/resource/script/shareAgeDeal.js"></script>
	<script src="<%=locationPath%>/resource/script/simple_dialog.js" type="text/javascript"></script> 
	<script src="<%=locationPath%>/resource/script/public.js" type="text/javascript"></script>
	<script src="<%=locationPath%>/resource/script/syTree.js" type="text/javascript"></script>
	<script src="<%=locationPath%>/resource/script/commons.js" type="text/javascript"></script>
	<script src="<%=locationPath%>/resource/script/commons_workspace.js" type="text/javascript"></script>
	<script src="<%=locationPath%>/resource/script/prototype.js" type="text/javascript"></script>
	<script type="text/javascript" src="<%=locationPath%>/resource/script/datetime.js"></script>
	<link rel="stylesheet" type="text/css" href="<%=locationPath%>/resource/css/yb.css" />
	<script type="text/javascript" src="<%=locationPath%>/resource/script/jquery-1.10.0.min.js"></script>
	<script type="text/javascript" src="<%=locationPath%>/resource/script/datePicker/WdatePicker.js"></script>
	  <script src="<%=locationPath%>/resource/script/xq/salesinfo.js" type="text/javascript"></script>
<script type="text/javascript">
    var branch_id = "<%=branch_id%>";//机构ID a by shiyawei for 613
    var channel_id="<%=channel_id%>";
	var message = '${message}';
	var resultinfo = '${resultinfo}';
	var probationDate = '${probation_date}';//入司时间(服务器时间);
	var rainInfo = '${rainInfo}';//获取免培训机构数据标记
	var vTemp='${result}';   //集团工号是否生成 M by wang_gq
	var isExists='${isExists}';   //特殊机构校验近亲属
	var ifclick = false;
	var teamLvl = "";
	var LeaderID = "";
	var LeaderID2 = "";
	var isHave = "";//是否为直辖组
	var have_manager = "";
	var recommend_type = "";
	var rankT = "";/* 推荐人的业务职级 */
	var is_inherency = "";//是否固有组
	//A by j_yc for L243 begin
    var a = '0';
    var b = '0';
    var c = '1';
	var team_is_new_army = '';/* add liu_yl forL1611服营新军 on 20211220 */
    //A by j_yc for L243 end
	var dismissInfo = '';//L2113 综金上次离职信息
	var recommend_team_id = '';

	function checkAndMatch(){
		var commissioner_code = document.all.commissioner_code.value;
		if(commissioner_code==""){
			alert("请输入财险专员代码！");
			document.all.commissioner_code.value = "";
			document.all.commissioner_name.value = "";
			return;
		}
		//l2284个险增加财险专员字段
		if((channel_id=="04" || channel_id=="01")){
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doGetcommissionerName,"");
				doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getCxzyName&commissioner_code="+commissioner_code,"GET",httpRequest);
			}
		}
	}


        async function getIdNo() {

	  var id_no=document.all.id_no.value;
		var id_type=document.all.id_type.value;
		//A by macj for BUG868  on 2013.01.05 begin
		document.all.sex.disabled=false;
		document.all.birthday.disabled=false;
		
		if(id_no==""){
			alert('请输入证件号码!');
			return;
		}
		//add by liu_xj for bug7308身份证小写字母转大写 start
		if(id_no.substr(id_no.length-1)=="x"){
	        id_no=id_no.substr(0,id_no.length-1)+"X";
	        document.all.id_no.value=id_no;
        }
		//add by liu_xj for bug7308身份证小写字母转大写 end
		if(document.all.id_type.value=="<%=CodeTypeConst.CODE_CODE_DOCUMENTS_CARD%>"){
			    	var id_no = document.all.id_no.value;
						if(id_no==""){
							return;
						}
		     if(checkCnId(id_no)==false){
							alert("身份证号码不合法！");
							document.all.id_no.value="";
							document.all.birthday.value = "";
							document.all.sex.value = "";
							//document.all.id_no.focus();
							return;
		     }
		
		}
            flag = true;
		// L2043 大个险该人员是否在黑名单上 start
		if(id_no==""  ){
			return;
            }

		//add by lzy for L2759 start
			if (channel_id == "03") {
				//必须先把组织代码填完，才能有职级可选，否则这里的渲染无效
				var team_id=document.all.team_id.value;
				if(team_id=="") {
					alert("请输入组织代码！");
					document.all.id_no.value = "";
					return;
				}

				var result="";
				var pars = '&id_type=' + id_type +'&id_no=' + id_no;
				var url = '<%=locationPath%>/specialEntry.do?method=qryLastAprovedRecord'
						+ pars;
				$.ajax({
					type : "get", // 请求方式
					url : url, // 目标资源
					dataType : "text", // 服务器响应的数据类型
					async : false,
					success : function(data) {
						result =  data ;
					}
				});
				if (null == result || "" == result) {
					document.all.rank.disabled = false;
				} else {
					document.all.rank.value = result;
					document.all.rank.disabled = true;
				}
			}

		//  add by lzy for L2759 end
            try {
                let result = await $.post('<%=locationPath%>/SalesInfo_gx.do?method=blackListCheck', {id_no: id_no});
                result = eval('(' + result + ')');
				if(result.status ==  "1"){
					alert('与该身份证号码完全一致的人员已纳入黑名单内，禁止进行预入司操作！'+'\n'
							+'黑名单人员信息：'+ id_no
					);
					document.all.id_no.value="";
					dialog_waiting.close();//解屏
                    flag = false;
					return ;
				}else if(result.status == "0"){
					flag = true;
				}
				else{
					alert('校验该业务员数据时发生错误，请反馈！');
					document.all.id_no.value="";
					dialog_waiting.close();//解屏
                    flag = false;
					}
            } catch (error) {
                console.error("请求发生错误：", error);
                flag = false;
				}

		if(channel_id == "01"  ||channel_id=='05' ||channel_id=='09' ||channel_id=='08' || channel_id == '04' || channel_id == '03'){
                let result = await $.post('<%=locationPath%>/SalesInfo_gx.do?method=backSurveyCheck', {
                    id_no: id_no,
                    id_type: id_type
                });
                result = eval('(' + result + ')');
					if(result.status == "4"){
						alert('该证件号码未进行预入司背景调查，请重新录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                    flag = false;
						return ;
					}else if(result.status == "1"){
						
						alert('该证件号码无背景调查结果，请稍后录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                    flag = false;
						return ;
					}else if(result.status == "2"){
						 
						alert('该证件号码为刑事案件高风险无法入司，请重新录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                    flag = false;
						return ;
					//a by cyy for L2728 start
					}else if(result.status == "3"){
						alert('该证件号码为高风险，未审核通过无法入司，请重新录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                    flag = false;
						return ;
					}else if(result.status == "6"){
						alert('该证件号码为高风险，需进行高风险人员审核，请重新录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                    flag = false;
						return ;
					}else if(result.status == "8"){
						 var tip = result.info;
						alert('该证件号码为'+tip+'无法入司，请重新录入！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
						flag = false;
						return ;
					}else if(result.status == "0"){
						flag = true;
					
					}
			}

		if('01,04,05,02'.indexOf(channel_id)>-1){
                try {
                    let result = await $.post('<%=locationPath%>/salesInfo_hd.do?method=hasZeroScore', {id_no: id_no});
                    result = eval('(' + result + ')');
				if(result.code==200){
					if(result.dataInfo>0){
						alert('该人员存在过品质得分为0的情况，不能再次入司！');
						document.all.id_no.value="";
						dialog_waiting.close();//解屏
                            flag = false;
					} else{
                            let d = await $.post('<%=locationPath%>/qcparamterAction.do?method=hasOneVetoBehaviour', {id_no: id_no});
                            d = eval('(' + d + ')');
                            if (d.code == 200) {
                                if (d.dataInfo > 0) {
									alert('该人员存在过‘一票否决行为’不能再次入司！');
									document.all.id_no.value="";
									dialog_waiting.close();//解屏
                                    flag = false;
								}
							}else{
								alert('校验该业务员数据时发生错误，请反馈！');
								document.all.id_no.value="";
								dialog_waiting.close();//解屏
                                flag = false;
                            }
                        }
                    }

                } catch (error) {
                    console.error("请求发生错误：", error);
                    flag = false;
				}
            }

            if (flag) {
				var httpRequest=new HttpRequest();
				with(httpRequest){
					init();
					doSetCallBack(doGetIsPrepare,id_no);
					doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIsPrepare&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id,"GET",httpRequest);
				}
			}
            return undefined;
		}

function doGetIsPrepare(str,id){
	var result=str.substring(0,1);
   	var content=str.substring(1,2);
	if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
	  if(channel_id=="01"){
	      var id_no=document.all.id_no.value;
		  var id_type=document.all.id_type.value;
		var httpRequest=new HttpRequest();
           	with(httpRequest){
	            init();
	            doSetCallBack(doCheckNos,id_no);
	            doSendResuest("<%=locationPath%>/SalesInfo_gx.do?method=checkNo&id_type="+id_type+"&id_no="+id_no,"GET",httpRequest);
	        }
		}else{  
		       var id_no=document.all.id_no.value;
		        var id_type=document.all.id_type.value;
                var branch_id = document.all.branch_id1.value;
                var httpRequest=new HttpRequest();
	           	with(httpRequest){
		            init();
		            doSetCallBack(doGetIsResigned,id_no);
		            doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIsResigned&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id,"GET",httpRequest);
		        }
		      
		 }
	}else{
	        if(content=="Y"){
					document.all.is_resigned.value="<%=ConstFlag.FLAG_VALID%>";
					document.getElementById("birthday").value="";
		            document.getElementById("sex").value="";
        	         document.all.id_no.value = '';
					alert("此人已预入司或正式入司,不可以重复录入！");
					//alert(str.substring(2));
	   			}
	   	   if(content=="N"){
					document.all.is_resigned.value="<%=ConstFlag.FLAG_VALID%>";
					alert("程序异常！");
	   				
	   		}
	   			
		}
}

function doCheckNos(str,id){
    	var result=str.substring(0,1);
   		var content=str.substring(1);
		if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
			if(content != "success"){
				 var info = content.split('#');
				if(info[0]!=null && info[0]!="" && info[0]!='null'){
					document.all.sales_name.value = info[0];
				}
				
				
				if(info[3]!=null && info[3]!='' && info[3]!='null'){
					document.all.sex.value = info[3];
				}
				if(info[4]!=null && info[4]!='' && info[3]!='null'){
					document.all.birthday.value = info[4];
				}
				
				if(info[6]!=null && info[6]!='' && info[6]!='null'){
					document.all.education.value = info[6];
				}	
					
																		
			
				if(info[12]!=null && info[12]!='' && info[12]!='null'){
					document.all.home_address.value = info[12];
				}	
			
				if(info[15]!=null && info[15]!='' && info[15]!='null'){
					document.all.mobile.value = info[15];
				}	
			 
					
			<%-- 	if(document.all.id_type.value=="<%=CodeTypeConst.CODE_CODE_DOCUMENTS_CARD%>"){
				var id_no = document.all.id_no.value;
						if(id_no==""){
							return;
						}
						document.all.birthday.value=getBirthdayFromCnId(id_no);
						document.all.birthday.disabled=true;
						if(getGenderFromCnId(id_no)=="M"){
							document.all.sex.value="1";
						}
						if(getGenderFromCnId(id_no)=="F"){
							document.all.sex.value="2";
						}
						document.all.sex.disabled=true;
				}		 --%>
				
			// document.getElementById("is_resigned_flag").value = document.getElementById("is_resigned").value; 
			 
			   var id_no=document.all.id_no.value;
		        var id_type=document.all.id_type.value;
                var branch_id = document.all.branch_id1.value;
                var httpRequest=new HttpRequest();
	           	with(httpRequest){
		            init();
		            doSetCallBack(doGetIsResigned,id_no);
		            doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIsResigned&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id,"GET",httpRequest);
		        }
																									
			}else{
			      var id_no=document.all.id_no.value;
		          var id_type=document.all.id_type.value;
                  var branch_id = document.all.branch_id1.value;
                  var httpRequest=new HttpRequest();
	             	with(httpRequest){
		            init();
		            doSetCallBack(doGetIsResigned,id_no);
		            doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIsResigned&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id,"GET",httpRequest);
		          }
			
			}
        }else{
        	alert(content);
        	document.getElementById("birthday").value="";
		    document.getElementById("sex").value="";
        	document.all.id_no.value = '';
        	/* document.all.id_no.focus(); */
        	return;
        	
		/*	********* li_jx 09-02-12 **************
			**********与招募甄选功能相结合**************
			
        	alert("该单证抵押金不存在,请先输入单证抵押金信息！")
        	document.all.id_no.value="";
        	document.all.sales_name.value="";
            document.all.deposit.value="";
            document.all.deposit.focus();
            */
        }
    } 


  function doCheckNo(str,id){
    	var result=str.substring(0,1);
   		var content=str.substring(1);
		if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
			
		
		/*	********* li_jx 09-02-12 **************
			**********与招募甄选功能相结合**************
			var deposit = content.split('#');
			if(deposit[0]=="" || deposit[0]==null || deposit[0]=="null"){
				document.all.sales_name.value="";
			}else{
				document.all.sales_name.value=deposit[0];
			}
			if(deposit[1]=="" || deposit[1]==null || deposit[1]=="null"){
				document.all.deposit.value="";
			}else{
				document.all.deposit.value=deposit[1];
			}*/
        }else{
        	alert(content);
        	document.getElementById("birthday").value="";
		    document.getElementById("sex").value="";
        	document.all.id_no.value = '';
        	//document.all.id_no.focus();
        	
		/*	********* li_jx 09-02-12 **************
			**********与招募甄选功能相结合**************
			
        	alert("该单证抵押金不存在,请先输入单证抵押金信息！")
        	document.all.id_no.value="";
        	document.all.sales_name.value="";
            document.all.deposit.value="";
            document.all.deposit.focus();
            */
        }
    } 
function doCheckIdNO(str,id){
        if(str!= "<%=ConstResult.AJAX_RESULT_SUCCESS%>"){	
            alert("该人员已入职，系统禁止录入");
            return;
        }else{
                var id_no=document.all.id_no.value;
		        var id_type=document.all.id_type.value;
                var branch_id = document.all.branch_id1.value;
                var httpRequest=new HttpRequest();
	           	with(httpRequest){
		            init();
		            doSetCallBack(doGetIsResigned,id_no);
		            doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIsResigned&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id,"GET",httpRequest);
		        }
        }
    } 
    
 function  clearidno(){
     document.all.id_no.value="";
     document.all.sex.value="";
     document.all.birthday.value="";
  
  }
function doGetIsResigned(str,id){
		var result=str.substring(0,1);
   		var content=str.substring(1,3).trim();
   		/* var content2=str.substring(3);
   		var org_sales_code;
   		var org_branchid;
   		if(content2 != null && content2 != "" && content2.length >10){
   		 org_sales_code=str.substring(3,13);
   		 org_branchid=str.substring(14);
   		} */


	if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
		var dismissInfo;
		if (channel_id == "04") {
			dismissInfo = str.split("#")[1];
		}
		//M by zhu_jp for BUG896  on 2013.01.08 begin
		/*  document.getElementById("org_sales_code").value =org_sales_code;
          document.getElementById("branch_id4").value =org_branchid; */
   			if(content=="Y0"){
				alert("此人已经离职,可以录入！\n" + dismissInfo);
	   			document.getElementById("flag").value = "successful"; 
	   			document.all.is_resigned.value="<%=ConstFlag.FLAG_VALID%>";
   			}
   			if(content=="Y2"){
	   			 
					alert("此人已经离职,可以录入！\n" + dismissInfo);
	   			//	document.getElementById("flag").value = "successful"; 
	   				document.all.is_resigned.value="<%=ConstFlag.FLAG_VALID%>";
	   				var  ss=document.all.is_resigned.value;
	   			}
			 if(content=="Y1"){
				
					document.all.is_resigned.value="<%=ConstFlag.FLAG_VALID%>";
					alert("此人已经离职,可以录入！\n" + dismissInfo);
	   				document.getElementById("flag").value = "successful"; 
	   				
	   			}
	   			
	   			
	   		//M by zhu_jp for BUG896  on 2013.01.08 end
			if (content == 'Y3') {
				alert("此代理制人员离司日期为" + str.substring(3,13) + "，半年内不能在银保渠道入司！");
				document.all.id_no.value = "";
				document.getElementById("flag").value = "";
				document.all.is_resigned.value = "<%=ConstFlag.FLAG_INVALID%>";
			}
   		}else{
   			if(content=="No"){
   				var channel_name=str.substring(3,str.length);
	   			alert("该证件号码在\""+channel_name+"\"渠道已经存在且还在职，不能录入！");
	   			document.all.id_no.value="";
   				document.getElementById("flag").value = "";
	   			document.all.is_resigned.value="<%=ConstFlag.FLAG_INVALID%>";
   			}
   			if(content=="N"){
   			    if(channel_id !="03"){
   			      alert("该证件号码不存在,可以录入！");
   			    }
   				document.getElementById("flag").value = "successful";
   				document.all.is_resigned.value="<%=ConstFlag.FLAG_INVALID%>";
   			}
   			
   			if(content=="E1"){
   				alert("对不起，该人员未通过招募甄选,不能录入！");
   				document.all.id_no.value="";
   				document.getElementById("flag").value = "";
	   			document.all.is_resigned.value="<%=ConstFlag.FLAG_INVALID%>";
   			}
			
			if(content=="S2"){
	   			     if(channel_id !="03"){
   			          alert("该证件号码不存在,可以录入！");
   			         }
	   				document.getElementById("flag").value = "successful";
	   				document.all.is_resigned.value="<%=ConstFlag.FLAG_INVALID%>";
	   			}
			 if(content=="S1"){
					 if(channel_id !="03"){
   			          alert("该证件号码不存在,可以录入！");
   			         }
	   				document.getElementById("flag").value = "successful";
	   				document.all.is_resigned.value="<%=ConstFlag.FLAG_INVALID%>";
	   			};
   		} 
   		//	if(channel_id !="01"){
	   				if(document.all.id_type.value=="<%=CodeTypeConst.CODE_CODE_DOCUMENTS_CARD%>"){
			    	var id_no = document.all.id_no.value;
						if(id_no==""){
							return;
						}
						
						document.all.birthday.value=getBirthdayFromCnId(id_no);
						document.getElementById("birthday").disabled =true;
						if(getGenderFromCnId(id_no)=="M"){
							document.all.sex.value="1";
						}
						if(getGenderFromCnId(id_no)=="F"){
							document.all.sex.value="2";
						}
						document.all.sex.disabled=true;
						if (channel_id == "03") {
							getStaffInfo();
						}
				}		
	   			
	   	//		}
   		 document.getElementById("is_resigned_flag").value = document.getElementById("is_resigned").value; 
	}

	function getStaffInfo(){
		var id_type = document.getElementById("id_type").value;
		var id_no = document.getElementById("id_no").value;
		if("" !== id_no) {
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doGetStaffInfo,"");
				doSendResuest("<%=locationPath%>/staffInfo_yb.do?method=getStaffInfoByIdNoNew&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id+"&flag=2","GET",httpRequest);
			}
		}
	}

	function doGetStaffInfo(str){
		var result=str.substring(0,1);
		var content=str.substring(1);
		if(result !== "<%=ConstResult.AJAX_RESULT_FAILURE%>") {
			//在职人员不允许申请
			/*alert("在职人员请不要重复申请!");
			document.getElementById("id_no").value = "";
			return;*/

		}else{ //人员未入司
			if(content!=""&&content!=undefined&&content!=null){
				var i = content.indexOf("#");
				var education = content.substring(0,i);
				// var entry_type = content.substring(i+1);
				//“最高学历”字段默认回填为“特殊人员类型申请”或“入司职级申请”最近一次审批通过的最新学历
				document.all.education.value = education;
				/*if(entry_type !="0"){ //入司职级申请应排除
					document.all.special_education.value = education;
				}*/
				var id_type = document.getElementById("id_type").value;
				var id_no = document.getElementById("id_no").value;
				if("" !== id_no) {
					var httpRequest=new HttpRequest();
					with(httpRequest){
						init();
						doSetCallBack(doSetSpecialEdu,"");
						doSendResuest("<%=locationPath%>/staffInfo_yb.do?method=getStaffInfoByIdNoNew&id_type="+id_type+"&id_no="+id_no+"&branch_id="+branch_id+"&flag=1","GET",httpRequest);
					}
				}

			}

		}
	}

	function doSetSpecialEdu(str) {
		var result = str.substring(0, 1);
		var content = str.substring(1);
		if(result !== "<%=ConstResult.AJAX_RESULT_FAILURE%>") {
			//在职人员不允许申请
			/*alert("在职人员请不要重复申请!");
			document.getElementById("id_no").value = "";
			return;*/
		}else{ //人员未入司
			if(content!=""&&content!=undefined&&content!=null){
				var i = content.indexOf("#");
				var education = content.substring(0,i);
				document.all.special_education.value = education;
			}
		}
	}

     function doGetIsRequest(str,id){
	     var oldSelasCode=str.substring(1,200);
	     if(str.indexOf("错误")!=-1){
			oldSelasCode='';
			}
		 //   document.getElementById("oldSaleCode").value=oldSelasCode; 
	  }
	  
 	
 	function changeBirthday(){
		if(document.all.birthday.value==""){
			return;
		}	
		if(document.all.id_type.value=="<%=CodeTypeConst.CODE_CODE_DOCUMENTS_CARD%>" && document.all.id_no.value!=""){
			if(checkCnIdBirth(document.all.id_no.value,document.all.birthday.value)==false){
					alert("出生日期与身份证号码不匹配,请重新输入出生日期!");
					document.all.birthday.value="";
					return;
				
			}
		}
	}
	
 function checkMobile(){
		if(document.all.mobile.value==""){
			return;
		}
		if(checkIsNum(document.all.mobile.value,<%=LengthConst.LENGTH_OF_MOBILE_GX%>)==false||isNumber(document.all.mobile.value)==false){
			alert("手机号为11位的纯数字，请重新输入!");
			document.all.mobile.value="";
			//document.all.mobile.focus();
			return;
		}
		var myreg = /^[1][3,4,5,6,7,8,9][0-9]{9}$/;
	    if (!myreg.test(document.all.mobile.value)) {
	    	alert("手机号必须是以13、14、15、16、17、18、19开头的合规手机号，请重新输入！");
	    	document.all.mobile.value="";
			//document.all.mobile.focus();
	        return;
	    }
	    var continuous = /([0-9])\1{7}/;
        if(continuous.test(document.all.mobile.value)){
            alert("手机号连续数字不能超过7位，请重新输入！");
            document.all.mobile.value="";
			//document.all.mobile.focus();
	        return;
        }
		//add by liu_xj for L522查询手机号码是否存在大于等于两次 
		var mobile =document.all.mobile.value;
       	var httpRequest=new HttpRequest();
           with(httpRequest){
               init();
               doSetCallBack(doGetMobile,mobile);
               doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getMobile&mobile="+mobile,"GET",httpRequest);
              }
	}
	   //add by liu_xj for L522查询手机号码是否存在大于等于两次 
	    function doGetMobile(str,mobile){
    	var result=str.substring(0,1);
   		var content=str.substring(1);
		if (result=="<%=ConstResult.AJAX_RESULT_FAILURE%>"){
		       alert(content);
		       document.all.mobile.value="";
        }
    }
    
    	function checkYBOverAge(oarUrl,id_type,id_no, birthday, branch_id) {
		var result="请求错误";
		var pars = '&id_type=' + id_type +'&id_no=' + id_no+ '&birthday=' + birthday + '&branch_id=' + branch_id;
		var url = oarUrl + pars;
		$.ajax({
			type : "get", // 请求方式
			url : url, // 目标资源
			dataType : "text", // 服务器响应的数据类型
			async : false,
			success : function(data) {
				result =  data ;
			}
		});
		if(""==result){
	        return true;
		}else{
			alert(result);
			return false;
		}
	}

	// L1650 增加二次入司校验
	function beforeSaveData() {
		var education=document.all.education.value;//最高学历
		var major = document.all.major.value;
		var is_major = document.all.is_major.value;
		if((branch_id.substring(0,3)!="137"||"<%=channel_id%>"=='0C'||"<%=channel_id%>"=='03')
				&& (branch_id.substring(0,3)!="145"||"<%=channel_id%>"=='0C'||"<%=channel_id%>"=='03'
						||"<%=channel_id%>"=='02')){
		if (education == '5'){
			if ("" == major.trim() || "" == is_major.trim()) {
				alert("请录入专业和选择是否相关专业！");
				return;
			}
		}
		}
		//特殊需要近亲属的机构需要校验
		if(isExists=="1"){
			//如果不填写近亲属必须勾选无需填写
			var checked =$("#box1").is(":checked");
			var count=0;
			if (checked==false) {
				var trs = $("tr[name='salesRelatives']");
				//勾选后删除父母/子女/配偶行
				for(var i = trs.length-1; i > -1; i -- ){
					var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
					if (close_relatives_rela != null && close_relatives_rela != '' && close_relatives_rela.trim().length != 0 && "01,02,03".indexOf(close_relatives_rela)>-1){
						count++;
					}
				}
				if (count==0) {
					alert('请勾选/录入父母/配偶/子女近亲属相关信息!');
					return;
				}
			}
		}

		debugger;
		var trs = $("tr[name='salesRelatives']");
		for(var i = 0; i < trs.length; i++){
			var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			//var tr_id = $(trs[i]).find("select[name='close_relatives_id_type']").attr("id");
			//alert(idType_id +":"+ tr_id+","+idType + ":"+close_relatives_id_type +","+idNoVal+":"+close_relatives_id_no);
			var pattern2 = /^[A-Z\s\W]+$/;
			var pattern3 = /[\u4e00-\u9fa5]/;
			if (close_relatives_id_type == "11" && close_relatives_name!=null && close_relatives_name!= "" && close_relatives_name.length > 0) {//修改
				if (!pattern2.test(close_relatives_name) || pattern3.test(close_relatives_name)) {
					alert("亲属姓名不符合港澳居民来往内地通行证（非中国籍）录入格式，只能录入英文名称");
					//document.getElementById("close_relatives_name").value = "";
					return false;
				}
			}
			var pattern = /^(HA|MA)\d{7}$/;
			if (close_relatives_id_type == "11" && close_relatives_id_no!=null && close_relatives_id_no != "" && close_relatives_id_no.length > 0) {//修改
				if (!pattern.test(close_relatives_id_no)) {
					alert("证件号码录入不符合港澳居民来往内地通行证（非中国籍）证件格式");
					//document.getElementById("close_relatives_id_no").value = "";
					return false;
				}
			}
		}
		//a by hbl for L1846 start
		if(channel_id == '04' || channel_id == '01'){
			var is_comprehensive = document.all.is_comprehensive.value;
			var introduce_id = document.all.introduce_id.value;
			var commissioner_code = document.all.commissioner_code.value;
			var introduce_name = document.all.introduce_name.value;
			if(is_comprehensive == '1'){
				if("" == introduce_id && "" == commissioner_code && "" == introduce_name){
					alert("“财险专员代码”、“财险介绍人代码”、“财险介绍人姓名” 不能同时为空，请检查！");
					return;
				}
			}
		}
		// var version_id = document.all.version_id.value;
		//a by hbl for L1846 end
		// L3118
		<%--var rankid = document.all.rank.value;--%>
		<%--var team_id = document.all.team_id.value;--%>
		<%--if(channel_id == '03' ){--%>
		<%--	&lt;%&ndash;var result = checkTeamManagerRanks('<%=locationPath%>/staffInfo_yb.do?method=checkTeamManagerRanks', rank, team_id);&ndash;%&gt;--%>
		<%--	&lt;%&ndash;if (result) {&ndash;%&gt;--%>
		<%--	&lt;%&ndash;	alert("当前组织已有主管，请修改人员所属组织");&ndash;%&gt;--%>
		<%--	&lt;%&ndash;	return;&ndash;%&gt;--%>
		<%--	&lt;%&ndash;}&ndash;%&gt;--%>
		<%--	if(LeaderID!="0000000"){--%>
		<%--		if(version_id=="3240"){--%>
		<%--			if(rankid >= "A06" && rankid<="A10"){--%>
		<%--				document.all.team_id.readOnly='';--%>
		<%--				// alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");--%>
		<%--				alert("当前组织已有主管，请修改人员所属组织!");--%>
		<%--				document.getElementById("team_id").value = "";--%>
		<%--				document.getElementById("team_name").value = "";--%>
		<%--				return;--%>
		<%--			}--%>
		<%--		}--%>
		<%--	}--%>
		<%--	/* 没有主管校验 */--%>
		<%--	if(version_id=="3240"){--%>
		<%--		if(rankid >= "A06" && rankid<="A10"){--%>
		<%--			if(LeaderID2!="0000000"){--%>
		<%--				document.all.team_id.readOnly='';--%>
		<%--				// alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");--%>
		<%--				alert("当前组织已有主管，请修改人员所属组织!");--%>
		<%--				document.getElementById("team_id").value = "";--%>
		<%--				document.getElementById("team_name").value = "";--%>
		<%--				return;--%>
		<%--			}--%>
		<%--		}--%>
		<%--	}--%>
		<%--}--%>


		
		var id_no = document.all.id_no.value;
		var id_type = document.all.id_type.value;
		var team_id = document.all.team_id.value;

		//L1967 银保渠道新增不可多次重复离司后入司 add by ouwendi 2022/09/15 start
		if (id_no != "" && id_type != "" && (channel_id == "03")) {
			var dismissTimes = checkEntryTimes('<%=locationPath%>/staffInfo_yb.do?method=getEntryTimesInfo', id_no, id_type);  //L2352 add by cy 20230926 修改限制
			if (dismissTimes) {
				alert("该人员属于重入司人员，无法入司。");
				return;
			}
		}
		//L1967 银保渠道新增不可多次重复离司后入司 add by ouwendi 2022/09/15 end

		if (id_no != "" && id_type != "" && (channel_id == "01" || channel_id == "05"||channel_id == "04" || channel_id == "08"||channel_id == "09")) {
			// L1728
			/*var isSecondEntry = checkSecondEntry('<%=locationPath%>/publicFuncton.do?method=checkSecondEntry', id_no, id_type);
			if (isSecondEntry) {
				if (confirm("该人员为二次入司，点击确认继续下一步流程")) {
					saveData();
				}
			} else {
				saveData();
			}*/
			var para = "&id_no="+id_no+"&id_type="+id_type+"&channel_id="+channel_id+"&team_id="+team_id;
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doSecondEntry,"");
				doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getSecondEntry"+para,"GET",httpRequest);
			}
		} else {
			saveData();
		}
	}

	function doSecondEntry(str,id){
		var result=str.substring(0,1);
		var content=str.substring(1);

		if (result!="0"){
			i = content.split('#');
			if(result=="1"){
				if (confirm("该人员为二次及以上（第"+i[5]+"次）入司，最近一次于"+i[1]+"至"+i[2]+"在"+i[3]+"分公司"+i[4]+"渠道在职，点击确认继续下一步流程")) {
					saveData();
				}
			}else if(result=="2"){
				alert("该人员为二次及以上（第"+i[5]+"次）入司，最近一次于"+i[1]+"至"+i[2]+"在"+i[3]+"分公司"+i[4]+"渠道在职，从个群渠道离司不满180天，不满足二次入司条件。");
				return;
			}
		} else {
			saveData();
		}
	}
	<%--function checkTeamManagerRanks(locationPath, rank, team_id) {--%>
	<%--	if (isEmpty(team_id)) {--%>
	<%--		return;--%>
	<%--	}--%>
	<%--	if (isEmpty(rank)) {--%>
	<%--		return;--%>
	<%--	}--%>
	<%--	var url = locationPath + "&rank=" + rank + "&team_id=" + team_id;--%>
	<%--	var result = false;--%>
	<%--	$.ajax({--%>
	<%--		type : "get", // 请求方式--%>
	<%--		url : url, // 目标资源--%>
	<%--		dataType : "text", // 服务器响应的数据类型--%>
	<%--		async : false,--%>
	<%--		success : function(data) {--%>
	<%--			if (data.replace(/\s/,'').replace(/\n/,'').substring(0,1) == "<%=ConstResult.AJAX_RESULT_FAILURE%>") {--%>
	<%--				result = true;--%>
	<%--			}--%>
	<%--		},--%>
	<%--		error: function (data) {--%>
	<%--			console.log("checkTeamManagerRanks error........" + data);--%>
	<%--		}--%>
	<%--	});--%>
	<%--	return result;--%>
	<%--}--%>
	function checkEntryTimes(locationPath, id_no, id_type) {
		if (isEmpty(id_no)) {
			return;
		}
		var url = locationPath + "&id_no=" + id_no + "&id_type=" + id_type;
		var result = false;
		$.ajax({
			type : "get", // 请求方式
			url : url, // 目标资源
			dataType : "text", // 服务器响应的数据类型
			async : false,
			success : function(data) {
				if (data.replace(/\s/,'').replace(/\n/,'').substring(0,1) == "<%=ConstResult.AJAX_RESULT_FAILURE%>") {
					result = true;
				}
			},
			error: function (data) {
				console.log("checkEntryTimes error........" + data);
			}
		});
		return result;
	}
 function saveData()
	{
	  //alert("=================前前前");
	   // checkIdNo();
	  //alert("=================后后后")
		if("<%=branch_id%>"=='1000000'){
			alert("总公司不能操作人员预入司，请由分公司操作");	 //by liqi 20190624
			return;
		}	
	  //add liu_yl forL1611服营新军 on 20211228 start
	  	if(team_is_new_army=='1'){
	  		// alert('该四级机构为服营IWP机构，不能预入司');
			alert('人员为非IWP,不能选择IWP组织');
	  		return;
	  	}
	  //add liu_yl forL1611服营新军 on 20211228 end
			dialog_waiting.open("正在保存数据");//锁屏效果 by cyh for L682 on 20181008
         	var id_no=document.all.id_no.value;
			var id_type=document.all.id_type.value;
		
			var branch_id = document.all.branch_id1.value;
			var employ_kind=document.all.employ_kind.value;//用工性质	
			//var sales_type=document.all.sales_type.value;//人员类别 
			var sales_name=document.all.sales_name.value;//人员姓名
			var home_address=document.all.home_address.value;//常住地址
			var education=document.all.education.value;//最高学历
			var major = document.all.major.value;//专业
			var is_major = document.all.is_major.value;//是否相关专业
			var mobile=document.all.mobile.value;//手机
			var birthday=document.all.birthday.value;//人员类别 
			var sex=document.all.sex.value;//人员类别 
			var is_resigned=document.all.is_resigned.value;
			var recommend_id=document.all.recommend_id.value;
			var rank = document.all.rank.value;

			if(channel_id=='01' ){//加上渠道限制  20220907
				var route_type = document.all.route_type.value; //SYY L1864 20220725
				
			}
			if (channel_id == '04'){//加上渠道限制
				var is_full_time_education = document.all.is_full_time_education.value; //L2113 添加是否全日制加上渠道限制
				if(is_full_time_education==""){
					alert("请选择是否全日制!");
					dialog_waiting.close();
					return;
				}
			}
		   if(employ_kind==""){
				alert("请选择用工性质！");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			} 
		   if(rank==""){
			    alert("请选择业务职级!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(id_type==""){
			    alert("请选择证件类型!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(id_no==""){
			    alert("请输入证件号码!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(sales_name==""){
			    alert("请输入人员姓名!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(sex==""){
			    alert("请选择性别!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(birthday==""){
			    alert("请选择出生日期!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(mobile==""){
			    alert("请输入手机号码!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(education==""){
			    alert("请选择学历!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			if(home_address==""){
			    alert("请输入常住地址!");
			    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				return;
			}
			//SYY L1864 20220725
			if(channel_id=='01' ||channel_id=='05' ){
				if(route_type==""){
					alert("请选择所属路线！");
					dialog_waiting.close();//解屏   
					return;
			    }
				if(route_type =='1'){/* 绩优人员  */
					if(parseInt(education)<7){//不满足绩优的学历条件
						alert('人员不符合绩优路线条件，请选择为常规路线。');
						dialog_waiting.close();//解屏   
						return;
					}
				}
				if(route_type =='0'){/* 常规人员  */
					check_recommendRouteType();
					if(c == '0'){
						return;
					}
				}
			}  
			//SYY L1864 20220725
			if(channel_id=="0C"){
				if(document.all.team_id.value==""){
					alert("组织代码不能为空,请录入组织代码!");
				    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
					return;
				}
			}else{
				if(recommend_id==""){
					 alert("推荐人代码不能为空!");
				     dialog_waiting.close();//解屏  by cyh for L682 on 20181008
					 return;
				}else{
					if(recommend_id=="0000000"&&document.all.team_id.value==""){
						alert("推荐人代码为空时,组织代码不能为空,请录入组织代码!");
					    dialog_waiting.close();//解屏  by cyh for L682 on 20181008
						return;
					}
				}
				//a by wzj for L1003 on 2020-8-26 15:48:02 start
				if(channel_id=='04'){
					var service_branch_id = document.all.service_branch_id.value;
					var accreditOrg=$('#accredit_org').val();
					service_branch_id = accreditOrg;
					if(accreditOrg=='') {
						if (confirm("请注意！若不录入派驻机构则无法指定派驻区域（财险），是否返回进行录入？")) {
							dialog_waiting.close();
							return;
						} else {
						}
					}
						
				}
				//a by wzj for L1003 on 2020-8-26 15:48:02 end
				if(channel_id=="03"||channel_id=="02"){
					var service_branch_id = document.all.service_branch_id.value;
					if(service_branch_id==""){
						 alert("服务机构不能为空!");
					     dialog_waiting.close();//解屏  by cyh for L682 on 20181008
						 return;
					}
				}
			}
			
			document.all.birthday.disabled=false;
			document.all.sex.disabled=false;

			if(channel_id=='04' || channel_id=='01' ){
				 var introduce_id=document.all.introduce_id.value;
				var medical_staff=document.all.medical_staff.value;
				var introduce_name = document.all.introduce_name.value; 
				var commissioner_code=document.all.commissioner_code.value;
				var commissioner_name = document.all.commissioner_name.value;
				var is_comprehensive=$('#is_comprehensive').find("option:selected").text();
			    /* var r=confirm('请注意！人员录入的共建队伍信息为：\n是否共建队伍：'+is_comprehensive+'，财险专员代码：'+introduce_id+'，介绍人姓名：'+introduce_name+'。\n是否保存？');   */
			    var r=confirm('请注意！人员录入的共建队伍信息为：\n是否共建队伍：'+is_comprehensive+'，财险专员代码：'+commissioner_code+'，财险专员姓名：'+commissioner_name+'。\n是否保存？');  
				if (r==false){
			    	dialog_waiting.close();
			    	return; 
			      }	
			}
			// a by chengyy for L2624 start
			/*if(channel_id=='01' ||channel_id=='05'){
				$.post('<%=locationPath%>/SalesInfo_gx.do?method=backSurveyCheck',{id_no:id_no,id_type:id_type},function (d){
					var result=eval('('+d+')');
					if(result.status == "3"){
						var r=confirm('该证件号码为司法记录高风险或经商办企业高风险，是否保存！');
						if (r==false){
							dialog_waiting.close();
							return;
						}
					}
				})
			}*/
			// a by chengyy for L2624 end
			//wangpenghui by 20200528
			var url = "<%=locationPath%>/staffInfo_tx.do?method=queryRankInfo";
			var param_url = "<%=locationPath%>/salesInfo_hd.do?method=queryAllowOverAge";
			var team_id = document.all.team_id.value;
			var version_id = document.all.version_id.value; 
			// 绩优/IWP增加 开关  syy for L1864 优化  20220928
			if(channel_id=='01'){
				var route_type = document.all.route_type.value;
				param_url = "<%=locationPath%>/salesInfo_hd.do?method=queryAllowOverAge"+'&route_type=' + route_type ;
			}  
			var age = checkAge(channel_id,birthday,rank,team_id,url,param_url,version_id);
			
			if(!age){
				dialog_waiting.close();//解屏
				return;
			}
			//wangpenghui by 20200528

		   //add by lzy for L1769 20220525 begin
		   //银保渠道，判断是否超龄入司并未审批通过
		   if(channel_id=="03" && (employ_kind=="02" || employ_kind=="2")){
			   var branchIdCurr = "<%=branch_id%>";
			   var oarUrl = "<%=locationPath%>/specialEntry.do?method=checkOverageExists";
			   var hasApprovedOverAge = checkYBOverAge(oarUrl,id_type,id_no,birthday,branchIdCurr);
			   if(!hasApprovedOverAge){
				   dialog_waiting.close();//解屏
				   return;
			   }
		   }
		   //add by lzy for L1769 20220525 end


		if(channel_id=="04"&&recommend_id!='0000000'){
				var recommend_id=document.all.recommend_id.value;
				var rank = document.all.rank.value;
				var team_id = document.all.team_id.value;
				var httpRequest=new HttpRequest();
	       		with(httpRequest){
	            	init();
	            	doSetCallBack(doCheckRanktypeA);
	            	doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getRanktype&rank="+rank+"&team_id="+team_id+"&recommend_id="+recommend_id,"GET",httpRequest);
	       		}
			}else{
				jsSalesInfoCheck();//m by liulei for L1888 20210625
			}
			
	}   
 	function doCheckRanktypeA(resultinfo){
		if(resultinfo!=""){
			alert(resultinfo);
			dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			return ;
		}else{
			jsSalesInfoCheck();//m by liulei for L1888 20210625
		}
	}

	//a by liulei for 江苏机构对接江苏省保险从业人员综合信息平台  start
	function jsSalesInfoCheck(){
		var branchIdCur_JS = "<%=branch_id%>";

		var credentialType = document.all.id_type.value == "01" ? "01" : "99";
		var credentialNo = document.all.id_no.value;
		var userName = encodeURI(encodeURI(document.all.sales_name.value));
		var comCode = branchIdCur_JS;
		if(branchIdCur_JS.substring(0,3) == '132'){
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doCheckJsSalesInfo);
				doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=querySalesFromJS&credentialType="+credentialType+"&credentialNo="+credentialNo
						+"&userName="+userName+"&comCode="+comCode,"GET",httpRequest);
			}
		}else{
			submitMessage();
		}
	}

	function doCheckJsSalesInfo(resultinfo){
		var rtnCode = resultinfo.substring(0,1);
		if(rtnCode=="1"){
			alert("来自江苏省保险从业人员综合信息管理平台：\n"+resultinfo.substring(2));
			dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			return ;
		}else{
			submitMessage();
		}
	}
	//a by liulei for 江苏机构对接江苏省保险从业人员综合信息平台  end

	function submitMessage(){
 		var id_no=document.all.id_no.value;//证件号码
		var id_type=document.all.id_type.value;//证件类型
		var sales_name=document.all.sales_name.value;//人员姓名
		var mobile=document.all.mobile.value;//手机
		/*L1523（协2021-757）关于开发“护航行动”相关信息系统的协办单-人员入司（身份信息真实性三要素验证）-需求规格说明书V1.2  */
		if(id_type=="01"){
		     var httpRequest=new HttpRequest();
	         with(httpRequest){
	             init();
	             doSetCallBack(doCheckThreeElements);
	             doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkThreeElements&id_no="+id_no+"&sales_name="+encodeURIComponent(sales_name,"utf-8")+"&mobile="+mobile,"GET",httpRequest);
	         }
		}else{
 			submitMessage1();
 		}
 	}
 	
 	/*  L1543  add by chaieq  20210924  start*/
	 function checkIdNo(){
	     /* var id_no = document.getElementById("id_no").value;
	     var mobile = document.getElementById("mobile").value;
	     var sales_name = document.getElementById("sales_name").value; */
	     var id_no=document.all.id_no.value;//证件号码
			var sales_name=document.all.sales_name.value;//人员姓名
			var mobile=document.all.mobile.value;//手机
	     if(id_no == null || id_no == ""){
	        alert("请录入证件号码！ ");
	        return;
	   }
	    var myreg = /^[1-9]\d{5}[1-9]\d{3}((0[1-9]|1[0-2]))((0[1-9]|[1|2]\d)|3[0-1])\d{3}(\d|X)$/;
	    if(myreg.test(id_no)){
	         var httpRequest=new HttpRequest();
	         with(httpRequest){
	                 init();
	             doSetCallBack(doGetIdNo,"");
	             doSendResuest("<%=locationPath%>/salesEmployKindChangeAction.do?method=cheackPhoneInfo&id_no="+id_no+"&mobile="+mobile+"&sales_name="+encodeURIComponent(sales_name,"utf-8"),"GET",httpRequest);
	        }
	    }else{
	        alert("证件号码错误！");
	       return;
	   }
	}
 	/* 校验是否上传手机资料证明 */
 	var exitPhonePic = 0;
 	function doGetIdNo(str,id){
 	  	var result=str.substring(0,1);
 		var content=str.substring(1);
 		if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
 			//alert("该人员手机号证明资料已存在！");
 			exitPhonePic = 1;
 			//return;
 		}
 	}
 	/*  L1543  add by chaieq  20210924  end*/

 	function doCheckThreeElements(str,id){
 		debugger;
 		if(str!="1101"){
 			checkIdNo();
 			/*  L1543  add by chaieq  20210924  start*/
 			setTimeout (function(){
			  if(exitPhonePic ==1){// 三要素不一致但是手机资料图片已上传，正常入司
	 	    		submitMessage1();
	 	    		//return;
	 	    	}else{
			 			if(str=="1102"){
			 				alert("手机号已实名，手机号和姓名一致，身份证不一致，请在【手机号证明资料管理】菜单上传手机号证明资料");
			 				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			 	 			return;
			 			}else if(str=="1103"){
			 				alert("手机号已实名，手机号和证件号一致，姓名不一致，请在【手机号证明资料管理】菜单上传手机号证明资料");
			 				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			 	 			return;
			 			}else if(str=="1104"){
			 				alert("手机号实名信息，身份证、姓名均不一致，请在【手机号证明资料管理】菜单上传手机号证明资料");
			 				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			 	 			return;
			 			}else{
			 				alert("其他不一致，请在【手机号证明资料管理】菜单上传手机号证明资料");
			 				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
			 	 			return;
			 			}
	 		          }
			},4000);
 	    	
 	    	/*  L1543  add by chaieq  20210924  end*/
 		}else{
 			submitMessage1();
 		}
 		
 	}
 	
 	function submitMessage1(){
 		var id_no=document.all.id_no.value;
		var id_type=document.all.id_type.value;
		var branch_id = document.all.branch_id1.value;
		var employ_kind=document.all.employ_kind.value;//用工性质	
		//var sales_type=document.all.sales_type.value;//人员类别 
		var sales_name=document.all.sales_name.value;//人员姓名
		var home_address=document.all.home_address.value;//常住地址
		var education=document.all.education.value;//最高学历
		var mobile=document.all.mobile.value;//手机
		var birthday=document.all.birthday.value;//人员类别 
		var sex=document.all.sex.value;//人员类别 
		var is_resigned=document.all.is_resigned.value;
		var recommend_id=document.all.recommend_id.value;
		var rank = document.all.rank.value;
 		var version_id = document.all.version_id.value;
		//var medical_staff=document.all.medical_staff.value;
		/*A by z_gb on L1006 20191120 职级校验  */
	     var httpRequest=new HttpRequest();
         with(httpRequest){
             init();
             doSetCallBack(doCheckVersionID);
             doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getCheckVersionID&version_id="+version_id+"&recommend_id="+recommend_id,"GET",httpRequest);
         }
 	}
 	
 	
 	 function doCheckVersionID(str,id){
 		 var result=str.substring(0,1);
   		 var content=str.substring(1);
 	     if(result!= "<%=ConstResult.AJAX_RESULT_FAILURE%>"){  
 	             alert(content);
 	    	 	 dialog_waiting.close();
 			     return;
         }else{
        	/**
 			 * gb.z
 			 * 获取免培训证件号码 若果存在,状态生效可不调用人保e学培训接口
 			 * on 20191211
 			 */
        	 var id_no=document.all.id_no.value;
 			 var id_type=document.all.id_type.value;
        	 var httpRequest=new HttpRequest();
	         with(httpRequest){
	           init();
	           doSetCallBack(chechPushID,"");
	           doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=chechPushID&id_type="+id_type+"&id_no="+id_no,"GET",httpRequest);
	        }
         }
 	  }

	  //L1536 2021/08/03 caoh add 校验新增近亲属的必填
 	  function checkCloseRelativesInfo(){
		  var trs = $("tr[name='salesRelatives']");
		  for(var i = 0; i < trs.length; i++){
			  var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			  var close_relatives_sex = $($(trs[i]).find("select[name='close_relatives_sex']")[0]).val();
			  var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			  var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			  var close_relatives_birthday = $($(trs[i]).find("input[name='close_relatives_birthday']")[0]).val();
			  var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
			  if(close_relatives_name == null || close_relatives_name == '' || close_relatives_name.trim().length == 0){
				  var row = $($(trs[i]).find("label")[0]).html();
				  alert("第"+row+"行记录请录入亲属姓名!");
				  dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				  //$($(trs[i]).find("input[name='close_relatives_name']")[0]).focus().css("border-color","red");
				  return false;
			  }
			  if(close_relatives_sex == null || close_relatives_sex == '' || close_relatives_sex.trim().length == 0){
				  var row = $($(trs[i]).find("label")[0]).html();
				  alert("第"+row+"行记录请选择亲属性别!");
				  dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				 // $($(trs[i]).find("input[name='close_relatives_sex']")[0]).focus().css("border-color","red");;
				  return false;
			  }
			  if(close_relatives_id_no == null || close_relatives_id_no == '' || close_relatives_id_no.trim().length == 0){
				  var row = $($(trs[i]).find("label")[0]).html();
				  alert("第"+row+"行记录请录入证件号码!");
				  dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				 // $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).focus().css("border-color","red");;
				  return false;
			  }
			  if(close_relatives_birthday == null || close_relatives_birthday == '' || close_relatives_birthday.trim().length == 0){
				  var row = $($(trs[i]).find("label")[0]).html();
				  alert("第"+row+"行记录请录入出生日期!");
				  dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				  //$($(trs[i]).find("input[name='close_relatives_birthday']")[0]).focus().css("border-color","red");;
				  return false;
			  }
			  if(close_relatives_rela == null || close_relatives_rela == '' || close_relatives_rela.trim().length == 0){
				  var row = $($(trs[i]).find("label")[0]).html();
				  alert("第"+row+"行记录请选择与销售人员关系!");
				  dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				 // $($(trs[i]).find("input[name='close_relatives_rela']")[0]).focus().css("border-color","red");;
				  return false;
			  }
		  }
	  }
 	
 	function chechPushID(str,id){

		//test_ch
		/*********************************************L1536 2021/08/02 caoh add start**********************************************************/
		//校验新增近亲属的必填
		// if(!checkCloseRelativesInfo()){
		// 	return;
		// };
		var trs = $("tr[name='salesRelatives']");
		//勾选无需填写父母/子女/配偶后，不能填写这三类近亲属
		var checked =$("#box1").is(":checked");
		for(var i = 0; i < trs.length; i++){
			var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			var close_relatives_sex = $($(trs[i]).find("select[name='close_relatives_sex']")[0]).val();
			var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			var close_relatives_birthday = $($(trs[i]).find("input[name='close_relatives_birthday']")[0]).val();
			var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
			//勾选无需填写父母/子女/配偶后，不能填写这三类近亲属
			if (close_relatives_rela != null && close_relatives_rela != '' && close_relatives_rela.trim().length != 0 && checked==true && "01,02,03".indexOf(close_relatives_rela)>-1){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("请正确勾选/录入父母/配偶/子女近亲属相关信息!");
				dialog_waiting.close();//解屏
				return false;
			}
			if(close_relatives_name = null || close_relatives_name == '' || close_relatives_name.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入亲属姓名!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_name']")[0]).focus().css("border-color","red");
				return false;
			}
			if(close_relatives_sex = null || close_relatives_sex == '' || close_relatives_sex.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请选择亲属性别!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_sex']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_id_no = null || close_relatives_id_no == '' || close_relatives_id_no.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入证件号码!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_id_no']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_birthday = null || close_relatives_birthday == '' || close_relatives_birthday.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入出生日期!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_birthday']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_rela = null || close_relatives_rela == '' || close_relatives_rela.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请选择近亲属关系!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_rela']")[0]).focus().css("border-color","red");;
				return false;
			}
		}
		//var trs = $("tr[name='salesRelatives']");
		var salesRelativeJson = "";
		for(var i = 0; i < trs.length; i++){
			var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			var close_relatives_sex = $($(trs[i]).find("select[name='close_relatives_sex']")[0]).val();
			var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			var close_relatives_birthday = $($(trs[i]).find("input[name='close_relatives_birthday']")[0]).val();
			var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
			salesRelativeJson += (close_relatives_name+":"+close_relatives_sex+":"+close_relatives_id_type+":"+close_relatives_id_no+":");
			salesRelativeJson += (close_relatives_birthday+":"+close_relatives_rela);
			salesRelativeJson += (";");
		}
		/*********************************************L1536 2021/08/02 caoh add end**********************************************************/
		var result=str.substring(0,1);
  		 var content=str.substring(1);
  		 debugger;
	     if(result!= "<%=ConstResult.AJAX_RESULT_FAILURE%>"){
	        	var is_resigned=document.all.is_resigned.value;
	        	var is_comprehensive="";
		    	 if(channel_id=="04" || channel_id=="01"){
		    		 is_comprehensive=document.getElementById("is_comprehensive").value;
		    	 }
			 	//L1536 2021/08/02 caoh add  salesRelativeJson
			    document.all.rank.disabled=false;
				doSubmit(document.forms[0],'<%=locationPath%>/salesInfo_hd.do?method=insertSalesPrepare&is_resigned='+is_resigned+'&salesRelativeJson='+salesRelativeJson+'&is_comprehensive='+is_comprehensive);
				document.all.modify22.className='disables';
	     }else{
	    	 var is_resigned=document.all.is_resigned.value;
	    	 var is_comprehensive="";
	    	 if(channel_id=="04"  || channel_id=="01" ){
	    		 is_comprehensive=document.getElementById("is_comprehensive").value;
	    	 }
	     	 if(version_id=="3160"||version_id=="4110"||channel_id=="0C"){
				 //L1536 2021/08/02 caoh add  salesRelativeJson
				 document.all.rank.disabled=false;
	 			doSubmit(document.forms[0],'<%=locationPath%>/salesInfo_hd.do?method=insertSalesPrepare&is_resigned='+is_resigned+'&salesRelativeJson='+salesRelativeJson+'&is_comprehensive='+is_comprehensive);
	 			document.all.modify22.className='disables';
	    		}else{
	    			var rank = document.all.rank.value;
	    	 		var version_id = document.all.version_id.value;
		 	   	 	var id_no=document.all.id_no.value;
		 			var id_type=document.all.id_type.value;
		 			var is_resigned=document.all.is_resigned.value;
		 			var is_comprehensive=""; 
			    	 if(channel_id=="04" || channel_id=="01" ){
			    		 is_comprehensive=document.getElementById("is_comprehensive").value;
			    	 }
	    			if((rank=="M01"||rank=="G01" ||rank=="H01" ||"03"==channel_id)&&rainInfo==1){  //L2352 a by cy 20231019 银保所有职级都需要看是否完成岗前培训
	    				var httpRequest=new HttpRequest();
	    		        with(httpRequest){
	    		           init();
	    		           doSetCallBack(doPISChick,"");
	    		           doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=chickPISData&id_type="+id_type+"&id_no="+encodeURI(encodeURI(id_no)),"GET",httpRequest);
	    		        }
	    			}else{

						//L1536 2021/08/02 caoh add  salesRelativeJson
						document.all.rank.disabled=false;
	    				doSubmit(document.forms[0],'<%=locationPath%>/salesInfo_hd.do?method=insertSalesPrepare&is_resigned='+is_resigned+'&salesRelativeJson='+salesRelativeJson+'&is_comprehensive='+is_comprehensive);
	    				document.all.modify22.className='disables';
	    			}
	    		}
	     }
 	}
 	 
	function doPISChick(str,id){
		/*********************************************L1536 2021/08/02 caoh add start**********************************************************/
		//校验近亲属信息
		// if(!checkCloseRelativesInfo()){
		// 	return;
		// };
		var trs = $("tr[name='salesRelatives']");
		for(var i = 0; i < trs.length; i++){
			var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			var close_relatives_sex = $($(trs[i]).find("select[name='close_relatives_sex']")[0]).val();
			var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			var close_relatives_birthday = $($(trs[i]).find("input[name='close_relatives_birthday']")[0]).val();
			var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
			if(close_relatives_name = null || close_relatives_name == '' || close_relatives_name.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入亲属姓名!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_name']")[0]).focus().css("border-color","red");
				return false;
			}
			if(close_relatives_sex = null || close_relatives_sex == '' || close_relatives_sex.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请选择亲属性别!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_sex']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_id_no = null || close_relatives_id_no == '' || close_relatives_id_no.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入证件号码!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_id_no']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_birthday = null || close_relatives_birthday == '' || close_relatives_birthday.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请录入出生日期!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_birthday']")[0]).focus().css("border-color","red");;
				return false;
			}
			if(close_relatives_rela = null || close_relatives_rela == '' || close_relatives_rela.trim().length == 0){
				var row = $($(trs[i]).find("label")[0]).html();
				alert("第"+row+"行记录请选择与销售人员关系!");
				dialog_waiting.close();//解屏  by cyh for L682 on 20181008
				//$($(trs[i]).find("input[name='close_relatives_rela']")[0]).focus().css("border-color","red");;
				return false;
			}
		}
		var salesRelativeJson = "";
		//var trs = $("tr[name='salesRelatives']");
		for(var i = 0; i < trs.length; i++){
			var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
			var close_relatives_sex = $($(trs[i]).find("select[name='close_relatives_sex']")[0]).val();
			var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
			var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
			var close_relatives_birthday = $($(trs[i]).find("input[name='close_relatives_birthday']")[0]).val();
			var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
			salesRelativeJson += (close_relatives_name+":"+close_relatives_sex+":"+close_relatives_id_type+":"+close_relatives_id_no+":");
			salesRelativeJson += (close_relatives_birthday+":"+close_relatives_rela);
			salesRelativeJson += (";");
		}
		/*********************************************L1536 2021/08/02 caoh add end**********************************************************/
		var result=str.substring(0,1);
   		var content=str.substring(1);
		if (result=="<%=ConstResult.AJAX_RESULT_FAILURE%>"){
		       alert(content);
		       document.all.modify22.className='btn';
			    document.all.sex.disabled=true;
				document.all.birthday.disabled=true
		       dialog_waiting.close();
		       return;
        }else{
        	var is_comprehensive="";
	    	 if(channel_id=="04" || channel_id=="01"){
	    		 is_comprehensive=document.getElementById("is_comprehensive").value;
	    	 }
        	var is_resigned=document.all.is_resigned.value;
        	//L1536 2021/08/02 caoh add  salesRelativeJson
			document.all.rank.disabled=false;
			doSubmit(document.forms[0],'<%=locationPath%>/salesInfo_hd.do?method=insertSalesPrepare&is_resigned='+is_resigned+'&salesRelativeJson='+salesRelativeJson+'&is_comprehensive='+is_comprehensive);
			document.all.modify22.className='disables';
		}
	}
 
	 function initJSP(){

	   var resultHR = '${resultHR}';
	   if(resultHR!=""){
	   alert(resultHR);
	   }
	   var result=vTemp.split("&");
	   var resultName= '${resultName}'; //M by wang_gq for 集团工号
	   debugger;
	   if(result[0]=="false"){  
	      alert("人员代码生成失败，"+result[1]+"!");
	      dialog_waiting.close();//解屏  by cyh for L682 on 20181008
	      window.location.href="javascript:history.go(-1)";
	      return;
	   }else if(result[0]=="temp"){
	      alert(resultName+"暂存成功!");
	      dialog_waiting.close();//解屏  by cyh for L682 on 20181008
	      document.all.tempHidden.value="";
	   }else if(result[0]=="true"){
	      alert("暂存失败!");
	      dialog_waiting.close();//解屏  by cyh for L682 on 20181008
	      window.location.href="javascript:history.go(-1)";
	      return;
	   }  
	   if(channel_id=="0C"){
		   document.all.recommend_id.readOnly=true;
		   document.all.team_id.readOnly=false;
	   }
		var input = document.getElementsByTagName('input');
		for(var i=0;i<input.length;i++){
			if(input[i].value==null){
				input[i].value=='';
			}
		}
		var select = document.getElementsByTagName('select');
		for(var i=0;i<select.length;i++){
			if(select[i].value==null){
				select[i].value=='';
			}
		}
		//add by guo_rl for L1070 2020-2-12
   		if((channel_id=="03")||(channel_id=="02")){
 			document.getElementById("service_branch_id1").style.display="";
 	   		document.getElementById("service_branch_id2").style.display="";
  		}
   		if(message=="message")
   		{
   			document.getElementById("message").innerHTML=resultinfo;
   			/* add liu_yl forL1611服营新军 on 20220411 start */
   			if((channel_id=="05")||(channel_id=="01")){
   				var salescoderesults = '${salescoderesults}';
   	   		   	var salescode = '${salescode}';
   	   		   	if('true' == salescoderesults && 'null' != salescode && '' != salescode){
   	   		  		getCombinescore(salescode,salescode)
   	   		   	}
   			}
   		 /* add liu_yl forL1611服营新军 on 20220411 end */
   		}
   		//综金L2113 增加二次入司 提示上次离职渠道及时间
		 var dismissInfo = '${dismissInfo}';
		 if (channel_id == "04" && dismissInfo != "") {
		 	alert(dismissInfo);
		 }
   		
	}  
	 /* add liu_yl forL1611服营新军 on 20220411 start */
	 function getCombinescore(id,params){
	 		var sales_id = id;
	 		var sales_code = params;
			var url = "<%=locationPath%>/salesInfo_hd.do?method=getSalesCombinescore"
								+"&sales_id="+sales_id
								+"&sales_code="+sales_code;
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doCombinescore,"");
				doSendResuest(url,"GET",httpRequest);
			}
	 	}
	 	function doCombinescore(str,id){
	 		var result=str.substring(0,1);
		   	var content=str.substring(1);
			if(result!="1"){
             	alert(content);
			}
	 	}
	 	/* add liu_yl forL1611服营新军 on 20220411 end */
	 function getName(){

			var recommend_id=document.all.recommend_id.value;
			var recommend_name=document.all.recommend_name.value;
			var branch_id=document.all.branch_id.value;
			
			if(recommend_id==""){
				if(channel_id=="0C"){
					return;
				}else{
					alert("推荐人代码不能为空！");
					return;
				}
			}
			if(recommend_id=="0000000"){
				document.all.recommend_name.value="";
				document.all.team_id.value="";	
				document.all.team_name.value="";
				if(channel_id=="04"){
	   				document.getElementById("is_comprehensive").value="0";
		   			document.getElementById("is_comprehensive").disabled=false;
					document.all.introduce_id.value="";
					document.all.introduce_name.value=""; 
		   			document.all.commissioner_code.value="";
					document.all.commissioner_name.value="";
					//m by hbl for L1846 start 
					 document.all.introduce_id.value="";
					 document.all.introduce_id.readOnly=true; 
					 document.all.introduce_name.value="";
					 document.all.introduce_name.readOnly=true;
					//m by hbl for L1846 end 
	   			}
				if( channel_id=="01"){
					document.getElementById("is_comprehensive").value="0";
					document.all.introduce_id.value = "";
					document.all.introduce_id.readOnly = true;
					document.all.introduce_name.value = "";
					document.all.introduce_name.readOnly = true;
					document.all.commissioner_code.value="";
					document.all.commissioner_code.readOnly=true;
					document.all.commissioner_name.value="";
					document.getElementById("is_comprehensive").disabled = false;
					document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
					document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
					document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
					document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
					document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
					document.getElementById("introduce_id").style.backgroundColor = "#efefef";
					document.getElementById("introduce_name").style.backgroundColor = "#efefef";
					document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
				}
				var prdCodeR=document.getElementById("rank");
				removeSel(prdCodeR);
				document.all.team_id.readOnly=false;
				return true;
			}
			if(recommend_id!="0000000"&&recommend_id!=""){
				recommend_type="2";
			}else{
				recommend_type="1";
			}
			if(isNumber(recommend_id)==false){
				alert("推荐人代码输入错误！");
				document.all.recommend_id.value="";	
				document.all.recommend_name.value="";
				document.all.team_id.value="";	
				document.all.team_name.value="";
				document.all.rank.value="";
				document.all.rank.disabled = false;
				if(channel_id=="04"){
	   				document.getElementById("is_comprehensive").value="0";
		   			document.getElementById("is_comprehensive").disabled=false;
					/* document.all.introduce_id.value="";
					document.all.introduce_name.value="";  */
		   			document.all.commissioner_code.value="";
					document.all.commissioner_name.value="";
	   			}
				if( channel_id=="01"){
					document.getElementById("is_comprehensive").value="0";
					document.all.introduce_id.value = "";
					document.all.introduce_id.readOnly = true;
					document.all.introduce_name.value = "";
					document.all.introduce_name.readOnly = true;
					document.all.commissioner_code.value="";
					document.all.commissioner_code.readOnly=true;
					document.all.commissioner_name.value="";
					document.getElementById("is_comprehensive").disabled = false;
					document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
					document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
					document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
					document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
					document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
					document.getElementById("introduce_id").style.backgroundColor = "#efefef";
					document.getElementById("introduce_name").style.backgroundColor = "#efefef";
					document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
				}
				return;
			}
			 if(checkIsNum(recommend_id,<%=LengthConst.LENGTH_OF_SALESCODE%>)==false){
				alert("推荐人代码输入错误！");
				document.all.recommend_id.value="";
				document.all.recommend_name.value="";	
				document.all.team_id.value="";	
				document.all.team_name.value="";
				document.all.rank.value="";
				 document.all.rank.disabled = fasle;
				if(channel_id=="04"){
	   				document.getElementById("is_comprehensive").value="0";
		   			document.getElementById("is_comprehensive").disabled=false;
					/* document.all.introduce_id.value="";
					document.all.introduce_name.value="";  */
		   			document.all.commissioner_code.value="";
					document.all.commissioner_name.value="";
	   			}
				 if( channel_id=="01"){
					 document.getElementById("is_comprehensive").value="0";
					 document.all.introduce_id.value = "";
					 document.all.introduce_id.readOnly = true;
					 document.all.introduce_name.value = "";
					 document.all.introduce_name.readOnly = true;
					 document.all.commissioner_code.value="";
					 document.all.commissioner_code.readOnly=true;
					 document.all.commissioner_name.value="";
					 document.getElementById("is_comprehensive").disabled = false;
					 document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
					 document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
					 document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
					 document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
					 document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
					 document.getElementById("introduce_id").style.backgroundColor = "#efefef";
					 document.getElementById("introduce_name").style.backgroundColor = "#efefef";
					 document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
				 }
				return;
			}
			 if(recommend_id!="0000000"&&recommend_id!=""){
				 checkPerformanceSwitch(recommend_id,"");
			 }

			var httpRequest=new HttpRequest();
			var rank=document.all.rank.value;
	 		with(httpRequest){
	     		init();
					doSetCallBack(doGetName,"");
	      		doSendResuest("<%=locationPath%>/staffInfo_tx.do?method=getRecommendName_tx&recommend_id="+recommend_id+"&branch_id="+branch_id+"&rank="+rank,"GET",httpRequest);
	     	}
		 //add zengzf by L2366银保入司优化推荐人不能预离司校验
		 if(channel_id=="03")
		 {
			 checkRecommendidPredismiss(recommend_id,channel_id);
		 }
		}
	 function doGetName(str,id){
	    	var result=str.substring(0,1);
	   		var content=str.substring(1);
	   		var i = new Array();
	   		var type = id;    
	   		var recommend_name=document.all.recommend_name.value
			if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
				i = content.split('#');
				if(i[0]!=null){
					document.all.recommend_name.value=i[0];
					document.all.recommend_name.readOnly=true;
				}
				if(i[1]!=null){
					recommend_team_id = i[1];
					document.all.team_id.value = i[1];
					document.all.team_id.readOnly = false;
				}
				if(i[2]!=null){
					document.all.team_name.value=i[2];
					document.all.team_name.readOnly=true;
				}
				if(i[9]!=null){
					rankT=i[9];
				}
				if(channel_id=="04" ){
					if(i[10]!=null&&i[10]!=""){
                            document.getElementById("is_comprehensive").value=i[10];
                            document.getElementById("is_comprehensive").disabled=true;
                            //m by hbl for L1846 start
                            document.all.introduce_id.value="";
                            document.all.introduce_id.readOnly=false;
                            document.all.introduce_name.value="";
                            document.all.introduce_name.readOnly=false;
                            //m by hbl for L1846 end
					}else{
						document.getElementById("is_comprehensive").value="0";
						//document.getElementById("is_comprehensive").disabled=true;
						//m by hbl for L1846 start 
					 	document.all.introduce_id.value="";
					 	document.all.introduce_id.readOnly=true; 
					 	document.all.introduce_name.value="";
					 	document.all.introduce_name.readOnly=true;
						document.getElementById("is_comprehensive").disabled=false;
						//m by hbl for L1846 end 
					}
					if(i[11]!=null){
						/* document.all.introduce_id.value=i[11];
						document.all.introduce_id.readOnly=true; */
						document.all.commissioner_code.value=i[11];
						document.all.commissioner_code.readOnly=true;
					}
					if(i[12]!=null){
						/* document.all.introduce_name.value=i[12];
						document.all.introduce_name.readOnly=true; */
						document.all.commissioner_name.value=i[12];
						document.all.commissioner_name.readOnly=true;
					}
				}
				if( channel_id=="01"){
					if(i[10]!=null&&i[10]!=""){
						if(i[10] == '1'){
							document.getElementById("is_comprehensive").value=i[10];
							document.getElementById("is_comprehensive").disabled=true;

							document.all.introduce_id.value="";
							document.all.introduce_id.readOnly=false;
							document.all.introduce_name.value="";
							document.all.introduce_name.readOnly=false;
							document.all.commissioner_code.readOnly=true;
							document.all.commissioner_name.readOnly=true;

							if(i[11]!=null){
								document.all.commissioner_code.value=i[11];
								document.all.commissioner_code.readOnly=true;
							}
							if(i[12]!=null){
								document.all.commissioner_name.value=i[12];
								document.all.commissioner_name.readOnly=true;
							}

							document.getElementById("introduce_id").style.backgroundColor = "";
							document.getElementById("introduce_name").style.backgroundColor = "";
							document.getElementById("commissioner_code").removeAttribute('disabled');
							document.getElementById("introduce_id").removeAttribute('disabled');
							document.getElementById("introduce_name").removeAttribute('disabled');
							document.getElementById("commissioner_name").removeAttribute('disabled');
						}else{
							document.getElementById("is_comprehensive").value="0";
							document.all.introduce_id.value = "";
							document.all.introduce_id.readOnly = true;
							document.all.introduce_name.value = "";
							document.all.introduce_name.readOnly = true;
							document.all.commissioner_code.value="";
							document.all.commissioner_code.readOnly=true;
							document.all.commissioner_name.value="";
							document.getElementById("is_comprehensive").disabled = false;

							document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
							document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
							document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
							document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');

							document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
							document.getElementById("introduce_id").style.backgroundColor = "#efefef";
							document.getElementById("introduce_name").style.backgroundColor = "#efefef";
							document.getElementById("commissioner_name").style.backgroundColor = "#efefef";

						}

					}else{
						document.getElementById("is_comprehensive").value="0";
						document.all.introduce_id.value = "";
						document.all.introduce_id.readOnly = true;
						document.all.introduce_name.value = "";
						document.all.introduce_name.readOnly = true;
						document.all.commissioner_code.value="";
						document.all.commissioner_code.readOnly=true;
						document.all.commissioner_name.value="";
						document.getElementById("is_comprehensive").disabled = false;

						document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');

						document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
						document.getElementById("introduce_id").style.backgroundColor = "#efefef";
						document.getElementById("introduce_name").style.backgroundColor = "#efefef";
						document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
					}

				}

				getTeam();
				//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 start
				getAccreditOrg($('#recommend_id').val(),1);
				//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 end
			}else{
		   		if(content=="NotSameBranch"){
		   			alert("该推荐人不存在！");
		   			document.all.recommend_id.value="";
		   			document.all.recommend_id.readOnly=false;//新加
		   			document.all.recommend_name.value="";
		   			document.all.team_id.value="";
		   			document.all.team_name.value="";
		   			if(channel_id=="04"){
		   				document.getElementById("is_comprehensive").value="0";
			   			document.getElementById("is_comprehensive").disabled=false;
						/* document.all.introduce_id.value="";
						document.all.introduce_name.value=""; */
			   			document.all.commissioner_code.value="";
						document.all.commissioner_name.value="";
		   			}
					if( channel_id=="01"){
						document.getElementById("is_comprehensive").value="0";
						document.all.introduce_id.value = "";
						document.all.introduce_id.readOnly = true;
						document.all.introduce_name.value = "";
						document.all.introduce_name.readOnly = true;
						document.all.commissioner_code.value="";
						document.all.commissioner_code.readOnly=true;
						document.all.commissioner_name.value="";
						document.getElementById("is_comprehensive").disabled = false;
						document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
						document.getElementById("introduce_id").style.backgroundColor = "#efefef";
						document.getElementById("introduce_name").style.backgroundColor = "#efefef";
						document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
					}
		// 			document.all.recommend_id.focus();
		   		}else
		   		if(content=="NotExist"){
		   			alert("推荐人代码输入错误！");
		   			document.all.recommend_id.value="";
		   			document.all.recommend_id.readOnly=false;//新加
		   			document.all.recommend_name.value="";
		   			document.all.team_id.value="";
		   			document.all.team_name.value="";
		   			if(channel_id=="04"){
		   				document.getElementById("is_comprehensive").value="0";
			   			document.getElementById("is_comprehensive").disabled=false;
						/* document.all.introduce_id.value="";
						document.all.introduce_name.value=""; */
			   			document.all.commissioner_code.value="";
						document.all.commissioner_name.value="";
		   			}
					if( channel_id=="01"){
						document.getElementById("is_comprehensive").value="0";
						document.all.introduce_id.value = "";
						document.all.introduce_id.readOnly = true;
						document.all.introduce_name.value = "";
						document.all.introduce_name.readOnly = true;
						document.all.commissioner_code.value="";
						document.all.commissioner_code.readOnly=true;
						document.all.commissioner_name.value="";
						document.getElementById("is_comprehensive").disabled = false;
						document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
						document.getElementById("introduce_id").style.backgroundColor = "#efefef";
						document.getElementById("introduce_name").style.backgroundColor = "#efefef";
						document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
					}
					//document.all.recommend_id.focus();
		   		}else
		   		if(content == "NotStat"){
		   			alert("此人已解约,请重新输入！");
		   			document.all.recommend_id.value="";
		   			document.all.recommend_id.readOnly=false;//新加
		   			document.all.recommend_name.value="";
		   			document.all.team_id.value="";
		   			document.all.team_name.value="";
		   			if(channel_id=="04"){
		   				document.getElementById("is_comprehensive").value="0";
			   			document.getElementById("is_comprehensive").disabled=false;
						/* document.all.introduce_id.value="";
						document.all.introduce_name.value=""; */
			   			document.all.commissioner_code.value="";
						document.all.commissioner_name.value="";
		   			}
					if( channel_id=="01"){
						document.getElementById("is_comprehensive").value="0";
						document.all.introduce_id.value = "";
						document.all.introduce_id.readOnly = true;
						document.all.introduce_name.value = "";
						document.all.introduce_name.readOnly = true;
						document.all.commissioner_code.value="";
						document.all.commissioner_code.readOnly=true;
						document.all.commissioner_name.value="";
						document.getElementById("is_comprehensive").disabled = false;
						document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
						document.getElementById("introduce_id").style.backgroundColor = "#efefef";
						document.getElementById("introduce_name").style.backgroundColor = "#efefef";
						document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
					}
		// 			document.all.recommend_id.focus();
		   		}else{
		   			alert("推荐人信息有误，请核对！");
		   			document.all.recommend_id.value="";
		   			document.all.recommend_id.readOnly=false;//新加
		   			document.all.recommend_name.value="";
		   			document.all.team_id.value="";
		   			document.all.team_name.value="";
		   			if(channel_id=="04"){
		   				document.getElementById("is_comprehensive").value="0";
			   			document.getElementById("is_comprehensive").disabled=false;
						/* document.all.introduce_id.value="";
						document.all.introduce_name.value=""; */
			   			document.all.commissioner_code.value="";
						document.all.commissioner_name.value="";
		   			}
					if( channel_id=="01"){
						document.getElementById("is_comprehensive").value="0";
						document.all.introduce_id.value = "";
						document.all.introduce_id.readOnly = true;
						document.all.introduce_name.value = "";
						document.all.introduce_name.readOnly = true;
						document.all.commissioner_code.value="";
						document.all.commissioner_code.readOnly=true;
						document.all.commissioner_name.value="";
						document.getElementById("is_comprehensive").disabled = false;
						document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
						document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
						document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
						document.getElementById("introduce_id").style.backgroundColor = "#efefef";
						document.getElementById("introduce_name").style.backgroundColor = "#efefef";
						document.getElementById("commissioner_name").style.backgroundColor = "#efefef";
					}
		   		}
			}
	    }
	  <%-- function getIntroduceName(){
		 var introduce_id = document.all.introduce_id.value;
		 if(introduce_id==""){
			 alert("请输入财险专员代码！");
	        	document.all.introduce_id.value = "";
	        	document.all.introduce_name.value = "";
				return;
		 }
		 if(channel_id=="04"){
			 var httpRequest=new HttpRequest();
	         	with(httpRequest){
			     	init();
			     	doSetCallBack(doGetIntroduceName,""); 
			     	doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getIntroduceName&introduce_id="+introduce_id,"GET",httpRequest);
		   		} 
		 }
	 } 
	 
	 
	  function doGetIntroduceName(str,id){
		 var result=str.substring(0,1);
		 if(str=="<%=ConstResult.AJAX_RESULT_FAILURE%>"){
	  		   alert("录入的财险专员代码不存在，请重新输入！");
	  	        	document.all.introduce_id.value = "";
	  	        	document.all.introduce_name.value = "";
					return;
	     	}
		 var result=str.substring(0,1);
   		 var content=str.substring(1);
   		 var i = new Array();
		 if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
			 i = content.split('&');
			 if(i[1]!=null){
				 document.all.introduce_name.value=i[1];
				 document.all.introduce_name.readOnly=true;
			 }
		 }
	 }  --%>
	 //a by hbl for L1846 start
	 function checkIntroduceId(){
		 var introduce_id = document.all.introduce_id.value;
		 //L2284修改校验信息和提示信息
		 var pattern = /^[\w]+$/;
		 if(!pattern.test(introduce_id)){//只能存在数字和字母
			 document.all.introduce_id.value = "";
			 alert("“财险介绍人代码”录入的内容只能是数字或字母，请重新输入！");
		 }
		 if((introduce_id.length)>50){//长度小于50
			 document.all.introduce_id.value = "";
			 alert("录入长度不能超过50个字符，请重新输入！");
		 }		 
	 }
	 
	 function checkIntroduceName(){
		 var introduce_name = document.all.introduce_name.value;
		 //L2284修改校验信息和提示信息
		 var pattern = /^[\u4e00-\u9fa5a-zA-Z·]+$/;
		 if(!pattern.test(introduce_name)){//“财险介绍人姓名”录入的内容只能是汉字或“·”或字母或这3者的任一组合
	   			document.all.introduce_name.value = "";
	   			 alert("“财险介绍人姓名”录入的内容只能是汉字或字母，请重新输入！");
	   		 }
		 if((introduce_name.length)>50){//长度小于50
			 document.all.introduce_name.value = "";
			 alert("录入长度不能超过50个字符，请重新输入！");
		 }		 
	 }
	//a by hbl for L1846 end
	 
	 function getCxzyName(){
		 var commissioner_code = document.all.commissioner_code.value;
		 var is_comprehensive =  document.getElementById("is_comprehensive").value
		 if(commissioner_code=="" && is_comprehensive != "1"){
			 alert("请输入财险专员代码！");
	        	document.all.commissioner_code.value = "";
	        	document.all.commissioner_name.value = "";
				return;
		 }
		 //l2284个险增加财险专员字段
		 if((channel_id=="04"/* || channel_id=="01"*/) && is_comprehensive != "1"){
			 var httpRequest=new HttpRequest();
	         	with(httpRequest){
			     	init();
			     	doSetCallBack(doGetcommissionerName,""); 
			     	doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getCxzyName&commissioner_code="+commissioner_code,"GET",httpRequest);
		   		} 
		 }
	 }
	 function doGetcommissionerName(str,id){
		 var result=str.substring(0,1);
		 if(str=="<%=ConstResult.AJAX_RESULT_FAILURE%>"){
	  		   alert("录入的财险专员代码不存在或已离职，请重新输入，或请填写财险介绍人代码、姓名！");
	  		 	document.all.commissioner_code.value = "";
	        	document.all.commissioner_name.value = "";
					return;
	     	}
		 var result=str.substring(0,1);
   		 var content=str.substring(1);
   		 var i = new Array();
		 if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
			 i = content.split('&');
			 if(i[1]!=null){
				 document.all.commissioner_name.value=i[1];
				 document.all.commissioner_name.readOnly=true;
			 }
		 }
	 }
	 
	 function IsComprehensive(selObj){		 
		 var sIndex=selObj.selectedIndex;
		 var is_comprehensive=selObj.options[sIndex].value;
		 var recommend_id=document.all.recommend_id.value;	
		
		 if(recommend_id==""){
			 alert("请先录入推荐人信息！");
			 document.getElementById("is_comprehensive").value="0";
			 return;
		 }
		 //if(is_comprehensive=="1"&&recommend_id=="0000000"){
		//m by hbl for L1846 start
		 if( channel_id=="04") {
			 if (is_comprehensive == "1") {
				 //m by hbl for L1846 end
				 document.all.introduce_id.value = "";
				 document.all.introduce_id.readOnly = false;
				 document.all.introduce_name.value = "";
				 document.all.introduce_name.readOnly = false;
				 document.all.commissioner_code.value = "";
				 document.all.commissioner_code.readOnly = false;
				 return true;
			 } else if (is_comprehensive == "0") {
				 document.all.introduce_id.value = "";
				 document.all.introduce_id.readOnly = true;
				 document.all.introduce_name.value = "";
				 document.all.introduce_name.readOnly = true;
				 document.all.commissioner_code.value = "";
				 document.all.commissioner_code.readOnly = true;
			 }
		 }
		 if( channel_id=="01"){
			 if (is_comprehensive == "1") {
				 document.all.introduce_id.value="";
				 document.all.introduce_id.readOnly=false;
				 document.all.introduce_name.value="";
				 document.all.introduce_name.readOnly=false;
				 document.all.commissioner_code.value="";
				 document.all.commissioner_code.readOnly=false;
				 document.all.commissioner_name.value="";
				 document.getElementById("commissioner_code").style.backgroundColor = "";
				 document.getElementById("introduce_id").style.backgroundColor = "";
				 document.getElementById("introduce_name").style.backgroundColor = "";
				 document.getElementById("is_comprehensive").disabled = false;
				 document.getElementById("commissioner_code").removeAttribute('disabled');
				 document.getElementById("introduce_id").removeAttribute('disabled');
				 document.getElementById("introduce_name").removeAttribute('disabled');
				 document.getElementById("commissioner_name").removeAttribute('disabled');
			 }else{
				 document.all.introduce_id.value="";
				 document.all.introduce_id.readOnly=true;
				 document.all.introduce_name.value="";
				 document.all.introduce_name.readOnly=true;
				 document.all.commissioner_code.value="";
				 document.all.commissioner_code.readOnly=true;
				 document.all.commissioner_name.value="";
				 document.getElementById("commissioner_code").setAttribute('disabled', 'disabled');
				 document.getElementById("introduce_id").setAttribute('disabled', 'disabled');
				 document.getElementById("introduce_name").setAttribute('disabled', 'disabled');
				 document.getElementById("commissioner_name").setAttribute('disabled', 'disabled');
				 document.getElementById("is_comprehensive").disabled = false;
				 document.getElementById("commissioner_code").style.backgroundColor = "#efefef";
				 document.getElementById("introduce_id").style.backgroundColor = "#efefef";
				 document.getElementById("introduce_name").style.backgroundColor = "#efefef";
			 }
		 }
	 }
	 function getTeam(){
		 
		 var team_id=document.all.team_id.value;
		 if(team_id==""){
			 alert("请输入组织代码！");
			 if (channel_id == "03") {
			 	//银保建议先填写组织代码
				 document.all.id_no.value="";
			 }
			 return;
		 }
		 if(isNumber(team_id)==false){
				alert("组织代码只能为数字！");
				document.all.team_id.value="";
				document.all.team_name.value="";
				return;
			}
		 if(channel_id=="02"){
			if(checkIsNum(team_id,<%=LengthConst.LENGTH_OF_TEAMID_TX%>)==false){
				alert("组织代码长度输入错误！");
				document.all.team_id.value="";
				document.all.team_name.value="";
				return;
			}
		 }else{
			 if(checkIsNum(team_id,<%=LengthConst.LENGTH_OF_TEAMID_GX%>)==false){
					alert("组织代码长度输入错误！");
					document.all.team_id.value="";
					document.all.team_name.value="";
					return;
				}
		 }
		 var recommend_id=document.all.recommend_id.value;
		 if(recommend_id == "0000000"){
			 checkPerformanceSwitch("",team_id);
		 }
		 	var pars = '&team_id=' + team_id;
		 // var httpRequest = new HttpRequest();
		 const request1 = new Promise((resolve, reject) => {
			 let httpRequest = new HttpRequest();
			 with (httpRequest) {
				 init();
				 doSetCallBack(showTeamName, "");
				 doSendResuest("<%=locationPath%>/teamInfo_hd.do?method=getTeamInfo" + pars, "GET", httpRequest);
			 }
			 resolve()
		 })

		 const request2 = new Promise((reject, resolve) => {
			 let httpRequest = new HttpRequest();
			 // add by meiqiujun for L2382	判断该team_id和推荐人的组织id是否在同一个三级机构下 begin
			 let params = '&team_id=' + team_id + '&recommend_team_id=' + recommend_team_id;
			 if (recommend_id != '0000000' && recommend_id != "") {
				 with (httpRequest) {
					 init();
					 doSetCallBack(showIsSameBranch3, "");
					 doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkIsSameBranch3" + params, "GET", httpRequest);
				 }
			 }
			 resolve()
		 })

		 async function handleRequests() {
			 try{
				 const data1 = await request1
				 const data2 = await request2
			 }catch(error){
				 console.log(error)
			 }
		 }

		 handleRequests()
	 }


	function showIsSameBranch3(str) {
		console.log('function in ')
		console.log(str)
		console.log('function out')
		if (str.trim() != '1') {
			alert("入司人员的组织代码需要与推荐人所在组织在同一个三级机构下，请重新填写！")
		}
	}

	// add by meiqiujun for L2382 end
	 function showTeamName(str,id){
  	   if(str=="<%=ConstResult.RESULT_CANNOTDO%>"){
  		   alert("此组织代码所属机构不在操作权限范围内");
				document.getElementById("team_id").value = "";
				document.getElementById("team_name").value = "";
				var prdCodeR=document.getElementById("rank");
				document.all.rural_networks_id.value= "";// a by shiaywei for 613
				removeSel(prdCodeR);
				return;
     	}
     		var team_id = document.getElementById("team_id").value.trim();
			var jsonDoc = eval('(' + str + ')');
			//L573 互动套转添加校验  qly
			if(jsonDoc.stat=="0"){
				alert("该组织已失效,请重新输入!");
				document.getElementById("team_id").value = "";
				document.getElementById("team_name").value = "";
				var prdCodeR=document.getElementById("rank");
				document.all.rural_networks_id.value= "";// a by shiaywei for 613
				removeSel(prdCodeR);
				return;
			}
			if(jsonDoc.teamName==0&&team_id!="")
			{
				alert("该组织代码不存在,请重新输入!");
				document.getElementById("team_id").value = "";
				document.getElementById("team_name").value = "";
				document.all.rural_networks_id.value= "";// a by shiaywei for 613
				var prdCodeR=document.getElementById("rank");
				removeSel(prdCodeR);
				return;
			}
			else if(team_id!="")
			{
				/* add liu_yl forL1611服营新军 on 20211220 start */
				team_is_new_army = jsonDoc.is_new_army;
				if(team_is_new_army=='1'){
					document.getElementById("team_id").value='';
					// alert('该四级机构为服营IWP机构，不能预入司');
					alert('人员为非IWP,不能选择IWP组织');
					return;
				}
				/* add liu_yl forL1611服营新军 on 20211220 end */
				document.getElementById("team_name").value = jsonDoc.teamName;
				document.getElementById("version_id").value = jsonDoc.version_id;
				// SMIS-1123-（协2021-2814）银保人管系统日常功能优化改造需求 modify by lizuochao 20210804 begin
				teamLvl = jsonDoc.team_lvl;
				isHave= jsonDoc.isHave;
				LeaderID= jsonDoc.LeaderID;
				LeaderID2= jsonDoc.LeaderID2;
				have_manager= jsonDoc.have_manager;
				is_inherency = jsonDoc.is_inherency;
				// SMIS-1123-（协2021-2814）银保人管系统日常功能优化改造需求 modify by lizuochao 20210804 end
				 if(channel_id!="02"){
					if(channel_id!="03"){
						if(jsonDoc.team_type!="1"){//jsonDoc.team_lvl!="1" 二分之的代码
							alert("组织代码,必须为营业组代码!");
							document.getElementById("team_id").value = "";
							var prdCodeR=document.getElementById("rank");
							removeSel(prdCodeR);
							return;
						}
					}else{
						if(jsonDoc.team_type!="2"){//jsonDoc.team_lvl!="2" 二分之的代码
							alert("组织代码,必须为营业组代码!");
							document.getElementById("team_id").value = "";
							var prdCodeR=document.getElementById("rank");
							removeSel(prdCodeR);
							return;
						}
						
					}
				}
			} 
			 if("<%=channel_id%>"=="03"||("<%=channel_id%>"=="02")){
				var branch_id=jsonDoc.branch_id;
				var channelid="<%=channel_id%>";
			 	var pars = '&branch_id=' + branch_id+'&channelid=' + channelid;
		  	 	var httpRequest=new HttpRequest();
		         with(httpRequest){
			     init();
			     doSetCallBack(showservicrbranch_id,""); 
			     doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getshowservicrbranch_id"+pars,"GET",httpRequest);
			   } 
			 }else if("<%=channel_id%>"=="01"){ /* a by syy for L1831 start 大个险预入司 根据所属路线联动业务职级 */
				 getExcellentrank(); 
			 }else{
				 var team_id=document.all.team_id.value;
			 	var pars = '&team_id=' + team_id;
		  	 	var httpRequest=new HttpRequest();
		         with(httpRequest){
			     init();
			     doSetCallBack(showRankName,""); 
			       doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getRankInfo"+pars,"GET",httpRequest);
			   } 
			   }
			//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 start
		 	if($('#recommend_id').val()=='0000000') getAccreditOrg(team_id,2);
		 	//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 end
	   }
			  function showservicrbranch_id(str,id){
				 var result=str.substring(0,1);
					if (result!="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
			  	  		alert("该组织所在的机构后两位不是99的机构或不是特殊机构，无法增员!");
			  	  	       return;
					}
				 
				    var team_id=document.all.team_id.value;
				 	var pars = '&team_id=' + team_id;
			  	 	var httpRequest=new HttpRequest();
			         with(httpRequest){
				     init();
				     doSetCallBack(showRankName,""); 
				     doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=getRankInfo"+pars,"GET",httpRequest);
			         }
			 } 
	 function showRankName(str,id){

			var result=str.substring(0,1);
	   		var content=str.substring(1);
			if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
				var rank = document.all.rank.value;
				//add by lzy for L2759
				//银保渠道，如果已经渲染过就不要再次更新选项了
				if(channel_id!="03" || null==rank || ''==rank) {
					str = eval("(" + content + ")");
					var prdCodeR = document.getElementById("rank");
					removeSel(prdCodeR);
					for (var i = 0; i < str.length; i++) {
						var option = document.createElement("option");
						option.setAttribute("value", str[i].codecode);
						option.innerHTML = str[i].codename;
						prdCodeR.appendChild(option);
					}
					document.all.rank.disabled = false;
				}
			}else{
				alert(content);
				var prdCodeR=document.getElementById("rank");
				removeSel(prdCodeR);
				return;
			}
			 if("<%=channel_id%>"=="03" ||("<%=channel_id%>"=="02")){
			var team_id=document.all.team_id.value;
			var channelid="<%=channel_id%>";
		 	var pars = '&team_id=' + team_id+'&channelid=' + channelid;
	  	 	 var httpRequest=new HttpRequest();
	         with(httpRequest){
		     init();
		     doSetCallBack(showservice_branch_id,""); 
		     doSendResuest("<%=locationPath%>/teamInfo_hd.do?method=getservice_branch_id"+pars,"GET",httpRequest);
	         } 
		}
     }
	////add by guo_rl for L1070 2020-2-12 
      function showservice_branch_id(strb,id){
	        	var result=strb.substring(0,1);
	 	   		var content=strb.substring(1);
	 			if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
	 				strb=eval("(" + content + ")");
	 	         	var prdCodeR=document.getElementById("service_branch_id");
	 	          	removeSel(prdCodeR);
	 				for(var i=0;i<strb.length;i++){
	 					var option=document.createElement("option");
	 						option.setAttribute("value",strb[i].codecode);
	 						option.innerHTML=strb[i].codename;
	 						prdCodeR.appendChild(option);
	 				} 
	 			}else{
	 				alert(content);
	 				var prdCodeR=document.getElementById("service_branch_id");
	 				removeSel(prdCodeR);
	 				return;
	 			} 
	         }
	 function removeSel(prdCodeR){       	   
         prdCodeR.options.length=0;
 		var option=document.createElement("option");
	        option.setAttribute("value","");
 		option.innerHTML="-请选择-";
 		prdCodeR.appendChild(option);
    }
	 
	 function show_isExcperson(checkType){
		 if(channel_id=="01"){
				var type = checkType;
				var team_id=document.all.team_id.value;
				var rank=document.all.rank.value;
				if(team_id==''){
					return;
				}
				if (rank >= "M05" && team_id!="") {
					if(LeaderID2!="0000000"){
						alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.all.team_id.readOnly='';
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					}
				}
				// a bu syy for L1864  on 20220826  start
				var recommend_id=document.all.recommend_id.value; 
				var rank_lvl = 0; 
	 			if (rank >= 'M09') {
					rank_lvl = 3;
				}else if (rank >= 'M07') {
					rank_lvl = 2;
				}else if (rank >= 'M05') {
					rank_lvl = 1;
				}  
				// a bu syy for L1864  on 20220826  end
				
				var httpRequest=new HttpRequest();
		        with(httpRequest){
	               init();
	               doSetCallBack(doGetRank,type);   // a bu syy for L1864  on 20220826   仿照服营的 SalesInfo_xq.do?method=getInfo 增加 触发 推荐人代码和组织代码必须归属同一个职场! 等的校验  
	               doSendResuest("<%=locationPath%>/SalesInfo_gx.do?method=getRank&rank="+rank+"&team_id="+team_id+"&recommend_id="+recommend_id+"&rank_lvl="+rank_lvl,"GET",httpRequest);
	            }
		 }else if(channel_id=="02"){
			 var rank=document.all.rank.value;
			 if(LeaderID!="0000000"){
				 if(rank>"G11"&&rank<"G90"){
					 document.all.team_id.readOnly='';
					 alert("该组织已存在营业部经理职级，请修改");
					 document.all.rank.value="";
					 document.all.rank.disabled = false;
					 return;
				 }
			 }
			 if(rank>"G11"&&rank<"G90"){
				  if(LeaderID2!="0000000"){
						alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.all.team_id.readOnly='';
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					}
			 }  
		 }else if(channel_id=="03"){
			 
			 var rankid = document.all.rank.value;
			 var team_id = document.all.team_id.value;
			 var version_id = document.all.version_id.value;

			 //add by lzy for L2759 start
			 var id_type = document.all.id_type.value;
			 var id_no = document.all.id_no.value;
			 if(rankid > "A01"){
				 var result="";
				 var pars = '&id_type=' + id_type +'&id_no=' + id_no;
				 var url = '<%=locationPath%>/specialEntry.do?method=qryLastAprovedRecord'
						 + pars;
				 $.ajax({
					 type : "get", // 请求方式
					 url : url, // 目标资源
					 dataType : "text", // 服务器响应的数据类型
					 async : false,
					 success : function(data) {
						 result =  data ;
					 }
				 });

				 if (null == result || "" == result || rankid!=result) {
					 document.all.rank.value = "";
					 document.all.rank.disabled = false;
					 alert("入司职级为非见习客户经理人员，需要在【人员入司申请】菜单发起入司职级申请，审批通过后才允许入司。");
					 return;
				 }

			 }

			 //add by lzy for L2759 end


			 if(LeaderID!="0000000"){
				 if(version_id=="3240"){
					 if(rankid >= "A06" && rankid<="A10"){
						 document.all.team_id.readOnly='';
						 // alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 alert("当前组织已有主管，请修改人员所属组织!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					 }
				 }
			 }
			 /* 没有主管校验 */
			  if(version_id=="3240"){
				 if(rankid >= "A06" && rankid<="A10"){
					 if(LeaderID2!="0000000"){
						 	document.all.team_id.readOnly='';
							// alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
							alert("当前组织已有主管，请修改人员所属组织!");
							 document.getElementById("team_id").value = "";
							 document.getElementById("team_name").value = "";
							return;
					}
				 }
			 }
			 
			 
			 if(version_id=="3240"){
				// SMIS-1123-（协2021-2814）银保人管系统日常功能优化改造需求 modify by lizuochao 20210804 begin
				if(is_inherency == '1'){
					//固有组限制不能存在主管
					if(rankid >= "A06" && rankid<="A10"){
						alert("该组织为固有组，业务职级只能选择销售系列职级！");
						document.all.rank.value = "";
						document.all.rank.disabled = false;
						return;
					}
				}
				// SMIS-1123-（协2021-2814）银保人管系统日常功能优化改造需求 modify by lizuochao 20210804 end
				if (isHave == 0) {
					if (rankid == "A06" || rankid == "A07" || rankid == "A08") {
						alert("部经理业务职级的人只能归属部直辖组!");
						document.all.rank.value = "";
						document.all.rank.disabled = false;
						return;
					}
				} else if (isHave == 1) {
					if (rankid == "A09" || rankid == "A10" ) {
						alert("区总监业务职级的人只能归属区直辖组!");
						document.all.rank.value = "";
						document.all.rank.disabled = false;
						return;
					}
				} else if (isHave == 2) {
					if (rankid == "A06" || rankid == "A07" || rankid == "A08") {
						alert("部经理业务职级的人只能归属部直辖组!");
						document.all.rank.value = "";
						document.all.rank.disabled = false;
						return;
					}
					if (rankid == "A09" || rankid == "A10") {
						alert("区总监业务职级的人只能归属区直辖组!");
						document.all.rank.value = "";
						document.all.rank.disabled = false;
						return;
					}
				}
			}
			 
		 }else if(channel_id=="04"){

			 var rankid = document.all.rank.value;
			 var team_id = document.all.team_id.value;
			 var version_id = document.getElementById('version_id').value;
			 
			 if(version_id=="4110"){
				 if(rankid >= "H11" && rankid<="H14"){
					 document.all.team_id.readOnly=false;
				 }
			 }else if(version_id=="4140"){
				 if(rankid >= "H06" && rankid<="H07"){
					 document.all.team_id.readOnly=false;
				 }
			 }else if(version_id=="4150"){
				 if(rankid >= "H06" && rankid<="H07"){
					 document.all.team_id.readOnly=false;
					 if(is_inherency == '1'){
						 //固有组限制不能存在主管
						 alert("该组织为固有组，业务职级只能选择销售系列职级！");
						 document.all.rank.value = "";
						 document.all.rank.disabled = false;
						 return;
					 }
				 }
			 }else if(version_id=="4130"){
				 if(rankid >= "M05" && rankid<="M10"){
					 document.all.team_id.readOnly=false;
				 }
			 }
			 
			 if(LeaderID!="0000000"){
				 if(version_id=="4110"){
					 if(rankid >= "H11" && rankid<="H14"){
						 document.all.team_id.readOnly='';
						 alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					 }
				 }else if(version_id=="4140"){
					 if(rankid >= "H06" && rankid<="H07"){
						 document.all.team_id.readOnly='';
						 alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					 }
				 }else if(version_id=="4150"){
					 if(rankid >= "H06" && rankid<="H07"){
						 document.all.team_id.readOnly='';
						 alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						 return;
					 }
				 }else if(version_id=="4130"){
					 if(rankid >= "M05" && rankid<="M10"){
						 document.all.team_id.readOnly='';
						 alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					 }
				 }
			 }
			 
			 /* 没有主管校验 */
			 if(version_id=="4110"){
				 if(rankid >= "H11" && rankid<="H14"){
					 if(LeaderID2!="0000000"){
							 document.all.team_id.readOnly='';
							alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
							 document.getElementById("team_id").value = "";
							 document.getElementById("team_name").value = "";
							return;
						}
				 }
			 }else if(version_id=="4140"){
				 if(rankid >= "H06" && rankid<="H07"){
					 if(LeaderID2!="0000000"){
							 document.all.team_id.readOnly='';
							alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
							 document.getElementById("team_id").value = "";
							 document.getElementById("team_name").value = "";
							return;
						}
				 }
			 }else if(version_id=="4150"){
				 if(rankid >= "H06" && rankid<="H07"){
					 if(LeaderID2!="0000000"){
						 document.all.team_id.readOnly='';
						 alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						 return;
					 }
				 }
			 }else if(version_id=="4130"){
				 if(rankid >= "M05" && rankid<="M10"){
					 if(LeaderID2!="0000000"){
							 document.all.team_id.readOnly='';
							alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
							 document.getElementById("team_id").value = "";
							 document.getElementById("team_name").value = "";
							return;
						}
				 }
			 }
			 if(version_id=="4110"){
				 if (isHave != 1) {
						if (rankid>="H11" && rankid<="H14") {
							alert("组长职务的人归属普通组（非区直辖组和部直辖组）!");
							document.all.rank.value="";
							return;
						}
					}
			}else if(version_id=="4140"){
				 if (isHave != 1) {
						if (rankid>="H06" && rankid<="H07") {
							alert("组长职务的人归属普通组（非区直辖组和部直辖组）!");
							document.all.rank.value="";
							return;
						}
					}
			}else if(version_id=="4150"){
				 if (isHave != 1) {
					 if (rankid>="H06" && rankid<="H07") {
						 alert("组长职务的人归属普通组（非区直辖组和部直辖组）!");
						 document.all.rank.value="";
						 return;
					 }
				 }
			}else if(version_id=="4130"){
				 if (isHave == 3) {
						if (rankid=="M07" || rankid=="M08") {
							alert("部经理、高级部经理业务职级的人只能归属部直辖组!");
							document.all.rank.value="";
							return;
						}
						if (rankid=="M05" || rankid=="M06") {
							alert("主任、高级主任业务职级的人只能归属普通服务营销组!");
							document.all.rank.value="";
							return;
						}
					}else if(isHave == 2) {
						if (rankid=="M09" || rankid=="M10") {
							alert("区总监、高级区总监业务职级的人只能归属区直辖组!");
							document.all.rank.value="";
							return;
						}
						if (rankid=="M05" || rankid=="M06") {
							alert("主任、高级主任业务职级的人只能归属普通服务营销组!");
							document.all.rank.value="";
							return;
						}
					}else if(isHave == 1) {
						if (rankid=="M09" || rankid=="M10") {
							alert("区总监、高级区总监业务职级的人只能归属区直辖组!");
							document.all.rank.value="";
							return;
						}
						if (rankid=="M07" || rankid=="M08") {
							alert("部经理、高级部经理业务职级的人只能归属部直辖组!");
							document.all.rank.value="";
							return;
						}
					}
			}
			 
		 }else if(channel_id=="05"){
				var rankid = document.all.rank.value;
				var team_id = document.all.team_id.value;
				
				if (rankid >= "M05" && team_id!="") {
					document.all.team_id.readOnly=false;
					if (have_manager == "no") {
						alert("此组织存在管理系列职级的人员，故不能再增员管理系列职级的人员!");
						document.all.rank.value="";
						return;
					}
				}
				// (增员销售系列，组织代码必须和推荐人保持一致)  syy  start  
					var recommend_id=document.all.recommend_id.value; 
					var branch_id=document.all.branch_id.value; 
					var httpRequest=new HttpRequest();
					var rank=document.all.rank.value;
			 		with(httpRequest){
			     		init();
					    doSetCallBack(doGetTeamID,"");
			            doSendResuest("<%=locationPath%>/SalesInfo_xq.do?method=checkRecommendTeamID&recommend_id="+recommend_id+"&branch_id="+branch_id+"&rank="+rank,"GET",httpRequest);
			     	}  
			 		
		 		//判定此时重输入的组织代码是否=推荐人的组织代码  syy  end 
				var rank_lvl = 0;
				if (rankid >= 'M09') {
					rank_lvl = 3;
				}else if (rankid >= 'M07') {
					rank_lvl = 2;
				}else if (rankid >= 'M05') {
					rank_lvl = 1;
				 }
				if (isHave == 3) {
					if (rankid=="M07" || rankid=="M08") {
						alert("部经理、高级部经理业务职级的人只能归属部直辖组!");
						document.all.rank.value="";
						return;
					}
					if (rankid=="M05" || rankid=="M06") {
						alert("主任、高级主任业务职级的人只能归属普通服务营销组!");
						document.all.rank.value="";
						return;
					}
				}else if(isHave == 2) {
					if (rankid=="M09" || rankid=="M10") {
						alert("区总监、高级区总监业务职级的人只能归属区直辖组!");
						document.all.rank.value="";
						return;
					}
					if (rankid=="M05" || rankid=="M06") {
						alert("主任、高级主任业务职级的人只能归属普通服务营销组!");
						document.all.rank.value="";
						return;
					}
				}else if(isHave == 1) {
					if (rankid=="M09" || rankid=="M10") {
						alert("区总监、高级区总监业务职级的人只能归属区直辖组!");
						document.all.rank.value="";
						return;
					}
					if (rankid=="M07" || rankid=="M08") {
						alert("部经理、高级部经理业务职级的人只能归属部直辖组!");
						document.all.rank.value="";
						return;
					}
				}
				var httpRequest=new HttpRequest();
		           with(httpRequest){
		        	   var id_no=document.all.id_no.value;
		        	   var id_type=document.all.id_type.value;
		        	   var recommend_id=document.all.recommend_id.value;
		               init();
		               doSetCallBack(doGetInfo,team_id);
		               doSendResuest("<%=locationPath%>/SalesInfo_xq.do?method=getInfo&team_id="+team_id+"&id_no="+id_no+"&id_type="+id_type+"&recommend_id="+recommend_id+"&recommend_type="+recommend_type+"&rank_lvl="+rank_lvl,"GET",httpRequest);   //L426
	              }
		 }else if(channel_id=="0C"){
			 var team_id=document.all.team_id.value;
			 var rank=document.all.rank.value;
			 var sales_type_rank="";
			 if(rank<= "D11"){
				 sales_type_rank="01";
			 }else if(rank<="D17"){
				 sales_type_rank="02";
			 }else{
				 sales_type_rank="03";
			 }
			 var tempNum2="001";
			 if(sales_type_rank!="<%=CodeTypeConst.CODE_CODE_RANK_01%>"){//不是业务人员
				 var httpRequest=new HttpRequest();
	        		with(httpRequest){
	            		init();
		             	doSetCallBack(getResult,tempNum2);
		             	doSendResuest("<%=locationPath%>/salesInfo_dx.do?method=provBySales&team_id="+team_id+"&rank="+rank+"&sales_type_rank="+sales_type_rank,"GET",httpRequest);
	             	}
				}else{// 业务人员直接保存
					
				}
		 }
	 }
	 function getResult(str,id){
				var result=str.substring(0,1);
         		var content=str.substring(1);
				if (result!="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){// 校验不通过
					alert(content);
					 document.getElementById("team_id").value = "";
					 document.getElementById("team_name").value = "";
					 document.getElementById("rank").value = "";
					 return;
				}else if(result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){// 校验通过
					if(LeaderID2!="0000000"){
						alert("该组织已经存在经理");
						 document.getElementById("team_id").value = "";
						 document.getElementById("team_name").value = "";
						return;
					}
				}
			}
	 function doGetInfo(str,id){
	    	var result=str.substring(0,1);
	   		var content=str.substring(1);
	   		var i=content.split('#');
			if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
				//组织不属于当前登录操作权限内
	   			if (i[8] == "") {
	   				alert("此组织代码所属机构不在操作权限范围内");
	   				return;
	   			}
	   	
	   			if (i[14] != "yes") {
	   				alert(i[14]);
	   				document.all.rank.value="";
	   				return;
	   			}
	        }else{
	            alert(content)
	            document.all.team_id.value="";
	            document.all.team_name.value="";
	            document.all.branch_id.value="";
	        }
	    }  
	 function doGetRank(str,id){
			var result=str.substring(0,1);
	   		var content=str.substring(1);
	   		//a by syy for L1864 start
	   		var str = content.split('#');
	   		content = str[0];//重赋值
	   		var re_team_str = str[1]; 
			//a by syy for L1864 end
			if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
				//a by syy for L1864
				if(re_team_str!="yes"){
					alert(re_team_str);
					document.all.team_id.readOnly='';
		        	document.all.team_id.value = "";
		        	document.all.team_name.value = ""; 
		        	//document.all.team_id.focus();
	   				document.all.rank.value="";
	   				return;
				}
	        }else{
	        	if(content=="NotMatch"){
	        		alert("主管级别与组织级别不匹配！");
	        		document.all.team_id.readOnly='';
		        	document.all.team_id.value = "";
		        	document.all.team_name.value = "";
		        	document.all.leader_id.value = "";
		        	document.all.workspace_id.value = "";
		        	document.all.branch_id.value = "";
		        	document.all.area_type.value = "";
		        	//document.all.team_id.focus();
		        	return;
	        	}
	        	if(content=="HaveLeader"){
	        		alert("该组织已存在主管！");
	        		document.all.team_id.readOnly='';
		        	document.all.team_id.value = "";
		        	document.all.team_name.value = "";
		        	document.all.leader_id.value = "";
		        	document.all.workspace_id.value = "";
		        	document.all.branch_id.value = "";
		        	document.all.area_type.value = "";
		        	//document.all.team_id.focus();
					return;
	        	} 
	        	//a by syy for L1864
				if(re_team_str!="yes"){
					alert(re_team_str);
					document.all.team_id.readOnly='';
		        	document.all.team_id.value = "";
		        	document.all.team_name.value = ""; 
		        	//document.all.team_id.focus();
	   				document.all.rank.value="";
	   				return;
				}
	        }
		}
		//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 start
		function getAccreditOrg(id,type) { //type   1 通过 推荐人id查询  2 通过组织代码查询
			if(id=='' || id=='0000000' || channel_id!='04') return; //不是互动渠道 跳出 无推荐人 跳
			$.post('<%=locationPath%>/salesInfo_hd.do?method=getAccreditOrg',{id:id,type:type},function (d) {
				d=eval('(' + d + ')');
				if(d.length>0){
					$('#accredit_org').empty();
					$('#accredit_org').append('<option value="">请选择</option>');
					for(var i=0;i<d.length;i++)
						$('#accredit_org').append('<option value="'+d[i].BRANCH_ID+'">'+d[i].BRANCH_NAME+'</option>');
				}
			});
		}
		//a by wzj for L1003 获取派驻机构 on 2020-8-17 10:42:59 end
		
		// by lx for L1227 
		function checkIsRank() {
			var rank_new = document.getElementById('rank').value;
			var team_id =document.getElementById('team_id').value;
			var param="&rank_id="+rank_new+"&team_id="+team_id;
			var httpRequest=new HttpRequest();
			with(httpRequest){
			    init();
			    doSetCallBack(doIsRank,"");
			    doSendResuest('<%=locationPath%>/rankConfirm_tx.do?method=checkIsRank'+param,"GET",httpRequest);
			    }
			    //alert(sales_code+"---"+rank_new);
			}
		function doIsRank(str,id){
		    var result=str.substring(0,1);
		    if(result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>" || result=="2"){
		    } else{
		        alert("当前营业部已存在营业部经理职级的人员，请核查！");
		    }
		}
				//add by hzp for L1565 ON 20210927 start
	function checkBranch_yb() {
		if("03"!=channel_id){
			return;
		}
		var service_branch_id =document.getElementById('service_branch_id').value;
		var httpRequest=new HttpRequest();
		with(httpRequest){
			init();
			doSetCallBack(doGetCheckInfo,"");
			doSendResuest("<%=locationPath%>/staffInfo_yb.do?method=checkServiceBranch&service_branch_id="+service_branch_id,"GET",httpRequest);
		}
	}
	function doGetCheckInfo(str,id){
		var result=str.substring(0,1);
		var content=str.substring(1);

		if(result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
		} else{
			alert(content);
			document.all.service_branch_id.value = "";
		}
	}

	//add by SYY ON 20211122  
	  function doGetTeamID(str,id){
		    	var result=str.substring(0,1);
		   		var content=str.substring(1);
		   		var i = new Array(); 
		   		var recommend_id=document.all.recommend_id.value;//推荐人代码
				var teamID_OLD = ""; //推荐人组织代码
				var rankType = ""; //预入司人员的职级层级
				var teamName_OLD = ""; //推荐人组织名称
				var teamID_NEW = document.all.team_id.value;//重填的组织代码
				if (result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
					i = content.split('#'); 
					if(i[0]!=null){
						teamID_OLD=i[0]; 
						} 
					if(i[1]!=null){
						rankType=i[1]; 
						}  
					if(i[2]!=null){
						teamName_OLD=i[2]; 
						}  
				}
				//销售系列重填之后且排除无推荐人的情况
				if(rankType == "01" && teamID_NEW != null && teamID_NEW.length > 0 && recommend_id!="0000000" ){ 
					if(teamID_OLD!=teamID_NEW){
						//alert("增员销售系列人员的组织代码必须为推荐人员隶属组织！");
						//document.all.rank.value="";//清空业务职级
						document.all.team_id.value = teamID_OLD;//还原为推荐人的组织代码
						document.all.team_name.value = teamName_OLD; //还原为推荐人的组织名称
						document.all.team_id.readOnly=false;//组织代码文本框只读
						return;
						}
				}
				
	  }
		//add by hzp for L1565 ON 20210927 end
		 
 /* add syy for L1831 on 20220712 start */ 
         //根据所属路线查询职级下拉框集合
       function getExcellentrank() {
          var route_type = document.all.route_type.value;//预入司人员 0-常规;1-绩优  
          if(route_type=='' || route_type == null){
        	  var prdCodeR=document.getElementById("rank");//职级元素
	    	   removeSel(prdCodeR); 
        	  return;
          }
          var param='&route_type='+route_type+'&version=2020&checkFlag=getRankRouteTypeInfo';
			 var httpRequest=new HttpRequest();
				with(httpRequest){
					init();
					doSetCallBack(doShowRankRouteTypeInfo);
					doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkRecommendInfo"+param,"GET",httpRequest);
				} 
        	 
        }
   	 function doShowRankRouteTypeInfo(resultinfo){
		 var result=resultinfo.substring(0,1);
		 var content=resultinfo.substring(1);  
	     var rankstr =content.split('#')[0];
	     if(result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){
	    	//根据所属路线选择 业务职级
    		 rankstr=eval("(" + rankstr + ")"); 
    		//清空职级重赋值
	    	   var prdCodeR=document.getElementById("rank");//职级元素
	    	   removeSel(prdCodeR);
    		for(var i=0;i<rankstr.length;i++){
				var option=document.createElement("option");
					option.setAttribute("value",rankstr[i].codecode);
					option.innerHTML=rankstr[i].codename;
					prdCodeR.appendChild(option);
			}  
	     }
		 
	 } 
		
  		//是否符合绩优要求
		function isCanExcellent(checked){
			//L2952 银保渠道增加“特殊人员类型申请”审批通过后，最高学历只能为大专及以上学历
			if (channel_id == "03") {
				var education = document.all.education.value;
				var special_education = document.all.special_education.value; //“特殊人员类型申请”审批通过的学历
				if(special_education!=""&&special_education!=undefined&&special_education!=null){ //人员通过了特殊人员入司
					if(education < '7'){ //大专以下学历
						alert("该人员通过了队伍建设方案人员的申请，最高学历只能为大专及以上学历！");
						document.all.education.value="";
						return;
					}
				}
			}
		 if(checked == 'checked'){//L1831  切换所属路线，职级下拉框随之改动
				getExcellentrank(); 
			} 
		var recommend_id =document.getElementById('recommend_id').value;//推荐人代码 
	    var route_type = document.all.route_type.value;//预入司人员 0-常规;1-绩优 
			if(route_type=='1'){/* 绩优人员  */
				var birthday = document.all.birthday.value; 
				var id_no = document.all.id_no.value;
				var education = document.all.education.value;
				if(null==id_no || ''==id_no){
					alert('请输入证件号码!');
					document.all.route_type.value ='0';
					return;
				}
				if(null==birthday || ''==birthday){
					alert('请输入出生日期!');
					document.all.route_type.value ='0';
					return;
				}
				if(null==education || ''==education){
					alert('请选择学历!');
					document.all.route_type.value ='0';
					return;
				}
				var age = jsGetAge(birthday); 
				if(parseInt(education)>=7){//满足绩优条件    此处只校验学历;在 保存处根据开关校验年龄 
					 //根据推荐人代码判断是否 0000000
					if(recommend_id=='0000000'){
						document.all.team_id.readOnly=false;//组织代码可修改
						 //填写组织代码后触发校验=getTeam;获取职级列表时增加所属路线的传参(此时预入司=绩优) 
					}else{
						document.all.team_id.readOnly=false;//组织代码置灰
						//填写推荐人代码后 触发 getName 方法;组织代码由推荐人带出。
						//getName中调用getTeam;;获取职级列表时增加所属路线的传参 (此时预入司=绩优)
					} 
				}else{
					alert('人员不符合绩优路线条件，请选择为常规路线。');
					document.all.route_type.value ='0';
					return;
				}
			}else{   
				return;
			}
			  
		}
	   
		//通过出生日期获取周岁
		function jsGetAge(strBirthday) {

	       var returnAge;
	       var strBirthdayArr = strBirthday.split("-");
	       var birthYear = strBirthdayArr[0];
	       var birthMonth = strBirthdayArr[1];
	       var birthDay = strBirthdayArr[2];
	       var d = new Date();
	       var nowYear = d.getFullYear();
	       var nowMonth = d.getMonth() + 1;
	       var nowDay = d.getDate();
	       if (nowYear == birthYear) {
	         returnAge = 0;//同年 则为0岁
	       } else {
	         var ageDiff = nowYear - birthYear; //年之差
	         if (ageDiff > 0) {
	           if (nowMonth == birthMonth) {
	             var dayDiff = nowDay - birthDay;//日之差
	             if (dayDiff < 0) {
	               returnAge = ageDiff - 1;
	             }
	             else {
	               returnAge = ageDiff;
	             }
	           }
	           else {
	             var monthDiff = nowMonth - birthMonth;//月之差
	             if (monthDiff < 0) {
	               returnAge = ageDiff - 1;
	             }
	             else {
	               returnAge = ageDiff;
	             }
	           }
	         } else {
	           returnAge = -1;//返回-1 表示出生日期输入错误 晚于今天
	         }
	       }
	       return returnAge;//返回周岁年龄
		}
     // 所属路线 相关校验
	 function is_routetype(){ 
		 isCanExcellent('checked');//判断是否符合绩优要求+根据所属路线联动业务职级下拉框
		 var recommend_id =document.getElementById('recommend_id').value;//推荐人代码 
		 var route_type = document.all.route_type.value;//预入司人员 0-常规;1-绩优 
		 //所属路线要判定=推荐人代码都不能为空  
		 //  if(recommend_id==''|| null==recommend_id){
			//   alert("推荐人代码不能为空");
			//   dialog_waiting.close();//解屏
			//   return;
		 // }
		 if(route_type=='0'){//预入司=常规   
			 check_recommendRouteType();//根据推荐人代码获取其所属路线
		 }

		 //所属路线被切换，先清空推荐人代码和组织代码  by syy on 20221117
		 document.all.recommend_id.value = "";
		 document.all.recommend_name.value = "";
		 document.all.recommend_id.readOnly = false;
		 document.getElementById('team_id').value= "";
		 document.all.team_name.value = "";
		 document.all.team_id.readOnly = false;
	  }	
		
	//根据推荐人代码获取其所属路线;
		function check_recommendRouteType() {
			 if(channel_id!="01"){
				return;
			}
			var recommend_id =document.getElementById('recommend_id').value;//推荐人代码 
			var param='&recommend_id='+recommend_id+'&checkFlag='+'recommendRouteType'; 
			var httpRequest=new HttpRequest();
			with(httpRequest){
				init();
				doSetCallBack(doGetRouteType);
				doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkRecommendInfo"+param,"GET",httpRequest);
			}
		}
	   //获取推荐人所属路线
		function doGetRouteType(resultinfo){ 
			var result = resultinfo.substring(0,1); 
			var content = resultinfo.substring(1); 
			var recommendRouteType=content.split('#')[0];//推荐人的所属路线 0-常规;1-绩优 
			var recommend_id = document.getElementById('recommend_id').value;//推荐人代码 
			var route_type = document.all.route_type.value;//预入司人员 0-常规;1-绩优 
			 
		 if(route_type=='0'){//预入司=常规
				if(result=="<%=ConstResult.AJAX_RESULT_SUCCESS%>"){ 
					 if(recommendRouteType=='0'){//推荐人所属路线=常规 
						 document.all.team_id.readOnly=false;//组织代码置灰
						//填写推荐人代码后 触发 getName 方法;组织代码由推荐人带出。
						//getName中调用getTeam;;获取职级列表时增加所属路线的传参 (此时预入司=常规)
					 } else if(recommendRouteType=='1'){//推荐人所属路线=绩优 
						 alert("推荐人为绩优人员，不能推荐常规人员。");
						 document.all.route_type.value = '0';//L1864 所属路线清空重填
						 dialog_waiting.close();//解屏   
						 c = '0';
						 return;
					 } 	 
				   } else{//空=无推荐人(0000000)
						// alert("--推荐人代码=0000000---"+recommend_id);
						 document.all.team_id.readOnly=false;//组织代码可填写 
						 //填写组织代码后触发校验=getTeam;获取职级列表时增加所属路线的传参(此时预入司=常规)
					} 
		     }
	   }
		
		/* add syy for L1831 on 20220712 end */

	// 校验 四级机构是否绩优
	function checkPerformanceSwitch (recommend_id,team_id){

		var param='&team_id='+team_id+'&recommend_id='+recommend_id;
		var httpRequest=new HttpRequest();
		with(httpRequest) {
			init();
			doSetCallBack(showPerformanceSwitch);
			doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkPerformanceSwitch" + param, "GET", httpRequest);
		}
	}

	function showPerformanceSwitch (resultinfo){
		var route_type = document.all.route_type.value;//预入司人员 0-常规;1-绩优 ;2-iwp(仅前台)
		if(resultinfo == "1" && route_type == "1"){//关
			alert("该归属机构只能选择常规路线, 请重新选择!");
			document.getElementById("route_type").value = '0';
			document.all.recommend_id.value = "";
			document.all.recommend_name.value = "";
			document.all.recommend_id.readOnly = false;
			document.getElementById('team_id').value= "";
			document.all.team_name.value = "";
			performanceSwitch1 = "";
		}
	}
	//add zengzf by L2366银保入司优化推荐人不能预离司校验
	function checkRecommendidPredismiss(recommend_id,channel_id){
		var param='&recommend_id='+recommend_id+'&channel_id='+channel_id;
		var httpRequest=new HttpRequest();
		with(httpRequest) {
			init();
			doSetCallBack(showRecommendidPredismiss);
			doSendResuest("<%=locationPath%>/moveInfo_yb.do?method=checkRecommendidPredismiss" + param, "GET", httpRequest);
		}
	}
	//add zengzf by L2366银保入司优化推荐人不能预离司校验
	function showRecommendidPredismiss (resultinfo){
		if(resultinfo == "0"){
			alert("预离司状态人员不可作为入司推荐人!");
 			//document.all.recommend_id.value = "";
			//document.all.recommend_name.value = "";
			document.getElementById('recommend_id').value= "";
			document.getElementById('recommend_name').value= "";
			document.all.recommend_id.readOnly = false;
			document.getElementById('team_id').value= "";
			document.getElementById('team_name').value= "";
			//document.all.team_name.value = "";
			document.all.team_id.readOnly = false;
 		}
	}
	function checkRelative() {
		var checked =$("#box1").is(":checked");
		if (checked==true) {
			//添加置灰
			//document.all.addRow.className='disables';
			var trs = $("tr[name='salesRelatives']");
			//勾选后删除父母/子女/配偶行
			for(var i = trs.length-1; i > -1; i -- ){
				var close_relatives_rela = $($(trs[i]).find("select[name='close_relatives_rela']")[0]).val();
				if (close_relatives_rela != null && close_relatives_rela != '' && close_relatives_rela.trim().length != 0 && "01,02,03".indexOf(close_relatives_rela)>-1){
					$(trs[i]).remove()
				}
			}
			freshRow();
		}else{
			document.all.addRow.className='btn';
		}

	}
	/* add by meiqiujun for L2382 */
	function checkRecommendIdIsCircle(recommend_id) {
		let param = '&recommend_id=' + recommend_id;
		let httpRequest = new HttpRequest();
		with (httpRequest) {
			init();
			doSetCallBack(showIsCircleErrorMsg);
			doSendResuest("<%=locationPath%>/salesInfo_hd.do?method=checkRecommendIdIsCircle" + param, "GET", httpRequest);
		}
	}

	// function showIsCircleErrorMsg(resultInfo){
	// 	console.log(resultInfo)
	// }
</script>
 <style type="text/css">
        input[readonly]{
            background-color:#E6E6E6;
        }
	  </style>
  </head>

<body onload="initJSP();" style="overflow: auto;">
<div class="header">
		<div class="middle_l h99">
			<div class="middle_r h100">
				<div class="middle_m h100">
					<div id="class1content" class="innerblock"  style="overflow:auto;height:100%;width:100%">
<form id="form1"  accept-charset="UTF-8"  _charset="UTF-8">
<input type="hidden" name="salesInfo_hd.token_id" value="${token}"/>
	<%
				if("04".equals(channel_id)){
			%>
<table width="80%" border="0" cellspacing="1" cellpadding="0" name="infoadd" id="infoadd" >
	<%
				}else{
			%>
<table width="98%" border="0" cellspacing="1" cellpadding="0" name="infoadd" id="infoadd" >
				
<%
				}
%>
	<%
		//add by lzy for L2759 银保的提示语修改 start
		if("03".equals(channel_id)){
	%>
	<tr>
		<td style="color: red;">注：1、人员预入司之前，请先在“入司背景调查及结果查询”菜单，进行背景调查授权协议上载操作。</td>
	</tr>
	<tr>
		<td style="color: red;">   2、入司人员业务职级为非见习客户经理时，需要在【人员入司申请】菜单发起入司职级申请并审批通过，否则无法预入司。</td>
	</tr>
	<tr>
		<td style="color: red;">   3、入司人员选择业务职级为管理职级时，组织代码会变为可编辑。若推荐人代码录入0000000（无推荐人）时，则组织代码为必录。</td>
	</tr>
	<tr>
		<td style="color: red;">   4、相关专业：是指教育部专业目录中财政税务类、金融类、财务会计类、统计类等。</td>
	</tr>
	<%
		}else {
	%>
	<tr>
		<td style="color: red;">入司人员为管理职级时，选择业务职级为管理职级，组织代码会变为可编辑。</td>
	</tr>
  <tr bgcolor="#dbe8fb" >
    <td style="color: red;">无推荐人时，推荐人代码录入0000000，同时组织代码为必录。</td>
  </tr>
    <tr>
        <td style="color: red;">注：相关专业：是指教育部专业目录中财政税务类、金融类、财务会计类、统计类等。</td>
    </tr>
	<%
			//add by lzy for L2759 银保的提示语修改 end
		}
	%>
	<%--a by chengyy for L2624 start--%>
	<tr>
		<%
			if ("01".equals(channel_id) || "05".equals(channel_id) || "09".equals(channel_id) || "08".equals(channel_id) || "04".equals(channel_id)) {
		%>
		   <td style="color: red;">人员预入司之前，请先在“入司背景调查及结果查询”菜单，进行背景调查授权协议上载操作。</td>
		<%
		   }
		%>
	</tr>
	<%--a by chengyy for L2624 end--%>
  <tr>
    <td height="10">
	<input name="flag" type="hidden" id="flag" value="" />
	 <input name="salesInfo_hd.branch_id" type="hidden" id="salesInfo_hd.branch_id" value="${branch_id}" />
	 <input name="salesInfo_hd.org_sales_code" type="hidden" id="org_sales_code"  value=""/>
	 <input name="salesInfo_hd.branch_id4" type="hidden" id="branch_id4"  value=""/>
	<input name="branch_id" type="hidden" id="branch_id1" value="${branch_id}" />

	<!--M by macj for BUG1326互动渠道-江苏互动渠道人员信息维护功能问题  on 2013.06.13 begin  -->
    <input type="hidden" id="employ_kind_hidden" name="employ_kind_hidden"/>
  <!--M by macj for BUG1326互动渠道-江苏互动渠道人员信息维护功能问题  on 2013.06.13 end  -->
	</td>
  </tr>
  	<tr>
		<td align="left" height='5'>
			<font color='red'><div id="message"></div></font>
		</td>
	</tr>
  <tr bgcolor="#dbe8fb">
    <td>>>人员基本信息</td>
  </tr>
	<%
		if("01".equals(channel_id)){
	%>
  <tr>
    <td>
	<table width="95%" border="1" align="center" cellpadding="2" cellspacing="0"  class="table_bk">
    	<tr>
    	 <!--  <td align="right" nowrap="nowrap">互动经理代码:</td>
    	  <td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly" name="salesInfo_hd.sales_id" id="sales_id" size="20" value=""/></td> -->
    	  <td align="right" nowrap="nowrap">用工性质(<span style="color:red">*</span>)：</td>
    	  <td nowrap="nowrap">
    	  <!--M by macj for BUG1321互动渠道-人员录入时数据库表中用工性质为空  on 2013.06.09 begin   -->
			<select name="salesInfo_hd.employ_kind" id="employ_kind" onchange="checkIsexcperson();">
			<!--M by macj for BUG1321互动渠道-人员录入时数据库表中用工性质为空  on 2013.06.09 end  -->
			<option value="2">代理制</option>
			<option value="1">合同制</option>
				<%--<c:forEach var="employ_kind" items="${employ_kind}">
						<option value="${employ_kind.codecode }">${employ_kind.codename}</option>
				</c:forEach>
			--%></select>  
		</td>
    	<%-- <td align="right" nowrap="nowrap">人员类别(<span style="color:red">*</span>):</td>
    	 	<td nowrap="nowrap">
				<select name="salesInfo_hd.sales_type" id="sales_type">
					<c:forEach var="ctype" items="${salesTypes}">
						<!-- <option value="03">寿险</option> -->
					</c:forEach>
					   <c:if test="${channel_id2 =='04'}">
    				      <option value="02">寿险</option>
    					</c:if>  
    					<c:if test="${channel_id2 !='04'}">
    				       <option value="03">寿险</option>
    					</c:if>   
				</select>
          	</td> --%>
          	
          	 <td align="right" nowrap="nowrap">证件类型(<span style="color:red">*</span>)：</td>
    	  <td nowrap="nowrap">
			<select name="salesInfo_hd.id_type" id="id_type"  onchange="clearidno();">
				<c:forEach var="idtype1" items="${idtype}">
				
						<option value="${idtype1.codecode }">${idtype1.codename}</option>
				</c:forEach>
			</select>          
          </td>
          
           <td align="right" nowrap="nowrap">证件号码(<span style="color:red">*</span>)：</td>
    	  <td nowrap="nowrap">
    	    <input type="text" class="ctlText" name="salesInfo_hd.id_no" id="id_no" size="20" value=""  onblur="getIdNo();"/>
			<div  class="btn" name="authentication" onclick="getIdNo();"><span>验证是否存在</span></div>             
		  </td>
		  
  	  </tr>
    	<tr>
    	 
          
    	 
		  
    	  <td align="right" nowrap="nowrap"> 人员姓名(<span style="color:red">*</span>)： </td>
    	  <td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.sales_name" id="sales_name" size="20" value=""/></td>
    	
    	  <td align="right" nowrap="nowrap">性别(<span style="color:red">*</span>)：</td>
    	  <td nowrap="nowrap">
			<select name="salesInfo_hd.sex" id="sex">
				<option value="">--请选择--</option>
				<c:forEach var="sex" items="${sex}">
							<option value="${sex.codecode }">${sex.codename}</option>
				</c:forEach>
			</select>
           </td>
           
            <td align="right" nowrap="nowrap">出生日期(yyyy-mm-dd)(<span style="color:red">*</span>)： </td>
    	  <td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.birthday" id="birthday" size="20" value="" onkeyup="autoValidDate(this)" autocomplete="off" onClick="WdatePicker()" onblur="changeBirthday();checkValidDate(this)" onchange="checkIsexcperson();"/>          </td>
    	
           
    	</tr>
    	<tr>
	     <td align="right" nowrap="nowrap"> 最高学历(<span style="color:red">*</span>)： </td>
    	  <td nowrap="nowrap">
			<select name="salesInfo_hd.education" id="education"  onchange="isCanExcellent();"> <!-- u by syy for L1864 on 20220826 -->
				<option value="">--请选择--</option>
				<c:forEach var="education" items="${education}">
					<option value="${education.codecode }">${education.codename}</option>
				</c:forEach>
			</select> 
          </td>
			<td align="right" nowrap="nowrap"> 专业：</td>
			<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.major" id="major" size="20" value=""/></td>
			<td align="right" nowrap="nowrap"> 是否相关专业：</td>
			<td nowrap="nowrap">
				<select name="salesInfo_hd.is_major" id="is_major"  >
					<option value="">--请选择--</option>
					<option value="0">否</option>
					<option value="1">是</option>
				</select>
			</td>
		</tr>
    	<tr>


    	 <td align="right">
					手机(<span style="color:red">*</span>):
		</td>
		<td nowrap="nowrap">
				<input type="text" class="ctlText" name="salesInfo_hd.mobile"
				id="mobile" onblur="checkMobile();" />
	    </td>

			<%
				if("04".equals(channel_id)){
			%>
			<td align="right" nowrap="nowrap">是否全日制(<span style="color:red">*</span>)：</td>
			<td nowrap="nowrap">
				<select class="ctlText" name="salesInfo_hd.is_full_time_education" id="is_full_time_education">
					<option value="">--请选择--</option>
						<c:forEach var="info" items="${is_full_time_education}">
							<option value="${info.codecode }">${info.codename}</option>
						</c:forEach>
				</select>
			</td>
			<td align="right" nowrap="nowrap"> 是否医护人员(<span style="color:red">*</span>)：</td>
			<td nowrap="nowrap">
				<select name="salesInfo_hd.medical_staff" id="medical_staff"  >
					<option value="1">否</option>
					<option value="0">是</option>
				</select>
			</td>
			<%
			}else{
			%>
			<td align="right" nowrap="nowrap"> 常住地址(<span style="color:red">*</span>)：</td>
			<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.home_address" id="home_address" size="20" value=""/></td>
			<%
				}
			%>
    	</tr>
    	<tr>
			<%
				if("04".equals(channel_id)){
			%>
			<td align="right" nowrap="nowrap"> 常住地址(<span style="color:red">*</span>)： </td>
			<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.home_address" id="home_address" size="20" value=""/></td>
			<%
			}
			%>

      	 <td align="right" nowrap="nowrap" >人员状态：</td>
    	  <td nowrap="nowrap">
    	  <select name="stat" id="stat" disabled="disabled">
			<option value="${stat.codecode}">${stat.codename}</option>
		 </select>
		 <input type="hidden" name="salesInfo_hd.stat" id="statHid" value="${stat.codecode}"/>

          </td>

           <td align="right" nowrap="nowrap">是否再次签约： </td>
    	  <td nowrap="nowrap">
			<select name="is_resigned" id="is_resigned" disabled="disabled">
				<c:forEach var="isresigned" items="${isresigned}">
					<option value="${isresigned.codecode }">${isresigned.codename}</option>
				</c:forEach>
			</select>
			<input name="salesInfo_hd.is_resigned" type="hidden" id="is_resigned_flag" value="" />
          </td>
			<td align="right" nowrap="nowrap"> 是否医护人员(<span style="color:red">*</span>)：</td>
			<td nowrap="nowrap">
				<select name="salesInfo_hd.medical_staff" id="medical_staff"  >
					<option value="1">否</option>
					<option value="0">是</option>
				</select>
			</td>
    </tr>
   <!--   <tr>


          <td align="right" nowrap="nowrap">原工号:</td>
		  <td nowrap="nowrap"><input type="text" class="ctlText" name="org_sales_code" id="oldSaleCode" readonly="readonly" size="20" maxlength="2000"/></td>
    </tr> -->
    	<tr>
	    	<td align="right" nowrap="nowrap">推荐人代码(<span style="color:red">*</span>)：</td>
			<td nowrap="nowrap">
				<input type="text" class="ctlText"name="salesInfo_hd.recommend_id" id="recommend_id" onblur="getName();" />
			</td>
			<td align="right" nowrap="nowrap">推荐人姓名：${channel_id}</td>
			<td nowrap="nowrap">
				<input type="text" class="ctlText" readonly="readonly" name="salesInfo_hd.recommend_name" id="recommend_name" />
			</td>

			<%--<td width="13%" align="right">是否共建队伍(<span style="color:red">*</span>)：</td>
			<td width="20%"><select class="select" name="salesInfo_hd.is_comprehensive" id="is_comprehensive" onblur="IsComprehensive(this);">
				<c:forEach var="iscomprehensive" items="${iscomprehensive}">
					<option id="option1" value="${iscomprehensive.codecode }">${iscomprehensive.codename}</option>
				</c:forEach>
			</select></td>--%>
			<td width="13%" align="right">是否共建队伍(<span style="color:red">*</span>)：</td>
			<td width="20%"><select class="select" name="salesInfo_hd.is_comprehensive" id="is_comprehensive" disabled="disabled" >
				<option id="option1" value="0" >否</option>
			</select>
			</td>
    	</tr>

         <tr>
    	 <td align="right" nowrap="nowrap">财险专员代码： </td>
    	  <td nowrap="nowrap">
	    	  <input type="text" class="ctlText"  name="salesInfo_hd.commissioner_code" readonly="readonly" id="commissioner_code" value="${commissioner_code}" size="20" <%--onblur="getCxzyName(this);--%>/>
	    	  <input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/></td>
    	  <td align="right" nowrap="nowrap">财险专员姓名： </td>
    	  <td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly"   name="salesInfo_hd.commissioner_name"  value="${commissioner_name}" id="commissioner_name" size="20"/></td>
			 <td align="right" nowrap="nowrap">财险介绍人代码： </td>
			 <td nowrap="nowrap">
				 <input type="text" class="ctlText"  name="salesInfo_hd.introduce_id" readonly="readonly" id="introduce_id" value="${introduce_id}" size="20" <%--onblur="checkIntroduceId(this);"--%>/>
				 <input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/></td>
         </tr>
         <tr>
    	  <td align="right" nowrap="nowrap">财险介绍人姓名： </td>
    	  <td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly"   name="salesInfo_hd.introduce_name"  value="${introduce_name}" id="introduce_name" size="20"<%-- onblur="checkIntroduceName(this);"--%>/></td>
			 <td align="right" nowrap="nowrap">组织代码：</td>
			 <td nowrap="nowrap">
				 <input type="text" class="ctlText" name="salesInfo_hd.team_id" readonly="readonly" id="team_id" onblur="getTeam(this);"/>
				 <input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/>
			 </td>
			 <td align="right" nowrap="nowrap">组织名称：</td>
			 <td nowrap="nowrap">
				 <input tyep="text" class="ctlText" readonly="readonly" name="salesInfo_hd.team_name" id="team_name"/>
			 </td>
         </tr>

		<tr>
			<td align="right" nowrap="nowrap" id="hide1">所属路线(<span style="color:red">*</span>)：</td>
			<td width="20%" id="hide2">
				<select name="salesInfo_hd.route_type" id="route_type" class="select" onchange="is_routetype();">
					<!-- <option value="">--请选择--</option> -->
					<c:forEach var="routetype" items="${routetypes}">
						<option value="${routetype.codecode}">
								${routetype.codename}
						</option>
					</c:forEach>
				</select>
			</td>
			<td align="right" nowrap="nowrap">业务职级(<span style="color:red">*</span>)：</td>
			<td width="20%">
				<select name="salesInfo_hd.rank" id="rank" class="select" onchange="show_isExcperson('select');">
					<option value="">--请选择--</option>
					<c:forEach var="rankcode" items="${rank}">
						<option value="${rankcode.rankid }" id="${rankcode.version }" title="${rankcode.persontype }">${rankcode.rankname}</option>
					</c:forEach>
				</select>
			</td>
    	</tr>
		<%-- m by wzj for L1003 on 2020-8-17 10:42:59 start--%>
    </table>
    </td>
  </tr>
	<%
	}else{
	%>
	<tr>
		<td>
			<table width="95%" border="1" align="center" cellpadding="2" cellspacing="0"  class="table_bk">
				<tr>
					<!--  <td align="right" nowrap="nowrap">互动经理代码:</td>
                     <td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly" name="salesInfo_hd.sales_id" id="sales_id" size="20" value=""/></td> -->
					<td align="right" nowrap="nowrap">用工性质(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<!--M by macj for BUG1321互动渠道-人员录入时数据库表中用工性质为空  on 2013.06.09 begin   -->
						<select name="salesInfo_hd.employ_kind" id="employ_kind" onchange="checkIsexcperson();">
							<!--M by macj for BUG1321互动渠道-人员录入时数据库表中用工性质为空  on 2013.06.09 end  -->
							<option value="2">代理制</option>
							<option value="1">合同制</option>
							<%--<c:forEach var="employ_kind" items="${employ_kind}">
                                    <option value="${employ_kind.codecode }">${employ_kind.codename}</option>
                            </c:forEach>
                        --%></select>
					</td>
					<%-- <td align="right" nowrap="nowrap">人员类别(<span style="color:red">*</span>):</td>
                         <td nowrap="nowrap">
                            <select name="salesInfo_hd.sales_type" id="sales_type">
                                <c:forEach var="ctype" items="${salesTypes}">
                                    <!-- <option value="03">寿险</option> -->
                                </c:forEach>
                                   <c:if test="${channel_id2 =='04'}">
                                      <option value="02">寿险</option>
                                    </c:if>
                                    <c:if test="${channel_id2 !='04'}">
                                       <option value="03">寿险</option>
                                    </c:if>
                            </select>
                          </td> --%>

					<td align="right" nowrap="nowrap">证件类型(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.id_type" id="id_type"  onchange="clearidno();">
							<c:forEach var="idtype1" items="${idtype}">

								<option value="${idtype1.codecode }">${idtype1.codename}</option>
							</c:forEach>
						</select>
					</td>

					<td align="right" nowrap="nowrap">证件号码(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText" name="salesInfo_hd.id_no" id="id_no" size="20" value=""  onblur="getIdNo();"/>
						<div  class="btn" name="authentication" onclick="getIdNo();"><span>验证是否存在</span></div>
					</td>

				</tr>
				<tr>




					<td align="right" nowrap="nowrap"> 人员姓名(<span style="color:red">*</span>)： </td>
					<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.sales_name" id="sales_name" size="20" value=""/></td>

					<td align="right" nowrap="nowrap">性别(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.sex" id="sex">
							<option value="">--请选择--</option>
							<c:forEach var="sex" items="${sex}">
								<option value="${sex.codecode }">${sex.codename}</option>
							</c:forEach>
						</select>
					</td>

					<td align="right" nowrap="nowrap">出生日期(yyyy-mm-dd)(<span style="color:red">*</span>)： </td>
					<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.birthday" id="birthday" size="20" value="" onkeyup="autoValidDate(this)" autocomplete="off" onClick="WdatePicker()" onblur="changeBirthday();checkValidDate(this)" onchange="checkIsexcperson();"/>          </td>


				</tr>
				<tr>
					<td align="right" nowrap="nowrap"> 最高学历(<span style="color:red">*</span>)： </td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.education" id="education"  onchange="isCanExcellent();"> <!-- u by syy for L1864 on 20220826 -->
							<option value="">--请选择--</option>
							<c:forEach var="education" items="${education}">
								<option value="${education.codecode }">${education.codename}</option>
							</c:forEach>
						</select>
						<input type="hidden" class="ctlText input" id="special_education" name="special_education" value=""/>
					</td>
					<td align="right" nowrap="nowrap"> 专业：</td>
					<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.major" id="major" size="20" value=""/></td>
					<td align="right" nowrap="nowrap"> 是否相关专业：</td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.is_major" id="is_major"  >
							<option value="">--请选择--</option>
							<option value="0">否</option>
							<option value="1">是</option>
						</select>
					</td>
				</tr>
				<tr>


					<td align="right">
						手机(<span style="color:red">*</span>):
					</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText" name="salesInfo_hd.mobile"
							   id="mobile" onblur="checkMobile();" />
					</td>

					<%
						if("04".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap">是否全日制(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<select class="ctlText" name="salesInfo_hd.is_full_time_education" id="is_full_time_education">
							<option value="">--请选择--</option>
							<c:forEach var="info" items="${is_full_time_education}">
								<option value="${info.codecode }">${info.codename}</option>
							</c:forEach>
						</select>
					</td>
					<td align="right" nowrap="nowrap"> 是否医护人员(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.medical_staff" id="medical_staff"  >
							<option value="1">否</option>
							<option value="0">是</option>
						</select>
					</td>
					<%
					}else{
					%>
					<td align="right" nowrap="nowrap"> 常住地址(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.home_address" id="home_address" size="20" value=""/></td>
					<%
						}
					%>
				</tr>
				<tr>
					<%
						if("04".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap"> 常住地址(<span style="color:red">*</span>)： </td>
					<td nowrap="nowrap"><input type="text" class="ctlText" name="salesInfo_hd.home_address" id="home_address" size="20" value=""/></td>
					<%
						}
					%>

					<td align="right" nowrap="nowrap" >人员状态：</td>
					<td nowrap="nowrap">
						<select name="stat" id="stat" disabled="disabled">
							<option value="${stat.codecode}">${stat.codename}</option>
						</select>
						<input type="hidden" name="salesInfo_hd.stat" id="statHid" value="${stat.codecode}"/>

					</td>

					<td align="right" nowrap="nowrap">是否再次签约： </td>
					<td nowrap="nowrap">
						<select name="is_resigned" id="is_resigned" disabled="disabled">
							<c:forEach var="isresigned" items="${isresigned}">
								<option value="${isresigned.codecode }">${isresigned.codename}</option>
							</c:forEach>
						</select>
						<input name="salesInfo_hd.is_resigned" type="hidden" id="is_resigned_flag" value="" />
					</td>
					<%
						if("01".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap"> 是否医护人员(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<select name="salesInfo_hd.medical_staff" id="medical_staff"  >
							<option value="1">否</option>
							<option value="0">是</option>
						</select>
					</td>
					<%
						}
					%>

				</tr>
				<!--   <tr>


                       <td align="right" nowrap="nowrap">原工号:</td>
                       <td nowrap="nowrap"><input type="text" class="ctlText" name="org_sales_code" id="oldSaleCode" readonly="readonly" size="20" maxlength="2000"/></td>
                 </tr> -->
				<tr>
					<td align="right" nowrap="nowrap">推荐人代码(<span style="color:red">*</span>)：</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText"name="salesInfo_hd.recommend_id" id="recommend_id" onblur="getName();" />
					</td>
					<td align="right" nowrap="nowrap">推荐人姓名：${channel_id}</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText" readonly="readonly" name="salesInfo_hd.recommend_name" id="recommend_name" />
					</td>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 start--%>
					<%
						if("04".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap">派驻机构：</td>
					<td nowrap="nowrap">
						<select class="ctlText" name="salesInfo_hd.accredit_org" id="accredit_org"><option value="">请选择</option></select>
					</td>
					<%
					}else{
					%>
					<td align="right" nowrap="nowrap">组织代码：</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText" name="salesInfo_hd.team_id" readonly="readonly" id="team_id" onblur="getTeam(this);"/>
						<input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/>
					</td>
					<%
						}
					%>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 end--%>
				</tr>
				<%
					if("04".equals(channel_id)){
				%>
				<tr>
					<td width="13%" align="right">是否共建队伍(<span style="color:red">*</span>)：</td>
					<td width="20%"><select class="select" name="salesInfo_hd.is_comprehensive" id="is_comprehensive" onblur="IsComprehensive(this);">
						<c:forEach var="iscomprehensive" items="${iscomprehensive}">
							<option id="option1" value="${iscomprehensive.codecode }">${iscomprehensive.codename}</option>
						</c:forEach>
					</select></td>
					<!-- <input name="salesInfo_hd.is_comprehensive" type="hidden" id="is_comprehensive" /> -->
					</td>

					<td align="right" nowrap="nowrap">财险专员代码： </td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText"  name="salesInfo_hd.commissioner_code" readonly="readonly" id="commissioner_code" value="${commissioner_code}" size="20" onblur="getCxzyName(this);"/>
						<input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/>
						<div  class="btn" name="authentication" onclick="checkAndMatch();"><span>验证并匹配</span></div>
					</td>
					<td align="right" nowrap="nowrap">财险专员姓名： </td>
					<td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly"   name="salesInfo_hd.commissioner_name"  value="${commissioner_name}" id="commissioner_name" size="20"/></td>

				</tr>
				<tr>
					<td align="right" nowrap="nowrap">财险介绍人代码： </td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText"  name="salesInfo_hd.introduce_id" readonly="readonly" id="introduce_id" value="${introduce_id}" size="20" onblur="checkIntroduceId(this);"/>
						<input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/></td>
					<td align="right" nowrap="nowrap">财险介绍人姓名： </td>
					<td nowrap="nowrap"><input type="text" class="ctlText" readonly="readonly"   name="salesInfo_hd.introduce_name"  value="${introduce_name}" id="introduce_name" size="20" onblur="checkIntroduceName(this);"/></td>

				</tr>
				<%
					}
				%>
				<tr>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 start--%>
					<%
						if("04".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap">组织代码：</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText" name="salesInfo_hd.team_id" readonly="readonly" id="team_id" onblur="getTeam(this);"/>
						<input type="hidden" class="ctlText" name="salesInfo_hd.version_id"  id="version_id"/>
					</td>
					<%
						}
					%>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 end--%>
					<td align="right" nowrap="nowrap">组织名称：</td>
					<td nowrap="nowrap">
						<input tyep="text" class="ctlText" readonly="readonly" name="salesInfo_hd.team_name" id="team_name"/>
					</td>
					<!-- L1864 syy start -->
					<%
						if("01".equals(channel_id)){
					%>
					<td align="right" nowrap="nowrap" id="hide1">所属路线(<span style="color:red">*</span>)：</td>
					<td width="20%" id="hide2">
						<select name="salesInfo_hd.route_type" id="route_type" class="select" onchange="is_routetype();">
							<!-- <option value="">--请选择--</option> -->
							<c:forEach var="routetype" items="${routetypes}">
								<option value="${routetype.codecode}">
										${routetype.codename}
								</option>
							</c:forEach>
						</select>
					</td>
					<%
						}
					%>
					<!-- L1864 syy end -->
					<td align="right" nowrap="nowrap">业务职级(<span style="color:red">*</span>)：</td>
					<td width="20%">
						<select name="salesInfo_hd.rank" id="rank" class="select" onchange="show_isExcperson('select');">
							<option value="">--请选择--</option>
							<c:forEach var="rankcode" items="${rank}">
								<option value="${rankcode.rankid }" id="${rankcode.version }" title="${rankcode.persontype }">${rankcode.rankname}</option>
							</c:forEach>
						</select>
					</td>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 start--%>
					<%
						if(!"04".equals(channel_id)){
					%>
					<td id="service_branch_id1" align="right" nowrap="nowrap" style="display:none">服务机构(<span style="color:red">*</span>)：</td>
					<td width="20%" id="service_branch_id2" style="display:none">
						<select name="salesInfo_hd.service_branch_id" id="service_branch_id" class="select" onchange="checkBranch_yb()" >
							<option value="">--请选择--</option>
							<c:forEach var="branchcode" items="${branch_id}">
								<option value="${branchcode.branch_id }">${branchcode.branch_name}</option>
							</c:forEach>
						</select>
					</td>
					<%
						}
					%>
					<%-- m by wzj for L1003 on 2020-8-17 10:42:59 end--%>
				</tr>
				<%-- m by wzj for L1003 on 2020-8-17 10:42:59 start--%>
				<%
					if("04".equals(channel_id)){
				%>
				<tr>
					<td id="service_branch_id1" align="right" nowrap="nowrap" style="display:none">服务机构(<span style="color:red">*</span>)：</td>
					<td width="20%" id="service_branch_id2" style="display:none">
						<select name="salesInfo_hd.service_branch_id" id="service_branch_id" class="select" >
							<option value="">--请选择--</option>
							<c:forEach var="branchcode" items="${branch_id}">
								<option value="${branchcode.branch_id }">${branchcode.branch_name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<%
					}
				%>
				<%-- m by wzj for L1003 on 2020-8-17 10:42:59 end--%>
			</table>
		</td>
	</tr>
	<%
		}
	%>
	<!-- L1536  2021/07/28 caoh add start -->
	<c:if test="${isExists == '1'}">
	<tr bgcolor="#dbe8fb">
		<td>>>近亲属相关信息：<label style="color: red;">注：1、最多可录入10条近亲属信息;<br/>
			2、如无需录入近亲属信息请勾选“该人员无需要填写的父母/配偶/子女近亲属信息”(勾选后可不填父母/配偶/子女近亲属信息，不勾选必填父母/配偶/子女近亲属信息),后续可通过【近亲属相关信息采集】菜单增加录入。
		</label></td>
	</tr>
	<tr>
		<td>
			<table width="95%" border="1" align="center" cellpadding="2" cellspacing="0"  class="table_bk">
				<tr name="salesRelatives">
					<td align="right" nowrap="nowrap">第<label>1</label>行</td>
					<td align="right" nowrap="nowrap">亲属姓名：</td>
					<td nowrap="nowrap">
						<input type="text" class="ctlText"  name="close_relatives_name" id="close_relatives_name" onblur="valiteClosedSalesName(this)" size="20"/>
					</td>
					<td align="right" nowrap="nowrap">性别：</td>
					<td nowrap="nowrap">
<%--						salesInfo_hd.salesinfoRelativesList[0].close_relatives_sex--%>
						<select name="close_relatives_sex" id="close_relatives_sex">
							<option value="">--请选择--</option>
							<c:forEach var="sex" items="${sex}">
								<option value="${sex.codecode }">${sex.codename}</option>
							</c:forEach>
						</select>
					</td>
					<td align="right" nowrap="nowrap">证件类型：</td>
					<td nowrap="nowrap">
<%--						salesInfo_hd.salesinfoRelativesList[0].close_relatives_id_type--%>
						<select name="close_relatives_id_type" id="close_relatives_id_type" onchange="validateIdType(this)">
<%--							<option  value="-1">请选择</option>--%>
<%--							<option value="1">身份证</option>--%>
<%--							<option value="2">军人证</option>--%>
<%--							<option value="3">出生证</option>--%>
<%--							<option value="4">异常身份证</option>--%>
<%--							<option value="5">回乡证</option>--%>
<%--							<option value="6">户口本</option>--%>
<%--							<option value="7">警官证</option>--%>
<%--							<option value="8">其他</option>--%>
							<c:forEach var="idtype1" items="${closeRelativeIdTypeList}">
								<option value="${idtype1.codecode }">${idtype1.codename}</option>
							</c:forEach>

						</select>
					</td>
					<td align="right" nowrap="nowrap">证件号码：</td>
					<td nowrap="nowrap">
<%--						salesInfo_hd.salesinfoRelativesList[0].close_relatives_id_no--%>
						<input type="text" class="ctlText"  name="close_relatives_id_no" id="close_relatives_id_no"  onblur="validateIdNo(this);"   size="20"/>
					</td>
					<td align="right" nowrap="nowrap">出生日期：</td>
					<td nowrap="nowrap">
<%--						salesInfo_hd.salesinfoRelativesList[0].close_relatives_birthday
							onblur="changeBirthday();checkValidDate(this)" onchange="checkIsexcperson();"
							onkeyup="autoValidDate(this)"
--%>
						<input type="text" class="ctlText" name="close_relatives_birthday" id="close_relatives_birthday" size="20" value="" autocomplete="off"  onClick="WdatePicker({dateFmt:'yyyy/MM/dd',alwaysUseStartDate:true})" />
					</td>
					<td align="right" nowrap="nowrap">近亲属关系：</td>
					<td nowrap="nowrap">
<%--						salesInfo_hd.salesinfoRelativesList[0].close_relatives_rela--%>
						<select name="close_relatives_rela" id="close_relatives_rela">
							<option value="">请选择</option>
<%--							<option value="01">父母</option>--%>
<%--							<option value="02">配偶</option>--%>
<%--							<option value="03">子女</option>--%>
<%--							<option value="04">祖父母</option>--%>
<%--							<option value="05">外祖父母</option>--%>
<%--							<option value="06">孙子女</option>--%>
<%--							<option value="07">外孙子女</option>--%>
<%--							<option value="08">兄弟姐妹</option>--%>
							<c:forEach items="${relative_name_list}" var="relative_name">
								<option value="${relative_name.codecode}">${relative_name.codename}</option>
							</c:forEach>
						</select>
					</td>
					<td align="right" nowrap="nowrap">
						<div class="btn" name="delRow" id="delRow" onclick="delRow1(this);"><span>删除</span></div>
					</td>
				</tr>
				<tr>
					<td align="left" colspan="7"  nowrap="nowrap">
						<input type="checkbox" id="box1" name="box1" onclick="checkRelative()"/>该人员无需要填写的父母/配偶/子女近亲属信息
					</td>
					<td align="right" colspan="7"  nowrap="nowrap">
						<div class="btn" name="addRow" id="addRow"  onclick="addRow1(this);"><span>添加</span></div>
					</td>
				</tr>
			</table>
			</td>
	</tr>
	</c:if>
	<script>
		var firstTr = $($("tr[name='salesRelatives']")[0]).clone(true);
		function freshRow(){
			var trs = $("tr[name='salesRelatives']");
			for(var i = 0; i < trs.length; i ++ ){
				var num = ((i+1)+"");
				$(trs[i]).children("td").first().children("label").html(num);
			}
		}

		function autoWriteDate(it){
			var idNoVal = $(it).val();
			var idType = $(it).parent().parent().find("select[name='close_relatives_id_type']").val();
			var birthday = "";
			if(idType == '01' && idNoVal != null && idNoVal.length > 0){//修改
				var reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
				var flag =  reg.test(idNoVal);
				if(flag){ //判断是否是合法的身份证号码
					if(idNoVal.length == 15){
						birthday = "19"+idNoVal.substr(6,6);
					} else if(idNoVal.length == 18){
						birthday = idNoVal.substr(6,8);
					}
					birthday = birthday.replace(/(.{4})(.{2})/,"$1/$2/");

					var close_relatives_birthday = $(it).parent().parent().find("input[name='close_relatives_birthday']");
					$(close_relatives_birthday).val(birthday);
					$(close_relatives_birthday).attr("disabled","disabled");
					//$(close_relatives_birthday).datepicker("setValue",birthday);
				}
			}

		}

		function valiteClosedSalesName(it){
			var closedsalesName = $(it).val();
			if(closedsalesName.indexOf(" ") != -1){
				alert("姓名前后及中间不能有空格!");
				return false;
			}
			if(strlen(closedsalesName) > 30){
				alert("姓名长度不能超过30个字符!");
				return false;
			}

			var idType = $(it).parent().parent().find("select[name='close_relatives_id_type']").val();
			var pattern2 = /^[A-Z\s\W]+$/;
			var pattern3 = /[\u4e00-\u9fa5]/;
			if (idType == "11" && closedsalesName!=null && closedsalesName != "" && closedsalesName.length > 0) {//修改
				if (!pattern2.test(closedsalesName) || pattern3.test(closedsalesName)) {
					alert("亲属姓名不符合港澳居民来往内地通行证（非中国籍）录入格式，只能录入英文名称");
					$(it).val("");
					return false;
				}
			}
		}

		function strlen(str){
			var len = 0;
			for (var i=0; i<str.length; i++) {
				var c = str.charCodeAt(i);
				//单字节加1
				if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
					len++;
				}
				else {
					len+=2;
				}
			}
			return len;
		}

		function validateIdType(it){
			var id_no = $("#id_no").val();
			var id_type = $("#id_type").val();
			var idType = $(it).val();
			var idNoVal = $(it).parent().parent().find("input[name='close_relatives_id_no']").val();
			var closeName = $(it).parent().parent().find("input[name='close_relatives_name']").val();

            debugger;
			var pattern2 = /^[A-Z\s\W]+$/;
			var pattern3 = /[\u4e00-\u9fa5]/;
			if (idType == "11" && closeName!=null && closeName!= "" && closeName.length > 0) {//修改
				if (!pattern2.test(closeName) || pattern3.test(closeName)) {
					alert("亲属姓名不符合港澳居民来往内地通行证（非中国籍）录入格式，只能录入英文名称");
					/*$(it).val("");*/
					$(it).parent().parent().find("input[name='close_relatives_name']").val("");
					/*document.getElementById("close_relatives_name").value = "";*/
					return false;
				}
			}
			var pattern = /^(HA|MA)\d{7}$/;
			if (idType == "11" && idNoVal!=null && idNoVal != "" && idNoVal.length > 0) {//修改
				if (!pattern.test(idNoVal)) {
					alert("证件号码录入不符合港澳居民来往内地通行证（非中国籍）证件格式");
					$(it).parent().parent().find("input[name='close_relatives_id_no']").val("");
					/*document.getElementById("close_relatives_id_no").value = "";*/
					return false;
				}
			}

			// alert(id_no+":"+idNoVal+","+id_type+":"+idType);

			if((id_type == '01' && idType == '01' && id_no == idNoVal) || (id_type == '03' && idType == '07' && id_no == idNoVal) || (id_type == '07' && idType == '02' && id_no == idNoVal)){
				alert("近亲属不允许为本人，请重新输入！");
				$(it).val("");
				return false;
			}

			
			//1.校验近亲属不能重复
			var idType_id = $(it).attr("id");
			var trs = $("tr[name='salesRelatives']");
			for(var i = 0; i < trs.length; i++){
				var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
				var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
				var tr_id = $(trs[i]).find("select[name='close_relatives_id_type']").attr("id");
				//alert(idType_id +":"+ tr_id+","+idType + ":"+close_relatives_id_type +","+idNoVal+":"+close_relatives_id_no);
				if(idType_id != tr_id){
					if(idType == close_relatives_id_type && idNoVal == close_relatives_id_no){
						//var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
						alert("证件类型和证件号码与已有亲属重复，请重新输入！");
						//$(it).parent().parent().find("select[name='close_relatives_id_type']").focus();
						$(it).val("")
						return false;
					}
				}
			}
			
		}

		function validateIdNo(it){
			var close_relatives_birthday = $(it).parent().parent().find("input[name='close_relatives_birthday']");
			$(close_relatives_birthday).removeAttr("disabled");
			var idNoVal = $(it).val();
			var idType = $(it).parent().parent().find("select[name='close_relatives_id_type']").val();
			//1.如果是身份证，验证身份证的合法性
			if (idType == "01" && idNoVal!=null && idNoVal != "" && idNoVal.length > 0) {//修改
				if (checkCnId(idNoVal) == false) {
					alert("您输入的身份证不合法!");
					$(it).val("");
					return false;
				}
			}
			var pattern = /^(HA|MA)\d{7}$/;
			if (idType == "11" && idNoVal!=null && idNoVal != "" && idNoVal.length > 0) {//修改
				if (!pattern.test(idNoVal)) {
					alert("证件号码录入不符合港澳居民来往内地通行证（非中国籍）证件格式");
					$(it).val("");
					return false;
				}
			}
			//1.校验近亲属不能重复
			var idNo_id = $(it).attr("id");
			var trs = $("tr[name='salesRelatives']");
			for(var i = 0; i < trs.length; i++){
				var close_relatives_id_type = $($(trs[i]).find("select[name='close_relatives_id_type']")[0]).val();
				var close_relatives_id_no = $($(trs[i]).find("input[name='close_relatives_id_no']")[0]).val();
				var tr_id = $(trs[i]).find("input[name='close_relatives_id_no']").attr("id");
				if(idNo_id != tr_id){
					if(idType == close_relatives_id_type && idNoVal == close_relatives_id_no){
						var close_relatives_name = $($(trs[i]).find("input[name='close_relatives_name']")[0]).val();
						alert("证件类型和证件号码重复，请重新输入！");
						//$(it).parent().parent().find("select[name='close_relatives_id_no']").focus();
						$(it).val("")
						return false;
					}
				}
			}

			//2.当录入近亲属证件类型+证件号码与销售人员本人一致
			var id_no = $("#id_no").val();
			var id_type = $("#id_type").val();
			//alert(id_no+":"+idNoVal+","+id_type+":"+idType);
			if((id_type == '01' && idType == '01' && id_no == idNoVal) || (id_type == '03' && idType == '07' && id_no == idNoVal) || (id_type == '07' && idType == '02' && id_no == idNoVal)){
				alert("近亲属不允许为本人，请重新输入！");
				$(it).val("");
				return false;
			}

			// if((id_no == idNoVal) && (id_type== '01' && idType == '01')){//修改
			// 	alert("近亲属不允许为本人，请重新输入！");
			// 	$(it).val("");
			// 	$(it).parent().parent().find("input[name='close_relatives_birthday']").val("");
			// 	return false;
			// }

			//3.如果校验都通过了，当填入的是身份证的时候，自动填充出生日期
			autoWriteDate(it);
		}

		function addRow1(it){
			var len = $(it).parent().parent().parent().children("tr").length;
			if(len >= 11){
				alert("最多可录入10条近亲属信息");
				return;
			}
			if(len == 1){
				$(firstTr).insertBefore($(it).parent().parent());
				return ;
			}
			var tr = $(it).parent().parent().prev().clone(true);
			var txt = $(tr).children("td").first().children("label").html();
			var numTxt = parseInt(txt)+1;
			$(tr).children("td").first().children("label").html(""+numTxt);
			//重新设置id
			$(tr).find("input[name='close_relatives_name']").attr("id",("close_relatives_name"+numTxt)).val("");
			$(tr).find("select[name='close_relatives_sex']").attr("id",("close_relatives_sex"+numTxt));
			$(tr).find("select[name='close_relatives_id_type']").attr("id",("close_relatives_id_type"+numTxt));
			$(tr).find("input[name='close_relatives_id_no']").attr("id",("close_relatives_id_no"+numTxt)).val("");
			$(tr).find("input[name='close_relatives_birthday']").attr("id",("close_relatives_birthday"+numTxt)).val("");
			$(tr).find("select[name='close_relatives_rela']").attr("id",("close_relatives_rela"+numTxt));

			$(it).parent().parent().prev().after(tr);
			freshRow();
		}

		function delRow1(it){
			var tr = $(it).parent().parent().remove();
			//alert(tr);
			//$(tr).parent().remove(tr);
			freshRow();
		}
	</script>
	<!-- L1536  2021/07/28 caoh add end -->
	<tr style="display:none">
    <td height="25">&nbsp;
    <input type="hidden" name="saveDa" id="saveDa" value="">
    </td>
  </tr>
  <tr>
    <td height="25">
	<table width="95%" border="1" align="center" cellpadding="2" cellspacing="0" class="table_bk">
  		<tr>
  		  <td width="6%" align="center">
  		  <input type="hidden" name="tempHidden" id="tempHidden" />
  		  <div class="btn" name="modify22" id="modify22" onclick="beforeSaveData();"><span>保存</span></div>
          <div  class="btn"  name='button2' id="button2" onclick="window.location.href='crossInsurance/agentInfoManage/preparePersonQueryList.jsp'"><span>返回 </span> </div>
	    </tr>
  	</table>	</td>
  </tr>
</table>
</table>
</form>
</div>
				</div>
			</div>
		</div>
      <div class="bottom_l"><div class="bottom_r"><div class="bottom_c"></div></div></div>
    </div>

</body>




</html>
