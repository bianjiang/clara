Ext.ns('Clara.ProtocolForm');

// RULES START HERE

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-study-status' ],
	dependantQuestionPaths : [ '/study-closure/study-status',
	                           '/study-closure/general-study-info/any-change-affect-study-exempt-review-classification' ],
	execute : function(answers) {
		
		if (answers['/study-closure/study-status'] == 'permanently-closed-to-enrollment'){			
			// Enable tabs
			claraInstance.navigation.enablePage("accrual-of-subjects");
			claraInstance.navigation.enablePage("study-report");
			claraInstance.navigation.enablePage("conflict-of-interest");
			//claraInstance.navigation.enablePage("documents");
				
		} else {
			// Disable tabs
			claraInstance.navigation.disablePage("accrual-of-subjects");
			claraInstance.navigation.disablePage("study-report");
			claraInstance.navigation.disablePage("conflict-of-interest");
			//claraInstance.navigation.disablePage("documents");
		}
		
	}
}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-study-closure-any-change-affect-study-exempt-review-classification' ],
			dependantQuestionPaths : [ "/study-closure/most-recent-study/approval-status", 
			                           "/study-closure/original-study/approval-status",
			                           "/study-closure/study-status" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/study-closure/most-recent-study/approval-status'] && answers['/study-closure/most-recent-study/approval-status'].length > 0 
						&& answers['/study-closure/most-recent-study/approval-status'] == 'Exempt'){
						hide = false;
						
						claraInstance.navigation.disablePage("accrual-of-subjects");
						claraInstance.navigation.disablePage("study-report");
						claraInstance.navigation.disablePage("conflict-of-interest");
						claraInstance.navigation.disablePage("risks");
						//claraInstance.navigation.disablePage("documents");

				} else {

					if (answers['/study-closure/original-study/approval-status'] == 'Exempt' 
						&& answers['/study-closure/study-status']
						&& answers['/study-closure/study-status'].length > 0
						&& answers['/study-closure/study-status'] != 'permanently-closed-to-enrollment'){
						hide = false;
						
						claraInstance.navigation.disablePage("accrual-of-subjects");
						claraInstance.navigation.disablePage("study-report");
						claraInstance.navigation.disablePage("conflict-of-interest");
						claraInstance.navigation.disablePage("risks");
						//claraInstance.navigation.disablePage("documents");

					} else {
						hide = true;
					}
				}

				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-audit-date',
					'question-is-report-issued',
					'question-is-report-copy-submitted' ],
			dependantQuestionPaths : [ "/study-closure/study-report/is-audited-by-federal-agency" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/study-closure/study-report/is-audited-by-federal-agency'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-is-irb-notified' ],
			dependantQuestionPaths : [ "/study-closure/study-report/study-focus-of-litigation" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/study-closure/study-report/study-focus-of-litigation'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-irb-not-notified-explain' ],
			dependantQuestionPaths : [ "/study-closure/study-report/study-focus-of-litigation", "/study-closure/study-report/study-focus-of-litigation/y/is-irb-notified" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/study-closure/study-report/study-focus-of-litigation'] == 'y'
					&& answers['/study-closure/study-report/study-focus-of-litigation/y/is-irb-notified'] == 'n') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-number-of-charts-reviewed' ],
			dependantQuestionPaths : [ "/study-closure/subject-accrual/chart-review-study-only" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/study-closure/subject-accrual/chart-review-study-only'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-study-closure-reason' ],
			dependantQuestionPaths : [ "/study-closure/study-status" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/study-closure/study-status'] == 'permanently-closed-to-enrollment') {
					hide = false;
				}
				this.hide(hide);
			}
		}));