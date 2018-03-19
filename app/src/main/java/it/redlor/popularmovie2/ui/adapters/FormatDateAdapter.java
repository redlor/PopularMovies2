package it.redlor.popularmovie2.ui.adapters;

import android.databinding.BindingAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class formats the release date.
 */

public class FormatDateAdapter {

    @BindingAdapter({"release_date"})
    public static void setFormattedReleaseDate(TextView textView, String string) {

        if (string != null) {
            String formattedDate = formatDateFromString("yyyy-MM-dd", "dd-MM-yyyy", string);
            textView.setText(formattedDate);
        }

    }

    private static String formatDateFromString(String inputFormat, String outputFormat, String inputDate){

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputDate;

    }
}
