Ext.ns('Clara','Clara.Contracts');

Clara.PageSize=20;
Clara.Contracts.StoreParmsSession = {
		searchingParams:{},
		filteringParams:{},
		pagingParams:{}
};

Clara.Contracts.SelectedContract = {};


Clara.Contracts.ContractListStore = new Ext.data.GroupingStore({
		proxy: new Ext.data.HttpProxy({
			url: appContext + "/ajax/contracts/list.xml", 		// this will be set dynamically as different bookmarks are chosen.
			method:"POST",
			headers:{'Accept':'application/xml;charset=UTF-8'}
		}),
		groupField:'contractIdentifier',
		autoLoad: false,
		// autoLoad: {params:{start: 0, limit: Clara.PageSize, searchCriterias:null}},
		sortInfo: {field:'contractId', direction:'DESC'},
		hasMultiSort:true,
		multiSortInfo: {
				sorters:[{
					field: 'contractId',
					direction:'DESC'
				},{
					field: 'timestamp',
					direction:'ASC'
				}
			],
			direction:'ASC'
		},
		reader: new Ext.data.XmlReader({
			record:'contract',
			root:'list',
			totalProperty:'@total',
			fields: [
			    {name:'title', mapping: 'title'},
			    {name:'created', mapping:'@created',type:'date',dateFormat:'m/d/Y'},
			    {name:'timestamp', mapping:'@timestamp'},
			    {name:'status',mapping:'status'},
			    {name:'formIndex',mapping:'@index'},
				{name:'priority',mapping:'status@priority'},
				{name:'studyType', mapping:'type'},
				{name:'contractId',mapping:'@id',type:'int'},
				{name:'contractIdentifier',mapping:'@identifier'},
				{name:'contractType',mapping:'@type'},
				{name:'studyIdentifier',mapping:'protocol'},
				{name:'contractEntityType',mapping:'type'},
				{name:'contractEntitySubtype',mapping:'type', convert:function(v,node){
					var stp = jQuery(node).find("type").find("sub-type:first").text();
					return stp;
				}},
				{name:'sponsors',convert:function(v,node){ return new Ext.data.XmlReader({
					record: 'sponsor',
					fields: ['name','company','title','department','phone','fax','email','address']
				}).readRecords(node).records; }},
				{name:'entity',mapping:'@entity'},	// will probably be a converted list later
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
				{name:'PI',mapping:'@pi'},			// will probably be a converted list later
				{
					name : 'assignedReviewers',
					mapping:'assigned-reviewers',
					convert : function(v, node) {
						var recs = new Ext.data.XmlReader(
								{
									record : '/committee-review/committee/assigned-reviewers/assigned-reviewer',
									idProperty:'@user-role-id',
									fields : [
											{
												name : 'reviewerName',
												mapping : '@user-fullname'
											},{
												name : 'reviewerId',
												mapping : '@user-id'
											},{
												name : 'reviewerRoleId',
												mapping : '@user-role-id'
											},{
												name : 'reviewerRoleName',
												mapping : '@user-role'
											},{
												name : 'assigningCommittee',
												mapping : '@assigning-committee'
											},{
												name : 'userRoleCommittee',
												mapping : '@user-role-committee'
											}]
								}).readRecords(node).records;
					    return recs;
					}
				}
		]}),
		listeners:{
			'exception':function(stuff){
				clog("EXCEPTION!");
				clog("EXCEPTION: Listing contracts");
				cwarn(stuff);
			}
		}
});

