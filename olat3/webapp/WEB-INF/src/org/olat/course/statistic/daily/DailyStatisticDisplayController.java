package org.olat.course.statistic.daily;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.statistic.DateChooserForm;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;

public class DailyStatisticDisplayController extends StatisticDisplayController {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(DailyStatisticDisplayController.class);

	private VelocityContainer dailyStatisticFormVc_;
	private VelocityContainer dailyStatisticVc_;
	private DateChooserForm form_;

	public DailyStatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
		super(ureq, windowControl, course, statisticManager);
	}
	
	@Override
	protected Component createInitialComponent(UserRequest ureq) {
		setVelocityRoot(Util.getPackageVelocityRoot(getClass()));

		dailyStatisticVc_ = this.createVelocityContainer("dailystatisticparent");
		
		dailyStatisticFormVc_ = this.createVelocityContainer("dailystatisticform");
		form_ = new DateChooserForm(ureq, getWindowControl(), 7);
		listenTo(form_);
		dailyStatisticFormVc_.put("statisticForm", form_.getInitialComponent());
		dailyStatisticFormVc_.contextPut("statsSince", getStatsSinceStr(ureq));

		dailyStatisticVc_.put("dailystatisticform", dailyStatisticFormVc_);

		Component parentInitialComponent = super.createInitialComponent(ureq);
		dailyStatisticVc_.put("statistic", parentInitialComponent);
		
		return dailyStatisticVc_;
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == form_ && event == Event.DONE_EVENT) {
			// need to regenerate the statisticResult
			
			if (!(getStatisticManager() instanceof DailyStatisticManager)) {
				// should not occur - config error!
				showWarning("datechooser.error");
				return;
			}

			// and now recreate the table controller
			recreateTableController(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected StatisticResult recalculateStatisticResult(UserRequest ureq) {
		// recalculate the statistic result based on the from and to dates.
		// do this by going via sql (see DailyStatisticManager)
		DailyStatisticManager dailyStatisticManager = (DailyStatisticManager)getStatisticManager();			
		StatisticResult statisticResult = 
			dailyStatisticManager.generateStatisticResult(ureq, getCourse(), getCourseRepositoryEntryKey(), form_.getFromDate(), form_.getToDate());
		return statisticResult;
	}
}
