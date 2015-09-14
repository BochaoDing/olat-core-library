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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: 11.01.2007 <br>
 * 
 * @author patrickb
 */
public class FormJSHelper {

	private static final String[] EXTJSACTIONS = { "dblclick", "click", "change" };

	/**
	 * create for example an
	 * <code>onclick="o_ffEvent('ofo_1377','ofo_1377_dispatchuri','o_fi1399','ofo_1377_eventval','1')"</code>
	 * 
	 * @param form
	 * @param id
	 * @param actions
	 * @return
	 */
	public static StringBuilder getRawJSFor(Form form, String id, int actions) {
		StringBuilder sb = new StringBuilder(64);
		// find correct action! only one action supported
		for (int i = FormEvent.ON_DOTDOTDOT.length - 1; i >= 0; i--) {
			if (actions - FormEvent.ON_DOTDOTDOT[i] > 0)
				throw new AssertionError("only one actions supported here");
			if (actions - FormEvent.ON_DOTDOTDOT[i] == 0) {
				sb.append(" on" + EXTJSACTIONS[i]);// javascript action
				sb.append("=\"");
				sb.append(getJSFnCallFor(form, id, i));
				sb.append("\"");
				break;
				// actions = actions - FormEvent.ON_DOTDOTDOT[i];
			}
		}
		return sb;
	}

	public static String getJSFnCallFor(Form form, String id, int actionIndex) {
		String content = "o_ffEvent('";
		content += form.getFormName() + "','";
		content += form.getDispatchFieldId() + "','";
		content += id + "','";
		content += form.getEventFieldId() + "','";
		content += (FormEvent.ON_DOTDOTDOT[actionIndex]);
		content += ("')");
		return content;
	}
	
	/**
	 * Build a flexi form event
	 * @param form
	 * @param id
	 * @param actionIndex
	 * @param dirtyCheck
	 * @param pushState
	 * @param pairs
	 * @return
	 */
	public static String getXHRFnCallFor(Form form, String id, int actionIndex, boolean dirtyCheck, boolean pushState, NameValuePair... pairs) {
		StringOutput sb = new StringOutput(128);
		sb.append("o_ffXHREvent('")
		  .append(form.getFormName()).append("','")
		  .append(form.getDispatchFieldId()).append("','")
		  .append(id).append("','")
		  .append(form.getEventFieldId()).append("','")
		  .append(FormEvent.ON_DOTDOTDOT[actionIndex])
		  .append("',").append(dirtyCheck)
		  .append(",").append(pushState).append("");

		if(pairs != null && pairs.length > 0) {
			for(NameValuePair pair:pairs) {
				sb.append(",'").append(pair.getName()).append("','").append(pair.getValue()).append("'");
			}
		}

		sb.append(")");
		IOUtils.closeQuietly(sb);
		return sb.toString();
	}
	
