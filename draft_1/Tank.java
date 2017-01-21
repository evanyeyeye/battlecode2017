package draft_1;
import battlecode.common.*;

public class Tank extends RobotPlayer {
	
	public static int ID = 0;
	
	public static boolean dying = false;
	
	public static void run(RobotController rc) { // if we get a tank from a tree
		
        RobotPlayer.rc = rc;

        System.out.println("Tank: Spawn");

        while (true) {

            try {
            	
                if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
            		Broadcast.dying(ID);
            		ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
                }
                
                if(ID < 500 && !dying)
                    ID = Broadcast.requestID(ID);
                
                MapLocation myLocation = rc.getLocation();
                MapLocation archonLocation = new MapLocation(
                		(rc.readBroadcast(Broadcast.MAIN_ARCHON_POSITION[0])), Float.intBitsToFloat(rc.readBroadcast(Broadcast.MAIN_ARCHON_POSITION[1])));
                
                if(ID > 500) {

                    int code = rc.readBroadcast(ID);
                    // code <= 0 means the action has been disabled or no action has been transmitted
                    if(code > 0 && Broadcast.isDynamicChannelCode(code)) {
                        int[] coordinates = Broadcast.readDynamicChannelCode2(code);
                        float x = Float.intBitsToFloat(rc.readBroadcast(coordinates[0]));
                        float y = Float.intBitsToFloat(rc.readBroadcast(coordinates[1]));
                        int type = coordinates[2];
                        switch (type) {
                            case 1: // (REINFORCE), don't want to waste bullets with tank, might as well have it act as a tank than
                                if (Direct.retreat())
                                    tryMove(myLocation.directionTo(archonLocation));
                                else {
                                    System.out.println("Responding to reinforcement request at: " + x +  " " + y);
                                    MapLocation requestedLocation = new MapLocation(x, y);

                                    if(myLocation.distanceTo(requestedLocation) < 4) {
                                        rc.broadcast(ID, code * -1);
                                        break;
                                    }
                                    
                                    tryMove(myLocation.directionTo(requestedLocation));
                                }
                                break;
                        }
                    }
                }
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Tank: Exception");
                e.printStackTrace();
            }
        }
	}
}