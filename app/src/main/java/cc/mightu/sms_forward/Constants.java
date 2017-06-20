package cc.mightu.sms_forward;

/**
 * Created by uuplusu on 20/06/2017.
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "cc.mightu.sms_forward.action.main";
        public static String STARTFOREGROUND_ACTION = "cc.mightu.sms_forward.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "cc.mightu.sms_forward.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
