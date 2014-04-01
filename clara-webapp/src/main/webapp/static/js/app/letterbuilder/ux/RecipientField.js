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
    
    getEmailXMLValue: function(){
    	var valueArray = [];
        var recs = this.getValueRecords();
        for (var i=0,l=recs.length;i<l;i++){
        	clog("TRYING EMAIL XML FOR ",recs[i]);
            valueArray.push("<email type=\""+recs[i].get("type")+"\" desc=\""+recs[i].get("desc")+"\">"+recs[i].get("address")+"</email>");
        }
        return valueArray.join("");
    },

	initComponent: function() {

		clog("Clara.LetterBuilder.ux.RecipientField: INIT");
		this.callParent();
	}
});