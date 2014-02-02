Ext.ns('Clara.ProtocolForm');

// RULES START HERE

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-study-involves' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-nature/biomedical-clinical/study-involves/involve" ],
	execute : function(answers) {

		var hide = true;
		
		jQuery('#has-drugs-or-devices').val('n');
		answers["/protocol/has-drugs-or-devices"] = "n";

		if (answers['/protocol/study-nature'] == 'biomedical-clinical') {
			hide = false;
			
			// Enable tabs
			
			claraInstance.navigation.enablePage("drugs-devices");
			//jQuery("#tab-drugs-devices").show();
			//jQuery("#tab-drugs-devices a").removeClass("notclickable").addClass("clickable").click(function(){submitXMLToNextPage( 'drugs-devices');});
			
			if (this
				.doesAnswerContains(
						answers['/protocol/study-nature/biomedical-clinical/study-involves/involve'],
						[ 'na' ])){
				jQuery('#has-drugs-or-devices').val('n');
				answers["/protocol/has-drugs-or-devices"] = "n";
				
				claraInstance.navigation.disablePage("drugs-devices");
			} else {
				jQuery('#has-drugs-or-devices').val('y');
				answers["/protocol/has-drugs-or-devices"] = "y";
			}
		} else if (answers['/protocol/study-nature'] == 'hud-use'){
			jQuery('#has-drugs-or-devices').val('y');
			answers["/protocol/has-drugs-or-devices"] = "y";
			
			claraInstance.navigation.enablePage("drugs-devices");
		}
		else {
			claraInstance.navigation.disablePage("drugs-devices");
		}

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-study-nature' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-nature/hud-use/where", "/protocol/site-responsible" ],
	execute : function(answers) {

		if (answers['/protocol/study-nature'] == 'hud-use') {
			claraInstance.navigation.disablePage("sites");
			
			claraInstance.navigation.disablePage("hipaa");
			
			claraInstance.navigation.disablePage("consent");
			
			claraInstance.navigation.disablePage("risks");
			
			claraInstance.navigation.disablePage("monitoring");
			
			claraInstance.navigation.disablePage("misc");
			
		} else {
			// Enable tabs
			claraInstance.navigation.enablePage("sites");
			claraInstance.navigation.enablePage("hipaa");			
			claraInstance.navigation.enablePage("consent");		
			claraInstance.navigation.enablePage("risks");
			claraInstance.navigation.enablePage("monitoring");
			claraInstance.navigation.enablePage("misc");
		}
		
		if ((answers['/protocol/site-responsible'] == 'ach-achri') || (answers['/protocol/study-nature'] == 'hud-use' && answers['/protocol/study-nature/hud-use/where'] == 'ach/achri')) {
			claraInstance.navigation.disablePage("contract");
			claraInstance.navigation.disablePage("funding-sources");
			claraInstance.navigation.disablePage("budget");
		} else {
			claraInstance.navigation.enablePage("contract");
			claraInstance.navigation.enablePage("funding-sources");
			claraInstance.navigation.enablePage("budget");
		}
	}
}));


/*
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-site-responsible' ],
	dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/study-nature" ],
	execute : function(answers) {
		if (answers['/protocol/study-nature'] == 'chart-review') {
			//claraInstance.navigation.disablePage("budget");
			claraInstance.navigation.disablePage("monitoring");
		} else {
			if (answers['/protocol/site-responsible'] == 'ach-achri') {
				claraInstance.navigation.disablePage("contract");
				claraInstance.navigation.disablePage("funding-sources");
				claraInstance.navigation.disablePage("budget");
			} else {
				claraInstance.navigation.enablePage("contract");
				claraInstance.navigation.enablePage("funding-sources");
				claraInstance.navigation.enablePage("budget");
			}
		}
	}
}));
*/

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					questionIds : [ 'question-drugs-devices' ],
					dependantQuestionPaths : [ "/protocol/study-nature" ],

					execute : function(answers) {
						var t = this;
						if (answers["/protocol/study-nature"] == 'social-behavioral-education') {
							var qSelector = "#question-drugs-devices";
							jQuery(qSelector)
									.addClass("disable-parent-section")
									.html(
											'<h1 class="conditional-question-label">You cannot add drugs or devices to this type of study.</h1>');
						}
					}
				}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-site-responsible' ],
	dependantQuestionPaths : [ "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = false;

		if (answers['/protocol/study-nature'] == 'hud-use') {
			hide = true;
		}

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-hud-be-used', 'question-device-desc', 'question-procedure-desc', 'question-reason-use-of-device', 'question-process-of-consent', 'question-charge-for-device' ],
	dependantQuestionPaths : [ "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/study-nature'] == 'hud-use') {
			hide = false;
		}

		this.hide(hide);
	}
}));





Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [// CONSENT xxxx
	               'question-consent-process-included'
 ],
	dependantQuestionPaths : [ "/protocol/study-nature" ],
	execute : function(answers) {

		if (answers['/protocol/study-nature'] == 'chart-review') {
			jQuery("#consent-process-included_3").attr("checked", true);
			answers['/protocol/consent/processes/included'] = 'none';
		}


	}
}));



Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-study-type', 'question-patient-locations', 'question-lay-summary' ],
	dependantQuestionPaths : [ "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/study-nature'] != 'hud-use') {
			hide = false;
		}

		this.hide(hide);
	}
}));

// START


Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-biosafety-bio-hazard-materials-material' ],
	dependantQuestionPaths : [ "/protocol/site-responsible" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] != "ach-achri") {
			hide = false;
		}

		this.hide(hide);

	}
}));


Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-study-phases' ],
					dependantQuestionPaths : [ "/protocol/study-nature",
							"/protocol/study-nature/biomedical-clinical/study-involves/involve" ],
					execute : function(answers) {
						var hide = true;
						if (answers['/protocol/study-nature'] == "biomedical-clinical"
								&& 
								this
								.doesAnswerContains(
										answers['/protocol/study-nature/biomedical-clinical/study-involves/involve'],
										[ 'drugs' ])) {
							hide = false;
						}
						this.hide(hide);

					}
				}));

// MISC

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-observation-goal' ],
	dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/misc/is-cancer-study/y/study-uses/use", "/protocol/misc/is-cancer-study" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/misc/is-cancer-study/y/study-uses/use'] == 'observation'
			&& answers['/protocol/misc/is-cancer-study'] == 'y') {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-intervention-goal' ],
	dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/misc/is-cancer-study/y/study-uses/use", "/protocol/misc/is-cancer-study" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/misc/is-cancer-study/y/study-uses/use'] == 'intervention'
			&& answers['/protocol/misc/is-cancer-study'] == 'y') {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-nontreatment-type' ],
	dependantQuestionPaths : [ 
			"/protocol/misc/is-cancer-study", "/protocol/misc/is-cancer-study/y/prmc/is-non-treatment-study" ],
	execute : function(answers) {
		var hide = true;
		
		if (answers['/protocol/misc/is-cancer-study'] == "y"
				&& answers['/protocol/misc/is-cancer-study/y/prmc/is-non-treatment-study'] == "y") {
			hide = false;
		}
		
		this.hide(hide);

	}
}));

