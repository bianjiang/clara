Ext.define('Clara.Dashboard.view.BookmarkPanel', {
	extend: 'Ext.grid.GridPanel',
	alias: 'widget.bookmarkpanel',
	autoScroll: true,
	cls:'sidebar',
	unstyled:true,
	style:'border:1px solid #96baea;border-left:0px;',
	rowLines:false,
	hideHeaders:true,
	requires:['Clara.Reports.ux.ComboCriteriaField','Clara.Common.ux.ClaraUserField','Clara.Common.ux.ClaraFundingSourceField','Clara.Common.ux.ClaraCollegeField'],
	title:'Bookmarks',
	store: 'Clara.Dashboard.store.Bookmarks',
	initComponent: function() { 
		var me = this;
		me.border=false;
		me.viewConfig = {
				trackOver:false,
				stripeRows: false,
				selectedItemCls: 'selected-bookmark',
				getRowClass: function(record, rowIndex, rowParams, store) {
					var immutableBookmarkMaxIndex = (claraInstance.type=="protocol")?3:1;
					var cls = "bookmark-row";
		            if (rowIndex < immutableBookmarkMaxIndex) {
		                cls += " immutable-bookmark";
		            }
		            return cls;
		        }
		};


		me.dockedItems= [{
			xtype:'toolbar',
			dock:'bottom',
			items:[{
				xtype:'button',
				text:'<strong>New</strong>',
				iconCls:'icn-gear',
				id:'btnAddBookmark'
			},{
				xtype:'button',
				text:'Edit',
				iconCls:'icn-gear--pencil',
				disabled:true,
				id:'btnEditBookmark'
			},{
				xtype:'button',
				text:'Remove',
				disabled:true,
				iconCls:'icn-gear--minus',
				id:'btnRemoveBookmark'
			}]
		}];
		me.columns = [{
			header : 'Bookmark',
			dataIndex : 'name',
			flex:1
		}];
		me.listeners = {
				added: function(){
					me.getStore().load();
				}	
		};
		me.callParent();

	}
});