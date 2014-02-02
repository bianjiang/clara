Ext.define('Clara.Common.ux.ClaraUserField', {
	extend: 'Ext.form.field.ComboBox',
	 alias: 'widget.clarafield.combo.user',

	 store: 'Clara.Common.store.Users',
	 displayField: 'username',
	 valueField:'userid',
	 typeAhead:false,
	 hideLabel:true,
	 hideTrigger:true,
	 anchor:'100%',
	 queryParam:'keyword',
	 minChars:3,
	 listConfig:{
		 loadingText: 'Searching..',
		 emptyText: 'No matching CLARA users found.',
		 getInnerTpl: function() {
             return '<h3>{firstname} {lastname}</h3>{email}';
         }
	 }
});