package com.zsp.hotfixdemo.hotfix;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 热修复工具(只认后缀是dex、apk、jar、zip的补丁)
 * author：Andy on 2019/4/9 0009 10:51
 * email：zsp872126510@gmail.com
 *
 * 步骤：
 * 1、从网络下载补丁包
 * 2、解压获取到修复好的.dex文件
 * 3、加载补丁
 * 4、合并之前的dex
 * 5、重写给DexPathList里面的Element[] dexElements赋值
 */
public class FixDexUtils {

    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    /**
     * 加载补丁，使用默认目录：data/data/包名/files/odex
     *
     * @param context
     */
    public static void loadFixedDex(Context context) {
        loadFixedDex(context, null);
    }

    /**
     * 加载补丁
     *
     * @param context       上下文
     * @param patchFilesDir 补丁所在目录
     */
    public static void loadFixedDex(Context context, File patchFilesDir) {
        if (context == null) {
            return;
        }
        // 遍历所有的修复dex
        File fileDir = patchFilesDir != null ? patchFilesDir : new File(context.getFilesDir(), DEX_DIR);// data/data/包名/files/odex（这个可以任意位置）
        File[] listFiles = fileDir.listFiles();
        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.getName().startsWith("output") &&
                        (file.getName().endsWith(DEX_SUFFIX)
                                || file.getName().endsWith(APK_SUFFIX)
                                || file.getName().endsWith(JAR_SUFFIX)
                                || file.getName().endsWith(ZIP_SUFFIX))) {
                    loadedDex.add(file);// 存入集合
                }
            }
            // dex合并之前的dex
            doDexInject(context, loadedDex);
        }
    }

    private static void doDexInject(Context appContext, HashSet<File> loadedDex) {
        String optimizeDir = appContext.getFilesDir().getAbsolutePath() + File.separator + OPTIMIZE_DEX_DIR;// data/data/包名/files/optimize_dex（这个必须是自己程序下的目录）
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        try {
            // 1.加载应用程序的dex
            PathClassLoader pathLoader = (PathClassLoader) appContext.getClassLoader();
            for (File dex : loadedDex) {
                // 2.加载指定的修复的dex文件
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),// 修复好的dex（补丁）所在目录
                        fopt.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,// 加载dex时需要的库
                        pathLoader// 父类加载器
                );
                // 3.合并
                //先获取到dexClassLoader里面的DexPathList类型的pathList
                Object myDexPathList = getPathList(dexLoader);
                //通过DexPathList拿到dexElements对象
                Object myDexElements = getDexElements(myDexPathList);
                //拿到应用程序使用的类加载器的pathList
                Object systemPathPathList = getPathList(pathLoader);
                //获取到系统的dexElements对象
                Object systemDexElements = getDexElements(systemPathPathList);
                // 合并完成
                Object newDexElements = combineArray(myDexElements, systemDexElements);
                // 重写给PathList里面的Element[] dexElements;赋值
                setField(systemPathPathList, systemPathPathList.getClass(), "dexElements", newDexElements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    /**
     * 反射得到类加载器中的pathList对象
     */
    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射得到pathList中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 数组合并
     */
    private static Object combineArray(Object myDexElements, Object systemDexElements) {
        //新建一个Element[]类型的dexElements实例
        Class<?> sigleElementClazz = myDexElements.getClass().getComponentType();
        int systemLength = Array.getLength(systemDexElements);
        int myLength = Array.getLength(myDexElements);
        int newSystenLength = systemLength + myLength;
        Object newElementsArray = Array.newInstance(sigleElementClazz, newSystenLength);

        //按着先加入dex包里面elment的规律依次加入所有的element，这样就可以保证classLoader先拿到的是修复包里面的Test类。
        for (int i = 0; i < newSystenLength; i++) {
            if (i < myLength) {
                Array.set(newElementsArray, i, Array.get(myDexElements, i));
            }else {
                Array.set(newElementsArray, i, Array.get(systemDexElements, i - myLength));
            }
        }
        return newElementsArray;
    }
}
