package com.dropboxish.model.interfaces;

import java.io.Serializable;

/**
 * Created by satyan on 12/6/17.
 * Object that can be serialized to a json format
 */
public interface JsonSerializable<T> extends Serializable{
    /**
     * Serialize {@link T}
     * @return the serialized object
     */
    String toJson();
    /**
     * Get {@link T} from {@param json} string.
     * @param json the serialized {@link T}
     * @return An instance of {@link T}
     */
    T fromJson(String json);
}
