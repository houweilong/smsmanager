package com.njust.smsmanager;

import android.app.SearchManager;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.njust.smsmanager.utils.Constants;

public class SearchProvider extends SearchRecentSuggestionsProvider {
    private final static String AUTHORITY = "com.njust.smsmanager.SearchProvider";
    private final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    private final String[] sms_projection = {
            "_id as " + BaseColumns._ID,
            "body as " + SearchManager.SUGGEST_COLUMN_TEXT_2,
            "body as " + SearchManager.SUGGEST_COLUMN_QUERY,
            "address as " + SearchManager.SUGGEST_COLUMN_TEXT_1
    };
    public SearchProvider() {
        setupSuggestions(AUTHORITY,MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        String sms_selection = "  body like '%" + selectionArgs[0] + "'";
        Context context = getContext();
        if(null != context) {
            return context.getContentResolver().query(Constants.URI_SMS, sms_projection,
                    sms_selection, null, null);
        }
        return null;
    }
}
