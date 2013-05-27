/**
 * This file contains helper methods for the olatcore web app framework and the
 * learning management system OLAT
 */

/** OpenOLAT namespace **/
OPOL = {};

//used to mark form dirty and warn user to save first.
var o2c=0;
var o3c=new Array();//array holds flexi.form id's
// o_info is a global object that contains global variables
o_info.guibusy = false;
o_info.linkbusy = false;
//debug flag for this file, to enable debugging to the olat.log set JavaScriptTracingController to level debug
o_info.debug = true;

/**
 * The BLoader object can be used to :
 * - dynamically load and unload CSS files
 * - dynamically load JS files
 * - execute javascript code in a global context, meaning on window level
 *
 * 03.04.2009 gnaegi@frentix.com 
 */
var BLoader = {
	// List of js files loaded via AJAX call.
	_ajaxLoadedJS : new Array(),
		
	// Internal mehod to check if a JS file has already been loaded on the page
	_isAlreadyLoadedJS: function(jsURL) {
		var notLoaded = true;
		// first check for scrips loaded via HTML head
		jQuery('head script[src]').each(function(s,t) {
			if (jQuery(t).attr('src').indexOf(jsURL) != -1) {
				notLoaded = false;
			};
		});
		// second check for script loaded via ajax call
		if (jQuery.inArray(jsURL, this._ajaxLoadedJS) != -1) notLoaded = false;
		return !notLoaded;
	},
		
	// Load a JS file from an absolute or relative URL by using the given encoding. The last flag indicates if 
	// the script should be loaded using an ajax call (recommended) or by adding a script tag to the document 
	// head. Note that by using the script tag the JS script will be loaded asynchronous 
	loadJS : function(jsURL, encoding, useSynchronousAjaxRequest) {
		if (!this._isAlreadyLoadedJS(jsURL)) {		
			if (o_info.debug) o_log("BLoader::loadJS: loading ajax::" + useSynchronousAjaxRequest + " url::" + jsURL);
			if (useSynchronousAjaxRequest) {
				jQuery.ajax(jsURL, {
					async: false,
					dataType: 'script',
					success: function(script, textStatus, jqXHR) {
						//BLoader.executeGlobalJS(script, 'loadJS');
					}
				});
				this._ajaxLoadedJS.push(jsURL);
			} else {
				jQuery.getScript(jsURL);			
			}
			if (o_info.debug) o_log("BLoader::loadJS: loading DONE url::" + jsURL);
		} else {
			if (o_info.debug) o_log("BLoader::loadJS: already loaded url::" + jsURL);			
		}
	},

	// Execute the given string as java script code in a global context. The contextDesc is a string that can be 
	// used to describe execution context verbally, this is only used to improve meaninfull logging
	executeGlobalJS : function(jsString, contextDesc) {
		try{
			// FIXME:FG refactor as soon as global exec available in prototype
			// https://prototype.lighthouseapp.com/projects/8886/tickets/433-provide-an-eval-that-works-in-global-scope 
			if (window.execScript) window.execScript(jsString); // IE style
			else window.eval(jsString);
		} catch(e){
			if(console) console.log(contextDesc, 'cannot execute js', jsString);
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::executeGlobalJS: Error when executing JS code in contextDesc::' + contextDesc + ' error::"'+showerror(e)+' for: '+escape(jsString));
			}
			if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug','BLoader::executeGlobalJS: Error when executing JS code in contextDesc::' + contextDesc + ' error::"'+showerror(e)+' for: '+escape(jsString), "functions.js::BLoader::executeGlobalJS::" + contextDesc);
			// Parsing of JS script can fail in IE for unknown reasons (e.g. tinymce gets 8002010 error)
			// Try to do a 'full page refresh' and load everything via page header, this normally works
			if (window.location.href.indexOf('o_winrndo') != -1) window.location.reload();
			else window.location.href = window.location.href + (window.location.href.indexOf('?') != -1 ? '&' : '?' ) + 'o_winrndo=1';
		}		
	},
	
	// Load a CSS file from the given URL. The linkid represents the DOM id that is used to identify this CSS file
	loadCSS : function (cssURL, linkid, loadAfterTheme) {
		var doc = window.document;
		try {
			if(doc.createStyleSheet) { // IE
				// double check: server side should do so, but to make sure that we don't have duplicate styles
				var sheets = doc.styleSheets;
				var cnt = 0;
				var pos = 0;
				for (i = 0; i < sheets.length; i++) {
					var sh = sheets[i];
					var h = sh.href; 
					if (h == cssURL) {
						cnt++;
						if (sh.disabled) {
							// enable a previously disabled stylesheet (ie cannot remove sheets? -> we had to disable them)
							sh.disabled = false;
							return;
						} else {
							if (o_info.debug) o_logwarn("BLoader::loadCSS: style: "+cssURL+" already in document and not disabled! (duplicate add)");
							return;
						}
					}
					// add theme position, theme has to move one down
					if (sh.id == 'b_theme_css') pos = i;
				}
				if (cnt > 1 && o_info.debug) o_logwarn("BLoader::loadCSS: apply styles: num of stylesheets found was not 0 or 1:"+cnt);
				if (loadAfterTheme) {
					// add at the end
					pos = sheets.length;
				}
				// H: stylesheet not yet inserted -> insert				
				var mystyle = doc.createStyleSheet(cssURL, pos);
			} else { // mozilla
				// double check: first try to remove the <link rel="stylesheet"...> tag, using the id.
				var el = jQuery('#' +linkid);
				if (el && el.size() > 0) {
					if (o_info.debug) o_logwarn("BLoader::loadCSS: stylesheet already found in doc when trying to add:"+cssURL+", with id "+linkid);
					return;
				} else {
					// create the new stylesheet and convince the browser to load the url using @import with protocol 'data'
					//var styles = '@import url("'+cssURL+'");';
					//var newSt = new Element('link', {rel : 'stylesheet', id : linkid, href : 'data:text/css,'+escape(styles) });
					var newSt = jQuery('<link id="' + linkid + '" rel="stylesheet" type="text/css" href="' + cssURL+ '">');
					if (loadAfterTheme) {
						newSt.insertBefore(jQuery('#b_fontSize_css'));
					} else {
						newSt.insertBefore(jQuery('#b_theme_css'));
					}
				}
			}
		} catch(e){
			if(console)  console.log(e);
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::loadCSS: Error when loading CSS from URL::' + cssURL);
			}
			if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug','BLoader::loadCSS: Error when loading CSS from URL::' + cssURL, "functions.js::BLoader::loadCSS");
		}				
	},

	// Unload a CSS file from the given URL. The linkid represents the DOM id that is used to identify this CSS file
	unLoadCSS : function (cssURL, linkid) {
		var doc = window.document;
		try {
			if(doc.createStyleSheet) { // IE
				var sheets = doc.styleSheets;
				var cnt = 0;
				// calculate relative style url because IE does keep only a 
				// relative URL when the stylesheet is loaded from a relative URL
				var relCssURL = cssURL;
				// calculate base url: protocol, domain and port https://your.domain:8080
				var baseURL = window.location.href.substring(0, window.location.href.indexOf("/", 8)); 
				if (cssURL.indexOf(baseURL) == 0) {
					//remove the base url form the style url
					relCssURL = cssURL.substring(baseURL.length);
				}
				for (i = 0; i < sheets.length; i++) {
					var h = sheets[i].href;
					if (h == cssURL || h == relCssURL) {
						cnt++;
						if (!sheets[i].disabled) {
							sheets[i].disabled = true; // = null;
						} else {
							if (o_info.debug) o_logwarn("stylesheet: when removing: matching url, but already disabled! url:"+h);
						}
					}
				}
				if (cnt != 1 && o_info.debug) o_logwarn("stylesheet: when removeing: num of stylesheets found was not 1:"+cnt);
				
			} else { // mozilla
				var el = jQuery('#' +linkid);
				if (el) {
					el.href = ""; // fix unload problem in safari
					el.remove();
					el = null;
					return;
				} else {
					if (o_info.debug) o_logwarn("no link with id found to remove, id:"+linkid+", url "+cssURL);
				}
			}
		} catch(e){
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::unLoadCSS: Error when unloading CSS from URL::' + cssURL);
			}
			if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug','BLoader::unLoadCSS: Error when unloading CSS from URL::' + cssURL, "functions.js::BLoader::loadCSS");
		}				
	}
};

