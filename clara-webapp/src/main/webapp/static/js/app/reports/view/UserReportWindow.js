Ext.define('Clara.Reports.view.UserReportWindow', {
    extend: 'Ext.window.Window',
    requires:['Clara.LetterBuilder.ux.RecipientField','Clara.Reports.view.ReportCriteriaGridPanel','Clara.Reports.view.ReportDisplayFieldGridPanel'],
    alias: 'widget.userreportwindow',
    title: 'Report Criteria',
    width:650,
    height:550,
    modal:false,
    
    style : 'z-index: -1;', // IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
    
    report:{},
    iconCls:'icn-gear',
    layout: {
        type: 'border'
    },
    save: function(){
    	var win = this,
    		data = {
    			description: Ext.getCmp("fldReportDescription").getValue(),
  				reportType: win.report.typeDescription,
  				globalOperator: Ext.getCmp("fldReportOperator").getValue(),
  				parameters:"<metadata><emails>"+Ext.getCmp("fldReportEmail").getEmailXMLValue()+"</emails></metadata>",
  				scheduleType:Ext.getCmp("fldSchedule").getValue()
    		},
    		msg:"Generating report, Please wait..."
    	});
    	lm.show();
    	clog("About to update",win.report);
    	jQuery.ajax({
			  type: 'POST',
			  
			  url: appContext+'/ajax/reports/'+win.report.id+'/update',
			  data: {
  				description: Ext.getCmp("fldReportDescription").getValue(),
  				reportType: win.report.typeDescription,
  				globalOperator: Ext.getCmp("fldReportOperator").getValue(),
  				cron:Ext.getCmp("fldSchedule").getValue(),
  				email:Ext.getCmp("fldReportEmail").getValue()
  			  },
			  success: function(data){
				  clog("update success",data);
				  lm.hide();
  		        if (!data.error){
  		        	Clara.Reports.app.fireEvent('userreportupdated', data);
          			win.close();
  		        }
			  },
			  error: function(){
				  lm.hide();
				  cwarn("Error creating new report");
			  }
		});
    },
    
    getEmailArray: function(store){
    	var emails = [];
    	
    		store.each(function(rec){
    			emails.push(rec);
    		});
    	
		  clog("About to return EMAILS",emails);
		  return emails;
    },
    
    initComponent: function() {
    	var t = this,
    		recipients = t.getEmailArray(Clara.Reports.app.getController("UserReport").selectedUserReport.recipients());
    
    	clog("INIT: with report",t.report,recipients);
    	t.items = [{
			region:'north',
			layout:'form',
			bodyPadding:6,
			border:false,
			height:118,
			items:[{
				xtype:'textfield',
				fieldLabel:'Description',
				id:'fldReportDescription',
				name:'fldReportDescription',
				allowBlank:false,
				labelWidth:160,
				value:(typeof t.report.description != 'undefined')?t.report.description:"New Report"
			},
			{xtype:'combo',id:'fldSchedule',labelWidth:160,fieldLabel:'When will this report run?',labelSeparator:'',hideLabel:false,store:new Ext.data.ArrayStore({
		    	  fields:['value','label'],
<<<<<<< HEAD
		    	  data: [["","Immediately (no schedule)"],["0 30 22 1/1 * ?","Daily"],["0 40 22 ? * SUN","Weekly"],["0 0 5 1 1/1 ?","Monthly (1st of every month)"]]
		      }),displayField:'label',valueField:'value',queryMode: 'local',autoSelect:true,value:(typeof t.report.cron != undefined && t.report.cron)?t.report.cron:""},
=======
		    	  data: [["NONE","Immediately (no schedule)"],["DAILY","Daily"],["WEEKLY","Weekly"],["MONTHLY","Monthly (1st of every month)"]]
		      }),displayField:'label',valueField:'value',queryMode: 'local',autoSelect:true,value:(typeof t.report.scheduleType != undefined && t.report.scheduleType)?t.report.scheduleType:"NONE"},
>>>>>>> claraoriginal/master
		      {
		    	  xtype:'recipientcombofield',
		    	  store:'ReportRecipients',
		    	  fieldLabel:'Email results to',
		    	  id:'fldReportEmail',
		    	  name:'fldReportEmail',
		    	  allowBlank: true
		      }
			]
		},{
			region:'center',
			xtype:'reportcriteriagridpanel',
			parentWindow:t,
			tbar:[{xtype:'tbtext',text:'<strong>Match</strong>'},
			      {xtype:'combo',id:'fldReportOperator',hideLabel:true,store:new Ext.data.ArrayStore({
			    	  fields:['value','label'],
			    	  data: [['OR','any'],['AND','all']]
			      }),displayField:'label',valueField:'value',queryMode: 'local',autoSelect:true,value:(typeof t.report.globalOperator != undefined)?t.report.globalOperator:"OR"},
			      {xtype:'tbtext',text:'<strong>of the criteria below:</strong>'},'->',{
			    	  xtype:'button',
			    	  text:'Add..',
			    	  iconCls:'icn-plus',
			    	  handler: function(){
			    		  var nwin = Ext.create('Clara.Reports.view.AddCriteriaWindow', {report:t.report,title:'New Search Criteria',modal:true});
			    		  nwin.show();
			    	  }
			      }]
		},{
			region:'south',
			title:'Choose which fields to show on the report:',
			xtype:'reportdisplayfieldgridpanel',
			border:false,
			parentWindow:t,
			split:true,
			tbar:[{
		    	  xtype:'button',
                  id:'btnAddDisplayFields',
		    	  text:'Add..',
		    	  iconCls:'icn-plus'
		      }],
			height:200
		}
    ];
    	t.buttons = [{text:'Done', handler:function(){
    		if (Ext.getCmp("fldReportDescription").validate()){
    			t.save();
    		} else {
    			alert("Description cannot be blank.");
    		}
    	}}];
    	
        t.callParent();
        clog("TRY to set email values");
        Ext.getCmp("fldReportEmail").setValue(recipients);
    }
});

