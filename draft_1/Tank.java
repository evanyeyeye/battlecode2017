package draft_1;
import battlecode.common.*;

public class Tank extends RobotPlayer {

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        initDirList();
    }
}