/**
 * The BFormatter object can be used to :
 * - formatt latex formulas using jsMath
 *
 * 18.06.2009 gnaegi@frentix.com 
 */
var BFormatter = {
	// process element with given dom id using jsmath
	formatLatexFormulas : function(domId) {
		try {
			if (jsMath) { // only when js math available
				if (jsMath.loaded) {
					setTimeout(function() {
						jQuery('#' + domId).each(function(index, el){
							jsMath.ProcessBeforeShowing(el);
						});
					}, 10);					
				} else { // not yet loaded (autoload), load first
					jsMath.Autoload.LoadJsMath();
					// retry formatting when ready (recursively until loaded)
					setTimeout(function() {
						BFormatter.formatLatexFormulas(domId);
					}, 100);
				}
			}
		} catch(e) {
			if (console) console.log("error in BFormatter.formatLatexFormulas: ", e);
		}
	}
};


function o_init() {
	try {
		// all init-on-new-page calls here
		//return opener window
		o_getMainWin().o_afterserver();	
	} catch(e) {
		if (o_info.debug) o_log("error in o_init: "+showerror(e));
	}	
}

function b_initEmPxFactor() {
	// read px value for 1 em from hidden div
	o_info.emPxFactor = jQuery('#b_width_1em').width();
	if (o_info.emPxFactor == 0 || o_info.emPxFactor == 'undefined') {
		o_info.emPxFactor = 12; // default value for all strange settings
		if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug','Could not read with of element b_width_1em, set o_info.emPxFactor to 12', "functions.js");
	}
}

function o_getMainWin() {
	try {
		if (window.opener && window.opener.OPOL) {
			// use the opener when opener window is an OpenOLAT window
			return window.opener;
		} else if (window.OPOL) {
			// other cases the current window is the main window
			return window;
		}
	} catch (e) {
		if (o_info.debug) { // add webbrowser console log
			o_logerr('Exception while getting main window. rror::"'+showerror(e));
		}
		if (B_AjaxLogger.isDebugEnabled()) { // add ajax logger
			B_AjaxLogger.logDebug('Exception while getting main window. rror::"'+showerror(e), "functions.js");
		}	
	}
	throw "Can not find main OpenOLAT window";
}


function o_beforeserver() {
//mal versuche mit jQuery().ready.. erst dann wieder clicks erlauben...
	o_info.linkbusy = true;
	showAjaxBusy();
	// execute iframe specific onunload code on the iframe
	if (window.suppressOlatOnUnloadOnce) {
		// don't call olatonunload this time, reset variable for next time
		window.suppressOlatOnUnloadOnce = false;
	} else if (window.olatonunload) {
		olatonunload();
	}
}

function o_afterserver() {
	o2c = 0;
	o_info.linkbusy = false;
	removeAjaxBusy();
}

function o2cl() {
	if (o_info.linkbusy) {
		return false;
	} else {
		var doreq = (o2c==0 || confirm(o_info.dirty_form));
		if (doreq) o_beforeserver();
		return doreq;
	}
}

function o3cl(formId) {
	if (o_info.linkbusy) {
		return false;
	} else {
		//detect if another flexi form on the screen is dirty too
		var isRegistered = o3c1.indexOf(formId) > -1;
		var flexiformdirty = (isRegistered && o3c1.length > 1) || o3c1.length > 0;
		//check if no other flexi form is dirty
		//otherwise ask if changes should be discarded.
		var doreq = ( !flexiformdirty || confirm(o_info.dirty_form));
		if (doreq) o_beforeserver();
		return doreq;
	}
}

