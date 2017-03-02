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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailListAdapter extends BaseAdapter {
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;
    private ImageButton favoriteButton;
    private FirebaseUser user;
    private String fFlag;

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
            favoriteButton = (ImageButton) convertView.findViewById(R.id.favoriteImageButton);
            // ログイン済みのユーザーを収録する
            user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                // ログインしていなければxxx
                Log.d("qaapp", "ログインしていません");
                favoriteButton.setVisibility(View.INVISIBLE);

            } else {
                // ログインしていれば
                Log.d("qaapp", "ログインしています。user="+user);
                favoriteButton.setVisibility(View.VISIBLE);
            }

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ボタンがクリックされた時に呼び出されます
                    Log.d("qaapp", "FavoriteImageボタンをタップしました");
                    fFlag = "add";
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
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference favoriteRef = dataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritePath);
        Log.d("qaapp", "FavoriteRef="+favoriteRef);
        //FavoriteRef=https://qaapp2-29922.firebaseio.com/users/13U6KoiV5EPO2JYDHYjk119sYgm2/favorites
        String questionUid = mQustion.getQuestionUid();
        favoriteRef = favoriteRef.child(questionUid);
        Log.d("qaapp", "FavoriteRef="+favoriteRef);
        //FavoriteRef=https://qaapp2-29922.firebaseio.com/users/13U6KoiV5EPO2JYDHYjk119sYgm2/favorites/-Kd0rxhtQM1XS49klNCM

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