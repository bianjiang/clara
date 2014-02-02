Ext.ns('Clara.Reviewer');

// Final Review Panel: Will contain certain xtypes, with each xtype
// pre-populated with data from xml

Clara.Reviewer.FinalReviewPanel = Ext.extend(Ext.Panel, {

	id : 'finalreviewpanel',
	reviewFormName : '',
	reviewFormType : claraInstance.type, // or 'contract'
	reviewPanelXtypes : [],
	constructor : function(config) {
		Clara.Reviewer.FinalReviewPanel.superclass.constructor.call(this,
				config);
	},

	validate : function() {
		var t = this;
		var valid = true;
		clog(t.items.items);
		for ( var i = 0; i < t.items.items.length; i++) {
			valid = valid && t.items.items[i].validate();
		}
		return valid;
	},

	getXML : function() {
		var t = this;
		//var xml = "<" + t.reviewFormType + "-" + t.reviewFormName + ">";
		var xml = "";
		clog(t.items.items);
		for ( var i = 0; i < t.items.items.length; i++) {
			xml += t.items.items[i].getXML();
		}
		//xml += "</" + t.reviewFormType + "-" + t.reviewFormName + ">"
		return xml;
	},

	initComponent : function() {
		var t = this;
		var config = {
			border : false
		};

		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		// call parent
		Clara.Reviewer.FinalReviewPanel.superclass.initComponent.apply(this,
				arguments);

		var xml;
		// AJAX call to get XML for this form, then pass the XML for each xtype
		// to each type item
		jQuery.ajax({
			// url:appContext+'/static/xml/samples/final-review-panel.xml', //
			// TODO: replace with real controller url
			url : appContext + "/ajax/"+claraInstance.type+"s/"
					+ claraInstance.id
					+ "/"+claraInstance.type+"-forms/"
					+ claraInstance.form.id
					+ "/review/committee-review-form",
			type : "GET",
			data : {
				reviewFormIdentifier : t.reviewFormName,
				reviewFormType : claraInstance.form.type
			},
			async : false,
			dataType : 'xml',
			success : function(data) {
				xml = data;
			}
		});

		// read xtypes, generate items

		jQuery(xml).find('panels').find('panel').each(function() {
			var p = this;
			var px = jQuery(p).attr('xtype');
			var pid = jQuery(p).attr('id');
			clog("ADDING XTYPE "+px);
			t.add({
				xtype : px,
				id : pid,
				reviewFormType : t.reviewFormType,
				reviewPanelXml : p,
				padding : 6
			});
		});

		t.doLayout();

	}

});
Ext.reg('clarareviewerfinalreviewpanel', Clara.Reviewer.FinalReviewPanel);




