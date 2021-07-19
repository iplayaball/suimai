package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.CategoryDao;
import com.study.suimai.product.entity.CategoryEntity;
import com.study.suimai.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        List<CategoryEntity> categoryTree = categoryEntityList.stream(
        ).filter(
                categoryEntity -> categoryEntity.getParentCid() == 0
        ).map(
                menu1 -> {
                    menu1.setChildren(getChildren(menu1,categoryEntityList));
                    return menu1;
                }
        ).sorted(
                Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());
        return categoryTree;
    }

    private List<CategoryEntity> getChildren(CategoryEntity menu1, List<CategoryEntity> categoryEntityList) {
        List<CategoryEntity> categoryChildrenTree = categoryEntityList.stream(
        ).filter(
                categoryEntity -> categoryEntity.getParentCid() == menu1.getCatId()
        ).map(
                menu -> {
                    menu.setChildren(getChildren(menu,categoryEntityList));
                    return menu;
                }
        ).sorted(
                Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());
        return categoryChildrenTree;
    }

    /**
     * 根据三级分类ID 获取完整的父分类数组
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        ArrayList<Long> path = new ArrayList<>();

        findParentId(catelogId, path);

        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    private void findParentId(Long catelogId, ArrayList<Long> path) {
        path.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        Long parentCid = categoryEntity.getParentCid();
        if (parentCid !=0) {
            findParentId(parentCid, path);
        }
    }
}