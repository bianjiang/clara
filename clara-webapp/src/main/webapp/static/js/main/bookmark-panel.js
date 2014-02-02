Ext.ns('Clara', 'Clara.Dashboard');

if (!Clara.Dashboard.MessageBus) Clara.Dashboard.MessageBus = new Ext.util.Observable();
Clara.Dashboard.MessageBus.addEvents('bookmarksupdated','bookmarkselected');

Clara.Dashboard.SelectedBookmarkRecord = null;

var criteriaStore = new Ext.data.ArrayStore({
    autoLoad: false,
    storeId: 'criteriaStore',
    idIndex: 0,  
    fields: [
       'searchFieldValue',
       'searchFieldDescription',
       'searchOperatorValue',
       'searchOperatorDescription',
       'searchKeyword'
    ]
});

Clara.ContractTypeStore = new Ext.data.SimpleStore({ 
	id:'Clara.ContractTypeStore',
	fields: ['value', 'name'],
    data:[['confidentiality-disclosure-agreement','Confidentiality Disclosure Agreement (CDA)'],
          ['clinical-trial-agreement','Clinical Trial Agreement (CTA)'],
          ['material-transfer-agreement','Material Transfer Agreement (MTA)'],
          ['research-agreement','Research Agreement'],
          ['subcontracts','Subcontracts'],
          ['hipaa-data-use-agreement','HIPAA Data Use Agreement (Limited Data Set)'],
          ['license','License'],
          ['other','Other']
          ] 
});


Clara.ContractStatusStore = new Ext.data.SimpleStore({
	id:'Clara.ContractStatusStore',
	fields:['value','name'],
	data:[['DRAFT','Draft'],
		['ARCHIVED','Archived'],
		['PENDING_PI_ENDORSEMENT','Pending PI Endorsement'],
		['REVISION_REQUESTED','Revision Requested'],
		['UNDER_COMMITTEE_REVIEW','Under Committee Review'],
		['PENDING_CONTRACT','Pending Contract'], 
		['PENDING_IND_IDE','Pending IND/IDE'], 
		['OPEN_FOR_ENROLLMENT','Open For Enrollment'], 
		['ON_CLINICAL_HOLD','On Clinical Hold'], 
		['CLOSE','Closed'],
		['CANCELLED','Contract Cancelled'], 
		['UNDER_BUDGET_REVIEW','Under Budget Review'],
		['UNDER_COVERAGE_REVIEW','Under Coverage Review'],
		['UNDER_REVISION','Under Revision'],
		['UNDER_REVIEW','Under Review'],
		['REVISION_PENDING_PI_ENDORSEMENT','Revision Pending PI Endorsement'],
		['PENDING_LEGAL_REVIEW','Pending Legal Review'],
		['PENDING_SPONSOR_RESPONSE','Pending Sponsor Response'],
		['UNDER_CONTRACT_REVIEW','Under Contract Review'],
		['UNDER_LEGAL_REVIEW','Under Legal Review'],
		['NOT_APPLICABLE','Not Applicable'],
		['FINAL_LEGAL_APPROVAL','Final Legal Approval'],
		['UNDER_CONTRACT_MANAGER_REVIEW','Under Contract Manager Review'],
		['CONTRACT_EXECUTED','Contract Executed'],
		['PENDING_REVIEWER_ASSIGNMENT','Pending Reviewer Assignment'],
		['PENDING_PI','Pending PI'],
		['PENDING_BUDGET','Pending Budget'],
		['PENDING_COVERAGE','Pending Coverage'],
		['PENDING_IRB','Pending IRB'],
		['OUTSIDE_IRB','Outside IRB'],
		['PENDING_SIGNATURE','Pending Signature'],
		['CONTRACT_EXECUTED_PENDING_DOCUMENTS','Contract Excuted Pending Documents']]
});

