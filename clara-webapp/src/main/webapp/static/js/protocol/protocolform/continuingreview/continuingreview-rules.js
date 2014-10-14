Ext.ns('Clara.ProtocolForm');

// RULES START HERE
Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-keep-study-open' ],
			dependantQuestionPaths : [ "/continuing-review/general-study-info/keep-study-open" ],
			execute : function(answers) {
				//var hide = true;
				jQuery('#need-cr').val('n');
				answers["/continuing-review/need-cr"] = "n";
				
				if (answers['/continuing-review/general-study-info/keep-study-open'] == 'y') {
					//hide = false;
					
					jQuery('#need-cr').val('y');
					answers["/continuing-review/need-cr"] = "y";
					
					claraInstance.navigation.enablePage("accrual-of-subjects");
					claraInstance.navigation.enablePage("study-report");
					claraInstance.navigation.enablePage("conflict-of-interest");
					claraInstance.navigation.enablePage("staff");
					claraInstance.navigation.enablePage("documents");
					
				} else {
					//hide = true;
					
					jQuery('#need-cr').val('n');
					answers["/continuing-review/need-cr"] = "n";
					
					claraInstance.navigation.disablePage("accrual-of-subjects");
					claraInstance.navigation.disablePage("study-report");
					claraInstance.navigation.disablePage("conflict-of-interest");
					claraInstance.navigation.disablePage("staff");
					//claraInstance.navigation.disablePage("risks");
					claraInstance.navigation.disablePage("documents");
				}
				//this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-any-change-affect-study-exempt-review-classification' ],
			dependantQuestionPaths : [ "/continuing-review/general-study-info/keep-study-open",
			                           "/continuing-review/most-recent-study/approval-status", 
			                           "/continuing-review/original-study/approval-status" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/general-study-info/keep-study-open'] == 'y') {
					if ((answers['/continuing-review/most-recent-study/approval-status'] && answers['/continuing-review/most-recent-study/approval-status'].length > 0 
							&& answers['/continuing-review/most-recent-study/approval-status'] == 'Exempt')
							|| (answers['/continuing-review/original-study/approval-status'] == 'Exempt'))
							{
							hide = false;
							claraInstance.navigation.disablePage("accrual-of-subjects");
							claraInstance.navigation.disablePage("study-report");
							claraInstance.navigation.disablePage("conflict-of-interest");
							claraInstance.navigation.disablePage("documents");
					}
					
				}

				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-any-change-affect-study-exempt-review-classification-other' ],
			dependantQuestionPaths : [ "/continuing-review/general-study-info/any-change-affect-study-exempt-review-classification" ],
			execute : function(answers) {
				this.hide((answers['/continuing-review/general-study-info/any-change-affect-study-exempt-review-classification'] == 'n'));
			}
		}));



Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-continuing-review-study-status' ],
			dependantQuestionPaths : [ "/continuing-review/general-study-info/keep-study-open",
			                           "/continuing-review/general-study-info/any-change-affect-study-exempt-review-classification",
			                           "/continuing-review/study-status/statuses/status" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/continuing-review/general-study-info/keep-study-open'] == 'y' 
					//&& !answers['/continuing-review/general-study-info/any-change-affect-study-exempt-review-classification']
					&& answers['/continuing-review/general-study-info/any-change-affect-study-exempt-review-classification'].length == 0) {
					hide = false;
				}
				
				if (answers['/continuing-review/general-study-info/study-status/statuses/status']
				&& answers['/continuing-review/general-study-info/study-status/statuses/status'].length > 0){
					if (answers['/continuing-review/general-study-info/study-status/statuses/status'] == 'study-no-initiated' ||
							answers['/continuing-review/general-study-info/study-status/statuses/status'] == 'no-subjects-enrolled'){
						claraInstance.navigation.disablePage("accrual-of-subjects");
						claraInstance.navigation.disablePage("study-report");
						claraInstance.navigation.disablePage("conflict-of-interest");
						claraInstance.navigation.disablePage("staff");
						//claraInstance.navigation.disablePage("risks");
						claraInstance.navigation.disablePage("documents");
				} else {
					claraInstance.navigation.enablePage("accrual-of-subjects");
					claraInstance.navigation.enablePage("study-report");
					claraInstance.navigation.enablePage("conflict-of-interest");
					claraInstance.navigation.enablePage("staff");
					//claraInstance.navigation.enablePage("risks");
					claraInstance.navigation.enablePage("documents");
				}
			}
				
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-number-of-charts-reviewed' ],
			dependantQuestionPaths : [ "/continuing-review/subject-accrual/chart-review-study-only" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/subject-accrual/chart-review-study-only'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-continuing-review-subject-array',
			                'question-continuing-review-first-subject-enrolled-date',
			                'question-vulnerable-populations-since-activation' ],
			dependantQuestionPaths : [ "/continuing-review/subject-accrual/chart-review-study-only" ],
			execute : function(answers) {
				var hide = false;
				if (answers['/continuing-review/subject-accrual/chart-review-study-only'] == 'y') {
					hide = true;
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
			dependantQuestionPaths : [ "/continuing-review/study-report/is-audited-by-federal-agency" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/is-audited-by-federal-agency'] == 'y') {
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
			dependantQuestionPaths : [ "/continuing-review/study-report/study-focus-of-litigation" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/study-focus-of-litigation'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-adverse-events-accur-at-frequency', 'question-adverse-events-change-risk' ],
			dependantQuestionPaths : [ "/continuing-review/study-report/any-adverse-events",
			                           "/continuing-review/has-external-sponsor" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/any-adverse-events'] == 'y' && 
						answers['/continuing-review/has-external-sponsor'] == 'n') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-sponsor-provide-information' ],
			dependantQuestionPaths : [ "/continuing-review/study-report/any-adverse-events",
			                           "/continuing-review/has-external-sponsor" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/any-adverse-events'] == 'y' && 
						answers['/continuing-review/has-external-sponsor'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-deviations-how-many', 'question-deviations-occur-in-pattern','question-deviations-negatively-impact' ],
			dependantQuestionPaths : [ "/continuing-review/study-report/any-deviations" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/any-deviations'] == 'y') {
					hide = false;
				}
				this.hide(hide);
			}
		}));

/*
Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-irb-not-notified-explain' ],
			dependantQuestionPaths : [ "/continuing-review/study-report/study-focus-of-litigation" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/continuing-review/study-report/study-focus-of-litigation'] == 'n') {
					hide = false;
				}
				this.hide(hide);
			}
		}));
*/