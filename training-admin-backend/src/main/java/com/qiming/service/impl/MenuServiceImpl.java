package com.qiming.service.impl;

import com.qiming.mapper.MenuMapper;
import com.qiming.pojo.Menu;
import com.qiming.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> listAll() {
        return menuMapper.listAll();
    }

    /**
     * 把扁平列表组装成树。排序沿用 SQL 的 parent_id, sort, id。
     */
    @Override
    public List<Menu> listAllTree() {
        // 所有菜单
        List<Menu> all = menuMapper.listAll();
        // 按照id建立map，便于后续查找
        Map<Integer, Menu> byId = new LinkedHashMap<>();
        for (Menu menu : all) {
            byId.put(menu.getId(), menu);
        }
        // 用于保存根节点
        List<Menu> roots = new ArrayList<>();
        for (Menu menu : all) {
            Integer parentId = menu.getParentId();
            // 为根节点
            if (parentId == null || parentId == 0) {
                roots.add(menu);
                continue;
            }
            Menu parent = byId.get(parentId);
            // 父节点不存在(脏数据)
            if (parent == null) {
                roots.add(menu);
                continue;
            }
            // 将子节点挂到父节点上
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(menu);
        }
        sortRecursively(roots);
        return roots;
    }

    private void sortRecursively(List<Menu> menus) {
        // 对树进行排序
        menus.sort(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Menu::getId));
        // 对每棵树的结点进行排序，递归实现
        for (Menu menu : menus) {
            if (menu.getChildren() != null) {
                sortRecursively(menu.getChildren());
            }
        }
    }

    @Override
    public Set<String> listPermissionCodes(Integer employeeId) {
        List<String> codes = menuMapper.listPermissionCodesByEmployeeId(employeeId);
        // 用 LinkedHashSet 保留 SQL 中的排序, 让登录返回的权限列表顺序稳定
        return codes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(codes);
    }

    @Override
    public List<Menu> listMenusByEmployeeId(Integer employeeId) {
        return menuMapper.listMenusByEmployeeId(employeeId);
    }

}
