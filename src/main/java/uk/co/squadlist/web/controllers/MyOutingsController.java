package uk.co.squadlist.web.controllers;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.DisplayObjectFactory;

@Controller
public class MyOutingsController {
		
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	private DisplayObjectFactory displayObjectFactory;
	
	@Autowired
	public MyOutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, DisplayObjectFactory displayObjectFactory) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.displayObjectFactory = displayObjectFactory;
	}
	
	@RequestMapping("/")
    public ModelAndView outings(@RequestParam(value = "month", required = false) String month) throws Exception {
    	ModelAndView mv = new ModelAndView("myOutings");
    	final String loggedInUser = loggedInUserService.getLoggedInUser();
		mv.addObject("loggedInUser", loggedInUser);
    	
		Date startDate = DateHelper.startOfCurrentOutingPeriod().toDate();
		Date endDate = DateHelper.endOfCurrentOutingPeriod().toDate();		
		if (month != null) {
    		final DateTime monthDateTime = ISODateTimeFormat.yearMonth().parseDateTime(month);	// TODO Can be moved to spring?
    		startDate = monthDateTime.toDate();
    		endDate = monthDateTime.plusMonths(1).toDate();
    	}
		
		mv.addObject("outings", displayObjectFactory.makeDisplayObjectsFor(api
				.getAvailabilityFor(SquadlistApi.INSTANCE, loggedInUser,
						startDate, endDate)));

		mv.addObject("outingMonths", api.getMemberOutingMonths(SquadlistApi.INSTANCE, loggedInUser));
    	mv.addObject("availabilityOptions", api.getAvailabilityOptions(SquadlistApi.INSTANCE));
    	return mv;
    }
	
}
