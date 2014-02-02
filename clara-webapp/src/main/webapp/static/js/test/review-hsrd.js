//{
//    xtype: 'window',
//    //id:'winCompleteReview',
//    //title: 'Complete Review',
//    //width: 558,
//    //height: 252,
//    //layout: 'absolute',
//    //buttons: [
//	  			{
//					text:'Close',
//					disabled:false,
//					handler: function(){
//						Ext.getCmp("winCompleteReview").close();
//					}
//				},
//				{
//					text:'Sign and Complete Review',
//					id:'btn-sign-and-complete',
//					disabled:false,
//					handler: function(){
//						var username = jQuery("#complete-fldUsername").val();
//						var password = jQuery("#complete-fldPassword").val();
//						var isHSRD = Ext.getCmp("fldIsHSRD").getValue();
//						
//						if (jQuery.trim(username) != "" && jQuery.trim(password) != "" && jQuery.trim(password) != ""  ){
//							// call the sign-and-complete url
//							var fId = currentFormReviewSession.protocolFormId;
//							var cId = currentFormReviewSession.committee;
//							var uId = currentFormReviewSession.userId;
//							var xml = "<form-review committee='"+cId+"' user-id='"+uId+"' action='ACKNOWLEDGEMENT'>";
//							xml = xml + "<data><is-hsrd>"+isHSRD+"</is-hsrd></data></form-review>";
//							
//							
//							clog("WILL CALL FORMID "+fId+" WITH: un: "+username+"  pw: "+password);
//							
//							completeReview(fId,cId,username,password,xml);
//							
//							clog("after ajax");
//							
//						} else {
//							alert("Please complete this form before continuing.");
//						}
//					
//						
//					}
//			}
//	        ],
//    items: [
//            
//        {
//            xtype: 'radio',
//            boxLabel: 'Yes, this is Human Subject Research',
//            x: 40,
//            y: 60,
//            style: 'font-size:16px;',
//            itemId: 'fldIsHSRD',
//            name: 'fldIsHSRD',
//            value: 'yes',
//            id: 'fldIsHSRD'
//        },
//        {
//            xtype: 'radio',
//            boxLabel: 'No, this does not involve Human Subject Research',
//            x: 40,
//            y: 80,
//            style: 'font-size:16px;',
//            name: 'fldIsHSRD',
//            value: 'no',
//            id: 'fldIsntHSRD'
//        },
//        {
//            xtype: 'textfield',
//            id:'complete-fldUsername',
//            x: 20,
//            y: 140,
//            width: 250
//        },
//        {
//            xtype: 'textfield',
//            id:'complete-fldPassword',
//            inputType:'password',
//            x: 300,
//            y: 140,
//            width: 220
//        },
//        {
//            xtype: 'displayfield',
//            value: 'UAMS username',
//            x: 20,
//            y: 120
//        },
//        {
//            xtype: 'displayfield',
//            value: 'Password',
//            x: 300,
//            y: 120
//        },
//        {
//            xtype: 'label',
//            text: 'Before you submit this form, please answer the following question(s) and sign the form with your UAMS username and password.',
//            x: 10,
//            y: 10,
//            width: 520,
//            style: 'font-size:15px;'
//        }
//    ]
//}