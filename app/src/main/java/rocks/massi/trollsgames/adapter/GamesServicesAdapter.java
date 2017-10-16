package rocks.massi.trollsgames.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.data.GameSearchService;

import java.util.List;

public class GamesServicesAdapter extends ArrayAdapter<GameSearchService> {

    public GamesServicesAdapter(Context context, int resource, List<GameSearchService> objects) {
        super(context, resource, objects);
    }

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

        gameNameTv.setText(g.getDisplayName());

        ImageView serviceImage = convertView.findViewById(R.id.service_image);

        switch (g.getService()) {
            case TRICTRAC:
                gameServiceTv.setText("TricTrac");
//                serviceImage.setImageResource(R.drawable.ic_search_black_24dp);
                serviceImage.setImageResource(R.drawable.tt_logo);
                break;
            case PHILIBERT:
                gameServiceTv.setText("Philibert");
//                serviceImage.setImageResource(R.drawable.ic_menu_gallery);
                serviceImage.setImageResource(R.drawable.philibert_logo);
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
