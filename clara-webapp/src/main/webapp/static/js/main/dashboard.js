Ext.ns('Clara','Clara.Protocols','Clara.Dashboard');
Ext.QuickTips.init();


var currentCommitteeQueue;
var dashbaordTabPanel;
var protocolPanel;
var toolbar;
var selRecordStore;
var selectedProtocolUrlPrefix = appContext+"/protocols/";


function renderHome(){
	
	
	new Ext.Viewport({
		layout:'border',
		listeners:{
			afterrender:function(){
				jQuery("#protocol-dashboard-starthere").delay(500).fadeIn(2000);
			}
		},
		items:[	{
				    region: 'north',
				    contentEl:'clara-header',
				    bodyStyle:{ backgroundColor:'transparent' },
				    height:48,
				    border: false,
				    margins: '0 0 0 0'
				}, 
				
				{	xtype:'panel',
					region:'center',
					border:true,
					layout:'border',
					tbar: new Ext.Toolbar({
						items:[{
								xtype:'panel',
								html:'Protocols',
								padding:4,
								unstyled:true,
								bodyStyle:'width:204px;font-size:24px;background:transparent;',
								border:false
							   },
				                {
						            xtype:'button',
						            text: '<span style="font-size:14px;font-weight:800;">Create...</span>',
						            ctCls:'x-btn-over',
						            iconCls: 'icn32-add',
						            scale:'large',
						            handler: function(){
							        	new Clara.NavigationWizard.MainWindow({title:"", activeTab:0}).show();
				        			}
						        },{
						        	xtype:'container',
						        	html:'<div id="protocol-dashboard-starthere">Start here to begin a new submission.</div>'
						        },'->',{
						        	xtype:'uxsearchfield',
						        	store:Clara.Protocols.ProtocolListStore,
						        	emptyText:'Search title or IRB Number',
						        	paramName : 'keyword',
						        	reloadAllAsClear:true,
						        	beforeSearch: function(){
						        		var bmPanel = Ext.getCmp("bookmark-panel");
						        		if (bmPanel){
						        			bmPanel.select(0,false,true);	// Set search back to "All Protocols", SUPPRESS EVENT to prevent double call to search.
						        		}
						        		Clara.Protocols.ProtocolListStore.setBaseParam("searchCriterias", null );
						        		Clara.Protocols.ProtocolListStore.setBaseParam("keyword", Clara.SearchKeyword );
						        		return true;
						        	},
						        	beforeClear:function(){
						        		var bmPanel = Ext.getCmp("bookmark-panel");
						        		if (bmPanel){
						        			bmPanel.select(0,false);	// Set search back to "All Protocols"
						        		}
						        		Clara.SearchKeyword = "";
						        		Clara.Protocols.ProtocolListStore.setBaseParam('keyword','');
						        	},
						        	listeners:{
						        		change:function(f,v,ov){
						        			Clara.SearchKeyword = v;
						        		}
						        	}
						        }
				           	]}),
					items:[
					    {xtype:'panel',border:false,region:'west',width:250,split:true,layout:'fit',items:[{xtype:'clarabookmarkpanel', type:'protocols', region:'west', width:200, split:true}]
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
						        				   var url = appContext+"/ajax/protocols/search-bookmarks/"+Clara.Dashboard.SelectedBookmarkRecord.get("id")+"/remove";
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
					    
					    
					    },
					    {xtype:'claraprotocollistpanel', region:'center', split:true}
					]
				}
		       ]
	});
	
	
	
}
