package rocks.massi.trollsgames.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import rocks.massi.trollsgames.GlideApp;
import rocks.massi.trollsgames.R;
import rocks.massi.trollsgames.data.Game;
import rocks.massi.trollsgames.events.GameSelectedEvent;

import java.util.List;

public class GamesAdapter extends ArrayAdapter<Game> {
    public GamesAdapter(Context context, int resource, List<Game> objects) {
        super(context, resource, objects);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Game g = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.gameName);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "font/Montserrat-Regular.ttf"));
        tv.setText(g.getName());
        ImageView iv = convertView.findViewById(R.id.gameImage);
        GlideApp.with(convertView)
                .load(g.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .fitCenter()
                .into(iv);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new GameSelectedEvent(g));
            }
        });

        return convertView;
    }
}
