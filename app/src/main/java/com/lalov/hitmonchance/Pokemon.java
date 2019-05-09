package com.lalov.hitmonchance;

import java.io.Serializable;

public class Pokemon implements Serializable {
    private long id;
    private String name;
    private String type1;
    private String type2;
    private long hp;
    private long attack;
    private long defense;
    private long spattack;
    private long spdefense;
    private long speed;

    public Pokemon(long Id,String Name,String Type1, long Speed, long Spdefense, long Spattack, long Defense, long Attack, long Hp){
        this.id = Id;
        this.name = Name;
        this.type1 = Type1;
        this.hp = Hp;
        this.attack = Attack;
        this.defense = Defense;
        this.spattack = Spattack;
        this.spdefense = Spdefense;
        this.speed = Speed;
    }

    public long getId(){
        return id;
    }
    public void setID(long id) {this.id = id;}

    public String getName(){
        return name;
    }
    public void setName(String name) {this.name = name;}

    public String getType1(){
        return type1;
    }
    public void setType1(String type1) {this.type1 = type1;}

    public String getType2(){
        return type2;
    }
    public void setType2(String type2) {this.type2 = type2;}

    public long getHp(){
        return hp;
    }
    public void setHp(long hp) {this.hp = hp;}

    public long getAttack(){
        return attack;
    }
    public void setAttack(long attack) {this.attack = attack;}

    public long getDefense(){
        return defense;
    }
    public void setDefense(long defense) {this.defense = defense;}

    public long getSpattack(){
        return spattack;
    }
    public void setSpattack(long spattack) {this.spattack = spattack;}

    public long getSpdefense(){
        return spdefense;
    }
    public void setSpdefense(long spdefense) {this.spdefense = spdefense;}

    public long getSpeed(){
        return speed;
    }
    public void setSpeed(long speed) {this.speed = speed;}
}
