Ext.define('Clara.Queue.view.QueueItem.ContractPanel', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.contractqueueitemspanel',
	requires:['Ext.grid.*','Ext.data.*'],
	autoScroll: true,
	border: true,
	stripeRows: true,
	hideHeaders:false,
	store: 'Clara.Queue.store.QueueItems',
	multiSelect: false,
    cls:'queueitemspanel contractqueueitemspanel',
	initComponent: function() { 
		var me = this;


		me.columns = [
		              {
		            	  resizable:true,
		            	  width:350,
		            	  header:'Contract',
		            	  sortable:true,
		            	  dataIndex: 'identifier',
		            	  renderer:function(v,p,r){
		            		  var cid = r.get("identifier");
		            		  var logs = r.logs();
		            		  var hasLogsClass = (logs.count() > 0)?" hasLogs":"";
		            		  var logHtml = "";

		            		  var s = "<span style='font-weight:800;'>"+cid+"</span>";
		            		  if (r.get("formType") == "Amendment") s += " <span style='background-color:yellow;'>(Amendment)</span>";
		            		  if (r.get("metaType") != "") s+= ": "+Clara_HumanReadableType(r.get("metaType"),"-");
		            		  if (r.get("contractEntitySubtype") != "") s+= " ("+Clara_HumanReadableType(r.get("contractEntitySubtype"),"-")+")";


		            		  if (logs.count() > 0) {

		            			  logHtml = "<div class='wrap queueitem-logs'><h1>"+r.get("title")+"</h1><ol>";

		            			  logs.each(function(r){
		            				  logHtml += "<li><strong>"+r.get("time")+"</strong> - "+r.get("log")+"</li>";
		            			  });

		            			  logHtml += "</ol></div>";

		            		  }


		            		  return "<div class='wrap"+hasLogsClass+"'>"+s+"</div>"+logHtml;
		            	  }
		              },
		              {
		            	  resizable:true,
		            	  header:'IRB #',
		            	  sortable:true,
		            	  dataIndex: 'studyIdentifier'
		              },
		              {
		            	  header:'PI',
		            	  resizable:true,
		            	  dataIndex:'metaType',	// needed for gridprinter (ugh)
		            	  renderer: function(v,p,r){
		            		  var html = "<div class='wrap'>";
		            		  var sm = r.staffMembers();
		            		  sm.each(function(rec){
		            			  var isPI = rec.isPI();
		            			  if (isPI) html+= "<strong>"+rec.get("firstname")+" "+rec.get("lastname")+"</strong>";
		            		  });
		            		  return html+"</div>";
		            	  }
		              },
		              {
		            	  header:'Entity',
		            	  resizable:true,
		            	  dataIndex:'formId',	// needed for gridprinter (ugh)
		            	  renderer: function(v,p,r){
		            		  var html = "";
		            		  var sp = r.sponsors();
		            		  sp.each(function(rec){
		            			  html+= "<div class='wrap'><strong>"+rec.get("company")+"</strong>" + (rec.get("name")?(": "+rec.get("name")):"") + "</div>";
		            		  });
		            		  return html;
		            	  }
		              },
		              {
		            	  header:'Status',
		            	  dataIndex : 'formStatus',
			  			  width:225,
			  			  sortable : true,
			  			  renderer : function(v,p,r) {
			  					if (v && r.get("formCommitteeStatus") && (v != r.get("formCommitteeStatus"))) return "<div class='form-list-row form-status'>" +r.get("formCommitteeStatus")+"<br/>("+v+")</div>";
			  					return "<div class='form-list-row form-status'>" + v + "</div>";
			  			  }
		              },{
		            	  header:'Added',
		            	  sortable:true,
		            	  dataIndex:'formCommitteeStatusModified',
		            	  renderer: function(v){
		            		  return "<div data-qtip='"+Ext.Date.format(v, 'm/d/Y h:ia')+"'>"+moment(v).format('l');+"</div>";
		            	  }
		              },{
		            	  header : 'Assigned to',
		            	  sortable : true,
		            	  dataIndex:'isMine',	// needed for gridprinter (ugh)
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