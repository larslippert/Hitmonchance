package com.lalov.hitmonchance;

import java.io.Serializable;

public class Pokemon implements Serializable {
    private int id;
    private String name;
    private int level;
    private String attack1;
    private String attack2;
    private int hp;
    private int attack;
    private int defense;
    private int spattack;
    private int spdefense;
    private int speed;
    private int generation;
    private boolean legendary;


    //@PrimaryKey(autoGenerate = true)
    //private int uid;

    //public Pokemon(){
    //}
    public Pokemon(int Id,String Name,int Level,String Attack1,String Attack2, int Hp,int Attack, int Defense, int Spattack,int Spdefense, int Speed, int Generation, boolean Legendary){
        this.id = Id;
        this.name = Name;
        this.level = Level;
        this.attack1 = Attack1;
        this.attack2 = Attack2;
        this.hp = Hp;
        this.attack = Attack;
        this.defense = Defense;
        this.spattack = Spattack;
        this.spdefense = Spdefense;
        this.speed = Speed;
        this.generation = Generation;
        this.legendary = Legendary;
    }

    public int getId(){
        return id;
    }
    public void setID(int id) {this.id = id;}

    public String getName(){
        return name;
    }
    public void setName(String name) {this.name = name;}

    public int getLevel(){
        return level;
    }
    public void setLevel(int level) {this.level = level;}

    public String getAttack1(){
        return attack1;
    }
    public void setAttack1(String attack1) {this.attack1 = attack1;}

    public String getAttack2(){
        return attack2;
    }
    public void setAttack2(String attack2) {this.attack2 = attack2;}

    public int getHp(){
        return hp;
    }
    public void setHp(int hp) {this.hp = hp;}

    public int getAttack(){
        return attack;
    }
    public void setAttack(int attack) {this.attack = attack;}

    public int getDefense(){
        return defense;
    }
    public void setDefense(int defense) {this.defense = defense;}

    public int getSpattack(){
        return spattack;
    }
    public void setSpattack(int spattack) {this.spattack = spattack;}

    public int getSpdefense(){
        return spdefense;
    }
    public void setSpdefense(int spdefense) {this.spdefense = spdefense;}

    public int getSpeed(){
        return speed;
    }
    public void setSpeed(int speed) {this.speed = speed;}

    public int getGeneration(){
        return generation;
    }
    public void setGeneration(int generation) {this.generation = generation;}

    public boolean getLegendary(){
        return legendary;
    }
    public void setLegendary(boolean legendary) {this.legendary = legendary;}
}
