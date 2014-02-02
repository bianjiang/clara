Ext.define('Clara.Common.store.StudySites', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.StudySite',    
    model: 'Clara.Common.model.StudySite',
    autoLoad: false
});