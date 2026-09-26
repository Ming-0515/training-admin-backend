package com.qiming.utils;

/**
 * 当前登录用户上下文。
 * <p>
 * 由 TokenInterceptor 在鉴权通过后写入、请求结束后清理,
 * 供 PermissionInterceptor 与业务代码(如"查看我的菜单")读取当前登录人。
 * <p>
 * 使用 ThreadLocal, 务必在请求结束(含鉴权失败)时调用 {@link #clear()} 清理,
 * 否则线程池复用会导致上一个请求的用户信息泄漏到下一个请求。
 */
public final class UserContext {

    private static final ThreadLocal<Integer> EMPLOYEE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> NAME = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Integer employeeId, String username, String name) {
        EMPLOYEE_ID.set(employeeId);
        USERNAME.set(username);
        NAME.set(name);
    }

    public static Integer getEmployeeId() {
        return EMPLOYEE_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static String getName() {
        return NAME.get();
    }

    public static void clear() {
        EMPLOYEE_ID.remove();
        USERNAME.remove();
        NAME.remove();
    }

}
