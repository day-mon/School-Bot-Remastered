package schoolbot.natives.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatter {


    public static Date formatClassTime(String input) throws ParseException { // sorry :( - Ryan
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy hh:mm");
        int at = input.indexOf("@");
        String[] splitAt = input.split("@");
        if (input.charAt(at - 1) != ' ') {
            input = splitAt[0] + " @" + splitAt[1];
        }
        if (input.charAt(at + 1) != ' ') {
            input = splitAt[0] + "@ " + splitAt[1];
        }
        String time = input.split(" ")[2].toLowerCase(); // issues
        boolean am = time.contains("am");
        int mins = Integer.parseInt(time.contains(":") ? time.split(":")[1].replace("am", "").replace("pm", "") : "0");
        int hour = Integer.parseInt(time.contains(":") ? time.split(":")[0] : time.replace("am", "").replace("pm", ""))
                + (am ? 0 : 12);
        hour = (hour == 12 && am ? 0 : hour);

        //String d = Ryan.today.getMonth().getValue() + "/" + Ryan.today.getDayOfMonth() + "/" + Ryan.today.getYear()
          //      + " " + hour + ":" + (mins == 0 ? "00" : mins);

        return null;
    }

    public static String formatMatrix(double[][] a) {
        String s = "```";
        for (int i = 0; i < a.length; i++) {
            s += "[";
            for (int j = 0; j < a[0].length; j++) {
                s += a[i][j] + " ";
            }
            s = s.substring(0, s.length() - 1) + "]" + (i != a.length - 1 ? "\n" : "");
        }
        s += "```";
        return s;
    }


}
