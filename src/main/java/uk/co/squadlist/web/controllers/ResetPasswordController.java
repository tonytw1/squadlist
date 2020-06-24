package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.exceptions.UnknownMemberException;

import com.google.common.base.Strings;

import java.io.IOException;

@Controller
public class ResetPasswordController {
	
	private final static Logger log = Logger.getLogger(ResetPasswordController.class);

	private final InstanceConfig instanceConfig;
	private final SquadlistApi squadlistApi;

	@Autowired
	public ResetPasswordController(InstanceConfig instanceConfig, SquadlistApiFactory squadlistApiFactory) throws IOException {
		this.instanceConfig = instanceConfig;
		this.squadlistApi = squadlistApiFactory.createClient();
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.GET)
	public ModelAndView resetPasswordPrompt() throws Exception {
		final ModelAndView mv = new ModelAndView("resetPassword");
		mv.addObject("title", "Reset password");
		return mv;
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public ModelAndView resetPassword(@RequestParam(value = "username", required = false) String username) throws Exception {
		if (Strings.isNullOrEmpty(username)) {
			final ModelAndView mv = new ModelAndView("resetPassword");
			mv.addObject("errors", true);
			mv.addObject("title", "Reset password");
			return mv;
		}

		String instance = instanceConfig.getInstance();
		log.info("Resetting password for: " + instance + " / " + username);
		try {
			squadlistApi.resetPassword(instance, username);	// TODO errors
			log.info("Reset password call successful for: " + username);
			final ModelAndView mv = new ModelAndView("resetPasswordSent");
			mv.addObject("title", "Reset password");
			return mv;
			
		} catch (UnknownMemberException e) {
			final ModelAndView mv = new ModelAndView("resetPassword");
			mv.addObject("title", "Reset password");
			mv.addObject("errors", true);
			return mv;
		}
	}
	
	@RequestMapping(value = "/reset-password/confirm", method = RequestMethod.GET)
	public ModelAndView confirmPasswordReset(@RequestParam String token) throws Exception {
		try {
			final String newPassword = squadlistApi.confirmResetPassword(instanceConfig.getInstance(), token);

			return new ModelAndView("resetPasswordConfirm").
					addObject("newPassword", newPassword);
		} catch (Exception e) {
			return new ModelAndView("resetPasswordInvalidToken");
		}
	}
	
}
