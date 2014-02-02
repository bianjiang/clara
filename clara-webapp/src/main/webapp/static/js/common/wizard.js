Ext.ns('Clara.Forms');

Clara.Forms.CurrentWizardPageId = "";

Clara.Forms.SubSectionWindow = Ext.extend(Ext.Window, {
	title: 'Questions in this section',
    width: 650,
    height: 450,
    layout: 'fit',
    itemId: 'winSubSections',
    modal: true,
    id: 'winSubSections',
    questionStore:{},
   
    initComponent: function() {
    	var t =this;
		this.buttons = [];
        this.items = [{
		     xtype: 'grid',
		     id:'gpSubSectionPanel',
		     deferRowRender:false,
		     
		     tbar: {xtype:'toolbar', 
		    	 items:['->',
		    	        {
		    	        	xtype:'textfield',
		    	        	id:'fldSubSectionFilter',
		    	        	emptyText:'Filter by question text',
		    	        	width:200,
		    	        	enableKeyEvents: true,
		    	        	listeners:{
		    	        		keydown:function(f,e){
		    	        			t.questionStore.filter('questionText',f.getValue().trim(),true,true);
		    	        		}
		    	        	}
		    	        },{
		    	        	xtype:'button',
		    	        	text:'Clear',
		    	        	iconCls:'icn-cross-button',
		    	        	handler:function(){
		    	        		Ext.getCmp("fldSubSectionFilter").setValue();
		    	        		t.questionStore.clearFilter();
		    	        	}
		    	        }
		    	        ]
		     },
		     view: new Ext.grid.GroupingView({
		         forceFit: true,
		         emptyText: 'No questions found.',
		         emptyGroupText: '<span>&nbsp;</span>',
		         headersDisabled:true,
		         getRowClass: function(rec,index,rp,st){
					return (rec.get("conditional") == true)?"subsection-row-conditional":"subsection-row";
				 },
				 showGroupName:false,
		         groupTextTpl: '{text}'
		     }),
		    
		      listeners: {
		          afterrender: function(grid) {
		              grid.getView().el.select('.x-grid3-header').setStyle('display',    'none');
		          },
		      	  rowdblclick: function(grid,index,e){
		        	  clog(grid.getStore().getAt(index).get("questionId"));
		        	  Ext.getCmp("clara-form-wizardpanel").goToPage(grid.getStore().getAt(index).get("questionId"));
		        	  t.close();
		          }
		      },
		     stripeRows:true,
		     border: false,
		     store:t.questionStore,
		     id: 'gpSubSections',
		     columns: [
					{
					    xtype: 'gridcolumn',
					    dataIndex: 'subSectionTitle',
					    header: 'Subsection',
					    sortable: false,
					    hidden:true
					},{
					    xtype: 'gridcolumn',
					    dataIndex: 'questionText',
					    header: 'Question',
					    renderer: function(val){return '<div style="font-size:13px;white-space:normal !important;">'+ val +'</div>';},
					    sortable: false
					}
		     ]
		    }];
        Clara.Forms.SubSectionWindow.superclass.initComponent.call(this);
    }
});

