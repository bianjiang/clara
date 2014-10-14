Ext.define('Clara.Dashboard.view.BookmarkWindow', {
    extend: 'Ext.window.Window',
    requires:[],
    alias: 'widget.bookmarkwindow',
    title: 'New Bookmark',
    width:800,
    height:450,
    bookmarkRecord:null,
    iconCls:'icn-gear',
    layout: {
        type: 'border'
    },

    initComponent: function() {
    	var me = this;
    	me.listeners={
        	show:function(w){
        		// populate if editing existing bookmark
        		if (w.bookmarkRecord){
        			var criteria = Ext.JSON.decode(w.bookmarkRecord.get("searchCriterias"));
        			
        			// For legacy bookmarks: add duplicate description values to array
        			for (i=0,l=criteria.length;i<l;i++){
        	
							criteria[i] = {
									"searchFieldValue": (!criteria[i].searchFieldValue || criteria[i].searchFieldValue == "")?criteria[i].searchField:criteria[i].searchFieldValue,
									"searchFieldDescription": (!criteria[i].searchFieldDescription || criteria[i].searchFieldDescription == "")?criteria[i].searchField:criteria[i].searchFieldDescription,
									"searchOperatorValue": (!criteria[i].searchOperatorValue || criteria[i].searchOperatorValue == "")?criteria[i].searchOperator:criteria[i].searchOperatorValue,
									"searchOperatorDescription": (!criteria[i].searchOperatorDescription || criteria[i].searchOperatorDescription == "")?criteria[i].searchOperator:criteria[i].searchOperatorDescription,
									"searchKeyword": (!criteria[i].searchKeyword || criteria[i].searchKeyword == "")?criteria[i].keyword:criteria[i].searchKeyword,
									"searchKeywordDescription": (!criteria[i].searchKeywordDescription || criteria[i].searchKeywordDescription == "")?criteria[i].keyword:criteria[i].searchKeywordDescription	
							};
						
        			}
        			
        			
        			var criteriaStore = w.down("grid").getStore();
        			clog("Editing bookmark record",w.bookmarkRecord,criteria);
        			criteriaStore.loadData(criteria);
        			Ext.getCmp("fldBookmarkName").setValue(w.bookmarkRecord.get("name"));
        			
        		}
        	}
        };
    	
    	me.items = [{
            xtype: 'grid',
            region:'center',
            border:0,
            store:new Ext.data.ArrayStore({
                fields: [
                   'searchFieldValue',
                   'searchFieldDescription',
                   'searchOperatorValue',
                   'searchOperatorDescription',
                   'searchKeyword',
                   'searchKeywordDescription'
                ]
            }),
            columns: [
                {
                	xtype:'actioncolumn',
    				iconCls:'icn-minus-circle',
    				icon:appContext+'/static/images/icn/minus-circle.png',
    				tooltip:'Remove',
    				width:30,
    				handler: function(grid,rowIndex,colIndex){
    					grid.getStore().removeAt(rowIndex);
    				}
    			
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'searchFieldDescription',
                    header: 'Search Field',
                    sortable: true,
                    flex:5
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'searchOperatorDescription',
                    header: 'Operator',
                    sortable: true,
                    flex:3
                },
                {
                    xtype: 'gridcolumn',
                    dataIndex: 'searchKeywordDescription',
                    header: 'Keyword',
                    sortable: true,
                    flex:10
                }
            ]
        },{
			region:'north',
			height:180,
			split:true,
			layout:'form',
			bodyPadding:6,
			border:false,
			items:[{
				xtype:'textfield',
				fieldLabel:'<strong>Bookmark name</strong>',
				id:'fldBookmarkName',
				name:'fldBookmarkName',
				allowBlank:false,
				labelAlign:'top'
			},
			{
				xtype:'fieldcontainer',
				fieldLabel:'Match the following rule(s)',
				labelAlign:'top',
				layout:'hbox',
				defaultType:'textfield',
				items:[{
					xtype: 'combo',
					flex:5,
					hideLabel:true,
	                typeAhead:false,
	                forceSelection:true,
	                store: 'Clara.Dashboard.store.AvailableSearchFields',
	                displayField:'label',
	                valueField: 'value',
	                editable:false,
		        	allowBlank:false,
	                mode:'local', 
		        	triggerAction:'all',
	                id: 'fldAvailableSearchFieldCombo',
	                listeners:{
	                	added: function(cb){
	                		cb.getStore().clearFilter();
	                		cb.getStore().filter("type",claraInstance.type);
	                	}
	                }
				},{
	                xtype: 'combo',
	                flex:3,
	                typeAhead:false,
	                forceSelection:true,
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
		        	margins: '0 0 0 5',
	                id: 'fldBookmarkSearchOperator'
	            },{
	        		xtype:'fieldcontainer',
	        		items:[],
	        		hidden:false,
	        		autoDestroy:false,
	        		id:'fldBookmarkCriteriaContainer',
	        		flex:10,
	        		margins: '0 0 0 5',
	        		layout:'fit'
	        	},{
	        		xtype:'button',
	        		text:'Add',
	        		disabled:true,
	        		id:'btnAddBookmarkCriteria',
	        		flex:2,
	        		margins: '0 0 0 5'
	        	}]
			}
			
			]
		}];
    	me.buttons = [{text:'Run (without saving)', iconCls:'icn-control', id:'btnRunBookmarkWithoutSaving'},{text:'Export to Excel', iconCls:'icn-document-excel-table', id:'btnExportExcelWithoutSaving', style:'margin-right:50px;'},{text:'Save Bookmark', id:'btnSaveBookmark'}];

        me.callParent();
    }
});

