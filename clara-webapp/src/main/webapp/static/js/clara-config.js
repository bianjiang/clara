/**
 * 
 * This includes configurations data for contents that often changed, or appears in multiple places
 * 
 */

Ext.ns('Clara','Clara.Config');

var CLARA_AJAX_TIMEOUT = 30000;
var CLARA_SESSION_TIMEOUT = 60*60*1000;	// 60 minutes

Array.prototype.hasValue = function(value) {
	  var i;
	  for (i=0; i<this.length; i++) { if (this[i] === value) return true; }
	  return false;
	};

//Attempt to fix firebug dependencies in JS
if (!window.console||!console.firebug){
	   var methods = [
 "log", "debug", "info", "warn", "error", "assert",
 "dir", "dirxml", "group", "groupEnd", "time", "timeEnd",
 "count", "trace", "profile", "profileEnd"
 ];
	   if (!window.console) {
		   window.console = {};
	   
		  for (var i=0; i<methods.length; i++){
		     window.console[methods[i]] = function(){};
		  }
	   }
	   
	   // Chrome doesnt have 'info' and 'debug' levels..
	   if (window.console["info"] == {}){
		   window.console["info"] = window.console["log"];
	   }

}

var loglevelstring = null;
if (typeof Ext != "undefined" && Ext.util && Ext.util.Cookies){
	loglevelstring = Ext.util.Cookies.get("claraLogLevels");
}
if (!loglevelstring){
	setLogLevel(defaultLogLevels || ["log","info","warn","debug","error"]);
} else {
	var levels = loglevelstring.split(",");
	setLogLevel(levels);
}

jQuery.ajaxSetup({ timeout:CLARA_AJAX_TIMEOUT });
Ext.override(Ext.data.Connection, { 
	timeout:CLARA_AJAX_TIMEOUT,
	listeners: {
		requestexception: function(conn,r,opt){
			var result = conn.parseStatus(r.xhr.status);
			cwarn("Ext.data.Connection requestexception: "+result,conn,r,opt);
		}
	}

});
// Ext.override(Ext.data.proxy.Ajax, { timeout:CLARA_AJAX_TIMEOUT });

