Ext.ns('Clara.ProtocolForm');

// RULES START HERE
 
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-is-audit'],
	dependantQuestionPaths : [ "/protocol/initial-mod",
	                           "/protocol/modification/to-modify-section/is-audit" ],
	execute : function(answers) {
		
		claraInstance.navigation.enablePage("budget");

		if (answers['/protocol/initial-mod'] == "y") {
			jQuery("#is-audit-n").attr("checked", true);

			jQuery("#is-audit-y").attr("disabled", 'disabled');
		} else {
			if (answers['/protocol/modification/to-modify-section/is-audit'] == "y") {
				claraInstance.navigation.disablePage("budget");
			} else {
				claraInstance.navigation.enablePage("budget");
			}
		}

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-complete-migration'],
	dependantQuestionPaths : [ "/protocol/migrated",
	                           "/protocol/initial-mod"],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/migrated'] == "y"&&answers['/protocol/initial-mod'] == "y") {
			hide = false;
		}

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-complete-budget-migration'],
	dependantQuestionPaths : [ "/protocol/modification/to-modify-section/complete-migration",
	                           "/protocol/migrated",
	                           "/protocol/initial-mod",
	                           "/protocol/compliance-approved" ],
	execute : function(answers) {

		var hide = true;
		
		if (answers['/protocol/migrated'] == "y") {
			if (answers['/protocol/initial-mod'] == "y" || answers['/protocol/compliance-approved'] == "y") {
				hide = true;
			} else {
				hide = false;
			}
				
		} else {
			hide = true;
		}

		if (answers['/protocol/modification/to-modify-section/complete-migration'] == 'y'){ hide = true; }

		this.hide(hide);
	}
}));



Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-submit-to-medicare' ],
			dependantQuestionPaths : [ "/protocol/modification/to-modify-section/is-audit",
			                           "/protocol/modification/to-modify-section/involve-change-in/budget-modified",
			                           "/protocol/modification/to-modify-section/involve-change-in/contract-modified",
			                           "/protocol/modification/to-modify-section/involve-change-in/pi-modified",
			                           "/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure",
			                           "/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy",
			                           "/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects",
			                           "/protocol/modification/to-modify-section/amendment-to-injury",
			                           "/protocol/modification/to-modify-section/submit-to-medicare",
			                           "/protocol/modification/to-modify-section/conduct-under-uams", 
			                           "/protocol/study-type",
			                           "/protocol/crimson/has-budget",
			                           "/protocol/site-responsible",
			                           "/protocol/migrated",
			                           "/protocol/budget/potentially-billed",
			                           "/protocol/budget/need-budget-in-clara" ],
			execute : function(answers) {
				jQuery('#require-review').val('irb');
				answers["/protocol/modification/require-review"] = "irb";
				
				jQuery('#require-budget-review').val('n');
				answers["/protocol/modification/require-budget-review"] = "n";
				
				if (answers['/protocol/modification/to-modify-section/involve-change-in/budget-modified'] == "y" 
					|| answers['/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure'] == "y"
						|| answers['/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy'] == "y"
						|| answers['/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects'] == "y"
						|| answers['/protocol/modification/to-modify-section/amendment-to-injury'] == "y" 
						|| answers['/protocol/modification/to-modify-section/submit-to-medicare'] == "y" ){
					jQuery('#require-budget-review').val('y');
					answers["/protocol/modification/require-budget-review"] = "y";
				} else {
					jQuery('#require-budget-review').val('n');
					answers["/protocol/modification/require-budget-review"] = "n";
				}
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-involve-change-in' ],
			dependantQuestionPaths : [ "/protocol/modification/to-modify-section/involve-change-in/contract-modified",
			                           "/protocol/modification/to-modify-section/involve-change-in/pi-modified" ],
			execute : function(answers) {
				jQuery('#notify-contract').val('n');
				answers["/protocol/modification/notify-contract"] = "n";
				
				if (answers['/protocol/modification/to-modify-section/involve-change-in/contract-modified'] == "y"
					|| answers['/protocol/modification/to-modify-section/involve-change-in/pi-modified'] == "y"
					) {
					
					jQuery('#notify-contract').val('y');
					answers["/protocol/modification/notify-contract"] = "y";
				} else {
					jQuery('#notify-contract').val('n');
					answers["/protocol/modification/notify-contract"] = "n";
				}

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-conduct-under-uams' ],
			dependantQuestionPaths : [ "/protocol/study-type",
			                           "/protocol/modification/to-modify-section/complete-migration"],
			execute : function(answers) {
				var hide = true;

				if ((answers['/protocol/study-type'] == "investigator-initiated" || answers['/protocol/study-type'] == "") && answers['/protocol/modification/to-modify-section/complete-migration'] != "y") {
					hide = false;
				} 

				this.hide(hide);
			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-describe-requested-change' ],
			dependantQuestionPaths : [ "/protocol/modification/to-modify-section/affect-risk",
			                           "/protocol/modification/to-modify-section/complete-migration" ],
			execute : function(answers) {
				var hide = true;
				
				if (answers['/protocol/modification/to-modify-section/affect-risk'] == "y" && answers['/protocol/modification/to-modify-section/complete-migration'] != "y") {
					hide = false;
				} 

				this.hide(hide);
			}
		}));
