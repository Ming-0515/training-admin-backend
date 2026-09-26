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
        List<Menu> all = menuMapper.listAll();
        Map<Integer, Menu> byId = new LinkedHashMap<>();
        for (Menu menu : all) {
            byId.put(menu.getId(), menu);
        }
        List<Menu> roots = new ArrayList<>();
        for (Menu menu : all) {
            Integer parentId = menu.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(menu);
                continue;
            }
            Menu parent = byId.get(parentId);
            if (parent == null) {
                // 父节点不存在(脏数据)时按顶级处理, 避免整个菜单树丢失
                roots.add(menu);
                continue;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(menu);
        }
        sortRecursively(roots);
        return roots;
    }

    private void sortRecursively(List<Menu> menus) {
        menus.sort(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Menu::getId));
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
