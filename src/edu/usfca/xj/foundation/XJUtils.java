/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package edu.usfca.xj.foundation;

import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XJUtils {

    public static String concatPath(String a, String b, String separator) {
        if(a.endsWith(separator) && b.startsWith(separator)) {
            return a+b.substring(1);
        } else if(a.endsWith(separator)) {
            return a+b;
        } else if(b.startsWith(separator)) {
            return a+b;
        }
        return a+separator+b;
    }

    public static String concatPath(String a, String b) {
        return concatPath(a, b, File.separator);
    }

    public static String getLastPathComponent(String path) {
        if(path == null)
            return null;

        int index = path.lastIndexOf(File.separator);
        if(index == -1)
            return path;
        else
            return path.substring(index+1, path.length());
    }

    public static String getPathByDeletingPathExtension(String path) {
        if(path == null)
            return null;

        int index = path.lastIndexOf(".");
        if(index == -1)
            return path;

        return path.substring(0, index);
    }

    public static String getPathByDeletingLastComponent(String path, String separator) {
        if(path == null)
            return null;

        int index = path.lastIndexOf(separator);
        if(index == -1)
            return path;
        else
            return path.substring(0, index);
    }

    public static String getPathByDeletingLastComponent(String path) {
        return getPathByDeletingLastComponent(path, File.separator);
    }

    public static List sortedFilesInPath(String path) {
        File[] files = new File(path).listFiles();
        List sortedFiles = Arrays.asList(files);
        Collections.sort(sortedFiles);
        return sortedFiles;
    }

    public static String escapeString(String a) {
        StringBuffer b = new StringBuffer();
        for(int i=0; i<a.length(); i++) {
            char c1 = a.charAt(i);
            char c2 = (i+1<a.length())?a.charAt(i+1):0;
            if(c1 == '\\' && c2 != '\\') {
                b.append('\\');
                b.append('\\');
            } else if(c1 == '\\') {
                b.append('\\');
                b.append('\\');
                i++;
            } else {
                b.append(c1);
            }
        }
        return b.toString();
    }

    public static Object clone(Object object) throws Exception {
        Object copy = null;

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();

            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            copy = ois.readObject();
        } finally {
            if(ois != null)
                ois.close();
            if(oos != null)
                oos.close();
        }
        return copy;
    }

    public static void writeStringToFile(String text, String file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        os.write(text.getBytes());
        os.close();
    }

    public static String getStringFromFile(String file) throws IOException {
        char [] data = null;
        FileReader fr = null;
        try {
            File f = new File(file);
            data = new char[(int)f.length()];
            fr = new FileReader(f);
            fr.read(data);
        } catch(IOException e) {
            throw e;
        } finally {
            if ( fr!=null ) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new String(data);
    }

    public static final String VERSION_EA = "ea";
    public static final String VERSION_BETA = "b";

    protected static int[] parseVersionElement(String e) {
        int[] v = new int[] { 0, 0, 0 };

        int index;
        if((index = e.indexOf(VERSION_EA)) > 0) {
            v[0] = Integer.parseInt(e.substring(0, index));
            v[1] = -2;
            v[2] = Integer.parseInt(e.substring(index+VERSION_EA.length()));
        } else if((index = e.indexOf(VERSION_BETA)) > 0) {
            v[0] = Integer.parseInt(e.substring(0, index));
            v[1] = -1;
            v[2] = Integer.parseInt(e.substring(index+VERSION_BETA.length()));
        } else {
            v[0] = Integer.parseInt(e);
        }

        return v;
    }

    protected static int compareVersionElement(String a, String b) {
        // -1 if a < b
        // 1 if a > b
        // 0 if a == b

        int[] va = parseVersionElement(a);
        int[] vb = parseVersionElement(b);

        for(int i=0; i<va.length; i++) {
            if(va[i] < vb[i])
                return -1;
            else if(va[i] > vb[i])
                return 1;
        }

        return 0;

    }

    public static boolean isVersionGreaterThan(String a, String b) {
        // Return true if version a > version b

        if(a == null || b == null)
            return false;

        String[] va = a.split("\\.");
        String[] vb = b.split("\\.");

        for(int i=0; i<va.length; i++) {
            if(i == vb.length)
                return true;

            switch(compareVersionElement(va[i], vb[i])) {
                case 1: // ia > ib
                    return true;
                case -1: // ia < ib
                    return false;
                case 0: // equal
                    // continue
            }
        }

        return false;
    }

    public static String encodeToURL(String s) {
        return encodeToURL(s, "");
    }

    public static String encodeToURL(String s, String defaultString) {
        String encoded = defaultString;
        if(s != null && s.length() > 0) {
            try {
                encoded = URLEncoder.encode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("XJUtils:encodeToURL exception: "+e);
            }
        }
        return encoded;
    }

    /** Returns a text where all line separator are \n
     *
     * @return the normalized text
     */

    public static String getNormalizedText(String text) {
        StringBuffer normalizedText = new StringBuffer();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        try {
            String line;
            while((line = reader.readLine()) != null) {
                normalizedText.append(line);
                normalizedText.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return normalizedText.toString();
    }

    /** Returns a text where all line separator are taken from the system property
     *
     * @return the localized text
     */

    public static String getLocalizedText(String text) {
        StringBuffer localizedText = new StringBuffer();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String lineSeparator = XJSystem.getLineSeparator();
        try {
            String line;
            while((line = reader.readLine()) != null) {
                localizedText.append(line);
                localizedText.append(lineSeparator);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localizedText.toString();
    }

    /** Returns a string containing the representation of the stack trace
     * of the exception
     *
     */

    public static String stackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

}
