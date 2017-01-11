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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.LogDelegator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.component.FormComponentTraverser;
import org.olat.core.util.component.FormComponentVisitor;


/**
 * <h4>Description:<h4>
 * This Form is responsible for creation of the form header and footer. It can
 * not hold form elements. Instead one has to create a form container and put
 * the form elements there. E.g. use FormLayoutContainer or
 * FormVelocityContainer.
 * <p>
 * This Form is the Component which gets dispatched by the framework. It then
 * dispatches further to the really clicked FormComponent. The Form implements
 * the following phases:
 * <ol>
 * <li>let all Formelements evaluate the Formrequest</li>
 * <li>dispatch to the correct FormComponent</li>
 * <li>the dispatched FormComponent may decide to SUBMIT the form, e.g. let all
 * FormComponents validate its input and report error, or taken other actions.</li>
 * <li>during the validatition phase each FormComponent can register an action</li>
 * <li>after the validation, all actions are applied</li>
 * <li>an event is thrown if the form validated or not</li>
 * </ol>
 * <p>
 * FormComponent and FormContainer form the same composite pattern as already
 * used for the core.Component and core.Container, and take notice that the
 * FormComponent itself is also a core.Component!<br>
 * As a consequence of this, each element which want to live inside of a form
 * must be a FormComponent but has also a Component side to the rendering
 * framework.
 * <p>
 * The goals of the new form infrastructure are
 * <ul>
 * <li>to allow complete freedom for layouting forms.</li>
 * <li>easy-migration path for existing forms</li>
 * <li>easy-usage for developers</li>
 * <li>easy-layouting for designers</li>
 * <li>allow subworkflows in forms without loosing form input</li>
 * <li>allow AJAX features for form elements (completion, on blur etc.)</li>
 * </ul>
 * Some extra care had to be taken to fullfill these requirements and still
 * beeing compliant with the already existing AJAX component replacement.<br>
 * It was decided that a FormComponent consist of
 * <ul>
 * <li>Formelement, e.g. input field, radio button, select box, a link!</li>
 * <li>Label for the Formelement</li>
 * <li>Error for the Formelement</li>
 * <li>Example for the Formelement</li>
 * </ul>
 * <p>
 * <h4>Multipart and file upload</h4>
 * Since release 6.1 the form infrastructure does also support multipart form
 * data (file uploads). The form switches to the multipart mode as soon as a
 * form element of type FormMultipartItem is added. In this case, all file
 * uploads and form parameters are parsed by the form class and added to the
 * requestParams and requestMultipartFiles maps. If no multipart element is in the form, the
 * normal non-multipart way is used (less overhead, stability).
 * <p>
 * Therefore it is important to always use the form.getParameter() methods and
 * not the getParameter() methods from the user request directly. Normally you
 * don't have to deal with this because the implemented form elements already
 * take care of this issue.
 * <p>
 * All submitted files are saved to a temporary location in the userdata/tmp/
 * directory. During the dispatch phase in evalFormRequest() this files can be
 * access using the getMultipartFilesSet() and getMultipartFile() methods. The
 * files must be moved to another location within the execution of the
 * evalFormRequest() because at the end of the method call, the temporary files
 * will be removed. The temporary files have a random file name, use the
 * getMultipartFileName() to retrieve the original file name.
 * <p>
 * When using the FileElement this is all already encapsulated, see the
 * documentation there.
 * <p>
 * Initial Date: 27.11.2006 <br>
 * 
 * @author patrickb
 */
public class Form extends LogDelegator {
	
	private static final OLog log = Tracing.createLoggerFor(Form.class);
	//
	public final static String FORMCMD = "fid";
	public final static String FORMID = "ofo_";
	public final static String FORM_UNDEFINED="undefined";
	
	public final static int REQUEST_ERROR_NO_ERROR = -1;
	public final static int REQUEST_ERROR_GENERAL = 1;
	public final static int REQUEST_ERROR_FILE_EMPTY = 2;
	public final static int REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED = 3;
	
	private String formName;
	private String dispatchFieldId;
	private String eventFieldId;

