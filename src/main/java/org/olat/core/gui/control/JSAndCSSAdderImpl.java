/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/ 

package org.olat.core.gui.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.delegating.DelegatingComponent;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.control.winmgr.WindowBackOfficeImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * responsible for rendering the &lt;link rel.. and &lt;script src=... tags in
 * the html header.<br>
 * we do not need remove methods, since in ajax-mode, any change will lead to a
 * page reload.
 * <P>
 * Initial Date:  04.05.2006 <br>
 *
 * @author Felix Jost
 */
public class JSAndCSSAdderImpl implements JSAndCSSAdder, ComponentRenderer {

	private DelegatingComponent dc;
	
	private HashMap<String, String> keyToPath = new HashMap<String, String>(10); // keys: key of a class e.g. 'org.olat.mypackage'; values: global mappath e.g. /m/10/

	private List<String> curCssList = new ArrayList<String>();
	private List<String> prevCssList = new ArrayList<String>();
	private Collection<String> curCssForceSet = new ArrayList<String>(3);
	private Collection<String> prevCssForceSet = new ArrayList<String>(3);

	private Set<String> allCssKeepSet = new HashSet<String>();
	private Set<String> allJsKeepSet = new HashSet<String>();
	
	private List<String> curJsList = new ArrayList<String>();
	private List<String> prevJsList = new ArrayList<String>();

	
	private Set<String> cssToAdd;  // the css to add for the next round
	private Set<String> cssToRemove;  // the css to remove for the next round
	private List<String> jsToAdd; // the js to add for the next round
	
	private List<String> cssToRender; // the css's to render
	private List<String> jsToRender; // the js's to render
	
	// FIXME:fj: make the rawset deprecated; all raw includes can be replaced by a css or js include; the js calls can be moved to the velocity files.
	// for QTIEditormaincontroller / Displaycontroller -> Autocomplete files which need are dynamic files to be included -> 
	// simplest sol would be: get the content of the file (in utf-8) and put it into <script> tags of the appropriate velocitycontainer.
	private Collection<String> curRawSet = new ArrayList<String>(2);
	private Collection<String> oldRawSet = new ArrayList<String>(2);
	
	private static final int MINIMAL_REFRESHINTERVAL = 1000;//in [ms] 
	private int refreshInterval = -1;
	private final WindowBackOfficeImpl wboImpl;

	private Map<String, Class<?>> jsPathToBaseClass = new HashMap<String, Class<?>>();
	private Map<String,String> jsPathToJsFileName = new HashMap<String, String>();
	private Map<String,String> jsPathToEvalBeforeAJAXAddJsCode = new HashMap<String, String>();
	private Map<String,String> jsPathToEvalFileEncoding = new HashMap<String, String>();
	
	private static final String ENCODING_DEFAULT = "utf-8";

	private Map<String, String> cssPathToId = new HashMap<String, String>();
	private Map<String, Integer> cssPathToIndex = new HashMap<String, Integer>();
	private final Comparator<String> cssIndexComparator = new Comparator<String>(){
		public int compare(String css1, String css2) {
			int index1 = cssPathToIndex.get(css1);
			int index2 = cssPathToIndex.get(css2);
			return (index1 - index2);
		}
	};
	private int cssCounter = 0;

	private boolean requiresFullPageRefresh = false;
	
