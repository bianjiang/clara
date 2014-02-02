Ext.ns('Clara','Clara.Contracts');
Ext.QuickTips.init();

var currentCommitteeQueue;
var dashbaordTabPanel;
var contractPanel;
var toolbar;
var selRecordStore;
var selectedContractUrlPrefix = appContext+"/contracts/";


Clara.SearchKeyword = "";

function renderContractHome(){
	
	
	new Ext.Viewport({
		layout:'border',
		items:[	{
				    region: 'north',
				    contentEl:'clara-header',
				    bodyStyle:{ backgroundColor:'transparent' },
				    height:48,
				    border: false,
				    margins: '0 0 0 0'
				}, 
				{
					xtype:'panel',
					region:'center',
					border:false,
					layout:'border',
					tbar: new Ext.Toolbar({
						items:[{
								xtype:'panel',
								html:'Contracts',
								padding:4,
								unstyled:true,
								bodyStyle:'width:204px;font-size:24px;background:transparent;',
								border:false
							   },
				                {
						            xtype:'button',
						            text: 'New Contract...',
						            iconCls: 'icn-plus-button',
						            handler: function(){
						            	new Clara.NavigationWizard.MainWindow({title:"", activeTab:1}).show();
				        			}
						        },'->',{
						        	xtype:'uxsearchfield',
						        	store:Clara.Contracts.ContractListStore,
						        	emptyText:'Search Contract Number, Company/Entity Name, PI Name or IRB#',
						        	paramName : 'keyword',
						        	reloadAllAsClear:true,
						        	beforeClear:function(){
						        		Clara.Contracts.ContractListStore.setBaseParam('keyword','');
						        	},
						        	listeners:{
						        		change:function(f,v,ov){
						        			Clara.SearchKeyword = v;
						        		}
						        	}
						        }
				           	]}),
					items:[{
					xtype:'panel',border:false,region:'west',width:250,split:true,layout:'fit',items:[{xtype:'clarabookmarkpanel', type:'contracts', region:'west', width:175, split:true}]
				,bbar: new Ext.Toolbar({
				    items:[{
				        xtype:'button',
				        text: 'New Bookmark...',
				        iconCls: 'icn-bookmark--plus',
				        handler: function(){
				        	var newBmWindow = new Clara.Dashboard.WindowAddSearchBookmark({entity:claraInstance.type});
				        	newBmWindow.show();
						}
				    },
				    {
				        xtype:'button',
				        text: 'Remove',
				        id:'btnRemoveBookmark',
				        iconCls: 'icn-bookmark--minus',
				        disabled:true,
				        handler: function(){
				        	Ext.Msg.show({
				        		   title:'Remove bookmark?',
				        		   msg: 'Are you sure you want to remove this bookmark? (NOTE: This will not delete any of your data)',
				        		   buttons: Ext.Msg.YESNOCANCEL,
				        		   fn: function(btn){
				        			   if (btn == 'yes'){
				        				   var url = appContext+"/ajax/contracts/search-bookmarks/"+Clara.Dashboard.SelectedBookmarkRecord.get("id")+"/remove";
			    						   var data = {userId: claraInstance.user.id || 0};
				        				   jQuery.ajax({
			    								  type: 'POST',
			    								  async:false,
			    								  url: url,
			    								  data: data,
			    								  success: function(v){
			    									  Clara.Dashboard.MessageBus.fireEvent('bookmarksupdated', this);
			    									  Ext.getCmp("btnRemoveBookmark").setDisabled(true);
			    								  },
			    								  error: function(){
			    									  
			    								  }
			    							});
				        			   }
				        		   },
				        		   animEl: 'elId',
				        		   icon: Ext.MessageBox.QUESTION
				        		});
						}
				    }]
				}),	
			    
			    
			    
				},{
					xtype:'claracontractlistpanel',
					region:'center',
					border:true
				}]
				}
				
		       ]
	});
}
