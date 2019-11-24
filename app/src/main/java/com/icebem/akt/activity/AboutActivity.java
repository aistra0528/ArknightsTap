package com.icebem.akt.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.IOUtil;

import org.json.JSONObject;

public class AboutActivity extends AppCompatActivity {
    private int i;
    private TextView desc_type;
    private LinearLayout container_version;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.fab).setOnClickListener(this::onClick);
        initView();
    }

    private void initView() {
        findViewById(R.id.container_comment).setOnClickListener(this::onClick);
        findViewById(R.id.container_discuss).setOnClickListener(this::onClick);
        findViewById(R.id.container_project).setOnClickListener(this::onClick);
        container_version = findViewById(R.id.container_version_state);
        container_version.setOnClickListener(this::onClick);
        findViewById(R.id.container_version_type).setOnClickListener(this::onClick);
        TextView desc_state = findViewById(R.id.txt_version_state_desc);
        desc_state.setText(BuildConfig.VERSION_NAME);
        desc_type = findViewById(R.id.txt_version_type_desc);
        manager = new PreferenceManager(this);
        desc_type.setText(manager.isPro() ? R.string.version_type_pro : R.string.version_type_lite);
    }

    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.action_donate);
                builder.setMessage(R.string.msg_donate);
                builder.setNeutralButton(R.string.action_donate_alipay, (dialog, which) -> {
                    try {
                        startActivity(Intent.parseUri("intent://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end", Intent.URI_INTENT_SCHEME));
                        Snackbar.make(view, R.string.info_donate_thanks, Snackbar.LENGTH_INDEFINITE).show();
                    } catch (Exception e) {
                        Snackbar.make(view, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
                    }
                });
                builder.setPositiveButton(R.string.no_way, null);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                break;
            case R.id.container_comment:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    Snackbar.make(view, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.container_discuss:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DN_OjFuCOkERq58jO2KoJEDD2a48vzB53")));
                } catch (Exception e) {
                    Snackbar.make(view, R.string.error_occurred, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.container_project:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IcebemAst/ArknightsTap")));
                break;
            case R.id.container_version_state:
                container_version.setOnClickListener(null);
                new Thread(this::checkVersionUpdate, "update").start();
                Snackbar.make(view, R.string.version_checking, Snackbar.LENGTH_INDEFINITE).show();
                break;
            case R.id.container_version_type:
                if (i == 15) {
                    i = 0;
                    manager.setPro(!manager.isPro());
                    desc_type.setText(manager.isPro() ? R.string.version_type_pro : R.string.version_type_lite);
                    Snackbar.make(view, R.string.version_type_changed, Snackbar.LENGTH_LONG).show();
                } else i++;
                break;
        }
    }

    private void checkVersionUpdate() {
        int id;
        String url = "https://github.com/IcebemAst/ArknightsTap/releases/latest";
        try {
            JSONObject json = new JSONObject(IOUtil.stream2String(IOUtil.fromWeb("https://api.github.com/repos/IcebemAst/ArknightsTap/releases/latest")));
            if (json.getString("tag_name").contains(BuildConfig.VERSION_NAME)) {
                id = R.string.version_latest;
            } else {
                id = R.string.version_update;
                url = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            }
        } catch (Exception e) {
            id = R.string.version_checking_failed;
        }
        int result = id;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        runOnUiThread(() -> {
            if (result == R.string.version_update)
                Snackbar.make(desc_type, result, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_update, v -> startActivity(intent)).show();
            else
                Snackbar.make(desc_type, result, Snackbar.LENGTH_LONG).show();
            container_version.setOnClickListener(v -> startActivity(intent));
        });
    }
}