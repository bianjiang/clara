Ext.define('Clara.Queue.view.QueueItem.ProtocolPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.protocolqueueitemspanel',
	requires:['Ext.grid.*','Ext.data.*'],
	autoScroll: true,
	border: true,
	stripeRows: true,
	hideHeaders:false,
	store: 'Clara.Queue.store.QueueItems',
	multiSelect: false,
	cls:'queueitemspanel protocolqueueitemspanel',
	initComponent: function() { 
		var me = this;
		me.columns = [
		              {
		            	  header:'IRB #',align:'right',sortable:false,dataIndex:'identifier',width:54,renderer:function(v,p,r){
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

		            		  var html = "<div class='protocol-row'><div class='protocol-desc' style='padding-left:0px;'><h2 class='protocol-title'>"+title+"</h2><div class='protocol-row-meta'>";
		            		  //if (typeof r.get("budget") != "undefined" && r.get("budget") != ""){
		            		  //  html += "<div ext:qtip='This protocol has a "+r.get("budget")+" budget.' class='protocol-row-budget budget-"+r.get("budget")+"'></div>";
		            		  //}

		            		  html += (r.get("formType") != "")?"<div class='protocol-row-formtype'>"+r.get("formType")+"</div>":"";
		            		  html += (studyNature != "")?"<span class='protocol-row-studyNature'>"+studyNature+"</span>":"";
		            		  html += (studyType != "" && studyNature != "")?": ":"";
		            		  html += (studyType != "")?"<span class='protocol-row-studyType'>"+studyType+"</span>":"";
		            		  html += "<div style='clear:both;'>";

		            		  var dst = r.itemDetails();
		            		  if (dst.count() > 0) {
                                  html += "<dl class='protocol-form-row-details'>";

		            			  dst.each(function(rec){
		            				  if (i > 0) html+="<br/>";
		            				  i++;
                                      html += "<dt>" + rec.get("detailName") + "</dt>";
                                      html += "<dd>" + rec.get("detailValue") + "</dd>";
		            			  });
                                  html += "</dl>";
		            		  }


		            		  var sm = r.staffMembers();
		            		  sm.each(function(rec){
		            			  var isPI = rec.isPI();
		            			  if (isPI) html+= "<div class='protocol-pi'>PI: <strong>"+rec.get("firstname")+" "+rec.get("lastname")+"</strong></div>";
		            		  });

                              var wst = r.itemWarnings();
                              if (wst.count() > 0) {
                                  html += "<div class='protocol-form-warnings'><h1>Warning</h1>";
                                  html += "<ul class='protocol-form-row-warnings'>";

                                  wst.each(function(rec){
                                      if (i > 0) html+="<br/>";
                                      i++;
                                      html += "<li><strong>" + rec.get("category") + "</strong>: "+ rec.get("warning") +"</li>";
                                  });
                                  html += "</ul></div>";
                              }

		            		  return html+"</div></div>";
		            	  }
		              },{
		            	  header:'Status',sortable:false,dataIndex:'status',width:250,renderer:function(v,p,r){
		            		  var statusClass = (r.get("priority"))?r.get("priority").toLowerCase():"";
		            		  var html = "<div class='wrap'>";

		            			  var formCommitteeStatus = r.get("formCommitteeStatus") || "Unknown";
		            			  var formStatus = r.get("formStatus") || "Unknown";
		            			  html += "<div class='protocol-status protocol-row-status protocol-status-"+statusClass+"'>"+formCommitteeStatus;
		            			  /* if (formCommitteeStatus != formStatus) */ html += "<br/><span style='font-weight:100;'>(This form: <span style='font-weight:800;'>"+formStatus+"</span>)</span>";
		            			  html += "</div>";

		            		  return html+"</div>";
		            	  }
		              },{
		            	  header:'Added',
		            	  sortable:true,
		            	  dataIndex:'formCommitteeStatusModified',
		            	  renderer: function(v){
		            		  //var now = new Date();
		            		  //if ((now - v)/(1000*60*60*24) > 180) {
		            			  return "<div>"+Ext.Date.format(v, 'm/d/Y h:ia')+"</div>";
		            		  //}
		            		  //else {
		            		//	  return "<div data-qtip='"+Ext.Date.format(v, 'm/d/Y h:ia')+"'>"+moment(v).fromNow()+"</div>";
		            		 // }
		            	  }
		              },{
		            	  header : 'Assigned to',
		            	  sortable : true,
		            	  dataIndex:'metaType',	// needed for gridprinter (ugh)
		            	  width : 230,
		            	  renderer : function(v,p,r) {

		            		  var st = r.assignedReviewers();
		            		  var html = "<ul class='form-list-row form-assigned-reviewers'>";

		            		  st.each(function(r){
		            			  html += "<li class='form-assigned-reviewer'>"+Clara_HumanReadableRoleName(r.get("reviewerRoleName"))+": "+r.get("reviewerName") + "</li>";
		            		  });

		            		  return html + "</ul>";

		            	  }
		              }
		              ];

		me.callParent();

	}
});