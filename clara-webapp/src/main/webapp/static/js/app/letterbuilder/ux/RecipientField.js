Ext.define('Clara.LetterBuilder.ux.RecipientField', {
    requires:'Ext.ux.form.field.BoxSelect',
	extend: 'Ext.ux.form.field.BoxSelect',
	alias: 'widget.recipientcombofield',
	store: 'Clara.LetterBuilder.store.Recipients',

	displayField: 'desc',
	pinList: false,
	grow:false,
	triggerOnClick:false,
    forceSelection:true,
	valueField:'address',
	typeAhead:true,
	fieldLabel:'',
	hideTrigger:false,


    getComboValue: function(){
        var valueArray = [];
        var recs = this.getValue();
        for (var i=0,l=recs.length;i<l;i++){
            valueArray.push(recs[i].get("address"));
        }

        return valueArray.join(',');
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

		clog("Clara.LetterBuilder.ux.RecipientField: INIT");
		this.callParent();
	}
});