// BASIC-DETAIL

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-study-type' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-type", "/protocol/site-responsible", "/protocol/study-type/investigator-initiated/investigator-description" ],
	execute : function(answers) {
		
		jQuery('#require-support-type').val('n');
		answers["/protocol/require-support-type"] = "n";
		
		if (answers['/protocol/study-nature'] != "hud-use" && answers['/protocol/study-type'] == "investigator-initiated") {
			if (answers['/protocol/site-responsible'] == "ach-achri") {
				jQuery('#require-support-type').val('y');
				answers['/protocol/require-support-type'] = "y";
			} else if (answers['/protocol/study-type/investigator-initiated/investigator-description'] != "student-fellow-resident-post-doc") {
				jQuery('#require-support-type').val('y');
				answers['/protocol/require-support-type'] = "y";
			} else {
				jQuery('#require-support-type').val('n');
				answers["/protocol/require-support-type"] = "n";
			}
		} else {
			jQuery('#require-support-type').val('n');
			answers["/protocol/require-support-type"] = "n";
		}

	}
})); 

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-investigator-description' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-type", "/protocol/site-responsible", "/protocol/study-type/investigator-initiated/investigator-description" ],
	execute : function(answers) {
		
		var hide = true;

		if (answers['/protocol/study-nature'] != "hud-use" && answers['/protocol/study-type'] == "investigator-initiated") {
			hide = false;
		}

		this.hide(hide);
	}
})); 

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-study-subtype'],
	dependantQuestionPaths : [ "/protocol/study-nature",
	                           "/protocol/study-type",
	                           "/protocol/study-type/investigator-initiated/investigator-description",
	                           "/protocol/site-responsible" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/study-nature'] != "hud-use"
			&& answers['/protocol/study-type'] == "investigator-initiated") {
			if (answers['/protocol/study-type/investigator-initiated/investigator-description'] == "student-fellow-resident-post-doc") {
				if (answers['/protocol/site-responsible'] == "ach-achri") {
					hide = false;
				} else {
					hide = true;
				}
			} else {
				hide = false;
			}
			
		}

		this.hide(hide);
	}
})); 

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-study-subtype' ],
	dependantQuestionPaths : [ "/protocol/study-nature",
	                           "/protocol/study-type",
	                           "/protocol/study-type/investigator-initiated/sub-type",
	                           "/protocol/site-responsible" ],
	execute : function(answers) {
		jQuery("#default-fa").val('0');
		answers['/protocol/default-fa'] = "0";

		if (answers['/protocol/study-nature'] == "hud-use"){
			jQuery("#default-fa").val('0');
			answers['/protocol/default-fa'] = "0";
		} else {
			if (answers['/protocol/study-type'] == "industry-sponsored"
				|| this
				.doesAnswerContains(
						answers['/protocol/study-type/investigator-initiated/sub-type'],
						[ 'industry-support-full-funding', 'industry-support-partial-funding', 'industry-support-drug-device-only' ])
				|| this
				.doesAnswerContains(
						answers['/protocol/study-nature/biomedical-clinical/study-involves/involve'],
						[ 'devices' ])){
				jQuery("#default-fa").val('25');
				answers['/protocol/default-fa'] = "25";
			}
			
			if (answers['/protocol/study-type'] == "cooperative-group"){
				jQuery("#default-fa").val('47');
				answers['/protocol/default-fa'] = "47";
			}
		}

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-other-investigator-description' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-type/investigator-initiated/investigator-description" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/study-nature'] != "hud-use" && answers['/protocol/study-type/investigator-initiated/investigator-description'] == "other") {
			hide = false;
		}

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-cooperative-group' ],
	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/study-type" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/study-nature'] != "hud-use" && answers['/protocol/study-type'] == "cooperative-group") {
			hide = false;
		}

		this.hide(hide);
	}
}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-disease-ontology' ],
//	dependantQuestionPaths : [ "/protocol/study-nature", "/protocol/examine-disease-or-condition" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (answers['/protocol/examine-disease-or-condition'] == "y") {
//			hide = false;
//		}
//
//		this.hide(hide);
//	}
//}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-uses', 'question-disease-ontology', 'question-cancer-codes', 'question-go-through-doc' ],
	dependantQuestionPaths : [ "/protocol/misc/is-cancer-study", "/protocol/site-responsible" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/misc/is-cancer-study'] == 'y') {
			hide = false;
		}

		this.hide(hide);
	}
}));

//Risks
Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-storage-of-biological-materials-specimens-be-collected',
			                'question-specimens-be-obtained-specimen-identification',
			                'question-biological-materials-for-future-research',
			                'question-describe-physical-location',
			                'question-who-manage-speciments',
			                'question-how-long-specimens-stored',
			                'question-process-for-destruction',
			                'question-sample-related-to-other-investigators'],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-whom-will-be-released-to',
			                'question-whether-sample-will-be-released-to',
			                'question-process-for-request-and-release-samples'
			               ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-process-used-to-code-records'
			               ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators/y/whether-sample-will-be-released-to" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators'] == "y"
					&& (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators/y/whether-sample-will-be-released-to'] == "coded" || 
						answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators/y/whether-sample-will-be-released-to'] == "de-identified")) {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-neither-coded-nor-deidentified-desc'
			               ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators/y/whether-sample-will-be-released-to" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/sample-related-to-other-investigators/y/whether-sample-will-be-released-to'] == "neither") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-research-records-process' ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/research-records" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/research-records'] == "coded" || 
						answers['/protocol/risks/storage-of-biological-materials/research-records'] == "de-identified") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-explain-research-records-process' ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/research-records" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/research-records'] == "neither") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-types-of-specimens'],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained", 
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-types-of-specimens-blood' ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained", 
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected/y/types-of-specimens/type" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
					&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected'] == "y"
					&& this
					.doesAnswerContains(
							answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected/y/types-of-specimens/type'],
							[ 'blood' ])) {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-types-of-specimens-blood' ],
			dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained", 
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected",
			                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected/y/types-of-specimens/type" ],
			execute : function(answers) {
				var hide = true;
				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"){
					if (this
							.doesAnswerContains(
									answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification'],
									[ 'coded' ])){
						hide = false;
					}				
				}

				this.hide(hide);

			}
		}));

// APPENDIX I Storage of Biological Materials for future research use
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [
	               'question-storage-of-biologicals-for-future-use-purpose',
	               'question-storage-of-biologicals-for-future-use-limits-exist',
	               'question-storage-of-biologicals-for-future-use-specify-procedures-of-withdraw' ],
	dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
	                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/biological-materials-for-future-research" ],
	execute : function(answers) {

				var hide = true;
		
			    if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y" 
			    	&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/biological-materials-for-future-research'] == "y") {
			        hide = false;
			    }
			    this.hide(hide);

	    }
}));

