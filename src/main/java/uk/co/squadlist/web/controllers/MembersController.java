package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.forms.MemberDetails;
import uk.co.squadlist.web.urls.UrlBuilder;

@Controller
public class MembersController {
	
	private static Logger log = Logger.getLogger(MembersController.class);
	
	private final SquadlistApi api;
	private final LoggedInUserService loggedInUserService;
	private final UrlBuilder urlBuilder;
	
	@Autowired
	public MembersController(SquadlistApi api, LoggedInUserService loggedInUserService, UrlBuilder urlBuilder) {
		this.api = api;
		this.loggedInUserService = loggedInUserService;
		this.urlBuilder = urlBuilder;
	}

	@RequestMapping("/member/{id}")
    public ModelAndView member(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("memberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(SquadlistApi.INSTANCE, id));
    	return mv;
    }
	
	@RequestMapping(value="/member/new", method=RequestMethod.GET)
    public ModelAndView newMember(@ModelAttribute("member") MemberDetails memberDetails) throws Exception {    	
		ModelAndView mv = new ModelAndView("newMember");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("squads", api.getSquads(SquadlistApi.INSTANCE));
		return mv;
    }
	
	@RequestMapping(value="/member/new", method=RequestMethod.POST)
    public ModelAndView newMemberSubmit(@ModelAttribute("member") MemberDetails memberDetails) throws Exception {
		final Member newMember = api.createMember(SquadlistApi.INSTANCE, memberDetails.getFirstName(), memberDetails.getLastName(), 
				api.getSquad(SquadlistApi.INSTANCE, memberDetails.getSquad()));
		final ModelAndView mv = new ModelAndView(new RedirectView(urlBuilder.memberUrl(newMember)));
		return mv;
    }
	
	@RequestMapping("/member/{id}/edit")
    public ModelAndView editMember(@PathVariable String id) throws Exception {
    	ModelAndView mv = new ModelAndView("editMemberDetails");
		mv.addObject("loggedInUser", loggedInUserService.getLoggedInUser());
    	mv.addObject("member", api.getMemberDetails(SquadlistApi.INSTANCE, id));
    	return mv;
    }
	
	@RequestMapping(value="/member/{id}/edit", method=RequestMethod.POST)
    public ModelAndView updateMember(@PathVariable String id, @ModelAttribute("member") MemberDetails memberDetails) throws Exception {		
		final Member member = api.getMemberDetails(SquadlistApi.INSTANCE, id);
		log.info("Updating member details: " + member.getId());
		member.setFirstName(memberDetails.getFirstName());
		member.setLastName(memberDetails.getLastName());
		
		api.updateMemberDetails(SquadlistApi.INSTANCE, member);		
		return new ModelAndView(new RedirectView(urlBuilder.memberUrl(member)));	
    }
	
}
