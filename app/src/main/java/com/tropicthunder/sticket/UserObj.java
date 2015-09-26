package com.tropicthunder.sticket;

/**
 * Created by shahidrogers on 9/26/15.
 */
public class UserObj {
    private String username;

    public void setUsername(String un){
        this.username = un;
    }

    public String getUsername(){
        return this.username;
    }

    private static final UserObj holder = new UserObj();
    public static UserObj getInstance() {return holder;}
}
