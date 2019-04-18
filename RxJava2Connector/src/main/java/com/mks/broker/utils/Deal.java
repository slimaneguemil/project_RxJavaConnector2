package com.mks.broker.utils;

import lombok.Data;

@Data
public class Deal implements   Event{
    private  long start ;
    private  long end ;
    private String version;
    private com.mks.broker.utils.Data data;


}
