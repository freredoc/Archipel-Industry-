package fr.archipel.industry;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Coquille WebView : charge le jeu (asset HTML autonome) hors-ligne.
 * Aucune connexion réseau requise — tout (React, logique, styles) est inline.
 */
public class MainActivity extends Activity {

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                 // sauvegardes localStorage persistantes
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);                           // ignore le zoom système, garde la mise en page

        // Le jeu reste dans la WebView ; les liens http(s) externes (ex. lien de
        // téléchargement d'une mise à jour) s'ouvrent dans le navigateur système.
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternally(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternally(Uri.parse(url));
            }
        });
        web.setBackgroundColor(0xFF0E1726);           // fond sombre pendant le chargement

        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    /** Ouvre les URLs http/https dans le navigateur système ; garde le reste dans la WebView. */
    private boolean openExternally(Uri uri) {
        String scheme = uri != null ? uri.getScheme() : null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        View d = getWindow().getDecorView();
        // On masque seulement la barre de statut (en haut). La barre de navigation
        // (les 3 boutons Android, en bas) reste visible et son espace est réservé :
        // le jeu se place AU-DESSUS, donc les boutons ne recouvrent plus l'UI du bas.
        d.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
