package com.ywy.core.base;

import com.ywy.core.vo.PageResultVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Basic Service interface
 */
public interface BaseService<T extends BaseEntity, P extends BaseParam> {
    /**
     * Get object by id
     */
    T getById(String id);

    /**
     * Query collection by parameter
     */
    List<T> findListByParam(@Param("param") P p);

    /**
     * Query collection by paging by parameter
     */
    PageResultVO<T> findListByPage(@Param("param") P p);

    /**
     * Query quantity by parameter
     */
    int findCountByParam(@Param("param") P p);

    /**
     * Add
     */
    int save(@Param("bean") T t);

    /**
     * Batch addition
     */
    int saveBatch(@Param("list") List<T> list);

    /**
     * Update by id
     */
    int updateById(@Param("bean") T t, @Param("id") String id);

    /**
     * Update by multiple conditions
     */
    int updateByParam(@Param("bean") T t, @Param("param") P p);

    /**
     * Delete by id
     */
    int deleteById(String id);

    /**
     * Delete by multiple conditions
     */
    int deleteByParam(@Param("param") P p);
}