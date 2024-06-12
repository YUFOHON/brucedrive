package com.ywy.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanUtil {
    public static <T, S> T copy(S s, Class<T> clazz) {
        T t = null;
        try {
            t = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }

    public static  <T, S> List<T> copyList(List<S> sList, Class<T> clazz) {
        List<T> tList = new ArrayList<>();
        for (S s : sList) {
            T t = copy(s, clazz);
            tList.add(t);
        }
        return tList;
    }
}