Clara.Forms.WizardPanel = Ext.extend(Ext.form.FormPanel, {
	layout:'card',
	id:'clara-form-wizardpanel',
	formId:'clara-form-wizardpanel-formwrapper',
	padding:8,
	activeItem:0,
	previousTab:'',
	nextTab:'',
	saveBetween:true,
	itemsToAdd:[],	// Array of index/panel pairs to add or mix between the question divs. Each one is: {index:0, panel:thePanel} and so on..
	questionCls:'question',
	defaults: {
		border:false
	},
	goToPage: function(questionId){
		clog("gotopage called",questionId);
		this.navHandler(questionId);
	},
	navHandler: function(incr){
		var wp = this;
	    var l = wp.getLayout();
	    var numCards = l.container.items.length;
	    Clara.Forms.CurrentWizardPageId = l.activeItem.id;
	    var i = l.activeItem.id.split('question-card-')[1];
	    
	    var hiddenCount = 0;
	    var totalCount = 0;

	    var currentCard = jQuery("#"+Clara.Forms.CurrentWizardPageId);
	    var next = 0;
	    clog("navhandler called",incr,typeof(incr),isNaN(incr) );
	    if (typeof(incr)=='string' && isNaN(incr)){
	    	// sent a question id.. find it and show it.
	    	for  (var i = 0; i < numCards; i++){
	    		jQuery("#question-card-"+i+" .question").each(function(idx,value){
		    		if (jQuery(value).attr("id") == incr) next = i;
		    	});
	    	}
	    	
	    } else {
	    	// Sent a number, go there (forward or back)
	    	next = parseInt(i, 10) + incr;
		    // Modify next if the next card has no visible questions 
		    while (next != -1 && next != numCards){
		    	jQuery("#question-card-"+next+" .question").each(function(index){
		    		totalCount++;
		    		if (jQuery(this).hasClass("form-question-hidden"))
		    			hiddenCount++;
		    	});
		    	
		    	if (totalCount == hiddenCount && totalCount > 0)
		    		var next = next + incr;
		    	else
		    		break;
		    }
		    if (wp.saveBetween) {
			    if (next==-1){
			    	if (claraInstance.navigation.current.id) submitXMLToNextPage(claraInstance.navigation.prev.id, true);
			    	else cwarn("wizard.js: claraInstance.navigation not inited. add \"claraInstance.navigation.init('pagename')\" to form page's jspx page");
			    } else if (next == numCards){
			    	if (claraInstance.navigation.current.id) submitXMLToNextPage(claraInstance.navigation.next.id, true);
			    	else cwarn("wizard.js: claraInstance.navigation not inited. add \"claraInstance.navigation.init('pagename')\" to form page's jspx page");
			    } else {
			    	submitXMLToNextPage('', false);
			    }
		    }
	    }
	    
		    if (next != -1 && next != numCards) {
		    	l.setActiveItem(next);
		    	Ext.getCmp("card-prev").setDisabled((next == 0) && (claraInstance.navigation.current.id == "first-page"));	// prevent going back before the first page of a section
		    }
	    
	},
	refreshLayout:function(){
		this.getLayout();
	},
	constructor:function(config){		
		Clara.Forms.WizardPanel.superclass.constructor.call(this, config);
	},
	getAddedPanelAtIndex:function(i){
		var it = null;
		jQuery.each(this.itemsToAdd, function(idx, item){
			clog(item.index+" and i:"+i);
			if (parseInt(item.index) == parseInt(i)) {
				clog("match");
				clog(item);
				it = item;
			}
		});
		return it;
	},
	validateActiveCard:function(bypass){
		if (bypass == true) return true;
		
		var cardid = this.getLayout().activeItem.id;
		var cbel;
		var validatedNames = [];
		var allNames = [];
		var vnfound = false;
		var anfound = false;

		var validateOptions = {
				errorPlacement: function(error, element) {
			        if (element.is(':radio') || element.is(':checkbox'))
			        {
			        	var wrapper = element.closest(".questionValue");
			        	error.insertAfter(wrapper);
			        }
			        else
			        {
			        	error.insertAfter(element);
			        }
			    }
		};
		
		if (typeof jQuery("#"+cardid+" .question-el:visible") != "undefined"){
			jQuery("#"+cardid+" .question-el:visible").each(function(idx,el){
				clog("testing "+"#"+cardid+" .question-el, id: ");
				clog(el,el.nodeName,el.nodeValue,el.innerHTML,el.nodeType,typeof el);
				// IE HACK: on the budget page an HTMLUnknownElement is found with name "/INPUT" (no other browser reports this).. we'll just skip checking them
				if (el.nodeName == "/INPUT"){
					cwarn("WIZARD: invalid element found",el,el.nodeName,el.nodeType);
				} else {
					if (typeof jQuery(el).attr("name") != "undefined" && jQuery.inArray(jQuery(el).attr("name"),allNames) == -1) allNames.push(jQuery(el).attr("name"));
					if (typeof jQuery(el).attr("name") != "undefined" && jQuery("#clara-form-wizardpanel-formwrapper").validate(validateOptions).element(el) && jQuery.inArray(jQuery(el).attr("name"),validatedNames) == -1) validatedNames.push(jQuery(el).attr("name"));
				}
			});
		}
		clog("Validate PASSED for "+validatedNames.length+" of "+allNames.length+" elements.");
		if (allNames.length == 0) return true;
		else return (validatedNames.length == allNames.length);
	},
	
	createItems:function(){
		var maxHeight = 0;
		var t = this;
		// Add question DIVs as panels in the card layout:
		var wp = this;
		var ap = null;
		
		var adjustedIndex = 0;
		
		qc = wp.questionCls;
		jQuery('.'+qc).each(function(index) {	
			maxHeight = (jQuery(this).outerHeight() > maxHeight)?jQuery(this).outerHeight():maxHeight;
			
			ap = t.getAddedPanelAtIndex(index);
			//clog("AP TO ADD at "+index+":");
			//clog(ap);
				
			wp.add({
				id:'question-card-'+adjustedIndex,
				contentEl:this
			});
			if (ap != null) {
				adjustedIndex++;
				//ap.id = 'question-card-'+adjustedIndex;
				wp.add(ap.panel);
			}
			
			adjustedIndex++;
		});
		
		maxHeight = maxHeight + 100;
		wp.setHeight(maxHeight);
		wp.doLayout();		

		//wp.getLayout().setActiveItem(0);
	},
	moveToFirstPage:function(){
		// this function is NOT WORKING. we need it to move to the first non-hidden page
		var t = this;
		// skip any hidden questions, go to first non-hidden question
		clog("items",this.items);
		for (var i=0;i<this.items.length;i++){
			var c = this.items.get(i);
			clog("ITERATING THROUGH CARDS, FOUND", c.contentEl.outerHTML);
			if (jQuery(c.contentEl.outerHTML).first("div").hasClass("form-question-hidden") == true) { 
				cwarn("Wizard: Page with hidden class found. SHOULD call navHandler(1)",jQuery(c.contentEl.outerHTML).first("div"),jQuery(c.contentEl.outerHTML).first("div").hasClass("form-question-hidden"));
				// t.navHandler(1);
			}
			else clog("NOTHIDDEN");
		}
	},
	initComponent: function() {

		var wizpanel = this;
		
		var config = {
				items:[],	
				border:false,
				tbar:{
					xtype:'toolbar',
					// style:'background:#eee url('+appContext +'/static/images/wizard-toolbar-bg.png) repeat-x top left;',
					items:[
				      {
					      id:'card-prev',
					      text:'Back',
					      handler: function(){
				    	  	//if (wizpanel.validateActiveCard(true)){
				    	  		wizpanel.navHandler(-1);
				    	  	//}
				    	  },
					      disabled:((claraInstance.navigation.current.id == "first-page")),
					      iconCls:'icn-arrow-180'
				      },'->',
				        {
				            id: 'card-next',
				            text: 'Next',
				            handler: function(){
				    	  	if (wizpanel.validateActiveCard()){
				    	  		wizpanel.navHandler(1);
				    	  	}
				    	  },
				            iconCls:'icn-arrow'
				        }
				     ]
				}
		};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));
		Clara.Forms.WizardPanel.superclass.initComponent.apply(this, arguments);
		this.createItems();		
		
		
	}
	

});
Ext.reg('claraformwizardpanel', Clara.Forms.WizardPanel);