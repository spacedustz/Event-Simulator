package com.generator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageDto {
    private int instanceNumber;
    private String testNumber;

    public MessageDto(int instanceNumber) {
        this.instanceNumber = instanceNumber;
        this.testNumber = "0";
    }
}
