Ext.define('Clara.Agenda.view.NewAgendaWindow', {
	extend: 'Ext.window.Window',
	alias: 'widget.newagendawindow',
	layout: 'form',
	title: 'New Agenda',
	modal:true,
	width:450,
	padding: 6,
	bodyPadding:6,
	initComponent: function() {
		var me = this;
		var controller = Clara.Agenda.app.getController("Agenda");
		
		me.buttons = [
		              {
		            	  text: 'Save',
		            	  handler: function(){
		            		var fldADate = Ext.getCmp('fldNewAgendaDate');
		          			var fldAComm = Ext.getCmp('fldNewAgendaCommittee');
		          			if (fldADate.validate() && fldAComm.validate()){
		          				var dt = (fldADate.getValue().getMonth()+1) + "/" + fldADate.getValue().getDate() + "/" + fldADate.getValue().getFullYear();
		          				
		          				controller.loadingMask.show();
		          	    		Ext.Ajax.request({
		          	    			method:'GET',
		          	    			url: appContext + "/ajax/agendas/create",
		          	    		    params: {
		          	    		    	userId:claraInstance.user.id  ,
		          	    		    	agendaDate:dt,
		          	    		    	irbRoster:fldAComm.getValue()
		          	    		    },
		          	    		    success: function(response){
		          	    		    	clog('NewAgendaWindow.save(): Ext.Ajax success',response);
		          	    		    	controller.loadingMask.hide();
		          	    		    	controller.fireEvent("agendasUpdated");
		          	    		    	me.close();
		          	    		    },
		          	    		    failure: function(error) {
		          	                    cwarn('NewAgendaWindow.save(): Ext.Ajax failure',error);
		          	                  controller.loadingMask.hide();
		          	                }
		          	    		});
		          	    		
		          			} else {
		          				alert("Choose a valid date and committee");
		          			}
		            	  }
		              }
		              ];
		me.items = [{
			fieldLabel:'Date',
			id:'fldNewAgendaDate',
			xtype:'datefield',
			allowBlank:false
		},{
			fieldLabel: 'Week',
			allowBlank:false,
			xtype:'combo',
			id:'fldNewAgendaCommittee',
			value:'WEEK_1',
		    store: new Ext.data.ArrayStore({
		    	fields:['id','desc'],
		    	data:[['WEEK_1','Committee 1'],['WEEK_2','Committee 2'],['WEEK_3','Committee 3'],['WEEK_4','Committee 4']]
		    }),
		    queryMode: 'local',
		    displayField: 'desc',
		    valueField: 'id'
		}];

		me.callParent();
	}
});