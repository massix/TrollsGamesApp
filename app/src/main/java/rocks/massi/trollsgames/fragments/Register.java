package rocks.massi.trollsgames.fragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import lombok.NoArgsConstructor;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.async.LoginRegisterAsyncConnector;
import rocks.massi.trollsgames.data.User;

@NoArgsConstructor
public class Register extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Set Typefaces
        TextView header = view.findViewById(R.id.f_register_header);
        header.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/Raleway-Regular.ttf"));

        // Set listener for Checkbox
        final CheckBox bggHandledCb = view.findViewById(R.id.cb_bggHandled);
        bggHandledCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText editBggNick = view.findViewById(R.id.edit_bggNick);
                EditText editForumNick = view.findViewById(R.id.edit_forumNick);
                editBggNick.setEnabled(isChecked);

                if (!isChecked) {
                    editBggNick.setText(editForumNick.getText());
                }
            }
        });

        // Changes in forumnick edit should be reflected in bggnick if account is not bgg handled
        final EditText editForumNick = view.findViewById(R.id.edit_forumNick);
        editForumNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!bggHandledCb.isChecked()) {
                    EditText editBggNick = view.findViewById(R.id.edit_bggNick);
                    editBggNick.setText(s);
                }
            }
        });

        // Create the listener for the button
        Button submitButton = view.findViewById(R.id.register_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String bggNick = ((EditText) view.findViewById(R.id.edit_bggNick)).getText().toString();
                String forumNick = ((EditText) view.findViewById(R.id.edit_forumNick)).getText().toString();
                String email = ((EditText) view.findViewById(R.id.edit_email)).getText().toString();
                String password = ((EditText) view.findViewById(R.id.edit_password)).getText().toString();
                boolean bggHandled = bggHandledCb.isChecked();

                // TODO: check that the parameters are correct!
                User toBeRegistered = new User(bggNick, forumNick, email, password, bggHandled);
                new LoginRegisterAsyncConnector(toBeRegistered, getActivity().getString(R.string.server)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
        });

        // View created!
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
