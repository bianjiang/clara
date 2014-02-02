Ext.ns('Clara.AgendaItem');

Clara.AgendaItem.MessageBus = new Ext.util.Observable();
Clara.AgendaItem.MessageBus.addEvents('afteragendaload');

var staffRoleReader = new Ext.data.XmlReader({
	record: 'role',
	fields: [{name:'role'}]
});

Clara.AgendaItem.AgendaStore = new Ext.data.GroupingStore({
	proxy: new Ext.data.HttpProxy({
		url: appContext, //changes dynamically
		method:"GET",
		headers:{'Accept':'application/xml;charset=UTF-8'}
	}),
	baseParams:{
		hideReported:true
	},
	autoLoad:false,
	groupField: 'category',
	reader: new Ext.data.XmlReader({
		record:'agenda-item',
		root: 'list',
		fields: [
			{name:'id', mapping:'@id'},
			{name:'category', mapping:'@category'},
			{name:'protocolFormType', mapping:'protocol-form>protocol-form-type'},
			{name:'xmlTitle', mapping:'xml-data>item>title'},
			{name:'xmlUrl', mapping:'xml-data>item>url'},
			{name:'studyType', mapping:'protocol-form>protocol-meta>protocol>study-type'},
			{name:'studyNature',mapping:'protocol-form>details>study-nature'},
			{name:'details',mapping:'protocol-form>details',convert:function(v,node){ return new Ext.data.XmlReader({record: 'value',fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:''}]}).readRecords(node).records; }},
			{name:'protocolFormTypeId', mapping:'protocol-form>protocol-form-type>@id'},
			{name:'protocolFormId', mapping:'protocol-form>@id'},
			{name:'protocolId', mapping:'protocol-form>protocol-meta>protocol>@id'},
			{name:'protocolTitle', mapping:'protocol-form>protocol-meta>protocol>title'},
			{name:'protocolFormStatus', mapping:'protocol-form>protocol-form-meta>status'},
			//{name:'protocolFormStatusDate', mapping:'protocol-form>status>modified', type: 'date', dateFormat: 'Y-m-d H:i:s.u'},
			{name:'reviewers', mapping:'reviewers', convert:function(v,node){
				return new Ext.data.XmlReader({
					record: 'reviewer',
					fields: [{name:'name', mapping:'name'}]
				}).readRecords(node).records; 
			}},
			{name:'pi',convert:function(v,node){ 
				// var isPI = false;
				
				var staffNode = jQuery(node).find("staff").filter(function(){
					return jQuery(this).find("role").text() == "Principal Investigator";
				});
				
				if (staffNode.length > 0) clog("staffNode",staffNode[0]);
				// jQuery(node).find("roles").find("role").each(function(){
				// 	isPI = isPI || (jQuery(this).text().toString() == "Principal Investigator");
				// });
				
				if (staffNode.length > 0) return new Ext.data.XmlReader({
				record: 'staff',
				fields: [{name:'firstname',mapping:'user>firstname'},
				         {name:'lastname',mapping:'user>lastname'},
				         {name:'email',mapping:'user>email'}
				]
			}).readRecords(staffNode[0]).records; }},
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
			
		]
	}),
	listeners:{
		load:function(){
			clog("LOADED");
			Clara.AgendaItem.MessageBus.fireEvent('afteragendaload', this);  
		}
	}
});

