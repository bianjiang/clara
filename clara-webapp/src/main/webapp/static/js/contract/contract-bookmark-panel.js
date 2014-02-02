Ext.ns('Clara.AllContractDashboard');

Clara.AllContractDashboard.MessageBus = new Ext.util.Observable();

Clara.AllContractDashboard.MessageBus.addEvents('bookmarksupdated');

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

Clara.AllContractDashboard.WindowAddSearchBookmark = Ext.extend(Ext.Window, {
    title: 'New Search Bookmark',
    id:'winAddBookmark',
    width: 667,
    height: 371,
    layout: 'absolute',
    initComponent: function() {
		this.buttons = [
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
		    							
		    							var url = appContext+"/ajax/contracts/search-bookmarks/save";
		    							var data = {userId: claraInstance.user.id || 0, name:name, searchCriterias:jQuery.toJSON(criteria)};
		    							
		    							jQuery.ajax({
		    								  type: 'POST',
		    								  async:false,
		    								  url: url,
		    								  data: data,
		    								  success: function(v){
		    									  if(v == "SUCCESS"){
		    										  criteriaStore.removeAll(true);
		    										  if(Clara.AllContractDashboard.MessageBus){
		    											  Clara.AllContractDashboard.MessageBus.fireEvent('bookmarksupdated', this);
		    										}		    										 
		    									  }
		    								  },
		    								  error: function(){
		    									  
		    								  }
		    							});
		    							
		    							Ext.getCmp("winAddBookmark").close();
		    						}
		    					}
		    				}
		    			}
		    		   ],
        this.items = [
            {
                xtype: 'textfield',
                x: 360,
                y: 30,
                width: 220,
                itemId: 'fldSearchText',
                name: 'fldSearchText',
                id: 'fldSearchText'
            },
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
                        dataIndex: 'searchKeyword',
                        header: 'Keyword',
                        sortable: true,
                        width: 300
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
	            	var newRow = {searchFieldValue: sf.getValue(), searchFieldDescription: sf.getRawValue(), searchOperatorValue: so.getValue(), searchOperatorDescription: so.getRawValue(), searchKeyword: sk.getValue()};
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
                store: new Ext.data.SimpleStore({ 
                	fields: ['value', 'name'],
                    data:[['TITLE','Title'],['STAFF_NAME','Staff Name'], ['PI_NAME','PI Name'], ['STUDY_TYPE','Study Type'],['PROTOCOL_STATUS','Status'],['COLLEGE', 'College'], ['DEPARTMENT','Department'], ['DIVISION', 'Division']] //
                }),
                displayField:'name',
                valueField: 'value',
                editable:false,
	        	allowBlank:false,
                mode:'local', 
	        	triggerAction:'all',
                id: 'fldSearchField'
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
        Clara.AllContractDashboard.WindowAddSearchBookmark.superclass.initComponent.call(this);
    }
});

Clara.AllContractDashboard.BookmarkGridPanel = Ext.extend(Ext.grid.GridPanel, {
	id: 'contract-bookmark-panel',
	autoScroll: true,
    border: false,
    stripeRows: false,
    loadMask: true,
    onBookmarksUpdated: function(){
    	this.getStore().reload();
    },
	constructor:function(config){		
		Clara.AllContractDashboard.BookmarkGridPanel.superclass.constructor.call(this, config);
		if(Clara.AllContractDashboard.MessageBus){
			Clara.AllContractDashboard.MessageBus.on('bookmarksupdated', this.onBookmarksUpdated, this);
		}
	},
	initComponent: function() {
		
		var config = {
				view: new Ext.grid.GridView({
			        forceFit: true,
					headersDisabled: true,
					rowOverCls:''
			    }),
				columns: [
					{
						id: 'name',
						resizable:false,
						dataIndex: 'name',
						header:'Bookmarks',
						renderer:function(v){
							return "<div class='bookmark-row'>"+v+"</div>";
						}
					},
					{
						resizable:false,
						dataIndex: 'id',
						width: 30,
						renderer:function(v){
							if(v < 2) {
								return;
							}
							return "<div><img style='float:left;cursor:pointer;' src='"+appContext+"/static/images/icons/bin_closed.png' border='0'/></div>";
						}
					}					
				],
				listeners:{
					cellclick :function(grid, rowIndex, columnIndex, e){ 
                     	clog("item clicked!" + columnIndex);
                     	
                     	if(columnIndex == 1) { 
                     		var record = grid.getStore().getAt(rowIndex);
                     		var url = appContext+"/ajax/contracts/search-bookmarks/" + record.id + "/remove";
							var data = {userId: claraInstance.user.id || 0};							
							jQuery.ajax({
								  type: 'POST',
								  async:false,
								  url: url,
								  data: data,
								  success: function(v){
									  if(v == "SUCCESS"){
										  
										  if(Clara.AllContractDashboard.MessageBus){
											  Clara.AllContractDashboard.MessageBus.fireEvent('bookmarksupdated', this);
										}		    										 
									  }
								  },
								  error: function(){
									  
								  }
							});
                     	}
                     	
                     },
					celldblclick :function(grid, rowIndex, columnIndex, e){ 
                     	clog("item clicked!" + columnIndex);
                     	
                     	if(columnIndex == 0) { 
                     		var record = grid.getStore().getAt(rowIndex);
                     	
                     		Clara.Contracts.ContractListStore.load({
                     			params: {
                     				"searchCriterias": record.data.searchCriterias //'[{"searchField":"TITLE","searchOperator":"CONTAINS","keyword":"a title"}]'
                     			}
                     		});
                     	}
                     	
                     }
				},
				store: new Ext.data.JsonStore({
					proxy: new Ext.data.HttpProxy({
						url: appContext+'/ajax/contracts/search-bookmarks/list',//'/static/js/test/contract-bookmarks.json',
						method:"GET",
						headers:{'Accept':'application/json;charset=UTF-8'}
					}),
					autoLoad:true,
					baseParams: {userId: claraInstance.user.id || 0},
					storeId:'bookmarkStore',
					root:'bookmarks',
					fields:['name','searchCriterias','id']
				}),
				bbar: new Ext.Toolbar({
				    items:[{
					            xtype:'button',
					            text: 'New Bookmark...',
					            iconCls: 'icn-bookmark--plus',
					            handler: function(){
						        	var newBmWindow = new Clara.AllContractDashboard.WindowAddSearchBookmark();
						        	newBmWindow.show();
			        			}
			                },'-']
			    })
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.AllContractDashboard.BookmarkGridPanel.superclass.initComponent.apply(this, arguments);
	}
});
Ext.reg('claracontractbookmarkpanel', Clara.AllContractDashboard.BookmarkGridPanel);