Ext.define('Clara.Common.store.FundingSources', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.FundingSource',    
    model: 'Clara.Common.model.FundingSource',
    autoLoad: false
});