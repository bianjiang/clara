/*!
 * jQuery Form Serilizer Plugin
 * version: 0.01 (4/24/2010)
 * @requires jQuery v1.3.2 or later
 *
 * licensed under GPL licenses:
 *   http://www.gnu.org/licenses/gpl.html
 */
;(function($) {

	/**
	 * Returns the value(s) of the element in the matched set.  For example, consider the following form:
	 *
	 *  <form><fieldset>
	 *	  <input name="A" type="text" />
	 *	  <input name="A" type="text" />
	 *	  <input name="B" type="checkbox" value="B1" />
	 *	  <input name="B" type="checkbox" value="B2"/>
	 *	  <input name="C" type="radio" value="C1" />
	 *	  <input name="C" type="radio" value="C2" />
	 *  </fieldset></form>
	 *
	 *  var v = $(':text').fieldValue();
	 *  // if no values are entered into the text inputs
	 *  v == ['','']
	 *  // if values entered into the text inputs are 'foo' and 'bar'
	 *  v == ['foo','bar']
	 *
	 *  var v = $(':checkbox').fieldValue();
	 *  // if neither checkbox is checked
	 *  v === undefined
	 *  // if both checkboxes are checked
	 *  v == ['B1', 'B2']
	 *
	 *  var v = $(':radio').fieldValue();
	 *  // if neither radio is checked
	 *  v === undefined
	 *  // if first radio is checked
	 *  v == ['C1']
	 *
	 * The successful argument controls whether or not the field element must be 'successful'
	 * (per http://www.w3.org/TR/html4/interact/forms.html#successful-controls).
	 * The default value of the successful argument is true.  If this value is false the value(s)
	 * for each element is returned.
	 *
	 */
	$.fn.fieldValue = function(successful) {
		for (var val=[], i=0, max=this.length; i < max; i++) {
			var el = this[i];
			var v = $.fieldValue(el, successful);
			if (v === null || typeof v == 'undefined' || (v.constructor == Array && !v.length))
				continue;
			v.constructor == Array ? $.merge(val, v) : val.push(v);
		}
		return val;
	};
	
	/**
	 * Returns the value of the field element.
	 */
	$.fieldValue = function(el, successful) {
		var n = el.name, t = el.type, tag = el.tagName.toLowerCase();
		if (typeof successful == 'undefined') successful = true;

		if (successful && (!n || el.disabled || t == 'reset' || t == 'button' ||
			(t == 'checkbox' || t == 'radio') && !el.checked ||
			(t == 'submit' || t == 'image') && el.form && el.form.clk != el ||
			tag == 'select' && el.selectedIndex == -1))
				return null;

		if (tag == 'select') {
			var index = el.selectedIndex;
			if (index < 0) return null;
			var a = [], ops = el.options;
			var one = (t == 'select-one');
			var max = (one ? index+1 : ops.length);
			for(var i=(one ? index : 0); i < max; i++) {
				var op = ops[i];
				if (op.selected) {
					var v = op.value;
					if (!v) // extra pain for IE...
						v = (op.attributes && op.attributes['value'] && !(op.attributes['value'].specified)) ? op.text : op.value;
					if (one) return v;
					a.push(v);
				}
			}
			return a;
		}
		return el.value;
	};

	
	/**
	 * formToObject() gathers form element data into an object
	 * ignore coordinates for <input type="image" /> elements
	 */
	$.fn.formToObject = function(ignoreClass) {
		var a = new Object();

		if (this.length == 0) return a;

		var form = this[0];
		var els = form.elements;
		if (!els) return a;
		
		for(var i=0, max=els.length; i < max; i++) {
			var el = els[i];
			var n = el.name;
			
			if (!n && $(el).hasClass(ignoreClass)) continue;
			
			var v = $.fieldValue(el, true);
						
			if ((v && v.constructor == Array) || (v !== null && typeof v != 'undefined')) {
				if(a[n]) {
					if(a[n].constructor != Array){
						var arr = new Array();
						arr.push(a[n]);
						a[n] = arr;
						a[n].push(v);
					}else{
						
						a[n].push(v);
					}
									
					
				}else{
					a[n] = v;
				}
			}
		}

		return a;
	};
	
	$.fn.clsToObject = function(cls){
		var o = {};
		var cs = $("."+cls);
		if (cs.length == 0) return o;
		for (var i=0;i<cs.length;i++){
			var v = $.fieldValue(cs[i], true);
			var n = jQuery(cs[i]).attr("name");
			if ((v && v.constructor == Array) || (v !== null && typeof v != 'undefined')) {
				if(o[n]) {
					if(o[n].constructor != Array){
						var arr = new Array();
						arr.push(o[n]);
						o[n] = arr;
						o[n].push(v);
					}else{
						o[n].push(v);
					}
				}else{
					o[n] = v;
				}
			} else {
				if (!o[n]) o[n] = null;
				//clog("clsToObject: Value from n='"+n+"'");
				//clog(v);
			}
		}
		return o;
	};
	
	
	$.fn.formToXmlByCls = function(cls){

		// Get a blank XML document if in IE, a blank document for other browsers.
		var rootDoc = createXmlDoc('form-root','');
		
		var els = $("."+cls);
		
		if (!els || els.length < 1 || this.length == 0) {
			return "";
		}
		
		for(var i=0, max=els.length; i < max; i++) {
			var el = els[i];
			var n = el.name;
			
			// Get leaf node attributes
			var attr = $(el).attr('data-attributes');
			if (attr == null && typeof attr == 'undefined') {
				eval("attr = {};");
			} else {
				// turn key-value string into a collection in JS
				// in: 'key':'value'
				//out: {'key:'value'}
				eval("attr = {"+attr+"};");
			}
			
			if (!n) continue;
			

			if (typeof exclude != 'undefined'){
				if($.inArray(n, exclude) > -1){
					continue;
				}
			}
			
			// Ignore 'form-question-hidden' class input values
			var v = $.fieldValue(el, true);

			if (v !== null && typeof v != 'undefined') {
				
				rootDoc = createDomFromXpath(rootDoc, n, v, attr);
				
			}
		}
		
		return xmlString("form-root",rootDoc.getElementsByTagName("form-root")[0]);
		
		function xmlString(rootNodeToRemove, doc){
			if (document.implementation && document.implementation.createDocument && window.XMLSerializer) {
				   // Firefox
				   xmlString = (new XMLSerializer()).serializeToString(doc);
				}
				else {
				   // IE
				   xmlString = doc.xml;
				}
			return xmlString.replace("<"+rootNodeToRemove+">", "").replace("</"+rootNodeToRemove+">","");
		}
		
		function createDomFromXpath(rootDoc, path, value, leafAttributes){
				
			
			var pathArr = removeEmptyElementFromArray(path.split("/"));

			clog(pathArr);
			
			var length = pathArr.length; 

			var rootDocPosition = rootDoc.getElementsByTagName("form-root")[0];
			
			for(var i = 0; i < length - 1; i ++){
				
				var name = pathArr[i];
				var c = rootDocPosition.getElementsByTagName(name);
				
					if(c.length > 0) {//exist
						
					}else{

						newElement = rootDoc.createElement(name);
						rootDocPosition.appendChild(newElement);
					}
					rootDocPosition = rootDocPosition.getElementsByTagName(name)[0];  // replaces "r=c"
				
			}
			
			var name = pathArr[length-1];
			//rootDocPosition = rootDoc.getElementsByTagName(name)[0];
			var lastElement = rootDoc.createElement(pathArr[length - 1]);
			
			
			if (!window.XMLSerializer) {
				if(Encoder) Encoder.EncodeType = "entity";
				else clog("[WARN] Encoder.htmlEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
			}
			$.each(leafAttributes, function(key, attrValue){
				newAttribute = rootDoc.createAttribute(key);
				
				if(!window.XMLSerializer) {
					if (Encoder && (typeof(Encoder.htmlEncode) == "function")) attrValue = Encoder.htmlEncode(attrValue);
					else clog("[WARN] Encoder.htmlEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
				}
				newAttribute.nodeValue = attrValue;
				
				lastElement.setAttributeNode(newAttribute);
			});
			if (!window.XMLSerializer) {
				if(Encoder && (typeof(Encoder.htmlEncode) == "function")) value = Encoder.htmlEncode(value);
				else clog("[WARN] Encoder.htmlEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
			}
			lastElementValue = rootDoc.createTextNode(value);
			lastElement.appendChild(lastElementValue);
			rootDocPosition.appendChild(lastElement);
			
			return rootDoc;
		}
		
		function exist(el){
			if(!el || el == null || el == "undefined"){
				return false;
			}else{
				if(typeof el == 'string') el = $.trim(el);
				if(el.length < 1){
					return false;
				}
				
				return true;
			}
		}
		
		function removeEmptyElementFromArray (arr){
			var length = arr.length;
			for(var i = 0; i < length; i ++ ){
				if(!exist(arr[i])){
					arr.splice(i, 1);
				}
			}
			
			return arr;
		};
		

		
		
		function createXmlDoc(rootTagName, namespaceURL) {
			  if (!rootTagName) rootTagName = "";
			  if (!namespaceURL) namespaceURL = "";
			  if (document.implementation && document.implementation.createDocument) {
			    // This is the W3C standard way to do it
			    return document.implementation.createDocument(namespaceURL, rootTagName, null);
			  }
			  else { // This is the IE way to do it
			    // Create an empty document as an ActiveX object
			    // If there is no root element, this is all we have to do
			    var doc = new ActiveXObject("MSXML2.DOMDocument");
			    // If there is a root tag, initialize the document
			    if (rootTagName) {
			      // Look for a namespace prefix
			      var prefix = "";
			      var tagname = rootTagName;
			      var p = rootTagName.indexOf(':');
			      if (p != -1) {
			        prefix = rootTagName.substring(0, p);
			        tagname = rootTagName.substring(p+1);
			      }
			      // If we have a namespace, we must have a namespace prefix
			      // If we don't have a namespace, we discard any prefix
			      if (namespaceURL) {
			        if (!prefix) prefix = "a0"; // What Firefox uses
			      }
			      else prefix = "";
			      // Create the root element (with optional namespace) as a
			      // string of text
			      var text = "<" + (prefix?(prefix+":"):"") +  tagname +
			          (namespaceURL
			           ?(" xmlns:" + prefix + '="' + namespaceURL +'"')
			           :"") +
			          "/>";
			      // And parse that text into the empty document
			      doc.loadXML(text);
			    }
			    return doc;
			  }

		}
	};
	
	/**
	 * formToXml() gathers form element data into xml String
	 * ignore coordinates for <input type="image" /> elements
	 */
	$.fn.formToXml = function(exclude) {
		// Get a blank XML document if in IE, a blank document for other browsers.
		var rootDoc = createXmlDoc('form-root','');

		var theForm = this[0];
		
		if (typeof theForm == 'undefined') {
			return '';
		}
		
		var els = theForm.elements;

		
		if (!els || els.length < 1 || this.length == 0) {
			return "";
		}

		if(typeof exclude != 'undefined' && exclude.length == els.length && exclude[0] == els[0].name){
			return "";
		}
		
		
		for(var i=0, max=els.length; i < max; i++) {
			var el = els[i];
			var n = el.name;
			
			// Get leaf node attributes
			var attr = $(el).attr('data-attributes');
			if (attr == null && typeof attr == 'undefined') {
				eval("attr = {};");
			} else {
				// turn key-value string into a collection in JS
				// in: 'key':'value'
				//out: {'key:'value'}
				eval("attr = {"+attr+"};");
			}
			
			if (!n) continue;
			

			if (typeof exclude != 'undefined'){
				if($.inArray(n, exclude) > -1){
					continue;
				}
			}
			
			// Ignore 'form-question-hidden' class input values
			var v = ($(el).hasClass('form-question-hidden'))?"":$.fieldValue(el, true);

			if (v !== null && typeof v != 'undefined') {
				
				// don't need to do this here...
				//if (!window.XMLSerializer && Encoder) v = Encoder.htmlEncode(v);
				//else cwarn("Encoder not defined. Add 'encoder.js' to this layout for field escaping.");
				
				rootDoc = createDomFromXpath(rootDoc, n, v, attr);
				
			}
		}
		
		var finalResult = xmlString("form-root",rootDoc.getElementsByTagName("form-root")[0]);
		return finalResult;
		
		function xmlString(rootNodeToRemove, doc){
			if (document.implementation && document.implementation.createDocument && window.XMLSerializer) {
				   // Firefox
				   xmlString = (new XMLSerializer()).serializeToString(doc);
				 
				}
				else {
				   // IE
				   xmlString = doc.xml;
				}
			return xmlString.replace("<"+rootNodeToRemove+">", "").replace("</"+rootNodeToRemove+">","");
		}
		
		function createDomFromXpath(rootDoc, path, value, leafAttributes){
				
			
			var pathArr = removeEmptyElementFromArray(path.split("/"));

			//clog(pathArr);
			
			var length = pathArr.length; 

			var rootDocPosition = rootDoc.getElementsByTagName("form-root")[0];
			
			for(var i = 0; i < length - 1; i ++){
				
				var name = pathArr[i];
				var c = rootDocPosition.getElementsByTagName(name);
				
					if(c.length > 0) {//exist
						
					}else{

						newElement = rootDoc.createElement(name);
						rootDocPosition.appendChild(newElement);
					}
					rootDocPosition = rootDocPosition.getElementsByTagName(name)[0];  // replaces "r=c"
				
			}
			
			var name = pathArr[length-1];
			//rootDocPosition = rootDoc.getElementsByTagName(name)[0];
			var lastElement = rootDoc.createElement(pathArr[length - 1]);
			
			
				if(Encoder) Encoder.EncodeType = "entity";
				else clog("[WARN] Encoder.numEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
			
			
			$.each(leafAttributes, function(key, attrValue){
				newAttribute = rootDoc.createAttribute(key);
				
				
					if(Encoder && (typeof(Encoder.numEncode) == "function")) attrValue = Encoder.numEncode(attrValue);
					else clog("[WARN] Encoder.numEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
				
				newAttribute.nodeValue = attrValue;
				
				lastElement.setAttributeNode(newAttribute);
			});
		
				if (Encoder && (typeof(Encoder.numEncode) == "function")) value = Encoder.numEncode(value);
				else clog("[WARN] Encoder.numEncode not defined. Make sure layout includes encoder.js to prevent UTF8 errors.");
			
			lastElementValue = rootDoc.createTextNode(value);
			lastElement.appendChild(lastElementValue);
			rootDocPosition.appendChild(lastElement);
			
			return rootDoc;
		}
		
		function exist(el){
			if(!el || el == null || el == "undefined"){
				return false;
			}else{
				if(typeof el == 'string') el = $.trim(el);
				if(el.length < 1){
					return false;
				}
				
				return true;
			}
		}
		
		function removeEmptyElementFromArray (arr){
			var length = arr.length;
			for(var i = 0; i < length; i ++ ){
				if(!exist(arr[i])){
					arr.splice(i, 1);
				}
			}
			
			return arr;
		};
		

		
		
		function createXmlDoc(rootTagName, namespaceURL) {
			  if (!rootTagName) rootTagName = "";
			  if (!namespaceURL) namespaceURL = "";
			  if (document.implementation && document.implementation.createDocument) {
			    // This is the W3C standard way to do it
			    return document.implementation.createDocument(namespaceURL, rootTagName, null);
			  }
			  else { // This is the IE way to do it
			    // Create an empty document as an ActiveX object
			    // If there is no root element, this is all we have to do
			    var doc = new ActiveXObject("MSXML2.DOMDocument");
			    // If there is a root tag, initialize the document
			    if (rootTagName) {
			      // Look for a namespace prefix
			      var prefix = "";
			      var tagname = rootTagName;
			      var p = rootTagName.indexOf(':');
			      if (p != -1) {
			        prefix = rootTagName.substring(0, p);
			        tagname = rootTagName.substring(p+1);
			      }
			      // If we have a namespace, we must have a namespace prefix
			      // If we don't have a namespace, we discard any prefix
			      if (namespaceURL) {
			        if (!prefix) prefix = "a0"; // What Firefox uses
			      }
			      else prefix = "";
			      // Create the root element (with optional namespace) as a
			      // string of text
			      var text = "<" + (prefix?(prefix+":"):"") +  tagname +
			          (namespaceURL
			           ?(" xmlns:" + prefix + '="' + namespaceURL +'"')
			           :"") +
			          "/>";
			      // And parse that text into the empty document
			      doc.loadXML(text);
			    }
			    return doc;
			  }

		}
	}
	
})(jQuery);