Ext.ns('Clara.Queues');

Clara.Queues.ShowHistory = false;

Clara.Queues.MessageBus = new Ext.util.Observable();
Clara.Queues.MessageBus.addEvents('queueselected', 'queuesloaded',
		'queueitemselected', 'queueitemupdated', 'queueloaded');


Clara.Queues.SelectedQueueItem = {};

Clara.Queues.MessageBus.addListener('queueitemselected', function(
		queueitem) {
	claraInstance.form.type = queueitem.formTypeId;
	claraInstance.form.id = queueitem.formId;
	claraInstance.type = Clara.Queues.SelectedQueue.objectType.toLowerCase();
});



Clara.Queues.SelectedQueue = {};

Clara.Queues.AssignedToMeFilter=false;
Clara.Queues.SelectedFormFilter="";
Clara.Queues.SelectedRoleFilter="";
Clara.Queues.SelectedStatusFilter="";
Clara.Queues.FilterFunction = function(rec,id){
	//clog("filtering",rec,"Clara.Queues.AssignedToMeFilter",Clara.Queues.AssignedToMeFilter,"Clara.Queues.SelectedFormFilter",Clara.Queues.SelectedFormFilter,"Clara.Queues.SelectedRoleFilter",Clara.Queues.SelectedRoleFilter);
	
	var roleFilter = true;
	
	if (Clara.Queues.SelectedRoleFilter == "" || Clara.Queues.SelectedRoleFilter == "Show All") {
		roleFilter = true;
	}else{
		roleFilter = (rec.get("roleName") == Clara.Queues.SelectedRoleFilter);
	}
	
	var statusFilter = true;
	
	if (Clara.Queues.SelectedStatusFilter == "" || Clara.Queues.SelectedStatusFilter == "Show All") {
		statusFilter = true;
	}else{
		statusFilter = (rec.get("formCommitteeStatus") == Clara.Queues.SelectedStatusFilter);
	}
	
	var formFilter = true;
	
	if (Clara.Queues.SelectedFormFilter == "" || Clara.Queues.SelectedFormFilter == "Show All"){
		formFilter = true;
	}else{
		formFilter = (rec.get("formType") == Clara.Queues.SelectedFormFilter);
	}
	
	var isMineFilter = true;
	
	if (!Clara.Queues.AssignedToMeFilter) {
		isMineFilter = true;
	}else{
		isMineFilter = rec.get("isMine") == "true";
	}
	
	var staticFilterVal = roleFilter && formFilter && isMineFilter && statusFilter;	

	
	return staticFilterVal;
};


Clara.Queues.IsReviewable = function(queueitem) {
	for ( var i = 0; i < queueitem.actions.length; i++) {
		if (queueitem.actions[i].data.name == "REVIEW")
			return true;
	}
	return false;
};

Clara.Queues.IsAssignable = function(queueitem) {
	for ( var i = 0; i < queueitem.actions.length; i++) {
		if (queueitem.actions[i].data.name == "ASSIGN_AGNEDA")
			return true;
	}
	return false;
};

Clara.Queues.FormHasAction = function(action, queueitem) {
	for ( var i = 0; i < queueitem.actions.length; i++) {
		if (queueitem.actions[i].data.name == action)
			return true;
	}
	return false;
};

Clara.Queues.CurrentQueueFormTypeStore = new Ext.data.ArrayStore({
	autoDestroy : false,
	storeId : 'current-queue-formtype-store',
	idIndex : 0,
	fields : [ 'formtype' ]
});

Clara.Queues.CurrentQueueFormStatusStore = new Ext.data.ArrayStore({
	autoDestroy : false,
	storeId : 'current-queue-formstatus-store',
	idIndex : 0,
	fields : [ 'formCommitteeStatus' ]
});

Clara.Queues.CurrentQueueUserRoleStore = new Ext.data.ArrayStore({
	autoDestroy : false,
	storeId : 'current-queue-userrole-store',
	idIndex : 0,
	fields : [ 'roleName' ]
});