// on ajax poll complete
function o_onc(response) {
	var te = response.responseText;
	BLoader.executeGlobalJS("o_info.last_o_onc="+te+";", 'o_onc');
	//asynchronous! from polling
	o_ainvoke(o_info.last_o_onc,false);
}

function o_allowNextClick() {
	o_info.linkbusy = false;
	removeAjaxBusy();
}

//remove busy after clicking a download link in non-ajax mode
//use LinkFactory.markDownloadLink(Link) to make a link call this method.
function removeBusyAfterDownload(e,target,options){
	o2c = 0;
	o_afterserver();
}

Array.prototype.search = function(s,q){
  var len = this.length;
  for(var i=0; i<len; i++){
    if(this[i].constructor == Array){
      if(this[i].search(s,q)){
        return true;
        break;
      }
     } else {
       if(q){
         if(this[i].indexOf(s) != -1){
           return true;
           break;
         }
      } else {
        if(this[i]==s){
          return true;
          break;
        }
      }
    }
  }
  return false;
}

if(!Function.prototype.curry) {
	Function.prototype.curry = function() {
	    if (arguments.length<1) {
	        return this; //nothing to curry with - return function
	    }
	    var __method = this;
	    var args = Array.prototype.slice.call(arguments);
	    return function() {
	        return __method.apply(this, args.concat(Array.prototype.slice.call(arguments)));
	    }
	}
}

if(!Array.prototype.indexOf) {
	Array.prototype.indexOf = function (searchElement /*, fromIndex */ ) {
		"use strict";
		if (this == null) {
			throw new TypeError();
        }
        var t = Object(this);
        var len = t.length >>> 0;
        if (len === 0) {
            return -1;
        }
        var n = 0;
        if (arguments.length > 1) {
            n = Number(arguments[1]);
            if (n != n) { // shortcut for verifying if it's NaN
                n = 0;
            } else if (n != 0 && n != Infinity && n != -Infinity) {
                n = (n > 0 || -1) * Math.floor(Math.abs(n));
            }
        }
        if (n >= len) {
            return -1;
        }
        var k = n >= 0 ? n : Math.max(len - Math.abs(n), 0);
        for (; k < len; k++) {
            if (k in t && t[k] === searchElement) {
                return k;
            }
        }
        return -1;
	}
}


// b_AddOnDomReplacementFinishedCallback is used to add callback methods that are executed after
// the DOM replacement has occured. Note that when not in AJAX mode, those methods will not be 
// executed. Use this callback to execute some JS code to cleanup eventhandlers or alike
var b_onDomReplacementFinished_callbacks=new Array();//array holding js callback methods that should be executed after the next ajax call
function b_AddOnDomReplacementFinishedCallback(funct) {
	var debug = jQuery(document).ooLog().isDebugEnabled();
	
	if(debug) jQuery(document).ooLog('debug',"callback stack size: " + b_onDomReplacementFinished_callbacks.length, "functions.js ADD"); 
	if (debug && b_onDomReplacementFinished_callbacks.toSource) {
		jQuery(document).ooLog('debug',"stack content"+b_onDomReplacementFinished_callbacks.toSource(), "functions.js ADD")
	};

	b_onDomReplacementFinished_callbacks.push(funct);
	if(debug) jQuery(document).ooLog('debug',"push to callback stack, func: " + funct, "functions.js ADD");
}
//fxdiff FXOLAT-310 
var b_changedDomEl=new Array();

//same as above, but with a filter to prevent adding a funct. more than once
//funct then has to be an array("identifier", funct) 
function b_AddOnDomReplacementFinishedUniqueCallback(funct) {
	if (funct.constructor == Array){
		if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"add: its an ARRAY! ", "functions.js ADD"); 
		//check if it has been added before
		if (b_onDomReplacementFinished_callbacks.search(funct[0])){
			if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"push to callback stack, already there!!: " + funct[0], "functions.js ADD");		
			return;
		} 
	}
	b_AddOnDomReplacementFinishedCallback(funct);
}

