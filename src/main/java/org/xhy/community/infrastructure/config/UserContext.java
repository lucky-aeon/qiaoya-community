package org.xhy.community.infrastructure.config;

public class UserContext {
    
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();
    
    public static void setCurrentUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }
    
    public static String getCurrentUserId() {
        String userId = USER_ID_HOLDER.get();
        if (userId == null) {
            throw new IllegalStateException("当前用户ID未设置，请检查用户认证状态");
        }
        return userId;
    }
    
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
    
    public static boolean hasCurrentUser() {
        return USER_ID_HOLDER.get() != null;
    }
}