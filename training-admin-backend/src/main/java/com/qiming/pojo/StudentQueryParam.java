package com.qiming.pojo;

import com.qiming.utils.PageUtils;
import lombok.Data;

@Data
public class StudentQueryParam {

    String name;    // 学员姓名
    Integer degree; // 学历(1:初中,2:高中,3:大专,4:本科,5:硕士,6:博士)
    Integer classId; // 班级ID
    Integer page = 1; // 页码
    Integer pageSize = 10; // 每页数量

    /**
     * 分页参数兜底 + 上限保护(覆盖 Lombok 生成的 getter)
     */
    public Integer getPage() {
        return PageUtils.normalizePage(page);
    }

    public Integer getPageSize() {
        return PageUtils.normalizePageSize(pageSize);
    }

}
