Ext.define('Clara.Queue.store.QueueItems', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Queue.model.QueueItem',    
    model: 'Clara.Queue.model.QueueItem',
    autoLoad: false,
    sorters:[{
		property: 'formType',
		direction:'ASC'
	},{
		property: 'formCommitteeStatus',
		direction:'ASC'
	},{
		property: 'formCommitteeStatusModified',
		direction:'DESC'
	}]
});