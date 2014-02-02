Ext.define('Clara.Meeting.store.MeetingStore', {
    extend: 'Ext.data.Store',
    requires: ['Clara.Meeting.model.Vote','Clara.Meeting.model.Meeting','Clara.Meeting.model.MemberStatus','Clara.Meeting.model.ActivityItem','Clara.Meeting.model.Motion','Clara.Meeting.model.AttendantActivityItem'],    
    model: 'Clara.Meeting.model.Meeting',
    autoLoad: false,
    
    // UI Store functions
    getActivityItemStore: function(){
    	var meetingStore = this;
    	var st = Ext.create('Ext.data.Store',{
    		model:'Clara.Meeting.model.ActivityItem'
    	});
    	// Can't use inline data, so load up the records
    	var aItems = meetingStore.getAt(0).activityItems();
    	aItems.each(function(rec){
    		st.add(rec);
    	});
    	return st;
    }
});