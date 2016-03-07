package ua.com.fielden.platform.web.menu.item.impl;

import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.custom_view.AbstractCustomView;
import ua.com.fielden.platform.web.menu.IModuleMenuConfigWithDone;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig0;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig1;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig2;
import ua.com.fielden.platform.web.menu.item.IModuleMenuConfig3;
import ua.com.fielden.platform.web.menu.module.impl.ModuleMenuConfig;
import ua.com.fielden.platform.web.menu.module.impl.WebMenuItem;
import ua.com.fielden.platform.web.menu.module.impl.WebView;

public class MenuItemConfig implements IModuleMenuConfig0, IModuleMenuConfig1, IModuleMenuConfig2 {

    private final WebMenuItem menuItem;
    private final ModuleMenuConfig moduleMenuConfig;

    public MenuItemConfig(final WebMenuItem menuItem, final ModuleMenuConfig moduleMenuConfig) {
        this.menuItem = menuItem;
        this.moduleMenuConfig = moduleMenuConfig;
    }

    @Override
    public IModuleMenuConfig1 description(final String desc) {
        menuItem.description(desc);
        return this;
    }

    @Override
    public IModuleMenuConfig2 centre(final EntityCentre<?> centre) {
        menuItem.view(new WebView(centre));
        return this;
    }

    @Override
    public IModuleMenuConfig2 view(final AbstractCustomView view) {
        menuItem.view(new WebView(view));
        return this;
    }

    @Override
    public IModuleMenuConfigWithDone done() {
        return moduleMenuConfig;
    }

    @Override
    public IModuleMenuConfig3 addSubMenuItem(final String title) {
        return new SubMenuItemConfig(menuItem.addSubMenuItem(title), this);
    }
}