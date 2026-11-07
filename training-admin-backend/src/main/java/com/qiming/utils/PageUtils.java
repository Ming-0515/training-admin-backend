package com.qiming.utils;

/**
 * 分页参数兜底与上限保护。
 * <p>
 * 分页参数完全来自客户端, 使用:
 * {@code ?pageSize=1000000} 会一次性把整张表(以及关联表)查出来, 是很容易触发的拖库手段。
 * 这里统一做归一化, 所有分页查询都必须经过它。
 */
public final class PageUtils {

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;
    /**
     * 默认每页条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /**
     * 每页条数上限(前端可选的最大值是 100, 保持一致)
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 防止实例化
     */
    private PageUtils() {
    }

    /**
     * 页码归一化: 为空或小于 1 时按第 1 页处理
     */
    public static int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        } else {
            return page;
        }
    }

    /**
     * 每页条数归一化: 为空或小于 1 时取默认值, 超过上限时截断到上限
     */
    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        } else {
            return pageSize > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageSize;
        }
    }

}
