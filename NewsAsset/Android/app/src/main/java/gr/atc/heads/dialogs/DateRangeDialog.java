package gr.atc.heads.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;

import gr.atc.heads.R;

/**
 * Created by kGiannakakis on 17/6/2014.
 */
public class DateRangeDialog extends DialogFragment {

    public static DateRangeDialog newInstance(int fromYear, int fromMonth, int fromDay,
                                              int toYear, int toMonth, int toDay) {
        DateRangeDialog f = new DateRangeDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("fromYear", fromYear);
        args.putInt("fromMonth", fromMonth);
        args.putInt("fromDay", fromDay);
        args.putInt("toYear", toYear);
        args.putInt("toMonth", toMonth);
        args.putInt("toDay", toDay);
        f.setArguments(args);

        return f;
    }

    private int fromYear;
    private int fromMonth;
    private int fromDay;
    private int toYear;
    private int toMonth;
    private int toDay;

    public DateRangeDialog () {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromYear = getArguments().getInt("fromYear");
        fromMonth = getArguments().getInt("fromMonth");
        fromDay = getArguments().getInt("fromDay");
        toYear = getArguments().getInt("toYear");
        toMonth = getArguments().getInt("toMonth");
        toDay = getArguments().getInt("toDay");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_range_dialog, container);
        //getDialog().setTitle(R.string.select_date_range);

        final DatePicker fromPicker = (DatePicker) view.findViewById(R.id.from_date);
        final DatePicker toPicker = (DatePicker) view.findViewById(R.id.to_date);

        fromPicker.updateDate(fromYear, fromMonth, fromDay);
        toPicker.updateDate(toYear, toMonth, toDay);

        Button ok = (Button) view.findViewById(R.id.date_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DateRangeDialogListener) getActivity()).onDateRangeSelected(
                        fromPicker.getYear(),
                        fromPicker.getMonth(),
                        fromPicker.getDayOfMonth(),
                        toPicker.getYear(),
                        toPicker.getMonth(),
                        toPicker.getDayOfMonth()
                );
                DateRangeDialog.this.dismiss();
            }
        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public interface DateRangeDialogListener {
        void onDateRangeSelected(int fromYear, int fromMonth, int fromDay,
                                 int toYear, int toMonth, int toDay);
    }
}