	public static String generateXHRFnCallVariables(Form form, String id, int actionIndex) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("var formNam = '").append(form.getFormName()).append("';\n")
		  .append("var dispIdField = '").append(form.getDispatchFieldId()).append("';\n")
		  .append("var dispId = '").append(id).append("';\n")
		  .append("var eventIdField = '").append(form.getEventFieldId()).append("';\n")
		  .append("var eventInt = ").append(FormEvent.ON_DOTDOTDOT[actionIndex]).append(";\n");
		return sb.toString();
	}

	/**
	 * 
	 * @param sb
	 * @param jsonRenderInstruction
	 * @param acceptedInstructions
	 */
	public static void appendRenderInstructions(StringOutput sb,
			String jsonRenderInstruction, Set<String> acceptedInstructions) {
		JSONObject instr;
		try {
			instr = new JSONObject(jsonRenderInstruction);
			sb.append(" ");// ensure blank before appending instructions -> '
							// '...
			for (Iterator<String> iter = acceptedInstructions.iterator(); iter
					.hasNext();) {
				String accepted = iter.next();
				if (instr.get(accepted) != null) {
					// generates i.e. 'class=\"thevalueclass\" '
					sb.append(accepted);// accepted key is also use as attribute
					sb.append("=\"");
					sb.append(instr.getString(accepted));
					sb.append("\" ");
				}
			}
		} catch (JSONException e) {
			throw new OLATRuntimeException(
					"error retrieving JSON style render instruction", e);
		}
	}

	/**
	 * @param rootForm
	 * @param id
	 * @return
	 */
	public static String getJSSubmitRegisterFn(Form form, String id) {
		String content = "o_ffRegisterSubmit('";
		content += form.getDispatchFieldId() + "','";
		content += id + "');";
		return content;
	}
	
	public static String getJSStartWithVarDeclaration(String id){
		StringBuilder sb = new StringBuilder(150);
		sb.append(" <script type=\"text/javascript\">\n /* <![CDATA[ */ \n");
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		sb.append("(function() {");
		sb.append("var ").append(id).append(" = jQuery('#").append(id).append("'); ");
		return sb.toString();
	}
	
	public static String getJSStart(){
		// Execute code within an anonymous function (closure) to not leak
		// variables to global scope (OLAT-5755)
		return " <script type=\"text/javascript\">\n /* <![CDATA[ */ \n (function() {";
	}
	
	public static String getJSEnd(){
		// Execute anonymous function (closure) now (OLAT-5755)
		return "})();\n /* ]]> */ \n</script>";
	}
	
	// Execute code within an anonymous function (closure) to not leak
	// variables to global scope (OLAT-5755)
	public static StringOutput appendFlexiFormDirty(StringOutput sb, Form form, String id) {
		sb.append("<script type=\"text/javascript\">\n /* <![CDATA[ */ \n")
		  .append("(function() { jQuery('#").append(id).append("').on('change keypress',{formId:\"").append(form.getDispatchFieldId()).append("\"},setFlexiFormDirtyByListener);")
		  .append("})();\n /* ]]> */ \n</script>");
		return sb;
	}
	
	public static StringOutput appendFlexiFormDirtyForCheckbox(StringOutput sb, Form form, String formDispatchId) {
		sb.append(" <script type=\"text/javascript\">\n /* <![CDATA[ */ \n")
		  .append("(function() { jQuery('#").append(formDispatchId).append("').on('change mouseup', {formId:\"").append(form.getDispatchFieldId()).append("\"}, setFlexiFormDirtyByListener);")
		  .append("})();\n /* ]]> */ \n</script>");
		return sb;
	}
	
	public static String getSetFlexiFormDirtyFnCallOnly(Form form){
		if(form.isDirtyMarking()){
			return "setFlexiFormDirty('"+form.getDispatchFieldId()+"');";
		}else{
			return " ";
		}
	}

	/**
	 * creates the JS fragment needed for the {@link #getInlineEditOkCancelHTML(StringOutput, String, String, String)} HTML fragment.
	 * @param sb where to append
	 * @param id formItemId of the InlineEdit FormItem
	 * @param oldHtmlValue escaped HTML value TODO:2009-09-26:pb: escaped values appear as &apos; and are not escaped back.
	 * @param rootForm to extract the ID of the Form where to submit to.
	 */
	public static void getInlineEditOkCancelJS(StringOutput sb, String id, String oldHtmlValue, Form rootForm) {
		/*
		 * yesFn emulates a click on the input field, which in turn "submits" to the inlineElement to extract the value
		 */
		sb.append("var ").append(id).append("=jQuery('#").append(id).append("');")
		  .append(id).append(".focus(1);")//defer focus
		  .append("var o_ff_inline_yesFn = function(e){")
		  .append(FormJSHelper.getJSFnCallFor(rootForm, id, FormEvent.ONCLICK)).append(";};")
		  .append("jQuery('#").append(id).append("').on('blur',o_ff_inline_yesFn);");		

		/*
		 * noFn replaces the old value in the input field, and then "submits" to the inlineElement via yesFn
		 */
		sb.append("var o_ff_inline_noFn = function(e){ jQuery('#").append(id).append("').val('").append(oldHtmlValue).append("'); o_ff_inline_yesFn(e); };")
		  .append("jQuery('#").append(id).append("').keydown(function(e) {")
	      .append(" if(e.which == 27) {")
	      .append("   o_ff_inline_noFn();")
	      .append(" } else if(e.which == 10 || e.which == 13) {")
	      .append("   o_ff_inline_yesFn();")
	      .append(" }")
	      .append("});");
	}
	
	/**
	 * submits a form when the enter key is pressed.
	 * TextAreas are handled special and do not propagate the enter event to the outer world
	 * @param formName
	 * @return
	 */
	public static String submitOnKeypressEnter(String formName) {
		StringBuilder sb = new StringBuilder();
		sb.append(getJSStart())
		  .append("jQuery('#").append(formName).append("').keypress(function(event) {\n")
		  .append(" if (13 == event.keyCode) {\n")
		  .append("  event.preventDefault();\n")
		  .append("  if (this.onsubmit()) { this.submit(); }\n")
		  .append(" }\n")
		  .append("});\n")
		  .append(getJSEnd());
		return sb.toString();
	}
}
