Ext.define('Clara.Dashboard.model.Bookmark', {
	extend: 'Ext.data.Model',
	fields: ['name','searchCriterias','id', 'userId'],
	proxy: {
		type: 'ajax',
		extraParams: {userId: claraInstance.user.id || 0},
		headers:{'Accept':'application/json;charset=UTF-8'},
		url: appContext+'/ajax/'+claraInstance.type+'s/search-bookmarks/list',
		reader: {
			type: 'json',
			idProperty: 'id',
			root: 'bookmarks'
		}
	}
});