package com.zendesk.rememberthedate;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class Constants {
    public final static SimpleDateFormat HUMAN_READABLE_DATETIME = new SimpleDateFormat("EEEE, dd MMMM hh:mm a", Locale.getDefault());
    public final static SimpleDateFormat HUMAN_READABLE_DATE = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
    public final static SimpleDateFormat HUMAN_READABLE_TIME = new SimpleDateFormat("hh:mm a ", Locale.getDefault());
    public final static String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

}