	// the real form
	private FormItemContainer formLayout;
	private FormWrapperContainer formWrapperComponent;
	private Integer action;
	private boolean hasAlreadyFired;
	private List<FormBasicController> formListeners;
	private boolean isValidAndSubmitted=true;
	private boolean isDirtyMarking=true;
	private boolean multipartEnabled = false;
	// temporary form data, only valid within execution of evalFormRequest()
	private Map<String,String[]> requestParams = new HashMap<String,String[]>();
	private Map<String, File> requestMultipartFiles = new HashMap<String,File>();
	private Map<String, String> requestMultipartFileNames = new HashMap<String,String>();
	private Map<String, String> requestMultipartFileMimeTypes = new HashMap<String,String>();
	private int requestError = REQUEST_ERROR_NO_ERROR;
	
	private Form() {
		//
	}

	/**
	 * create a new form, where the caller is attached as component listener.
	 * Caller receives form validation success or failure events.
	 * 
	 * @param name
	 * @param translator
	 * @param rootFormContainer if null the default layout is choosen, otherwise
	 *          the given layouting container is taken.
	 * @param listener the component listener of this form, typically the caller
	 * @return
	 */
	public static Form create(String id, String name, FormItemContainer formLayout, Controller listener) {
		Form form = new Form();
		// this is where the formitems go to
		form.formLayout = formLayout;
		form.formLayout.setRootForm(form);
		form.formListeners = new ArrayList<FormBasicController>(1);
		if(listener instanceof FormBasicController){
			form.formListeners.add((FormBasicController)listener);
		}
		Translator translator = formLayout.getTranslator();
		if (translator == null) { throw new AssertException("please provide a translator in the FormItemContainer <" + formLayout.getName()
				+ ">"); }
		// renders header + <formLayout> + footer of html form
		form.formWrapperComponent = new FormWrapperContainer(id, name, translator, form);
		form.formWrapperComponent.addListener(listener);
		//form.formWrapperComponent.put(formLayout.getComponent().getComponentName(), formLayout.getComponent());
		// generate name for form and dispatch uri hidden field

		form.formName = Form.FORMID + form.formWrapperComponent.getDispatchID();
		form.dispatchFieldId = form.formName + "_dispatchuri";
		form.eventFieldId = form.formName +"_eventval";
		
		return form;
	}
	
	/**
	 * @param ureq
	 */
	public void evalFormRequest(UserRequest ureq) {
		// Initialize temporary request parameters
		if (isMultipartEnabled() && isMultipartContent(ureq.getHttpReq())) {
			doInitRequestMultipartDataParameter(ureq);
		} else {
			doInitRequestParameter(ureq);
		}
		
		String dispatchUri = getRequestParameter("dispatchuri");
		String dispatchAction = getRequestParameter("dispatchevent");
		boolean invalidDispatchUri = dispatchUri == null || dispatchUri.equals(FORM_UNDEFINED);
		boolean invalidDispatchAct = dispatchAction == null || dispatchAction.equals(FORM_UNDEFINED);
		boolean implicitFormSubmit = false;//see also OLAT-3141
		if(invalidDispatchAct && invalidDispatchUri){
			//case if:
			//enter was pressed in Safari / IE
			//crawler tries form links
			
			SubmitFormComponentVisitor efcv = new SubmitFormComponentVisitor();
			new FormComponentTraverser(efcv, formLayout, false).visitAll(ureq);
			Submit submitFormItem = efcv.getSubmit();
			if(submitFormItem != null) {
				//if we have submit form item
				//assume a click on this item
				dispatchUri = FormBaseComponentIdProvider.DISPPREFIX + submitFormItem.getComponent().getDispatchID();
				action = FormEvent.ONCLICK;
			}else{
				// instead of 
				// throw new AssertException("You have an input field but no submit item defined! this is no good if enter is pressed.");
				// assume a desired implicit form submit
				// see also OLAT-3141
				implicitFormSubmit = true;
			}
		} else {
			try {
				action = Integer.valueOf(dispatchAction);
			} catch(Exception e) {
				throw new InvalidRequestParameterException();
			}
		}
		hasAlreadyFired = false;
		isValidAndSubmitted = false;
		//
		// step 1: call evalFormRequest(ureq) on each FormComponent this gives
		// ....... for each element the possibility to intermediate save a value.
		// ....... As a sideeffect the formcomponent to be dispatched is found.
		EvaluatingFormComponentVisitor efcv = new EvaluatingFormComponentVisitor(dispatchUri);
		FormComponentTraverser ct = new FormComponentTraverser(efcv, formLayout, false);
		ct.visitAll(ureq);
		// step 2: dispatch to the form component
		// ......... only one component to be dispatched can be found, e.g. clicked
		// ......... element....................................................
		// ......... dispatch changes server model -> rerendered
		// ......... dispatch may also request a form validation by
		// ......... calling the submit
		FormItem dispatchFormItem = efcv.getDispatchToComponent();
		//.......... doDispatchFormRequest is called on the found item
		//.......... which in turn may call submit(UserRequest ureq).
		//.......... After submitting, which fires a ok/nok event
		//.......... the code goes further with step 3.........................
		if (implicitFormSubmit) {
			//implicit Submit (Press Enter without on a Field without submit item.)
			// see also OLAT-3141
			submit(ureq);
		}else{
			if (dispatchFormItem == null) {
				// source not found. This "never happens". Try to produce some hints.
				String fbc = new String();
				for (FormBasicController i: formListeners) {
					if (fbc.length()>0) {
						fbc += ",";
					}
					fbc+=(i.getClass().getName());
				}
				logWarn("OLAT-5061: Could not determine request source in FlexiForm >"+formName+"<. Check >"+fbc+"<", null);
				
				// TODO: what now? 
				// Assuming the same as "implicitFormSubmit" for now.
				submit(ureq);
				
			} else {
				// ****************************************
				// explicit Submit or valid form dispatch *
				// ****************************************
				dispatchFormItem.doDispatchFormRequest(ureq);
				// step 3: find parent container of dispatched component
				// .......... check dependency rules
				// .......... apply dependency rules
				FindParentFormComponentVisitor fpfcv = new FindParentFormComponentVisitor(
						dispatchFormItem);
				ct = new FormComponentTraverser(fpfcv, formLayout, false);
				ct.visitAll(ureq);
				fpfcv.getParent().evalDependencyRuleSetFor(ureq, dispatchFormItem);
			}
		}
		//
		action = -1;
		
		// End of request dispatch: cleanup temp files: ureq requestParams and multipart files
		doClearRequestParameterAndMultipartData();
	}
	
