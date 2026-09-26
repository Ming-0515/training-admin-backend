package com.qiming.pojo;

import com.qiming.utils.PageUtils;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EmployeeQueryParam {

    String name;                            //姓名
    Integer gender;                         //性别
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate begin;                        //入职日期(区间开始)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate end;                          //入职日期(区间结束, 含当天)
    Integer page = 1;                       //当前页码
    Integer pageSize = 10;                  //每页显示条数

    /**
     * 分页参数兜底 + 上限保护。
     * 分页参数来自客户端, 直接交给 PageHelper 会被 ?pageSize=1000000 拖垮数据库。
     * 这里覆盖 Lombok 生成的 getter, 服务层拿到的已经是安全值。
     */
    public Integer getPage() {
        return PageUtils.normalizePage(page);
    }

    public Integer getPageSize() {
        return PageUtils.normalizePageSize(pageSize);
    }

}