// SITES
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-multisite-is-local-lead-entity' ],
	dependantQuestionPaths : [ "/protocol/sites/single-or-multi" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/sites/single-or-multi'] == "multiple-sites") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-communication-protecting-participants', 'question-communication-irb-oversight-arrangements-description' ],
					dependantQuestionPaths : [
							"/protocol/sites/single-or-multi",
							"/protocol/sites/single-or-multi/multiple-sites/is-local-lead-entity" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/sites/single-or-multi'] == "multiple-sites"
								&& answers['/protocol/sites/single-or-multi/multiple-sites/is-local-lead-entity'] == "y") {
							hide = false;
						}
						this.hide(hide);
					}
				}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-have-new-contract' ],
	dependantQuestionPaths : [ "/protocol/study-nature",
	                           "/protocol/study-type",
	                           "/protocol/study-type/investigator-initiated/sub-type",
	                           "/protocol/study-nature/biomedical-clinical/study-involves/involve",
	                           "/protocol/site-responsible",
	                           "/protocol/need-budget-by-department",
	                           "/protocol/study-nature/hud-use/where",
	                           "/protocol/budget/potentially-billed" ],
	execute : function(answers) {
		
		if (claraInstance.form.type == 'NEW_SUBMISSION'){
			if (answers['/protocol/study-type'] == "industry-sponsored" || answers['/protocol/study-type'] == "cooperative-group" ||
					this
					.doesAnswerContains(
							answers['/protocol/study-type/investigator-initiated/sub-type'],
							[ 'industry-support-full-funding', 'industry-support-partial-funding', 'industry-support-drug-device-only' ])) {

				jQuery("#have-new-contract-y").attr("checked", true);

				//jQuery("#q1-y").attr("disabled", 'disabled');
				// above is commented out to prevent blanking issue in IE
				
				jQuery("#have-new-contract-n").attr("disabled", 'disabled');
				
				answers['/protocol/contract/have-new-contract'] = "y";
				
				jQuery('#require-contract-before-irb').val('y');
				answers['/protocol/contract/require-contract-before-irb'] = "y";
			} else {
				jQuery('#require-contract-before-irb').val('n');
				answers['/protocol/contract/require-contract-before-irb'] = "n";
			}
		}
		
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-bq01' ],
	dependantQuestionPaths : [ "/protocol/study-nature",
	                           "/protocol/study-type",
	                           "/protocol/study-type/investigator-initiated/sub-type",
	                           "/protocol/study-nature/biomedical-clinical/study-involves/involve",
	                           "/protocol/site-responsible",
	                           "/protocol/need-budget-by-department",
	                           "/protocol/study-nature/hud-use/where",
	                           "/protocol/budget/potentially-billed" ],
	execute : function(answers) {

		if ((answers['/protocol/study-nature'] == "hud-use" && answers['/protocol/study-nature/hud-use/where'] == "uams") 
				|| ((answers['/protocol/study-type'] == "industry-sponsored"
				|| answers['/protocol/study-type'] == "cooperative-group"
				|| this
				.doesAnswerContains(
						answers['/protocol/study-nature/biomedical-clinical/study-involves/involve'],
						[ 'devices' ])) && answers['/protocol/site-responsible'] == "uams"&& answers['/protocol/study-type'] != "investigator-initiated")) {

				jQuery("#q1-y").attr("checked", true);
				jQuery("#q1-n").attr("disabled", 'disabled');
				
				answers['/protocol/budget/potentially-billed'] = "y";
			}
		
		
		if (answers['/protocol/site-responsible'] == "ach-achri" || answers['/protocol/need-budget-by-department'] == "n"){
			jQuery("#q1-n").attr("checked", true);
			jQuery("#q1-y").attr("disabled", 'disabled');
			
			answers['/protocol/budget/potentially-billed'] = "n";
		}

	}
}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-epic-title' ],
			                dependantQuestionPaths : [ "/protocol/budget/potentially-billed",
			                                           "/protocol/study-nature" ],
			            	execute : function(answers) {

			            		if (answers['/protocol/budget/potentially-billed'] == "y" && answers['/protocol/study-nature'] != "hud-use"){
			            			claraInstance.navigation.enablePage("epic");
			            		} else {
			            			claraInstance.navigation.disablePage("epic");
			            		}
			            	}
		}));


Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-need-budget-in-clara' ],
			                dependantQuestionPaths : [ "/protocol/budget/potentially-billed",
			                                           "/protocol/budget/involves/uams-clinics",
			                                           "/protocol/budget/involves/uams-inpatient-units",
			                                           "/protocol/budget/involves/uams-ss-ou",
			                                           "/protocol/budget/involves/uams-clinicallab",
			                                           "/protocol/budget/involves/uams-radiology",
			                                           "/protocol/budget/involves/uams-pharmacy",
			                                           "/protocol/budget/involves/uams-other",
			                                           "/protocol/budget/involves/uams-supplies",
			                                           "/protocol/budget/involves/fgp-fees",
			                                           "/protocol/budget/involves/industry-support",
			                                           "/protocol/crimson/has-budget",
			                                           "/protocol/initial-mod"],
			            	execute : function(answers) {
			            		var hide = true;
			            		
			            		// check same ruleset as the first question, since its not saved on page load
			            		if (claraInstance.form.type == 'MODIFICATION'){
			            			if (answers['/protocol/initial-mod'] != "y" && answers['/protocol/site-responsible'] != "ach-achri" && answers['/protocol/budget/potentially-billed'] == "y" && answers['/protocol/study-nature'] != 'hud-use'){
			            				hide = false;
			            			}
			            			
			            		}
			            		
			            		this.hide(hide);
			            	}
		}));


Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-budget-determination-answer' ],
	dependantQuestionPaths : [ "/protocol/study-type",
	                           "/protocol/budget/potentially-billed",
                               "/protocol/budget/involves/uams-clinics",
                               "/protocol/budget/involves/uams-inpatient-units",
                               "/protocol/budget/involves/uams-ss-ou",
                               "/protocol/budget/involves/uams-clinicallab",
                               "/protocol/budget/involves/uams-radiology",
                               "/protocol/budget/involves/uams-pharmacy",
                               "/protocol/budget/involves/uams-other",
                               "/protocol/budget/involves/uams-supplies",
                               "/protocol/budget/involves/fgp-fees",
                               "/protocol/budget/involves/industry-support",
	                           "/protocol/study-type/investigator-initiated/sub-type",
	                           "/protocol/study-nature/biomedical-clinical/study-involves/involve",
	                           "/protocol/budget-created",
	                           "/protocol/crimson/has-budget",
	                           "/protocol/budget/need-budget-in-clara",
	                           "/protocol/site-responsible",
	                           "/protocol/initial-mod"],
	execute : function(answers) {
		if (answers['/protocol/initial-mod'] == "y"){
		this.hide(true);
		}
		var needsBudgetPaths = ["/protocol/budget/potentially-billed",
	                              "/protocol/budget/involves/uams-clinics",
	                              "/protocol/budget/involves/uams-inpatient-units",
	                              "/protocol/budget/involves/uams-ss-ou",
	                              "/protocol/budget/involves/uams-clinicallab",
	                              "/protocol/budget/involves/uams-radiology",
	                              "/protocol/budget/involves/uams-pharmacy",
	                              "/protocol/budget/involves/uams-other",
	                              "/protocol/budget/involves/uams-supplies",
	                              "/protocol/budget/involves/fgp-fees",
	                              "/protocol/budget/involves/industry-support"];
		
		var needsBudget = false;
		
		// check same ruleset as the first question, since its not saved on page load
		if (answers['/protocol/study-type'] == "industry-sponsored"
			|| answers['/protocol/study-type'] == "cooperative-group"
			|| this
			.doesAnswerContains(
					answers['/protocol/study-type/investigator-initiated/sub-type'],
					[ 'industry-support-full-funding', 'industry-support-partial-funding', 'industry-support-drug-device-only' ])
			|| this
			.doesAnswerContains(
					answers['/protocol/study-nature/biomedical-clinical/study-involves/involve'],
					[ 'devices' ])){
			if (answers['/protocol/site-responsible'] != "ach-achri"){
				if (claraInstance.form.type == 'MODIFICATION'){
					if (answers['/protocol/budget/need-budget-in-clara'] == "y"){
						needsBudget = true;
					} else {
						needsBudget = false;
					}
				} else {
					needsBudget = true;
				}
			}
				
		}
		
		for (var i=0,l=needsBudgetPaths.length;i<l;i++){
			needsBudget = needsBudget || (answers[needsBudgetPaths[i]] == "y");
		}
		
		var hasCrimsonBudget = (answers['/protocol/crimson/has-budget'] == "yes")?true:false;
		
		var budgetAlreadyCreated = false;

		if (answers['/protocol/budget-created'] == "y" || answers['/protocol/crimson/has-budget'] == "yes"){
			budgetAlreadyCreated = true;
		}
		
		if (needsBudget === true) {
			
			if (budgetAlreadyCreated === true){
				jQuery(".question-budget-builder-created").removeClass(
				'form-question-hidden')
				.removeClass('form-question-visible').addClass(
						'form-question-visible');
				if (hasCrimsonBudget){
					if (answers['/protocol/budget/need-budget-in-clara'] == "y"){
						jQuery(".question-budget-builder-openlink").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-visible');
						
						jQuery(".question-budget-crimson-openlink").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-hidden');
					} else {
						jQuery(".question-budget-crimson-openlink").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-visible');
						
						jQuery(".question-budget-builder-openlink").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-hidden');
					}
				} else {
					jQuery(".question-budget-builder-openlink").removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(
							'form-question-visible');
					
					jQuery(".question-budget-crimson-openlink").removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(
							'form-question-hidden');
				}
				

				jQuery(".question-budget-builder-no").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-hidden');
				
				//jQuery(".question-budget-builder-yes").removeClass(
				//'form-question-hidden')
				//.removeClass('form-question-visible').addClass(
				//		'form-question-hidden');
				
				jQuery(".question-budget-builder-yes").removeClass(
				'form-question-hidden')
				.removeClass('form-question-visible').addClass(
						'form-question-visible');
			
			
				jQuery(".budget-determination-answer").removeClass(
						'budget-determination-answer-no').addClass(
						'budget-determination-answer-yes');
			} else {
				jQuery(".question-budget-builder-yes").removeClass(
				'form-question-hidden')
				.removeClass('form-question-visible').addClass(
						'form-question-visible');

				jQuery(".question-budget-builder-no").removeClass(
						'form-question-hidden')
						.removeClass('form-question-visible').addClass(
								'form-question-hidden');
				
				jQuery(".question-budget-builder-created").removeClass(
				'form-question-hidden')
				.removeClass('form-question-visible').addClass(
						'form-question-hidden');
			
			
				jQuery(".budget-determination-answer").removeClass(
						'budget-determination-answer-no').addClass(
						'budget-determination-answer-yes');
			}
			
		} else {
			jQuery(".question-budget-builder-yes").removeClass(
			'form-question-hidden')
			.removeClass('form-question-visible').addClass(
					'form-question-hidden');

			jQuery(".question-budget-builder-no").removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(
							'form-question-visible');
			
			jQuery(".question-budget-builder-created").removeClass(
			'form-question-hidden')
			.removeClass('form-question-visible').addClass(
					'form-question-hidden');
		
		
			jQuery(".budget-determination-answer").removeClass(
					'budget-determination-answer-yes').addClass(
					'budget-determination-answer-no');
		}
	}
}));

