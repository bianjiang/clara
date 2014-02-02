Ext.ns('Clara','Clara.Protocols');

Clara.PageSize=20;
Clara.Protocols.StoreParmsSession = {
		searchingParams:{},
		filteringParams:{},
		pagingParams:{}
};

Clara.Protocols.SelectedProtocol = {};


Clara.Protocols.NameMappings = {
		'studyNature' : {
			'social-behavioral-education' : 'Social / Behavioral / Education',
			'biomedical-clinical' : 'Biomedical / Clinical',
			'hud-use' : 'HUD Use for Treatment/Diagnosis',
			'chart-review' : 'Chart Review Only'
		},
		'studyType' : {
			'industry-sponsored' : 'Industry Sponsored',
			'cooperative-group' : 'Cooperative Group',
			'investigator-initiated' : 'Investigator Initiated'
		}
};


Clara.Protocols.RowRenderer = function(v,p,r,withLinks,withQueueFormStatus){
	withLinks = withLinks || false;
	withQueueFormStatus = withQueueFormStatus || false;
	
	clog(v,p,r);
	var id = r.get("identifier") || r.get("protocolIdentifier") || r.get("protocolId");
	
	var title = (r.get("title") == "")?"<span class='no-title'>This item does not have a title.</span>":r.get("title");
	var url = appContext+"/protocols/";
	var studyType = Clara.Protocols.NameMappings.studyType[r.get("studyType")] || "";
	var studyNature = Clara.Protocols.NameMappings.studyNature[r.get("studyNature")] || "";
	
	var details = (typeof r.get("details") == 'undefined')?null:r.get("details");
	
	
	
	var statusClass = r.get("priority").toLowerCase();
	var idHtml = (withLinks)?("<div class='protocol-id'><a href='"+url+r.get("protocolId")+"/dashboard'>"+id+"</a></div>"):("<div class='protocol-id'>"+id+"</div>");
	
	title = (withLinks)?("<a href='"+url+r.get("protocolId")+"/dashboard'>"+title+"</a>"):title;
	var html = "<div class='protocol-row'>";
	
	if (withQueueFormStatus){
		var formCommitteeStatus = r.get("formCommitteeStatus") || "Unknown";
		var formStatus = r.get("formStatus") || "Unknown";
		html += "<div class='protocol-status protocol-row-status protocol-status-"+statusClass+"'>"+formCommitteeStatus;
		if (formCommitteeStatus != formStatus) html += " <span style='font-weight:100;'>(This form: <span style='font-weight:800;'>"+formStatus+"</span>)</span>";
		html += "</div>";
	}else{
		var status = (r.get("status") || r.get("formCommitteeStatus") || "Unknown status");
		html += "<div class='protocol-status protocol-row-status protocol-status-"+statusClass+"'>"+status+"</div>";
	}
	
	html+=idHtml+"<div class='protocol-desc'><h2 class='protocol-title'>"+title+"</h2>";

	
	
	
	
	html += "<div class='protocol-row-meta'>";

	if (r.get("budget") != ""){
		html += "<div ext:qtip='This protocol has a "+r.get("budget")+" budget.' class='protocol-row-budget budget-"+r.get("budget")+"'></div>";
	}
	
	html += (r.get("formType") != "")?"<div class='protocol-row-formtype'>"+r.get("formType")+"</div>":"";
	html += (studyNature != "")?"<span class='protocol-row-studyNature'>"+studyNature+"</span>":"";
	html += (studyType != "" && studyNature != "")?": ":"";
	html += (studyType != "")?"<span class='protocol-row-studyType'>"+studyType+"</span>":"";
	html += "<div style='clear:both;'>";
	
	if (details != null && details.length > 0){
			html += "<dl class='protocol-form-row-details'>";
			for (var i=0; i<details.length; i++) {
				html += "<dt>" + details[i].get("detailName") + "</dt>";
				html += "<dd>" + details[i].get("detailValue") + "</dd>";
			}
			html += "</dl>";
	}
	
	if (typeof r.get("staffs") != "undefined"){
		for (var i=0;i<r.get("staffs").length;i++){
			if (r.get("staffs")[i].get("isPI")) html+= "<div class='protocol-pi'>PI: <strong>"+r.get("staffs")[i].get("firstname")+" "+r.get("staffs")[i].get("lastname")+"</strong></div>";
		}
	}
	
	html += "</div></div>";

	
	return html;
};


Clara.Protocols.RowRendererWithoutLinks = function(v,p,r){
	return Clara.Protocols.RowRenderer(v,p,r,false,false);
};