//claraInstance is the standard object for storing form/data id's, etc. 
var claraInstance = {
	id:	null,
	identifier:null,
	type: null,
	action:null,
	status:null,
	pageSubType:null,
	title:null,
	
	form: {
		id:null,
		urlName:null,
		type:null,
		xmlDataId:null,
		xmlBaseTag:null,
		editing:false,
		readOnly:false
	},
	
	
	HasAnyPermissions: function(setToCheck, logMsg){
		var me = this;
		var found = false;
		if (isArray(setToCheck) === false) setToCheck = [setToCheck];
		if (me.user.permissions){
			for (var i=0;i<setToCheck.length;i++)
			{		
				for(var j=0; j<me.user.permissions.length; j++) {
					if (setToCheck[i] == me.user.permissions[j]) found = true;
				}
			}
		}
		cdebug("HasAnyPermissions",setToCheck,found,(logMsg || ""));
		return found;
	},
	
	HasAnyPermissionsLike: function(setToCheck, logMsg){
		var me = this;

		if (isArray(setToCheck) === false) setToCheck = [setToCheck];
		if (me.user.permissions){
			for (var i=0;i<setToCheck.length;i++)
			{		
				for(var j=0; j<me.user.permissions.length; j++) {
					if (me.user.permissions[j].indexOf(setToCheck[i]) > -1){
						cdebug("HasAnyPermissionsLike",setToCheck,true,(logMsg || ""));
						return true;
					}
				}
			}
		}
		cdebug("HasAnyPermissionsLike",setToCheck,false,(logMsg || ""));
		return false;
	},
	
	HasAllPermissions: function(setToCheck, logMsg){
		var me = this;
		if (me.user.permissions){
			cdebug("HasAllPermissions: "+(logMsg || ""),setToCheck,"in",me.user.permissions);
			
			for (var i=0;i<setToCheck.length;i++)
			{		
				var hasthisone = false;
				for(var j=0; j<me.user.permissions.length; j++) {
					if (setToCheck[i] == me.user.permissions[j]) hasthisone = true;
				}
				if (!hasthisone) return false;
			}
		}
		return true;
	},
	
	session: {
		expired:false,
		syncTimeoutId:null,
		expireTime: new Date().getTime() +  CLARA_SESSION_TIMEOUT,//2*60*1000,	// expire 2 mintues after that
		syncWithSessionCookie: function(){
			// This function will be called every second, to check warn/end timers set by other tabs
			var currentTimeMillis = new Date().getTime();
			
			    var cookieExpireTimeMillis = parseFloat(readCookie("claraSessionTimeoutMillis")) || 0;
				
			    jQuery(".expire-seconds-desc").html("in "+Math.round((cookieExpireTimeMillis - currentTimeMillis)/1000)+" seconds.");
			    
				if ( Math.round(cookieExpireTimeMillis)/1000 > Math.round(claraInstance.session.expireTime/1000)){

					var oldExpireTime = claraInstance.session.expireTime;
					claraInstance.session.expireTime = cookieExpireTimeMillis;
					
					if ( Math.round(cookieExpireTimeMillis/1000) > ( Math.round(oldExpireTime/1000) + 3) ){
						claraInstance.session.hideWarning();	// only reset if cookie is 3 seconds or more off
					}
				}
				if (claraInstance.session.expireTime - currentTimeMillis <= 60*1000 && !jQuery("#clara-header-notify").is(":visible")){
					claraInstance.session.warn();
				}
				else if (claraInstance.session.expireTime - currentTimeMillis < 1){
					claraInstance.session.expire();
				} else {
					clearTimeout(claraInstance.session.syncTimeoutId);
					claraInstance.session.syncTimeoutId = setTimeout(claraInstance.session.syncWithSessionCookie, 1000);	// every second, check cookie for new timeout value 
				}
				
			
			
			
		},
		start: function(){
			clog("Starting session timer");
			
			
				var expireTimeMillis = new Date().getTime() + CLARA_SESSION_TIMEOUT;
				createCookie("claraSessionTimeoutMillis",expireTimeMillis);
			
			this.syncWithSessionCookie();	// first check cookie for new timeout value
		},
		reset:function(){
			clog("Resetting session timer");
			clearTimeout(this.syncTimeoutId);
			this.start();
		},
		hideWarning: function(){
			clog("Hiding warn message");
			jQuery("#clara-header-notify").hide();
			jQuery(".clara-header-logo").show();
			jQuery(".clara-header-links").show();
			jQuery(".clara-header-userinfo").show();
			clearTimeout(this.syncTimeoutId);
			this.syncTimeoutId = setTimeout(this.syncWithSessionCookie, 1000);
		},
		warn: function(){
			cwarn("warn called.");
			// Show message and button to renew timer
			jQuery(".clara-header-logo").hide();
			jQuery(".clara-header-links").hide();
			jQuery(".clara-header-userinfo").hide();
			jQuery("#clara-header-notify").slideDown();
			clearTimeout(this.syncTimeoutId);
			this.syncTimeoutId = setTimeout(this.syncWithSessionCookie, 1000);
		},
		expire: function(){
			this.expired=true;
			jQuery("body").html("<div class='sessionexpiremessage'>Your CLARA session has expired.<br/><a href='javascript:location.reload();'>Reload page to continue.</a></div>");
			clearTimeout(this.syncTimeoutId);
		}
	},
	
	
	user: {
		id:null,
		committee:null,
		permissions:[]
	},
	
	budget: {
		id: null,
		defaultFA:null,
		studyType:null
	},
	
	review: {
		checklistXmlDocument:null
	},
	
	navigation: {
			init: function(pagename){
				clog("Init page '"+pagename+"'");
				this.current.id = pagename;
				this.update();
			},
			update: function(){
				var formCurrentPage = jQuery("#tab-"+this.current.id);
				var formPreviousPage = jQuery(formCurrentPage).prevAll(':has(div.tab-name a:not(.notclickable))')[0];
				var formNextPage = jQuery(formCurrentPage).nextAll(':has(div.tab-name a:not(.notclickable))')[0];
				this.prev.id = (typeof jQuery(formPreviousPage).attr('id') != 'undefined')?jQuery(formPreviousPage).attr('id').substring(4):null; // cut off "tab-"
				this.prev.name = (this.prev.id)?jQuery(formPreviousPage).text():null;
				this.current.name =  jQuery(formCurrentPage).text();
				this.next.id =  (typeof jQuery(formNextPage).attr('id') != 'undefined')?jQuery(formNextPage).attr('id').substring(4):null;	// cut off "tab-"
				this.next.name =  (this.next.id)?jQuery(formNextPage).text():null;	
				if (this.next.id) {
					jQuery("#btnNextPage").show().html("<a href='javascript:;' class='button white' onclick='submitXMLToNextPage(\""+this.next.id+"\");'>Skip to <strong>"+this.next.name+"</strong>..</a>");
				} else{
					jQuery("#btnNextPage").hide();
				}
			},
			disablePage: function(pagename){
                jQuery("#tab-"+pagename+" a").addClass("notclickable")
                    .removeClass("clickable")
                    .attr('onclick', 'javascript:;')
                    .click(function(){return false;})
                    .unbind()
                    .removeClass("clickable")
                    .addClass("notclickable");

                this.update();
                
                // Also hide summary row (since these rules may be triggered on the summary page)
                jQuery("#summary-page-"+pagename).hide();
			},
			enablePage: function(pagename){
				jQuery("#tab-"+pagename).show();
                jQuery("#tab-"+pagename+" a").removeClass("notclickable").addClass("clickable").click(function(){submitXMLToNextPage(pagename);});
                this.update();
                
                // Also hide summary row (since these rules may be triggered on the summary page)
                jQuery("#summary-page-"+pagename).show();
			},
			prev:{
				id:null,
				name:null
			},
			current: {
				id:null,
				name:null
			},
			next:{
				id:null,
				name:null
			}
	},

	setType: function(t) { this.type = t; },
	setId: function(t) { 
		this.id = parseInt(""+t,10);
	},
	setIdentifier: function(t) {
		this.identifier=t.toUpperCase();
	},
	setForm: function(f) {
		this.form = f;
	},
	setUser: function(o) {
		o.permissions = (typeof o.permissions != "undefined")?o.permissions : this.user.permissions;
		this.user = o;
	},
	
	addUserPermission: function(p) {
		//this.user.permissions = (typeof this.user.permissions != "undefined")?this.user.permissions : [];
		this.user.permissions.push(p);
	}
};

