jQuery.expr[':'].icontains = function(obj, index, meta, stack) {
	return (obj.textContent || obj.innerText || jQuery(obj).text() || '')
	.toLowerCase().indexOf(meta[3].toLowerCase()) >= 0;
};

function clearSearch() {
	jQuery("#summary-content div").show();
	renderPermissions();
}
function renderPermissions(){
	if (!claraInstance.HasAnyPermissions(["EDIT_STUDY"])) {
		jQuery(".tools-edit-page").hide();
		jQuery(".tools-locked-page").show();
	} else {
		jQuery(".tools-edit-page").show();
		jQuery(".tools-locked-page").hide();
	}
	if (getUrlVars()["review"] == "false") {
		clog("NOT REVIEWING, NO EDIT ALLOWED");
		jQuery(".tools-edit-page").hide();
		jQuery(".tools-locked-page").show();
	} else if (isLocked){	// cant use two ampersands for AND because JSPX hates us all
		if (lockedUserId != claraInstance.user.id){
			clog("LOCKED FOR EDITING",isLocked,lockedUserId,claraInstance.user);
			jQuery(".summary-header-locked").show();
			jQuery(".tools-edit-page").hide();
			jQuery(".tools-locked-page").show();
		} else {
			jQuery(".summary-header-locked").hide();
			jQuery(".tools-edit-page").show();
			jQuery(".tools-locked-page").hide();
		}
	}


	// "lock" sections that have questions with class .disable-parent-section
	jQuery(".disable-parent-section").each(function(idx){
		jQuery(this).closest(".tools-edit-page").hide();
		jQuery(this).closest(".tools-locked-page").show();
	});

}

function performSearch(str) {
	jQuery(".summary-section").hide().filter(
			":icontains('" + str + "')").find('.summary-section')
			.andSelf().show();
	jQuery(".summary-row").hide().filter(":icontains('" + str + "')")
	.find('.summary-row').andSelf().show();
}

function switchToEditView(url){
	url += "?noheader=true&committee="+((claraInstance.user.committee)?claraInstance.user.committee:'PI');
	clog(url,claraInstance);

	// if (typeof window.parent.Clara.Reviewer != "undefined") window.parent.Clara.Reviewer.MessageBus.fireEvent("editingform",url);
	window.top.location.href = url;
	//location.href=url;
}

function exitEditView(url){
	window.location.reload();
}

function saveSummaryEditableAnswer(path, windowId){
	
	var inputObject = jQuery(windowId+" input[name=editValueElement]");
	saveToForm = (claraInstance.form && claraInstance.form.id && claraInstance.form.id > 0);
	var fieldVal = inputObject?(inputObject.val()):null;
	clog("fieldVal:",fieldVal);
	var displayVal = fieldVal;

	if (fieldVal == null){
		clog("not an input, trying select");
		fieldVal = jQuery(windowId+" select[name=editValueElement]").val();
		displayVal = jQuery(windowId+" select[name=editValueElement] option:selected").text();
	}

	if (fieldVal == null){
		clog("not an select, trying textarea");
		fieldVal = jQuery(windowId+" textarea[name=editValueElement]").val();
		displayVal = fieldVal;
	}

	var url = appContext + "/ajax/" + claraInstance.type + "s/" + claraInstance.id;
	if (saveToForm) url += "/" + claraInstance.type + "-forms/" + claraInstance.form.id;

	url += "/update-summary";

	var opts = {
			path:path,
			value:fieldVal,
			userId:claraInstance.user.id
	};
	clog("Saving",url,opts);
	jQuery.ajax({
		url:url,
		type: 'POST',
		async:true,
		data:opts,
		success: function(data){
			var rowHTML = (displayVal != '')?("<strong>"+displayVal+"</strong>"):("<span class='muted summary-value-novalue'>Not answered.</span>");
			if (checkAjaxError(null,data,null) == false){
				clog(jQuery("div[id*='"+path+"'].summary-row-editable"), "SHOW: '"+displayVal+"'");
				jQuery("div[id*='"+path+"'].summary-row-editable").html(rowHTML);
				jQuery(windowId).modal("hide");
			}
		},
		error: function(){
			jQuery(windowId).modal("hide");
		}
	});

}