// SUBJECTS

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-total-accural-goal-for-all-sites' ],
	dependantQuestionPaths : [ "/protocol/sites/single-or-multi" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/sites/single-or-multi'] == "multiple-sites") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-pregnant-excluded-justification' ],
	dependantQuestionPaths : [ "/protocol/subjects/is-pregnant-excluded" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/subjects/is-pregnant-excluded'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-disease-ontology' ],
//	dependantQuestionPaths : [ "/protocol/subjects/is-registered-at-researchmatch" ],
//	execute : function(answers) {
//		var hide = true;
//
//		if (answers['/protocol/subjects/is-registered-at-researchmatch'] == "y") {
//			hide = false;
//		}
//
//		this.hide(hide);
//
//	}
//}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-non-english-speaking-excluded-justification' ],
	dependantQuestionPaths : [ "/protocol/subjects/is-non-english-speaking-exluded" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/subjects/is-non-english-speaking-exluded'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));


Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-subject-population' ],
			dependantQuestionPaths : [ "/protocol/study-nature" ],
			execute : function(answers) {

				var hide = true;

				if ( answers['/protocol/study-nature'] == "social-behavioral-education") {
					hide = false;
				} else if (answers['/protocol/study-nature'] == 'chart-review') {
					hide = true;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ // SUBJECTS
				               'question-vulnerable-populations-included',
				               'question-vulnerable-populations-targeted',
				               'question-is-pregnant-exluded',
				               'question-is-non-english-speaking-exluded',
				               'question-subjects-identified',
				               'question-advertising-type',
				               'question-compensation-provided-to-subjects' ],
			dependantQuestionPaths : [ "/protocol/study-nature" ],
			execute : function(answers) {

				var hide = false;

				if ( answers['/protocol/study-nature'] == "hud-use" ||  answers['/protocol/study-nature'] == "chart-review" ) {
					hide = true;
				}

				this.hide(hide);

			}
		}));

//Clara.ProtocolForm.Rules
//.addRule(new Clara.ProtocolForm.Rule(
//		{
//			id : Ext.id(),
//			questionIds : [ 'question-inclusion-exclusion-criteria-for-this-study' ],
//			dependantQuestionPaths : [ "/protocol/subjects/more-than-one-sef-of-inclusion-exclusion-criteria" ],
//			execute : function(answers) {
//
//				var hide = true;
//
//				if ( answers['/protocol/subjects/more-than-one-sef-of-inclusion-exclusion-criteria'] == "n"  ) {
//					hide = false;
//				}
//
//				this.hide(hide);
//
//			}
//		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-method-and-amount-of-compensation' ],
			dependantQuestionPaths : [ "/protocol/subjects/compensation-provided-to-subjects" ],
			execute : function(answers) {

				var hide = true;

				if ( answers['/protocol/subjects/compensation-provided-to-subjects'] == "y" ) {
					hide = false;
				}

				this.hide(hide);

			}
		}));

