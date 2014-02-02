Ext.ns('Clara', 'Clara.NewSubmission');

Clara.NewSubmission.Staff = function(o){
	this.id=				(o.id || '');	
	this.userid=			(o.userid || '');	
	this.lastname=			(o.lastname || '');
	this.firstname=			(o.firstname || '');	
	this.email=				(o.email || '');
	this.phone=				(o.phone || '');
	this.sap=				(o.sap || '');
	this.conflictofinterest=(o.conflictofinterest || 'false');
	this.conflictofinterestdesc=(o.conflictofinterestdesc || '');
	this.costs=				(o.costs || []);
	this.roles=				(o.roles || []);
	this.responsibilities=	(o.responsibilities || []);
	this.notify=(o.notify || false);
	
	this.costXML = function(){
		var p = "";
		for (var i=0;i<this.costs.length;i++){
			p += "<cost startdate='"+this.costs[i][0]+"' enddate='"+this.costs[i][1]+"' salary='"+this.costs[i][2]+"' fte='"+this.costs[i][3]+"' />";
		}
		return p;
	};
	
	this.arrayXML= function(arr, tag){
		if (typeof arr == 'undefined') return "";
		var p = "";
		for (var i=0;i<arr.length;i++){
			p += "<"+tag+">"+arr[i]+"</"+tag+">";
		}
		return p;
	};
	
	this.save= function(){
		var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
		var savedID='';
		url += "xml-elements/add";
		var data = {
				listPath: "/" + claraInstance.form.xmlBaseTag + '/staffs/staff',
				elementXml: this.toXML()
			};
		jQuery.ajax({
			async: false,
			url: url,
			type: "POST",
			dataType: 'xml',
			data: data,
			success: function(data){
				jQuery(data).find('staff').each(function(){
					savedID = jQuery(this).attr('id');
				});
			}
		});
		return savedID;
	};
	
	this.update= function(){
		var url = appContext + "/ajax/"+claraInstance.type+"s/" + claraInstance.id + "/"+claraInstance.type+"-forms/" + claraInstance.form.id + "/"+claraInstance.type+"-form-xml-datas/" + claraInstance.form.xmlDataId + "/";
		var savedID='';
		url += "xml-elements/update";
		var data = {
				listPath: "/" + claraInstance.form.xmlBaseTag + '/staffs/staff',
				elementXml: this.toXML(),
				elementId: this.id
			};
		jQuery.ajax({
			async: false,
			url: url,
			type: "POST",
			dataType: 'xml',
			data: data,
			success: function(data){
				jQuery(data).find('staff').each(function(){
					savedID = jQuery(this).attr('id');
				});
			}
		});
		return savedID;
	};
	
	this.toXML= function(){
		return "<staff id='"+this.id+"'><user sap='"+this.sap+"' phone='"+this.phone+"' id='"+this.userid+"'><lastname>"+Encoder.htmlEncode(this.lastname)+"</lastname><firstname>"+Encoder.htmlEncode(this.firstname)+"</firstname><email>"+Encoder.htmlEncode(this.email)+"</email>"+
			   "<roles>"+this.arrayXML(this.roles, "role")+"</roles><reponsibilities>"+this.arrayXML(this.responsibilities, "responsibility")+
			   "</reponsibilities><costs>"+this.costXML()+"</costs><conflict-of-interest>"+this.conflictofinterest+"</conflict-of-interest><conflict-of-interest-description>"+this.conflictofinterestdesc+"</conflict-of-interest-description></user><notify>"+this.notify+"</notify></staff>";
	};
};