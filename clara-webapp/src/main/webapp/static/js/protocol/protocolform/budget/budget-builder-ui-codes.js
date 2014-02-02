Ext.ns('Clara.BudgetBuilder');

Clara.BudgetBuilder.CodeSearchStore = new Ext.data.Store({
	header :{
		'Accept': 'application/json'
	},
	baseParams:{
		codetype:'SNOMED_CT'
	},
	proxy: new Ext.data.HttpProxy({
		url:appContext+"/ajax/terminology/query-by-type-and-content",
		method:'POST',
		timeout:240000	// 4 minutes
	}),
	reader: new Ext.data.JsonReader({
		fields: [
		{name:'id', mapping:'identifier', type: 'string'},
		{name:'type', mapping:'codeType'},
		{name:'shortDesc', mapping:'description'},
		{name:'longDesc', mapping:'longDescription'}
	]})
});


Clara.BudgetBuilder.CodePanel = Ext.extend(Ext.Panel, {
    title: 'Other Procedure Codes',
    height: 340,
    layout: 'border',
    border:false,
    parentWindow:{},
	constructor:function(config){		
		Clara.BudgetBuilder.CodePanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		
		var t = this;
		t.parentWindow = this.ownerCt.ownerCt;
		readOnly = t.parentWindow.readOnly;
		//t.id = parentWindow.id + "_tCodes";
		var config = {
				items: [{region:'center', xtype:'claraProcedureCodesGridPanel', parentWindow:t}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.CodePanel.superclass.initComponent.apply(this, arguments);
		
		if (!readOnly){
			t.add({title:'Code Search',width:350,region:'west',xtype:'claraAltCodeSearchPanel', store:Clara.BudgetBuilder.CodeSearchStore, parentWindow:t});
		}
		
		if(t.parentWindow.procedure) t.reloadProcedureCodes();
	},
	addCode: function(recordIndex, custom, codeType){
		custom = custom || false;
		codeType = codeType || "";
		if (!custom){
			var st = Clara.BudgetBuilder.CodeSearchStore;
			var rec = st.getById(recordIndex);
			var c = new Clara.BudgetBuilder.ProcedureCode({id:rec.get("id"),type:rec.get("type"),description:rec.get("shortDesc")});
		} else {
			var c = new Clara.BudgetBuilder.ProcedureCode({id:codeType+"___"+recordIndex,type:codeType,description:codeType+": "+recordIndex});
		}
		this.parentWindow.procedure.addCode(c);
		this.reloadProcedureCodes();
	},
	removeCode: function(recordIndex){
		//cdebug("removeCode",recordIndex);
		this.parentWindow.procedure.removeCodeById(recordIndex);
		this.reloadProcedureCodes();
	},
	reloadProcedureCodes: function(){
		var t = this;
		if(t.parentWindow.procedure){
			var codes = t.parentWindow.procedure.getCodeArray();
			//cdebug("codes",codes);
			Ext.getCmp('gpProcedureCodes').getStore().loadData(codes);
			Ext.getCmp('gpProcedureCodes').getStore().sort("id","ASC");
		} else {
			//cdebug("[ERROR]","gpProcedureCodes: parentWindow has no proceure",t.parentWindow);
		}
	}
});
Ext.reg('claraCodePanel', Clara.BudgetBuilder.CodePanel);

Clara.BudgetBuilder.ProcedureCodesGridPanel = Ext.extend(Ext.grid.GridPanel,{
    id:'gpProcedureCodes',
    border:false,
	constructor:function(config){		
		Clara.BudgetBuilder.ProcedureCodesGridPanel.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var t = this;
		
		var config = {
				store: new Ext.data.ArrayStore({fields:['id','type','description']}),
				viewConfig: {
			        forceFit: true,
			        deferEmptyText:false,
			        emptyText:'No codes assigned to this procedure.'
			    },
				colModel: new Ext.grid.ColumnModel({
			        columns: [
			            {id: 'id', header: 'Codes for this procedure',sortable: true, dataIndex: 'id', renderer: function(v,s,r){
			         		var p = r.data;
			         		var recid = r.id;
			         		var pcid = (p.id.split("___").length > 1)?p.id.split("__")[1]:p.id;

			         		str = "<div class='proc-search-row'>";
			         		str = str + "<div class='proc-cptcode' style='float:none;'>"+pcid+"</div>";
			         		str = str + "<div class='proc-shortdesc' style='float:none;'>"+p.description+"</div></div>";
			         		str = str + "<div class='proc-longdesc'>"+p.type+"</div>";
			         		str = str + "<div class='proc-locations' style='text-align:right;'>";
			         		str = str + "<a class='proc-choose' href='javascript:Ext.getCmp(\""+t.parentWindow.id+"\").removeCode(\""+p.id+"\");'>Remove</a>";
			         		str = str + "</div>";
			         		return str + "</div>";
			         	}}
			        ]
			    })
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.BudgetBuilder.ProcedureCodesGridPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraProcedureCodesGridPanel', Clara.BudgetBuilder.ProcedureCodesGridPanel);

Clara.BudgetBuilder.CodeSearchPanel = Ext.extend(Ext.Panel,{
	constructor:function(config){		
		Clara.BudgetBuilder.CodeSearchPanel.superclass.constructor.call(this, config);
	},
	layout:'border',
	initComponent: function(){
		var t = this;
		var config = {
				items:[
				       {xtype:'panel', height:32,region:'north',border:false,layout:'hbox',items:[
							{
								xtype: 'combo',
								fieldLabel: 'Code Type',
								id:'code-codetype',
								typeAhead: true,
								allowBlank:false,
								triggerAction: 'all',
								store: new Ext.data.SimpleStore({
									fields:['d','id'],
									data: [['SNOMED','SNOMED_CT'],['ICD9','ICD_9'],['ICD10','ICD_10'],['SOFT','SOFT_ID']]
								}),
								lazyRender: true,
								displayField:'d',
								valueField:'id',
								value:'SNOMED',
								mode:'local',
								flex:1,
								selectOnFocus:true,
								anchor: '100%',
								listeners:{
									change:function(t,v){
										Clara.BudgetBuilder.CodeSearchStore.setBaseParam('codetype',v);
									}
								}
							},
							{ xtype:'uxsearchfield',
								store:t.store,
								//region:'north',
								flex:2,
								id:'code-searchfield',
								title:'Search for the procedure you wish to add..',
								emptyText:'Search or enter a code',
								paramName : 'content',
								listeners:{
									focus:function(t){ 
										////cdebug("fucusced.");
									}},
									beforeSearch: function(){
										return (jQuery.trim(this.getRawValue()).length > 2);
									}},
									{ xtype:'uxsearchfield',
										store:t.store,
										//region:'north',
										flex:2,
										id:'code-searchfield',
										title:'Search for the procedure you wish to add..',
										emptyText:'Search or enter a code',
										paramName : 'content',
										listeners:{
											focus:function(t){ 
												////cdebug("fucusced.");
											}
										},
										beforeSearch: function(){
												return (jQuery.trim(this.getRawValue()).length > 2);
											}
										},
											{   xtype:'button',
												text:'Add',
												flex:1,
												id:'code-btnadd',
												handler:function(){
													var code = Ext.getCmp("code-searchfield").getRawValue();
													if ((jQuery.trim(code).length == 0)) {
														alert("Enter a code to add first.");
													} else {
														Ext.getCmp(t.parentWindow.id).addCode(code,true,Ext.getCmp("code-codetype").getValue());
													}
												}
											}
							]},


		{
			xtype: 'grid',

			viewConfig: {
				forceFit: true,
				rowOverCls:'',
				emptyText: 'No search results.',
				headersDisabled:true
			},
			listeners: {
				render: function(grid) {
					grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
				}                
				      },
				     disableSelection:true,
				     stripeRows:true,
				     loadMask:true,
				     region: 'center',
				     itemId: 'cSearchResults',
				     border: false,
				     store:t.store,
				     id: 'cSearchResults',
				     columns: [
				         {
				             xtype: 'gridcolumn',
				             dataIndex: 'id',
				             header: 'Column',
				             sortable: true,
				             renderer: function(v,s,r){
				         		var p = r.data;
				         		var recid = r.id;

				         		str = "<div class='proc-search-row'>";
				         		str = str + "<div class='proc-cptcode' style='float:none;'>"+p.id+"</div>";
				         		str = str + "<div class='proc-shortdesc' style='float:none;'>"+p.shortDesc+"</div></div>";
				         		str = str + "<div class='proc-longdesc'>"+p.longDesc+"</div>";
				         		str = str + "<div class='proc-locations' style='text-align:right;'>";
				         		str = str + "<a class='proc-choose' href='javascript:Ext.getCmp(\""+t.parentWindow.id+"\").addCode(\""+recid+"\");'>Add</a>";
				         		str = str + "</div>";
				         		return str + "</div>";
				         	}
				             
				         }
				     ]
				    }
				 ]
			};
			Ext.apply(this, Ext.apply(this.initialConfig, config));
			Clara.BudgetBuilder.CodePanel.superclass.initComponent.apply(this, arguments);

	}
});
Ext.reg('claraAltCodeSearchPanel', Clara.BudgetBuilder.CodeSearchPanel);