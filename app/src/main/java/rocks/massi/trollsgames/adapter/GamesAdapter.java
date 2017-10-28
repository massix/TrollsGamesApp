package rocks.massi.trollsgames.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
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
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "font/Raleway-Regular.ttf"));
        tv.setText(g.getName());

        if (g.isExtension()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tv.setTextColor(getContext().getResources().getColor(R.color.gameExpansion, getContext().getTheme()));
            }
            else {
                tv.setTextColor(Color.parseColor("#AFAFAF"));
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tv.setTextColor(getContext().getResources().getColor(R.color.gameDefault, getContext().getTheme()));
            }
            else {
                tv.setTextColor(Color.parseColor("#000000"));
            }
        }

        ImageView iv = convertView.findViewById(R.id.gameImage);

        GlideApp.with(iv)
                .load(g.getNormalizedThumbnail())
                .placeholder(R.drawable.ic_search_black_24dp)
                .circleCrop()
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