// main interpreter for ajax mode
var o_debug_trid = 0;
function o_ainvoke(r) {
	// commands
	if(r == undefined) {
		return;
	}
	
	o_info.inainvoke = true;
	var cmdcnt = r["cmdcnt"];
	if (cmdcnt > 0) {
		//fxdiff FXOLAT-310 
		b_changedDomEl = new Array();
		
		if (o_info.debug) { o_debug_trid++; }
		var cs = r["cmds"];
		for (var i=0; i<cmdcnt; i++) {
			var acmd = cs[i];
			var co = acmd["cmd"];
			var cda = acmd["cda"];
			var wid = acmd["w"];
			var wi = this.window; // for cross browser window: o_info.wins[wid]; 
			var out;
			if (wi) {
				switch (co) {
					case 1: // Excecute JavaScript Code
						var jsexec = cda["e"];
						BLoader.executeGlobalJS(jsexec, 'o_ainvoker::jsexec');
						if (o_info.debug) o_log("c1: execute jscode: "+jsexec);
					case 2:  // redraw components command
						var cnt = cda["cc"];
						var ca = cda["cps"];
						for (var j=0;  j<cnt; j++) {
							var c1 = ca[j];
							var ciid = c1["cid"]; // component id
							var civis = c1["cidvis"];// component visibility
							var hfrag = c1["hfrag"]; // html fragment of component
							var jsol = c1["jsol"]; // javascript on load
							var hdr = c1["hdr"]; // header
							if (o_info.debug) o_log("c2: redraw: "+c1["cname"]+ " ("+ciid+") "+c1["hfragsize"]+" bytes, listener(s): "+c1["clisteners"]);
							//var con = jQuery(hfrag).find('script').remove(); //Strip scripts
							var hdrco = hdr+"\n\n"+hfrag;
							var inscripts = '';//jQuery(hfrag).find('script');//hfrag.extractScripts();
							
							var replaceElement = false;
							var newc = jQuery("#o_c"+ciid);
							if (newc == null || (newc.length == 0)) {
								//not a container, perhaps an element
								newc = jQuery("#o_fi"+ciid);
								replaceElement = true;
							} 
							if (newc != null) {
								if(civis){ // needed only for ie 6/7 bug where an empty div requires space on screen
									newc.css('display','');//.style.display="";//reset?
								}else{
									newc.css('display','none'); //newc.style.display="none";
								}
								// do dom replacement
								// remove listeners !! ext overwrite or prototype replace does NOT remove listeners !!
//								newc.descendants().each(function(el){if (el.stopObserving) el.stopObserving()});
								
								if(replaceElement) {
									newc.replaceWith(hdrco);	
								} else {
									newc.empty();
									try{
										newc.html(hdrco);//Ext.DomHelper.overwrite(newc, hdrco, false);
									} catch(e) {
										if(console) console.log(e);
										if(console) console.log('Fragment',hdrco);
									}
									b_changedDomEl.push('o_c'+ciid);
								}
								newc = null;
								
								// exeucte inline scripts
								if (inscripts != "") {
									inscripts.each( function(val){
										BLoader.executeGlobalJS(val, 'o_ainvoker::inscripts');}
									);
								}
								if (jsol != "") {
									BLoader.executeGlobalJS(jsol, 'o_ainvoker::jsol');
								}
							}
						}
						break;
					case 3:  // createParentRedirectTo leads to a full page reload
						wi.o2c = 0;//??
						var rurl = cda["rurl"];
						wi.o_afterserver();
						wi.document.location.replace(rurl);
						break;
					case 5: // create redirect for external resource mapper
						wi.o2c = 0;//??
						var rurl = cda["rurl"];
						//in case of a mapper served media resource (xls,pdf etc.)
						wi.o_afterserver();
						wi.document.location.replace(rurl);//opens non-inline media resource
						break;
					case 6: // createPrepareClientCommand
						wi.o2c = 0;
						wi.o_afterserver();
						break;
					case 7: // JSCSS: handle dynamic insertion of js libs and dynamic insertion/deletion of css stylesheets
						// css remove, add, js add order should makes no big difference? except js calling/modifying css? 
						var loc = wi.document.location;
						var furlp = loc.protocol+"//"+loc.hostname; // e.g. http://my.server.com:8000
						if (loc.port != "" ) furlp += ":"+ loc.port; 
						// 1. unload css file
						var cssrm = cda["cssrm"];
						for (j = 0; j<cssrm.length; j++) {
							var ce = cssrm[j];
							var id = ce["id"];
							var url = furlp + ce["url"];
							BLoader.unLoadCSS(url, id);
							if (o_info.debug) o_log("c7: rm css: id:"+id+" ,url:'"+url+"'");
						}
						// 2) load css file
						var cssadd = cda["cssadd"];
						for (k = 0; k<cssadd.length; k++) {
							var ce = cssadd[k];
							var id = ce["id"];
							var url = furlp + ce["url"];
							var pt = ce["pt"];
							BLoader.loadCSS(url,id,pt);
							if (o_info.debug) o_log("c7: add css: id:"+id+" ,url:'"+url+"'");
						}
						
						// 3) js lib adds
						var jsadd = cda["jsadd"];
						for (l=0; l<jsadd.length; l++) {
							var ce = jsadd[l];
							// 3.1) execute before AJAX-code
							var preJsAdd = ce["before"];
							if (jQuery.type(preJsAdd) === "string") {
								BLoader.executeGlobalJS(preJsAdd, 'o_ainvoker::preJsAdd');
							}
							// 3.2) load js file
							var url = ce["url"];
							var enc = ce["enc"];
							if (jQuery.type(url) === "string") BLoader.loadJS(url, enc, true);
							if (o_info.debug) o_log("c7: add js: "+url);
						}	
						break;	
					default:
						if (o_info.debug) o_log("?: unknown command "+co); 
					if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"Error in o_ainvoke(), ?: unknown command "+co, "functions.js");
						break;
				}		
			} else {
				if (o_info.debug) o_log ("could not find window??");
				if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"Error in o_ainvoke(), could not find window??", "functions.js");
			}		
		}
		// execute onDomReplacementFinished callback functions
		var stacklength = b_onDomReplacementFinished_callbacks.length;
		if (b_onDomReplacementFinished_callbacks.toSource && jQuery(document).ooLog().isDebugEnabled()) { 
			jQuery(document).ooLog('debug',"stack content"+b_onDomReplacementFinished_callbacks.toSource(), "functions.js");
		}
		
		for (mycounter = 0; stacklength > mycounter; mycounter++) {
			
			if (mycounter > 50) {
				if(jQuery(document).ooLog().isDebugEnabled()) {
					jQuery(document).ooLog('debug',"Stopped executing DOM replacement callback functions - to many functions::" + b_onDomReplacementFinished_callbacks.length, "functions.js");
				}
				break; // emergency break
			}
			if(jQuery(document).ooLog().isDebugEnabled()) {
				jQuery(document).ooLog('debug',"Stacksize before shift: " + b_onDomReplacementFinished_callbacks.length, "functions.js");
			}
			var func = b_onDomReplacementFinished_callbacks.shift();
			if (typeof func.length === 'number'){
				if (func[0] == "glosshighlighter") {
					var tmpArr = func[1];
					if(jQuery(document).ooLog().isDebugEnabled())
						jQuery(document).ooLog('debug',"arr fct: "+ tmpArr, "functions.js");
					func = tmpArr;
				 }				
			}
			if(jQuery(document).ooLog().isDebugEnabled())
				jQuery(document).ooLog('debug',"Executing DOM replacement callback function #" + mycounter + " with timeout funct::" + func, "functions.js");
			// don't use execScript here - must be executed outside this function scope so that dom replacement elements are available
			
			//func.delay(0.01);
			func();//TODO jquery
			
			if(jQuery(document).ooLog().isDebugEnabled())
				jQuery(document).ooLog('debug',"Stacksize after timeout: " + b_onDomReplacementFinished_callbacks.length, "functions.js");
		}
	}
	
	o_info.inainvoke = false;
	
