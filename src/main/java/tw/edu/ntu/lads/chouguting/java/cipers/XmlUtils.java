package tw.edu.ntu.lads.chouguting.java.cipers;

public class XmlUtils {
    public static boolean labelExists(String xml, String label) {
        return xml.contains("<" + label + ">") && xml.contains("</" + label + ">");
    }

    public static String getLabelValue(String xml, String label) {
        int start = xml.indexOf("<" + label + ">") + label.length() + 2;
        int end = xml.indexOf("</" + label + ">");
        return xml.substring(start, end);
    }
}
