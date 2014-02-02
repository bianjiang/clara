Ext.define('Clara.Dashboard.store.Bookmarks', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Dashboard.model.Bookmark',    
    model: 'Clara.Dashboard.model.Bookmark',
    autoLoad: false
});