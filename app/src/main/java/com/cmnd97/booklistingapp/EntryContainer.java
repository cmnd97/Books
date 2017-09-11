package com.cmnd97.booklistingapp;

import java.util.ArrayList;

class EntryContainer {

    private static EntryContainer instance = null;

    static EntryContainer getInstance() {

        return (instance != null) ? instance : (instance = new EntryContainer());
    }

    ArrayList<Entry> entries;
    boolean rotatedWithEmptyAdapter=true; //to prevent crashing by referencing a null adapter

}