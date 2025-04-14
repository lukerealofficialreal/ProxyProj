//import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class SlidingWindow<T> {

    //TODO: Might also be a good idea instead to remember the maximum ever currPos

    public enum AckState {
        NOT_SENT, SENT, ACKED;
    }

    private final ArrayList<T> data; //The data over which the window will slide
    private ArrayList<AckState> dataState; //The state of the data

    private int windowSize;
    private int leftBound = 0; //left bound of the window (inclusive)
    private int rightBound; //right bound of the window (exclusive)
    private int currPos = 0; //The current 0-based index within the window
    private int savedPos = 0;

    public SlidingWindow(int size, int windowSize) {
        this.data = new ArrayList<>(size);
        this.dataState = new ArrayList<>(Collections.nCopies(size, AckState.NOT_SENT));

        this.windowSize = windowSize;
        this.rightBound = min(windowSize, size);
    }
    public SlidingWindow(T[] arr, int windowSize) {
        this.data = new ArrayList<>(Arrays.asList(arr));
        this.dataState = new ArrayList<>(Collections.nCopies(data.size(), AckState.NOT_SENT));
        this.windowSize = windowSize;
        this.rightBound = min(windowSize, data.size());
    }
    public SlidingWindow(int windowSize) {
        this.data = new ArrayList<>();
        this.dataState = new ArrayList<>();
        this.windowSize = windowSize;
        this.rightBound = 0;
    }

    //Put an item at the end of the data
    @SafeVarargs
    public final void put(T... items) {
        data.addAll(Arrays.asList(items)); //add the items
        dataState.addAll(Collections.nCopies(items.length, AckState.NOT_SENT));

        //If current size of the window was less than windowSize due to not enough elements in data, expand the boundaries
        if(rightBound - leftBound <= windowSize) {
            rightBound += min(items.length, (windowSize - rightBound - leftBound));
        }
    }

    //returns the number of consecutive items starting from the item at leftBound which have the state ACKED
    public int getNumAcknowledged() {
        int i = 0;
        for(i = leftBound; i < rightBound; i++) {
            if(dataState.get(i) != AckState.ACKED) {
                return i;
            }
        }
        return i;
    }
    public int getWindowSize() {
        return windowSize;
    }
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
    public T getItem() {
        return data.get(currPos);
    }
    public AckState getState() {
        return dataState.get(currPos);
    }
    public void setState(AckState state) {
        dataState.set(currPos, state);
    }
    public void savePos() {
        this.savedPos = this.currPos;
    }


    //Moves currPos forward by amount if it is within bounds. Else, moves currPos to rightBound-1 and
    //returns the remainder
    public int advancePosition(int amount) {
        assert amount >= 0;
        int move = min(amount, (rightBound)-currPos);
        currPos += move;
        return amount - move;
    }

    //moves to the next position if it is within bounds
    //If the next position is out of bounds, return false, make no move
    public boolean nextPosition() {
        //If currPos is at the end of the window,
        //Move the window
        return advancePosition(1) == 0;


        /*
        if(currPos == rightBound-1) {
            pushBound();
        }
        //If currPos is still not at the end of the window, move it right
        if(currPos != rightBound-1) {
            currPos++;
        }
         */
    }
    //Sets the absolute position of currPos within the data if the given position is also within the window boundaries.
    //Returns true if successful, false otherwise
    public boolean setAbsolutePosition(int pos) {
        if(leftBound <= pos && pos < rightBound)
        {
            currPos = pos;
            return true;
        }
        /*else {
            currPos = leftBound;
        }*/
        return false;
    }
    //Sets the relative position of currPos within the window from the left boundary if the given position is within the window boundaries.
    //Returns true if successful, false otherwise
    public boolean setRelativePosition(int pos) {
        if(0 <= pos && pos < rightBound - leftBound)
        {
            currPos = pos - leftBound;
            return true;
        }
        return false;
    }

    public int getCurrPos() {
        return currPos;
    }
    public int getLeftBound() {
        return leftBound;
    }
    public int getRightBound() {
        return rightBound;
    }

    //Resets the currPos to leftBound
    public void resetPosition() {
        setAbsolutePosition(leftBound);
    }
    //Sets currPos to the rightMost element of the window
    public void toRightBound() {
        currPos = rightBound-1;
    }
    //gets the distance from currPos to the next element in the window with the given state
    //Returns -1 if no such state exists to the right of currPos
    public int findState(AckState state) {
        for(int i = currPos+1; i < rightBound; i++) {
            if(dataState.get(i) == state) {
                return i;
            }
        }
        return -1;
    }
    public int findFirstState(AckState state) {
        int tempPos = currPos;
        currPos = leftBound-1;
        int index = findState(state);
        currPos = tempPos;
        return index;
    }

    //Push the left and right boundaries by amount.
    //
    //amount must be positive
    //amount # of packets from the left bound must have ack state ACKNOWLEDGED
    //sets currPos to the new LeftBoundary position
    public void pushBound(int amount) {
        assert amount >= 0;
        //Determine if the boundaries can be pushed
        //If they can, increment both by amount.
        //Else, move left bound by amount, or until it equals right bound
        if(rightBound + amount <= data.size()) {
            rightBound += amount;
        } else {
            rightBound = data.size();
        }
        leftBound += min(amount, rightBound - leftBound);
        currPos = leftBound;
    }
    //Increment bounds by 1
    private void pushBound() {
        pushBound(1);
    }

    //Push the left and right boundaries by amount.
    //
    //amount must be positive
    //amount # of packets from the left bound must have ack state ACKNOWLEDGED
    //sets currPos to the new LeftBoundary position
    public void pushBoundTo(int dest) {
        assert dest >= leftBound;
        //Determine if the boundaries can be pushed
        //If they can, increment both by amount.
        //Else, move left bound by amount, or until it equals right bound
        if(dest+windowSize <= data.size()) {
            rightBound = dest+windowSize;
        } else {
            rightBound = data.size();
        }
        leftBound = min(dest, rightBound);
        currPos = leftBound;
    }

    public boolean isReadOnly() {
        return false;
    }
    //returns true if the next amount packets have state ACKNOWLEDGED
    public boolean isNumAcknowledged(int amount) {
        for(int i = leftBound; i <= leftBound + amount; i++) {
            if(dataState.get(i) != AckState.ACKED) {
                return false;
            }
        }
        return true;
    }

    //Returns true if there is data accessible within the window
    public boolean isDataRemaining() {
        return leftBound != rightBound;
    }
    //Returns true if currPos is at the edge of the window
    public boolean isEndOfWindow() {
        return rightBound == currPos;
    }

    //Resets this instance for a fresh traversal. Clears all previous state
    //keeps all data from the previous run
    public void reset() {
        currPos = 0;
        leftBound = 0;
        rightBound = min(windowSize, data.size());
        dataState = new ArrayList<>(Collections.nCopies(data.size(), AckState.NOT_SENT));
        savedPos = 0;
    }

}
