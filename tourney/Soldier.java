package tourney;
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
    //
    static Direction directionMoving = null;
    static MapLocation myLocation = null;
    public static void tryMoveSoldier(MapLocation ml, int a, int b) throws GameActionException {
        tryMove(ml, a, b);
        if(myLocation != null)
            directionMoving = myLocation.directionTo(ml);
    }
    public static void tryMoveSoldier(Direction dir, int a, int b) throws GameActionException {
        tryMove(dir, a, b);
        directionMoving = dir;
    }
    public static void tryMoveSoldier(Direction dir) throws GameActionException {
        tryMove(dir);
        directionMoving = dir;
    }

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

                myLocation = rc.getLocation();

                MapLocation archonLocation = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(Broadcast.MAIN_ARCHON_POSITION[0])), 
                		Float.intBitsToFloat(rc.readBroadcast(Broadcast.MAIN_ARCHON_POSITION[1])));
                float distanceToArchon = myLocation.distanceTo(archonLocation);
                if(Broadcast.checkMainArchonDistress() || dying) {
                    if(distanceToArchon > 15) {
                        tryMoveSoldier(archonLocation, 2, 45);
                    }
                }

                TreeInfo[] neutralTrees = rc.senseNearbyTrees(INTERACT_RADIUS, Team.NEUTRAL);
				int i = 0;
                for (; i<neutralTrees.length; i++) {
                	Broadcast.requestLumberjack(neutralTrees[i]);
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation()))
                        rc.shake(neutralTrees[i].getLocation()); // Collect free bullets from neutral trees
                }
				boolean shoot_tree = i > 0;
                
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
                                    tryMoveSoldier(archonLocation, 2, 45);
                                else {
                                    System.out.println("Responding to reinforcement request at: " + x_f +  " " + y_f);
                                    MapLocation requestedLocation = new MapLocation(x_f, y_f);

                                    if(myLocation.distanceTo(requestedLocation) < 4) {
                                        rc.broadcast(ID, code*-1);
                                        break;
                                    }

                                    tryMoveSoldier(requestedLocation, 2, 45);
                                }
                                break;
                        }
                    }
                }
                
                BulletInfo[] bullets = rc.senseNearbyBullets(SENSE_RADIUS);
                if (bullets.length > 0) 
                	tryDodge(bullets[bullets.length/2]);

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(SENSE_RADIUS, enemy);
                RobotInfo[] friendlies = rc.senseNearbyRobots(SENSE_RADIUS, ally);
                
                // If there are some...
                if (robots.length > 0) {
                	// run away from robots so we dont take 5 bullets to the face
                	if (robots[0].location.distanceTo(myLocation) < 5) {
                		// rotate slightly to the left to avoid any incoming fire
                		tryMoveSoldier(robots[0].location.directionTo(myLocation).rotateLeftDegrees(20), 2, 45);
                	}
                	
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
                                tryMoveSoldier(towardsEn.opposite(), 2, 45);
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMoveSoldier(towardsEn, 2, 45);
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
                                if(myLocation.directionTo(friendly.location).degreesBetween(towardsEn) < 40) {
                                    shoot = false;
                                    break;
                                }
                            }
                            if(shoot && towardsEn != null) {
                                rc.fireSingleShot(towardsEn);
                                tryMoveSoldier(towardsEn.opposite(), 2, 45);
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMoveSoldier(towardsEn, 2, 45);
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
                                tryMoveSoldier(towardsEn.opposite());
                                break;
                            }
                        }
                        if(!shoot && towardsEn != null) {
                            tryMoveSoldier(towardsEn);
                        }
                    }
                }

                if(Direct.retreat())
                    tryMoveSoldier(myLocation.directionTo(archonLocation));
                else {
                    if(!Broadcast.anyReinforcementsRequests()) {
                        if(distanceToArchon < 30) {
                            int len = RobotPlayer.enemyArchonLocations.length;
                            if(len > 0) {
                                int index = (int)(Math.random() * len);
                                tryMoveSoldier(myLocation.directionTo(RobotPlayer.enemyArchonLocations[index]));
                            }
                        } else {
                            tryMoveSoldier(myLocation.directionTo(archonLocation));
                        }
                    }
                }
                if(shoot_tree && directionMoving != null && rc.canFireSingleShot()) {
                    if(myLocation.directionTo(neutralTrees[0].location).degreesBetween(directionMoving) < 25) {
                        rc.fireSingleShot(directionMoving);
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
