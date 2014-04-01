Ext.define('Clara.Reports.store.ReportRecipients', {
    extend: 'Ext.data.Store',
    requires: 'Clara.LetterBuilder.model.Recipient',    
    model: 'Clara.LetterBuilder.model.Recipient',
    autoLoad:false
    
});