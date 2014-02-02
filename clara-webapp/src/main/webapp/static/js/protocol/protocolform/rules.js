Ext.ns('Clara.ProtocolForm');

Clara.ProtocolForm.Rule = function(o){
	this.id=							(o.id || Ext.id());	
	this.questionIds=					(o.questionIds || []);
	this.dependantQuestionPaths=		(o.dependantQuestionPaths || []);
	this.execute=						(o.execute || function(){return true;});
	this.silent=						(o.silent || false);
	this.setCssCls=	function(cls){
		jQuery("#"+questionId).addClass(cls);
	};
	this.hide = (o.hide || function(hide) {
		
		var cls = "form-question-visible";
		if(hide) {
			var cls = "form-question-hidden";
		}
		
		jQuery.each(this.questionIds, function(i, questionId) {
			var qSelector = "#" + questionId;
			jQuery(qSelector).removeClass('form-question-hidden').removeClass(
					'form-question-visible').addClass(cls);
			jQuery(qSelector).closest('.question').removeClass(
					'form-question-hidden')
					.removeClass('form-question-visible').addClass(cls);
		});
	});
	
	this.getIntersect = function(arr1, arr2) {
	    var r = [], o = {}, l = arr2.length, i, v;
	    for (i = 0; i < l; i++) {
	        o[arr2[i]] = true;
	    }
	    l = arr1.length;
	    for (i = 0; i < l; i++) {
	        v = arr1[i];
	        if (v in o) {
	            r.push(v);
	        }
	    }
	    return r;
	};
	
	this.doesAnswerContains = (o.doesAnswerContains || function (answer, checkValues){
		
		array1 = (answer instanceof Array)?answer:[answer];
		array2 = (checkValues instanceof Array)?checkValues:[checkValues];
		
		if(typeof answer == "undefined" || typeof checkValues == "undefined" || answer.length == 0 || checkValues.length == 0) return false;
		
		return (this.getIntersect(array1,array2).length > 0);
		
	});
	
};

Clara.ProtocolForm.Rules = {
		rules: 				[],
		remoteCallSuccess:	false,
		answerCache:		null,
		silent:false,
		questionBaseCls:	'question',
		addRule: 	function(rule){
			this.rules.push(rule);
		},
		processId:			function(id){
			var t = this;
			var rules = t.rules;
			for ( var i = 0; i < rules.length; i++ ) {
				if (jQuery.inArray(id, rules[i].questionIds) > -1){
					rules[i].execute(t.answerCache);
				}
			}
		},
		processSummaryPage: function(questionCls){
			// We may need to do additional processing to summary pages.
			this.processPage(questionCls);
		},
		processPage:		function(questionCls){
			var t = this;
			questionCls = (questionCls == null)?"question-el":questionCls;
			//if (t.silent == false){
				clog("rules.js: Calling processPage("+questionCls+")");
			//}
			
			var rules = t.rules;
			var questionIds = [];
			jQuery("."+questionCls).each(function(){
				var qId = jQuery(this).closest("."+t.questionBaseCls).attr("id");
				if (jQuery.inArray(qId, questionIds) == -1) questionIds.push(qId);
			});
			t.answerCache = this.getAnswersForQuestions(questionIds);	// unnecessary?
			var pageElements = jQuery().clsToObject(questionCls);
			for (attrname in pageElements){
				t.answerCache[attrname] = (pageElements[attrname])?pageElements[attrname]:"";
			}
			
			if (t.silent == false){
				clog("Cache updated with local page answers: ");
				clog(t.answerCache);
			}
			
			jQuery.each(questionIds, function(index, questionId){
				t.processId(questionId);
			});
		},
		getDependencies: 	function(questionId){
			for ( var i = 0; i < this.rules.length; i++ ) {
				if (jQuery.inArray(questionId, this.rules[i].questionIds) > -1){
					return this.rules[i].dependantQuestionPaths;
				}
			};
			return [];
		},
		getAnswersForQuestions: function(questionIds){
			var t = this;
			var allPaths = [];
			var nearestQuestionId = "";
			jQuery.each(questionIds, function(qindex, qvalue){
				nearestQuestionId = jQuery("#"+qvalue).closest("."+t.questionBaseCls).attr("id");
				jQuery.each(t.getDependencies(nearestQuestionId),function(dindex, dvalue){
					if ( jQuery.inArray(dvalue,allPaths) == -1 ){
						allPaths.push(dvalue);
					}
				});
			});
			if (allPaths.length > 0 && t.remoteCallSuccess==false){
				if (t.silent == false){
					clog("About to call AJAX for paths:");
					clog(allPaths);
				}
				return t.getAnswersForPaths(allPaths);
			} else if (t.remoteCallSuccess) {
				if (t.silent == false) clog("No AJAX call needed. Returning answer cache: ");
				return t.answerCache;
			} else {
				return [];
			}
		},
		getAnswersForPaths: function(paths){
			if (paths.length == 0) return {};
			var t = this;

			var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/xml-elements/listValues";
			jQuery.ajax({
				type: 'POST',
				url: url,
				async:false,
				data: {listPaths: paths},
				success: function(data){
					t.answerCache = data;
				}
			});
			
			t.remoteCallSuccess = true;
			return t.answerCache;
		}
		
};

//for older pages
function processDependencies(a,b){
	clog("[DEPRECATED] rules.js: processDependencies() has been replaced with Clara.ProtocolForm.Rules.processPage()");
	Clara.ProtocolForm.Rules.processPage();
}