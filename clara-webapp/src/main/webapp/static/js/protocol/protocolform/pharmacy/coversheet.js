Ext.ns('Clara.Pharmacy');

renderDescription = function(e){
	return e[3];
};

renderCoversheet = function() {
	clog("renderCoversheet() start");

	expenses = pharmacy.getArray();
	
	
	expenses.sort(function(a, b) {
		return (a[0] < b[0]); 
	});
	
	
	
	
	clog(expenses);
	var subtotal = 0;
	var total = 0;
	var previousSubtype = '';
	var note = "";
	var moreInfo = "";
	var pe = null;
	
	jQuery.each(expenses,
			function(i, e) {
				pe = pharmacy.getExpense(e[0]);
				clog("EXPENSE",e, pe);
				
				if (pe.drugs && pe.drugs.length > 0){
					moreInfo += "<h5>Drugs</h5><ul class='coversheet-drugs'>";
					jQuery.each(pe.drugs,function(j,d){
						moreInfo += "<li>"+d.name+"</li>";
					});
					moreInfo +="</ul>";
				}
				
				if (pe.fees && pe.fees.length > 0){
					moreInfo += "<h5>Fees</h5><ul class='coversheet-fees'>";
					jQuery.each(pe.fees,function(j,f){
						moreInfo += "<li>"+f.description+"</li>";
					});
					moreInfo +="</ul>";
				}

				var money = Ext.util.Format.usMoney(e[4]);
				if (e[7] == true){	// if WAIVED
					money = "<span class='waived-expense' style='color:red;text-decoration: line-through;'>"+money+"</span>&nbsp;<span style='font-weight:800;color:red;'>WAIVED</span>";
				}
				
				note = (e[6] && e[6].length > 0)?("<div class='coversheet-note'><p class='small'><strong>Note: </strong><em class='muted'>"+e[6]+"</em></p></div>"):"";
				jQuery("#expenses-"+e[8]+" table tbody").append(
						"<tr><td>"
								+ renderDescription(e)
								+ moreInfo
								+ note
								+ "</td><td>"
								+ money
								+ "</td></tr>");
				
				moreInfo = "";
			});

	jQuery("#total table tbody").append(
			"<tr><td colspan='5'><strong>Total</strong> - initiation costs only"+(pharmacy.waived?" (WAIVED)":"")+"</td><td class='totalcost"+(pharmacy.waived?" waived-total":"")+"'><strong>"
					+ (pharmacy.getDisplayTotal()) + "</strong></td></tr>");
		if (pharmacy.waived){
			jQuery("#waived").show();
		}			
					

};