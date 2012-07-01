package uk.co.squadlist.web.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.exceptions.HttpFetchException;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.model.display.DisplayOuting;

@Controller
public class SquadsController {
	
	private SquadlistApi api;
	private LoggedInUserService loggedInUserService;
	
	@Autowired
	public SquadsController(SquadlistApi api, LoggedInUserService loggedInUserService) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
	}
	
	@RequestMapping("/squad/{id}/availability")
    public ModelAndView availability(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadAvailability");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));

    	Map<String, String> availability = new HashMap<String, String>();
    	final List<Outing> outings = api.getSquadOutings(id);
		mv.addObject("outings", makeDisplayObjectsFor(outings));    	
    	for (Outing outing : outings) {
			Map<String, String> outingAvailability = api.getOutingAvailability(outing.getId());
			for (String member : outingAvailability.keySet()) {
				availability.put(outing.getId() + "-" + member, outingAvailability.get(member));				
			}
		}
    	mv.addObject("availability", availability);
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/contacts")
    public ModelAndView contacts(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadContacts");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/entrydetails")
    public ModelAndView entrydetails(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadEntryDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("members", api.getSquadMembers(id));
    	return mv;
    }
	
	@RequestMapping("/squad/{id}/outings")
    public ModelAndView outings(@PathVariable int id) throws Exception {
    	ModelAndView mv = new ModelAndView("squadOutings");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());

		mv.addObject("squad", api.getSquad(id));
    	mv.addObject("outings", makeDisplayObjectsFor(api.getSquadOutings(id)));
    	return mv;
    }

	private List<DisplayOuting> makeDisplayObjectsFor(List<Outing> outings) throws JsonParseException, JsonMappingException, IOException, HttpFetchException {
		List<DisplayOuting> displayOutings = new ArrayList<DisplayOuting>();
		final Map<Integer, Squad> squadsMap = api.getSquadsMap();
		for (Outing outing : outings) {
			displayOutings.add(new DisplayOuting(outing.getId(), 
					outing.getSquad(), 
					squadsMap.get(outing.getSquad()).getName(), 
					outing.getDate()));
		}
		return displayOutings;
	}
	
}
