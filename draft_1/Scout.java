package draft_1;
import battlecode.common.*;

public class Scout extends RobotPlayer {

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        initDirList();
    }
}
