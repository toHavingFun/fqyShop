package com.fqyshop.service.impl;

import com.fqyshop.common.Const;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.common.TokenCache;
import com.fqyshop.dao.CategoryMapper;
import com.fqyshop.dao.UserMapper;
import com.fqyshop.pojo.Category;
import com.fqyshop.pojo.User;
import com.fqyshop.service.ICategoryService;
import com.fqyshop.service.IUserService;
import com.fqyshop.util.MD5Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2019/1/16.
 */
@Service("iCategoryService")
public class CategoryServiceimpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    private Logger logger = LoggerFactory.getLogger(CategoryServiceimpl.class);

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.responseErrMsg("添加分类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(parentId);
        category.setStatus(true);
        int insert = categoryMapper.insert(category);
        if (insert > 0) {
            return ServerResponse.responseMsg("添加分类成功");
        }
        return ServerResponse.responseErrMsg("添加分类失败");
    }

    public ServerResponse updateCategoryname(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.responseErrMsg("更新分类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);
        int i = categoryMapper.updateByPrimaryKeySelective(category);
        if (i > 0) {
            ServerResponse.responseMsg("更新名字成功");
        }
        return ServerResponse.responseErrMsg("更新名字失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.getCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到该类的子分类");
        }
        return ServerResponse.responseData(categoryList);
    }
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        ArrayList<Integer> categoryIdLists = Lists.newArrayList();
        if (categoryId!=null){
            for (Category c: categorySet){
                categoryIdLists.add(c.getId());
            }
        }
        return ServerResponse.responseData(categoryIdLists);
    }

    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        List<Category> categoryList = categoryMapper.getCategoryChildrenByParentId(categoryId);
        for (Category c:categoryList){
            findChildCategory(categorySet,c.getId());
        }
        return categorySet;
    }
}
