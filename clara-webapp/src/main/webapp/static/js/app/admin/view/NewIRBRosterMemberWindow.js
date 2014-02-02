Ext.define('Clara.Admin.view.NewIRBRosterMemberWindow', {
	extend: 'Ext.window.Window',
	requires:['Clara.Common.ux.ClaraUserField','Clara.Common.model.IrbRoster'],
	alias: 'widget.newirbrostermemberwindow',
	layout: 'form',
	title: 'Add Member',
	modal:true,
	width:450,
	padding: 6,
	bodyPadding:6,
	initComponent: function() {
		var me = this;

		me.buttons = [
		              {
		            	  text: 'Save',
		            	  handler: function(){

		            		  var verify = true;
		            		  me.items.each(function(item){
		            			  verify = verify && item.validate();
		            		  });

		            		  if (verify == true){
		            			  
		            			  // "roster" Java object is too complicated to use Store's save function. Switching to guns..
		            			  
		            			  var data = {
		            					  userId:  Ext.getCmp("fldUser").getValue(),
		            					  week:  Ext.getCmp("fldWeek").getValue(),
		            					  type:  Ext.getCmp("fldType").getValue(),
		            					  degree:  Ext.getCmp("fldDegree").getValue(),
		            					  specialty:  Ext.getCmp("fldSpecialty").getValue(),
		            					  expedited:  Ext.getCmp("fldExpedited").getValue(),
		            					  chair:  Ext.getCmp("fldChair").getValue(),
		            					  affiliated:  Ext.getCmp("fldAffiliated").getValue(),
		            					  comment:  Ext.getCmp("fldComment").getValue()
		            					
		            			  };
		            			  jQuery.ajax({
							    		url: appContext + '/ajax/rosters/reviewers/create',
							    		type: "POST",
							    		async: false,
							    		data: data,
							    		success: function(data){
							    			me.fireEvent('irbrostermemberadded');	// function is in controller
							    			me.close();
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
				fieldLabel: 'CLARA User',
				xtype:'clarafield.combo.user',
				hideLabel:false,
				id:'fldUser'
			},{
				fieldLabel: 'Week',
				xtype:'combo',
				id:'fldWeek',
				value:'WEEK_1',
			    store: new Ext.data.ArrayStore({
			    	fields:['id'],
			    	data:[['WEEK_1'],['WEEK_2'],['WEEK_3'],['WEEK_4']]
			    }),
			    queryMode: 'local',
			    displayField: 'id',
			    valueField: 'id'
			},{
				fieldLabel: 'Type',
				xtype:'combo',
				id:'fldType',
				value:'S',
			    store: new Ext.data.ArrayStore({
			    	fields:['id'],
			    	data:[['S'],['N']]
			    }),
			    queryMode: 'local',
			    displayField: 'id',
			    valueField: 'id'
			},{
            	xtype: 'textarea',
            	id:'fldDegree',
            	fieldLabel: 'Degree',
            	allowBlank:false,
            	anchor: '100%'
            },{
            	xtype: 'textarea',
            	id:'fldSpecialty',
            	fieldLabel: 'Specialty',
            	allowBlank:false,
            	anchor: '100%'
            },{
            	xtype: 'checkbox',
            	id:'fldAffiliated',
            	fieldLabel: 'Affiliated',
            	anchor: '100%'
            },{
            	xtype: 'checkbox',
            	id:'fldExpedited',
            	fieldLabel: 'Expedited',
            	anchor: '100%'
            },{
            	xtype: 'checkbox',
            	id:'fldChair',
            	fieldLabel: 'Chair',
            	anchor: '100%'
            },{
            	xtype: 'textarea',
            	id:'fldComment',
            	fieldLabel: 'Comment',
            	allowBlank:false,
            	anchor: '100%'
            }
		            ];

		me.callParent();
	}
});