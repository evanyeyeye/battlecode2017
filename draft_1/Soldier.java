package draft_1;
import battlecode.common.*;

public class Soldier extends RobotPlayer {

    static boolean dying = false;

    static int ID = 0;

    /*
     * SOLDIER SPECIFIC CODES
     */

    final public static int REINFORCE = 1;
    final static int RETURN_TO_ARCHON = 1001; // not implemented

    /*
     * END SOLDIER SPECIFIC CODES
     */

    // 0 == Need ID
    // [490,499] == ID request processing
    // [500,999] == ID established

    public static void run(RobotController rc) throws GameActionException {
        
    	RobotPlayer.rc = rc;
    	System.out.println("Soldier: Spawn");
    	
        Team enemy = rc.getTeam().opponent();
        Team ally  = rc.getTeam();

        while (true) {

            try {

                if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                	Broadcast.decrementRobotCount(RobotType.GARDENER); // Broadcast death on low health
            		Broadcast.dying(ID);
            		ID = -ID; // render ID unusable
                    dying = true; // code will not enter this if statement again
                }
                
                if(ID < 500 && !dying) {
                    ID = Broadcast.requestID(ID);
                }

                
                float archon_x = Float.intBitsToFloat(rc.readBroadcast(
                            Broadcast.MAIN_ARCHON_POSITION[0]));
                float archon_y = Float.intBitsToFloat(rc.readBroadcast(
                            Broadcast.MAIN_ARCHON_POSITION[1]));

                MapLocation myLocation = rc.getLocation();

                MapLocation archonLocation = new MapLocation(archon_x, archon_y);
                float distanceToArchon = myLocation.distanceTo(archonLocation);
                if(Broadcast.checkMainArchonDistress() || dying) {
                    if(distanceToArchon > 15) {
                        tryMove(myLocation.directionTo(archonLocation));
                    }
                }
                
                TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL); 
            	for (int i=0; i<neutralTrees.length; i++)
            		if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
            			rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees
                
                if(ID > 500) {

                    int code = rc.readBroadcast(ID);
                    // code <= 0 means the action has been disabled
                    // or no action has been transmitted
                    if(code > 0 && Broadcast.isDynamicChannelCode(code)) {
                        int[] coordinates = Broadcast.readDynamicChannelCode2(code);
                        int x = rc.readBroadcast(coordinates[0]);
                        int y = rc.readBroadcast(coordinates[1]);
                        float x_f = Float.intBitsToFloat(x);
                        float y_f = Float.intBitsToFloat(y);
                        int type = coordinates[2];
                        switch (type) {
                            case REINFORCE:
                                if(Direct.retreat())
                                    tryMove(myLocation.directionTo(archonLocation));
                                else {
                                    System.out.println("Responding to reinforcement request at: " + x_f +  " " + y_f);
                                    MapLocation requestedLocation = new MapLocation(x_f, y_f);

                                    if(myLocation.distanceTo(requestedLocation) < 4) {
                                        rc.broadcast(ID, code*-1);
                                        break;
                                    }
                                    
                                    tryMove(myLocation.directionTo(requestedLocation));
                                }
                                break;
                        }
                    }
                }

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().sensorRadius, enemy);
                RobotInfo[] friendlies = rc.senseNearbyRobots(rc.getType().sensorRadius, ally);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    Broadcast.requestReinforcements(myLocation);
                    if(distanceToArchon < 25) {
                        Broadcast.alertArchon(myLocation);
                    }
                    if (Direct.retreat() && rc.canFirePentadShot()) {
                    	boolean shoot = true;
                        Direction towardsEn = null;
                        for(RobotInfo en : robots) {
                            towardsEn = myLocation.directionTo(en.location);
                            if(myLocation.distanceTo(en.location) < 4) {
                                rc.firePentadShot(towardsEn);
                                break;
                            }
                            for(RobotInfo friendly : friendlies) {
                                if(myLocation.directionTo(friendly.location).degreesBetween(towardsEn) < 50) {
                                    shoot = false;
                                    break;
                                }
                            }
                            if(shoot && towardsEn != null) {
                                rc.firePentadShot(towardsEn);
                                tryMove(towardsEn.opposite());
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMove(towardsEn);
                        }
                    }
                   	else if (friendlies.length > robots.length * 2 && rc.canFireSingleShot()) {
                   		boolean shoot = true;
                        Direction towardsEn = null;
                        for(RobotInfo en : robots) {
                            towardsEn = myLocation.directionTo(en.location);
                            if(myLocation.distanceTo(en.location) < 4) {
                                rc.fireSingleShot(towardsEn);
                                break;
                            }
                            for(RobotInfo friendly : friendlies) {
                                if(myLocation.directionTo(friendly.location).degreesBetween(towardsEn) < 50) {
                                    shoot = false;
                                    break;
                                }
                            }
                            if(shoot && towardsEn != null) {
                                rc.fireSingleShot(towardsEn);
                                tryMove(towardsEn.opposite());
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMove(towardsEn);
                        }
                   	}
                    else if (rc.canFireTriadShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        boolean shoot = true;
                        Direction towardsEn = null;
                        for(RobotInfo en : robots) {
                            towardsEn = myLocation.directionTo(en.location);
                            if(myLocation.distanceTo(en.location) < 4) {
                                rc.fireTriadShot(towardsEn);
                                break;
                            }
                            for(RobotInfo friendly : friendlies) {
                                if(myLocation.directionTo(friendly.location).degreesBetween(towardsEn) < 50) {
                                    shoot = false;
                                    break;
                                }
                            }
                            if(shoot && towardsEn != null) {
                                rc.fireTriadShot(towardsEn);
                                tryMove(towardsEn.opposite());
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMove(towardsEn);
                        }
                    }
                }

                if(Direct.retreat())
                    tryMove(myLocation.directionTo(archonLocation));
                else {
                    if(!Broadcast.anyReinforcementsRequests()) {
                        if(distanceToArchon < 30) {
                            int len = RobotPlayer.enemyArchonLocations.length;
                            if(len > 0) {
                                int index = (int)(Math.random() * len);
                                tryMove(myLocation.directionTo(RobotPlayer.enemyArchonLocations[index]));
                            }
                        } else {
                            tryMove(myLocation.directionTo(archonLocation));
                        }
                    }
                }

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier: Exception");
                e.printStackTrace();
            }
        }
    }
}
