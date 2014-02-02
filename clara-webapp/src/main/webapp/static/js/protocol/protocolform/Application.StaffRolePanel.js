// TODO: DO NOT make two copies lise this. Instead, extend formpanel and make a class for each type.


var staffConflictsPanel = new Ext.form.FormPanel({
	id:'coi-group-panel',
	region:'south',
	labelWidth:250,
	title:'Conflicts of Interest',
	//autoHeight:true,
	height:150,
	layout:'form',
	collapsed:false,
	collapsible:false,
	items:[
			new Ext.form.RadioGroup({	
			    fieldLabel: 'Does this staff member have a conflict of interest that may impact this study?',
			    
			    columns:[50,50],
			    items:[
			           {boxLabel:'Yes',inputValue:'y',name: '/conflict-of-interest',width:50},
			           {boxLabel:'No',inputValue:'n',name: '/conflict-of-interest',width:50}
			           ]
			}),
			new Ext.form.TextField({	
			    fieldLabel: 'Please describe the conflict',
			    name: '/conflict-of-interest-description'
			}),
			new Ext.form.Label({	
			    html:'<strong>Important Note: </strong>This question does <strong>NOT</strong> replace your responsibility to keep the information in <a href="#">ClickCommerce COI</a> current.'
			})
	       ]
});

var editStaffConflictsPanel = new Ext.form.FormPanel({
	id:'edit-coi-group-panel',
	region:'south',
	title:'Conflicts of Interest',
	labelWidth:350,
	height:120,
	layout:'form',
	collapsed:false,
	collapsible:false,
	items:[
			new Ext.form.RadioGroup({	
			    fieldLabel: 'Does this staff member have a conflict of interest that may impact theis study?',
			    columns:[50,50],
			    items:[
			           {boxLabel:'Yes',inputValue:'y',name: '/conflict-of-interest',width:50},
			           {boxLabel:'No',inputValue:'n',name: '/conflict-of-interest',width:50}
			           ]
			}),
			new Ext.form.TextField({	
			    fieldLabel: 'Please describe the conflict',
			    name: '/conflict-of-interest-description'
			}),
			new Ext.form.Label({	
			    html:'<strong>Important Note: </strong>This question does <strong>NOT</strong> replace your responsibility to keep the information in <a href="#">ClickCommerce COI</a> current.'
			})
	       ]
});


