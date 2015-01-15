Ext.define('Clara.Admin.store.PiwikUserVisits', {
    extend: 'Ext.data.Store',
    requires: 'Clara.Admin.model.PiwikUserVisit',    
    model: 'Clara.Admin.model.PiwikUserVisit',
    autoLoad: false,
    loadPiwikIPAddressVisits: function(ipAddress, startDate, endDate){
    	var t = this;
    	// if no date range, default to past 30 days
    	var today = new Date();
    	startDate = startDate || new Date(today.getTime() - 30*24*60*60*1000);
    	endDate   = endDate   || new Date(today.getTime() + 1*24*60*60*1000);
    	var rangeString = startDate.toISOString().slice(0,10)+","+endDate.toISOString().slice(0,10); // YYYY-MM-DD
    	t.getProxy().url = "https://clarafs.uams.edu/piwik/?module=API&method=Live.getLastVisitsDetails&idSite=1&period=range&date="+rangeString+"&format=json&token_auth=a67928b08a91a94c32a66aa95843c5c7&segment=visitIp=="+ipAddress;
    	t.load();
    },
    loadPiwikUserVisits: function(username, startDate, endDate){
    	var t = this;
    	// if no date range, default to past 30 days
    	var today = new Date();
    	startDate = startDate || new Date(today.getTime() - 30*24*60*60*1000);
    	endDate   = endDate   || new Date(today.getTime() + 1*24*60*60*1000);
    	var rangeString = startDate.toISOString().slice(0,10)+","+endDate.toISOString().slice(0,10); // YYYY-MM-DD
    	t.getProxy().url = "https://clarafs.uams.edu/piwik/?module=API&method=Live.getLastVisitsDetails&idSite=1&period=range&date="+rangeString+"&format=json&token_auth=a67928b08a91a94c32a66aa95843c5c7&segment=customVariableValue1=@"+username;
    	t.load();
    }
});