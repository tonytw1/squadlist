package uk.co.squadlist.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import uk.co.squadlist.web.exceptions.InvalidSquadException;
import uk.co.squadlist.web.model.AvailabilityOption;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Outing;
import uk.co.squadlist.web.model.OutingAvailability;
import uk.co.squadlist.web.model.OutingWithSquadAvailability;
import uk.co.squadlist.web.model.Squad;

public class SquadlistApiIT {

	private static final String TEST_INSTANCE_PREFIX = "testit";

	private SquadlistApi api;
	private String instanceName;

	@Before
	public void setup() {
		api = new SquadlistApi("http://localhost:9090");
		instanceName = TEST_INSTANCE_PREFIX + System.currentTimeMillis();
		
		final List<Instance> instances = api.getInstances();
		System.out.println(instances);
		// TODO assert not already present
		
		final Instance instance = api.createInstance(instanceName, "Test instance");
		System.out.println(instance);	
	}
	
	@Test(expected=InvalidSquadException.class)
	public void shouldValidateNewSquadRequests() throws Exception {
		api.createSquad(instanceName, "");
	}
	
	@Test(expected=InvalidSquadException.class)
	public void shouldNowAllowDuplicateSquadNamesWhenAddingNewSquads() throws Exception {
		api.createSquad(instanceName, "New squad");
		api.createSquad(instanceName, "New squad");
	}
	
	@Test
	public void canCreateAndPopulateNewInstances() throws Exception {		
		
		List<AvailabilityOption> availabilityOptions = api.getAvailabilityOptions(instanceName);
		System.out.println(availabilityOptions);
		assertTrue(availabilityOptions.isEmpty());
		
		final AvailabilityOption available = api.createAvailabilityOption(instanceName, "Available");
		availabilityOptions = api.getAvailabilityOptions(instanceName);
		System.out.println(availabilityOptions);
		assertEquals(1, availabilityOptions.size());
		assertEquals("Available", availabilityOptions.get(0).getLabel());
		
		api.createAvailabilityOption(instanceName, "Not available");
		
		List<Squad> squads = api.getSquads(instanceName);
		System.out.println(squads);
		assertTrue(squads.isEmpty());
		
		Squad squad = api.createSquad(instanceName, "Novice men");
		System.out.println(squad);
		assertEquals("novice-men", squad.getId());
		assertEquals("Novice men", squad.getName());
		
		final Squad seniorWoman = api.createSquad(instanceName, "Senior women");
		
		squads = api.getSquads(instanceName);
		assertEquals(2, squads.size());
		List<Member> members = api.getMembers(instanceName);
		System.out.println(members);
		assertTrue(members.isEmpty());
		
		squad = api.getSquad(instanceName, squad.getId());
		assertNotNull(squad);
		
		api.createMember(instanceName, "John", "Smith", squad);
		members = api.getMembers(instanceName);
		System.out.println(members);
		assertEquals(1, members.size());
		
		Member newMember = members.get(0);
		assertEquals("JOHNSMITH", newMember.getId());
		assertEquals("John", newMember.getFirstName());
		assertEquals("Smith", newMember.getLastName());
		
		newMember = api.getMemberDetails(instanceName, newMember.getId());
		assertNotNull(newMember);
		
		assertFalse(newMember.getSquads().isEmpty());
		assertEquals(newMember.getSquads().get(0).getName(), "Novice men");
		
		api.createMember(instanceName, "Andy", "Green", squad);
		api.createMember(instanceName, "Tim", "Brown", squad);
		api.createMember(instanceName, "Jane", "Smith", seniorWoman);
		
		List<Outing> outings = api.getOutings(instanceName);
		System.out.println(outings);
		assertTrue(outings.isEmpty());
		
		final LocalDateTime outingDate = new LocalDateTime(2012, 11, 5, 8, 0);
		final Outing newOuting = api.createOuting(instanceName, squad.getId(), outingDate);	
		assertEquals("novice-men-2012-11-05-0800", newOuting.getId());
		assertEquals("Novice men", newOuting.getSquad().getName());
		assertEquals(outingDate.toDateTime(DateTimeZone.forID("Europe/London")), new DateTime(newOuting.getDate()));
		
		final LocalDateTime outingDateDuringBST = new LocalDateTime(2012, 6, 5, 8, 0, 0);
		final Outing newOutingDuringBST = api.createOuting(instanceName, squad.getId(), outingDateDuringBST);
		assertEquals("novice-men-2012-06-05-0800", newOutingDuringBST.getId());
		assertEquals(outingDateDuringBST.toDateTime(DateTimeZone.forID("Europe/London")), new DateTime(newOutingDuringBST.getDate()));
		assertEquals("2012-06-05T08:00:00.000+01:00", new DateTime(newOutingDuringBST.getDate()).toString());
		
		outings = api.getOutings(instanceName);
		assertEquals(2, outings.size());
		
		final List<Outing> squadOutings = api.getSquadOutings(instanceName, squad.getId(), new DateTime(1970, 1, 1, 0, 0).toDate(), new DateTime(2020, 1, 1, 0, 0).toDate());
		assertEquals(2, squadOutings.size());
		
		final OutingAvailability outingAvailability = api.setOutingAvailability(instanceName, newMember.getId(), newOuting.getId(), available.getLabel());
		assertEquals("Available", outingAvailability.getAvailability());
		
		Map<String, String> updatedOutingAvailability = api.getOutingAvailability(instanceName, newOuting.getId());
		assertEquals("Available", updatedOutingAvailability.get(newMember.getId()));
		
		final Date dayBeforeEarliestOuting = new DateTime(newOuting.getDate()).minusDays(1).toDate();
		List<OutingAvailability> membersAvailability = api.getAvailabilityFor(instanceName, newMember.getId(), dayBeforeEarliestOuting, DateTime.now().toDate());		
		final OutingAvailability membersOutingAvailability = membersAvailability.get(0);
		assertEquals(newOuting.getId(), membersOutingAvailability.getOuting().getId());
		assertEquals("Available", membersOutingAvailability.getAvailability());
		
		final List<OutingWithSquadAvailability> squadAvailability = api.getSquadAvailability(instanceName, newOuting.getSquad().getId(), dayBeforeEarliestOuting, DateTime.now().toDate());		
		final OutingWithSquadAvailability membersAvailabiltyForSquadOuting = squadAvailability.get(0);
		assertEquals(newOuting.getId(), membersAvailabiltyForSquadOuting.getOuting().getId());
		assertEquals("Available", membersAvailabiltyForSquadOuting.getAvailability().get(newMember.getId()));
				
		newMember.setFirstName("Jim");
		api.updateMemberDetails(instanceName, newMember);
		
		newMember = api.getMemberDetails(instanceName, newMember.getId());
		assertEquals("Jim", newMember.getFirstName());
	}
	
}
