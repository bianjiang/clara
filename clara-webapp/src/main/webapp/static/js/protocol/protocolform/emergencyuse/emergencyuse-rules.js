Ext.ns('Clara.ProtocolForm');

// RULES START HERE

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-ieu-or-eu' ],
	dependantQuestionPaths : [ "/emergency-use/type",
	                           "/emergency-use/basic-details/ieu-or-eu" ],
	execute : function(answers) {
		var hide = false;
		if (answers['/emergency-use/type'] == 'follow-up'){
			//jQuery("#ieu-or-eu").attr("disabled", "disabled");
			hide = true;
		} else {
			if (answers['/emergency-use/basic-details/ieu-or-eu'] == 'emergency-use-follow-up-report') {	
				claraInstance.navigation.enablePage("follow-up-report");
				
			} else {
				claraInstance.navigation.disablePage("follow-up-report");
			}
			
			if (answers['/emergency-use/basic-details/ieu-or-eu'] == 'intended-emergency-use') {	
				claraInstance.navigation.enablePage("notification");
				
			} else {
				claraInstance.navigation.disablePage("notification");
			}
		}
		
		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-irb-acknowledgement-date' ],
	dependantQuestionPaths : [ "/emergency-use/follow-up-report/received-prior-to-eu" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/emergency-use/follow-up-report/received-prior-to-eu'] == 'y') {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-followup-test-article-date',
	                'question-was-life-threatening',
	                'question-was-standard-acceptable-treatment-available', 
	                'question-was-sufficient-time-obtain-irb-approval', 
	                'question-followup-describe-rationale-for-emergency-use' ],
	dependantQuestionPaths : [ "/emergency-use/follow-up-report/received-prior-to-eu" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/emergency-use/follow-up-report/received-prior-to-eu'] == 'n') {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-report-initial-treatement-results-date' ],
	dependantQuestionPaths : [ "/emergency-use/follow-up-report/initial-treatement-results-available" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/emergency-use/follow-up-report/initial-treatement-results-available'] == 'n') {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-informed-consent-obtained-answer' ],
	dependantQuestionPaths : [ "/emergency-use/follow-up-report/informed-consent-obtained" ],
	execute : function(answers) {
		if (answers['/emergency-use/follow-up-report/informed-consent-obtained'] == "y") {
			
			jQuery(".question-informed-consent-obtained-yes").removeClass(
			'form-question-hidden')
			.removeClass('form-question-visible').addClass(
					'form-question-visible');

			jQuery(".question-informed-consent-obtained-no").removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(
							'form-question-hidden');
		
		
			jQuery(".informed-consent-obtained-answer").removeClass(
					'informed-consent-obtained-answer-no').addClass(
					'informed-consent-obtained-answer-yes');
		} else {
			jQuery(".question-informed-consent-obtained-yes").removeClass(
			'form-question-hidden')
			.removeClass('form-question-visible').addClass(
					'form-question-hidden');

			jQuery(".question-informed-consent-obtained-no").removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(
							'form-question-visible');
		
		
			jQuery(".informed-consent-obtained-answer").removeClass(
					'informed-consent-obtained-answer-yes').addClass(
					'informed-consent-obtained-answer-no');
		}
	}
}));