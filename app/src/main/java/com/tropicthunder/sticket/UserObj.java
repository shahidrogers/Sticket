package com.tropicthunder.sticket;

/**
 * Created by shahidrogers on 9/26/15.
 */
public class UserObj {
    private String username;

    private boolean parkingStatus;

    public void setUsername(String un){
        this.username = un;
    }

    public String getUsername(){
        return this.username;
    }

    public void setParkingStatus(boolean parkingStatus){
        this.parkingStatus = parkingStatus;
    }

    public boolean getParkingStatus(){
        return this.parkingStatus;
    }

    private static final UserObj holder = new UserObj();
    public static UserObj getInstance() {return holder;}
}
