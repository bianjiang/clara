
Ext.ns("Clara");

Clara.FormReviewErrorStore = new Ext.data.Store({
	autoLoad:true,
	header :{
    	'Accept': 'application/json'
	},
	proxy: new Ext.data.HttpProxy({
		url: appContext+"/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/validate?committee="+claraInstance.user.committee,
		method:"GET"
	}),
	reader: new Ext.data.JsonReader({}, [
		{name:'pagename', mapping:'additionalData.pagename'},
		{name:'pageref', mapping:'additionalData.pageref'},
		{name:'constraintLevel', mapping:'constraint.constraintLevel'},
		{name:'errorMessage', mapping:'constraint.errorMessage'}
	]),
	listeners: {
		'load': function(store,records,opts){
			var hasErrors = (store.find("constraintLevel", "ERROR") > -1)?true:false;

			if (store.getCount() > 0){
				clog("store.getCount() = " + store.getCount());
				
			}
			clog("store",store,"errors",hasErrors,"comm",claraInstance.user.committee);
				if (claraInstance.user.committee == 'PI' && hasErrors == false && usingNoHeaderTemplate == false){
					//jQuery('#review-no-messages').show();
					//signFormPanel.render('sign-and-submit');
					
					Ext.getCmp("formreviewpanel-info").layout.setActiveItem(1);
					// show the sign panel.
					Ext.getCmp("formreviewpanel-signsubmit").show();
				} else if (claraInstance.user.committee != 'PI' || usingNoHeaderTemplate == true){
					Ext.getCmp("formreviewpanel-info").layout.setActiveItem(2);
					//jQuery('#review-no-messages-othercommittee').show();
				}
			
		}
	
	}

});

Clara.FormReviewErrorGridPanel = Ext.extend(Ext.grid.GridPanel, {
	constructor:function(config){		
		Clara.FormReviewErrorGridPanel.superclass.constructor.call(this, config);
	},
	initComponent:function(){
		var t = this;
		var config = {
				border:false,
		    	frame:false,
		    	trackMouseOver:false,
		    	//renderTo: 'review-list',
		        store: Clara.FormReviewErrorStore,
		        title:'Please pay attention to the following messages',
		        sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
		        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Performing a final check. Please wait..."}),
		        height:250,
		        viewConfig: {
		    		forceFit:true
		    	},
		    	listeners:{
		            'rowdblclick':{
		                fn: function (gridObj, rowIdx, e) {
		                    var row = gridObj.getStore().getAt(rowIdx);
		                    if (typeof row != 'undefined' && row.data.pagename != ''){
		                    	location.href = appContext + "/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id +"/" + claraInstance.form.urlName +"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/" + row.data.pageref.toLowerCase();
		                    }
		                }
		            }
		        },
		        columns: [
		                  {
		                	  	dataIndex:'constraintLevel',
		                	  	sortable:true,
		                	  	renderer:function(value, p, record){

		                	  		clog(record.data);
		                	  		var outHTML='<div class="review-row">';

		                	  		outHTML = outHTML + '<div class="review-row-icon-'+record.data.constraintLevel+'">'+record.data.constraintLevel+'</div><div class="review-row-message"><h3 class="review-row-message-page">'+record.data.pagename+'</h3>';
		                	  		outHTML = outHTML + '<span class="review-row-message-description">'+record.data.errorMessage+'</span></div>';

		                	  		return outHTML+'</div>';

		                	  	},
		                	  	width:680
		                  }
		        ]
		    
		};
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.FormReviewErrorGridPanel.superclass.initComponent.apply(this, arguments);
	}
	
});
Ext.reg('claraformreviewerrorpanel', Clara.FormReviewErrorGridPanel);

Clara.FormReviewPanel = Ext.extend(Ext.Panel, {
	authenticated:null,
	constructor:function(config){		
		Clara.FormReviewPanel.superclass.constructor.call(this, config);
	},
	initComponent:function(){
		
		var t = this;
		clog(t.authenticated);
		var config = {
			border:false,
			layout:'border',
			items: [	
				
				{
					region:'center',
					xtype:'panel',
					border:false,
					id:'formreviewpanel-info',
					layout:'card',
					items:[{xtype:'claraformreviewerrorpanel'},{xtype:'container',border:false,autoScroll:true,contentEl:'review-no-messages'},{xtype:'container',autoScroll:true,contentEl:'review-no-messages-othercommittee'}],
					activeItem:0
				},
					{
						listeners:{
							hide:function(){t.doLayout();},
							show:function(){t.doLayout();}
						},
					    region:'south',
					    height:110,
					    autoScroll:true,
					    hidden:true,
					    id:'formreviewpanel-signsubmit',
					    xtype:'form',
						standardSubmit: true,
				        labelWidth: 64, // label settings here cascade unless overridden
				        url: appContext + "/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.form.urlName+"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/sign",
				        //id:'signSubmitForm',
				        frame:true,
				        title: 'Enter your username and password below.',
				        bodyStyle:'padding:5px 5px 0',
				        defaults: {
				            width: 120
				         },
				        items: [
				    	        {
				    				itemCls:'review-password',
				    	        	xtype: 'textfield',
				    	        	inputType:'password',
				    			    fieldLabel: 'Password',
				    			    name: 'password',
				    			    allowBlank:false,
				    			    tabIndex:3
				    			},{
				    	        	itemCls:'review-username',
				    	        	xtype: 'textfield',
				    			    fieldLabel: 'Username',
				    			    name: 'username',
				    			    allowBlank:false,
				    			    tabIndex:2
				    			},{
				                    xtype:'label',
				                    text: t.authenticated=='false'?'Wrong username and password!':'',
				                    name: 'error',
				                    style: 'font-weight:bold;color:red;font-size:16px;',
				                    tabIndex:2
				                 }
				    	        
				        ],
				        buttons: [{
				            text: 'Sign and Submit for Review',
					        formBind:true,
				            handler:function(){ 
				        		Ext.getCmp("formreviewpanel-signsubmit").getForm().submit(); 
				            }
				        }]
				    
					}
			]
		};
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		  
		// call parent
		Clara.FormReviewPanel.superclass.initComponent.apply(this, arguments);
	}

});

