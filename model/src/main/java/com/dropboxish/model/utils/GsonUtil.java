package com.dropboxish.model.utils;

import com.dropboxish.model.FileInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by satyan on 12/11/17.
 */
public class GsonUtil {
    public final static Gson GSON = new Gson();
    public final static Type LIST_FILE_INFO_TYPE = new TypeToken<List<FileInfo>>(){}.getType();
    public final static Type LIST_STRING_TYPE = new TypeToken<List<String>>(){}.getType();
    public final static Type MAP_STRING_BOOLEAN_TYPE = new TypeToken<Map<String,Boolean>>(){}.getType();
}