/* minimalistic debugger / profiler	
	BDebugger.logDOMCount();
	BDebugger.logGlobalObjCount();
	BDebugger.logGlobalOLATObjects();
	BDebugger.logManagedOLATObjects();
*/
}

/**
 * Method to remove the ajax-busy stuff and let the user click links again. This
 * should only be called from the ajax iframe onload method to make sure the UI
 * does not freeze when the server for whatever reason does not respond as expected.
 */
function clearAfterAjaxIframeCall() {
	if (o_info.linkbusy) {
		// A normal ajax call will clear the linkbusy, so something went wrong in 
		// the ajax channel, e.g. error message from apache or no response from server
		// Call afterserver to remove busy icon clear the linkbusy flag
		o_afterserver();
		showMessageBox('info', o_info.i18n_noresponse_title, o_info.i18n_noresponse, undefined);
	}
}

function showAjaxBusy() {
	// release o_info.linkbusy only after a successful server response 
	// - otherwhise the response gets overriden by next request
	setTimeout(function(){
		if (o_info.linkbusy) {
			// try/catch because can fail in full page refresh situation when called before DOM is ready
			try {
				jQuery('#b_ajax_busy').each(function(index, el) {
					jQuery(el).addClass('b_ajax_busy');
					jQuery('#b_body').addClass('b_ajax_busy');
				});
			} catch (e) {
				if(console) console.log(e);
			}
		}
	}, 500);
}

function removeAjaxBusy() {
	// try/catch because can fail in full page refresh situation when called before page DOM is ready
	try {
		jQuery('#b_ajax_busy').each(function(index, el) {
			jQuery(el).removeClass('b_ajax_busy');
			jQuery('#b_body').removeClass('b_ajax_busy');
		});
	} catch (e) {
		if(console) console.log(e);
	}
}

function setFormDirty(formId) {
	// sets dirty form content flag to true and renders the submit button
	// of the form with given dom id as dirty.
	// (fg) 
	o2c=1;
	// fetch the form and the forms submit button is identified via the olat 
	// form submit name
	var myForm = document.getElementById(formId);
	//TODO:gs:a why not directly accessing the submit button by an id. name="olat_fosm" send additional parameter which is unused. OLAT-1363
	if (myForm != null) {
		var mySubmit = myForm.olat_fosm_0;
		if(mySubmit == null){
			mySubmit = myForm.olat_fosm;
		}
		// set dirty css class
		if(mySubmit) mySubmit.className ="b_button b_button_dirty";
	} else if(jQuery(document).ooLog().isDebugEnabled()) {
		jQuery(document).ooLog('debug',"Error in setFormDirty, myForm was null for formId=" + formId, "functions.js");
	}
}


//Pop-up window for context-sensitive help
function contextHelpWindow(URI) {
	helpWindow = window.open(URI, "HelpWindow", "height=760, width=940, left=0, top=0, location=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no");
	helpWindow.focus();
}

//TODO: for 5.3 add popup capability to link and table
function o_openPopUp(url, windowname, width, height, menubar) {
	// generic window popup function
	attributes = "height=" + height + ", width=" + width + ", resizable=yes, scrollbars=yes, left=100, top=100, ";
	if (menubar) {
		attributes += "location=yes, menubar=yes, status=yes, toolbar=yes";
	} else {
		attributes += "location=no, menubar=no, status=no, toolbar=no";
	}
	var win = window.open(url, windowname, attributes);
	win.focus();
}

function b_togglebox(domid, toggler) {
	// toggle the domid element and switch the toggler classes
	jQuery('#'+domid).slideToggle(400, function() {
		var togglerEl = jQuery(toggler);
		togglerEl.toggleClass('b_togglebox_closed');
		togglerEl.toggleClass('b_togglebox_opened');
	});
}

function b_handleFileUploadFormChange(fileInputElement, fakeInputElement, saveButton) {
	// file upload forms are rendered transparent and have a fake input field that is rendered.
	// on change events of the real input field this method is triggered to display the file 
	// path in the fake input field. See the code for more info on this
	var fileName = fileInputElement.value;
	// remove unix path
	slashPos = fileName.lastIndexOf('/');
	if (slashPos != -1) {
		fileName=fileName.substring(slashPos + 1); 
	}
	// remove windows path
	slashPos = fileName.lastIndexOf('\\');	
	if (slashPos != -1) {
		fileName=fileName.substring(slashPos + 1); 
	}
	fakeInputElement.value=fileName;
	// mark save button as dirty
	if (saveButton) {
		saveButton.className='b_button b_button_dirty'
	}
	// set focus to next element if available
	var elements = fileInputElement.form.elements;
	for (i=0; i < elements.length; i++) {
		var elem = elements[i];
		if (elem.name == fakeInputElement.name && i+1 < elements.length) {
			elements[i+1].focus();
		}
	}
}

// goto node must be in global scope to support content that has been opened in a new window 
// with the clone controller - real implementation is moved to course run scope o_activateCourseNode()
function gotonode(nodeid) {
	try {
		// check if o_activateCourseNode method is available in this window
		if (typeof o_activateCourseNode != 'undefined') {
			o_activateCourseNode(nodeid);
		} else {
			// must be content opened using the clone controller - search in opener window
			if (opener && typeof opener.o_activateCourseNode != 'undefined') {
			  opener.o_activateCourseNode(nodeid);
			} else if(jQuery(document).ooLog().isDebugEnabled()) {
				jQuery(document).ooLog('debug',"Error in gotonode(), could not find main window", "functions.js");
			}			
		}
	} catch (e) {
		alert('Goto node error:' + e);
		if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"Error in gotonode()::" + e.message, "functions.js");
	}
}


function o_openUriInMainWindow(uri) {
	// get the "olatmain" window
	try {
		var w = o_getMainWin();
		w.focus();
		w.location.replace(uri);		
	} catch (e) {
		showMessageBox("error", "Error", "Can not find main OpenOLAT window to open URL.");		
	}
}

