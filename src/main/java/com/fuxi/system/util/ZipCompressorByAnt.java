package com.fuxi.system.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

public class ZipCompressorByAnt {

    private File zipFile;

    public ZipCompressorByAnt(String pathName) {
        zipFile = new File(pathName);
    }

    public void compress(String srcPathName) {
        this.compress(srcPathName, null, null);
    }

    public void compress(String srcPathName, List includes, List excludes) {
        File srcdir = new File(srcPathName);
        if (!srcdir.exists())
            throw new RuntimeException(srcPathName + "不存在！");
        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(srcdir);
        // fileSet.setIncludes("**/*.java"); 包括哪些文件或文件夹
        // eg:zip.setIncludes("*.java");
        // fileSet.setExcludes(...); 排除哪些文件或文件夹
        if (includes != null) {
            for (Object o : includes) {
                String s = (String) o;
                fileSet.setIncludes(s);
            }
        }
        if (excludes != null) {
            for (Object o : excludes) {
                String s = (String) o;
                fileSet.setExcludes(s);
            }
        }
        zip.addFileset(fileSet);
        zip.execute();
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        String filename = "c:/fpda.rar";
        zipIeap(filename);
    }

    public static void zipIeap(String filename) {
        System.out.println("正在生成升级文件：");
        ZipCompressorByAnt zca = new ZipCompressorByAnt(filename);
        List excludes = new ArrayList();
        excludes.add("**/*svn*");
        excludes.add("**/log/**");
        excludes.add("**/bin/**");
        excludes.add("**/upload/**");
        excludes.add("**/plug-in/**");
        excludes.add("**/userfile/**");
        excludes.add("**/image/**");
        excludes.add("**/WEB-INF/lib/**");
        excludes.add("**/WEB-INF/tld/**");
        excludes.add("**/WEB-INF/classes/dbconfig.properties");
        excludes.add("**/WEB-INF/classes/sysConfig.properties");
        excludes.add("**/WEB-INF/classes/log4j.properties");
        excludes.add("**/WEB-INF/classes/ehcache.xml");
        excludes.add("**/license.dat");

        List includes = new ArrayList();
        String path = System.getProperty("user.dir") + "/WebRoot";
        System.out.println("压缩路径为：" + path);
        zca.compress(path, includes, excludes);
        System.out.println("生成升级文件成功:" + filename);
    }

}
