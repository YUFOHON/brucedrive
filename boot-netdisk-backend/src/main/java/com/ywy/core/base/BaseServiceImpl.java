package com.ywy.core.base;

import com.ywy.core.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseServiceImpl<T extends BaseEntity, P extends BaseParam> implements BaseService<T, P> {

    @Autowired
    protected BaseMapper<T, P> baseMapper;


    @Override
    public T getById(String id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<T> findListByParam(P p) {
        return baseMapper.selectList(p);
    }

    @Override
    public PageResultVO<T> findListByPage(P p) {
        int count = baseMapper.selectCount(p);
        List<T> list = baseMapper.selectList(p);
        return new PageResultVO(count, p.getPageSize(), p.getPageNo(), list);
    }

    @Override
    public int findCountByParam(P p) {
       return baseMapper.selectCount(p);
    }

    @Override
    public int save(T t) {
        return baseMapper.insert(t);
    }

    @Override
    public int saveBatch(List<T> list) {
        return baseMapper.insertBatch(list);
    }

    @Override
    public int updateById(T t, String id) {
        return baseMapper.updateById(t, id);
    }

    @Override
    public int updateByParam(T t, P p) {
        return baseMapper.updateByParam(t, p);
    }

    @Override
    public int deleteById(String id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public int deleteByParam(P p) {
        return baseMapper.deleteByParam(p);
    }
}
