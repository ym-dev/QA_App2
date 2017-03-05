package jp.techacademy.yusuke.murai.qa_app2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailListAdapter extends BaseAdapter {
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;
    private ImageButton favoriteButtonB;
    private ImageButton favoriteButtonG;
    private FirebaseUser user;
    private String fFlag;
    private DatabaseReference dataBaseReference;
    private DatabaseReference favoriteRef;
    private DataSnapshot dataSnapshot;

    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }
            String body = mQustion.getBody();
            String name = mQustion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

            //お気に入りボタン処理追加
            favoriteButtonB = (ImageButton) convertView.findViewById(R.id.favoriteImageButton);
            favoriteButtonB.setVisibility(View.INVISIBLE);

            favoriteButtonG = (ImageButton) convertView.findViewById(R.id.favoriteImageButtonG);
            favoriteButtonG.setVisibility(View.INVISIBLE);
            // ログイン済みのユーザーを収録する
            user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                // ログインしていなければxxx
                Log.d("qaapp", "ログインしていません");

            } else {
                // ログインしていれば
                Log.d("qaapp", "ログインしています。user="+user);

                dataBaseReference = FirebaseDatabase.getInstance().getReference();
                favoriteRef = dataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritePath).child(mQustion.getQuestionUid());
                Log.d("qaapp", "favoriteString="+favoriteRef.toString());

                favoriteRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                            HashMap map = (HashMap) dataSnapshot.getValue();
                            Log.d("qaapp", "map= "+map);

                            if (map != null){
                                Log.d("qaapp", "お気に入り選択済み");
                                favoriteButtonG.setVisibility(View.INVISIBLE);
                                favoriteButtonB.setVisibility(View.VISIBLE);    //Blueボタン表示
                            }else{
                                Log.d("qaapp", "お気に入り未選択");
                                favoriteButtonB.setVisibility(View.INVISIBLE);
                                favoriteButtonG.setVisibility(View.VISIBLE);   //Grayボタン表示(未選択状態)


                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
            }


            favoriteButtonG.setOnClickListener(new View.OnClickListener() { //未選択Gray色ボタンを押すとお気に入り登録しBlueボタンに
                @Override
                public void onClick(View v) {
                    // ボタンがクリックされた時に呼び出されます
                    Log.d("qaapp", "FavoriteImageBボタンをタップしました");

                    fFlag = "add";
                    setFavoriteFlag(fFlag);      //Firebaseに書き込み
                }
            });

            favoriteButtonB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ボタンがクリックされた時に呼び出されます
                    Log.d("qaapp", "FavoriteImageGボタンをタップしました");

                    fFlag = "del";
                    setFavoriteFlag(fFlag);      //Firebaseに書き込み
                }
            });



        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }


    private void setFavoriteFlag(String fFlag) {

        // FirebaseAuthのオブジェクトを取得する
        Log.d("qaapp", "setFavoriteFlag FavoriteRef="+favoriteRef);

        if (fFlag == "add"){
            Log.d("qaapp", "fFlag=add "+fFlag);
            Integer genre = mQustion.getGenre();


            Log.d("qaapp", "genre="+genre);
            Map<String, Integer> favoritedata = new HashMap<String, Integer>();
            favoritedata.put("genre", genre);
            favoriteRef.setValue(favoritedata);     //favoriteRefで定義したパスにgene:genre番号を保存

        } else if (fFlag =="del"){
            Log.d("qaapp", "fFlag=del "+fFlag);
            favoriteRef.removeValue();
            
        }
    }

    private void delFavoriteFlag(){

    }


}