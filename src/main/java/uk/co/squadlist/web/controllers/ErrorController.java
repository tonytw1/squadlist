package uk.co.squadlist.web.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private final static Logger log = Logger.getLogger(ErrorController.class);

    private final HttpServletRequest request;

    @Autowired
    public ErrorController(HttpServletRequest request) {
        this.request = request;
    }

    @RequestMapping("/error")
    public ModelAndView error() {
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = null;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
        }

        if (statusCode != null && statusCode == HttpStatus.NOT_FOUND.value()) {
            log.info("404 page shown for path: " + path);
            return new ModelAndView("404");
        }

        log.info("Error page shown for status " + statusCode + " on path: " + path);
        return new ModelAndView("404");
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
