Ext.define('Clara.Common.ux.ClaraDateField', {
	extend: 'Ext.form.FieldContainer',//'Ext.form.field.Date',
	alias: 'widget.clarafield.date',
	format:'m/d/Y',
	isDateRange: false,
	name:'claradatecontainerfield',
	validate: function(){
		var dt1 = Ext.getCmp(this.id+"_date1"),
		    dt2 = Ext.getCmp(this.id+"_date2");
		if (this.isDateRange){
			return dt1.validate() && dt2.validate();
		}else{
			return dt1.validate();
		}
	},
	getName: function(){
		return this.name;
	},
	getValue: function(){
		var dt1 = Ext.getCmp(this.id+"_date1"),
	    dt2 = Ext.getCmp(this.id+"_date2");
		if (this.isDateRange){
			return dt1.getValue()+","+dt2.getValue();
		}else{
			return dt1.getValue();
		}
	},
	getRawValue: function(){
		var dt1 = Ext.getCmp(this.id+"_date1"),
	    dt2 = Ext.getCmp(this.id+"_date2");
		if (this.isDateRange){
			return dt1.getRawValue()+" - "+dt2.getRawValue();
		}else{
			return dt1.getRawValue();
		}
	},

	initComponent: function() {
		var me = this;
		clog("Clara.Common.ux.ClaraDateField: INIT");

		me.items = [{
			xtype:'datefield',
			format:'m/d/Y',
			fieldLabel:'Date',	// change to "From" if "RANGE"
			parentContainer:me,
			name:me.name+"_date1",
			id:me.id+"_date1",
			rawToValue: function(rawInput){
				return rawInput;
			},
			listeners: {
				change: function(dt,newValue){
					var dt2 = Ext.getCmp(me.id+"_date2"),
					    newDateValue = new Date(newValue),
					    secondDateValue = new Date(dt2.getValue());
					
					
					dt2.setMinValue(newDateValue);
					if (newDateValue.getTime() > secondDateValue.getTime()){
						dt2.setValue(newDateValue);
						dt2.initValue(newDateValue);
					}
				}
			}
		},
		{
			xtype:'datefield',
			format:'m/d/Y',
			fieldLabel:'To',
			parentContainer:me,
			hidden:true,
			name:me.name+"_date2",
			id:me.id+"_date2",
			rawToValue: function(rawInput){
				return rawInput;
			}
		}
		];
		
		me.listeners = {
			criteriaOperatorChanged: function(operatorValue){
				var dt1 = Ext.getCmp(me.id+"_date1"),
					dt2 = Ext.getCmp(me.id+"_date2");
				clog("Clara.Common.ux.ClaraDateField: operatorValue changed to "+operatorValue);
				if (operatorValue == "BETWEEN"){
					me.isDateRange = true;
					dt1.setLabel("From");
					dt2.setVisible(true);
				} else {
					me.isDateRange = false;
					dt1.setLabel("Date");
					dt2.setVisible(false);
				}
			}
		};
		
		this.callParent();
	}
});