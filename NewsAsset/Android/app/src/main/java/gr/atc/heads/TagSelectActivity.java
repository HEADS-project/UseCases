package gr.atc.heads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.atc.heads.model.Tag;
import gr.atc.heads.model.TagModel;


public class TagSelectActivity extends ActionBarActivity {

    public final static String TAGS_PARAM = "user";

    private List<TagModel> tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_select);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            tags = (List<TagModel>) extras.getSerializable(TAGS_PARAM);
        }
        else {
            tags = (List<TagModel>) savedInstanceState.getSerializable(TAGS_PARAM);
        }

        if (tags == null) {
            List<Tag> appTags = ((HeadsApplication) getApplication()).getTags();
            tags = new ArrayList<TagModel>();

            for (Tag tag : appTags) {
                tags.add(new TagModel(tag.getName(), tag.getId()));
            }
        }

        Collections.sort(tags);

        ListView listview = (ListView) findViewById(R.id.list);
        TagArrayAdapter adapter = new TagArrayAdapter(this, tags);
        listview.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(TAGS_PARAM, (Serializable) tags);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tag_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(TAGS_PARAM, (Serializable) tags);
            setResult(RESULT_OK, returnIntent);
            finish();
            return true;
        }
        else if (id == R.id.action_cancel) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
    }

    class TagArrayAdapter extends ArrayAdapter {
        private final List<TagModel> list;
        private final Activity context;

        public TagArrayAdapter(Activity context, List<TagModel> list) {
            super(context, R.layout.tag_row, list);
            this.context = context;
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflator = context.getLayoutInflater();
                view = inflator.inflate(R.layout.tag_row, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(R.id.label);
                viewHolder.text.setClickable(true);
                viewHolder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TagModel element = (TagModel) viewHolder.checkbox
                                .getTag();
                        element.setSelected(!element.isSelected());
                        viewHolder.checkbox.setChecked(element.isSelected());
                    }
                });

                viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
                viewHolder.checkbox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                                         boolean isChecked) {
                                TagModel element = (TagModel) viewHolder.checkbox
                                        .getTag();
                                element.setSelected(buttonView.isChecked());
                            }
                        });
                view.setTag(viewHolder);
                viewHolder.checkbox.setTag(list.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text.setText(list.get(position).getName());
            holder.checkbox.setChecked(list.get(position).isSelected());
            return view;
        }
    }

}
