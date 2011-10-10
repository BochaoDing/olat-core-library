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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingEditController extends FormBasicController {
	
	private DateChooser beginChooser;
	private DateChooser endChooser;
	private SingleSelection beginBufferEl;
	private SingleSelection endBufferEl;
	private SingleSelection roomSizeEl;
	
	private static final String[] bufferKeys = new String[]{"0", "15", "30", "45", "60"};
	private static final String[] bufferValues = bufferKeys;
	private final String[] roomSizes;
	
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final ViteroBooking booking;
	private final ViteroManager viteroManager;
	
	public ViteroBookingEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores, ViteroBooking booking) {
		super(ureq, wControl);
		
		this.group = group;
		this.ores = ores;
		this.booking = booking;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		
		List<Integer> sizes = viteroManager.getLicencedRoomSizes();
		roomSizes = new String[sizes.size()];
		
		int i=0;
		for(Integer size:sizes) {
			roomSizes[i++] = size.toString();
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//begin
		beginChooser = uifactory.addDateChooser("vc.table.begin", "", formLayout);
		beginChooser.setDisplaySize(21);
		beginChooser.setDateChooserTimeEnabled(true);
		beginChooser.setMandatory(true);
		beginChooser.setDate(booking.getStart());
		//end
		endChooser = uifactory.addDateChooser("vc.table.end", "", formLayout);
		endChooser.setDisplaySize(21);
		endChooser.setDateChooserTimeEnabled(true);
		endChooser.setMandatory(true);
		endChooser.setDate(booking.getEnd());
		
		//buffer start
		beginBufferEl = uifactory.addDropdownSingleselect("vc.table.beginBuffer", formLayout, bufferKeys, bufferValues, null);
		beginBufferEl.select(Integer.toString(booking.getStartBuffer()), true);
		
		//buffer end
		endBufferEl = uifactory.addDropdownSingleselect("vc.table.endBuffer", formLayout, bufferKeys, bufferValues, null);
		endBufferEl.select(Integer.toString(booking.getEndBuffer()), true);
		
		//room size
		roomSizeEl = uifactory.addDropdownSingleselect("vc.table.roomSize", formLayout, roomSizes, roomSizes, null);
		if(booking.getRoomSize() > 0) {
			roomSizeEl.select(Integer.toString(booking.getRoomSize()), true);
		}

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("ok", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public ViteroBooking getUserObject() {
		return booking;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		Date begin = beginChooser.getDate();

		Date end = endChooser.getDate();
		
		
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date begin = beginChooser.getDate();
		booking.setStart(begin);
		
		int beginBuffer = 0;
		if(beginBufferEl.isOneSelected()) {
			beginBuffer = Integer.parseInt(beginBufferEl.getSelectedKey());
		}
		booking.setStartBuffer(beginBuffer);
		
		Date end = endChooser.getDate();
		booking.setEnd(end);
		
		int endBuffer = 0;
		if(endBufferEl.isOneSelected()) {
			endBuffer = Integer.parseInt(endBufferEl.getSelectedKey());
		}
		booking.setStartBuffer(endBuffer);
		
		int roomSize = -1;
		if(roomSizeEl.isOneSelected()) {
			roomSize = Integer.parseInt(roomSizeEl.getSelectedKey());
		}
		booking.setRoomSize(roomSize);
		
		if(booking.getBookingId() >= 0) {
			viteroManager.updateBooking(group, ores, booking);
		} else {
			if(viteroManager.createBooking(group, ores, booking)) {
				showInfo("vc.check.ok");
			} else {
				showError("vc.check.nok");
			}
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}