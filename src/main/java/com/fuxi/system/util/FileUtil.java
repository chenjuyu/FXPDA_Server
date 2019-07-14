package com.fuxi.system.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class FileUtil {

    public static final String _tempPath = "/WEB-INF/tmp";
    public static final String _download = "/downloads";
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public FileUtil() {}

    public static String file2String(InputStream inputstream) throws IOException {
        StringWriter stringwriter = new StringWriter();
        copyFile(inputstream, stringwriter);
        return stringwriter.toString();
    }

    public static void copyFile(InputStream inputstream, Writer writer) throws IOException {
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        copyFile(((Reader) (inputstreamreader)), writer);
    }

    public static int copyFile(Reader reader, Writer writer) throws IOException {
        char ac[] = new char[4096];
        int i = 0;
        for (int j = 0; -1 != (j = reader.read(ac));) {
            writer.write(ac, 0, j);
            i += j;
        }

        return i;
    }



    public static Object loadObject(String s) throws Exception {
        return loadObject(new File(s));
    }

    public static Object loadObject(File paramFile) throws Exception {
        if ((!paramFile.exists()) || (!paramFile.isFile())) {
            throw new Exception(new FileNotFoundException(String.valueOf(paramFile)));
        }
        Object localObject1 = null;
        FileInputStream localFileInputStream = null;
        ObjectInputStream localObjectInputStream = null;
        try {
            localFileInputStream = new FileInputStream(paramFile);
            localObjectInputStream = new ObjectInputStream(localFileInputStream);
            localObject1 = localObjectInputStream.readObject();
            return localObject1;
        } catch (IOException localIOException3) {
            throw new Exception("Error loading object from file : " + paramFile, localIOException3);
        } catch (ClassNotFoundException localClassNotFoundException) {
            throw new Exception("Class not found when loading object from file : " + paramFile, localClassNotFoundException);
        } finally {
            if (localObjectInputStream != null) {
                try {
                    localObjectInputStream.close();
                } catch (IOException localIOException4) {
                }
            }
            if (localFileInputStream != null) {
                try {
                    localFileInputStream.close();
                } catch (IOException localIOException5) {

                }
            }
        }
    }

    public static Object loadObject(InputStream paramInputStream) throws Exception {
        Object localObject1 = null;
        ObjectInputStream localObjectInputStream = null;
        try {
            localObjectInputStream = new ObjectInputStream(paramInputStream);
            localObject1 = localObjectInputStream.readObject();
            return localObject1;
        } catch (IOException localIOException2) {
            throw new Exception("Error loading object from InputStream", localIOException2);
        } catch (ClassNotFoundException localClassNotFoundException) {
            throw new Exception("Class not found when loading object from InputStream", localClassNotFoundException);
        } finally {
            if (localObjectInputStream != null) {
                try {
                    localObjectInputStream.close();
                } catch (IOException localIOException3) {
                }
            }
        }
    }

    public static void saveObject(Object obj, String s) throws Exception {
        saveObject(obj, new File(s));
    }

    public static void saveObject1(Object obj, File file) throws Exception {}

    public static void saveObject(Object paramObject, File paramFile) throws Exception {
        FileOutputStream localFileOutputStream = null;
        ObjectOutputStream localObjectOutputStream = null;
        try {
            localFileOutputStream = new FileOutputStream(paramFile);
            localObjectOutputStream = new ObjectOutputStream(localFileOutputStream);
            localObjectOutputStream.writeObject(paramObject);
            localObjectOutputStream.flush();
            localFileOutputStream.flush();
            return;
        } catch (IOException localIOException3) {
            throw new Exception("Error saving file : " + paramFile, localIOException3);
        } finally {
            if (localObjectOutputStream != null) {
                try {
                    localObjectOutputStream.close();
                } catch (IOException localIOException4) {
                }
            }
            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException localIOException5) {
                }
            }
        }
    }

    public static void saveObject(Object paramObject, OutputStream paramOutputStream) throws Exception {
        ObjectOutputStream localObjectOutputStream = null;
        try {
            localObjectOutputStream = new ObjectOutputStream(paramOutputStream);
            localObjectOutputStream.writeObject(paramObject);
            localObjectOutputStream.flush();
            return;
        } catch (IOException localIOException2) {
            throw new Exception("Error saving object to OutputStream", localIOException2);
        } finally {
            if (localObjectOutputStream != null) {
                try {
                    localObjectOutputStream.close();
                } catch (IOException localIOException3) {
                }
            }
        }
    }

}
