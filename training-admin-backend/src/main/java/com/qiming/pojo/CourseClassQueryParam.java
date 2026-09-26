package com.qiming.pojo;

import com.qiming.utils.PageUtils;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class CourseClassQueryParam {

    String name;                            //班级名称(模糊匹配)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate begin;                        //结课时间(区间开始)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate end;                          //结课时间(区间结束, 含当天)
    Integer page = 1;                       //当前页码
    Integer pageSize = 10;                  //每页显示条数

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
