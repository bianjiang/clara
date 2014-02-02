Ext.define('Clara.Common.ux.ClaraCollegeField', {
	extend: 'Ext.form.FieldSet',
	 alias: 'widget.clarafield.college',
	 flex:1,
	 layout:'form',
	 name:'fldCollegeDeptSubdept',
	 selectedCollege:null,
	 selectedDept:null,
	 selectedSubDept:null,
	 deptStore:null,
	 subDeptStore: null,
	 title:'College / Department (keep blank for All Colleges)',
	 getName: function() { return this.name; },
	 getFieldLabel: function() { return "College / Dept. / Subdept."; },
	 getRawValue: function() {
		 var me = this;
		 if (!me.selectedCollege) return "All Colleges / Departments";
		 var v = me.selectedCollege.get("name");
		 v += (me.selectedDept)?(": " + me.selectedDept.get("name")):"";
		 v += (me.selectedSubDept)?(": " + me.selectedSubDept.get("name")):"";
		 return v;
	 },
	 getValue: function() {
		 var me = this;
		 if (!me.selectedCollege) return "*";
		 var v = me.selectedCollege.get("id");
		 v += (me.selectedDept)?("," + me.selectedDept.get("id")):"";
		 v += (me.selectedSubDept)?("," + me.selectedSubDept.get("id")):"";
		 return v;
	 },
	 validate: function() {
		return true;//Ext.getCmp("fldCollege").validate(); 
	 },
	 initComponent: function() {
		 var me = this;
		 me.items = [{
			 xtype:'combo',
			 store: 'Clara.Common.store.Colleges',
			 displayField: 'name',
			 valueField:'sapCode',
			 typeAhead:false,
			 hideLabel:false,
			 fieldLabel:'College',
			 id:'fldCollege',
			 hideTrigger:false,
			 flex:1,
			 listeners:{
				 'select': function(cmb,recs,idx){
						me.selectedCollege = recs[0];
						clog("SELECTED COLLEGE",me.selectedCollege);
						Ext.getCmp('fldDept').clearValue();
						Ext.getCmp('fldSubDept').clearValue();
						Ext.getCmp('fldSubDept').disable();
						me.deptStore = Ext.data.StoreManager.lookup('Clara.Common.store.Departments');
						me.deptStore.getProxy().url = appContext + '/ajax/colleges/'+me.selectedCollege.get("id")+'/departments/list';
						me.deptStore.load();
						Ext.getCmp('fldDept').enable();
					}
			 },
			 listConfig:{
				 loadingText: 'Finding colleges..',
				 emptyText: 'No colleges found.',
				 getInnerTpl: function() {
		             return '<h3>{name}</h3>';
		         }
			 }
		 },{
			 xtype:'combo',
			 store: 'Clara.Common.store.Departments',
			 disabled:'true',
			 displayField: 'name',
			 valueField:'sapCode',
			 typeAhead:false,
			 hideLabel:false,
			 fieldLabel:'Department',
			 id:'fldDept',
			 hideTrigger:false,
			 flex:1,
			 listeners:{
				 'select': function(cmb,recs,idx){
						me.selectedDept = recs[0];
						clog("SELECTED DEPT",me.selectedDept);
						Ext.getCmp('fldSubDept').clearValue();
						me.subDeptStore = Ext.data.StoreManager.lookup('Clara.Common.store.Subdepartments');
						me.subDeptStore.getProxy().url = appContext + '/ajax/colleges/'+me.selectedCollege.get("id")+'/departments/'+me.selectedDept.get("id")+'/sub-departments/list';
						me.deptStore.load();
						Ext.getCmp('fldSubDept').enable();
					}
			 },
			 listConfig:{
				 loadingText: 'Finding departments..',
				 emptyText: 'No departments found.',
				 getInnerTpl: function() {
		             return '<h3>{name}</h3>';
		         }
			 }
		 },{

			 xtype:'combo',
			 store: 'Clara.Common.store.Subdepartments',
			 displayField: 'name',
			 disabled:'true',
			 valueField:'sapCode',
			 typeAhead:false,
			 hideLabel:false,
			 fieldLabel:'Subdepartment',
			 id:'fldSubDept',
			 hideTrigger:false,
			 flex:1,
			 listeners:{
				 'select': function(cmb,recs,idx){
						me.selectedSubDept = recs[0];
						clog("SELECTED SUBDEPT",me.selectedSubDept);
					}
			 },
			 listConfig:{
				 loadingText: 'Finding subdepartments..',
				 emptyText: 'No subdepartments found.',
				 getInnerTpl: function() {
		             return '<h3>{name}</h3>';
		         }
			 }
		 
		 }];
		 
		 this.callParent();
	 }
	 
});