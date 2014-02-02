Ext.define('Clara.Common.view.ProtocolGridPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.commonprotocolgridpanel',
	autoScroll: true,
	cls:'protocolgridpanel',
	stripeRows: true,
	hideHeaders:false,
	pageSize:25,
	clickableRows:false,
	includeQueueStatus:false,
	store: 'Clara.Common.store.Protocols',
	initComponent: function() { 
		var me = this;

		me.columns = [
		              {
		            	  header:'',align:'right',sortable:false,dataIndex:'identifier',width:54,renderer:function(v,p,r){
		            		  return "<div class='protocol-id'>"+((me.clickableRows)?("<a href='"+appContext+"/protocols/"+r.get("id")+"/dashboard"+"'>"+v+"</a>"):v)+"</div>";
		            	  }
		              },
		              {
		            	  header:'',sortable:false,dataIndex:'title',flex:1,renderer:function(v,p,r){
		            		  var id = r.get("id");
		            		  var title = (r.get("title") == "")?"<span class='no-title'>This item does not have a title.</span>":r.get("title");
		            		  var url = appContext+"/protocols/";
		            		  var studyType = r.get("studyTypeDesc");
		            		  var studyNature = r.get("studyNatureDesc");


		            		  title = (me.clickableRows)?("<a href='"+url+id+"/dashboard'>"+title+"</a>"):title;

		            		  var html = "<div class='protocol-row'><div class='protocol-desc' style='padding-left:0px;'><h2 class='protocol-title'>"+title+"</h2><div class='protocol-row-meta' style='padding-left:12px;'>";
		            		  if (r.get("budget") != ""){
		            			  html += "<div ext:qtip='This protocol has a "+r.get("budget")+" budget.' class='protocol-row-budget budget-"+r.get("budget")+"'></div>";
		            		  }

		            		  html += (r.get("formType") != "")?"<div class='protocol-row-formtype'>"+r.get("formType")+"</div>":"";
		            		  html += (studyNature != "")?"<span class='protocol-row-studyNature'>"+studyNature+"</span>":"";
		            		  html += (studyType != "" && studyNature != "")?": ":"";
		            		  html += (studyType != "")?"<span class='protocol-row-studyType'>"+studyType+"</span>":"";
		            		  html += "<div style='clear:both;'>";

		            		  var dst = r.itemDetails();
		            		  if (dst.count() > 0) {
		            			  str += "<dl class='protocol-form-row-details'>";

		            			  dst.each(function(rec){
		            				  if (i > 0) html+="<br/>";
		            				  i++;
		            				  str += "<dt>" + rec.get("detailName") + "</dt>";
		            				  str += "<dd>" + rec.get("detailValue") + "</dd>";
		            			  });
		            			  str += "</dl>";
		            		  }





		            		  if (r.get("collegeId") && r.get("collegeId") > -1){
		            			  html += "<div class='protocol-dept'>Department: <span class='college'>"+r.get("collegeDesc")+"</span>";
		            			  if (r.get("deptId")){
		            				  html += "(<span class='dept'>"+r.get("deptDesc")+"</span>";
		            				  if (r.get("sebdeptId")){
		            					  html += ": <span class='subdept'>"+r.get("subdeptDesc")+"</span>)";
		            				  } else {
		            					  html += ")"
		            				  }
		            			  }

		            			  html += "</div>";
		            		  }

		            		  return html+"</div></div>";
		            	  }
		              },
		              {
		            	  header:'',width:150,renderer:function(v,p,r){
		            		  var html="";
		            		  var sm = r.staffMembers();
		            		  sm.each(function(rec){
		            			  var isPI = rec.isPI();
		            			  if (isPI) html+= "<div class='wrap protocol-pi'>PI: <strong>"+rec.get("firstname")+" "+rec.get("lastname")+"</strong></div>";
		            		  });
		            		  return html;
		            	  }
		              },
		              {
		            	  header:'',align:'right',sortable:false,dataIndex:'status',width:150,renderer:function(v,p,r){
		            		  var statusClass = (r.get("priority"))?r.get("priority").toLowerCase():"";
		            		  var html = "<div class='wrap'>";
		            		  if (me.includeQueueStatus){
		            			  var formCommitteeStatus = r.get("formCommitteeStatus") || "Unknown";
		            			  var formStatus = r.get("formStatus") || "Unknown";
		            			  html += "<div class='protocol-status protocol-row-status protocol-status-"+statusClass+" status-"+camelize(formStatus)+"'>"+formCommitteeStatus;
		            			  if (formCommitteeStatus != formStatus) html += " <span style='font-weight:100;'>(This form: <span style='font-weight:800;'>"+formStatus+"</span>)</span>";
		            			  html += "</div>";
		            		  }else{
		            			  var status = (r.get("status") || r.get("formCommitteeStatus") || "Unknown status");
		            			  html += "<div class='protocol-status protocol-row-status protocol-status-"+statusClass+" status-"+camelize(status)+"'>"+status+"</div>";
		            		  }

		            		  return html+"</div>";
		            	  }
		              }
		              ];
		me.listeners = {
				added: function(){
					me.getStore().load({
						params:{
							start: 0, 
							limit: me.pageSize, 
							searchCriterias:null
						}
					});
				}	
		};
		me.callParent();
	}
});
