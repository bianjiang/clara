Ext.ns('Clara.ProtocolForm');

// RULES START HERE
Clara.ProtocolForm.Rules
.addRule(new Clara.ProtocolForm.Rule(
		{
			id : Ext.id(),
			questionIds : [ 'question-involve-change-in' ],
			dependantQuestionPaths : [ "/protocol/modification/to-modify-section/involve-change-in/pi-modified" ],
			execute : function(answers) {
				jQuery('#notify-contract').val('n');
				answers["/protocol/modification/notify-contract"] = "n";
				
				if (answers['/protocol/modification/to-modify-section/involve-change-in/pi-modified'] == "y"
					) {
					
					jQuery('#notify-contract').val('y');
					answers["/protocol/modification/notify-contract"] = "y";
				} else {
					jQuery('#notify-contract').val('n');
					answers["/protocol/modification/notify-contract"] = "n";
				}

			}
		}));

