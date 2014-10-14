Ext.Loader.setConfig({
	enabled: true,
	// Don't set to true, it's easier to use the debugger option to disable caching
	disableCaching: true,
	paths: {
		'Clara.Reports': appContext + '/static/js/app/reports',
		'Clara.Login': appContext + '/static/js/app/login',
		'Clara.Meeting': appContext + '/static/js/app/meeting',
		'Clara.Queue': appContext + '/static/js/app/queue',
		'Clara.Admin': appContext + '/static/js/app/admin',
		'Clara.Agenda': appContext + '/static/js/app/agenda',
		'Clara.User': appContext + '/static/js/app/user',
		'Clara.Common': appContext + '/static/js/app/common',
		'Clara.Super': appContext + '/static/js/app/super'
		}
	}
);

Ext.Loader.setPath('Clara.Common', appContext+'/static/js/app/common');
Ext.Loader.setPath('Ext.ux', appContext+'/static/js/ext4/ux');
Ext.Loader.setPath('Ext.ux.ComboFieldBox', appContext+'/static/js/ext4/ux/ComboFieldBox/ComboFieldBox.js');
Ext.Loader.setPath('Ext.ux.ComboView', appContext+'/static/js/ext4/ux/ComboFieldBox/ComboView.js');

function LoadJs(url){
	  var js = document.createElement('script');
	  js.type = "text/javascript";
	  js.src = url;
	  document.body.appendChild(js);
}

function Clara_HumanReadableType(str,separator){
	var ra = str.split(separator);
	return ra.join(" ").toLowerCase().toTitleCase();
}

function Clara_HumanReadableRoleName(role){
	var ra = role.split("_");
	ra.shift();
	return ra.join(" ").toLowerCase().toTitleCase();
}


Ext.Ajax.on("requestcomplete",function(conn,r,opt){
	try{
		var data = Ext.decode(r.responseText);
		if (typeof data.error != 'undefined' && data.error){
			clog("AJAX ERROR",data.error,data.message,data);
			Ext.Msg.alert('CLARA', data.message, function(){
				if (data.shouldRedirect){
					location.href = data.redirect;
				}
			});
		} else {
			if (typeof claraInstance != "undefined") claraInstance.session.reset();
		}
	} catch (e){
		if (typeof claraInstance != "undefined") claraInstance.session.reset();
		clog("AJAX requestcomplete (non-JSON)");
	}
});

Ext.Ajax.on("requestexception",function(conn,r,opt){
	cwarn("AJAX requestexception: "+r.statusText,conn,r,opt);
	Ext.getBody().unmask();
});
