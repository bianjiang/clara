Ext.ns('Clara.BudgetBuilder');

renderCoversheet = function(onlyShowIds, printOnLoad) {
	var subtotal = 0;
	var total = 0;
	var previousSubtype = '';
	var ea = [];
	
	clog("renderCoversheet() start",onlyShowIds, printOnLoad);

	if (onlyShowIds.length == 0 || (onlyShowIds.length > 0 && jQuery.inArray("funding-sources",onlyShowIds) > -1)){
		clog("Showing funding sources");
		jQuery("#funding-sources").show();
	} else {
		jQuery("#funding-sources").hide();
	}
	
	if (onlyShowIds.length == 0 || (onlyShowIds.length > 0 && jQuery.inArray("expenses",onlyShowIds) > -1)){
		clog("Showing expenses");
		ea = budget.getExpenseArray('Initial Cost'); // budget.getExpenseArray('Initial Cost', true); //they change their mind... 
		//clog("EA", ea);
		clog(ea);

		ea.sort(function(a, b) {
			var nameA = a[2].toLowerCase(), nameB = b[2].toLowerCase();
			if (nameA < nameB) // sort string ascending
				return -1;
			if (nameA > nameB)
				return 1;
			return 0; // default return value (no sorting)
		});
		
		jQuery.each(ea,
				function(i, e) {
					jQuery("#expenses table tbody").append(
							"<tr><td>"
									+ e[4]
									+ "</td><td>"
									+ e[5]
									+ "</td><td>"
									+ Ext.util.Format.usMoney(e[10])
									+ "</td><td>"
									+ e[6]
									+ "</td><td class='totalcost'>"
									+ Ext.util.Format.usMoney(parseFloat(e[10])
											+ (parseFloat(e[10])
													* (parseFloat(e[6])) / 100))
									+ "</td></tr>");
					total += (parseFloat(e[10]) + (parseFloat(e[10])
							* (parseFloat(e[6])) / 100));
				});
		jQuery("#expenses table tbody").append(
				"<tr><td colspan='4'><strong>Total</strong></td><td class='totalcost'><strong>"
						+ Ext.util.Format.usMoney(total) + "</strong></td></tr>");
	
	} else {
		jQuery("#expenses").hide();
	}

	if (onlyShowIds.length == 0 || (onlyShowIds.length > 0 && jQuery.inArray("totals",onlyShowIds) > -1)){
		
		clog("Showing totals");
		
		var armTotals = {
			R : 0,
			C : 0,
			I : 0,
			RNS : 0,
			CNMS : 0,
			CL : 0
		};
	
		var armTotalsPerSubject = {
			R : 0,
			C : 0,
			I : 0,
			RNS : 0,
			CNMS : 0,
			CL : 0
		};
		var arms = budget.getAllArms();
		for ( var i = 0; i < arms.length; i++) {
			var a = arms[i];
			var at = a.getTotals();
			armTotals.R += (at.R + at.R * (budget.FA / 100));
			armTotals.C += (at.C);
			armTotals.I += (at.I + at.I * (budget.FA / 100));
			armTotals.RNS += (at.RNS);
			armTotals.CNMS += (at.CNMS);
			armTotals.CL += (at.CL);
	
		}
	
		cdebug("armTotals", armTotals);
	
		// Budget Totals
		var thtml = "<tr><td>Study Initiation Expenses</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(parseFloat(total)) + "</td></tr>";
		thtml += "<tr><td>Procedure Total</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(parseFloat(armTotals.R)) + "</td></tr>";
		thtml += "<tr><td>Budget Total (Research Costs Only)</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(parseFloat(total)
						+ parseFloat(armTotals.R)) + "</td></tr>";
		thtml += "<tr><td>Total of I</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(armTotals.I) + "</td></tr>";
		thtml += "<tr><td>Total of C</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(armTotals.C) + "</td></tr>";
		thtml += "<tr><td>Total of RNS</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(armTotals.RNS) + "</td></tr>";
		thtml += "<tr><td>Total of CNMS</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(armTotals.CNMS) + "</td></tr>";
		thtml += "<tr><td>Total of CL</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(armTotals.CL) + "</td></tr>";
		thtml += "<tr><td>Total of Others (I+C+RNS+CNMS+CL)</td><td class='totalcost'>"
				+ Ext.util.Format.usMoney(parseFloat(armTotals.C)
						+ parseFloat(armTotals.I) + parseFloat(armTotals.RNS)
						+ parseFloat(armTotals.CNMS) + parseFloat(armTotals.CL))
				+ "</td></tr>";
	
		jQuery("#totals table tbody").append(thtml);

	} else {
		jQuery("#totals").hide();
	}
	
	if (onlyShowIds.length == 0 || (onlyShowIds.length > 0 && jQuery.inArray("invoicables",onlyShowIds) > -1)){
		clog("Showing invoicables");
		ea = budget.getExpenseArray('Invoicable');
		clog("EA", ea);
		subtotal = 0;
		total = 0;
		previousSubtype = '';
	
		ea.sort(function(a, b) {
			var nameA = a[2].toLowerCase(), nameB = b[2].toLowerCase();
			if (nameA < nameB) // sort string ascending
				return -1;
			if (nameA > nameB)
				return 1;
			return 0; // default return value (no sorting)
		});
	
		jQuery
				.each(
						ea,
						function(i, e) {
							clog(e);
							var subtotal = (e[7] == true)?(parseFloat(e[9]) * parseFloat(e[10]))+ ((parseFloat(e[9]) * parseFloat(e[10])) * (parseFloat(e[6])) / 100):(parseFloat(e[9]) * parseFloat(e[10]));
							jQuery("#invoicables table tbody")
									.append(
											"<tr><td>"
													+ e[4]
													+ "</td><td>"
													+ e[5]
													+ "</td><td>"
													+ Ext.util.Format
															.usMoney(e[10])
													+ "</td><td>"
													+ parseInt(e[9])
													+ "</td><td>"
													+ e[6]
													+ "</td><td class='totalcost'>"
													+ Ext.util.Format.usMoney(subtotal)
													+ "</td></tr>");
							total += (subtotal);
						});
		jQuery("#invoicables table tbody").append(
				"<tr><td colspan='5'><strong>Total</strong></td><td class='totalcost'><strong>"
						+ Ext.util.Format.usMoney(total) + "</strong></td></tr>");
	
	} else {
		jQuery("#invoicables").hide();
	}

	if (printOnLoad) { window.print(); }
	
};