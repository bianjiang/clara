function renderSummaryPage(){
	
	jQuery(".summary-section-header").click(function(){
		jQuery(this).next(".summary-section-content").toggle();
	});

	jQuery(".summary-section-header").dblclick(function(){
		jQuery(".summary-section-content").show();
		//jQuery(this).next(".summary-section-content").show();
	});

		
	initializeStaffStores();
	initializeSitesStores();
	renderSites();
	renderStaff();
	
	jQuery(".summary-row-value").each(function(){
		//breakValue = nl2br(jQuery(this).html(), true);
		//jQuery(this).html(breakValue);
		if (jQuery.trim(jQuery(this).text()) == ''){
			jQuery(this).closest('.summary-row').addClass('summary-row-empty-value');
		}
	});

}

function renderSites(){
	var protocolSitePanel = new Ext.grid.GridPanel({
    	frame:false,
    	trackMouseOver:false,
    	renderTo: 'external-site-list',
        store: protocolSiteStore,
        sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
        autoHeight:true,
        viewConfig: {
    		forceFit:true
    	},
        columns: [
                  {
                	  	header:'Site Name',
                	  	dataIndex:'sitename',
                	  	editor   : new Ext.form.TextField(),
                	  	sortable:true,
                	  	width:250
                  },
                  {
                	  	header:'Contact Name',
                	  	dataIndex:'contact',
                	  	sortable:true,
                	  	editor   : new Ext.form.TextField(),
                	  	width:200
                    },
                    {
                  	  	header:'Phone',
                  	  	dataIndex:'phone',
                  	  	sortable:false,
                  	    editor   : new Ext.form.TextField(),
                  	  	width:200
                      },
                      {
                    	  	header:'E-mail',
                    	  	dataIndex:'email',
                    	  	sortable:false,
                    	  	editor   : new Ext.form.TextField(),
                    	  	width:200
                        }
        ]
    });
}

function renderStaff(){
    function renderStaff(value, p, record){
        return String.format(
                '<b>{1} {0}</b><br/><a href="mailto:{3}">{3}</a>',
                value, record.data.firstname, record.data.lastname, record.data.email);
    }
    
    function renderRoles(value, p, records){
    	
    	var outHTML='<ul>';
    	
    	for (var i=0; i<records.data.roles.length; i++) {
    		if (jQuery.browser.msie)
    			outHTML = outHTML + "<li class='staff-row-role'>"+records.data.roles[i].node.text+"</li>";
    		else
    			outHTML = outHTML + "<li class='staff-row-role'>"+records.data.roles[i].node.textContent+"</li>";
    	}

    	return outHTML+'</ul>';
    	
    }
    
    function renderResp(value, p, records){
    	
    	var outHTML='<ul>';
    	
    	for (var i=0; i<records.data.responsibilities.length; i++) {
    		if (jQuery.browser.msie)
    			outHTML = outHTML + "<li class='staff-row-responsibility'>"+records.data.responsibilities[i].node.text+"</li>";
    		else
    			outHTML = outHTML + "<li class='staff-row-responsibility'>"+records.data.responsibilities[i].node.textContent+"</li>";
    	}

    	return outHTML+'</ul>';
    	
    }
    
	var staffDataView = new Ext.grid.GridPanel({
    	frame:false,
    	trackMouseOver:false,
    	renderTo: 'staff-list',
        store: staffstore,
        sm: new Ext.grid.RowSelectionModel({singleSelect: true}),
        loadMask: new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."}),
        autoHeight:true,
        viewConfig: {
    		forceFit:true
    	},
        columns: [
                  {
                	  	header:'Staff Member',
                	  	dataIndex:'lastname',
                	  	sortable:true,
                	  	renderer:renderStaff,
                	  	width:150
                  },
                  {
                	  	header:'Roles',
                	  	dataIndex:'roles',
                	  	sortable:false,
                	  	renderer:renderRoles,
                	  	width:150
                    },
                    {
                  	  	header:'Responsibilities',
                  	  	dataIndex:'responsibilities',
                  	  	sortable:false,
                  	  	renderer:renderResp,
                  	  	width:250
                      }
        ]
    });
}


function nl2br (str, is_xhtml) {
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: Philip Peterson
    // +   improved by: Onno Marsman
    // +   improved by: Atli or
    // +   bugfixed by: Onno Marsman
    // +      input by: Brett Zamir (http://brett-zamir.me)
    // +   bugfixed by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: Brett Zamir (http://brett-zamir.me)
    // +   improved by: Maximusya
    // *     example 1: nl2br('Kevin\nvan\nZonneveld');
    // *     returns 1: 'Kevin<br />\nvan<br />\nZonneveld'
    // *     example 2: nl2br("\nOne\nTwo\n\nThree\n", false);
    // *     returns 2: '<br>\nOne<br>\nTwo<br>\n<br>\nThree<br>\n'
    // *     example 3: nl2br("\nOne\nTwo\n\nThree\n", true);
    // *     returns 3: '<br />\nOne<br />\nTwo<br />\n<br />\nThree<br />\n'

    var breakTag = (is_xhtml || typeof is_xhtml === 'undefined') ? '<br />' : '<br>';

    return (str + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1'+ breakTag +'$2');
}