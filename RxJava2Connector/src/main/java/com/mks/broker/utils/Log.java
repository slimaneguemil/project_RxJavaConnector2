package com.mks.broker.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Log implements Event{
    String content;
}
