package com.example.sugiyama.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener, AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private int quantity = 0;
    private QuantityInfoAdapter adapter = null;
    private final SimpleDateFormat formatter = new SimpleDateFormat("kk:mm:ss");
    private final Timer timer = new Timer();
    private TextView timerTextView = null;
    private TimerTask timerTask = null;
    static final int RESULT_CODE_SUB_ACTIVITY = 1000;
    private static final int RESULT_BACK = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    @Override
    //
    protected void onResume() {
        super.onResume();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 現在時刻の表示
                        timerTextView.setText(formatter.format(new Date()));
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // バックグラウンドでの時刻表示の廃止
        timerTask.cancel();
    }

    // 数値の初期値、0を表示
    private void initView() {
        // Textview(palette)、textView(変数)4
        TextView quantityTextView = (TextView) findViewById(R.id.textview_quantity);
        quantityTextView.setText("" + quantity);

        //Button(palette)、plus_button(変数)
        // プラスボタン
        final Button plusButton = (Button) findViewById(R.id.button_plus);
        plusButton.setOnClickListener(this);

        // マイナスボタン
        final Button minusButton = (Button) findViewById(R.id.button_minus);
        minusButton.setOnClickListener(this);

        // 追加ボタン
        final Button addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(this);

        // クリアボタン
        final Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(this);

        // 選択された合計数量ボタン
        final Button selectButton = (Button) findViewById(R.id.button_select);
        selectButton.setOnClickListener(this);

        timerTextView = (TextView) findViewById(R.id.textview_time);

        ListView quantityInfoListView = (ListView) findViewById(R.id.listview_quantity_info);
        adapter = new QuantityInfoAdapter(MainActivity.this);
        adapter.setQuantityInfoList(getInfoList());
        quantityInfoListView.setAdapter(adapter);
        quantityInfoListView.setOnItemClickListener(this);
        quantityInfoListView.setOnItemLongClickListener(this);
    }

    // ボタンを押した時
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_plus:
                // プラスボタンが押下された場合
                // 加算される数、上限値
                calcQuantity(1, 9999);
                return;
            case R.id.button_minus:
                // マイナスボタンが押下された場合
                // 加算される数、下限値
                calcQuantity(-1, 0);
                return;
            case R.id.button_add:
                // 追加ボタンを押下された場合
                // 数量情報を1件追加
                addQuantityInfo();
                return;
            case R.id.button_clear:
                // クリアボタンを押下された場合
                // 全てのチェック項目をクリア
                clearList();
                return;
            case R.id.button_select:
                // 選択された合計数量ボタンを押下された場合
                // 合計数量を計算し表示
                selectList();
                return;
            default:
                return;
        }
    }


    private void calcQuantity(int addValue, int validatedValue) {

        // 数量が上限値または下限値に達していない場合
        if (validatedQuantity(validatedValue)) {
            // 数量を加算する
            addQuantity(addValue);
        } else {
            // 達している場合
            // メッセージを表示、加算しない
            Toast.makeText(MainActivity.this, R.string.toast_validated_quantity_ng, Toast.LENGTH_SHORT).show();
        }
    }

    // 選択された合計数量ボタン押下後の処理
    private void selectList() {
        int sum = 0;

        for (QuantityInfo info : getInfoList()) {
            // チェックボックスにチェックが入っている場合
            if (info.isSelected()) {
                // 数量を加算する
                sum += info.quantity;
            }
        }
        // トーストに結果表示
        String viewQuantity = String.valueOf(sum);
        Toast.makeText(MainActivity.this, viewQuantity, Toast.LENGTH_SHORT).show();
    }


    // クリアボタン押下後の処理
    private void clearList() {
        // リストを全てクリア
        getInfoList().clear();
        // リストの表示更新
        adapter.notifyDataSetChanged();
    }


    // 追加ボタン押下後の処理
    private void addQuantityInfo() {

        // 時刻を取得する
        TextView textView = (TextView) findViewById(R.id.textview_time);
        String time = textView.getText().toString();
        // コメントを取得する
        EditText edittext = (EditText) findViewById(R.id.edit_comment);
        String comment = edittext.getText().toString();
        // 数量情報を1件追加する
        QuantityInfo quantityInfo = new QuantityInfo();
        quantityInfo.setTime(time);
        quantityInfo.setComment(comment);
        quantityInfo.setQuantity(quantity);
        getInfoList().add(quantityInfo);
        // リストの表示更新
        adapter.notifyDataSetChanged();

    }

    // 数量が引数と同じでない場合、true
    private boolean validatedQuantity(int value) {
        // 数量と引数と同じ場合、false
        if (quantity == value) {
            return false;
        }

        return true;
    }

    // 数量を引数分加算し、結果を表示する
    private void addQuantity(int value) {
        // 引数を加算する
        quantity += value;
        // 計算結果を画面に表示する
        TextView textView = (TextView) findViewById(R.id.textview_quantity);
        textView.setText("" + quantity);
    }

    @Override
    // ビュー押下後の処理
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        QuantityInfo info = getInfoList().get(position);
        info.setSelected(!info.isSelected());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // 画面遷移
        QuantityInfo info = getInfoList().get(position);
        Intent intent = DetailActivity.getNewIntent(this, position);
        startActivityForResult(intent, RESULT_CODE_SUB_ACTIVITY);

        return true;
    }

    // AppBeanから情報取得
    private ArrayList<QuantityInfo> getInfoList(){
        AppBean appBean = (AppBean) getApplication();
        return appBean.list;
    }

}