Clara.Contracts.ContractListPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'contract-list-panel',
    border: false,
    stripeRows: true,
    bodyStyle:'border-left:1px solid #8DB2E3;',
	constructor:function(config){		
		Clara.Contracts.ContractListPanel.superclass.constructor.call(this, config);
	},
	loadMask: true,
	clickableTitles:true,
	cls:'gpContractList',
	autoExpandColumn:'contract-col-id',
	autoExpandMax:3000,
	initComponent: function() {
		var t = this;
		
		var config = {
				hideHeaders:false, 

			    view: new Ext.grid.GroupingView({
		    		forceFit:true,
		    		showGroupName:false,
		    		startCollapsed : false,
		    		
		    	}),
			    listeners:{
			    	rowclick: function(grid, rowI, event)   {
						var record = grid.getStore().getAt(rowI);
						clog(record);
						if (typeof Clara.Contracts.SelectedContract != "undefined"){
							Clara.Contracts.SelectedContract = record;
							if (typeof Ext.getCmp("btnAddRelatedContract") != 'undefined') Ext.getCmp("btnAddRelatedContract").setDisabled(false);
						}
				    }
			    },
				columns: [
					{
						id: 'contract-col-id',
						hidden:true,
						dataIndex: 'contractIdentifier'
					},{
						resizable:true,
						width:350,
						header:'Contract',
						sortable:true,
						dataIndex: 'contractType',
						renderer:function(v,p,r){
							var cid = r.get("identifier") || r.get("contractId");
							if (t.clickableTitles && r.get("contractType") == "New Contract"){
								var s = "<a href='"+appContext+"/contracts/"+cid+"/dashboard' style='font-weight:800;'>"+r.get("contractIdentifier")+"</a>";
								if (r.get("contractEntityType") != "") s+= ": "+Clara.HumanReadableType(r.get("contractEntityType"),"-");
								if (r.get("contractEntitySubtype") != "") s+= " ("+Clara.HumanReadableType(r.get("contractEntitySubtype"),"-")+")";
								return "<div class='wrap'>"+s+"</div>";
							} else if (r.get("contractType") == "Amendment"){
								return v + " " +r.get("formIndex");
							} 
							else {
								return v;
							}
						}
					},{
						resizable:true,
						header:'IRB #',
						width:65,
						sortable:true,
						dataIndex: 'studyIdentifier',
						renderer:function(v,p,r){
							return "<a href='"+appContext+"/protocols/"+v+"/dashboard' style='font-weight:100;'>"+v+"</a>";
						}
					},{
						resizable:true,
						width:100,
						sortable:true,
						header:'PI',
						dataIndex: 'staffs',
						renderer:function(v,p,r){
							if (typeof r.get("staffs") == "undefined") return "";
							var pi = "<ul>";
							for (var i=0;i<r.get("staffs").length;i++){
								if (r.get("staffs")[i].get("isPI")) pi += "<li>"+r.get("staffs")[i].get("firstname")+" "+r.get("staffs")[i].get("lastname");
							}
							return pi+"</ul>";
						}
					},{
						resizable:true,
						width:100,
						sortable:true,
						header:'Entity',
						dataIndex: 'sponsors',
						renderer:function(v,p,r){
							if (typeof r.get("sponsors") == "undefined") return "";
							clog("sponsors",r);
							var pi = "<div class='wrap'><ul>";
							for (var i=0;i<r.get("sponsors").length;i++){
								pi += "<li>"+r.get("sponsors")[i].get("company");
								if (r.get("sponsors")[i].get("name") != "") pi += ": "+r.get("sponsors")[i].get("name");
							}
							return pi+"</ul></div>";
						}
					},{
						resizable:true,
						sortable:true,
						width:100,
						header:'Status',
						dataIndex: 'status',
						renderer: function(v,p,r){
							return (r.get("status") || r.get("formCommitteeStatus") || "Unknown status")+"";
						}
					},{
						resizable:true,
						sortable:true,
						header:'Created',
						dataIndex: 'created',
						renderer:function(v){
							return Ext.util.Format.date(v, 'm/d/Y');
						}
					},
					{
						header : 'Assigned To',
						dataIndex : 'assignedReviewers',
						sortable : true,
						width : 200,
						renderer : function(v) {
							var h = "<ul class='form-list-row form-assigned-reviewers'>";
								for(var i=0;i<v.length;i++) {
									clog(v[i]);
									h +="<li class='form-assigned-reviewer'>"+Clara.HumanReadableRoleName(v[i].data.reviewerRoleName)+": "+v[i].data.reviewerName + "</li>";
								}
								h += "</ul>";
									
							return h;
						}
					}
				],
			store:Clara.Contracts.ContractListStore,
			bbar:{	
			xtype:'paging',
		    store:Clara.Contracts.ContractListStore,
		    pageSize:Clara.PageSize,
		    displayInfo:true,
	    	listeners:{
	    		beforeChange:function(pt,params){
	    			if (Clara.Contracts.SearchKeyword != "") Clara.Contracts.ContractListStore.setBaseParam('keyword',Clara.Contracts.SearchKeyword);
	    		}
	    	}
			}
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		this.getStore().load({params:{start: 0, limit: Clara.PageSize, searchCriterias:null}});
		Clara.Contracts.ContractListPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractlistpanel', Clara.Contracts.ContractListPanel);