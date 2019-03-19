package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.Category;

import java.util.List;

/**
 * Created by Administrator on 2019/1/20.
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryname(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
