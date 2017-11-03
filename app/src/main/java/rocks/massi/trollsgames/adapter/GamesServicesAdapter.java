package rocks.massi.trollsgames.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.GlideApp;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.data.GameSearchService;

import java.util.List;

public class GamesServicesAdapter extends ArrayAdapter<GameSearchService> {

    public GamesServicesAdapter(Context context, int resource, List<GameSearchService> objects) {
        super(context, resource, objects);
    }

    @SuppressWarnings("MissingTranslation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GameSearchService g = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_search_item, parent, false);
        }

        TextView gameNameTv = convertView.findViewById(R.id.game_name);
        TextView gameServiceTv = convertView.findViewById(R.id.service_description);

        gameNameTv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "font/Raleway-Regular.ttf"));
        gameServiceTv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "font/Raleway-Regular.ttf"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gameNameTv.setTextColor(getContext().getColor(R.color.gameDefault));
            gameServiceTv.setTextColor(getContext().getColor(R.color.gameExpansion));
        }
        else {
            gameNameTv.setTextColor(Color.parseColor("#000000"));
            gameServiceTv.setTextColor(Color.parseColor("#AFAFAF"));
        }

        gameNameTv.setText(g.getDisplayName());
        ImageView serviceImage = convertView.findViewById(R.id.service_image);

        switch (g.getService()) {
            case TRICTRAC:
                GlideApp.with(serviceImage)
                        .load(R.drawable.tt_logo)
                        .into(serviceImage);
                gameServiceTv.setText(R.string.TricTrac);
                break;
            case PHILIBERT:
                GlideApp.with(serviceImage)
                        .load(R.drawable.philibert_logo)
                        .into(serviceImage);
                gameServiceTv.setText(R.string.Philibert);
                break;
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(g);
            }
        });

        return convertView;
    }
}
