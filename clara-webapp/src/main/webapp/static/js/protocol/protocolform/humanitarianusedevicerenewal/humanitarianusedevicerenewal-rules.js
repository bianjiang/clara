Ext.ns('Clara.ProtocolForm');

// RULES START HERE

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	questionIds : [ 'question-initial-hud-or-hud-renewal-application' ],
//	dependantQuestionPaths : [ "/hud/basic-details/initial-hud-or-hud-renewal-application" ],
//	execute : function(answers) {
//
//		if (answers['/hud/basic-details/initial-hud-or-hud-renewal-application'] == 'initial') {	
//			jQuery("#tab-initial-application").show();
//			jQuery("#tab-initial-application a").removeClass("notclickable").addClass("clickable").click(function(){submitXMLToNextPage( 'initial-application');});
//			
//		} else {
//			jQuery("#tab-initial-application a").addClass("notclickable").removeClass("clickable");
//			jQuery("#tab-initial-application a").attr('onclick', 'javascript:;').click(function(){return false;}).unbind().removeClass("clickable").addClass("notclickable");
//		}
//		
//		if (answers['/hud/basic-details/initial-hud-or-hud-renewal-application'] == 'renewal') {	
//			jQuery("#tab-renewal-application").show();
//			jQuery("#tab-renewal-application a").removeClass("notclickable").addClass("clickable").click(function(){submitXMLToNextPage( 'renewal-application');});
//			
//		} else {
//			jQuery("#tab-renewal-application a").addClass("notclickable").removeClass("clickable");
//			jQuery("#tab-renewal-application a").attr('onclick', 'javascript:;').click(function(){return false;}).unbind().removeClass("clickable").addClass("notclickable");
//		}
//
//	}
//}));

//Renewal Application
/*Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-unanticipated-problems-occurred-desc' ],
	dependantQuestionPaths : [ "/hud-renewal/renewal-application/unanticipated-problems-occurred" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/hud-renewal/renewal-application/unanticipated-problems-occurred'] == 'y') {
			hide = false;
		}

		this.hide(hide);

	}
}));*/