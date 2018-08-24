package io.tabletoptools.hawthorne.services;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class HomebrewItemServiceTest {

    @Test
    public void load() {
        HomebrewItemService.instance().load();
        Collection items = HomebrewItemService.instance().getItems();
        assert(items.size() > 0);
    }
}