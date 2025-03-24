package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

import java.lang.NullPointerException;

import java.util.ArrayDeque;
import java.util.Queue;

import ca.mcmaster.se2aa4.island.team023.Heading.HeadingStates;

public class DroneActions {

    private Drone drone;
    private Queue<JSONObject> actionQueue = new ArrayDeque<>();
    private Queue<Point<Integer>> nextPosition = new ArrayDeque<>();

    private boolean echoLeft = false;
    private boolean echoForward = false;
    private boolean echoRight = false;
    private boolean scan = false;
    private boolean leftTurn = false;
    private boolean rightTurn = false;
    private boolean flyForward = true;


    public DroneActions(Drone drone) {
        if (drone == null) throw new NullPointerException();
        this.drone = drone;
    }

    public JSONObject nextAction(){
        drone.setRelativePos(nextPosition.remove());
        if (!actionQueue.isEmpty()) return actionQueue.remove();

        // queue up actions based on flags. Always done in order: echo -> scan -> turns -> forward
        if (echoLeft) actionQueue.add(radar(drone.getHeading().getHeadingState().prev()));
        if (echoForward) actionQueue.add(radar(drone.getHeading().getHeadingState()));
        if (echoRight) actionQueue.add(radar(drone.getHeading().getHeadingState().next()));
        if (scan) actionQueue.add(scan());
        if (leftTurn) actionQueue.add(turnLeft());
        if (rightTurn) actionQueue.add(turnRight());
        if (flyForward) actionQueue.add(forward());

        return actionQueue.remove();
    }

    public void clearQueue() {
        nextPosition.clear();
        actionQueue.clear();
    }

    public void disableMovement() {
        echoLeft = false;
        echoForward = false;
        echoRight = false;
        scan = false;
        leftTurn = false;
        rightTurn = false;
        flyForward = false;
    }

    public void setDefaultEcho(boolean left, boolean ahead, boolean right) {
        echoLeft = left;
        echoForward = ahead;
        echoRight =  right;
    }

    public void setDefaultScan(boolean enable) {
        scan = enable;
    }

    public void setDefaultMovement(boolean leftTurn, boolean flyForward, boolean turnRight) {
        this.leftTurn = leftTurn;
        this.flyForward = flyForward;
        this.rightTurn = turnRight;
    }

    public void queueStop() {
        clearQueue();
        actionQueue.add(stop());
    }

    public void addLeftTurn() {
        clearQueue();
        actionQueue.add(turnLeft());
    }

    public void addRightTurn() {
        clearQueue();
        actionQueue.add(turnRight());
    }

    public void addDoubleLeft() {
        clearQueue();
        actionQueue.add(turnLeft());
        actionQueue.add(turnLeft());
    }
    
    public void addDoubleRight() {
        clearQueue();
        actionQueue.add(turnRight());
        actionQueue.add(turnRight());
    }
    
    public void addLongDoubleLeft() {
        clearQueue();
        actionQueue.add(turnLeft());
        actionQueue.add(forward());
        actionQueue.add(turnLeft());
    }
    
    public void addLongDoubleRight() {
        clearQueue();
        actionQueue.add(turnRight());
        actionQueue.add(forward());
        actionQueue.add(turnRight());
    }

    // Action to fly forward
    private JSONObject forward() {
        JSONObject action = new JSONObject();
        action.put("action", "fly");

        if (nextPosition.isEmpty()) {
            nextPosition.add(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        } else {
            nextPosition.add(Point.addInts(nextPosition.peek(), drone.getHeading().getHeadingState().getNextPoint()));
        }
        return action;
    }

    private JSONObject turnLeft() {
        
        // update next position and rotate the drone heading
        if (nextPosition.isEmpty()) {
            nextPosition.add(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        } else {
            nextPosition.add(Point.addInts(nextPosition.peek(), drone.getHeading().getHeadingState().getNextPoint()));
        }
        drone.getHeading().turnCounterClockwise();
        nextPosition.add(Point.addInts(nextPosition.remove(), drone.getHeading().getHeadingState().getNextPoint()));

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", drone.getHeading().getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    private JSONObject turnRight() {

        // update next position and rotate the drone heading
        if (nextPosition.isEmpty()) {
            nextPosition.add(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        } else {
            nextPosition.add(Point.addInts(nextPosition.peek(), drone.getHeading().getHeadingState().getNextPoint()));
        }
        drone.getHeading().turnClockwise();
        nextPosition.add(Point.addInts(nextPosition.remove(), drone.getHeading().getHeadingState().getNextPoint()));

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", drone.getHeading().getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    private JSONObject radar(HeadingStates dir) {

        // no net change in position
        if (nextPosition.isEmpty()) {
            nextPosition.add(drone.getRelativePos());
        } else {
            nextPosition.add(nextPosition.peek());
        }

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "echo");
        direction.put("direction", dir.toString());
        action.put("parameters", direction);
        return action;
    }

    private JSONObject scan() {
        
        // no net change in position
        if (nextPosition.isEmpty()) {
            nextPosition.add(drone.getRelativePos());
        } else {
            nextPosition.add(nextPosition.peek());
        }

        JSONObject action = new JSONObject();
        action.put("action", "scan");
        return action;
    }

    private JSONObject stop() {
        
        // no net change in position
        if (nextPosition.isEmpty()) {
            nextPosition.add(drone.getRelativePos());
        } else {
            nextPosition.add(nextPosition.peek());
        }

        JSONObject action = new JSONObject();
        action.put("action", "stop");
        return action;
    }
}