Clara.ProtocolStatusStore = new Ext.data.SimpleStore({ 
	id:'Clara.ProtocolStatusStore',
	fields: ['value', 'name'],
    data:[['ARCHIVED','Archived from ARIA/Crimson'],
          ['ARCHIVED_OPEN','Archived from ARIA/Crimson - Open'],
          ['DRAFT','Draft'],
          ['UNDER_IRB_OFFICE_REVIEW','Under IRB Office Review'],
          ['UNDER_PREREVIEW','Under Pre-review'],
          ['REVISION_REQUESTED','Revision Requested'],
          ['PENDING_IRB_REVIEW_ASSIGNMENT','Pending IRB Review Assignment'],
          ['IRB_AGENDA_ASSIGNED','Assigned to an IRB Agenda'],
          ['PENDING_PI_ENDORSEMENT','Pending PI Endorsement'],
          ['PENDING_PI_SIGN_OFF','Pending PI Sign Off'],
          ['PENDING_REVIEWER_ASSIGNMENT','Pending Reviewer Assignment'],
          ['PENDING_CONTRACT','Pending Contract'],
          ['PENDING_IND_IDE','Pending IND/IDE'],
          ['OPEN_FOR_ENROLLMENT','Open For Enrollment'],
          ['CLOSED','Closed'],
          ['UNDER_PHARMACY_REVIEW','Under Pharmacy Review'],
          ['UNDER_BUDGET_REVIEW','Under Budget Review'],
          ['UNDER_BUDGET_DEVELOP','Under Budget Development'],
          ['IRB_APPROVED','IRB Approved'],
          ['IRB_DECLINED','IRB Declined'],
          ['IRB_DEFERRED_WITH_MINOR_CONTINGENCIES','IRB Deferred with Major Contingencies'],
          ['IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES','IRB Deferred with Minor Contingencies'],
          ['IRB_TABLED','IRB Tabled'],
          ['IRB_ACKNOWLEDGED','IRB Acknowledged'],
          ['IRB_SUSPENDED','IRB Suspended'],
          ['IRB_TERMINATED','IRB Terminated'],
          ['IRB_CLOSED','IRB Closed'],
          ['EXPEDITED_APPROVED','Expedited Approved'],
          ['EXPEDITED_DECLINED','Expedited Declined'],
          ['EXEMPT_APPROVED','Exempt Approved'],
          ['EXEMPT_DECLINED','Exempt Declined'],
          ['EXPIRED','Expired'],
          ['DETERMINED_HUMAN_SUBJECT_RESEARCH','Determined Human Subject Research'],
          ['DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH','Determined Not Human Subject Research'],
          ['OPEN','Open']
          ] 
});

Clara.StudyTypeStore = new Ext.data.SimpleStore({ 
	id:'Clara.StudyTypeStore',
	fields: ['value', 'name'],
    data:[['investigator-initiated','Investigator Initiated'],
          ['cooperative-group','Cooperative Group'],
          ['industry-sponsored','Industry']
          ] 
});

Clara.FormTypeStore = new Ext.data.SimpleStore({ 
	id:'Clara.FormTypeStore',
	fields: ['value', 'name'],
    data:[['NEW_SUBMISSION','New Submission'],
          ['HUMAN_SUBJECT_RESEARCH_DETERMINATION','Human Subject Research Determination'],
          ['MODIFICATION','Modification'],
          ['CONTINUING_REVIEW','Continuing Review'],
          ['STUDY_CLOSURE','Study Closure'],
          ['ARCHIVE','Archive (from ARIA/CRIMSON)'],
          ['REPORTABLE_NEW_INFORMATION','Reportable New Information'],
          ['EMERGENCY_USE','Emergency Use'],
          ['HUMANITARIAN_USE_DEVICE_RENEWAL','Humanitarian Use Device Renewal Application"'],
          ['AUDIT','Audit']
          ] 
});

Clara.CollegeStore = new Ext.data.Store({
	id : 'Clara.CollegeStore',
	header : {
		'Accept' : 'application/json'
	},
	proxy : new Ext.data.HttpProxy({
		url : appContext + '/ajax/colleges/list',
		method : 'GET'
	}),
	autoLoad : false,
	reader : new Ext.data.JsonReader({
		idProperty : 'id'
	}, [ {
		name : 'value',
		mapping : 'id'
	}, {
		name : 'sapCode'
	}, {
		name : 'name'
	} ])
});