// APPENDIX A Pregnant Women, Fetuses
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-pregnant-women-studies-for-assessing-risks',
							'question-pregnant-women-direct-benefit',
							'question-pregnant-wormen-least-possibel-for-achiveving',
							'question-fetus-risk' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'pregnant-women', 'fetuses' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX B (Neonates)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					questionIds : [ 'question-neonates-determine-person',
							'question-neonates-studies-for-assessing-risks',
							'question-neonates-viability' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'neonates' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-neonates-uncertain-viability-risk' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/neonates/viability" ],
					execute : function(answers) {
						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'neonates' ])
								&& answers['/protocol/subjects/vulnerable-populations/neonates/viability'] == "uncertain-viability"
								|| answers['/protocol/subjects/vulnerable-populations/neonates/viability'] == "both") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					questionIds : [ 'question-neonates-nonviable-research-inclusion' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included","/protocol/subjects/vulnerable-populations/neonates/viability" ],
					execute : function(answers) {
						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'neonates' ])
								&& answers['/protocol/subjects/vulnerable-populations/neonates/viability'] == "nonviable"
								|| answers['/protocol/subjects/vulnerable-populations/neonates/viability'] == 'both') {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX D (Prisoners)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-prisoners-involvement-categories',
							'question-prisoners-describe-facility',
							'question-prisoners-advantages-to-participating-prisoners',
							'question-prisoners-plans-to-avoid-undue-influence',
							'question-prisoners-similar-risk',
							'question-prisoners-selection',
							'question-prisoners-followup-exams',
							'question-prisoners-safeguards-to-provide-assureances' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'prisoners' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX C (Children)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-children-risk-category',
					        'question-present-with-child',
							'question-children-is-private-info-shared',
							'question-children-wards-of-participants' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'children' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-children-justify-risk-by-anticipated-benefit',
							'question-children-anticipated-benefit-relation-to-risk' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/children/risk-category" ],
					execute : function(answers) {
						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'children' ])
								&& answers['/protocol/subjects/vulnerable-populations/children/risk-category'] == "more-than-min-risk-has-direct-benefit") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-children-risk-minor-increase',
							'question-children-intervention-or-procedure',
							'question-children-intervention-or-procedure-disorder' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/children/risk-category" ],
					execute : function(answers) {
						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'children' ])
								&& answers['/protocol/subjects/vulnerable-populations/children/risk-category'] == "more-than-min-risk-has-no-direct-benefit") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX E (Non-English Speaking)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-non-english-speaking-languages',
							'question-list-translators',
							'question-translation-services-recruitmentconsent',
							'question-translation-services-duration',
							'question-emergency-contacts' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'non-english-speaking' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX F (Cognitively Impaired)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-cognitively-impaired-range-participant-impairment',
							'question-cognitively-impaired-obtain-assent',
							'question-cognitively-impaired-ensuring-ongoing-consent',
							'question-cognitively-impaired-category' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'cognitively-impaired' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-cognitively-impaired-anticipated-benefits' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/cognitively-impaired/risk-category", 
					                           "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'cognitively-impaired' ])
								&&  answers['/protocol/subjects/vulnerable-populations/cognitively-impaired/risk-category'] == "greater-than-min-risk-with-direct-benefit") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-cognitively-impaired-greater-than-min-risk-without-direct-benefit-explain' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/cognitively-impaired/risk-category" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'cognitively-impaired' ])
								&& answers['/protocol/subjects/vulnerable-populations/cognitively-impaired/risk-category'] == "greater-than-min-risk-without-direct-benefit") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// APPENDIX G (Research in International Settings)
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-international-sites-info',
					        'question-ethics-board-or-irb',
							'question-address-cultural-differences',
							'question-local-exceptions-to-consent',
							'question-benefits-to-local-community',
							'question-researcher-experience',
							'question-info-of-not-affiliated-consultants',
							'question-communication-oversight-plans',
							'question-procedures-storage-data',
							'question-procedures-standard' ],
					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/included" ],
					execute : function(answers) {

						var hide = true;

						if (this
								.doesAnswerContains(
										answers['/protocol/subjects/vulnerable-populations/included'],
										[ 'research-subjects-in-international-setting' ])) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

//Clara.ProtocolForm.Rules
//		.addRule(new Clara.ProtocolForm.Rule(
//				{
//					id : Ext.id(),
//					questionIds : [ 'question-procedures-standard',
//							'question-provisions-emergency-treatment' ],
//					dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/research-in-international-settings/has-medical-procedures" ],
//					execute : function(answers) {
//						var hide = true;
//
//						if (this
//								.doesAnswerContains(
//										answers['/protocol/subjects/vulnerable-populations/included'],
//										[ 'research-subjects-in-international-setting' ])
//								&& answers['/protocol/subjects/vulnerable-populations/research-in-international-settings/has-medical-procedures'] == "y") {
//							hide = false;
//						}
//
//						this.hide(hide);
//
//					}
//				}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-contact-information-for-board' ],
			dependantQuestionPaths : [ "/protocol/subjects/vulnerable-populations/research-in-international-settings/ethics-board-or-irb" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/subjects/vulnerable-populations/research-in-international-settings/ethics-board-or-irb'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

// HIPAA
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-phi', 'question-hipaa-phi-details' ],
	dependantQuestionPaths : [ "/protocol/hipaa/is-phi-obtained" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/hipaa/is-phi-obtained'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-phi-accessed',
	                'question-existing-hipaa-phi-details',
	                'question-hipaa-access-existing-phi-data-source' ],
	dependantQuestionPaths : [ "/protocol/hipaa/access-existing-phi" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/hipaa/access-existing-phi'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-disclose-data-list' ],
					dependantQuestionPaths : [ "/protocol/hipaa/will-disclose-limited-data-set" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/hipaa/will-disclose-limited-data-set'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-requesting-hipaa-waiver-type',
			'question-phi-involved-info', 'question-reason-to-access-phi',
			'question-min-necessary-informaiton-required',
			'question-no-more-than-minimal-risk-to-privacy',
			'question-identifiers-protection-plan',
			'question-identifiers-destroyed', 'question-reason-for-waiver',
			'question-phi-investigatorAssurance' ],
	dependantQuestionPaths : [ "/protocol/hipaa/is-requesting-hipaa-waiver" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/hipaa/is-requesting-hipaa-waiver'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-phi-destroyed-plan' ],
					dependantQuestionPaths : [ "/protocol/hipaa/is-requesting-hipaa-waiver", 
					                           "/protocol/hipaa/is-requesting-hipaa-waiver/y/will-identifiers-be-destroyed" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/hipaa/is-requesting-hipaa-waiver'] == "y" 
							&& answers['/protocol/hipaa/is-requesting-hipaa-waiver/y/will-identifiers-be-destroyed'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-phi-justicication-identifiers' ],
					dependantQuestionPaths : [ "/protocol/hipaa/is-requesting-hipaa-waiver", 
					                           "/protocol/hipaa/is-requesting-hipaa-waiver/y/will-identifiers-be-destroyed" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/hipaa/is-requesting-hipaa-waiver'] == "y" 
							&& answers['/protocol/hipaa/is-requesting-hipaa-waiver/y/will-identifiers-be-destroyed'] == "n") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

// Consent
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-request-waiver-of-consent-documentation' ],
	dependantQuestionPaths : [ "/protocol/consent/processes/included" ],
	execute : function(answers) {

		var hide = true;

		if (this.doesAnswerContains(
				answers['/protocol/consent/processes/included'], [
				        'assent-process',
						'parental-permission-process',
						'information-consent-process' ])) {
			hide = false;
		}

		this.hide(hide);

	}
}));

// APPENDIX M2 Waiver of Consent Documentation
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-fda-regulations', 'question-waiver-or-consent-documentation-risk-procedures' ],
	dependantQuestionPaths : [ "/protocol/consent/processes/included", "/protocol/consent/request-waiver-of-consent-documentation" ],
	execute : function(answers) {

		var hide = true;

		if (this.doesAnswerContains(
				answers['/protocol/consent/processes/included'], [
				                          				        'assent-process',
				                          						'parental-permission-process',
				                          						'information-consent-process' ]) && answers['/protocol/consent/request-waiver-of-consent-documentation'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{					
			questionIds : [ 'question-waiver-or-consent-documentation-risk-procedures-nono' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/request-waiver-of-consent-documentation",
			                           "/protocol/consent/processes/waiver-or-consent-documentation/greater-than-minimal-risk",
			                           "/protocol/consent/processes/waiver-or-consent-documentation/involve-procedures-require-consent" ],
			execute : function(answers) {
				var hide = true;

				if (this.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'assent-process',
            						'parental-permission-process',
              						'information-consent-process'  ]) 
              			&& answers['/protocol/consent/request-waiver-of-consent-documentation'] == "y"
						&& answers['/protocol/consent/processes/waiver-or-consent-documentation/greater-than-minimal-risk'] == "n"
						&& answers['/protocol/consent/processes/waiver-or-consent-documentation/involve-procedures-require-consent'] == "n"
						) {
					hide = false;
				}
				
				
				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-consent-links-participant-and-research' ],
					dependantQuestionPaths : [ "/protocol/consent/processes/included",
					                           "/protocol/consent/request-waiver-of-consent-documentation",
					                           "/protocol/consent/processes/waiver-or-consent-documentation/is-subject-to-fda-regulations",
					                           "/protocol/consent/processes/waiver-or-consent-documentation/greater-than-minimal-risk",
					                           "/protocol/consent/processes/waiver-or-consent-documentation/involve-procedures-require-consent" ],
					execute : function(answers) {
						var hide = true;							
												
						if (this.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'assent-process',
          						'parental-permission-process',
          						'information-consent-process' ])
          					&& answers['/protocol/consent/request-waiver-of-consent-documentation'] == "y"
							&& answers['/protocol/consent/processes/waiver-or-consent-documentation/is-subject-to-fda-regulations'] == "n"
							&& (answers['/protocol/consent/processes/waiver-or-consent-documentation/greater-than-minimal-risk'] != "n"
							|| answers['/protocol/consent/processes/waiver-or-consent-documentation/involve-procedures-require-consent'] != "n")) {
							hide = false;
						}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-consent-links-participant-and-research-yesyes' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/request-waiver-of-consent-documentation",
			                           "/protocol/consent/processes/waiver-or-consent-documentation/is-subject-to-fda-regulations",
			                           "/protocol/consent/processes/waiver-or-consent-documentation/privacy/consent-links-participant-and-research",
			                           "/protocol/consent/processes/waiver-or-consent-documentation/privacy/harm-caused-by-confidentiality-breach"],
			execute : function(answers) {
				var hide = true;						
														
				if (this
						.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'assent-process',
	          						'parental-permission-process',
	          						'information-consent-process' ]) 
	          			&& answers['/protocol/consent/request-waiver-of-consent-documentation'] == "y"
						&& answers['/protocol/consent/processes/waiver-or-consent-documentation/is-subject-to-fda-regulations'] == "n"
						&& answers['/protocol/consent/processes/waiver-or-consent-documentation/privacy/consent-links-participant-and-research'] == "y"
						&& answers['/protocol/consent/processes/waiver-or-consent-documentation/privacy/harm-caused-by-confidentiality-breach'] == "y") {
					hide = false;
				}

				this.hide(hide);

	}
}));

