Ext.ns('Clara.ProtocolForm');

Clara.ProtocolForm.RepsonibleDepartment = {
		collegeid: 0,
		collegename:'',
		deptid: 0,
		deptname:'',
		subdeptid: 0,
		subdeptname:''
};

Clara.ProtocolForm.ResponsibleDepartmentWindow = Ext.extend(Ext.Window, {
	id:'winRespDept',
    title: 'Choose a department',
    width: 496,
    height: 156,
    layout: 'form',
    padding: 6,
    collegestore:new Ext.data.Store({
		header :{
           'Accept': 'application/json'
       },
		proxy: new Ext.data.HttpProxy({
			url: appContext + '/ajax/colleges/list',
			method:'GET'
		}),
		autoLoad:false,
		sortInfo: {
		    field: 'name',
		    direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
		},
		reader: new Ext.data.JsonReader({
				idProperty: 'id'
				}, [{name:'id'},
				    {name:'sapCode'},
				    {name:'name'}])
       }),
	selectCollege: function(id){
		var c = Ext.getCmp('fldCollege');
		var d = Ext.getCmp('fldDept');
		var s = Ext.getCmp('fldSubDept');
		d.clearValue();
		s.clearValue();
		if(s.editable) s.setReadOnly('true');
		jQuery('#fldSubDept').addClass('x-item-disabled');
		d.getStore().proxy.setUrl(appContext + '/ajax/colleges/'+id+'/departments/list');
		d.getStore().load();
		d.setReadOnly(false);
		jQuery('#fldDept').removeClass('x-item-disabled');
	},
	selectDepartment: function(id){
		var s = Ext.getCmp('fldSubDept');
		s.clearValue();
		s.getStore().proxy.setUrl(appContext + '/ajax/colleges/'+Clara.ProtocolForm.RepsonibleDepartment.collegeid+'/departments/'+id+'/sub-departments/list');
		s.getStore().load();
		s.setReadOnly(false);
		jQuery('#fldSubDept').removeClass('x-item-disabled');
	},
    toXML: function(){
		var xml = "<responsible-department ";
		if (Clara.ProtocolForm.RepsonibleDepartment.collegeid > 0) {
			xml+="collegeid=\""+Clara.ProtocolForm.RepsonibleDepartment.collegeid+"\" collegedesc=\""+Encoder.htmlEncode(Clara.ProtocolForm.RepsonibleDepartment.collegename)+"\" ";
			if (Clara.ProtocolForm.RepsonibleDepartment.deptid > 0) {
				xml+="deptid=\""+Clara.ProtocolForm.RepsonibleDepartment.deptid+"\" deptdesc=\""+Encoder.htmlEncode(Clara.ProtocolForm.RepsonibleDepartment.deptname)+"\" ";
				if (Clara.ProtocolForm.RepsonibleDepartment.subdeptid > 0) {
					xml+="subdeptid=\""+Clara.ProtocolForm.RepsonibleDepartment.subdeptid+"\" subdeptdesc=\""+Encoder.htmlEncode(Clara.ProtocolForm.RepsonibleDepartment.subdeptname)+"\" ";
				}
			}
		}
		return xml + "/>";
	},
    initComponent: function() {
		var t = this;
		t.buttons = [{
			text:'Save',
			handler:function(){
				updateProtocolXml(t.toXML(), true);
				Ext.getCmp("protocolform-respdepartment-panel").load('/protocol/responsible-department');
				t.close();
			}
		}];
        t.items = [
            {
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
			                mode:'remote', 
				        	triggerAction:'all',
				        	listeners:{
			            		'select': function(cmb,rec,idx){
			            			Clara.ProtocolForm.RepsonibleDepartment.collegeid = rec.data.id;
			            			Clara.ProtocolForm.RepsonibleDepartment.collegename = rec.data.name;
			            			Clara.ProtocolForm.RepsonibleDepartment.deptid = 0;
			            			Clara.ProtocolForm.RepsonibleDepartment.deptname = "";
			            			Clara.ProtocolForm.RepsonibleDepartment.subdeptid = 0;
			            			Clara.ProtocolForm.RepsonibleDepartment.subdeptname = "";
			            			t.selectCollege(Clara.ProtocolForm.RepsonibleDepartment.collegeid);
			            			clog(t.toXML());
			            		}
			            	}
			            },
			            {
			                xtype: 'combo',
			                scope:this,
			                width:350,
			                id: 'fldDept',
			                fieldLabel:'Department',
			                typeAhead:false,
			                forceSelection:true,
			                displayField:'name', 
			                valueField:'id',
			                editable:false,
			                mode:'local', 
				        	triggerAction:'all',
			                store: new Ext.data.Store({
				        			header :{
				     	           'Accept': 'application/json'
				     	       },
				     			proxy: new Ext.data.HttpProxy({
				     				url: appContext + '/ajax/colleges/#/departments/list',
				     				method:'GET'
				     			}),
				     			autoLoad:false,
				     			reader: new Ext.data.JsonReader({
				     					idProperty: 'id'
				     					}, [{name:'id'},
				     					    {name:'collegeId'},
				     					    {name:'sapCode'},
				     					    {name:'name'}])
				     	    }),
				     	    readOnly: true,
				     	    fieldClass: "x-item-disabled",
				        	listeners:{
			            		'select': function(cmb,rec,idx){

	            				Clara.ProtocolForm.RepsonibleDepartment.deptid = rec.data.id;
	            				Clara.ProtocolForm.RepsonibleDepartment.deptname = rec.data.name;

			            			t.selectDepartment(rec.data.id);
			            			
			            			clog(t.toXML());
			            		}
			            	}
			            },
			            {
			                xtype: 'combo',

			                fieldLabel:'Subdepartment',
			                typeAhead:false,
			                width:350,
			                forceSelection:true,
			                displayField:'name', 
			                valueField:'id',
			                editable:false,
				        	//allowBlank:false,
			                mode:'local', 
				        	triggerAction:'all',
			                store: new Ext.data.Store({
				        			header :{
				     	           'Accept': 'application/json'
				     	       },
				     			proxy: new Ext.data.HttpProxy({
				     				url: appContext + '/ajax/colleges/#/departments/#/list',
				     				method:'GET'
				     			}),
				     			autoLoad:false,
				     			reader: new Ext.data.JsonReader({
				     					idProperty: 'id'
				     					}, [{name:'id'},
				     					    {name:'departmentId'},
				     					    {name:'sapCode'},
				     					    {name:'name'}])
				     	    }),
				     	    readOnly: true,
				     	    fieldClass: "x-item-disabled",
			                id: 'fldSubDept',
				        	listeners:{
			            		'select': function(cmb,rec,idx){

	            			Clara.ProtocolForm.RepsonibleDepartment.subdeptid = rec.data.id;
	            			Clara.ProtocolForm.RepsonibleDepartment.subdeptname = rec.data.name;
	            			
	            			clog(t.toXML());
			            		}
			            	}
			            }
        ];
		Clara.ProtocolForm.ResponsibleDepartmentWindow.superclass.initComponent.apply(this, arguments);
    }
});

