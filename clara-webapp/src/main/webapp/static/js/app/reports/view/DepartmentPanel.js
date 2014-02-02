Ext.define('Clara.Reports.view.DepartmentPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.departmentpanel',
    title: 'Choose a Department',
    repsonibleDepartment:{},
    collegestore:new Ext.data.Store({
		header :{
           'Accept': 'application/json'
       },
		proxy: new Ext.data.HttpProxy({
			url: appContext + '/ajax/colleges/list',
			method:'GET'
		}),
		autoLoad:true,
		reader: new Ext.data.JsonReader({
				idProperty: 'id'
				}, [{name:'id'},
				    {name:'sapCode'},
				    {name:'name'}])
       }),
    initComponent: function() {
    	var t=this;
    	this.items=[{
            xtype: 'combo',
            width:350,
            itemId: 'fldCollege',
            fieldLabel:'College',
            store:t.collegestore,
            id: 'fldCollege',
            typeAhead:false,
            forceSelection:true,
            displayField:'name', 
            valueField:'id',
            editable:false,
        	allowBlank:false,
            mode:'local', 
        	triggerAction:'all',
        	listeners:{
        		'select': function(cmb,rec,idx){
        			t.repsonibleDepartment.collegeid = rec.data.id;
        			t.repsonibleDepartment.collegename = rec.data.name;
        			t.repsonibleDepartment.deptid = 0;
        			t.repsonibleDepartment.deptname = "";
        			t.repsonibleDepartment.subdeptid = 0;
        			t.repsonibleDepartment.subdeptname = "";
        			t.selectCollege(t.repsonibleDepartment.collegeid);
        		}
        	}
        }];
        this.callParent();
    }
});