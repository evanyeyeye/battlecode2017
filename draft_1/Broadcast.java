package draft_1;
import battlecode.common.*;

public class Broadcast {

    static RobotController rc;

    final public static int REINFORCEMENTS_FULFILL_TIME = 10;
    final public static int DYING = -666;

    /*
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     */

    public static int MAIN_ARCHON_POSITION[]    = {0, 1};
    public static int ARCHON_POSITIONS[]        = {0, 1, 2, 3, 4, 5};
    public static int MAIN_ARCHON_IN_DISTRESS   = 6;

    /* Indexes 7-10 Keeps track of the number of each robot type spawned */
    public static int GARDENER_COUNT_INDEX      = 7;
    public static int SOLDIER_COUNT_INDEX       = 8;
    public static int LUMBERJACK_COUNT_INDEX    = 9;
    public static int SCOUT_COUNT_INDEX         = 10;

    public static int REINFORCEMENTS_REQUESTS[] = {200, 203, 206, 209};

    // Each robot takes 3 indices: [age, x, y]
    public static int ARCHON_AVOID_ROBOTS[]     = {100, 103, 106, 109, 112, 115, 118, 121, 124, 127};

    public static int LUMBERJACK_REQUESTS[]      = {130, 134, 138, 142, 146}; // four

    public static int MAIN_ARCHON               = 99;

    // IDs between 400 and 489 are used dynamically by the Archon


    // Only 10 robots can simultaneously request IDs
    public static int ID_REQUESTS[]             = {490, 491, 492, 493, 494, 495, 496, 497, 498, 499};

    // FOR INDICES > 500:
    // -- Indices greater than 500 are used for specific robot actions
    // -- Each robot that requests an ID will constantly check their spot

    /*
     * END INDEX ALLOCATION
     * END INDEX ALLOCATION
     * END INDEX ALLOCATION
     */

    public static void initBroadcaster(RobotController r) {
        rc = r;
    }

    public static boolean requestLumberjack (TreeInfo tree) throws GameActionException {
        for (int i : LUMBERJACK_REQUESTS) {
            if (rc.readBroadcast(i) == 0) {
            	MapLocation treeLoc = tree.getLocation();
                rc.broadcast(i, rc.getRoundNum()*1000 + (int)(Math.random()*1000)); // fake ID
                rc.broadcast(i+1,
                        Float.floatToRawIntBits(treeLoc.x));
                rc.broadcast(i+2,
                        Float.floatToRawIntBits(treeLoc.y));
                rc.broadcast(i+3, Float.floatToRawIntBits(tree.getRadius()));
                System.out.println("Requesting lumberjacks at " + treeLoc.y + " " + treeLoc.x);
                return true;
            }
        }
        return false;
    }

    public static boolean checkMainArchonDistress() throws GameActionException {
        int status = rc.readBroadcast(MAIN_ARCHON_IN_DISTRESS);
        return status != 0;
    }

    public static boolean checkMainArchon() throws GameActionException {

        if(rc.readBroadcast(MAIN_ARCHON) == 0) {
            rc.broadcast(MAIN_ARCHON, 1);
            return true;
        }
        return false;

    }

    public static int genDynamicCode3(int first, int second, int third, int status) {
        return first + 1000*second + 1000*1000*third + 1000*1000*1000*status;
    }
    public static int genDynamicCode2(int first, int second, int status) {
        return first + 1000*second + 1000*1000*status;
    }

    // Returns 0 if request has been ignored
    // Returns [490, 500) if request is placed
    // Returns [500, 1000) if request succeeded
    public static int requestID(int ticket) throws GameActionException {
        if(ticket == 0) {
            for(int i : ID_REQUESTS) {
                if(rc.readBroadcast(i) == 0) {
                    rc.broadcast(i, 1);
                    // Establish ticket
                    return i;
                }
            }
            return 0;
        }
        int id = rc.readBroadcast(ticket);
        if(id >= 500) {
            // Revoke ticket
            rc.broadcast(ticket, 0);
            return id;
        }
        return ticket;
    }

    static int locationCodeStatusFactor = 1000 * 1000;

    static boolean isDynamicChannelCode(int code) {
        int acode = Math.abs(code)%locationCodeStatusFactor;
        return acode >= 400000 && acode < 490000;
    }
    static int[] readDynamicChannelCode2(int code) {
        int t = Math.abs(code)%locationCodeStatusFactor;
        int[] deconst = {t%1000, t/1000, code/locationCodeStatusFactor};
        return deconst;
    }

