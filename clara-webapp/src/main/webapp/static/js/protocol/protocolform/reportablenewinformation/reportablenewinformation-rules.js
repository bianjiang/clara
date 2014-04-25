Ext.ns('Clara.ProtocolForm');

// RULES START HERE
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-unanticipated-problem-involving-risk' ],
	dependantQuestionPaths : [ '/reportable-new-information/info-category', '/reportable-new-information/unanticipated-problem-involving-risk' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/info-category'] == 'non-local-adverse-events' || answers['/reportable-new-information/info-category'] == ''){
			hide = false;			
		} else {
			hide = true;
		}
		
		if (answers['/reportable-new-information/info-category'] != '' && answers['/reportable-new-information/info-category'] == 'non-local-adverse-events') {
			if (answers['/reportable-new-information/unanticipated-problem-involving-risk'] == 'n') {
				jQuery('#is-reportable').val('n');
				answers["/reportable-new-information/is-reportable"] = "n";
				
				claraInstance.navigation.disablePage("basic-details");
				claraInstance.navigation.disablePage("report");
				claraInstance.navigation.disablePage("documents");
			} else if (answers['/reportable-new-information/unanticipated-problem-involving-risk'] == 'y') {
				jQuery('#is-reportable').val('y');
				answers["/reportable-new-information/is-reportable"] = "y";
				
				claraInstance.navigation.enablePage("basic-details");
				claraInstance.navigation.enablePage("report");
				claraInstance.navigation.enablePage("documents");
			}
		} else {
			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";
			
			claraInstance.navigation.enablePage("basic-details");
			claraInstance.navigation.enablePage("report");
			claraInstance.navigation.enablePage("documents");
		}
		
		this.hide(hide);
	}
}));

/*
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-unanticipated-problem-involving-risk' ],
	dependantQuestionPaths : [ '/reportable-new-information/unanticipated-problem-involving-risk' ],
	execute : function(answers) {
		
		if (answers['/reportable-new-information/unanticipated-problem-involving-risk'] == 'n'){

			jQuery('#is-reportable').val('n');
			answers["/reportable-new-information/is-reportable"] = "n";
			
			claraInstance.navigation.disablePage("basic-details");
			claraInstance.navigation.disablePage("report");
			claraInstance.navigation.disablePage("documents");
			
		} else if (answers['/reportable-new-information/unanticipated-problem-involving-risk'] == 'y') {

			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";
			
			claraInstance.navigation.enablePage("basic-details");
			claraInstance.navigation.enablePage("report");
			claraInstance.navigation.enablePage("documents");
		}

	}
}));
*/

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-consent-protocol-changed' ],
	dependantQuestionPaths : [ '/reportable-new-information/basic-details/consent-protocol-changed' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/basic-details/consent-protocol-changed'] == 'y'){
			hide = false;
		}
		
		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-was-adverse-effect-serious', 'question-was-effect-caused-by-device', 'question-was-effect-previously-identified'],
	dependantQuestionPaths : [ '/reportable-new-information/info-category' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/info-category'] == 'unanticipated-adverse-device-effects'){
			hide = false;
		}
		
		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-was-effect-previously-identified' ],
	dependantQuestionPaths : [ '/reportable-new-information/basic-details/was-effect-previously-identified' ],
	execute : function(answers) {
		
		if (answers['/reportable-new-information/basic-details/was-effect-previously-identified'] == 'n'){
			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";
			
			claraInstance.navigation.disablePage("report");
			claraInstance.navigation.disablePage("documents");
			
		} else if (answers['/reportable-new-information/unanticipated-problem-involving-risk'] == 'y') {
			claraInstance.navigation.enablePage("report");
			claraInstance.navigation.enablePage("documents");

		}

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-manner' ],
	dependantQuestionPaths : [ '/reportable-new-information/report/unexpected-or-unanticipated' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/report/unexpected-or-unanticipated'] == 'y'){
			hide = false;
		}
		
		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-related-to-the-research' ],
	dependantQuestionPaths : [ '/reportable-new-information/info-category', '/reportable-new-information/report/unexpected-or-unanticipated' ],
	execute : function(answers) {
		var hide = true;
		
		if (answers['/reportable-new-information/report/unexpected-or-unanticipated'] == 'n' && (answers['/reportable-new-information/info-category'] == 'local-adverse-events' ||
				answers['/reportable-new-information/info-category'] == 'new-information' ||
				answers['/reportable-new-information/info-category'] == 'other')){
			hide = true;
			
			jQuery('#is-reportable').val('n');
			answers["/reportable-new-information/is-reportable"] = "n";
			
		} else {

			hide = false;
			
			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";
		}
		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-related-to-the-research' ],
	dependantQuestionPaths : [ '/reportable-new-information/report/related-to-the-research' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/report/related-to-the-research'] == 'y'){
			hide = false;
		}
		
		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-involve-new-or-increased-risks' ],
	dependantQuestionPaths : [ '/reportable-new-information/info-category', '/reportable-new-information/report/related-to-the-research' ],
	execute : function(answers) {
		var hide = true;
		
		if (answers['/reportable-new-information/report/related-to-the-research'] == 'n' && (answers['/reportable-new-information/info-category'] == 'local-adverse-events' ||
				answers['/reportable-new-information/info-category'] == 'new-information' ||
				answers['/reportable-new-information/info-category'] == 'other')){
			hide = true;
			
			jQuery('#is-reportable').val('n');
			answers["/reportable-new-information/is-reportable"] = "n";
			
		} else if (answers["/reportable-new-information/is-reportable"] == 'y'){
			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";

			hide = false;
			
		}
		
		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-involve-new-or-increased-risks' ],
	dependantQuestionPaths : [ '/reportable-new-information/report/involve-new-or-increased-risks' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/reportable-new-information/report/involve-new-or-increased-risks'] == 'y'){
			hide = false;
		}
		
		this.hide(hide);

	}
}));

/*
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-involve-new-or-increased-risks' ],
	dependantQuestionPaths : [ '/reportable-new-information/info-category', '/reportable-new-information/report/involve-new-or-increased-risks' ],
	execute : function(answers) {
		
		if (answers['/reportable-new-information/report/involve-new-or-increased-risks'] == 'n' && (answers['/reportable-new-information/info-category'] == 'local-adverse-events' ||
				answers['/reportable-new-information/info-category'] == 'new-information' ||
				answers['/reportable-new-information/info-category'] == 'other')){
			
			jQuery('#is-reportable').val('n');
			answers["/reportable-new-information/is-reportable"] = "n";
			
		} else if (answers["/reportable-new-information/is-reportable"] == 'y') {
			jQuery('#is-reportable').val('y');
			answers["/reportable-new-information/is-reportable"] = "y";
		}

	}
}));
*/