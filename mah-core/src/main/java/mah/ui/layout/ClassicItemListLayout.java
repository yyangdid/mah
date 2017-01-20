package mah.ui.layout;

import mah.action.ActionHandler;
import mah.ui.pane.input.InputPaneProvider;
import mah.ui.pane.item.Item;
import mah.ui.pane.item.ItemList;

import java.util.List;

/**
 * Created by zgq on 2017-01-08 13:51
 */
public interface ClassicItemListLayout extends ItemList,Layout,InputPaneProvider,ActionHandler{

    void updateItems(List<? extends Item> items);

    void updateItems(Item... items);

    void clear();

    void refresh();
}
