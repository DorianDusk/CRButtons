package com.melothemelon.crbuttons;

public class SoundObject {
    private String itemName;
    private Integer itemID;
    private String characterName;

    public SoundObject(String itemName, String characterName, Integer itemID){
        this.itemName = itemName;
        this.characterName = characterName;
        this.itemID = itemID;
    }

    public String getItemName(){
        return itemName;
    }

    public Integer getItemID(){
        return itemID;
    }

    public String getCharacterName(){ return characterName;}

}
