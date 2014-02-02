Ext.define('Clara.Common.ux.ClaraFundingSourceField', {
	extend: 'Ext.form.field.ComboBox',
	 alias: 'widget.clarafield.combo.fundingsource',

	 store: 'Clara.Common.store.FundingSources',
	 displayField: 'name',
	 valueField:'name',  // 'id' may not work if we're using this field for BOTH freetext and selectable funding sources
	 typeAhead:false,
	 hideLabel:true,
	 hideTrigger:false,
	 anchor:'100%',
	 queryParam:'query',
	 minChars:3,
	 listConfig:{
		 loadingText: 'Searching..',
		 getInnerTpl: function() {
             return '<h3>{name}</h3>';
         }
	 }
});