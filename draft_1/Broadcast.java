package draft_1;
import battlecode.common.*;

public class Broadcast {

    static RobotController rc;

    public static int REINFORCEMENTS_FULFILL_TIME = 20;

    /*
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     * BEGIN INDEX ALLOCATION
     */

    public static int MAP_DIMENSIONS[]          = {0, 1};
    public static int REINFORCEMENTS_REQUESTS[] = {200, 203, 206, 209};

    // Each robot takes 3 indices: [age, x, y]
    public static int ARCHON_AVOID_ROBOTS[]     = {100, 103, 106, 109, 112, 115, 118, 121, 124, 127};

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

    public static boolean checkMainArchon() throws GameActionException {

        if(rc.readBroadcast(MAIN_ARCHON) == 0) {
            rc.broadcast(MAIN_ARCHON, 1);
            return true;
        }
        return false;

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

    /*
     * Location codes: dynamically allocated codes
     *  -- negative indicates move away from
     */
    static boolean isLocationCode(int code) {
        int acode = Math.abs(code)%10000000;
        return acode >= 400000 && acode < 490000;
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

}
