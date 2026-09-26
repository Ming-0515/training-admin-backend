package com.qiming.mapper;

import com.qiming.pojo.EmployeeExperience;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmployeeExperienceMapper {

    /**
     * 批量添加员工工作经历
     */
    void addBatch(List<EmployeeExperience> experienceList);

    /**
     * 批量删除员工工作经历
     */
    void deleteBatch(List<Integer> employeeIds);
}
