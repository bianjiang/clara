function switchToEditView(url){
	url += "?noheader=true&committee="+((claraInstance.user.committee)?claraInstance.user.committee:'PI');
	clog(url,claraInstance);
	
	// if (typeof window.parent.Clara.Reviewer != "undefined") window.parent.Clara.Reviewer.MessageBus.fireEvent("editingform",url);
	window.top.location.href = url;
	//location.href=url;
}

function exitEditView(url){
	window.location.reload();
}