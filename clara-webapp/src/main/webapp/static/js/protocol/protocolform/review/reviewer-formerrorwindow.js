Ext.ns('Clara','Clara.Reviewer');

//Clara.Reviewer.FormErrorStore = ;

Clara.Reviewer.FormErrorWindow = Ext.extend(Ext.Window, {
	selectedRevertId:'',
	modal:true,
	width:500,
	height:400,
    title: 'Checking Form for Errors..',
    layout: 'fit',
    success: {},
	constructor:function(config){		
		Clara.Reviewer.FormErrorWindow.superclass.constructor.call(this, config);
	},
	initComponent: function() {
		var t =this;
		var config = {
				buttons:[{
					text: 'Close',
					id:'btnFormErrorWindowClose',
					disabled:false,
	                handler:function(){
						t.close();
					}
				},{
					text: 'Continue..',
					id:'btnFormErrorWindowContinue',
					disabled:true,
	                handler:function(){
	                	t.success();
						t.close();
					}
				}],
				items: [{
					xtype:'clarareviewerformerrorpanel',
					parentWindow:t
				}]
		};
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Reviewer.FormErrorWindow.superclass.initComponent.apply(this, arguments);
	}
});

Clara.Reviewer.FormErrorPanel = Ext.extend(Ext.grid.GridPanel, {
	border:false,
	constructor:function(config){		
		Clara.Reviewer.FormErrorPanel.superclass.constructor.call(this, config);
	},
	parentWindow:null,
	reloadStore: function(){
		this.getStore().removeAll();
		this.getStore().load();
	},
	initComponent: function() {
		var t =this;
		var config = {

					     deferRowRender:false,
					     viewConfig: {
					         forceFit: true,
					         emptyText: 'No errors found. Click "Continue.." to sign this form.',
					         headersDisabled:true,
					         getRowClass: function(record,index,rp,st){
								return (index == st.getCount()-1)?"history-row-current":"history-row";
							 }
					      },
					      listeners: {
					          afterrender: function(grid) {
					              grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
					          },
					      	  rowclick: function(grid,index,e){
					        	  var rec = grid.getStore().getAt(index);
					          },
					          rowdblclick:function (gridObj, rowIdx, e) {
					                    var row = gridObj.getStore().getAt(rowIdx);
					                    clog("SAW REC",row);
					                    if (typeof row != 'undefined' && row.data.pagename != ''){
					                    	window.top.location.href  = appContext + "/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/" + claraInstance.form.id +"/" + claraInstance.form.urlName +"/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/" + row.data.pageref.toLowerCase() + "?noheader=true&committee="+((claraInstance.user.committee)?claraInstance.user.committee:'PI');
					                    	// t.parentWindow.close();
					                    }
					                }
					            
					      },
					     stripeRows:true,
					     loadMask:true,
					     border: false,
					     store:new Ext.data.Store({
					    		autoLoad:false,
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
					    				
					    				// check for one row of null, remove it if found:
					    				if (store.getCount() == 1){
					    					var rec = store.getAt(0);
					    					if (rec.get("constraintLevel") == null && rec.get("errorMessage") == null){
					    						store.removeAll();
					    					}
					    				}
					    				
					    				clog("reviewer-formerrorwindow: store.getCount() = " + store.getCount());
					    				clog("store",store,"errors",hasErrors,"comm",claraInstance.user.committee);
					    				Ext.getCmp("btnFormErrorWindowContinue").setDisabled(hasErrors);
					    			}
					    		
					    		}

					    	}),
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
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Reviewer.FormErrorPanel.superclass.initComponent.apply(this, arguments);
		this.reloadStore();
	}
});
Ext.reg('clarareviewerformerrorpanel', Clara.Reviewer.FormErrorPanel);