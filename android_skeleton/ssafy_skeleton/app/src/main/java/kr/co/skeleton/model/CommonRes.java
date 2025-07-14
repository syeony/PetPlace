package kr.co.skeleton.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class CommonRes<T> {
    @SerializedName("output")
    private int output = 0;

    @SerializedName("msg")
    private String message = "";

    @SerializedName("data")
    private T data;

}
