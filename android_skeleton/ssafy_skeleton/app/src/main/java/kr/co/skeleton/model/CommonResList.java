package kr.co.skeleton.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Data;


@Data
public class CommonResList <T> {
    @SerializedName("output")
    private int output = 0;

    @SerializedName("msg")
    private String message = "";

    @SerializedName("data")
    private ArrayList<T> data;

}