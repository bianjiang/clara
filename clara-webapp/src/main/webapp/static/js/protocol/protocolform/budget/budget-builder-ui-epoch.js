Ext.ns('Clara.BudgetBuilder');


Clara.BudgetBuilder.NewEpochWindow = Ext.extend(Ext.Window, {
	title: 'Add Phase',
    width: 400,
    height: 250,
    padding:6,
    layout: 'form',
    itemId: 'winNewPhase',
    modal: true,
    id: 'winNewPhase',
    epoch:null,
    subjectCount:1,
    visitCount:1,
    initComponent: function() {
    	var t = this;
    	this.epoch = {};
    	var epoch = this.epoch;
    	clog("new phase window: epoch",epoch);
		this.buttons = [
		    {
		        text: 'Save',
		        handler: function(){
		        	
		        	var name = jQuery.trim(Ext.getCmp("fldNewPhaseEpochName").getValue());
					name = name.substr(0,31);
					var epochNameExists = (budget.getEpochByName(name) != null);
					var epochNameIsBlank = (name == "");
					var isSimpleAndHasNoSubjects = (epoch.simple && t.subjectCount < 1);
						
					if (!isSimpleAndHasNoSubjects && !epochNameExists && !epochNameIsBlank && Clara.BudgetBuilder.validatePhaseName(name)){
			    		var id = budget.newId();
			
			    		var e = new Clara.BudgetBuilder.Epoch({
			    			id: 			id,
			    			name: 			epoch.name,
			    			simple:			epoch.simple,
			    			conditional:	epoch.conditional || false
			    		});
			    		
			    		if (epoch.simple){
							var a = new Clara.BudgetBuilder.Arm({id:budget.newId()});
							var c = new Clara.BudgetBuilder.Cycle({id:budget.newId(),simple:true});
							for (var i=0;i<parseInt(t.visitCount); i++){
								c.visits.push(new Clara.BudgetBuilder.Visit({id:budget.newId(), unitvalue:i+1, unit:'Day', subjectcount:t.subjectCount, cycleindex:i+1, name:"Day "+(i+1)}));
							}
							c.recalculateDateRanges();
							a.cycles.push(c);
							e.arms.push(a);
			    		}
						budget.addEpoch(e);
						// budget.save();
						
						Ext.getCmp("budget-tabpanel").selectEpoch(e);
						budget.save();
						t.epoch=null;
						t.close();
					} else {
						if (isSimpleAndHasNoSubjects) alert("You need to have at least one subject.");
						if (epochNameExists) alert("A phase already exists with that name. Choose another one and try again.");
						if (epochNameIsBlank) alert("Phase name cannot be blank.");
						if (!Clara.BudgetBuilder.validatePhaseName(name)) alert("Phase name cannot contain the following: "+Clara.BudgetBuilder.InvalidPhaseCharacters);
					}
					
		        }
		    }
		];
        this.items = [
            {
            	xtype:'textfield',
            	fieldLabel:'Study phase name',
            	id:'fldNewPhaseEpochName',
            	autoCreate: {tag: 'input', type: 'text', size: '20', autocomplete: 'off', maxlength: '31'},
            	listeners: {
					change:function(t){
						epoch.name = t.getValue();
					}
				}
            },
            {
			    xtype: 'fieldset',
			    title: 'Simple phase..',
			    collapsed:true,
			    id:'fldNewPhaseSimpleEpoch',
			    checkboxToggle: true,
			    items: [
			        {
			            xtype: 'numberfield',
			            id:'fldNewPhaseSubjectCount',
			            fieldLabel: 'Number of subjects',
			            value:1,
			            anchor: '100%',
					    listeners: {
							change:function(f){
								t.subjectCount = f.getValue();
							}
						}
			        },{
			            xtype: 'numberfield',
			            id:'fldNewPhaseVisitCount',
			            fieldLabel: 'Number of visits',
			            value:1,
			            anchor: '100%',
					    listeners: {
							change:function(f){
								t.visitCount = f.getValue();
							}
						}
			        }
			    ],
			    listeners: {
					collapse:function(){
						epoch.simple = false;
					},
					expand:function(){
						epoch.simple = true;
					}
				}
			},
            {
            	xtype:'checkbox',
            	boxLabel:'This is a conditional phase',
            	id:'fldNewPhaseEpochConditional',
            	hideLabel:true,
            	listeners: {
					check:function(t,v){
						clog("conditional",v);
						epoch.conditional = v;
					}
				}
            }
        ];
        Clara.BudgetBuilder.NewEpochWindow.superclass.initComponent.call(this);
    }
});