	private void doInitRequestMultipartDataParameter(UserRequest ureq) {
		HttpServletRequest req = ureq.getHttpReq();
		try {				
			for(Part part:req.getParts()) {
				String name = part.getName();
				String contentType = part.getContentType();
				String fileName = getSubmittedFileName(part);
				if(StringHelper.containsNonWhitespace(fileName)) {
					File tmpFile = new File(WebappHelper.getTmpDir(), "upload-" + CodeHelper.getGlobalForeverUniqueID());
					part.write(tmpFile.getAbsolutePath());
					
					// Cleanup IE filenames that are absolute
					int slashpos = fileName.lastIndexOf("/");
					if (slashpos != -1) fileName = name.substring(slashpos + 1);
					slashpos = fileName.lastIndexOf("\\");
					if (slashpos != -1) fileName = fileName.substring(slashpos + 1);
					
					requestMultipartFiles.put(name, tmpFile);
					requestMultipartFileNames.put(name, fileName);
					requestMultipartFileMimeTypes.put(name, contentType);
				} else {
					String value = IOUtils.toString(part.getInputStream(), "UTF-8");
					addRequestParameter(name, value);
				}
				/*
				 * The Jetty servlet container "delete()" method really
				 * deletes the uploaded data.
				 */
				//part.delete();
			}
		} catch (IOException | ServletException e) {
			log.error("", e);
		}
	}
	
	private String getSubmittedFileName(Part part) {
		final String disposition = part.getHeader("Content-Disposition");
	    if (disposition != null) {
	        if (disposition.startsWith("form-data")) {
	           String fileName = ServletUtil.extractQuotedValueFromHeader(disposition, "filename");
	            if (fileName != null) {
	                return fileName;
	            }
	        }
	    }
	    return null;
	}
	
