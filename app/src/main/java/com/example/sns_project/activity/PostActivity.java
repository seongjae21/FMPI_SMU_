package com.example.sns_project.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.FirebaseHelper;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.adapter.HomeAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.example.sns_project.view.ReadContentsVIew;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PostActivity extends BasicActivity {
    EditText addreply;//내용
    TextView repadd;//등록버튼
    private PostInfo postInfo;
    private FirebaseHelper firebaseHelper;
    private ReadContentsVIew readContentsVIew;
    private LinearLayout contentsLayout;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private RecyclerView mMainRecyclerView;
    private FirebaseAuth mAuth;

    private FirebaseUser user;

    private MainAdapter mAdapter;
    private List<Rep> mRepList;
    private String id;
    private HomeAdapter homeAdapter;
    private ArrayList<PostInfo> postList;
    private Rep repInfo;
    private String repId;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postList = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, postList);
        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readContentsView);
        repInfo = (Rep) getIntent().getSerializableExtra("repInfo");
        user = FirebaseAuth.getInstance().getCurrentUser();
        id = postInfo.getId();
        firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.setOnPostListener(onPostListener);

        mAuth = FirebaseAuth.getInstance();
        addreply = findViewById(R.id.reptext);
        repadd = findViewById(R.id.repadd);
        repadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addreply.getText().toString().equals("")) {
                    Toast.makeText(PostActivity.this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    addreply();
                }
            }
        });

        mMainRecyclerView = findViewById(R.id.main_recycler_view);


        mRepList = new ArrayList<>();
        firebaseFirestore.collection("posts").document(id).collection("rep")
                .orderBy("timestamp", Query.Direction.ASCENDING)//오래된순으로 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            mRepList.clear();
                            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                                String repId = snap.getId();
                                String publisher = "작성자 : " + user.getUid();
                                String rep = (String) snap.getData().get("rep");
                                Rep data = new Rep(repId, publisher, rep);

                                mRepList.add(data);
                            }
                            mAdapter = new MainAdapter(mRepList);
                            mMainRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
        uiUpdate();
    }

    private void addreply() {
        repId = firebaseFirestore.collection("posts").document(id).collection("rep").document().getId();
        Map<String, Object> reply = new HashMap<>();
        reply.put("repId", repId);
        reply.put("rep", addreply.getText().toString());
        reply.put("publisher", user.getUid());
        reply.put("timestamp", FieldValue.serverTimestamp());
        firebaseFirestore.collection("posts").document(id).collection("rep").document(repId).set(reply);
        finish();

        addreply.setText("");
        hideKeyboard();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    postInfo = (PostInfo) data.getSerializableExtra("postinfo");
                    contentsLayout.removeAllViews();
                    uiUpdate();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                firebaseHelper.storageDelete(postInfo);
                return true;
            case R.id.modify:
                myStartActivity(WritePostActivity.class, postInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(PostInfo postInfo) {
            Log.e("로그 ", "삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그 ", "수정 성공");
        }
    };

    private void uiUpdate() {
        setToolbarTitle(postInfo.getTitle());
        readContentsVIew.setPostInfo(postInfo);
    }

    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("postInfo", postInfo);
        startActivityForResult(intent, 0);
    }

    private class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

        private List<Rep> mRepList;

        public MainAdapter(List<Rep> mRepList) {
            this.mRepList = mRepList;
        }

        @NonNull
        @Override
        public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MainViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
            Rep data = mRepList.get(position);
            holder.mTitleTextView.setText(data.getContents());
            holder.mNameTextView.setText(data.getPublisher());

        }

        @Override
        public int getItemCount() {
            return mRepList.size();
        }

        class MainViewHolder extends RecyclerView.ViewHolder {

            private TextView mTitleTextView;
            private TextView mNameTextView;

            public MainViewHolder(View itemView) {
                super(itemView);

                mTitleTextView = itemView.findViewById(R.id.item_title_text);
                mNameTextView = itemView.findViewById(R.id.item_name_text);
            }
        }
    }

    public void btnClick(View v) {
        firebaseFirestore.collection("posts").document(id).collection("rep").document(mRepList.get(position).getRepId())
                .update("rep", addreply.getText().toString());
        Toast.makeText(getApplicationContext(), "수정",
                Toast.LENGTH_SHORT).show();

    }

    public void btnClick1(View v) {
        firebaseFirestore.collection("posts").document(id).collection("rep").document(mAdapter.mRepList.get(position).getRepId()).delete();
        Toast.makeText(getApplicationContext(), "삭제",
                Toast.LENGTH_SHORT).show();

    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}