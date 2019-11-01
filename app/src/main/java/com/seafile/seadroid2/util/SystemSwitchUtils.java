package com.seafile.seadroid2.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;

import com.seafile.seadroid2.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SystemSwitchUtils {
    private Context context;
    private ConnectivityManager connManager;
    private static SystemSwitchUtils util;
    private Map<String, String> infos = new HashMap<String, String>();
    private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static SystemSwitchUtils getInstance(Context context){
        if (util == null) {
            util = new SystemSwitchUtils(context);
        }
        return util;

    }

    private SystemSwitchUtils(Context context) {
        super();
        this.context = context;
    }


    /**
     * Open Sync
     * @return
     */
    @SuppressWarnings("deprecation")
    public boolean isSyncSwitchOn() {
        if (connManager == null) {
            connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        return connManager.getBackgroundDataSetting()
                && ContentResolver.getMasterSyncAutomatically();
    }

    /**
     * synchro switch
     */
    public void syncSwitchUtils() {

        if (!isSyncSwitchOn()) {
            ContentResolver.setMasterSyncAutomatically(!isSyncSwitchOn());
        }

    }
    /**
     * Save the log
     * @param ex
     * @return
     */
    private String saveCrashInfo2File(Throwable ex,Context ct) {
        collectDeviceInfo(ct);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash_exception" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = getLogPath(ct);
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {

        }
        return null;
    }
    /**
     * Collect equipment information
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {

        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception e) {

        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {

            }
        }
    }
    public static String getCacheDir(Context context) {
        String esDir = null;
        try {
            esDir=context.getExternalCacheDir().toString();
        } catch (Exception e) {
            esDir=context.getCacheDir().toString();
        }
        return esDir;
    }
    public static String getLogPath(Context context){
        String path = getCacheDir(context);
        File log = new File(path,"log");
        if(!log.exists()){
            log.mkdirs();
        }
        return log.getAbsolutePath();
    }

    public static String date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseDateTime(String dateString) {
        if (dateString == null) return null;
        if (dateString.contains("T")) dateString = dateString.replace('T', ' ');
        String[] arr1 = dateString.split("\\+");
        return Utils.translateCommitTime(Long.parseLong(date2TimeStamp(arr1[0], "yyyy-MM-dd HH:mm:ss")) * 1000);
    }

    public static String obj_type(Context ct, String obj_type, String op_type) {
        if (obj_type.equals("repo")) {
            if (op_type.equals("create")) {
                return ct.getString(R.string.create_new_repo);
            } else if (op_type.equals("rename")) {
                return ct.getString(R.string.rename_repo);
            } else if (op_type.equals("delete")) {
                return ct.getString(R.string.delete_repo_title);
            } else if (op_type.equals("restore")) {
                return ct.getString(R.string.recover_library);
            } else if (op_type.equals("edit")) {
                return ct.getString(R.string.edit);
            } else {
                return "";
            }
        } else if (obj_type.equals("dir")) {
            if (op_type.equals("create")) {
                return ct.getString(R.string.create_new_dir);
            } else if (op_type.equals("rename")) {
                return ct.getString(R.string.rename_dir);
            } else if (op_type.equals("delete")) {
                return ct.getString(R.string.delete_dir);
            } else if (op_type.equals("restore")) {
                return ct.getString(R.string.recover_folder);
            } else if (op_type.equals("move")) {
                return ct.getString(R.string.move_folder);
            } else if (op_type.equals("edit")) {
                return ct.getString(R.string.edit);
            } else {
                return "";
            }
        } else if (obj_type.equals("file")) {
            if (op_type.equals("create")) {
                return ct.getString(R.string.create_new_file);
            } else if (op_type.equals("rename")) {
                return ct.getString(R.string.rename_file);
            } else if (op_type.equals("delete")) {
                return ct.getString(R.string.delete_file_f);
            } else if (op_type.equals("restore")) {
                return ct.getString(R.string.recover_file);
            } else if (op_type.equals("move")) {
                return ct.getString(R.string.move_file);
            } else if (op_type.equals("update")) {
                return ct.getString(R.string.update_file);
            } else if (op_type.equals("edit")) {
                return ct.getString(R.string.edit);
            } else {
                return "";
            }
        } else if (obj_type.equals("draft")) {
            if (op_type.equals("create")) {
                return ct.getString(R.string.create_draft);
            } else if (op_type.equals("rename")) {
                return ct.getString(R.string.rename_draft);
            } else if (op_type.equals("delete")) {
                return ct.getString(R.string.del_draft);
            } else if (op_type.equals("update")) {
                return ct.getString(R.string.update_draft);
            } else if (op_type.equals("publish")) {
                return ct.getString(R.string.release_draft);
            } else if (op_type.equals("edit")) {
                return ct.getString(R.string.edit);
            } else {
                return "";
            }
        } else if (obj_type.equals("files")) {
            if (op_type.equals("create")) {
                return ct.getString(R.string.create_files);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private Context mContext;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String mParentPath;
    private String mParentFileName = "seafile";
    private String mFileName = "seafileLog.txt";


    public void init(Context context) {
        this.mContext = context;
        mParentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            File file = new File(mParentPath, mParentFileName);
            file.mkdir();
            if (file.exists()) {

            } else {
                file.createNewFile();
            }
            File mFile = new File(file.getPath(), mFileName);
            if (!mFile.exists()) {
                mFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void wtriteSportData(Context ct,String content) {
        init(ct);
        String data =
                sdf.format(new Date()) + "\n" + content + "\n"
                        + "\n";
        BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fs = null;
        try {
            String path = mParentPath + File.separator + mParentFileName + File.separator + mFileName;
            fs = new FileOutputStream(path, true);
            osw = new OutputStreamWriter(fs);
            bw = new BufferedWriter(osw);
            bw.write(data);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}