// APPENDIX M1 Waiver or Alteration of Consent Process
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-waiver-or-alteration-of-consent-process-fda-regulations' ],
	dependantQuestionPaths : [ "/protocol/consent/processes/included" ],
	execute : function(answers) {

		var hide = true;

		if (this
				.doesAnswerContains(
						answers['/protocol/consent/processes/included'],
						[ 'none' ])) {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-subject-to-government-approval' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations" ],
			execute : function(answers) {
				var hide = true;

				if (this
						.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'none' ])){
					if (answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations'] == "n"){
						
						hide = false;
					}	
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-consent-reason-for-no-more-than-minimial-risk',
			                'question-no-adversary-affect-to-participants',
			                'question-consent-reason-for-waiver-or-alteration',
			                'question-consent-is-pertinent-information-provided' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations/n/is-subject-to-government-approval" ],
			execute : function(answers) {
				var hide = true;

				if (this
						.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'none' ])){
					if (answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations'] == "n" 
						&& answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations/n/is-subject-to-government-approval'] == "n" ){
						
						hide = false;
					}	
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-reason-for-waiver-or-alteration' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations/n/is-subject-to-government-approval" ],
			execute : function(answers) {
				var hide = true;

				if (this
						.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'none' ]) 
						&& answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations'] == "n"
						&& answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations/n/is-subject-to-government-approval'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-consent-provider',
			                'question-location-of-consent-process', 
			                'question-consent-discussion-time', 
			                'question-consent-waiting-period',
			                'question-how-to-undue-influence',
			                'question-other-tools',
			                'question-children-continue' ],
			dependantQuestionPaths : [ "/protocol/consent/processes/included",
			                           "/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations" ],
			execute : function(answers) {
				var hide = false;

				if (this
						.doesAnswerContains(
								answers['/protocol/consent/processes/included'],
								[ 'none' ])
								&& answers['/protocol/consent/processes/waiver-or-alteration-of-consent-process/consent-process-fda-regulations'] == "n") {
					
					hide = true;
				}

				this.hide(hide);

			}
		}));

// Appendix E for Consent
//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [
//'question-consent-nonenglish-speaking-languages',
//'question-consent-translation-investigator',
//'question-consent-translation-services-recruitmentconsent',
//'question-consent-translation-services-duration',
//'question-consent-provisions-emergencycontacts'],
//	dependantQuestionPaths : [ "/protocol/consent/processes/included" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (this.doesAnswerContains(
//				answers['/protocol/consent/processes/included'], ['translated-consent-assent-forms']
//				)) {
//			hide = false;
//		}
//
//		this.hide(hide);
//
//	}
//}));


