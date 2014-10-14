Ext.ns('Clara.Pharmacy');

Clara.Pharmacy.MessageBus = new Ext.util.Observable();

Clara.Pharmacy.MessageBus.addEvents('beforepharmacyload','pharmacyloaded','pharmacyinfoupdated','onpharmacyloaderror',
									'beforepharmacysave','afterpharmacysave','afterhistoryupdated','onpharmacysaveerror',
								    'expenseadded','expenseselected','expenseremoved','expenseupdated','waivechanged');

Clara.Pharmacy.MessageBus.on("pharmacyloaded", function(){
	// set waived button and suppress event
	
	
	if (pharmacy.expenses.length == 0){
		// Add basic expense
		pharmacy.addExpense(
				new Clara.Pharmacy.Expense({
					id:			pharmacy.newId(),
					count:		1,
					cost:		1000,
					waived:		false,
					name:		'base-fee',
					type:		'simc',
					description:'Base Fee'
				})
		);
	}
});

var canEditPharmacy = null;

Clara.Pharmacy.canEdit = function(){
	canEditPharmacy = (canEditPharmacy == null)?(claraInstance.HasAnyPermissions(['EDIT_PHARMACY'])):canEditPharmacy;
	return canEditPharmacy;
};

Clara.Pharmacy.Form = function(o){
	this.idGenerator=			(o.idGenerator || 1000);									// Auto-generated ID's, used universally for all elements TODO: Change back to 0 when done testing.
	this.id=					(o.id || '');												// ID / GUID assigned when saved
	this.expenses=				(o.expenses || []);
	this.initialWaived=			(o.initialWaived || false);
	this.total=					(o.total || 0);
	this.displaytotal=			(o.displaytotal || 0);										// Transient
		
	this.newId= function(){
		return this.idGenerator++;
	};
	
	this.setInitialWaived = function(waived){
		this.initialWaived = waived;
		for (var i=0; i<this.expenses.length;i++){
			if (this.expenses[i].type === "simc") {
				this.expenses[i].waived = waived;
			}
		}
		Clara.Pharmacy.MessageBus.fireEvent("waivechanged", waived);
	}
	
	this.setWaived = function(waived){
		// this.waived = waived;
		
		for (var i=0; i<this.expenses.length;i++){
			this.expenses[i].waived = waived;
		}
		
		Clara.Pharmacy.MessageBus.fireEvent("waivechanged", waived);
	};
	
	this.setExpenseWaived = function(id, waived){

		for (var i=0; i<this.expenses.length;i++){
			if (this.expenses[i].id == id){
				clog(("Found expense, setting waived to "+waived), this.expenses[i]);
				this.expenses[i].waived = waived;
			}
		}
		
		Clara.Pharmacy.MessageBus.fireEvent("waivechanged", waived);
	};
	
	this.getDisplayTotal = function(){
		this.updateTotal();
		
		var anySimcWaived = (this.displaytotal !== this.total)?true:false;
		
		var s = "<span class='"+(anySimcWaived?"waived-total":"")+"' id='pharmacy-total-value'>"+Ext.util.Format.usMoney(this.displaytotal);
		
		if (anySimcWaived) s += "</span> <span id='pharmacy-total-value'>"+Ext.util.Format.usMoney(this.total);
		
		s += "</span>";
		
		return s;
	};
	
	this.updateTotal= function(){
			var t = 0, waivedT = 0;
			var anySimcWaived = false;
			for (var i=0;i<this.expenses.length;i++){
				anySimcWaived = anySimcWaived || this.expenses[i].waived;
				if (this.expenses[i].type == 'simc'){
					t += parseFloat(this.expenses[i].cost);
					if (this.expenses[i].waived === false) waivedT += parseFloat(this.expenses[i].cost);
				}
			}
			this.total = waivedT;
			this.displaytotal = t;
			clog("updateTotal: total="+waivedT+" displayTotal="+t);
	};
	
	this.addExpense= function(e){
		if (typeof e.id == 'undefined' || e.id == ''){
			e.id = this.newId();
		}
		this.expenses.push(e);
		Clara.Pharmacy.MessageBus.fireEvent('expenseadded', e);
	};
	
	this.updateExpenseCost= function(id, cost){
		clog("updateExpenseCost("+id+","+cost+")");
		for (var i=0; i<this.expenses.length;i++){
			if (this.expenses[i].id === id){
				clog("Expense found",this.expenses[i]);
				this.expenses[i].cost = cost;
				Clara.Pharmacy.MessageBus.fireEvent('expenseupdated', this.expenses[i]);
				return true;
			}
		}
		return false;
	};
	
	this.updateExpense= function(a){
		for (var i=0; i<this.expenses.length;i++){

					if (this.expenses[i].id == a.id){
						this.expenses[i].description = a.description;
						this.expenses[i].type = a.type;
						this.expenses[i].cost = a.cost;
						this.expenses[i].waived = a.waived;
						this.expenses[i].count = a.count;
						this.expenses[i].notes = a.notes;
						Clara.Pharmacy.MessageBus.fireEvent('expenseupdated', a);
						return true;
					}

		}
		return false;
	};
	
	this.removeExpense= function(e){
		for (var i=0; i<this.expenses.length;i++){
					if (this.expenses[i].id == e.id){
						this.expenses.splice(i,1);
						Clara.Pharmacy.MessageBus.fireEvent('expenseremoved', e);
						return true;
					}
		}
		return false;
	};
			
	this.getExpense= function(id){
		for (var i=0; i<this.expenses.length;i++){
			if (this.expenses[i].id == id){
				return this.expenses[i];
			}
		}
		return null;
	};
	
	this.removeExpenseById= function(id){
		for (var i=0; i<this.expenses.length;i++){
			if (this.expenses[i].id == id){
				this.expenses.splice(i,1);
			}
		}
	};
	
	this.getStore= function(){
		var fields = [{name:'id'},{name:'type'},{name:'tdesc'},{name:'description'},{name:'cost'},{name:'count'},{name:'notes'},{name:'waived'}];
		return new Ext.data.GroupingStore({
			reader: new Ext.data.ArrayReader({},fields),
			autoLoad:false,
			sortInfo:{field:'type', direction:'ASC'},
			groupField:'type'
		});
	};
	
	this.getArray= function(){
		var a = [];
		for (var i=0; i<this.expenses.length;i++){
			a.push([this.expenses[i].id, this.expenses[i].getTypeDesc(),this.expenses[i].getTypeDesc(), this.expenses[i].description, this.expenses[i].cost, this.expenses[i].count, this.expenses[i].notes,this.expenses[i].waived,this.expenses[i].type]);
		}
		clog(a);
		return a;
	};
	
	this.load= function(){
		Clara.Pharmacy.MessageBus.fireEvent('beforepharmacyload', this);
		var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/pharmacy/get";
		var b = this;
		jQuery.ajax({
			  type: 'GET',
			  async:false,
			  url: url,
			  success: function(data){
				  b.fromXML(data);
				  Clara.Pharmacy.MessageBus.fireEvent('pharmacyloaded', this);  
			  },
			  error: function(){
				  Clara.Pharmacy.MessageBus.fireEvent('onpharmacyloaderror', this);  
			  },
			  dataType: 'xml'
		});
	};
	
	this.save= function(xmlstring){
		Clara.Pharmacy.MessageBus.fireEvent('beforepharmacysave', this);
		var url = appContext+"/ajax/protocols/"+claraInstance.id+"/protocol-forms/"+claraInstance.form.id+"/pharmacy/save";
		var data = (xmlstring)?xmlstring:this.toXML();
	
		
		jQuery.ajax({
			  type: 'POST',
			  async:false,
			  url: url,
			  data: {xmlData: data},
			  success: function(){
				  Clara.Pharmacy.MessageBus.fireEvent('afterpharmacysave', this);  
			  },
			  error: function(){
				  Clara.Pharmacy.MessageBus.fireEvent('onpharmacysaveerror', this);  
			  },
			  dataType: 'xml'
		});
		
	};
	
	this.toXML= function(){
		this.updateTotal();
		var xml = "<pharmacy id='"+this.id+"' initial-waived='"+this.initialWaived+"' total='"+this.total+"'>";
		xml = xml + "<expenses>";
		for (var i=0; i<this.expenses.length;i++){
			xml = xml + this.expenses[i].toXML();
		}
		xml = xml + "</expenses>";
		xml = xml + "</pharmacy>";
		return xml;
	};
	
	this.fromXML = function(xml){
		var t = this;
		clog("from: ",xml);
		var maxid = 0;
		jQuery(xml).find("pharmacy").each(function(){
			t.id = parseFloat(jQuery(this).attr('id'));
			t.total = parseFloat(jQuery(this).attr('total'));
			t.initialWaived = (jQuery(this).attr('initial-waived') === 'true')?true:false;
		});
		var exps = [];		// expenses
		jQuery(xml).find("expenses").find("expense").each(function(){
			var drugs = [];
			var fees = [];
			
			var exp = new Clara.Pharmacy.Expense({
				id:			parseFloat(jQuery(this).attr('id')),
				count:		parseFloat(jQuery(this).attr('count')),
				cost:		parseFloat(jQuery(this).attr('cost')),
				waived:	(jQuery(this).attr('waived') == 'true')?true:false,
				name:		Encoder.htmlDecode(jQuery(this).attr('name')),
				type:		Encoder.htmlDecode(jQuery(this).attr('type')),
				description:Encoder.htmlDecode(jQuery(this).attr('description')),
				notes:		Encoder.htmlDecode(jQuery(this).attr('notes'))
			});
			
			jQuery(this).find("drugs").find("drug").each(function(){
				clog("drug?");
				var drug = new Clara.Pharmacy.Drug({
					id:			parseFloat(jQuery(this).attr('id')),
					name:		Encoder.htmlDecode(jQuery(this).attr('name'))
				});
				clog(drug);
				drugs.push(drug);
			});
			
			
			jQuery(this).find("fees").find("fee").each(function(){
				clog("fee?");
				var fee = new Clara.Pharmacy.Fee({
					amount:		parseFloat(jQuery(this).attr('amount')),
					description:Encoder.htmlDecode(jQuery(this).attr('description'))
				});
				clog(fee);
				fees.push(fee);
			});
			
			

			
			exp.fees = fees;
			exp.drugs = drugs;
			clog("EXP:");
			clog(exp);
			maxid = (maxid < exp.id)?exp.id:maxid;
			exps.push(exp);
			
		});
		this.expenses = exps;
		this.idGenerator = maxid+1;
	};
	
	
};