function b_viewportHeight() {
	// based on prototype library
	var prototypeViewPortHeight = jQuery(document).height()
	if (prototypeViewPortHeight > 0) {
		return prototypeViewPortHeight;
	} else {
		return 600; // fallback
	}
}


/**
 *  calculate the height of the inner content area that can be used for 
 *  displaying content without using scrollbars.
 *  @dependencies: prototype library, ExtJS
 *  @author: Florian Gnaegi
 */
OPOL.getMainColumnsMaxHeight =  function(){
	var col1Height = 0,
	col2Height = 0,
	col3Height = 0,
	mainInnerHeight = 0,
	mainHeight = 0,
	mainDomElement,
	col1DomElement = jQuery('#b_col1_content'),
	col2DomElement = jQuery('#b_col2_content'),
	col3DomElement = jQuery('#b_col3_content');
	
	if (col1DomElement != 'undefined' && col1DomElement != null) {
		col1Height = col1DomElement.height();
	}
	if (col2DomElement != 'undefined' && col2DomElement != null){
		col2Height = col2DomElement.height();
	}
	if (col3DomElement != 'undefined' && col3DomElement != null){
		col3Height = col3DomElement.height();
	}

	mainInnerHeight = (col1Height > col2Height ? col1Height : col2Height);
	mainInnerHeight = (mainInnerHeight > col3Height ? mainInnerHeight : col3Height);
	if (mainInnerHeight > 0) {
		return mainInnerHeight;
	} 
	
	// fallback, try to get height of main container
	mainDomElement = jQuery('#b_main');
	if (mainDomElement != 'undefined' && mainDomElement != null) { 
		mainHeight = mainDomElement.height();
	}
	if (mainDomElement > 0) {
		return mainDomElement;
	} 
	// fallback to viewport height	
	return b_viewportHeight();
};

  
function b_resizeIframeToMainMaxHeight(iframeId) {
	// adjust the given iframe to use as much height as possible
	// (fg)
	var theIframe = jQuery('#' + iframeId);
	if (theIframe != 'undefined' && theIframe != null) {
		var colsHeight = OPOL.getMainColumnsMaxHeight();
		
		var potentialHeight = b_viewportHeight() - 100;// remove some padding etc.
		var elem = jQuery('#b_header');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.height();
		elem = jQuery('#b_nav');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.height();
		elem = jQuery('#b_footer');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.height();
		// resize now
		var height = (potentialHeight > colsHeight ? potentialHeight : colsHeight);
		theIframe.height(height);
	}
}
// for gui debug mode
var o_debu_oldcn, o_debu_oldtt;

function o_debu_show(cn, tt) {
	if (o_debu_oldcn){
		o_debu_hide(o_debu_oldcn, o_debu_oldtt);
	}
	jQuery(cn).css('border','3px solid #00F').css('margin','0px').css('background-color','#FCFCB8');
	jQuery(tt).show();

	o_debu_oldtt = tt;
	o_debu_oldcn = cn;
}

function o_debu_hide(cn, tt) {
	jQuery(tt).hide();
	jQuery(cn).css('border','1px dotted black').css('margin','2px').css('background-color','');
}

function o_dbg_mark(elid) {
	var el = jQuery('#' + elid);
	if (el) {
		el.css('background-color','#FCFCB8');
		el.css('border','3px solid #00F'); 
	}
}

function o_dbg_unmark(elid) {
	var el = jQuery('#' + elid);
	if (el) {
		el.css('border',''); 
		el.css('background-color','');
	}
}

function o_clearConsole() {
 o_log_all="";
 o_log(null);
}

var o_log_all = "";
function o_log(str) {
	if (str) {	
		o_log_all = "\n"+o_debug_trid+"> "+str + o_log_all;
		o_log_all = o_log_all.substr(0,4000);
	}
	var logc = jQuery("#o_debug_cons");
	if (logc) {
		if (o_log_all.length == 4000) o_log_all = o_log_all +"\n... (stripped: to long)... ";
		logc.value = o_log_all;
	}
	if(!jQuery.type(window.console) === "undefined"){
		//firebug log window
		window.console.log(str);
	}
}

function o_logerr(str) {
	o_log("ERROR:"+str);
}

function o_logwarn(str) {
	o_log("WARN:"+str);
}


function showerror(e) {
	var r = "";
    for (var p in e) r += p + ": " + e[p] + "\n";
    return "error detail:\n"+r;
}




// Each flexible.form item with an javascript 'on...' configured calls this fn.
// It is called at least if a flexible.form is submitted.
// It submits the component id as hidden parameters. This specifies which 
// form item should be dispatched by the flexible.form container. A second
// parameter submitted is the action value triggering the submit.
// A 'submit' is not the same as 'submit and validate'. if the form should validate
// is defined by the triggered component.
function o_ffEvent (formNam, dispIdField, dispId, eventIdField, eventInt){
	//set hidden fields and submit form
	var dispIdEl, defDispId,eventIdEl,defEventId;
	
	dispIdEl = document.getElementById(dispIdField);
	defDispId = dispIdEl.value;
	dispIdEl.value=dispId;
	eventIdEl = document.getElementById(eventIdField);
	defEventId = eventIdEl.value;
	eventIdEl.value=eventInt;
	// manually execute onsubmit method - calling submit itself does not trigger onsubmit event!
	if (document.forms[formNam].onsubmit()) {
		document.forms[formNam].submit();
	}
	dispIdEl.value = defDispId;
	eventIdEl.value = defEventId;
}

function o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt) {
	var data = new Object();
	data['dispatchuri'] = dispId;
	data['dispatchevent'] = eventInt;
	if(arguments.length > 5) {
		var argLength = arguments.length;
		for(var i=5; i<argLength; i=i+2) {
			if(argLength > i+1) {
				data[arguments[i]] = arguments[i+1];
			}
		}
	}
	
	
	var targetUrl = jQuery('#' + formNam).attr("action");
	jQuery.ajax(targetUrl,{
		type:'GET',
		data: data,
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
			o_ainvoke(data);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			if(console) console.log('Error status', textStatus);
		}
	})
}

