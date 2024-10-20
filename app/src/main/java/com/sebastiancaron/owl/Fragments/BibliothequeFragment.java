package com.sebastiancaron.owl.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastiancaron.owl.MainListener;
import com.sebastiancaron.owl.Objects.Mp3file;
import com.sebastiancaron.owl.Objects.Playlist;
import com.sebastiancaron.owl.Owl;
import com.sebastiancaron.owl.R;
import com.sebastiancaron.owl.Tools.Mp3RecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BibliothequeFragment extends Fragment {

    private ListView listView;
    private RecyclerView recyclerView;
    private TextView progressText;
    private Mp3RecyclerAdapter adapter;
    private static final int REQUEST_PERMISSION = 1001;
    private List<Mp3file> mp3FilesList;
    private List<Mp3file> allList;
    MainListener mainActivity;
    private Toolbar toolbar;
    private ImageView clear, more;
    private TextView selectedItemCount;
    private Owl owl;
    private BibliothequeFragment bibliothequeFragment;
    private final int dataLimit = -1;
    private int currentLoaded = 0;
    private Playlist p;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bibliotheque, container, false);
        owl = Owl.getInstance();
        mainActivity = (MainListener) getActivity();
        recyclerView = rootView.findViewById(R.id.recyclerView);
        toolbar = rootView.findViewById(R.id.toolbar);
        clear = rootView.findViewById(R.id.imageViewClearSelected);
        more = rootView.findViewById(R.id.imageViewMoreSelected);
        selectedItemCount = rootView.findViewById(R.id.selectedItemCount);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setTitle(null);

        owl.setBibliothequeFragment(this);



        mp3FilesList = new ArrayList<>();
        p = new Playlist();
        p.setNom(getResources().getString(R.string.tous));
        p.setSongs(mp3FilesList);
        allList = mainActivity.allFiles;

        if(allList.size() > 0){
            p.setPathToImage(allList.get(0).getThumbnailPath());
        }
        adapter = new Mp3RecyclerAdapter(getContext(), p);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadMoreData();

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
//
//                int threshold = currentLoaded - 30;
//                if (firstVisibleItemPosition + visibleItemCount >= threshold) {
//                    loadMoreData();
//                }
//            }
//        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                owl.clearSelectedFiles();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogDownSelection customDialogDownSelection = new CustomDialogDownSelection(getContext(), R.style.CustomDialogTheme, owl.getSelectedFiles(p));
                customDialogDownSelection.setCanceledOnTouchOutside(true);
                customDialogDownSelection.show();
            }
        });
        owl.clearSelectedFiles();

        EditText searchEditText = rootView.findViewById(R.id.searchEditText);
        ImageView searchIcon = rootView.findViewById(R.id.searchIcon);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchEditText.isFocused()){
                    searchEditText.clearFocus();
                    searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
                    searchEditText.setText("");
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }else{
                    searchEditText.requestFocus();
                    searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear));
                }

            }
        });
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    searchIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear));
                }
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Not used in this example
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Update the RecyclerView with the search results
                String searchQuery = charSequence.toString();
                List<Mp3file> searchResults = searchMp3Files(allList, searchQuery);
                p.setSongs(searchResults);
                adapter = new Mp3RecyclerAdapter(getContext(), p);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used in this example
            }
        });


        updateToolbarVisibility();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public List<Mp3file> searchMp3Files(List<Mp3file> mp3Files, String query) {
        List<Mp3file> searchResults = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        for (Mp3file mp3file : mp3Files) {
            if (mp3file.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    mp3file.getArtist().toLowerCase().contains(lowercaseQuery)) {
                searchResults.add(mp3file);
            }
        }

        return searchResults;
    }

    private void loadMoreData() {
        // Simulate loading additional data (next 25 elements)
        if(dataLimit < 0 && mp3FilesList.size() != allList.size()){
            mp3FilesList.addAll(allList);
            adapter.notifyDataSetChanged();
            return;
        }
        if(currentLoaded >= allList.size()){
            return;
        }
        for (int i = 0; i < dataLimit; i++) {
            mp3FilesList.add(allList.get(currentLoaded));
            currentLoaded++;
            if(currentLoaded >= allList.size()){
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Méthode pour mettre à jour le compteur d'éléments sélectionnés
    private void updateSelectedItemCounter() {
        int selectedCount = 0;
        for (Mp3file item : mp3FilesList) {
            if (item.isSelected()) {
                selectedCount++;
            }
        }
        selectedItemCount.setText(getString(R.string.selected_items_count, selectedCount));
    }

    // Méthode pour mettre à jour la visibilité de la Toolbar
    private void updateToolbarVisibility() {
        boolean isAnyItemSelected = false;
        for (Mp3file item : mp3FilesList) {
            if (item.isSelected()) {
                isAnyItemSelected = true;
                break;
            }
        }
        if (isAnyItemSelected) {
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }
    }

    public void updateUI(){
        updateSelectedItemCounter();
        updateToolbarVisibility();

    }
    public void updateAdapter(){
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroy() {
        owl.setBibliothequeFragment(null);
        super.onDestroy();

    }


}