Clara.ProtocolForm.ResponsibleDepartmentPanel= Ext.extend(Ext.Panel, {
	id: 'protocolform-respdepartment-panel',
    width: 800,
    height: 300,
    layout:'auto',
    border:false,
    selectedCollege:{},
	selectedDept:{},
	selectedSubDept:{},

    load: function( xmlpath){
		var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/list";
		var t = this;
		jQuery.ajax({
			type: 'GET',
			url: url,
			async:false,
			data: {listPath: xmlpath},
			success: function(data){
				clog("data:");
				clog(data);
				t.fromXML(data);
				var deptdesc = "No information entered.";
				
				Ext.getCmp("btnChooseDepartment").setDisabled(Clara.ProtocolForm.RepsonibleDepartment.collegeid == -1);
				Ext.getCmp("cbChooseDepartmentNA").setValue(Clara.ProtocolForm.RepsonibleDepartment.collegeid == -1);
				
					if (Clara.ProtocolForm.RepsonibleDepartment.collegeid > 0){
						deptdesc = "College: <span class='resp-collegedesc'>"+Clara.ProtocolForm.RepsonibleDepartment.collegename+"</span>";
					}
					if (Clara.ProtocolForm.RepsonibleDepartment.deptid > 0){
						deptdesc += "<br/>Department: <span class='resp-deptdesc'>"+Clara.ProtocolForm.RepsonibleDepartment.deptname+"</span>";
					}	
					if (Clara.ProtocolForm.RepsonibleDepartment.subdeptid > 0){
						deptdesc += "<br/>Subdepartment: <span class='resp-subdeptdesc'>"+Clara.ProtocolForm.RepsonibleDepartment.subdeptname+"</span>";
					}
				
				clog("Should be: "+deptdesc);
				jQuery("#respdeptdesc").html(deptdesc);
				jQuery("#fldDeptHidden").val(deptdesc);
			},
			dataType: 'xml'
		});
	},
    fromXML: function(xml){
		var t = this;

		jQuery(xml).find("responsible-department").each(function(){
			
			Clara.ProtocolForm.RepsonibleDepartment.collegeid = parseFloat(jQuery(this).attr('collegeid'));
			Clara.ProtocolForm.RepsonibleDepartment.collegename = (jQuery(this).attr('collegedesc'));
			if (parseFloat(jQuery(this).attr('deptid'))) Clara.ProtocolForm.RepsonibleDepartment.deptid = parseFloat(jQuery(this).attr('deptid'));
			Clara.ProtocolForm.RepsonibleDepartment.deptname = (jQuery(this).attr('deptdesc'));
			if (parseFloat(jQuery(this).attr('subdeptid'))) Clara.ProtocolForm.RepsonibleDepartment.subdeptid = parseFloat(jQuery(this).attr('subdeptid'));
			Clara.ProtocolForm.RepsonibleDepartment.subdeptname = (jQuery(this).attr('subdeptdesc'));
			clog(Clara.ProtocolForm.RepsonibleDepartment);
		});
		
		
	},


	initComponent: function() {
		var t = this;
		var config = {
				border:false,
				items:[
				       {	xtype:'panel',
				    	    border:false,
				    	    html:'<div id="respdeptdesc">Reading department information, please wait..</div><input style="display:none;" class="question-el required" id="fldDeptHidden" value=""/>',
				    	    fbar:[{text:'Choose department...', id:'btnChooseDepartment',
						    	   handler:function(){
						    	   		new Clara.ProtocolForm.ResponsibleDepartmentWindow({modal:true}).show();
						       	   }
						       }],
				    	    listeners:{
				    	   		afterrender:function(p){
				    	   			clog("Afterrender called.");	
				    	   			t.load('/protocol/responsible-department');
				       			}
				       		}
				       },{

			    	    	xtype:'checkbox',
			    	    	id:'cbChooseDepartmentNA',
			    	    	boxLabel:'Not applicable / I don\'t know my college or deparment',
			    	    	listeners: {
			    	    		check: function(cb,v){
			    	    			Ext.getCmp("btnChooseDepartment").setDisabled(v);
			    	    			
			    	    			if (v){
			    	    				updateProtocolXml('<responsible-department collegeid="-1" collegedesc="Not applicable" />', true);
			    	    				Ext.getCmp("protocolform-respdepartment-panel").load('/protocol/responsible-department');
			    	    			} 
			    	    			
			    	    		}
			    	    	}
			    	    	
			    	    
				       }]
					
				};
				Ext.apply(this, Ext.apply(this.initialConfig, config));
				Clara.ProtocolForm.ResponsibleDepartmentPanel.superclass.initComponent.apply(this, arguments);

			}});
Ext.reg('claraprotocolformrespdepartmentpanel', Clara.ProtocolForm.ResponsibleDepartmentPanel);