Clara.AgendaItem.Toolbar = Ext.extend(Ext.Toolbar, {
	id: 'clara-agendaitem-toolbar',
	agenda:{},
	previousItemIndex:0,
	nextItemIndex:0,
	constructor:function(config){		
		Clara.AgendaItem.Toolbar.superclass.constructor.call(this, config);
		Clara.AgendaItem.MessageBus.on('afteragendaload', this.onStoreLoad, this);
	},
	onStoreLoad: function(){
		var t = this;
		// Find current item to determine previous/next items for toolbar
		var currentIndex = Clara.AgendaItem.AgendaStore.find("id",Ext.getCmp("clara-protocol-dashboardpanel").agendaItem.id);
		
		t.previousItemIndex = -1;
		for (var prev=currentIndex-1;prev>-1;prev--){
			if (Clara.AgendaItem.AgendaStore.getAt(prev).get("category") != "MINUTES") {
				t.previousItemIndex = prev;
				break;
			}
		}

		t.nextItemIndex = -1;
		for (var next=currentIndex+1;next<(Clara.AgendaItem.AgendaStore.getCount() - 1);next++){
			if (Clara.AgendaItem.AgendaStore.getAt(next).get("category") != "MINUTES") {
				t.nextItemIndex = next;
				break;
			}
		}

		//t.previousItemIndex = (currentIndex > 0)?(currentIndex-1):-1;
		//t.nextItemIndex = (currentIndex < (Clara.AgendaItem.AgendaStore.getCount() - 1))?(currentIndex+1):-1;
		
		Ext.getCmp("btn-previous-item").setDisabled((t.previousItemIndex == -1));
		Ext.getCmp("btn-previous-item").setHandler(function(){
			var url = appContext + "/agendas/" + Ext.getCmp("clara-protocol-dashboardpanel").agenda.id + "/agenda-items/" + Clara.AgendaItem.AgendaStore.getAt(t.previousItemIndex).get("id") + "/view";
			location.href = url;
		});
		
		Ext.getCmp("btn-next-item").setDisabled((t.nextItemIndex == -1));
		Ext.getCmp("btn-next-item").setHandler(function(){
			var url = appContext + "/agendas/" + Ext.getCmp("clara-protocol-dashboardpanel").agenda.id + "/agenda-items/" + Clara.AgendaItem.AgendaStore.getAt(t.nextItemIndex).get("id") + "/view";
			location.href = url;
		});
	},
	initComponent: function(){
		var t = this;
		var config = {
				items:[{
						xtype:'panel',
						html:'Agenda: '+((typeof t.agenda != 'undefined' && t.agenda.date)?t.agenda.date:'ERROR: UNKNOWN DATE'),
						padding:4,
						unstyled:true,
						bodyStyle:'font-size:24px;background:transparent;',
						border:false
					   },'->',{
						   xtype:'button',
						   text:'Previous item',
						   id:'btn-previous-item',
						   disabled:true,
						   iconCls:'icn-control-double-180',
						   handler:function(){}
					   },{
						   xtype:'button',
						   text:'Jump to item..',
						   iconCls:'icn-control-eject',
						   handler:function(){
						   		new Clara.AgendaItem.AgendaListWindow({agenda:Ext.getCmp("clara-protocol-dashboardpanel").agenda}).show();
					   	   }
					   },{
						   xtype:'button',
						   text:'Next item',
						   disabled:true,
						   id:'btn-next-item',
						   iconCls:'icn-control-double',
						   handler:function(){}
					   }]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.AgendaItem.Toolbar.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendaitemtoolbar', Clara.AgendaItem.Toolbar);

Clara.AgendaItem.AgendaListWindow = Ext.extend(Ext.Window, {
	id: 'clara-agendaitem-listwindow',
	title:'Choose an agenda item..',
	layout:'fit',
	agenda:{},
	constructor:function(config){		
		Clara.AgendaItem.AgendaListWindow.superclass.constructor.call(this, config);
	},
	initComponent: function(){
		var t = this;
		var config = {
				items:[{
					xtype:'grid',
					border:false,
					loadMask:true,
					autoExpandColumn:'col-agenda-item-row-title',
					store: Clara.AgendaItem.AgendaStore,
					view: new Ext.grid.GroupingView({
			    		startCollapsed : false,
			    		showGroupName:false,
			    		enableGroupingMenu:false,
			    		groupTextTpl:'<div class="agenda-items-group-header">{text} ({[values.rs.length]} {[values.rs.length > 1 ? "items" : "item"]})</div>',
			    		getRowClass: function(record, index){
			    			return (record.get('reviewers').length == 0 && record.get('category') == 'FULL_BOARD')?'agenda-item-row-noreviewers':'';
			    		},
			    		emptyText:'<h1>There are no agenda items assigned to this date.</h1>Items are assigned by the IRB office once they have been approved by the appropirate committees.'
			    	}),
			    	columns: [
					        {header: 'Category', groupRenderer: function(v){ return Clara.Agenda.GetStatusText(v); }, sortable: true, dataIndex: 'category',hidden:true,id:'col-agenda-item-row-category'},
					        {header: 'IRB #', sortable: true, width:60, dataIndex: 'protocolId',renderer:function(v,p,r){return "<div class='agenda-list-row'>"+v+"</div>";}},
					        {header: 'Protocol Name', sortable: true, dataIndex: 'protocolTitle',id:'col-agenda-item-row-title',renderer:function(v,p,r){return "<div class='agenda-list-row'>"+v+"</div>";}},
					        {header: 'Type', width:245, sortable: true, dataIndex: 'protocolFormType',renderer:function(v,p,r){
						        	var str = "<div class='agenda-list-row'><h1>"+v+"</h1><h2>"+r.get("studyNature")+"</h2>";
						        	if (r.get("details").length > 0){
					        			str += "<dl class='protocol-form-row-details'>";
					        			var a = r.get("details");
					        			for (var i=0; i<a.length; i++) {
					        				str += "<dt>" + a[i].get("detailName") + "</dt>";
					        				str += "<dd>" + a[i].get("detailValue") + "</dd>";
					        			}
					        			str += "</dl>";
					        		}
						        	return str+"</div>";
						        }
					        }
					        ],
					listeners: {
							    rowdblclick: function(grid, rowI, event)   {
									var record = grid.getStore().getAt(rowI);
									clog(record);
									if (record.get("category") != "MINUTES"){
										var url = appContext + "/agendas/" + t.agenda.id + "/agenda-items/" + record.get("id") + "/view";
										location.href = url;
									} else {
										//
									}
							    }
							}
				}],
				modal:true,
				width:720,
				height:450,
				buttons:[{
					text:'Close',
					handler:function(){}
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.AgendaItem.AgendaListWindow.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claraagendaitemlistwindow', Clara.AgendaItem.AgendaListWindow);
