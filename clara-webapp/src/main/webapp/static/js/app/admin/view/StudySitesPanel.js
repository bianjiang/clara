Ext.define('Clara.Admin.view.StudySitesPanel', {
	extend: 'Ext.grid.Panel',
	requires: ['Ext.ux.form.SearchField'],
	alias: 'widget.studysitespanel',
	title:'Study Sites',
	iconCls:'icn-building',
	border:false,
	viewConfig:{
		trackOver:false
	},
	store:'Clara.Common.store.StudySites',
	listeners:{
		itemclick: function(gp,rec){
			adminGlobals.selectedStudySite = rec;
			if (rec.get("approved") == false) Ext.getCmp("btnApproveSite").setDisabled(false);
		},
		itemdblclick: function(gp,rec){
			var w = Ext.create("Clara.Admin.view.StudySiteWindow", { site:rec }).show();
		}
	},
	initComponent: function() {
		var t = this;
		var siteStore = Ext.data.StoreManager.lookup('Clara.Common.store.StudySites');
		this.dockedItems = [{
			dock: 'top',
			border:false,
			xtype: 'toolbar',
			items: [{
				xtype:'searchfield',
				store:siteStore,
				title:'Search site name or location',
				emptyText:'Search site name or location',
				paramName : 'keyword',
				reloadAllAsClear:true,
				flex:1,
				beforeSearch: function(){
              	  clog("beforesearch!");
              	Ext.getCmp("btnApproveSite").setDisabled(true);
              	  siteStore.getProxy().url = (appContext+"/ajax/protocols/protocol-forms/sites/search");
              	  clog("leaving beforesearch");
              	  return true;
                },
               
                afterClear:function(){
                	Ext.getCmp("btnApproveSite").setDisabled(true);
                	siteStore.getProxy().url = (appContext+"/ajax/sites/list");
                	siteStore.load();
                }
			},'->', {
				xtype: 'button',
				id:'btnAddSite',
				text: 'Add Site',
				disabled:false,
				iconCls:'icn-building--plus',
				handler: function(){
					Ext.create("Clara.Admin.view.StudySiteWindow", { site:null }).show();
				}
			},{
				xtype: 'button',
				id:'btnApproveSite',
				action:'approve_study_site',
				text: 'Approve site',
				disabled:true,
				iconCls:'icn-building',
				handler: function(){
					var url = appContext+'/ajax/sites/approve';
					jQuery.ajax({
						  type: 'POST',
						  async:false,
						  url: url,
						  data: {siteid: adminGlobals.selectedStudySite.get("id")},
						  error: function(){
							  alert("Error approving site.");
						  },
						  dataType: 'xml'
					});
					siteStore.load();
		      		Ext.getCmp("btnApproveSite").disable();
				}
			}]
		}];
		
		t.columns = [
                     
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'siteName',
                         header: 'Name',
                         sortable: true,
                         flex:1
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'city',
                         header: 'City',
                         sortable: true,
                         width: 150
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'state',
                         header: 'State',
                         sortable: true,
                         width: 47
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'approved',
                         header: 'Approved?',
                         sortable: true,
                         width: 67
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'fwaObtained',
                         header: 'FWA?',
                         sortable: true,
                         width: 67
                     },
                     {
                         xtype: 'gridcolumn',
                         dataIndex: 'fwaNumber',
                         header: 'FWA #',
                         sortable: true,
                         width: 110
                     }
                 ];
		t.callParent();
	}

});