Ext.define('Clara.LetterBuilder.store.Recipients', {
    extend: 'Ext.data.Store',
    requires: 'Clara.LetterBuilder.model.Recipient',    
    model: 'Clara.LetterBuilder.model.Recipient',
    autoLoad:false
});