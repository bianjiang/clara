Ext.define('Clara.Admin.view.NewLookupItemWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.newlookupitemwindow',
	layout: 'form',
	title: 'Add Lookup Item',
	modal:true,
	width:450,
	padding: 6,
	bodyPadding:6,
	modal: true,
	initComponent: function() {
		var me = this;
		var user = me.user;

		me.buttons = [
		              {
		            	  text: 'Save',
		            	  handler: function(){

		            		  var verify = true;
		            		  me.items.each(function(item){
		            			  verify = verify && item.validate();
		            		  });

		            		  if (verify == true){
		            			  
		            			  var rec = Ext.create('Clara.Admin.model.Thing', {
		            				  type: Ext.getCmp("fldType").getValue(),
		            				  value:Ext.getCmp("fldValue").getValue(),
		            				  description:Ext.getCmp("fldDescription").getValue(),
		            				  approved:Ext.getCmp("fldApproved").getValue()
		            			  });

		            			  rec.save({ 
		            			        success: function(record, operation)
		            			        {
		            			            clog("Save successful",record);
		            			            me.close();
		            			        },
		            			        failure: function(record, operation)
		            			        {
		            			        	cwarn("Save FAILED",record);
		            			        }
		            			    });

		            		  } else {
		            			  alert("Missing values. Check fields and try again.");
		            		  }

		            	  }
		              }
		              ];
		me.items = [
		            {
				fieldLabel: 'Type',
				xtype:'combo',
				id:'fldType',
				value:'SPONSOR',
			    store: new Ext.data.ArrayStore({
			    	fields:['id'],
			    	data:[['DRUG'],['DEVICE'],['SPONSOR'],['ICD_9_PROC'],['ICD_9_DIAG'],['TOXIN'],['RESEARCH_ORGANIZATION']]
			    }),
			    queryMode: 'local',
			    displayField: 'id',
			    valueField: 'id'
			},{
		            	xtype: 'textarea',
		            	id:'fldValue',
		            	fieldLabel: 'Value',
		            	allowBlank:false,
		            	anchor: '100%'
		            },{
		            	xtype: 'textarea',
		            	id:'fldDescription',
		            	fieldLabel: 'Description',
		            	allowBlank:false,
		            	anchor: '100%'
		            },{
		            	xtype: 'checkbox',
		            	id:'fldApproved',
		            	fieldLabel: 'Approved',
		            	anchor: '100%'
		            }
		            ];

		me.callParent();
	}
});