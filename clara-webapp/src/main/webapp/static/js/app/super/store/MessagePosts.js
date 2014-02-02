Ext.define('Clara.Super.store.MessagePosts', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Super.model.MessagePost',    
    model: 'Clara.Super.model.MessagePost',
    autoLoad: true
});