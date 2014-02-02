Ext.define('Clara.Reports.ux.ComboCriteriaField', {
    requires:'Ext.ux.ComboFieldBox',
	extend: 'Ext.ux.ComboFieldBox',
	alias: 'widget.clarafield.combo.criteria',
	store: 'ComboCriterias',
    viewCfg:{
        maxLength:36
    },
	displayField: 'name',

    forceSelection:true,
	valueField:'value',
	typeAhead:false,
	fieldLabel:'',
	comboId:'',
	hideTrigger:false,
    _preventClear:true,

    getComboValue: function(){
        var valueArray = [];
        var recs = this.getValue();
        for (var i=0,l=recs.length;i<l;i++){
            valueArray.push(recs[i].get("value"));
        }

        return valueArray.join('|');
    },

    getComboRawValue: function(){
        var valueArray = [];
        var recs = this.getValue();
        for (var i=0,l=recs.length;i<l;i++){
            valueArray.push(recs[i].get("name"));
        }
        return valueArray.join(', ');
    },

	initComponent: function() {

		clog("Clara.Reports.ux.ComboCriteriaField: INIT",this.comboId);
		this.listeners = {
			render: function(cb){
				if (cb.comboFilter != ""){
					cb.getStore().clearFilter();
					clog("filtering");
					cb.getStore().filter("comboId",cb.comboId);
				}else{
					cwarn("Clara.Reports.ux.ComboCriteriaField,",cb.getId(),"comboFilter is empty, cannot create.");
				}
			}
		};
		
		this.callParent();
	}
});