package mah.ui.support.swing.pane.item;

import mah.common.util.CollectionUtils;
import mah.ui.UIException;
import mah.ui.pane.item.FullItem;
import mah.ui.pane.item.ItemListPane;
import mah.ui.pane.item.ItemPane;
import mah.ui.pane.Text;
import mah.ui.support.swing.pane.SwingPane;
import mah.ui.support.swing.theme.LayoutThemeImpl;
import mah.ui.support.swing.util.StringUtils;
import mah.ui.support.swing.util.SwingUtils;
import mah.ui.theme.LayoutTheme;
import mah.ui.theme.Themeable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @see ItemListPane
 * @see ItemPane
 * Created by zgq on 2017-01-08 14:16
 */
public final class FullItemPane implements ItemPane<FullItem>, SwingPane, Themeable {

    private JPanel panel;
    private JPanel container;
    private JLabel iconLabel;
    private JPanel middlePanel;
    private JTextPane content;
    private JTextPane description;
    private JLabel numLabel;
    private FullItem item;
    private LayoutThemeImpl theme;
    private static final int ITEM_HEIGHT = 58;
    private static final int TEXT_LEN = 60;

    public FullItemPane(FullItem fullItem,int num) {
        init(fullItem,num);
    }

    private void init(FullItem item,int num) {
        try {
            this.panel = new JPanel();
            this.panel.setLayout(new BorderLayout());
            this.panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
            this.container = SwingUtils.createPanelWithXBoxLayout();
            this.container.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 5));
            this.middlePanel = SwingUtils.createPanelWithYBoxLayout();
            this.middlePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            iconLabel = new JLabel();
            this.container.add(iconLabel);
            this.container.add(middlePanel);
            this.panel.add(container);
            this.panel.setPreferredSize(new Dimension(580, ITEM_HEIGHT));
            initContent();
            initDescription();
            initNum();
            reset(item,num);
        } catch (Exception e) {
            throw new UIException(e);
        }
    }

    private void initNum() {
        this.numLabel = new JLabel("text");
        this.container.add(this.numLabel);
    }

    private void initDescription() {
        this.description = new JTextPane();
        this.description.setEditable(false);
        this.middlePanel.add(this.description);
    }

    private void initContent() {
        this.content = new JTextPane();
        this.content.setEditable(false);
        this.middlePanel.add(this.content);
    }


    private void setIcon(FullItem item) throws IOException {
        InputStream iconInputStream = item.getIconInputStream();
        if (iconInputStream == null) {
            iconLabel.setIcon(null);
            return;
        }
        BufferedImage icon = ImageIO.read(iconInputStream);
        ImageIcon imageIcon = new ImageIcon(icon); // load the image to a imageIcon
        Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(ITEM_HEIGHT, ITEM_HEIGHT, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        imageIcon = new ImageIcon(newimg);  // transform it back
        iconLabel.setIcon(imageIcon);
    }

    private void setDescription(FullItem item) {
        Text description = item.getDescription();
        String descriptionStr="";
        if (description != null) {
            descriptionStr = item.getDescription().getText();
        }
        setNormalDescription(descriptionStr);
    }

    private void setNormalContent() {
        setNormalContent(item.getContent().getText());
    }

    private void setNormalContent(String text) {
        String qualifyStr = StringUtils.getStrBySpecificLength(text, TEXT_LEN);
        this.content.setDocument(createNormalDocument(qualifyStr));
    }

    private void setNormalDescription() {
        setNormalDescription(item.getDescription().getText());
    }

    private void setNormalDescription(String text) {
        String qualifyStr = StringUtils.getStrBySpecificLength(text, TEXT_LEN);
        this.description.setDocument(createNormalDocument(qualifyStr));
    }

    private StyledDocument createNormalDocument(String text) {
        StyledDocument document = new DefaultStyledDocument();
        try {
            document.insertString(0, text, normalTextStyle);
            return document;
        } catch (BadLocationException e) {
            throw new UIException(e);
        }
    }

    private void setContent(FullItem item) {
        String contentStr = item.getContent().getText();
        String text = StringUtils.getStrBySpecificLength(contentStr, TEXT_LEN);
        setNormalContent(text);
    }

    @Override
    public FullItem getItem() {
        return item;
    }

    private void applyBackground(Color color) {
        this.content.setBackground(color);
        this.description.setBackground(color);
        this.iconLabel.setBackground(color);
        this.container.setBackground(color);
        this.middlePanel.setBackground(color);
    }

    @Override
    public void pending() {
        String property = theme.findProperty("pending-background-color");
        Color color = Color.decode(property);
        applyBackground(color);
        Text content = item.getContent();
        java.util.List<Integer> highlightIndexs = content.getHighlightIndexs();
        if (CollectionUtils.isNotEmpty(highlightIndexs)) {
            this.content.setDocument(highlightText(highlightIndexs, content));
        }
        Text description = item.getDescription();
        if (description != null) {
            java.util.List<Integer> descriptionHighlightIndexs = description.getHighlightIndexs();
            if (CollectionUtils.isNotEmpty(descriptionHighlightIndexs)) {
                this.description.setDocument(highlightText(descriptionHighlightIndexs, description));
            }
        }
    }

    private DefaultStyledDocument highlightText(java.util.List<Integer> highlightIndexs, Text textObj) {
        String text = textObj.getText();
        String con = StringUtils.getStrBySpecificLength(text, TEXT_LEN);
        DefaultStyledDocument hightlightDocument = HightlightHelper.createHightlightDocument(con, highlightIndexs, matchedTextStyle, normalTextStyle);
        return hightlightDocument;
    }

    private Style matchedTextStyle;
    private Style normalTextStyle;

    private void initStyle(LayoutThemeImpl theme) {

        Color highlightForegroundColor = Color.decode(theme.findProperty("text-highlight-foreground-color"));
        Color highlightBackgroundColor = Color.decode(theme.findProperty("text-highlight-background-color"));
        matchedTextStyle = description.addStyle("matchedText", null);
        StyleConstants.setForeground(matchedTextStyle, highlightForegroundColor);
        StyleConstants.setBackground(matchedTextStyle, highlightBackgroundColor);
        StyleConstants.setFontSize(matchedTextStyle, 14);

        Color foregroundColor = Color.decode(theme.findProperty("text-foreground-color"));
        Color backgroundColor = Color.decode(theme.findProperty("text-background-color"));
        normalTextStyle = description.addStyle("normalText", null);
        StyleConstants.setForeground(normalTextStyle, foregroundColor);
        StyleConstants.setBackground(normalTextStyle, backgroundColor);
        StyleConstants.setFontSize(normalTextStyle, 14);
    }


    @Override
    public void reset(FullItem item, int num) {
        try {
            setIcon(item);
            setContent(item);
            setDescription(item);
            setNum(num);
            this.item = item;
        } catch (Exception e) {
            throw new UIException(e);
        }
    }


    @Override
    public void unpending() {
        Color color = Color.decode(theme.findProperty("background-color"));
        applyBackground(color);
        setNormalContent();
        setNormalDescription();
    }

    private void setNum(int num) {
        this.numLabel.setText(String.valueOf(num));
    }


    @Override
    public JPanel getPanel() {
        return panel;
    }


    @Override
    public void apply(LayoutTheme theme) {
        if (theme instanceof LayoutThemeImpl) {
            LayoutThemeImpl layoutTheme = (LayoutThemeImpl) theme;
            decorateContent(layoutTheme);
            decorateMiddleContainer(layoutTheme);
            decorateDescription(layoutTheme);
            decorateNum(layoutTheme);
            decoratePane(layoutTheme);
            initStyle(layoutTheme);
            this.theme = layoutTheme;
        }
    }

    private void decorateMiddleContainer(LayoutThemeImpl layoutTheme) {
        String numFontColor = layoutTheme.findProperty("middle-container-background-color");
        this.middlePanel.setBackground(Color.decode(numFontColor));
    }

    private void decorateNum(LayoutThemeImpl layoutTheme) {
        String numFontColor = layoutTheme.findProperty("num-font-color");
        numLabel.setForeground(Color.decode(numFontColor));
    }

    private void decoratePane(LayoutThemeImpl layoutTheme) {
        String itemBackgroundColor = layoutTheme.findProperty("item-pane-background-color");
        this.container.setBackground(Color.decode(itemBackgroundColor));
        this.panel.setBackground(Color.decode(itemBackgroundColor));
    }

    private void decorateDescription(LayoutThemeImpl layoutTheme) {
        String descriptionColor = layoutTheme.findProperty("text-foreground-color");
        this.description.setForeground(Color.decode(descriptionColor));
        String descriptionBackgroundColor = layoutTheme.findProperty("text-background-color");
        this.description.setBackground(Color.decode(descriptionBackgroundColor));
    }

    private void decorateContent(LayoutThemeImpl layoutTheme) {
        String contentColor = layoutTheme.findProperty("text-foreground-color");
        String contentBackgroundColor = layoutTheme.findProperty("text-background-color");
        this.content.setForeground(Color.decode(contentColor));
        this.content.setBackground(Color.decode(contentBackgroundColor));
    }
}
