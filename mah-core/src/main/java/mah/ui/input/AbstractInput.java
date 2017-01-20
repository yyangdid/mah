package mah.ui.input;

import java.util.LinkedList;

/**
 * Created by zgq on 2017-01-09 14:03
 */
public abstract class AbstractInput implements Input{

    private final LinkedList<TextState> undoStack = new LinkedList<>();
    private final LinkedList<TextState> redoStack = new LinkedList<>();
    private boolean fireEvent=true;
    private TextState textState;


    protected boolean canBackward() {
        return getCaretPosition() > 0;
    }

    private void push() {
        pushToUndo(newTextState(getText(),getCaretPosition()));
    }

    private void pushToUndo(TextState textState) {
        if (undoStack.size() > 100) {
            undoStack.pollLast();
        }
        undoStack.push(textState);
    }


    protected boolean canFoward() {
        return getCaretPosition() < getText().length();
    }

    @Override
    public void beginningOfLine() {
        setCaretPosition(0);
    }

    @Override
    public void endOfLine() {
        setCaretPosition(getText().length());
    }

    @Override
    public void forwardChar() {
        if (getCaretPosition() < getText().length()) {
            setCaretPosition(getCaretPosition() + 1);
        }
    }

    public void backwardChar() {
        if (getCaretPosition() > 0) {
            setCaretPosition(getCaretPosition() - 1);
        }
    }

    public void deleteChar() {
        String text = getText();
        int caretPosition = getCaretPosition();
        if (caretPosition >= text.length()) {
            return;
        }
        push();
        remove(caretPosition,1);
    }

    protected abstract void remove(int off, int len);


    public void forwardWord() {
        if (!canFoward()) {
            return;
        }
        int caretPosition = forwardByWord();
        setCaretPosition(caretPosition);
    }

    protected int forwardByWord() {
        String text = getText();
        int caretPosition = getCaretPosition();
        boolean hasLetter = false;
        for (int i = caretPosition; i < text.length(); i++) {
            char c = text.charAt(i);
            if (hasLetter) {
                if (!isWordLetter(c)) {
                    break;
                }
            } else {
                if (isWordLetter(c)) {
                    hasLetter = true;
                }
            }
            caretPosition++;
        }
        return caretPosition;
    }

    protected boolean isWordLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    @Override
    public void backwardWord() {
        if (!canBackward()) {
            return;
        }
        int caretPosition = backwardWord2();
        setCaretPosition(caretPosition);
    }

    protected int backwardWord2() {
        String text = getText();
        int caretPosition = getCaretPosition();
        int caret = caretPosition;
        boolean hasLetter = false;
        for (int i = caret - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == ' ' && i != caret - 1) {
                break;
            }

            if (hasLetter) {
                if (!isWordLetter(c)) {
                    break;
                }
            } else {
                if (isWordLetter(c)) {
                    hasLetter = true;
                }
            }
            caretPosition--;
        }
        return caretPosition;
    }


    public void killWord() {
        if (!canFoward()) {
            return;
        }
        int origin = getCaretPosition();
        int forward = forwardByWord();
        if (forward > origin) {
            remove(origin,forward-origin);
            push();
        }
    }

    @Override
    public void killWholeLine() {
        String text = getText();
        if (text != null && text.length() > 0) {
            push();
            remove(0, text.length());
        }
    }

    @Override
    public void killLine() {
        int caret = getCaretPosition();
        String text = getText();
        if (caret < text.length()) {
            push();
            remove(caret,text.length()-caret);
        }
    }

    @Override
    public void backwardKillWord() {
        if (!canBackward()) {
            return;
        }
        int origin = getCaretPosition();
        int backwardWord = backwardWord2();
        if (backwardWord == origin) {
            return;
        }
        push();
        remove(backwardWord,origin-backwardWord);
    }

    @Override
    public void backwardDeleteChar() {
        if (!canBackward()) {
            return;
        }
        push();
        int caret = getCaretPosition();
        remove(caret-1,1);
        setCaretPosition(caret-1);
    }

    @Override
    public final void undo() {
        TextState textState = undoStack.pollFirst();
        if (textState != null) {
            pushToRedo();
            setText(textState.getText());
            setCaretPosition(textState.getPosition());
        }
    }


    public void setTextState(TextState textState) {
        this.textState = textState;
        setText(textState.getText());
        setCaretPosition(textState.getPosition());
    }

    public TextState getTextState() {
        return textState;
    }

    private TextState newTextState(String text, int position) {
        return new TextState.Builder(text, position).build();
    }

    private void pushToRedo() {
        pushToRedo(newTextState(getText(), getCaretPosition()));
    }

    private void pushToRedo(TextState textState) {
        if (redoStack.size() > 100) {
            redoStack.pollLast();
        }
        redoStack.push(textState);
    }

    @Override
    public final void redo() {
        TextState textState = redoStack.pollFirst();
        if (textState != null) {
            setText(textState.getText());
            setCaretPosition(textState.getPosition());
            pushToUndo(textState);
        }
    }


    @Override
    public void setTextStateQuietly(TextState textState) {
        fireEvent = false;
        setTextState(textState);
        fireEvent = true;
    }

    @Override
    public void clear() {
        String text = getText();
        if (text != null && !text.isEmpty()) {
            remove(0,text.length());
        }
    }
}