Clara.Pharmacy.Drug = function(o){
	this.id=				(o.id || '');
	this.name=				(o.name || '');
	this.toXML= function(){
		return "<drug id='"+this.id+"' name='"+Encoder.htmlEncode(this.name)+"'/>";
	};
};

Clara.Pharmacy.Fee = function(o){
	this.amount=			(o.amount || 0);
	this.description=		(o.description || '');
	this.toXML= function(){
		return "<fee amount='"+this.amount+"' description='"+Encoder.htmlEncode(this.description)+"'/>";
	};
};

Clara.Pharmacy.Expense = function(o){
	this.id=				(o.id || '');	
	this.count=				(o.count || 0);
	this.type=				(o.type || '');
	this.name=				(o.name || '');
	this.waived=			(o.waived || false);
	this.cost=				(o.cost || 0);
	this.description=		(o.description || '');	
	this.notes=				(o.notes || '');	
	this.fees=				(o.fees || []);
	this.drugs=				(o.drugs || []);
	
	// Functions

	this.getTypeDesc= function(){
		var str =  (this.type == "simc")?"Study Initiation, Management and Closeout":((this.type == "disp")?"Dispensing Fees (per treatment)":"Other Charges (Annual charges for the pharmacy budget)");
		clog(str);
		return (this.type == "drug")?"Drug Dispensing Fee":str;
	};
	
	this.addDrug= function(id,name){
		this.drugs.push(new Clara.Pharmacy.Drug({id:id,name:name}));
	};
	
	this.addFee= function(amount,description){
		this.fees.push(new Clara.Pharmacy.Fee({amount:amount,description:description}));
	};
	
	this.toXML= function(){
		var xml ="<expense id='"+this.id+"' count='"+this.count+"' waived='"+this.waived+"' cost='"+this.cost+"' type='"+this.type+"' name='"+Encoder.htmlEncode(this.name)+"' description='"+Encoder.htmlEncode(this.description)+"' notes='"+Encoder.htmlEncode(this.notes)+"'>";
		xml += "<drugs>";
		for (var i=0; i<this.drugs.length;i++){
			xml = xml + this.drugs[i].toXML();
		}
		xml += "</drugs><fees>";
		for (var i=0; i<this.fees.length;i++){
			xml = xml + this.fees[i].toXML();
		}
		return xml+"</fees></expense>";
	};
};