var editRoleGroup = new Ext.form.FormPanel({
		id:'edit-role-group-panel',
		region:'north',
    	title:'Roles',
    	height:100,
    	layout:'form',
    	collapsed:false,
    	collapsible:false,
    	items:[
				new Ext.form.CheckboxGroup({
					   id:'edit-role-group-cbgroup',
					   hideLabel:true,
					   xtype:'checkboxgroup',
					   name:'rolelist',
					   columns:2,
					   items:[
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Principal Investigator', inputValue:'Principal Investigator', name:'/roles/role'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Co-Investigator', inputValue:'Co-Investigator', name:'/roles/role'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Sub-Investigator', inputValue:'Sub-Investigator', name:'/roles/role'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Study Coordinator', inputValue:'Study Coordinator', name:'/roles/role'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Support Staff', inputValue:'Support Staff', name:'/roles/role'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Mentor', inputValue:'Mentor', name:'/roles/role'})
    	    	        ]})
    	       
    	]       
});


	
var editRespGroup = new Ext.form.FormPanel({
		id:'edit-resp-group-panel',
		region:'center',
    	title:'Responsibilities',
    	height:210,
    	layout:'form',
    	collapsed:false,
    	collapsible:false,
    	items:[
    	       new Ext.form.CheckboxGroup({
    	    	   id:'edit-resp-group-cbgroup',
    	    	   hideLabel:true,
    	    	   xtype:'checkboxgroup',
    	    	   name:'resplist',
    	    	   columns:2,
    	    	   items:[
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Subject Recruitment', inputValue:'Subject Recruitment', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Perform Disease Assessments', inputValue:'Perform Disease Assessments', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Regulatory Documentation', inputValue:'Regulatory Documentation', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Dispense Investigational Product', inputValue:'Dispense Investigational Product', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Study Administration and Support', inputValue:'Study Administration and Support', name:'/responsibilities/responsibility'}),
    	    			new Ext.form.Checkbox({hideLabel:true, boxLabel:'Collect Blood / Lab Samples', inputValue:'Collect Blood / Lab Samples', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Obtaining Informed Consent', inputValue:'Obtaining Informed Consent', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Assess Inclusion / Exclusion', inputValue:'Assess Inclusion / Exclusion', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Physical Exam', inputValue:'Physical Exam', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'CRF Completion', inputValue:'CRF Completion', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'CRF / Query Sign-off', inputValue:'CRF / Query Sign-off', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'SAE Sign-off', inputValue:'SAE Sign-off', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Randomization', inputValue:'Randomization', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Lead Subject Interview / Focus Groups', inputValue:'Lead Subject Interview / Focus Groups', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Initiate / Perform Follow-up Contacts', inputValue:'Initiate / Perform Follow-up Contacts', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Perform Non-Medical Study Interventions', inputValue:'Perform Non-Medical Study Interventions', name:'/responsibilities/responsibility'})
    	    	   ]
    	       })
    	]       
    });





var roleGroup = new Ext.form.FormPanel({
		id:'role-group-panel',
		region:'north',
    	title:'Roles',
    	//autoHeight:true,
    	height:100,
    	layout:'form',
    	collapsed:false,
    	collapsible:false,
    	items:[
				new Ext.form.CheckboxGroup({
					   id:'role-group-cbgroup',
					   hideLabel:true,
					   xtype:'checkboxgroup',
					   name:'rolelist',
					   columns:2,
					   items:[
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Principal Investigator', inputValue:'Principal Investigator', name:'/roles/role'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Co-Investigator', inputValue:'Co-Investigator', name:'/roles/role'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Sub-Investigator', inputValue:'Sub-Investigator', name:'/roles/role'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Study Coordinator', inputValue:'Study Coordinator', name:'/roles/role'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Support Staff', inputValue:'Support Staff', name:'/roles/role'}),
		    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Mentor', inputValue:'Mentor', name:'/roles/role'})
    	    	        ]})
    	       
    	]       
});


	
var respGroup = new Ext.form.FormPanel({
		id:'resp-group-panel',
		region:'center',
    	title:'Responsibilities',
    	//autoHeight:true,
    	height:210,
    	layout:'form',
    	collapsed:false,
    	collapsible:false,
    	items:[
    	       new Ext.form.CheckboxGroup({
    	    	   id:'resp-group-cbgroup',
    	    	   hideLabel:true,
    	    	   xtype:'checkboxgroup',
    	    	   name:'resplist',
    	    	   columns:2,
    	    	   items:[
      	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Subject Recruitment', inputValue:'Subject Recruitment', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Perform Disease Assessments', inputValue:'Perform Disease Assessments', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Regulatory Documentation', inputValue:'Regulatory Documentation', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Dispense Investigational Product', inputValue:'Dispense Investigational Product', name:'/responsibilities/responsibility'}),
    	    	        new Ext.form.Checkbox({hideLabel:true, boxLabel:'Study Administration and Support', inputValue:'Study Administration and Support', name:'/responsibilities/responsibility'}),
    	    			new Ext.form.Checkbox({hideLabel:true, boxLabel:'Collect Blood / Lab Samples', inputValue:'Collect Blood / Lab Samples', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Obtaining Informed Consent', inputValue:'Obtaining Informed Consent', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Assess Inclusion / Exclusion', inputValue:'Assess Inclusion / Exclusion', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Physical Exam', inputValue:'Physical Exam', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'CRF Completion', inputValue:'CRF Completion', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'CRF / Query Sign-off', inputValue:'CRF / Query Sign-off', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'SAE Sign-off', inputValue:'SAE Sign-off', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Randomization', inputValue:'Randomization', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Lead Subject Interview / Focus Groups', inputValue:'Lead Subject Interview / Focus Groups', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Initiate / Perform Follow-up Contacts', inputValue:'Initiate / Perform Follow-up Contacts', name:'/responsibilities/responsibility'}),
    	    	    	new Ext.form.Checkbox({hideLabel:true, boxLabel:'Perform Non-Medical Study Interventions', inputValue:'Perform Non-Medical Study Interventions', name:'/responsibilities/responsibility'})
    	    	   ]
    	       })
    	]       
    });


var staffDetailsPanel= new Ext.Panel({
    layout:'border',
    region:'center',
    height:440,
    border:false,
    flex:1,
    collapsible: false,
    items: [roleGroup,respGroup,staffConflictsPanel]
});

var editStaffDetailsPanel= new Ext.Panel({
    layout:'border',
    region:'center',
    height:440,
    border:false,
    flex:1,
    collapsible: false,
    items: [editRoleGroup,editRespGroup,editStaffConflictsPanel]
});