Ext.define('Clara.Reports.view.AddDisplayFieldWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.adddisplayfieldwindow',
    width:500,
    parentWindow:{},
    iconCls:'icn-gear',
    layout: {
        type: 'form'
    },

    style : 'z-index: -1;', // IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
    
    bodyPadding: 5,
    defaultType:'combobox',
    title:'Add display field',
    initComponent: function() {
    	var me = this;
    	me.save = function(){

            var report = Clara.Reports.app.getController("UserReport").selectedUserReport;
    		var	data = {
    			fieldname: Ext.getCmp("cbDisplayField").getValue()
    		};
    		
        	jQuery.ajax({
    			  type: 'POST',
    			  async:false,
    			  url: appContext+'/ajax/reports/'+report.get("id")+'/add-displayfield',
    			  data: data,
    			  success: function(data){
      		        if (!data.error){
      		        	var userReportDFStore = Ext.StoreMgr.get('UserReportDisplayFields');
      		        	userReportDFStore.load();
              			me.close();
      		        }
    			  },
    			  error: function(){
    				  cwarn("Error saving display field");
    			  }
    		});
        };
    	me.items = [{
        	xtype:'combobox',
        	id:'cbDisplayField',
        	fieldLabel:'Field:',
        	allowBlank:false,
        	store:'ReportDisplayFields',
        	displayField:'fieldlabel',
        	valueField:'fieldname'
        }
        ];
    	me.buttons = [{text:'Save', handler:function(){
    		if (Ext.getCmp("cbDisplayField").isValid()) me.save();
    		else alert("Choose a display field");
    	}}];

        me.callParent();
    }
});

