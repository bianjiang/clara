Ext.ns('Clara.ContractForm');

// RULES START HERE

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-irb-number' ],
	dependantQuestionPaths : [ "/contract/basic-information/is-study-related" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/is-study-related'] == 'y') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-confidentiality-disclosure-agreement-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'confidentiality-disclosure-agreement') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-clinical-trial-agreement-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'clinical-trial-agreement') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-material-transfer-agreement-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'material-transfer-agreement') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-research-agreement-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'research-agreement') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-subcontracts-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'subcontracts') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	questionIds : [ 'question-license-sub-type' ],
	dependantQuestionPaths : [ "/contract/basic-information/contract-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/basic-information/contract-type'] == 'license') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-cooperative-group-contract' ],
	dependantQuestionPaths : [ "/contract/study-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/study-type'] == "cooperative-group") {
			hide = false;
		}

		this.hide(hide);
	}
}));

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-investigator-description-contract' ],
	dependantQuestionPaths : [ "/contract/study-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/study-type'] == "investigator-initiated") {
			hide = false;
		}

		this.hide(hide);
	}
})); 

Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-other-investigator-description-contract' ],
	dependantQuestionPaths : [ "/contract/study-type/investigator-initiated/investigator-description" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/study-type/investigator-initiated/investigator-description'] == "other") {
			hide = false;
		}

		this.hide(hide);
	}
}));


Clara.ContractForm.Rules.addRule(new Clara.ContractForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-study-subtype-contract' ],
	dependantQuestionPaths : [ "/contract/study-type",
	                           "/contract/study-type/investigator-initiated/investigator-description" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/contract/study-type'] == "investigator-initiated"
			&& answers['/contract/study-type/investigator-initiated/investigator-description'] != "student-fellow-resident-post-doc") {
			hide = false;
		}

		this.hide(hide);
	}
})); 