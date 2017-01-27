package tourney;
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

                float distanceToArchon = myLocation.distanceTo(archonLocation);

                if(ID > 500) {

                    int code = rc.readBroadcast(ID);
                    // code <= 0 means the action has been disabled or no action has been transmitted
                    if(code > 0 && Broadcast.isDynamicChannelCode(code)) {
                        int[] coordinates = Broadcast.readDynamicChannelCode2(code);
                        float x = Float.intBitsToFloat(rc.readBroadcast(coordinates[0]));
                        float y = Float.intBitsToFloat(rc.readBroadcast(coordinates[1]));
                        int type = coordinates[2];
                        switch (type) {
                            case 1: // (REINFORCE), don't want to waste bullets with tank, might as well have it act as a tank then
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

                Team enemy = rc.getTeam().opponent();
                Team ally  = rc.getTeam();

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

                Clock.yield();

            } catch (Exception e) {
                System.out.println("Tank: Exception");
                e.printStackTrace();
            }
        }
    }
}