	private boolean isMultipartContent(HttpServletRequest request) {
		if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
		
		String contentType = request.getContentType();
		if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/")) {
            return true;
        }
		return false;
	}
	
	/**
	 * Get parameters the standard way
	 * @param ureq
	 */
	private void doInitRequestParameter(UserRequest ureq) {
		Set<String> keys = ureq.getParameterSet();
		for (String key : keys) {
			String[] values = ureq.getHttpReq().getParameterValues(key);
			if (values != null) {
				requestParams.put(key, values);	
			} else {
				addRequestParameter(key, ureq.getParameter(key));					
			}
		}
	}

	/**
	 * Internal helper to add the request parameters to the request param map.
	 * Takes care of multi value parameters
	 * 
	 * @param key
	 * @param value
	 */
	private void addRequestParameter(String key, String value) {
		String[] values = requestParams.get(key);
		if (values == null) {
			// First element for this key
			values = new String[]{value};
			//use log.debug instead of System.out.println("PARAMS:"+key+" :: "+value);
		}
		else {
			// A multi-key element (e.g. radio button)
			values = ArrayHelper.addToArray(values, value, true);
		//use log.debug instead of System.out.println("PARAMS:"+key+" ::[array of values]");
		}
		
		requestParams.put(key, values);		
	}
	
	/**
	 * Internal helper to clear the temporary request parameter and file maps.
	 * Will delete all uploaded files if they have not been removed by the
	 * responsible FormItem.
	 */
	private void doClearRequestParameterAndMultipartData() {
		for (Entry<String, File> entry : requestMultipartFiles.entrySet()) {
			File tmpFile = entry.getValue();
			if (tmpFile.exists()) tmpFile.delete();
		}
		requestMultipartFiles.clear();
		requestMultipartFileNames.clear();
		requestMultipartFileMimeTypes.clear();
		requestParams.clear();
		requestError = REQUEST_ERROR_NO_ERROR;
	}

	/**
	 * Check if there was an error while parsing this request. See REQUEST_ERROR_*
	 * constants
	 * 
	 * @return the last error code
	 */
	public int getLastRequestError() {
		return requestError;
	}

	/**
	 * @param ureq
	 */
	public void submit(UserRequest ureq) {
		submit(ureq, org.olat.core.gui.components.form.Form.EVNT_VALIDATION_OK);
	}
	
	public void submitAndNext(UserRequest ureq) {
		submit(ureq, org.olat.core.gui.components.form.Form.EVNT_VALIDATION_NEXT);
	}
	
	public void submitAndFinish(UserRequest ureq) {
		submit(ureq, org.olat.core.gui.components.form.Form.EVNT_VALIDATION_FINISH);
	}
	
	private final void submit(UserRequest ureq, Event validationOkEvent) {	
		ValidatingFormComponentVisitor vfcv = new ValidatingFormComponentVisitor();
		FormComponentTraverser ct = new FormComponentTraverser(vfcv, formLayout, false);
		ct.visitAll(ureq);
		// validate all form elements and gather validation status
		ValidationStatus[] status = vfcv.getStatus();
		//
		boolean isValid = status == null || status.length == 0;
		// let the businesslogic validate this is implemented by the outside listener
		
		for (Iterator<FormBasicController> iterator = formListeners.iterator(); iterator.hasNext();) {
			FormBasicController fbc = iterator.next();
			//let all listeners validate and calc the total isValid
			//let further validate even if one fails.
			isValid = fbc.validateFormLogic(ureq) && isValid;
		}

		formWrapperComponent.fireValidation(ureq, isValid, validationOkEvent);
		isValidAndSubmitted = isValid;
		hasAlreadyFired = true;
	}
	
	
	/**
	 * @param ureq
	 */
	public void reset(UserRequest ureq) {
		ResettingFormComponentVisitor rfcv = new ResettingFormComponentVisitor();
		FormComponentTraverser ct = new FormComponentTraverser(rfcv, formLayout, false);
		ct.visitAll(ureq);//calls reset on all elements!
		//
		evalAllFormDependencyRules(ureq);
		//
		formWrapperComponent.fireFormEvent(ureq, FormEvent.RESET);
		hasAlreadyFired = true;
	}

	/**
	 * @return
	 */
	ComponentCollection getFormLayout() {
		return (ComponentCollection) formLayout.getComponent();
	}
	
	FormItemContainer getFormItemContainer() {
		return formLayout;
	}

	public Component getInitialComponent() {
		return formWrapperComponent;
	}

	/**
	 * add another listener then the default listener, which is added at construction
	 * time.
	 * @param listener
	 */
	public void addListener(Controller listener){
		formWrapperComponent.addListener(listener);
	}

	public void removeListener(Controller listener){
		formWrapperComponent.removeListener(listener);
	}

	/**
	 * Return the form parameter for a certain key. This takes care if a multipart
	 * form has been used or a normal form.
	 * <p>
	 * LiveCycle scope: only within one call of evalFormRequest() !
	 * 
	 * @param key
	 * @return the value of the parameter with key 'key'
	 */
	public String getRequestParameter(String key) {
		String[] values = requestParams.get(key);
		if (values != null) return values[0];
		else return null;
	}

	/**
	 * Return the form parameter values for a certain key. This takes care if a
	 * multipart form has been used or a normal form. <br />
	 * This method is used to retrieve multi-value elements, e.g. radio buttons.<br />
	 * Use the getRequestParameter() to retrieve single value elements, e.g. input
	 * type=text elements
	 * 
	 * @param key
	 * @return Array of values for this key
	 */
	public String[] getRequestParameterValues(String key) {
		return requestParams.get(key);
	}
	
	/**
	 * Return the form parameter set. This takes care if a multipart form has been
	 * used or a normal form.
	 * <p>
	 * LiveCycle scope: only within one call of evalFormRequest() !
	 * 
	 * @return the Set of parameters
	 */
	public Set<String> getRequestParameterSet() {
		return requestParams.keySet();
	}

	/**
	 * Return the multipart file for this key
	 * <p>LiveCycle scope: only within one call of evalFormRequest() !
	 * @param key
	 * @return
	 */
	public File getRequestMultipartFile(String key) {
		return requestMultipartFiles.get(key);
	}

	/**
	 * Return the multipart file name for this key: 
	 * <p>LiveCycle scope: only within one call of evalFormRequest() !
	 * @param key
	 * @return
	 */
	public String getRequestMultipartFileName(String key) {
		return requestMultipartFileNames.get(key);
	}

	/**
	 * Return the multipart file mime type (content type) for this key: 
	 * <p>LiveCycle scope: only within one call of evalFormRequest() !
	 * @param key
	 * @return
	 */
	public String getRequestMultipartFileMimeType(String key) {
		return requestMultipartFileMimeTypes.get(key);
	}

	/**
	 * @return The set of multipart file identifyers
	 */
	public Set<String> getRequestMultipartFilesSet() {
		return requestMultipartFiles.keySet();
	}
	
	
	/**
	 * Description:<br>
	 * TODO: patrickb Class Description for EvaluatingFormComponentVisitor
	 * <P>
	 * Initial Date: 04.12.2006 <br>
	 * 
	 * @author patrickb
	 */
	private class EvaluatingFormComponentVisitor implements FormComponentVisitor {

		private boolean foundDispatchItem = false;
		private FormItem dispatchFormItem = null;
		private String dispatchId;

		public EvaluatingFormComponentVisitor(String dispatchUri) {
			this.dispatchId = dispatchUri;
		}

		public FormItem getDispatchToComponent() {
			return dispatchFormItem;
		}

		@Override
		public boolean visit(FormItem fi, UserRequest ureq) {
			/*
			 * check if this is the FormItem to be dispatched
			 */
			Component tmp = fi.getComponent();
			String tmpD = FormBaseComponentIdProvider.DISPPREFIX + tmp.getDispatchID();

			if (!foundDispatchItem && tmpD.equals(dispatchId)) {
				dispatchFormItem = fi;
				foundDispatchItem = true;
			}
			
			/*
			 * let the form item evaluate the form request, e.g. get out its data
			 */
			fi.evalFormRequest(ureq);
			return true;// visit further
		}
	}
	
	private static class SubmitFormComponentVisitor implements FormComponentVisitor {

		private Submit submit;

		public Submit getSubmit() {
			return submit;
		}

		@Override
		public boolean visit(FormItem fi, UserRequest ureq) {
			if(fi instanceof Submit) {
				submit = (Submit)fi;
				return false;
			}
			return true;
		}
	}

	private class FindParentFormComponentVisitor implements FormComponentVisitor {

		private FormItem child;
		private FormItemContainer parentContainer = formLayout;

		public FindParentFormComponentVisitor(FormItem child){
			this.child = child;
		}
		
		public FormItemContainer getParent(){
			return parentContainer;
		}
		
		public boolean visit(FormItem comp, UserRequest ureq) {
			if (comp instanceof FormItemContainer) {
				FormItemContainer new_name = (FormItemContainer) comp;
				if(new_name.hasFormComponent(child)){
					parentContainer = (FormItemContainer)comp;
					return false;
				}
			}
			return true;
		}
		
	}
	

	private static class FormDependencyRulesInitComponentVisitor implements FormComponentVisitor {

		public boolean visit(FormItem comp, UserRequest ureq) {
			if (comp instanceof FormItemContainer) {
				FormItemContainer fic = (FormItemContainer)comp;
				Iterable<FormItem> pairs = fic.getFormItems();
				//go to next container if no elements inside
				if(pairs != null) {
					//otherwise iterate overall elements and evaluate dependency rules
					for (FormItem item : pairs) {
						fic.evalDependencyRuleSetFor(ureq, item);
					}
				}
			}
			return true;
		}
		
	}
	
	private static class ValidatingFormComponentVisitor implements FormComponentVisitor {
		final List<ValidationStatus> tmp = new ArrayList<ValidationStatus>();

		public ValidationStatus[] getStatus() {
			return sort(tmp);
		}

		@Override
		public boolean visit(FormItem comp, UserRequest ureq) {
			if(comp.isVisible() && comp.isEnabled()){
				//validate only if form item is visible and enabled
				comp.validate(tmp);
			}
			return true;
		}
		
		private ValidationStatus[] sort(List<ValidationStatus> statusList) {
			Collections.sort(statusList, new ErrorsGtWarningGtInfo());
			/*
			 * remove NOERRORS size bigger 1; NOERROR -> Level == OFF > SEVERE > WARNING >
			 * INFO
			 */
			if (statusList.size() > 1) {
				for(int i=0;i<statusList.size();i++) {
					if(statusList.get(i)==ValidationStatus.NOERROR) {
						//aha found one to remove. Remove shifts all elements to the left, e.g. subtracts one of the indices
						statusList.remove(i);
						//thus we have to loop on place to remove all NOERRORS which shift to the ith place.
						while(statusList.get(i)==ValidationStatus.NOERROR) {
							statusList.remove(i);
						}
					}
				}
			}

			return statusList.toArray(new ValidationStatus[statusList.size()]);
		}

		private static class ErrorsGtWarningGtInfo implements Comparator<ValidationStatus> {
			@Override
			public int compare(ValidationStatus s1, ValidationStatus s2) {
				return s2.getLevel().intValue() - s1.getLevel().intValue();
			}
		}
	}

	private static class ResettingFormComponentVisitor implements FormComponentVisitor {

		public boolean visit(FormItem comp, UserRequest ureq) {
			//reset all fields including also non visible and disabled form items!
			comp.reset();
			return true;
		}
	}
	
	public String getFormId() {
		return formWrapperComponent.getDispatchID();
	}

	public String getDispatchFieldId() {
		return dispatchFieldId;
	}

	public String getFormName() {
		return formName;
	}

	public void fireFormEvent(UserRequest ureq, FormEvent event) {
		formWrapperComponent.fireFormEvent(ureq, event);
		hasAlreadyFired = true;
	}

	boolean hasAlreadyFired(){
		return hasAlreadyFired;
	}
	/**
	 * @return Returns the eventFieldId.
	 */
	public String getEventFieldId() {
		return eventFieldId;
	}

	public int getAction() {
		return action;
	}

	/**
	 * @param ureq
	 */
	void evalAllFormDependencyRules(UserRequest ureq) {
		FormDependencyRulesInitComponentVisitor fdrocv = new FormDependencyRulesInitComponentVisitor();
		FormComponentTraverser ct = new FormComponentTraverser(fdrocv, formLayout, false);
		ct.visitAll(ureq);//visit all container and eval container with its elements!
	}

	public boolean isSubmittedAndValid(){
		return isValidAndSubmitted;
	}

	/**
	 * true if the form should not loose unsubmitted changes, if another link
	 * is clicked which throws away the changes.
	 * @return
	 */
	public boolean isDirtyMarking() {
		return isDirtyMarking;
	}
	public void setDirtyMarking(boolean isDirtyMarking){
		this.isDirtyMarking = isDirtyMarking;
	}

	public void addSubFormListener(FormBasicController formBasicController) {
		this.formListeners.add(formBasicController);
		addListener(formBasicController);
	}

	public void removeSubFormListener(FormBasicController formBasicController) {
		this.formListeners.remove(formBasicController);
		removeListener(formBasicController);
	}

	public void setMultipartEnabled(boolean multipartEnabled) {
		this.multipartEnabled = multipartEnabled;
	}
	
	public boolean isMultipartEnabled() {
		return multipartEnabled;
	}
}
