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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.course.editor.NodeEditController;
import org.olat.group.BusinessGroup;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date: 6 oct. 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingsEditController extends FormBasicController {

	private FormLink newButton;
	private FormLink occupiedRoomsLink;
	private final List<BookingDisplay> bookingDisplays = new ArrayList<BookingDisplay>();

	private CloseableModalController cmc;
	private DialogBoxController dialogCtr;
	private ViteroBookingEditController bookingController;
	private ViteroRoomsOverviewController roomsOverviewController;
	
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final ViteroManager viteroManager;

	public ViteroBookingsEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores) {
		super(ureq, wControl, "edit");
		
		this.group = group;
		this.ores = ores;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		reloadModel();
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons-cont", getTranslator());
		formLayout.add(buttonLayout);
		newButton = uifactory.addFormLink("vc.booking.new", buttonLayout, Link.BUTTON);
		occupiedRoomsLink = uifactory.addFormLink("vc.booking.roomsOverview", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	protected void reloadModel() {
		bookingDisplays.clear(); 
		List<ViteroBooking> bookings = viteroManager.getBookings(group, ores);
		for(ViteroBooking booking:bookings) {
			BookingDisplay display = new BookingDisplay(booking);
			display.setDeleteButton(uifactory.addFormLink("delete", flc, Link.BUTTON));
			display.setEditButton(uifactory.addFormLink("edit", flc, Link.BUTTON));
			bookingDisplays.add(display);
		}
		flc.contextPut("bookingDisplays", bookingDisplays);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == newButton) {
			newBooking(ureq);
		} else if (source == occupiedRoomsLink) {
			occupiedRooms(ureq);
		} else if (source instanceof FormLink) {
			for(BookingDisplay display: bookingDisplays) {
				if(display.getDeleteButton() == source) {
					confirmDeleteBooking(ureq, display);
					break;
				} else if(display.getEditButton() == source) {
					ViteroBooking viteroBooking = display.getMeeting();
					editBooking(ureq, viteroBooking);
					break;
				}
			}
			reloadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc ) {
			removeAsListenerAndDispose(bookingController);
			removeAsListenerAndDispose(cmc);
		} else if (source == bookingController) {
			if(Event.DONE_EVENT.equals(event)) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			removeAsListenerAndDispose(bookingController);
			removeAsListenerAndDispose(cmc);
			reloadModel();
		} else if(source == dialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				ViteroBooking booking = (ViteroBooking)dialogCtr.getUserObject();
				deleteBooking(ureq, booking);
			}
		}
	}
	
	protected void occupiedRooms(UserRequest ureq) {
		removeAsListenerAndDispose(bookingController);

		roomsOverviewController = new ViteroRoomsOverviewController(ureq, getWindowControl());			
		listenTo(roomsOverviewController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), roomsOverviewController.getInitialComponent(), true, translate("vc.booking.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void deleteBooking(UserRequest ureq, ViteroBooking booking) {
		if( viteroManager.deleteBooking(booking)) {
			showInfo("vc.table.delete");
		} else {
			showError("vc.table.delete");
		}
		reloadModel();
	}

	protected void confirmDeleteBooking(UserRequest ureq, BookingDisplay bookingDisplay) {
		String title = translate("vc.table.delete");
		String text = translate("vc.table.delete.confirm");
		dialogCtr = activateOkCancelDialog(ureq, title, text, dialogCtr);
		dialogCtr.setUserObject(bookingDisplay.getMeeting());
	}
	
	protected void editBooking(UserRequest ureq, ViteroBooking viteroBooking) {
		removeAsListenerAndDispose(bookingController);

		bookingController = new ViteroBookingEditController(ureq, getWindowControl(), group, ores, viteroBooking);			
		listenTo(bookingController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bookingController.getInitialComponent(), true, translate("vc.booking.title"));
		listenTo(cmc);
		cmc.activate();
	}

	protected void newBooking(UserRequest ureq) {
		ViteroBooking viteroBooking = viteroManager.createBooking();
		editBooking(ureq, viteroBooking);
	}

	public class BookingDisplay {
		private final ViteroBooking meeting;
		private FormLink deleteButton;
		private FormLink editButton;
		
		public BookingDisplay(ViteroBooking meeting) {
			this.meeting = meeting;
		}

		public ViteroBooking getMeeting() {
			return meeting;
		}

		public Date getBegin() {
			return meeting.getStart();
		}
		
		public Date getEnd() {
			return meeting.getEnd();
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}

		public FormLink getEditButton() {
			return editButton;
		}

		public void setEditButton(FormLink editButton) {
			this.editButton = editButton;
		}
	}
}