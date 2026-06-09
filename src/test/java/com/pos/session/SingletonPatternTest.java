package com.pos.session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SingletonPatternTest {

    @Before
    public void setUp() {
        POSSession.clear();
    }

    @After
    public void tearDown() {
        POSSession.clear();
    }

    @Test
    public void testInitialSessionState() {
        assertNull(POSSession.getCurrentUserId());
        assertEquals("Guest", POSSession.getCurrentRole());
        assertNull(POSSession.getCurrentName());
        assertTrue(POSSession.isGuest());
        assertFalse(POSSession.isAdmin());
        assertFalse(POSSession.isMember());
    }

    @Test
    public void testSetAdminUser() {
        POSSession.setCurrentUser(1, "Admin", "John Admin");

        assertEquals(Integer.valueOf(1), POSSession.getCurrentUserId());
        assertEquals("Admin", POSSession.getCurrentRole());
        assertEquals("John Admin", POSSession.getCurrentName());
        assertTrue(POSSession.isAdmin());
        assertFalse(POSSession.isMember());
        assertFalse(POSSession.isGuest());
    }

    @Test
    public void testSetMemberUser() {
        POSSession.setCurrentUser(2, "Member", "Jane Member");

        assertEquals(Integer.valueOf(2), POSSession.getCurrentUserId());
        assertEquals("Member", POSSession.getCurrentRole());
        assertEquals("Jane Member", POSSession.getCurrentName());
        assertTrue(POSSession.isMember());
        assertFalse(POSSession.isAdmin());
        assertFalse(POSSession.isGuest());
    }

    @Test
    public void testClearSession() {
        POSSession.setCurrentUser(1, "Admin", "Admin User");
        assertTrue(POSSession.isAdmin());

        POSSession.clear();

        assertNull(POSSession.getCurrentUserId());
        assertEquals("Guest", POSSession.getCurrentRole());
        assertNull(POSSession.getCurrentName());
        assertTrue(POSSession.isGuest());
    }

    @Test
    public void testSessionUpdate() {
        POSSession.setCurrentUser(1, "Admin", "Admin");
        assertEquals("Admin", POSSession.getCurrentRole());

        POSSession.setCurrentUser(1, "Member", "Admin as Member");
        assertEquals("Member", POSSession.getCurrentRole());
        assertEquals("Admin as Member", POSSession.getCurrentName());
    }

    @Test
    public void testRoleChecks() {
        POSSession.setCurrentUser(1, "Admin", "Admin User");
        assertTrue(POSSession.isAdmin());

        POSSession.setCurrentUser(2, "Member", "Member User");
        assertTrue(POSSession.isMember());

        POSSession.clear();
        assertTrue(POSSession.isGuest());
    }
}