    public static void broadcastLocation(int index, float x, float y) throws GameActionException {
        int x_i = Float.floatToRawIntBits(x);
        int y_i = Float.floatToRawIntBits(y);

        // Set age to 1
        rc.broadcast(index, 1);
        rc.broadcast(index+1, x_i);
        rc.broadcast(index+2, y_i);
    }

    // Returns true if broadcast succeeded
    // Returns false if all channels are occupied
    public static boolean requestReinforcements(MapLocation ml) throws GameActionException {

        System.out.println("Requesting reinforcements at: " + ml.x + " " + ml.y);

        int age;
        int max_age = 0;
        int max_index = 0;
        for(int i : REINFORCEMENTS_REQUESTS) {
            age = rc.readBroadcast(i);
            if(age == 0) {
                broadcastLocation(i, ml.x, ml.y);
                return true;
            }
            if(age > max_age) {
                max_age = age;
                max_index = i;
            }
        }

        // If all other requests are still being fulfilled,
        // it is a waste of time to constantly switch to new ones
        if(max_age <= REINFORCEMENTS_FULFILL_TIME) return false;
        broadcastLocation(max_index, ml.x, ml.y);
        return true;
    }

    public static void alertArchon(MapLocation ml) throws GameActionException {

        System.out.println("Alerting archon of belligerent at: " + ml.x + " " + ml.y);

        int age;
        int max_age = Integer.MAX_VALUE;
        int max_index = 0;
        for(int i : ARCHON_AVOID_ROBOTS) {
            age = rc.readBroadcast(i);
            if(age == 0) {
                broadcastLocation(i, ml.x, ml.y);
                return;
            }
            if(age > max_age) {
                max_age = age;
                max_index = i;
            }
        }
        broadcastLocation(max_index, ml.x, ml.y);

    }

    public static boolean anyReinforcementsRequests() throws GameActionException {
        for(int i : REINFORCEMENTS_REQUESTS) {
            if(rc.readBroadcast(i) > 0) return true;
        }
        return false;
    }

    public static float[] getArchonLocation() throws GameActionException {
        float coordinates[] = {
            Float.intBitsToFloat(rc.readBroadcast(0)),
            Float.intBitsToFloat(rc.readBroadcast(1))
        };
        return coordinates;
    }

    public static void dying(int ID) throws GameActionException {

        if(ID >= 500 && ID < 1000)
            rc.broadcast(ID, DYING);

    }

    /*
     * Add one to robot count
     * @param type RobotType being incremented
     */
    public static void incrementRobotCount(RobotType type) throws GameActionException {

        int index = 0;
        switch (type) {
            case GARDENER:
                index = GARDENER_COUNT_INDEX;
                break;
            case SOLDIER:
                index = SOLDIER_COUNT_INDEX;
                break;
            case LUMBERJACK:
                index = LUMBERJACK_COUNT_INDEX;
                break;
            case SCOUT:
                index = SCOUT_COUNT_INDEX;
                break;
        }

        rc.broadcast(index, rc.readBroadcast(index) + 1);
    }

    /*
     * Subtract one from robot count
     * @param type RobotType being decremented
     */
    public static void decrementRobotCount(RobotType type) throws GameActionException {

        int index = 0;
        switch (type) {
            case GARDENER:
                index = GARDENER_COUNT_INDEX;
                break;
            case SOLDIER:
                index = SOLDIER_COUNT_INDEX;
                break;
            case LUMBERJACK:
                index = LUMBERJACK_COUNT_INDEX;
                break;
            case SCOUT:
                index = SCOUT_COUNT_INDEX;
                break;
        }

        rc.broadcast(index, rc.readBroadcast(index) - 1);
    }

    /*
     * Get robot count
     * @param type RobotType needed
     * @return count of RobotType in Team
     */
    public static int getRobotCount(RobotType type) throws GameActionException {

        int index = 0;
        switch (type) {
            case GARDENER:
                index = GARDENER_COUNT_INDEX;
                break;
            case SOLDIER:
                index = SOLDIER_COUNT_INDEX;
                break;
            case LUMBERJACK:
                index = LUMBERJACK_COUNT_INDEX;
                break;
            case SCOUT:
                index = SCOUT_COUNT_INDEX;
                break;
        }

        return rc.readBroadcast(index);
    }
}
