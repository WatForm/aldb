package state;

import java.util.ArrayList;
import java.util.List;

/**
 * StatePath represents the path of nodes visited in the active model.
 */
public class StatePath {
    private List<StateNode> path;
    int tempPathSize;
    private StateNode prev;
    private int position;

    public StatePath() {
        path = new ArrayList<>();
        position = 0;
        tempPathSize = 0;
    }

    private void appendPath(List<StateNode> path) {
        this.path.addAll(path);
        position = this.path.size() - 1;
    }

    public void initWithPath(List<StateNode> path) {
        clearPath();
        appendPath(path);
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public boolean atEnd() {
        return position == path.size() - 1;
    }

    public StateNode getNode(int pos) {
        if (pos < 0 || pos >= path.size()) {
            return null;
        }
        return path.get(pos);
    }

    public StateNode getCurNode() {
        return path.isEmpty() ? null : path.get(position);
    }

    public StateNode getPrevNode() {
        return prev;
    }

    public void setPosition(int position) {
        prev = getCurNode();
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void incrementPosition(int steps) {
        setPosition(position + steps);
    }

    public void decrementPosition(int steps, boolean traceMode) {
        int newPos = position < steps ? 0 : position - steps;
        setPosition(newPos);
        if (position == 0) {
            prev = null;
        } else {
            prev = path.get(position - 1);
        }
        if (traceMode) {
            return;
        }
        path = path.subList(0, newPos + 1);
    }

    public void commitNodes() {
        tempPathSize = 0;
    }

    public void setTempPath(List<StateNode> tempPath) {
        clearTempPath();
        tempPathSize = tempPath.size();
        appendPath(tempPath);
    }

    public void clearTempPath() {
        position -= tempPathSize;
        for (int i = 0; i < tempPathSize; i++) {
            path.remove(path.size() - 1);
        }

        tempPathSize = 0;
    }

    public void clearPath() {
        path.clear();
        tempPathSize = 0;
    }

    public String getHistory(int n, boolean traceMode) {
        int i = position - 1;
        int j = i;
        StringBuilder sb = new StringBuilder();
        while (j >= 0 && i - j < n) {
            if (traceMode && j != 0) {
                sb.insert(0, path.get(j).getDiffString(path.get(j - 1)));
            } else {
                sb.insert(0, path.get(j).toString());
            }
            sb.insert(0, String.format("\n(-%d)\n----", i - j + 1));
            j--;
        }
        return sb.toString();
    }
}
