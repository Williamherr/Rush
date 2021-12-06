package com.example.rush.View.fragments.classes;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rush.MainActivity;
import com.example.rush.Model.ClassInfo;
import com.example.rush.Model.Member;
import com.example.rush.R;
import com.example.rush.View.fragments.messages.MessageFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ClassDetailsFragment extends Fragment {
    private String name, description, id, userID, professor, professorID;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView recycle;
    private DetailsAdapter adapter;
    private ArrayList<Member> students;
    private CollectionReference messageRef;
    private MessageFragment.MessageFragmentListener mListener;
    private TextView nameOfInstructor, descriptionOfClass, label;
    private ActionBar actionBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessageFragment.MessageFragmentListener) context;
    }


    public ClassDetailsFragment() {

    }

    public ClassDetailsFragment(String name, String instructor, String description, String id, String createdBy) {
        this.name = name;
        professor = instructor;
        this.description = description;
        this.id = id;
        professorID = createdBy;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class_details, container, false);
        messageRef = database.collection("chat-messages").document("private-messages").collection("all-private-messages");
        students = new ArrayList<>();
        recycle = (RecyclerView) view.findViewById(R.id.studentsInClass);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recycle.setLayoutManager(manager);
        recycle.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back);
        actionBar.setDisplayHomeAsUpEnabled(true);


        String[] words = new String[]{"Professor", "Class Description"};
        SpannableString stringSpanner = new SpannableString(words[0]);
        stringSpanner.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner.length(), 0);
        stringSpanner.setSpan(new RelativeSizeSpan(1.1f), 0, stringSpanner.length(), 0);
        SpannableString stringSpanner2 = new SpannableString(words[1]);
        stringSpanner2.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner2.length(), 0);
        stringSpanner2.setSpan(new RelativeSizeSpan(1.1f), 0, stringSpanner2.length(), 0);

        nameOfInstructor = view.findViewById(R.id.instructorOfClass);
        nameOfInstructor.setText(stringSpanner);
        nameOfInstructor.append("\n" + professor);
        descriptionOfClass = view.findViewById(R.id.descriptionOfClass);
        descriptionOfClass.setText(stringSpanner2);
        descriptionOfClass.append("\n" + description);
        label = view.findViewById(R.id.listLabel);
        label.setText("Class List");

        //Add the professor to the list
        Member p = new Member(professor, professorID);
        students.add(p);
        adapter = new DetailsAdapter(students);
        recycle.setAdapter(adapter);

        //Get all students in the class
        Task studentRetrieval = database.collection("classes").document(id).collection("Students").get();
        studentRetrieval.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = (String) document.getData().get("Name");
                            String studentID = (String) document.getData().get("ID");
                            String studentEmail = (String) document.getData().get("Email");

                            Member m = new Member(name, studentID);
                            students.add(m);
                            sortStudents();
                            adapter = new DetailsAdapter(students);
                            recycle.setAdapter(adapter);
                        }
                    }
                }
            }
        });
        return view;
    }
//Sort students alphabetically by name
    private void sortStudents() {
        //Professor should always be first, so create a sublist from index 1
        Collections.sort(students.subList(1, students.size()), new Comparator<Member>() {
            @Override
            public int compare(Member m1, Member m2) {
                int comparison = m1.getName().compareToIgnoreCase(m2.getName());
                if (comparison < 0) {
                    return -1;
                } else if (comparison == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ((MainActivity) getActivity()).backFragment();
        return true;
    }


    public class DetailsAdapter extends RecyclerView.Adapter<ClassDetailsFragment.DetailsAdapter.ViewHolder> {
        private ArrayList<Member> students;

        public DetailsAdapter(ArrayList<Member> students) {
            this.students = students;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.students_list,
                    parent, false);
            return new ClassDetailsFragment.DetailsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member obj = students.get(position);
            holder.studentName.setText(obj.getName());

            if (obj.getName().equals(professor) && obj.getUid().equals(professorID)) {
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.INVISIBLE);
            }
            holder.status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "Professor", Toast.LENGTH_SHORT).show();
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                //Allow the user to start a new chat by clicking on any of the class's members
                public void onClick(View view) {
                    Map<String, Object> data = new HashMap<>();

                    ArrayList<Map<String, Object>> members = new ArrayList<>();
                    members.add(Map.of("uid", userID, "name", user.getDisplayName()));
                    members.add(Map.of("uid", obj.getUid(), "name", obj.getName()));
                    data.put("members", members);
                    data.put("time", Timestamp.now());
                    data.put("recentMessage", "");

                    String newDoc = messageRef.document().getId();
                    messageRef.document(newDoc).set(data);

                    database.collection("users").document(obj.getUid()).update("messages", FieldValue.arrayUnion(newDoc));
                    database.collection("users").document(userID).update("messages", FieldValue.arrayUnion(newDoc));


                    mListener.goToPrivateChatFragment(obj, newDoc);
                }
            });

        }

        @Override
        public int getItemCount() {
            if (students != null) {
                return students.size();
            } else {
                return 0;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView studentName;
            private ImageView status;


            public ViewHolder(View view) {
                super(view);
                this.view = view;
                studentName = view.findViewById(R.id.nameOfStudent);
                status = view.findViewById(R.id.professorStatus);
            }
        }
    }
}