// Monitoring
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-describe-specific-data',
	                'question-who-is-monitoring',
	                'question-dsmb-review-frequency',
	                'question-procedure-for-communication' ],
	dependantQuestionPaths : [ "/protocol/monitoring/is-dsmp-in-place" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/monitoring/is-dsmp-in-place'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-data-and-safety-monitor-desc' ],
	dependantQuestionPaths : [ "/protocol/monitoring/is-dsmp-in-place",
	                           "/protocol/monitoring/is-dsmp-in-place/y/who-is-monitoring" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/monitoring/is-dsmp-in-place'] == "y" 
			&& answers['/protocol/monitoring/is-dsmp-in-place/y/who-is-monitoring'] == "data-and-safety-monitor") {
			hide = false;
		}

		this.hide(hide);

	}
}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	id : Ext.id(),
//	questionIds : [ 'question-external-review-bodies' ],
//	dependantQuestionPaths : [ "/protocol/monitoring/is-monitored-externally" ],
//	execute : function(answers) {
//		var hide = true;
//
//		if (answers['/protocol/monitoring/is-monitored-externally'] == "y") {
//			hide = false;
//		}
//
//		this.hide(hide);
//
//	}
//}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [
'quesiton-websites-url-list'],
	dependantQuestionPaths : [ "/protocol/subjects/types-of-advertising/type" ],
	execute : function(answers) {

		var hide = true;

		if (this.doesAnswerContains(
				'websites',answers['/protocol/subjects/types-of-advertising/type']
				) || this.doesAnswerContains(
				'social-media',answers['/protocol/subjects/types-of-advertising/type']
				)) {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [
'question-advertised-criteria'],
	dependantQuestionPaths : [ "/protocol/subjects/types-of-advertising/type", "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = true;

		if (!this.doesAnswerContains(
				'na',answers['/protocol/subjects/types-of-advertising/type']
				) && answers['/protocol/subjects/types-of-advertising/type'] && (answers['/protocol/study-nature'] != "hud-use" &&  answers['/protocol/study-nature'] != "chart-review")) {
			hide = false;
		}

		this.hide(hide);

	}
}));

// Risks
Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-types-of-specimens' ],
					dependantQuestionPaths : [ "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
					                           "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected" ],
					execute : function(answers) {

						var hide = true;

						if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
							&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-types-of-specimens-blood' ],
					dependantQuestionPaths : [
					        "/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected",
							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected/y/types-of-specimens/type" ],
					execute : function(answers) {

						var hide = true;

						if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"
							&& answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected'] == "y"
							&& this.doesAnswerContains(
									'blood',answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/will-specimens-be-collected/y/types-of-specimens/type']
												)) {
							hide = false;
						}

						this.hide(hide);

					}
				}));

