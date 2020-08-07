package com.example.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.io.FileUtils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button add_btn;
    EditText input_item;
    RecyclerView todo_list;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add_btn = findViewById(R.id.add_btn);
        input_item = findViewById(R.id.edit_item);
        todo_list = findViewById(R.id.todo_list);

        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener= new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                //Delete the item from the mode
                items.remove(position);
                //Notify which item was deleted
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed.", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single Clicked at position " + position);

                // Create new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // Pass data to next activity
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // Display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        todo_list.setAdapter(itemsAdapter);
        todo_list.setLayoutManager(new LinearLayoutManager(this));

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = input_item.getText().toString();
                //Add the item to the model
                items.add(todoItem);
                //notify adapter of item insert
                itemsAdapter.notifyItemInserted(items.size() - 1);
                input_item.setText("");
                Toast.makeText(getApplicationContext(), todoItem + " was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    // handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            // retrieve the updated text value
            assert data != null;
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            //extract original position of the edited item from the position key
            int position = Objects.requireNonNull(data.getExtras()).getInt(KEY_ITEM_POSITION);
            // update the model with the new item text
            items.set(position, itemText);
            // notify the adapter
            itemsAdapter.notifyItemChanged(position);
            // persist changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item Updated", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    // This function will load items by reading every line of the data file
    private void loadItems() {
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }
    // Writing the file
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}