//
// param formId a String with flexi form id
function setFlexiFormDirtyByListener(e){
	setFlexiFormDirty(e.data.formId);
}

function setFlexiFormDirty(formId){
	var isRegistered = o3c.indexOf(formId) > -1;
	if(!isRegistered){
		o3c.push(formId);
	}
	jQuery('#'+formId).each(function() {
		var submitId = jQuery(this).data('FlexiSubmit');
		if(submitId != null) {
			jQuery('#'+submitId).addClass('b_button b_button_dirty');
			o2c=1;
		}
	});
}

//
//
function o_ffRegisterSubmit(formId, submElmId){
	jQuery('#'+formId).data('FlexiSubmit', submElmId);
}
/*
* renders an info msg that slides from top into the window
* and hides automatically
*/
function showInfoBox(title, content){
	// Factory method to create message box
	var uuid = Math.floor(Math.random() * 0x10000 /* 65536 */).toString(16);
	var info = '<div id="' + uuid + '" class="b_msg-div msg" style="display:none;"><div class="b_msg_info_content b_msg_info_winicon o_sel_info_message"><h3>'
		 + title + '</h3>' + content + '<br/><br/></div></div>';
    var msgCt = jQuery('#b_page').prepend(info);
    // Hide message automatically
    var time = (content.length > 150) ? 8000 : ((content.length > 70) ? 6000 : 4000);
    jQuery('#' + uuid).slideDown(300).delay(time).slideUp(300);
    // Visually remove message box immediately when user clicks on it
    // The ghost event from above is triggered anyway. 
    jQuery('#' + uuid).click(function(e) {
    	jQuery('#' + uuid).remove();
    });
	
    // Help GC, prevent cyclic reference from on-click closure (OLAT-5755)
    title = null;
    content = null;
    msgCt = null;
    time = null;
}
/*
* renders an message box which the user has to click away
* The last parameter buttonCallback is optional. if a callback js 
* function is given it will be execute when the user clicks ok or closes the message box
*/
function showMessageBox(type, title, message, buttonCallback){
	if(type == 'info'){
		showInfoBox(title, message);
		return null;
	} else {
		var prefix;
		if("warn" == type) {
			prefix = '<div><div class="b_msg_info_content b_msg_warn_winicon">';
		} else if("error" == type) {
			prefix = '<div><div class="b_msg_info_content b_msg_error_winicon">';
		} else {
			prefix = '<div><div>';
		}
		return jQuery(prefix + '<p>' + message + '</p></div></div>').dialog({
			modal: true,
			title: title,
			resizable:false,
			close: function(event, ui) {
				try {
					jQuery(this).dialog('destroy').remove()
				} catch(e) {
					//possible if the user has closed the window
				}
			}
		}).dialog('open').dialog("widget").css('z-index', 11000);
		
	}
}

/*
 * For standard tables
 */
function tableFormInjectCommandAndSubmit(formName, cmd, param) {
	document.forms[formName].elements["cmd"].value = cmd;
	document.forms[formName].elements["param"].value = param;
	document.forms[formName].submit();
}

/*
 * For standard tables
 */
function b_table_toggleCheck(ref, checked) {
	var tb_checkboxes = document.forms[ref].elements["tb_ms"];
	len = tb_checkboxes.length;
	if (typeof(len) == 'undefined') {
		tb_checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			tb_checkboxes[i].checked=checked;
		}
	}
}

/*
 * For menu tree
 */
function onTreeStartDrag(event, ui) {
	jQuery(event.target).addClass('b_dd_proxy');
}

function onTreeStopDrag(event, ui) {
	jQuery(event.target).removeClass('b_dd_proxy');
}

function onTreeDrop(event, ui) {
	var dragEl = jQuery(ui.draggable[0]);
	var el = jQuery(this);
	el.css({position:'', width:''});
	var url =  el.droppable('option','endUrl');
	if(url.lastIndexOf('/') == (url.length - 1)) {
		url = url.substring(0,url.length-1);
	}
	var dragId = dragEl.attr('id')
	var targetId = dragId.substring(2, dragId.length);
	url += '%3Atnidle%3A' + targetId;

	var droppableId = el.attr('id');
	if(droppableId.indexOf('ds') == 0) {
		url += '%3Asne%3Ayes';
	} else if(droppableId.indexOf('dt') == 0) {
		url += '%3Asne%3Aend';
	}
	frames['oaa0'].location.href = url + '/';
}

function treeAcceptDrop(el) {
	var dragEl = jQuery(el);
	var dragElId = dragEl.attr('id');
	if(dragElId != undefined && (dragElId.indexOf('dd') == 0 ||
		dragElId.indexOf('ds') == 0 || dragElId.indexOf('dt') == 0 ||
		dragElId.indexOf('da') == 0 || dragElId.indexOf('row') == 0)) {

		var dropEl = jQuery(this)
		var dropElId = dropEl.attr('id');//dropped
		var dragNodeId = dragElId.substring(2, dragElId.length);
		var dropId = dropElId.substring(2, dropElId.length);
		if(dragNodeId == dropId) {
			return false;
		} 
		
		var sibling = "";
		if(dropElId.indexOf('ds') == 0) {
			sibling = "yes";
		} else if(dropElId.indexOf('dt') == 0) {
			sibling = "end";
		}
		
		var dropAllowed = dropEl.data(dragNodeId + "-" + sibling);
		if(dropAllowed === undefined) {
			var url = dropEl.droppable('option', 'fbUrl');
			//use prototype for the Ajax call
			jQuery.ajax(url, { 
				async: false,
				data: { nidle:dragNodeId, tnidle:dropId, sne:sibling },
				dataType: "json",
				method:'GET',
				success: function(data) {
					dropAllowed = data.dropAllowed;
				}
	  		});
			dropEl.data(dragNodeId + "-" + sibling, dropAllowed);
		}
		return dropAllowed;
	}
	return false;
}

/*
 * For checkbox
 */
