package com.bram.belajarosmdroid.API;

public class Utils {
    public static final String BASE_URL_API = "https://dev.sabinsolusi.com/sikopen-api/";

    public static BaseApiService getAPI(){
        return RetroClient.getClient(BASE_URL_API).create(BaseApiService.class);
    }
}