function prepareFormSummaryPage(){

	jQuery(document).ready(function() {

		// First add hidden class to related quesions (if you havent answered a question, its related questions shouldnt show (rulesdont fix this))

		jQuery(".summary-question:has(.summary-row-related)").addClass("form-question-hidden");

		if (claraInstance.type == "protocol"){
			Clara.ProtocolForm.Rules.questionBaseCls = "summary-question";
			Clara.ProtocolForm.Rules.processSummaryPage("summary-question-el");
		} else if (claraInstance.type == "contract"){
			Clara.ContractForm.Rules.questionBaseCls = "summary-question";
			Clara.ContractForm.Rules.processSummaryPage("summary-question-el");
		}

		renderPermissions();

		var pharmacyurl = appContext
		+ "/protocols/"
		+ claraInstance.id
		+ "/protocol-forms/"
		+ claraInstance.form.id
		+ "/pharmacy/pharmacybuilder";

		var budgeturl = appContext
		+ "/protocols/"
		+ claraInstance.id
		+ "/protocol-forms/"
		+ claraInstance.form.id
		+ "/budgets/budgetbuilder";

		if (claraInstance.HasAnyPermissions("EDIT_BUDGET") === false) {
			clog("No EDIT_BUDGET permission found, making readonly URL");
			budgeturl += "?readOnly=true";
		}

		if (isLocked) {
			if (lockedUserId != claraInstance.user.id) {
				pharmacyurl += "?readOnly=true";
			}
		}

		if (hasBudget)
		{
			jQuery("#budget-matrix-link")
			.show()
			.click(
					function() {
						window.open(budgeturl,'','toolbar=no,status=no,width=950,height=650,resizable=yes');
					});
		}
		if (hasPharmacy)
		{
			jQuery("#pharmacy-form-link")
			.show()
			.click(
					function() {
						window.open(pharmacyurl,'','toolbar=no,status=no,width=950,height=650,resizable=yes');
					});
		}

		// Add hidden class to subquestions THAT IMMEDIATELY FOLLOW a hidden question
		jQuery(".form-question-hidden").nextUntil(".summary-question:not(.form-question-hidden)",".summary-subquestion").addClass("form-question-hidden");

		// Remove hidden rows after rules run..
		jQuery(".form-question-hidden").remove();

		jQuery("#search").val("");
		jQuery("#search").keyup(function() {
			var val = jQuery("#search").val();
			clog(val + " calling");
			if (jQuery.trim(val) == "") {
				clearSearch();
			} else {
				performSearch(jQuery("#search").val());
			}
		});


		jQuery(".summary-table-type-combo").each(function(i){
			var types = [];
			var selectEl = jQuery(this);
			jQuery(this).attr('id', "summary-table-type-combo-"+i);
			// sort, eliminate duplicate types

			jQuery(this).children().each(function(){
				var optionHtml = jQuery(this).html();
				if (jQuery.inArray(optionHtml,types) == -1) types.push(optionHtml);
			});

			types.sort();
			clog("types sorted",types);
			jQuery("#summary-table-type-combo-"+i).empty();

			jQuery("#summary-table-type-combo-"+i).append("<option value='*'>All</option>");
			for (var j=0,l=types.length;j<l;j++){
				jQuery("#summary-table-type-combo-"+i).append("<option>"+types[j]+"</option>");
			}

			jQuery("#summary-table-type-combo-"+i).change(function(){
				var str = jQuery(this).val();
				if (str == "*") jQuery(this).parent().siblings("tbody").children("tr").show();
				else {
					jQuery(this).parent().siblings("tbody").children("tr").hide();
					jQuery(this).parent().siblings("tbody").children("tr").children("td[headers='type']").filter(function(){
						return jQuery(this).text().toLowerCase() == str.toLowerCase();
					}).closest("tr").show();
				}
			});

		});


		// Check for previous versions (for protocols only for now)

		if (claraInstance.type == "protocol"){
			var versionUrl = appContext+"/ajax/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/versions-for-comparison";
			jQuery.ajax({
				url:versionUrl,
				async:false,
				success: function(data){
					clog("VERSION AJAX DATA",data);
					var html = "", rowClass="",iconClass="",actionHtml="";
					var versionArray = [];
					jQuery(data).find("form-info").each(function(){
						versionArray.push([jQuery(this).attr('id'),jQuery(this).attr('formType'),jQuery(this).attr('submitted'),jQuery(this).attr('status')]);
					});

					versionArray.sort(function(a,b){
						return parseInt(b[0]) - parseInt(a[0]);
					});

					clog("versionArray",versionArray);

					for (var i=0,l=versionArray.length;i<l;i++){

						if (i==0){
							rowClass="version-current";
							iconClass = "";
							actionHtml="<em style='font-weight:200;'>This form</em>";
						} else if (parseInt(versionArray[i][0]) == parseInt(compareFormA.id)) {
							rowClass="version-compareto info";
							iconClass = "version-compareto-icon";
							actionHtml="<em style='font-weight:200;'>Comparing..</em>";
						} else {
							var url = appContext+"/"+claraInstance.type+"s/"+claraInstance.id+"/"+claraInstance.type+"-forms/"+claraInstance.form.id+"/"+claraInstance.urlName+"/summary?noheader="+getURLParameter("noheader")+"&review="+getURLParameter("review")+"&compareto="+versionArray[i][0];
							actionHtml = "<a href='"+url+"'><strong>Compare</strong></a>";
						}

						html += "<tr class='"+rowClass+"'><td>";
						html += versionArray[i][1].toHumanReadable();
						html += "</td><td>";
						html += moment(versionArray[i][2]).calendar();//.format("MM/dd/yyyy hh:mm a");
						html += "</td><td>";
						html += versionArray[i][3].toHumanReadable();
						html += "</td><td class='"+iconClass+"'>"+actionHtml+"</td>";
						html += "</tr>";
						rowClass="";
						iconClass="";
						actionHtml="";
					}

					if (versionArray.length > 1) {
						jQuery("#nav-compare").show();
						if (compareFormB.id != 0) {
							var compareInfoHtml = "<h4>Comparing <span class=''>"+compareFormB.type.toHumanReadable()+" <span>("+moment(compareFormB.created).calendar()+")</span></span> to <span class=''>"+compareFormA.type.toHumanReadable()+" <span>("+moment(compareFormA.created).calendar()+")</span></span>";
							jQuery("#alert-compare-info").html(compareInfoHtml);
							jQuery("#alert-compare-info").show();
						}
						jQuery("#version-table-body").append(html);
					}

				}
			});
		}

		var isEmbed = window != window.parent;						

		if (!isEmbed) jQuery("a.summary-navbar-title").first().append('<span style="margin-left:16px;"><a href="javascript:history.go(-1);">Go back</a></span>');

		// SHOW THE PAGE NOW!!
		jQuery("#loading").hide();
		jQuery("#summary-content").fadeIn();

	});
}