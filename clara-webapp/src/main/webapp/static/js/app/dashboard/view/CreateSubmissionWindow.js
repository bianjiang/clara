var submissionWindowHtml = [];

submissionWindowHtml.push("<div class='wizardrow dashboard-wizard-newstudy'><h1><a href='"+appContext+"/protocols/protocol-forms/new-submission/create'>New Study</a></h1><h3>Use this form to submit a new research protocols and HUD requests.</h3>"
		+ "<ul><li>This includes expanded access studies (compassionate use, single use).</li></ul></div>"
		+ "<div class='wizardrow dashboard-wizard-hsrd'><h1><a href='"+appContext+"/protocols/protocol-forms/human-subject-research-determination/create'>Human Subject Research Determination Request</a></h1>Uncertain whether your research involves the <strong>use of human subjects</strong>? Begin by filling out this form.</div>"
		+ "<div class='wizardrow dashboard-wizard-emer'><h1><a href='"+appContext+"/protocols/protocol-forms/emergency-use/create'>Emergency Use Notification/Follow-up Report</a></h1>This includes notifications and follow-up reports.</div>");

if (claraInstance.HasAnyPermissions(['CAN_CREATE_CONTRACT'])){ 
	submissionWindowHtml.push('<div class="wizardrow dashboard-wizard-cont"><h1><a href="'+appContext+'/contracts/contract-forms/new-contract/create">New Contract</a></h1><h2>A contract is required if one of the following conditions exists:</h2><ul>'
	        + '<li>You are receiving funding  from an individual or an entity outside of UAMS.</li>'
	       	+ '<li>You are receiving a drug from an individual or an entity outside of UAMS.</li>'  
	       	+ '<li>You are receiving a device from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing research data to an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing or receiving any biological materials, animals or other materials to or from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are in a collaborative research arrangement with an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing a Limited Data Set of Protected Health Information to or from an entity outside of UAMS.</li>' 
	       	+ '<li>You are using an individual or an entity outside of UAMS as a subcontractor.</li>' 
	       	+ '<li>You are subcontractor doing work for an individual or entity outside of UAMS.</li>' 
	       	+ '</ul><br/><h2>Please follow the quesitons and instructions in the New Contract form.</h2><br/>'
	       	+ 'If you are not sure, please send your contract request to <a href="mailto:ResearchContracts@uams.edu">ResearchContracts@uams.edu</a> or contact (501)526-6808.<br/><span style="color:red;font-weight:800;">Information for studies requiring IRB review should begin on the Protocol side of the CLARA system.  Go to the first tab and select "I want to submit a New Study" </span></div>');
} else {
	submissionWindowHtml.push('<div class="wizardrow dashboard-wizard-cont"><h1>New Contracts</h1><h2>A contract is required if one of the following conditions exists:</h2><ul>'
	        + '<li>You are receiving funding  from an individual or an entity outside of UAMS.</li>'
	       	+ '<li>You are receiving a drug from an individual or an entity outside of UAMS.</li>'  
	       	+ '<li>You are receiving a device from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing research data to an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing or receiving any biological materials, animals or other materials to or from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are in a collaborative research arrangement with an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing a Limited Data Set of Protected Health Information to or from an entity outside of UAMS.</li>' 
	       	+ '<li>You are using an individual or an entity outside of UAMS as a subcontractor.</li>' 
	       	+ '<li>You are subcontractor doing work for an individual or entity outside of UAMS.</li>' 
	       	+ '</ul><br/>);
	     

}
submissionWindowHtml.push("<div class='wizardrow dashboard-wizard-wikihelp'><h1>CLARA Help</h1><h3>Looking for online help? Visit the <a href='https://clara.uams.edu/wiki/doku.php?id=start'>Clara Help site</a>.</h3></div>"
		+ "<div class='wizardrow dashboard-wizard-irb'><h1>Contact the IRB Office</h1>E-mail the IRB.</div>" // TODO: Change to your IRB page
		+ "<div class='wizardrow dashboard-wizard-account'><h1><a href='"+appContext+"/user/profile'>My CLARA account</a></h1><h2><a href='"+appContext+"/user/profile'>Visit this page</a> to:</h2><ul><li>Change your password</li><li>Upload or update your CV</li><li>View your security settings</li></ul></div>");

Ext.define('Clara.Dashboard.view.CreateSubmissionWindow', {
	extend: 'Ext.window.Window',
	requires:[],
	alias: 'widget.createsubmissionwindow',
	title: '',
	width:650,
	modal:true,
	height:500,
	activeTab:0,
	layout: {
		type: 'border'
	},

	initComponent: function() {
		var me = this;
		me.listeners={
				show:function(w){
					w.down("tabpanel").setActiveTab(me.activeTab);
				}
		};

		me.items = [{
			html : "<div class='window-header' style='background-color:#dce6f5;border-bottom:0px;'><h1 class='window-header-title'>What do you want to do?</h1></div>",
			border : false,
			region : 'north'
		},{
			xtype:'tabpanel',
			region:'center',
			items:[{
				xtype : 'panel',
				padding:6,
				iconCls:'icn-book-open-list',
				title : "<h1 class='header-title'>I want to submit a(n)...</h1>",
				html:submissionWindowHtml[0]
			},
			{
				xtype : 'panel',
				padding:6,
				iconCls:'icn-certificate',
				title : "<h1 class='header-title'>Create a New Contract</h1>",
				html:submissionWindowHtml[1]
			},
			{
				xtype : 'panel',
				iconCls:'icn-question',
				padding:6,
				title : "<h1 class='header-title'>Find Help</h1>",
				html:submissionWindowHtml[2]
			}]
		}];
		me.buttons = [{text:'Close', handler:function(){me.close();}}];

		me.callParent();
	}
});

