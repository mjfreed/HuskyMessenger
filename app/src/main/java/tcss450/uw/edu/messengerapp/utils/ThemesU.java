package tcss450.uw.edu.messengerapp.utils;

import tcss450.uw.edu.messengerapp.R;

public class ThemesU {

    public static final int THEME_RUGGED = 0;
    public static final int THEME_MODERN = 1;
    public static final int THEME_SUMMER = 2;
    public static final int THEME_UW = 3;

    public static int getThemeId(int theme){
        int themeId=0;
        switch (theme){
            case THEME_RUGGED  :
                themeId = R.style.AppTheme_Rugged;
                break;
            case THEME_MODERN  :
                themeId = R.style.AppTheme_Modern;
                break;
            case THEME_SUMMER  :
                themeId = R.style.AppTheme_Summer;
                break;
            case THEME_UW  :
                themeId = R.style.AppTheme_UW;
                break;

            default:
                break;
        }
        return themeId;
    }
}