Clara.Protocols.RowRendererWithLinks = function(v,p,r){
	return Clara.Protocols.RowRenderer(v,p,r,true,false);
};

Clara.Protocols.QueueRowRenderer = function(v,p,r){
	return Clara.Protocols.RowRenderer(v,p,r,false,true);
};

Clara.Protocols.SearchKeyword = "";
Clara.Protocols.ProtocolListStore = new Ext.data.XmlStore({
		proxy: new Ext.data.HttpProxy({
			url: appContext + "/ajax/protocols/list.xml", 		// this will be set dynamically as different bookmarks are chosen.
			method:"POST",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
		autoLoad: false,
		sortInfo: {field:'protocolId', direction:'DESC'},
		record:'protocol',
		root:'list',
		totalProperty:'@total',
		fields: [
		    {name:'title', mapping: 'title'},
			{name:'status',mapping:'status'},
			{name:'studyNature',mapping:'study-nature'},
			{name:'priority',mapping:'status@priority'},
			{name:'studyType', mapping:'study-type'},
			{name:'submissionType',mapping: '@type'},
			{name:'formType',mapping: '@type'},
			{name:'protocolId',mapping:'@id',type:'int'},
			{name:'budget',mapping:'protocol',convert:function(v,node){
				if (jQuery(node).find('budget-created').text() == "y") return "CLARA";
				else if (jQuery(node).find('crimson').find('has-budget').text() == "yes") return "CRIMSON";
				else return "";
			}},
			{name:'staffs',convert:function(v,node){ 
				return new Ext.data.XmlReader({
				record: 'staff',
				fields: [{name:'firstname',mapping:'user>firstname'},
				         {name:'lastname',mapping:'user>lastname'},
				         {name:'email',mapping:'user>email'},
				         {name:'isPI',mapping:'roles',convert:function(v,node){
				        	 	var isPI = false;
								
								jQuery(node).find("roles").find("role").each(function(){
									isPI = isPI || (jQuery(this).text().toString() == "Principal Investigator");
								});
								
								return isPI;
				         }}
				]
			}).readRecords(node).records; }},
			{name:'protocolIdentifier',mapping:'@identifier'}
		],
		listeners:{
			'exception':function(stuff,type,action,options,response,arg){
				clog("EXCEPTION: Listing protocols");
				clog(stuff,type,action,options,response,arg);
			}
		}
});

Clara.Protocols.ProtocolListPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'protocol-list-panel',
    border: false,
    stripeRows: true,
    clickableTitles:true,
    bodyStyle:'border-left:1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.Protocols.ProtocolListPanel.superclass.constructor.call(this, config);
	},
	listeners:{
    	rowclick: function(grid, rowI, event)   {
			var record = grid.getStore().getAt(rowI);
			if (typeof Clara.Protocols.SelectedProtocol != "undefined"){
				Clara.Protocols.SelectedProtocol = record;
				if (typeof Ext.getCmp("btnAddRelatedProtocol") != 'undefined') Ext.getCmp("btnAddRelatedProtocol").setDisabled(false);
			}
	    }
    },
	loadMask: true,

    hideHeaders:true, 
	
	autoExpandColumn:'protocol-col-name',
	autoExpandMax:3000,
	initComponent: function() {
		var t = this;
		var config = {
			store:Clara.Protocols.ProtocolListStore,
			view: new Ext.grid.GridView({
				rowOverCls:'',
				selectedRowClass:(t.clickableTitles)?'':'x-grid3-row-selected',
				bodyCssClass:'protocol-list',
				headersDisabled:true
		    }),
			columns: [
			  		{
			  			id: 'protocol-col-name',
			  			resizable:false,
			  			header:'Title / Study Type',
			  			dataIndex: 'title',
			  			renderer:(t.clickableTitles)?Clara.Protocols.RowRendererWithLinks:Clara.Protocols.RowRenderer
			  		}
			  	],
			bbar:{	
				xtype:'paging',
		    	store:Clara.Protocols.ProtocolListStore,
		    	pageSize:Clara.PageSize,
		    	displayInfo:true,
		    	listeners:{
		    		beforeChange:function(pt,params){
		    			if (Clara.Protocols.SearchKeyword && Clara.Protocols.SearchKeyword != "") Clara.Protocols.ProtocolListStore.setBaseParam('keyword',Clara.Protocols.SearchKeyword);
		    		}
		    	}
			}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		t.getStore().load({params:{start: 0, limit: Clara.PageSize, searchCriterias:null}});
		Clara.Protocols.ProtocolListPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraprotocollistpanel', Clara.Protocols.ProtocolListPanel);