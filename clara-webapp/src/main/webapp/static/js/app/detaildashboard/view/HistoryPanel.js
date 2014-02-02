Ext.define('Clara.DetailDashboard.view.HistoryPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.historypanel',
	requires:[],
	title:'History',
	layout:'fit',
	border:false,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.History',
	features: [{
		ftype:'grouping',
		enableGroupingMenu:false,
        groupHeaderTpl: '{[values.rows[0].data.formTypeDesc]} (Form ID# {[values.rows[0].data.parentFormId]})'
	}],
	type:null,
	
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [{
			enableToggle: true,
			iconCls:'icn-ui-check-box-uncheck',
			text:'Group by Form',
			id:'btnToggleGroupHistory',
			pressed: false	
		},'->','-',{
    		xtype:'button',
    		id:'btnPrintHistory',
    		tooltip:'Print list (opens new window)',
    		tooltipType:'title',
    		iconCls:'icn-printer'
    	}]
	}],
	
	initComponent: function() { 
		var me = this,
		    objType = (me.type=='contract')?'edu.uams.clara.webapp.contract.domain.Contract':'edu.uams.clara.webapp.protocol.domain.Protocol';

		me.listeners = {
			activate:function(p){
				p.getStore().loadHistory(objType);
			}
		};
		
		me.columns = [{
			header:'Parent Form ID',
	    	dataIndex: 'parentFormId',
	    	hidden: true,
	    	menuDisabled:true
		},
		{
	    	header:'Form ID',
	    	dataIndex: 'formId',
	    	hidden: true,
	    	menuDisabled:true
	    },
	    {
            header: 'Date', width: 135,fixed:true, dataIndex: 'datetime',
            xtype: 'datecolumn', format: 'm/d/Y h:ia'
        },
        {
        	header: 'Note', dataIndex: 'desc',flex:1,renderer:function(v,p,r){
        	var html = "<div>"+v+"</div>";

        	var logNote = jQuery(html).find("span.log-committee-note-body");
        	
        	if (logNote.length > 0 && jQuery.trim(jQuery(logNote).text()) == "") {
        		
        		html = jQuery(html).clone().find(".log-committee-note").remove().end();
        		
        	} 
        	var h = "<div class='history-note'>";
        	h += "<div class='history-note-body'>"+jQuery(html).html()+"</div>";
        	h += "<div class='history-note-meta'>";
        	h += "<div class='history-meta-formevent'><span class='formType'>"+r.get("formType")+"</span>: <span class='eventType'>"+r.get("eventType")+"</span> by <span class='actor'>"+r.get("actor")+"</span></div>";
        	return h + "</div></div>";
        }},{
	    	header:'Form',
	    	dataIndex: 'formType',
	    	hidden: false,
	    	width:150,
	    	fixed:true,
	    	menuDisabled:true,
	    	renderer:function(v,p,r){
	    		
	    		var html = stringToHumanReadable(v);
	    		var linkableEvents = ["STUDY_SUBMITTED_TO_STAFFS","CONTINUING_REVIEW_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","CONTINUING_REVIEW_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","HUD_RENEWAL_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","HUD_RENEWAL_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","NEW_HUMAN_SUBJECT_RESEARCH_DETERMINATION_SUBMITTED","MODIFICATION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","MODIFICATION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","NEW_SUBMISSION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","NEW_SUBMISSION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED","REPORTABLE_NEW_INFORMATION_RESPONSE_TO_MAJOR_CONTINGENCIES_SUBMITTED_TO_IRB_OFFICE","REPORTABLE_NEW_INFORMATION_RESPONSE_TO_MINOR_CONTINGENCIES_SUBMITTED"];
	    		
	    		if (linkableEvents.hasValue(r.get("eventType"))){
	    			var summaryUrl = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+r.get("formId")+"/"+r.get("formType").toUrlEncodedName()+"/summary?noheader=true&review=false&historyid="+r.get("id");
	    			html += "<a href='"+summaryUrl+"' parent='_blank'><div class='icon-rowaction icn-blue-document-invoice' style='padding-left:20px;'>View summary</div></a>";
	    		}
	    		
	    		return html;
	    	}
	    }
	    
	    ];

		me.callParent();

	}
});