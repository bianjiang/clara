Ext.define('Clara.Common.store.StaffMembers', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Common.model.StaffMember',    
    model: 'Clara.Common.model.StaffMember',
    autoLoad: false
    // May need to define proxy here..
});