package uk.co.squadlist.web.controllers

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import org.apache.log4j.Logger
import org.codehaus.jackson.map.ObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.co.squadlist.web.api.InstanceSpecificApiClient
import uk.co.squadlist.web.api.SquadlistApiFactory
import uk.co.squadlist.web.auth.LoggedInUserService
import uk.co.squadlist.web.context.GoverningBodyFactory
import uk.co.squadlist.web.model.Member
import uk.co.squadlist.web.services.PreferedSquadService
import uk.co.squadlist.web.urls.UrlBuilder
import uk.co.squadlist.web.views.CsvOutputRenderer
import uk.co.squadlist.web.views.PermissionsHelper
import uk.co.squadlist.web.views.ViewFactory
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
public class EntryDetailsController(val instanceSpecificApiClient: InstanceSpecificApiClient,
                                    val preferedSquadService: PreferedSquadService,
                                    val viewFactory: ViewFactory,
                                    val entryDetailsModelPopulator: EntryDetailsModelPopulator,
                                    val csvOutputRenderer: CsvOutputRenderer, val governingBodyFactory: GoverningBodyFactory,
                                    val squadlistApiFactory: SquadlistApiFactory, val loggedInUserService: LoggedInUserService,
                                    val urlBuilder: UrlBuilder, val permissionsHelper: PermissionsHelper) {

    private val log = Logger.getLogger(EntryDetailsController::class.java)

    private val squadlistApi = squadlistApiFactory.createClient()

    private val entryDetailsHeaders: List<String> = Lists.newArrayList("First name", "Last name", "Date of birth", "Effective age", "Age grade",
            "Weight", "Rowing points", "Rowing status",
            "Sculling points", "Sculling status", "Registration number")

    @GetMapping("/entry-details/{squadId}")
    fun entrydetails(@PathVariable squadId: String): ModelAndView {
        val mv = viewFactory.getViewForLoggedInUser("entryDetails")
        mv.addObject("squads", instanceSpecificApiClient.squads)
        mv.addObject("governingBody", governingBodyFactory.governingBody)
        mv.addObject("urlBuilder", urlBuilder)
        mv.addObject("permissionsHelper", permissionsHelper)

        val squadToShow = preferedSquadService.resolveSquad(squadId)
        entryDetailsModelPopulator.populateModel(squadToShow, mv)
        return mv
    }

    @GetMapping("/entry-details/ajax")
    fun ajax(@RequestBody json: String): ModelAndView {
        val loggedInUserApi = squadlistApiFactory.createForToken(loggedInUserService.loggedInMembersToken)

        val selectedMembers = Lists.newArrayList<Member>()

        val readTree = ObjectMapper().readTree(json)
        val iterator = readTree.iterator()
        while (iterator.hasNext()) {
            selectedMembers.add(loggedInUserApi.getMember(iterator.next().asText()))
        }

        val rowingPoints = Lists.newArrayList<String>()
        val scullingPoints = Lists.newArrayList<String>()
        for (member in selectedMembers) {
            rowingPoints.add(member.rowingPoints)
            scullingPoints.add(member.scullingPoints)
        }

        val mv = viewFactory.getViewForLoggedInUser("entryDetailsAjax")
        if (!selectedMembers.isEmpty()) {
            mv.addObject("members", selectedMembers)

            val governingBody = governingBodyFactory.governingBody

            val crewSize = selectedMembers.size
            val isFullBoat = governingBody.boatSizes.contains(crewSize)
            mv.addObject("ok", isFullBoat)
            if (isFullBoat) {
                mv.addObject("rowingPoints", governingBody.getTotalPoints(rowingPoints))
                mv.addObject("rowingStatus", governingBody.getRowingStatus(rowingPoints))

                mv.addObject("scullingPoints", governingBody.getTotalPoints(scullingPoints))
                mv.addObject("scullingStatus", governingBody.getScullingStatus(scullingPoints))

                val datesOfBirth = Lists.newArrayList<Date>()
                for (member in selectedMembers) {
                    datesOfBirth.add(member.dateOfBirth)
                }

                val effectiveAge = governingBody.getEffectiveAge(datesOfBirth)
                if (effectiveAge != null) {
                    mv.addObject("effectiveAge", effectiveAge)
                    mv.addObject("ageGrade", governingBody.getAgeGrade(effectiveAge))
                }
            }
        }
        return mv
    }

    @GetMapping("/entry-details/{squadId}.csv")
    fun entrydetailsCSV(@PathVariable squadId: String, response: HttpServletResponse) {
        viewFactory.getViewForLoggedInUser("entryDetails")  // TODO

        val squadToShow = preferedSquadService.resolveSquad(squadId)
        val squadMembers = squadlistApi.getSquadMembers(squadToShow.id)

        val entryDetailsRows = entryDetailsModelPopulator.getEntryDetailsRows(squadMembers)
        csvOutputRenderer.renderCsvResponse(response, entryDetailsHeaders, entryDetailsRows)
    }

    @GetMapping("/entry-details/selected.csv") // TODO Unused
    fun entrydetailsSelectedCSV(@RequestParam members: String, response: HttpServletResponse) {
        log.info("Selected members: $members")
        val selectedMembers = Lists.newArrayList<Member>()
        val iterator = Splitter.on(",").split(members).iterator()
        while (iterator.hasNext()) {
            val selectedMemberId = iterator.next()
            log.info("Selected member id: $selectedMemberId")
            selectedMembers.add(squadlistApi.getMember(selectedMemberId))
        }

        csvOutputRenderer.renderCsvResponse(response, entryDetailsHeaders, entryDetailsModelPopulator.getEntryDetailsRows(selectedMembers))
    }

}