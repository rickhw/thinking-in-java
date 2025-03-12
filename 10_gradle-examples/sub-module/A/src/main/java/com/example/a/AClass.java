package com.example.a;

import com.example.shared.SharedInterface;

public class AClass implements SharedInterface {

    @Override
    public void performAction() {
        System.out.println("Action performed by A");
    }
}
