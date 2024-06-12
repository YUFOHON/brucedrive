package com.ywy.core.base;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *Basic mapper interface
 */
public interface BaseMapper<T, P> {
    /**
     * Get the object based on id
     */
    T selectById(String id);

    /**
     * Query the collection based on parameters
     */
    List<T> selectList(@Param("param") P p);

    /**
     *Query quantity based on parameters
     */
    int selectCount(@Param("param") P p);

    /**
     * insert
     */
    int insert(@Param("bean") T t);

    /**
     * Batch insert
     */
    int insertBatch(@Param("list") List<T> list);

    /**
     * Update based on id
     */
    int updateById(@Param("bean") T t, @Param("id") String id);

    /**
     *Multiple condition updates
     */
    int updateByParam(@Param("bean") T t, @Param("param") P p);

    /**
     * Delete based on id
     */
    int deleteById(String id);

    /**
     *Multi-condition deletion
     */
    int deleteByParam(@Param("param") P p);
}