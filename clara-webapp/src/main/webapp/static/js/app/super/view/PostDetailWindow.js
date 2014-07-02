Ext.define('Clara.Super.view.PostDetailWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.postdetailwindow',
	layout: 'form',
	title: 'New Post',
	modal:true,
	width:800,
	padding: 6,
	bodyPadding:6,
	initComponent: function() {
		var me = this;
		
		me.buttons = [
		              {
		            	  text: 'Save',
		            	  handler: function(){

		            		  var msg = Ext.getCmp("fldMessage").getValue();
		            		  var title = Ext.getCmp("fldTitle").getValue();
		            		  var expDate = Ext.getCmp("fldExpireDate").getValue().getFullYear()+"-"+(Ext.getCmp("fldExpireDate").getValue().getMonth()+1) + "-" + Ext.getCmp("fldExpireDate").getValue().getDate();
		            		  clog("Expire: "+expDate, new Date(expDate));
		            		  if (msg.length <= 18000){
		            			  
		            			  var rec = Ext.create('Clara.Super.model.MessagePost', {
		            				  title:title,
		          	    		      message:msg,
		          	    		      messageLevel:Ext.getCmp("fldMessageLevel").getValue(),
		          	    		      created:new Date(),
		          	    		      expireDate:expDate
		            			  });

		            			  rec.save({ 
		            			        success: function(record, operation)
		            			        {
		            			            clog("Save successful",record);
		            			            Ext.data.StoreManager.lookup("MessagePosts").reload();
		            			            me.close();
		            			        },
		            			        failure: function(record, operation)
		            			        {
		            			        	cwarn("Save FAILED",record);
		            			        }
		            			    });
		            			  
		            		  } else {
		            			  alert("HTML of message cannot be more than 18000 characters.");
		            		  }
		            		  
		          	    		

		            	  }
		              }
		              ];
		me.items = [{
			fieldLabel:'Title',
			id:'fldTitle',
			xtype:'textfield',
			flex:1,
			maxLength:255,
			enforceMaxLength:true,
			allowBlank:false
		},{
			fieldLabel:'Expires',
			id:'fldExpireDate',
			xtype:'datefield',
			allowBlank:false
		},{
			fieldLabel: 'Severity',
			allowBlank:false,
			xtype:'combo',
			id:'fldMessageLevel',
			value:'INFO',
		    store: new Ext.data.ArrayStore({
		    	fields:['id','desc'],
		    	data:[['INFO','Information'],['MODERATE','Moderate'],['SEVERE','Severe']]
		    }),
		    queryMode: 'local',
		    displayField: 'desc',
		    valueField: 'id'
		},{
			fieldLabel:'Message',
			id:'fldMessage',
			xtype:'htmleditor',
			defaultValue:'<!-- Empty Message -->',
			flex:1,
			height:240,
			allowBlank:false,
			enableColors:false,
			enableFont:false,
			enableFontSize:false
		}];

		me.callParent();
	}
});