Clara.Queues.StatusFilterPanel = Ext.extend(Ext.list.ListView,{

	id : 'clara-queues-statusfilterpanel',
	border : false,
	hideHeaders:true,
	trackMouseOver : false,
	userId : 0,
	singleSelect:true,
	selectedFilterRecord : {},
	constructor : function(config) {
		Clara.Queues.StatusFilterPanel.superclass.constructor.call(this, config);
	},
	queueUpdated : function(queue) {
		// clear any selections?
		clog(queue);
		var hide = (queue.identifier === "QUEUE_IRB_REVIEWER");
		Ext.getCmp("cbCompletedItems").setDisabled(false);
		Ext.getCmp("cbShowOnlyMyQueue").setVisible(!hide);
	},
	initComponent : function() {
		
		Clara.Queues.MessageBus.addListener('queueselected',
				this.queueUpdated);
		Clara.Queues.MessageBus.addListener(
				'queueitemupdated', this.queueUpdated);
		
		var config = {
			tpl: new Ext.XTemplate(
		                '<h2 class="sidebar-list-header">Review Status</h2><ul class="sidebar-list queue-user-list"><tpl for="rows">',
		                    '<li class="queue-user-list-item">',
		                        '<tpl for="parent.columns">',
		                        '<span class="queue-user-list-item-value">',
		                            '{[values.tpl.apply(parent)]}',
		                        '</span>',
		                        '</tpl>',
		                    '</li>',
		                '</tpl></ul>'
		            ),
			store : Clara.Queues.CurrentQueueFormStatusStore,
			itemSelector: 'li',
			viewConfig : {
				forceFit : true
			},
				listeners : {
					click : function(list, rowIndex) {
						// Get the list of protocols in the queue for this user
						var record = list.getStore().getAt(rowIndex);
						list.selectedFilterRecord = record;
						// filter!
						var value = record.id;
						Clara.Queues.SelectedStatusFilter = value;
						Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
					}
				},
			
			loadMask : new Ext.LoadMask(Ext.getBody(), {
				msg : "Loading..."
			}),
			columns : [ {
				dataIndex : 'formCommitteeStatus',
				sortable : true,
				renderer : function(v,p,r) {
					clog(r);
					return "<div class='queue-list-row'>" + v + "</div>";
				}
			} ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.StatusFilterPanel.superclass.initComponent.apply(this,
				arguments);
	}

});
Ext.reg('claraqueuestatusfilterpanel', Clara.Queues.StatusFilterPanel);

Clara.Queues.RoleFilterPanel = Ext.extend(Ext.list.ListView,{

	id : 'clara-queues-rolefilterpanel',
	border : false,
	hideHeaders:true,
	trackMouseOver : false,
	userId : 0,
	singleSelect:true,
	selectedFilterRecord : {},
	constructor : function(config) {
		Clara.Queues.RoleFilterPanel.superclass.constructor.call(this, config);
	},
	queueUpdated : function(queue) {
		// clear any selections?
	},
	initComponent : function() {
		
		Clara.Queues.MessageBus.addListener('queueselected',
				this.queueUpdated);
		Clara.Queues.MessageBus.addListener(
				'queueitemupdated', this.queueUpdated);
		
		var config = {
			tpl: new Ext.XTemplate(
		                '<h2 class="sidebar-list-header">Reviewer Role</h2><ul class="sidebar-list queue-user-list"><tpl for="rows">',
		                    '<li class="queue-user-list-item">',
		                        '<tpl for="parent.columns">',
		                        '<span class="queue-user-list-item-value">',
		                            '{[values.tpl.apply(parent)]}',
		                        '</span>',
		                        '</tpl>',
		                    '</li>',
		                '</tpl></ul>'
		            ),
			store : Clara.Queues.CurrentQueueUserRoleStore,
			itemSelector: 'li',
			viewConfig : {
				forceFit : true
			},
				listeners : {
					click : function(list, rowIndex) {
						// Get the list of protocols in the queue for this user
						var record = list.getStore().getAt(rowIndex);
						list.selectedFilterRecord = record;
						// filter!
						var value = record.id;
						Clara.Queues.SelectedRoleFilter = value;
						//clog(value);
						//if (value == 'Show All')
						//	Ext.getCmp("clara-queues-formlistpanel")
						//			.getStore().clearFilter();
						//else
							Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
					}
				},
			
			loadMask : new Ext.LoadMask(Ext.getBody(), {
				msg : "Loading..."
			}),
			columns : [ {
				dataIndex : 'roleName',
				sortable : true,
				renderer : function(v) {
					return "<div class='queue-list-row'>" + v + "</div>";
				}
			} ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.RoleFilterPanel.superclass.initComponent.apply(this,
				arguments);
	}

});
Ext.reg('claraqueuerolefilterpanel', Clara.Queues.RoleFilterPanel);

Clara.Queues.FormTypeFilterPanel = Ext.extend(Ext.list.ListView,{

	id : 'clara-queues-formtypefilterpanel',
	border : false,
	hideHeaders:true,
	trackMouseOver : false,
	userId : 0,
	singleSelect:true,
	selectedFilterRecord : {},
	constructor : function(config) {
		Clara.Queues.FormTypeFilterPanel.superclass.constructor.call(this, config);
	},
	queueUpdated : function(queue) {
		// clear any selections?
	},
	initComponent : function() {
		
		Clara.Queues.MessageBus.addListener('queueselected',
				this.queueUpdated);
		Clara.Queues.MessageBus.addListener(
				'queueitemassigned', this.queueUpdated);
		
		var config = {
			tpl: new Ext.XTemplate(
		                '<h2 class="sidebar-list-header">Form Type</h2><ul class="sidebar-list queue-user-list"><tpl for="rows">',
		                    '<li class="queue-user-list-item">',
		                        '<tpl for="parent.columns">',
		                        '<span class="queue-user-list-item-value">',
		                            '{[values.tpl.apply(parent)]}',
		                        '</span>',
		                        '</tpl>',
		                    '</li>',
		                '</tpl></ul><div class="sidebar-separator"></div>'
		            ),
			store : Clara.Queues.CurrentQueueFormTypeStore,
			itemSelector: 'li',
			viewConfig : {
				forceFit : true
			},
				listeners : {
					click : function(list, rowIndex) {
						// Get the list of protocols in the queue for this user
						var record = list.getStore().getAt(rowIndex);
						list.selectedFilterRecord = record;
						// filter!
						var value = record.id;
						Clara.Queues.SelectedFormFilter = value;
						Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
					}
				},
			
			loadMask : new Ext.LoadMask(Ext.getBody(), {
				msg : "Loading..."
			}),
			columns : [ {
				dataIndex : 'formtype',
				sortable : true,
				renderer : function(v) {
					return "<div class='queue-list-row'>" + v + "</div>";
				}
			} ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.FormTypeFilterPanel.superclass.initComponent.apply(this,
				arguments);
	}

});
Ext.reg('claraqueueformtypefilterpanel', Clara.Queues.FormTypeFilterPanel);

Clara.Queues.QueueListPanel = Ext.extend(Ext.list.ListView, {
	id : 'clara-queues-queuelistpanel',
	border : false,
	hideHeaders:true,
	trackMouseOver : false,
	userId : 0,
	singleSelect:true,
	selectedQueue : {
		identifier : '',
		objectType : ''
	},
	constructor : function(config) {
		Clara.Queues.MessageBus.addListener('queueselected',
				this.queueUpdated);
		Clara.Queues.MessageBus.addListener(
				'queueitemassigned', this.queueUpdated);
		Clara.Queues.QueueListPanel.superclass.constructor.call(this, config);
	},
	queueUpdated : function(queue) {
		// Clara.Queues.SelectedQueue = queue;
		var t = Ext.getCmp("clara-queues-formlistpanel");
		clog("QueueListPanel: queueUpdated called.",queue,t.objectType,(t.objectType != queue.objectType));
		var t = Ext.getCmp("clara-queues-formlistpanel");
		
		if (t.objectType != queue.objectType){ //still the same objectType, then I don't have to reload the column models
			clog("queueUpdated: objectType",t.objectType);
			t.objectType = queue.objectType;
			Ext.getCmp("btnOpenQueueItem").setText("View "+t.objectType);
			var colmodel = (t.objectType == "Protocol")?protocolColumnModel:contractColumnModel;
			Ext.getCmp("btnShowQueueItemNotes").setVisible(t.objectType == "Contract");
			t.reconfigure(t.getStore(),colmodel);
		}
		
		var st = t.getStore();
		
		st.removeAll();
		st.reload({
			params : {
				queue : queue.identifier,
				objectType : queue.objectType,
				userId : claraInstance.user.id,
				showHistory: Clara.Queues.ShowHistory
			}, callback: function() {
				Clara.Queues.MessageBus.fireEvent("queueloaded",queue);
			}
		});
	
		
		
	},
	initComponent : function() {
		var t =this;
		var config = {
			tpl: new Ext.XTemplate(
		                '<h2 class="sidebar-list-header">Available Queues</h2><ul class="sidebar-list queue-user-list"><tpl for="rows">',
		                    '<li class="queue-user-list-item">',
		                        '<tpl for="parent.columns">',
		                        '<span class="queue-user-list-item-value">',
		                            '{[values.tpl.apply(parent)]}',
		                        '</span>',
		                        '</tpl>',
		                    '</li>',
		                '</tpl></ul><div class="sidebar-separator"></div>'
		            ),
			store : new Ext.data.XmlStore({
				proxy : new Ext.data.HttpProxy({
					url : appContext + "/ajax/queues/list-user-queues.xml",
					method : "GET",
					data : {
						userId : claraInstance.user.id
					},
					headers : {
						'Accept' : 'application/xml;charset="utf-8"'
					}
				}),
				autoLoad : false,
				record : 'queue',
				root : 'list',
				sortInfo:{
					field:'name',direction:'ASC'
				},
				fields : [ {
					name : 'identifier',
					mapping : '@identifier'
				}, {
					name : 'name',
					mapping : '@name'
				}, {
					name : 'objectType',
					mapping : '@object-type'
				} ]
			}),
			itemSelector: 'li',
			viewConfig : {
				forceFit : true
			},
				listeners : {

					selectionChange : function(list, rowIndex) {
						// Get the list of protocols in the queue for this user
						var record = list.getStore().getAt(list.getSelectedIndexes()[0]);
						list.selectedQueue = {
							identifier : record.data.identifier,
							objectType : record.data.objectType
						};
						Clara.Queues.MessageBus.fireEvent("queueselected",
								list.selectedQueue);
					}
				},
			
			loadMask : new Ext.LoadMask(Ext.getBody(), {
				msg : "Loading committees & departments..."
			}),
			columns : [ {
				header : 'Committee / Department',
				dataIndex : 'name',
				sortable : true,
				renderer : function(v) {
					return "<div class='queue-list-row'>" + v + "</div>";
				}
			} ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.QueueListPanel.superclass.initComponent.apply(this,
				arguments);
		this.store.removeAll();
		clog("Loading queues for user " + claraInstance.user.id);
		this.store.setBaseParam('userId', claraInstance.user.id);
		this.store.load({callback:function(recs){
			// select a queue (after a review)
			if (typeof fromQueue != 'undefined' && fromQueue != ''){
				
				for (var i=0;i<recs.length;i++){
					clog("CHECKING",recs[i].get("identifier"));
					if (recs[i].get("identifier") == fromQueue){
						Ext.getCmp("clara-queues-queuelistpanel").select(recs[i]);
					}
				}

			} else {
				clog("No fromQueue string",fromQueue);
			}
		}});

	}
});
Ext.reg('claraqueuelistpanel', Clara.Queues.QueueListPanel);



var protocolRowRenderer = Clara.Protocols.QueueRowRenderer;


var protocolColumnModel = new Ext.grid.ColumnModel({
	columns : [
			{
				resizable:false,
				header:'Study',
				id : 'col-queue-row-title',
				dataIndex: 'title',
				renderer: protocolRowRenderer // Clara.Protocols.RowRenderer
			},{
				header : 'Added to queue',
				dataIndex : 'formCommitteeStatusModified',
				sortable : true,
				width : 130,
				renderer : function(v) {
					return "<div class='form-list-row form-date-added'>"
							+ Ext.util.Format.date(v, 'm/d/Y h:ia') + "</div>";
				}
			},
			{
				header : 'Assigned To',
				dataIndex : 'assignedReviewers',
				sortable : true,
				width : 230,
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
		]
});

var contractColumnModel = new Ext.grid.ColumnModel({
	columns : [
			 {
						resizable:true,
						width:350,
						header:'Contract',
						sortable:true,
						dataIndex: 'identifier',
						renderer:function(v,p,r){
							var cid = r.get("identifier");
			
							var hasLogsClass = (r.get("logs").length > 0)?" hasLogs":"";
							var logs = "";
							
								var s = "<span style='font-weight:800;'>"+cid+"</span>";
								if (r.get("formType") == "Amendment") s += " <span style='background-color:yellow;'>(Amendment)</span>";
								if (r.get("metaType") != "") s+= ": "+Clara.HumanReadableType(r.get("metaType"),"-");
								if (r.get("contractEntitySubtype") != "") s+= " ("+Clara.HumanReadableType(r.get("contractEntitySubtype"),"-")+")";
								
								
								if (r.get("logs").length > 0){
									logs = "<div class='wrap queueitem-logs'><h1>"+r.get("title")+"</h1><ol>";
									for (var i=0,l=r.get("logs").length;i<l;i++){
										logs += "<li><strong>"+r.get("logs")[i].get("time")+"</strong> - "+r.get("logs")[i].get("log")+"</li>";
									}
									logs += "</ol></div>";
								}
								
								return "<div class='wrap"+hasLogsClass+"'>"+s+"</div>"+logs;
						}
				},
			{
				header : 'Form Type',
				dataIndex : 'formType',
				hidden : true
			},
			{
				header:'Type',
				hidden:true,
				id:'col-queue-row-title',
				dataIndex: 'metaType'
			},{
				resizable:true,
				header:'IRB #',
				sortable:true,
				dataIndex: 'studyIdentifier'
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
						clog("looking at staff",r.get("staffs")[i]);
						if (r.get("staffs")[i].get("isPI") ) pi += "<li>"+r.get("staffs")[i].get("firstname")+" "+r.get("staffs")[i].get("lastname");
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
					var pi = "<ul>";
					for (var i=0;i<r.get("sponsors").length;i++){
						pi += "<li>"+r.get("sponsors")[i].get("company");
						if (r.get("sponsors")[i].get("name") != "") pi += ": "+r.get("sponsors")[i].get("name");
					}
					return pi+"</ul>";
				}
			},
			{
				header : 'Form Status',
				dataIndex : 'formStatus',
				width:225,
				sortable : true,
				renderer : function(v,p,r) {
					if (v && r.get("formCommitteeStatus") && (v != r.get("formCommitteeStatus"))) return "<div class='form-list-row form-status'>" +r.get("formCommitteeStatus")+"<br/>("+v+")</div>";
					return "<div class='form-list-row form-status'>" + v
							+ "</div>";
				}
			},{
				header : 'Added to queue',
				dataIndex : 'formCommitteeStatusModified',
				sortable : true,
				renderer : function(v) {
					return "<div class='form-list-row form-date-added'>"
							+ Ext.util.Format.date(v, 'm/d/Y h:ia') + "</div>";
				}
			},
			{
				header : 'Assigned To',
				dataIndex : 'assignedReviewers',
				sortable : true,
				width : 230,
				renderer : function(v) {
					var h = "<ul class='form-list-row form-assigned-reviewers'>";
						for(var i=0;i<v.length;i++) {
							clog(v[i]);
							h +="<li class='form-assigned-reviewer'>"+Clara.HumanReadableRoleName(v[i].data.reviewerRoleName)+": "+v[i].data.reviewerName + "</li>";
						}
						h += "</ul>";
							
					return h;
				}
			} ]
});

committeeReviewXmlReader = new Ext.data.XmlReader(
		{
			record : 'committee-review>committee',
			fields : [
					{
						name : 'committee',mapping:'@type'
					},
					{
						name : 'action'
					},
					{
						name : 'actor'
					}
					]
		});

actionXmlReader = new Ext.data.XmlReader(
		{
			record : 'actions>action',
			fields : [
					{
						name : 'name'
					},
					{
						name : 'url'
					},{
						name : 'assignToRole',
						mapping : "assign-to-role"
					}]
		});

committeeNoteXmlReader = new Ext.data.XmlReader(
		{
			record : 'committee-notes>committee-note',
			fields : [
					{
						name : 'note', mapping:''
					}]
		});

logXmlReader = new Ext.data.XmlReader(
		{
			record : 'latest-logs>log',
			fields : [
					{
						name : 'log', mapping:''
					},{
						name: 'time', mapping:'@date-time'
					},{
						name: 'actor', mapping:'@actor'
					}]
		});

assignedReviewerXmlReader = new Ext.data.XmlReader(
		{
			record : '/assigned-reviewers/assigned-reviewer',
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
		});
Clara.Queues.QueueFormListPanel = Ext
		.extend(
				Ext.grid.GridPanel,
				{
					id : 'clara-queues-formlistpanel',
					frame : false,
					border : false,
					colModel : protocolColumnModel,
					objectType: "Protocol",
					stripeRows:true,
					trackMouseOver : false,
					bodyStyle : 'border-left:1px solid #8DB2E3;',
					constructor : function(config) {
						Clara.Queues.QueueFormListPanel.superclass.constructor
								.call(this, config);
					},
					viewConfig: {
			    		getRowClass: function(record, index){
			    			var rID = record.get('formTypeId');
			    			if (rID == "emergency-use") { return 'protocol-row-error'; }
			    		}
			        },
					reconfigure: function(store, colModel) {
						var rendered = this.rendered;
				        if(rendered){
				            if(this.loadMask){
				                this.loadMask.destroy();
				                this.loadMask = new Ext.LoadMask(this.bwrap,
				                        Ext.apply({}, {store:store}, this.initialConfig.loadMask));
				            }
				        }
				        if(this.view){
				            this.view.initData(store, colModel);
				        }
				        this.store = store;
				        this.colModel = colModel;
				        
				        if(rendered){
				            this.view.refresh(true);
				        }
				        this.fireEvent('reconfigure', this, store, colModel);
					},	
					initComponent : function() {
						var t = this;
						
						var config = {
							border:false,
							autoExpandColumn : 'col-queue-row-title',
							store : new Ext.data.XmlStore(
									{
										proxy : new Ext.data.HttpProxy(
												{
													url : appContext
															+ "/ajax/queues/forms/list.xml", // changes
													// dynamically
													data : {
														userId : claraInstance.user.id
													},
													method : "GET",
													headers : {
														'Accept' : 'application/xml;charset="utf-8"'
													}
												}),
										record : 'form',
										autoLoad : false,
										root : 'list',
										hasMultiSort:true,
										multiSortInfo: {
												sorters:[{
													field: 'formType',
													direction:'ASC'
												},{
													field: 'formCommitteeStatus',
													direction:'ASC'
												},{
													field: 'formCommitteeStatusModified',
													direction:'DESC'
												}
											],
											direction:'ASC'
										},
										fields : [
												{
													name : 'formId',
													mapping : '@form-id'
												},
												{
													name : 'isMine',
													mapping : '@is-mine'
												},
												{
													name: 'irbSuggestedType',
													mapping:'meta>summary>irb-determination>suggested-type'
												},
												{name:'studyNature',mapping:'protocol-form>details>study-nature'},
												{name:'details',mapping:'protocol-form>details',convert:function(v,node){ return new Ext.data.XmlReader({record: 'value',fields: [{name:'detailName', mapping:'@name'},{name:'detailValue',mapping:''}]}).readRecords(node).records; }},

												{
													name : 'roleName',
													mapping : '@role-name'
												},
												{	name:  'metaType',
													mapping:'meta>type'
												},
												{name:'contractEntitySubtype',mapping:'type', convert:function(v,node){
													var stp = jQuery(node).find("sub-type:first").text();
													return stp;
												}},
												{
													name: 'roleId',
													mapping: '@role-id'
												},
												{
													name : 'committee',
													mapping : '@committee'
												},
												{
													name : 'committeeName',
													mapping : '@committee-name'
												},
												{
													name:'formCommitteeStatus',
													mapping:'form-committee-status>description'
												},
												{
													name : 'claraIdentifier',
													mapping : 'meta@id'
												},
												{
													name : 'identifier',
													mapping : 'meta@identifier'
												},
												{
													name : 'title',
													mapping : 'meta>title'
												},
												
												{
													name : 'formType',
													mapping : 'form-type',
													sortType: function(value){
														if (value == "Emergency Use"){
															return 1;
														} else {
															return 10;
														}
													}
												},
												{
													name : 'studyType',
													mapping : 'meta>study-type'
												},
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
													         {name:'isPI', convert:function(v,node){
													        	 var isPI = false;
																	clog("STAFF NODE",v,node);
																	jQuery(node).find("roles").find("role").each(function(){
																		isPI = isPI || (jQuery(this).text().toString() == "Principal Investigator");
																	});
																	return isPI;
													         }}
													]
												}).readRecords(node).records; }},
												{name:'PI',mapping:'@pi'},			// will probably be a converted list later
												{name:'studyIdentifier',mapping:'protocol'},
												{
													name : 'formTypeId',
													mapping : 'form-type@id'
												},
												{
													name : 'url',
													mapping : 'url'
												},
												{
													name : 'editurl',
													mapping : 'editurl'
												},
												{
													name : 'formStatus',
													mapping : 'form-status>description'
												},{
													name : 'priority',
													mapping : 'meta>status@priority'
												},
												{
													name : 'formStatusModified',
													mapping : 'form-status>modified-at',
													type : 'date',
													dateFormat : 'Y-m-d H:i:s.u'
												},
												{
													name : 'formCommitteeStatus',
													mapping : 'form-committee-status>description',
													sortType: function(value){
														if (value == "Potential Non-compliance In Review"){
															return 1;
														} else {
															return 10;
														}
													}
												},
												{
													name : 'formCommitteeStatusModified',
													mapping : 'form-committee-status>modified-at',
													type : 'date',
													dateFormat : 'Y-m-d H:i:s.u'
												},
												{
													name : 'actions',
													convert : function(v, node) {
														return actionXmlReader
																.readRecords(node).records;
													}
												},
												/*{
													name: 'committeeNotes',
													convert: function(v,node){
														return committeeNoteXmlReader.readRecords(node).records;
													}
												},*/
												{
													name: 'logs',
													convert: function(v,node){
														return logXmlReader.readRecords(node).records;
													}
												},
												{
													name: 'committeeReviews',
													convert:function(v,node){
														return committeeReviewXmlReader.readRecords(node).records;
													}
												},
												{
													name : 'assignedReviewers',
													mapping:'assigned-reviewers',
													convert : function(v, node) {
														var recs = assignedReviewerXmlReader.readRecords(node).records;
													    return recs;
													}
												}],
										listeners : {
											load : function(st) {
												// Update formTypes filter array..
												var uniqueformtypes = st
														.collect('formType',
																true, true);
												var formTypes = [ [ 'Show All' ] ];
												for ( var i = 0; i < uniqueformtypes.length; i++) {
													formTypes
															.push([ uniqueformtypes[i] ]);
												}
												//clog(formTypes);
												Clara.Queues.CurrentQueueFormTypeStore
														.removeAll();
												Clara.Queues.CurrentQueueFormTypeStore
														.loadData(formTypes);
												
												// Update statusTypes filter array..
												var uniquestatustypes = st
														.collect('formCommitteeStatus',
																true, true);
												var formStatusTypes = [ [ 'Show All' ] ];
												for ( var i = 0; i < uniquestatustypes.length; i++) {
													formStatusTypes
															.push([ uniquestatustypes[i] ]);
												}
												//clog(formTypes);
												Clara.Queues.CurrentQueueFormStatusStore
														.removeAll();
												Clara.Queues.CurrentQueueFormStatusStore
														.loadData(formStatusTypes);
											
												// Update role filter array..
												var unqiueRoleNames = st
														.collect('roleName',
																true, true);
												var roleNames = [ [ 'Show All' ] ];
												for ( var i = 0; i < unqiueRoleNames.length; i++) {
													roleNames
															.push([ unqiueRoleNames[i] ]);
												}
								
												Clara.Queues.CurrentQueueUserRoleStore
														.removeAll();
												Clara.Queues.CurrentQueueUserRoleStore
														.loadData(roleNames);
												
											}
										}
									}),
							loadMask : new Ext.LoadMask(Ext.getBody(), {
								msg : "Reading queue..."
							}),
							selModel : new Ext.grid.RowSelectionModel(
									{
										singleSelect : true,
										listeners : {
											rowselect : function(grid,
													rowIndex, record) {
												Clara.Queues.MessageBus
														.fireEvent(
																"queueitemselected",
																record.data);
											}
										}
									})
						};
						Ext.apply(this, Ext.apply(this.initialConfig, config));
						Clara.Queues.QueueFormListPanel.superclass.initComponent
								.apply(this, arguments);
						this.store.removeAll();
					}
				});
Ext.reg('claraqueueformlistpanel', Clara.Queues.QueueFormListPanel);

Clara.Queues.ToolBar = Ext.extend(Ext.Toolbar, {
	id : 'clara-queues-toolbar',
	border : false,
	bodyStyle : 'border-left:1px solid #8DB2E3;border-top:1px solid #8DB2E3;',
	constructor : function(config) {
		Clara.Queues.ToolBar.superclass.constructor.call(this, config);
	},
	initComponent : function() {

		var t = this;

		Clara.Queues.MessageBus.addListener('queueitemselected', function(
				queueitem) {
			Clara.Queues.SelectedQueueItem = queueitem;
			
			clog(queueitem);
			
			var btnReview = Ext.getCmp("btnReviewQueueItem");
			
			var btnOpen = Ext.getCmp("btnOpenQueueItem");
			var btnAssignToReviewer = Ext.getCmp("btnAssignItemToReviewer");
			var btnAssignToAgenda = Ext.getCmp("btnAssignItemToAgenda");

			btnReview.purgeListeners();
			btnOpen.purgeListeners();
			btnOpen.setDisabled(false);
			btnOpen.on('click', function() {	
				var url = appContext+"/"+Clara.Queues.SelectedQueue.objectType.toLowerCase()+"s/"+queueitem.claraIdentifier+"/dashboard?formId="+queueitem.formId
				window.open( url+"&fromQueue="+Clara.Queues.SelectedQueue.identifier);
			});
			
			var reviewAction = null;
			
			for ( var i = 0; i < queueitem.actions.length; i++) {
				if (queueitem.actions[i].data.name == "REVIEW")
					reviewAction = queueitem.actions[i].data;
			}
			
			if (reviewAction) {
				btnReview.on('click', function() {					
					location.href =  appContext + reviewAction.url+"&fromQueue="+Clara.Queues.SelectedQueue.identifier;
					
				});
				btnReview.setDisabled(false);
			} else {
				btnReview.setDisabled(true);
			}

			if (Clara.Queues.FormHasAction("ASSIGN_REVIEWER",queueitem)) {
				btnAssignToReviewer.setDisabled(false);
			} else {
				btnAssignToReviewer.setDisabled(true);
			}
			
			if (Clara.Queues.FormHasAction("ASSIGN_AGENDA",queueitem)) {
				btnAssignToAgenda.setDisabled(false);
			} else {
				btnAssignToAgenda.setDisabled(true);
			}
		});

		Clara.Queues.MessageBus.addListener('queueselected', function(q) {
			Clara.Queues.SelectedQueue = q;
			var btnReview = Ext.getCmp("btnReviewQueueItem");
			var btnOpen = Ext.getCmp("btnOpenQueueItem");
			var btnAssignToAgenda = Ext.getCmp("btnAssignItemToAgenda");
			var btnAssignToReviewer = Ext.getCmp("btnAssignItemToReviewer");
			
			if (q.identifier == 'QUEUE_IRB_OFFICE') {
				btnAssignToAgenda.setVisible(true);
				btnAssignToReviewer.setVisible(false);
			} else {
				btnAssignToAgenda.setVisible(false);
				btnAssignToReviewer.setVisible(true);
			}
			btnReview.setDisabled(true);
			btnOpen.setDisabled(true);
			btnAssignToAgenda.setDisabled(true);
			btnAssignToReviewer.setDisabled(true);
			
			Ext.getCmp("txtFilterBy").setVisible(true);
			Ext.getCmp("fldQueueTextFilterField").setVisible(true);
			Ext.getCmp("btnClearQueueTextFilter").setVisible(true);
	
			
		});

		var config = {
			items : [
					{
						xtype : 'panel',
						html : 'Queues',
						padding : 4,
						iconAlign:'top',
						unstyled : true,
						bodyStyle : 'font-size:24px;background:transparent;',
						border : false
					},
					'->', {
						xtype : 'button',
						text : 'View study',
						iconAlign:'top',
						id : 'btnOpenQueueItem',
						iconCls : 'icn-book',
						disabled : true
					},{
						xtype : 'button',
						text : '<span style="font-weight:800;">Review form</span>',
						iconAlign:'top',
						id : 'btnReviewQueueItem',
						iconCls : 'icn-clipboard-search-result',
						disabled : true
					}, {
						xtype : 'button',
						text : 'Assign',
						iconAlign:'top',
						id : 'btnAssignItemToReviewer',
						iconCls : 'icn-user--arrow',
						disabled : true,
						hidden : false,
						handler : function() {
							new Clara.Queues.ReviewAssignmentWindow({
								queueitem : Clara.Queues.SelectedQueueItem,
								queue:Clara.Queues.SelectedQueue
							}).show();
						}
					}, {
						xtype : 'button',
						text : 'Assign',
						iconAlign:'top',
						id : 'btnAssignItemToAgenda',
						iconCls : 'icn-calendar--arrow',
						disabled : true,
						hidden : true,
						handler : function() {
							new Clara.Queues.AgendaAssignmentWindow({
								queueitem : Clara.Queues.SelectedQueueItem
							}).show();
						}
					} ]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Queues.ToolBar.superclass.initComponent.apply(this, arguments);

	}
});
Ext.reg('claraqueuetoolbar', Clara.Queues.ToolBar);



function renderQueuePage() {
	new Ext.Viewport({
		layout : 'border',
		items : [ {
			region : 'north',
			contentEl : 'clara-header',
			bodyStyle : {
				backgroundColor : 'transparent'
			},
			height : 48,
			border : false
		}, {
			id: "clara-queues-panel",
			xtype : 'panel',
			region : 'center',
			border : true,
			layout : 'border',
			tbar : {
				xtype : 'claraqueuetoolbar'
			},
			items : [ {
				xtype:'panel',
				region:'west',
				border:false,
				autoScroll:true,
				unstyled:true,
				cls:'sidebar',
				width:200,
				layout:'auto',
				items:[{
					xtype : 'claraqueuelistpanel'
				},{
					xtype:'claraqueuerolefilterpanel'
				},{
					xtype:'claraqueuestatusfilterpanel'
				},{xtype:'claraqueueformtypefilterpanel'},
				{xtype:'container',html:'<h2 class="sidebar-list-header">Show Only</h2>'},
				{
					xtype : 'checkbox',
					id:'cbShowOnlyMyQueue',
					boxLabel : '<span id="btnTogglePersonalQueueFilter" class="sidebar-item-label">Assigned directly to me</span>',
					cls: '',
					listeners:{
						check:function(t,v){
							Clara.Queues.AssignedToMeFilter = v;
							Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
						}
					}
				},{
					xtype : 'checkbox',
					id:'cbCompletedItems',
					disabled:true,
					boxLabel : '<span id="btnTogglePersonalQueueFilter" class="sidebar-item-label">Completed items (last 30 days)</span>',
					listeners:{
						check:function(t,v){
							Clara.Queues.ShowHistory = v;
							Clara.Queues.MessageBus.fireEvent('queueselected',Clara.Queues.SelectedQueue);	// to force reload.
						}
					}
				}]
			}
			          , {				
				xtype : 'claraqueueformlistpanel',
				border : false,
				region : 'center',
				tbar:[{
					iconCls:'icn-sticky-note',
					id:'btnShowQueueItemNotes',
					text: 'Show Log',
					pressed:false,
					hidden:true,
					enableToggle:true,
					toggleHandler: function(btn,st){
						if (st) {
							jQuery(".queueitem-logs").show();
							btn.setText("Hide Log");
						} else {
							jQuery(".queueitem-logs").hide();
							btn.setText("Show Log");
						}
						
					}
				},'->',{
					xtype:'tbtext',
					text:'Filter by: ',
					id:'txtFilterBy',
					hidden:true
				},{
					xtype:'textfield',
					id:'fldQueueTextFilterField',
					enableKeyEvents:true,
					listeners:{
						keyup:function(f){
						    var v = f.getValue();
							// Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
							if (jQuery.trim(v) == "") {
								clog("nothing to search by");
								jQuery("#clara-queues-formlistpanel .x-grid3-row").show();
							}
						
							else {
								clog("filtering by "+v);
								jQuery("#clara-queues-formlistpanel .x-grid3-row").each(function(){
									var row = this;
									if (jQuery(row).text().toLowerCase().indexOf(jQuery.trim(v).toLowerCase()) == -1) jQuery(row).hide();
									else jQuery(row).show();
								});
							}
						}
					},
					hidden:true
				},{
					xtype:'button',
					id:'btnClearQueueTextFilter',
					iconCls:'icn-cross',
					enableKeyEvents:true,
					handler:function(){
						Ext.getCmp("fldQueueTextFilterField").setValue("");
						Ext.getCmp("clara-queues-formlistpanel").getStore().filterBy(Clara.Queues.FilterFunction);
					},
					hidden:true
				},'-',{
		    		xtype:'button',
		    		tooltip:'Print list (opens new window)',
		    		tooltipType:'title',
		    		iconCls:'icn-printer',

					handler: function(){
						var gp = Ext.getCmp("clara-queues-formlistpanel");
						Ext.ux.Printer.print(gp,{ title:'Queue', keepWindowOpen:true });
					}
		    	}]
			}

			]
		}

		]
	});
}
