package com.xu.xmaster.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.xu.xmaster.beans.FileBean;

import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static List<String> loadPhoto(Context context) {
        List<String> list = new ArrayList<>();

        String[] mediaColumns = {MediaStore.Images.Media.DATA};
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mediaColumns,
                null,
                null,
                null);

        if (cursor.moveToLast()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToPrevious());
        }
        cursor.close();

        return list;
    }

    public static List<String> loadVideo(Context context) {
        List<String> list = new ArrayList<>();

        String[] mediaColumns = {MediaStore.Video.Media.DATA};
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns,
                null,
                null,
                null);

        if (cursor.moveToLast()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToPrevious());
        }
        cursor.close();

        return list;
    }

    public static List<FileBean> loadVideoBean(Context context) {
        List<FileBean> list = new ArrayList<>();

        String[] mediaColumns = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT};
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                mediaColumns,
                null,
                null,
                null);

        if (cursor.moveToLast()) {
            do {
                FileBean fileBean = new FileBean(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3),
                        cursor.getInt(4));
                list.add(fileBean);
            } while (cursor.moveToPrevious());
        }
        cursor.close();

        return list;
    }
}
