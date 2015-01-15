Ext.define('Clara.DetailDashboard.view.LetterPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.letterpanel',
	requires:[],
	title:'Letters',
	layout:'fit',
	border:false,
	viewConfig: {
		stripeRows: true,
		trackOver:false
	},
	loadMask:true,
	store: 'Clara.DetailDashboard.store.Letters',
	features: [{
		ftype:'grouping',
		enableGroupingMenu:false,
		groupHeaderTpl:'{[values.rows[0].data["letterType"]]}'
	}],
	type:null,
	
	dockedItems: [{
		dock: 'top',
		border:false,
		xtype: 'toolbar',
		items: [
	    {iconCls:'icn-mail--plus',text:'New IRB Letter..',id:'btnNewIRBLetter',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER'])},
	    {iconCls:'icn-mail--pencil',text:'New Correction Letter..',disabled:true,id:'btnNewCorrectionLetter',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER'])},
	    {iconCls:'icn-mail--exclamation',text:'New Audit Report Letter..',id:'btnNewAuditReportLetter',hidden:!claraInstance.HasAnyPermissions(['CAN_SEND_IRB_LETTER'])},
		'->',{
			enableToggle: true,
			iconCls:'icn-ui-check-box-uncheck',
			text:'Group by Letter Type',
			id:'btnToggleGroupLetters',
			pressed: false	
		},'-',{
    		xtype:'button',
    		id:'btnPrintLetters',
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
				p.getStore().loadLetters(objType);
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
            header: 'Date', width: 135,fixed:true, dataIndex: 'timestampDate', xtype: 'datecolumn', format: 'm/d/Y h:ia'
        },
        {
	    	header:'Sender',
	    	dataIndex: 'actor',
	    	width:150
	    },
	    {
	    	header:'Letter Type',
	    	dataIndex: 'letterType'
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
        }}
	    
	    ];

		me.callParent();

	}
});