Clara.UserStore = new Ext.data.Store({
	id : 'Clara.UserStore',
    header :{
        'Accept': 'application/json'
},
proxy: new Ext.data.HttpProxy({
        url: appContext + '/ajax/users/persons/search',
        method:'GET'
}),
reader: new Ext.data.JsonReader({
        root: 'persons',
        idProperty: 'username'
}, [
    {name:'id'},
    {name:'userid',mapping:'userId'},
    {name:'sap'},
    {name:'username'},
    {name:'annualSalary'},
    {name:'firstname'},
    {name:'lastname'},
    {name:'email'},
    {name:'workphone'}
    ])
})


Clara.Dashboard.ContractSearchFieldStore = new Ext.data.SimpleStore({ 
	fields: ['value', 'name', 'type', 'optionStore'],
    data:[['TITLE','Title','string',null],['STAFF_USERID','Staff Name','user','Clara.UserStore'], ['PI_USERID','PI Name','user','Clara.UserStore'], 
          ['ENTITY_NAME','Entity Name','string',null], ['CONTRACT_TYPE','Contract Type','option','Clara.ContractTypeStore'],['CONTRACT_STATUS','Status','option','Clara.ContractStatusStore'],
          ['ASSIGNED_REVIEWER_USERID', 'Assigned Reviewer','user','Clara.UserStore']] //
});

Clara.Dashboard.ProtocolSearchFieldStore = new Ext.data.SimpleStore({ 
	fields: ['value', 'name', 'type', 'optionStore'],
    data:[['TITLE','Title','string',null],['STAFF_USERID','Staff Name','user','Clara.UserStore'], ['PI_USERID','PI Name','user','Clara.UserStore'], ['PRIMARY_SITE','Primary Institution','string',null], ['LOCATION','Location','string',null], 
          ['DRUG_NAME','Drug Name','string',null], ['STUDY_TYPE','Study Type','option','Clara.StudyTypeStore'],['PROTOCOL_STATUS','Status','option','Clara.ProtocolStatusStore'],['COLLEGE', 'College','option-remote','Clara.CollegeStore'], 
          ['DEPARTMENT','Department','string',null], ['DIVISION', 'Division','string',null] , ['FORM_TYPE', 'Form Type','option','Clara.FormTypeStore'], ['ASSIGNED_REVIEWER_USERID', 'Assigned Reviewer','user','Clara.UserStore']] //
});