function b_choice_toggleCheck(ref, checked) {
	var checkboxes = document.forms[ref].elements;
	len = checkboxes.length;
	if (typeof(len) == 'undefined') {
		checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			if (checkboxes[i].type == 'checkbox' && checkboxes[i].getAttribute('class') == 'b_checkbox') {
				checkboxes[i].checked=checked;
			}
		}
	}
}

/*
 * For briefcase
 */
function b_briefcase_isChecked(ref, warning_text) {
	var i;
	var myElement = document.getElementById(ref);
	var numselected = 0;
	for (i=0; myElement.elements[i]; i++) {
		if (myElement.elements[i].type == 'checkbox' && myElement.elements[i].name == 'paths' && myElement.elements[i].checked) {
			numselected++;
		}
	}
	
	if (numselected < 1) {
		alert(warning_text);
		return false;
	}
	return true;
}
function b_briefcase_toggleCheck(ref, checked) {
	var myElement = document.getElementById(ref);
	len = myElement.elements.length;
	var i;
	for (i=0; i < len; i++) {
		if (myElement.elements[i].name=='paths') {
			myElement.elements[i].checked=checked;
		}
	}
}


/*
 * print command, prints iframes when available
 */
function b_doPrint() {
	// When we have an iframe, issue print command on iframe directly
	var iframes =  jQuery('div.b_iframe_wrapper iframe');
	if (iframes.length > 0) {
		try {
			var iframe = iframes[0];
			frames[iframe.name].focus();
			frames[iframe.name].print();
			return;
		} catch (e) {
			// When iframe content renames the window, the method above does not work.
			// We use best guess code to find the target iframe in the window frames list
			for (i=0; frames.length > i; i++) {
				iframe = frames[i];
				if (iframe.name == 'oaa0') continue; // skip ajax iframe
				var domFrame = document.getElementsByName(iframe.name)[0];
				if (domFrame && domFrame.getAttribute('class') == 'ext-shim') continue; // skip ext shim iframe
				// Buest guess is that this is our renamed target iframe			
				if (iframe.name != '') {
					try {
						frames[iframe.name].focus();
						frames[iframe.name].print();				
					} catch (e) {
						// fallback to window print
						window.print()
					}
					return;
				}
			}		
			// fallback to window print
			window.print()
		}
	} else {
		// no iframes found, print window
		window.print()
	}
}


/*
 * Attach event listeners to enable inline translation tool hover links
 */ 
function b_attach_i18n_inline_editing() {
	// Add hover handler to display inline edit links
	jQuery('span.b_translation_i18nitem').hover(function() {
		jQuery(this.firstChild).show();
		if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"Entered i18nitem::" + this.firstChild, "functions.js:b_attach_i18n_inline_editing()");
	},function(){
		jQuery('a.b_translation_i18nitem_launcher').hide();
		if(jQuery(document).ooLog().isDebugEnabled()) jQuery(document).ooLog('debug',"Leaving i18nitem::" + this, "functions.js:b_attach_i18n_inline_editing()");
	});
	// Add highlight effect on link to show which element is affected by this link
	jQuery('a.b_translation_i18nitem_launcher').hover(function() {	
		var parent = jQuery(this).parent('span.b_translation_i18nitem')
		parent.effect("highlight");
	});
	// Add to on ajax ready callback for next execution
	b_AddOnDomReplacementFinishedCallback(b_attach_i18n_inline_editing);
}
 
 
/**
 * Minimalistic debugger to find ever growing list of DOM elements, 
 * global variables or OLAT managed variables. To use it, uncomment
 * lines in o_ainvoke()
 */
var BDebugger = {
	_lastDOMCount : 0,
	_lastObjCount : 0,
	_knownGlobalOLATObjects : ["o_afterserver","o_onc","o_getMainWin","o_ainvoke","o_info","o_beforeserver","o_ffEvent","o_openPopUp","o_debu_show","o_logwarn","o_dbg_unmark","o_ffRegisterSubmit","o_clearConsole","o_init","o_log","o_allowNextClick","o_dbg_mark","o_debu_hide","o_logerr","o_debu_oldcn","o_debu_oldtt","o_openUriInMainWindow","o_debug_trid","o_log_all"],
		
	_countDOMElements : function() {
		return document.getElementsByTagName('*').length;
	},
	_countGlobalObjects : function() {
			var objCount=0; 
			for (prop in window) {
				objCount++;
			} 
			return objCount;
	},
	
	logDOMCount : function() {
		var self = BDebugger;
		var DOMCount=self._countDOMElements();
		var diff = DOMCount - self._lastDOMCount;
		console.log( (diff > 0 ? "+" : "") + diff + " \t" + DOMCount + " \tDOM element count after DOM replacement");
		self._lastDOMCount = DOMCount;
		DOMCount = null;
	},

	logGlobalObjCount : function() {	
		var self = BDebugger;
		var objCount = self._countGlobalObjects();
		var diff = objCount - self._lastObjCount;
		console.log( (diff > 0 ? "+" : "") + diff + " \t" + objCount + " \tGlobal object count after DOM replacement");
		self._lastObjCount = objCount;
		objCount = null;
	},
	
	logGlobalOLATObjects : function() {
		var self = BDebugger;
		var OLATObjects = new Array();
		for (prop in window) {
			if (prop.indexOf("o_") == 0 && self._knownGlobalOLATObjects.indexOf(prop) == -1) {
				OLATObjects.push(prop);
			}
		} 	
		if (OLATObjects.length > 0) {
			console.log(OLATObjects.length + " global OLAT objects found:");
			OLATObjects.each(function(o){
				console.log("\t" + typeof window[o] + " \t" + o);
			});
		}
	},
	
	logManagedOLATObjects : function() {
		var self = BDebugger;
		if (o_info.objectMap.length > 0) {
			console.log(o_info.objectMap.length + " managed OLAT objects found:");
			o_info.objectMap.eachKey(function(key){
				var item=o_info.objectMap.get(key); 
				console.log("\t" + typeof item + " \t" + key); 
				return true;
			});
		}
	}
}

 