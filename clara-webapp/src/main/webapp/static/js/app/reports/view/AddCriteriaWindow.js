Ext.define('Clara.Reports.view.AddCriteriaWindow', {
    extend: 'Ext.window.Window',
    requires: ['Clara.Common.ux.ClaraCollegeField','Clara.Common.ux.ClaraDateField','Clara.Common.ux.ClaraUserField','Clara.Reports.ux.ComboCriteriaField'],
    alias: 'widget.addcriteriawindow',
    width:500,
    parentWindow:{},
    iconCls:'icn-gear',
    layout: {
        type: 'form'
    },
    bodyPadding: 5,
    
    style : 'z-index: -1;', // IE8 fix (http://www.sencha.com/forum/archive/index.php/t-241500.html?s=15ad65f757fb7325aa20735e3226faab)
    
    defaultType:'combobox',
    report:null,
    initComponent: function() {
    	var t = this;
    	t.save = function(){
    		var win = this;
    		var fieldItems = Ext.getCmp("fldCriteriaContainer").items.items;
    		clog("fieldItems[0]",fieldItems[0]);
            var fieldCmp = Ext.getCmp(fieldItems[0].getName());
    		var	data = {
    			xtype: fieldItems[0].getXType(),
    			fieldname: fieldItems[0].getName(),
    			fieldlabel: fieldItems[0].getFieldLabel(),
    			operator: Ext.getCmp("cbNewCriteriaOperator").getValue(),
    			fieldvalue: (typeof(fieldCmp.getComboValue) == "function")?fieldCmp.getComboValue():fieldCmp.getValue(),
    			displayvalue: (typeof(fieldCmp.getComboRawValue) == "function")?fieldCmp.getComboRawValue():fieldCmp.getRawValue()
    		};
    		
        	jQuery.ajax({
    			  type: 'POST',
    			  async:false,
    			  url: appContext+'/ajax/reports/'+win.report.id+'/add-criteria',
    			  data: data,
    			  success: function(data){
      		        if (!data.error){
      		        	var userReportCriteriasStore = Ext.StoreMgr.get('UserReportCriterias');
      		        	userReportCriteriasStore.removeAll();
      		        	userReportCriteriasStore.load();
              			win.close();
      		        }
    			  },
    			  error: function(){
    				  cwarn("Error adding criteria");
    			  }
    		});
        };
    	t.items = [{
        	xtype:'combobox',
        	fieldLabel:'Field:',
        	allowBlank:false,
        	store:'ReportCriterias',
        	displayField:'fieldlabel',
        	valueField:'id',
        	listeners: {
        		expand: function(cb){
        			clog("EXPAND field. Store:",cb.getStore());
        		},
        		select: function(cb,rec){
        			var op = Ext.getCmp("cbNewCriteriaOperator");
        			var opArray = [],
        			    allowedOpArray = rec[0].get("operators").split("|");
        			
        			for (var i=0, l=allowedOpArray.length;i<l;i++){
        				opArray.push({"operator": allowedOpArray[i]});
        			}
        			op.setVisible(true);
        			op.getStore().loadData(opArray);
        			op.select(op.store.data.items[0]);

        			// add dynamic criteria field.
        			var ct = Ext.getCmp("fldCriteriaContainer");
        			ct.removeAll();

        			var isFieldDefined = false;
        			
        			try{
        				// try making a field. if we can, then its defined (PLEASE SENCHA there has to be a better way, isDefined and isRegistered doesnt work!)
        				var dummy = Ext.createByAlias("widget."+rec[0].get("xtype"),{name:"dummy"});

                        if (dummy.cls == "x-tree-panel x-tree-lines x-grid") {
                            throw "Production tried to make a tree.. WHY?";
                        }
	        			else if (Ext.isDefined(isFieldDefined)){
	        				clog("Found defined ",rec[0].get("xtype"),dummy);
	        				// we have this custom xtype already registered, so just add it
	        				ct.add({
	            				xtype:rec[0].get("xtype"),
	            				name:rec[0].get("fieldname"),
	            				id:rec[0].get("fieldname"),
	            				fieldLabel:rec[0].get("title"),
	            				allowBlank:false,
	            				style:'width:100%;'
	            			});
	        			}
	        			isFieldDefined = null;
	        			
        			} catch(e) {
        				cwarn("xtype '"+rec[0].get("xtype")+"' UNDEFINED, assuming 'clarafield.combo.criteria' type.");
        				cwarn(e);
        				if (rec[0].get("xtype").indexOf("clarafield.combo.") == 0){
            				// This is a "generic" name/value combobox, filtering on the "xtype" comboId passed here. 
            				// Actual xtype is "clarafield.combo.criteria"
            				ct.add({
                				xtype:"clarafield.combo.criteria",
                				comboId:rec[0].get("xtype"),
                                multiSelect:true,
                				name:rec[0].get("fieldname"),
                				id:rec[0].get("fieldname"),
                				fieldLabel:rec[0].get("title"),
                				allowBlank:false,
                				style:'width:100%;'
                			});
            			}
        				else cwarn("CANNOT Determine field type");
        			}
	        			    			
        			ct.setVisible(true);
        		}
        	}
        },
    	{
    		xtype:'combobox',
    		fieldLabel:'Operator',
    		id:'cbNewCriteriaOperator',
    		allowBlank:false,
    		queryMode: 'local',
    		displayField:'operator',
        	valueField:'operator',
    		store:new Ext.data.ArrayStore({
    			  autoDestroy:true,
    	    	  fields:['operator']
    	    }),
    	    hidden:true,
    	    listeners: {
        		expand: function(cb){
        			clog("expanded",cb,cb.getStore())
        		},
        		select: function(cb,recs){
        			// Pass selected operator event down to custom field(s)
        			var fieldItems = Ext.getCmp("fldCriteriaContainer").items.items;
            		clog(fieldItems);
            		for (var i=0;i<fieldItems.length;i++){
            			fieldItems[i].fireEvent("criteriaOperatorChanged", recs[0].get("operator"));
            		}
        		}
        		
        	}
    	},{
    		xtype:'fieldcontainer',
    		items:[],
    		hidden:true,
    		autoDestroy:false,
    		id:'fldCriteriaContainer',
    		flex:1,
    		layout:'fit'
    	}
        
        ];
    	t.buttons = [{text:'Save', handler:function(){
    		var valid = true;
    		var fieldItems = Ext.getCmp("fldCriteriaContainer").items.items;
    		clog(fieldItems);
    		for (var i=0;i<fieldItems.length;i++){
    			valid = valid && fieldItems[i].validate();
    		}
    		
    		if (valid) t.save();
    		else alert("Enter a criteria value.");
    	}}];

        t.callParent();
    }
});