Clara.Dashboard.WindowAddSearchBookmark = Ext.extend(Ext.Window, {
    title: 'New Search Bookmark',
    id:'winAddBookmark',
    type:null,
    width: 667,
    height: 371,
    layout: 'absolute',
    removeCriteria: function(id){
    	var r = criteriaStore.getById(id);
    	if (r) criteriaStore.remove(r);
    },
    initComponent: function() {
    	var t = this;
		this.buttons = [{
			text:'Run without saving',
			disabled:false,
			handler:function(){

				var listStore = (claraInstance.type == "protocol")?Clara.Protocols.ProtocolListStore:Clara.Contracts.ContractListStore;
				
				if (criteriaStore.getCount() < 1) {
					alert("Enter at least one search criteria.");
				} else {
					var criteria = [];
					for (var i = 0; i < criteriaStore.getCount(); i++)
					{
						criteria.push({"searchField":criteriaStore.getAt(i).data.searchFieldValue, "searchOperator":criteriaStore.getAt(i).data.searchOperatorValue, "keyword":criteriaStore.getAt(i).data.searchKeyword });
					}
					clog(jQuery.toJSON(criteria));
					
					Clara.SearchKeyword = "";
					listStore.setBaseParam("searchCriterias", jQuery.toJSON(criteria) );
					listStore.load({
                 		params: {
                 			start: 0, limit: Clara.PageSize
                 		}
                 	});
  
					t.close();
				}
			
			
			}
		},
		    			{
		    				text:'Save Bookmark',
		    				disabled:false,
		    				handler: function(){
		    					var name = jQuery("#fldBookmarkName").val();
		    					if (name == ""){
		    						alert("Enter a bookmark name");
		    					} else {
		    						if (criteriaStore.getCount() < 1) {
		    							alert("Enter at least one search criteria.");
		    						} else {
		    							var criteria = [];
		    							for (var i = 0; i < criteriaStore.getCount(); i++)
		    							{
		    								criteria.push({"searchField":criteriaStore.getAt(i).data.searchFieldValue, "searchOperator":criteriaStore.getAt(i).data.searchOperatorValue, "keyword":criteriaStore.getAt(i).data.searchKeyword });
		    							}
		    							clog(jQuery.toJSON(criteria));
		    							
		    							var url = appContext+"/ajax/"+claraInstance.type+"s/search-bookmarks/save";
		    							var data = {userId: claraInstance.user.id || 0, name:name, searchCriterias:jQuery.toJSON(criteria)};
		    							
		    							jQuery.ajax({
		    								  type: 'POST',
		    								  async:false,
		    								  url: url,
		    								  data: data,
		    								  success: function(v){
		    									  Clara.Dashboard.MessageBus.fireEvent('bookmarksupdated', this);
		    									  if(v == "SUCCESS"){
		    										  criteriaStore.removeAll(true);
		    						   										 
		    									  }
		    								  },
		    								  error: function(){
		    									  
		    								  }
		    							});
		    							criteriaStore.removeAll();
		    							t.close();
		    						}
		    					}
		    				}
		    			}
		    		   ],
        this.items = [
            
            
            {
                xtype: 'grid',
                height: 190,
                x: 0,
                y: 70,
                store:criteriaStore,
                columns: [
                    {
                        xtype: 'gridcolumn',
                        dataIndex: 'searchFieldDescription',
                        header: 'Search Field',
                        sortable: true,
                        width: 200
                    },
                    {
                        xtype: 'gridcolumn',
                        dataIndex: 'searchOperatorDescription',
                        header: 'Operator',
                        sortable: true,
                        width: 120
                    },
                    {
                        xtype: 'gridcolumn',
                        dataIndex: 'searchKeywordDescription',
                        header: 'Keyword',
                        sortable: true,
                        width: 230
                    },{
                    	xtype: 'gridcolumn',
                        dataIndex: 'searchFieldDescription',
                        header: 'Action',
                        width:70,
                        renderer:function(v,p,r){
                        	return "<a href='javascript:;' onclick='Ext.getCmp(\"winAddBookmark\").removeCriteria(\""+r.id+"\");'>Remove</a>";
                        }
                    }
                ]
            },
            {
                xtype: 'button',
                text: 'Add',
                x: 590,
                y: 30,
                width: 50,
                height: 22,
                itemId: 'btdAddCriteria',
                id: 'btdAddCriteria',
                handler:function(){
            		var sf = Ext.getCmp("fldSearchField");
            		var so = Ext.getCmp("fldSearchOperator");
            		var sk = Ext.getCmp("fldSearchText");
	            	var newRow = {searchFieldValue: sf.getValue(), searchFieldDescription: sf.getRawValue(), searchOperatorValue: so.getValue(), searchOperatorDescription: so.getRawValue(), searchKeyword: sk.getValue(), searchKeywordDescription: sk.getRawValue()};
	            	var newRecord = new criteriaStore.recordType(newRow);
	            	criteriaStore.add(newRecord);
	            	
            	}
            },
            {
                xtype: 'combo',
                width: 190,
                x: 10,
                y: 30,
                typeAhead:false,
                forceSelection:true,
                itemId: 'fldSearchField',
                store: (claraInstance.type == 'protocol')?Clara.Dashboard.ProtocolSearchFieldStore:Clara.Dashboard.ContractSearchFieldStore,
                displayField:'name',
                valueField: 'value',
                editable:false,
	        	allowBlank:false,
                mode:'local', 
	        	triggerAction:'all',
                id: 'fldSearchField',
                listeners:{
                	select:function(cb,rec,idx){
                		if (Ext.getCmp("fldSearchText")){
                			Ext.getCmp("fldSearchText").destroy();
                		}
                		
                		var type = rec.get("type");
                		var searchField = null;
                		var searchFieldOptions = {
                				x: 360,
                                y: 30,
                                width: 220,
                                itemId: 'fldSearchText',
                                name: 'fldSearchText',
                                id: 'fldSearchText'
                			};
                		
                		if (type == "string"){
                			
                			searchField = new Ext.form.TextField(searchFieldOptions);
                			
                			Ext.getCmp("fldSearchOperator").setValue("CONTAINS");
                			Ext.getCmp("fldSearchOperator").setDisabled(false);
                		}
                		else if (type == "user"){
                			searchFieldOptions.displayField = "username";
                			searchFieldOptions.valueField = "userid";
                			searchFieldOptions.typeAhead = false;
                			searchFieldOptions.forceSelection = true;
                			searchFieldOptions.allowBlank = false;
                			searchFieldOptions.allowOnlyWhitespace = false;
                			searchFieldOptions.minChars = 3;
                			searchFieldOptions.emptyText = 'Search by PI name or email';
                			searchFieldOptions.tpl = '<tpl for="."><div class="x-combo-list-item"><h3>{firstname} {lastname}</h3>{email}</div></tpl>';
                			searchFieldOptions.queryParam = 'keyword';
                			searchFieldOptions.hideTrigger = true;
                			
                			searchFieldOptions.editable = true;
                			searchFieldOptions.triggerAction = 'all';
                			searchFieldOptions.store = Ext.StoreMgr.get(rec.get("optionStore"));
                			searchField = new Ext.form.ComboBox(searchFieldOptions);
                			
                			Ext.getCmp("fldSearchOperator").setValue("EQUALS");
                			Ext.getCmp("fldSearchOperator").setDisabled(true);
  
                		} else if (type == "option" || type == "option-remote"){
                			searchFieldOptions.displayField = "name";
                			searchFieldOptions.valueField = "value";
                			searchFieldOptions.typeAhead = false;
                			searchFieldOptions.forceSelection = true;
                			searchFieldOptions.allowBlank = false;
                			searchFieldOptions.editable = false;
                			searchFieldOptions.mode = (type == "option")?'local':'remote';
                			searchFieldOptions.triggerAction = 'all';
                			searchFieldOptions.store = Ext.StoreMgr.get(rec.get("optionStore"));
                			searchField = new Ext.form.ComboBox(searchFieldOptions);
                			
                			Ext.getCmp("fldSearchOperator").setValue("EQUALS");
                			Ext.getCmp("fldSearchOperator").setDisabled(true);
                		}

                		clog("Ext.getCmp(rec.get('optionStore'));",Ext.getCmp(rec.get("optionStore")),rec.get("optionStore"));
                		Ext.getCmp("winAddBookmark").add(searchField);
                		Ext.getCmp("winAddBookmark").doLayout();
                		
                	}
                }
            },
            {
                xtype: 'combo',
                width: 140,
                x: 210,
                y: 30,
                typeAhead:false,
                forceSelection:true,
                itemId: 'fldSearchOperator',
                store: new Ext.data.SimpleStore({ 
                	fields: ['value', 'name'],
                    data:[['EQUALS','Equals'],['CONTAINS','Contains'], ['DOES_NOT_CONTAIN','Does Not Contain']]
                }),
                displayField:'name',
                valueField: 'value',
                editable:false,
	        	allowBlank:false,
                mode:'local', 
	        	triggerAction:'all',
                id: 'fldSearchOperator'
            },
            {
                xtype: 'label',
                text: 'Match the following rule:',
                x: 10,
                y: 10,
                style: 'font-size:14px;'
            },
            {
                xtype: 'label',
                text: 'Name this bookmark',
                x: 290,
                y: 270,
                style: 'font-size:14px;'
            },
            {
                xtype: 'textfield',
                x: 430,
                y: 270,
                width: 210,
                itemId: 'fldBookmarkName',
                name: 'fldBookmarkName',
                id: 'fldBookmarkName'
            }
        ];
        Clara.Dashboard.WindowAddSearchBookmark.superclass.initComponent.call(this);
    }
});

