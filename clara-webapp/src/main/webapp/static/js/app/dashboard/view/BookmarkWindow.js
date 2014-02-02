Ext.define('Clara.Dashboard.view.BookmarkWindow', {
    extend: 'Ext.window.Window',
    requires:[],
    alias: 'widget.bookmarkwindow',
    title: 'New Bookmark',
    width:800,
    height:450,
    bookmark:{},
    iconCls:'icn-gear',
    layout: {
        type: 'border'
    },

    initComponent: function() {
    	var me = this;
    	me.listeners={
        	show:function(w){}
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
    	me.buttons = [{text:'Run without saving', id:'btnRunBookmarkWithoutSaving'},{text:'Save', id:'btnSaveBookmark'}];

        me.callParent();
    }
});