//Clara.ProtocolForm.Rules
//		.addRule(new Clara.ProtocolForm.Rule(
//				{
//					id : Ext.id(),
//					questionIds : [ 'question-specimen-identification-coded-has-phi' ],
//					dependantQuestionPaths : [
//							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
//							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification" ],
//					execute : function(answers) {
//						var hide = true;
//
//						if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y"){
//							if (this
//									.doesAnswerContains(
//											'coded',answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification']
//											)){
//								hide = false;
//							}
//					
//						}
//
//						this.hide(hide);
//
//					}
//				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-radiation-exceed-standard-of-care' ],
					dependantQuestionPaths : [ "/protocol/misc/radiation-safety/involve-the-use-of-radiation" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/misc/radiation-safety/involve-the-use-of-radiation'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-nature-of-study-exercise' ],
			dependantQuestionPaths : [ "/protocol/misc/radiation-safety/involve-use-of-strenuous-exercise" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/misc/radiation-safety/involve-use-of-strenuous-exercise'] == "y") {
					hide = false;
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-specimen-identification-de-identify-process' ],
					dependantQuestionPaths : [
							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
							"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y") {
							if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification'] == "de-identified" || 
									answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification'] == "coded"){
								hide = false;
							}
							
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-specimen-neither-explain' ],
			dependantQuestionPaths : [
					"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained",
					"/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification" ],
			execute : function(answers) {
				var hide = true;

				if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained'] == "y") {
					if (answers['/protocol/risks/storage-of-biological-materials/will-specimens-be-obtained/y/specimen-identification'] == "neither"){
						hide = false;
					}
					
				}

				this.hide(hide);

			}
		}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-regulated-toxins-list' ],
					dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/misc/biosafety/bio-hazard-materials/material" ],
					execute : function(answers) {

						var hide = true;

						if (answers['/protocol/site-responsible'] != "ach-achri" && this.doesAnswerContains('regulated-toxins',answers['/protocol/misc/biosafety/bio-hazard-materials/material'])) {
							clog("HAS REG TOXIN RULE");
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-genetic-research-type', 'question-types-of-genes',
			'question-genes-cause-hereditary-diseases',
			'question-gene-testing-purpose',
			'question-genetic-testing-clinically-available',
			'question-genetic-testing-clinically-available-2',
			'question-genetic-testing-exist-incidential-findings',
			'question-genetic-testing-exist-incidential-findings-2',
			'question-inform-participants-testing-results',
			'question-inform-participants-testing-results-2',
			'question-genetic-testing-provide-conseling',
			'question-inform-participants-new-developments',
			'question-inform-participants-new-developments-2',
			'question-inform-participants-involve-family-member' ],
	dependantQuestionPaths : [ "/protocol/risks/genetic-testing/is-planned" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/risks/genetic-testing/is-planned'] == "y") {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-genetic-therapy-type' ],
	dependantQuestionPaths : [ "/protocol/risks/genetic-testing/is-planned",
	                           "/protocol/risks/genetic-testing/is-planned/y/genetic-research-type" ],
	execute : function(answers) {
		var hide = true;

		if (answers['/protocol/risks/genetic-testing/is-planned'] == "y" && this.doesAnswerContains(answers['/protocol/risks/genetic-testing/is-planned/y/genetic-research-type'],['gene-therapy-research'])) {
			hide = false;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-inform-participants-testing-results-plan',
							'question-inform-participants-testing-results-opt-out-procedures',
							'question-inform-participants-testing-results-clinical-significance',
							'question-inform-participants-testing-results-implications-for-others'],
					dependantQuestionPaths : [ "/protocol/risks/genetic-testing/is-planned","/protocol/risks/genetic-testing/is-planned/y/will-inform-participants-testing-results" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/risks/genetic-testing/is-planned'] == "y"
								&& answers['/protocol/risks/genetic-testing/is-planned/y/will-inform-participants-testing-results'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-provide-conseling-counselor-and-qualifications',
							'question-provide-conseling-cost-participants' ],
					dependantQuestionPaths : [
							"/protocol/risks/genetic-testing/is-planned",
							"/protocol/risks/genetic-testing/is-planned/y/will-provide-conseling" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/risks/genetic-testing/is-planned'] == "y"
								&& answers['/protocol/risks/genetic-testing/is-planned/y/will-provide-conseling'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [
							'question-inform-participants-involve-family-member-readily-identifiable',
							'question-involve-family-members-require-private-information' ],
					dependantQuestionPaths : [
							"/protocol/risks/genetic-testing/is-planned",
							"/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/risks/genetic-testing/is-planned'] == "y"
								&& answers['/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules
		.addRule(new Clara.ProtocolForm.Rule(
				{
					id : Ext.id(),
					questionIds : [ 'question-methods-form-recruiting-family-members' ],
					dependantQuestionPaths : [
							"/protocol/risks/genetic-testing/is-planned",
							"/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members/y/readily-identifiable",
							"/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members/y/will-require-private-information" ],
					execute : function(answers) {
						var hide = true;

						if (answers['/protocol/risks/genetic-testing/is-planned'] == "y"
								&& answers['/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members/y/readily-identifiable'] == "y"
								&& answers['/protocol/risks/genetic-testing/is-planned/y/will-involve-family-members/y/will-require-private-information'] == "y") {
							hide = false;
						}

						this.hide(hide);

					}
				}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-examine-disease-or-condition' ],
	dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/misc/is-registered-at-trialsearch" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/misc/is-registered-at-trialsearch'] == 'y') {
			hide = false;
		} 

		this.hide(hide);
	}
}));


// show condition panel when it's uams, "social-behavioral-education" and will registered at trialsearch
/*
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	id : Ext.id(),
	questionIds : [ 'question-condition-ontology' ],
	dependantQuestionPaths : [ "/protocol/misc/is-registered-at-trialsearch", "/protocol/site-responsible", '/protocol/study-nature' ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/study-nature'] == "social-behavioral-education" && answers['/protocol/misc/is-registered-at-trialsearch'] == 'y') {
			hide = false;
		}

		this.hide(hide);
	}
}));
*/
Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-inclusion-exclusion-criteria-for-this-study' ],
	dependantQuestionPaths : [ "/protocol/site-responsible", "/protocol/study-nature", "/protocol/misc/is-registered-at-trialsearch" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams' && answers['/protocol/study-nature'] == "biomedical-clinical" && answers['/protocol/misc/is-registered-at-trialsearch'] == 'y') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	questionIds : [ 'question-re-consent-process' ],
//	dependantQuestionPaths : [ "/protocol/consent/provider" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (this.doesAnswerContains(answers['/protocol/consent/provider'],['parents-and-or-guardian'])) {
//			hide = false;
//		} 
//
//		this.hide(hide);
//	}
//}));

//Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
//	questionIds : [ 'question-justify-no-need-re-consent' ],
//	dependantQuestionPaths : [ "/protocol/consent/provider", "/protocol/consent/provider/parents-and-or-guardian/re-consent-process" ],
//	execute : function(answers) {
//
//		var hide = true;
//
//		if (this.doesAnswerContains(answers['/protocol/consent/provider'],['parents-and-or-guardian']) && answers['/protocol/consent/provider/parents-and-or-guardian/re-consent-process'] == 'n') {
//			hide = false;
//		} 
//
//		this.hide(hide);
//	}
//}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-is-registered-at-trialsearch'],
	dependantQuestionPaths : [ "/protocol/site-responsible" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'uams') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-cancer-study' ],
	dependantQuestionPaths : [ "/protocol/site-responsible",'/protocol/study-nature' ],
	execute : function(answers) {

		var hide = true;
		//&& answers['/protocol/study-nature'] == 'biomedical-clinical' doesn't look like it's only biomedical studies...
		if (answers['/protocol/site-responsible'] == 'uams') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-name-of-company' ],
	dependantQuestionPaths : [ "/protocol/contract/transfer-to-foreign-entity" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/contract/transfer-to-foreign-entity'] == 'y') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-uams-faculty' ],
	dependantQuestionPaths : [ "/protocol/site-responsible" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/site-responsible'] == 'ach-achri') {
			hide = false;
		} 

		this.hide(hide);
	}
}));


Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [ 'question-test-intervention',
	                'question-is-trial-well-supported',
	                'question-duplicate-existing-studies',
	                'question-trial-design-appropriate-to-answer',
	                'question-trial-sponsored-by-organization',
	                'question-trial-in-compliance',
	                'question-all-aspects-according-to-standards' ],
	dependantQuestionPaths : [ "/protocol/site-responsible",
	                           "/protocol/budget/potentially-billed",
	                              "/protocol/budget/involves/uams-clinics",
	                              "/protocol/budget/involves/uams-inpatient-units",
	                              "/protocol/budget/involves/uams-ss-ou",
	                              "/protocol/budget/involves/uams-clinicallab",
	                              "/protocol/budget/involves/uams-radiology",
	                              "/protocol/budget/involves/uams-pharmacy",
	                              "/protocol/budget/involves/uams-other",
	                              "/protocol/budget/involves/uams-supplies",
	                              "/protocol/budget/involves/fgp-fees",
	                              "/protocol/budget/involves/industry-support",
	                           "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = true;

		var needsBudgetPaths = ["/protocol/budget/potentially-billed",
	                              "/protocol/budget/involves/uams-clinics",
	                              "/protocol/budget/involves/uams-inpatient-units",
	                              "/protocol/budget/involves/uams-ss-ou",
	                              "/protocol/budget/involves/uams-clinicallab",
	                              "/protocol/budget/involves/uams-radiology",
	                              "/protocol/budget/involves/uams-pharmacy",
	                              "/protocol/budget/involves/uams-other",
	                              "/protocol/budget/involves/uams-supplies",
	                              "/protocol/budget/involves/fgp-fees",
	                              "/protocol/budget/involves/industry-support"];
		
		var needsBudget = false;
		
		for (var i=0,l=needsBudgetPaths.length;i<l;i++){
			needsBudget = needsBudget || (answers[needsBudgetPaths[i]] == "y");
		}

		if (answers['/protocol/site-responsible'] != 'ach-achri' && needsBudget && answers['/protocol/study-nature'] != 'hud-use') {
			hide = false;
		} 

		this.hide(hide);
	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [
	               // RISKS
	               'question-research-records',
	               'question-genetic-testing-is-planned',
	               // MISC
	               'question-is-registered-at-trialsearch',
	               'question-cancer-study',
	               'question-biosafety-bio-hazard-materials-material',
	               'question-radiation-safety-involve',
	               'question-involve-use-of-strenuous-exercise'
 ],
	dependantQuestionPaths : [ "/protocol/study-nature" ],
	execute : function(answers) {

		var hide = false;

		if (answers['/protocol/study-nature'] == 'chart-review') {
			hide = true;
		}

		this.hide(hide);

	}
}));

Clara.ProtocolForm.Rules.addRule(new Clara.ProtocolForm.Rule({
	questionIds : [
	               'question-nct-number'
 ],
	dependantQuestionPaths : [ "/protocol/misc/is-registered-at-clinical-trials" ],
	execute : function(answers) {

		var hide = true;

		if (answers['/protocol/misc/is-registered-at-clinical-trials'] == 'y') {
			hide = false;
		}

		this.hide(hide);

	}
}));


