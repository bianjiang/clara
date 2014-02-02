Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');

Ext.application({
    name: 'Clara.Meeting', 
    appFolder:appContext + '/static/js/app/meeting',

    models:['Clara.Meeting.model.Meeting','Clara.Queue.model.AssignedReviewer','Clara.Queue.model.Reviewer','Clara.Queue.model.QueueItem','Clara.Agenda.model.IrbReviewer','Clara.Agenda.model.Agenda','Clara.Common.model.IrbRoster','Clara.Common.model.User'],
    stores:['Clara.Meeting.store.MeetingStore','Clara.Queue.store.AssignedReviewers','Clara.Queue.store.Reviewers','Clara.Queue.store.QueueItems','Clara.Agenda.store.IrbReviewers','Clara.Agenda.store.AssignedIrbReviewers','Clara.Agenda.store.Agendas','Clara.Common.store.IrbRosters','Clara.Common.store.Users'],
    requires:['Clara.Meeting.class.Meeting'],
    controllers: [],
    
    autoCreateViewport: true,
    enableQuickTips: true,
    launch: function() {
    	Clara.Meeting.app = this;
    	Ext.tip.QuickTipManager.init();
    	clog("Ext.app launch.");
    	
    	LoadJs(appContext+'/static/js/app/common/data/NameMappings.js');
    	LoadJs(appContext+'/static/js/encoder.js');
    	
        var st = Ext.data.StoreManager.lookup('Clara.Meeting.store.MeetingStore');
        st.getProxy().url = appContext + '/ajax/agendas/' + meeting.agendaId + '/load-meeting-xml-data';
        st.load({callback:function(recs){
        	clog("MeetingStore Loaded.",recs[0]);
        	clog("MeetingStore: Activity",recs[0].activityItems());
        	clog("ActivityStore: ",st.getActivityItemStore());
        }});
        
        clog("launch() done.");
    }
});