Clara.Dashboard.BookmarkPanel = Ext.extend(Ext.list.ListView, {
	id : 'bookmark-panel',
	border : false,
	type:'protocols',
	hideHeaders:true,
	trackMouseOver : false,
	userId : 0,
	singleSelect:true,
	selectedQueue : {
		identifier : '',
		objectType : ''
	},
	constructor : function(config) {
		Clara.Dashboard.BookmarkPanel.superclass.constructor.call(this, config);
	},
	refreshBookmarkList:function(){
		var st = Ext.getCmp("bookmark-panel").getStore();
		st.removeAll();
		clog("Loading bookmarks for user " + claraInstance.user.id);
		st.setBaseParam('userId', claraInstance.user.id);
		st.load();
	},
	initComponent : function() {
		var t = this;
		Clara.Dashboard.MessageBus.addListener('bookmarksupdated', t.refreshBookmarkList);
		storeUrl = appContext+'/ajax/'+claraInstance.type+'s/search-bookmarks/list';
		var config = {
				
				unstyled:true,
				cls:'sidebar',
				style:'border-right:1px solid #96baea;',
				tpl: new Ext.XTemplate(
		                '<h2 class="sidebar-list-header">Bookmarks</h2><ul class="sidebar-list queue-user-list"><tpl for="rows">',
		                    '<li class="bookmark-user-list-item bookmark-user-id-{userId}" id="protocol-bookmark-{id}">',
		                        '<tpl for="parent.columns">',
		                        '<span class="bookmark-user-list-item-value">',
		                            '{[values.tpl.apply(parent)]}',
		                        '</span>',
		                        '</tpl>',
		                    '</li>',
		                '</tpl></ul><div class="sidebar-separator"></div>'
		            ),
		            store: new Ext.data.JsonStore({
						proxy: new Ext.data.HttpProxy({
							url: storeUrl,
							method:"GET",
							headers:{'Accept':'application/json;charset=UTF-8'}
						}),
						autoLoad:false,
						baseParams: {userId: claraInstance.user.id || 0},
						storeId:'bookmarkStore',
						root:'bookmarks',
						fields:['name','searchCriterias','id', 'userId']
					}),
					
			itemSelector: 'li',
			viewConfig : {
				forceFit : true
			},
				listeners : {
					click : function(list, rowIndex) {
						var listStore = (claraInstance.type == "protocol")?Clara.Protocols.ProtocolListStore:Clara.Contracts.ContractListStore;
						listStore.setBaseParam('keyword','');
						var record = list.getStore().getAt(rowIndex);
						clog(record);
						Clara.Dashboard.SelectedBookmarkRecord = record;
						if (Ext.getCmp("btnRemoveBookmark")){
							Ext.getCmp("btnRemoveBookmark").setDisabled(rowIndex <= 1);
						}
						
						Clara.SearchKeyword = "";
						listStore.setBaseParam("searchCriterias", record.get("searchCriterias") );
						listStore.load({
	                 		params: {
	                 			start: 0, limit: Clara.PageSize
	                 		}
	                 	});
                 		
					}
				},
			
			loadMask : new Ext.LoadMask(Ext.getBody(), {
				msg : "Loading bookmarks..."
			}),
			columns : [ {
				header : 'Bookmark',
				dataIndex : 'name',
				sortable : true
			}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Dashboard.BookmarkPanel.superclass.initComponent.apply(this,
				arguments);
		this.refreshBookmarkList();
	}
});
Ext.reg('clarabookmarkpanel', Clara.Dashboard.BookmarkPanel);