function getUrlVars()
{
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function closeCurrentForm(callback, saveFormFirst){
	saveFormFirst = saveFormFirst || false;
	
	if (saveFormFirst){
		submitXMLToNextPage('', false);
	}
	Ext.MessageBox.show({
		msg: 'Closing form..',
		progressText:'Closing...',
		width:300,
		wait:true,
		animEl:'mb7'
	});
	_closeForm();
	
	
	function _closeForm(){
		var url = appContext+"/ajax/users/"+claraInstance.user.id+"/close-open-form";
		jQuery.ajax({
			  type: 'POST',
			  async:false,
			  url: url,
			  data:{
				'formId':claraInstance.form.id,
				'type':claraInstance.type
			  },
			  success: function(data){
				  if (typeof callback == "function") callback();
			  },
			  error: function(){
				  alert("Couldn't close form.");
				  if (typeof callback == "function") callback();
			  }
		});
	}
	
	
}

function OpenHelpPage(page){
	var url = "http://clara/help";  // TODO: Change to your help page
	if (page) url += "?id="+page;
	window.open(url,'');
}

function gotoRelativeUrl(url){
	location.href=appContext+url;
}

function sleep(milliseconds) {
	var start = new Date().getTime();
	while ((new Date().getTime() - start) < milliseconds){
	// Do nothing
	}
}





	
function setLogLevel(levels){
	if (!levels || levels.length == 0) levels = ["log","info","warn","debug","error"];
	
	if (navigator.appName == 'Microsoft Internet Explorer'){
		   window.console["debug"] = window.console["log"];
	} 
	
	var emptyFn = function(){};
	var logFn = function(){clog.history=clog.history||[];clog.history.push(arguments);if(this.console){console.log(Array.prototype.slice.call(arguments));}};
	var errFn = function(){cerr.history=cerr.history||[];cerr.history.push(arguments);if(this.console){console.error(Array.prototype.slice.call(arguments));}};
	var wrnFn = function(){cwarn.history=cwarn.history||[];cwarn.history.push(arguments);if(this.console){console.warn(Array.prototype.slice.call(arguments));}};
	var infFn = function(){cinfo.history=cinfo.history||[];cinfo.history.push(arguments);if(this.console){console.info(Array.prototype.slice.call(arguments));}};
	var dbgFn = function(){cdebug.history=cdebug.history||[];cdebug.history.push(arguments);if(this.console){console.debug(Array.prototype.slice.call(arguments));}};

	
	window.clog = (levels.hasValue("log"))?logFn:emptyFn;
	window.cerr = (levels.hasValue("error"))?errFn:emptyFn;
	window.cwarn = (levels.hasValue("warn"))?wrnFn:emptyFn;
	window.cinfo = (levels.hasValue("info"))?infFn:emptyFn;
	window.cdebug = (levels.hasValue("debug"))?dbgFn:emptyFn;
	
	if (Ext && Ext.util && Ext.util.Cookies){
		loglevelstring = Ext.util.Cookies.set("claraLogLevels",levels.join(","));
	}
}




function enableLogs(){
	setLogLevel(["log","info","warn","debug","error"]);
}

function disableLogs(){
	setLogLevel(["error"]);
}

function pad(number, length) {  
 var str = '' + number;
 while (str.length < length) {
     str = '0' + str;
 }
 return str;
}

function stringToHumanReadable(str){
	var arr = str.split(/\s|_/);
    for(var i=0,l=arr.length; i<l; i++) {
        arr[i] = arr[i].substr(0,1).toUpperCase() + 
                 (arr[i].length > 1 ? arr[i].substr(1).toLowerCase() : "");
    }
    return arr.join(" ");
}

String.prototype.toHumanReadable = function() {
	return stringToHumanReadable(this);
}

String.prototype.toUrlEncodedName = function() {
	var str = this;
	var arr = str.split(/\s|_/);
    for(var i=0,l=arr.length; i<l; i++) {
        arr[i] = arr[i].toLowerCase();
    }
    return arr.join("-");
}

String.prototype.toTitleCase = function () {
	  var smallWords = /^(a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|vs?\.?|via)$/i;

	  return this.replace(/([^\W_]+[^\s-]*) */g, function (match, p1, index, title) {
	    if (index > 0 && index + p1.length !== title.length &&
	      p1.search(smallWords) > -1 && title.charAt(index - 2) !== ":" && 
	      title.charAt(index - 1).search(/[^\s-]/) < 0) {
	      return match.toLowerCase();
	    }

	    if (p1.substr(1).search(/[A-Z]|\../) > -1) {
	      return match;
	    }

	    return match.charAt(0).toUpperCase() + match.substr(1);
	  });
	};

function camelize(str) {
	  return str.replace(/(?:^\w|[A-Z]|\b\w|\s+)/g, function(match, index) {
	    if (+match === 0) return ""; // or if (/\s+/.test(match)) for white spaces
	    return index == 0 ? match.toLowerCase() : match.toUpperCase();
	  });
	}


jQuery(document).ready(function(){
	claraInstance.session.start();
});

jQuery(document).ajaxSuccess(function(evt, request, settings) {
	  claraInstance.session.reset();
	  checkAjaxError(evt,request,settings);
	});

jQuery(document).ajaxError(function(evt, request, settings,error) {
	  if (error == "timeout"){
		  cwarn("TIMEOUT",evt, request, settings,error);
		  Ext.Msg.alert('CLARA', 'The server took too long to respond. Try again by refreshing the page.', function(){
				// do nothing, just close the window
			});
	  } else {
		  cwarn("AJAXERROR",evt, request, settings,error);
	  }
});



function checkAjaxError(conn, rawResponse, options){
	var resetTimer = true;
	if (typeof rawResponse.responseText !== "undefined" && rawResponse.responseText.substr(0,1) === "<")
		{ 
			var xmlFind = jQuery(rawResponse.responseText).find("error:first");
		
			if (!jQuery.isEmptyObject(xmlFind) && jQuery(rawResponse.responseText).find("error:first").text() == "true"){
				// XML error
				var xmlMessage = jQuery(rawResponse.responseText).find("message:first").text();
				showErrorMessageBox(xmlMessage,"Error", function(){ });
			}
		
			if (rawResponse.responseText.substr(0,1) === "<" && rawResponse.responseText.substr(0,14) !== "<!DOCTYPE HTML") return false;
			if (rawResponse.responseText.substr(0,14) === "<!DOCTYPE HTML") {
				cerr("XML ERROR", conn,options);
				cerr(rawResponse.responseText);
				return false;// showErrorMessageBox(rawResponse.responseText,"Server Error", function(){ });
			}
			// Catch unwrapped xml responses, return false for now
			
			return false;
		}
	try{
		
		var jsonData = (typeof rawResponse.error !== "undefined" && rawResponse.message !== "undefined" && typeof rawResponse.error !== "function")?rawResponse:jQuery.parseJSON(rawResponse.responseText);

		if (jsonData && jsonData.error === true){
			clog(jsonData.message);
			if (typeof jsonData != "undefined" && ((typeof jsonData.redirect != "undefined" && !jsonData.redirect) && jsonData.shouldRedirect == true)){
				cwarn("checkAjaxError: NO REDIRECT URL FOUND!",jsonData);
			}
			if (typeof jsonData != "undefined" && ((typeof jsonData.redirect != "undefined" && jsonData.redirect && jsonData.redirect != '') && jsonData.shouldRedirect == true)){
				showErrorMessageBox(jsonData.message,"Error", function(){ location.href = appContext+jsonData.redirect; });
			} 
			else {
				showErrorMessageBox(jsonData.message,"Error", function(){ });
			}
			return true;
		} else {
			return false;
		}
	}catch (syntaxError){
		// showErrorMessageBox(syntaxError,"Error", function(){ window.location.reload(); });
		resetTimer = false;
		clog(rawResponse,syntaxError);
	}
	return false;
}

function replaceAll(txt, replace, with_this) {
	  return txt.replace(new RegExp(replace, 'g'),with_this);
	}

function showErrorMessageBox( message, title, callbackFn ){
	
		Ext.MessageBox.show({
		      title: title,
		      msg: message,
		      minWidth:500,
		      buttons: Ext.MessageBox.OK,
		      icon: Ext.MessageBox.INFO,
		      fn:function(btn){
		    	  if (btn == 'ok' && typeof callbackFn == 'function') callbackFn();
		      }
		     });
	
	  clog("showErrorMessageBox",message,title,callbackFn);
	   
	 }


function xmlClean(str){
	return htmlspecialchars(str,'ENT_QUOTES');
}

function isNumber(n) {
	  return !isNaN(parseFloat(n)) && isFinite(n);
	}


Array.prototype.unique = function() {
 var o = {}, i, l = this.length, r = [];
 for(i=0; i<l;i+=1) o[this[i]] = this[i];
 for(i in o) r.push(o[i]);
 return r;
};

function htmlspecialchars (string, quote_style, charset, double_encode) {
 // Convert special characters to HTML entities  
 // 
 // version: 1008.1718
 // discuss at: http://phpjs.org/functions/htmlspecialchars
 // +   original by: Mirek Slugen
 // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
 // +   bugfixed by: Nathan
 // +   bugfixed by: Arno
 // +    revised by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
 // +    bugfixed by: Brett Zamir (http://brett-zamir.me)
 // +      input by: Ratheous
 // +      input by: Mailfaker (http://www.weedem.fr/)
 // +      reimplemented by: Brett Zamir (http://brett-zamir.me)
 // +      input by: felix
 // +    bugfixed by: Brett Zamir (http://brett-zamir.me)
 // %        note 1: charset argument not supported
 // *     example 1: htmlspecialchars("<a href='test'>Test</a>", 'ENT_QUOTES');
 // *     returns 1: '&lt;a href=&#039;test&#039;&gt;Test&lt;/a&gt;'
 // *     example 2: htmlspecialchars("ab\"c'd", ['ENT_NOQUOTES', 'ENT_QUOTES']);
 // *     returns 2: 'ab"c&#039;d'
 // *     example 3: htmlspecialchars("my "&entity;" is still here", null, null, false);
 // *     returns 3: 'my &quot;&entity;&quot; is still here'
 var optTemp = 0, i = 0, noquotes= false;
 if (typeof quote_style === 'undefined' || quote_style === null) {
     quote_style = 2;
 }
 string = string.toString();
 if (double_encode !== false) { // Put this first to avoid double-encoding
     string = string.replace(/&/g, '&amp;');
 }
 string = string.replace(/</g, '&lt;').replace(/>/g, '&gt;');

 var OPTS = {
     'ENT_NOQUOTES': 0,
     'ENT_HTML_QUOTE_SINGLE' : 1,
     'ENT_HTML_QUOTE_DOUBLE' : 2,
     'ENT_COMPAT': 2,
     'ENT_QUOTES': 3,
     'ENT_IGNORE' : 4
 };
 if (quote_style === 0) {
     noquotes = true;
 }
 if (typeof quote_style !== 'number') { // Allow for a single string or an array of string flags
     quote_style = [].concat(quote_style);
     for (i=0; i < quote_style.length; i++) {
         // Resolve string input to bitwise e.g. 'PATHINFO_EXTENSION' becomes 4
         if (OPTS[quote_style[i]] === 0) {
             noquotes = true;
         }
         else if (OPTS[quote_style[i]]) {
             optTemp = optTemp | OPTS[quote_style[i]];
         }
     }
     quote_style = optTemp;
 }
 if (quote_style & OPTS.ENT_HTML_QUOTE_SINGLE) {
     string = string.replace(/'/g, '&#039;');
 }
 if (!noquotes) {
     string = string.replace(/"/g, '&quot;');
 }

 return string;
}


function XMLStringToObject(txt){
	
	var doc;
    if(window.ActiveXObject){
      doc = new ActiveXObject("Microsoft.XMLDOM");
      doc.async = "false";
      doc.loadXML(txt);
    }else{
      doc = new DOMParser().parseFromString(txt,"text/xml");
    }
	
	return doc;
}

function XMLObjectToString(elem){
	
	var serialized;
	
	try {
		// XMLSerializer exists in current Mozilla browsers
		serializer = new XMLSerializer();
		serialized = serializer.serializeToString(elem);
	} 
	catch (e) {
		// Internet Explorer has a different approach to serializing XML
		serialized = elem.xml;
	}
	
	return serialized;
}


function getURLParameter(name) {
 return decodeURI(
     (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
 );
}


Clara.HumanReadableType = function(str,separator){
	var ra = str.split(separator);
	return ra.join(" ").toLowerCase().toTitleCase();
};

Clara.HumanReadableRoleName = function(role){
	var ra = role.split("_");
	ra.shift();
	return ra.join(" ").toLowerCase().toTitleCase();
};

var appContext = "/clara-webapp";
//this window appears when you hit "Create New Protocol"
Clara.Config.NewProtocolOptionStore = new Ext.data.ArrayStore({
	autoDestroy:false,
	fields:['formtype','shortdesc', 'longdesc', 'url'],
	data:[
	      ['newsubmission','New Study', '<h3>Use this form to submit a new research protocols and HUD requests.</h3><ul><li>This includes expanded access studies (compassionate use, single use).</li></ul>',appContext+"/protocols/protocol-forms/new-submission/create"],
	      ['hsd','Human Subject Research Determination Request', 'Uncertain whether your research involves the <strong>use of human subjects</strong>? Begin by filling out this form.',appContext+"/protocols/protocol-forms/human-subject-research-determination/create"],
	      /*['singleuse','Single Use IND', 'Submit a protocol that uses an investigational drug or device on a single human research subject that does not fit the requirements of an <strong>Emergency Use Protocol</strong>.',appContext+"/protocols/protocol-forms/new-submission/create"],*/
		  //['hud','Humanitarian Use Device Application', 'As defined in 21 CFR 814.3(n), a HUD is a "medical device intended to benefit patients in the treatment or diagnosis of a disease or condition that affects or is manifested in fewer than 4,000 individuals in the United States per year."',appContext+"/protocols/protocol-forms/humanitarian-use-device/create"],
		  ['emergencyuse','Emergency Use Notification/Follow-up Report', 'This includes notifications and follow-up reports.',appContext+"/protocols/protocol-forms/emergency-use/create"]
	       ]
});



Clara.Config.NewContractOptionStore = new Ext.data.ArrayStore({
	autoDestroy:false,
	fields:['formtype','shortdesc', 'longdesc', 'url'],
	data:[
	      ['newcontract','New Contract', '<h2>A contract is required if one of the following conditions exists:</h2><ul>'
	        + '<li>You are receiving funding  from an individual or an entity outside of UAMS.</li>'
	       	+ '<li>You are receiving a drug from an individual or an entity outside of UAMS.</li>'  
	       	+ '<li>You are receiving a device from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing research data to an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing or receiving any biological materials, animals or other materials to or from an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are in a collaborative research arrangement with an individual or an entity outside of UAMS.</li>' 
	       	+ '<li>You are providing a Limited Data Set of Protected Health Information to or from an entity outside of UAMS.</li>' 
	       	+ '<li>You are using an individual or an entity outside of UAMS as a subcontractor.</li>' 
	       	+ '<li>You are subcontractor doing work for an individual or entity outside of UAMS.</li>' 
	       	+ '</ul><br/><h2>Please follow the quesitons and instructions in the New Contract form.</h2><br/>'
	       ,appContext+"/contracts/contract-forms/new-contract/create"]
	       ]
});


Clara.Config.OtherOptionStore = new Ext.data.ArrayStore({
	autoDestroy:false,
	fields:['formtype','shortdesc', 'longdesc', 'url'],
	data:[
	      ['viewprofile','Edit my Clara profile', '<h2>Visit this page to:</h2><ul><li>Change your password</li><li>Upload or update your CV</li><li>View your security settings</li></ul>', appContext+"/users/forget-password"]
	       ]
});

Clara.Config.HelpOptionStore = new Ext.data.ArrayStore({
	autoDestroy:false,
	fields:['formtype','shortdesc', 'longdesc', 'url'],
	data:[['clara-help','Online Help','Looking for online help? Visit the Clara Help site.','https://clara/help'], // TODO: Change to your help page
	      

	       ['viewprofile','Edit my Clara profile', '<h2>Visit this page to:</h2><ul><li>Change your password</li><li>Upload or update your CV</li><li>View your security settings</li></ul>', appContext+"/user/profile"]

	       ]

});

function claraAjaxPing(callback){
	var url = appContext+"/ajax/system/get-current-time";
	jQuery.ajax({
		  type: 'GET',
		  async:true,
		  url: url,
		  success: function(data){
			  if (typeof callback == "function") callback();
		  },
		  error: function(){
			  cwarn("Couldn't ping /ajax/system/get-current-time.");
			  if (typeof callback == "function") callback();
		  }
	});
}

function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    } else var expires = "";
    document.cookie = escape(name) + "=" + escape(value) + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = escape(name) + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return unescape(c.substring(nameEQ.length, c.length));
    }
    return null;
}

function eraseCookie(name) {
    createCookie(name, "", -1);
}

function isArray(obj) {
    return Object.prototype.toString.call(obj) === '[object Array]';
}

function ajax_link_clicked(url, response, timeoutSecs){
	var response = (response && response.length > 0)?response:'string'; //default to be string
	timeoutSecs = timeoutSecs || 120;
	var loadMask = new Ext.LoadMask(Ext.getBody(), {msg:"Processing..."});

	jQuery.ajax({url: url,
		type: "GET",
		async: true,
		timeout: timeoutSecs*1000,
		data: {},
		beforeSend: function(){
			loadMask.show();
		},
		success: function(msg){
			var message = msg;
			if(response == 'json'){
				clog(msg);
				message = (msg.error?"Error: ":"") + msg.message;
			}

			Ext.Msg.show({
			   title:'Response?',
			   msg: message,
			   buttons: Ext.Msg.OK
			});
			loadMask.hide();
		}
	});

}

window.sayswho= (function(){
    var N= navigator.appName, ua= navigator.userAgent, tem;
    var M= ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
    if(M && (tem= ua.match(/version\/([\.\d]+)/i))!= null) M[2]= tem[1];
    M= M? [M[1], M[2]]: [N, navigator.appVersion, '-?'];

    return M;
})();

