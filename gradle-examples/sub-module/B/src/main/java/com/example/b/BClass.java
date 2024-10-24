package com.example.b;

import com.example.shared.SharedInterface;

public class BClass implements SharedInterface {

    @Override
    public void performAction() {
        System.out.println("Action performed by B");
    }
}
