package draft_1;
import battlecode.common.*;

public class Tank extends RobotPlayer {

    static final int MAX_HP = 200;

    public static void run(RobotController rc) {
        RobotPlayer.rc = rc;
        initDirList();
    }
}