	public JSAndCSSAdderImpl(WindowBackOfficeImpl wboImpl) {
		this.wboImpl = wboImpl;
		dc = new DelegatingComponent("jsAndCssAdderDeleComp", this);
		dc.setDomReplaceable(false);
		cssToRender = curCssList;
		jsToRender = curJsList;
	}
	
	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredJsFile(java.lang.Object,
	 *      java.lang.String)
	 */
	public void addRequiredStaticJsFile(String jsFileName) {
		addRequiredJsFile(null, jsFileName, ENCODING_DEFAULT, null);
	}
	
	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredJsFile(java.lang.Object,
	 *      java.lang.String)
	 */
	public void addRequiredJsFile(Class<?> baseClass, String jsFileName) {
		addRequiredJsFile(baseClass, jsFileName, ENCODING_DEFAULT, null);
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredJsFile(java.lang.Object,
	 *      java.lang.String, java.lang.String)
	 */
	public void addRequiredJsFile(Class<?> baseClass, String jsFileName, String fileEncoding) {
		addRequiredJsFile(baseClass, jsFileName, fileEncoding, null);
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredJsFile(java.lang.Class,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addRequiredJsFile(Class<?> baseClass, String jsFileName, String fileEncoding,
			String AJAXAddJsCode) {

		String jsPath;
		if(baseClass == null) {
			StringOutput sb = new StringOutput(50);
			Renderer.renderStaticURI(sb, jsFileName);
			jsPath = sb.toString();
		} else {
			jsPath = getMappedPathFor(baseClass, jsFileName);
		}

		if (!curJsList.contains(jsPath)) {
			//System.out.println("reqJs:"+jsPath);
			curJsList.add(jsPath);
			jsPathToBaseClass.put(jsPath, baseClass);
			jsPathToJsFileName.put(jsPath, jsFileName);
			if (StringHelper.containsNonWhitespace(AJAXAddJsCode)) {
				jsPathToEvalBeforeAJAXAddJsCode.put(jsPath, AJAXAddJsCode);
			}
			if (fileEncoding != null) {
				jsPathToEvalFileEncoding.put(jsPath, fileEncoding);
			} else {
				jsPathToEvalFileEncoding.put(jsPath, ENCODING_DEFAULT);
			}
		}		
	}

	@Override
	public void addStaticCSSPath(String cssPath) {
		addRequiredCSSFile(null, cssPath, false, JSAndCSSAdder.CSS_INDEX_BEFORE_THEME);
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredCSSFile(java.lang.Class, java.lang.String, boolean)
	 */
	@Override
	public void addRequiredCSSFile(Class<?> baseClass, String cssFileName, boolean forceRemove) {
		addRequiredCSSFile(baseClass, cssFileName, forceRemove, JSAndCSSAdder.CSS_INDEX_BEFORE_THEME);
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredCSSFile(java.lang.Class, java.lang.String, boolean, int)
	 */
	@Override
	public void addRequiredCSSFile(Class<?> baseClass, String cssFileName, boolean forceRemove, Integer cssLoadIndex) {
		String cssPath = getMappedPathFor(baseClass, cssFileName);
		addRequiredCSSPath(cssPath, forceRemove, cssLoadIndex);
	}
	
	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredCSSPath(java.lang.String, boolean, int)
	 */
	@Override
	public void addRequiredCSSPath(String cssPath, boolean forceRemove, Integer cssLoadIndex) {
		if (!curCssList.contains(cssPath)) {
			//System.out.println("reqCss:"+cssPath+" force "+forceRemove);
			String id = cssPathToId.get(cssPath);
			if (id == null) { // no html id for this stylesheet import yet -> create one
				cssPathToId.put(cssPath, "o_css"+(++cssCounter));
			}
			curCssList.add(cssPath);
			if (forceRemove) {
				curCssForceSet.add(cssPath);
			}
			if(cssLoadIndex == null) {
				cssLoadIndex = JSAndCSSAdder.CSS_INDEX_BEFORE_THEME;
			}
			cssPathToIndex.put(cssPath, cssLoadIndex);
			// sort css after index
			Collections.sort(curCssList, cssIndexComparator);
		}
	}
	
	/**
	 * 
	 * requires that a full page reload takes places.	
	 * sometimes eval'ing a more complex js lib (such as tiny mce) directly into global context does not work (timing issues?)
	 * this should be used only rarely when complex js is executed and has errors in it, 
	 * since a full page refresh is slower than a ajax call.
	 * <br>
	 * when a component is validated (last cycle before rendering), and a full page refresh is required, then a full page request command is 
	 * sent via JSON to the browser which then executes it using document.location.replace(...). Since this step involves two calls (JSON+reload),
	 * this is slower than a normal full page click (aka known as non-ajax mode). 
	 * 
	 */
	public void requireFullPageRefresh() {
		requiresFullPageRefresh = true;
	}
	
	public boolean finishAndCheckChange() {
		// ----- find out whether there are any freshly added or removed css classes. -----
		// create new sets since we need to keep the original list untouched 
		// (e.g. needed for rendering when doing a browser full page reload, or when in non-ajax-mode)
		Set<String> curCss = new HashSet<String>(curCssList);
		Set<String> prevCss = new HashSet<String>(prevCssList);
		curCss.removeAll(prevCssList); // the current minus the previous ones = the new ones to be added
		curCss.removeAll(allCssKeepSet); // but take those away which were used earlier and didn't need to be removed
		prevCss.removeAll(curCssList); // the previous minus the current ones = the ones not needed anymore = to be deleted
		prevCss.retainAll(prevCssForceSet); // only keep those css in the remove collection which really need to be removed (flagged as forceremove) 
		cssToAdd = curCss;
		cssToRemove = prevCss;
		// ----- find out whether there are new js libs to be added. -----
		// it doesn't make sense to require a removal of js libs (js libs should never interfere which each other by design) -
		// therefore we only have to take care about new ones to be added.
		List<String> curJs = new ArrayList<String>(curJsList);
		curJs.removeAll(allJsKeepSet); // the current minus the previously added ones = the new ones to be added
		jsToAdd = curJs;

		//System.out.println("---- css-add:\n"+cssToAdd);
		//System.out.println("---- css-remove:\n"+cssToRemove);
		//System.out.println("---- js-add:\n"+jsToAdd);
		
		// raw set -> deprecated, see comments at variable declaration
		boolean wasRawChanged = false;
		if (curRawSet.size() != oldRawSet.size()) {
			wasRawChanged = true;
		} else {
			// same size, but could still be different:
			wasRawChanged = !curRawSet.containsAll(oldRawSet);
		}

		
		// ----- end-of-calculations: make the cur to the prev for the next add-round -----
		// css
		List<String> tmpCss = prevCssList;
		prevCssList = curCssList;
		cssToRender = curCssList;
		tmpCss.clear();
		curCssList = tmpCss;
		
		// remember which non-remove-force css entries have once already been added
		allCssKeepSet.addAll(cssToAdd);
		allCssKeepSet.removeAll(curCssForceSet);
		
		// change current cssFrceSet and clear it for the next validate process
		Collection<String> forceTmp = prevCssForceSet;
		prevCssForceSet = curCssForceSet;
		curCssForceSet = forceTmp;
		curCssForceSet.clear();
		
		// js
		allJsKeepSet.addAll(jsToAdd);
		
		List<String> jsTmp = prevJsList;
		jsTmp.clear();
		prevJsList = curJsList;
		curJsList = jsTmp;
		jsToRender = prevJsList;
		// raw set -> deprecated, see comments at variable declaration
		Collection<String> tmp = oldRawSet;
		oldRawSet = curRawSet;
		curRawSet = tmp;
		curRawSet.clear();		
		
		// set and reset update/refresh intervall for ajax polling
		wboImpl.setRequiredRefreshInterval(refreshInterval);
		refreshInterval = -1;

		boolean fullPageRefresh = requiresFullPageRefresh;
		requiresFullPageRefresh = false;
		
		return wasRawChanged || fullPageRefresh;
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#getMappedPathFor(java.lang.Class, java.lang.String)
	 */
	public String getMappedPathFor(Class<?> baseClass, String fileName) {
		//fxdiff make it possible to put absolute paths to js-files
		// e.g. /olat/raw/fx-OLAT/themes/frentix/myjs.js FXOLAT-310
		if(baseClass == null){
			return fileName;
		}
		String packkey = getKey(baseClass); 
		String mappath = keyToPath.get(packkey);
		if (mappath == null) {
			synchronized (keyToPath) {
				mappath = keyToPath.get(packkey);
				if (mappath == null) {
					// never used before, get a path from the global mapper provider
					mappath = wboImpl.getWinmgrImpl().getMapPathFor(baseClass);
					keyToPath.put(packkey, mappath);
				}				
			}
		}
		if (fileName == null) {
			return mappath;
		} else {
			return mappath + "/" + fileName;
		}
	}

	/**
	 * @param baseClass
	 * @return
	 */
	private String getKey(Class<?> baseClass) {
		String cla = baseClass.getName();
		int ls = cla.lastIndexOf('.');
		// post: ls != -1, since we don't use the java default package
		String pkg = cla.substring(0, ls);
		// using baseClass.getPackage() would add unneeded inefficient and synchronized code
		return pkg;
	}
	

	/**
	 * @return
	 */
	public Component getJsCssRawHtmlHeader() {
		return dc;
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		// The render argument is used to indicate rendering before and after themes loading
		if (args == null || args.length != 1) {
			throw new AssertException("Programming error: can't render JSAndCSSAdder without 'pre-theme' or 'post-thee' render argument");
		}
		boolean postThemeRendering = args[0].equals("post-theme") ? true : false;
		
		// clear the added-since-last-fullpagerefresh, since we are doing a full page refresh here (only then is the <head> part here rerendered.)
		// this aims to minimize the number of js and css "imports" in the html head when using the non-ajax-mode (only those imports really needed are listed)
		allCssKeepSet.clear();
		allJsKeepSet.clear();
		
		//sb.append("<!-- css and js include test \n");
		//sb.append("js-files:\n");
		// JS scripts are rendered when in pre-theme rendering phase
		if (!postThemeRendering) {
			for (Iterator<String> it_js = jsToRender.iterator(); it_js.hasNext();) {
				String jsExpr = it_js.next();
				sb.append("<script type=\"text/javascript\" src=\"").append(jsExpr).append("\"></script>\n");
			}
		}
		
		// sort css files
			
		//sb.append("css-files:\n");
		for (Iterator<String> it_css = cssToRender.iterator(); it_css.hasNext();) {
			String cssExpr = it_css.next();
			// render post-theme css when in post-theme rendering phase and pre-theme
			// css when in pre-them rendering phase. List is sorted after index
			int cssIndex = cssPathToIndex.get(cssExpr);
			if ((postThemeRendering && cssIndex > JSAndCSSAdder.CSS_INDEX_THEME)
					|| (!postThemeRendering && cssIndex < JSAndCSSAdder.CSS_INDEX_THEME)) {
				String acssId = cssPathToId.get(cssExpr);
				// use media=all to load always and use @media screen/print within the stylesheet
				sb.append("<link id=\"").append(acssId).append("\" rel=\"StyleSheet\" href=\"").append(cssExpr).append("\" type=\"text/css\" media=\"all\" />\n");
			}
		}
		
		if (postThemeRendering) {
			// Render raw header after theme. See also OLAT-4262
			for (Iterator<String> it_raw = oldRawSet.iterator(); it_raw.hasNext();) {
				String rawE = it_raw.next();
				sb.append("\n").append(rawE);
			}			
		}
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		//
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.JSAndCSSAdder#addRequiredRawHeader(java.lang.Class)
	 */
	public void addRequiredRawHeader(Class<?> baseClass, String rawHeader) {
		curRawSet.add(rawHeader);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.JSAndCSSAdder#setRequiredRefreshInterval(java.lang.Class, int)
	 */
	public void setRequiredRefreshInterval(Class<?> baseClass, int refreshIntervall) {
		if(refreshIntervall < MINIMAL_REFRESHINTERVAL){
			throw new AssertException("Poll refresh intervall is smaller then defined MINIMAL value " + MINIMAL_REFRESHINTERVAL);
		}
		// idea: baseClass for later de-prioritising by configuration
		if (this.refreshInterval == -1 || refreshIntervall < this.refreshInterval) {
			this.refreshInterval = refreshIntervall;
			//System.out.println("setting new refresh intervall: "+this.refreshInterval);
		} // else we already have a request that requires a higher frequency of updates, we will take that one
	}
	
	
	public Command extractJSCSSCommand() {
		try {			
			JSONObject root = new JSONObject();
			
			//css to add
			JSONArray cssAdd = new JSONArray();
			root.put("cssadd", cssAdd);
			for (String addCss : cssToAdd) {
				// the id and the whole relative css path, e.g. /g/4/my.css
				JSONObject styleinfo = new JSONObject();
				String cssId = cssPathToId.get(addCss);
				styleinfo.put("id", cssId);
				styleinfo.put("url", addCss);
				// on js level only pre and post theme rendering supported
				styleinfo.put("pt", cssPathToIndex.get(addCss) > JSAndCSSAdder.CSS_INDEX_THEME ? true : false);
				cssAdd.put(styleinfo); 
			}
			
			//css to remove
			JSONArray cssRemove = new JSONArray();
			root.put("cssrm", cssRemove);
			for (String removeCss : cssToRemove) {
				// the id and the whole relative css path, e.g. /g/4/my.css
				JSONObject styleinfo = new JSONObject();
				String cssId = cssPathToId.get(removeCss);
				styleinfo.put("id", cssId);
				styleinfo.put("url", removeCss);
				cssRemove.put(styleinfo); 
			}
			
			//jsToAdd
			JSONArray jsAdd = new JSONArray();
			root.put("jsadd", jsAdd);
			for (String addJs : jsToAdd) {
				// load file with correct encoding. OLAT files are all UTF-8, but some
				// libraries like TinyMCE are ISO-88591. The window.execScript() in IE
				// can fail when the string has the wrong encoding (IE error 8002010)
				String fileEncoding = jsPathToEvalFileEncoding.get(addJs);
				JSONObject fileInfo = new JSONObject();
				fileInfo.put("url", addJs);
				fileInfo.put("enc", fileEncoding);
				// add code to be executed before the js code is inserted
				if (jsPathToEvalBeforeAJAXAddJsCode.containsKey(addJs)) {
					fileInfo.put("before", jsPathToEvalBeforeAJAXAddJsCode.get(addJs));					
				}
				jsAdd.put(fileInfo);
			}
			Command com = CommandFactory.createJSCSSCommand();
			com.setSubJSON(root);
			return com;
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
	}

}
