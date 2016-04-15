package fourtabview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mainview.demo.R;

/**
 * Created by admin on 2016/2/25.
 */
public class TabTwoFragment extends Fragment {

    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.tabfour_view, null);

        return view;
    }
}
