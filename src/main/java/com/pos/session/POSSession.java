package com.pos.session;

public class POSSession {
    private static Integer currentUserId = null;   // members.id or admins.id
    private static String currentRole = "Guest";   // "Admin", "Member", "Guest"
    private static String currentName = null;

    public static void setCurrentUser(Integer userId, String role, String name) {
        currentUserId = userId;
        currentRole = role;
        currentName = name;
    }

    public static void clear() {
        currentUserId = null;
        currentRole = "Guest";
        currentName = null;
    }

    public static Integer getCurrentUserId() { return currentUserId; }
    public static String getCurrentRole() { return currentRole; }
    public static String getCurrentName() { return currentName; }

    public static boolean isAdmin() { return "Admin".equals(currentRole); }
    public static boolean isMember() { return "Member".equals(currentRole); }
    public static boolean isGuest() { return "Guest".equals(currentRole); }
}