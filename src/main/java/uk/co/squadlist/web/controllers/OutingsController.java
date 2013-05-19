package uk.co.squadlist.web.controllers;

import javax.validation.Valid;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.forms.OutingDetails;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.JsonSerializer;
import uk.co.squadlist.web.views.JsonView;

@Controller
public class OutingsController {
	
	private LoggedInUserService loggedInUserService;
	private SquadlistApi api;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public OutingsController(LoggedInUserService loggedInUserService, SquadlistApi api, UrlBuilder urlBuilder) {
		this.loggedInUserService = loggedInUserService;
		this.api = api;
		this.urlBuilder = urlBuilder;
	}
	
	@RequestMapping("/outings/{id}")
    public ModelAndView outings(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("outing");
    	final Outing outing = api.getOuting(InstanceConfig.INSTANCE, id);
    	    	
		mv.addObject("outing", outing);
		mv.addObject("outingMonths", api.getMemberOutingMonths(InstanceConfig.INSTANCE, loggedInUserService.getLoggedInUser()));
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());	// TODO shouldn't need todo this explictly on each controller - move to velocity context
		mv.addObject("squad", outing.getSquad());
    	mv.addObject("members", api.getSquadMembers(InstanceConfig.INSTANCE, outing.getSquad().getId()));
    	mv.addObject("availability", api.getOutingAvailability(InstanceConfig.INSTANCE, outing.getId()));
    	return mv;
    }
	
	@RequestMapping(value="/outings/new", method=RequestMethod.GET)
    public ModelAndView newOuting() throws Exception {
		final LocalDateTime defaultOutingDateTime = DateHelper.defaultOutingStartDateTime();
		final OutingDetails defaultOutingDetails = new OutingDetails(defaultOutingDateTime);  
    	return renderNewOutingForm(defaultOutingDetails);
	}
	
	@RequestMapping(value="/outings/new", method=RequestMethod.POST)
    public ModelAndView newOutingSubmit(@Valid @ModelAttribute("outing") OutingDetails outingDetails, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			return renderNewOutingForm(outingDetails);
		}		
		
    	final Outing outing = api.createOuting(InstanceConfig.INSTANCE, outingDetails.getSquad(), outingDetails.toLocalTime());
		ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.outingUrl(outing)));
    	return mv;
	}
	
	private ModelAndView renderNewOutingForm(OutingDetails outingDetails) {
		ModelAndView mv = new ModelAndView("newOuting");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("outingMonths", api.getMemberOutingMonths(InstanceConfig.INSTANCE, loggedInUserService.getLoggedInUser()));
		mv.addObject("squads", api.getSquads(InstanceConfig.INSTANCE));
		  	
		mv.addObject("outing", outingDetails);
		return mv;
	}
	
	@RequestMapping(value="/availability/ajax", method=RequestMethod.POST)
    public ModelAndView updateAvailability(
    		@RequestParam(value="outing", required=true) String outingId,
    		@RequestParam(value="availability", required=true) String availability) throws Exception {
    	final Outing outing = api.getOuting(InstanceConfig.INSTANCE, outingId);
    	
    	OutingAvailability result = api.setOutingAvailability(InstanceConfig.INSTANCE, loggedInUserService.getLoggedInUser(), outing.getId(), availability);    	
    	ModelAndView mv = new ModelAndView(new JsonView(new JsonSerializer()));
		mv.addObject("data", result);
    	return mv;
    }
	
    @ExceptionHandler(UnknownOutingException.class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND, reason = "No outing was found with the requested id")
    public void unknownUser(UnknownOutingException e) {
    }
    
}
