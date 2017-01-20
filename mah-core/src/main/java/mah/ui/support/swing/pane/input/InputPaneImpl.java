package mah.ui.support.swing.pane.input;

import mah.ui.UIException;
import mah.ui.event.EventHandler;
import mah.ui.input.InputPaneFactoryBean;
import mah.ui.input.InputTextChangedEvent;
import mah.ui.input.TextState;
import mah.ui.pane.input.InputPane;
import mah.ui.pane.input.InputPaneSupport;
import mah.ui.support.swing.pane.SwingPane;
import mah.ui.support.swing.theme.LayoutThemeImpl;
import mah.ui.support.swing.util.StringUtils;
import mah.ui.theme.LayoutTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.text.AttributedCharacterIterator;

/**
 * Created by zgq on 2017-01-08 12:00
 */
public class InputPaneImpl extends InputPaneSupport implements InputPane, SwingPane {

    private JTextComponent input;
    private JPanel panel;
    private final int panelPrefWidth;
    private final int panelPrefHeight;
    private final int maxNumberOfCharacters;

    private InputPaneImpl() {
        this(600, 70, 40);
    }

    private InputPaneImpl(int panelPrefWidth, int panelPrefHeight, int maxNumberOfCharacters) {
        this.panelPrefWidth = panelPrefWidth;
        this.panelPrefHeight = panelPrefHeight;
        this.maxNumberOfCharacters = maxNumberOfCharacters;
    }

    public void init() {
        this.panel = new JPanel();
        this.panel.setLayout(new GridBagLayout());
        initInput();
    }

    public JTextComponent getInput() {
        return input;
    }

    private void initInput() {
        JTextPane input = new JTextPane();
        int inputWidth = (int) (panelPrefWidth * 0.9);
        int inputHeight = (int) (panelPrefHeight * 0.8);
        input.setPreferredSize(new Dimension((inputWidth), inputHeight));
        input.setBorder(BorderFactory.createCompoundBorder(
                input.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        input.setFont(new Font(null, Font.PLAIN, 25));

        limitInputCharacterNum(input);
        listenInputMethod(input);
        listenInput(input);
        disposeKeybinds(input);

        this.input = input;
        this.panel.setPreferredSize(new Dimension(panelPrefWidth, panelPrefHeight));
        this.panel.add(input);
    }

    private void listenInput(final JTextPane input) {
        input.getDocument().

                addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        try {
                            fireTextChangedEvent(e.getDocument());
                        } catch (BadLocationException e1) {
                            throw new UIException(e1);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        try {
                            fireTextChangedEvent(e.getDocument());
                        } catch (BadLocationException e1) {
                            throw new UIException(e1);
                        }
                    }

                    private void fireTextChangedEvent(Document document) throws BadLocationException {
                        TextState oldState = new TextState.Builder(document.getText(0, document.getLength()),
                                input.getCaretPosition()).build();
                        SwingUtilities.invokeLater(() -> {
                            try {
                                TextState newState = new TextState.Builder(document.getText(0,
                                        document.getLength()), input.getCaretPosition()).build();
                                InputTextChangedEvent inputTextChangedEvent = new InputTextChangedEvent(newState,
                                        oldState);
                                for (EventHandler eventHandler : getInputTextChangedHandlers()) {
                                    eventHandler.handle(inputTextChangedEvent);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                });
    }

    private void limitInputCharacterNum(JTextPane input) {
        input.setStyledDocument(new DefaultStyledDocument() {

                                    @Override
                                    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                                        String text = getText(0, getLength()) + str;
                                        int length = StringUtils.getLength(text);
                                        if (length <= maxNumberOfCharacters)

                                        {
                                            super.insertString(offs, str, a);
                                        } else

                                        {
                                            Toolkit.getDefaultToolkit().beep();
                                        }

                                    }

                                }

        );
    }

    private void listenInputMethod(final JTextPane input) {
        input.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                AttributedCharacterIterator text = event.getText();
                int committedCharacterCount = event.getCommittedCharacterCount();
                char c = text.first();
                StringBuilder textBuffer = new StringBuilder();
                boolean allLetter = true;
                while (committedCharacterCount-- > 0) {
                    textBuffer.append(c);
                    c = text.next();
                    if (!Character.isLetter(c)) {
                        allLetter = false;
                    }
                }
                if (event.getCommittedCharacterCount() > 0) {
                    event.consume();
                    if (allLetter) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        int caretPosition = input.getCaretPosition();
                        try {
                            input.getDocument().insertString(caretPosition, textBuffer.toString(), null);
                        } catch (BadLocationException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
            }

        });
    }

    private void disposeKeybinds(JTextComponent input) {
        InputMap inputMap = input.getInputMap();
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK, false);
        inputMap.put(keystroke, "none");
        KeyStroke keystroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_MASK, false);
        inputMap.put(keystroke2, "none");
        KeyStroke keystroke3 = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, false);
        inputMap.put(keystroke3, "none");
        KeyStroke keystroke4 = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK, false);
        inputMap.put(keystroke4, "none");
        KeyStroke keystroke5 = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        inputMap.put(keystroke5, "none");
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void apply(LayoutTheme theme) {
        if (theme instanceof LayoutThemeImpl) {
            LayoutThemeImpl layoutTheme = (LayoutThemeImpl) theme;
            String panelBackgroundColor = layoutTheme.findProperty("background-color");
            panel.setBackground(Color.decode(panelBackgroundColor));
            String inputFieldBackgroundColor = layoutTheme.findProperty("input-field-background-color");
            input.setBackground(Color.decode(inputFieldBackgroundColor));
            String fontColor = layoutTheme.findProperty("input-field-font-color");
            input.setForeground(Color.decode(fontColor));
            String cursorColor = layoutTheme.findProperty("input-cursor-color");
            input.setCaretColor(Color.decode(cursorColor));
        }
    }

    public static InputPaneImpl newInstance() {
        InputPaneImpl inputPane = new InputPaneImpl();
        InputPaneFactoryBean.getInstance().initBean(inputPane);
        inputPane.init();
        return inputPane;
    }

    @Override
    public void setText(String text) {
        input.setText(text);
    }

    @Override
    public void setCaretPosition(int position) {
        input.setCaretPosition(position);
    }

    @Override
    public String getText() {
        return input.getText();
    }


    @Override
    public int getCaretPosition() {
        return input.getCaretPosition();
    }


    public void requireFocus() {
        input.requestFocus();
    }

    @Override
    protected void remove(int off, int len) {
        try {
            input.getDocument().remove(off, len);
        } catch (BadLocationException e) {
            throw new UIException("Remove failed,off " + off + ",len " + len + "", e);
        }
    }
}
