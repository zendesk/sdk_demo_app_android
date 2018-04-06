package com.zendesk.rememberthedate;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class Constants {
    public final static SimpleDateFormat HUMAN_READABLE_DATETIME = new SimpleDateFormat("EEEE, dd MMMM HH:mm", Locale.getDefault());
    public final static SimpleDateFormat HUMAN_READABLE_DATE = new SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault());
    public final static SimpleDateFormat HUMAN_READABLE_TIME = new SimpleDateFormat("HH:mm a ", Locale.getDefault());
}
