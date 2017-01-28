package tourney;
import battlecode.common.*;

public class Scout extends RobotPlayer {
	/*public static double[] goToEdge() {
		Direction way = randomDirection();
		double[] count = new double[2];
		count[0] = 0.0;
		count[1] = 0.0;

		try {

			while(rc.onTheMap(rc.getLocation(), RobotType.SCOUT.sensorRadius)) {
				if(tryMove(way)) {
					count[0] += (Math.cos(way.radians) * RobotType.SCOUT.strideRadius);
					count[1] += (Math.sin(way.radians) * RobotType.SCOUT.strideRadius);
				}
			}
			System.out.println("I am at the edge");

		} catch (Exception e) {
			System.out.println("This will never happen");
		}
		return count;
	}*/
	
	/*public static double[] skirtEdge() {
		Direction way = randomDirection();
		double[] count = new double[2];
		count[0] = 0.0;
		count[1] = 0.0;

		try {
			while(rc.onTheMap(rc.getLocation())) {
				
				
				tryMove(Direction.NORTH);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.EAST);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.WEST);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			while(rc.onTheMap(rc.getLocation())) {
				tryMove(Direction.SOUTH);
				Direction enemy = findEnemies();
				if(enemy != null) {
					rc.fireSingleShot(enemy);
				}
        		count[1] += RobotType.SCOUT.strideRadius;
			}
			/*while(rc.onTheMap(rc.getLocation(), RobotType.SCOUT.sensorRadius)) {
				if(tryMove(way)) {
					count[0] += (Math.cos(way.radians) * RobotType.SCOUT.strideRadius);
					count[1] += (Math.sin(way.radians) * RobotType.SCOUT.strideRadius);
				}
			}
			System.out.println("I am at the edge");

		} catch (Exception e) {
			System.out.println("This will never happen");
		}
		return count;
	}*/
	
	public static MapLocation findEnemy() {
		RobotInfo[] robots = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadius, rc.getTeam().opponent());
        //TreeInfo[] trees = rc.senseNearbyTrees();
        if(robots.length > 0) { // && !rc.hasAttacked()) {
        	//MapLocation[] locations = new MapLocation[robots.length];
        	/*for(int i = 0; i < robots.length; i++) {
        		locations[i] = robots[i].getLocation();
        	}*/
        	//return locations;
        	return robots[0].getLocation();
        }
        return null;

	}
	
	public static MapLocation findTree() {
		TreeInfo[] trees = rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius);
        if(trees.length > 0) {
        	for(int i = 0; i < trees.length; i++) {
        		if(trees[i].getTeam() != rc.getTeam().opponent() && trees[i].getTeam() != rc.getTeam() && rc.canShake(trees[i].getID()) && trees[i].getContainedBullets() > 5) {
        			return trees[i].getLocation();
        		}
        	}
        }
        return null;
	}
	
	static boolean dying = false;
	static boolean init = true;
	//public static double range = 50.0;
	static Direction dir = Direction.EAST;
	static int ID = 0;
    public static void run(RobotController rc) {
    	
        RobotPlayer.rc = rc;
        System.out.println("Scout: Spawn");
        Team enemy = rc.getTeam().opponent();
        Team ally  = rc.getTeam();
        
        while (true) {

            try {
            	/*double minMapSize[] = new double[2];
            	if(init) {
            		minMapSize = skirtEdge();
            		init = false;
            	}*/
            	//should detect bullets and find where to move to instead; implement in dodge()
            	//double temp = range * 2;
            	dir = randomDirection();
            	//while(range > 0.0) {
        		//MapLocation attackable = findEnemy();
        		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, enemy);
        		RobotInfo[] friendlies = rc.senseNearbyRobots(rc.getType().sensorRadius, ally);
        		TreeInfo[] teamTrees = rc.senseNearbyTrees(rc.getType().sensorRadius, ally);
        		//MapLocation shakeable = findTree();
        		//if(shakeable != null) {
        			//tryMove(rc.getLocation().directionTo(shakeable));
       			 	//rc.shake(shakeable);
        		if(ID < 500 && !dying) {
                    ID = Broadcast.requestID(ID);
                }
        		
        		if (rc.getHealth() < rc.getType().maxHealth / 10 && !dying) {
                    Broadcast.decrementRobotCount(RobotType.SCOUT); // Broadcast death on low health
                    Broadcast.dying(ID);
                    ID = -ID; // render ID unusable
                    dying = true;
                }
        		
        		TreeInfo[] neutralTrees = rc.senseNearbyTrees(rc.getType().bodyRadius + rc.getType().strideRadius, Team.NEUTRAL);
                for (int i=0; i < neutralTrees.length; i++) {
                    MapLocation loc = neutralTrees[i].getLocation();
                    if (neutralTrees[i].getContainedBullets() > 0 && rc.canShake(neutralTrees[i].getLocation())) {
                        rc.shake(loc); // Collect free bullets from neutral trees
                    }
                }
                boolean shoot = true;
                Direction toEnemy = rc.getLocation().directionTo(enemies[0].getLocation());
                for(RobotInfo friendly : friendlies) {
                    if(rc.getLocation().directionTo(friendly.getLocation()).degreesBetween(toEnemy) < 10) {
                        shoot = false;
                        break;
                    }
                }
                for(TreeInfo teamTree : teamTrees) {
                    if(rc.getLocation().directionTo(teamTree.getLocation()).degreesBetween(toEnemy) < 10) {
                        shoot = false;
                        break;
                    }
                }
            	//for(int i = 0; i < enemies.length; i++) {
            		if((enemies[0].getType() == RobotType.ARCHON 
            				|| enemies[0].getType() == RobotType.GARDENER) 
            				&& rc.senseNearbyBullets(rc.getType().bulletSightRadius).length < 5 ) {
            			tryMove(rc.getLocation().directionTo(enemies[0].getLocation()));
            		} 
            		if(dying || rc.senseNearbyBullets(rc.getType().bulletSightRadius).length > 5) {
        				tryMove(rc.getLocation().directionTo(enemies[0].getLocation()).opposite());
        			}
            		if(rc.canFireSingleShot() && !rc.hasAttacked() && shoot) {
        				rc.fireSingleShot(rc.getLocation().directionTo(enemies[0].getLocation()));
        			}
            	//}
                
        		if(!rc.onTheMap(rc.getLocation())) {
        			tryMove(dir.rotateLeftDegrees((float) 90.0));
        		} else {
        			tryMove(dir);
        			//range -= RobotType.SCOUT.strideRadius;
        		}
        		Clock.yield();
            	//}
            	//range = temp;
                /*else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        tryMove(toEnemy);
                    }

                }
                /*if (trees.length > 0) {
                    for(int i = 0; i < 1; i++) {
                        Direction towardsTree = new Direction((float) Math.atan((trees[i].location.x-rc.getLocation().x)/(trees[i].location.y-rc.getLocation().y)));
                        tryMove(towardsTree);
                    }
                } else {*/
                   
               // }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                
            } catch (Exception e) {
                System.out.println("Scout: Exception");
                e.printStackTrace();
            }
        }
    }
}