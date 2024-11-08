package com.dfire.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogHelpUtil {

    public static String getSpecificTrace(Throwable t) {
        StringWriter stringWriter = null;
        PrintWriter writer = null;
        try {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
            t.printStackTrace(writer);
            StringBuffer buffer = stringWriter.getBuffer();
            return buffer.toString();
        } catch (Exception e) {
            try {
                e.printStackTrace();
                return e.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                return ex.toString();
            }
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                stringWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
