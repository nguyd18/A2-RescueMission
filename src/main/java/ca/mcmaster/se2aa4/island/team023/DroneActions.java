package ca.mcmaster.se2aa4.island.team023;

import org.json.JSONObject;

import java.lang.NullPointerException;

import java.util.ArrayDeque;
import java.util.Queue;

import ca.mcmaster.se2aa4.island.team023.Heading.HeadingStates;

public class DroneActions {

    Drone drone;
    Queue<JSONObject> actionQueue = new ArrayDeque<>();

    private boolean echoLeft = false;
    private boolean echoForward = false;
    private boolean echoRight = false;
    private boolean scan = false;
    private boolean leftTurn = false;
    private boolean rightTurn = false;
    private boolean flyForward = false;


    public DroneActions(Drone drone) {
        if (drone == null) throw new NullPointerException();
        this.drone = drone;
    }

    public JSONObject nextAction(){
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

    public void clear() {
        actionQueue.clear();
    }

    public void enableEcho(boolean left, boolean ahead, boolean right) {
        echoLeft = left;
        echoForward = ahead;
        echoRight =  right;
    }

    public void setScan(boolean enable) {
        scan = enable;
    }

    public void setMovement(boolean leftTurn, boolean flyForward, boolean turnRight) {
        this.leftTurn = leftTurn;
        this.flyForward = flyForward;
        this.rightTurn = turnRight;
    }

    public void queueStop() {
        clear();
        actionQueue.add(stop());
    }

    // Action to fly forward
    private JSONObject forward() {
        JSONObject action = new JSONObject();
        action.put("action", "fly");
        drone.setRelativePos(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        return action;
    }

    private JSONObject turnLeft() {
        // TODO encapsulate this
        drone.setRelativePos(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        drone.getHeading().turnCounterClockwise();
        drone.setRelativePos(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", drone.getHeading().getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    private JSONObject turnRight() {

        // TODO encapsulate this
        drone.setRelativePos(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));
        drone.getHeading().turnClockwise();
        drone.setRelativePos(Point.addInts(drone.getRelativePos(), drone.getHeading().getHeadingState().getNextPoint()));

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "heading");
        direction.put("direction", drone.getHeading().getHeadingState().toString());
        action.put("parameters", direction);

        return action;
    }

    private JSONObject radar(HeadingStates dir) {

        // assert dir != heading.getHeadingState().next().next() : "Invalid echo. Drone tried to send radar backwards";

        JSONObject action = new JSONObject();
        JSONObject direction = new JSONObject();
        action.put("action", "echo");
        direction.put("direction", dir.toString());
        action.put("parameters", direction);
        return action;
    }

    private JSONObject scan() {
        JSONObject action = new JSONObject();
        action.put("action", "scan");
        return action;
    }

    private JSONObject stop() {
        JSONObject action = new JSONObject();
        action.put("action", "stop");
        return action;
    }
}
